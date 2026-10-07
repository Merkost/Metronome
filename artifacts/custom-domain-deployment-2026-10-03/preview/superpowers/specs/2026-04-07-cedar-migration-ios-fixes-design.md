# Cedar Migration + iOS Code Quality Fixes

## Goal

Replace Android-only Timber logging with Cedar (KMP), replace Android-only Firebase Crashlytics with GitLive's KMP Crashlytics, and fix iOS code quality issues identified in the pre-release audit.

## Scope

Two intertwined workstreams shipped together:

1. **Logging migration** — Timber + Android Crashlytics → Cedar + GitLive Crashlytics (KMP)
2. **iOS code quality** — thread safety, error logging, device info in support email, lifecycle cleanup

## Dependencies

### Remove

| Artifact | From |
|----------|------|
| `com.jakewharton.timber:timber:5.0.1` | libs.versions.toml, shared/androidMain, androidApp |

### Add

| Artifact | Version | Source Set |
|----------|---------|-----------|
| `org.kimplify:cedar-logging` | `0.2.0` | shared/commonMain |
| `dev.gitlive:firebase-crashlytics` | `2.4.0` | shared/commonMain |

### Keep (unchanged)

- `com.google.firebase:firebase-crashlytics` (via BOM) in androidApp — GitLive wraps the native Android SDK, does not replace it
- `com.google.firebase:firebase-analytics` in androidApp (no KMP equivalent needed)
- `com.google.firebase:firebase-perf` in androidApp (no KMP equivalent needed)
- `com.google.firebase:firebase-bom` in androidApp (still governs analytics + perf versions)
- Firebase Crashlytics Gradle plugin in androidApp (still needed for symbol upload)

### iOS native dependency (SPM)

- Add `firebase-ios-sdk` via Swift Package Manager in the Xcode project
- Include the **FirebaseCrashlytics** product — required by GitLive's KMP wrapper at runtime on iOS

## Design

### 1. CrashlyticsTree — shared/commonMain

New file: `shared/src/commonMain/kotlin/com/merkost/metronome/logging/CrashlyticsTree.kt`

```kotlin
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

Forwards WARNING and ERROR logs (with optional throwables) to Crashlytics. Runs on both platforms via GitLive's KMP Crashlytics.

### 2. Logging initialization — shared/commonMain

New file: `shared/src/commonMain/kotlin/com/merkost/metronome/logging/CedarSetup.kt`

```kotlin
object CedarSetup {
    fun initialize(isDebug: Boolean) {
        if (isDebug) {
            Cedar.plant(ConsoleTree())
        } else {
            Cedar.plant(CrashlyticsTree())
        }
    }
}
```

Called from:
- **Android**: `MetronomeApp.onCreate()` with `BuildConfig.DEBUG`
- **iOS**: `MainViewController()` with a `isDebug` parameter (compile-time constant or passed from Swift)

### 3. Replace Timber call sites

| File | Current | New |
|------|---------|-----|
| `MetronomeApp.kt` | `Timber.plant(...)` | `CedarSetup.initialize(BuildConfig.DEBUG)` |
| `MainActivity.kt:60` | `Timber.tag("BOUND_SERVICE").d(...)` | `Cedar.tag("BOUND_SERVICE").d(...)` |
| `NotificationPermission.kt:121` | `Timber.w(e, ...)` | `Cedar.tag("NotificationPermission").w(...)` |

Remove all `timber.log.Timber` imports. Remove Timber dependency from both build.gradle.kts files.

### 4. MetronomePlayerIos — thread safety + error logging

File: `shared/src/iosMain/kotlin/com/merkost/metronome/engine/MetronomePlayerIos.kt`

**Thread safety:** Add `@Volatile` to all mutable state:
```kotlin
@Volatile private var audioEngine: AVAudioEngine? = null
@Volatile private var playerNode: AVAudioPlayerNode? = null
@Volatile private var varispeedNode: AVAudioUnitVarispeed? = null
@Volatile private var audioBuffer: AVAudioPCMBuffer? = null
```

**Error logging in `initialize()`:**
- Log when `URLForResource` returns null
- Log when `startAndReturnError` could fail (wrap in success check)

**Error logging in `switchSound()`:**
- Log when `URLForResource` returns null
- Log when engine restart could fail

Pattern: `Cedar.tag("MetronomePlayerIos").e("Failed to ...")` then return early.

### 5. IosPlatformActions — device info in support email

File: `shared/src/iosMain/kotlin/com/merkost/metronome/platform/IosPlatformActions.kt`

Add device info to the mailto URL body, matching Android's pattern:
- `UIDevice.currentDevice.model` (device model)
- `UIDevice.currentDevice.systemVersion` (iOS version)
- `UIDevice.currentDevice.systemName` (OS name)
- App version via `IosAppVersionProvider().getAppVersion()`

### 6. Lifecycle cleanup

File: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`

Add `onClose` to the MetronomeEngine Koin single:
```kotlin
single { MetronomeEngine(get(), get()).also { it.start() } } onClose { it?.release() }
```

This ensures AVAudioEngine resources are released when Koin scope is torn down.

### 7. iOS debug flag

For `CedarSetup.initialize(isDebug)` on iOS, use a compile-time approach:
- Add `isDebug()` expect/actual function in platform utils, or
- Pass from Swift side, or
- Use `Platform.isDebugBinary` if available

Simplest: use `kotlin.native.Platform.isDebugBinary` in iosMain.

## Files Changed

| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add cedar, gitlive-crashlytics versions; remove timber |
| `shared/build.gradle.kts` | Add cedar + gitlive-crashlytics to commonMain; remove timber from androidMain |
| `androidApp/build.gradle.kts` | Remove timber dep; remove firebase-crashlytics dep |
| `shared/src/commonMain/.../logging/CrashlyticsTree.kt` | New file |
| `shared/src/commonMain/.../logging/CedarSetup.kt` | New file |
| `shared/src/commonMain/.../di/CommonModule.kt` | Add onClose for MetronomeEngine |
| `androidApp/.../MetronomeApp.kt` | Replace Timber with CedarSetup |
| `androidApp/.../MainActivity.kt` | Replace Timber with Cedar |
| `shared/src/androidMain/.../NotificationPermission.kt` | Replace Timber with Cedar |
| `shared/src/iosMain/.../MetronomePlayerIos.kt` | Add @Volatile, add Cedar error logging |
| `shared/src/iosMain/.../IosPlatformActions.kt` | Add device info to support email |
| `shared/src/iosMain/.../MainViewController.kt` | Call CedarSetup.initialize() |
