# Changelog

All notable releases of **Metronome: Feel the Beat!** Each entry includes a
short, paste-ready **store message** for the App Store ("What's New") and Google
Play ("What's new"). Store messages are kept under 500 characters to fit Play's
limit. Newest first.

---

## 1.3.0 — Structured practice
_2026-09-03 · Android versionCode 8 · iOS build 1_

**Highlights**
- **Practice Presets.** Named, editable setups that capture BPM, time signature,
  subdivision, per-beat accents and mutes, and count-in state. Favourite,
  reorder, rename, duplicate, and apply them in one tap. Existing saved tempos
  migrate across without loss.
- **Practice Sets.** Order presets into a sequence and give any step an optional
  duration or bar target. Move back and forward through steps, pause and resume,
  and finish the set — an interrupted session is recovered when you return.
- **Practice Again.** Repeat the set you last completed straight from the tempo
  sheet.
- **Active practice strip.** One restrained strip on the main screen shows the
  running set, its progress, and previous/pause/next without leaving the
  instrument.
- **What's new in the app.** Release highlights appear once after an update, and
  stay available from Settings. New installs go straight to the app.

**Motion and interaction**
- A single motion system across the app: every animation now runs on one set of
  calm spring tokens, tuned for near-zero bounce rather than the springier mix
  that preceded it.
- Screen transitions are a parallax push — the incoming screen travels the full
  width while the one behind it recedes — used for navigation and for in-screen
  route changes alike.
- Lists animate when practice sets and presets are added, removed, or reordered,
  and reorder mode cross-fades in place of switching instantly.
- Consistent press feedback on every control, and haptics on the tempo cluster,
  steppers, chips, beat balls, and sliders, with slider detents derived from each
  slider's own range.
- Expanding a section in a sheet now scrolls it into view.

**Fixes**
- Playback stops predictably on iOS when a call or another app takes over audio,
  through a dedicated audio-focus controller matching the Android behaviour.
- A pressed practice row now scales as one card instead of shrinking its contents
  inside a card that stayed put.

**Under the hood**
- The review prompt appears only after a qualifying practice pause, and is
  suppressed while a timer, trainer, preset editor, or Practice Set is active.
- 139 unit tests, up from 26 in 1.2.1, covering preset and set storage, session
  control and recovery, review- and release-note prompt policy, navigation, and
  the audio-focus and sound-loading state machines.

**Store message**
```
What's new in 1.3.0

• Practice Presets — save a full setup and return to it in one tap
• Practice Sets — line up presets into a sequence with time or bar goals
• Practice Again — repeat your last finished set instantly
• A calmer, more consistent feel throughout, with smoother screen transitions
```

---

## 1.2.1 — Tighter timing
_2026-06-15 · Android versionCode 7_

**Fixes**
- **More accurate, drift-free timing.** The beat scheduler is now anchored to a
  monotonic clock with exact fractional tempo, replacing the old delay loop that
  accumulated drift and quantized BPM. Tap tempo rounds instead of truncating.
- **Audio robustness.** iOS serializes all audio-engine access (no more
  sound-switch glitch or crash), Android no longer drops a click when switching
  sounds, and the metronome stops cleanly on a phone call or when another app
  takes over audio.

**Under the hood**
- First unit tests in the repo: 26 tests covering the tempo math and the per-beat
  click schedule (subdivisions, accents, mute, per-channel volume).

**Store message**
```
What's new in 1.2.1

• More accurate, rock-steady timing
• Reliable audio on iPhone and iPad
• Smoother sound switching
```

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
