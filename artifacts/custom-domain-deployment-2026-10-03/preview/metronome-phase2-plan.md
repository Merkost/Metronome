# Metronome Timing Precision — Phase 2 Implementation Plan (Robustness)

> **Status: IMPLEMENTED 2026-06-15.** All three items (H1, L7, L8) are built and both platforms compile (`:androidApp:assembleDebug`, `:shared:linkDebugFrameworkIosSimulatorArm64`) with the 19 Phase-1 unit tests still green. Decisions taken on the §5 open questions: (1) **all focus losses stop** the metronome — no auto-resume; (2) **notifications stop the metronome too** ("stop, don't duck") — `setWillPauseWhenDucked(true)` makes the system report duck requests as a loss, and the listener treats `AUDIOFOCUS_LOSS` + `_TRANSIENT` + `_TRANSIENT_CAN_DUCK` all as stop (to instead let notifications keep the click playing, drop `setWillPauseWhenDucked` and the `_CAN_DUCK` branch); (3) **start anyway** if focus is denied; (4) dedupe via **Option B** (idempotent `switchSound` tracking `requestedSound`) on both players, not `drop(1)`; (5) iOS `@Volatile` dropped (single-thread-confined). The plan below is the original design of record.
>
> Status (original): planning only — no source changed.
> Scope: the three **robustness** defects from the audit, not timing precision. Phase 1 (the drift-free monotonic scheduler in `MetronomeEngine.kt`) is being implemented separately and is **out of scope** here.
> Items covered: **H1** (iOS audio-graph data race), **L7** (Android dropped click on sound switch), **L8** (Android audio focus & lifecycle).
> Source of record: `docs/metronome-precision-audit.md` §3 (findings) and §4 (Phase 2).

---

## 0. Pre-flight facts (verified against the repo)

| Fact | Value | Where |
| --- | --- | --- |
| Kotlin | 2.4.0 | `gradle/libs.versions.toml:1` |
| Coroutines | 1.11.0 | `gradle/libs.versions.toml` (`coroutines`) |
| Koin | 4.2.1 | `gradle/libs.versions.toml` |
| Android `minSdk` | **26** | `gradle/libs.versions.toml:6` |
| iOS deployment target | 16.0 / 17.0 | `iosApp/iosApp.xcodeproj/project.pbxproj` |
| Player interface | `MetronomePlayer` (5 **non-suspend** methods) | `shared/src/commonMain/.../engine/MetronomePlayer.kt:6-12` |
| Players injected as | Koin `single<MetronomePlayer>` | `AndroidModule.kt:19`, `IosModule.kt:16` |
| Engine constructed | Koin `single { MetronomeEngine(...).also { it.start() } }` | `CommonModule.kt:17-20` |
| Engine threading | `CoroutineScope(Dispatchers.Default)`, sibling `launch`es | `MetronomeEngine.kt:28, 38-43` |
| Sound-switch trigger | `viewModel.selectedSound.collectLatest { player.switchSound(it) }` | `MetronomeEngine.kt:40-42` |

Two facts drive the whole plan:

1. **`minSdk = 26`** → `AudioFocusRequest` (added in API 26) is available **unconditionally**. No `Build.VERSION.SDK_INT` gating, no deprecated `requestAudioFocus(listener, stream, hint)` fallback. (L8)
2. **`MetronomePlayer` methods are non-suspend** and are called from inside the engine's hot timing loop (`player.play(...)` at `MetronomeEngine.kt:65, 86, 97`). The interface contract **must not change** — the engine relies on `play()` returning immediately (fire-and-forget) so the monotonic scheduler's `delayUntil` math (`MetronomeEngine.kt:127-130`) stays the timing authority. Any serialization we add must be **internal to the iOS impl** and **non-blocking from the caller's perspective**. (H1)

### Kotlin/Native concurrency model (confirmed)

The new K/N memory manager (default since Kotlin 1.7.20, fully matured in 2.x) **removes `freeze()` entirely** — there is no freezing, no `@SharedImmutable`/`@ThreadLocal` dance, and objects may be shared and mutated across threads/dispatchers like on the JVM. `Dispatchers.Default` on Apple targets is backed by a global queue dispatched to a worker pool, so two coroutines launched on it **can run on different threads concurrently** — which is exactly the H1 race. `kotlinx.coroutines` `limitedParallelism(1)` and `newSingleThreadContext` are both available on K/N in 1.11.0 and give true single-thread (single-`Worker`) confinement. No freezing concerns apply to the captured `this@MetronomePlayerIos` or the `AVAudio*` cinterop objects. (Sources in §6.)

---

## H1 — iOS audio-graph data race (HIGH — highest real-world impact)

### H1.1 Problem recap (with file:line)

`MetronomePlayerIos` holds the AVAudioEngine graph in four `@Volatile` fields (`MetronomePlayerIos.kt:22-29`): `audioEngine`, `playerNode`, `varispeedNode`, `audioBuffer`.

- **`play()`** (`MetronomePlayerIos.kt:73-99`) reads `audioBuffer`/`playerNode`, **mutates `varispeedNode.rate`** (line 77), sets `player.volume`/`pan`, may call `player.play()`, and calls `player.scheduleBuffer(...)` (line 93).
- **`switchSound()`** (`MetronomePlayerIos.kt:120-155`) reads a new file, then **`player.stop()` / `engine.stop()` / `disconnectNodeOutput()` ×2 / `connect()` ×2** (lines 138-144) and **reassigns `audioBuffer`** (line 146), then restarts the engine and player.

