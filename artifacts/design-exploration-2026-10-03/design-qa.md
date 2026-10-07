# Metronome web exploration — visual QA

3 October 2026

final result: passed

## Findings

The latest onboarding direction is the hands-on revision at the end of this report. No actionable P0, P1 or P2 issues remain within the requested web-mockup scope. The native app remains a separate implementation and acceptance task.

The initial baseline is the existing app's instrument identity, together with `PRODUCT.md`, `DESIGN.md`, current shared UI and the user's requested settings/sound/Suby exploration. This is a redesign proposal, not a pixel clone or user approval of a native implementation.

## Source and implementation evidence

Source visual truth:

- `/Users/merkost/StudioProjects/Metronome/artifacts/product-review-2026-10-03/01-main.png`
- `/Users/merkost/StudioProjects/Metronome/artifacts/product-review-2026-10-03/02-settings-top.png`
- Four further source captures in the same folder ground tools, sets, editing and timer.

Rendered implementation: `http://127.0.0.1:8767/`.

Full-view, same-input comparisons:

- `captures/comparison-main.png`: source and rendered Instrument together.
- `captures/comparison-settings.png`: source and rendered Settings together.
- `captures/contact-practice.png` and `captures/contact-settings.png`: all sixteen rendered views.

Focused, same-input comparisons:

- `captures/comparison-focus-main.png`: heading, meter and beats.
- `captures/comparison-focus-settings.png`: heading, first group and sound row.

Additional rendered evidence: `captures/main-dark.png`, `settings-dark.png`, `suby-dark.png`, `settings-pixel.png`, `main-seven-beats.png`, `narrow-browser.png`, `design-board.png`, `gallery-browser.png` and `overview.png`.

## Normalization and state

Source PNGs are 718 × 1710 pixels. Their top simulator/window area was cropped at y=153; the 718 × 1557 content region was normalized to 393 × 852 pixels. This normalization is evidence preparation; the original captures remain intact. The template's calibrated screen includes its own live chrome and camera asset, so clock differences and device-frame differences are excluded from app-content fidelity judgments.

Implementation comparison viewport: 1400 × 1100 CSS pixels. The iPhone screen was verified at exactly 393 × 852 CSS pixels, scale 1, with a 393 × 852 pixel screenshot. No additional density scaling was applied. Explicit screen clips used its observed x=503.5, y=124 bounds. Pixel 10 was verified at 427 × 952 CSS pixels and captured at that size. Temporary viewport overrides were reset; the final retained preview uses the browser's default 1280 × 720 viewport.

Instrument comparison state: light, stopped, 80 BPM, 4/4, quarter subdivision, first beat accented, Wood, elapsed 0:00. Settings comparison state: light, Wood, 100% volume, haptics off and centre pan. The proposed secondary pan group is intentionally collapsed. Gallery captures use the default 75% preview volume.

The installed iOS 1.3.0 capture predates the checkout's Settings headings and theme selector. The source review confirms those improvements already exist in code; the proposal does not present them as wholly new features. Source captures and current-source evidence are distinguished in the suggestions.

## Required fidelity surfaces

- Fonts and typography: system font and strong tempo hierarchy retained. The BPM display is 62 px, Play is 85 px, body labels are 15 px with readable secondary copy. The centred sub-screen titles and calmer tempo descriptor are intentional changes. Full and focused comparisons show readable wrapping; the Suby description wraps naturally in both themes. No decorative/custom font was introduced.
- Spacing and layout: 18 px content padding, fixed playback controls and restrained separators retained. Practice and sound shortcuts, task-first Settings order and a starter routine are intentional additions. All sixteen views were checked. Seven beats now fit inside the screen with 48 × 48 px targets. Long Settings/editor content scrolls; final content does not require moving the persistent toolbar.
- Colors and tokens: semantic surface, container, primary, foreground, secondary text and outline roles follow the app's monochrome direction. Dark foreground, status icons and home indicator remain legible. Named scheme selection avoids swatch-only meaning. Native Material token mapping and large-type contrast acceptance remain implementation checks.
- Image quality and assets: real Metronome and Suby icons are used at appropriate scale; Lucide supplies UI icons. Supplied template device/status/keyboard assets remain intact. No handcrafted logo, emoji replacement, rasterized status bar or generated device frame was added.
- Copy and content: task labels and units are explicit. Existing presets/sets/trainers/stats are identified as current features; starter routines and sound sketches are proposals. Suby uses verified product wording and a website destination. Design rationale and mockup caveats remain outside the proposed phone UI, except explanatory import/reset demo states. Example totals are identified outside the phone and in the suggestions.

