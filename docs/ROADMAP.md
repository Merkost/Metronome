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
- iOS settings parity: Volume row via system `MPVolumeView` (`UIKitView` interop, tinted, route button hidden); native `UISwitch` toggles via `expect/actual PlatformSwitch`; background-play confirmed (`audio` mode + `playback` session already present, permission-check actual a no-op). No common changes. Builds green; on-device QA pending. KVO on `AVAudioSession.outputVolume` is not expressible in Kotlin/Native, so the numeric volume caption was dropped in favour of the slider's own live visual.
- iOS Live Activity + Dynamic Island (iOS 17+): BPM/tempo-name/time-signature, adaptive compact island (timer when a practice timer is active, BPM otherwise), self-ticking practice timer via `timerInterval` views, play/pause via `AudioPlaybackIntent`, "Session ended" stale UI, Settings toggle (default on). Kotlin `LiveActivityController` seam + `LiveActivityObserver` (debounced snapshots, wall-clock anchors); iOS Koin moved to global `startKoin` from `iOSApp.swift`; pure-Swift `MetronomeWidgets` extension target. Device QA passed 2026-06-13. A future Android Glance widget can reuse the same snapshot seam.

## Now

### Pre-merge quality gate
- Full multi-agent review of PR #5 once usage limits allow; on-device audio pass (sub-click volume, count-in feel, gap cycle)
- iOS settings on-device QA: `MPVolumeView` drag changes system volume; native switches persist and tint to the active scheme; audio survives backgrounding / screen lock; stereo pan slider, haptic toggle (Core Haptics availability), color scheme picker, sheet insets with the home indicator

## Next

- **Practice reminders**: daily local notification at a user-chosen time ("Time to practice — 3-day streak"). Platform interface + Koin (matching `PlatformActions`), Android `AlarmManager`/`WorkManager` + iOS `UNUserNotificationCenter`.
- **Localization**: move all hardcoded English strings to compose-resources `strings.xml`; start with ES/DE/PT/JA. Mechanical but large; unlocks store reach.
- **Polyrhythms**: second click track (e.g. 3:2, 4:3) layered in the engine; UI as a fourth accordion section. Engine needs a unified tick scheduler first (see Tech debt).

## Later

- Home-screen widget (Android Glance) — can build on the `LiveActivityController` snapshot seam
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
