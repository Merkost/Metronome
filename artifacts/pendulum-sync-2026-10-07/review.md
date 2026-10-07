# Pendulum synchronization review

Scope: the pendulum change in this PR working tree, against `86a6cf360c94f288a8a157e252bf553504137caf`. Earlier redesign and other subsystems are excluded. This reviewer inspected source without editing source/Git or running builds.

## Findings

No actionable finding remains in the focused review. The implementation meets the selected primary-beat crossing behavior with the evidence and limits below. The engine now publishes playable primary/count-in pulses from backend submission acknowledgements. Android acknowledges successful SoundPool submission, including queued loads; iOS acknowledges after `scheduleBuffer`. Stale primary and intentionally silent beats retain scheduled phase. The beat scheduler is unchanged.

**Resolved during review: fence callback publication across playback sessions.** The initial callback implementation checked the captured coroutine context and playing state separately from publication. A cancellation/restart interleaving could therefore allow an old callback to overwrite a new session. The final `BeatClockState` stores generation and pulse together; session reset and publication use atomic StateFlow updates. Publication requires the current generation and a strictly newer ordinal. This closes both the session race and out-of-order callback rewinds. A deterministic test interrupts timestamp capture in the former check-to-publication window.

## Before / After / Why

| Before | After | Why |
| --- | --- | --- |
| Per-index tween targets alternating outer edges. | Every primary beat starts at zero displacement; the arm travels to an outer edge halfway between beats. | The visual click occurs at the centre crossing, with the slowest movement at the edges. |
| Motion depends on selected index changes and local direction. | A monotonically increasing beat ordinal controls alternating direction. | One-beat patterns, odd-length bars, and the count-in transition keep a consistent swing. |
| Each UI animation carries its own relative clock. | Display frames sample elapsed time from the submitted pulse's monotonic mark and exact beat duration. | Late mounting and dropped frames recover the current phase instead of accumulating animation delay. |
| Starting/stopping an independent animation can leave residual state. | Stop invalidates the pulse session and the arm settles with the shared calm spring; restart creates a fresh generation. | Pausing has a controlled visual response and replay begins from a known crossing. |
| Continuous motion does not explicitly honor the shared reduced-motion setting. | Reduced motion keeps the arm centred. | Users can retain the beat display without continuous oscillation. |

## Behavior and limits

The phase model is a driven harmonic oscillation, bounded to 20 degrees. It crosses the centre at integer beat intervals, reverses at half intervals, and has zero angular velocity at each reversal. This is a visual approximation of metronome motion, suitable for fixed beat timing; it does not simulate escapement, mass, or gravity.

Primary crossings follow the backend submission acknowledgement on Android and iOS; the backwards-compatible default interface implementation acknowledges an engine request. Subdivision clicks occur within that swing. Muted beats and gap bars continue to supply timing motion, preserving the visual reference during silent practice. Tempo changes use the exact interval captured by the engine for each pulse; scheduler lateness may require phase correction at the next accepted primary request.

The UI updates on display frames. Mathematical zero displacement at the audio request does not mean every display can render a frame at that exact instant. Native raster results need a tolerance consistent with sampling time and pixel rounding.

On iOS, `MetronomePlayerIos.play` enqueues an audio command and acknowledges after `scheduleBuffer(atTime = null)` runs in the serial handler. Android acknowledges only a nonzero SoundPool stream result, including queued load completion. These acknowledgements establish submission timing, not speaker onset. Phase tests and native raster captures do not establish acoustic timing, physical-device behavior, or Bluetooth synchronization.

## Declined behavior

- Centre crossings for every subdivision click: the selected default follows the existing primary-beat display behavior. The optional user choice was unanswered; extra crossings would multiply the perceived pendulum rate and need an explicit product decision.
- Replacing the beat scheduler or platform audio queues: not needed to fix the index-driven visual error and would change unrelated timing behavior.
- Full physical simulation: fixed musical timing is the controlling behavior; nonlinear physics and escapement modeling add complexity without stronger synchronization.
- Claims of acoustic/device/Bluetooth synchronization: no measured physical-output evidence has been supplied.

## Verification evidence

- Independently read final XML: **196 Android tests** and **242 iOS simulator tests**, all with zero failures, errors, or skipped tests. The native total includes **46 rendering tests**; this is a subset, not an additional total. New focused suites comprise six phase tests, eight actual-engine integration tests, and five native pendulum rendering tests.
- The implementer reported the final exporter-free Gradle gate completed successfully in **2m51s**, including Android debug build, iOS device compilation, iOS simulator framework linking, and Wasm compilation. This reviewer did not run builds.
- The actual-engine suite exercises delayed and out-of-order submission, cancellation during timestamp capture, normal pause/resume, meter restart, count-in through odd-length bars, live tempo changes, muted beats, gap bars, subdivisions, and stale-primary recovery.
- Native raster tests cover centre crossings, repeated indices, odd bars, late mounting, dropped-frame recovery, tempo updates, pause/restart, reduced motion, and a complete cycle with stable layout bounds.
- Baseline native regression evidence: the implementer reproduced the former failure at the second click, with tip **111.753px** against centre **150px**. The independently inspected [baseline capture](baseline-beat.png) shows the arm left of centre.
- Independently inspected accepted captures: [fixed click](fixed-beat.png), [cycle start](cycle-000.png), [right turn](cycle-025.png), [second crossing](cycle-050.png), [left turn](cycle-075.png), [third crossing](cycle-100.png), [dark fast swing](fixed-dark-fast-left.png), [stopped](fixed-stopped.png), and [reduced motion](fixed-reduced-motion.png). Crossings are centred, turning points are symmetric, and the body remains stable.
- A [silent cycle video](pendulum-cycle.mp4) is available as a visual artifact. It provides no acoustic synchronization evidence.

Acceptance is bounded to phase math, actual-engine submission/cancellation behavior, native raster output, and compilation/build gates. No physical-device recording, acoustic onset measurement, route-latency compensation, or Bluetooth proof is claimed.
