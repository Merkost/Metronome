import com.merkost.metronome.engine.MetronomePlayerDesktop;
import com.merkost.metronome.model.Beat;
import com.merkost.metronome.model.ClickSound;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioSystem;
import kotlin.Unit;

public final class AudioPlaybackSmoke {
    private static final long DEADLINE_NANOS = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
    private static final AtomicInteger SUBMISSIONS = new AtomicInteger();

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) throw new IllegalArgumentException("Pass the packaged Metronome.app path.");
        Path app = Path.of(arguments[0]).toRealPath();
        URL playerOrigin = MetronomePlayerDesktop.class.getProtectionDomain().getCodeSource().getLocation();
        requirePackagedFile(app, playerOrigin);
        URL audioOrigin = MetronomePlayerDesktop.class.getResource("/audio/origin-manifest.json");
        if (audioOrigin == null) throw new IllegalStateException("The packaged audio origin manifest is missing.");
        requirePackagedFile(app, audioOrigin);
        String digest;
        try (var stream = audioOrigin.openStream()) {
            digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes()));
        }
        String mixer = AudioSystem.getMixer(null).getMixerInfo().getName();
        System.out.println("{\"type\":\"provenance\",\"playerOrigin\":" + quote(playerOrigin.toExternalForm())
                + ",\"audioOrigin\":" + quote(audioOrigin.toExternalForm()) + ",\"audioManifestSha256\":" + quote(digest)
                + ",\"defaultMixer\":" + quote(mixer) + "}");
        MetronomePlayerDesktop player = new MetronomePlayerDesktop();
        List<String> failures = new ArrayList<>();
        try {
            for (ClickSound sound : ClickSound.values()) {
                player.initialize(sound);
                checkPulse(player, sound, Beat.LOW, 1f, 0f, failures);
                checkPulse(player, sound, Beat.HIGH, 0f, 1f, failures);
            }
            CountDownLatch muted = new CountDownLatch(1);
            player.play(Beat.MUTE, 1f, 1f, () -> { muted.countDown(); return Unit.INSTANCE; });
            if (muted.await(100, TimeUnit.MILLISECONDS)) throw new IllegalStateException("A muted beat submitted audio.");
        } finally {
            player.stop();
            player.release();
        }
        CountDownLatch released = new CountDownLatch(1);
        player.play(Beat.LOW, 1f, 1f, () -> { released.countDown(); return Unit.INSTANCE; });
        if (released.await(100, TimeUnit.MILLISECONDS)) throw new IllegalStateException("A released player submitted audio.");
        int expected = ClickSound.values().length * 2;
        System.out.println("{\"type\":\"summary\",\"sounds\":" + ClickSound.values().length + ",\"acceptedSubmissions\":" + SUBMISSIONS.get()
                + ",\"failedSubmissions\":" + failures.size() + ",\"muteSuppressed\":true,\"releaseSuppressedPlayback\":true,\"scope\":\"Production callbacks after PCM acceptance; no speaker, audibility, latency, or sandbox measurement\"}");
        if (SUBMISSIONS.get() != expected || !failures.isEmpty()) throw new IllegalStateException("One or more output submissions failed: " + failures);
    }

    private static void checkPulse(MetronomePlayerDesktop player, ClickSound sound, Beat beat, float left, float right, List<String> failures) throws Exception {
        try {
            submit(player, sound, beat, left, right);
        } catch (IllegalStateException failure) {
            failures.add(failure.getMessage());
            System.out.println("{\"type\":\"failure\",\"sound\":" + quote(sound.name()) + ",\"beat\":" + quote(beat.name()) + ",\"message\":" + quote(failure.getMessage()) + "}");
        } finally {
            player.stop();
        }
    }

    private static void submit(MetronomePlayerDesktop player, ClickSound sound, Beat beat, float left, float right) throws Exception {
        CountDownLatch accepted = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> callbackThread = new AtomicReference<>();
        player.play(beat, left, right, () -> {
            calls.incrementAndGet();
            SUBMISSIONS.incrementAndGet();
            callbackThread.set(Thread.currentThread().getName());
            accepted.countDown();
            return Unit.INSTANCE;
        });
        long remaining = DEADLINE_NANOS - System.nanoTime();
        if (remaining <= 0 || !accepted.await(Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(1500)), TimeUnit.NANOSECONDS)) {
            throw new IllegalStateException("No accepted output submission for " + sound + " " + beat + ". Check the Mac's default output device.");
        }
        if (calls.get() != 1) throw new IllegalStateException("Duplicate submission for " + sound + " " + beat);
        System.out.println("{\"type\":\"submission\",\"sound\":" + quote(sound.name()) + ",\"beat\":" + quote(beat.name())
                + ",\"left\":" + left + ",\"right\":" + right + ",\"callbackThread\":" + quote(callbackThread.get()) + "}");
    }

    private static void requirePackagedFile(Path app, URL origin) throws Exception {
        URL container = origin;
        if ("jar".equals(origin.getProtocol())) container = ((JarURLConnection) origin.openConnection()).getJarFileURL();
        if (!"file".equals(container.getProtocol()) || !Path.of(container.toURI()).toRealPath().startsWith(app)) {
            throw new IllegalStateException("Smoke test code or audio was loaded outside the packaged app: " + origin);
        }
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
}
