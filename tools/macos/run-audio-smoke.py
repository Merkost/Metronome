import argparse
import datetime
import json
import os
from pathlib import Path
import subprocess
import sys


DEFAULT_JDK = Path(os.environ.get("METRONOME_MACOS_JDK_HOME", os.environ.get("JAVA_HOME", str(Path.home() / "Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home"))))


def packaged_classpath(app):
    application = (app / "Contents/app").resolve()
    configurations = list(application.glob("*.cfg"))
    if len(configurations) != 1:
        raise ValueError("The packaged launcher configuration is missing or ambiguous.")
    entries = []
    for line in configurations[0].read_text().splitlines():
        if line.startswith("app.classpath="):
            entries.extend(line.split("=", 1)[1].split(os.pathsep))
    if not entries:
        raise ValueError("The packaged launcher has no application classpath.")
    paths = []
    for entry in entries:
        if not entry.startswith("$APPDIR/"):
            raise ValueError("The launcher classpath contains an external entry.")
        path = (application / entry.removeprefix("$APPDIR/")).resolve(strict=True)
        path.relative_to(application)
        if not path.is_file():
            raise ValueError("A packaged classpath entry is not a file.")
        paths.append(str(path))
    return os.pathsep.join(paths)


def main():
    parser = argparse.ArgumentParser(description="Plays fourteen brief clicks through packaged production code and the Mac's current default output. Checks submission, not audibility or speaker timing.")
    parser.add_argument("--app", type=Path, required=True)
    parser.add_argument("--jdk-home", type=Path, default=DEFAULT_JDK)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    try:
        app = args.app.expanduser().resolve(strict=True)
        if not app.is_dir() or app.suffix != ".app":
            raise ValueError("Pass an existing packaged Metronome.app image.")
        jdk = args.jdk_home.expanduser().resolve(strict=True)
        release = dict(line.split("=", 1) for line in (jdk / "release").read_text().splitlines() if "=" in line)
        if release.get("JAVA_VERSION", "").strip('"').split(".")[0] != "21" or "JetBrains" not in release.get("IMPLEMENTOR", ""):
            raise ValueError("Run this smoke test with the configured JetBrains Runtime 21 SDK.")
        java = jdk / "bin/java"
        source = Path(__file__).with_name("AudioPlaybackSmoke.java")
        command = [str(java), "--class-path", packaged_classpath(app), "--source", "21", str(source), str(app)]
        result = subprocess.run(command, capture_output=True, text=True, timeout=28)
        events = []
        for line in result.stdout.splitlines():
            if line.startswith("{"):
                events.append(json.loads(line))
        summaries = [event for event in events if event.get("type") == "summary"]
        passed = result.returncode == 0 and len(summaries) == 1 and summaries[0].get("acceptedSubmissions") == 14
        report = {
            "checkedAtUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "appPath": str(app),
            "runtimePath": str(jdk),
            "checksPassed": passed,
            "scope": "Unsandboxed output submission through packaged production classes and audio. Does not measure speakers, hearing, latency, stereo audibility, route changes, or the signed app sandbox.",
            "events": events,
            "stderr": result.stderr,
        }
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(json.dumps(report, indent=2) + "\n")
        print(json.dumps(report, indent=2))
        return 0 if passed else 1
    except subprocess.TimeoutExpired:
        print("Audio smoke test exceeded its 28-second process limit; the test JVM was terminated.", file=sys.stderr)
        return 2
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"Audio smoke test could not run: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
