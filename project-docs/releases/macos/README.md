# Metronome for Mac release preparation

Prepared on 22 October 2026. This directory contains the desktop release configuration and submission draft. A successful build, artifact verification, sandbox runtime checks, upload receipt and App Review submission are separate evidence gates.

| Setting | Value |
| --- | --- |
| Store listing | Existing Metronome App Store record, ID 6761737690 |
| Store name baseline | Metronome: Practice & Tempo |
| App bundle ID | com.merkost.metronome |
| Runtime bundle ID | com.oracle.java.com.merkost.metronome |
| Developer team | P47X2292CM |
| Mac version / build | 1.5.0 / 1, independent of iOS build 4 and Android build 12 |
| Supported Mac | Apple silicon, macOS 13 or later |
| Category | Music |
| Packaging runtime | Bundled arm64 JetBrains Runtime 21; no separately installed Java requirement |
| Store format | Distribution-signed PKG containing one self-contained app |

The iOS 1.5.0 release is publicly available according to Apple's live Australian lookup on 22 October 2026. App Store Connect was also checked in the signed-in Chrome session: version 1.5.0 selects build 4 and is Ready for Distribution. Preserve the signed iOS export and original archives in `shared/release/1.5.0`.

## Local development

Use an arm64 JBR 21 shell. `:desktopApp:run` launches the source build, while `:desktopApp:createDistributable` creates a self-contained development app without enabling App Store signing. The package configuration checks the runtime vendor, version, architecture and icon before producing an app image.

```sh
export JAVA_HOME="/path/to/arm64-jbr-21/Contents/Home"
./gradlew :desktopApp:run
./gradlew :desktopApp:createDistributable
```

Run the packaged app separately to exercise the reduced runtime, rather than treating a successful source launch as package acceptance.

The build's `prepareMacSkikoRuntime` task prepares a separate copy of the resolved Skiko dependency before packaging. It removes the unused x64 renderer and checksum, preserves the exact arm64 renderer and other resources, and records source/output hashes in `desktopApp/build/macos/skiko-runtime/provenance.json`. Compose then performs its normal arm64 extraction and signing. The dependency cache and signed app are never modified by this preparation step.

## Store build

The existing Mac App Distribution and Mac Installer Distribution identities can sign the production Mac App Store release without provisioning profiles. Apple permits Mac App Store apps with only unrestricted entitlements to ship without profiles; App Sandbox itself is unrestricted. This mode omits application-identifier and team-identifier entitlement claims, while the actual code signatures still establish the developer team. It remains sandboxed and uses the normal Mac App Store signing and review process. It is **not eligible for TestFlight**.

```sh
export METRONOME_MACOS_JDK_HOME="$JAVA_HOME"
./gradlew :desktopApp:packagePkg -Pmacos.appStore=true
```

## Xcode archive and Organizer upload

The `Metronome Mac` scheme in `desktopApp/macosXcode/MetronomeMac.xcodeproj` is a native macOS application packaging target. Its build phase runs `tools/macos/stage-xcode-app.py`, checks the prepared app's identifier/version/build and signature, and copies its launcher, resources, application JARs and nested runtime into Xcode's staging directory. Xcode processes the original app Info.plist, signs the staged app with the existing Mac App Distribution identity, and generates the archive metadata itself. The Gradle-built app and PKG remain preserved.

Build and verify the signed Compose package first, then create the archive:

```sh
xcodebuild \
  -project desktopApp/macosXcode/MetronomeMac.xcodeproj \
  -scheme "Metronome Mac" \
  -configuration Release \
  -destination 'generic/platform=macOS' \
  -derivedDataPath /private/tmp/MetronomeMacXcode \
  -archivePath artifacts/releases/1.5.0/macos/Metronome-1.5.0-1.xcarchive \
  archive

/usr/bin/python3 tools/macos/verify-package.py \
  --app artifacts/releases/1.5.0/macos/Metronome-1.5.0-1.xcarchive/Products/Applications/Metronome.app \
  --jdk-home "$METRONOME_MACOS_JDK_HOME" \
  --output desktopApp/build/macos/verification/xcode-archive-app.json
```

