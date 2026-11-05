# merkost.dev — Framer site brief

Paste the Framer project link into a new thread, then point it at this file.
Written 2026-09-05. Everything under "Current state" was read off the published
page, not assumed.

## The site

`merkost.dev` is a Cloudflare 301 to `merkost.framer.website`, a Framer-hosted
personal site. Title: "Merkost — Mobile App Developer". It is a portfolio whose
job is to get a client to hire the owner, so the APPS section is evidence, not
decoration.

Editing requires the `framer` skill, which needs `npx @framer/agent@latest setup`
to have run (it has) and the project link pasted in the thread.

## Current state — the APPS section

Four cards, numbered `[01]`–`[04]`, each with type tags like `[MOBILE APP]`
`[KMP + COMPOSE]`. Their links today:

| Card | Links to | Correct? |
|---|---|---|
| SUBY `[01]` | `https://subyapp.com` | yes — leave it alone |
| METRONOME `[02]` | `https://merkost.notion.site/Portfolio-07e5d0d3944548f49f40851fc39e6546` | **no** |
| FISHING NOTES `[03]` | the same Notion portfolio page | needs a decision |
| ANDROID DRAWABLE PREVIEW `[04]` | the same Notion portfolio page | needs a decision |

So three of four cards dead-end on one generic Notion page. The section promises
four projects and delivers one real destination.

## Change 1 — point Metronome at its own site (the actual ask)

`METRONOME [02]` → **`https://metronome.merkost.dev`**

That site is live, on a Google-issued cert, and is the product's own landing
page: real screenshots, the full practice-tools story, and both store links.
It is a far stronger destination than a Notion page.

## Change 2 — add the browser demo

Nothing on merkost.dev mentions that the Metronome app **runs live in a browser
tab**, compiled from the same Kotlin/Compose source to WebAssembly. For a client
evaluating a mobile developer, "here is my app, running right now, no install"
is a stronger proof of Compose Multiplatform than any screenshot.

Add a secondary link on the Metronome card, or in whatever detail view it opens:

- Label: **Try it in the browser**
- URL: **`https://metronome.merkost.dev/app/`**

Keep it visually secondary to the primary card link — it is supporting evidence,
not the headline.

## Change 3 — decide the other two cards

Not my call, but the section is weaker while they point at a shared Notion page:

- `FISHING NOTES [03]` — is there a store listing or repo to link?
- `ANDROID DRAWABLE PREVIEW [04]` — this is an Android Studio plugin, and the
  repo `https://github.com/Merkost/Android-drawable-preview-plugin` is already
  linked elsewhere on the page. The JetBrains Marketplace listing would be
  better if one exists.

If a real destination does not exist for one, linking its GitHub repo beats
linking a portfolio index.

## Design constraints

**Do not redesign the site.** The card system — numbered `[01]`–`[04]` with
bracketed type tags — is clean and reads well. The weakness is destinations, not
styling. A visual overhaul was considered and rejected: restyling will not fix a
section where three cards go to the same place.

If any new element is added, match what is already there: sentence-free
uppercase tags in brackets, the existing type scale, no new colours.

## Verification

After publishing, confirm:

1. `METRONOME [02]` resolves to `https://metronome.merkost.dev` and returns 200.
2. `SUBY [01]` still resolves to `https://subyapp.com` — it was already correct
   and must not be disturbed.
3. The browser-demo link resolves to `https://metronome.merkost.dev/app/`.
4. The APPS section still renders at phone width without the cards reflowing.

## Context worth carrying into that thread

- `metronome.merkost.dev` is served by **Cloudflare Pages**, deployed by a
  GitHub Action in the Metronome repo. It is not GitHub Pages any more.
- The Metronome landing page was rebuilt to use the app's own visual language:
  white ground, one tonal step for a card, no shadows, monochrome. If you want
  the Framer card's thumbnail refreshed, take it from that site rather than from
  older marketing art, which used a warm palette that no longer exists anywhere.
