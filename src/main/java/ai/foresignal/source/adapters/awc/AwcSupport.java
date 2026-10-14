package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * What every Aviation Weather Center source shares (prompt 087; the station-list and cache-file logic moved here from
 * prompt 047's {@code awc-metars}): the base URL, the station scope and allowlist, the conditional cache-file GET and
 * gzip inflation, URL building, JSON and GeoJSON reading, and the field-spec helpers. Not an adapter.
 *
 * <p><strong>AWC's published limits</strong> (aviationweather.gov/data/api, verified 2026-10-14): every client is
 * rate-limited to <strong>100 requests per minute</strong>, asked to wait between consecutive requests, to keep to
 * about one request per minute per endpoint and thread, and to set a custom User-Agent (Foresignal's
 * {@code UpstreamClient} always sends its own). A 429 carries the standard backoff, honoring {@code Retry-After}. Every
 * AWC source is serial and polls no faster than {@link #MIN_INTERVAL}, and the default per-minute budgets of all seven
 * AWC sources together ({@link #BUDGET_SUM_PER_MINUTE}) stay far below the ceiling.
 */
public final class AwcSupport {

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://aviationweather.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}, shared by every AWC source (prompt 047's property). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.awc.base-url";

    /** AWC's per-client ceiling, requests per minute. */
    public static final int RATE_LIMIT_PER_MINUTE = 100;

    /** The shortest poll interval of a prompt 087 AWC source. */
    public static final Duration MIN_INTERVAL = Duration.ofMinutes(2);

    /**
     * The default per-minute budgets of the AWC sources added up: {@code awc-metars} 2, {@code awc-sigmets} 3,
     * {@code awc-gairmets} 1, {@code awc-cwas} 1, {@code awc-pireps} 1, {@code awc-tafs} 2.
     */
    public static final int BUDGET_SUM_PER_MINUTE = 10;

    /** The attribution of every AWC source. */
    public static final String ATTRIBUTION = "NOAA/NWS Aviation Weather Center";

    /** The documentation page. */
    public static final String HOMEPAGE = "https://aviationweather.gov/data/api/";

    /** The terms. */
    public static final String TERMS_URL = "https://www.weather.gov/disclaimer";

    /** The license of every AWC source: US Government work. */
    public static final String LICENSE = "US-PD";

    /** The station scopes. */
    public static final List<String> SCOPES = List.of("aoi", "conus", "global");

    /** The conterminous US box used by {@code scope=conus}, {@code [minLon, minLat, maxLon, maxLat]}. */
    static final double[] CONUS = {-125.0, 24.0, -66.5, 50.0};

    /** The AOI used when no AOI service is wired (unit tests): the Texas box. */
    static final double[] DEFAULT_ENVELOPE = {-106.65, 25.84, -93.51, 36.50};

    /** The largest inflated cache file accepted. */
    static final long MAX_INFLATED_BYTES = 64L * 1024 * 1024;

    /** The JSON mapper of AWC documents. */
    static final JsonMapper JSON = JsonMapper.builder().build();

    private static final GeoJsonReader GEOJSON = new GeoJsonReader();

    private AwcSupport() {}

    /**
     * The base URL: {@code override} without a trailing slash, or {@link #DEFAULT_BASE_URL} when blank.
     *
     * @param override the value of {@value #BASE_URL_PROPERTY}
     */
    public static String baseUrl(String override) {
        String chosen = override == null || override.isBlank() ? DEFAULT_BASE_URL : override.trim();
        return chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    /**
     * {@code base + path} with the query parameters URL-encoded in order; null values are left out.
     *
     * @param base the base URL from {@link #baseUrl}
     * @param path the path, starting with a slash
     * @param query parameter names to values, in order
     */
    public static URI uri(String base, String path, Map<String, ?> query) {
        StringBuilder url = new StringBuilder(base).append(path);
        char separator = path.contains("?") ? '&' : '?';
        for (Map.Entry<String, ?> e : query.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            url.append(separator).append(encode(e.getKey())).append('=').append(encode(String.valueOf(e.getValue())));
            separator = '&';
        }
        return URI.create(url.toString());
    }

    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * AWC's {@code bbox} parameter, {@code lat0,lon0,lat1,lon1} (south, west, north, east): the order of the AWC
     * OpenAPI ({@code components.parameters.bbox}) and of the live API, which answers an empty collection for a
     * longitude-first box (verified 2026-10-14).
     *
     * @param box the box as Foresignal stores it (x = longitude)
     */
    public static String bboxLatLon(Envelope box) {
        return trim(box.getMinY()) + "," + trim(box.getMinX()) + "," + trim(box.getMaxY()) + "," + trim(box.getMaxX());
    }

    private static String trim(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    // ---------- stations ----------

    /** The allowlist: trimmed, upper-cased, blanks dropped, in order. */
    public static Set<String> stations(List<String> stations) {
        Set<String> allow = new LinkedHashSet<>();
        if (stations != null) {
            for (String station : stations) {
                if (station != null && !station.isBlank()) {
                    allow.add(station.strip().toUpperCase(Locale.ROOT));
                }
            }
        }
        return allow;
    }

    /**
     * The envelope of a station scope, {@code [minLon, minLat, maxLon, maxLat]}, or null for {@code global}.
     *
     * @param scope {@code aoi} (also when null or unknown), {@code conus} or {@code global}
     * @param aoi the places AOI, or null for the default Texas box
     */
    public static double[] envelope(String scope, AreaOfInterest aoi) {
        return switch (scope == null ? "aoi" : scope) {
            case "global" -> null;
            case "conus" -> CONUS.clone();
            default -> aoi == null ? DEFAULT_ENVELOPE.clone() : aoi.envelope();
        };
    }

    /**
     * True when a station is kept: by the allowlist when it is not empty, otherwise by the envelope (null keeps
     * everything).
     */
    public static boolean keeps(double[] envelope, Set<String> stations, String station, double lon, double lat) {
        if (!stations.isEmpty()) {
            return stations.contains(station);
        }
        return envelope == null || AreaOfInterest.contains(envelope, lon, lat);
    }

    /** {@code stations} split into chunks of at most {@code size}, in order. */
    public static List<List<String>> chunks(Set<String> stations, int size) {
        List<List<String>> out = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String station : stations) {
            current.add(station);
            if (current.size() == size) {
                out.add(List.copyOf(current));
                current.clear();
            }
        }
        if (!current.isEmpty()) {
            out.add(List.copyOf(current));
        }
        return out;
    }

    // ---------- cache files ----------

    /**
     * One conditional GET of an AWC cache file ({@code /data/cache/*.gz}).
     *
     * @param ctx the fetch context
     * @param uri the file
     * @param stateKey where the validators are kept
     * @param what what is read, for messages
     * @return the body, or null when the upstream answered 304
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the body is truncated or the status is not 200; the validators are
     *     forgotten so the next run reads the file again
     */
    public static byte[] conditionalBody(FetchContext ctx, URI uri, String stateKey, String what)
            throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(uri, stateKey);
        if (response.notModified()) {
            return null;
        }
        if (response.truncated() || response.status() != 200) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
            throw new UpstreamFormatException(what + (response.truncated()
                    ? " exceeded maxResponseBytes and was truncated" : " answered HTTP " + response.status()), null);
        }
        return response.body();
    }

    /**
     * {@code bytes} inflated when they are gzip (AWC serves its cache files as {@code application/octet-stream}
     * without {@code Content-Encoding}), unchanged otherwise.
     *
     * @param what what is read, for messages
     * @throws UpstreamFormatException when the gzip is unreadable or inflates past 64 MB
     */
    public static byte[] gunzip(byte[] bytes, String what) {
        if (bytes == null || bytes.length < 2 || (bytes[0] & 0xff) != 0x1f || (bytes[1] & 0xff) != 0x8b) {
            return bytes;
        }
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] out = in.readNBytes((int) MAX_INFLATED_BYTES + 1);
            if (out.length > MAX_INFLATED_BYTES) {
                throw new UpstreamFormatException(what + " inflates past " + MAX_INFLATED_BYTES + " bytes", null);
            }
            return out;
        } catch (IOException e) {
            throw new UpstreamFormatException(what + " is not readable gzip: " + e.getMessage(), null, e);
        }
    }

    // ---------- JSON ----------

    /**
     * The body of a plain (unconditional) API response.
     *
     * @param what what is read, for messages
     * @return the body; an empty array for a 204 (AWC answers 204 to a JSON query with no results)
     * @throws UpstreamFormatException when the status is not 200 or 204 or the body was truncated
     */
    public static byte[] body(UpstreamResponse response, String what) {
        if (response.status() == 204) {
            return new byte[0];
        }
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(what + (response.truncated() ? " exceeds the response cap"
                    : " answered HTTP " + response.status()), null);
        }
        return response.body();
    }

    /**
     * Parses a JSON document.
     *
     * @param what what is read, for messages
     * @throws UpstreamFormatException when it is not JSON
     */
    public static JsonNode json(byte[] body, String what) {
        try {
            JsonNode root = JSON.readTree(body == null ? new byte[0] : body);
            if (root == null || root.isMissingNode() || root.isNull()) {
                throw new UpstreamFormatException(what + " is empty", "");
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " is not JSON: " + e.getOriginalMessage(), "", e);
        }
    }

    /**
     * The features of a GeoJSON FeatureCollection; an empty body (a 204) has none.
     *
     * @throws UpstreamFormatException when the document is not a FeatureCollection
     */
    public static List<JsonNode> features(byte[] body, String what) {
        if (body == null || body.length == 0) {
            return List.of();
        }
        JsonNode root = json(body, what);
        JsonNode features = root.get("features");
        if (!"FeatureCollection".equals(text(root.get("type"))) || features == null || !features.isArray()) {
            throw new UpstreamFormatException(what + " is not a GeoJSON FeatureCollection", "");
        }
        List<JsonNode> out = new ArrayList<>(features.size());
        features.forEach(out::add);
        return out;
    }

    /**
     * The geometry of a feature, or null when it has none or it cannot be read (counted by the caller). Polygon rings
     * AWC leaves open (three international SIGMETs of the 2026-10-14 capture end on a different vertex than they
     * start) are closed first.
     */
    public static Geometry geometry(JsonNode feature) {
        JsonNode geometry = feature == null ? null : feature.get("geometry");
        if (geometry == null || geometry.isNull() || !geometry.isObject()) {
            return null;
        }
        try {
            return GEOJSON.read(closeRings(geometry));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** {@code geometry} with every open Polygon or MultiPolygon ring closed; other geometries unchanged. */
    static JsonNode closeRings(JsonNode geometry) {
        String type = text(geometry.get("type"));
        JsonNode coordinates = geometry.get("coordinates");
        if (coordinates == null || !coordinates.isArray()
                || !("Polygon".equals(type) || "MultiPolygon".equals(type))) {
            return geometry;
        }
        tools.jackson.databind.node.ObjectNode copy = (tools.jackson.databind.node.ObjectNode) geometry.deepCopy();
        tools.jackson.databind.node.ArrayNode polygons = "Polygon".equals(type)
                ? JSON.createArrayNode().add(copy.get("coordinates")) : (tools.jackson.databind.node.ArrayNode)
                copy.get("coordinates");
        for (JsonNode polygon : polygons) {
            if (!polygon.isArray()) {
                continue;
            }
            for (JsonNode ring : polygon) {
                if (ring.isArray() && ring.size() >= 3 && !ring.get(0).equals(ring.get(ring.size() - 1))) {
                    ((tools.jackson.databind.node.ArrayNode) ring).add(ring.get(0).deepCopy());
                }
            }
        }
        return copy;
    }

    /** A scalar's text, trimmed, or null when missing, null, a container or blank. */
    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return null;
        }
        String text = node.asString();
        return text == null || text.isBlank() ? null : text.strip();
    }

    /** A number from a JSON number or numeric text, or null. */
    public static Double number(JsonNode node) {
        String text = text(node);
        if (text == null) {
            return null;
        }
        try {
            double value = node.isNumber() ? node.doubleValue() : Double.parseDouble(text);
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** An integer from a JSON number or integral text, or null. */
    public static Long integer(JsonNode node) {
        Double value = number(node);
        return value == null || value != Math.rint(value) ? null : value.longValue();
    }

    /**
     * An instant from ISO-8601 text ({@code 2026-10-14T03:55:00.000Z}, or {@code 2026-10-14 03:55:00.000Z} as the
     * TAF {@code issueTime}) or from epoch seconds (the CWA and TAF validity, per AWC's OpenAPI); null otherwise.
     */
    public static Instant instant(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isNumber()) {
            return Instant.ofEpochSecond(node.longValue());
        }
        return instant(text(node));
    }

    /** An instant from ISO-8601 text (a space instead of {@code T} is accepted), or null. */
    public static Instant instant(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.strip();
        if (value.matches("^\\d+$")) {
            return Instant.ofEpochSecond(Long.parseLong(value));
        }
        if (value.length() > 10 && value.charAt(10) == ' ') {
            value = value.substring(0, 10) + "T" + value.substring(11);
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    // ---------- descriptors ----------

    /**
     * A descriptor builder with what every AWC source shares: name, description, kind, homepage, attribution,
     * license and terms.
     */
    public static SourceDescriptor.Builder builder(String id, String name, String description, String kind) {
        return SourceDescriptor.builder(id)
                .name(name)
                .description(description)
                .kind(kind)
                .homepage(HOMEPAGE)
                .attribution(ATTRIBUTION)
                .licence(LICENSE)
                .termsUrl(TERMS_URL);
    }

    /** Declares a text field. */
    public static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    /** Declares a number field with a UCUM unit, or null. */
    public static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }

    /** Declares an integer field with a UCUM unit, or null. */
    public static void integer(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }

    /** Declares a timestamp field. */
    public static void timestamp(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.TIMESTAMP, null, label, description, null, Set.of()));
    }

    /** Declares an array field. */
    public static void array(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.ARRAY, null, label, description, null, Set.of()));
    }
}
