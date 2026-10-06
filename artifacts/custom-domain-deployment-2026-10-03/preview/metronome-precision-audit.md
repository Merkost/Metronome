# Metronome Timing Precision — Audit & Best-Practice Solution

> Status: investigation complete, no code changed yet.
> Scope: timing accuracy of the click engine on Android + iOS.
> Method: 6-dimension parallel code audit (43 raw findings) → independent adversarial verification (37 survived, several severities corrected downward) → best-practice research against primary sources.

---

## 1. Executive summary

Perceived inaccuracy comes from **two root causes in the shared timing core — not from the platform audio code**:

1. **Open-loop `delay()` scheduler with no absolute-time anchor.** Each beat sleeps `delay(interval)` *relative to when the previous beat finished*. Per-beat work (StateFlow updates, `player.play()`, haptics) and `delay()` overshoot are never reclaimed, so error **accumulates forward with no catch-up**. Because `delay()` is a floor (it never fires early), the bias is one-sided — the metronome drifts **slow/late**, and the error grows for the whole session.

2. **Integer-division tempo quantization.** `60000 / bpm` (and the same expression in tap-tempo) truncates fractional milliseconds, so the interval is always rounded **down** → a deterministic **sharp/fast** offset at every BPM that doesn't divide 60000 evenly.

