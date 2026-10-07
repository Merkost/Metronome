# Native bottom-sheet safe-area verification

The actual production AppBottomSheet was rendered in the UIKit/Metal application, with temporary debug-only launch fixtures. The fixture and MainViewController guard were removed before final production-source gates. No app data was cleared.

## Verified on iPhone 17 Pro / iOS 26.5

- Native window: 402 × 874 points, scale 3. Safe area top 62 points (186 pixels), bottom 34 points (102 pixels).
- Original sheet shape reaches y=0 and extends behind the Dynamic Island/status region. The default content inset alone protects the title while leaving the surface behind the cutout.
- Fixed tall sheet shape starts at y=186 pixels. The title/handle remain in reach; the surrounding status region is scrim rather than sheet surface.
- Native opening recording: all 32 detected modal frames have painted top at or below y=186. Detection samples the solid surface at x=150 native pixels, requires a 20-pixel white run and a neutral scrim marker to exclude Home Screen/app-launch frames. The recording contains 140 original frames. This is sampled native rendering evidence, not an exhaustive physical-device or gesture proof.
- Scrolling 24 rows to the final Done action produces bounds [54,2352,1152,2520] pixels. The bottom is exactly the safe viewport edge (2622 − 102).
- Real Simulator accessibility text-size category accessibility-extra-extra-extra-large produces modal fontScale 1.8. The final action remains fully visible with bounds [54,2304,1152,2520]. The original category large was restored and verified.

Recommended images: baseline-tall.png, fixed-tall.png, fixed-end.png, fixed-accessibility-end.png.
Recommended motion clip: fixed-sheet-motion.mp4 (native recording trimmed to app-only entrance, 402-pixel review width).
Exact measurements: native-valid-summary.json, fixed-tall.painted-top.json, baseline-tall.painted-top.json, associated *.measurements.jsonl.

## Additional evidence and limits

- Native iPad mini portrait: 744 × 1133 points, scale 2, safe top 32 points and bottom 20 points. The fixed sheet and centered body respect these dimensions. Use fixed-ipad-portrait.png; the original capture filename fixed-ipad-landscape is misleading and should not be published under that name.
- Official UIWindowScene landscape request failed with: The current windowing mode does not allow for programmatic changes to interface orientation. Native landscape is therefore unverified.
- The text field becomes focused, but Simulator hardware-keyboard mode suppresses the onscreen keyboard and reports imeBottomPx=0. Use fixed-focused-field.png only as focus evidence; native IME behavior is unverified.
- The Mac was locked, so CUA could not operate Simulator to rotate or toggle the hardware keyboard. No workaround bypassed that boundary.
- The earlier custom outer LocalDensity font-scale fixture did not propagate through the native modal layer. fixed-font2-end is not accepted as large-text evidence; genuine system-category proof is fixed-accessibility-end.
- One fixed native build failed with libtool ENOSPC (errno 28). Removing obsolete generated switch-baseline DerivedData recovered space; the retry succeeded. Source/API compilation was not the failure.