## Comparison history

1. **Blocked — P1: phone root auto-scroll after keyboard input.** Early captures showed displaced app chrome and the closed keyboard asset at the bottom. The app-owned sheet layer now dismisses the keyboard and pins the outer screen scroll position; ordinary content scrolling remains inside `MobileScroll`. Post-fix exact-tempo capture returned root scroll=0 and keyboard=false.
2. **Blocked — P1: auto-scroll could recur after changing devices; P2: transparent route background.** The initial reset only covered sheet/keyboard changes. A listener now keeps the outer phone screen at scroll=0 across browser focus/device changes. The app theme is applied to the correct `.flow-screen` selector. Recaptured all sixteen views after verifying settled navigation transforms. Pixel → iPhone and keyboard dismissal were rechecked. Updated contact/comparison sheets show clear edges and intact headers.
3. **Blocked — P2: seven-beat controls overflowed the phone.** 7/8 extended beyond both screen edges. Six/seven-beat rows now use 48 px square targets with 3 px gaps. Post-fix geometry: seven controls, minimum width 48, left=523 and right=877 inside the screen's 503.5–896.5 bounds. `captures/main-seven-beats.png` confirms the result.
4. **Passed.** Reopened the latest combined main/settings comparisons and focused Settings crop; inspected light/dark, seven-beat and compact-browser evidence. No remaining P0/P1/P2 layout or visual findings. Compact capture was taken after navigation settled, not during its spring transition.

## Browser interactions checked

- Exact BPM rejects 999, accepts 80, dismisses keyboard and returns to a stable screen.
- Sound previews start/stop and end after one bar; selection updates the shortcut; previews are separate from practice time and require paused playback.
- A starter can be reordered, edited, saved, started, paused, advanced and finished; early Finish returns to the instrument.
- Timer duration selection starts playback with a visible countdown; pause returns the main control to Play.
- Tempo and gap trainers start with the corresponding status strip; playback pauses correctly.
- Settings expand stereo controls and reflect checked state; both light/dark appearances render; Live Activity is absent on Pixel.
- Suby points to `https://subyapp.com/` and dismissal hides the recommendation without adding a new native destination.
- 360 × 900 browser: the Explore selector changes the view; page width remains 360, phone is scaled to fit, selector and device picker remain separate. The gallery also reflows to two columns.
- Suggestions render coverage, hierarchy, priorities, limits and four native audio players with the expected local WAV sources.
- Console errors/warnings checked after the clean reload: zero new entries. An earlier dependency-refresh/HMR error was resolved before the clean run and is not reported as a current app defect.

## Verification and limits

Final `npm run build` passed with the verified Node runtime. TypeScript and Vite passed; the runtime integrity check passed all 28 protected files. No protected runtime files were edited.

Only the main frontend journey is implemented. Light/Dark are rendered; System defaults to light in this preview. Count-in, pan, Pendulum, flash, reduced-motion and OS settings expose representative selections rather than their native effects. Imports, support/rating and statistics reset are explanatory demo states. Example presets simplify the full native preset model. Refresh resets state; audio uses a browser demonstration loop. No native timing, physical listening, Bluetooth, background, storage/recovery, VoiceOver/TalkBack or large-text acceptance is claimed.

## Implementation checklist

- Use the gallery and connected mockup to select the design direction.
- Preserve full preset/recovery contracts when implementing the hierarchy natively.
- Author and audition release sound masters separately from these sketches.
- Complete native light/dark, accessibility, interruption and audio acceptance before release.

## Follow-up polish

P3: native type scaling, localization and platform-specific settings wording should be reviewed during implementation. They do not block this local visual proposal.

## Onboarding refinement — 3 October 2026

Result: passed. The prototype now contains seventeen views.

Source visual truth: `captures/welcome-before-refinement.png`, captured in this follow-up at 393 × 852 pixels. Native onboarding findings were checked against the current CoachMarks, MetronomeViewModel, AppNavigation and AnimatedSplash source; no fresh native onboarding run is claimed.

Implementation evidence: `captures/welcome-light.png`, `welcome-dark.png`, `welcome-pixel.png`, `welcome-narrow.png`, `onboarding-sound-light.png` and `onboarding-first-beat.png`. Full source/proposal comparison is `captures/comparison-onboarding.png`; flow evidence is `captures/onboarding-storyboard.png`. The two 393 px panels make the heading, mark, copy and controls legible in the full comparison, so a further cropped-region comparison was unnecessary.

