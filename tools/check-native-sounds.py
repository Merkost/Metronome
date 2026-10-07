import hashlib
import json
import math
import struct
import sys
import wave
from pathlib import Path

root = Path(__file__).resolve().parents[1]
records = []
for sound in ["soft", "rim", "clave", "studio"]:
    for variant in ["", "_accent"]:
        name = sound + variant + ".wav"
        source = root / "web/design-review/public/audio" / name.replace("_accent", "-accent")
        expected = source.read_bytes()
        paths = [root / folder / name for folder in ["shared/src/androidMain/res/raw", "iosApp/iosApp/Resources", "shared/src/wasmJsMain/resources/sounds"]]
        for path in paths:
            assert path.read_bytes() == expected, str(path)
        with wave.open(str(paths[0]), "rb") as audio:
            channels, width, rate, frames = audio.getnchannels(), audio.getsampwidth(), audio.getframerate(), audio.getnframes()
            assert (channels, width, rate) == (1, 2, 48000), name
            samples = struct.unpack("<" + "h" * frames, audio.readframes(frames))
        peak = max(abs(sample) for sample in samples) / 32768
        rms = math.sqrt(sum((sample / 32768) ** 2 for sample in samples) / frames)
        onset = next(index for index, sample in enumerate(samples) if abs(sample) >= 33) / rate * 1000
        duration = frames / rate * 1000
        clipped = sum(abs(sample) >= 32767 for sample in samples)
        assert duration <= 100 and onset <= 1 and clipped == 0 and samples[-1] == 0 and peak < .9, name
        records.append({"resource": name, "channels": channels, "bits": width * 8, "sampleRateHz": rate, "durationMs": round(duration, 3), "onsetMs": round(onset, 3), "peakDbfs": round(20 * math.log10(peak), 3), "rmsDbfs": round(20 * math.log10(rms), 3), "clippedSamples": clipped, "lastSample": samples[-1], "sha256": hashlib.sha256(expected).hexdigest(), "identicalOnAllPlatforms": True})
result = {"newSounds": 4, "resourcesPerPlatform": 8, "normalization": "Original peak-normalized design sound sketches preserved byte-for-byte", "resources": records, "limits": "Static PCM proof; output volume, scheduling and Bluetooth latency still require device acceptance"}
serialized = json.dumps(result, indent=2) + "\n"
if len(sys.argv) > 1:
    target = Path(sys.argv[1])
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(serialized)
print(serialized, end="")
