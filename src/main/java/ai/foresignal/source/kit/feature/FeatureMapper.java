package ai.foresignal.source.kit.feature;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Maps one GeoJSON feature to a record under its spec (prompt 073). Pure: the same feature and spec always give the
 * same record.
 *
 * <ol>
 *   <li><strong>Allowlist:</strong> only the attributes named in {@code fields} are read, each renamed to its target;
 *   <li><strong>Sentinels and nulls removed:</strong> null, blank text, {@code -999} and {@code -9999}, and dates in
 *       year 1 ({@code 0001-01-01…}, or that epoch value) never reach the payload (adapter rules 8 and 11);
 *   <li><strong>Value maps:</strong> a coded value is looked up by its text ({@code {"1": "low"}}) and replaced;
 *   <li><strong>Typing:</strong> numbers are coerced from text, text is trimmed, booleans accept
 *       {@code true/false/1/0/yes/no/y/n}, timestamps are epoch milliseconds, epoch seconds, ISO-8601 or
 *       {@code yyyy-MM-dd HH:mm:ss} in the spec's zone, stored as ISO-8601 instants;
 *   <li><strong>Constants</strong> from the spec are added;
 *   <li><strong>Identity:</strong> {@code externalId} is the {@code idField} value as text; a feature without one is
 *       rejected ({@value #REJECT_MISSING_ID});
 *   <li><strong>Times:</strong> {@code observedAt}, {@code eventTime}, {@code validFrom} and {@code validUntil} come
 *       from the fields the spec names, never from a fallback; a feature missing its {@code observedAt} field is
 *       rejected ({@value #REJECT_MISSING_OBSERVED_AT});
 *   <li><strong>Geometry:</strong> through {@link FeatureGeometry}, with the simplification flags in the payload.
 * </ol>
 */
public final class FeatureMapper {

    /** The rejection reason of a feature without an id. */
    public static final String REJECT_MISSING_ID = "missing-id";

    /** The rejection reason of a feature without its {@code observedAt} field. */
    public static final String REJECT_MISSING_OBSERVED_AT = "missing-observed-at";

    /** The rejection reason of a feature whose geometry could not be read. */
    public static final String REJECT_BAD_GEOMETRY = "bad-geometry";

    /** The rejection reason of a value that is not a feature object. */
    public static final String REJECT_NOT_A_FEATURE = "not-a-feature";

    /** The longest text value kept, in characters. */
    public static final int MAX_TEXT = 4000;

    /** {@code 0001-01-01T00:00:00Z} in epoch milliseconds. */
    static final long YEAR_ONE_EPOCH_MS = -62_135_596_800_000L;

    /** {@code 0002-01-01T00:00:00Z} in epoch milliseconds: anything earlier is a year-1 sentinel. */
    static final long YEAR_TWO_EPOCH_MS = YEAR_ONE_EPOCH_MS + 365L * 86_400_000L;

    private static final DateTimeFormatter SPACE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss",
            Locale.ROOT);

    /**
     * The outcome of one feature.
     *
     * @param record the record, or null when rejected
     * @param reason the rejection reason, or null
     */
    public record Mapped(NormalizedRecord record, String reason) {

        static Mapped rejected(String reason) {
            return new Mapped(null, reason);
        }

        /** True when the feature became a record. */
        public boolean accepted() {
            return record != null;
        }
    }

    private FeatureMapper() {}

    /**
     * Maps one feature.
     *
     * @param spec the source's spec
     * @param feature a GeoJSON Feature
     * @return the record, or the rejection reason
     */
    public static Mapped map(FeatureSourceSpec spec, JsonNode feature) {
        if (feature == null || !feature.isObject()) {
            return Mapped.rejected(REJECT_NOT_A_FEATURE);
        }
        JsonNode properties = feature.get("properties");
        ZoneId zone = zone(spec);
        String format = spec.time().format() == null ? "auto" : spec.time().format();
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Instant> instants = new LinkedHashMap<>();
        for (Map.Entry<String, FeatureSourceSpec.FieldMapping> e : spec.fields().entrySet()) {
            FeatureSourceSpec.FieldMapping m = e.getValue();
            String target = m.target() == null ? e.getKey() : m.target();
            JsonNode raw = properties == null || !properties.isObject() ? null : properties.get(e.getKey());
            Object value = convert(raw, m, format, zone);
            if (value == null) {
                continue;
            }
            if (value instanceof Instant instant) {
                instants.put(target, instant);
                payload.put(target, instant.toString());
            } else {
                payload.put(target, value);
            }
        }
        payload.putAll(spec.constants());

        Object id = payload.get(spec.idField());
        String externalId = id == null ? null : textOf(id);
        if (externalId == null || externalId.isBlank()) {
            return Mapped.rejected(REJECT_MISSING_ID);
        }
        FeatureSourceSpec.TimeSpec time = spec.time();
        Instant observedAt = null;
        if (!time.observedAtReceived()) {
            observedAt = instants.get(time.observedAt());
            if (observedAt == null) {
                return Mapped.rejected(REJECT_MISSING_OBSERVED_AT);
            }
        }
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            return Mapped.rejected(REJECT_BAD_GEOMETRY);
        }
        if (geometry.simplified()) {
            payload.put(FeatureGeometry.SIMPLIFIED_KEY, true);
            payload.put(FeatureGeometry.TOLERANCE_KEY, geometry.toleranceDeg());
        }
        NormalizedRecord record = NormalizedRecord.builder(externalId, spec.kind())
                .observedAt(observedAt)
                .eventTime(time.eventTime() == null ? null : instants.get(time.eventTime()))
                .validFrom(time.validFrom() == null ? null : instants.get(time.validFrom()))
                .validUntil(time.validUntil() == null ? null : instants.get(time.validUntil()))
                .geometry(geometry.geometry())
                .putAll(payload)
                .build();
        return new Mapped(record, null);
    }

    /**
     * One attribute value converted under its mapping, or null when it is absent, a sentinel or not convertible.
     *
     * @param raw the attribute node, or null
     * @param mapping the mapping
     * @param format the time format
     * @param zone the zone of local date-times
     */
    static Object convert(JsonNode raw, FeatureSourceSpec.FieldMapping mapping, String format, ZoneId zone) {
        if (raw == null || raw.isNull() || raw.isMissingNode() || raw.isContainer()) {
            return null;
        }
        String text = raw.isString() ? raw.stringValue().strip() : raw.asString();
        if (text == null || text.isEmpty() || isSentinel(raw, text)) {
            return null;
        }
        if (!mapping.valueMap().isEmpty() && mapping.valueMap().containsKey(text)) {
            return mapping.valueMap().get(text);
        }
        String type = mapping.type() == null ? "string" : mapping.type();
        return switch (type) {
            case "number" -> number(raw, text);
            case "integer" -> integer(raw, text);
            case "boolean" -> bool(raw, text);
            case "timestamp" -> timestamp(raw, text, format, zone);
            default -> text.length() > MAX_TEXT ? text.substring(0, MAX_TEXT) : text;
        };
    }

    /** True for the scrubbed sentinels: {@code -999}, {@code -9999} and year-1 dates. */
    static boolean isSentinel(JsonNode raw, String text) {
        if (text.startsWith("0001-01-01")) {
            return true;
        }
        if (raw.isNumber() || text.matches("^-?\\d+(\\.\\d+)?$")) {
            try {
                BigDecimal value = new BigDecimal(text);
                return value.compareTo(BigDecimal.valueOf(-999)) == 0 || value.compareTo(BigDecimal.valueOf(-9999)) == 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private static Double number(JsonNode raw, String text) {
        double value;
        if (raw.isNumber()) {
            value = raw.doubleValue();
        } else {
            try {
                value = Double.parseDouble(text.replace(",", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return Double.isFinite(value) ? value : null;
    }

    private static Long integer(JsonNode raw, String text) {
        if (raw.isIntegralNumber()) {
            return raw.longValue();
        }
        Double value = number(raw, text);
        if (value == null || value != Math.rint(value) || Math.abs(value) > 9.0e15) {
            return null;
        }
        return value.longValue();
    }

    private static Boolean bool(JsonNode raw, String text) {
        if (raw.isBoolean()) {
            return raw.booleanValue();
        }
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "y", "t" -> Boolean.TRUE;
            case "false", "0", "no", "n", "f" -> Boolean.FALSE;
            default -> null;
        };
    }

    /**
     * A timestamp under {@code format}, or null when it cannot be read.
     *
     * @param raw the attribute node
     * @param text its text
     * @param format {@code auto}, {@code epoch-ms}, {@code epoch-s}, {@code iso} or a pattern
     * @param zone the zone of local date-times
     */
    static Instant timestamp(JsonNode raw, String text, String format, ZoneId zone) {
        boolean numeric = raw.isNumber() || text.matches("^-?\\d{9,}(\\.\\d+)?$");
        Instant value;
        try {
            value = switch (format) {
                case "epoch-ms" -> numeric ? epoch(text, 1) : null;
                case "epoch-s" -> numeric ? epoch(text, 1000) : null;
                case "iso" -> iso(text, zone);
                case "auto" -> numeric ? epoch(text, 1) : auto(text, zone);
                default -> pattern(text, format, zone);
            };
        } catch (DateTimeParseException | ArithmeticException | NumberFormatException e) {
            return null;
        }
        // A date in year 1 (0001-01-01, the .NET and Esri "no date") is a sentinel, not a time.
        return value == null || value.toEpochMilli() < YEAR_TWO_EPOCH_MS ? null : value;
    }

    private static Instant epoch(String text, long multiplier) {
        BigDecimal value = new BigDecimal(text).multiply(BigDecimal.valueOf(multiplier));
        return Instant.ofEpochMilli(value.longValueExact());
    }

    private static Instant auto(String text, ZoneId zone) {
        Instant iso = iso(text, zone);
        if (iso != null) {
            return iso;
        }
        return LocalDateTime.parse(text, SPACE_PATTERN).atZone(zone).toInstant();
    }

    /** ISO-8601 with an offset, or a local date-time or date read in {@code zone}. */
    static Instant iso(String text, ZoneId zone) {
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return LocalDateTime.parse(text).atZone(zone).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return LocalDate.parse(text).atStartOfDay(zone).toInstant();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static Instant pattern(String text, String pattern, ZoneId zone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
        TemporalAccessor parsed = formatter.parseBest(text, OffsetDateTime::from, LocalDateTime::from,
                LocalDate::from);
        if (parsed instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (parsed instanceof LocalDateTime ldt) {
            return ldt.atZone(zone).toInstant();
        }
        return ((LocalDate) parsed).atStartOfDay(zone).toInstant();
    }

    private static ZoneId zone(FeatureSourceSpec spec) {
        String zone = spec.time().zone();
        return zone == null || zone.isBlank() ? ZoneOffset.UTC : ZoneId.of(zone);
    }

    private static String textOf(Object value) {
        if (value instanceof Double d && d == Math.rint(d) && Math.abs(d) < 9.0e15) {
            return Long.toString(d.longValue());
        }
        return String.valueOf(value).strip();
    }
}
