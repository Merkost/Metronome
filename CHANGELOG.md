# Changelog

All notable releases of **Metronome: Practice & Tempo**, named "Metronome: Feel
the Beat!" before 1.3.0. Each entry includes a short, paste-ready **store
message** for the App Store ("What's New") and Google Play ("What's new").
Play's field is capped at 500 characters, so from 1.3.0 each release carries a
separate message per store rather than one shared text — the App Store copy has
room to explain, and platform-specific fixes only appear in the store they apply
to. Newest first.

---

## 1.6.0 — Speaks your language
_2026-11-05 · Android versionCode 13 · iOS build 5 · macOS build 2_

Metronome in ten languages, a desktop app for the Mac and an instrument
with more voice. This release also carries everything prepared for 1.5.0, which
was never published on its own.

**Highlights**
- **Ten languages.** Spanish, French, German, Brazilian Portuguese, Italian,
  Japanese, Korean, Simplified Chinese, Russian and Turkish across Android, iOS,
  desktop and web, including the iOS Live Activity.
- **Metronome for Mac.** A native desktop app with a sidebar, keyboard
  shortcuts (Space, ⌘1 to ⌘3, ⌘,), shortcut hints as tooltips and a title bar
  merged into the window.
- **An instrument that talks back.** The count-in is spelled out in words,
  tempo names sit in non-overlapping bands, Settings sums up your practice and
  the timer and statistics show your week.
- **Hear before you start.** A Hear one bar button on the welcome step and a
  beat display preview in Settings. Colour schemes are drawn as miniature
  instrument faces.
- **More practice tools.** The tempo trainer can step after each gap cycle or
  on tap, a phrase marker accents the first bar of each phrase, and an option
  stops the metronome when the practice timer ends.
- **Steadier in the background.** The click keeps playing when the Android
  screen locks, Android gets lock-screen media controls, notifications duck the
  click and playback resumes after short interruptions. If audio cannot start,
  the metronome stops and says so, and a silent click explains why.
- **A beat with character.** Each beat is a strike with a tempo-aware ring, the
  constant halo is gone, the dark theme has a designed grey ladder, and motion
  respects the system reduce motion setting.
- **Everything from 1.5.0.** The one-screen welcome, a calmer main screen,
  clearer Settings and more focused practice tools.

**Fixes and interaction polish**
- Singular forms read correctly ("1 bar", "1 step").
- Hover tints no longer stack on the ripple on desktop.
- Tempo names no longer overlap between bands.
- The beat flash is limited to three flashes per second, and screen readers no
  longer announce every beat.

**Under the hood**
- Pure sentence builders return `UiText`, backed by string resources.
  `tools/check-translations.py` checks every locale file and a rendering test
  covers the key screens in all ten locales. 826 unit and desktop tests pass.
- Translations are machine-written and structurally checked, not yet reviewed
  by native speakers.

**Store message — Google Play** (488 of 500 characters)
```
Metronome 1.6.0

• Now in ten languages: Spanish, French, German, Portuguese, Italian, Japanese, Korean, Chinese, Russian and Turkish
• The count-in is spelled out in words, tempo names sit in clear bands and Settings sums up your practice
• The tempo trainer can step after each gap cycle or when you tap
• A phrase marker accents the first bar of every phrase
• Lock-screen playback controls, and the click keeps going with the screen off
• A new beat animation and a refined dark theme
```

**Store message — App Store** (1386 of 4000 characters)
```
A steadier, friendlier metronome

Metronome 1.6.0 speaks your language, keeps time more reliably in the background and gives the beat more character.

TEN LANGUAGES
Metronome is now available in Spanish, French, German, Brazilian Portuguese, Italian, Japanese, Korean, Simplified Chinese, Russian and Turkish.

AN INSTRUMENT THAT TALKS BACK
The count-in is spelled out in words, tempo names such as Andante and Allegro sit in clear bands, and Settings sums up your practice. The timer and statistics show your week at a glance, and a Hear one bar button on the welcome screen lets you try the click before you start.

MORE PRACTICE TOOLS
The tempo trainer can step after each gap cycle or when you tap. A phrase marker accents the first bar of every phrase and counts the bars. An option stops the metronome when the practice timer ends.

MORE RELIABLE IN THE BACKGROUND
Short interruptions such as notifications no longer end the session, and the click resumes afterwards. If audio cannot start, the metronome stops and tells you why. If the click is silent because a volume is at zero, you get a hint.

A BEAT WITH CHARACTER
Each beat is a strike with a ring that follows the tempo, and the dark theme has a calmer grey ladder. The beat animation follows the system reduce motion setting.

Also in this release: the cleaner welcome, calmer main screen and clearer Settings from 1.5.0.
```

