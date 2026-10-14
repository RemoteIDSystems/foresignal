package ai.foresignal.source.adapters.ucdp;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import tools.jackson.databind.JsonNode;

/**
 * Maps UCDP API {@code gedevents} pages (prompt 081): {@code {TotalCount, TotalPages, PreviousPageUrl, NextPageUrl,
 * Result: [...]}} (UCDP API docs, verified 2026-10-13).
 *
 * <p>One {@value #KIND} record per event: {@code externalId = id} (kept across candidate versions, so a revised event
 * is a new version), a point at {@code longitude, latitude}, {@code quality.accuracyMeters} from {@code where_prec}
 * (GED codebook 26.1: 1 exact → 1 km, 2 within 25 km → 25 km, 3 second-order division → 50 km, 4 first-order
 * division → 150 km, 5 a linear feature or fuzzy area → 250 km; 6 only the country known → the country's label
 * point from the bundled table with {@code geometryApproximate}; 7 international waters or airspace → the coded
 * point with {@code geometryApproximate}). {@code eventTime = date_start}; {@code observedAt} is left to the framework
 * (receipt time: the API has no per-event update stamp).
 *
 * <p>Payload: {@code conflictName, dyadName, sideA, sideB, violenceType} (1 state-based, 2 non-state, 3 one-sided),
 * {@code country, region, adm1, adm2, deathsBest, deathsLow, deathsHigh, deathsCivilians, sourceCount,
 * sourceHeadline} (at most {@value #HEADLINE_MAX_BYTES} bytes, untrusted), {@code datePrecision, wherePrecision,
 * datasetVersion} (volatile) and {@code geometryApproximate}.
 */
public final class UcdpParser {

    /** The kind. */
    public static final String KIND = "conflict-event";

    /** The largest stored {@code sourceHeadline}, UTF-8 bytes. */
    public static final int HEADLINE_MAX_BYTES = 1024;

    /** The skip reason of an event without a usable position. */
    public static final String BAD_POSITION = "bad-position";

    /**
     * One page.
     *
     * @param records the events of the page
     * @param dateEnds {@code date_end} of every mapped event, by external id
     * @param totalCount the {@code TotalCount} of the query
     * @param totalPages the {@code TotalPages} of the query
     * @param nextPage the {@code page} parameter of {@code NextPageUrl}, or null when there is none
     * @param skipped skip reason to count
     */
    public record Page(List<NormalizedRecord> records, Map<String, LocalDate> dateEnds, long totalCount,
            int totalPages, Integer nextPage, Map<String, Integer> skipped) {

        /** Copies the collections. */
        public Page {
            records = List.copyOf(records);
            dateEnds = Map.copyOf(dateEnds);
            skipped = Map.copyOf(skipped);
        }
    }

    private UcdpParser() {}

    /** The text of a {@code type_of_violence} code, or null. */
    public static String violenceType(Long code) {
        if (code == null) {
            return null;
        }
        return switch (code.intValue()) {
            case 1 -> "state-based";
            case 2 -> "non-state";
            case 3 -> "one-sided";
            default -> null;
        };
    }

    /** The accuracy of a {@code where_prec} code, meters, or null (country-level, waters, unknown). */
    public static Double accuracyMeters(Long wherePrecision) {
        if (wherePrecision == null) {
            return null;
        }
        return switch (wherePrecision.intValue()) {
            case 1 -> 1_000.0;
            case 2 -> 25_000.0;
            case 3 -> 50_000.0;
            case 4 -> 150_000.0;
            case 5 -> 250_000.0;
            default -> null;
        };
    }

