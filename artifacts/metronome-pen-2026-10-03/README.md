# Metronome design handoff

Created 3 October 2026.

Open [Metronome.pen](../../Metronome.pen) in Pen. The editable document contains **49 app screen/state frames, 10 website pages, 59 reusable components, 89 variables and 15 documentation/overview boards**. There are 74 top-level frames in total.

[App overview](exports/tdKiE.png) · [App and website overview](../website-refresh-2026-10-03/pen-exports/w4IXz.png) · [Complete 74-page PDF](exports/export.pdf) · [Canvas audit](design-audit.json)

## What's included

- Foundation: semantic colours, light/dark appearances, Mono/Violet/Blue/Mint/Pink schemes, system typography, spacing, radii and touch targets.
- Original Metronome vector mark from `docs/logo.svg`, rendered with semantic ink in the main toolbar. Existing Metronome and Suby artwork.
- Primary/secondary/icon actions, Play states, tempo steps and per-digit readout, custom linear and centred sliders, choices, fields, validation, rows, switches, disclosures and sheet headings.
- Welcome and optional sound selection; instrument stopped/playing/pendulum/timer states; Practice, rhythm, sound library, presets, practice sets, editor, sessions, timer, tempo trainer, gap trainer, data and Help & about.
- Settings in light/dark appearance, collapsed/expanded controls and Android layouts. Stereo pan, practice and motion controls, web app discovery and a permanent Suby section.
- Exact tempo, validation, preset saving, adding a preset, finish/reset confirmation, import, what's new, guide and sound sheets.
- Empty, inline-action, reorder, manual/bar goal, save failure, missing preset, session recovery, completion and final-step states.

## Website

Boards 60–73 cover the implemented public website: desktop/mobile homepage in light and dark, desktop/mobile Support, Privacy and 404 pages, 13 reusable website components, interaction states, responsive foundations and an overview.

The website uses the original Metronome mark, shared semantic colours, Roboto/system typography and Lucide icons. A playable 4/4 preview uses the three existing sounds. A custom slider animates changed tempo digits independently. Practice tool tabs, colour and appearance previews, FAQ expansion, platform choices and a permanent Suby recommendation share the same motion layer.

Sources are `docs/index.html`, `support.html`, `privacy.html`, `404.html`, `site.css`, `site.js` and `motion.js`. The privacy policy body and update date are unchanged. Native/web differences are stated explicitly: native apps save practice locally and support offline/background use; browser settings and presets currently reset on reload.

The website source was published through the existing main-branch deployment workflow. See [website verification](../website-refresh-2026-10-03/README.md) for release evidence and browser checks.

## Motion contract

The motion board maps to the existing `AppAnimations.kt` presets:

| Preset | Stiffness | Damping ratio |
| --- | ---: | ---: |
| Press | 1600 | 1 |
| Quick | 1100 | 1 |
| Standard | 700 | 1 |
| Emphasized | 340 | 0.94 |
| Calm | 190 | 1 |
| Expressive | 520 | 0.66 |
| Navigation | 400 | 1 |

Web conversion uses mass 1 and damping = 2 × ratio × sqrt(stiffness). The document includes corresponding variables, geometry, state examples and interaction specifications.

- A tempo change from 80 to 81 keeps the 8 still; only the ones place changes. Incoming layers use Standard, outgoing layers use Quick, with a 24 px distance and bounded layers.
- Reveals coordinate height, opacity, inner lift and chevron with Emphasized. Rapid reversal retargets the current transition.
- Theme colours use Calm and palette selection uses Emphasized; maintain readable contrast through appearance changes.
- Sliders follow pointer input immediately, use Standard for keyboard/programmatic changes, Press for the grip and Quick for hints.
- Reduced motion applies spatial changes and numerical values immediately; short opacity feedback remains available for other text and icons.
- Pendulum motion follows the beat interval; stop and weight-position changes use Calm.

The Pen canvas specifies these behaviours through static states and documentation. Use the [welcome preview](http://127.0.0.1:8767/?screen=welcome) and [Settings preview](http://127.0.0.1:8767/?screen=settings) to review actual prototype motion.

## Product and implementation boundaries

The file captures the current design proposal; it does not change the native app. Canvas Roboto is the Android system-font rendering proxy. Production retains the existing Material 3 platform-system typography and semantic MaterialTheme colours.

Wood, Click and Classic are current voices. Soft, Rim, Clave and Studio are proposed original audio sketches requiring production audio and device validation. Recovery, missing-preset repair and import conflict handling are proposed UX states that require native integration.

The public web action points to [Metronome web app](https://metronome.merkost.dev/app/). The permanent studio card points to [Suby](https://subyapp.com/).

## Sources

- `PRODUCT.md`, `DESIGN.md` and the project instructions.
- `shared/src/commonMain/kotlin/com/merkost/metronome/ui/AppAnimations.kt`.
- `shared/src/commonMain/kotlin/com/merkost/metronome/ui/Dimensions.kt`.
- `shared/src/commonMain/kotlin/com/merkost/metronome/components/Pendulum.kt`.
- `artifacts/design-exploration-2026-10-03/src/Prototype.tsx` and its shared motion/slider/theme components.
- Existing proposal captures under `artifacts/design-exploration-2026-10-03/public/captures`.

## Validation and portability

The final canvas audit found no overlapping top-level frames, broken component references, unnamed nodes, missing text fills, unfinished placeholders or unintended clipping. The overview intentionally crops the full-scroll Settings render to show its first viewport. Key screens, component states, expanded controls and dark variants were visually inspected.

The PDF contains all 74 top-level frames in numbered order. Native compilation, accessibility execution, audio timing and physical-device acceptance are separate work.

The Pen file uses relative references to 12 existing/project-generated PNG assets. Keep the repository folder structure when moving the document; their exact paths are in `design-audit.json`. The PDF provides a standalone review copy.

The permanent Suby recommendation is titled **More by Merkost** across the current Settings mockups, reusable Pen components and website. This replaces the earlier studio heading.

Beat indicators use unnumbered dots throughout the current instrument and rhythm designs. The accent ring, solid normal dot and outlined mute icon distinguish states. Accessible beat controls retain their index and current state.
