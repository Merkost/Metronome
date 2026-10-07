# iOS Live Activity + Dynamic Island Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Live Activity + Dynamic Island (iOS 17+) showing BPM/time-signature/play-state/practice-timer with a play/pause button, per spec `docs/superpowers/specs/2026-06-12-live-activity-design.md`.

**Architecture:** Kotlin `LiveActivityController` interface implemented in Swift via ActivityKit; global `startKoin` moved to `iOSApp.swift` init; pure-Swift widget extension (`com.merkost.metronome.widgets`, deployment 17.0) sharing `ActivityAttributes` + `AudioPlaybackIntent` by dual target membership with a `#if canImport(ComposeApp)` guard.

**Tech Stack:** Kotlin Multiplatform, Koin 4 (global context on iOS), ActivityKit, WidgetKit, AppIntents, SwiftUI. pbxproj edits scripted with the ruby `xcodeproj` gem (manual-Xcode fallback included).

**Testing note:** automated coverage = `:androidApp:assembleDebug`, `:shared:linkDebugFrameworkIosSimulatorArm64`, and `xcodebuild` simulator build. Live Activity behavior itself is device-only (simulator actively misleading) — final QA checklist at the bottom is run by the owner.

---

### Task 0: Commit pending fixes (clean baseline)

- [ ] **Step 1: Commit the two modified files**

```bash
git add shared/src/iosMain/kotlin/com/merkost/metronome/platform/IosPlatformActions.kt \
        shared/src/iosMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.ios.kt
git commit -m "fix: correct App Store id for rateApp, volume slider poll deference

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 1: Kotlin seam (interface, snapshot, bridge, initKoin) — no behavior change

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/platform/LiveActivityController.kt`
- Modify: `shared/src/androidMain/kotlin/com/merkost/metronome/di/AndroidModule.kt` (register no-op)
- Create: `shared/src/androidMain/kotlin/com/merkost/metronome/platform/NoopLiveActivityController.kt`
- Create: `shared/src/iosMain/kotlin/com/merkost/metronome/InitKoin.kt`
- Create: `shared/src/iosMain/kotlin/com/merkost/metronome/LiveActivityBridge.kt`

- [ ] **Step 1: Create the interface + snapshot (commonMain)**

```kotlin
package com.merkost.metronome.platform

enum class TimerKind { NONE, STOPWATCH, COUNTDOWN }

data class LiveActivitySnapshot(
    val isPlaying: Boolean,
    val bpm: Int,
    val tempoName: String,
    val timeSignatureLabel: String,
    val timerKind: TimerKind,
    val timerStartEpochMillis: Long?,
    val timerEndEpochMillis: Long?,
    val timerFrozenMillis: Long?,
)

interface LiveActivityController {
    fun start(snapshot: LiveActivitySnapshot)
    fun update(snapshot: LiveActivitySnapshot)
    fun end()
}
```

- [ ] **Step 2: Android no-op + registration**

`NoopLiveActivityController.kt`:
```kotlin
package com.merkost.metronome.platform

class NoopLiveActivityController : LiveActivityController {
    override fun start(snapshot: LiveActivitySnapshot) {}
    override fun update(snapshot: LiveActivitySnapshot) {}
    override fun end() {}
}
```
In `AndroidModule.kt` add to the existing `module { }` block (match import style):
```kotlin
single<LiveActivityController> { NoopLiveActivityController() }
```

- [ ] **Step 3: iOS initKoin (does NOT replace the existing path yet)**

`InitKoin.kt`:
```kotlin
package com.merkost.metronome

import com.merkost.metronome.di.commonModule
import com.merkost.metronome.di.iosModule
import com.merkost.metronome.platform.LiveActivityController
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin(liveActivityController: LiveActivityController) {
    startKoin {
        modules(
            commonModule,
            iosModule,
            module { single { liveActivityController } }
        )
    }
}
```
(Exports to Swift as `InitKoinKt.doInitKoin(liveActivityController:)` — ObjC init-family renaming.)

