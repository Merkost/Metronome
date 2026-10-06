# Click tone-profile review — 2026-10-07

**Verdict: approved. No remaining actionable findings.** This review covers the static tone profiles and picker integration.

| Before | After | Why |
| --- | --- | --- |
| Sound choices offered names, character descriptions and an audible preview. | Every sound also has a passive histogram derived from its bundled normal click audio. | Users can compare relative tone shapes without starting playback. |
| Selection used button semantics and animated preview icons carried their own action labels. | One radio group exposes seven selected/unselected choices; separate 48dp preview buttons expose only their current action. Histogram semantics are cleared. | Selection and preview remain distinct, with no additional chart focus targets or overlapping outgoing action labels. |

The weighted text column preserves icon and preview space. Each histogram reserves 24dp and draws 32 bars whose geometry and low-to-high order remain static during selection or preview. Semantic tint uses the shared color spring; the existing check marker identifies selection independently of color. Captures show readable normal cards and naturally wrapped 200% dark RTL text, including a reachable final Studio card. Previewing Wood does not select it; Studio's idle/active captures preserve its row and profile layout.

The generator uses fixed logarithmic bands from 80Hz to 12kHz. It transforms each channel separately and averages channel power, so opposite stereo polarity does not cancel the profile. It averages FFT-bin power within each band, takes its square root, and normalizes each sound to its own largest band. Heights therefore describe relative tone distribution/density, not comparative loudness or total energy in widening frequency bands. Analysis uses the complete normal clip with a rectangular FFT, zero-padding to a power of two of at least 8192; accent variants and device playback processing are outside this profile.

Independent read-only generation checks passed. All seven records have 32 finite values in [0,1] with a normalized peak of 1, match generated Kotlin data, and verify identical Android/iOS/Wasm source bytes. Analytic impulse, low/high tone, opposite stereo and silence checks pass. The corrected constant-signal check is accurately scoped: the DC FFT bin is excluded, but short rectangular clips can leak into nonzero bins after zero-padding; no universal DC-offset rejection is claimed. Python 3.12+ is required for bundled extensible PCM WAVs, with ffmpeg/ffprobe for legacy MP3 extraction.

Fresh final XML confirms 223 native tests, 182 Android shared tests and all 41 rendering cases passed with zero failures, errors or skips. The four new picker cases cover distinct painted profiles, passive semantics, profile-tap versus preview callbacks, stable geometry, rapid reversals, current action descriptions, readable large RTL text and Studio reachability. Android debug, iOS arm64 and Wasm gates succeeded; the final exporter-free native run reports `BUILD SUCCESSFUL in 54s`.

All five source hashes and seven saved PNGs match the reviewed versions; `git diff --check` passed. Temporary capture hooks are absent from final test source. The images render production SoundPickerContent/SoundToneHistogram in native raster fixtures. They are not actual UIKit modal, physical-device or audible-preview proof. Audio assets, preview engine and playback callbacks are unchanged. No production edits or Gradle runs were performed by this review.
