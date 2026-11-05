# Android verification helper

The reusable checker is `tools/verify-android-release.py`. It verifies existing
packages; it does not build, sign or upload them. Version and build code are
required inputs so the same checker can validate a previous release.

Before use for 1.5.0 (12), it was exercised against the existing signed Android
1.4.0 (11) AAB and APK. Both packages identify `com.merkost.metronome`, minSdk 26,
targetSdk 37, with `debuggable` and `testOnly` false.

The final integration command passed **4 tests in 72.595 seconds**, exit code 0:

```text
/usr/bin/python3 -m unittest discover -s tools/tests -p 'test_*.py' -v
```

| Case | Result |
|---|---|
| Existing signed 1.4.0 (11) packages | All package checks passed |
| Request 1.5.0 for the 1.4.0 package | Rejected at the manifest identity check |
| Append an unsigned AAB asset | Rejected by the signed-payload check |
| Modify an APK after signing | Rejected by the APK signature check |

The successful baseline report records 501 signed application payloads, a
separately verified signed manifest, zero unsigned payloads, eleven matching
canonical audio resources in each package, two baseline-profile files in each
package, and eight native libraries in each package with 16KiB ELF alignment.
APK ZIP alignment also passed. Audio matching uses content hashes, including
the APK's obfuscated resource filenames.

The expected signing certificate SHA256 is
`e73661c9046d6755c908f745cbddcadaaa8323f0102bfadc29add13dba6f2036`.
The AAB's strict JAR verification returns code 4 for its self-signed certificate
chain. The checker accepts only those specific chain errors after every payload
signature and the pinned certificate are verified. It records the timestamp and
JarInputStream ordering warnings rather than treating an arbitrary strict error
as successful verification.

The downloaded [official bundletool 1.18.3](https://github.com/google/bundletool/releases/tag/1.18.3)
all-JAR was checked against its published SHA256 before execution:
`a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29`.

This baseline proves the checker against real package boundaries. The 1.5.0
artifacts require their own successful report and checksums after the release
build.