`LiveActivityBridge.kt`:
```kotlin
package com.merkost.metronome

import com.merkost.metronome.viewModels.MetronomeViewModel
import org.koin.mp.KoinPlatform

fun togglePlayback() {
    val viewModel = KoinPlatform.getKoin().get<MetronomeViewModel>()
    viewModel.onPlayPauseClicked(viewModel.isPlaying.value)
}
```

- [ ] **Step 4: Build both platforms**

```bash
./gradlew :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64
```
Expected: BUILD SUCCESSFUL. (App behavior unchanged — `MainViewController` still uses the `KoinApplication` composable; `initKoin` is dormant until Task 3.)

- [ ] **Step 5: Commit**

```bash
git add -A shared/src && git commit -m "feat: LiveActivityController seam and iOS initKoin entry point

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: Settings toggle + LiveActivityObserver

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/model/AppDatastore.kt` + `AppDatastoreImpl.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/SettingsViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.kt` + Android/iOS actuals + `SettingsScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/engine/LiveActivityObserver.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`, `shared/src/iosMain/kotlin/com/merkost/metronome/InitKoin.kt`

- [ ] **Step 1: DataStore setting (mirror the haptic pattern exactly)**

`AppDatastore.kt` interface:
```kotlin
val liveActivityEnabled: Flow<Boolean>
suspend fun saveLiveActivityEnabled(enabled: Boolean)
```
`AppDatastoreImpl.kt` (add `LIVE_ACTIVITY_ENABLED` key beside the existing keys, e.g. `booleanPreferencesKey("live_activity_enabled")`):
```kotlin
override val liveActivityEnabled: Flow<Boolean> = dataStore.data
    .map { preferences ->
        preferences[LIVE_ACTIVITY_ENABLED] ?: true
    }

override suspend fun saveLiveActivityEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
        preferences[LIVE_ACTIVITY_ENABLED] = enabled
    }
}
```

- [ ] **Step 2: SettingsViewModel (mirror haptic)**

```kotlin
val liveActivityEnabled = appDatastore.liveActivityEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

fun onLiveActivityChanged(enabled: Boolean) {
    viewModelScope.launch {
        appDatastore.saveLiveActivityEnabled(enabled)
    }
}
```

- [ ] **Step 3: iOS-only settings row via the established expect/actual pattern**

`PlatformSettingsComponents.kt` (commonMain), below `PlatformSwitch`:
```kotlin
@Composable
expect fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit)
```
Android actual (renders nothing):
```kotlin
@Composable
actual fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
}
```
iOS actual:
```kotlin
@Composable
actual fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingsSwitch(
        "Live Activity",
        checked,
        onCheckedChange,
        subtitle = "Tempo and timer on the Lock Screen",
    )
}
```
`SettingsScreen.kt`: collect `liveActivityEnabled` beside the other flows and add below the "Background Play" switch:
```kotlin
LiveActivitySettingsRow(liveActivityEnabled, viewModel::onLiveActivityChanged)
```

- [ ] **Step 4: LiveActivityObserver (commonMain, modeled on MetronomeEngine)**

