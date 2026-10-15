package ai.foresignal.source.adapters.gdacs;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses one GDACS {@code geteventlist/MAP} response into {@code disaster} records (prompt 067).
 *
 * <p>A pure function of the bytes and the requested event type (adapter rule 3).
 *
 * <p><strong>One record per event.</strong> The MAP response is a GeoJSON FeatureCollection with several
 * features per event (verified 2026-10): a {@code Point_Centroid} point, and for some types also track lines,
 * wind-buffer polygons and, for floods, {@code Point_Global} and {@code Point_Affected} points. Only Point
 * features are read; per {@code eventid} the {@code Point_Centroid} feature is preferred, else the first point,
 * and of several episodes the highest {@code episodeid} wins. {@code externalId = {eventtype}:{eventid}}, so a
 * new episode updates the event's record. Green alerts are kept; rules filter on {@code alertLevel}.
 *
 * <p><strong>Time base</strong> (revised events, Core-Data-Model §2): {@code eventTime = fromdate},
 * {@code observedAt = datemodified}. GDACS writes these without an offset ({@code 2026-10-09T18:22:15}); they
 * are read as UTC. An event without a readable {@code datemodified} is skipped and counted, never given another
 * clock.
 *
 * <p>GDACS alerts are computed automatically and not reviewed, so every record carries
 * {@code automated: true}. Strings are stripped of control characters and capped at {@value #MAX_TEXT}
 * characters; {@code reportUrl} is kept only on {@code https://www.gdacs.org/}.
 */
public final class GdacsEventParser {

    /** The record kind. */
    public static final String KIND = "disaster";

    /** The longest string kept, in characters. */
    public static final int MAX_TEXT = 1024;

    /** The event types GDACS publishes. */
    public static final List<String> EVENT_TYPES = List.of("EQ", "TC", "FL", "VO", "DR", "WF");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");
    private static final Pattern ISO3 = Pattern.compile("^[A-Z]{3}$");

    private GdacsEventParser() {}

    /**
     * Parses one response.
     *
     * @param json the response body
     * @param eventType the type that was requested; features of another type are skipped
     * @return the records and counts
     * @throws UpstreamFormatException when the body is not a GeoJSON FeatureCollection
     */
    public static ParseResult parse(byte[] json, String eventType) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("GDACS response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("GDACS response is not valid JSON: " + e.getOriginalMessage(),
                    snippet(json), e);
        }
        JsonNode features = root == null ? null : root.get("features");
        if (root == null || !root.isObject() || features == null || !features.isArray()) {
            throw new UpstreamFormatException("GDACS response is not a GeoJSON FeatureCollection", snippet(json));
        }
        Map<String, Candidate> best = new LinkedHashMap<>();
        int skipped = 0;
        int nonPoint = 0;
        for (JsonNode feature : features) {
            JsonNode geometry = feature == null ? null : feature.get("geometry");
            JsonNode p = feature == null ? null : feature.get("properties");
            if (geometry == null || !"Point".equals(string(geometry.get("type")))) {
                nonPoint++;
                continue;
            }
            if (p == null || !p.isObject()) {
                skipped++;
                continue;
            }
            String type = string(p.get("eventtype"));
            Long eventId = integer(p.get("eventid"));
            double[] lonLat = lonLat(geometry.get("coordinates"));
            if (type == null || !type.equalsIgnoreCase(eventType) || eventId == null || lonLat == null) {
                skipped++;
                continue;
            }
            String externalId = eventType.toUpperCase(Locale.ROOT) + ":" + eventId;
            long episode = integer(p.get("episodeid")) == null ? -1 : integer(p.get("episodeid"));
            boolean centroid = "Point_Centroid".equals(string(p.get("Class")));
            Candidate candidate = new Candidate(p, lonLat, episode, centroid);
            Candidate current = best.get(externalId);
            if (current == null || candidate.betterThan(current)) {
                best.put(externalId, candidate);
            }
        }
        List<NormalizedRecord> records = new ArrayList<>();
        for (Map.Entry<String, Candidate> e : best.entrySet()) {
            NormalizedRecord record = record(e.getKey(), eventType.toUpperCase(Locale.ROOT), e.getValue());
            if (record == null) {
                skipped++;
            } else {
                records.add(record);
            }
        }
        return new ParseResult(records, features.size(), nonPoint, skipped);
    }

    private static NormalizedRecord record(String externalId, String eventType, Candidate c) {
        JsonNode p = c.properties();
        Instant modified = time(p.get("datemodified"));
        if (modified == null) {
            return null;
        }
        Instant from = time(p.get("fromdate"));
        Instant to = time(p.get("todate"));
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, KIND)
                .observedAt(modified)
                .eventTime(from)
                .point(c.lonLat()[0], c.lonLat()[1]);
        b.put("eventType", eventType);
        b.put("eventId", integer(p.get("eventid")));
        b.put("episodeId", integer(p.get("episodeid")));
        b.put("name", text(p.get("name")));
        b.put("description", text(p.get("description")));
        b.put("alertLevel", text(p.get("alertlevel")));
        b.put("alertScore", number(p.get("alertscore")));
        b.put("episodeAlertLevel", text(p.get("episodealertlevel")));
        JsonNode severity = p.get("severitydata");
        if (severity != null && severity.isObject()) {
            b.put("severity", number(severity.get("severity")));
            b.put("severityText", text(severity.get("severitytext")));
            b.put("severityUnit", text(severity.get("severityunit")));
        }
        // GDACS puts a sea or region descriptor in country for open-water events ("West Of Macquarie
        // Island") with no iso3; keep country only when it names a real country (a valid iso3).
        String iso3 = text(p.get("iso3"));
        boolean validIso3 = iso3 != null && ISO3.matcher(iso3).matches();
        b.put("country", validIso3 ? text(p.get("country")) : null);
        b.put("iso3", validIso3 ? iso3 : null);
        List<String> affected = new ArrayList<>();
        JsonNode countries = p.get("affectedcountries");
        if (countries != null && countries.isArray()) {
            for (JsonNode country : countries) {
                String code = text(country == null ? null : country.get("iso3"));
                if (code != null && ISO3.matcher(code).matches() && !affected.contains(code)) {
                    affected.add(code);
                }
            }
        }
        b.put("affectedCountries", affected.isEmpty() ? null : affected);
        b.put("fromDate", from == null ? null : from.toString());
        b.put("toDate", to == null ? null : to.toString());
        b.put("isCurrent", bool(p.get("iscurrent")));
        b.put("isTemporary", bool(p.get("istemporary")));
        b.put("glide", text(p.get("glide")));
        JsonNode url = p.get("url");
        b.put("reportUrl", url != null && url.isObject() ? gdacsLink(url.get("report")) : null);
        b.put("automated", true);
        return b.build();
    }

    /** A GDACS time: an ISO local date-time read as UTC, or an ISO date-time with an offset; else null. */
    static Instant time(JsonNode node) {
        String value = string(node);
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        try {
            return LocalDateTime.parse(text).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(text).toInstant();
            } catch (DateTimeParseException again) {
                return null;
            }
        }
    }

    /** The link when it is an {@code https://www.gdacs.org/} URL of at most 512 characters; else null. */
    static String gdacsLink(JsonNode node) {
        String value = string(node);
        if (value == null) {
            return null;
        }
        String link = value.strip();
        return link.length() <= 512 && link.startsWith("https://www.gdacs.org/") && !CONTROL.matcher(link).find()
                ? link : null;
    }

    /** {@code "true"} / {@code "false"} (GDACS sends strings) or a JSON boolean; else null. */
    static Boolean bool(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        String value = string(node);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        return "false".equalsIgnoreCase(value) ? false : null;
    }

    private static double[] lonLat(JsonNode coordinates) {
        if (coordinates == null || !coordinates.isArray() || coordinates.size() < 2
                || !coordinates.get(0).isNumber() || !coordinates.get(1).isNumber()) {
            return null;
        }
        double lon = coordinates.get(0).doubleValue();
        double lat = coordinates.get(1).doubleValue();
        if (!Double.isFinite(lon) || !Double.isFinite(lat) || lon < -180 || lon > 180 || lat < -90 || lat > 90) {
            return null;
        }
        return new double[] {lon, lat};
    }

    private static Long integer(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isIntegralNumber()) {
            return node.longValue();
        }
        String value = string(node);
        if (value != null && value.strip().matches("\\d{1,18}")) {
            return Long.valueOf(value.strip());
        }
        return null;
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    private static String text(JsonNode node) {
        String value = string(node);
        if (value == null) {
            return null;
        }
        String cleaned = CONTROL.matcher(value).replaceAll(" ").strip();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.length() <= MAX_TEXT ? cleaned : cleaned.substring(0, MAX_TEXT);
    }

    private static String string(JsonNode node) {
        return node != null && node.isString() ? node.stringValue() : null;
    }

    private static String snippet(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }

    private record Candidate(JsonNode properties, double[] lonLat, long episode, boolean centroid) {
        boolean betterThan(Candidate other) {
            if (episode != other.episode) {
                return episode > other.episode;
            }
            return centroid && !other.centroid;
        }
    }

    /**
     * One parsed response.
     *
     * @param records one record per event, in feed order
     * @param features features in the response
     * @param nonPoint features skipped because they are not points (tracks, buffers)
     * @param skipped point features or events that could not become a record
     */
    public record ParseResult(List<NormalizedRecord> records, int features, int nonPoint, int skipped) {}
}
