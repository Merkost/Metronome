---
version: 1
slug: "docs-index-html"
primary_target: "docs/index.html"
related_targets: []
---

Scope: `docs/index.html`, the public landing page at metronome.merkost.dev. Visitor mode: **Persuade**.

Audience: musicians who practise alone and are deciding whether to install. Action: **install** — one ink CTA in the sticky bar, both store badges in the hero and in the closing panel. The browser build is proof, offered beside them.

Belief to earn: this is a beautiful, serious instrument. Design carries the argument, so the page's own craft is the proof.

Direction: **The product's own surface.** Superseded "The Practice Log" on client rejection: *"the UI/UX does not represent the same feel as in app elements! There is no calm and elevated card feel! And there are lines, triangles and green color which we don't use at all!"* All three named offenders were real, and none of them existed in the app.

## What the redesign changed

**Depth is one tonal step, because that is the app's entire depth system.** `Surface(shape = RoundedCornerShape(16.dp), color = surfaceContainerLow)` on a `surface` ground, and Compose defaults both tonal and shadow elevation to zero. Evidence: `.shadow(` has zero hits across 102 commonMain files; `shadowElevation` appears twice and is `0.dp` both times (`AppMenu.kt:45`, `DropdownSelector.kt:56`); the four elevation tokens at `Dimensions.kt:39-42` have no call sites at all. So the page uses no `box-shadow` anywhere. A card is `#FFFFFF` → `#F7F7F7`, and the one active card is `#E6E6E6`.

**The palette is the default scheme, verified rather than assumed.** A fresh install resolves to `BLACKNWHITE` in all three places that pick one — `AppDatastoreImpl.kt:59`, `Theme.kt:19`, `SettingsViewModel.kt:21` — and Settings names it "Monochrome". Green is `MINT_GREEN`, one of five user themes; `MintGreenDark = #1a8a2e` was verbatim the old page's accent. Every app neutral is strictly R=G=B, where every neutral on the old page was hue-120° tinted.

**Ink and text are different roles and the app enforces it.** The BPM numeral is `onSurface` `#1D1B20` (`MainScreen.kt:472`); the play button and selected chips are `primary` `#000000`. So all running text is `#1D1B20` and only filled objects are pure ink. The app has no grey-text role — `onSurfaceVariant` is overridden to black (`ColorSchemes.kt:72`) — so de-emphasis is alpha of the ink, never a separate grey.

**Deleted outright:** the ruled field and every ledger separator, the playhead triangle and its script, the rail, the tempo ruler, the CSS device frame, the `.split` two-column layout, and all nine accent tokens. The page draws exactly two hairlines: under the bar once scrolled, and above the footer.

**Chips are the app's, not decoration.** `AppChip` inverts its container to ink and sets the label Bold when selected, so selection is never colour alone. Time signatures, subdivisions, per-beat states and click sounds each show the app's real default.

**Hierarchy.** The stats were the largest objects on the page (89.6px against an 88px h1) — three disclaimed example figures outranking the headline, against DESIGN.md's "Tempo Owns the Room". They are demoted into the practice section as the app's own 12px-label-over-20px-value stat row, and h1 owns the page.

## Deviations, disclosed

Captures are of the light theme, so in dark appearance they read as bright panels on black. Honest but imperfect; dark-theme captures would need a re-shoot of every screen.

Container padding is 24px where the app uses 16dp: a 680px web container at 16px reads mean. Still on the app's 8/16/32 ladder, and named here as a deliberate scale-up rather than an app value.

The images carry no container. A capture brings its own white, so a grey card around one reads as a card inside a card — the nesting the craft floor bans. Cards hold data; captures stand on the page.

## Open

- The hero would be stronger with a properly composited device raster in the manner of `og-card.png`. Not attempted: it is an asset to cut, not something to fake in CSS, and the drawn frame it replaces had failed twice.
- `og-card.png` is built on `#F2EFEA`, a warm neutral that agrees with neither the app nor this page. Worth re-exporting on `#FFFFFF`.
