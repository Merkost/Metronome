# Practice Presets Foundation Design

Date: 2026-09-01
Status: Approved direction, pending written-spec review

## Purpose

Practice Presets are the foundation for structured practice, faster control, and meaningful local practice insight. They replace anonymous saved-tempo bookmarks with named, durable configurations that musicians can recognize and recall immediately.

The first release must improve the complete flow rather than merely change storage. A musician should be able to save the current setup, give it a useful name, find it later, apply it without doubt, and manage a growing collection without turning the main metronome into a dashboard.

## Audience and outcome

The feature serves musicians of all levels practising independently. A beginner may save “C major scale” and “Chord changes,” while an experienced musician may save rehearsal excerpts, odd meters, gap-training drills, or performance tempi.

Success means:

- Saving the current configuration takes only a few seconds.
- Applying a preset is a single deliberate action from the tempo sheet.
- The musician can confirm what changed without reading a dense summary.
- Presets remain available offline and survive app restarts and schema upgrades.
- Existing saved tempos migrate without loss.
- The main screen continues to feel like a focused instrument.

## Product sequence

The broader roadmap is intentionally ordered:

1. Practice Presets create stable, recognisable practice configurations.
2. Practice Sets arrange those configurations into repeatable sessions.
3. Faster controls recall the same configurations from recents, favourites, widgets, and hardware.
4. Practice insight attributes real sessions to the configurations and sets that produced them.

Tempo Trainer remains one optional configuration tool. It does not become the product identity or the primary navigation model.

## Scope

### Preset contents

Each preset has:

- Stable generated identifier
- User-editable name
- Creation and last-used timestamps
- Favourite state
- Manual sort position
- BPM
- Time signature
- Subdivision
- Per-beat accent and mute configuration
- Count-in enabled state

Click sound, volume, stereo position, theme, haptics, display style, keep-screen-awake, and background-play behavior remain global preferences. Tempo Trainer, Gap Trainer, and timer state are excluded from P1A because they introduce active-session semantics that belong in Practice Sets.

### Collection limits

P1A supports up to 50 presets. This is large enough for genuine practice use while keeping the first storage and interaction model bounded. No folders, tags, cloud sync, or free-text search are included. Favourites and recency provide the primary retrieval aids.

### Existing-data migration

Every legacy `SavedTempo` becomes a preset. Migration assigns a stable identifier, preserves BPM, time signature, and subdivision, uses the current default beat configuration for that signature, preserves current count-in state, and generates names such as “120 BPM · 4/4.” Name collisions receive a numeric suffix.

Migration is idempotent. Legacy data remains readable until the new collection has been written successfully. A failed or interrupted migration must leave the original saved tempos available for the next attempt.

## Information architecture

The app retains its existing main and settings destinations and adds one full-screen `Presets` destination. It does not add bottom navigation.

The tempo sheet remains the quick-access surface:

- A compact `Favourites` group appears first when favourites exist.
- A compact `Recent` group shows the most recently applied presets.
- `Manage presets` opens the full-screen Presets destination.
- `Save current setup` begins the creation flow.

The main screen shows the applied preset name only as quiet context near the tempo controls. Manual changes after applying a preset mark that context as edited. Playback, BPM, beat visualization, and the primary play button retain visual priority.

## Core user flows

### Save the current setup

1. The musician chooses `Save current setup` from the tempo sheet.
2. A focused naming dialog appears with a generated, editable name and a concise configuration summary.
3. The primary action is `Save preset`; the keyboard action performs the same save when the name is valid.
4. On success, the dialog closes, the preset becomes the active preset, and a brief non-blocking confirmation appears.

Blank names are invalid after trimming. Matching names are allowed because musicians may intentionally reuse exercise names, but the configuration summary prevents ambiguity. When the 50-preset limit is reached, creation is blocked with a direct route to manage presets.

### Apply a preset

1. The musician taps a favourite or recent preset in the tempo sheet.
2. The complete rhythmic configuration applies atomically.
3. The sheet closes and the main screen announces the applied preset through visible text and accessibility semantics.
4. If playback is active, the change takes effect on the next safe bar boundary rather than producing a partial bar.

Applying a preset never changes global audio or appearance preferences.

### Browse and manage

The Presets screen uses a single-column list capped at the established content width. Favourites appear first, followed by the remaining manually ordered presets. Each row presents name, BPM, meter, subdivision when non-quarter, and a favourite control. Tapping the row applies it and returns to the metronome. A trailing overflow action provides rename, duplicate, and delete.

Reordering enters an explicit reorder mode so drag handles do not compete with row activation or assistive technology. Reorder mode has visible `Done` and cancel behavior and supports accessible move-up and move-down actions.

### Rename and duplicate

Rename uses the same focused naming dialog and never changes the rhythmic configuration. Duplicate creates a new identifier, appends “Copy” to the name, places the result after the source, and immediately opens rename so the copied preset does not remain ambiguous.

### Delete

Delete requires confirmation containing the preset name. If the preset is currently applied, deleting it removes the saved reference but leaves the current metronome configuration unchanged. Undo is not required in P1A because confirmation is explicit and deletion is local, immediate, and deterministic.

## UI and interaction design

The feature extends “The Precision Instrument” rather than introducing a library-style visual world.