Viewport/density: 1400 × 1100 browser, iPhone 393 × 852 at scale 1 and Pixel 427 × 952, with equal-size pixel captures. Source web welcome also uses 393 × 852 pixels; no density normalization was needed. The 360 × 900 responsive capture has page width=360 and phone-root scroll=0. Temporary overrides were reset.

State: fresh light welcome at 80 BPM, 4/4 and Wood. The optional sound capture selects Click; the resulting instrument plays Click at 80 BPM. Native defaults, completion persistence and measured launch/audio latency remain outside this web evidence.

Required surfaces:

- Typography: platform system face retained; a 42 px two-line headline, 96 px interactive tempo and quieter 15/14/12 px supporting tiers create the welcome hierarchy. The instrument still uses its original 62 px BPM. Text remains readable in the full comparison and both themes.
- Spacing: 18 px content padding, safe areas, 48 px targets and fixed primary actions are preserved. Welcome and optional sound choice avoid a three-feature card stack. Larger text must be verified in native implementation.
- Colors: existing semantic light/dark tokens retained; secondary text uses the theme's secondary foreground. No decorative gradient, ornamental shadow or added accent palette.
- Assets: the supplied Metronome mark is used in the toolbar. Beat buttons are functional instrument controls; Lucide supplies the action/preview icons. Protected phone/status/keyboard assets remain intact.
- Copy: Your music. Your tempo. is new welcome copy; Precise timing. Better practice. is the approved tagline. Start the beat names its result, Skip intro opens silently, and sound choice is labelled optional in the walkthrough. Offline/no-ads/no-account wording is grounded in PRODUCT.md. No privacy/telemetry claim was added.

Bounded comparison history:

1. Initial batched inspection covered iPhone light/dark, sound choice, Pixel and compact browser. [P2] Choosing the Click voice produced Click click in the welcome caption; supporting copy also lost its intended top spacing to an earlier selector. Fixed the duplicate suffix and added a scoped spacing rule. [P2] Completing the pushed optional sound route could leave Welcome underneath the instrument; completion now clears earlier flow entries before replacing with the instrument. Another already-selected voice remains available in the compact picker.
2. Re-captured the same states after the fix and opened the latest combined comparison, storyboard and Pixel capture. Confirmed selection survives Back, the caption is clear, spacing is restored, and completed onboarding has one route layer with no Welcome present. No remaining actionable P0/P1/P2 issues; stopped after this confirmation pass.

Interaction evidence: direct Start enters playback, Skip stays silent, preview starts/stops, selected Click is applied to the first beat, Back preserves selection, and completion removes the welcome route. Audio was paused after capture. Zero new console errors/warnings after the clean run. Final TypeScript/Vite build and all 28 protected-runtime integrity checks passed.

Native one-time presentation, stored hint dismissal, contextual tips, reduced-motion launch timing, hardware audio/failure handling and VoiceOver/TalkBack are recommendations and acceptance checks, not implemented web behaviours. Current DESIGN.md was used as authority; its generated sidecar can be refreshed separately.


## Settings brand refinement — 3 October 2026

Result: passed. The user's current request for a modern, catchy, playful Settings experience supersedes the earlier restrained Settings proposal. All changes are scoped to app-owned Settings, its footer excerpt, related review notes and visual artifacts. Native code and the other phone screens were not restyled.

Source truth: `captures/settings-before-playful.png` is a fresh 393 × 852 capture of the previous web Settings. Current proposal: `captures/settings-light.png`, `settings-dark.png`, `settings-appearance-light.png`, `settings-practice-light.png`, `settings-motion-light.png`, `suby-light.png` and `suby-dark.png`. The combined source/proposal comparison is `captures/comparison-settings-playful.png`; `captures/settings-storyboard.png` shows the connected scroll hierarchy, and `captures/settings-theme-comparison.png` compares both themes. The original native-versus-first-proposal Settings comparison is historical evidence, not a claim of fidelity for this revision.

State and density: before/after are both light, Wood, 75% volume, haptics off, Mono, advanced pan and practice/motion groups closed. Browser 1400 × 1100, iPhone 393 × 852 at scale 1, matching pixel captures. Pixel evidence is 427 × 952. Compact browser is 360 × 900; document width remained 360 and phone-root scroll stayed zero. Appearance capture selects Violet; expanded-practice capture shows the controls. Temporary viewport override is reset at handoff.

