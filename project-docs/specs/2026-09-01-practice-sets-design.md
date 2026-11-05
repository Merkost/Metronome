# Practice Sets Design

Date: 2026-09-01
Status: Approved direction, pending written-spec review

## Purpose

Practice Sets turn durable Practice Presets into repeatable, ordered sessions for warmups, exercises, repertoire, rehearsals, and performance preparation. They provide enough structure to keep a musician moving without turning the metronome into a dashboard or forcing automatic progression.

The first release is deliberately manual. A set tells the musician what is current, what is next, and whether an optional target has been reached. It never changes exercises automatically or interrupts playing without a deliberate action.

## Audience and outcome

The feature serves musicians of all levels practising independently. A beginner might create “Daily foundations” from scales and chord-change presets. An experienced musician might arrange warmup, technique, odd-meter, and repertoire presets into a rehearsal order.

Success means:

- A reusable set can be assembled from existing presets in a few minutes.
- Starting a set applies the first preset and begins playback without requiring another setup form.
- Previous, pause or resume, next, and finish remain reachable while the main metronome stays visually dominant.
- Optional time and bar targets provide guidance without auto-advancing.
- A session interrupted by navigation, backgrounding, or process restart can resume safely.
- Preset deletion cannot silently damage a saved set.
- All behavior remains local, offline, account-free, and consistent across Android and iOS.

## Chosen approach

P1B adds a dedicated Practice Sets library and editor plus a restrained active-session strip on the main metronome. It does not fold set editing into the timer sheet and does not treat a temporary queue as a saved set.

This structure keeps three concerns distinct:

- Practice Presets own reusable rhythmic configurations.
- Practice Sets own named, ordered plans that reference presets.
- Active Practice Sessions own a resolved, immutable run snapshot and its progress.

The separation prevents edits to a saved set or preset from unexpectedly mutating a session already in progress.

## Scope and limits

P1B supports:

- Up to 30 Practice Sets.
- Up to 20 ordered steps per set.
- A required set name up to 80 characters.
- One referenced Practice Preset per step.
- An optional target per step: none, duration, or bars.
- Duration targets from 1 to 120 minutes.
- Bar targets from 1 to 999 bars.
- Create, rename, edit, reorder, and delete for sets.
- Start, previous, pause, resume, next, restart step, and finish for active sessions.
- Persisted recovery of an interrupted active session.

P1B does not include folders, tags, search, automatic progression, loops, reminders, notes, sharing, cloud sync, collaborative editing, trainer configuration inside a step, or historical charts.

## Domain model

### Practice Set

`PracticeSet` is an immutable common model containing:

- Stable generated identifier.
- User-editable name.
- Creation and update timestamps.
- Last-started timestamp.
- Manual sort position.
- Ordered list of `PracticeSetStep` values.

An empty draft may exist in the editor, but a saved set requires at least one valid step.

### Practice Set Step

`PracticeSetStep` contains:

- Stable step identifier.
- Referenced Practice Preset identifier.
- One `PracticeStepTarget`.

`PracticeStepTarget` is a sealed common type with exactly three variants:

- `None`
- `Duration(minutes)`
- `Bars(count)`

Targets are guidance. Reaching a target marks the current step as reached and optionally produces one restrained confirmation haptic. Playback continues until the musician pauses, advances, or finishes.

### Active Practice Session

Starting a set resolves every referenced preset and creates an immutable `ActivePracticeSession` snapshot containing:

- Unique session identifier.
- Source set identifier and name.
- Resolved ordered steps containing preset snapshots and targets.
- Current and pending step indexes.
- Accumulated duration and bar progress for the current step.
- Playback intent: running or paused.
- Target-reached state.
- Start and last-checkpoint timestamps.

The session snapshot isolates the current run from later preset or set edits. A resumed session always restores paused and never starts audio automatically.

## Preset reference integrity