These run as **sibling coroutines on `Dispatchers.Default`** with no serialization:
- The hot loop calls `player.play(...)` from `MetronomeEngine.kt:65, 86, 97`.
- The switch path is the separate `launch { selectedSound.collectLatest { player.switchSound(it) } }` at `MetronomeEngine.kt:39-43`.

`@Volatile` only makes each individual reference read/write atomic. It does **not** serialize the multi-step graph mutations. `AVAudioEngine` / `AVAudioPlayerNode` / `AVAudioUnitVarispeed` are **not thread-safe**. So a `scheduleBuffer` (play) can interleave with a `disconnectNodeOutput`/`connect` (switch), or `play()` can read `audioBuffer` mid-reassign and schedule a buffer whose format no longer matches the freshly-reconnected node.

**Impact:** one glitched / dropped / late beat per user-initiated sound change; a rare AVFoundation crash worst case (scheduling onto a disconnected node, or format mismatch). This is the single highest real-world-impact defect in the audit — a concurrency bug, not a precision one.

### H1.2 Design — single-thread confinement (actor-style), internal to the iOS impl

**Decision: confine ALL player-node access to one serial dispatcher** created from `Dispatchers.Default.limitedParallelism(1)`. Every public method body runs on that dispatcher, so `initialize()`, `play()`, `switchSound()`, `stop()`, `release()`, and every `audioBuffer`/node access share **one critical section**. There is exactly one thread that ever touches the graph → no race is possible, and `@Volatile` becomes unnecessary (kept harmless, or dropped — see H1.6).

**Why not the alternatives:**

