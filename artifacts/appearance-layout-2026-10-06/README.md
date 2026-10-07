# Stable appearance selection

Selecting System, Light or Dark changed the chip font weight and its intrinsic width, shifting sibling controls. Palette labels also changed weight, and the selected palette name used an implicit size transition. Five new native regression tests failed on the previous PR revision before production changes.

Appearance now uses a reusable equal-sized selector with a shared spring-driven indicator. Selection, press and keyboard-focus feedback change only rendered colours and indicator position. Typography and control bounds stay fixed. RTL mirrors the horizontal indicator; sufficiently large text uses equally sized stacked choices without reducing the requested font size. The heading reserves space for its icon.

Palette labels keep the same weight and remain on one line, with a 48dp minimum target and natural width at larger text sizes. Their ring/check motion remains. The selected palette name reserves the widest option and uses the reusable fade without a size transform. Generic AppChip labels also retain one weight across selection.

## Verification

- Baseline: 199 native tests, five expected appearance failures, zero errors.
- Final: 202 native tests, zero failures/errors/skips. This includes eight appearance rendering tests and the existing twelve iOS switch rendering tests.
- Android debug build and 182 shared unit tests passed. iOS arm64 and Wasm compilation passed.
- The appearance suite measures actual control, label, header and following-content bounds at intermediate and settled frames. It checks visible motion, rapid reversals, reduced motion, one selected radio per group, keyboard focus, Tab/Space callbacks, RTL with 150% text, and fully readable System at 200% text.
- Native Compose 1.12.1 reconstructs semantics TextLayoutResult with an original natural layoutSize and a MultiParagraph using the maximum input constraints. The synthetic didOverflowWidth value can report overflow for centred text. Large-text acceptance therefore compares actual rendered Text bounds to intrinsic glyph width and paragraph height, and checks complete characters, no ellipsis/truncation, and one line.

[Light](light.png), [dark RTL with 150% text](dark-rtl.png), and [200% text](2xfont.png) show the production panel in the native raster test fixture. They are distinct from the full UIKit/Metal captures saved for the iOS switch fix. Temporary raster export code is absent from the final test source.

Run the native suite with the real SDK linkage prepared by tools/testing/package-ios-test-sdk.py:

```sh
./gradlew :shared:iosSimulatorArm64Test -PiosTestSdkDirectory=/private/tmp/MetronomeIosTestSdk
```

Physical-device VoiceOver and hardware gesture acceptance remain separate from these automated results. The original checkout and its unrelated changes were preserved.