Preserve completed release archives and choose a new archive path for subsequent builds. Open the generated archive in Xcode Organizer and confirm it is a macOS App Archive for team `P47X2292CM`, version 1.5.0 build 1 and Apple silicon. Choose Distribute App, Custom, App Store Connect and Upload. Use the existing Mac App Distribution identity and installer signing identity; this production configuration has no selected provisioning profile. Review the generated entitlements and signing summary before delivery.

Organizer uses the developer account already signed in to Xcode. Installing the separate Transporter app is not a prerequisite. Archive creation and entering the upload flow do not establish delivery: retain Apple's completed upload receipt and verify subsequent processing in App Store Connect.

## Optional TestFlight packaging

Use `-Pmacos.testFlight=true` in addition to `-Pmacos.appStore=true` only when preparing a TestFlight-capable package. This mode selects separate provisioned entitlement files and requires two **Mac App Store** provisioning profiles for the same team and application signing certificate:

- `com.merkost.metronome`
- `com.oracle.java.com.merkost.metronome`

An iOS profile, a wildcard development profile, a Developer ID profile or another app's Mac profile cannot replace these. The validation task checks explicit bundle IDs, macOS platform, expiration, distribution type, debugger restrictions and certificate membership. It copies the profiles only into the ignored build directory using the filenames required by the packaging tools. Provisioned identifiers must remain consistent with the embedded profiles and actual bundle IDs.

```sh
export METRONOME_MACOS_JDK_HOME="$JAVA_HOME"
export METRONOME_MAC_APP_PROFILE="/path/to/metronome-store.provisionprofile"
export METRONOME_MAC_RUNTIME_PROFILE="/path/to/metronome-runtime-store.provisionprofile"
./gradlew :desktopApp:packagePkg -Pmacos.appStore=true -Pmacos.testFlight=true
```

Equivalent settings can be supplied as Gradle properties:

| Gradle property | Environment variable | Default |
| --- | --- | --- |
| macos.appStore | METRONOME_MAC_APP_STORE | false |
| macos.testFlight | METRONOME_MAC_TESTFLIGHT | false; requires appStore=true |
| macos.jdkHome | METRONOME_MACOS_JDK_HOME | JAVA_HOME, then the Gradle JVM |
| macos.appProvisioningProfile | METRONOME_MAC_APP_PROFILE | Required only for TestFlight packaging |
| macos.runtimeProvisioningProfile | METRONOME_MAC_RUNTIME_PROFILE | Required only for TestFlight packaging |
| macos.signingIdentity | METRONOME_MAC_SIGNING_IDENTITY | Konstantin Merenkov (P47X2292CM) |
| macos.signingKeychain | METRONOME_MAC_SIGNING_KEYCHAIN | System keychain search list |

Never put credentials, private keys or provisioning profiles into tracked source files. No certificate creation, account changes, upload or submission is performed by these packaging tasks.

## Permissions

The main app process enables the App Sandbox, the JVM's JIT permission, and access to files explicitly chosen through the native open/save panels. The nested runtime's `libjli.dylib` is signed library code loaded inside that process; it has no separate process entitlements. The production mode does not claim restricted application/team identifiers or embed profiles. The optional TestFlight configuration adds the matching identifiers and profiles and requires its own verification; it has not been accepted in this release's production-package checks. Neither configuration requests microphone, camera, incoming/outgoing network, debugger, dynamic-loader environment, unsigned executable memory or disabled library-validation permissions.

Audio playback alone does not need microphone access. Opening the system browser for support or the product site does not grant the app general network access. Any later permission change must follow an actual failing runtime case and be reflected in verification and store privacy information.

## Verification

Use the exact app image that produced the installer:

```sh
/usr/bin/python3 tools/macos/verify-package.py \
  --app desktopApp/build/compose/binaries/main/app/Metronome.app \
  --pkg desktopApp/build/compose/binaries/main/pkg/Metronome-1.5.0.pkg \
  --jdk-home "$METRONOME_MACOS_JDK_HOME" \
  --output desktopApp/build/macos/verification/store-package.json
```

