# Metronome UI/UX review — 11 October 2026

Status: implemented in shared native Compose; Android and iOS builds passed, with 199 Android unit tests and 283 iOS tests (including 84 native rendering cases), plus native runtime walkthroughs. See verification.md for exact scope and limits.
Source revision: 7847dec373bb049d6968c5c645f2d8a7ec7484bd

## Evidence and limits

Reviewed the current shared screen composition, navigation/onboarding integration, brand rules and committed native captures. Inspected welcome, main playback, Rhythm and appearance captures from 6–7 October against their current component source. These are incumbent native captures, not new runtime acceptance. Remaining screens were assessed from current source; gestures, screen-reader behavior and every device/theme combination have not been exercised in this pass. Do not call this a complete native accessibility or runtime audit.

## Intended outcome

Restore a clean, immediately understandable instrument. Keep the modern, playful identity through a few branded graphic details. Introduce the app with short benefit explanations rather than an early settings form. Keep useful functionality and established beat semantics.

## Priority findings

1. P1 — Welcome combines explanation, audio audition, three pace choices, sound selection and completion. The illustration is also a playback control. Selecting a pace triggers audio, and the final action starts playback. This creates several competing intentions before the core instrument is understood. Evidence: WelcomeScreen.kt and WelcomeRoute in AppNavigation.kt, plus native welcome capture.
2. P1 — Main has a primary tempo/playback cluster plus four equally outlined tempo-math controls, a tonal tempo chip, paired feature cards and bottom controls. The repeated circles and containers flatten priority. Evidence: MainScreen.kt and native main capture. Keep BPM/play dominant; relocate advanced tempo shortcuts to a clearly accessible adjustment surface.
3. P2 — Settings repeats its page title through a large marketing-style heading and adds several promotional subheads. The large audio panel and web/Suby cards occupy more visual weight than everyday preferences. Evidence: SettingsScreen.kt and SettingsComponents.kt. Keep grouped preferences, reduce the hero treatment, and make section names literal.
4. P2 — The sound picker combines a description, selection icon, tone graph and preview action in every full card. At narrow widths its rows become tall. Evidence: SoundPickerSheet.kt and existing picker raster captures. Use compact list rows, selection-only tint, quiet tone thumbnails and stable preview touch targets.
5. P2 — Practice has overlapping entry points and a large starter card alongside normal set/preset actions. It needs clear hierarchy rather than more introductory copy. Evidence: PracticeHubSheet.kt, PracticeStarterCard.kt and practice list screens. Keep one primary create/save action; make the starter routine a secondary, contextual empty-state suggestion.

## Surface coverage and proposed treatment

| Surface | Treatment |
| --- | --- |
| Splash/loading | Retain the brand mark and retry/error states; no additional ceremony. |
| Welcome | One short, skippable graphic screen introducing steady rhythm, sound/rhythm customization and practice tools. Three brief highlights and one Get started action open the instrument silently. No tempo/sound form and no replica of the main instrument. |
| Main | Preserve BPM, play, beat display, tap tempo and direct Rhythm/Practice/Sound access. Quiet secondary surfaces and move advanced tempo math off the primary canvas. Preserve active practice status and exact-BPM access. |
| Exact tempo/tempo selection | Consolidate exact entry and quick adjustments without hiding saved setups. Keep keyboard-safe layout and clear confirmation. |
| Rhythm | Keep the corrected grouping of meter, beats, subdivision and count-in. Reduce excess card/tint treatment; preserve beat semantics. |
| Sound picker | Compact sound comparison and selection; independent previews, stable layout and real tone profiles remain. |
| Settings | Audio, Appearance, Playback, Beat display, Practice data and Help. Use spacing and restrained neutral containment; retain progressive disclosure and existing shared switches. |
| Settings statistics/backups | Keep clear data labels, validated additive import/export and busy/error feedback. No cloud-sync or full-device-backup claims. |
| Settings web/Suby | Compact web link and a permanent quiet More by Merkost/Suby feature. No dismiss button; no removal of cross-promotion. |
| Practice hub | Straightforward groups: saved setups and training tools. Remove introductory marketing sentences. |
| Presets | Keep favourite/reorder/rename/duplicate behavior; clarify one primary save action and empty-state hierarchy. |
| Practice sets/starter | Keep routine preview/edit/save and recovery. Starter suggestion stays contextual and secondary. |
| Active session | Preserve progress, previous/next and pause/resume; quiet presentation around active status. |
| Timer | Preserve straightforward duration/start/pause flow; align section/type treatment with other sheets. |
| Tempo/gap trainers | Group required parameters clearly; keep domain explanations where necessary rather than removing useful guidance. |
| What's New/help/about | Use concise, factual copy and consistent list/sheet hierarchy; keep manual access. |

## Non-negotiables

- Filled accent, quiet normal/muted state and an independent outer ring for the active beat; no visible beat numbering.
- Keep shared spring/microtransition tokens, stable value/button sizing, clipping and reduced-motion behavior.
- Keep semantic Material theme roles, system typography, Lucide icons, 18dp gutters and at least 48dp touch targets.
- Preserve bottom-sheet safe areas, large-text/RTL adaptation, onboarding completion migration and error/retry handling.
- No changes to playback scheduling, pendulum/audio synchronization, stored practice data or platform services for visual cleanup.
- Preserve the pending local iOS build-number edit and existing release/signing/design files. Keep only main/dev local branches and the existing checkout.

## Implementation approval

The user approved full native implementation on 12 October and then requested a single welcome screen during native preview. That latest direction is implemented in shared Compose. The original concept image is historical; current native captures and verification results are the acceptance evidence.

## Latest implementation direction

The approved three-page concept was superseded by the user during native review: welcome is one graphic screen with three concise feature highlights and a single Get started action. Carousel paging, page dots and Back are removed. Content scrolls for accessibility sizes; completion remains silent. The original concept image is historical, not final acceptance evidence.

The final visual pass also corrected a wrapped welcome wordmark at large text sizes, vertically centered the welcome action's visible text inside its reserved saving-label space, mirrored Settings Back in RTL and preserved left-to-right digit order inside otherwise RTL screens. Number animation remains keyed by decimal place, so only changed digits move. Shared sheet content now receives the remaining modal height and retains 32dp scrolling end padding.
