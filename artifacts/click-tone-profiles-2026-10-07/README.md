# Click tone profiles

“Pick your click.” now pairs each existing sound description with a compact histogram derived from that sound's bundled regular-click audio. The 32 bars share a low-to-high 80 Hz–12 kHz frequency scale. Heights are normalized individually to show tone shape; they do not compare loudness. The bars remain still while selection tint follows the existing spring tokens.

| Before | After | Why |
| --- | --- | --- |
| Sound names, descriptions and audio preview were the only identification cues. | Every sound has a distinct, audio-derived tone histogram. | Recognize the sound's spectral character before listening. |
| Text had no weight inside the selection row. | The text/profile column takes the remaining width beside a separate 48dp preview. | Preserve readable words and controls on narrow screens with large text. |
| Crossfading icons owned preview descriptions. | The preview parent exposes only its current action; icons and graphs are decorative. | Selection and preview remain separate, clear accessibility actions. |

## Visual evidence

- [All seven tone profiles](profiles-light.png).
- Production picker content: [light at the top](picker-light-top-idle.png), [Studio selected](picker-light-studio-selected.png), [Wood preview](picker-light-wood-preview.png).
- Narrow dark RTL at 2x text: [top](picker-dark-rtl-2x-top-idle.png), [final Studio row](picker-dark-rtl-2x-studio-idle.png), [Studio preview](picker-dark-rtl-2x-studio-preview.png).
- [Validation/source hashes](validation.json), [build results](build-result.txt), [rendering tests](rendering-result.xml), [independent review](review.md).

These are native raster fixtures rendering the production picker content and histogram, rather than screenshots of an actual UIKit modal or physical device. The standalone chart fixture proves visibly distinct painted profiles. The picker tests exercise real selection and preview targets, callback separation, current-only descriptions, stable row/card/chart geometry, rapid reversals, automatic preview completion, whole-word large-text layout and bounded scrolling to the final sound. Existing bottom-sheet UIKit safe-area evidence remains in `../bottom-sheet-safearea-2026-10-07/`.

Temporary image export hooks were removed before the final native run. `capture-fixture.kt.txt` preserves their reproduction source outside compiled source sets; the PPM captures were converted to PNG without modifying pixels. All profile colors come from MaterialTheme roles, and graphs introduce no new focus stops.

## Data generation

Run `python3 tools/generate-click-spectra.py` after changing bundled normal-click audio, then `python3 tools/generate-click-spectra.py --check` to verify the checked-in Kotlin data. The exhaustive enum mapping, asset hashes and Android/iOS/Wasm byte parity catch missing or stale profiles.

Use Python 3.12+ and installed ffmpeg/ffprobe. The Classic sample uses WAVE_FORMAT_EXTENSIBLE PCM, whose stdlib support was added in [Python 3.12](https://docs.python.org/3/library/wave.html). Unsupported Python versions now fail with a clear message. No new app runtime dependency or audio decoding occurs in the UI.

The generator performs a full-clip rectangular FFT, zero-padded to the next power of two with at least 8192 points. It averages channel power before averaging bins in each fixed logarithmic band, takes the square root, then normalizes to the strongest band. No taper suppresses the initial transient, and opposite stereo phase cannot cancel the profile. These bars represent relative mean spectral density, rather than integrated energy per widening band.

Only DC bin zero is excluded. A shorter constant clip can leak low-band energy after rectangular truncation/zero padding; the analytic constant-signal rejection case is specifically FFT-sized. The report records this boundary without claiming DC-offset invariance. Profiles represent regular clicks, with no claim that they visualize accent variants or the four-beat preview timeline. Audio resources and the preview engine are unchanged.

[Extraction/analytic checks](spectra-analysis.json) record source hashes, frequency edges and profiles. [Reproducibility checks](generation-repro.json) prove repeated generation matches the repository, stale checks do not write, and unsupported Python errors are clear. The native suite uses the project's packaged real simulator SwiftPM SDK.