Saved sets reference live preset identifiers. Starting a new session resolves the latest version of each preset, so deliberate preset edits affect future runs.

A preset referenced by any saved set cannot be deleted. The delete flow reports how many sets use it, names the affected sets when space permits, and offers a route to Practice Sets. The musician must replace or remove those steps before deleting the preset.

This block applies to saved sets only. An already-running session owns resolved snapshots, but editing or deleting its source set remains unavailable until that session is finished. These constraints avoid silent cascades and broken sessions.

If storage corruption or an interrupted legacy write produces a missing reference, the affected set remains visible with a `Preset missing` step. It cannot start until the step is replaced or removed. Other sets remain usable.

## Persistence

A `PracticeSetRepository` interface exposes ordered sets and focused mutation operations. Its DataStore implementation owns a versioned codec, validation, collection limits, atomic writes, and reference lookup.

Required repository operations are:

- Observe ordered sets.
- Create and update a set.
- Delete and reorder sets.
- Mark a set started.
- Find sets that reference a preset identifier.

A separate `PracticeSessionRepository` stores at most one active-session snapshot. It supports observe, save checkpoint, and clear. Keeping session recovery separate prevents temporary progress writes from rewriting the durable set collection.

The active runner checkpoints at every explicit transition and no less often than every five seconds while playback progress changes. An abnormal process termination may lose at most the uncheckpointed interval. Restored sessions are paused and display the recovered progress before playback can resume.

Both codecs use an explicit schema version, tolerate additive future fields, and skip only the corrupt set or session item rather than discarding the complete collection.

## State ownership and architecture

### Practice Sets collection

`PracticeSetsViewModel` owns:

- Library state.
- Editor draft and validation.
- Preset-selection state.
- Step target editing.
- Explicit reorder mode.
- Unsaved-change handling.
- Storage, limit, missing-reference, and preset-in-use events.

It depends on `PracticeSetRepository` and `PracticePresetRepository`. UI code never parses persisted values or performs reference checks itself.

### Active session

`PracticeSessionController` is a focused common singleton injected through Koin. It owns active-session state, target progress, pending navigation, checkpoints, recovery, and deterministic state transitions. It does not play audio or mutate metronome state directly.

`MetronomeViewModel` remains the playback boundary. It forwards playback intervals and completed bars to the controller, executes the controller’s preset-application requests, and acknowledges immediate or safe-bar-boundary application. This reuses the existing preset boundary mechanism without moving session UI state into the audio engine.

The controller exposes immutable `StateFlow<PracticeSessionState>` and explicit commands. It has no platform APIs and accepts injected time and persistence boundaries for deterministic tests.

## Session lifecycle

### Start

1. The musician selects `Start set` from the set detail or library.
2. Every preset reference is resolved and validated before any playback state changes.
3. If a timer, Tempo Trainer, or Gap Trainer is active, a confirmation explains that starting the set will stop that tool.
4. The active session snapshot is persisted.
5. The first preset applies atomically.
6. The app returns to the main metronome and starts playback, respecting the preset’s count-in setting.

If any reference is missing or invalid, the set does not start and no partial session is created.

### Progress

Duration progress accumulates only while playback is active. Bar progress increments only on completed metronome bars. Pausing freezes both forms of progress.

At the target boundary:

- The step becomes `Target reached`.
- One restrained state change and optional confirmation haptic occurs.
- Playback continues.
- No automatic next-step command is issued.

Steps without targets show elapsed practice time and completed bars as quiet context but never report overdue status.

### Previous and next

When paused, previous or next applies immediately. During playback, it queues the destination preset for the next complete bar boundary. The strip reports `Next step at bar boundary` and disables repeated navigation until the pending application completes.

Progress resets only when the destination preset has actually applied. Previous from the first step is disabled. Next from the last step becomes `Finish` rather than wrapping.

### Pause and resume