Required surfaces: system typography remains, with a 34 px introduction, 30 px selected voice, 25 px appearance heading and explicit supporting labels. Spacing stays 18 px outside, 16 px in the sound panel, with 48 px or larger targets. Melrose #B89FFF, Periwinkle #C9D5FE, Mint Green #9EFFAE and Pink Lace #FFCAEA are existing brand tokens, confirmed in current DESIGN.md. High-contrast ink sits on the violet panel in both themes; selected swatches have a checkmark, border, name and pressed state. Semantic theme surfaces keep their existing roles. Lucide icons and real app icons remain; no custom font, decorative illustration, gradient or new logo was introduced. Friendlier section copy keeps the ordinary control names intact. Suby is still static and dismissible, after data/help, with verified product copy and an external website link.

Bounded QA: one batched inspection covered light/dark, sound choice and audition, theme/colour selection, advanced pan, practice/motion groups, footer, Pixel and compact viewport. The disclosure controls originally referenced targets not mounted while closed; targets now stay mounted and hidden, and the confirmation pass verified all references resolve. Inspected the final combined comparison and storyboard. No unresolved actionable P0/P1/P2 issues remain in this web scope.

Interactions: selected Soft appears in the Settings sound control; one-bar preview starts and stops separately. Haptics and Count-in reflect their checked state; volume changes 75 to 74 with a keyboard arrow; pan expands; Pendulum reports pressed. Colour and appearance selection update the model. Live Activity control is absent on Pixel. Suby points to https://subyapp.com/ and dismissal removes its link. Current Settings controls have no horizontal overflow or target below 48 CSS pixels at scale 1. Zero new console errors/warnings after the clean reload.

Native effects and persistence remain outside the prototype. Count-in, pan, haptics, display effects, reduced motion, OS integration and device timing still need native implementation/acceptance. The four new sounds remain audition sketches. System is represented as light in the web preview. Final TypeScript/Vite build passed, including all 28 protected-runtime integrity checks. A non-blocking Vite bundle-size advisory remains for this review prototype. Preview tabs and server are retained; there was no native build or deployment.


## Hands-on onboarding revision — 3 October 2026

Result: passed. The user's rejection of the earlier welcome as text-heavy, passive and unsmooth is the current direction. Accepted Settings remains unchanged. This revision replaces the first onboarding proposal with a working first-play surface.

Source truth: captures/onboarding-before-interactive.png, freshly captured before edits. Both before/after are iPhone 393 × 852, light, Mono, 80 BPM, Wood, 4/4, first accent and stopped, with the same template chrome. Latest combined comparison: captures/comparison-onboarding.png. Interaction storyboard: captures/onboarding-storyboard.png. Additional evidence: welcome-light/dark/pixel/narrow, onboarding-playing-light, onboarding-first-beat, onboarding-reduced-motion, onboarding-extra-sound and onboarding-sound-light. Earlier onboarding screenshots/comparison descriptions are historical.

Required surfaces: system type retained; 36 px single-line heading, 86 px directional BPM and short supporting copy. The reduced copy is supported by real Play/Pause, tempo slider and steppers, beat controls and voice choices. Play is 85 px, beats are 40 px in 48 px targets, outer padding 18 px. Violet uses the existing brand token; other semantic roles preserve both themes. The supplied mark and Lucide icons remain. No custom font, decorative graphic or idle loop was introduced.

Normalization: 1400 × 1100 browser, iPhone 393 × 852 and Pixel 427 × 952 at scale 1, with matching pixel captures. Narrow capture is 360 × 900; document width=360 and root scroll=0. Default iPhone controls end at y=812.9, before the fixed footer at y=826. Content fits without scrolling (content and visible area both 738 px). No horizontal control overflow or target smaller than 48 px at scale 1.

Motion: app-owned web presets mirror current AppAnimations: Press 1600/1, Quick 1100/1, Standard 700/1, Emphasized 340/0.94 and Expressive 520/0.66 (stiffness/damping ratio). Damping is converted with mass 1. Press and border-radius animations have separate presets. Direction remains stable during number exits. The protected template keeps its own route spring; native timing/smoothness is not claimed. Reduced motion removes spatial movement and pulse scaling while retaining state/opacity feedback. The review override reported reduced=true, with all four beat transforms none while playing.

Bounded inspection: the built view was checked across light/dark, iPhone/Pixel and narrow browser, then through actual interactions. A browser connection interruption was recovered with a fresh tab in the same in-app browser; it was not treated as an app defect. No actionable P0/P1/P2 layout issue emerged. Final combined before/after and interaction storyboard were inspected after updating captures. No further speculative polish was performed.

