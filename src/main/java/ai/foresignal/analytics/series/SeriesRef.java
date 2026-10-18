package ai.foresignal.analytics.series;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A series reference (prompt 119, Intelligence-Design §4.3):
 *
 * <ul>
 *   <li>{@code series:{sourceId}/{seriesId}}: the {@code series-point} records of one 093/117 series (and the PortWatch
 *       chokepoint transits, {@code series:imf-portwatch-chokepoints/{portId}});
 *   <li>{@code rollup:{hour|day}/{sourceId}/{kind}/{region|cell:h3}}: activity rollup rows, by 040 region (resolutions
 *       2 and 3) or by H3 cell (resolutions 0 and 1); {@code sourceId} {@code *} is the cross-source event count;
 *   <li>{@code rule:{ruleId}/{groupKey}}: the 100 anomaly rule's {@code activity_sample} rows of one group;
 *   <li>{@code rollup:news/{topicId|situationId}/{bucket}}: the daily news coverage of an attention topic or a
 *       situation in one bucket ({@code left}, {@code center}, {@code right}, {@code unrated}, {@code state}), in
 *       evidence groups (prompt 133).
 * </ul>
 *
 * @param type the reference type
 * @param sourceId the source ({@code series}, {@code rollup}), or null
 * @param seriesId the series id ({@code series}), or the topic or situation id ({@code news}), or null
 * @param resolution {@code hour} or {@code day} ({@code rollup}), or null
 * @param kind the record kind ({@code rollup}), or null
 * @param region the 040 region code ({@code rollup} by region), or null
 * @param cell the H3 address ({@code rollup} by cell), or null
 * @param ruleId the anomaly rule ({@code rule}), or null
 * @param groupKey the rule group ({@code rule}), or the coverage bucket ({@code news}), or null
 */
