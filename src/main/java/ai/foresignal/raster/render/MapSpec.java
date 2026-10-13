package ai.foresignal.raster.render;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import org.locationtech.jts.geom.Geometry;

/**
 * Everything {@link MapRenderer} draws, already resolved (prompt 055): the box, the size, the product
 * frames chosen for the requested time, the alert geometry, the place and its range rings, the theme and
 * the units. {@link StaticMapService} builds it from a request (a {@code bbox}, or a center and a radius;
 * products and styles; {@code time}; {@code alert}; {@code place}).
 *
 * @param bbox the requested box {@code w,s,e,n} in WGS84 degrees ({@code w > e} crosses ±180°); the
 *     renderer snaps it once and widens it to the map area's aspect
 * @param width the image width, pixels (at most {@value #MAX_WIDTH})
 * @param height the image height, pixels
 * @param layers the product overlays, drawn in order
 * @param alert the alert or record geometry, or null
 * @param place the place marker, or null
 * @param rangeRingsKm the range rings around the place (or the map center when there is no place), km
 * @param dark the dark theme (else light)
 * @param imperial imperial units for the scale bar, rings and legends
 * @param zone the time zone of the local times in the header (the place's), UTC for none
 */
public record MapSpec(
        double[] bbox,
        int width,
        int height,
        List<Layer> layers,
        Alert alert,
        Place place,
        List<Double> rangeRingsKm,
        boolean dark,
        boolean imperial,
        ZoneId zone) {

    /** The widest map, pixels. */
    public static final int MAX_WIDTH = 1280;

    /** The default height as a fraction of the width. */
    public static final double DEFAULT_ASPECT = 0.75;

    /** Requires a box and a sane size; copies the lists. */
    public MapSpec {
        Objects.requireNonNull(bbox, "bbox");
        if (bbox.length != 4) {
            throw new IllegalArgumentException("bbox must have four numbers");
        }
        bbox = bbox.clone();
        if (width < 16 || width > MAX_WIDTH || height < 16 || height > MAX_WIDTH) {
            throw new IllegalArgumentException("map size " + width + "x" + height + " is out of range");
        }
        layers = layers == null ? List.of() : List.copyOf(layers);
        rangeRingsKm = rangeRingsKm == null ? List.of() : List.copyOf(rangeRingsKm);
        zone = zone == null ? ZoneOffset.UTC : zone;
    }

    /** The default height of a map {@code width} wide. */
    public static int defaultHeight(int width) {
        return (int) Math.round(width * DEFAULT_ASPECT);
    }

    /** A copy at another size (the 600 px downgrade of an attachment). */
    public MapSpec resized(int newWidth, int newHeight) {
        return new MapSpec(bbox, newWidth, newHeight, layers, alert, place, rangeRingsKm, dark, imperial, zone);
    }

    /** The status of a layer's frame. */
    public enum FrameStatus {
        /** A current frame. */
        OK,
        /** A frame older than the product's maximum gap from the requested time. */
        STALE,
        /** No usable frame. */
        UNAVAILABLE
    }

    /**
     * One product overlay.
     *
     * @param spec the product
     * @param style the style it is drawn with
     * @param frame the chosen frame, or null when {@link FrameStatus#UNAVAILABLE}
     * @param status the frame status
     * @param reason why the frame is stale or unavailable, or null
     */
    public record Layer(ProductSpec spec, StyleRegistry.Resolved style, RasterFrameRow frame, FrameStatus status,
            String reason) {

        /** Requires a product, a style and a status; a frame unless unavailable. */
        public Layer {
            Objects.requireNonNull(spec, "spec");
            Objects.requireNonNull(style, "style");
            Objects.requireNonNull(status, "status");
            if (frame == null && status != FrameStatus.UNAVAILABLE) {
                throw new IllegalArgumentException("a layer without a frame must be unavailable");
            }
        }

        /** The short display name: the product title, else its id. */
        public String name() {
            return spec.title() == null || spec.title().isBlank() ? spec.id() : spec.title();
        }
    }

    /**
     * The alert or record geometry.
     *
     * @param geometry WGS84 geometry
     * @param severity {@code info}, {@code warning} or {@code critical} (stroke and fill color)
     * @param label a short label, or null
     */
    public record Alert(Geometry geometry, String severity, String label) {

        /** Requires a geometry. */
        public Alert {
            Objects.requireNonNull(geometry, "geometry");
            severity = severity == null ? "info" : severity;
        }
    }

    /**
     * A place marker.
     *
     * @param id the place id
     * @param name the display name
     * @param lon longitude
     * @param lat latitude
     */
    public record Place(String id, String name, double lon, double lat) {}
}
