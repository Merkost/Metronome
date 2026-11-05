# Precision Hardening and Practice Again Design

## Purpose

This milestone strengthens the app's primary promise, precise independent practice, before adding the next convenience feature. It first makes beat cadence deterministic and testable without replacing either platform audio implementation. It then adds one fast route back to the most recently completed Practice Set without turning the main metronome into a dashboard.

The work remains local, offline, account-free, ad-free, and consistent across Android and iOS.

## Outcomes

- Playback deadlines remain anchored to a monotonic timeline instead of accumulating coroutine or processing latency.
- Late wake-ups never produce a burst of stale clicks and recover without permanently shifting every future beat.
- Count-in, primary beats, subdivisions, haptics, tempo changes, and meter restarts have explicit timing semantics backed by deterministic common tests.
- A musician can start the most recently completed saved Practice Set from the existing practice-management area with one action.
- Replaced, abandoned, missing, invalid, or unsaved sessions never become misleading Practice Again entries.

## Chosen sequence

The milestone has two independently verifiable slices:

1. Precision hardening and deterministic timing tests.
2. Practice Again persistence, start orchestration, and UI.

The timing slice ships through its full test and platform compilation gates before Practice Again changes begin. The slices share no storage or UI model.

## Precision hardening

### Existing behavior retained

`MetronomeEngine` continues to own the playback coroutine, read live metronome state, request audio focus, update the selected beat, trigger haptics, and invoke `MetronomePlayer`. `MetronomePlayerAndroid` and `MetronomePlayerIos` remain unchanged unless verification exposes a platform defect.

The current monotonic cumulative-deadline approach is retained. This milestone extracts and formalizes it rather than replacing it with wall-clock timing, a native sequencer, or a new audio engine.

### Beat timeline

A focused common `BeatTimeline` owns cadence math. It accepts an injectable monotonic `TimeSource` and exposes explicit operations to:

- Start or re-anchor at the current monotonic instant.
- Return the current primary-beat deadline.
- Advance from the previous scheduled deadline by the current beat interval.
- Classify wake-up lateness relative to the current interval.
- Re-anchor after a severe stall without requesting catch-up playback.

`MetronomeEngine` owns suspension through `delay`; `BeatTimeline` never launches coroutines, accesses platform APIs, plays audio, or reads application state.

### Deadline semantics

- Initial playback and count-in anchor at one monotonic instant.
- Every next primary beat is calculated from the prior scheduled deadline, not from the time playback work finishes.
- Subdivision deadlines are offsets from the owning primary-beat deadline.
- A wake-up less than one beat interval late emits the current due event and retains the existing cadence anchor.
- A wake-up at least one beat interval late emits no stale subdivision burst. The current logical beat is emitted once, the timeline re-anchors at that wake-up, and later beats continue from the new anchor.
- A tempo change applies to the interval following the current emitted primary beat. It does not retroactively move the current deadline.
- A meter change restarts the logical beat sequence and bar count at a fresh anchor. It does not replay count-in during the same continuous playback session.
- Stop and fresh play always create a new anchor. No deadline survives a stopped session.

These rules prefer one intelligible recovery click over catch-up bursts after suspension or scheduler starvation.

### Audio and UI ordering

At a primary-beat deadline, the engine reads one coherent state snapshot for beat, tempo, subdivision, stereo, volume, and gap muting. It updates the visible beat and triggers the matching audio and haptic actions from that snapshot. Subdivision events retain their owning beat's interval and mix values so a mid-beat state change cannot partially rewrite an already-started beat.

Muted beats and gap bars keep their deadlines even when they produce no audio. Logical bars therefore do not shorten when clicks are muted.

### Timing tests

Common tests use an injected controllable monotonic source and cover:

- Hundreds of beats without cumulative deadline drift.
- Minimum and maximum supported BPM.
- Every subdivision count and its offsets.
- Small wake-up lateness that preserves cadence.
- Severe lateness that re-anchors and suppresses stale-event bursts.
- Tempo changes at a beat boundary.
- Meter restart, count-in continuity, stop, and fresh restart.
- Muted beats and gap bars retaining logical time.

The tests assert scheduled deadlines and emitted-event order. They do not claim device-level audio latency, which remains a physical-device release gate.

## Practice Again

### Completion authority

`PracticeSet` gains a nullable `lastCompletedAtEpochMillis` field. The existing tolerant codec appends the field so older records decode with `null`. `PracticeSetRepository` gains `markCompleted(id)`, implemented as an atomic DataStore mutation.