Pass the full arm64 JBR 21 used to build the package as `--jdk-home`; the verifier also accepts `METRONOME_MACOS_JDK_HOME` or `JAVA_HOME`. It uses that JBR's `jimage` and `javap` to inspect the packaged runtime's compiled `VersionProps.class`, requiring exact vendor, version and build agreement. The reduced runtime's `release` file alone lacks vendor and architecture fields, so those omissions are not treated as a missing runtime identity. Architecture and minimum deployment versions are verified independently for every native binary.

For a local unsigned development image, omit `--pkg` and add `--unsigned`. For a TestFlight package, add `--testflight`; this requires both matching profiles and provisioned entitlement claims. The default signed production check instead requires those restricted claims and embedded profiles to be absent. The script validates actual package versions and identifiers, category and minimum OS, arm64 native payloads, the embedded JBR, signed native libraries outside JARs, all eleven audio payloads against their source/conversion manifest, the icon and the selected entitlement mode. The main launcher must have its required process entitlements, while the production runtime bundle must be library code with no process entitlements. Store verification additionally checks trusted application/runtime signatures and installer identity, expands the PKG and compares its complete app bytes with the verified image. It rejects custom installer scripts, external bundle symlinks, world-writable content and an extra plug-in directory.

Artifact checks do not replace runtime checks. Before upload, exercise:

1. Fresh welcome, normal launch and saved-state restoration.
2. Compact, default and large windows in both appearances; resizing and keyboard focus.
3. Playback, each click/accent, tempo changes, subdivisions, panning, pause/resume and audio-device changes.
4. Timer, tempo/gap training, presets, routines and local statistics.
5. Import/export through the native file panels under the signed App Sandbox.
6. Settings, links, close/quit behavior, reduced motion and VoiceOver/keyboard access.

## Verified preparation status

On 22 October 2026, the combined build completed successfully: all 245 common/desktop tests passed, the signed Mac PKG was built, Android debug assembled, and the iOS simulator Kotlin source compiled. The current JUnit XML reports are under `shared/build/test-results/desktopTest`; the build log is `/private/tmp/metronome-mac-final-2.log`.

The final static verification receipts are `artifacts/macos-review-2026-10-22/tests/signed-app-verification.json` and `signed-package-verification.json`. They verify 30 arm64 native binaries, 11 audio payloads, JBR `21.0.11+10-b1163.116`, app/runtime distribution signatures and identical installer/app contents. The verified PKG is 76,138,637 bytes with SHA256 `3365107e87412ed09bfa39be99757ef9367f4422ffd640c5aa5f52356caece82`. Any rebuild needs a new receipt and checksum.

The final output smoke in `artifacts/macos-review-2026-10-22/tests/audio-hardware-final.json` accepted all 14 low/high submissions across seven sounds and verified mute/release suppression using packaged production classes and resources. It is an unsandboxed output-submission check, not a measurement of audible output, latency, route changes or signed sandbox acceptance.

Fresh native welcome acceptance completed on a frozen copy signed by the same team with a unique sandbox identity, preserving the production app's 79 JARs, launcher configuration and 204 non-code resource files. Welcome fits at 1000×780 and 520×600, Get started succeeds, and relaunch skips welcome. Native import restored three presets and one practice set; Escape works after the Open panel, and Mint/Dark settings persist after Quit. Provenance is recorded in `artifacts/macos-review-2026-10-22/acceptance/welcome-final-provenance.json`, with screenshots `native/07-fresh-welcome-qa.jpg` through `native/10-restored-settings-qa.jpg` in the review artifact directory. This isolated acceptance app is never a submission artifact.

The Mac 1.5.0 listing now has saved description, promotional text, review notes, working support/marketing URLs and five uploaded native 2560×1600 screenshots. The initial Add for Review validation required choosing a build; that requirement is now resolved. See `artifacts/macos-review-2026-10-22/store/listing-status.json` and `store/mac-submission-requires-build.jpg` in the review directory. Status is now Waiting for Review, with automatic release after approval selected.

