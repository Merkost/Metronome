# Metronome: next UI and feature concepts

This is a proposal for the next web design mockup, grounded in the current working tree on 22 October 2026. It does not change the submitted 1.5.0 Mac app. Benefits below are product hypotheses, not measured user demand. Current screens need the parent task's fresh visual review; source inspection alone is not a visual audit.

## Keep the instrument in charge

Keep BPM, the unnumbered beat row and Play as the main hierarchy. The outside ring means the active beat; solid fill means an accented sound. Show only one active practice summary on the instrument. Secondary tools open a focused sheet on a phone or bounded dialog on Mac. Use literal names, a little warmth, system typography, 48px targets and the existing spring vocabulary.

The welcome stays one graphic screen with three brief benefits and Get started. It should not ask the musician to configure a session. Entering the app stays silent and preserves their setup. More by Merkost stays a permanent, quiet Settings footer, with web access immediately above it.

## Existing features to improve, not announce as new

| Current capability | Evidence in the current tree | Mockup treatment |
| --- | --- | --- |
| Seven sounds, audition and sound profiles | `model/ClickSound.kt`; `model/ClickSoundSpectrum.kt`; `screens/SoundPickerSheet.kt` | Keep a stable preview control, a named sound and a small tone profile. No extra card just to announce sounds. |
| Presets and ordered practice sets, starter routine, recovery and Practice Again | `presets/`; `practiceSets/`; `screens/PracticeStarterCard.kt` | Use consistent user labels: Saved setups and Routines. Show meaningful rhythm/goal summaries and one clear resume/start action. |
| Timer, tempo trainer, fixed gap trainer and statistics | `screens/PracticeHubSheet.kt:64-109` | Group by intent: Practice tools, then Saved. Avoid a second stats dashboard or repeating the same totals in multiple places. |
| Import/export of presets and practice sets | `components/PracticeBackupControls.kt:21-52` | Put export/import together under Practice data, with stable-width buttons and a visible success/result row. |
| Desktop navigation, native menus, playback and tempo shortcuts | `ui/DesktopAppShell.kt:67-71`; `desktopApp/.../Main.kt:177-211` | Improve discoverability in tooltips and the keyboard-help view. Keyboard support itself is already implemented. |
| Single-screen welcome, appearance, themes and shared motion | `screens/WelcomeScreen.kt`; `ui/AppAnimations.kt`; `DESIGN.md` | Preserve the established direction; refine density, disclosure and focus rather than adding another onboarding flow. |

Paths above are relative to `shared/src/commonMain/kotlin/com/merkost/metronome/`, except the explicitly named desktop and design files.

## Proposed features, in delivery order

### 1. Mini-player for Mac

**Why:** Keep the beat beside sheet music or a lesson without dedicating a large window to it. The current desktop window has a 520×600 minimum (`desktopApp/src/main/kotlin/com/merkost/metronome/desktop/Main.kt:142`); there is no separate mini-player mode in the current shell.

**Flow:** Instrument → Mini-player → compact pulse window → expand to the full app. The compact state contains BPM, four or more unnumbered beats, minus/plus, Play and Expand. An optional Keep on top control is explicit. Playback continues across the transition, and Escape or Expand returns to the full instrument. Keep the same audio session and setup; do not create a second playback instance.

**Mockup:** A working compact/full toggle with a 320px-style preview and shared controls. Label window pinning as a proposed native behavior in the review notes; a browser surface does not prove actual macOS window behavior.

**Scope:** Highest-priority desktop proposal. On mobile, use Focus view only if testing finds it useful; do not replace the already-clean main screen.

### 2. Tempo trainer: hold, repeat and time-based steps

**Why:** Let a player repeat an exercise at a target tempo and gradually build a short practice block without restarting the ramp manually. The current `GradualTempoConfig.kt:5-32` only defines start BPM, end BPM, increment and bars per step. Hold, loop/rounds and seconds per step are new behavior. The roadmap already proposes related work in R7-01; this is a design proposal, not an accepted release date.

**Flow:** Practice → Tempo trainer → Start/Target → Step size → Every [bars/seconds] → At target [Hold/Repeat/Stop]. Show one sentence before starting: “80 → 112 BPM · +4 every 4 bars · hold 8 bars · repeat 3 times.” Start returns to the instrument with one progress strip. The expanded active view shows current phase, next change and round count. Pause preserves the phase; editing makes Apply versus Restart explicit.