The most recent eligible set is the existing set with the greatest non-null completion timestamp. Deriving recency from the saved collection means deleting a set automatically removes it from Practice Again and renaming or editing it deliberately updates what a future run will use.

`lastStartedAtEpochMillis` remains separate. Opening or starting a set does not make it a completed set.

### Finish reasons

Practice-session finish commands distinguish:

- `Completed`: the musician explicitly confirms Finish from the session controls.
- `Replaced`: the musician confirms replacing the session with another set, timer, Tempo Trainer, or Gap Trainer.

Only `Completed` records `lastCompletedAtEpochMillis`. Recovery discard records nothing. A failed session-clear retry retains the original finish reason and records completion only after the clear eventually succeeds.

This prevents abandoned or replaced sessions from displacing the last genuinely completed set.

### Start flow

Practice Again resolves the current saved set and starts it through the same `MetronomeViewModel.startPracticeSet` boundary used by the library.

- The latest preset versions are resolved when the session starts.
- A missing preset, invalid set, or session-persistence failure leaves the musician on the current screen and shows the existing specific error.
- A running timer or trainer uses the existing replacement confirmation before starting.
- An active Practice Session hides Practice Again because its Resume action is authoritative.
- Navigation changes only after `PracticeSessionStartResult.Started`.

No copied session snapshot is used for Practice Again. Deliberate edits to the saved set and its presets affect the next run.

### Placement and hierarchy

The existing tempo and practice-tools sheet gains one restrained Practice Again row immediately before the Practice Sets management action. It contains:

- `Practice again`
- The current saved set name
- A concise step count
- One Lucide replay or play icon

The complete row is a single 48dp-or-larger button target. It uses existing surface, type, spacing, color, and spring interaction patterns. It does not add a card to the main metronome, bottom navigation, a history screen, celebration, streak messaging, or social proof.

At large text sizes the label and set name stack while the action remains reachable. Long names use two lines and ellipsis. Screen-reader semantics announce the action, set name, and step count once without reading decorative icons.

### States and failures

- No completed set: render no Practice Again row.
- Completed set renamed or reordered: show its current name and current sequence.
- Completed set deleted: the row disappears through repository state; no stale identifier remains visible.
- Completed set has a missing preset: keep the row visible, reject start, and show the existing missing-preset message so the musician can repair it in Practice Sets.
- Completion timestamp write fails: finish still succeeds, Practice Again remains unchanged, and a non-blocking storage message explains that recent practice could not be updated.
- Session start persistence fails: remain in place and do not update completion or navigation state.

## State ownership

- `BeatTimeline` owns cadence deadlines and lateness classification only.
- `MetronomeEngine` owns coroutine scheduling and player invocation.
- `PracticeSessionController` owns finish reason until durable session clear succeeds.
- `PracticeSetRepository` owns start and completion timestamps.
- `PracticeSetsViewModel` continues to own library and editor state.
- `MetronomeViewModel` coordinates completed-session commands, Practice Again starts, replacement conflicts, and user-visible start or completion results.
- Compose renders repository-derived recent-set state and never parses persisted values.

No new platform service abstraction is required.

## Boundaries

This milestone does not add automatic Practice Set advancement, history charts, session scoring, reminders, folders, search, sharing, cloud sync, new audio assets, a native audio-engine rewrite, or claims about measured output latency.

The main metronome remains the dominant screen. Existing Navigation 3 destinations, transitions, preset behavior, timers, trainers, review policy, background playback, Live Activity behavior, and theme system remain intact.

## Verification and acceptance

The milestone is accepted when:

- Deterministic cadence tests prove no cumulative deadline drift and the documented stall behavior.
- Existing beat-schedule, preset, Practice Set, session, navigation, and review tests remain green.
- Repository tests prove completion persistence, old-record compatibility, deletion behavior, and storage failure handling.
- Session-controller tests prove Completed versus Replaced behavior, including failed-clear retry.
- Compose behavior tests or stable presentation tests cover absent, available, missing-preset, conflict, and large-text Practice Again states.
- `git diff --check` passes.
- `:shared:testDebugUnitTest`, `:androidApp:assembleDebug`, and `:shared:linkDebugFrameworkIosSimulatorArm64` pass.
- The full iOS simulator Xcode app build passes.
- One normal and one maximum Dynamic Type simulator inspection confirm hierarchy, reachability, centering, and no clipped action.
- Physical Android and iOS timing, audio-route, TalkBack, VoiceOver, and predictive-gesture checks remain explicitly reported as release gates unless run on devices.