Interaction evidence: a rapid 40→43 tempo edit stayed in playback; the observed phase moved from beat 3 to 0 over the interaction rather than restarting on each change. Exact entry 90 and +1 produced 91. Click selected and beat 2 changed to accent. Continue reached the main instrument at 91/Click, still playing, with elapsed time preserved; one route remained and Welcome was absent. Skip while playing produced a silent main screen. More sounds selected Soft and returned four fitting chips. Pause cancels the preview's scheduled audio and voices. Timer started with a 15:00 readout; tempo/gap trainers showed their active status and paused correctly after the shared demonstration player change. This is UI/source evidence, not hardware listening or scheduler precision.

Zero new console errors/warnings after the clean run. The build and all 28 protected-runtime checks passed. New changes are limited to the prototype's app-owned code, motion translations, capture/review assets and relevant docs. The four sound sketches are unchanged. Native application code, native audio and adopted roadmap are unchanged.

## Reusable motion refinement — 3 October 2026

Result: passed within the web prototype scope. The current request addresses abrupt pan, colour/appearance changes, grouped reveals and related microtransitions. The accepted visual hierarchy remains. The new coverage document is `../../docs/motion-exploration-2026-10-03.md`; the public walkthrough is `/motion.html`.

Visual source: the verified pre-motion Settings capture `captures/settings-light.png` from the accepted revision. `captures/settings-before-motion.png` was also captured in this turn but includes a transient blank toolbar, so it is not the normalized comparison source. `captures/settings-motion-light.png` is the current matching state: 393 × 852 iPhone, light/Mono, 80 BPM, Wood, volume 75, haptics off, disclosures closed. `captures/comparison-settings-motion.png` pairs the verified pre-motion and current states. `settings-motion-comparison.png` compares expanded pan at scroll zero in light/Mono and dark/Violet; the selections are named explicitly. Current Pixel, compact, welcome and settled-continuation captures are retained. Status-bar clock times differ between capture sessions.

Required surfaces: system typography, Lucide icons, the real app marks, 18 px outer spacing, 48 px targets, 85 px Play and 40 px welcome beats are retained. Colour schemes now tint semantic primary control surfaces; the violet sound panel and pastel palette retain their existing branded treatment. No ambient loop, decorative animation or promotional motion was introduced.

Reusable motion: current AppAnimations presets are translated in `src/app-motion.ts`; screen-specific durations/spring values were consolidated. `motion-components.tsx` owns reveal, chevron, scoped selection, state swap, directional number, press, list/tile and toast primitives. `theme-motion.tsx` owns interruptible semantic colour interpolation. Protected navigation/sheet/keyboard/device chrome keeps its own implementation. No protected file changed. Reduced motion is shared, including the review override and OS preference; it removes spatial travel while retaining short opacity/state feedback.

Motion evidence: pan opening was observed at heights 68.53→70.63→72.28→73.26 before resting at 74 px; opacity and chevron matrix changed alongside it. Closing showed 5.87→3.18→2.33, with aria-hidden and inert already true, then reversed into an open state. Practice disclosure showed intermediate heights around 263.60→269.16 before its full 278.09 px. Colour change produced intermediate primary values #4b3c81→#59459c; dark appearance showed surface #474747→#343434 before resting at its dark target. Rapid scheme clicks ended on Pink with one ring and one appearance fill. Switch feedback included an intermediate 17.59 px thumb translation and the checked state. Reduced pan was immediate at 74/0, with spatial movement removed. These are observed frame states, not frame-rate or physical-device performance measurements.

Other interaction evidence: preset options expanded, duplicate added a fourth row, removal returned three with an inert shrinking exit row. Routine reorder collapsed goal controls, moved Build the pulse before Easy start and restored the existing two-minute goals. Invalid tempo 30 disabled Set tempo and revealed its message; valid 92 applied. Suby dismissal collapsed through intermediate heights and removed its accessible link immediately. Welcome Play/edit/continue retained its setup; after settling there was one flow, no Welcome, 81 BPM and active playback. Playback was paused after checks. Earlier transient two-flow states were observed during the protected route exit and were not treated as settled navigation.

Bounded inspection and fixes: the first batched inspection covered Settings disclosures, palettes, both appearances, reduced motion, presets/editor, validation, welcome, Suby, iPhone/Pixel and a 360 px browser. It caught [P2] the shared preview icon being clipped by an old broad span selector, and [P2] a closed group's retained children keeping an iPhone-only switch after changing to Pixel. Fixed the selector and kept closed content current while retaining null content for its exit. The confirmation pass verified the visible preview icon, all disclosure references, three practice switches on Pixel and four on iPhone, and settled welcome completion. The normalized comparisons were inspected and captures corrected for scroll position. No remaining actionable P0/P1/P2 issue was found in this scope; stopped after confirmation.

