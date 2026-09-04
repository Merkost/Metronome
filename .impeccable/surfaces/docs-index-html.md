---
version: 1
slug: "docs-index-html"
primary_target: "docs/index.html"
related_targets: []
---

Scope: `docs/index.html`, the public landing page at metronome.merkost.dev. Visitor mode: **Persuade**.

Audience: musicians who practise alone and are deciding whether to install. Action: **install** — both store links sit in the sticky bar and the badges repeat in the hero and the closing entry. The browser app is proof, reached by a secondary link.

Belief to earn: this is a beautiful, serious instrument. Design carries the argument, so the page's own craft is the proof.

Direction: **The Practice Log** (surface-scope roll, seed key 903debb9b1d3). Approved comp `.impeccable/mocks/decision/practice-log.webp`, sidecar marked approved.

## What the critique changed

A dual-agent critique (design review + detector) found the first build had abandoned the direction: the ledger had been deleted, leaving a page a budgeting app could ship unchanged. Rebuilt from those findings.

**Registration, not density, is the ledger's variable.** Hairlines out of phase with the type read as moire, which is what "too many stripes" was. The ruling is now two devices: a **field** (repeating gradient) only on blocks whose leading is exactly one 28px band, and **single drawn rules** at display baselines. Verified empirically with `Range.getClientRects()` — baselines land at 20.2px against `--rule-y: 20px`.

Never ruled: the device column, the store badges, h1/h2 blocks, the bar, the footer, FAQ answers. **No rule passes between a number and its own label.**

**Colour is concentrated, not spread.** Committed (30-60%) was rejected: it would lie about a genuinely monochrome product. The accent owns one semantic role — progression — plus one inverted closing region, roughly 10-12% coverage.

**The accent is the product's own.** `#167936` was invented by the comp and is not in DESIGN.md. Reconciled to Deep Mint `#1A8A2E`, with `--accent-ink #167936` for text sizes (4.94:1) and `--accent-deep #116018` for the filled region (6.97:1; the undarkened mint gave 4.00:1 and failed AA there).

## Tokens as built

ground `#F2F3F2` · ink `#000000` · secondary `#55605A` · field rule `#C8CEC8` (1.44:1) · structure rule `#A9B2A9` (1.96:1) · paper `#FFFFFF` · accent `#1A8A2E` / ink `#167936` / deep `#116018`. Lattice `--lh: 28px`, all leading integer multiples. Rules 1px, 0.5px at >=2dppx. Dark appearance declared; all four dark pairs pass AA (16.64 / 7.32 / 10.72 / 8.43:1).

## Implementation inventory and provenance

| Region | Medium | Provenance |
|---|---|---|
| Top bar, ledger, rules, figures, spec rows, FAQ, closing region | HTML/CSS | authored |
| Playhead | CSS + JS, snapped to the lattice | authored |
| Device frame | CSS: tonal bezel `#1C1E1C`->`#0A0B0A`, 36px radius, inner highlight, contact shadow | authored |
| `app-main.png` | raster, hero device screen | crop of `tools/store-screenshots/raw/main-nolabel.png`, real emulator capture 2026-09-03 |
| `detail-*.png` (5) | raster, evidential crops | native-resolution crops of the raw captures in `tools/store-screenshots/raw/`; no generated imagery on this page |
| Favicon | inline SVG data URI | authored |
| Store badges | remote official artwork | Apple and Google, required by their brand rules |

`detail-tempo.png` was deliberately re-cropped below the Start/Target steppers: the original capture predates commit `d9369a1` and still showed the `onPrimaryContainer` purple, which the app no longer renders. Shipping it would have advertised a fixed bug.

## Deviations, disclosed

The comp renders a photorealistic tilted device with a cast shadow. It ships as a drawn device with a **contact** shadow — physical truth, which is what the comp was expressing — rather than a stock 3D mockup. Tracking is `-0.04em`, not the `-0.05em` the review suggested, because the craft floor sets that as a hard limit.

## Open

- **Recommended DESIGN.md change, not made** (a durable system decision the owner should approve): scope the Flat Instrument Rule to in-product controls, distinguishing ornamental shadow (forbidden) from contact shadow (permitted on marketing surfaces). Applying it to a page depicting the product was a category error that cost the page its only element with mass.
- PRODUCT.md is stale: it records the old store name and claims index.html links a superseded App Store id. `docs/aso-strategy.md` is newer and disagrees. Reported, not acted on.
- The detector reports findings that are largely false positives (it cannot resolve `calc(var())`, and DESIGN.md documents the app's mobile palette with no web grey ramp). Not individually re-triaged after the rebuild.
