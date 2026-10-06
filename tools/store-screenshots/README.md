# Store screenshots

The current App Store set contains eight branded, opaque portrait PNGs in each
of [`screenshots/final/6.5-1242x2688/`](../../screenshots/final/6.5-1242x2688/)
and [`screenshots/final/6.7-1284x2778/`](../../screenshots/final/6.7-1284x2778/).
Their editable source is the [Metronome page in Figma](https://www.figma.com/design/ZOpOs8XklFaKvZXNM4uIem/App-Store-Screenshot-Template--Community-?node-id=2647-13).
Export its eight frames at 1× into `screenshots/source/figma-1320x2868/`, then
run `python3 tools/store-screenshots/export_app_store.py`. The script fits each
master to both accepted sizes with a centered vertical crop of about 5–6 px
per edge. Upload only a set from `screenshots/final/`.

The local renderer below generates alternate phone frames from real app
screenshots and the copy manifest in `frames.json`. Its output is separate
from the current Figma-designed App Store set.

```
tools/store-screenshots/
  frames.json     the eight frames: title, subtitle, source screenshot, badges
  template.html   one layout, sized from the target device
  build.mjs       renders each frame with headless Chrome
  raw/            source app screenshots (committed)
```

Output goes to `artifacts/store-screenshots/<device>/NN-<id>.png`.

## Regenerate

```bash
node tools/store-screenshots/build.mjs                      # every frame, every device
node tools/store-screenshots/build.mjs gap-trainer           # one frame
node tools/store-screenshots/build.mjs --device=iphone-6.9   # one device
```

The script exits non-zero and lists any frame whose source screenshot is
missing, so an incomplete set fails loudly rather than shipping a gap.

## Capturing the source screenshots

Sizes come from the device, not from this tool — capture at native resolution
and the template scales it.

**iOS simulator** (preferred; matches App Store dimensions exactly)

```bash
xcrun simctl boot "iPhone 17 Pro Max"
xcodebuild -project iosApp/Metronome.xcodeproj -scheme Metronome \
  -configuration Debug -destination "id=<UDID>" build
xcrun simctl install booted <path to Metronome.app>
xcrun simctl launch booted com.merkost.metronome
xcrun simctl io booted screenshot tools/store-screenshots/raw/main.png
```

**Android emulator**

```bash
adb shell screencap -p /sdcard/s.png
adb pull /sdcard/s.png tools/store-screenshots/raw/main.png
```

## Adding or changing a frame

Edit `frames.json`. `title` accepts `\n` for a line break. `badges` is optional
and renders a row of pills under the subtitle.

Only claims that are true of the shipped build belong here. Do not add
"Featured", "Editor's Choice", award or rating badges — both stores prohibit
implying editorial endorsement, and `docs/aso-strategy.md` rules out
review-derived claims until there is a real review corpus.

## Alternate renderer output sizes

| Device key | Size | Used for |
|---|---|---|
| `iphone-6.9` | 1290 × 2796 | Alternate iPhone preview; use the Figma-derived sets above for App Store upload |
| `android-phone` | 1080 × 1920 | Google Play phone screenshots |

The frame order in `frames.json` applies to this alternate renderer. The
Figma-derived App Store set uses its numbered file order.
