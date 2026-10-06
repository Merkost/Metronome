# Cedar Migration + iOS Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Timber with Cedar (KMP logging), add GitLive KMP Crashlytics with a shared CrashlyticsTree, and fix iOS code quality issues (thread safety, error logging, device info, lifecycle cleanup).

**Architecture:** Cedar + GitLive Crashlytics go in shared/commonMain so both platforms get logging and crash reporting. A shared `CedarSetup` object initializes logging per build type. iOS-specific fixes use Cedar for error logging. Android keeps native Firebase SDKs (wrapped by GitLive on Android, SPM on iOS).

**Tech Stack:** Cedar Logger 0.2.0, GitLive firebase-crashlytics 2.4.0, Koin DI, Kotlin Multiplatform

**Spec:** `docs/superpowers/specs/2026-04-07-cedar-migration-ios-fixes-design.md`

---

## File Structure

| File | Action | Responsibility |
|------|--------|---------------|
| `gradle/libs.versions.toml` | Modify | Add cedar + gitlive-crashlytics versions, remove timber |
| `shared/build.gradle.kts` | Modify | Add cedar + gitlive-crashlytics to commonMain, remove timber from androidMain |
| `androidApp/build.gradle.kts` | Modify | Remove timber dependency |
| `shared/src/commonMain/.../logging/CrashlyticsTree.kt` | Create | LogTree that forwards WARNING/ERROR to Firebase Crashlytics |
| `shared/src/commonMain/.../logging/CedarSetup.kt` | Create | Shared logging initialization |
| `shared/src/commonMain/.../platform/PlatformUtils.kt` | Modify | Add expect fun isDebug() |
| `shared/src/androidMain/.../platform/PlatformUtils.android.kt` | Modify | Add actual fun isDebug() |
| `shared/src/iosMain/.../platform/PlatformUtils.ios.kt` | Modify | Add actual fun isDebug() |
| `shared/src/commonMain/.../di/CommonModule.kt` | Modify | Add onClose for MetronomeEngine, call CedarSetup |
| `androidApp/.../MetronomeApp.kt` | Modify | Replace Timber with CedarSetup |
| `androidApp/.../MainActivity.kt` | Modify | Replace Timber with Cedar |
| `shared/src/androidMain/.../components/NotificationPermission.kt` | Modify | Replace Timber with Cedar |
| `shared/src/iosMain/.../engine/MetronomePlayerIos.kt` | Modify | Add @Volatile, add Cedar error logging |
| `shared/src/iosMain/.../platform/IosPlatformActions.kt` | Modify | Add device info to support email |
| `shared/src/iosMain/.../MainViewController.kt` | Modify | Remove direct Koin init (now in CommonModule) |

---

### Task 1: Update dependencies in version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add Cedar and GitLive Crashlytics, remove Timber**

In `gradle/libs.versions.toml`, add new versions and library entries, remove timber:

In the `[versions]` section, remove:
```
timber = "5.0.1"
```

Add:
```
cedar = "0.2.0"
gitliveCrashlytics = "2.4.0"
```

In the `[libraries]` section, remove:
```
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }
```

Add:
```
cedar-logging = { module = "org.kimplify:cedar-logging", version.ref = "cedar" }
gitlive-crashlytics = { module = "dev.gitlive:firebase-crashlytics", version.ref = "gitliveCrashlytics" }
```

- [ ] **Step 2: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: add cedar + gitlive-crashlytics deps, remove timber from catalog"
```

---

### Task 2: Update Gradle build files

**Files:**
- Modify: `shared/build.gradle.kts`
- Modify: `androidApp/build.gradle.kts`

- [ ] **Step 1: Add Cedar and GitLive Crashlytics to shared/commonMain, remove Timber from androidMain**

In `shared/build.gradle.kts`, add to the `commonMain.dependencies` block (after the existing coroutines line):

```kotlin
implementation(libs.cedar.logging)
implementation(libs.gitlive.crashlytics)
```

In the `androidMain.dependencies` block, remove:
```kotlin
implementation(libs.timber)
```

- [ ] **Step 2: Remove Timber from androidApp**

In `androidApp/build.gradle.kts`, in the `dependencies` block, remove:
```kotlin
implementation(libs.timber)
```

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :shared:compileKotlinAndroid :androidApp:compileDebugKotlin 2>&1 | tail -5`

