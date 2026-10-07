import argparse
import hashlib
import json
import plistlib
import re
import subprocess
from pathlib import Path


def command(*arguments):
    return subprocess.check_output(arguments, text=True).strip()


def inspect_object(path):
    architecture = command('/usr/bin/lipo', '-archs', str(path))
    if architecture != 'arm64':
        raise SystemExit(f'Expected arm64 simulator object: {path} ({architecture})')
    load_commands = command('/usr/bin/otool', '-l', str(path))
    if not re.search(r'platform\s+(?:7|IOSSIMULATOR)\b', load_commands):
        raise SystemExit(f'Expected iOS Simulator build platform: {path}')
    autolink = []
    for block in load_commands.split('Load command'):
        if 'LC_LINKER_OPTION' in block:
            values = re.findall(r'^\s*string #\d+\s+(.+)$', block, re.MULTILINE)
            if values:
                autolink.append(values)
    return {
        'path': str(path),
        'sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
        'architecture': architecture,
        'platform': 'ios-simulator',
        'autolink': autolink,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--derived-data', required=True, type=Path)
    parser.add_argument('--output-directory', required=True, type=Path)
    arguments = parser.parse_args()
    derived = arguments.derived_data.resolve()
    output = arguments.output_directory.resolve()
    products = derived / 'Build/Products/Debug-iphonesimulator'
    names = [
        'Firebase.o',
        'FirebaseCore.o',
        'FirebaseCoreExtension.o',
        'FirebaseCoreInternal.o',
        'FirebaseCrashlytics.o',
        'FirebaseCrashlyticsSwift.o',
        'FirebaseInstallations.o',
        'FirebaseRemoteConfigInterop.o',
        'FirebaseSessions.o',
        'FirebaseSessionsObjC.o',
        'FBLPromises.o',
        'Promises.o',
        'GoogleDataTransport.o',
        'GoogleUtilities-Environment.o',
        'GoogleUtilities-Logger.o',
        'GoogleUtilities-NSData.o',
        'GoogleUtilities-UserDefaults.o',
        'nanopb.o',
        'third-party-IsAppEncrypted.o',
    ]
    objects = [products / name for name in names]
    missing = [str(path) for path in objects if not path.is_file()]
    if missing:
        raise SystemExit('Build the Metronome simulator app with Xcode first. Missing SDK objects:\n' + '\n'.join(missing))
    if output.exists():
        raise SystemExit(f'Use a fresh output directory: {output}')
    inspected = [inspect_object(path) for path in objects]
    developer = Path(command('/usr/bin/xcode-select', '-p'))
    toolchain = developer / 'Toolchains/XcodeDefault.xctoolchain/usr'
    swift = toolchain / 'lib/swift/iphonesimulator'
    sdk = Path(command('/usr/bin/xcrun', '--sdk', 'iphonesimulator', '--show-sdk-path'))
    if not swift.is_dir():
        raise SystemExit(f'Swift simulator runtime libraries are missing: {swift}')
    output.mkdir(parents=True)
    libtool = str(toolchain / 'bin/libtool')
    primary = {'FirebaseCore': products / 'FirebaseCore.o', 'FirebaseCrashlytics': products / 'FirebaseCrashlytics.o'}
    for name, path in primary.items():
        framework = output / f'{name}.framework'
        framework.mkdir()
        command(libtool, '-static', '-o', str(framework / name), str(path))
        with (framework / 'Info.plist').open('wb') as stream:
            plistlib.dump({
                'CFBundleExecutable': name,
                'CFBundleIdentifier': f'testing.sdk.{name}',
                'CFBundlePackageType': 'FMWK',
            }, stream)
    transitive = output / 'libMetronomeIosTestSdk.a'
    command(libtool, '-static', '-o', str(transitive), *[str(path) for path in objects if path not in primary.values()])
    options = [
        f'-F{output}',
        f'-L{swift}',
        f'-L{sdk / "usr/lib/swift"}',
        '-framework', 'FirebaseCore',
        '-framework', 'FirebaseCrashlytics',
        '-force_load', str(transitive),
        '-rpath', '/usr/lib/swift',
        '-lc++', '-lz',
        '-framework', 'Security',
        '-framework', 'SystemConfiguration',
        '-framework', 'CoreTelephony',
        '-framework', 'UIKit',
    ]
    swift_libraries = sorted({
        group[0]
        for item in inspected
        for group in item['autolink']
        if len(group) == 1 and group[0].startswith('-lswift')
    })
    options.extend(swift_libraries)
    (output / 'linker-options.txt').write_text('\n'.join(options) + '\n')
    (output / 'manifest.json').write_text(json.dumps({
        'purpose': 'Link Kotlin iOS simulator tests against the real SDK products built by the Metronome Xcode project',
        'derivedData': str(derived),
        'developerDirectory': str(developer),
        'sdk': str(sdk),
        'objects': inspected,
        'linkerOptions': options,
    }, indent=2) + '\n')
    print(output)
    print(f'Prepared {len(objects)} real SDK objects for -PiosTestSdkDirectory={output}')


if __name__ == '__main__':
    main()
