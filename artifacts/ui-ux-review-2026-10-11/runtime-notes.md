# Native runtime verification

## Android target and provenance

Tests run on the task-only `MetronomeCleanUI20261012` headless Pixel 7 emulator, `emulator-5596`, Android 37.1, 1080×2400 at 420 dpi. Its configuration and user data are in `/private/tmp/MetronomeCleanAndroid.avd`. The human's Medium Phone preview remains untouched after they began using it. The initially proposed Small Phone image was missing.

Final installed APK SHA256: `b40e99d797143c5c477979f2e2a9cdc2c64c8cf93ab251a8dcb79ff8ab55793c`. It was installed preserving test data before the final RTL checks. The earlier appearance, sheet and Practice correction pass used `b4b1a2f5bce5ee6facd31005d883e08bfc84c51ddf0155ce552c9c1295315b24`; earlier screen captures used `33dabcfe3b89f85471709217bf37a4959b20a386027e9009115212494a0697c6`. Final welcome, relaunch, RTL home and RTL Settings captures use the final APK.

## Observed native checks

- Home has BPM/playback priority, quiet tempo label, no four math buttons, equal Practice/Sound footprints and clear bottom space. It stayed paused through entry and preview checks.
- Exact tempo displays the numeric keyboard without obscuring its confirmation. A cancelled draft preserved live tempo. A verified 132 BPM draft changed to 137 with +5 and updated live tempo only after Set tempo.
- Rhythm order is time signature, beat pattern, subdivision and count-in. Seven-eight selection, muting beat 2, Triplets and count-in persisted independently. Accent fill and muted state remained distinct; no visible beat numbers.
- All seven sound rows fit the normal viewport. Choosing Soft retained one selected row. Previewing Click showed a square Stop control while Soft stayed selected; early stop returned to Play without row movement. Previewing left practice statistics at 0 minutes.
- Audio's Stereo pan revealed and collapsed. Playback and Beat display disclosures contained their controls and left no visible switches after collapse. Palette and theme selection retained their footprint.
- Help, web availability, permanent More by Merkost/Suby promotion and version footer were reachable. The promotion has no dismiss control.
- Practice data, Help/About, guide and What's New were readable and reachable. No backups were imported/exported and no statistics were reset.

## Correction pass

Native checks found manual Dark mode could leave system-bar icons black, an unselected Mono swatch could disappear on black, and the long Practice data sheet clipped its Done button while vertical swipes did not reveal its end gap. The correction APK fixes all three in actual Android UI: Dark icons remain readable under light OS; the black swatch has a visible outline; an upward scroll now moves the long-sheet title offscreen. Pixel measurement of Done gives painted top 2116 / bottom 2252, 137 px ≈ 52 dp, with exactly 84 px = 32 dp below it to safe viewport bottom 2337.

## Final acceptance pass

- At 1.6 text scale with Arabic app locale, the final actual home shows 80 in the correct order and the Settings Back arrow points right. The updated captures replace the earlier defect screenshots.
- The human explicitly approved resetting only Metronome's app data on this disposable emulator after automatic review had rejected the original reset. Two approved resets of only `com.merkost.metronome` on `emulator-5596` were used for Get started and Skip checks. No other app or device data was reset.
- The fresh welcome is one graphic screen with three brief feature rows and Get started / Skip. There are no pages, pagination dots or Next controls. All content and the bottom action fit at standard text size and at 1.6 scale with Arabic RTL. Large text uses an accessible logo header and a stable-height action with a mirrored directional arrow.
- Get started enters default 80 BPM / Wood / 4/4, showing Start metronome and 0:00. It does not start playback. Force-stop and reopen go directly to the same paused home, establishing completion persistence.
- A second fresh-state check confirmed Skip also enters the default paused home.
- Restored configuration was read back as app locale list [], font scale 1.0, forced RTL 0 / false and night mode no. The human's Medium Phone configuration and data remain untouched.

## Proof limits

These screenshots and interactions establish the observed native UI states. The emulator was launched without audio output, so they do not establish audible quality or hardware audio timing. Software graphics made initial startup/input very slow; the task-only emulator was restarted with host graphics and its debug code precompiled. No performance acceptance is claimed from this loaded debug environment. Automated rendering and callback checks are recorded separately by the main verification gate.

## Additional native Practice and accessibility checks

- Practice hub has clear saved-setup/training groups and a quiet summary.
- Five-minute timer started and counted down; pausing playback froze it at 4:26 across a later control-sheet inspection. Stop timer returned to paused home.
- Tempo trainer displayed Start/Target/Step/Every parameters, started playback and an 80→120 active strip; pause/stop returned to idle.
- Gap trainer selected 2 play bars + 1 mute bar, started and displayed Muted bar 1/1 with 2+1 summary; pause/stop returned to idle.
- Preset empty state exposes one Save current setup action. Its named dialog kept Cancel/Save above the keyboard; cancellation saved nothing.
- Practice-set empty state makes Create set primary and warm-up preview secondary. Starter preview opened a three-step editor stating nothing is added until Save; leaving it saved nothing.
- At 1.6 text scale with Arabic app locale, Settings headings/descriptions/swatches and theme choices remain readable; Sound names/descriptions and independent previews remain readable. A slow scroll in the neutral outer gutter fully exposes Studio and its description/preview.
- Saved-set creation/playback and session editing were covered by the separate automated native rendering/logic gate rather than this manual Android pass.
