# Shared bottom-sheet safe areas

All 12 native sheet call sites use `AppBottomSheet`. Its surface now clears the top and horizontal safe-drawing insets; dialog-local bottom insets protect the scrollable content exactly once. Negative spring overshoot is cancelled at placement so the surface cannot rise into the cutout. Downward dragging and dismissal retain their normal placement.

| Before | After | Why |
| --- | --- | --- |
| A tall sheet painted from y=0 behind the iPhone Dynamic Island. | Its painted top stays at or below the 186px safe edge. | Protect the sheet shape as well as its title. |
| Horizontal cutouts had no shared surface constraint. | The reusable modifier consumes top and side insets before sheet sizing. | All callers inherit the same behavior, including asymmetric cutouts. |
| A second body navigation-bar padding duplicated inset ownership. | Material owns bottom padding inside the modal; children inherit consumed insets. | Preserve usable scroll space and avoid additive keyboard/home padding. |
| Spring settling could travel above the expanded anchor. | Only negative anchored placement is compensated. | Keep animated presentation within the same safe edge. |

## Evidence

- [Before](baseline-tall.png), [after](fixed-tall.png), [scrolled final action](fixed-end.png), [system accessibility text size](fixed-accessibility-end.png), [iPad portrait](fixed-ipad-portrait.png).
- [Native opening motion](fixed-sheet-motion.mp4) and [painted-top measurements](fixed-tall.painted-top.json): 32 detected modal frames out of 140 original recording frames, minimum painted top 186px. This is sampled entrance evidence.
- [Native verification and limits](native-review.md), [window/content bounds](native-valid-summary.json), [validation](validation.json), [build results](build-result.txt), and independent [review](review.md).
- Four native raster regression cases verify actual surface pixels, asymmetric side cutouts, consumed nested insets, negative/positive anchored placement, large RTL content, changing bottom obstructions and the reachable final action. They exercise the production safe-area modifier and body with simulated Material anchor placement/bottom ownership; actual modal rendering is proved separately by the UIKit captures.

The iPhone 17 Pro / iOS 26.5 fixture window is 402 x 874pt at 3x, with 62pt top and 34pt bottom safe areas. The original surface top was 0px; the fixed top is 186px. The final action ends at 2520px, above the 102px bottom safe area. Genuine system accessibility sizing produced modal fontScale 1.8 and retained a reachable final action. The original Simulator text-size category was restored.

Native software-keyboard and landscape behavior remain unverified: hardware-keyboard mode suppressed the software keyboard; the current iPad window mode rejected programmatic rotation. Synthetic asymmetric landscape and bottom-obstruction tests passed. Physical-device and exhaustive gesture checks are separate release acceptance.

## Reproduction

`native-fixture.kt.txt` preserves the temporary capture source as evidence; it is outside compiled source sets. The capture build temporarily routed a debug launch to `SheetSafeAreaRegressionScreen` using `--sheet-safearea-regression`, with `--sheet-safearea-end` to reveal the last action. The system content-size category provided the accepted large-text capture; the fixture's custom outer font-scale override did not propagate into the modal and is not accepted as evidence. No launch fixture or guard remains in production.

The native test SDK packages the project's real arm64 simulator SwiftPM archives. Re-run the command in `build-result.txt`; it runs the full native suite, Android tests/debug build and device/Web source compilation.