Pause stops metronome playback but leaves the active session and current progress intact. Resume restarts playback with the current resolved preset and preserves progress. If the musician manually changed the rhythmic configuration, resume first restores the current step preset and communicates that restoration.

### Restart step

Restart step restores the resolved step preset and resets that step’s duration, bars, pending state, and target-reached state. It does not alter completed progress from earlier steps because P1B does not expose historical per-step summaries.

### Finish

Finish requires confirmation when invoked before the last target is reached. It pauses playback, clears the active-session snapshot, retains the current metronome configuration, and marks the source set last used. No celebration, score, streak prompt, or forced review request appears.

Completing the final step and choosing Finish follows the same path without an early-finish warning. P1B stores the last completed set identifier and completion timestamp as a future seam for `Practice again`, but does not add history UI.

### Recovery

On launch, a stored active session becomes a paused recovered session. The main screen shows `Resume <set name>`. Selecting it restores the current step preset before enabling playback. Dismissing recovery requires explicit confirmation and clears the snapshot.

## Interaction with manual changes and other tools

Changing BPM, meter, subdivision, beats, or count-in during a session marks the current step `Edited`. Its target continues to accumulate. Previous, next, restart, recovery, or resume after a full pause restores the relevant resolved preset before playing.

Applying another preset from quick access during a session also marks the step edited. It does not silently replace the saved set step. Editing the set itself remains unavailable until the session finishes.

Starting a standalone practice timer or trainer while a set is active requires confirmation to finish the set first. Only one structured practice mode may own the metronome at a time.

Review prompting is suppressed whenever an active or recoverable Practice Session exists, including while paused or after a target has been reached.

## Information architecture

The app adds a `Practice Sets` destination without adding bottom navigation.

The tempo sheet gains one concise `Practice sets` action near preset management. It opens the library. The existing Presets destination remains focused on reusable configurations.

The Practice Sets library provides:

- Set name.
- Step count.
- A short preview of the first steps.
- In-progress state when applicable.
- Explicit `Start`, `Resume`, or `Edit` actions.
- Create, reorder, and delete management.

Set editing uses a dedicated full-screen flow because naming, ordering, targets, preset selection, keyboard handling, unsaved changes, and accessibility need stable space.

## Editor flow

### Create

1. Choose `Create set` from the empty state or library.
2. Enter a set name.
3. Choose `Add preset`.
4. Select one or more presets from a searchable-free, favourites-first list.
5. Reorder steps as needed.
6. Optionally choose no target, minutes, or bars for each step.
7. Save atomically.

The primary action remains disabled until the name and every step are valid. The 20-step and 30-set limits are explained before an action is discarded.

### Edit

Editing uses a local draft. Back with unsaved changes opens `Discard changes?`. Saving publishes one complete set update. If another screen changes the underlying set while an editor is open, saving detects the updated timestamp and asks the musician to reload rather than overwriting silently.

### Reorder and remove

Reordering uses an explicit mode with drag handles plus accessible move-up and move-down actions. Removing a step is immediate within the unsaved draft. Deleting the complete set requires confirmation containing its name.

### Preset selection

Preset selection is a focused sheet or destination that reuses preset row summaries without management actions. Already-added presets may be added again because repeated exercises are valid. Each new step receives its own stable identifier and independent target.

## Active-session UI

The main instrument gains one `PracticeSessionStrip` above the bottom playback controls only while a session is active or recoverable.

The compact strip shows:

- Set name and `current step of total`.
- Current preset name.
- Target progress or `Target reached`.
- Previous, pause or resume, and next or finish controls.

Tapping the strip opens an expanded session sheet with the current configuration summary, restart step, upcoming step names, recovery context, and explicit Finish action. Arbitrary step jumping is excluded from P1B so progress and safe-boundary behavior remain predictable.

The strip uses existing tonal surfaces, semantic colors, Lucide icons, 48dp targets, and the established spring motion. It does not compete with BPM, beat state, or the primary play button. At large text sizes the text and controls stack; essential actions never shrink below the touch minimum.