These two errors push in opposite directions and partially mask each other at "nice" tempos. **120 BPM is exact and drift-free** (500 ms divides evenly *and* there's little per-beat overhead), which is exactly why casual testing at round tempos looks fine while real practice at, say, 137 BPM feels off.

**The single highest-leverage fix resolves both at once:** schedule beats against a **monotonic clock with fractional intervals** — anchor a start instant and target beat *n* at `anchor + n × (60000.0 / bpm)`, sleeping `max(0, target − now)`. This is the software-metronome industry standard (the "look-ahead / two-clock" pattern). No platform audio rewrite is required to make the timing correct.

**One important correction from verification:** the raw audit inflated drift figures by ~10× (assuming the audio/haptic calls block for 5–40 ms; they don't — `SoundPool.play`, `scheduleBuffer(atTime=null)`, and `vibrate` are all non-blocking). The truncation error is sub-1 BPM (below the ~1% tempo just-noticeable-difference). The realistic dominant error is the **accumulating scheduler drift**, and the one defect that most degrades real-world UX today is a **concurrency bug**: an iOS audio-graph data race during sound switching.

---

## 2. How an accurate software metronome should be built (best practice)

Every robust software metronome (Web Audio, CoreAudio, JUCE, Oboe samples) converges on the same principle:

> **Never chain relative sleeps. Derive every event time from a single monotonic anchor, and decouple the timing clock from the work.**

There are three tiers of correctness. The app today is below Tier 0.

### Tier 1 — Absolute-time anchoring (the "two clocks" pattern)

From Chris Wilson's *A Tale of Two Clocks* (the canonical reference): use a **coarse waker** (a periodic loop) to look a little way into the future, but compute the **exact event time** from a precise clock. The waker may jitter; the event times never drift because each is `anchor + n × interval` from a fixed origin, not `previous + interval`.

```
secondsPerBeat = 60.0 / tempo           // fractional, NOT integer
nextBeatTime  += secondsPerBeat          // advance an absolute target
// sleep until just before nextBeatTime; the target is never "previous + sleep"
```

Key properties:
- **Tempo is read fresh each iteration** → live tempo changes with zero recalculation of the past.
- **Jitter is absorbed**, not accumulated: a late wake-up still fires the event at (or measured against) the correct absolute target.
- Drift is bounded to a single wake-up's jitter (single-digit ms, non-accumulating) instead of growing all session.

For Kotlin Multiplatform the correct clock is **`kotlin.time.TimeSource.Monotonic`** (works in `commonMain`; `System.nanoTime()` does not compile there). Anchor with `markNow()`, target with `anchor + n * interval`, sleep `delay(max(0, (target - anchor.elapsedNow())))`.

### Tier 2 — Sample-accurate scheduling (audio-clock, gold standard)

The look-ahead loop above still ultimately fires via a coroutine, so individual clicks land within ~coroutine-`delay()` resolution (≈ a few ms). To go tighter, schedule the clicks **on the audio render clock itself**, ahead of time, so the hardware places each sample exactly:

- **iOS (AVAudioEngine):** Apple's *HelloMetronome* pre-schedules buffers **ahead of real time** ("beats in reserve") with `scheduleBuffer(atTime:)` using an explicit `AVAudioTime` derived from the player's **sample time** (`sampleTime + n × samplesPerBeat`, `sampleRate`). A completion handler arms the *next* beat while the current one is still playing — there must always be a buffer in reserve or you get gaps. This yields ~22.7 µs precision at 44.1 kHz and is independent of UI/coroutine timing. Today the app calls `scheduleBuffer(atTime = null)` ("as soon as possible"), which throws away this capability and inherits all coroutine jitter.
- **Android:** SoundPool has no sample-accurate scheduling API. The equivalent upgrade is **Oboe** (wrapping AAudio with `PERFORMANCE_MODE_LOW_LATENCY`), feeding clicks into a callback that mixes by **sample count**, with buffers sized to a multiple of `PROPERTY_OUTPUT_FRAMES_PER_BUFFER` at `PROPERTY_OUTPUT_SAMPLE_RATE`. This is a larger, native rewrite.

### Tier 3 — Decouple the visual/haptic from the audio

UI beat indicators and haptics should be driven from the *scheduled* beat time (or the audio callback), never block the timing loop. Apple drives the UI from a delegate locked to audio sample time for exactly this reason.

**Pragmatic recommendation:** Tier 1 alone moves timing well below the human tempo-discrimination threshold and is a `commonMain`-only change. Tier 2 is a polish/headroom investment, justified only if measurement after Tier 1 shows audible per-click jitter on real devices.

---

## 3. Prioritized findings

All line numbers are at audit time on `feature/practice-tools-redesign`. Severities are the **verification-corrected** values.

### High

**H1 — iOS audio-graph data race between `play()` and `switchSound()`**
`shared/src/iosMain/.../engine/MetronomePlayerIos.kt:73-99, 120-155`
`play()` and the sound-switch path run as sibling coroutines on `Dispatchers.Default` (`MetronomeEngine.kt:36-40` vs `41-103`) with no mutex / serialization. `AVAudioEngine` / `AVAudioPlayerNode` / `AVAudioUnitVarispeed` are **not thread-safe**. `play()` mutating `varispeedNode.rate` and `scheduleBuffer` can race `switchSound()`'s `stop()` / `disconnectNodeOutput()` / `connect()` and the `@Volatile audioBuffer` reassignment.
**Impact:** one glitched/dropped/late beat per user-initiated sound change; rare AVFoundation crash worst case. This is the highest real-world-impact defect — a concurrency bug, not a timing-precision one.
**Fix:** confine all player-node access to a single serial dispatcher (`Dispatchers.Default.limitedParallelism(1)`); the `audioBuffer` reassign and node reconnect must share the critical section with the `play()` reads.

### Medium

**M1 — Open-loop `delay()` loop, no absolute-time anchor (core drift) — root cause**
`shared/src/commonMain/.../engine/MetronomeEngine.kt:64, 86, 95` (loop 69-102; dispatcher 26) — *merged from 6 findings*
Each beat is scheduled with a fresh relative `delay(interval)` after inline per-beat work. No `startTime`, no cumulative target, no catch-up. (Verified: the subdivision sub-delays telescope to exactly `interval`, so intra-beat drift is zero — the error is purely once-per-beat overhead + `delay()` overshoot, accumulating forward.)
**Impact:** drifts **slow/late** (delay never fires early). ≈ bpm × W where W = per-beat non-delay overhead ≈ 0.2–1.0 ms → **~24–120 ms/min at 120 BPM**; per-beat jitter ~1–5 ms typical, spiking 10–20 ms under load/GC.
**Fix:** Tier-1 absolute-time anchoring with `TimeSource.Monotonic.markNow()`; re-base the anchor on tempo / beat-count / count-in / subdivision changes. Do **not** move to `Dispatchers.Main` (UI-contended) or `Unconfined`.

**M2 — Integer-division tempo quantization (`60000 / bpm`)**
`shared/src/commonMain/.../model/MetronomeState.kt:3-4, 20-21` — *merged from 7 findings*
`val Int.interval: Int get() = 60000 / this` truncates fractional ms and drives every `delay()`. Always rounds **down** → systematically **sharp/fast**.
**Impact:** per-beat error **0 to ~0.98 ms** (worst near 177 BPM); max tempo bias **~+0.78 BPM at 219 BPM** (~0.36%); cumulative ~52 ms/min @ 121, ~80 ms/min @ 140. **Exactly zero** at divisor BPMs (48, 60, 75, 80, 96, 100, 120, 125, 150, 160, 200, …). 187/201 integer BPMs in 40–220 are affected, but each error is sub-1 BPM — below the audible tempo JND on its own; it matters mainly because it compounds the architecture.
**Fix:** do **not** switch to `Long`/`.toLong()` (truncates identically). Fold into M1 — compute targets from **un-truncated** `60000.0 / bpm` so fractional ms carry forward. If an integer interval is still needed anywhere, `roundToInt()` (halves worst case, removes the one-sided bias).

**M3 — Time-signature change inserts a one-interval silent gap**
`shared/src/commonMain/.../engine/MetronomeEngine.kt:41-54`
The 3-level nested `collectLatest` (`isPlaying` → `beats.size` distinct → `createBeatsSequence`) restarts the beat loop when beat count changes; `resumeWithDelay` then forces a deliberate `delay(interval)` before the new downbeat. All 6 time signatures have distinct beat counts, so **every** mid-play meter change triggers it.
**Impact:** exactly one interval of inserted silence (500 ms @ 120 BPM, 1000 ms @ 60 BPM), up to ~2× depending on phase when `collectLatest` abandons the in-flight delay.
**Fix:** remove/zero the `resumeWithDelay` delay branch so the new pattern's downbeat fires immediately. (Do **not** try to "resume mid-bar across meters" — ill-defined. Restarting the bar on a meter change is musically correct.)

### Low (correctness & polish — none change steady-state click accuracy)

- **L1 — Tap-tempo truncates instead of rounds** `MetronomeViewModel.kt:208,210`. `(60000 / medianInterval).toInt()` is Long/Long integer division; median-of-evens also truncates `.5`. One-sided 0–~1 BPM low bias, worst at **low** tempos. Fix: `(60000f / medianInterval).roundToInt().coerceIn(...)`. (`rhythm` is `Int` by design — don't refloat it.)
- **L2 — Count-in plays one extra beat** `MetronomeEngine.kt:56`. `for (remaining in beatsCount + 1 downTo 1)` yields 5 clicks for 4/4. Fix: `beatsCount downTo 1`.
- **L3 — Gradual-tempo step dispatched async (one-beat lag)** `MetronomeViewModel.kt:397`. `incrementGradualTempo()` wraps a thread-safe `_metronomeState.update{}` in `viewModelScope.launch{}` (Main) while the engine reads on Default → first beat of a new bar may use the old tempo (~10 ms typical). Fix: run the body inline; keep only the dismiss `delay` in a child coroutine.
- **L4 — Trainer duration estimate truncates (display only)** `TempoTrainerSheet.kt:583-584`. Cosmetic "≈ N min" underestimate. Fix: render mm:ss from `totalBars * beatsPerBar * 60f / averageBpm`.
- **L5 — Tap-tempo silently clamps out-of-range** `MetronomeViewModel.kt:210`. `coerceIn(40,220)` with no cue. Fix: reuse the existing `pulseOnChange` tint when raw ≠ clamped. (Don't widen the range — breaks slider/Pendulum/SavedTempo invariants.)
- **L6 — Subdivision leftover-ms spread** `MetronomeEngine.kt:86,95`. ≤1 ms between subdivisions, **zero accumulation** (telescopes exactly). **No change warranted** — the originally-proposed "fix" actually breaks the telescoping.
- **L7 — Android dropped click on sound switch** `MetronomePlayerAndroid.kt:35-39,55-59`. `play()` no-ops while `soundReady=false`; switch unloads then async-reloads. At most one silenced click per switch (cold-start race is effectively impossible). Fix: load-then-swap into a new `soundId`, flush at most one pending request on load-complete; dedupe the redundant startup `switchSound`.
- **L8 — Android robustness nits** `MetronomePlayerAndroid.kt`. No audio-focus handling (notifications/nav only duck; only a call/exclusive-audio silently advances) — request `AudioFocusRequest`, stop on `AUDIOFOCUS_LOSS`. `USAGE_MEDIA` is not a low-latency path (constant inaudible offset; real remedy is Oboe — Tier 2). `maxStreams=4` is never exceeded. Thread-priority elevation is unfixable as proposed and subsumed by M1.
- **L9 — Accent via pitch (`Beat.kt`, `rate=1.4f`)** — **design choice, not a bug.** Orthogonal to scheduling (zero timing error). The iOS `varispeedNode.rate` mutation is not an artifact source (the prior short click has decayed and is hard-stopped by `BufferInterrupts`). Keep; document.
- **L10 — Pendulum back-derives BPM from truncated interval** `Pendulum.kt:60` — feeds only the weight-bob position (sub-pixel), not displayed. ~0 user impact. If touched, pass `rhythm` directly.

---

## 4. Recommended fix plan

### Phase 1 — Minimal, high-leverage (kills both root causes; `commonMain` only)

1. **Anchor the beat loop to a monotonic clock with fractional intervals.** In `MetronomeEngine.kt`, capture `val anchor = TimeSource.Monotonic.markNow()` at loop start; target beat *n* at `n * (60000.0 / bpm)` ms from `anchor`, sleeping `delay(max(0, (target - anchor.elapsedNow()).toLong()))`. Re-base the anchor on tempo / beat-count / count-in / subdivision change. **Eliminates M1 and M2 simultaneously** (targets derive from un-truncated `60000.0 / bpm`). *Expected: phase drift bounded to per-wake jitter (single-digit ms, non-accumulating) instead of tens-to-hundreds of ms/min; tempo bias → 0.*
2. **Round, don't truncate, at any remaining integer BPM↔interval boundary** (`MetronomeState.kt:4`, tap-tempo `MetronomeViewModel.kt:208,210`): float the dividend + `roundToInt()`.
3. **Cheap correctness fixes:** remove the `resumeWithDelay` delay (M3), drop the count-in `+1` (L2), inline the gradual-tempo update (L3).

### Phase 2 — Robustness

4. **Serialize iOS audio-graph access** (H1) via a single-thread dispatcher around `play()` / `switchSound()` / `audioBuffer`. Highest real-world-impact fix.
5. **Android load-then-swap** for sound switching (L7) and **audio-focus handling** (L8).

### Phase 3 — Deeper upgrade (only if Phase 1 measurement proves insufficient)

6. **Sample-accurate scheduling (Tier 2).** iOS: `scheduleBuffer(atTime:)` driven by player sample time, pre-arming several future beats via the existing AVFoundation cinterop (no Swift render-callback bridge needed). Android: migrate SoundPool → Oboe/AAudio (`PERFORMANCE_MODE_LOW_LATENCY`) with the coroutine as a non-timing-critical feeder. *Expected: per-click jitter from ~ms toward sample accuracy (~22.7 µs @ 44.1 kHz). Note Phase 1 alone already clears the human discrimination threshold — Phase 3 is headroom, not a correctness requirement.*

---

## 5. References

- Chris Wilson — *A Tale of Two Clocks: Scheduling Web Audio with Precision* — https://web.dev/articles/audio-scheduling (the canonical look-ahead / two-clock pattern; 25 ms wake interval, 100 ms look-ahead, `nextNoteTime += 60/tempo`).
- Apple — *HelloMetronome* sample (AVAudioEngine, sample-accurate `scheduleBuffer(atTime:)`, beats-in-reserve, completion-handler chain, UI delegate locked to audio time) — https://developer.apple.com/library/archive/samplecode/HelloMetronome/Introduction/Intro.html
- Mehdi Samadi — *Making Sense of Time in AVAudioPlayerNode* (render vs node vs sample time; computing a future `AVAudioTime`) — https://medium.com/@mehsamadi/making-sense-of-time-in-avaudioplayernode-475853f84eb6
- Android — *Audio latency (NDK)* / *Design for reduced latency* (AAudio/Oboe, `PROPERTY_OUTPUT_FRAMES_PER_BUFFER`, sample-based timing, buffer-multiple jitter reduction) — https://developer.android.com/ndk/guides/audio/audio-latency · https://source.android.com/docs/core/audio/latency/design
- Kotlin — `TimeSource.Monotonic` (KMP-safe monotonic anchor) — https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.time/-time-source/-monotonic/ · `delay` rounds up to whole ms — https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/delay.html