```kotlin
package com.merkost.metronome.engine

import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.platform.LiveActivityController
import com.merkost.metronome.platform.LiveActivitySnapshot
import com.merkost.metronome.platform.TimerKind
import com.merkost.metronome.platform.currentTimeMillis
import com.merkost.metronome.viewModels.MetronomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class LiveActivityObserver(
    private val viewModel: MetronomeViewModel,
    private val controller: LiveActivityController,
    private val appDatastore: AppDatastore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastSent: LiveActivitySnapshot? = null
    private var active = false
    private var pushJob: kotlinx.coroutines.Job? = null

    fun start() {
        scope.launch {
            combine(
                viewModel.metronomeState,
                viewModel.practiceTimerGoal,
                viewModel.practiceTimerRemaining,
                appDatastore.liveActivityEnabled,
            ) { state, goal, remaining, enabled ->
                if (!enabled) null else snapshot(state, goal, remaining)
            }
                .distinctUntilChanged()
                .collect { snapshot -> dispatch(snapshot) }
        }
    }

    private fun snapshot(
        state: com.merkost.metronome.model.MetronomeState,
        goal: Long?,
        remaining: Long,
    ): LiveActivitySnapshot {
        val now = currentTimeMillis()
        val kind = when {
            goal != null -> TimerKind.COUNTDOWN
            state.stopWatchState.elapsedTime > 0 || state.playing -> TimerKind.STOPWATCH
            else -> TimerKind.NONE
        }
        return LiveActivitySnapshot(
            isPlaying = state.playing,
            bpm = state.rhythm,
            tempoName = state.tempoName,
            timeSignatureLabel = state.timeSignature.label,
            timerKind = kind,
            timerStartEpochMillis = if (state.playing && kind == TimerKind.STOPWATCH) {
                now - state.stopWatchState.elapsedTime
            } else null,
            timerEndEpochMillis = if (state.playing && kind == TimerKind.COUNTDOWN) {
                now + remaining
            } else null,
            timerFrozenMillis = if (!state.playing) {
                when (kind) {
                    TimerKind.COUNTDOWN -> remaining
                    TimerKind.STOPWATCH -> state.stopWatchState.elapsedTime
                    TimerKind.NONE -> null
                }
            } else null,
        )
    }

    private fun dispatch(snapshot: LiveActivitySnapshot?) {
        pushJob?.cancel()
        if (snapshot == null) {
            if (active) {
                controller.end()
                active = false
                lastSent = null
            }
            return
        }
        val playTransition = snapshot.isPlaying != (lastSent?.isPlaying ?: false)
        pushJob = scope.launch {
            if (!playTransition) delay(1500)
            push(snapshot)
        }
    }

    private fun push(snapshot: LiveActivitySnapshot) {
        when {
            snapshot.isPlaying && !active -> {
                controller.start(snapshot)
                active = true
            }
            !snapshot.isPlaying && active && snapshot.timerKind == TimerKind.NONE -> {
                controller.end()
                active = false
            }
            active -> controller.update(snapshot)
            else -> return
        }
        lastSent = snapshot
    }
}
```
Note on semantics: pause with a timer running keeps the activity alive (frozen timer); stop with no timer ends it. Debounce 1500 ms for non-transition changes (BPM scrubbing) via `collectLatest`-style job cancellation; play/stop transitions flush immediately.

The timer-flow names above came from repo recon (`practiceTimerGoal: StateFlow<Long?>` line ~278, `practiceTimerRemaining: StateFlow<Long>` line ~279 in MetronomeViewModel) — verify exact names/nullability when editing and adapt mechanically if they differ.

- [ ] **Step 5: Register + warm (iOS only)**

`CommonModule.kt` add:
```kotlin
single { LiveActivityObserver(get(), get(), get()) }
```
`InitKoin.kt` — warm after `startKoin`:
```kotlin
fun initKoin(liveActivityController: LiveActivityController) {
    startKoin {
        modules(
            commonModule,
            iosModule,
            module { single { liveActivityController } }
        )
    }
    KoinPlatform.getKoin().get<LiveActivityObserver>().start()
}
```
(`import org.koin.mp.KoinPlatform`, `import com.merkost.metronome.engine.LiveActivityObserver`. Android never resolves the observer single — `LiveActivityController` IS resolvable there via the no-op, so even accidental warm-up is safe.)

- [ ] **Step 6: Build both platforms, commit**

```bash
./gradlew :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64
git add -A shared/src && git commit -m "feat: live activity observer, settings toggle, snapshot pipeline

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 3: Swift + Xcode (the flip)

**Files:**
- Create: `iosApp/iosApp/LiveActivity/MetronomeActivityAttributes.swift` (app + extension membership)
- Create: `iosApp/iosApp/LiveActivity/TogglePlaybackIntent.swift` (app + extension membership)
- Create: `iosApp/iosApp/LiveActivity/MetronomeLiveActivityManager.swift` (app only)
- Create: `iosApp/MetronomeWidgets/MetronomeWidgetsBundle.swift`, `iosApp/MetronomeWidgets/MetronomeLiveActivityWidget.swift`, `iosApp/MetronomeWidgets/Info.plist` (extension only)
- Modify: `iosApp/iosApp/Info.plist` (NSSupportsLiveActivities), `iosApp/iosApp/iOSApp.swift`, `shared/src/iosMain/kotlin/com/merkost/metronome/MainViewController.kt`
- Modify: `iosApp/iosApp.xcodeproj/project.pbxproj` (scripted)

- [ ] **Step 1: Write the Swift files**

`MetronomeActivityAttributes.swift`:
```swift
import ActivityKit
import Foundation