Final geometry: 393 × 852 iPhone and 427 × 952 Pixel. No active phone control below 48 px was found in the Settings check; palette overflow was false. At a 360 px browser the document stayed 360 px wide, the palette did not overflow and phone-root scroll stayed zero. Zero new console errors/warnings in the final clean confirmation run. Historical hot-reload errors during source replacement were excluded from the clean run. Final TypeScript/Vite and all 28 protected-runtime checks passed; the existing bundle-size advisory remains non-blocking. Native code/audio, native theme effects, hardware motion/accessibility and production persistence are unchanged and unverified by this proposal.


## Distinct welcome, logo, permanent studio and custom sliders — 3 October 2026

Result: passed within the interactive web scope. This is the current revision. The user explicitly requested a permanent, more appealing studio section, a main toolbar logo, onboarding distinct from the instrument, and a custom slider with shared motion. Previous hands-on welcome and dismissible studio screenshots/QA above are historical.

Source truth: fresh pre-edit `welcome-before-distinct.png`, `main-before-brand-slider.png` and `studio-before-permanent.png`. Current comparisons: `comparison-welcome-distinct.png`, `comparison-main-brand-slider.png` and `comparison-studio-permanent.png`; combined proof: `brand-refinement-storyboard.png`. Before/after instrument and welcome are iPhone 393 × 852, light/Mono, 80 BPM, Wood, 4/4 and stopped. Studio comparisons are the same light/Mono iPhone footer excerpt at scroll zero. Status clock times differ. Public gallery and walkthroughs now reference current captures.

Required surfaces: the real mark sits beside Metronome in the toolbar to preserve immediate recognition. Welcome uses a 38 px greeting, a 196 px listening mark, three 74 px pace targets and one 58 px Start action. It contains no BPM readout, slider, beat editing or voice picker. Padding stays 18 px and typography stays system. Violet and Mint are existing brand tokens. Suby's real 54 px icon, useful feature labels and 48 px external action sit in a static Periwinkle section after Help; there is no close/dismiss control. Semantic surfaces preserve both appearances. No new logo, font, gradient, invented product claim or idle animation was added.

Reusable slider: `BrandedSlider.tsx` owns all five sliders. Each retains a labelled 56 px native range target beneath custom track/fill, grip, centred pan marker and focus/drag value hint. Pointer movement is direct. Standard drives discrete position changes, Press the grip and Quick the hint; scale/distances live with shared motion tokens. Reduced motion removes travel/scale, retaining immediate position and opacity feedback. A continuous-update option on shared number/state swaps keeps dragged tempo text direct.

Bounded QA: one batched check covered light/dark instrument and Settings, welcome light/dark/iPhone/Pixel, compact browser, permanent studio, preview/Stop/automatic completion, Start/Skip, keyboard limits, pointer drag, centred pan, focus and reduced motion. It found one actionable P2: rapidly dragged tempo changes stacked outgoing digits. The reusable continuous-update path fixed it. Confirmation immediately after a drag showed value 220, one readout frame, thumb 100% and dragging=false. Discrete 80→85 showed thumb 24.9552% before resting at 25%. No further speculative polish followed.

Interaction evidence: tempo Home=40, End=220 and ArrowLeft=219; a 35% drag produced 103 BPM and thumb 35%. Volume Home/End produced 0/100%; pan produced Left 100% and Right 100%; timer produced 1/60 min. Focus outline was 2 px with the thumb visible. Reduced thumb reached 100% immediately with transform none. Easy and Brisk auditions changed the selected pace and pulse; preview completed automatically. Start reached a settled instrument at 120 BPM with active playback; Skip during preview reached stopped playback. Suby has zero close buttons and links to https://subyapp.com/.

Geometry: welcome content and visible area are both 738 px on iPhone and 780 px on Pixel, with no content scrolling. At a 360 × 900 browser, document width stayed 360 and phone-root scroll zero; the app content's client and scroll widths were both 393. The protected template's invisible synthetic cursor retained an out-of-bounds box after resizing; it was excluded from app-content overflow. No app control extended the horizontal bounds. Temporary viewport override is reset at handoff. Final comparison/storyboard and both appearances were visually inspected.

