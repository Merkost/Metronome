# Focused Rhythm and home review

Review scope: uncommitted MainScreen.kt, MainShortcuts.kt, RhythmSheet.kt and HomeRhythmLayoutTest.kt in /Users/merkost/.codex/worktrees/native-design-implementation/Metronome, against d972690930d8b8b20bd43089258c821a56a8c3cb. Source review is read-only.

## Findings

No open actionable findings in the final source and refreshed native captures. The earlier P2 dark-shortcut contrast finding is closed: titles now explicitly use onSurface, both chevron arrangements use onSurfaceVariant, and refreshed shortcuts-dark-rtl-2x.png visibly paints clear white titles/arrows. The new rendered-title regression assertion checks foreground-versus-background channel difference, catching the original black-on-dark rendering; it is not a full WCAG contrast certification. Rhythm top-level headings also use onSurface explicitly. The muted legend now includes the X glyph to match the actual muted beat.

## Before / After / Why

| Area | Before | After | Why |
| --- | --- | --- | --- |
| Home shortcuts | Baseline 324dp native raster has a wrapped Practice caption and 86px Practice versus 70px Sound card. | Both cards reserve the same measured title/caption slots; accepted light raster shows equal 86px silhouettes. All seven sound names participate in measurement. | Equal hit areas and alignment survive changing sound labels. |
| Home scroll end | Prior scrolling content had top padding only; implementer reports baseline zero bottom gap. | Production MainScrollableContent keeps 32dp padding at both ends, with bottom padding inside the scrolling region. | The last shortcuts retain space above anchored playback controls when scrolled to the end. |
| Rhythm structure | Meter, beat editing, subdivision and count-in were separate equally weighted groups. | Meter and Beat pattern share a quiet rounded panel, with a concise instruction and explicit Accent/Normal/Muted legend; subdivisions sit below and count-in uses the existing SettingsSwitch row. | The bar and its voices form one understandable group; supporting timing choices remain subordinate. |
| Large text and RTL | Independent card sizing could vary with wrapping. | Native 2x dark RTL raster reflows shortcuts to equal full-width rows; Rhythm chips, legend, beats and count-in wrap, and count-in remains reachable at scroll end. | Text retains room without reduced touch targets; chevrons mirror with layout direction. |

## Behavior and accessibility

No behavior regression found in source: callbacks remain the same production view-model callbacks; no engine or persistence changes. Rhythm forwards beat position/state directly to unchanged MetronomeBalls, preserving solid accent, tonal normal, outlined muted state with X glyph and the independent active playback ring, with no visible beat numbers. Time signature/subdivision expose selected RadioButton semantics inside selectable groups. Section headings expose heading semantics. Legend glyphs are excluded from accessibility while their labels remain readable. Count-in uses the established full-row switch component. Shortcut content descriptions retain title and current sound; production click targets retain button roles.

## Evidence and limits

Visual inspection used lossless PNG conversions of native Compose PPM outputs under /private/tmp/metronome-rhythm-home. Final reviewed files are baseline-shortcuts.png, fixed-shortcuts.png, shortcuts-dark-rtl-2x.png, rhythm-light.png, rhythm-dark-rtl-2x-top.png and rhythm-dark-rtl-2x-end.png. Earlier *.review.png captures were used for the first review and are superseded for acceptance. These are native Compose raster fixtures with injected Density(1f) and 1x/2x font scale; the RTL case changes LocalLayoutDirection. They demonstrate those extracted production composables at those widths. They do not establish an installed full-app UIKit modal presentation, device/window safe-area behavior, native Dynamic Type mapping, VoiceOver traversal/announcement, physical-device interaction or audio timing. The Rhythm fixtures wrap RhythmContent in a test scroll container rather than AppBottomSheet. The home bottom-gap fixture uses MainScrollableContent with synthetic preceding blocks rather than complete MainScreen. Temporary PPM exporters were removed from final test source. Review did not run builds/tests; the implementer reports the native recapture suite passed and owns final full-gate results. Refreshed source diff passes git diff --check.
