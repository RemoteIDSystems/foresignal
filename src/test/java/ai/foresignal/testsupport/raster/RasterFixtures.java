package ai.foresignal.testsupport.raster;

import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;

/** Frames of the {@link SyntheticRasterAdapter} products and file-system helpers for the raster ITs. */
public final class RasterFixtures {

    /** A 4 × 3 latlon grid over central Texas (never at Null Island). */
    public static final GridDefinition GRID = GridDefinition.latLon(4, 3, -100.0, 28.0, -94.0, 34.0);

    private RasterFixtures() {}

    /** Twelve u8 codes: a no-coverage top row, then echoes; {@code shift} varies the content. */
    public static int[] codes(int shift) {
        return new int[] {0, 0, 0, 0, 1, 66 + shift, 86, 106, 1, 1, 126, 2};
    }

    /** A complete observation frame of {@link SyntheticRasterAdapter#OBS} valid at {@code t}. */
    public static FrameInput observation(Instant t, int shift) {
        return FrameInput.builder(SyntheticRasterAdapter.OBS).validTime(t).grid(GRID)
                .bands(DecodedBands.codes(codes(shift))).upstreamKey("synthetic/" + t).build();
    }

    /** A partial observation frame. */
    public static FrameInput partialObservation(Instant t) {
        return FrameInput.builder(SyntheticRasterAdapter.OBS).validTime(t).grid(GRID)
                .bands(DecodedBands.codes(codes(0))).partial(true).build();
    }

    /** Step {@code step} of the forecast run at {@code run}, physical gusts in m/s. */
    public static FrameInput forecast(Instant run, int step) {
        float[] gusts = new float[12];
        for (int i = 0; i < gusts.length; i++) {
            gusts[i] = 5.0f + i + step;
        }
        return FrameInput.builder(SyntheticRasterAdapter.FCST).forecast(run, step).grid(GRID)
                .bands(DecodedBands.physical(gusts)).build();
    }

    /** Deletes {@code dir} and everything under it, if it exists. */
    public static void deleteTree(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