## Accessibility and localization

- All new copy uses Compose resources.
- Set and preset names are user data and are never translated.
- Every set row announces name, step count, missing-reference state, and available action.
- Every session control announces the set, step position, target progress, pending boundary state, and result of activation.
- Target state never relies on color alone.
- Reorder provides explicit accessible move actions in addition to dragging.
- Focus returns to the initiating control after sheets and dialogs.
- Large text, compact phones, dark mode, every color scheme, and reduced motion preserve the complete flow.
- Recovery and conflict dialogs use direct language and never imply that background playback will resume automatically.

## Failure handling

- Corrupt sets are skipped individually and reported through non-fatal diagnostics.
- A missing preset reference keeps the set editable but blocks Start.
- Set persistence failure leaves the previous stored collection intact and keeps the editor draft available for retry.
- Session checkpoint failure leaves the active in-memory session usable and shows a concise recovery warning.
- Preset application failure does not advance the session index or reset progress.
- Repeated previous or next input is ignored while a boundary application is pending.
- A deleted source set cannot occur during its active session because edit and delete are blocked.
- Recovery with a corrupt session snapshot offers `Discard recovery`; it never changes the metronome configuration automatically.

## Testing and acceptance

### Model and persistence

- Set and session codecs round-trip every field.
- Unknown future fields preserve known data.
- Limits, validation, ordering, rename, update, delete, and reference lookup are deterministic.
- Corrupt entries do not discard valid siblings.
- Checkpoint save and clear operations are atomic.

### Session controller

- Start resolves all presets before publishing a session.
- Missing references prevent partial start.
- Duration counts only during playback.
- Bars count only on completed bars.
- Targets mark reached without advancing or stopping playback.
- Previous and next apply immediately while paused and queue while playing.
- Pending navigation cannot be repeated.
- Progress resets only after application acknowledgement.
- Pause, resume, restart, finish, and recovery produce deterministic states.
- Manual changes mark the current step edited.
- Restored sessions are always paused.

### Integration

- Starting a set handles timer and trainer conflicts explicitly.
- Set step changes reuse atomic preset application and safe bar boundaries.
- Review prompting remains blocked throughout active and recoverable sessions.
- Preset deletion reports saved-set references and never creates a broken set.
- Finishing preserves the live metronome configuration and clears recovery state.

### Compose behavior

- Empty, populated, limit, missing-preset, storage-error, unsaved-change, conflict, active, pending, target-reached, early-finish, and recovery states render correctly.
- Editor keyboard, back, reorder, target selection, and preset selection flows are complete.
- Main-strip hierarchy remains subordinate to BPM and playback.
- Compact and accessibility-size layouts do not clip or hide essential actions.
- TalkBack and VoiceOver expose meaningful labels, state, and traversal order.

### Release gates

- Shared unit tests pass.
- Android debug and iOS simulator framework builds pass.
- Full iOS simulator application build passes.
- Android and iOS runtime flows cover create, edit, start, target reached, next at boundary, pause, recovery, finish, missing preset, and conflict handling.
- Physical-device checks cover low and high BPM, every subdivision, background and interruption behavior, TalkBack, and VoiceOver.

## Explicit exclusions

- Automatic or hands-free step advancement.
- Per-step Tempo Trainer, Gap Trainer, or standalone timer configuration.
- Step loops, branching, randomization, or conditional progression.
- Session notes, ratings, charts, calendar history, or goals.
- Set favourites, folders, tags, artwork, search, import, export, or sharing.
- Cloud sync, accounts, collaborative sets, or cross-device recovery.
- Widgets, watches, MIDI, Bluetooth pedals, and lock-screen set controls.
- Audio recording, tuner, media attachments, or repertoire files.

These exclusions keep P1B focused on trustworthy reusable structure. They preserve clear seams for faster recall, external controls, and private practice insight without weakening the metronome’s primary role.
