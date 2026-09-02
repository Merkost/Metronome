# Store screenshots

Generates the App Store and Google Play screenshot frames from real app
screenshots plus a text manifest, so they can be regenerated every release
instead of being hand-edited in a design tool.

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

## Store requirements

| Device key | Size | Used for |
|---|---|---|
| `iphone-6.9` | 1290 × 2796 | App Store 6.9" iPhone (Apple scales it down for smaller sizes) |
| `android-phone` | 1080 × 1920 | Google Play phone screenshots |

Both stores take between 2 and 8 phone screenshots. The frame order in
`frames.json` is the order they should be uploaded in — the first three carry
the core promise, because Apple can surface them directly in search results.
