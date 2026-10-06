# iOS Settings Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give iOS a working Volume row (system volume via `MPVolumeView`), native `UISwitch` toggles for every settings switch, and confirmed background-play parity — filling the empty iOS actuals.

**Architecture:** Implement the `VolumeSlider` iOS actual by embedding the system `MPVolumeView` in a Compose `UIKitView` with a live percentage caption driven by KVO on `AVAudioSession.outputVolume`. Add an `expect fun PlatformSwitch` (Material on Android, `UISwitch` via `UIKitView` on iOS) and route the shared `SettingsSwitch` through it. Leave `BackgroundPlayPermissionCheck` an empty no-op after verifying the `Info.plist` audio mode and playback session category are present.

**Tech Stack:** Kotlin 2.4.0 Multiplatform, Compose Multiplatform 1.11.1 (`androidx.compose.ui.interop.UIKitView`), Kotlin/Native Objective-C interop (`platform.MediaPlayer.MPVolumeView`, `platform.AVFAudio.AVAudioSession`, `platform.UIKit.UISwitch`/`UIColor`, `platform.darwin.NSObject` KVO + `@ObjCAction` target-action). ObjC interop on Kotlin 2.4 needs `@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)`.

**Testing note:** The only automated test target (`:shared:testDebugUnitTest`) is Android JVM and cannot compile `iosMain`. Verification is an Android compile + the iOS framework link + a manual device checklist. No unit tests are written for this UI interop.

---

### Task 1: Add `PlatformSwitch` expect, Android actual, and wire `SettingsSwitch`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.kt`
- Modify: `shared/src/androidMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.android.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/SettingsScreen.kt:35,458`

- [ ] **Step 1: Declare the expect**

In `PlatformSettingsComponents.kt`, below the existing `expect` declarations, add:

```kotlin
@Composable
expect fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit)
```

- [ ] **Step 2: Add the Android actual**

In `PlatformSettingsComponents.android.kt`, add the import
`import androidx.compose.material3.Switch` and this actual:

```kotlin
@Composable
actual fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(checked = checked, onCheckedChange = onCheckedChange)
}
```

- [ ] **Step 3: Route `SettingsSwitch` through `PlatformSwitch`**

In `SettingsScreen.kt`, replace line 458:

```kotlin
        Switch(checked = checked, onCheckedChange = onCheckedChange)
```

with:

```kotlin
        PlatformSwitch(checked = checked, onCheckedChange = onCheckedChange)
```

Then delete the now-unused import on line 35:

```kotlin
import androidx.compose.material3.Switch
```

(`PlatformSwitch` is in the same `com.merkost.metronome.screens` package, so no new import is needed. `Slider` and `SliderDefaults` imports stay — they are still used.)

- [ ] **Step 4: Verify Android still compiles**

Run:
```bash
./gradlew :androidApp:assembleDebug
```
Expected: `BUILD SUCCESSFUL`. (Confirms the expect + Android actual + common wiring are consistent before touching iOS.)

---

### Task 2: Implement the iOS actuals file

**Files:**
- Modify: `shared/src/iosMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.ios.kt`

- [ ] **Step 1: Replace the whole file**

Replace the entire contents (currently two empty actuals) with:

```kotlin
package com.merkost.metronome.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFAudio.AVAudioSession
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSNumber
import platform.Foundation.NSSelectorFromString
import platform.MediaPlayer.MPVolumeView
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch
import platform.darwin.NSObject
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun VolumeSlider() {
    val session = remember { AVAudioSession.sharedInstance() }
    var volume by remember { mutableStateOf(session.outputVolume) }
    val sliderTint = MaterialTheme.colorScheme.primary

    DisposableEffect(session) {
        val observer = VolumeObserver { volume = it }
        session.addObserver(
            observer,
            forKeyPath = "outputVolume",
            options = NSKeyValueObservingOptionNew,
            context = null
        )
        onDispose {
            session.removeObserver(observer, forKeyPath = "outputVolume")
        }
    }

    SettingsRow(title = "Volume") {
        Text(
            text = "${(volume * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        UIKitView(
            factory = {
                MPVolumeView().apply {
                    setShowsRouteButton(false)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            update = { view ->
                view.tintColor = sliderTint.toUIColor()
            }
        )
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val onTint = MaterialTheme.colorScheme.primary
    val latestOnChange = rememberUpdatedState(onCheckedChange)
    val switchTarget = remember { SwitchTarget { latestOnChange.value(it) } }

    UIKitView(
        factory = {
            UISwitch().apply {
                addTarget(
                    target = switchTarget,
                    action = NSSelectorFromString("onValueChanged:"),
                    forControlEvents = UIControlEventValueChanged
                )
            }
        },
        modifier = Modifier.size(width = 51.dp, height = 31.dp),
        update = { view ->
            view.setOn(checked, animated = true)
            view.onTintColor = onTint.toUIColor()
        }
    )
}

@Composable
actual fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean) {
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class VolumeObserver(
    private val onChange: (Float) -> Unit
) : NSObject() {
    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: COpaquePointer?
    ) {
        val newValue = (change?.get(NSKeyValueChangeNewKey) as? NSNumber)?.floatValue ?: return
        onChange(newValue)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class SwitchTarget(
    private val onChange: (Boolean) -> Unit
) : NSObject() {
    @ObjCAction
    fun onValueChanged(sender: UISwitch) {
        onChange(sender.on)
    }
}

private fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble()
)
```