Expected: BUILD SUCCESSFUL (with errors in source files that still reference Timber — that's expected at this stage)

- [ ] **Step 4: Commit**

```bash
git add shared/build.gradle.kts androidApp/build.gradle.kts
git commit -m "chore: wire cedar + gitlive-crashlytics in gradle, remove timber"
```

---

### Task 3: Add isDebug expect/actual

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/platform/PlatformUtils.kt`
- Modify: `shared/src/androidMain/kotlin/com/merkost/metronome/platform/PlatformUtils.android.kt`
- Modify: `shared/src/iosMain/kotlin/com/merkost/metronome/platform/PlatformUtils.ios.kt`

- [ ] **Step 1: Add expect declaration**

In `shared/src/commonMain/kotlin/com/merkost/metronome/platform/PlatformUtils.kt`, add after the existing `expect fun currentTimeMillis(): Long`:

```kotlin
expect fun isDebug(): Boolean
```

- [ ] **Step 2: Add Android actual**

In `shared/src/androidMain/kotlin/com/merkost/metronome/platform/PlatformUtils.android.kt`, add after the existing actual function:

```kotlin
actual fun isDebug(): Boolean = com.merkost.metronome.BuildConfig.DEBUG
```

Note: This uses the shared module's BuildConfig. The shared module already has `buildFeatures { buildConfig = true }` in its android block. If `BuildConfig.DEBUG` is not available in the shared module, use `android.os.Build.TYPE == "userdebug"` won't work. Instead, we'll check if it's available — if not, we'll pass it from androidApp. But since the shared module has `buildFeatures.compose = true` and is an Android library, `BuildConfig` should be generated.

Actually, Android library modules don't generate a `DEBUG` field in BuildConfig by default. A simpler approach: check if the app is debuggable at runtime.

```kotlin
actual fun isDebug(): Boolean = false
```

We'll override this properly in Task 6 when we wire up MetronomeApp. For now use `false` as a safe default — release behavior.

Better approach — use a mutable flag set at app startup:

In `shared/src/androidMain/kotlin/com/merkost/metronome/platform/PlatformUtils.android.kt`:

```kotlin
actual fun currentTimeMillis(): Long = System.currentTimeMillis()

var debugFlag: Boolean = false

actual fun isDebug(): Boolean = debugFlag
```

- [ ] **Step 3: Add iOS actual**

In `shared/src/iosMain/kotlin/com/merkost/metronome/platform/PlatformUtils.ios.kt`, add:

```kotlin
actual fun isDebug(): Boolean = Platform.isDebugBinary
```

The full file becomes:

```kotlin
package com.merkost.metronome.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlin.native.Platform

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun isDebug(): Boolean = Platform.isDebugBinary
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/metronome/platform/PlatformUtils.kt \
       shared/src/androidMain/kotlin/com/merkost/metronome/platform/PlatformUtils.android.kt \
       shared/src/iosMain/kotlin/com/merkost/metronome/platform/PlatformUtils.ios.kt
git commit -m "feat: add isDebug expect/actual for platform-specific debug detection"
```

---

### Task 4: Create CrashlyticsTree and CedarSetup in commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/logging/CrashlyticsTree.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/logging/CedarSetup.kt`

- [ ] **Step 1: Create CrashlyticsTree**

Create `shared/src/commonMain/kotlin/com/merkost/metronome/logging/CrashlyticsTree.kt`:

```kotlin
package com.merkost.metronome.logging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import org.kimplify.cedar.LogPriority
import org.kimplify.cedar.LogTree

class CrashlyticsTree : LogTree {
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        if (priority < LogPriority.WARNING) return
        val crashlytics = Firebase.crashlytics
        crashlytics.log("${priority.name.first()}/$tag: $message")
        if (throwable != null) {
            crashlytics.recordException(throwable)
        }
    }
}
```

- [ ] **Step 2: Create CedarSetup**

Create `shared/src/commonMain/kotlin/com/merkost/metronome/logging/CedarSetup.kt`:

```kotlin
package com.merkost.metronome.logging

import org.kimplify.cedar.Cedar
import org.kimplify.cedar.ConsoleTree

object CedarSetup {
    fun initialize(isDebug: Boolean) {
        Cedar.clearForest()
        if (isDebug) {
            Cedar.plant(ConsoleTree())
        } else {
            Cedar.plant(CrashlyticsTree())
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/metronome/logging/CrashlyticsTree.kt \
       shared/src/commonMain/kotlin/com/merkost/metronome/logging/CedarSetup.kt
git commit -m "feat: add CrashlyticsTree and CedarSetup in commonMain"
```

---

### Task 5: Wire up CedarSetup in CommonModule and add lifecycle cleanup

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`

- [ ] **Step 1: Update CommonModule**

Replace the full content of `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt` with:

```kotlin
package com.merkost.metronome.di

import com.merkost.metronome.engine.MetronomeEngine
import com.merkost.metronome.logging.CedarSetup
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.platform.isDebug
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    single<AppDatastore> { AppDatastoreImpl(get()) }
    single { MetronomeViewModel(get(), get()) }
    single {
        CedarSetup.initialize(isDebug())
        MetronomeEngine(get(), get(), get()).also { it.start() }
    } onClose { it?.release() }
    viewModel { SettingsViewModel(get()) }
}
```

Key changes:
- `CedarSetup.initialize(isDebug())` is called when the MetronomeEngine single is created (first module to init, guarantees logging is ready)
- `onClose { it?.release() }` ensures AVAudioEngine/SoundPool resources are cleaned up when Koin tears down

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt
git commit -m "feat: initialize Cedar in CommonModule, add onClose lifecycle cleanup"
```

