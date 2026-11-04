import importlib.util
import hashlib
from pathlib import Path
import sys
import tempfile
import unittest
import zipfile
from unittest.mock import patch


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("metronome_macos_release_verifier", ROOT / "tools/macos/verify-package.py")
VERIFIER = importlib.util.module_from_spec(SPEC)
sys.path.insert(0, str(ROOT / "tools/macos"))
try:
    SPEC.loader.exec_module(VERIFIER)
finally:
    sys.path.pop(0)

SKIKO_SPEC = importlib.util.spec_from_file_location("metronome_skiko_preparer", ROOT / "tools/macos/prepare-skiko-runtime.py")
SKIKO = importlib.util.module_from_spec(SKIKO_SPEC)
SKIKO_SPEC.loader.exec_module(SKIKO)


MODERN = """Metronome.app/Contents/MacOS/Metronome:
Mach header
      magic  cputype cpusubtype  caps    filetype ncmds sizeofcmds      flags
 0xfeedfacf 16777228          0  0x00           2    17       1728 0x00200085
Load command 0
      cmd LC_SEGMENT_64
  cmdsize 72
  segname __PAGEZERO
Load command 1
      cmd LC_BUILD_VERSION
  cmdsize 32
 platform 1
    minos 13.0
      sdk 26.5
   ntools 1
     tool 3
  version 1200.0
"""

LEGACY = """Metronome.app/Contents/runtime/Contents/Home/lib/libjvm.dylib:
Load command 0
      cmd LC_ID_DYLIB
  cmdsize 80
     name @rpath/libjvm.dylib (offset 24)
Load command 1
      cmd LC_VERSION_MIN_MACOSX
  cmdsize 16
  version 10.15
      sdk 26.5
"""

VERSION_PROPS = """class java.lang.VersionProps {
  private static final java.lang.String java_version = "21.0.11";
  private static final java.lang.String java_runtime_version = "21.0.11+10-b1163.116";
  private static final java.lang.String VENDOR = "JetBrains s.r.o.";
}
"""


class BundledRuntimeIdentityTest(unittest.TestCase):
    def setUp(self):
        self.source = {
            "JAVA_VERSION": "21.0.11",
            "JAVA_RUNTIME_VERSION": "21.0.11+10-b1163.116",
            "IMPLEMENTOR": "JetBrains s.r.o.",
            "OS_ARCH": "aarch64",
            "OS_NAME": "Darwin",
        }
        self.trimmed = {"JAVA_VERSION": "21.0.11", "MODULES": "java.base java.desktop"}

    def test_trimmed_release_uses_compiled_identity(self):
        result = VERIFIER.check_compiled_runtime_identity(self.trimmed, self.source, VERSION_PROPS)
        self.assertEqual("JetBrains s.r.o.", result["IMPLEMENTOR"])
        self.assertEqual("21.0.11+10-b1163.116", result["JAVA_RUNTIME_VERSION"])
        self.assertEqual("java.base/java/lang/VersionProps.class", result["identitySource"])

    def test_full_input_validation_still_rejects_trimmed_metadata(self):
        with self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.check_runtime_properties(self.trimmed)

    def test_wrong_vendor_compiled_into_runtime_is_rejected(self):
        with self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.check_compiled_runtime_identity(self.trimmed, self.source, VERSION_PROPS.replace("JetBrains s.r.o.", "Oracle Corporation"))

    def test_compiled_release_and_runtime_versions_must_match_selected_jdk(self):
        for original, replacement in [("21.0.11\"", "21.0.10\""), ("21.0.11+10-b1163.116", "21.0.11+9-other")]:
            with self.subTest(replacement=replacement), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.check_compiled_runtime_identity(self.trimmed, self.source, VERSION_PROPS.replace(original, replacement))

    def test_release_version_must_match_compiled_version(self):
        with self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.check_compiled_runtime_identity(dict(self.trimmed, JAVA_VERSION="21.0.10"), self.source, VERSION_PROPS)

    def test_missing_or_ambiguous_compiled_constants_are_rejected(self):
        for text in [VERSION_PROPS.replace('  private static final java.lang.String VENDOR = "JetBrains s.r.o.";\n', ""), VERSION_PROPS + VERSION_PROPS, "JetBrains s.r.o. 21.0.11"]:
            with self.subTest(text=text), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.check_compiled_runtime_identity(self.trimmed, self.source, text)

    def test_unverified_inspection_runtime_is_rejected(self):
        for key, value in [("IMPLEMENTOR", "Oracle Corporation"), ("JAVA_VERSION", "25.0.1"), ("OS_ARCH", "x86_64"), ("OS_NAME", "Linux"), ("JAVA_RUNTIME_VERSION", "")]:
            with self.subTest(key=key), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.check_compiled_runtime_identity(self.trimmed, dict(self.source, **{key: value}), VERSION_PROPS)

    def test_conflicting_optional_release_identity_is_rejected(self):
        for key, value in [("IMPLEMENTOR", "Oracle Corporation"), ("JAVA_RUNTIME_VERSION", "21.0.11+9-other"), ("OS_ARCH", "x86_64"), ("OS_NAME", "Linux")]:
            with self.subTest(key=key), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.check_compiled_runtime_identity(dict(self.trimmed, **{key: value}), self.source, VERSION_PROPS)


class RuntimeLibraryEntitlementsTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix="metronome-runtime-library-")
        self.addCleanup(self.temporary.cleanup)
        self.runtime = Path(self.temporary.name)
        self.info = {"CFBundleExecutable": "libjli.dylib"}
        executable = self.runtime / "Contents/MacOS/libjli.dylib"
        executable.parent.mkdir(parents=True)
        executable.write_bytes(b"library")

    def test_signed_library_may_have_no_entitlements(self):
        with patch.object(VERIFIER, "run", return_value=b"Mach-O 64-bit dynamically linked shared library arm64"), patch.object(VERIFIER, "read_code_entitlements", return_value={}):
            self.assertEqual({}, VERIFIER.check_runtime_library_entitlements(self.runtime, self.info))

    def test_missing_sandbox_on_a_main_executable_cannot_use_library_exception(self):
        with patch.object(VERIFIER, "run", return_value=b"Mach-O 64-bit executable arm64"), patch.object(VERIFIER, "read_code_entitlements", return_value={}), self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.check_runtime_library_entitlements(self.runtime, self.info)

    def test_library_cannot_claim_process_entitlements(self):
        with patch.object(VERIFIER, "run", return_value=b"Mach-O 64-bit dynamically linked shared library arm64"), patch.object(VERIFIER, "read_code_entitlements", return_value={"com.apple.security.app-sandbox": True}), self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.check_runtime_library_entitlements(self.runtime, self.info)

    def test_absent_entitlement_blob_is_empty_but_malformed_blob_is_rejected(self):
        support = sys.modules["release_support"]
        with patch.object(support, "run", return_value=b""):
            self.assertEqual({}, support.read_code_entitlements(self.runtime))
        with patch.object(support, "run", return_value=b"invalid"), self.assertRaises(support.plistlib.InvalidFileException):
            support.read_code_entitlements(self.runtime)


class SkikoPreparationTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix="metronome-skiko-preparation-")
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.source = self.root / "source/skiko-awt-runtime-macos-arm64-0.150.1.jar"
        self.destination = self.root / "prepared" / self.source.name
        self.receipt = self.root / "prepared/provenance.json"
        self.entries = {
            "META-INF/MANIFEST.MF": b"Manifest-Version: 1.0\n",
            SKIKO.ARM_LIBRARY: b"arm64-renderer",
            SKIKO.ARM_LIBRARY + ".sha256": hashlib.sha256(b"arm64-renderer").hexdigest().encode(),
            SKIKO.INTEL_LIBRARY: b"intel-renderer",
            SKIKO.INTEL_LIBRARY + ".sha256": hashlib.sha256(b"intel-renderer").hexdigest().encode(),
            "LICENSE": b"upstream-license",
        }

    def write_source(self):
        self.source.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(self.source, "w") as archive:
            for name, contents in self.entries.items():
                archive.writestr(name, contents)

    def test_preparation_preserves_arm64_and_other_resources_without_mutating_source(self):
        self.write_source()
        original = self.source.read_bytes()
        result = SKIKO.prepare_runtime(self.source, self.destination, self.receipt)
        self.assertEqual(original, self.source.read_bytes())
        with zipfile.ZipFile(self.destination) as archive:
            self.assertEqual({name: contents for name, contents in self.entries.items() if not name.startswith(SKIKO.INTEL_LIBRARY)}, {name: archive.read(name) for name in archive.namelist()})
        self.assertEqual(hashlib.sha256(original).hexdigest(), result["sourceSha256"])
        self.assertEqual(sorted([SKIKO.INTEL_LIBRARY, SKIKO.INTEL_LIBRARY + ".sha256"]), result["removedEntries"])

    def test_arm64_only_upstream_is_supported_without_removals(self):
        self.entries.pop(SKIKO.INTEL_LIBRARY)
        self.entries.pop(SKIKO.INTEL_LIBRARY + ".sha256")
        self.write_source()
        result = SKIKO.prepare_runtime(self.source, self.destination, self.receipt)
        self.assertEqual([], result["removedEntries"])

    def test_corrupt_arm64_hash_is_rejected_before_output(self):
        self.entries[SKIKO.ARM_LIBRARY + ".sha256"] = b"0" * 64
        self.write_source()
        with self.assertRaises(SKIKO.ValidationError):
            SKIKO.prepare_runtime(self.source, self.destination, self.receipt)
        self.assertFalse(self.destination.exists())

    def test_unknown_native_payload_or_signature_cannot_be_silently_dropped(self):
        for unexpected in ["unknown.dylib", "native.dll", "META-INF/UPSTREAM.RSA"]:
            with self.subTest(unexpected=unexpected):
                self.entries[unexpected] = b"unexpected"
                self.write_source()
                with self.assertRaises(SKIKO.ValidationError):
                    SKIKO.prepare_runtime(self.source, self.destination, self.receipt)
                self.entries.pop(unexpected)

    def test_dependency_cache_cannot_be_used_as_output(self):
        self.write_source()
        with self.assertRaises(SKIKO.ValidationError):
            SKIKO.prepare_runtime(self.source, self.source, self.receipt)