Zero new browser console errors/warnings in the clean confirmation run. TypeScript/Vite and all 28 protected-runtime integrity checks passed; the existing bundle-size advisory is non-blocking. Scope is app-owned prototype code, current review docs and captures. Native app code/audio, stored onboarding/preferences, native detents/haptics, hardware accessibility and device timing/performance are unchanged and unverified. Preview tabs/server are retained. No native build, commit, publication or deployment was performed.

Capture normalization note: final Settings captures were recaptured after returning the template virtual scroll to the top. Its content transform was matrix(1, 0, 0, 1, 0, 0), native scrollTop zero and introduction y=256 in the 1400 × 1100 browser. Earlier same-session scrolled Settings captures were replaced; current closed/pan comparisons use the normalized files. All media references in the four current gallery/walkthrough pages resolve.


## Tempo animation restoration — 3 October 2026

Result: passed in the web prototype. The user requested tempo animation as before for every value change. This supersedes the direct-drag readout decision in the previous section. Shared `AnimatedNumber` now derives two persistent rolling text layers from one retargetable Standard spring and the existing 24 px number-distance token. There is no continuous-input bypass or accumulating exit queue. Stable character width preserves two/three-digit legibility; exact requested values stay available to accessibility.

Observed up motion: 80→81, incoming y 0.8494→0.5684 and opacity 0.9646→0.9763. Observed down motion: 81→80, incoming y -0.4967→-0.4144 and opacity 0.9793→0.9827. Large End change had target 220 while the rolling layers progressed through 85/86, 93/94 and 105/106. Pointer dragging ended with requested/input 216 and in-flight 175/176. Rapid Home/End/ArrowLeft settled at 219 with two layers, one fully visible and the adjacent layer opacity zero. All these layers remained the same mounted pair.

Exact entry applied 100, then ×2/÷2 and +5/−5 returned 100. The retained 3ch width prevented horizontal text clipping. Reduced preview set 40 immediately, opacity 1/0 and transforms none. Dark mode used rgb(248,248,248) and had no frame overflow. The shared timer reached 60 with no content overflow; native browser accessibility showed one current 60-minute value. The final capture `captures/tempo-animation-restored.png` is a stopped, light/Mono, 80 BPM instrument at the default browser/device scale. It is a settled handoff image; intermediate frame observations establish the motion.

No application errors occurred. The existing MotionConfig reduced-motion preview emitted its expected library notice; it is not a new application failure. The build passed TypeScript, Vite and all 28 protected-runtime integrity checks, with the existing non-blocking bundle-size advisory. No native app, native audio, runtime-protected file, dependency, commit or deployment changed. Preview tabs/server were retained.


## Changed digits and Settings transition refinement — 3 October 2026

Result: implemented and checked in the local interactive web prototype. This supersedes the whole-number interpolation in the preceding section. User feedback requires only changed digits to move and a complete Settings motion review. Existing onboarding, branded hierarchy and permanent studio content were preserved.

Baseline observations: 80→81 moved the entire number; three rapid palette choices produced four outgoing/current label frames; the haptics track changed immediately while its fill opacity was 0.5805 and thumb x=8.3228; a Dark update showed surface #c9c9c9 while the Mono swatch had already switched to rgb(248,248,248). These were concrete coordination defects.

Shared fixes: bounded two-slot text/icon/digit swaps, stable decimal-place keys, Standard entry/Quick exit, preserved visible reversals and correct entry direction when reusing a fully hidden slot. Unchanged digits do not restart. Strict Mode leading-place entry was corrected. The visible value sizes and centers the readout; retained leading presentation places sit outside that sizing width and do not shift two-digit values. Accessible exact values appear once, with inert hidden presentation layers.

Settings review matrix:

| Interaction | Evidence and outcome |
| --- | --- |
| Haptics switch | Track stayed its off colour; fill opacity 0.533361 and thumb x=9.60051 moved in the same Standard spring |
| Stereo-pan reveal/reversal | Intermediate height 77.0078/81, opacity 0.948812, inner y=-0.61426; reversing gave height 4.2266, opacity 0.052631, inert=true |
| Pan focus and hints | Home/End exposed Left/Right 100%; focus returned to More audio controls on collapse; hidden panel aria-hidden/inert immediately |
| Volume | Home/End reached 0/100%; hint and native range stayed current; input remains 56 px |
| Rapid colours | Violet→Pink→Mint retained two label slots (Pink/Mint), one ring and intermediate primary #884c65; no exit queue |
| Appearance | Light reveal observed at inset(0px 61.9019% 0px 0px); Dark→Light reversal settled Light with no residual transition; semantic swatch/foreground moved with the theme |
| Sound preview | Preview→Stop→Preview and picker open/close exercised; Click→Studio→Wood left the current voice Wood and exactly two layers per voice/name swap |
| Keep your flow | All four iPhone switches exercised; closed controls left the current accessibility group; Pixel correctly exposed three practice switches |
| See the beat | Pendulum→Dots returned one fill and selected Dots; flash and motion preference switches exercised; settled height matched content at 256.6953 |
| Other Settings navigation | What's New/Got it, Practice data/Back, Help/Back exercised using the existing protected sheet and navigation runtime |
| Suby | Permanent studio content unchanged; static external action retained |
| Reduced motion | Tempo 40 applied immediately with all digit transforms none; pan height 81 with inner identity transform; Dark applied immediately with no appearance reveal |