---

### Task 6: Replace Timber in Android code

**Files:**
- Modify: `androidApp/src/main/kotlin/com/merkost/metronome/app/MetronomeApp.kt`
- Modify: `androidApp/src/main/kotlin/com/merkost/metronome/MainActivity.kt`
- Modify: `shared/src/androidMain/kotlin/com/merkost/metronome/components/NotificationPermission.kt`

- [ ] **Step 1: Update MetronomeApp.kt**

Replace the full content of `androidApp/src/main/kotlin/com/merkost/metronome/app/MetronomeApp.kt` with:

```kotlin
package com.merkost.metronome.app

import android.app.Application
import com.merkost.metronome.android.BuildConfig
import com.merkost.metronome.di.androidModule
import com.merkost.metronome.di.commonModule
import com.merkost.metronome.platform.debugFlag
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MetronomeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        debugFlag = BuildConfig.DEBUG

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@MetronomeApp)
            modules(commonModule, androidModule)
        }
    }
}
```

Key changes:
- Removed all Timber imports and tree planting
- Removed the `CrashReportingTree` inner class (replaced by shared `CrashlyticsTree`)
- Sets `debugFlag` before Koin starts so `isDebug()` returns the correct value when `CedarSetup.initialize()` runs inside CommonModule

- [ ] **Step 2: Update MainActivity.kt**

In `androidApp/src/main/kotlin/com/merkost/metronome/MainActivity.kt`:

Replace the import:
```kotlin
import timber.log.Timber
```
with:
```kotlin
import org.kimplify.cedar.Cedar
```

Replace line 60:
```kotlin
Timber.tag("BOUND_SERVICE").d(metronomeService.toString())
```
with:
```kotlin
Cedar.tag("BOUND_SERVICE").d(metronomeService.toString())
```

- [ ] **Step 3: Update NotificationPermission.kt**

In `shared/src/androidMain/kotlin/com/merkost/metronome/components/NotificationPermission.kt`:

Replace the import:
```kotlin
import timber.log.Timber
```
with:
```kotlin
import org.kimplify.cedar.Cedar
```

Replace line 121:
```kotlin
Timber.w(e, "startNotificationSettings")
```
with:
```kotlin
Cedar.tag("NotificationPermission").w("startNotificationSettings")
```

Also remove the unused import:
```kotlin
import android.util.Log
```

- [ ] **Step 4: Verify Android build**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/kotlin/com/merkost/metronome/app/MetronomeApp.kt \
       androidApp/src/main/kotlin/com/merkost/metronome/MainActivity.kt \
       shared/src/androidMain/kotlin/com/merkost/metronome/components/NotificationPermission.kt
git commit -m "refactor: replace Timber with Cedar across Android code"
```

---

### Task 7: Fix MetronomePlayerIos — thread safety + error logging

**Files:**
- Modify: `shared/src/iosMain/kotlin/com/merkost/metronome/engine/MetronomePlayerIos.kt`

- [ ] **Step 1: Add @Volatile and Cedar error logging**

Replace the full content of `shared/src/iosMain/kotlin/com/merkost/metronome/engine/MetronomePlayerIos.kt` with:

```kotlin
package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import kotlinx.cinterop.ExperimentalForeignApi
import org.kimplify.cedar.Cedar
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeBufferInterrupts
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioUnitVarispeed
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import kotlin.math.max

@OptIn(ExperimentalForeignApi::class)
class MetronomePlayerIos : MetronomePlayer {
    @Volatile
    private var audioEngine: AVAudioEngine? = null
    @Volatile
    private var playerNode: AVAudioPlayerNode? = null
    @Volatile
    private var varispeedNode: AVAudioUnitVarispeed? = null
    @Volatile
    private var audioBuffer: AVAudioPCMBuffer? = null