The genuine Xcode archive completed successfully and Organizer recognizes it as a macOS App Archive, version 1.5.0 build 1, team `P47X2292CM`, Apple silicon. Its signed app passed static verification in `artifacts/macos-review-2026-10-22/tests/xcode-archive-app-verification.json`. Independent comparison in `tests/xcode-archive-provenance.json` within the review directory confirms all 79 JARs, nested native binaries, resources and symlinks match the verified source; only genuine Xcode Info.plist metadata and the outer launcher's signature changed. The launcher code/data and original Gradle PKG remain unchanged. Archive log: `artifacts/macos-review-2026-10-22/tests/xcode-archive-build.log`; Organizer evidence: `store/xcode-mac-archive.jpg`.

Organizer completed the upload using the existing Mac App Distribution identity, automatic installer identity selection and no provisioning profile. Apple processed build 1, which was selected and submitted at 22:17 AEDT on 22 October 2026. App Store Connect confirms Waiting for Review, submission `d8f0ecac-ab77-4fae-b2df-80b49ddde8f1`. Receipts: `store/xcode-upload-status.json` and `store/mac-waiting-for-review.jpg` in the review directory.

Upload completed with non-blocking warnings: this profileless production build cannot use TestFlight, and matching dSYMs are missing for the prebuilt launcher, Skiko and runtime, limiting native crash symbolication. The complete warning text is recorded in `store/xcode-upload-completed.txt`. Organizer's exported signed PKG, distribution summary and export options are preserved in `store/Metronome-Mac-1.5.0-1-upload/`.

Organizer's exported delivery package also passed static verification. All 30 native binaries retain identical executable content, entitlements and signing flags after accounting for renewed signatures; all 79 JARs match exactly. The nested runtime Info.plist was reformatted without changing its parsed values. The original Gradle PKG remains unchanged. Receipts: `artifacts/macos-review-2026-10-22/tests/organizer-export-package-verification.json` and `organizer-export-provenance.json` in the same directory.

## Submission

The macOS 1.5.0 submission is Waiting for Review in App Store Connect. Its platform-specific text and five native screenshots are saved. Future screenshot replacements must be actual opaque 16:10 PNG or JPEG at 1280×800, 1440×900, 2560×1600 or 2880×1800. Keep the reviewed [metadata draft](app-store-metadata.md) aligned with the final Mac package.

Upload, processing, build selection and review submission are complete. Apple approval and public Mac availability remain pending. No public Mac availability claim should be added to the website until that state is confirmed.

The Mac build does not inherit Firebase integrations from the iOS or Android source sets. Verify the final dependency graph before changing the shared App Store privacy declarations. The app's existing privacy URL must remain accurate for all platforms.

## Primary references

- [JetBrains native distributions](https://kotlinlang.org/docs/multiplatform/compose-native-distribution.html)
- [JetBrains Mac signing and store profiles](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Signing_and_notarization_on_macOS)
- [Apple DTS: TestFlight, provisioning profiles and the Mac App Store](https://developer.apple.com/forums/thread/733942)
- [Apple DTS: Distribution signing and main executables versus library code](https://developer.apple.com/forums/thread/701514)
- [Apple custom build phases](https://developer.apple.com/documentation/xcode/running-custom-scripts-during-a-build)
- [Apple app archive structure](https://developer.apple.com/documentation/technotes/tn3110-resolving-generic-xcode-archive-issue)
- [Apple Organizer upload workflow](https://help.apple.com/xcode/mac/current/en.lproj/dev442d7f2ca.html)
- [Apple add-platform workflow](https://developer.apple.com/help/app-store-connect/create-an-app-record/add-platforms/)
- [Apple screenshot requirements](https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications)
- [Apple upload tools and processing](https://developer.apple.com/help/app-store-connect/manage-builds/upload-builds/)
- [Apple Mac App Store requirements](https://developer.apple.com/app-store/review/guidelines/)