**Mockup:** Interactive configuration and a short simulated round preview. Avoid presenting accelerated preview time as actual playback accuracy.

**Scope:** P1 practice depth. Keep all advanced choices inside the trainer, not on the home screen. Persist the configuration with the setup when implemented.

### 3. Swing feel

**Why:** Add an audible rhythmic feel rather than another visual style. The current `Subdivision.kt:3-7` exposes only Quarter, Eighths, Triplets and 16ths. Swing is not present. Roadmap R7-02 proposes ratio-based swing and subdivision gain.

**Flow:** Rhythm → Subdivision → Eighths → Feel [Straight/Swing]. Selecting Swing reveals a ratio selector with 50%, 58%, 66% and 75%, plus a pair of visual offbeat markers. A short audition lets the musician hear the choice. Keep the beat unit and BPM unchanged; label the new offbeat placement explicitly. Softer subdivision clicks live in an expandable control below.

**Mockup:** Ratio changes move the two markers and change the audition rhythm. The “New concept” label belongs in review annotations, not as a permanent product badge.

**Scope:** P1 musical capability with engine work. It needs native scheduling, audio-onset checks and preset migrations before release. The web concept proves interaction and explanation only.

### 4. Gap trainer: build the silence

**Why:** Extend the existing fixed audible/muted cycle with progression. Current `GapTrainerConfig.kt:3-18` has only fixed playBars and muteBars; there is no progressive or random mode. Roadmap R7-01 already outlines these extensions.

**Flow:** Practice → Gap trainer → Mode [Fixed/Progressive]. Fixed remains the default. Progressive starts with “Play 4 bars, mute 1”; “Add 1 quiet bar every 2 cycles, up to 4” is disclosed only for this mode. Show a small play/quiet cycle diagram and an exact sentence before Start. The active strip says “Quiet · 2 of 3 bars” instead of implying playback stopped.

**Mockup:** Mode switch reveals the extra controls smoothly, cycle markers show the current phase, and a preview advances through the progression. Random mode can stay a later annotation so the first design is not overloaded.

**Scope:** P2 depth after the trainer and rhythm work. Do not promise timing scores or accuracy feedback: the app does not listen to the musician.

## UI changes to show beside the proposals

1. **A clearer practice entry.** Keep one Practice entry point. Use task names Timer, Tempo trainer and Gap trainer, then Saved setups and Routines. On Mac, the current rail sends “Practice” directly to the set library in compact mode; a unified Practice overview would make the meaning more consistent. This is a proposed navigation change, not a missing feature.
2. **One active-mode summary.** Keep the instrument clean when idle. When a timer, trainer or routine is running, a single strip owns status, progress and access to details. Avoid parallel status chips that compete with the beat.
3. **Use density where it helps.** On Mac, dialogs can put short coupled fields on one row, while the instrument stays a centered, readable width. On phone, the same fields stack and retain 48px hit areas. Large text should reflow without shrinking controls.
4. **Settings stays familiar.** Audio, Appearance, Playback, Beat display, Practice data and Help remain literal groups. Audio and Appearance are the directly editable highlights. Secondary controls reveal with the shared height/opacity motion; label and icon swaps stay bounded. Test rapid reversal and reduced motion.
5. **Consistent naming and states.** A preset stores more than a tempo, so Saved setups is a better proposed user label. Routines can be the product label for ordered practice sets. Empty states offer one useful starter; unfinished edits keep Cancel/Discard protection. Do not silently seed content or lose a running session when navigating.

## Deliberately leave out

Accounts, cloud sync, leaderboard scores, generic AI coaching, a tuner, recording and new subscription prompts do not serve this scope. Session history and notes are explicitly parked in `docs/ROADMAP.md:549`; show them only as a research question if needed, not a top-priority dashboard. Do not add a correctness score without audio input and validated analysis. More sounds should follow listening and loudness work rather than making the picker larger by default.

## Proposal acceptance

- The mockup labels proposed versus existing behavior in its review layer.
- BPM only animates changed digits; preview buttons and appearance selectors keep their size.
- Beat sound state and active state remain distinct and accessible.
- Disclosure, theme, sheet and page motion share tokens and respect reduced motion.
- Welcome, instrument, Practice, settings and the four proposed flows work at phone and desktop sizes.
- Browser interaction tests are kept separate from native UI/audio timing evidence.