| Option | Verdict | Reason |
| --- | --- | --- |
| `Mutex` (coroutines) | Rejected | `Mutex.withLock` is `suspend`; the interface methods are non-suspend. Would force `runBlocking` at every `play()` call inside the timing loop → blocks the scheduler thread on lock contention during a switch = the very jitter we are removing. |
| `NSLock` / `@Synchronized` | Rejected | Makes `play()` **block** on the lock while `switchSound()` holds it through file I/O + engine restart (tens of ms). That stall lands directly in the timing loop → a late/dropped beat — trading a rare race for a guaranteed hiccup. Also re-entrancy-hostile. |
| `newSingleThreadContext("...")` | Viable but rejected | Allocates a **dedicated OS thread** for the lifetime of the player `single`. `limitedParallelism(1)` reuses the existing `Dispatchers.Default` worker pool and needs no `close()`. Functionally equivalent confinement; lower footprint and no lifecycle to manage. |
| **`Dispatchers.Default.limitedParallelism(1)` + `launch` (fire-and-forget)** | **Chosen** | Non-blocking at the call site (caller returns immediately, preserving the non-suspend contract and the scheduler's timing authority), true serial execution, no extra thread, no `Mutex`/`runBlocking`. Matches the audit's recommendation verbatim ("`Dispatchers.Default.limitedParallelism(1)`", audit §3 H1). |

**The suspend-vs-blocking resolution:** the interface stays non-suspend. Internally, the iOS impl owns a `CoroutineScope` on the serial dispatcher and each public method does `scope.launch { ... }`. The caller (`MetronomeEngine`) sees an immediate return exactly as today; the real work is enqueued onto the single confinement thread and executes in submission order. Because a single `Dispatchers.Default` worker processes the queue FIFO, `play` and `switchSound` are **serialized by enqueue order**, never overlapping.

### H1.3 Proposed structure (Kotlin, no comments per project rule)

```kotlin
@OptIn(ExperimentalForeignApi::class, ExperimentalCoroutinesApi::class)
class MetronomePlayerIos : MetronomePlayer {

    private val audioDispatcher = Dispatchers.Default.limitedParallelism(1)
    private val scope = CoroutineScope(audioDispatcher + SupervisorJob())

    private var audioEngine: AVAudioEngine? = null
    private var playerNode: AVAudioPlayerNode? = null
    private var varispeedNode: AVAudioUnitVarispeed? = null
    private var audioBuffer: AVAudioPCMBuffer? = null

    override fun initialize(initialSound: ClickSound) {
        scope.launch { initializeInternal(initialSound) }
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        scope.launch { playInternal(beat, stereoLeft, stereoRight) }
    }

    override fun stop() {
        scope.launch { playerNode?.stop() }
    }

    override fun switchSound(sound: ClickSound) {
        scope.launch { switchSoundInternal(sound) }
    }

    override fun release() {
        scope.launch {
            playerNode?.stop()
            audioEngine?.stop()
            audioEngine = null
            playerNode = null
            varispeedNode = null
            audioBuffer = null
        }
    }

    private fun initializeInternal(initialSound: ClickSound) { /* current initialize() body */ }
    private fun playInternal(beat: Beat, stereoLeft: Float, stereoRight: Float) { /* current play() body */ }
    private fun switchSoundInternal(sound: ClickSound) { /* current switchSound() body */ }

    private fun soundFileInfo(sound: ClickSound): Pair<String, String> = when (sound) {
        ClickSound.WOOD -> "wood" to "mp3"
        ClickSound.CLICK -> "click" to "mp3"
        ClickSound.CLASSIC -> "metronome" to "wav"
    }
}
```

The `*Internal` functions are the **verbatim** current bodies of `initialize`/`play`/`switchSound` (lines 31-71, 73-99, 120-155), moved into private functions and now guaranteed to run only on `audioDispatcher`. No graph logic changes — only where it runs.

### H1.4 Exact changes — `shared/src/iosMain/.../engine/MetronomePlayerIos.kt`

1. **Add imports:** `kotlinx.coroutines.CoroutineScope`, `Dispatchers`, `SupervisorJob`, `launch`, `ExperimentalCoroutinesApi`. (`kotlin.concurrent.Volatile` import can be removed — see step 4.)
2. **Add fields** (top of class, before the graph fields):
   ```kotlin
   private val audioDispatcher = Dispatchers.Default.limitedParallelism(1)
   private val scope = CoroutineScope(audioDispatcher + SupervisorJob())
   ```
3. **Convert each public method** to a one-line `scope.launch { ... }` delegating to a private `*Internal` worker holding the existing body (lines as in H1.3). `stop()` and `release()` bodies move inside the `launch` directly.
4. **Drop the four `@Volatile` annotations** on `audioEngine` / `playerNode` / `varispeedNode` / `audioBuffer` (`MetronomePlayerIos.kt:22-29`). They are now only ever touched on the single confinement thread, so `@Volatile` is dead weight. (Keeping them is harmless but misleading — removing documents the new invariant "single-thread-confined".)
5. **Class annotation:** add `ExperimentalCoroutinesApi` to the existing `@OptIn(ExperimentalForeignApi::class)` (`limitedParallelism` is marked experimental in 1.11.0).

No other file changes for H1. The interface (`MetronomePlayer.kt`) and the engine (`MetronomeEngine.kt`) are **untouched**.

### H1.5 Edge cases & thread-safety reasoning

- **In-flight `scheduleBuffer` during a switch.** Because everything is serialized FIFO on one thread, a `play` enqueued *before* a `switch` runs fully (its `scheduleBuffer` completes) before the switch's `disconnectNodeOutput` runs. A `play` enqueued *after* the switch runs against the already-reconnected graph and the new `audioBuffer`. There is **no interleaving window** — the previous code's race (read `audioBuffer`, then have it reassigned mid-`play`) is structurally impossible.
- **`AVAudioPlayerNodeBufferInterrupts`** (the existing `play` option, line 96) already hard-stops any currently-playing buffer when a new one is scheduled. Combined with serialization, the "previous click still ringing when we switch" case is bounded and correct (consistent with audit L9: the prior short click has decayed / is hard-stopped).
- **Reentrancy.** `limitedParallelism(1)` is not reentrant-recursive, but no `*Internal` method calls another public method, so there is no self-deadlock risk. (If one ever needs to, it must call the `*Internal` peer directly, never the public `scope.launch` wrapper.)
- **Ordering vs `initialize`.** `MetronomeEngine.start()` calls `player.initialize(sound)` (`MetronomeEngine.kt:37`) then launches the collectors. With confinement, `initialize` is now async-enqueued. The very first `play`/`switchSound` are enqueued *after* `initialize` on the same FIFO thread, so `initialize` always completes first. The pre-existing `audioBuffer ?: return` / `playerNode ?: return` guards in `play` (lines 74-75) already cover any "called before init finished" case gracefully (no-op, no crash).
- **`release()` then late `play()`.** A `play` enqueued after `release` finds the nulled fields and no-ops via the existing guards. Safe.
- **Scope lifecycle.** `SupervisorJob` ensures one failed `launch` (e.g. a transient AVFoundation error inside a `runCatching`-free path) does not cancel the scope and kill future clicks. The player is a process-lifetime Koin `single`, so the scope is never explicitly cancelled (matches today's behavior where the player object lives forever).
- **K/N specifics.** Confirmed: no freezing in the new MM; sharing `this` and the cinterop objects across the enqueued lambda is legal and mutation-safe. The single worker gives a happens-before edge between successive tasks, so field writes in `switchSoundInternal` are visible to the next `playInternal` without `@Volatile`.

### H1.6 Interaction with Phase 1

Phase 1 owns timing via `delayUntil` against `TimeSource.Monotonic` (`MetronomeEngine.kt:127-130`). H1 does **not** touch timing: `play()` still returns immediately to the loop. The only change is *where* the AVFoundation work runs (one dedicated thread vs. the Default pool). Net effect on timing: **neutral-to-positive** — moving graph work off whatever Default worker the scheduler happens to use slightly reduces the chance of the scheduler thread being delayed by a heavy `switchSound`. There is one nuance: `play` is now enqueued rather than executed inline, adding a sub-millisecond hand-off latency that is **constant** (not accumulating) and **identical for every click**, so it shifts all clicks by a fixed epsilon and introduces **zero relative jitter** — invisible musically and irrelevant to the monotonic anchor.

### H1.7 Verification

- **Build:** `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` must compile (watch for the `limitedParallelism` opt-in and the new imports).
- **Manual (device or simulator):**
  1. Start the metronome at a non-round tempo (e.g. 137 BPM).
  2. While playing, rapidly toggle sound (wood ↔ click ↔ classic) ~20× in a few seconds.
  3. Expected: no crash, no audible gap longer than one beat, clicks resume on the new sound cleanly. Pre-fix, this is the scenario that drops/glitches a beat or crashes.
  4. Toggle sound while **stopped**, then start — first downbeat uses the new sound.
  5. Background/foreground the app mid-switch; confirm no crash.
- **Stress (optional):** loop sound-switch every 50 ms for a minute under Instruments (Time Profiler / Thread sanitizer is not available for K/N, but Instruments' "Hangs" + audio glitch listening suffices).

---

## L7 — Android dropped click on sound switch (LOW)

### L7.1 Problem recap (with file:line)

`MetronomePlayerAndroid` (`MetronomePlayerAndroid.kt`):

- `play()` (lines 35-39) **no-ops while `soundReady == false`**: `if (soundReady) soundPool?.play(...)`.
- `switchSound()` (lines 55-59) does: `soundReady = false` → `soundPool?.unload(soundId)` → `soundId = pool.load(...)`. The new sound loads **asynchronously**; `soundReady` flips back true only in `setOnLoadCompleteListener` (lines 28-30) on `status == 0`.

So between the `unload` and the load-complete callback, **every `play()` is silently dropped** — at most one missed click per switch at normal tempos (the load is fast), but audible as a skipped beat.

Secondary issue (the "dedupe" in audit L7): `MetronomeEngine.start()` calls `player.initialize(initialSound)` (`MetronomeEngine.kt:37`), then immediately `viewModel.selectedSound.collectLatest { player.switchSound(it) }` (`MetronomeEngine.kt:40-42`). `collectLatest` fires once with the **current** value on subscription → an immediate, redundant `switchSound(initialSound)` that unloads the just-loaded sound and reloads the identical resource, opening a needless drop window at startup.

### L7.2 Design — load-then-swap into a new soundId + dedupe

**Core idea:** never unload the playing sound until the replacement is fully loaded. Load the new resource into a **new** `soundId`, keep playing the **old** `soundId`, and on the load-complete callback (`status == 0`) **atomically swap** the active id and unload the old one. No `soundReady=false` gap.

Add a small pending-flush: if a beat *was* requested during the (now much shorter, and only-at-cold-start) window where no sound is ready at all, flush **at most one** queued play on load-complete, so a click that landed during cold start still sounds (deferred by a few ms) rather than vanishing. This keeps the "no blocking waits" rule (no ANR) — everything is callback-driven.

**State model (replacing the current 3 fields):**

```kotlin
@Volatile private var soundPool: SoundPool? = null
@Volatile private var activeSoundId: Int = 0          // currently playable; 0 = none yet
@Volatile private var pendingSoundId: Int = 0         // loading; 0 = none in flight
@Volatile private var pendingPlay: PendingPlay? = null // at most one deferred cold-start click
```

`activeSoundId == 0` replaces `soundReady == false` as the "nothing playable yet" signal (`SoundPool.load` never returns 0; valid ids are ≥ 1).

**Load-complete listener (the swap point):**

```kotlin
pool.setOnLoadCompleteListener { _, sampleId, status ->
    if (status != 0) return@setOnLoadCompleteListener
    val replacing = activeSoundId
    if (sampleId == pendingSoundId) {
        activeSoundId = sampleId
        pendingSoundId = 0
        if (replacing != 0 && replacing != sampleId) pool.unload(replacing)
    } else if (activeSoundId == 0) {
        activeSoundId = sampleId
    }
    pendingPlay?.let { p ->
        pendingPlay = null
        pool.play(sampleId, p.left, p.right, 1, 0, p.rate)
    }
}
```

**`play()`** — play the active sound if there is one; otherwise stash the latest request (overwriting any older pending → "at most one"):

```kotlin
override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
    val pool = soundPool ?: return
    val id = activeSoundId
    if (id != 0) {
        pool.play(id, stereoLeft, stereoRight, 1, 0, beat.rate)
    } else {
        pendingPlay = PendingPlay(stereoLeft, stereoRight, beat.rate)
    }
}
```

**`switchSound()`** — load into a new id, never unload the active one here:

```kotlin
override fun switchSound(sound: ClickSound) {
    val pool = soundPool ?: return
    val newId = pool.load(context, soundResource(sound), 1)
    pendingSoundId = newId
}
```

The old `activeSoundId` keeps playing every click until the callback swaps it; the old buffer is unloaded **inside** the callback, after the new one is ready → **zero drop window** during a warm switch.

**Dedupe the startup switch** (audit L7). Two equally valid options — recommend **Option A** (engine-side, also benefits iOS implicitly by avoiding a redundant graph rebuild):

- **Option A (engine, recommended):** in `MetronomeEngine.start()` (`MetronomeEngine.kt:40-42`), skip the first emission since `initialize` already loaded it:
  ```kotlin
  viewModel.selectedSound
      .drop(1)
      .collectLatest { sound -> player.switchSound(sound) }
  ```
  Requires `import kotlinx.coroutines.flow.drop`. **Caveat:** `drop(1)` is correct only because `selectedSound` is a hot/stateful flow that re-emits its current value first (it is `appDatastore.selectedSound`, a DataStore-backed `Flow` mapped through `stateIn`-like collection). Verify the first emission always equals `viewModel.selectedSound.value` passed to `initialize` (`MetronomeEngine.kt:36-37`). If there is any chance the persisted value changes between `.value` read and first collect, prefer Option B.
- **Option B (player, defensive, platform-local):** make `switchSound` a no-op when asked for the sound that is already active. On Android, track the active `ClickSound` and early-return if unchanged; on iOS, same guard avoids a needless engine stop/reconnect. This is robust regardless of flow semantics and helps **both** platforms, at the cost of one extra field per impl.

**Recommendation:** ship **Option B** (idempotent `switchSound`) as the primary dedupe because it is platform-agnostic, defends against any future double-emit, and shrinks H1's switch frequency too; optionally also `drop(1)` (Option A) as belt-and-suspenders. Decision flagged in §5.

### L7.3 Exact changes — `shared/src/androidMain/.../engine/MetronomePlayerAndroid.kt`

1. Replace fields `soundId`/`soundReady` with `activeSoundId`/`pendingSoundId`/`pendingPlay` (+ a `private data class PendingPlay(val left: Float, val right: Float, val rate: Float)`).
2. In `initialize()` (lines 19-33): set the load-complete listener to the swap logic above; `pendingSoundId = pool.load(context, soundResource(initialSound), 1)` (so the initial sound flows through the same swap path → `activeSoundId` set on completion). Optionally store `activeClickSound = initialSound` for Option B.
3. Rewrite `play()`, `switchSound()` as above. `stop()` stays a no-op (line 41). `release()` (lines 43-47): release pool, null fields, `activeSoundId = 0`, `pendingSoundId = 0`, `pendingPlay = null`.
4. If Option B: add `@Volatile private var activeClickSound: ClickSound? = null`; in `switchSound`, `if (sound == activeClickSound) return` then set it; update it in the load-complete swap.

If Option A is also taken: edit `MetronomeEngine.kt:40-42` to `.drop(1)` (+ import). No interface change.

### L7.4 Edge cases & reasoning

- **Rapid double switch (A→B→C before either loads).** Each `switchSound` overwrites `pendingSoundId` and starts a new load. The listener only swaps when `sampleId == pendingSoundId`, so a stale B-load completing after C was requested is ignored for the active swap — but B's buffer is now **leaked** (loaded, never unloaded). Mitigation: when overwriting `pendingSoundId` in `switchSound`, `unload` the previous pending id if it was still in flight: `val prev = pendingSoundId; if (prev != 0) pool.unload(prev); pendingSoundId = pool.load(...)`. (Unloading an id that is mid-load is safe — SoundPool just cancels it.) Add this to avoid the leak.
- **Cold start (`activeSoundId == 0`) with a beat requested.** `play()` stashes one `pendingPlay`; the load-complete listener flushes it once. At most one click is *delayed* (not dropped), and only at the very first load — the audit notes this cold-start race is "effectively impossible" in practice, so this is pure insurance.
- **`status != 0` (load failure).** Listener returns early; `activeSoundId` keeps the old sound → metronome keeps clicking on the previous sound rather than going silent. Correct degradation. (Optionally `Cedar.e` it, matching the iOS impl's logging style.)
- **No blocking / ANR.** Nothing waits; `switchSound` and `play` return immediately; all coordination is on SoundPool's own callback thread. Satisfies the audit's "avoid blocking waits (ANR risk)".
- **Thread visibility.** `activeSoundId`/`pendingSoundId`/`pendingPlay` are written on the SoundPool callback thread and read on the engine's Default thread → keep `@Volatile` (unlike iOS, there is no single-thread confinement here; SoundPool owns its callback thread). `@Volatile` is sufficient because each is a single independent field and the swap is a lone volatile write that publishes the already-loaded buffer.

### L7.5 Interaction with Phase 1 / H1

Independent of Phase 1 timing. Reduces the number of real `switchSound` graph rebuilds (via Option B idempotency), which also lightens H1's serialized work on iOS. No timing impact: `play()` remains non-blocking and immediate.

### L7.6 Verification

- **Build:** `./gradlew :androidApp:assembleDebug`.
- **Manual:**
  1. Play at 137 BPM, switch sound repeatedly while playing → **no skipped beat** (pre-fix drops ~1 click per switch).
  2. Switch sound very rapidly (A→B→C fast) → no crash, ends on C, no runaway memory (verify with a quick heap check that ids are not leaking — the pending-unload mitigation).
  3. Cold start: tap play immediately on app launch → first click still sounds.
  4. Confirm startup no longer double-loads: add a temporary `Cedar.d` count in `switchSound` during dev (remove before commit — **no comments / no stray logging in final**), expect zero switch calls before the user changes the sound (Option A) or zero *effective* switches (Option B).

---

## L8 — Android audio focus & lifecycle robustness (LOW)

### L8.1 Problem recap (with file:line)

`MetronomePlayerAndroid` never requests audio focus. `initialize()` builds the `SoundPool` with `USAGE_MEDIA` (`MetronomePlayerAndroid.kt:22-27`) but no `AudioFocusRequest`. Consequences (audit L8):

- A phone call or any **exclusive-audio** app can take focus and the metronome keeps "advancing" **inaudibly** — the user sees the UI ticking but hears nothing, or worse, keeps practicing against silence.
- Notifications/navigation only *duck* (which is fine), but the app has no hook to react to a true `AUDIOFOCUS_LOSS`.

`MainActivity` sets `volumeControlStream = AudioManager.STREAM_MUSIC` (`MainActivity.kt:36`) — the only existing audio-system touch. The metronome is driven by `_metronomeState.playing` via `viewModel.isPlaying` (`MetronomeViewModel.kt:115-116`); the canonical stop path is `viewModel.onStopClicked()` (`MetronomeViewModel.kt:122-124`), already used by the service's notification stop action (`MetronomeService.kt:43`).

### L8.2 Design — request transient focus on start, abandon on stop, stop on loss

**Behavior decisions (metronome-specific):**

- **Gain type:** `AUDIOFOCUS_GAIN` (not `_TRANSIENT`). The metronome can run for a long practice session, and the audit's "stop, don't duck" stance means we want full focus while running. (Transient is for <45 s clips per Google's guidance; a metronome session is open-ended.) Either gain works; **`AUDIOFOCUS_GAIN`** is the better fit for "I am the primary audio now."
- **`setWillPauseWhenDucked(true)`** → we do **not** want to duck the click (a quieter click is a worse metronome, not an acceptable one). With this flag the system sends `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` as a transient loss instead of auto-ducking, so we treat duck-requests the same as a transient loss.
- **`AUDIOFOCUS_LOSS` (permanent):** route through the existing stop path — call `onAudioFocusLost()` which triggers `viewModel.onStopClicked()`. Do **not** silently continue.
- **`AUDIOFOCUS_LOSS_TRANSIENT` / `_CAN_DUCK`:** a metronome should **stop/pause, not duck**. Simplest correct behavior: **stop** (same as permanent). There is no musically meaningful "resume mid-bar after a 2-second call-waiting beep," and auto-resume after transient loss is a notorious source of surprise playback. **Decision: treat all losses as stop.** (Flagged in §5 as a product choice — auto-resume-after-transient is the only alternative and is explicitly *not* recommended for a metronome.)
- **`AUDIOFOCUS_GAIN` (regained):** no auto-restart (since we stopped). User presses play again. Keeps behavior predictable.

**Where it lives — the seam.** Audio focus is Android-only and must call into the shared `MetronomeViewModel.onStopClicked()`. Two placement options:

| Placement | Pros | Cons |
| --- | --- | --- |
| **Inside `MetronomePlayerAndroid`** | Co-located with `SoundPool`/`AudioManager` (already imports `AudioManager`); `initialize`/`release` are natural request/abandon points; zero new wiring. But it cannot call `viewModel.onStopClicked()` (the player has no ViewModel ref, and shouldn't). | Needs a stop callback injected. |
| **A new `AndroidAudioFocus` helper + the interface+Koin pattern** | Matches CLAUDE.md ("new platform features: interface + Koin"); testable; keeps the player pure-audio. | One more class + Koin entry. |

**Recommendation:** introduce a tiny **`AudioFocusController` interface in `commonMain`** with a no-op default implementation, mirroring the existing `LiveActivityController` / `NoopLiveActivityController` pattern (`AndroidModule.kt:23`, `IosModule.kt` has none → iOS gets the no-op). The Android impl owns the `AudioManager`/`AudioFocusRequest` and is **driven by the engine's play/stop**, calling back to stop on loss. This honors the project's interface+Koin convention and keeps the player focused on sound.

**Interface (commonMain):**

```kotlin
interface AudioFocusController {
    fun requestFocus(): Boolean
    fun abandonFocus()
    fun setOnFocusLost(onLost: () -> Unit)
}

class NoopAudioFocusController : AudioFocusController {
    override fun requestFocus(): Boolean = true
    override fun abandonFocus() {}
    override fun setOnFocusLost(onLost: () -> Unit) {}
}
```

**Android impl (`androidMain`):**

```kotlin
class AndroidAudioFocusController(context: Context) : AudioFocusController {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var onLost: (() -> Unit)? = null

    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onLost?.invoke()
        }
    }

    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(listener)
        .build()

    override fun setOnFocusLost(onLost: () -> Unit) { this.onLost = onLost }

    override fun requestFocus(): Boolean =
        audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    override fun abandonFocus() {
        audioManager.abandonAudioFocusRequest(request)
    }
}
```

The `AudioAttributes` mirror the existing `SoundPool` builder (`MetronomePlayerAndroid.kt:23-26`) so focus and output describe the same stream. No `Build.VERSION` gate needed (`minSdk = 26`).

**Driving it from the engine (commonMain, platform-neutral).** The engine already toggles on `isPlaying` (`MetronomeEngine.kt:44-122`). Inject `AudioFocusController` into `MetronomeEngine` and:

- On transition to playing: `audioFocus.requestFocus()` (before/with `player.initialize` is already done once; request on each start).
- On transition to not playing (the `else` branch at `MetronomeEngine.kt:118-122`): `audioFocus.abandonFocus()`.
- Wire the loss callback once in `start()` to stop: `audioFocus.setOnFocusLost { viewModel.onStopClicked() }`. (The engine already holds `viewModel`.)

Because `onStopClicked()` flips `_metronomeState.playing = false` (`MetronomeViewModel.kt:122-124`), the existing `isPlaying` collector cleanly tears down (calls `player.stop()`, resets index) **and** the service hides its notification (`MetronomeService.kt:55-60`). One code path, fully reused.

**Important — never double-request (audit/Google rule).** Request focus only on the **false→true** edge and abandon only on **true→false**. The `collectLatest` on `isPlaying` already fires once per state change, so requesting in the `if (playing)` entry and abandoning in the `else` is naturally edge-triggered. Guard against a redundant request if `requestFocus()` is somehow re-entered (idempotent: `AudioManager` de-dupes identical `AudioFocusRequest` objects, but we still only call on the edge).

### L8.3 Exact changes

1. **New file** `shared/src/commonMain/.../platform/AudioFocusController.kt` — the `interface` + `NoopAudioFocusController` (above).
2. **New file** `shared/src/androidMain/.../platform/AndroidAudioFocusController.kt` — the Android impl (above).
3. **`shared/src/androidMain/.../di/AndroidModule.kt`** — add `single<AudioFocusController> { AndroidAudioFocusController(androidContext()) }`.
4. **`shared/src/iosMain/.../di/IosModule.kt`** — add `single<AudioFocusController> { NoopAudioFocusController() }`.
5. **`shared/src/commonMain/.../di/CommonModule.kt:19`** — add a `get()` for the new dependency: `MetronomeEngine(get(), get(), get(), get()).also { it.start() }`.
6. **`shared/src/commonMain/.../engine/MetronomeEngine.kt`** — add constructor param `private val audioFocus: AudioFocusController`; in `start()` call `audioFocus.setOnFocusLost { viewModel.onStopClicked() }`; in the `isPlaying` collector request on entry and abandon in the `else` branch (`MetronomeEngine.kt:44-45` and `118`).

No `MetronomeService` change is strictly required (loss → `onStopClicked` → `isPlaying=false` → service hides notification automatically). The service is a notification host only (`MetronomeService.kt:51-53`).

### L8.4 Edge cases & reasoning

- **Focus denied (`requestFocus()` false).** Rare (another app holds exclusive focus). Decision: **still allow play** but the user will hear the other app duck/stop per system policy — or, stricter, refuse to start. Recommend **allow play** (don't surprise the user by ignoring their tap); log a debug line. Flagged in §5.
- **Permanent vs transient loss.** Both map to stop (decision above). Avoids the auto-resume foot-gun. The listener treats `_CAN_DUCK` as loss because `setWillPauseWhenDucked(true)` asked the system *not* to duck us.
- **Background play setting.** The app has `backgroundPlay` (`MainActivity.kt:27, 70`). Audio focus is orthogonal — if the user enabled background play and a call comes in, stopping is still correct (you can't practice over a call). No conflict.
- **Abandon on `release()`?** The engine abandons on stop; the player's `release()` is process teardown. Ensure abandon also happens if the engine is torn down while playing — `abandonFocus()` is idempotent, so calling it in both the `else` branch and (optionally) engine teardown is safe.
- **Double-stop.** `onStopClicked()` is idempotent (`copy(playing = false)`), so a focus loss arriving when already stopped is harmless.

### L8.5 Interaction with Phase 1 / other Phase 2 items

Independent of timing (Phase 1). Shares the stop path with the existing service notification action — no new stop semantics introduced. Independent of H1 (iOS gets the no-op controller). Independent of L7.

### L8.6 Verification

- **Build:** `./gradlew :androidApp:assembleDebug`.
- **Manual:**
  1. Start metronome → in another app start music / a voice memo recording (exclusive audio) → metronome **stops** (UI shows stopped), not silently advancing.
  2. Start metronome → receive/place a phone call → metronome stops; after the call, it stays stopped (no surprise auto-resume).
  3. Start metronome → trigger a notification sound → metronome keeps playing (notifications are `_CAN_DUCK`; with `setWillPauseWhenDucked` we treat as loss → it stops; **confirm this is acceptable**, see §5 — if undesirable, drop `setWillPauseWhenDucked` and let notifications duck).
  4. Stop normally → confirm focus is abandoned (start a music app immediately, it should gain focus without a fight).
- **iOS:** `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` — confirm the no-op controller compiles and DI resolves.

---

## 4. Sequencing & risk

### Recommended order

1. **L8 (audio focus)** first — fully additive (new files + DI + one engine param), no behavioral change to existing happy path, easy to verify in isolation, and lowest blast radius. Establishes the `AudioFocusController` seam before touching the player.
2. **L7 (Android load-then-swap)** second — contained to `MetronomePlayerAndroid` (+ optional one-line engine `drop(1)`). Verify sound switching independently.
3. **H1 (iOS confinement)** last — the highest-impact but most mechanically invasive change (restructures the whole iOS player). Doing it last means the Android items are already verified, so any iOS regression is unambiguously H1.

H1 and L7/L8 touch disjoint files (`MetronomePlayerIos.kt` vs `MetronomePlayerAndroid.kt` + Android platform/DI), so they can also proceed **in parallel** if desired; only the engine constructor (L8) is shared and should land first.

### What could regress

- **H1:** If a `*Internal` method is accidentally left calling a public `scope.launch` wrapper, ordering/recursion bugs appear. Mitigation: `*Internal` methods only call other `*Internal` methods or AVFoundation directly. Also: `initialize` becoming async could surface a latent "play before init" assumption — covered by the existing `?: return` guards, but verify the first-click path on a cold launch.
- **L7:** SoundPool id leaks on rapid switching if the pending-unload mitigation is omitted. The `activeSoundId == 0` sentinel assumes `load` never returns 0 (true for SoundPool). The `drop(1)` dedupe is **flow-semantics-dependent** — if `selectedSound`'s first emission ever differs from the value passed to `initialize`, a real switch would be wrongly dropped; the idempotent-`switchSound` (Option B) is the safer dedupe.
- **L8:** Over-eager stopping on notification ducks if `setWillPauseWhenDucked(true)` is kept and the product wants notifications to merely duck. This is a one-flag toggle. Requesting `AUDIOFOCUS_GAIN` (permanent) is slightly more aggressive toward other apps than `_TRANSIENT`; acceptable for a foreground metronome.

### Interaction with the Phase-1 scheduler

- **`play()` stays non-suspend and immediate on both platforms.** The monotonic scheduler (`MetronomeEngine.delayUntil`, `MetronomeEngine.kt:127-130`) remains the sole timing authority. H1's enqueue adds a **constant** sub-ms hand-off (zero relative jitter); L7/L8 add nothing to the hot path.
- **No new `delay`/blocking** enters the timing loop. L8 requests focus on the play *edge* (outside per-beat work) and abandons on the stop edge — both off the hot path.
- **Engine constructor changes (L8)** are compile-time only; the `single { ... .also { it.start() } }` wiring (`CommonModule.kt:17-20`) is unaffected beyond the extra `get()`.

---

## 5. Open questions / decisions for the user

1. **L8 — transient focus loss = stop, never auto-resume?** Recommended: yes, stop on *all* losses (no surprise resume). Alternative: pause-and-auto-resume on `AUDIOFOCUS_GAIN` after a transient loss — explicitly *not* recommended for a metronome. **Confirm "always stop."**
2. **L8 — should notifications duck or stop the metronome?** With `setWillPauseWhenDucked(true)`, a notification's `_CAN_DUCK` becomes a loss → metronome stops. Many users would rather the click keep going through a notification chime. **Choose:** keep `setWillPauseWhenDucked` (notifications stop the metronome — cleanest, audit's "stop don't duck") **vs.** drop it and let notifications duck (metronome quietly continues). Recommend keeping it for predictability, but this is a UX call.
3. **L8 — if audio focus is *denied*, start anyway or refuse?** Recommended: start anyway (honor the user's tap), log it. Alternative: show a hint / refuse. **Confirm "start anyway."**
4. **L7 — dedupe via engine `drop(1)` (Option A) or idempotent `switchSound` (Option B) or both?** Recommended: **Option B** (platform-agnostic, also reduces H1 churn), optionally plus A. **Confirm B.**
5. **H1 — drop the `@Volatile` annotations** once single-thread-confined? Recommended yes (they become misleading). Harmless to keep. **Confirm removal.**

None of these block implementation; defaults above are safe to proceed with if no preference is given.

---

## 6. References

- **Kotlin/Native new memory manager — freezing removed, threads share/mutate freely; `Dispatchers.Default` is a worker-pool/global-queue on Apple targets; `newSingleThreadContext`/`limitedParallelism` give single-`Worker` confinement.** JetBrains — *Migrate to the new memory manager* — https://kotlinlang.org/docs/native-migration-guide.html ; *Concurrency overview* — https://kotlinlang.org/docs/native-concurrency-overview.html ; A. Zharkova — *Kotlin Native. New Memory management Model* — https://medium.com/google-developer-experts/kotlin-native-new-memory-management-model-7191fa30db30
- **`CoroutineDispatcher.limitedParallelism` (serial/actor-style confinement, replacement for `newSingleThreadContext` allocation).** kotlinx.coroutines API — https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/limited-parallelism.html
- **AVAudioEngine / AVAudioPlayerNode are not thread-safe; confine graph mutation to one thread.** Apple — *HelloMetronome* sample (single-queue access to the engine) — https://developer.apple.com/library/archive/samplecode/HelloMetronome/Introduction/Intro.html ; Apple — AVAudioEngine docs — https://developer.apple.com/documentation/avfaudio/avaudioengine
- **Android audio focus — `AudioFocusRequest` (API 26+), `AUDIOFOCUS_GAIN`/`_GAIN_TRANSIENT`, abandon on stop, stop on `AUDIOFOCUS_LOSS`, never double-request, `setWillPauseWhenDucked`.** Android Developers — *Manage audio focus* — https://developer.android.com/media/optimize/audio-focus ; Android Developers Blog — *Respecting Audio Focus* — https://android-developers.googleblog.com/2013/08/respecting-audio-focus.html
- **SoundPool load-then-swap — `setOnLoadCompleteListener` (`status == 0`), `load` returns a non-zero sample id, `unload` after swap.** Android — `SoundPool` reference — https://developer.android.com/reference/android/media/SoundPool
- Cross-reference: `docs/metronome-precision-audit.md` §3 (H1, L7, L8) and §4 Phase 2.
