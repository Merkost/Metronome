# Precision Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the existing cumulative monotonic beat cadence explicit, deterministic, stall-safe, and testable without replacing either platform audio player.

**Architecture:** Add a pure common `BeatTimeline` around an injectable comparable `TimeSource`, then make `MetronomeEngine` use it for count-in, primary-beat, and subdivision deadlines. `MetronomeEngine` remains the coroutine and playback boundary; platform players remain unchanged.

**Tech Stack:** Kotlin Multiplatform, Kotlin time, coroutines, kotlin.test, kotlinx-coroutines-test.

**Spec:** `docs/superpowers/specs/2026-09-01-precision-practice-again-design.md`

## Global Constraints

- Retain cumulative monotonic deadlines; never use wall-clock time for audio cadence.
- Do not change `MetronomePlayerAndroid` or `MetronomePlayerIos` unless a focused platform defect is proven.
- Never emit catch-up bursts after a stall of one beat interval or more.
- Count-in, muted beats, gap bars, and subdivisions keep the same logical timeline.
- A tempo change applies after the current emitted primary beat.
- A meter change re-anchors and resets logical beat and bar state without replaying count-in during continuous playback.
- Use common Kotlin only; add no platform service abstraction.
- Write no code comments.
- Preserve unrelated worktree changes. Source commits are conditional on an isolated clean tree; otherwise record a targeted diff checkpoint instead.

---

## File map

- Create `shared/src/commonMain/kotlin/com/merkost/metronome/engine/BeatTimeline.kt`: monotonic deadline and lateness policy.
- Create `shared/src/commonTest/kotlin/com/merkost/metronome/engine/BeatTimelineTest.kt`: deterministic cadence and stall tests.
- Modify `shared/src/commonMain/kotlin/com/merkost/metronome/engine/MetronomeEngine.kt`: consume `BeatTimeline` for count-in and beat playback.
- Modify `shared/src/commonTest/kotlin/com/merkost/metronome/engine/BeatEventsTest.kt`: complete subdivision and extreme-interval ordering coverage.
- Modify `docs/ROADMAP.md`: mark deterministic cadence hardening implemented only after all gates pass.

### Task 1: Deterministic beat timeline

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/engine/BeatTimeline.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/engine/BeatTimelineTest.kt`

**Interfaces:**
- Consumes: `TimeSource.WithComparableMarks`, positive `Duration` beat intervals.
- Produces: `BeatTimeline.deadline`, `advance(interval)`, `isStale(eventDeadline, interval)`, and `reanchor()`.

- [ ] **Step 1: Write the failing cumulative-deadline tests**

```kotlin
class BeatTimelineTest {
    @Test
    fun hundredsOfBeatsRemainAnchoredToTheOriginalCadence() {
        val timeSource = TestTimeSource()
        val timeline = BeatTimeline(timeSource)
        val initial = timeline.deadline

        repeat(1_000) { timeline.advance(200.milliseconds) }

        assertEquals(200_000.milliseconds, timeline.deadline - initial)
    }

    @Test
    fun smallLatenessKeepsCadenceAndSevereLatenessReanchors() {
        val timeSource = TestTimeSource()
        val timeline = BeatTimeline(timeSource)
        val initial = timeline.deadline

        timeSource += 1_100.milliseconds
        timeline.advance(1.seconds)
        assertEquals(1.seconds, timeline.deadline - initial)

        timeSource += 1_900.milliseconds
        timeline.advance(1.seconds)
        assertEquals(Duration.ZERO, timeline.deadline.elapsedNow())
    }
}
```

- [ ] **Step 2: Run the timeline test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*BeatTimelineTest' --console=plain`

Expected: compilation fails because `BeatTimeline` does not exist.

- [ ] **Step 3: Implement the minimal timeline**

```kotlin
internal class BeatTimeline(
    private val timeSource: TimeSource.WithComparableMarks,
) {
    var deadline: ComparableTimeMark = timeSource.markNow()
        private set

    fun advance(interval: Duration) {
        require(interval > Duration.ZERO)
        val scheduled = deadline + interval
        deadline = if (scheduled.elapsedNow() >= interval) timeSource.markNow() else scheduled
    }

    fun isStale(eventDeadline: ComparableTimeMark, interval: Duration): Boolean =
        eventDeadline.elapsedNow() >= interval

    fun reanchor() {
        deadline = timeSource.markNow()
    }
}
```

