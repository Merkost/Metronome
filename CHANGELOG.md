# Changelog

All notable releases of **Metronome: Feel the Beat!** Each entry includes a
short, paste-ready **store message** for the App Store ("What's New") and Google
Play ("What's new"). Store messages are kept under 500 characters to fit Play's
limit. Newest first.

---

## 1.2.0 — Live Activities & Dynamic Island
_2026-06-13 · Android versionCode 6 · iOS build 1_

**Highlights**
- **Live Activities & Dynamic Island (iOS 17+)** — current tempo, time signature
  and practice timer on the Lock Screen and in the Dynamic Island, with play/pause
  from there. The practice timer keeps ticking while the phone is locked.
- **Redesigned pendulum** — an upright mechanical metronome whose weight slides
  along the arm as the tempo changes.

**iOS settings, fully working**
- Native iOS switches for every toggle
- Background playback confirmed and tidy
- The volume slider now sets the metronome's own click loudness (same on both platforms)

**Polish**
- Softer "whisper-tint" tempo chip and +/− controls, so the BPM stays the star
- Animated tempo-name label on the main screen

**Fixes**
- Pendulum swings smoothly in odd time signatures (3/4, 5/4, 7/8)
- Corrected the App Store rating link

> **Note (iOS):** iOS skipped 1.1.0, so iOS users receive the 1.1.0 practice-tools
> features (below) in this update as well. The App Store "What's New" can fold in
> the 1.1.0 highlights if desired.

**Store message**
```
What's new in 1.2.0

• Live Activities & Dynamic Island (iOS): your tempo, time signature and practice timer on the Lock Screen — play and pause straight from the Dynamic Island.
• Redesigned pendulum: a real mechanical metronome whose weight shifts with the tempo.
• Cleaner main screen with a refined tempo chip and +/− controls.
• The volume slider now sets click loudness directly.
• Smoother pendulum swing in odd time signatures, plus iOS polish and fixes.
```

---

## 1.1.0 — Practice Tools
_2026-06-11 · Android versionCode 5 · Android only_

**Highlights**
- **Practice timer** — countdown or stopwatch, with custom durations, extend and restart
- **Tempo trainer** — gradually speed up or slow down across bars
- **Gap trainer** — alternate playing and silent bars to train your inner clock
- **Subdivisions** — eighths, triplets and sixteenths, with softer sub-clicks
- **Saved tempos** — bookmark BPM + time signature + subdivision
- **Practice stats** — daily time, total time and streaks
- One-bar count-in, per-beat accents and mute, pendulum beat display
- Refreshed design system, Lucide icons, and per-scheme theming

**Store message**
```
What's new in 1.1.0

• New practice timer: countdown or stopwatch, with custom durations, extend and restart.
• Tempo trainer to gradually speed up or slow down across bars.
• Gap trainer: alternate playing and silent bars to test your timing.
• Subdivisions — eighths, triplets and sixteenths.
• Save your favorite tempos, and track daily practice time and streaks.
• One-bar count-in, per-beat accents and mute, and a cleaner look.
```

---

## 1.0.0 — Cross-platform foundation
_Android versionCode ≤4 · iOS 1.0.0 (App Store launch)_

The app moved to Kotlin Multiplatform + Compose Multiplatform and launched on
iPhone and iPad alongside Android, sharing one codebase.

**Highlights**
- Precise tempo from 40 to 240 BPM
- Adjustable time signatures and per-beat accents
- Multiple click sounds and color themes (light & dark)
- Stereo panning, haptic feedback, keep-screen-awake
- Background playback

**Store message**
```
Metronome: Feel the Beat — now on iPhone and iPad.

• Precise, reliable tempo from 40 to 240 BPM.
• Adjustable time signatures and per-beat accents.
• Multiple click sounds and color themes (light & dark).
• Stereo panning, haptics and a clean, focused design.
```

---

_Going forward, add a new section at the top for each version before tagging the
release. Keep store messages under 500 characters for Google Play._
