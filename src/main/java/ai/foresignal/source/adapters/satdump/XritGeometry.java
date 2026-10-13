package ai.foresignal.source.adapters.satdump;

import ai.foresignal.raster.decode.GeosGrids;
import ai.foresignal.source.spi.GridDefinition;
import java.util.Locale;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.MultiPolygon;
import tools.jackson.databind.JsonNode;

/**
 * Maps a SatDump geostationary {@code projection_cfg} to a Foresignal {@code geos} grid (prompt 049,
 * Weather-Imagery-Design §4.5 item 1).
 *
 * <p>The keys are {@code type: geos}, {@code lon0}, {@code sweep_x}, {@code scalar_x},
 * {@code scalar_y}, {@code offset_x}, {@code offset_y}, {@code width}, {@code height} and
 * {@code altitude}. Column {@code i} sits at scan-plane x {@code offset_x + i · scalar_x} and row
 * {@code j} at {@code offset_y + j · scalar_y}, in meters of the PROJ {@code geos} plane (scan angle
 * times {@code altitude}); {@code sweep_x} picks the GOES or Meteosat axis order. When an image is
 * smaller or larger than {@code width × height}, the scalars scale with it. The maths is the CGMS
 * normalized geostationary projection and the GOES-R PUG fixed grid, from the public specifications
 * ({@link GeosGrids}); no SatDump projection code.
 *
 * <p>FY-4 and MSG HRV have no usable projection in SatDump's output and are refused with
 * {@link #FY4_MSG_REASON}.
 */
public final class XritGeometry {

    /** The health reason for FY-4 and MSG HRV (Weather-Imagery-Design §4.3). */
    public static final String FY4_MSG_REASON = "FY-4/MSG HRV has no usable projection.";

    private static final Pattern FY4_OR_MSG = Pattern.compile("^(fy-?4|fengyun-?4|msg|meteosat).*");

    private XritGeometry() {}

    /** A grid could not be built; {@link #getMessage()} is the health reason. */
    public static final class NoProjectionException extends Exception {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public NoProjectionException(String reason) {
            super(reason);
        }
    }

    /**
     * The grid of an image.
     *
     * @param satellite the satellite name (for the FY-4 and MSG refusal)
     * @param channel the channel name
     * @param projection {@code projection_cfg}, or null
     * @param imageWidth the image width in pixels
     * @param imageHeight the image height in pixels
     * @throws NoProjectionException when the configuration is missing, not {@code geos}, or incomplete
     */
    public static GridDefinition grid(String satellite, String channel, JsonNode projection, int imageWidth,
            int imageHeight) throws NoProjectionException {
        String sat = satellite == null ? "" : satellite.trim().toLowerCase(Locale.ROOT);
        boolean fy4OrMsg = FY4_OR_MSG.matcher(sat).matches();
        if (sat.startsWith("fy") || sat.startsWith("fengyun")) {
            // SatDump writes no usable FY-4 projection at all.
            if (fy4OrMsg) {
                throw new NoProjectionException(FY4_MSG_REASON);
            }
        }
        if (projection == null || !"geos".equals(projection.path("type").asString(""))) {
            throw new NoProjectionException(fy4OrMsg ? FY4_MSG_REASON
                    : satellite + " " + channel + " has no geos projection_cfg; the frame was skipped.");
        }
        double lon0 = number(projection, "lon0");
        double scalarX = number(projection, "scalar_x");
        double scalarY = number(projection, "scalar_y");
        double offsetX = number(projection, "offset_x");
        double offsetY = number(projection, "offset_y");
        double altitude = number(projection, "altitude");
        double width = projection.path("width").isNumber() ? projection.path("width").asDouble() : imageWidth;
        double height = projection.path("height").isNumber() ? projection.path("height").asDouble() : imageHeight;
        boolean sweepX = projection.path("sweep_x").asBoolean(false);
        if (!Double.isFinite(lon0) || !Double.isFinite(scalarX) || !Double.isFinite(scalarY) || !Double.isFinite(offsetX)
                || !Double.isFinite(offsetY) || !(altitude > 0) || scalarX == 0 || scalarY == 0 || width <= 0
                || height <= 0) {
            throw new NoProjectionException(fy4OrMsg ? FY4_MSG_REASON
                    : satellite + " " + channel + " has an incomplete geos projection_cfg; the frame was skipped.");
        }
        double sx = scalarX * width / imageWidth;
        double sy = scalarY * height / imageHeight;
        try {
            return GeosGrids.fromScanPlane(imageWidth, imageHeight, lon0, altitude, sweepX, sx, sy, offsetX, offsetY);
        } catch (IllegalArgumentException e) {
            throw new NoProjectionException(satellite + " " + channel + ": " + e.getMessage());
        }
    }

    /**
     * The footprint of a frame: the disc visible from {@code lon0} at {@code altitude}.
     *
     * @param grid a grid from {@link #grid}
     */
    public static MultiPolygon footprint(GridDefinition grid) {
        double lon0 = ((Number) grid.params().get("lon0")).doubleValue();
        double h = ((Number) grid.params().get("h")).doubleValue();
        return GeosGrids.visibleDisc(lon0, h);
    }

    private static double number(JsonNode node, String key) {
        JsonNode v = node.path(key);
        return v.isNumber() ? v.asDouble() : Double.NaN;
    }
}