public record SeriesRef(Type type, String sourceId, String seriesId, String resolution, String kind, String region,
        String cell, UUID ruleId, String groupKey) {

    /** The reference types. */
    public enum Type {
        /** {@code series:} */
        SERIES,
        /** {@code rollup:} */
        ROLLUP,
        /** {@code rule:} */
        RULE,
        /** {@code rollup:news/} (prompt 133) */
        NEWS
    }

    /** The news coverage buckets of {@code rollup:news/} references (prompt 133). */
    public static final List<String> NEWS_BUCKETS = List.of("left", "center", "right", "unrated", "state");

    private static final Pattern NEWS_SCOPE = Pattern.compile("^([a-z0-9-]{3,48}|[0-9A-HJKMNP-TV-Z]{26})$");

    /** The cross-source pseudo source of rollup references. */
    public static final String ALL_SOURCES = "*";

    private static final Pattern SOURCE = Pattern.compile("^(\\*|[a-z0-9][a-z0-9._-]{0,63})$");
    private static final Pattern KIND = Pattern.compile("^[a-z0-9][a-z0-9._-]{0,63}$");
    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");
    private static final Pattern H3 = Pattern.compile("^[0-9a-f]{15,16}$");

    /** The reference of one 093/117 series. */
    public static SeriesRef series(String sourceId, String seriesId) {
        return new SeriesRef(Type.SERIES, sourceId, seriesId, null, null, null, null, null, null);
    }

    /** The reference of a region rollup. */
    public static SeriesRef rollup(String resolution, String sourceId, String kind, String region) {
        return new SeriesRef(Type.ROLLUP, sourceId, null, resolution, kind, region, null, null, null);
    }

    /** The reference of an H3 cell rollup. */
    public static SeriesRef cellRollup(String resolution, String sourceId, String kind, String cell) {
        return new SeriesRef(Type.ROLLUP, sourceId, null, resolution, kind, null, cell, null, null);
    }

    /** The reference of a daily news-coverage series (prompt 133). */
    public static SeriesRef news(String scopeId, String bucket) {
        return new SeriesRef(Type.NEWS, null, scopeId, "day", null, null, null, null, bucket);
    }

    /** The reference of an anomaly rule group. */
    public static SeriesRef rule(UUID ruleId, String groupKey) {
        return new SeriesRef(Type.RULE, null, null, null, null, null, null, ruleId, groupKey);
    }

    /**
     * Parses a reference.
     *
     * @throws ValidationException 400 {@code SERIES_REF_INVALID}
     */
    public static SeriesRef parse(String text) {
        if (text == null || text.isBlank()) {
            throw invalid(text, "is required");
        }
        String value = text.strip();
        int colon = value.indexOf(':');
        if (colon < 0) {
            throw invalid(value, "must start with series:, rollup: or rule:");
        }
        String scheme = value.substring(0, colon);
        String rest = value.substring(colon + 1);
        switch (scheme) {
            case "series" -> {
                int slash = rest.indexOf('/');
                if (slash <= 0 || slash == rest.length() - 1) {
                    throw invalid(value, "must be series:{sourceId}/{seriesId}");
                }
                String source = rest.substring(0, slash);
                if (!SOURCE.matcher(source).matches() || ALL_SOURCES.equals(source)) {
                    throw invalid(value, "has an invalid source id");
                }
                return series(source, rest.substring(slash + 1));
            }
            case "rollup" -> {
                String[] parts = rest.split("/", -1);
                if (parts.length == 3 && "news".equals(parts[0])) {
                    if (!NEWS_SCOPE.matcher(parts[1]).matches()) {
                        throw invalid(value, "names no attention topic or situation id");
                    }
                    if (!NEWS_BUCKETS.contains(parts[2])) {
                        throw invalid(value, "has a coverage bucket other than " + NEWS_BUCKETS);
                    }
                    return news(parts[1], parts[2]);
                }
                if (parts.length != 4) {
                    throw invalid(value, "must be rollup:{hour|day}/{sourceId}/{kind}/{region|cell:h3}");
                }
                if (!"hour".equals(parts[0]) && !"day".equals(parts[0])) {
                    throw invalid(value, "has a resolution other than hour or day");
                }
                if (!SOURCE.matcher(parts[1]).matches()) {
                    throw invalid(value, "has an invalid source id");
                }
                if (!KIND.matcher(parts[2]).matches()) {
                    throw invalid(value, "has an invalid kind");
                }
                if (parts[3].startsWith("cell:")) {
                    String cell = parts[3].substring(5);
                    if (!H3.matcher(cell).matches()) {
                        throw invalid(value, "has an invalid H3 address");
                    }
                    return cellRollup(parts[0], parts[1], parts[2], cell);
                }
                if (!REGION.matcher(parts[3]).matches()) {
                    throw invalid(value, "has an invalid 040 region code");
                }
                return rollup(parts[0], parts[1], parts[2], parts[3]);
            }
            case "rule" -> {
                int slash = rest.indexOf('/');
                if (slash <= 0 || slash == rest.length() - 1) {
                    throw invalid(value, "must be rule:{ruleId}/{groupKey}");
                }
                try {
                    return rule(UUID.fromString(rest.substring(0, slash)), rest.substring(slash + 1));
                } catch (IllegalArgumentException e) {
                    throw invalid(value, "has a rule id that is not a UUID");
                }
            }
            case "social" -> throw invalid(value, "social series arrive with prompt 135");
            default -> throw invalid(value, "must start with series:, rollup: or rule:");
        }
    }

    /** The canonical text form. */
    public String format() {
        return switch (type) {
            case SERIES -> "series:" + sourceId + "/" + seriesId;
            case ROLLUP -> "rollup:" + resolution + "/" + sourceId + "/" + kind + "/"
                    + (cell != null ? "cell:" + cell : region);
            case RULE -> "rule:" + ruleId + "/" + groupKey;
            case NEWS -> "rollup:news/" + seriesId + "/" + groupKey;
        };
    }

    /** True for the cross-source rollup ({@code *}). */
    public boolean allSources() {
        return type == Type.ROLLUP && ALL_SOURCES.equals(sourceId);
    }

    /** The 040 region this reference covers, or null (rule groups that are region codes count). */
    public String regionCode() {
        if (type == Type.ROLLUP) {
            return region;
        }
        if (type == Type.RULE && groupKey != null && REGION.matcher(groupKey).matches()) {
            return groupKey;
        }
        return null;
    }

    @Override
    public String toString() {
        return format();
    }

    private static ValidationException invalid(String value, String message) {
        return new ValidationException(ErrorCodes.SERIES_REF_INVALID, "The series reference '" + value + "' " + message
                + ".", List.of(new FieldError("ref", message)));
    }
}
