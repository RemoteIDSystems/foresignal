package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * OpenMHz's {@code /systems} catalog as {@code scanner} records (prompt 082). Pure.
 *
 * <p>Unverified against a live response: the API answered every request from Foresignal's identifiable client with a
 * Cloudflare challenge (2026-10). The shape follows the OpenMHz web client's use of the endpoint: a JSON object with a
 * {@code systems} array (a bare array is accepted too), each system with {@code shortName, name, location, state,
 * county, lastActive, clientCount} and, when present, {@code lat}/{@code lng} (or {@code lon}).
 *
 * <ul>
 *   <li><strong>Challenge pages:</strong> {@link #looksLikeHtml} tells an HTML body apart from JSON, so the adapter
 *       can report {@code BLOCKED_CHALLENGE} instead of a parse error.
 *   <li><strong>Identity:</strong> {@code externalId = shortName}; a system without one is skipped.
 *   <li><strong>Payload:</strong> {@code name, shortName, location, state, county, lastActive} (ISO-8601 text) and
 *       the volatile {@code clientCount}.
 * </ul>
 */
public final class OpenMhzParser {

    /** The record kind. */
    public static final String KIND = "scanner";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private OpenMhzParser() {}

    /**
     * True when {@code body} is an HTML page (a challenge or error page) rather than JSON: it starts, after
     * whitespace, with {@code <} (a doctype, {@code <html>} or any tag).
     */
    public static boolean looksLikeHtml(byte[] body, String contentType) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("text/html")) {
            return true;
        }
        if (body == null) {
            return false;
        }
        for (byte b : body) {
            if (!Character.isWhitespace(b)) {
                return b == '<';
            }
        }
        return false;
    }

    /**
     * Parses the catalog.
     *
     * @throws UpstreamFormatException when the body is not JSON or holds no systems list
     */
    public static List<NormalizedRecord> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body == null ? new byte[0] : body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("The OpenMHz systems list is not JSON: " + e.getOriginalMessage(), "", e);
        }
        JsonNode systems = root != null && root.isArray() ? root : root == null ? null : root.get("systems");
        if (systems == null || !systems.isArray()) {
            throw new UpstreamFormatException("The OpenMHz response holds no systems array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        for (JsonNode system : systems) {
            String shortName = KiwiSdrParser.text(system.get("shortName"));
            if (shortName == null || !ids.add(shortName)) {
                continue;
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder(shortName, KIND)
                    .put("name", KiwiSdrParser.text(system.get("name")))
                    .put("shortName", shortName)
                    .put("location", KiwiSdrParser.text(system.get("location")))
                    .put("state", KiwiSdrParser.text(system.get("state")))
                    .put("county", KiwiSdrParser.text(system.get("county")))
                    .put("lastActive", isoInstant(KiwiSdrParser.text(system.get("lastActive"))))
                    .put("clientCount", integer(system.get("clientCount")));
            Double lat = number(system.get("lat"));
            Double lon = number(system.has("lng") ? system.get("lng") : system.get("lon"));
            if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
                b.point(lon, lat);
            }
            out.add(b.build());
        }
        return out;
    }

    private static String isoInstant(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Instant.parse(text).toString();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Integer integer(JsonNode node) {
        if (node == null || !node.isNumber() || !node.canConvertToInt()) {
            return null;
        }
        return node.intValue();
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.doubleValue();
        }
        try {
            return node.isString() ? Double.parseDouble(node.asString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
