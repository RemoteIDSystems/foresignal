package ai.foresignal.raster.geotiff;

import ai.foresignal.raster.RasterDecodePool;
import ai.foresignal.raster.RasterPaths;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The optional {@code gdalwarp} path for GeoTIFFs the built-in reader refuses: a CRS other than
 * EPSG:4326 or EPSG:3857, a rotated transformation, or BigTIFF (prompt 054, Weather-Imagery-Design §3
 * "GDAL" and §4.5 item 4).
 *
 * <p>GDAL is a separate program, installed in the image only when it is built with
 * {@code --build-arg WITH_GDAL=true} ({@code gdal-bin} only: no JNI, no AGPL drivers). It is run with
 * {@link ProcessBuilder} and an argument list (never a shell):
 * {@code gdalwarp -t_srs EPSG:4326 -of GTiff -co COMPRESS=DEFLATE <in> <out>}, with
 * {@code GDAL_CACHEMAX=256}, a {@value #TIMEOUT_SECONDS}-second timeout, on the raster decode pool, and
 * with its input and output only under {@code rasters/tmp/}.
 *
 * <p>{@code foresignal.raster.gdal.enabled}: {@code auto} (the default) runs {@code gdalwarp --version}
 * once at startup and uses GDAL when that succeeds; {@code true} does the same but warns when GDAL is
 * missing; {@code false} never runs it.
 */
@Component
public class GdalWarp {

    private static final Logger log = LoggerFactory.getLogger(GdalWarp.class);

    /** The timeout of one warp. */
    public static final long TIMEOUT_SECONDS = 120;

    /** The timeout of the version probe. */
    public static final long PROBE_SECONDS = 10;

    /** The GDAL block cache, MB. */
    public static final String CACHE_MAX = "256";

    /** The largest warped output read back. */
    public static final long MAX_OUTPUT_BYTES = 512L * 1024 * 1024;

    /** The program. */
    public static final String PROGRAM = "gdalwarp";

    /**
     * What a finished process returned.
     *
     * @param exitCode the exit code, or -1 after a timeout
     * @param output the combined standard output and error, at most 8 KB
     * @param timedOut true when it was killed at the timeout
     */
    public record Result(int exitCode, String output, boolean timedOut) {}

    /** Runs one process; replaced in tests. */
    @FunctionalInterface
    public interface ProcessRunner {

        /**
         * Runs {@code command} with {@code environment} added, killing it after {@code timeout}.
         *
         * @throws IOException when it cannot be started
         * @throws InterruptedException when interrupted while waiting
         */
        Result run(List<String> command, Map<String, String> environment, Duration timeout)
                throws IOException, InterruptedException;
    }

    private final String mode;
    private final RasterPaths paths;
    private final RasterDecodePool pool;
    private final ProcessRunner runner;
    private volatile Boolean available;

    /**
     * Creates the warp.
     *
     * @param mode {@code auto}, {@code true} or {@code false}
     */
    @Autowired
    public GdalWarp(@Value("${foresignal.raster.gdal.enabled:auto}") String mode, RasterPaths paths,
            RasterDecodePool pool) {
        this(mode, paths, pool, GdalWarp::exec);
    }

    /** Creates the warp with a process runner (tests). */
    public GdalWarp(String mode, RasterPaths paths, RasterDecodePool pool, ProcessRunner runner) {
        this.mode = mode == null ? "auto" : mode.strip().toLowerCase(Locale.ROOT);
        this.paths = paths;
        this.pool = pool;
        this.runner = runner;
    }

    /** Probes for {@code gdalwarp} once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        available();
    }

    /** True when GDAL is enabled and {@code gdalwarp --version} answered; probed once. */
    public boolean available() {
        Boolean known = available;
        if (known != null) {
            return known;
        }
        synchronized (this) {
            if (available == null) {
                available = probe();
            }
            return available;
        }
    }

    private boolean probe() {
        if (mode.equals("false")) {
            return false;
        }
        try {
            Result result = runner.run(List.of(PROGRAM, "--version"), Map.of(), Duration.ofSeconds(PROBE_SECONDS));
            if (result.exitCode() == 0) {
                log.info("GDAL is available for exotic GeoTIFFs: {}", result.output().strip());
                return true;
            }
            report("gdalwarp --version exited with " + result.exitCode());
        } catch (IOException e) {
            report("gdalwarp is not installed (" + e.getMessage() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private void report(String why) {
        if (mode.equals("true")) {
            log.warn("foresignal.raster.gdal.enabled is true but {}; build the image with WITH_GDAL=true", why);
        } else {
            log.debug("GDAL not used: {}", why);
        }
    }

    /** The argument list of one warp. */
    public static List<String> command(Path input, Path output) {
        return List.of(PROGRAM, "-t_srs", "EPSG:4326", "-of", "GTiff", "-co", "COMPRESS=DEFLATE", input.toString(),
                output.toString());
    }

    /**
     * Warps a GeoTIFF to EPSG:4326.
     *
     * @param geotiff the file's bytes
     * @return the warped GeoTIFF, or empty when GDAL is unavailable
     * @throws IOException when the warp fails or times out
     */
    public Optional<byte[]> warpToWgs84(byte[] geotiff) throws IOException {
        if (!available()) {
            return Optional.empty();
        }
        Path tmp = paths.tmp().toAbsolutePath().normalize();
        String id = UUID.randomUUID().toString();
        Path input = tmp.resolve("gdal-" + id + "-in.tif");
        Path output = tmp.resolve("gdal-" + id + "-out.tif");
        try {
            Files.write(input, geotiff);
            Result result;
            try {
                result = pool.run(() -> runner.run(command(input, output), Map.of("GDAL_CACHEMAX", CACHE_MAX),
                        Duration.ofSeconds(TIMEOUT_SECONDS)));
            } catch (IOException e) {
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while running gdalwarp", e);
            } catch (Exception e) {
                throw new IOException("gdalwarp could not run: " + e.getMessage(), e);
            }
            if (result.timedOut()) {
                throw new IOException("gdalwarp timed out after " + TIMEOUT_SECONDS + " s");
            }
            if (result.exitCode() != 0) {
                throw new IOException("gdalwarp exited with " + result.exitCode() + ": " + result.output().strip());
            }
            if (!Files.isRegularFile(output) || Files.size(output) > MAX_OUTPUT_BYTES) {
                throw new IOException("gdalwarp wrote no usable output");
            }
            return Optional.of(Files.readAllBytes(output));
        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
        }
    }

    /** Runs a real process with an argument list, draining its output, killing it at the timeout. */
    static Result exec(List<String> command, Map<String, String> environment, Duration timeout)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().putAll(environment);
        Process process = builder.start();
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        Thread drain = Thread.ofVirtual().start(() -> {
            try (InputStream in = process.getInputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) > 0) {
                    synchronized (captured) {
                        if (captured.size() < 8192) {
                            captured.write(buffer, 0, Math.min(n, 8192 - captured.size()));
                        }
                    }
                }
            } catch (IOException e) {
                // The process ended or was killed.
            }
        });
        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }
        drain.join(1000);
        String output;
        synchronized (captured) {
            output = captured.toString(StandardCharsets.UTF_8);
        }
        return finished ? new Result(process.exitValue(), output, false) : new Result(-1, output, true);
    }
}
