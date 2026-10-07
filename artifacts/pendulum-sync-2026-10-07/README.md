# Pendulum timing and motion

The pendulum now crosses centre on main clicks and count-in ticks. Its small-angle harmonic motion is fastest through centre and slows to zero at each turning point. It reads elapsed time from the same monotonic pulse recorded at sound submission, so late composition and dropped display frames catch up rather than restarting an eased tween. Stop uses the existing calm spring; reduced motion keeps the arm centred.

| Before | After | Why |
| --- | --- | --- |
| Each changed beat index starts an eased trip towards an edge. | A pulse timestamp and exact captured Duration drive the display-frame phase. | The click is a centre crossing, rather than the end of a tween. |
| Equal indices do not restart the animation; bar indices own direction. | A monotonically increasing pulse ordinal owns alternation. | Repeated indices and odd bars do not freeze or reset the swing. |
| UI timing precedes native playback queues. | Android acknowledges successful SoundPool submission; iOS acknowledges scheduleBuffer from its command handler. | Startup/load and command-queue delay are reflected in the visual timestamp. |
| Delayed callbacks can outlive their playback session. | Generation and pulse publication share one atomic state, with strict ordinal ordering. | A cancelled or older callback cannot restore or rewind a new session. |

## Evidence

- [Original beat pose](baseline-beat.png): the native rendered regression fails at tip 111.753px rather than centre 150px.
- [Fixed beat pose](fixed-beat.png), [right turning point](cycle-025.png), [left turning point](cycle-075.png), [dark/fast pose](fixed-dark-fast-left.png), [stopped](fixed-stopped.png), [reduced motion](fixed-reduced-motion.png).
- [Silent cycle preview](pendulum-cycle.mp4): 101 native-raster frames across a controlled 1000ms cycle, with centre at 0/500/1000ms and symmetric turns at 250/750ms. Native component geometry is fixed 300 x 132px at density 1. The baseline fixture uses default Material colors; fixed fixtures use semantic Black-and-White colors. Position assertions measure the same geometry independently of palette.
- [Validation/source hashes](validation.json), [gate output](build-result.txt), [independent review](review.md), and the three focused XML reports.

The six pure phase tests check centre/turning positions, natural velocity, late frames, very long elapsed periods, exact fractional cadence, future timestamps and invalid cadences. Eight actual-engine tests use the real ViewModel/DataStore/engine with an external submission test driver: deferred/older audio handoffs, cancellation during timestamp capture, count-in into odd bars, silent gaps, subdivision behavior, stale scheduler recovery, pause/resume, meter restart, live tempo updates and muted primaries. Five native rendering tests inspect the actual painted arm at clicks/turns, repeated indices, late mounts, dropped frames, tempo changes, stopping/restarting, reduced motion and a complete cycle.

The default crossing policy follows main beats, preserving the existing metronome visual meaning. Eighth/triplet/sixteenth sub-clicks do not accelerate the pendulum. Muted and gap-training beats continue on the scheduled primary clock; stale primary requests retain scheduled phase until a newly accepted sound reanchors it. Backend timing callbacks do not change audio deadlines, sample resources, volume, pan or subdivision scheduling. The other-player overload fallback acknowledges engine handoff; native backends provide queue-aware submission timing.

These images/movie are production Pendulum in native raster fixtures, not full UIKit, microphone, speaker or physical-device captures. Native submission timing is not an acoustic-output timestamp: device buffers and Bluetooth latency remain manual release acceptance. This implements harmonic motion rather than a full mass/gravity simulation.

Temporary exporters were removed before final gates. `capture-fixture.kt.txt` and `baseline-fixture.kt.txt` preserve reproduction code outside compiled source sets. PPM snapshots were converted losslessly to PNG; the movie is encoded from the sampled native component poses. The native test SDK uses packaged real simulator SwiftPM archives.
