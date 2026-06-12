# Metronome Roadmap

Last updated: 2026-06-12. Lanes are ordered by priority; items move up as they're scoped.

## Shipped (PR #5 — practice tools redesign)

- Practice timer redesign: bottom sheet config, mode-aware timer chip (stopwatch / countdown ring / done), extend & restart, custom durations
- Tempo sheet: single-expanded accordion (presets · saved tempos · subdivision · tempo trainer · gap trainer), deep links from status strips
- Tempo trainer: descending ramps, hold-to-repeat steppers, stop-keep / reset-to-start, last config persisted
- Subdivisions (eighths / triplets / sixteenths) with softer sub-clicks
- Gap trainer (play N / mute M bars) with live phase strip
- Per-beat mute in the ball tap cycle; compact balls for 6+ beats
- Saved tempo bookmarks (BPM + time signature + subdivision, max 8)
- Practice stats: today / streak / total, local-timezone day boundaries
- One-bar + 1 count-in with rolling countdown in the BPM display
- Pendulum beat display option
- Design system: AppChip, PillChip, AppBottomSheet, AppDialog, StatusStrip, ExpandableSection, ValueStepper, ProgressRing, micro-animation kit in AppAnimations
- Lucide icons everywhere (Material icons removed); container/tint slots defined in all color schemes; Melrose uses its namesake palette
- Keep screen awake (Modifier.keepScreenOn + toggle), count-in toggle, edge-to-edge settings, confirmed stat reset, UI haptics

## Now

### iOS settings parity (committed next)
Settings must work fully on iOS; today `VolumeSlider` and `BackgroundPlayPermissionCheck` have empty iOS actuals, so iOS users silently lose the Volume row and the background-play permission flow.

Plan:
1. **Volume row on iOS**: iOS forbids setting system volume directly via API; the supported path is `MPVolumeView`. Wrap it with `UIKitView` inside the existing `VolumeSlider` iOS actual (interop in `iosMain`, no common changes). Style it to match the Android row (caption + slider height); read `AVAudioSession.outputVolume` (KVO) for the "n / max" caption.
2. **Background play on iOS**: the audio session category is already `playback`; verify background audio works with the toggle and add the `audio` background mode to `Info.plist` if missing. The permission-check actual becomes a no-op explanation row (iOS needs no runtime permission).
3. **QA pass on device**: stereo pan slider, haptic toggle (Core Haptics availability), color scheme picker, sheet insets with the home indicator.

### Pre-merge quality gate
- Full multi-agent review of PR #5 once usage limits allow; on-device audio pass (sub-click volume, count-in feel, gap cycle)

## Next

- **Practice reminders**: daily local notification at a user-chosen time ("Time to practice — 3-day streak"). Platform interface + Koin (matching `PlatformActions`), Android `AlarmManager`/`WorkManager` + iOS `UNUserNotificationCenter`.
- **Localization**: move all hardcoded English strings to compose-resources `strings.xml`; start with ES/DE/PT/JA. Mechanical but large; unlocks store reach.
- **Polyrhythms**: second click track (e.g. 3:2, 4:3) layered in the engine; UI as a fourth accordion section. Engine needs a unified tick scheduler first (see Tech debt).

## Later

- Home-screen widget (Android Glance) and Live Activity / Dynamic Island timer (iOS)
- Apple Watch / Wear OS remote (play/pause + tempo)
- Setlists: ordered saved tempos with auto-advance and per-song bar counts
- More click sounds + per-sound accent pitch; possible sound pack downloads
- Practice history charts (per-day bars, weekly goals) on top of existing daily stats
- Ableton Link / MIDI clock sync

## Tech debt

- Engine timing: replace per-beat `delay()` with cumulative target timestamps to eliminate drift accumulation (prerequisite for polyrhythms)
- `MetronomeViewModel` is growing (~450 lines): consider splitting trainer/timer/stats into focused state holders
- Sessions spanning midnight credit all time to the pause day; streak flows don't re-evaluate at midnight while the app stays open
- Onboarding coach marks reference circles; copy should adapt to pendulum mode
- `app_name` string is the only localized resource; track under Localization