struct MetronomeActivityAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        var isPlaying: Bool
        var bpm: Int
        var tempoName: String
        var timeSignature: String
        var timerKind: String
        var timerStart: Date?
        var timerEnd: Date?
        var timerFrozenSeconds: Int?
    }
}
```

`TogglePlaybackIntent.swift`:
```swift
import AppIntents
#if canImport(ComposeApp)
import ComposeApp
#endif

struct TogglePlaybackIntent: AudioPlaybackIntent {
    static var title: LocalizedStringResource = "Play or pause metronome"

    func perform() async throws -> some IntentResult {
        #if canImport(ComposeApp)
        LiveActivityBridgeKt.togglePlayback()
        #endif
        return .result()
    }
}
```

`MetronomeLiveActivityManager.swift`:
```swift
import ActivityKit
import ComposeApp
import Foundation

final class MetronomeLiveActivityManager: LiveActivityController {
    init() {
        end()
    }

    func start(snapshot: LiveActivitySnapshot) {
        guard #available(iOS 17.0, *) else { return }
        Task { @MainActor in
            guard ActivityAuthorizationInfo().areActivitiesEnabled else { return }
            if !Activity<MetronomeActivityAttributes>.activities.isEmpty {
                await Self.push(snapshot)
                return
            }
            _ = try? Activity.request(
                attributes: MetronomeActivityAttributes(),
                content: Self.content(snapshot)
            )
        }
    }

    func update(snapshot: LiveActivitySnapshot) {
        guard #available(iOS 17.0, *) else { return }
        Task { @MainActor in
            await Self.push(snapshot)
        }
    }

    func end() {
        guard #available(iOS 17.0, *) else { return }
        Task { @MainActor in
            for activity in Activity<MetronomeActivityAttributes>.activities {
                await activity.end(activity.content, dismissalPolicy: .immediate)
            }
        }
    }

    @available(iOS 17.0, *)
    @MainActor
    private static func push(_ snapshot: LiveActivitySnapshot) async {
        for activity in Activity<MetronomeActivityAttributes>.activities {
            await activity.update(content(snapshot))
        }
    }

    @available(iOS 17.0, *)
    private static func content(_ s: LiveActivitySnapshot) -> ActivityContent<MetronomeActivityAttributes.ContentState> {
        ActivityContent(
            state: MetronomeActivityAttributes.ContentState(
                isPlaying: s.isPlaying,
                bpm: Int(truncating: s.bpm as NSNumber),
                tempoName: s.tempoName,
                timeSignature: s.timeSignatureLabel,
                timerKind: s.timerKind.name.lowercased(),
                timerStart: s.timerStartEpochMillis.map { Date(timeIntervalSince1970: $0.doubleValue / 1000.0) },
                timerEnd: s.timerEndEpochMillis.map { Date(timeIntervalSince1970: $0.doubleValue / 1000.0) },
                timerFrozenSeconds: s.timerFrozenMillis.map { Int(truncating: $0) / 1000 }
            ),
            staleDate: Date().addingTimeInterval(180)
        )
    }
}
```
(K/N export note: `LiveActivitySnapshot.bpm: Int` arrives as `Int32` in Swift — if `Int(truncating:)` complains, use `Int(s.bpm)`. `Long?` arrives as `KotlinLong?`; `.map { $0.doubleValue }` is correct. The implementer adapts mechanically to whatever the exported header shows.)

`MetronomeWidgetsBundle.swift`:
```swift
import SwiftUI
import WidgetKit