- The BPM display remains the largest element in the product.
- Preset rows use existing tonal surfaces, typography, spacing, and 48dp interaction targets.
- Favourite state uses icon shape and semantics in addition to colour.
- Configuration summaries are short and aligned for scanning; they do not become dense badges.
- Creation and rename use a dialog because they are brief, focused decisions.
- Collection management uses a full screen because ordering, destructive actions, keyboard handling, and accessibility need stable space.
- Motion uses existing spring specifications for interactive state and restrained fades for destination changes.
- No celebratory animation, streak prompt, card carousel, dashboard, or ornamental artwork is added.

### Empty state

The empty Presets screen explains the practical value in one sentence and offers `Save current setup` as the only dominant action. It uses the live metronome configuration, so users do not complete a separate setup form.

### Edited state

After a preset is applied, changing BPM, meter, subdivision, beats, or count-in shows `Preset name · Edited`. The user can continue freely. Saving opens a choice between `Update preset` and `Save as new`; destructive overwriting never happens automatically.

### Active playback

Preset creation and management are allowed while playback is paused. Quick applying remains available during playback and commits on the next safe bar boundary. Entering the full management screen while playing pauses playback first and communicates that state change.

## Accessibility and localization

- Every row has a complete spoken label containing name and essential rhythm configuration.
- Favourite, active, edited, selected, and reorder states are exposed semantically and never rely on colour alone.
- Reorder mode provides accessible move controls in addition to dragging.
- Dialog focus begins in the name field, validation is announced, and focus returns to the initiating control.
- Dynamic type may wrap preset names to two lines; essential controls must not clip at large font scales.
- Reduced-motion behavior avoids scale pulses and uses state changes or fades.
- All new strings use Compose resources from the first implementation.
- Names are user data and are never translated; generated migration names use localized formatting.

## Architecture

### Domain model

`PracticePreset` is an immutable common model. A versioned codec owns serialization and migration. UI code never parses stored strings directly.

Per-beat configuration is stored by semantic beat type rather than by UI index where possible. Applying a preset validates BPM and beat count against current domain limits before publishing one complete metronome state update.

### Persistence boundary

A `PracticePresetRepository` interface exposes ordered presets and focused mutation operations. Its DataStore implementation owns encoding, size limits, migration, and atomic writes. This keeps persistence details out of `MetronomeViewModel` and provides a future seam for Practice Sets without introducing a database prematurely.

Expected operations are observe, create, update, duplicate, delete, reorder, mark favourite, mark used, and migrate legacy saved tempos.

### State ownership

A focused `PracticePresetsViewModel` owns collection-management state, dialogs, validation, reorder mode, and user-facing failures. `MetronomeViewModel` applies a selected preset to playback state and reports the active or edited preset identity. Communication occurs through immutable preset values and repository operations rather than direct cross-ViewModel mutation.

### Atomic application

Applying a preset produces one validated configuration command. When paused, it applies immediately. During playback, the engine queues it for the next bar boundary and reports completion so the UI does not claim the preset is active early.

## Failure handling

- Invalid stored presets are skipped individually and reported through non-fatal diagnostics; valid presets remain usable.
- Migration failure retains legacy data and exposes a retryable, non-blocking state.
- Persistence failure leaves the previous in-memory collection intact and presents a concise error.
- A preset removed while visible is handled by identifier; screens do not retain list indexes as identity.
- Applying an incompatible or corrupt preset fails without partially changing metronome state.
- Reaching the collection limit explains the limit and opens management rather than silently dropping an item.

## Testing and acceptance

### Model and persistence

- Codec round-trips every field.
- Unknown future fields do not corrupt known data.
- Legacy saved tempos migrate exactly once and remain recoverable after an interrupted write.
- Generated identifiers and collision-safe names are deterministic under test.
- Limit, ordering, favourite, recency, duplicate, update, and delete behavior are covered.

### State and engine integration

- Applying while paused updates the full configuration atomically.
- Applying during playback commits at the next bar boundary.
- Manual rhythmic changes mark the active preset as edited.
- Updating and saving as new produce distinct, correct repository operations.
- Deleting the active preset preserves the live metronome configuration.

### Compose behavior

- Empty, populated, limit, validation, migration-failure, and persistence-failure states render correctly.
- Favourite and recent quick access applies the intended preset.
- Dialog keyboard, focus restoration, back behavior, confirmation, and reorder mode are exercised.
- Large text, narrow screens, dark mode, and all existing colour schemes remain usable.
- TalkBack and VoiceOver labels communicate name, configuration, favourite state, and available actions.

### Release gates

- Shared unit tests pass.
- Android debug and iOS simulator framework builds pass.
- Targeted Android and iOS UI checks pass on compact and large text configurations.
- On-device playback verifies boundary-safe application at low and high BPM with every subdivision.
- Existing saved-tempo data from the current release migrates on both platforms.

## Explicit exclusions

- Practice Set editing and running
- Trainer or timer configuration inside presets
- Folders, tags, search, artwork, or imported media
- Cloud synchronization, accounts, sharing, or cross-device merge
- Practice charts, goals, reminders, and session notes
- Widgets, watch controls, MIDI, and pedal bindings
- Automatic preset suggestions

These exclusions keep P1A independently useful while preserving clear seams for the approved P1B, P2, and P3 roadmap work.
