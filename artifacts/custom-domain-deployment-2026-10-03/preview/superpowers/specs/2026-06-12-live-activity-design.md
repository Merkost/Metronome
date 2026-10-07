# iOS Live Activity + Dynamic Island — Design

Date: 2026-06-12
Status: Approved (design); spec pending user review
Branch: feature/practice-tools-redesign (or successor)

## Goal

While the metronome plays — especially backgrounded — show session state (BPM,
time signature, play state, practice timer) on the lock screen and Dynamic
Island, with a play/pause control. Android behavior unchanged.

## Product decisions (locked with owner)

- **Auto-start:** activity starts on every play; "Live Activity" switch in
  Settings (DataStore-backed, default ON) disables it.
- **Hero content: adaptive.** Compact island + minimal view show the practice
  timer when one is running, otherwise the BPM number.
- **iOS floor: 17.0** for the feature (one interactive code path). The app
  itself stays at deployment target 16.0; all ActivityKit paths are gated
  `@available(iOS 17, *)`. Sub-17 users simply get no activity.
- **Controls: play/pause only.** No BPM ± (background-update bug would leave
  the displayed BPM stale while audio changes). No other buttons.

## Verified platform constraints (research, 2026-06-12)

1. **ActivityKit is Swift-only** — not callable from Kotlin/Native. Bridge =
   Kotlin interface implemented in Swift, injected at startup (pattern proven
   by Software Mansion kmp-live-activity).
2. **Widget extension must not link the KMP framework** — static `ComposeApp`
   + K/N GC exceeds the ~30 MB extension memory limit (JetBrains KT-66589).
   Extension is pure Swift; `ActivityAttributes` shared via dual target
   membership. No App Group needed.
3. **FB11683922 (unresolved through iOS 18):** `activity.update()` silently
   fails while the app is backgrounded with the `.playback` audio category —
   exactly this app. Consequence: the activity must be self-sufficient.
   Practice timer renders via `Text(timerInterval:)` /
   `ProgressView(timerInterval:)` (self-ticking, zero updates). BPM/play-state
   update on in-app changes and via App Intents (which run in-process and CAN
   update). Updates from background state observation are best-effort only.
4. **Per-beat animation categorically impossible** (update budget ~one per
   5–15 s on iOS 18; animation modifiers ignored; no continuous animations).
   Tempo is conveyed as the number.
5. iOS 17 App Intents (`AudioPlaybackIntent`) run in the app's process and can
   cold-launch it **without the UI scene** — Koin must not be
   composition-scoped on iOS.
6. `NSSupportsLiveActivities` missing from Info.plist today; must be added.
   Frequent-updates entitlement is push-only — not needed.
7. Extension bundle id must be prefixed: `com.merkost.metronome.widgets`.
   Automatic signing, team P47X2292CM, single existing target — adding a
   target is straightforward (objectVersion 56 project).

## Architecture

### Piece 1 — Koin restructure (prerequisite, separate commit)

- New `fun initKoin(liveActivityController: LiveActivityController)` in
  `shared/src/iosMain/.../di/` calling global
  `startKoin { modules(commonModule, iosModule, module { single { liveActivityController } }) }`.
- Called from `iOSApp.swift` `init()` (alongside `FirebaseApp.configure()`).
- `MainViewController.kt` switches from the `KoinApplication` composable to
  `KoinContext` over the global context.
- Android (`startKoin` in app shell) untouched. Full regression pass on both
  platforms before the feature work proceeds.

### Piece 2 — commonMain seam

- `interface LiveActivityController { fun start(snapshot); fun update(snapshot); fun end() }`
  in `com.merkost.metronome.platform` (mirrors `PlatformActions`).
- `data class LiveActivitySnapshot(isPlaying: Boolean, bpm: Int, tempoName: String,
  timeSignatureLabel: String, timerKind: TimerKind, timerStartEpochMillis: Long?,
  timerEndEpochMillis: Long?)` with `enum TimerKind { NONE, STOPWATCH, COUNTDOWN }`.
- No-op Android implementation registered in the Android module (future seam
  for an ongoing notification).
- `LiveActivityObserver` (Koin `single`, modeled on `MetronomeEngine`):
  collects `metronomeState`, `isPlaying`, `practiceTimerGoal`,
  `practiceTimerRemaining`, and the Live Activity settings flow; computes
  wall-clock anchors at play/pause transitions (countdown stores no end date:
  anchor = now + remaining; stopwatch: start = now − elapsed); debounces ~2 s
  with immediate flush on play/stop transitions; diffs against the last-pushed
  snapshot; honors the settings toggle (off → end + no-op). Warmed eagerly
  from `initKoin`.

### Piece 3 — Swift app target

- `MetronomeLiveActivityManager: LiveActivityController` (Kotlin protocol via
  the exported framework), all ActivityKit behind `@available(iOS 17, *)`;
  no-ops below 17. Handles request/update/end, sets
  `staleDate = now + 3 min` advanced on each update.
- `TogglePlaybackIntent: AudioPlaybackIntent` — resolves
  `MetronomeViewModel` from global Koin, calls `onPlayPauseClicked(...)`.
  (Works on cold launch because Koin now starts in `iOSApp.init`.)

### Piece 4 — Widget extension target

- `MetronomeWidgets` target, bundle `com.merkost.metronome.widgets`,
  deployment target 17.0, pure Swift/SwiftUI — no ComposeApp link, no gradle
  build phase, no Firebase.
- `MetronomeActivityAttributes` (ContentState mirrors `LiveActivitySnapshot`)
  with dual target membership (app + extension).
- Views: lock-screen banner; Dynamic Island expanded (leading: BPM large +
  tempo name + time signature; trailing: timer ring/text or hidden;
  bottom: play/pause button), compact (glyph + timer-or-BPM, adaptive),
  minimal (timer-or-BPM). Stale state (`isStale`) renders "Session ended".
  Session-state content only — no logo/branding. Verify legibility in
  StandBy red-tint with default treatment.
- `NSSupportsLiveActivities = YES` in the app's Info.plist.

## Lifecycle

- Start on play (toggle on, iOS ≥ 17). End on stop with short dismissal
  policy. Orphan sweep on every app launch: end all `Activity.activities`.
- staleDate ~3 min; stale UI says the session ended (covers swipe-kill —
  audio dies instantly; activity must not claim "playing" for hours).
- Countdown reaching 0:00 while locked freezes at 0:00 (platform limitation);
  the progress ring reads complete; reconciled on next in-app update.
- BPM changed in-app while activity live → debounced update (foreground
  updates work; background ones best-effort per FB11683922).

## Out of scope (v1)

- BPM ± buttons (stale-display trap), trainer/gap bar position
  (throttle-impossible), Watch `.supplementalActivityFamilies` layout,
  StandBy polish beyond defaults, APNs push updates, MPNowPlayingInfoCenter
  (separate product decision — occupies the media slot).

## Verification

- `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` +
  `:androidApp:assembleDebug` per commit; Android regression after the Koin
  restructure (app launches, settings work, metronome plays).
- Physical-device matrix (simulator actively misleading for LAs): activity
  appears on play; island on Pro-class hardware; timer ticks while locked;
  play/pause button toggles audio + visual state when locked; stale UI after
  force-kill; toggle off ends activity; orphan sweep on relaunch.

## Effort

~4–6 focused days. Sequencing: (1) Koin restructure commit; (2) seam +
observer commit (compiles, no-op everywhere); (3) Xcode targets + Swift
implementation; (4) device QA hardening.