@main
struct MetronomeWidgetsBundle: WidgetBundle {
    var body: some Widget {
        MetronomeLiveActivityWidget()
    }
}
```

`MetronomeLiveActivityWidget.swift`:
```swift
import ActivityKit
import SwiftUI
import WidgetKit

struct MetronomeLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: MetronomeActivityAttributes.self) { context in
            LockScreenView(state: context.state, isStale: context.isStale)
                .activityBackgroundTint(Color.black.opacity(0.55))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("\(context.state.bpm)")
                            .font(.system(size: 34, weight: .heavy, design: .rounded))
                        Text("\(context.state.tempoName) · \(context.state.timeSignature)")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.leading, 4)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    TimerView(state: context.state)
                        .font(.system(.title3, design: .rounded).weight(.semibold))
                        .frame(maxWidth: 64)
                        .padding(.trailing, 4)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Button(intent: TogglePlaybackIntent()) {
                        Label(
                            context.state.isPlaying ? "Pause" : "Play",
                            systemImage: context.state.isPlaying ? "pause.fill" : "play.fill"
                        )
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .tint(.accentColor)
                }
            } compactLeading: {
                Image(systemName: context.state.isPlaying ? "play.fill" : "pause.fill")
            } compactTrailing: {
                CompactValueView(state: context.state)
            } minimal: {
                CompactValueView(state: context.state)
            }
        }
    }
}

private struct CompactValueView: View {
    let state: MetronomeActivityAttributes.ContentState

    var body: some View {
        if let view = timerText {
            view.monospacedDigit().frame(maxWidth: 52)
        } else {
            Text("\(state.bpm)")
        }
    }

    private var timerText: Text? {
        if state.isPlaying, state.timerKind == "countdown", let end = state.timerEnd {
            return Text(timerInterval: Date.now...max(end, Date.now), countsDown: true)
        }
        if state.isPlaying, state.timerKind == "stopwatch", let start = state.timerStart {
            return Text(timerInterval: start...Date.distantFuture, countsDown: false)
        }
        if let frozen = state.timerFrozenSeconds, state.timerKind != "none" {
            return Text(Self.format(frozen))
        }
        return nil
    }

    static func format(_ seconds: Int) -> String {
        String(format: "%d:%02d", seconds / 60, seconds % 60)
    }
}

private struct TimerView: View {
    let state: MetronomeActivityAttributes.ContentState

    var body: some View {
        if state.timerKind == "none" {
            EmptyView()
        } else if state.isPlaying, state.timerKind == "countdown", let end = state.timerEnd {
            ProgressView(timerInterval: Date.now...max(end, Date.now)) {
                EmptyView()
            } currentValueLabel: {
                Text(timerInterval: Date.now...max(end, Date.now), countsDown: true)
            }
            .progressViewStyle(.circular)
        } else if state.isPlaying, state.timerKind == "stopwatch", let start = state.timerStart {
            Text(timerInterval: start...Date.distantFuture, countsDown: false)
        } else if let frozen = state.timerFrozenSeconds {
            Text(CompactValueView.format(frozen))
        }
    }
}

private struct LockScreenView: View {
    let state: MetronomeActivityAttributes.ContentState
    let isStale: Bool