---

## 1.5.0 — More space for your rhythm
_Prepared 2026-10-12 · Android versionCode 12 · iOS build 4_

A cleaner instrument and a simpler welcome, with refined practice tools,
stable controls and more comfortable layouts.

**Highlights**
- **One-screen welcome.** A graphic introduction with three short feature
  highlights and Get started or Skip. Both open the instrument silently and
  preserve the current setup.
- **A calmer main screen.** Tempo and the beat take priority. The −5, +5, halve
  and double shortcuts move into Set tempo and change only the draft until
  confirmation. Dismissing the sheet keeps the current tempo.
- **Clearer Settings and Rhythm.** Literal section names, quieter grouping
  and compact sound choices make controls easier to scan.
- **More focused practice.** Refined presets, routines, session controls,
  timers and trainers. The starter routine stays contextual to an empty
  practice-set library.

**Fixes and interaction polish**
- Long bottom sheets keep the complete final action visible with 32dp end
  spacing above the safe-area boundary.
- Larger text layouts wrap and scroll without shrinking touch targets.
- Welcome uses an accessible logo header at large text sizes; its primary
  action keeps a stable size and centers its text during saving feedback.
- Numeric readouts retain correct digit order in right-to-left layouts,
  while only the changed decimal places animate. Back arrows mirror correctly.
- Android system-bar icons follow app appearance for readable contrast.
- Unselected Mono swatches stay visible on dark surfaces.
- Welcome completion blocks duplicate taps and preserves existing sound
  selection without an unnecessary settings write.

**Store message — Google Play** (459 of 500 characters)
```
More space for your rhythm in 1.5.0

• One illustrated welcome with a few short highlights
• A cleaner main screen and clearer Rhythm, Sound and Settings
• Quick tempo adjustments moved into Set tempo, applied when you confirm
• Refined preset, routine, timer and trainer layouts with smooth transitions
• Better large-text layouts and scrolling, safer bottom-sheet spacing, correct right-to-left numbers and navigation
• Clearer status-bar icons in dark mode
```

**Store message — App Store** (968 of 4000 characters)
```
More space for your rhythm

Metronome 1.5.0 brings a cleaner, calmer experience, from the first welcome to your next practice session.

A SIMPLE START
A single illustrated welcome introduces tempo, sound and practice in a few short highlights. Get started or skip straight to the instrument, with no setup steps or automatic playback.

A CLEARER INSTRUMENT
The main screen gives the beat and BPM more room. Quick tempo adjustments now live in Set tempo: add or subtract 5 BPM, halve or double the value, then apply it when you're ready. Rhythm, sound and Settings are easier to scan.

SMOOTHER PRACTICE
Refined preset, routine, timer and trainer layouts keep the next action clear. Controls stay steady as their state changes, and transitions follow the app's shared motion style.

MORE COMFORTABLE LAYOUTS
Improved large-text layouts, scrolling and bottom-sheet spacing keep actions within reach. Numbers and navigation now display correctly in right-to-left layouts.
```

---

## 1.4.0 — Find your flow
_2026-10-07 · Android versionCode 11 · iOS build 2_

A feature and design release with a hands-on welcome, four new click sounds,
redesigned Settings and easier access to everyday practice tools.

**Highlights**
- **Interactive welcome.** Hear a starting pace, try click sounds and choose a
  setup before practising, or skip straight to the app.
- **Four new sounds.** Soft, Rim, Clave and Studio, each with a distinct accent.
  Quick previews and audio-derived tone profiles help compare the character of
  all seven sounds.
- **Redesigned Settings.** Audio, appearance, playback, beat feedback and practice
  data have clearer groups, supporting text and expandable controls.
- **Refreshed main screen.** A branded header, exact BPM entry and direct access
  to dedicated Rhythm and Practice tools.
- **Practice backups.** Export and import presets and practice sets using native
  file pickers on Android and iOS. Imports add to existing setups and validate
  the imported data.
- **Starter routine.** Preview and customise an editable practice routine before
  saving it as a Practice Set.
- **More ways to reach the app.** A web-app shortcut in Settings and a permanent,
  understated More by Merkost section featuring Suby.

**Motion and interaction**
- Shared motion tokens drive Settings reveals, stereo-pan controls, appearance
  and colour-scheme changes, sliders and press feedback.
