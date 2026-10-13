package ai.foresignal.kml;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * The value types {@link KmlWriter} writes (prompt 058). Every string here is upstream or operator text
 * and is escaped by the writer, never by the caller.
 */
public final class KmlModel {

    private KmlModel() {}

    /**
     * One shared {@code Style} per kind.
     *
     * @param id the style id (the kind)
     * @param icon the sprite icon name, or null for Google Earth's default pushpin
     * @param color the KML color {@code aabbggrr}
     */
    public record Style(String id, String icon, String color) {}

    /**
     * One {@code Placemark} of a record.
     *
     * @param name the title
     * @param when {@code TimeStamp/when}: the record's {@code observedAt}, or null
     * @param description plain text ({@code field: value unit} lines)
     * @param style the shared style of its kind, or null
     * @param heading the icon heading in degrees from the kind's {@code rotateField}, or null
     * @param data {@code ExtendedData/Data}: field name to display name and value
     * @param geometry the geometry (SRID 4326), or null
     */
    public record Placemark(String name, Instant when, String description, Style style, Double heading,
            List<Data> data, Geometry geometry) {

        /** Copies the data. */
        public Placemark {
            data = data == null ? List.of() : List.copyOf(data);
        }
    }

    /**
     * One {@code ExtendedData/Data} element.
     *
     * @param name the field name
     * @param displayName the label, or null
     * @param value the value as text
     */
    public record Data(String name, String displayName, String value) {}

    /**
     * A records document.
     *
     * @param name the document name
     * @param description the attributions of the included sources (plain text), or null
     * @param styles the shared styles
     * @param placemarks the placemarks, newest first
     * @param truncated true when the cap was hit
     * @param total the number of matching records (a lower bound when {@code totalExact} is false)
     * @param totalExact false when {@code total} is a lower bound
     */
    public record Records(String name, String description, List<Style> styles, List<Placemark> placemarks,
            boolean truncated, long total, boolean totalExact) {

        /** Copies the lists. */
        public Records {
            styles = List.copyOf(styles);
            placemarks = List.copyOf(placemarks);
        }
    }

    /**
     * One entity's {@code gx:Track}.
     *
     * @param name the entity's title or id
     * @param style the shared style of its kind, or null
     * @param whens the times, oldest first
     * @param coords {@code [lon, lat, alt]} parallel to {@code whens}
     * @param altitudeMode {@code absolute}, {@code relativeToGround} or {@code clampToGround}
     * @param truncated true when more than the per-track point cap existed (the newest are kept)
     * @param data extra {@code ExtendedData}
     */
    public record Track(String name, Style style, List<Instant> whens, List<double[]> coords, String altitudeMode,
            boolean truncated, List<Data> data) {

        /** Copies the lists. */
        public Track {
            whens = List.copyOf(whens);
            coords = List.copyOf(coords);
            data = data == null ? List.of() : List.copyOf(data);
        }
    }

    /**
     * A tracks document.
     *
     * @param name the document name
     * @param description the attributions of the included sources, or null
     * @param styles the shared styles
     * @param tracks the tracks
     * @param entitiesTruncated true when more entities matched than the entity cap
     */
    public record Tracks(String name, String description, List<Style> styles, List<Track> tracks,
            boolean entitiesTruncated) {

        /** Copies the lists. */
        public Tracks {
            styles = List.copyOf(styles);
            tracks = List.copyOf(tracks);
        }
    }

    /**
     * One {@code NetworkLink}.
     *
     * @param name the layer name
     * @param href the records URL
     */
    public record Link(String name, String href) {}

    /**
     * A KMZ: {@code doc.kml} plus the icons it references.
     *
     * @param kml the document text, whose icon hrefs are {@code icons/<name>.png}
     * @param icons icon name to PNG bytes
     */
    public record Kmz(String kml, Map<String, byte[]> icons) {}
}