    var body: some View {
        if isStale {
            Text("Session ended")
                .font(.callout)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity)
                .padding()
        } else {
            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(state.bpm) BPM")
                        .font(.system(size: 28, weight: .heavy, design: .rounded))
                    Text("\(state.tempoName) · \(state.timeSignature)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                TimerView(state: state)
                    .font(.system(.title3, design: .rounded).weight(.semibold))
                Button(intent: TogglePlaybackIntent()) {
                    Image(systemName: state.isPlaying ? "pause.fill" : "play.fill")
                        .font(.title3)
                }
                .buttonStyle(.bordered)
                .clipShape(Circle())
            }
            .padding()
        }
    }
}
```

`iosApp/MetronomeWidgets/Info.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CFBundleDevelopmentRegion</key>
	<string>en</string>
	<key>CFBundleDisplayName</key>
	<string>MetronomeWidgets</string>
	<key>CFBundleExecutable</key>
	<string>$(EXECUTABLE_NAME)</string>
	<key>CFBundleIdentifier</key>
	<string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>
	<key>CFBundleInfoDictionaryVersion</key>
	<string>6.0</string>
	<key>CFBundleName</key>
	<string>$(PRODUCT_NAME)</string>
	<key>CFBundlePackageType</key>
	<string>XPC!</string>
	<key>CFBundleShortVersionString</key>
	<string>1.0</string>
	<key>CFBundleVersion</key>
	<string>1</string>
	<key>NSExtension</key>
	<dict>
		<key>NSExtensionPointIdentifier</key>
		<string>com.apple.widgetkit-extension</string>
	</dict>
</dict>
</plist>
```

- [ ] **Step 2: App Info.plist + Swift entry + MainViewController flip**

`iosApp/iosApp/Info.plist` — add under the top-level dict:
```xml
	<key>NSSupportsLiveActivities</key>
	<true/>
```

`iOSApp.swift` — call Koin init before any UI:
```swift
import SwiftUI
import FirebaseCore
import ComposeApp

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        InitKoinKt.doInitKoin(liveActivityController: MetronomeLiveActivityManager())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```
(Adapt mechanically to the file's existing structure; the recon-verified current shape is `FirebaseApp.configure()` in `init()` + `WindowGroup { ContentView() }`. If the Swift compiler names the exported function differently — check the generated `ComposeApp-Swift` interface — use the name it exports.)

`MainViewController.kt` — replace `KoinApplication(configuration = ...)` with global-context usage:
```kotlin
package com.merkost.metronome

import androidx.compose.ui.window.ComposeUIViewController
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.ui.theme.MetronomeTheme
import org.koin.compose.KoinContext

fun MainViewController() = ComposeUIViewController {
    KoinContext {
        MetronomeTheme {
            AppNavigation()
        }
    }
}
```

- [ ] **Step 3: Script the pbxproj changes**

```bash
gem install xcodeproj --user-install
```
If install fails (network/permissions), STOP and use the manual fallback below. Otherwise run this script from the repo root (`ruby add_widget_target.rb`, file deleted after):

```ruby
require 'xcodeproj'

proj = Xcodeproj::Project.open('iosApp/iosApp.xcodeproj')
app = proj.targets.find { |t| t.name == 'Metronome' }

ext = proj.new_target(:app_extension, 'MetronomeWidgets', :ios, '17.0')
ext.build_configurations.each do |c|
  c.build_settings['PRODUCT_BUNDLE_IDENTIFIER'] = 'com.merkost.metronome.widgets'
  c.build_settings['SWIFT_VERSION'] = '5.0'
  c.build_settings['INFOPLIST_FILE'] = 'MetronomeWidgets/Info.plist'
  c.build_settings['GENERATE_INFOPLIST_FILE'] = 'NO'
  c.build_settings['CODE_SIGN_STYLE'] = 'Automatic'
  c.build_settings['DEVELOPMENT_TEAM'] = 'P47X2292CM'
  c.build_settings['TARGETED_DEVICE_FAMILY'] = '1,2'
  c.build_settings['CURRENT_PROJECT_VERSION'] = '1'
  c.build_settings['MARKETING_VERSION'] = '1.0'
  c.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '17.0'
end

widgets = proj.main_group.new_group('MetronomeWidgets', 'MetronomeWidgets')
bundle_ref = widgets.new_file('MetronomeWidgetsBundle.swift')
widget_ref = widgets.new_file('MetronomeLiveActivityWidget.swift')
widgets.new_file('Info.plist')
ext.add_file_references([bundle_ref, widget_ref])

ios_app_group = proj.main_group.children.find { |g| g.display_name == 'iosApp' }
la = ios_app_group.new_group('LiveActivity', 'LiveActivity')
attrs_ref = la.new_file('MetronomeActivityAttributes.swift')
intent_ref = la.new_file('TogglePlaybackIntent.swift')
manager_ref = la.new_file('MetronomeLiveActivityManager.swift')
app.add_file_references([attrs_ref, intent_ref, manager_ref])
ext.add_file_references([attrs_ref, intent_ref])

