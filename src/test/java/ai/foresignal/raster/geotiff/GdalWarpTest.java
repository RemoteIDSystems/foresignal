package ai.foresignal.raster.geotiff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterDecodePool;
import ai.foresignal.raster.RasterPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link GdalWarp} with the process mocked (prompt 054): disabled when {@code gdalwarp} is absent or
 * turned off, probed once, the exact argument list, {@code GDAL_CACHEMAX}, the 120-second timeout, files
 * only under {@code rasters/tmp/} and deleted afterwards, and failures reported.
 */
class GdalWarpTest {

    @TempDir Path data;

    /** A recording runner: the version probe answers {@code probeExit}, a warp writes {@code output}. */
    static final class Recorder implements GdalWarp.ProcessRunner {

        final List<List<String>> commands = new ArrayList<>();
        final List<Map<String, String>> environments = new ArrayList<>();
        final List<Duration> timeouts = new ArrayList<>();
        int probeExit = 0;
        boolean absent;
        GdalWarp.Result warp = new GdalWarp.Result(0, "", false);
        byte[] output = {1, 2, 3};

        @Override
        public GdalWarp.Result run(List<String> command, Map<String, String> environment, Duration timeout)
                throws IOException {
            commands.add(command);
            environments.add(environment);
            timeouts.add(timeout);
            if (absent) {
                throw new IOException("Cannot run program \"gdalwarp\": error=2, No such file or directory");
            }
            if (command.contains("--version")) {
                return new GdalWarp.Result(probeExit, "GDAL 3.8.4, released 2024/02/08", false);
            }
            if (warp.exitCode() == 0 && !warp.timedOut()) {
                Files.write(Path.of(command.get(command.size() - 1)), output);
            }
            return warp;
        }
    }

    private GdalWarp warp(String mode, Recorder recorder) {
        RasterPaths paths = new RasterPaths(data);
        paths.createLayout();
        return new GdalWarp(mode, paths, new RasterDecodePool(1), recorder);
    }

    @Test
    @DisplayName("auto with gdalwarp absent: unavailable, a warp returns empty, probed once")
    void absent() throws Exception {
        Recorder recorder = new Recorder();
        recorder.absent = true;
        GdalWarp gdal = warp("auto", recorder);
        assertThat(gdal.available()).isFalse();
        assertThat(gdal.warpToWgs84(new byte[] {0})).isEmpty();
        assertThat(gdal.available()).isFalse();
        assertThat(recorder.commands).containsExactly(List.of("gdalwarp", "--version"));
        assertThat(recorder.timeouts).containsExactly(Duration.ofSeconds(GdalWarp.PROBE_SECONDS));
    }

    @Test
    @DisplayName("false never runs anything; a failing probe is unavailable too")
    void disabled() throws Exception {
        Recorder recorder = new Recorder();
        assertThat(warp("false", recorder).warpToWgs84(new byte[] {0})).isEmpty();
        assertThat(recorder.commands).isEmpty();
        Recorder failing = new Recorder();
        failing.probeExit = 1;
        assertThat(warp("true", failing).available()).isFalse();
    }

    @Test
    @DisplayName("available: the exact argument list, GDAL_CACHEMAX=256, 120 s, files only in rasters/tmp and deleted")
    void argumentsAndTimeout() throws Exception {
        Recorder recorder = new Recorder();
        GdalWarp gdal = warp("auto", recorder);
        gdal.onReady();
        assertThat(gdal.available()).isTrue();
        byte[] out = gdal.warpToWgs84(new byte[] {9, 9}).orElseThrow();
        assertThat(out).containsExactly(1, 2, 3);
        List<String> command = recorder.commands.get(1);
        assertThat(command.subList(0, 7)).containsExactly("gdalwarp", "-t_srs", "EPSG:4326", "-of", "GTiff", "-co",
                "COMPRESS=DEFLATE");
        Path tmp = data.resolve("rasters").resolve("tmp").toAbsolutePath().normalize();
        Path in = Path.of(command.get(7));
        Path outPath = Path.of(command.get(8));
        assertThat(in.getParent()).isEqualTo(tmp);
        assertThat(outPath.getParent()).isEqualTo(tmp);
        assertThat(recorder.environments.get(1)).containsExactly(Map.entry("GDAL_CACHEMAX", "256"));
        assertThat(recorder.timeouts.get(1)).isEqualTo(Duration.ofSeconds(120));
        assertThat(Files.exists(in)).isFalse();
        assertThat(Files.exists(outPath)).isFalse();
        assertThat(GdalWarp.command(Path.of("/a.tif"), Path.of("/b.tif")))
                .containsExactly("gdalwarp", "-t_srs", "EPSG:4326", "-of", "GTiff", "-co", "COMPRESS=DEFLATE", "/a.tif",
                        "/b.tif");
    }

    @Test
    @DisplayName("a non-zero exit or a timeout is an IOException, and the files are still deleted")
    void failures() throws Exception {
        Recorder recorder = new Recorder();
        recorder.warp = new GdalWarp.Result(1, "ERROR 1: no such CRS", false);
        GdalWarp gdal = warp("auto", recorder);
        assertThatThrownBy(() -> gdal.warpToWgs84(new byte[] {1})).isInstanceOf(IOException.class)
                .hasMessageContaining("exited with 1");
        recorder.warp = new GdalWarp.Result(-1, "", true);
        assertThatThrownBy(() -> gdal.warpToWgs84(new byte[] {1})).hasMessageContaining("timed out after 120 s");
        try (var files = Files.list(data.resolve("rasters").resolve("tmp"))) {
            assertThat(files.filter(p -> p.getFileName().toString().startsWith("gdal-")).toList()).isEmpty();
        }
    }

    @Test
    @DisplayName("the real runner runs an argument list and reports a missing program as an IOException")
    void realRunner() {
        assertThatThrownBy(() -> GdalWarp.exec(List.of("foresignal-no-such-program-054"), Map.of(), Duration.ofSeconds(5)))
                .isInstanceOf(IOException.class);
    }
}
