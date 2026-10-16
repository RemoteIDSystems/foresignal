package ai.foresignal.source.adapters.inat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlaceTimeZones;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses iNaturalist v1 {@code /observations} responses into {@code nature-observation} records (prompt 095).
 *
 * <p>A pure function of its inputs (adapter rule 3): no network, no clock, no database. The only external
 * dependency is {@link PlaceTimeZones}, needed to turn a date-only {@code observed_on} into an instant.
 *
 * <p><strong>Envelope</strong> (verified 2026-10 against the v1 API): an object with {@code total_results},
 * {@code page}, {@code per_page} and a {@code results} array. Anything else is an
 * {@link UpstreamFormatException}.
 *
 * <p><strong>Privacy.</strong> A record whose {@code geoprivacy} or {@code taxon_geoprivacy} is {@code private},
 * or that carries no coordinates, is skipped. When {@code obscured} is true the coordinates are already
 * randomised upstream; the parser never tries to refine them and records the upstream's own
 * {@code public_positional_accuracy} as {@link Quality#accuracyMeters()}. Contributor identity is never read:
 * {@code user}, {@code description} and comments are ignored entirely.
 *
 * <p><strong>Photos.</strong> {@code photoUrl} is emitted only when the first photo's {@code license_code} is
 * present and its URL host is {@code inaturalist-open-data}; every other photo URL is omitted.
 */
public final class InatParser {

    /** The record kind. */
    public static final String KIND = "nature-observation";

    /** The open-data photo host a {@code photoUrl} must be on. */
    public static final String OPEN_DATA_HOST = "inaturalist-open-data";

    /** The exact host of the open-data photo bucket ({@value #OPEN_DATA_HOST} on Amazon S3). */
    public static final String OPEN_DATA_BUCKET_HOST = OPEN_DATA_HOST + ".s3.amazonaws.com";

    /** {@code license_code} values iNaturalist treats as open. */
    private static final Pattern OPEN_LICENSE = Pattern.compile("^cc(0|-.+)$");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");

    private InatParser() {}

    /**
     * What the adapter asks the parser to keep.
     *
     * @param qualityGrade the required quality grade, or null for none
     * @param iconicTaxa iconic taxon names kept; empty keeps every taxon
     * @param licensedOnly true to skip observations whose {@code license_code} is null (all rights reserved)
     * @param includeCaptive true to keep observations flagged captive or cultivated
     * @param zones the timezone lookup for a date-only {@code observed_on}; never null
     */
    public record Options(String qualityGrade, List<String> iconicTaxa, boolean licensedOnly, boolean includeCaptive,
            PlaceTimeZones zones) {

        /** Copies the taxon list. */
        public Options {
            iconicTaxa = iconicTaxa == null ? List.of() : List.copyOf(iconicTaxa);
        }
    }

    /**
     * One parsed page.
     *
     * @param records the kept observations, in response order
     * @param rows the length of {@code results}, kept or not
     * @param skipped rows dropped for privacy, missing fields, a filter or no coordinates
     * @param maxId the largest observation id on the page, kept or not (the next {@code id_above})
     */
    public record Result(List<NormalizedRecord> records, int rows, int skipped, long maxId) {

        /** Copies the records. */
        public Result {
            records = List.copyOf(records);
        }
    }

    /**
     * Parses one response.
     *
     * @param json the response body
     * @param options the filters
     * @return the records and counts
     * @throws UpstreamFormatException when the body is empty, not JSON, or not the iNaturalist envelope
     */
    public static Result parse(byte[] json, Options options) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("iNaturalist response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("iNaturalist response is not valid JSON: " + e.getOriginalMessage(),
                    snippet(json), e);
        }
        JsonNode results = root == null ? null : root.get("results");
        if (root == null || !root.isObject() || results == null || !results.isArray()) {
            throw new UpstreamFormatException("iNaturalist response is not an object with a results array",
                    snippet(json));
        }
        List<NormalizedRecord> records = new ArrayList<>();
        int skipped = 0;
        long maxId = 0L;
        for (JsonNode row : results) {
            String rawId = id(row.get("id"));
            if (rawId != null) {
                maxId = Math.max(maxId, parseLong(rawId));
            }
            NormalizedRecord record = observation(row, options);
            if (record == null) {
                skipped++;
            } else {
                records.add(record);
            }
        }
        return new Result(records, results.size(), skipped, maxId);
    }

    /** One observation, or null when it is skipped. */
    private static NormalizedRecord observation(JsonNode row, Options options) {
        if (row == null || !row.isObject()) {
            return null;
        }
        String id = id(row.get("id"));
        if (id == null) {
            return null;
        }
        if (isPrivate(text(row.get("geoprivacy"))) || isPrivate(text(row.get("taxon_geoprivacy")))) {
            return null;
        }
        Point point = coordinates(row);
        if (point == null) {
            return null;
        }
        String qualityGrade = lower(text(row.get("quality_grade")));
        if (options.qualityGrade() != null && !options.qualityGrade().equalsIgnoreCase(qualityGrade)) {
            return null;
        }
        String licenseCode = text(row.get("license_code"));
        if (options.licensedOnly() && licenseCode == null) {
            return null;
        }
        JsonNode taxon = row.get("taxon");
        String iconicTaxon = taxon != null && taxon.isObject() ? text(taxon.get("iconic_taxon_name")) : null;
        if (!options.iconicTaxa().isEmpty() && (iconicTaxon == null
                || options.iconicTaxa().stream().noneMatch(t -> t.equalsIgnoreCase(iconicTaxon)))) {
            return null;
        }
        boolean captive = truthy(row.get("captive"));
        if (captive && !options.includeCaptive()) {
            return null;
        }
        Instant observedAt = parseInstant(row.get("updated_at"));
        if (observedAt == null) {
            return null;
        }
        boolean obscured = truthy(row.get("obscured"));
        Instant eventTime = eventTime(row, point, options.zones());

        NormalizedRecord.Builder b = NormalizedRecord.builder("inat:" + id, KIND).observedAt(observedAt)
                .eventTime(eventTime).point(point.lon(), point.lat());
        b.put("taxonId", taxon != null && taxon.isObject() ? longValue(taxon.get("id")) : null);
        b.put("taxonName", taxon != null && taxon.isObject() ? text(taxon.get("name")) : null);
        b.put("commonName", taxon != null && taxon.isObject() ? text(taxon.get("preferred_common_name")) : null);
        b.put("iconicTaxon", iconicTaxon);
        b.put("qualityGrade", qualityGrade);
        b.put("licenseCode", licenseCode);
        b.put("obscured", obscured);
        b.put("uri", text(row.get("uri")));
        b.put("photoUrl", photoUrl(row.get("photos")));
        if (obscured) {
            b.quality(new Quality(positionalAccuracy(row.get("public_positional_accuracy")), null, null, Map.of()));
        }
        return b.build();
    }

    /** The point, from {@code geojson}, then {@code location} ("lat,lon"), then {@code latitude}/{@code longitude}. */
    static Point coordinates(JsonNode row) {
        JsonNode geojson = row.get("geojson");
        if (geojson != null && geojson.isObject()) {
            JsonNode coords = geojson.get("coordinates");
            if (coords != null && coords.isArray() && coords.size() >= 2) {
                Double lon = number(coords.get(0));
                Double lat = number(coords.get(1));
                if (valid(lon, lat)) {
                    return new Point(lon, lat);
                }
            }
        }
        String location = text(row.get("location"));
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            Double lat = parseDouble(parts[0]);
            Double lon = parts.length > 1 ? parseDouble(parts[1]) : null;
            if (valid(lon, lat)) {
                return new Point(lon, lat);
            }
        }
        Double lat = number(row.get("latitude"));
        Double lon = number(row.get("longitude"));
        return valid(lon, lat) ? new Point(lon, lat) : null;
    }

    /** {@code time_observed_at} (ISO with offset), else {@code observed_on} at local midnight. */
    static Instant eventTime(JsonNode row, Point point, PlaceTimeZones zones) {
        Instant observed = parseInstant(row.get("time_observed_at"));
        if (observed != null) {
            return observed;
        }
        String date = text(row.get("observed_on"));
        if (date == null) {
            return null;
        }
        LocalDate day;
        try {
            day = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return null;
        }
        ZoneId zone = zoneAt(point, zones);
        return day.atStartOfDay(zone).toInstant();
    }

    /** The nearest place's zone, else UTC. */
    static ZoneId zoneAt(Point point, PlaceTimeZones zones) {
        if (zones == null) {
            return ZoneOffset.UTC;
        }
        Optional<ZoneId> zone = zones.byPoint(point.lon(), point.lat());
        return zone.orElse(ZoneOffset.UTC);
    }

    /** The first photo's URL when its licence is open and its host is {@code inaturalist-open-data}. */
    static String photoUrl(JsonNode photos) {
        if (photos == null || !photos.isArray() || photos.isEmpty()) {
            return null;
        }
        JsonNode first = photos.get(0);
        if (first == null || !first.isObject()) {
            return null;
        }
        String license = text(first.get("license_code"));
        if (license == null || !OPEN_LICENSE.matcher(license).matches()) {
            return null;
        }
        String url = text(first.get("url"));
        return isOpenDataUrl(url) ? url : null;
    }

    /**
     * True when {@code url} is an {@code https} URL whose host is exactly the open-data bucket host
     * {@value #OPEN_DATA_BUCKET_HOST} (2026-10 review: a substring test accepted any host or path that merely
     * contained the name, such as {@code inaturalist-open-data.example.net} or {@code ?x=inaturalist-open-data}).
     */
    static boolean isOpenDataUrl(String url) {
        if (url == null) {
            return false;
        }
        try {
            java.net.URI uri = java.net.URI.create(url.strip());
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) && host != null
                    && host.toLowerCase(Locale.ROOT).equals(OPEN_DATA_BUCKET_HOST);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static Instant parseInstant(JsonNode node) {
        String value = text(node);
        if (value == null) {
            return null;
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

    private static Double positionalAccuracy(JsonNode node) {
        Double value = number(node);
        return value == null || value < 0 ? null : value;
    }

    private static boolean isPrivate(String value) {
        return value != null && value.equalsIgnoreCase("private");
    }

    private static boolean truthy(JsonNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        String text = text(node);
        return text != null && (text.equalsIgnoreCase("true") || text.equals("1"));
    }

    private static String id(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isIntegralNumber()) {
            return Long.toString(node.asLong());
        }
        return text(node);
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value.strip());
        } catch (NumberFormatException | NullPointerException e) {
            return 0L;
        }
    }

    /** An integral JSON value as a {@link Long}, else null. */
    static Long longValue(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        return node.canConvertToLong() ? node.asLong() : null;
    }

    static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double d = node.asDouble();
        return Double.isFinite(d) ? d : null;
    }

    private static Double parseDouble(String value) {
        try {
            return Double.valueOf(value.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean valid(Double lon, Double lat) {
        return lon != null && lat != null && lon >= -180 && lon <= 180 && lat >= -90 && lat <= 90;
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String raw = node.asString();
        String clean = CONTROL.matcher(raw).replaceAll("").strip();
        return clean.isEmpty() ? null : clean;
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static String snippet(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }

    /** A WGS84 point. */
    record Point(double lon, double lat) {}
}
