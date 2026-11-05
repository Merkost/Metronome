# Metronome 1.5.0 release preparation

Prepared on 12 October 2026. Publication and store uploads have not been performed.

| Platform | Version | Build |
| --- | --- | --- |
| Android | 1.5.0 | 12 |
| iOS app and widget | 1.5.0 | 4 |
| Web app source | 1.5.0 | 12 |

Build numbers follow the current repository and published GitHub release. Store upload histories were not inspected.

## Release copy

- [App Store What's New](whats-new-app-store.txt): 968 characters.
- [Google Play What's New](whats-new-google-play.txt): 459 characters, within the 500-character limit.
- [GitHub release notes](release-notes.md).
- [Changelog](../../../CHANGELOG.md).

The in-app release sheet contains three highlights: A simpler welcome, A cleaner instrument and Smoother practice. Upgrades from 1.4.0 show the 1.5.0 highlights once; fresh installs retain the welcome flow.

## Packages and verification

The signed Android [AAB](../../../androidApp/release/1.5.0/Metronome-1.5.0-12.aab) and [APK](../../../androidApp/release/1.5.0/Metronome-1.5.0-12.apk) built and passed [package verification](../../../androidApp/release/1.5.0/release-verification.json). The production certificate matches the previous release. Both packages have the expected identity, all eleven canonical audio payloads, embedded baseline profiles and 16 KiB native-library/ZIP alignment. [Checksums](../../../androidApp/release/1.5.0/SHA256SUMS) and [source provenance](../../../androidApp/release/1.5.0/source-provenance.json) are saved beside the packages.

The unsigned iOS [device Release archive ZIP](../../../shared/release/1.5.0/Metronome-1.5.0-4-unsigned.xcarchive.zip) built successfully and passed [archive verification](../../../artifacts/releases/1.5.0/ios-archive-verification.json). The app and widget both contain 1.5.0 (4), device-only arm64 binaries and matching dSYMs. All 62 archive files match the successful build and ZIP, and all 254 source hashes match the recorded provenance. [Checksums](../../../shared/release/1.5.0/SHA256SUMS) are saved beside the archive. This host has an Apple Development identity available but no Apple Distribution identity was found. App Store signing and export are separate steps; this is not a signed store IPA.

The UI implementation passed 199 Android unit tests and 283 iOS tests, including 84 native UI rendering cases, before the version and release-copy updates. Those results are documented in [the native UI verification](../../../artifacts/ui-ux-review-2026-10-11/verification.md). All 200 Android unit tests now pass, including the new 1.4.0-to-1.5.0 once-only highlights regression. Both [native What's New checks](../../../artifacts/releases/1.5.0/whats-new-native-verification.json) passed: the real modal's three highlights, complete text, scrolling and dismissal in normal and 2x RTL layouts. The four integration tests for the [Android verification helper](verification-helper.md) also passed, including rejection of wrong versions, unsigned AAB additions and APK tampering. The signed Android packages and unsigned iOS archive passed their package checks.

The source includes the approved UI changes currently in the local dev working tree. Package provenance records source hashes and the base revision rather than attributing these uncommitted changes to the existing v1.4.0 tag. No branch, tag, merge or GitHub release has been created by this preparation. The web source compiles, but the production web app has not been redeployed as part of this preparation.
