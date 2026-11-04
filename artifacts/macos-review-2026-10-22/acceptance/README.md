# Fresh welcome acceptance copy

This local test app uses identical JARs, audio, resources and launcher configuration from the signed production image. Its app and runtime bundle IDs are unique. It is signed using the existing Mac Application distribution identity so all native code has team P47X2292CM. App Sandbox, hardened runtime, library validation, JIT and selected-file entitlements remain enabled.

The app uses a fresh sandbox container and normal data-path code. No production data is read, reset, moved or deleted. The original app signature remains valid and the original PKG hash matches its prior verification receipt.

Identity: `com.merkost.metronome.acceptance.a13df6317ae92465e836cd191bcebc8a1`.

The first ad-hoc fixture failed macOS library validation and exited before Java startup. Its newly created test container is retained. The corrected fixture uses another fresh identifier and was observed running as process 91413. The system log confirms initialization of its new sandbox container at 18:52:36 Australia/Sydney on 22 October 2026. Native welcome UI acceptance remains pending.

`welcome-copy-provenance.json` records every JAR digest and signature verification. Native UI acceptance is pending. This copy must never be uploaded to App Store Connect.