Final numeric confirmation: 80→81 left 8 at opacity 1 and identity transform while the incoming 1 had opacity 0.575107 and y=10.1974. After 180→80→81, number and readout centers both measured x=700.00390625. Carry 99→100 moved all changed places, including a newly mounted leading 1 (opacity 0.560105, y=10.5575). A reused hidden ones slot initially entered from its previous exit side; this P2 was corrected and confirmation showed all incoming 100 digits at positive y=4.3597. Borrow and mid-motion reversal were exercised. Rapid Home/End/ArrowLeft produced requested 219 and exactly two layers per place. Reduced motion produced one visible requested character per place, without movement.

Geometry and captures: iPhone 393×852 and Pixel 427×952; Pixel palette client/scroll widths both 391 and phone-root scroll zero. Both native slider targets are 56 px. Current proof is `tempo-changed-digits.png` and `settings-refined-light.png`/`settings-refined-dark.png`, with the combined `settings-refined-comparison.png`. Settings captures have one settled current flow, pan height 81/opacity 1, phone-root scroll zero and virtual content transform identity. Mono/Wood, 75% volume and centred pan are matched; appearance differs.

Tool limitation: optional 360 px viewport retesting and viewport reset timed out in browser-control tooling. No compact-window success is claimed for this turn. The successful device checks and captures used the 1400×1100 review viewport. The existing user Settings tab remains available; attempts to mark/resize/clean the stalled older preview did not complete. Browser transport failure is separate from application verification. No browser-console clean-run claim is made for this revision.

Final TypeScript/Vite and the 28 protected-runtime integrity checks passed. Existing bundle-size advisory remains non-blocking. Native source/audio, stored preferences, physical-device accessibility, frame timing and native spring acceptance were not changed or verified. No dependency, native build, commit, publication or deployment was added.


## Web-app discovery in Settings — 3 October 2026

Result: added to the local web mockup and the matching Settings-footer excerpt. A compact “Your beat. Any screen.” semantic-surface card follows app information and precedes the permanent studio section. It uses the existing mint brand token, Lucide Monitor, one direct external link and reusable PressableLink feedback. The shared Press spring and subtle scale apply to the link; reduced motion disables spatial feedback. The new-tab destination is announced to accessibility without extra visible copy. The About website row now names the landing page truthfully.

Destination verification: the live landing page links its web-app actions to `app/`. Direct `https://metronome.merkost.dev/app/` returned HTTP 200, the title “Metronome — web app | Merkost” and the `webAudio.js`/`composeApp.js` bootstrap. The older copy document names the domain root; the mockup deliberately uses the discovered direct app path. No data-sync or browser/native parity claim was added. Browser activation was exercised; no external app rendering, audio, timing or persistence acceptance is claimed.

Visual and interaction checks: light and dark iPhone, plus light Pixel 10. The iPhone card measures 357 × 228.5 px, with equal client/scroll widths of 355 px. Pixel measures 391 × 228.5 px, client/scroll widths both 389 px. The external action is 60 px tall on both devices. Phone-root native scroll remains zero. Light card foreground/background are rgb(22,22,22)/rgb(242,242,242); dark uses rgb(248,248,248)/rgb(36,36,36). Keyboard navigation reaches the web link before Suby with a visible 2 px focus outline. The reduced-motion preview leaves its transform as none. The current user Settings tab is retained; no temporary viewport change was made in this turn.

Captures: `settings-web-light.png`, `settings-web-dark.png`, and `settings-web-comparison.png`, copied to the public gallery. The Settings walkthrough and hierarchy notes include the new section. Final TypeScript/Vite build and all 28 protected-runtime integrity checks passed; the existing bundle-size advisory remains non-blocking. Native code/audio and protected runtime files were unchanged. No dependency, native build, commit, publication or deployment was added.