    override fun initialize(initialSound: ClickSound) {
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AVAudioSession.sharedInstance().setActive(true, error = null)

        val (name, ext) = soundFileInfo(initialSound)
        val url = NSBundle.mainBundle.URLForResource(name, withExtension = ext)
        if (url == null) {
            Cedar.tag("MetronomePlayerIos").e("Audio resource not found: $name.$ext")
            return
        }
        val audioFile = AVAudioFile(forReading = url, error = null)

        val frameCount = audioFile.length.toUInt()
        val buffer = AVAudioPCMBuffer(
            pCMFormat = audioFile.processingFormat,
            frameCapacity = frameCount
        )
        audioFile.readIntoBuffer(buffer, error = null)
        audioBuffer = buffer

        val engine = AVAudioEngine()
        val player = AVAudioPlayerNode()
        val varispeed = AVAudioUnitVarispeed()

        engine.attachNode(player)
        engine.attachNode(varispeed)

        val format = audioFile.processingFormat
        engine.connect(player, varispeed, format)
        engine.connect(varispeed, engine.mainMixerNode, format)

        if (!engine.startAndReturnError(null)) {
            Cedar.tag("MetronomePlayerIos").e("Failed to start audio engine")
            return
        }
        player.play()

        audioEngine = engine
        playerNode = player
        varispeedNode = varispeed
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        val buffer = audioBuffer ?: return
        val player = playerNode ?: return

        varispeedNode?.rate = beat.rate

        val volume = max(stereoLeft, stereoRight)
        val pan = if (stereoLeft + stereoRight > 0f) {
            (stereoRight - stereoLeft) / max(stereoLeft, stereoRight)
        } else {
            0f
        }

        player.volume = volume
        player.pan = pan

        if (!player.playing) {
            player.play()
        }

        player.scheduleBuffer(
            buffer,
            atTime = null,
            options = AVAudioPlayerNodeBufferInterrupts,
            completionHandler = null
        )
    }

    override fun stop() {
        playerNode?.stop()
    }

    override fun release() {
        playerNode?.stop()
        audioEngine?.stop()
        audioEngine = null
        playerNode = null
        varispeedNode = null
        audioBuffer = null
    }

    private fun soundFileInfo(sound: ClickSound): Pair<String, String> = when (sound) {
        ClickSound.WOOD -> "wood" to "mp3"
        ClickSound.CLICK -> "click" to "mp3"
        ClickSound.CLASSIC -> "metronome" to "wav"
    }

