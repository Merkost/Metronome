# Product

<!-- impeccable:product-schema 1 -->

## Platform

adaptive

## Users

Musicians of all levels practising independently. They need a dependable pulse, clear tempo control, and structured tools that help them improve without interrupting the act of playing.

## Product Purpose

Metronome provides precise, reliable beat playback and serious practice support on Android and iOS. It exists to help musicians hold tempo, build speed, strengthen their internal clock, and sustain focused practice sessions. Success means the app can be trusted immediately, stays out of the musician's way, and remains useful as their practice becomes more demanding.

## Positioning

A precise, distraction-free metronome with a substantial practice toolkit built directly into an offline-capable experience. The product combines drift-resistant timing with tempo and gap trainers, subdivisions, timers, saved tempos, practice statistics, per-beat control, and platform-native background experiences without ads, accounts, or subscription pressure.

## Operating Context

- Used during independent instrumental or vocal practice, including sessions where the screen is locked or another app is foregrounded.
- Core metronome and practice functionality works without an internet connection.
- Android supports background playback through a foreground service and notification.
- iOS supports background playback, Live Activities, and Dynamic Island controls on supported devices.
- Settings and practice data are stored locally on the device.

## Capabilities and Constraints

- Precise tempo control, tap tempo, time signatures, accents, per-beat mute, subdivisions, stereo panning, multiple click sounds, visual beat displays, and haptic feedback.
- Practice timer, tempo trainer, gap trainer, count-in, saved tempos, and local practice statistics including daily time and streaks.
- Background playback, keep-screen-awake support, light and dark themes, and multiple color schemes.
- Android and iOS share product behaviour and Compose UI while retaining platform-appropriate capabilities and controls.
- The product is free, contains no advertisements, requires no account, and does not require internet access for its core experience.
- No personally identifiable information is required or intentionally collected.
- The repository privacy policy describes anonymous Firebase Crashlytics, Analytics, and Performance Monitoring data on Android, while the current Google Play declaration says no data is collected. These declarations must be reconciled before stronger privacy or data-collection claims are published.
- Localization beyond the current English implementation and the exact accessibility conformance target remain open decisions.

## Brand Commitments

- Current product name: **Metronome: Feel the Beat!**
- Current on-device display name: **Metronome**
- Approved next store name: **Metronome: BPM & Practice**
- Approved App Store subtitle: **Precise Timing & Rhythm Tools**
- Approved product tagline: **Precise timing. Better practice.**
- The voice should be clear, calm, useful, and credible to musicians rather than promotional or distracting.
- Store metadata should retain immediate category recognition, communicate precision and practice value, and avoid unsupported superiority claims. The approved metadata package is recorded in `docs/aso-strategy.md`.

## Evidence on Hand

- Current features and technical overview: `README.md`
- Shipped practice capabilities and roadmap: `docs/ROADMAP.md`
- Release history and timing claims: `CHANGELOG.md`
- Current store copy and confirmed no-ads/offline claims: `RELEASE_NOTES.md`
- Privacy commitments and Android telemetry disclosure: `docs/privacy.html`
- Existing product page, screenshots, store links, and current tagline: `docs/index.html` and `docs/screenshots/`
- Android package identity: `com.merkost.metronome`
- Current iOS App Store identity: `6761737690`
- `docs/index.html` still links to the superseded App Store identity `6480380648` and should be corrected before the next website publication.
- No testimonials, awards, comparative benchmarks, or independently verified performance measurements are established in the repository and must not be fabricated.

## Product Principles

1. Timing earns trust: playback accuracy and audio reliability come before secondary features.
2. Practice has depth, not friction: serious training tools should remain quick to reach and simple to operate while playing.
3. Distraction-free means respectful: no ads, no account requirement, no subscription pressure, and no unnecessary dependence on connectivity.
4. Local by default: settings and practice activity remain on the musician's device unless a future feature explicitly requires otherwise.
5. Native where it matters: maintain a coherent cross-platform product while using Android and iOS conventions and system capabilities appropriately.

## Accessibility & Inclusion

The app serves musicians at every experience level. Future work must preserve clear control labels, strong state communication that does not rely on colour alone, usable touch targets, assistive-technology semantics, reduced-motion compatibility, and platform text-scaling behaviour. A formal accessibility standard has not yet been selected.
