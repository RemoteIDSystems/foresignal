package ai.foresignal.source.adapters.worldportindex;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.ReferenceSupport;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The NGA World Port Index public JSON (prompt 094): {@code {"ports":[…]}}, 2,951 ports.
 *
 * <p>Geometry uses the decimal {@code ycoord}/{@code xcoord}; the DMS {@code latitude}/{@code longitude}
 * strings are ignored. Every port also yields a buffered {@code port-area}: a 64-vertex geodesic circle whose
 * radius is set by {@code harborSize} (V 2 km, S 3 km, M 6 km, L 10 km; unknown 3 km).
 */
public final class WpiParser {

    /** The port kind. */
    public static final String PORT_KIND = "port";

    /** The buffered port-area kind. */
    public static final String AREA_KIND = "port-area";

    /** Vertices of a port-area buffer. */
    public static final int BUFFER_VERTICES = 64;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private WpiParser() {}

    /** Parses the whole document into port and port-area records. */
    public static List<NormalizedRecord> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("World Port Index document could not be parsed",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8), e);
        }
        JsonNode ports = root == null ? null : root.get("ports");
        if (ports == null || !ports.isArray()) {
            throw new UpstreamFormatException("World Port Index document has no ports array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>(ports.size() * 2);
        for (JsonNode port : ports) {
            String number = text(port, "portNumber");
            if (number == null) {
                number = text(port, "port_number");
            }
            if (number == null) {
                continue;
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
            out.add(b.build());

            double bufferKm = bufferKm(harborSize);
            if (valid(lat, lon)) {
                out.add(NormalizedRecord.builder("wpi-area:" + number, AREA_KIND)
                        .geometry(ReferenceSupport.geodesicCircle(lon, lat, bufferKm, BUFFER_VERTICES))
                        .put("bufferKm", bufferKm)
                        .put("portNumber", number)
                        .build());
            }
        }
        return out;
    }

    /** The buffer radius, kilometres, by harbor size code. */
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