    /**
     * Parses one page.
     *
     * @param body the response body
     * @param version the dataset version read, stored as {@code datasetVersion}
     * @param countries the bundled country table, for country-level events
     * @throws UpstreamFormatException when the body is not a page with a {@code Result} array
     */
    public static Page parse(byte[] body, String version, CountryCentroids countries) {
        JsonNode root = ConflictSources.json(body, "UCDP gedevents");
        JsonNode result = root.isObject() ? root.get("Result") : null;
        if (result == null || !result.isArray()) {
            throw new UpstreamFormatException("UCDP gedevents: no Result array", "");
        }
        Long totalCount = ConflictSources.integer(root.get("TotalCount"));
        Long totalPages = ConflictSources.integer(root.get("TotalPages"));
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, LocalDate> ends = new LinkedHashMap<>();
        Map<String, Integer> skipped = new TreeMap<>();
        for (JsonNode event : result) {
            MappedRecord mapped = event(event, version, countries);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
                continue;
            }
            records.add(mapped.record());
            LocalDate end = date(ConflictSources.text(event.get("date_end")));
            if (end != null) {
                ends.put(mapped.record().externalId(), end);
            }
        }
        return new Page(records, ends, totalCount == null ? 0 : totalCount,
                totalPages == null ? 0 : totalPages.intValue(), pageOf(ConflictSources.text(root.get("NextPageUrl"))),
                skipped);
    }

    /**
     * The {@code TotalCount} of a probe response ({@code pagesize=1}), 0 when absent.
     *
     * @throws UpstreamFormatException when the body is not JSON
     */
    public static long totalCount(byte[] body) {
        JsonNode root = ConflictSources.json(body, "UCDP gedevents");
        Long count = root.isObject() ? ConflictSources.integer(root.get("TotalCount")) : null;
        return count == null ? 0 : count;
    }

    /** Maps one event. */
    public static MappedRecord event(JsonNode event, String version, CountryCentroids countries) {
        String id = ConflictSources.text(event.get("id"));
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Long wherePrec = ConflictSources.integer(event.get("where_prec"));
        Double lat = ConflictSources.number(event.get("latitude"));
        Double lon = ConflictSources.number(event.get("longitude"));
        String country = ConflictSources.text(event.get("country"));
        boolean approximate = false;
        if (wherePrec != null && wherePrec == 6) {
            Optional<CountryRef> ref = country(countries, country);
            if (ref.isPresent()) {
                lat = ref.get().labelLat();
                lon = ref.get().labelLon();
            }
            approximate = true;
        } else if (wherePrec != null && wherePrec == 7) {
            approximate = true;
        }
        boolean valid = lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180
                && !(lat == 0.0 && lon == 0.0);
        if (!valid && !approximate) {
            return MappedRecord.skipped(BAD_POSITION);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .eventTime(ConflictSources.dayStart(ConflictSources.text(event.get("date_start"))))
                .quality(new Quality(accuracyMeters(wherePrec), null, null, Map.of()))
                .put("conflictName", ConflictSources.text(event.get("conflict_name")))
                .put("dyadName", ConflictSources.text(event.get("dyad_name")))
                .put("sideA", ConflictSources.text(event.get("side_a")))
                .put("sideB", ConflictSources.text(event.get("side_b")))
                .put("violenceType", violenceType(ConflictSources.integer(event.get("type_of_violence"))))
                .put("country", country)
                .put("region", ConflictSources.text(event.get("region")))
                .put("adm1", ConflictSources.text(event.get("adm_1")))
                .put("adm2", ConflictSources.text(event.get("adm_2")))
                .put("deathsBest", ConflictSources.integer(event.get("best")))
                .put("deathsLow", ConflictSources.integer(event.get("low")))
                .put("deathsHigh", ConflictSources.integer(event.get("high")))
                .put("deathsCivilians", ConflictSources.integer(event.get("deaths_civilians")))
                .put("sourceCount", nonNegative(ConflictSources.integer(event.get("number_of_sources"))))
                .put("sourceHeadline", ConflictSources.truncateUtf8(ConflictSources.text(event.get("source_headline")),
                        HEADLINE_MAX_BYTES))
                .put("datePrecision", ConflictSources.integer(event.get("date_prec")))
                .put("wherePrecision", wherePrec)
                .put("datasetVersion", version);
        if (valid) {
            b.point(lon, lat);
        }
        if (approximate) {
            b.put("geometryApproximate", true);
        }
        return MappedRecord.of(b.build());
    }

    /** The {@code page} parameter of a paging URL, or null when absent or unreadable. */
    static Integer pageOf(String url) {
        if (url == null) {
            return null;
        }
        try {
            String query = URI.create(url).getRawQuery();
            if (query == null) {
                return null;
            }
            Integer page = null;
            for (String part : query.split("&")) {
                if (part.startsWith("page=")) {
                    page = Integer.parseInt(part.substring(5));
                }
            }
            return page;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** The country of a UCDP country name ("Yemen (North Yemen)" also tries "Yemen"). */
    static Optional<CountryRef> country(CountryCentroids countries, String name) {
        if (countries == null || name == null) {
            return Optional.empty();
        }
        Optional<CountryRef> ref = countries.byName(name);
        int paren = name.indexOf('(');
        if (ref.isEmpty() && paren > 0) {
            ref = countries.byName(name.substring(0, paren).strip());
        }
        return ref;
    }

    private static Long nonNegative(Long value) {
        return value == null || value < 0 ? null : value;
    }

    private static LocalDate date(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text.length() > 10 ? text.substring(0, 10) : text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
