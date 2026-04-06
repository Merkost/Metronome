<p align="center">
  <img src="androidApp/src/main/ic_launcher-playstore.png" width="120" alt="Metronome icon" />
</p>

<h1 align="center">Metronome</h1>
<p align="center"><i>Feel the Beat!</i></p>
<p align="center">A clean, simple metronome app built with <b>Kotlin Multiplatform</b> and <b>Compose Multiplatform</b> for Android and iOS.</p>

---

<img width="608" alt="Screenshot" src="https://user-images.githubusercontent.com/56008710/235447286-ab7db3fe-f206-44ea-827d-f7b21a31f6be.png">

## Features

- **Tap tempo** and BPM slider (40-220 BPM) with quick-adjust buttons
- **Time signatures** -- 2/4, 3/4, 4/4, 5/4, 6/8, 7/8
- **Click sounds** -- Wood, Click, Classic
- **Stereo panning** and volume control
- **Haptic feedback** on each beat
- **Practice timer** with countdown
- **Gradual tempo increase** -- automatically raise BPM over a set number of bars
- **Background playback** with foreground notification (Android)
- **Multiple color schemes** including dynamic Material You
- Light and dark theme support

## Tech Stack

| Layer | Tech |
|-------|------|
| Shared UI | Compose Multiplatform |
| Business logic | Kotlin Multiplatform (`commonMain`) |
| DI | Koin |
| Persistence | DataStore Preferences |
| Audio (Android) | SoundPool |
| Audio (iOS) | AVAudioEngine |

## Project Structure

```
shared/                 -- KMP library (shared UI, models, engine)
  src/commonMain/       -- Shared code
  src/androidMain/      -- Android platform implementations
  src/iosMain/          -- iOS platform implementations
androidApp/             -- Android application shell
iosApp/                 -- Xcode project entry point
```

## Build

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS (simulator)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Then open `iosApp/iosApp.xcodeproj` in Xcode to run on iOS.

## License

See [LICENSE](LICENSE) for details.
