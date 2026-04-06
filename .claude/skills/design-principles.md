---
name: design-principles
description: Enforce design principles when creating or modifying UI components, screens, or visual elements in the Metronome app. Use when writing Compose UI code, adding new features with UI, or reviewing UI changes.
---

# Metronome Design Principles

Apply these principles when creating or modifying any UI in this project.

## Reusable Components

Before creating new composables, check if an existing one can be reused or extended:

- **Buttons**: `MySecondaryButton`, `MySecondaryTextButton`, `MyIconButton`, `PlayButton` in `components/Buttons.kt`
- **Balls**: `MetronomeBalls` in `components/MetronomeBalls.kt` — custom layout with animated indicator
- **Settings rows**: `SettingsRow` pattern in `screens/SettingsScreen.kt`

When building new shared patterns, extract them as composables:

- **DropdownSelector** — for any tappable element that opens a dropdown menu (time signatures, tempo presets, etc.). Consistent dark surface, 16dp corners, highlighted active item.
- **FeatureCard** — for overlay info cards on the main screen (practice timer, gradual tempo). Consistent: `surface` background, `outline` border, 16dp corners. Takes title, status tag, content slot, optional progress bar.
- **StatusTag** — small pill label inside cards. Takes text + color. 9px bold, 8dp corners, tinted background.
- **SpotlightOverlay** — for coach marks / tooltip guidance. Takes target bounds, tooltip content, step info, navigation callbacks.

## Animation Rules

1. **Interactive animations** (responding to user action): Always use `SpringSpec`
   - `stiffness = Spring.StiffnessMedium`
   - `dampingRatio = Spring.DampingRatioMediumBouncy`
   - Examples: ball selection, button press, dropdown open/close

2. **Ambient animations** (continuous, non-interactive): Use `InfiniteTransition`
   - Examples: play button breathing glow while metronome is active
   - Keep subtle — alpha range 0.08–0.18, duration ~2 seconds

3. **State transitions**: Use `AnimatedContent` or `Crossfade` with `tween(250)`
   - Examples: play/pause icon switch, tempo name changes

4. **Beat-synced animations**: Use `SpringSpec(stiffness=600, dampingRatio=0.8)`
   - Matches existing color flash animation timing
   - Examples: ball glow on beat, ball scale bump

## Color & Theming

- **Always use `MaterialTheme.colorScheme.*`** — never hardcode hex colors for UI surfaces, text, or borders
- The app supports 6+ color schemes in both light and dark mode
- Feature-specific accent colors (green for timer, orange for gradual tempo) are allowed ONLY in `StatusTag` and progress bar fills — not for surfaces or text
- Use `primary`, `primaryContainer`, `surface`, `surfaceVariant`, `outline` from the theme

## Spacing & Layout

Follow constants from `ui/Dimensions.kt`:
- `horizontalPadding = 18.dp` — screen edge padding
- `BallSize = 40.dp` — beat ball clickable area
- `CircleSize = 72.dp` — ball indicator ring
- `defaultPlayButtonSize = 85.dp`
- Inter-section spacing: `32.dp` (matches existing MainScreen gaps)
- Settings row spacing: `16.dp`

## Typography

- Use Material3 styles only: `bodyLarge`, `titleLarge`, `displayLarge`
- BPM display: `displayLarge` + `fontWeight = ExtraBold` + `fontSize = 62.sp`
- Section labels: `bodyLarge` + `fontWeight = Bold`
- Button text: `titleLarge` + `fontWeight = Bold`
- Do NOT add custom text styles or font sizes

## Platform Abstraction

- Use **interface + Koin DI** for platform services (audio, haptics, version info, platform actions)
- Use **`expect/actual`** only for composable functions that render differently per platform
- Register platform implementations in `AndroidModule.kt` / `IosModule.kt`

## Dropdown Menus

Both time signature and tempo preset pickers follow the same pattern:
- Triggered by tapping an existing UI element (chip or label)
- Rendered as `DropdownMenu` with consistent styling
- Dismiss on selection OR outside tap
- Active item highlighted
- Keep options concise — label + secondary info

## Feature Cards (Overlay Cards)

Cards for active features (practice timer, gradual tempo) follow these rules:
- Appear **above the bottom control bar** on the main screen
- Use `FeatureCard` composable with consistent appearance
- Only **one card visible at a time** — if both active, gradual tempo shows (timer runs silently)
- Cards are session-only — dismissed on app close
- Include a dismiss/close gesture (swipe or tap X)

## Long-Press Activation

Advanced features (practice timer, gradual tempo) activate via long-press on existing elements:
- Provide haptic confirmation on long-press
- Show configuration UI (duration picker or bottom sheet) before activating
- Keep tap behavior unchanged (tap = existing action, long-press = advanced)