- [ ] **Step 4: Add focused tests for exact-boundary lateness, event staleness, and re-anchor**

```kotlin
@Test
fun oneFullIntervalLateIsStale() {
    val source = TestTimeSource()
    val timeline = BeatTimeline(source)
    val event = timeline.deadline
    source += 500.milliseconds
    assertTrue(timeline.isStale(event, 500.milliseconds))
}

@Test
fun explicitReanchorDropsTheOldDeadline() {
    val source = TestTimeSource()
    val timeline = BeatTimeline(source)
    source += 3.seconds
    timeline.reanchor()
    assertEquals(Duration.ZERO, timeline.deadline.elapsedNow())
}

@Test
fun changedIntervalAppliesAfterTheCurrentDeadline() {
    val source = TestTimeSource()
    val timeline = BeatTimeline(source)
    val initial = timeline.deadline

    timeline.advance(1.seconds)
    timeline.advance(500.milliseconds)

    assertEquals(1_500.milliseconds, timeline.deadline - initial)
}

@Test
fun countInAndMainBeatsCanShareOneCumulativeTimeline() {
    val source = TestTimeSource()
    val timeline = BeatTimeline(source)
    val initial = timeline.deadline

    repeat(5) { timeline.advance(500.milliseconds) }

    assertEquals(2_500.milliseconds, timeline.deadline - initial)
}
```

- [ ] **Step 5: Run the focused tests and verify GREEN**

Run: `./gradlew :shared:testDebugUnitTest --tests '*BeatTimelineTest' --console=plain`

Expected: all `BeatTimelineTest` cases pass with zero failures.

- [ ] **Step 6: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/engine/BeatTimeline.kt shared/src/commonTest/kotlin/com/merkost/metronome/engine/BeatTimelineTest.kt`

Expected: no output. Commit only in an isolated clean tree with `feat: add deterministic beat timeline`.

### Task 2: Extreme cadence and subdivision contract

**Files:**
- Modify: `shared/src/commonTest/kotlin/com/merkost/metronome/engine/BeatEventsTest.kt`
- Reuse: `shared/src/commonMain/kotlin/com/merkost/metronome/engine/BeatSchedule.kt`

**Interfaces:**
- Consumes: `beatEvents(beat, interval, clicksPerBeat, muted, stereoLeft, stereoRight, volume, subClickVolume)`.
- Produces: regression evidence for offset ordering and no events for fully muted gap bars.

- [ ] **Step 1: Add failing or characterization tests for every subdivision and BPM extreme**

```kotlin
@Test
fun everySubdivisionProducesStrictlyOrderedOffsetsWithinOneBeat() {
    listOf(1, 2, 3, 4).forEach { clicksPerBeat ->
        listOf(200.milliseconds, 3.seconds).forEach { interval ->
            val events = beatEvents(
                Beat.HIGH,
                interval,
                clicksPerBeat,
                muted = false,
                stereoLeft = 1f,
                stereoRight = 1f,
                volume = 1f,
                subClickVolume = 0.35f,
            )
            assertEquals(clicksPerBeat, events.size)
            assertEquals(events.map { it.offset }.sorted(), events.map { it.offset })
            assertTrue(events.all { it.offset >= Duration.ZERO && it.offset < interval })
        }
    }
}
```

- [ ] **Step 2: Run and classify the result**

Run: `./gradlew :shared:testDebugUnitTest --tests '*BeatEventsTest' --console=plain`

Expected: existing behavior passes as a characterization gate. If any case fails, change only `subClickOffsets` or `beatEvents` enough to satisfy strict ordering and bounds, then rerun.

- [ ] **Step 3: Add muted-timeline evidence**

```kotlin
@Test
fun mutedGapProducesNoAudioEventsWithoutChangingItsInterval() {
    assertTrue(
        beatEvents(
            Beat.HIGH,
            200.milliseconds,
            4,
            muted = true,
            stereoLeft = 1f,
            stereoRight = 1f,
            volume = 1f,
            subClickVolume = 0.35f,
        ).isEmpty(),
    )
}
```

- [ ] **Step 4: Run the complete engine-domain tests**

Run: `./gradlew :shared:testDebugUnitTest --tests 'com.merkost.metronome.engine.*' --console=plain`

Expected: all engine-domain tests pass.

- [ ] **Step 5: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonTest/kotlin/com/merkost/metronome/engine/BeatEventsTest.kt shared/src/commonMain/kotlin/com/merkost/metronome/model/Subdivision.kt`