app.add_dependency(ext)
embed = app.new_copy_files_build_phase('Embed Foundation Extensions')
embed.symbol_dst_subfolder_spec = :plug_ins
build_file = embed.add_file_reference(ext.product_reference)
build_file.settings = { 'ATTRIBUTES' => ['RemoveHeadersOnCopy'] }

proj.save
```
Path caveat: group paths are relative to the parent group's path. After saving, verify with `xcodebuild -list -project iosApp/iosApp.xcodeproj` (expect targets Metronome + MetronomeWidgets) and fix paths if the build can't find sources.

**Manual fallback (user does this in Xcode, ~5 min):** File → New → Target → Widget Extension, name `MetronomeWidgets`, bundle `com.merkost.metronome.widgets`, deployment 17.0, no configuration intent, don't activate scheme; delete the template files; add the five Swift files with the memberships listed above (attributes + intent → both targets; manager → app; bundle/widget → extension); replace the generated Info.plist with the one above; confirm the app target gained the embed phase + dependency.

- [ ] **Step 4: Verify everything builds**

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
xcodebuild -project iosApp/iosApp.xcodeproj -scheme Metronome -configuration Debug \
  -destination 'generic/platform=iOS Simulator' build | tail -5
```
Expected: `** BUILD SUCCEEDED **` (this compiles BOTH targets and runs the Kotlin gradle phase). Iterate on compile errors — K/N-export name mismatches in the Swift files are expected and mechanical.

- [ ] **Step 5: Commit**

```bash
git add -A iosApp shared/src/iosMain && git commit -m "feat: iOS Live Activity with Dynamic Island and play/pause intent

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 4: Docs + roadmap

- [ ] **Step 1: ROADMAP.md** — move "Live Activity / Dynamic Island timer (iOS)" from Later into Shipped (note: device QA pending), keep Android Glance widget under Later.
- [ ] **Step 2: Commit**

```bash
git add docs/ROADMAP.md && git commit -m "docs: live activity shipped in roadmap

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

## Device QA checklist (owner, physical iPhone — Pro-class for the island)

- [ ] Play → activity appears (lock screen + island); stop → disappears promptly
- [ ] Compact island: BPM when no timer; countdown/stopwatch ticking when timer active (lock the phone — timer must keep ticking)
- [ ] Expanded island + lock screen: BPM/tempo-name/signature correct; play/pause button toggles audio AND flips the button state while locked
- [ ] Pause with timer → frozen time shown; resume → ticking resumes from the right place
- [ ] BPM change in-app while activity live → island updates within ~2 s
- [ ] Force-kill app mid-play → activity shows "Session ended" within ~3 min; relaunch → orphan cleaned
- [ ] Settings toggle off → activity ends and never reappears; on → works again
- [ ] StandBy (charging, landscape): legible, no clipped content
- [ ] Settings screen on Android unchanged (no Live Activity row)

## Self-review

- **Spec coverage:** seam+snapshot (T1), Koin restructure (T1/T3 split so every commit builds), observer+debounce+anchors+toggle (T2), attributes/manager/intent/extension/plist/views incl. adaptive compact + stale UI (T3), lifecycle (orphan sweep in manager `init`, staleDate 180 s, immediate end), roadmap (T4), device matrix (checklist). `timerFrozenMillis` added to the snapshot (spec addendum — paused display).
- **Placeholders:** none; all file contents complete. Two flagged mechanical-adaptation points (K/N export names, recon-sourced flow names) are verification instructions, not gaps.
- **Type consistency:** `LiveActivitySnapshot` fields match `ContentState` 1:1 (epochs→Dates, millis→seconds at the bridge); `TimerKind.name.lowercased()` ↔ the string switch in views; `togglePlayback()` ↔ `onPlayPauseClicked(isPlaying.value)`.
