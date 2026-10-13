package ai.foresignal.raster.render;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Specs of the prompt 055 static-map unit tests: a box around Austin, TX, a synthetic reflectivity frame
 * (bands of 20, 35 and 50 dBZ, drawn by a {@link RasterRenderService} stand-in so no database or frame file
 * is needed), a warning polygon and a place with range rings. {@code MapRendererTest} compares renders of
 * these specs with the golden PNGs under {@code src/test/resources/maps/}.
 */
public final class MapFixtures {

    /** The frame's valid time. */
    public static final Instant T = Instant.parse("2026-10-12T21:04:00Z");

    /** The map box around Austin. */
    public static final double[] BOX = {-98.6, 29.6, -96.9, 31.0};

    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private MapFixtures() {}

    /** The reflectivity product (the shared render fixture). */
    public static ProductSpec reflectivity() {
        return RenderFixtures.reflectivity();
    }

    /** The {@code nws-reflectivity@1} style, resolved with an explicit version. */
    public static StyleRegistry.Resolved reflectivityStyle() {
        return StyleRegistry.shipped().resolve("nws-reflectivity@1");
    }

    /** A complete frame row of the reflectivity product at {@link #T}. */
    public static RasterFrameRow frame() {
        return new RasterFrameRow("test-refl", "20261012T210400Z", T, null, null, null, null, "grid", "x.fsg", 1,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", "u8", 1, null, null,
                RasterFrameRow.COMPLETE, false, false, T, null);
    }

    /**
     * A stand-in renderer that draws three vertical bands of 20, 35 and 50 dBZ over the middle of the map
     * through the real compiled style, and records how often it was asked.
     */
    public static final class BandRenderer extends RasterRenderService {

        /** Draws requested so far. */
        public int calls;

        /** Creates the stand-in (no stores: only {@link #drawDirect} is used). */
        public BandRenderer() {
            super(null, null, StyleRegistry.shipped(), null, null, null);
        }

        @Override
        public TileRenderer.Drawn drawDirect(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style,
                ImageRequest request) {
            calls++;
            CompiledStyle compiled = CompiledStyle.compile(style.style(), spec);
            int w = request.width();
            int h = request.height();
            byte[] pixels = new byte[w * h];
            int[] bands = {compiled.indexOfValue(20), compiled.indexOfValue(35), compiled.indexOfValue(50)};
            for (int y = h / 4; y < h * 3 / 4; y++) {
                for (int x = w / 4; x < w * 3 / 4; x++) {
                    pixels[y * w + x] = (byte) bands[Math.min(2, (x - w / 4) * 3 / (w / 2))];
                }
            }
            return new TileRenderer.Drawn(w, h, pixels, null, compiled, false);
        }
    }

    /** The warning polygon east of Austin. */
    public static Geometry warning() {
        return GEOMETRY.createPolygon(new Coordinate[] {new Coordinate(-97.6, 30.1), new Coordinate(-97.2, 30.1),
            new Coordinate(-97.2, 30.5), new Coordinate(-97.6, 30.5), new Coordinate(-97.6, 30.1)});
    }

    /** The full spec: one layer, the polygon, the Austin place with 25 and 50 km rings. */
    public static MapSpec full(boolean dark) {
        return new MapSpec(BOX, 800, 600,
                List.of(new MapSpec.Layer(reflectivity(), reflectivityStyle(), frame(), MapSpec.FrameStatus.OK, null)),
                new MapSpec.Alert(warning(), "warning", "Severe storm"),
                new MapSpec.Place("austin", "Austin", -97.74, 30.27), List.of(25.0, 50.0), dark, false,
                ZoneId.of("America/Chicago"));
    }

    /** A spec whose only product has no frame. */
    public static MapSpec unavailable() {
        return new MapSpec(BOX, 800, 600,
                List.of(new MapSpec.Layer(reflectivity(), reflectivityStyle(), null, MapSpec.FrameStatus.UNAVAILABLE,
                        "product test-refl has no frame at or before 2026-10-12T21:04:00Z")),
                null, null, List.of(), false, false, ZoneId.of("UTC"));
    }

    /** A renderer over the real basemap and the band stand-in. */
    public static MapRenderer renderer(BandRenderer bands) {
        return new MapRenderer(new BasemapStore(), bands, new OperatorBasemap(null, null, null), null);
    }
}
