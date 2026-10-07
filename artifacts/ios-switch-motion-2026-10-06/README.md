# iOS switch motion regression

The original `PlatformSwitch` rendered `UISwitch` through `UIKitView` with `placedAsOverlay=true`. The native view sat above the Compose canvas, so ancestor opacity, clipping, scrims and section transitions did not consistently affect its pixels.

The replacement is an iOS-proportioned Compose control. It shares the canvas with its rows and participates in every existing parent transition. Tap, horizontal drag, cancellation, RTL, semantic theme colors and spring motion are preserved. Settings rows own one labelled switch action; decorative children do not add accessibility or keyboard-focus stops.

## Evidence

`baseline/` uses the original native wrapper. `fixed/` uses the replacement. Both are actual composited iOS Simulator captures from the same debug-only fixture and phase values. The normal app startup path is unchanged; the fixture requires a debug binary plus `--switch-motion-regression`.

- `alpha-half.png`: baseline switches remain opaque; fixed switches follow parent alpha.
- `invisible.png`: baseline controls remain visible after all Compose content disappears; fixed control pixels are absent.
- `clip-half.png` and `clipped.png`: fixed controls respect the current ancestor clipping region.
- `scrim.png` and `sheet.png`: controls draw beneath Compose modal content.
- `fixed/transitions.mp4`: compact native 32-second cycle with enter/exit, rapid reversal, ancestor fades, clips, movement, scrim and sheet cover.
- `native-frame-measurements.json`: the fully visible fixed reference contains control pixels; fixed zero-alpha and zero-clip frames contain zero control-colored pixels. The measured half-alpha energy ratio is 0.5.

## Automated rendering tests

`SettingsSwitchRenderingTest` uses a paused clock and production controls/disclosures for intermediate-pixel, gesture, semantic, palette, RTL and keyboard assertions. The Compose v2 runner renders a raster surface; these tests are complementary to the full native captures, and cannot reproduce a separate UIKit overlay by themselves.

For canceled touches, the test calls the same `ComposeScene.cancelPointerInput()` boundary used by the iOS mediator. The library's synthetic cancel dispatcher does not deliver that event.

The Kotlin iOS executable needs the Firebase SDK products normally supplied by Xcode. Build the app first, then package its real simulator objects and run:

```sh
python3 tools/testing/package-ios-test-sdk.py --derived-data <XcodeDerivedData> --output-directory <freshSdkDirectory>
./gradlew :shared:iosSimulatorArm64Test -PiosTestSdkDirectory=<freshSdkDirectory>
```

The packaging helper validates arm64 simulator metadata and records hashes. It supplies real SDK archives and Swift/transitive dependencies only to the simulator test executable. Production linkage is unchanged.

Physical-device VoiceOver announcements and gesture mediation have not been evaluated by these captures. Automated checks cover the accessible merged role, label, state, focus traversal and callback behavior.