class NativeMacOSDeploymentTargetTest(unittest.TestCase):
    def test_modern_target_at_claimed_minimum_accepts_newer_sdk(self):
        self.assertEqual("13.0", VERIFIER.native_minimum_macos(MODERN))

    def test_legacy_target_returns_actual_deployment_version(self):
        self.assertEqual("10.15", VERIFIER.native_minimum_macos(LEGACY))

    def test_modern_older_targets_and_zero_patch_are_compatible(self):
        for version in ["10.15", "11.0", "12.7.6", "13.0.0"]:
            with self.subTest(version=version):
                self.assertEqual(version, VERIFIER.native_minimum_macos(MODERN.replace("minos 13.0", f"minos {version}")))

    def test_macos_platform_names_are_accepted(self):
        for platform in ["macos", "MACOS"]:
            with self.subTest(platform=platform):
                self.assertEqual("13.0", VERIFIER.native_minimum_macos(MODERN.replace("platform 1", f"platform {platform}")))

    def test_modern_later_os_is_rejected(self):
        for version in ["13.0.1", "13.1", "14.0", "26.0"]:
            with self.subTest(version=version), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.native_minimum_macos(MODERN.replace("minos 13.0", f"minos {version}"))

    def test_legacy_later_os_is_rejected(self):
        with self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.native_minimum_macos(LEGACY.replace("version 10.15", "version 14.0"))

    def test_missing_deployment_command_is_rejected(self):
        with self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.native_minimum_macos("Load command 0\n cmd LC_LOAD_DYLIB\n name /usr/lib/libSystem.B.dylib\n")

    def test_multiple_deployment_commands_are_rejected(self):
        for extra in [MODERN, LEGACY]:
            with self.subTest(extra=extra), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.native_minimum_macos(MODERN + extra)

    def test_wrong_apple_platform_is_rejected(self):
        for platform in ["2", "6", "ios", "maccatalyst", "unknown"]:
            with self.subTest(platform=platform), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.native_minimum_macos(MODERN.replace("platform 1", f"platform {platform}"))
        with self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.native_minimum_macos(LEGACY.replace("LC_VERSION_MIN_MACOSX", "LC_VERSION_MIN_IPHONEOS"))

    def test_missing_and_duplicate_required_fields_are_rejected(self):
        for output in [
            MODERN.replace(" platform 1\n", ""),
            MODERN.replace("    minos 13.0\n", ""),
            MODERN.replace(" platform 1\n", " platform 1\n platform 2\n"),
            MODERN.replace("    minos 13.0\n", "    minos 13.0\n minos 14.0\n"),
            LEGACY.replace("  version 10.15\n", ""),
            LEGACY.replace("  version 10.15\n", "  version 10.15\n version 14.0\n"),
        ]:
            with self.subTest(output=output), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.native_minimum_macos(output)

    def test_malformed_version_numbers_are_rejected(self):
        for version in ["13", "13.0beta", "13.0 extra", "13.0.0.1", "-1.0", "0.0", "NaN", ""]:
            with self.subTest(version=version), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.native_minimum_macos(MODERN.replace("minos 13.0", f"minos {version}"))

    def test_unstructured_command_text_is_not_accepted_as_metadata(self):
        with self.assertRaises(VERIFIER.ValidationError):
            VERIFIER.native_minimum_macos("cmd LC_BUILD_VERSION\nplatform 1\nminos 13.0\n")

    def test_each_native_binary_records_its_actual_minimum_os(self):
        with tempfile.TemporaryDirectory(prefix="metronome-native-target-") as directory:
            app = self.create_native_payloads(Path(directory))
            with patch.object(VERIFIER, "run", side_effect=self.native_tool_output):
                entries = VERIFIER.check_mach_o(app, signed=False)
            self.assertEqual(4, len(entries))
            self.assertEqual({"13.0", "10.15"}, {entry["minimumMacOS"] for entry in entries})

    def test_later_native_dependency_is_rejected_even_if_launcher_supports_13(self):
        with tempfile.TemporaryDirectory(prefix="metronome-native-target-") as directory:
            app = self.create_native_payloads(Path(directory))

            def tool_output(arguments):
                result = self.native_tool_output(arguments)
                if arguments[0] == "/usr/bin/otool" and Path(arguments[-1]).name == "libskiko.dylib":
                    return result.replace(b"minos 13.0", b"minos 14.0")
                return result

            with patch.object(VERIFIER, "run", side_effect=tool_output), self.assertRaises(VERIFIER.ValidationError):
                VERIFIER.check_mach_o(app, signed=False)

    def create_native_payloads(self, root):
        app = root / "Metronome.app"
        for name in ["Metronome", "libjvm.dylib", "libawt.dylib", "libskiko.dylib"]:
            path = app / "Contents/MacOS" / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(b"\xcf\xfa\xed\xfe" + name.encode())
        return app

    def native_tool_output(self, arguments):
        if arguments[0] == "/usr/bin/file":
            return b"Mach-O 64-bit arm64 dynamically linked shared library\n"
        if arguments[0] == "/usr/bin/lipo":
            return b"arm64\n"
        if arguments[0] == "/usr/bin/otool":
            return (LEGACY if Path(arguments[-1]).name == "libjvm.dylib" else MODERN).encode()
        raise AssertionError(arguments)


if __name__ == "__main__":
    unittest.main()
