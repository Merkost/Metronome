# Native UI verification — 12 October 2026

The latest direction is a single graphic welcome screen, three brief feature highlights, Skip and Get started. There is no carousel, Next button, initial setup form or automatic playback.

Final visual inspection corrected wordmark wrapping at 2x text size, centered the welcome action's visible label in its reserved saving-label space, preserved correct digit order in RTL counters and mirrored Settings Back. All final build and test gates now pass.

## Final build checks

The final production source passed Android debug assembly, Android unit tests, iOS device Kotlin compilation and Wasm Kotlin compilation. The fresh unit-test report contains 199 tests across 34 suites, with zero failures, errors or skipped tests. Build log: `/private/tmp/metronome-clean-final-ui-gates.log`. The full iOS simulator app also built successfully with all final header, label-alignment and RTL corrections. Build log: `/private/tmp/metronome-clean-native-accepted-uikit-build.log`. Its recorded production-source hashes match the current checkout.

The final iOS simulator test run passed 283 tests across 47 suites, with zero failures, errors or skipped tests: the 199 common tests also passed on iOS, together with 84 native rendering tests. Build log: `/private/tmp/metronome-clean-native-final-verification.log`. An earlier run stopped at a synchronous test scroll loop; the corrected tests use bounded real scroll requests with explicit frame advancement and retain full control visibility and value assertions.

| Native rendering coverage | Passed cases |
| --- | ---: |
| Single welcome, completion, Skip, same-frame duplicate guard, error/retry, 2x text/RTL, wide layout, reduced motion | 9 |
| Practice libraries, step targets, timer, tempo/gap trainer, session recovery/navigation | 8 |
| Settings hierarchy, persistence through disclosure, 2x text/RTL, Mono outline, reduced motion, data/help callbacks | 7 |
| Appearance component layout | 14 |
| Sound preview state | 3 |
| Switch and disclosure rendering/motion | 12 |
| Click picker selection, preview, disabled preview and layout | 6 |
| Home shortcuts and Rhythm | 5 |
| Exact tempo draft, bounds, keyboard confirmation and 2x text/RTL | 4 |
| Pendulum rendering | 5 |
| Actual number raster order, surrounding RTL, whole-value accessibility and changed-only digit motion | 2 |
| Backup actions | 4 |
| Sheet cutouts, bottom obstructions, scroll end and real Material modal footer | 5 |

The two new number-rendering cases failed meaningfully before the RTL fix and pass afterward. Diagnostic captures establish that 32ms is the startup frame; 64/96/128ms frames differ from both endpoints while unchanged prefix pixels stay identical. The final test requires those intermediate frames at the original threshold and exact prefix/frame stability. Welcome alignment checks use intrinsic text bounds plus a one-pixel font-leading allowance, which still detects the earlier reserved-slot misalignment. All final native assertions passed.

## Runtime coverage

The task-owned iOS simulator rendered the single welcome in light, dark and accessibility text sizes. Get started opened the instrument silently. The native walkthrough exercised exact-tempo draft and cancellation, meter and subdivision, independent sound selection and preview, saving a preset, previewing/saving/starting/pausing/finishing a routine, timer duration and trainer disclosures. The refreshed app confirms the final Mono swatch outline and the last sound row with clear sheet end spacing. Keyboard focus reaches lower Settings groups in the running app; physical touch scrolling is not established by this automation.

The final iOS app was installed and launched with its test data preserved. Its accepted home capture shows paused 80 BPM with Soft still selected, centered pendulum and equal Practice/Sound shortcuts. Earlier UIKit flow captures predate the final header/digit refinements and are identified in the build manifest; exact-size final native welcome captures cover those refinements.

The task-owned Android emulator exercised home, exact tempo draft/apply/cancel, independent meter/beat/subdivision/count-in changes, all seven click choices, sound preview versus selection, settings disclosures, appearance, practice data, Help and What's New. The final APK passed dark-theme status-bar contrast, unselected Mono swatch visibility and the long Practice data sheet's scrolling end: the complete 52dp Done button has 32dp clear space above the bottom safe-area boundary. Practice timer, tempo/gap trainer start/stop, preset empty/save-dialog/cancel and set/starter preview/editor also passed.

The final APK passed real 1.6 text-scale RTL checks for correct visible 80 BPM, mirrored Settings Back, stable appearance and preview controls, and reachable final sound rows. Following the user's approval to reset only the disposable test app, its single welcome passed both large RTL and standard English layouts. Get started and Skip open paused 80 BPM / Wood / 4/4 with zero practice time; completion persists after reopening. The emulator's locale, font scale, RTL and appearance were restored. Detailed coverage is in [runtime notes](runtime-notes.md).

## Evidence and limits

Current single-screen welcome evidence is [the final native Android capture](native-captures/android-welcome-final.png). The original onboarding concept and earlier Android welcome captures show a superseded three-page draft and are historical. iOS welcome captures predate the final header/RTL polish; the welcome source and native rendering captures document those refinements separately.

The user approved resetting only the disposable Android test app on emulator-5596 for fresh welcome verification. Both exits were checked on the final corrected APK, without affecting preview-device data. No physical-device installation, deployment, release, commit or push is part of this verification pass.

Independent source review found no further actionable regression in welcome/navigation, shared sheet or sound picker changes. The user's existing iOS build-number change from 2 to 3 remains unchanged. Playback scheduling, audio resources, pendulum synchronization, persistence formats and release metadata were not modified by this UI cleanup.