Notes:
- No code comments (project rule).
- `BackgroundPlayPermissionCheck` stays empty — that is the intended iOS no-op.
- `rememberUpdatedState` keeps the toggle callback fresh so the long-lived
  `SwitchTarget` never invokes a stale lambda.

---

### Task 3: Compile-verify the iOS framework link

**Files:** none (build only)

- [ ] **Step 1: Link the iOS simulator framework**

Run:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Resolve interop-signature errors if the link fails**

These are the only plausible failure points; apply the matching fix and re-run Step 1:

- **`UIKitView` overload / experimental API.** If the compiler reports the
  `factory`/`update` overload is unresolved or requires opt-in, add
  `import androidx.compose.ui.ExperimentalComposeUiApi` and include
  `ExperimentalComposeUiApi::class` in the `@OptIn(...)` of `VolumeSlider` and
  `PlatformSwitch`.
- **Opt-in marker mismatch.** If the compiler names a different required marker
  for KVO / `@ObjCAction` / `NSObject` subclassing (e.g. only
  `BetaInteropApi`, or an additional one), set the `@OptIn(...)` annotations to
  exactly the markers it names. The opt-ins are boilerplate; the markers it
  requests are authoritative.
- **Deprecated-as-error on `setShowsRouteButton`.** `showsRouteButton` is
  deprecated since iOS 13 but functional. If the build treats the deprecation as
  an error, replace `setShowsRouteButton(false)` with `showsVolumeSlider = true`
  and drop the route-button line.
- **`observeValueForKeyPath` override mismatch.** The override parameter types
  must exactly match the generated K/N stub:
  `(keyPath: String?, ofObject: Any?, change: Map<Any?, *>?, context: COpaquePointer?)`.
  Adjust to match and re-run.
- **`addTarget` named arguments.** If `target =`/`action =`/`forControlEvents =`
  names do not resolve, call positionally:
  `addTarget(switchTarget, NSSelectorFromString("onValueChanged:"), UIControlEventValueChanged)`.

- [ ] **Step 3: Confirm the link is clean**

Run:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL` with no errors.

---

### Task 4: Verify background-play prerequisites (no code change)

**Files:** none (verification only)

- [ ] **Step 1: Confirm the `audio` background mode is present**

Run:
```bash
grep -A3 UIBackgroundModes iosApp/iosApp/Info.plist
```
Expected output contains:
```
	<key>UIBackgroundModes</key>
	<array>
		<string>audio</string>
	</array>
```
If absent, add that `UIBackgroundModes` array under the top-level `<dict>`.

- [ ] **Step 2: Confirm the playback session category is set**

Run:
```bash
grep -n "AVAudioSessionCategoryPlayback\|setActive" shared/src/iosMain/kotlin/com/merkost/metronome/engine/MetronomePlayerIos.kt
```
Expected: `setCategory(AVAudioSessionCategoryPlayback, ...)` and `setActive(true, ...)` in `initialize`. Both are already present; no change.

---

### Task 5: Commit

**Files:** the modified actuals + wiring + the spec/plan docs.

- [ ] **Step 1: Stage and commit (only after the user confirms they want a commit)**

```bash
git add shared/src/iosMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.ios.kt \
        shared/src/androidMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.android.kt \
        shared/src/commonMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.kt \
        shared/src/commonMain/kotlin/com/merkost/metronome/screens/SettingsScreen.kt \
        docs/superpowers/specs/2026-06-12-ios-settings-parity-design.md \
        docs/superpowers/plans/2026-06-12-ios-settings-parity.md
git commit -m "feat: iOS volume row + native UISwitch toggles, background-play parity

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

## Manual device QA checklist (post-merge, real device)

Cannot be automated from a framework link — run on a physical iPhone:

- [ ] Volume row renders in Settings; dragging the slider changes system volume.
- [ ] Caption updates live when pressing the hardware volume buttons.
- [ ] Slider tint matches the active color scheme; re-check after switching schemes.
- [ ] Every settings toggle is a native iOS switch; flipping it persists and the
      `onTintColor` matches the active scheme.
- [ ] Audio keeps playing when the app is backgrounded / screen locks.
- [ ] Stereo pan slider, haptic toggle (Core Haptics availability), color-scheme
      picker, and sheet insets behave with the home indicator.

---

## Self-review

- **Spec coverage:** Volume row + KVO caption + scheme tint + route button hidden
  (Task 2); `PlatformSwitch` expect/Android actual/wiring (Task 1) and iOS
  `UISwitch` actual (Task 2); background-play no-op + plist/session verification
  (Task 4); Android compile (Task 1) and iOS link (Task 3); device QA checklist
  (bottom). All spec sections mapped.
- **Placeholders:** none — full file content given; Task 3 contingencies are
  concrete error→fix pairs, not deferred logic.
- **Type consistency:** `PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit)`
  is identical across expect, Android actual, iOS actual, and the `SettingsSwitch`
  call site. `SwitchTarget(onChange: (Boolean) -> Unit)`,
  `VolumeObserver(onChange: (Float) -> Unit)`, and `Color.toUIColor(): UIColor`
  are used consistently.
