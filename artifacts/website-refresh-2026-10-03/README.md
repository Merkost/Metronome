# Metronome website refresh

Published 3 October 2026 at [metronome.merkost.dev](https://metronome.merkost.dev/).

[Release evidence](https://github.com/Merkost/Metronome/actions/runs/37097667633) · Commit `2325af588f06a0eb8d6856259c237744acae87d6` · Cloudflare Pages project `metronome`.

## Experience

- A shorter, branded homepage with an interactive 4/4 beat preview, 40–220 BPM custom slider and the existing Wood, Click and Classic sounds.
- Independent tempo digits: 41 → 42 animates the units only. Rapid edits remove stale layers.
- Practice tool tabs for Tempo Trainer, Gap Trainer and practice sets, with keyboard arrow/Home/End support.
- Interactive five-colour preview and light/dark appearance. A shared spring adapter maps directly to the app's physical motion presets.
- Clear browser and mobile choices, with accurate persistence, offline and background-play differences.
- Reusable FAQ expansion that reverses from the current visible height.
- A permanent, quiet Suby studio card with the original icon and verified website destination.
- Matching Support, Privacy and branded 404 pages. The policy body and update date are preserved.

## Editable design

[Metronome.pen](../../Metronome.pen) now contains 74 top-level frames: 49 app screen/state frames, 10 responsive website pages and 15 documentation/overview boards. There are 59 reusable components, including 13 for the website, and 89 variables.

Website boards are numbered 60–73. They cover the desktop/mobile homepage in light and dark, Support, Privacy, 404, reusable components, interaction states and responsive foundations.

[Complete 74-page PDF](../metronome-pen-2026-10-03/exports/export.pdf) · [Website overview](pen-exports/w4IXz.png) · [Canvas audit](../metronome-pen-2026-10-03/design-audit.json).

The design audit found zero root overlaps, broken references, unfinished placeholders, unnamed nodes, missing text fills or unintended clipping. The existing Settings overview crop is intentional. All 12 referenced image assets exist. The saved Pen file is 1,307,674 bytes. The PDF was parsed and verified as 74 pages.

## Verification

Source checks passed for JavaScript syntax, formatting, one main heading per page, unique IDs, local destinations/assets, current store ID and unchanged privacy content. See [source-checks.json](source-checks.json).

Browser checks covered widths 320, 375, 393, 721, 768, 1001, 1024 and 1440 px, with zero horizontal overflow. Visible controls have 48 px minimum targets, and the tempo slider has a 56 px hit area. Slider endpoints and increments, audio start/stop and sound selection, keyboard tabs, appearance, colour selection and rapid FAQ reversal were exercised. Settled 41 → 42 preserved one layer in the hundreds/tens slots and temporarily used two layers only in the units slot. Reduced-motion behavior is implemented through the shared adapter and CSS media query; an operating-system preference change was not exercised.

The production release completed successfully in 5m13s. Live checks confirmed the homepage, stylesheet, modules, sounds, icon, Support, Privacy and 404. Cloudflare's existing email protection transforms the Support/Privacy HTML, so those pages were compared by decoded text and contact destination; both match the source. The contact links were also checked in the rendered browser. An unknown URL returns the branded page with HTTP 404. See [live-checks.json](live-checks.json).

The `/app/` index, `composeApp.js` and `webAudio.js` match their pre-release hashes. The running web app was visually checked after publication and loaded its existing onboarding. Native application acceptance, exhaustive assistive-technology coverage and physical-device audio timing were outside this website release.

## Previews

[Desktop light](live-desktop-light.jpg) · [Desktop dark](live-desktop-dark.jpg) · [Mobile light](live-mobile-light.jpg).

The website stays on the existing vanilla HTML/CSS/JavaScript stack. Shared implementation lives in `docs/site.css`, `docs/site.js` and `docs/motion.js`; there are no new framework dependencies. Only the 11 website files were committed and published. Unrelated app and store-asset work was preserved.

The permanent Suby recommendation is titled **More by Merkost** across the current Settings mockups, reusable Pen components and website. This replaces the earlier studio heading.
