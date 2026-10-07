# iOS Settings Parity — Design

Date: 2026-06-12
Status: Approved
Branch: feature/practice-tools-redesign

## Problem

Two settings components have empty iOS actuals, so iOS users silently lose
functionality the Android build has:

- `VolumeSlider` — empty on iOS; the Volume row does not render at all.
- `BackgroundPlayPermissionCheck` — empty on iOS; harmless, but the
  background-play prerequisites were never confirmed.

Both actuals live in
`shared/src/iosMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.ios.kt`.
The `expect` declarations are in
`shared/src/commonMain/.../PlatformSettingsComponents.kt`.

Additionally, every settings toggle renders the Material 3 `Switch`, which looks
foreign on iOS. iOS should use the native `UISwitch` while Android keeps Material.

## Scope

Fill the two iOS actuals (`SettingsRow` is defined in `SettingsScreen.kt` in the
same `com.merkost.metronome.screens` package, so the iOS actual calls it directly,
exactly as the Android actual does). Plus add a platform-native switch:
introduce `expect fun PlatformSwitch` and route the shared `SettingsSwitch`
through it (Material on Android, `UISwitch` on iOS).

## Design

### 1. Volume row (`VolumeSlider` iOS actual)

iOS forbids setting system output volume programmatically. The only
App-Store-safe path is `MPVolumeView`, the system-provided volume view. The iOS
actual renders the same `SettingsRow(title = "Volume")` shell the Android actual
uses, containing:

- **Caption** — `Text` styled to match Android (`MaterialTheme.typography.bodySmall`,
  `color = MaterialTheme.colorScheme.onSurfaceVariant`), showing
  `"${(outputVolume * 100).roundToInt()}%"`. (Android shows `n / max`; iOS volume
  is a 0.0–1.0 float, so a percentage is the natural iOS rendering.)
- **Slider** — `MPVolumeView` embedded via `androidx.compose.ui.interop.UIKitView`:
  - `showsRouteButton = false` to hide the AirPlay/route button (slider only).
  - `tintColor` set from `MaterialTheme.colorScheme.primary` inside the `UIKitView`
    `update` block, so it re-tints when the color scheme changes.
  - Height ~34.dp so the native thumb renders cleanly (the Android Material slider
    is 16.dp, but the native UISlider needs more vertical room).
- **Live caption updates** — KVO observer on `AVAudioSession.sharedInstance()`
  key path `"outputVolume"`. Both hardware volume-button presses and slider drags
  update the caption. Registered/unregistered through a `DisposableEffect`.

New private helpers in the iOS file:

- `private class VolumeObserver(onChange: (Float) -> Unit) : NSObject()` overriding
  `observeValueForKeyPath(...)`, reading `change[NSKeyValueChangeNewKey]` as
  `NSNumber.floatValue`.
- `private fun Color.toUIColor(): UIColor` converting Compose RGBA to `UIColor`.

Frameworks `platform.MediaPlayer` (`MPVolumeView`) and `platform.UIKit`
(`UIColor`) are available to `iosMain` with no Gradle changes.

### 2. Background play (`BackgroundPlayPermissionCheck` iOS actual)

**Pure no-op — the actual stays empty.** iOS needs no runtime permission for
background audio. Verification confirms both prerequisites are already in place:

- `iosApp/iosApp/Info.plist` has `UIBackgroundModes → audio`.
- `MetronomePlayerIos.initialize` sets `AVAudioSessionCategoryPlayback` and
  `setActive(true)`.

No code change. This item is verify-only. (Per the project's no-comments rule,
the empty actual carries no explanatory comment.)

### 3. Native settings switch (`PlatformSwitch`)

All five settings toggles (Haptic, Color Flash, Keep Screen Awake, Count-in,
Background Play) render through the shared `SettingsSwitch` composable, whose only
control is a Material 3 `Switch` (`SettingsScreen.kt:458`). This is the single
Material `Switch` in `commonMain`.

Introduce a platform composable following the project's `expect/actual`
convention:

- `commonMain` `PlatformSettingsComponents.kt`:
  `expect fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit)`.
- Android actual: `Switch(checked, onCheckedChange)` (Material 3 — unchanged look).
- iOS actual: `UISwitch` embedded via `UIKitView`:
  - `update` sets `setOn(checked, animated = true)` and `onTintColor` from
    `MaterialTheme.colorScheme.primary` (reusing the same `Color.toUIColor()`).
  - Value changes wired through a target-action: a `private class SwitchTarget :
    NSObject()` with an `@ObjCAction` method registered for
    `UIControlEventValueChanged`. The callback is kept fresh with
    `rememberUpdatedState` so recomposition never leaves a stale lambda.
  - Fixed `Modifier.size(width = 51.dp, height = 31.dp)` (UISwitch intrinsic size)
    so layout reserves space for the native overlay.

`SettingsSwitch` swaps `Switch(...)` for `PlatformSwitch(...)`; the now-unused
`import androidx.compose.material3.Switch` is removed from `SettingsScreen.kt`.
No `enabled` parameter — no caller disables a settings switch (YAGNI).

## Out of scope (flagged, not implemented)

- **Vestigial iOS toggle**: with the playback category + `audio` background mode,
  audio continues in the background on iOS regardless of the "Background Play"
  toggle. Making the toggle actually gate background continuation is a behavior
  change beyond settings parity — flagged as a future follow-up.
- **On-device QA**: stereo pan, Core Haptics availability, color-scheme picker,
  and home-indicator sheet insets require a real device/simulator run, not a
  framework link. Delivered as a checklist, not automated.

## Verification

- `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` — confirms the new
  interop + KVO observer compile for the iOS target.
- Manual device checklist (background audio survives backgrounding; volume
  caption tracks hardware buttons; slider tint follows the active scheme).

## Files touched

- `shared/src/iosMain/.../PlatformSettingsComponents.ios.kt` — implement
  `VolumeSlider` and `PlatformSwitch` (`UISwitch`); leave
  `BackgroundPlayPermissionCheck` empty.
- `shared/src/commonMain/.../PlatformSettingsComponents.kt` — add
  `expect fun PlatformSwitch`.
- `shared/src/androidMain/.../PlatformSettingsComponents.android.kt` — add the
  Material `PlatformSwitch` actual.
- `shared/src/commonMain/.../SettingsScreen.kt` — `SettingsSwitch` calls
  `PlatformSwitch`; drop the unused `material3.Switch` import.
