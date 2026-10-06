# Metronome — interactive design exploration

Local web mockups and recommendations, 3 October 2026.

- [Interactive mockups](http://127.0.0.1:8767/)
- [Screen gallery](http://127.0.0.1:8767/gallery.html)
- [Suggestions and sound auditions](http://127.0.0.1:8767/suggestions.html)
- [Source-grounded review](http://127.0.0.1:8767/review/index.html)
- [Playful Settings refresh](http://127.0.0.1:8767/settings.html)
- [Revised onboarding](http://127.0.0.1:8767/onboarding.html)
- [Motion coverage](http://127.0.0.1:8767/motion.html)
- [Visual QA](design-qa.md)

Seventeen views cover the instrument, Practice, rhythm, presets, sets, editor, active routine, timer, both trainers, sounds, Settings, practice data, help, a Settings-footer excerpt, the distinct Hello, rhythm welcome and a standalone optional sound review. Use the outside screen rail, or the Explore selector in a narrow browser. iPhone and Pixel 10 previews share the proposed interface; Live Activity controls appear only on iPhone.

Settings was refined following the user’s request for a modern, catchy and playful brand voice. Sound and appearance stay immediately editable; practice/playback and beat/motion unfold in place. Suby now has a permanent soft-blue studio section, with the real icon, useful features and one external link. The walkthrough includes before/after and both-theme comparisons. A compact “Your beat. Any screen.” card now makes the web app discoverable before the studio section, with a direct link to https://metronome.merkost.dev/app/. Its external action uses the shared press spring and respects reduced motion.

Onboarding now has its own interaction: tap the mark to hear one bar, choose Easy/Steady/Brisk and start practice at that pace, or Skip silently. It no longer duplicates the instrument's tempo, beats and sound controls. The main title keeps the mark beside Metronome. The browser player applies live settings at pulse boundaries; native timing remains outside this preview.

The full motion pass uses `src/app-motion.ts`, reusable primitives in `src/motion-components.tsx`, and interruptible semantic palette changes in `src/theme-motion.tsx`. `src/BrandedSlider.tsx` owns all five sliders, including centred pan, direct dragging, spring selection updates, focus/drag hints and reduced motion. Tempo values now roll only the digits that change, including during dragging. Each place keeps two bounded presentation layers; unchanged digits remain still. Text/icon swaps use the same interruption boundary. Switch feedback and disclosure motion are synchronized. Light/Dark uses a readable phone-scoped reveal sampled from the Standard spring; reduced motion and unsupported browsers apply the theme immediately. Pan, grouped settings, segmented choices, switches, lists, routine goals, status, validation and feedback share these boundaries. See [coverage and native adoption notes](../../docs/motion-exploration-2026-10-03.md).

The mockup is interactive and uses temporary in-memory data. Refresh resets it. Practice totals are examples. Four original synthesized sound sketches include normal and accent voices; see `sound-sketches.json`. Existing audio samples and both app icons were copied from the local projects. New audio was not added to the native application.

Native integration, production scheduling, storage/recovery, hardware accessibility and several secondary setting effects are outside this prototype. The detailed suggestions identify these limits and distinguish existing features from proposals.

## Run locally

The preview is running on port 8767. From this directory, after installing dependencies:

```sh
npm run dev -- --host 127.0.0.1 --port 8767 --strictPort
```

On this Mac, the verified runtime is Homebrew Node at `/opt/homebrew/bin/node`. If the default Node rejects the native build binding, use:

```sh
/opt/homebrew/bin/node node_modules/vite/bin/vite.js --host 127.0.0.1 --port 8767 --strictPort
PATH=/opt/homebrew/bin:$PATH npm run build
```

The final build passed TypeScript, Vite and the mobile-runtime integrity check for 28 protected files. Native app builds and device acceptance remain outside this web prototype.

The permanent Suby recommendation is titled **More by Merkost** across the current Settings mockups, reusable Pen components and website. This replaces the earlier studio heading.

Beat indicators use unnumbered dots throughout the current instrument and rhythm designs. The accent ring, solid normal dot and outlined mute icon distinguish states. Accessible beat controls retain their index and current state.

This interactive redesign is configured for private web hosting. Hosting identity is retained in `.openai/hosting.json`; deployment receipts are kept with the Metronome design artifacts.
