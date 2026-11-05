# Metronome — motion coverage and reusable web primitives

3 October 2026 · Current interactive proposal

[Try Settings](http://127.0.0.1:8767/?screen=settings) · [Motion walkthrough](http://127.0.0.1:8767/motion.html)

The accepted playful Settings, distinct Hello rhythm welcome and custom sliders share one motion vocabulary. Disclosures make space in place, selections follow the user's choice, and appearance changes morph the semantic palette. Motion supports the action; there is no decorative idle loop or animated Suby promotion.

## Token authority

Current `shared/src/commonMain/kotlin/com/merkost/metronome/ui/AppAnimations.kt` is the authority. `src/app-motion.ts` translates its stiffness and damping ratios with mass 1 and damping = 2 × ratio × √stiffness.

| Preset | Stiffness / damping ratio | Web use |
| --- | --- | --- |
| Press | 1600 / 1 | Press and release feedback |
| Quick | 1100 / 1 | Exits and switch track feedback |
| Standard | 700 / 1 | Numbers, state swaps, switch thumb |
| Emphasized | 340 / .94 | Disclosure height, selection movement, list reflow |
| Calm | 190 / 1 | Semantic colour and appearance interpolation |
| Expressive | 520 / .66 | Play shape and active beat pulse |
| Navigation | 400 / 1 | Available for native parity; protected web navigation retains its own spring |

Press scales, number displacement, entry/exit scale, feedback opacity timing and Play shape live beside these presets. Reduced feedback uses a short 120 ms opacity change; height, selection travel, rotation and number travel become immediate. No spring values are scattered through screen implementations. The previous onboarding-only token file was replaced by this shared module.

## Reusable boundary

`src/motion-components.tsx` owns the primitives:

- `AppMotionProvider` combines the OS preference with the outside-phone review override and passes the result to MotionConfig. OS reduced motion is respected across app-owned content and the protected motion runtime. The existing preference switch remains a demonstrated native setting.
- `AnimatedReveal` combines emphasized expansion/shrink with standard entry and quick exit opacity. Targets stay mounted so aria-controls always resolves and form values survive. Closing content becomes inert and aria-hidden immediately; focus returns to its trigger when needed. Content remains available for its exit, and closed groups still receive current values and platform changes.
- `DisclosureChevron` uses the same disclosure state and spring.
- `AnimatedChoices` scopes each shared selection with LayoutGroup/useId, preventing unrelated controls from sharing a moving pill.
- `AnimatedSwap` and `AnimatedNumber` handle text/icons and directional numeric changes. Exiting content is inert and hidden from accessibility. Text/icons and digits use two bounded presentation slots; rapid retargeting preserves the dominant layer and reversals reuse existing content. Unchanged digits do not restart.
- `AnimatedItem` and `AnimatedTile` cover list additions/removals, reordering and preview tiles. Stable step identities preserve values when reordering.
- `Pressable` and `AnimatedToast` supply shared press and feedback motion.

`src/theme-motion.tsx` owns semantic palette targets and one interruptible Calm spring. Each frame updates surface, container, foreground, primary, primary foreground, outline and accent variables together. A new selection stops the old transition and starts from the currently rendered values. Selected schemes now tint actual control surfaces as well as accent feedback. The existing fixed violet sound panel and named pastel swatches retain their brand identity. The simulated status icons follow the same appearance progress. System remains represented as light in this review prototype.

## Coverage audit

| Surface / state | Treatment |
| --- | --- |
| Stereo pan | Reveal/shrink, fade, rotating chevron; custom centred slider |
| All five slider instances | Shared track, grip, direct drag, Standard spring selection, Press grip feedback and Quick hint |
| Practice/playback and beat/motion groups | Same shared disclosure; platform controls remain current |
| Colour scheme | Moving selection ring, check/label swap, semantic palette interpolation |
| System/Light/Dark and other segmented choices | Scoped moving fill, press feedback, palette interpolation |
| Switches | Track fade and spring thumb |
| Click selection and preview | Check/voice/description/icon swaps and selected-surface interpolation |
| Suby studio section | Permanent static section; no dismissal or ambient promotional animation |
| Preset options | Accessible reveal target and collapse |
| Preset create/delete/favourite | List entry/exit/reflow and favourite icon swap |
| Practice-set empty/saved states | Reveals and list motion |
| Routine edit/reorder/goal amount | Mode swaps, nested reveals, stable list reordering and numeric changes |
| Active routine and upcoming steps | Label/number swaps, list removal and final-step reveal |
| Active timer cancel control | Reveal/shrink |
| Tempo trainer | Shared directional numeric controls |
| Gap preview tiles | Entry/exit and position motion |
| Instrument training/routine strip | Reveal with retained exit content |
| Instrument Play/Pause and welcome audition | Expressive instrument shape, preview mark pulses, icon swaps and press spring |
| Instrument tempo | Only changed digits roll; two bounded layers per decimal place with Standard entry and Quick exit |
| Welcome hint and paces | Label swap and scoped moving selection; reduced-motion path |
| Exact-tempo validation | Accessible reveal/shrink; invalid action remains disabled |
| Toasts | Fade/scale/rise entry and short exit |
| Route, sheet, keyboard and device-menu chrome | Existing protected runtime transitions; no duplicate animation layer |

Static route-specific labels and explanatory text appear within the animated route. Range dragging and the practice clock keep continuous/direct feedback. No artificial delay was added to playback or actions.

## Verification and implementation limits

The latest slider/brand revision is recorded in the appended section below. The following paragraph describes the earlier motion pass; its Suby dismissal and instrument-based welcome are superseded.

Browser evidence includes intermediate disclosure heights, opacity and chevron transforms; reversed pan expansion; intermediate primary/surface colours; rapid scheme changes; switch thumb position; preset and routine list exits; unchanged reordered goals; invalid then valid exact entry; quiet Suby dismissal; and continued first-use playback. A settled welcome continuation had one flow, no welcome, and the selected tempo. All audio was paused after testing.

The first batched review caught a clipped preview icon and stale iPhone-only content cached inside a closed disclosure after changing to Pixel. Both were fixed. The confirmation pass verified three practice switches on Pixel, four on iPhone, valid disclosure references, and no new console errors/warnings. Light/dark, 393 × 852 iPhone, 427 × 952 Pixel and a 360 px browser were checked. The compact page remained 360 px wide with no palette overflow; the phone root stayed at scroll zero. Captures show visual states; intermediate DOM frames provide motion evidence.

Build verification covers TypeScript, Vite and the integrity check for all 28 protected runtime files. The existing bundle-size advisory is non-blocking. This is a local web prototype, with temporary state. Native UI, scheduling, native palette animation, pan/haptics/background effects, stored onboarding and physical-device accessibility/performance have not been implemented or accepted by this revision.

For native adoption, reuse `AppAnimations.expandEnter/shrinkExit`, scoped selection and digit presets through common composable helpers; animate semantic colour roles at the theme boundary; and test interruption, nested expansion, reduced motion, form retention and focus on both platforms. Preserve interfaces/Koin and the existing audio path. Native adoption is a separate implementation step.


## Distinct welcome, logo and custom-slider refinement

The permanent Suby section replaces dismissal. Welcome no longer duplicates the instrument. Its finite one-bar preview runs at the chosen Easy/Steady/Brisk tempo; a user-started pulse drives the mark and a scoped fill follows the pace choice. The main toolbar uses the real mark beside the product name. Native source is unchanged.

`BrandedSlider.tsx` owns all five range instances. Accessible native inputs remain underneath an app-owned track, centred fill where applicable, grip and focus/drag value hint. Standard controls the selected position, Press the grip scale and Quick the hint. Direct pointer drag and reduced-motion changes jump to the current position. Geometry distances and scale sit with shared tokens. The latest follow-up restores rolling animation during dragging too. One retargetable numeric spring drives exactly two presentation layers, with shared Standard and number-distance tokens; outgoing number queues are eliminated.

Verification exercised tempo Home/End and Arrow, native pointer dragging, volume 0/100, pan Left 100%/Right 100%, timer 1/60, focus outline, reduced motion, welcome preview/Stop/automatic completion/Start/Skip, and the permanent external link. Drag confirmation produced value 220, one readout frame, thumb 100% and dragging=false. A discrete 80→85 update was observed at thumb 24.9552% before resting at 25%. Reduced-motion thumb was immediate at 100%, with transform none. Inputs are 56 px tall.

Current captures include light/dark instrument and Settings, welcome light/dark/Pixel/narrow, and studio light/dark/narrow. At 360 px browser width, document width stayed 360, phone-root scroll stayed zero and app content width was 393 with scroll width 393. The template's invisible synthetic cursor can extend the outer scroll-width measurement after resizing; no app content extends the viewport. The rapid-digit stack was the actionable P2 discovered in the batched pass and was fixed through a reusable continuous-update path. Confirmation had zero new console errors/warnings.

The current walkthroughs and gallery use fresh captures. Historical comparison files remain separate. TypeScript/Vite and all 28 protected-runtime checks passed. Native performance, native slider detents/haptics, one-time onboarding and stored preferences still need app implementation and physical-device acceptance.


## Tempo value animation restored

The user's latest instruction supersedes the direct-text exception introduced for slider dragging. Every tempo update now rolls, including dragging, range keyboard input, quick tempo actions, exact entry and programmatic changes. `AnimatedNumber` uses one shared Standard spring on the requested numeric value. Two permanent text layers derive their number, 24 px vertical offset and complementary opacity from that spring. Updates retarget its current position and velocity; no new exit nodes are queued. The requested value remains immediate in model, input and accessibility text.

The wrapper keeps enough character width for the widest encountered value, avoiding clipping when switching between two and three digits. Transform and opacity are the only frame-driven styles. Reduced motion jumps directly to the requested value, with no translation. Current and adjacent presentation numbers are aria-hidden; the exact requested value remains accessible. Native AppAnimations was reread to confirm the current Standard spring and directional-number vocabulary. This is a web translation, not a native change.

Browser checks observed 80→81 upward and 81→80 downward offsets/opacity. Pointer dragging, rapid Home/End/Arrow reversal, exact 100, ×2/÷2 and ±5 all settled on their requested value with exactly two presentation layers. Reduced motion produced 40 immediately with both transforms none. Dark theme inherited its semantic foreground; the shared timer reached 60 without overflow. The accessibility tree exposed the requested timer value once. One expected Motion-library notice appeared when activating the existing reduced-motion preview; no application errors occurred. TypeScript/Vite and all 28 protected runtime checks passed. Physical-device timing and feel remain outside this web scope.


## Changed digits and Settings transition refinement — 3 October 2026

This supersedes the whole-number interpolation described in the previous follow-up. `80 → 81` now animates only the ones digit. Decimal-place keys keep unchanged digits mounted and still. Carry, borrow, leading-place entry and rapid reversals use the same shared `BoundedSwap` implementation as labels/icons. There are exactly two presentation slots per place or swap; a new third identity replaces the less visible slot, while returning to either existing identity retargets that slot without a fresh entrance. Current requested content is exposed once to accessibility; presentation slots are hidden and inert. Strict Mode mounting and leading-place entry are covered. The current value sizes the readout, keeping two-digit values centered after a three-digit value; retained presentation places do not widen that sizing box.

| Settings interaction | Current behavior |
| --- | --- |
| Audio, practice and motion disclosures | Emphasized height, coordinated opacity and a 12 px inner lift; reverses from current progress; hidden controls become inert immediately |
| Sound voice, colour name and preview icons | Two bounded slots, Standard entry/Quick exit; no accumulated labels; voice-description space stays stable |
| All switches | Standard fill, thumb and foreground feedback; sound-panel track no longer snaps to the on colour |
| Colour scheme | Calm semantic palette interpolation and a scoped Emphasized selection ring; Mono follows the semantic ink rather than an immediate theme dataset override |
| Appearance | Phone-scoped spatial reveal, sampled from the shared Standard spring; avoids the intermediate grey-on-grey state of foreground/background blending |
| Segmented appearance and beat display | Scoped Emphasized fill, coordinated label colour, stable font weight and target geometry |
| Volume and pan | Direct drag; Standard discrete selection; transform-only track fill and grip positioning; Press grip and Quick hint; labelled native input remains 56 px |
| Sound/What's New sheets, Practice data and Help | Existing protected sheet/navigation behavior retained and exercised; no replacement navigation runtime |
| Suby | Permanent static studio section; no close control or decorative loop |

Reduced motion removes number travel, selection travel, disclosure translation and the appearance reveal. Number state, theme and height apply immediately; short opacity feedback remains for text/icon state. Older browsers without View Transitions apply appearance immediately. System still represents Light in this prototype. The reveal uses the browser's [View Transition API](https://developer.mozilla.org/en-US/docs/Web/API/Document/startViewTransition); no runtime or dependency was changed.

For adoption, retain the behavioral rules and app spring vocabulary in shared Compose components. The web snapshot mechanism itself is a browser implementation detail. Native audio timing, theme transitions, stored state, haptics, background behavior and physical-device acceptance remain separate work.


Current proof: [Settings comparison](../artifacts/design-exploration-2026-10-03/captures/settings-refined-comparison.png) and [changed-digit instrument](../artifacts/design-exploration-2026-10-03/captures/tempo-changed-digits.png). See the prototype design-qa.md for each Settings interaction, intermediate-state evidence and the compact-window browser-tool limitation.
