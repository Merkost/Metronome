# Appearance selection review — 2026-10-06

**Verdict: approved. No remaining actionable findings.**

Source review confirms that selection preserves typography and control geometry. The adaptive selector uses equal horizontal cells or equal vertical cells when labels cannot fit, retains the requested font scale, and moves its shared spring indicator along the appropriate axis. RTL reverses only the horizontal indicator. Radio-group semantics and visible keyboard-focus feedback remain intact. The proposed minimum 48dp width guard is implemented, and vertical sizing uses the ceiling of the measured paragraph height. Palette labels keep one weight, palette presses preserve swatch size, and the selected palette name reserves its widest label and uses the shared fade without a size transform.

Visual inspection of [light](light.png), [dark RTL at 150% text](dark-rtl.png), and [200% text](2xfont.png) confirms visible headings, palette names and palette glyph. Horizontal cells appear equal; the large-text capture uses equal stacked cells with “System” fully visible on one line. The saved PNGs match the inspected captures byte for byte.

The original baseline at `f42e61625839672662debb62b145b557e3065a67` ran 199 native tests with five expected appearance failures and zero errors. The final gate log reports `BUILD SUCCESSFUL in 1m 9s`. Fresh XML confirms:

| Check | Verified result |
| --- | --- |
| Native iOS simulator suite | 202 tests; zero failures, errors or skips |
| Appearance rendering suite | 8 tests passed; XML timestamp `2026-10-06T12:37:25.215Z` |
| Existing iOS switch rendering suite | 12 tests passed |
| Android shared unit suite | 182 tests; zero failures, errors or skips |
| Android debug build | Passed |
| iOS arm64 and Wasm source compilation | Passed |

The eight appearance cases cover:

1. Stable theme controls, labels and following content during and after selection.
2. Stable palette controls, labels and following content.
3. Dark RTL with 150% text.
4. Fully readable, single-line “System” at 200% text, using actual text bounds, intrinsic glyph width, paragraph height, all characters and no ellipsis.
5. Exactly one selected radio per group and one callback per action.
6. Rapid reversals preserving geometry and finishing on the last choices.
7. Reduced motion rendering the final choices immediately.
8. Visible keyboard focus, Tab navigation and single Space callbacks without geometry changes.

All five reviewed source hashes match `validation.json`; `git diff --check` passed. Temporary capture-export code is absent from the final test source. These are native raster-fixture results; physical-device VoiceOver and hardware-gesture acceptance remain separate.
