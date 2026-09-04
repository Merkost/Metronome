---
version: alpha
name: Metronome
description: "Precise timing. Better practice."
colors:
  instrument-ink: "#000000"
  clean-paper: "#FFFFFF"
  surface-light: "#F2F2F2"
  surface-dark: "#1A1A1A"
  melrose-violet: "#B89FFF"
  melrose-violet-deep: "#6F55C8"
  periwinkle-blue: "#C9D5FE"
  periwinkle-blue-deep: "#5B6ABF"
  mint-pulse: "#9EFFAE"
  mint-pulse-deep: "#1A8A2E"
  pink-lace: "#FFCAEA"
  pink-lace-deep: "#C4547E"
typography:
  tempo-display:
    fontFamily: "system-ui, sans-serif"
    fontSize: "62px"
    fontWeight: 800
    lineHeight: "64px"
    letterSpacing: "-0.2px"
  data-display:
    fontFamily: "system-ui, sans-serif"
    fontSize: "45px"
    fontWeight: 800
    lineHeight: "52px"
    letterSpacing: "0px"
  title-large:
    fontFamily: "system-ui, sans-serif"
    fontSize: "22px"
    fontWeight: 700
    lineHeight: "28px"
    letterSpacing: "0px"
  body-large:
    fontFamily: "system-ui, sans-serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: "24px"
    letterSpacing: "0.5px"
  body-medium:
    fontFamily: "system-ui, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: "20px"
    letterSpacing: "0.2px"
  label-large:
    fontFamily: "system-ui, sans-serif"
    fontSize: "14px"
    fontWeight: 500
    lineHeight: "20px"
    letterSpacing: "0.1px"
rounded:
  sm: "10px"
  md: "12px"
  lg: "16px"
  xl: "28px"
  full: "9999px"
spacing:
  sm: "8px"
  md: "16px"
  lg: "32px"
  horizontal: "18px"
components:
  play-button:
    backgroundColor: "{colors.instrument-ink}"
    textColor: "{colors.clean-paper}"
    rounded: "{rounded.full}"
    size: "85px"
  primary-action:
    backgroundColor: "{colors.instrument-ink}"
    textColor: "{colors.clean-paper}"
    rounded: "{rounded.full}"
    height: "52px"
  secondary-action:
    backgroundColor: "{colors.clean-paper}"
    textColor: "{colors.instrument-ink}"
    rounded: "{rounded.full}"
    height: "48px"
  chip-selected:
    backgroundColor: "{colors.instrument-ink}"
    textColor: "{colors.clean-paper}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: "48px"
  chip-rest:
    backgroundColor: "{colors.surface-light}"
    textColor: "{colors.instrument-ink}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: "48px"
  slider:
    backgroundColor: "{colors.surface-light}"
    textColor: "{colors.instrument-ink}"
    rounded: "{rounded.full}"
    height: "48px"
  beat-indicator:
    backgroundColor: "{colors.instrument-ink}"
    textColor: "{colors.clean-paper}"
    rounded: "{rounded.full}"
    size: "40px"
  status-strip:
    backgroundColor: "{colors.surface-light}"
    textColor: "{colors.instrument-ink}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: "48px"
  bottom-sheet:
    backgroundColor: "{colors.clean-paper}"
    textColor: "{colors.instrument-ink}"
    rounded: "{rounded.xl}"
    padding: "18px"
    width: "480px"
---

# Design System: Metronome

## Overview

**Creative North Star: "The Precision Instrument"**

Metronome should feel like a beautifully machined studio instrument: immediate, calm and exact. The interface is predominantly monochrome, with a single dominant tempo readout, tactile circular controls and quiet supporting surfaces. Its visual confidence comes from proportion, spacing and state clarity rather than decoration.

The basic pulse must remain obvious to a first-time musician while serious practice tools unfold progressively for experienced users. Motion is brief and physical, colour is a signal, and platform-specific behavior appears only where native conventions or capabilities make the product better. The system rejects flashy effects, gamified celebration, dashboard density and ornamental complexity.

**Key Characteristics:**

- Instrument-like hierarchy led by BPM, beat state and playback.
- Restrained monochrome foundation with optional focused accent schemes.
- Circular primary controls, pill-shaped choices and softly rounded sheets.
- Spacious single-column composition capped at a compact content width.
- Subtle spring feedback that communicates state without demanding attention.
- Shared Android and iOS identity with native behavior where it matters.

