# Project documentation

Internal documentation for Metronome: strategy, store work, design research, engineering audits, plans and release records. Nothing here is published. The public website lives in `docs/`, which is deployed to Cloudflare Pages as is, so keep internal writing out of it.

## Where things are

| Folder | What it holds |
| --- | --- |
| `strategy/` | `ROADMAP.md` (the 2026-2027 release train and item ids), the product reviews and the competitive landscape that the roadmap argues from |
| `store/` | `aso-strategy.md` (approved names, subtitle, tagline, keywords), `screenshot-research.md`, the App Review reply |
| `marketing/` | Landing page copy, legal pages copy, the Framer site brief for merkost.dev |
| `design/` | Design, motion and onboarding explorations, the UI/UX audit |
| `engineering/` | The timing precision audit and the Phase 2 plan that came out of it |
| `plans/` | Implementation plans, one file per feature, dated |
| `specs/` | The design documents those plans implement, dated |
| `releases/` | One folder per release with notes and store text, plus `macos/` for the Mac App Store submission |

Other places that hold project writing:

- `CHANGELOG.md` is the release history, with the per-store "What's New" messages for each version.
- `PRODUCT.md` and `DESIGN.md` at the repository root are the product and design briefs that tooling reads.
- `CLAUDE.md` and `AGENTS.md` are the engineering rules, kept identical.
- `tools/store-screenshots/README.md` documents how the store screenshots are captured and exported.
- `artifacts/` holds dated evidence from individual reviews and experiments. It is a record, not a source of truth, and `artifacts/custom-domain-deployment-2026-10-03/preview/` is an old copy of the docs folder.

## Releases

| Version | Notes |
| --- | --- |
| 1.6.0 | `releases/1.6.0/release-notes.md`: GitHub notes, App Store and Google Play text in one file |
| 1.5.0 | `releases/1.5.0/` |
| Mac App Store | `releases/macos/` |
| 1.1 to 1.4 | `releases/legacy-store-notes-1.1-1.4.md` (older store copy) |

Each release gets a folder named by version with a single `release-notes.md`. Add a README only when the release needs a verification record.

## Conventions

- Name dated documents `YYYY-MM-DD-topic.md`, or put the date at the end for research (`product-review-2026-09.md`).
- Refer to other documents by repository path, for example `project-docs/strategy/ROADMAP.md`.
- New implementation plans go in `plans/` and their specs in `specs/`. The superpowers tooling writes to `docs/superpowers/`, which is git-ignored; move anything worth keeping here.
