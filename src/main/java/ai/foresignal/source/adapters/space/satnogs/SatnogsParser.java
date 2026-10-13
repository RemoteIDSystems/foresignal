package ai.foresignal.source.adapters.space.satnogs;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * SatNOGS Network stations ({@code /api/stations/}) as {@code ground-station} records (prompt 077). Pure.
 *
 * <ul>
 *   <li><strong>Page:</strong> a JSON array of stations, or a Django REST Framework page
 *       {@code {results: [...], next}}; the next page comes from the {@code Link: <…>; rel="next"} header
 *       ({@link #nextLink}) or the page's {@code next}.
 *   <li><strong>Record:</strong> {@code externalId = id}, a point from {@code lat}/{@code lng}, payload
 *       {@code name, status, altitudeM} ({@code altitude}), {@code qthLocator, antennas} (distinct
 *       {@code {band, type}} pairs, {@code type} = {@code antenna_type}), {@code clientVersion} and {@code lastSeen}
 *       (volatile). The raw {@code observations} counter, the owner, the description and the image are left out.
 * </ul>
 */
public final class SatnogsParser {

    /** The record kind. */
    public static final String KIND = "ground-station";

    /** {@code <url>; rel="next"} in a {@code Link} header. */
    private static final Pattern NEXT = Pattern.compile("<([^>]+)>\\s*;[^,]*rel=\"?next\"?", Pattern.CASE_INSENSITIVE);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed page.
     *
     * @param records one record per station
     * @param next the page's own {@code next} URL, or null
     * @param skipped skip reason to count
     */
    public record Page(List<NormalizedRecord> records, String next, Map<String, Integer> skipped) {}

    private SatnogsParser() {}

    /**
     * Parses one page.
     *
     * @throws UpstreamFormatException when the body is neither an array nor a page object
     */
    public static Page parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("SatNOGS stations are not JSON: " + e.getOriginalMessage(), "", e);
        }
        JsonNode items;
        String next = null;
        if (root != null && root.isArray()) {
            items = root;
        } else if (root != null && root.isObject() && root.path("results").isArray()) {
            items = root.get("results");
            next = JsonProps.text(root, "next");
        } else {
            throw new UpstreamFormatException("SatNOGS stations are neither an array nor a results page", "");
        }
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode item : items) {
            MappedRecord m = station(item);
            if (!m.accepted()) {
                skipped.merge(m.reason(), 1, Integer::sum);
            } else if (ids.add(m.record().externalId())) {
                out.add(m.record());
            } else {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
            }
        }
        return new Page(out, next, skipped);
    }

    /** One station (see the class note). */
    public static MappedRecord station(JsonNode s) {
        Long id = JsonProps.integer(s, "id");
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(String.valueOf(id), KIND);
        Double lat = JsonProps.number(s, "lat");
        Double lng = JsonProps.number(s, "lng");
        if (lat != null && lng != null && Math.abs(lat) <= 90 && Math.abs(lng) <= 180) {
            b.point(lng, lat);
        }
        String lastSeen = JsonProps.text(s, "last_seen");
        return MappedRecord.of(b.put("name", SpaceSources.cut(JsonProps.text(s, "name"), 200))
                .put("status", JsonProps.text(s, "status"))
                .put("altitudeM", JsonProps.integer(s, "altitude"))
                .put("qthLocator", JsonProps.text(s, "qthlocator"))
                .put("antennas", antennas(s.get("antenna")))
                .put("clientVersion", JsonProps.text(s, "client_version"))
                .put("lastSeen", SpaceSources.utc(lastSeen) == null ? null : SpaceSources.utc(lastSeen).toString())
                .build());
    }

    /** The distinct {@code {band, type}} pairs of an {@code antenna} array (at most 20), or null. */
    static List<Map<String, String>> antennas(JsonNode array) {
        List<Map<String, String>> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode a : array) {
                Map<String, String> pair = new LinkedHashMap<>();
                String band = JsonProps.text(a, "band");
                String type = JsonProps.text(a, "antenna_type");
                if (band != null) {
                    pair.put("band", band);
                }
                if (type != null) {
                    pair.put("type", type);
                }
                if (!pair.isEmpty() && !out.contains(pair) && out.size() < 20) {
                    out.add(Map.copyOf(pair));
                }
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
    }

    /**
     * The {@code rel="next"} URL of a {@code Link} header, or null.
     *
     * @param header the header value, or null
     */
    public static URI nextLink(String header) {
        if (header == null) {
            return null;
        }
        Matcher m = NEXT.matcher(header);
        if (!m.find()) {
            return null;
        }
        try {
            return URI.create(m.group(1).strip());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