## Colors

The palette is a high-contrast black-and-white instrument face with quiet tonal containers and four optional accent families.

### Primary

- **Instrument Ink** (#000000): The default light-theme primary, dominant text, selected controls and decisive actions.
- **Clean Paper** (#FFFFFF): The default light canvas, inverse content and the dark-theme primary.

### Secondary

- **Melrose Violet** (#B89FFF) and **Deep Melrose Violet** (#6F55C8): A calm violet option for active state, progress and theme identity.
- **Periwinkle Blue** (#C9D5FE) and **Deep Periwinkle Blue** (#5B6ABF): A cool, measured option with the same semantic role.
- **Mint Pulse** (#9EFFAE) and **Deep Mint Pulse** (#1A8A2E): A fresh timing signal, never a decorative wash.
- **Pink Lace** (#FFCAEA) and **Deep Pink Lace** (#C4547E): A warm optional accent that retains the product's restraint.

### Neutral

- **Soft Light Surface** (#F2F2F2): Unselected controls and quiet light-theme containment.
- **Deep Dark Surface** (#1A1A1A): The equivalent dark-theme tonal layer.
- Intermediate light containers range from near-paper to soft grey; dark containers range from near-black to charcoal. Use the semantic `MaterialTheme.colorScheme` roles rather than selecting these tones directly in UI code.

### Named Rules

**The Signal, Not Decoration Rule.** Accent colour marks selection, active playback, progress or a chosen theme; it does not fill screens or create visual novelty.

**The Semantic Reversal Rule.** Light and dark appearances reverse semantic foreground and background roles while preserving hierarchy and contrast.

## Typography

**Display Font:** Platform system sans-serif

**Body Font:** Platform system sans-serif

**Character:** Native, direct and highly legible. Weight creates the instrument hierarchy; no custom display face competes with the musician's task.

### Hierarchy

- **Tempo Display** (extra-bold, 62px with a 64px line height): The singular BPM readout. It auto-sizes only when the available width or accessibility scale requires it.
- **Data Display** (extra-bold, 45px with a 52px line height): Timer values and active trainer BPM.
- **Title Large** (bold, 22px with a 28px line height): Sheet titles and high-level configuration headings.
- **Body Large** (regular, 16px with a 24px line height): Primary labels and settings headings; use bold or semi-bold for emphasis.
- **Body Medium** (regular, 14px with a 20px line height): Supporting explanations and expandable-section summaries.
- **Label Large** (medium, 14px with a 20px line height): Chips, status controls and compact actions; selected labels become bold.

### Named Rules

**The Tempo Owns the Room Rule.** Only the current BPM receives the largest type on the main surface; secondary values must not compete with it.

**The Native Voice Rule.** Use Material 3 typography with the platform system font and dynamic text scaling. Do not introduce custom fonts or uppercase display styling.

## Layout

The app uses a centered single-column instrument layout. Content fills compact screens but never exceeds 480dp on wide phones or tablets. Screens and sheets use 18dp horizontal padding, with an 8dp, 16dp and 32dp spacing rhythm for local, grouped and sectional relationships.

The main screen keeps playback controls anchored as a stable bottom action row while the tempo and beat region can scroll when text or available height demands it. Settings and bottom sheets scroll vertically, respect safe areas and navigation insets, and keep content centered inside the same maximum width. Advanced tools remain progressively disclosed inside the tempo sheet so the basic metronome stays immediate.

At large accessibility sizes, compact labels may use bounded auto-sizing, descriptive text may wrap, and repeated values divide available width evenly. Do not solve text pressure by reducing touch targets below 48dp or by expanding tablet content into an unnecessarily wide dashboard.

## Elevation & Depth

Depth is primarily tonal and structural: thin outlines, surface-container steps, scrims and the physical silhouette of controls establish hierarchy. Material elevation tokens of 1dp, 2dp, 4dp and 8dp exist for genuinely layered surfaces, but the incumbent interface is flat by default and avoids conspicuous shadows. Bottom sheets use a modal scrim and a distinct surface rather than glass, blur or a heavy floating-card treatment.

### Named Rules

**The Flat Instrument Rule.** Controls rest on the surface without decorative shadow; depth appears only when containment, modality or interaction state requires it.

## Shapes

The form language combines exact circles with restrained soft rectangles. Beat indicators, the play control and compact tempo actions are circular. Chips, status strips, selectors and full-width actions are pills. Containers use gently increasing 10dp, 12dp and 16dp radii, while modal bottom sheets use a 28dp top radius. The play button transforms from a circle toward a compact rounded square while playback is active.

Borders are thin and quiet by default. Selected sound and colour controls may use a stronger border to express state, but selection must remain understandable without relying on colour alone.

## Components

### Play Button

- **Character:** The singular, tactile playback control and the strongest object on the screen.
- **Shape:** 85dp circle at rest; animates toward a rounded square during playback.
- **Colour:** Semantic primary with inverse on-primary icon.
- **State:** Brief spring press, haptic confirmation, play/pause icon crossfade and a restrained ambient glow only while active.

### Primary Actions

- **Character:** Confident but rare; used to start or stop a configured practice mode.
- **Shape:** Full pill with a 52dp height.
- **Colour:** Semantic primary with on-primary text.
- **Usage:** One dominant primary action per focused sheet state.

### Secondary and Icon Buttons

- **Character:** Refined and restrained.
- **Shape:** Thin outlined pills for text actions; 70dp circles for prominent icon actions; never below the 48dp touch minimum.
- **State:** Scale to 96% on press using the shared interactive spring. Icon-button containers use a low-alpha semantic-primary tint.

### Chips and Selectors

- **Style:** 48dp minimum-height pills. Selected chips use primary/on-primary; unselected chips use surface-variant/on-surface-variant.
- **State:** Colour and size changes use the shared 600 stiffness, 0.8 damping spring. Press scales to 94%.
- **Usage:** `AppChip` owns selectable and removable options; `PillChip` owns compact dropdown anchors.

### Slider

- **Style:** `AppSlider` is the only numeric slider. It preserves Material semantics, a 48dp touch target and restrained tick visibility.
- **Usage:** Supply a concise accessibility label and allow the native slider semantics to communicate range and value.

### Beat Indicator

- **Character:** A compact visual pulse with a larger invisible interaction target.
- **Size:** 40dp normally and 32dp in compact layouts inside a 48dp minimum hit area.
- **State:** Accent, normal and mute are distinguished by fill, outline and assistive-technology descriptions.

### Status Strip

- **Character:** A quiet active-mode signal, not a dashboard card.
- **Shape:** 48dp pill with low-alpha accent fill and a proportional progress layer.
- **Content:** Icon, concise title, compact status and an optional 48dp stop action.

### Bottom Sheet

- **Character:** Focused configuration layered over the instrument.
- **Shape:** 28dp rounded top corners with a native modal scrim.
- **Layout:** Scrollable, safe-area aware, 18dp horizontal padding and a centered 480dp maximum content width.

## Do's and Don'ts

### Do:

- **Do** keep BPM, beat state and playback visually dominant.
- **Do** reuse `PlayButton`, `MySecondaryButton`, `MyIconButton`, `AppChip`, `PillChip`, `AppSlider`, `ValueStepper`, `StatusStrip` and `AppBottomSheet` before creating another control.
- **Do** use `MaterialTheme.colorScheme` roles so every colour scheme works in light and dark appearance.
- **Do** preserve 48dp minimum interaction targets even when the visible indicator is smaller.
- **Do** use the shared spring vocabulary for interactive feedback and reserve infinite motion for ambient active state.
- **Do** keep serious practice tools progressively disclosed and available offline.

### Don't:

- **Don't** make Tempo Trainer, Gap Trainer or practice statistics compete with the core metronome on the main surface.
- **Don't** add gradients, glass effects, ornamental shadows, decorative cards or multiple competing accent colours.
- **Don't** use hard-coded UI colours, Material icons or custom fonts.
- **Don't** introduce playful celebration, gamified rewards or persistent motion into a focused practice session.
- **Don't** shrink, truncate or overlap essential controls to accommodate text scaling; reflow or use bounded auto-sizing instead.
- **Don't** stretch the compact instrument composition across tablet width without evidence that a wider practice layout is needed.
