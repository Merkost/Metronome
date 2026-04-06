# Metronome — Compose Multiplatform

## Project Overview

A metronome app built with Kotlin Multiplatform and Compose Multiplatform targeting Android and iOS.

## Architecture

- **Kotlin Multiplatform** with `commonMain`, `androidMain`, `iosMain` source sets
- **Compose Multiplatform** for shared UI
- **Koin** for dependency injection (interface-based platform abstraction, NOT `expect/actual`)
- **DataStore Preferences** for persistent settings
- **AVAudioEngine** (iOS) / **SoundPool** (Android) for audio playback

### Module Structure

```
composeApp/
  src/commonMain/   — Shared UI, models, view models, engine
  src/androidMain/  — Android Activity, Service, audio, platform impls
  src/iosMain/      — iOS view controller, audio, platform impls
iosApp/             — Xcode project (Swift entry point, resources)
```

### Key Patterns

- **Platform abstraction via interfaces + Koin**, not `expect/actual` for services. Example: `MetronomePlayer` interface with `MetronomePlayerAndroid` / `MetronomePlayerIos` injected via Koin modules.
- **`expect/actual`** used only for composable functions that differ per platform: `DynamicTheme`, `PlatformUtils`, `PlatformSettingsComponents`.
- **State management**: `MutableStateFlow` in ViewModels, collected via `collectAsState()` in composables.
- **Single ViewModel pattern**: `MetronomeViewModel` is a `single{}` in Koin (not `viewModel{}`), shared across the app. `SettingsViewModel` uses `viewModel{}`.

## Build Commands

```bash
# Android debug build
./gradlew :composeApp:assembleDebug

# iOS framework (simulator)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# iOS framework (device)
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

## Code Style

- **Animations**: Use `SpringSpec` for interactive animations (`StiffnessMedium`, `DampingRatioMediumBouncy`). Use `InfiniteTransition` only for ambient/non-interactive effects.
- **Spacing**: Follow `Dimensions.kt` constants — `horizontalPadding = 18.dp`, `BallSize = 40.dp`, `defaultPlayButtonSize = 85.dp`.
- **Colors**: Always use `MaterialTheme.colorScheme.*` — never hardcode hex values for UI elements. The app supports both light and dark themes across multiple color schemes.
- **Typography**: Use Material3 type styles from `Type.kt`. No custom fonts.
- **Components**: Prefer reusable composables. Existing patterns: `MySecondaryButton`, `MyIconButton`, `PlayButton`. New components should follow the same parameter conventions (content slot, onClick, modifier).
- **New platform features**: Use interface + Koin DI pattern (matching `MetronomePlayer`, `PlatformActions`, `AppVersionProvider`). Reserve `expect/actual` for composable-only differences.

## Audio Resources

- Android: `composeApp/src/androidMain/res/raw/` — wood.mp3, click.mp3, metronome.wav
- iOS: `iosApp/iosApp/Resources/` — must be added to Xcode "Copy Bundle Resources"

## Testing

```bash
./gradlew :composeApp:testDebugUnitTest   # Android unit tests
```