- Custom sliders keep their track, thumb and feedback consistent across platforms.
- Tempo transitions animate only the digits that change.
- The pendulum uses harmonic motion with smooth turning points and crosses the
  centre on main clicks and count-in. Its phase follows native audio-submission
  timestamps, including live tempo changes, pauses and restarts.
- Unnumbered beat dots distinguish accents and mutes, with a separate outer ring
  for the active beat.

**Fixes and layout polish**
- iOS switches fade, move and clip with their Settings sections instead of
  appearing over neighbouring content during reveal and hide transitions.
- Appearance selections and sound-preview buttons keep stable dimensions when
  their state changes. Colour choices are left-aligned.
- Beat-haptics switch feedback stays inside its rounded shape.
- Backup actions have clearer labels, supporting copy and busy-state feedback.
- A shared bottom-sheet component respects top and side display cutouts and
  bottom insets; long content remains scrollable.
- The Rhythm sheet groups meter and beat editing, subdivisions and count-in.
  Home's Practice and Sound shortcuts have matching heights, and the final
  controls have extra bottom spacing.
- First-run tips place Skip using the device's safe-area inset and provide a
  full 48dp touch target. Round-button hover feedback stays inside the circle.

**Under the hood**
- Kotlin 2.4.20, Compose Multiplatform 1.12.1, AGP 9.3.3, Navigation 3 1.2.0 and
  Navigation 3 UI 1.1.2, Firebase BoM 34.19.0, GitLive Crashlytics 2.7.0.
- Platform-only dependencies moved out of `commonMain`.
- Expanded regression coverage for Settings motion, stable layouts, sheet safe
  areas, sound tone profiles, pendulum phase and practice backup handling.

**Store message — Google Play** (395 of 500 characters)
```
What's new in 1.4.0

• A fresh, interactive welcome with sound previews
• Four new clicks: Soft, Rim, Clave and Studio, with distinct accents and visual tone profiles
• Redesigned Settings and easier access to Rhythm and Practice
• Exact BPM entry, preset and practice-set backups, and an editable starter routine
• Smoother controls, more natural pendulum motion and better bottom-sheet layouts
```

**Store message — App Store** (1320 of 4000 characters)
```
Find your flow

Metronome has a fresh look and a more hands-on welcome. Try a starting pace, hear different clicks and choose what feels right before you begin practising.

FOUR NEW CLICK SOUNDS
Soft, Rim, Clave and Studio join the sound collection, each with its own accent. Quick previews and visual tone profiles make it easier to compare sounds and find your favourite.

CLEARER CONTROLS
Redesigned Settings bring audio, appearance, playback and beat options into clear groups. On the main screen, enter an exact BPM or jump straight to Rhythm and Practice. The Rhythm sheet makes meter, beat accents, subdivisions and count-in easier to find.

YOUR PRACTICE, READY TO GO
Back up and restore your presets and practice sets. Try the new starter routine, adjust it to suit your practice and save it when you are ready.

SMOOTHER THROUGHOUT
• More natural pendulum motion that follows the beat
• Smooth theme changes, custom sliders and tempo animation that moves only the digits you change
• Stable appearance choices and sound-preview buttons
• Better bottom-sheet spacing around screen cutouts and more breathing room on the main screen
• iOS switches now fade and move with their Settings sections

Settings also has a shortcut to Metronome on the web and a quiet More by Merkost section where you can explore Suby.
```

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

**Store message — Google Play** (426 of 500 characters)
```
What's new in 1.3.0

Practice Presets — save a complete setup (tempo, time signature, subdivision, accents and count-in) and return to it in one tap.

Practice Sets — line your presets up into a sequence and give each step a time or bar goal. Move through it at your own pace; an interrupted session resumes where you left it.

Practice Again repeats the set you last finished.

Plus a calmer, more consistent feel throughout.
```

**Store message — App Store** (977 of 4000 characters)
```
Structured practice

Save a complete setup as a Practice Preset — tempo, time signature, subdivision, per-beat accents and mutes, and count-in — then return to it in one tap. Favourite the ones you reach for most, rename or duplicate any of them, and keep them in the order you practise. Your existing saved tempos carry across.

Build a Practice Set from those presets and give any step a time or bar goal. Move forward and back at your own pace, pause and resume, and finish when you are done. If practice is interrupted, the session picks up where you left it.

Practice Again repeats the set you last finished, straight from the tempo sheet.

ALSO IN THIS VERSION
• A calmer, more consistent feel throughout, with smoother screen transitions
• Steadier feedback under your thumb across the tempo controls and sliders
• Playback now stops reliably when a call or another app takes over audio
• A short summary of what changed appears after each update, and stays in Settings
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