Expected: no output. Commit only in an isolated clean tree with `test: define extreme beat scheduling contract`.

### Task 3: Metronome engine integration

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/engine/MetronomeEngine.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/engine/BeatTimelineTest.kt`

**Interfaces:**
- Consumes: `BeatTimeline(TimeSource.WithComparableMarks)` and its deadline policy.
- Produces: count-in, primary beats, and subdivisions scheduled from one timeline with stale subdivisions suppressed.

- [ ] **Step 1: Add a failing lifecycle test to the timeline contract**

```kotlin
@Test
fun aFreshTimelineNeverInheritsThePreviousDeadline() {
    val source = TestTimeSource()
    val first = BeatTimeline(source)
    first.advance(1.seconds)
    source += 5.seconds

    val restarted = BeatTimeline(source)

    assertEquals(Duration.ZERO, restarted.deadline.elapsedNow())
}
```

- [ ] **Step 2: Run the lifecycle test and verify its result**

Run: `./gradlew :shared:testDebugUnitTest --tests '*BeatTimelineTest.aFreshTimelineNeverInheritsThePreviousDeadline' --console=plain`

Expected: PASS once Task 1 exists. This is a required characterization before engine replacement, not RED production behavior.

- [ ] **Step 3: Replace local `nextBeat` mutation with `BeatTimeline`**

```kotlin
private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic

private suspend fun delayUntil(target: ComparableTimeMark) {
    val remaining = -target.elapsedNow()
    if (remaining > Duration.ZERO) delay(remaining)
}
```

Inside each playing meter collection, create `val timeline = BeatTimeline(timeSource)`. Count-in delays to `timeline.deadline`, emits once, then calls `timeline.advance(currentBeatDuration)`. Each main beat captures `val beatStart = timeline.deadline`, delays to it, takes one state snapshot, derives all `BeatEvent` values from that snapshot, and plays only events for which `!timeline.isStale(beatStart + event.offset, interval)`. After the beat, call `timeline.advance(interval)`.

- [ ] **Step 4: Preserve meter and count-in semantics**

Keep `isRestart` scoped to one continuous `isPlaying` collection. A beat-count change cancels the prior meter collector, resets index and bar state, constructs a fresh timeline, and skips count-in when `isRestart` is already true.

- [ ] **Step 5: Run engine tests and both platform compilations**

Run: `./gradlew :shared:testDebugUnitTest --tests 'com.merkost.metronome.engine.*' :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64 --console=plain`

Expected: all engine tests pass and both platform compilations succeed.

- [ ] **Step 6: Inspect the focused diff**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/engine shared/src/commonTest/kotlin/com/merkost/metronome/engine`

Expected: no output, no player implementation changes, and no code comments. Commit only in an isolated clean tree with `refactor: harden monotonic beat cadence`.

### Task 4: Precision slice verification

**Files:**
- Modify: `docs/ROADMAP.md`

**Interfaces:**
- Consumes: Tasks 1–3 green evidence.
- Produces: verified precision-hardening milestone and truthful roadmap status.

- [ ] **Step 1: Run the complete pre-feature gate**

Run: `git diff --check && ./gradlew :shared:testDebugUnitTest :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64 --console=plain`

Expected: zero test failures, Android APK assembled, and iOS simulator framework linked.

- [ ] **Step 2: Update only the timing roadmap item**

Replace the cumulative-timestamp item with: `Deterministic cumulative monotonic beat and subdivision scheduling is implemented and covered by common tests; physical-device output latency remains a release gate.`

- [ ] **Step 3: Re-run diff and focused test checks**

Run: `git diff --check && ./gradlew :shared:testDebugUnitTest --tests 'com.merkost.metronome.engine.*' --console=plain`

Expected: zero failures and no whitespace errors.

- [ ] **Step 4: Record the precision milestone**

Run: `git diff --stat -- shared/src/commonMain/kotlin/com/merkost/metronome/engine shared/src/commonTest/kotlin/com/merkost/metronome/engine docs/ROADMAP.md`

Expected: only the timing slice and its roadmap line. Commit only in an isolated clean tree with `feat: harden metronome timing precision`.
