package ai.foresignal.source.adapters.worldportindex;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.ReferenceStreams;
import ai.foresignal.source.support.ReferenceSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;

/**
 * The NGA World Port Index public JSON (prompt 094): {@code {"ports":[…]}}, 2,951 ports.
 *
 * <p>Geometry uses the decimal {@code ycoord}/{@code xcoord}; the DMS {@code latitude}/{@code longitude}
 * strings are ignored. Every port also yields a buffered {@code port-area}: a 64-vertex geodesic circle whose
 * radius is set by {@code harborSize} (V 2 km, S 3 km, M 6 km, L 10 km; unknown 3 km), split at the
 * antimeridian when it crosses it. The ports are streamed one at a time ({@link ReferenceStreams}).
 */
public final class WpiParser {

    /** The port kind. */
    public static final String PORT_KIND = "port";

    /** The buffered port-area kind. */
    public static final String AREA_KIND = "port-area";

    /** Vertices of a port-area buffer. */
    public static final int BUFFER_VERTICES = 64;

    private WpiParser() {}

    /** Parses the whole document into port and port-area records, as a list (tests and small documents). */
    public static List<NormalizedRecord> parse(byte[] body) {
        List<NormalizedRecord> out = new ArrayList<>();
        parse(body, out::add);
        return out;
    }

    /**
     * Streams the document's ports into {@code sink}: each port record, then its port-area when it has a
     * position.
     *
     * @throws UpstreamFormatException when the document does not parse or has no ports array
     */
    public static ReferenceSupport.ParseCounts parse(byte[] body, Consumer<NormalizedRecord> sink) {
        int[] counts = new int[3];
        ReferenceStreams.forEachElement(body, "World Port Index document", port -> {
            int emitted = map(port, sink);
            if (emitted == 0) {
                counts[1]++;
            }
            counts[0] += emitted;
        }, "ports");
        return new ReferenceSupport.ParseCounts(counts[0], counts[1], counts[2]);
    }

    /** Hands one port's records to {@code sink}; the number handed over (0 when the port has no number). */
    private static int map(JsonNode port, Consumer<NormalizedRecord> sink) {
        String number = text(port, "portNumber");
        if (number == null) {
            number = text(port, "port_number");
        }
        if (number == null) {
            return 0;
        }
        Double lat = number(port.get("ycoord"));
        Double lon = number(port.get("xcoord"));
        String harborSize = text(port, "harborSize");
        NormalizedRecord.Builder b = NormalizedRecord.builder("wpi:" + number, PORT_KIND)
                .put("portName", text(port, "portName"))
                .put("countryCode", text(port, "countryCode"))
                .put("countryName", text(port, "countryName"))
                .put("unloCode", text(port, "unloCode"))
                .put("regionName", text(port, "regionName"))
                .put("harborSize", harborSize)
                .put("harborType", text(port, "harborType"))
                .put("harborUse", text(port, "harborUse"))
                .put("shelter", text(port, "shelter"))
                .put("channelDepthM", number(first(port, "channelDepthM", "channelDepth")))
                .put("anchorageDepthM", number(first(port, "anchorageDepthM", "anchorageDepth")))
                .put("maxVesselLengthM", number(first(port, "maxVesselLengthM", "maxVesselLength")))
                .put("maxVesselDraftM", number(first(port, "maxVesselDraftM", "maxVesselDraft")))
                .put("alternateName", text(port, "alternateName"));
        if (valid(lat, lon)) {
            b.point(lon, lat);
        }
        sink.accept(b.build());

        double bufferKm = bufferKm(harborSize);
        if (valid(lat, lon)) {
            sink.accept(NormalizedRecord.builder("wpi-area:" + number, AREA_KIND)
                    .geometry(ReferenceSupport.geodesicArea(lon, lat, bufferKm, BUFFER_VERTICES))
                    .put("bufferKm", bufferKm)
                    .put("portNumber", number)
                    .build());
            return 2;
        }
        return 1;
    }

    /** The buffer radius, kilometers, by harbor size code. */
    public static double bufferKm(String harborSize) {
        if (harborSize == null || harborSize.isBlank()) {
            return 3.0;
        }
        return switch (Character.toUpperCase(harborSize.strip().charAt(0))) {
            case 'V' -> 2.0;
            case 'S' -> 3.0;
            case 'M' -> 6.0;
            case 'L' -> 10.0;
            default -> 3.0;
        };
    }

    private static JsonNode first(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    private static boolean valid(Double lat, Double lon) {
        return lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180;
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        if (node.isNumber()) {
            double value = node.doubleValue();
            return Double.isFinite(value) ? value : null;
        }
        String s = node.asString();
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            double value = Double.parseDouble(s.strip());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isContainer()) {
            return null;
        }
        String s = value.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }
}
