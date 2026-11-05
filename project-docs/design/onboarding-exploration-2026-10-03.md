# Metronome — Hello, rhythm

3 October 2026 · Current welcome proposal

[Try the welcome](http://127.0.0.1:8767/?screen=welcome) · [Visual walkthrough](http://127.0.0.1:8767/onboarding.html)

The latest feedback replaces the hands-on instrument onboarding. Welcome now has its own identity: a short greeting, the real Metronome mark, one finite listening interaction and three starting paces. The detailed instrument belongs on the main screen.

## Interaction

“Hello, rhythm.” opens with one short line. Tap the violet brand mark to hear one bar at the chosen pace. Easy is 60 BPM, Steady 80 BPM and Brisk 120 BPM. Choosing a pace auditions it and moves the selected mint surface between the options. The mark moves only during the user-started preview; there is no idle loop or autoplay on arrival.

“Start practicing” stops preview, enters the instrument at the selected tempo and starts practice playback. “Skip intro” stops preview and opens the instrument silently. Audition is independent of practice tracking and does not start a trainer or timer. Beat editing, exact BPM, the custom tempo slider and sound choice are introduced on their own surfaces.

The optional sound-review view remains in the seventeen-screen gallery for comparison. It is not a required onboarding step.

## Motion and access

The shared AppAnimations translation supplies Press, Expressive and Emphasized springs. The mark responds to preview pulses, its icon swaps between Play/Pause and pace selection uses a scoped moving fill. Reduced motion removes rotation, pulse scale and selection travel while preserving selection and short opacity feedback. All welcome targets are at least 48 CSS pixels; the three pace choices are 74 px high. System type and the existing violet/mint brand colours remain.

The main toolbar keeps the name beside the real mark. The name preserves immediate recognition while the mark strengthens the product identity.

## Evidence and limits

Fresh before/after: `captures/comparison-welcome-distinct.png`. Current combined proposal: `captures/brand-refinement-storyboard.png`. Light/dark iPhone, Pixel and compact-browser captures use the `welcome-distinct-*` filenames. Previous onboarding captures and earlier QA sections are historical.

Preview, Stop, pace changes, automatic preview completion, Start and Skip were exercised. A Brisk welcome completed into active playback at 120 BPM; Skip during preview produced a stopped instrument. Reduced-motion preview reported transform none. The 393 × 852 iPhone and 427 × 952 Pixel fit without content scrolling. A 360 px browser kept document width 360 and phone-root scroll zero, with content width 393 and no horizontal content overflow.

The custom slider now lives in one reusable component for tempo, timer, sound-library volume, Settings volume and stereo pan. Its grip, value hint and selection updates use shared tokens. Direct drag tracks the pointer immediately; button/keyboard changes settle with the Standard spring. Tempo values animate for both continuous and discrete changes. One retargetable numeric spring drives two permanent presentation layers, preserving directional rolling without stacking outgoing numbers.

These are web mockups with temporary state and browser demonstration audio. Native one-time presentation, stored hints, adaptive text, VoiceOver/TalkBack, interruptions, audio recovery and physical-device timing/motion still need implementation and acceptance. Native code and audio assets are unchanged.