    override fun switchSound(sound: ClickSound) {
        val (name, ext) = soundFileInfo(sound)
        val url = NSBundle.mainBundle.URLForResource(name, withExtension = ext)
        if (url == null) {
            Cedar.tag("MetronomePlayerIos").e("Audio resource not found: $name.$ext")
            return
        }
        val audioFile = AVAudioFile(forReading = url, error = null)
        val frameCount = audioFile.length.toUInt()
        val newFormat = audioFile.processingFormat
        val buffer = AVAudioPCMBuffer(pCMFormat = newFormat, frameCapacity = frameCount)
        audioFile.readIntoBuffer(buffer, error = null)

        val engine = audioEngine ?: return
        val player = playerNode ?: return
        val varispeed = varispeedNode ?: return

        val wasRunning = engine.running
        player.stop()
        engine.stop()

        engine.disconnectNodeOutput(player)
        engine.disconnectNodeOutput(varispeed)
        engine.connect(player, varispeed, newFormat)
        engine.connect(varispeed, engine.mainMixerNode, newFormat)

        audioBuffer = buffer

        if (wasRunning) {
            if (!engine.startAndReturnError(null)) {
                Cedar.tag("MetronomePlayerIos").e("Failed to restart audio engine after sound switch")
                return
            }
            player.play()
        }
    }
}
```

Changes from original:
- Added `@Volatile` to all four mutable state fields (lines 22-29)
- `initialize()`: Log and return early when audio resource not found (line 36-39)
- `initialize()`: Check `startAndReturnError` return value, log on failure (line 57-60)
- `switchSound()`: Log and return early when audio resource not found (line 109-112)
- `switchSound()`: Check `startAndReturnError` return value, log on failure (line 127-130)

- [ ] **Step 2: Commit**

```bash
git add shared/src/iosMain/kotlin/com/merkost/metronome/engine/MetronomePlayerIos.kt
git commit -m "fix: add @Volatile and Cedar error logging to MetronomePlayerIos"
```

---

### Task 8: Add device info to iOS support email

**Files:**
- Modify: `shared/src/iosMain/kotlin/com/merkost/metronome/platform/IosPlatformActions.kt`

- [ ] **Step 1: Update IosPlatformActions with device info**

Replace the full content of `shared/src/iosMain/kotlin/com/merkost/metronome/platform/IosPlatformActions.kt` with:

```kotlin
package com.merkost.metronome.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IosPlatformActions : PlatformActions {
    override fun contactSupport() {
        val device = UIDevice.currentDevice
        val appVersion = IosAppVersionProvider().getAppVersion()
        val body = buildString {
            append("Please describe the issue you're experiencing or your question below:\n")
            append("\n\n\n\n\n")
            append("Device Information:\n")
            append("OS Version: ${device.systemName} ${device.systemVersion}\n")
            append("Device Model: ${device.model}\n")
            append("App version: ${appVersion?.versionName ?: "unknown"} (${appVersion?.versionNumber ?: "unknown"})\n")
        }
        val encodedBody = body.replace(" ", "%20")
            .replace("\n", "%0A")
            .replace(":", "%3A")
            .replace("(", "%28")
            .replace(")", "%29")
        val url = NSURL(string = "mailto:merkostdev+metronome@gmail.com?subject=Support%20Request%20from%20Metronome%20App&body=$encodedBody") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun rateApp() {
        val url = NSURL(string = "https://apps.apple.com/app/id6480380648") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun isDynamicColorSupported(): Boolean = false
}
```

Changes: Added device model, OS version, and app version to the mailto body, matching Android's `AndroidPlatformActions.contactSupport()`.

- [ ] **Step 2: Commit**

```bash
git add shared/src/iosMain/kotlin/com/merkost/metronome/platform/IosPlatformActions.kt
git commit -m "feat: add device info to iOS support email"
```

---

### Task 9: Update iOS MainViewController

**Files:**
- Modify: `shared/src/iosMain/kotlin/com/merkost/metronome/MainViewController.kt`

- [ ] **Step 1: Verify MainViewController needs no changes**

The current `MainViewController.kt` already initializes Koin with `commonModule` and `iosModule`. Since `CedarSetup.initialize()` is called inside `commonModule` (Task 5), no changes are needed here.

Verify the file still looks correct:

```kotlin
package com.merkost.metronome

import androidx.compose.ui.window.ComposeUIViewController
import com.merkost.metronome.di.commonModule
import com.merkost.metronome.di.iosModule
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.ui.theme.MetronomeTheme
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController {
    KoinApplication(application = {
        modules(commonModule, iosModule)
    }) {
        MetronomeTheme {
            AppNavigation()
        }
    }
}
```

No changes needed. Skip to next task.

---

### Task 10: iOS SPM — Add Firebase Crashlytics

**Files:**
- Modify: `iosApp/iosApp.xcodeproj/project.pbxproj` (via Xcode)

- [ ] **Step 1: Add Firebase iOS SDK via SPM in Xcode**

This step must be done manually in Xcode:

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select the project in the navigator → Package Dependencies tab
3. Click "+" to add a package
4. Enter URL: `https://github.com/firebase/firebase-ios-sdk`
5. Set version rule to "Up to Next Major" from `11.0.0`
6. Select only the **FirebaseCrashlytics** product
7. Add it to the `iosApp` target

- [ ] **Step 2: Initialize Firebase in Swift**

In `iosApp/iosApp/iOSApp.swift` (or the app entry point), add Firebase initialization if not already present:

```swift
import Firebase

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
    }
    // ...
}
```

This is required for GitLive's `Firebase.crashlytics` to work on iOS.

- [ ] **Step 3: Add GoogleService-Info.plist**

If not already present, add the Firebase `GoogleService-Info.plist` to the Xcode project's `iosApp` target. This file is required for Firebase initialization on iOS.

- [ ] **Step 4: Commit**

```bash
git add iosApp/
git commit -m "chore: add Firebase Crashlytics via SPM for iOS"
```

---

### Task 11: Final verification

- [ ] **Step 1: Verify Android debug build**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify Android release build**

Run: `./gradlew :androidApp:assembleRelease 2>&1 | tail -5`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verify iOS framework build**

Run: `./gradlew :shared:linkReleaseFrameworkIosArm64 2>&1 | tail -5`

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Grep for any remaining Timber references**

Run: `grep -r "timber\|Timber" --include="*.kt" --include="*.kts" --include="*.toml" .`

Expected: No output (all Timber references removed)

- [ ] **Step 5: Commit any fixups if needed**
