# Metronome interactive design review

The current app design exploration is published at https://metronome.merkost.dev/design/. The production website remains at `/` and the Kotlin/Wasm app at `/app/`.

This copy contains the reviewed settings, welcome flow, shared motion components, unnumbered beats, web-app discovery and permanent More by Merkost section. Practice data is temporary, and new sounds are audition sketches.

From the repository root, install dependencies with `npm ci --prefix web/design-review`, then run `node tools/build-design-review.mjs`. The output is `_site/design/`. Supply a different site output directory as the final argument when needed.

The deployment wrapper checks the protected runtime and TypeScript, builds with `/design/` as its base, and adapts absolute resource links in the exported files. It verifies page resources and the device and sound assets. The original runtime files and their lock hashes stay unchanged.

The Deploy site workflow builds this review alongside the existing website and web app, then publishes all three to the existing Cloudflare Pages project.
