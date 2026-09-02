# Metronome UI/UX Audit

Last updated: 2026-08-31

## Direction

The product should feel like a beautifully machined practice instrument: calm, precise, premium and modern. The interface remains predominantly monochrome, with colour reserved for a selected scheme, active state, progress or platform capability. Controls should feel physical without becoming decorative, and practice tools should add depth without making the basic metronome harder to use.

The current visual foundation already supports this direction. The audit therefore preserves the composition, bold tempo hierarchy, circular control language and restrained surfaces rather than redesigning the app into a dashboard.

## Scope and Evidence

The audit covered the shared Compose UI, platform settings components and native iOS presentation in these states:

- iPhone 17 in light and dark appearance;
- iPhone 17 at accessibility-extra-extra-large text size;
- iPad Pro 11-inch in portrait;
- onboarding coach marks;
- idle main metronome;
- Settings;
- Practice Timer sheet;
- Tempo sheet, saved tempo controls, Subdivision, Tempo Trainer and Gap Trainer entry points.

Runtime screenshots are stored in `artifacts/ui-audit/`. Android source, resources and build output were inspected and compiled, but no Android emulator was available for a matching screenshot pass.

## Findings Resolved

### Native review request

- Uses the native App Store and Google Play review flows rather than a custom rating dialog.
- Becomes eligible after five minutes of cumulative playback in the current app process.
- Waits for playback to pause and remain paused for two seconds.
- Rechecks the live state after the delay and cancels if playback resumes.
- Does not request while a countdown timer, Tempo Trainer or Gap Trainer is active.
- Does not request while the timer sheet, tempo sheet or onboarding is visible.
- Limits attempts to once per app version and enforces a 180-day cooldown across versions.
- Persists an attempt only after a foreground native presentation host accepts the platform request pipeline.
- Keeps the manual Rate the App action in Settings.

The operating system can suppress a native review sheet, so the app never assumes that a person saw, dismissed or completed it.

### Touch targets and control semantics

- Added a shared 48dp minimum touch target and applied it to beat controls, chips, status controls and steppers.
- Kept beat dots visually compact while expanding their invisible hit areas.
- Added useful labels for beat states, play and pause, increment and decrement, sliders, colour schemes and saved-tempo removal.
- Removed spoken labels from decorative timer, pointer, check and app-icon imagery.
- Merged switch labels and switch state into coherent assistive-technology nodes.
- Enlarged the onboarding Skip action and gave it a theme-aware surface.

### Component and token consistency

- Added one shared `AppSlider` for tempo, timer, volume and stereo controls.
- Centralised slider touch sizing, tick treatment and accessibility labelling.
- Reused the shared spring motion token for chip colour and size transitions.
- Reused shared corner-radius tokens in dropdown surfaces.
- Corrected modifier ordering in the reusable icon and play buttons so caller modifiers behave predictably.
- Constrained bottom-sheet and onboarding content to the shared 480dp adaptive content width.
- Reused `MySecondaryButton` for support and rating actions so Settings no longer presents two competing primary calls to action.

### Text scaling and resilience

- Added bounded auto-sizing to compact app-bar, tempo, timer, Tap Tempo and quick-adjust labels.
- Kept the dominant BPM readout responsive within the space between its controls.
- Allowed total practice time to wrap instead of truncating behind Reset.
- Constrained timer statistics to equal columns with independent auto-sizing and alignment.
- Added bounded auto-sizing to active-trainer secondary actions.
- Verified that the Tempo sheet reflows presets and trainer entry points cleanly at accessibility-extra-extra-large text size.

### Visual consistency

- Preserved the high-contrast monochrome instrument layout in both appearances.
- Preserved the app's circular primary controls and pill-shaped secondary controls.
- Kept settings and practice sheets on the same spacing, shape, type and colour system as the main surface.
- Avoided adding gradients, glass effects, decorative cards or competing accent colours.

## Component System

The shared UI should continue to build from these components:

- `PlayButton` for the singular primary playback action;
- `MyIconButton` and `MySecondaryButton` for secondary actions;
- `AppChip` for selectable options and saved items;
- `PillChip` for compact dropdown anchors;
- `AppSlider` for adjustable numeric ranges;
- `ValueStepper` for exact trainer values;
- `StatusStrip` for active trainer state and progress;
- `AppBottomSheet` for focused practice configuration;
- `ExpandableSection` for layered advanced tools;
- `AppDialog` only for consequential confirmation.

New controls should reuse the spacing, sizing, radius, elevation and motion values in `Dimensions.kt` and `AppAnimations.kt`. UI colours should come from `MaterialTheme.colorScheme`.

## Remaining Validation

These are follow-up validation tasks rather than blockers for the implemented pass:

1. Perform a full VoiceOver traversal on iOS and TalkBack traversal on Android, including rotor order and slider announcements.
2. Verify reduced-motion behaviour on physical iOS and Android devices; the app does not yet provide its own cross-platform reduced-motion abstraction.
3. Run the same visual matrix on an Android phone and tablet when an emulator or device is available.
4. Move remaining hard-coded English UI strings into shared resources before localisation work begins.
5. Reassess a tablet-specific two-pane practice layout only if product evidence shows that the intentionally compact instrument layout is limiting iPad use.

## Guardrails

- Do not add engagement prompts while the metronome, timer or trainer is active.
- Do not use a custom sentiment gate before the native review sheet.
- Do not turn Tempo Trainer into the product's primary identity; it is one part of the broader precise-practice toolkit.
- Do not trade timing clarity or one-tap playback for feature density.
- Do not add visual novelty that competes with the beat, BPM or play state.
