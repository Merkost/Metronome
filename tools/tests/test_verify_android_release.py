import hashlib
import json
import os
import subprocess
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
VERIFIER = ROOT / "tools/verify-android-release.py"
PRIOR = ROOT / "androidApp/release/1.4.0"
BUNDLETOOL = Path(os.environ.get("METRONOME_RELEASE_TEST_BUNDLETOOL", "/private/tmp/MetronomeRelease150/tools/bundletool-all-1.18.3.jar"))


@unittest.skipUnless(BUNDLETOOL.is_file() and PRIOR.is_dir(), "Signed release and bundletool are required for integration checks")
class AndroidReleaseVerificationTest(unittest.TestCase):
    def run_verifier(self, output, aab=None, apk=None, version="1.4.0"):
        return subprocess.run(
            [
                sys.executable,
                str(VERIFIER),
                "--aab", str(aab or PRIOR / "Metronome-1.4.0-11.aab"),
                "--apk", str(apk or PRIOR / "Metronome-1.4.0-11.apk"),
                "--bundletool-all", str(BUNDLETOOL),
                "--outputdir", str(output),
                "--expected-version", version,
                "--expected-code", "11",
            ],
            capture_output=True,
            text=True,
        )

    def test_prior_signed_release_passes_real_package_checks(self):
        with tempfile.TemporaryDirectory(prefix="metronome-prior-release-") as directory:
            output = Path(directory)
            result = self.run_verifier(output)
            self.assertEqual(0, result.returncode, result.stdout + result.stderr)
            report = json.loads((output / "release-verification.json").read_text())
            self.assertTrue(report["checksPassed"])
            self.assertEqual("1.4.0", report["versionName"])
            self.assertEqual(11, report["versionCode"])
            self.assertEqual(report["identities"]["aab"], report["identities"]["apk"])
            self.assertFalse(report["identities"]["apk"]["debuggable"])
            self.assertFalse(report["identities"]["apk"]["testOnly"])
            self.assertTrue(report["productionSignaturesVerified"])
            self.assertTrue(report["aabValidationPassed"])
            self.assertTrue(report["apk16KiBAlignmentVerified"])
            self.assertEqual(501, report["packages"]["aab"]["signedPayloadEntries"])
            self.assertEqual(0, report["packages"]["aab"]["unsignedPayloadEntries"])
            self.assertTrue(report["signatures"]["aab"]["signedManifestVerified"])
            for package in ["aab", "apk"]:
                self.assertEqual(11, report["packages"][package]["soundResources"])
                self.assertEqual(2, len(report["packages"][package]["embeddedProfiles"]))
                self.assertEqual(8, len(report["packages"][package]["nativeLibraries"]))
                for library in report["packages"][package]["nativeLibraries"]:
                    self.assertTrue(library["elf16KiBAligned"])
            for line in (output / "SHA256SUMS").read_text().splitlines():
                digest, name = line.split("  ", 1)
                self.assertEqual(digest, hashlib.sha256((PRIOR / name).read_bytes()).hexdigest())

    def test_wrong_version_fails_at_manifest_boundary(self):
        with tempfile.TemporaryDirectory(prefix="metronome-wrong-version-") as directory:
            output = Path(directory)
            result = self.run_verifier(output, version="1.5.0")
            self.assertNotEqual(0, result.returncode)
            report = json.loads((output / "release-verification.json").read_text())
            self.assertFalse(report["checksPassed"])
            self.assertIn("versionName", report["error"])

    def test_unsigned_aab_payload_is_rejected(self):
        with tempfile.TemporaryDirectory(prefix="metronome-unsigned-payload-") as directory:
            output = Path(directory)
            aab = output / "unsigned-payload.aab"
            aab.write_bytes((PRIOR / "Metronome-1.4.0-11.aab").read_bytes())
            with zipfile.ZipFile(aab, "a") as archive:
                archive.writestr("base/assets/untrusted.bin", b"This was appended after signing.")
            result = self.run_verifier(output, aab=aab)
            self.assertNotEqual(0, result.returncode)
            report = json.loads((output / "release-verification.json").read_text())
            self.assertFalse(report["checksPassed"])
            self.assertIn("unsigned", report["error"].lower())

    def test_modified_apk_is_rejected_by_real_signature_check(self):
        with tempfile.TemporaryDirectory(prefix="metronome-modified-apk-") as directory:
            output = Path(directory)
            apk = output / "modified.apk"
            apk.write_bytes((PRIOR / "Metronome-1.4.0-11.apk").read_bytes())
            with zipfile.ZipFile(apk, "a") as archive:
                archive.writestr("assets/untrusted.bin", b"This was appended after signing.")
            result = self.run_verifier(output, apk=apk)
            self.assertNotEqual(0, result.returncode)
            report = json.loads((output / "release-verification.json").read_text())
            self.assertFalse(report["checksPassed"])
            self.assertIn("APK signature", report["error"])


if __name__ == "__main__":
    unittest.main()
