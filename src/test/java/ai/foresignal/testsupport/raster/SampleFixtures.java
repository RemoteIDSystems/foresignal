package ai.foresignal.testsupport.raster;

import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.EncodedFrame;
import ai.foresignal.raster.FrameEncoder;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Synthetic frames for the prompt 052 sampling and derived-weather tests: an MRMS-like reflectivity product
 * (u8, 0.5 dBZ steps from −33, code 0 = no coverage, code 1 = no echo), a bilinear scalar and a u/v wind
 * field, on small lat/lon grids, built through the real {@link FrameEncoder} so the views decode exactly what
 * the pipeline would store.
 */
public final class SampleFixtures {

    /** A 0.01° grid of 200 × 200 cells over central Texas (west −99, north 31.5). */
    public static final GridDefinition TEXAS = GridDefinition.latLon(200, 200, -99.0, 29.5, -97.0, 31.5);

    private SampleFixtures() {}

    /** The MRMS-like reflectivity spec (the synthetic observation product of prompt 042). */
    public static ProductSpec reflectivity() {
        return SyntheticRasterAdapter.observationSpec();
    }

    /** The reflectivity spec under another id (for example {@code mrms-reflectivity}). */
    public static ProductSpec reflectivity(String id) {
        ProductSpec s = reflectivity();
        return new ProductSpec(id, s.title(), s.kind(), s.category(), s.units(), s.dtype(), s.scale(), s.offset(),
                s.nodata(), s.codes(), s.bands(), s.styles(), s.defaultStyle(), s.defaultOpacity(), s.temporal(),
                s.spatial(), s.retention(), s.attribution(), s.coverageMeaning(), s.legendSummary(), s.resample());
    }

    /** A bilinear i16 scalar (gusts, m/s, 0.01 steps), as a forecast product. */
    public static ProductSpec gusts(String id) {
        return ProductSpec.builder(id).title("Gusts").category("model").units("m/s").dtype(RasterDtype.I16)
                .scaleOffset(0.01, 0.0).nodata(-32768.0).defaultStyle("wind-speed@1")
                .temporal(ProductSpec.Temporal.forecast(Duration.ofHours(1), Duration.ofHours(3)))
                .spatial(new ProductSpec.Spatial("CONUS", 6, 0)).retention(ProductSpec.RetentionLadder.runs(2))
                .attribution(new ProductSpec.Attribution("Synthetic model", null, null, "US-PD", null, false))
                .resample(ProductSpec.RESAMPLE_BILINEAR).build();
    }

    /** A u/v wind field ({@code kind=field}, two i16 bands of 0.01 m/s, bilinear). */
    public static ProductSpec wind(String id) {
        return ProductSpec.builder(id).title("Wind").kind(ProductSpec.KIND_FIELD).category("model").units("m/s")
                .dtype(RasterDtype.I16).scaleOffset(0.01, 0.0).nodata(-32768.0).bands("u", "v")
                .defaultStyle("wind-speed@1")
                .temporal(ProductSpec.Temporal.forecast(Duration.ofHours(3), Duration.ofHours(6)))
                .spatial(new ProductSpec.Spatial("global", 4, 0)).retention(ProductSpec.RetentionLadder.runs(2))
                .attribution(new ProductSpec.Attribution("Synthetic model", null, null, "US-PD", null, false))
                .resample(ProductSpec.RESAMPLE_BILINEAR).build();
    }

    /** The decoded frame of raw u8 codes. */
    public static FrameView codes(ProductSpec spec, GridDefinition grid, int[] codes, RasterFrameRow row) {
        EncodedFrame encoded = FrameEncoder.encode(spec, grid, DecodedBands.codes(codes), false);
        return new FrameView(spec, row, GridDef.of(grid), new ElgCodec.Decoded(encoded.header(), encoded.decoded()));
    }

    /** The decoded frame of physical values (one band per array). */
    public static FrameView physical(ProductSpec spec, GridDefinition grid, RasterFrameRow row, float[]... bands) {
        EncodedFrame encoded = FrameEncoder.encode(spec, grid, DecodedBands.physical(bands), false);
        return new FrameView(spec, row, GridDef.of(grid), new ElgCodec.Decoded(encoded.header(), encoded.decoded()));
    }

    /** The u8 code of a reflectivity in dBZ. */
    public static int dbz(double value) {
        return (int) Math.round((value + 33.0) / 0.5);
    }

    /**
     * Reflectivity codes on {@code grid}: "no echo" everywhere, and a disc of {@code coreDbz} of radius
     * {@code discKm} centered {@code km} east of {@code (lon, lat)}.
     */
    public static int[] echoes(GridDefinition grid, double lon, double lat, double km, double discKm, double coreDbz) {
        GridDef g = GridDef.of(grid);
        int[] codes = new int[grid.nx() * grid.ny()];
        double centerLon = lon + km / (111.32 * Math.cos(Math.toRadians(lat)));
        for (int r = 0; r < grid.ny(); r++) {
            for (int c = 0; c < grid.nx(); c++) {
                double[] ll = g.indexToLonLat(c, r);
                double d = ai.foresignal.source.spi.Geo.haversineKm(lat, centerLon, ll[1], ll[0]);
                codes[r * grid.nx() + c] = d <= discKm ? dbz(coreDbz) : 1;
            }
        }
        return codes;
    }

    /** Codes that are all "no coverage" (MRMS code 0). */
    public static int[] noCoverage(GridDefinition grid) {
        return new int[grid.nx() * grid.ny()];
    }

    /** A frame row with the given times (for views that need keys and valid times). */
    public static RasterFrameRow row(String productId, Instant validTime, Instant run, Integer step) {
        return new RasterFrameRow(productId, run == null ? "o" + validTime.toEpochMilli() : "f" + step, validTime, run,
                step, null, null, "grid", "frames/x.fsg", 1, "0123456789abcdef0123456789abcdef", "u8", 1, Map.of(),
                null, RasterFrameRow.COMPLETE, false, run != null, validTime, null);
    }
}
