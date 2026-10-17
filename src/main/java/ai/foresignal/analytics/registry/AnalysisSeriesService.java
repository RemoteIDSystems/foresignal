package ai.foresignal.analytics.registry;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and changes the analysis-series registry (prompt 119): {@code GET /v1/analytics/series} (filters by family,
 * region and enabled flag, keyset cursor on {@code ref}), {@code PUT} (upsert by {@code ref} as an operator row) and
 * {@code DELETE}. Findings already written for a removed row are kept.
 */
@Service
public class AnalysisSeriesService {

    /** The detectors a row may name. */
    public static final List<String> DETECTORS = List.of("changepoint-cusum", "changepoint-bocpd", "forecast");

    /** The frequencies a row may have. */
    public static final List<String> FREQUENCIES = List.of("hour", "day", "week");

    /** The default detectors of a new row. */
    public static final List<String> DEFAULT_DETECTORS = List.of("changepoint-cusum", "changepoint-bocpd");

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private final AnalysisSeriesRepository rows;
    private final NamedParameterJdbcTemplate jdbc;
    private final SeriesReader reader;
    private final SignalFamilyRegistry families;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public AnalysisSeriesService(AnalysisSeriesRepository rows, NamedParameterJdbcTemplate jdbc, SeriesReader reader,
            SignalFamilyRegistry families, JsonMapper json, Clock clock) {
        this.rows = rows;
        this.jdbc = jdbc;
        this.reader = reader;
        this.families = families;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A page.
     *
     * @param items the rows
     * @param nextCursor the next cursor, or null on the last page
     */
    public record Page(List<AnalysisSeriesEntity> items, String nextCursor) {}

    /** The rows matching the filters, by {@code ref}. */
    @Transactional(readOnly = true)
    public Page list(List<String> family, List<String> region, Boolean enabled, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT ref FROM analysis_series WHERE 1 = 1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (family != null && !family.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String f : family) {
                names.addAll(families.storedNames(f));
            }
            sql.append(" AND family IN (:families)");
            params.addValue("families", names);
        }
        if (region != null && !region.isEmpty()) {
            sql.append(" AND region_code IN (:regions)");
            params.addValue("regions", region);
        }
        if (enabled != null) {
            sql.append(" AND enabled = :enabled");
            params.addValue("enabled", enabled);
        }
        String after = decode(cursor);
        if (after != null) {
            sql.append(" AND ref > :after");
            params.addValue("after", after);
        }
        sql.append(" ORDER BY ref LIMIT :limit");
        params.addValue("limit", limit + 1);
        List<String> refs = jdbc.queryForList(sql.toString(), params, String.class);
        boolean more = refs.size() > limit;
        List<String> page = more ? refs.subList(0, limit) : refs;
        List<AnalysisSeriesEntity> items = new ArrayList<>();
        for (String ref : page) {
            rows.findById(ref).ifPresent(items::add);
        }
        return new Page(items, more && !page.isEmpty() ? encode(page.get(page.size() - 1)) : null);
    }

    /**
     * The result of an upsert.
     *
     * @param row the row
     * @param created true when it did not exist
     */
    public record Upsert(AnalysisSeriesEntity row, boolean created) {}

    /**
     * Creates or replaces the row of {@code body.ref} as an operator row.
     *
     * @throws ValidationException 400 ({@code SERIES_REF_INVALID} for the reference)
     * @throws NotFoundException 404 {@code SERIES_NOT_FOUND} when the series does not exist
     */
    @Transactional
    public Upsert upsert(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an AnalysisSeriesInput object.");
        }
        SeriesRef ref = SeriesRef.parse(body.hasNonNull("ref") ? body.get("ref").asString() : null);
        List<FieldError> errors = new ArrayList<>();
        Set<String> allowed = Set.of("ref", "enabled", "detectors", "frequency", "family", "regionCode", "watchPairs");
        for (String field : body.propertyNames()) {
            if (!allowed.contains(field)) {
                errors.add(new FieldError(field, "is not an AnalysisSeriesInput member"));
            }
        }
        if (!reader.exists(ref)) {
            throw new NotFoundException(ErrorCodes.SERIES_NOT_FOUND, "No series '" + ref.format() + "'.");
        }
        SeriesReader.Meta meta = reader.meta(ref);
        Boolean enabled = null;
        if (body.has("enabled") && !body.get("enabled").isNull()) {
            if (!body.get("enabled").isBoolean()) {
                errors.add(new FieldError("enabled", "must be a boolean"));
            } else {
                enabled = body.get("enabled").asBoolean();
            }
        }
        List<String> detectors = null;
        if (body.has("detectors") && !body.get("detectors").isNull()) {
            detectors = new ArrayList<>();
            if (!body.get("detectors").isArray()) {
                errors.add(new FieldError("detectors", "must be an array"));
            } else {
                for (JsonNode d : body.get("detectors")) {
                    if (!d.isString() || !DETECTORS.contains(d.asString())) {
                        errors.add(new FieldError("detectors", "must contain only " + DETECTORS));
                    } else if (!detectors.contains(d.asString())) {
                        detectors.add(d.asString());
                    }
                }
            }
        }
        String frequency = null;
        if (body.hasNonNull("frequency")) {
            frequency = body.get("frequency").asString();
            if (!FREQUENCIES.contains(frequency)) {
                errors.add(new FieldError("frequency", "must be one of " + FREQUENCIES));
            }
        }
        String family = null;
        if (body.hasNonNull("family")) {
            family = families.canonical(body.get("family").asString()).orElse(null);
            if (family == null) {
                errors.add(new FieldError("family", "is not a signal family of the registry"));
            }
        }
        String region = null;
        if (body.hasNonNull("regionCode")) {
            region = body.get("regionCode").asString();
            if (!REGION.matcher(region).matches()) {
                errors.add(new FieldError("regionCode", "is not a 040 region code"));
            }
        }
        List<Map<String, Object>> pairs = null;
        if (body.has("watchPairs") && !body.get("watchPairs").isNull()) {
            pairs = watchPairs(body.get("watchPairs"), errors);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The analysis series is invalid.", errors);
        }
        Instant now = clock.instant();
        AnalysisSeriesEntity row = rows.findById(ref.format()).orElse(null);
        boolean created = row == null;
        if (created) {
            row = new AnalysisSeriesEntity(ref.format(), "operator", now);
        }
        row.setOrigin("operator");
        row.setEnabled(enabled == null ? (created || row.isEnabled()) : enabled);
        row.setDetectors(detectors != null ? detectors : created ? DEFAULT_DETECTORS : row.getDetectors());
        row.setFrequency(frequency != null ? frequency : created ? defaultFrequency(meta) : row.getFrequency());
        row.setFamily(family != null ? family : created || row.getFamily() == null ? meta.signalFamily()
                : row.getFamily());
        row.setRegionCode(region != null ? region : created || row.getRegionCode() == null ? meta.regionCode()
                : row.getRegionCode());
        if (pairs != null) {
            row.setWatchPairs(pairs);
        } else if (created) {
            row.setWatchPairs(List.of());
        }
        row.setUpdatedAt(now);
        rows.save(row);
        return new Upsert(row, created);
    }

    /** The registry frequency of a series: its own when hour, day or week; day otherwise. */
    static String defaultFrequency(SeriesReader.Meta meta) {
        SeriesFrequency f = meta.frequency();
        return f == SeriesFrequency.WEEK ? "week" : "day";
    }

    private List<Map<String, Object>> watchPairs(JsonNode node, List<FieldError> errors) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (!node.isArray()) {
            errors.add(new FieldError("watchPairs", "must be an array"));
            return out;
        }
        int i = 0;
        for (JsonNode item : node) {
            String field = "watchPairs[" + i++ + "]";
            if (!item.isObject() || !item.hasNonNull("eventFamily")) {
                errors.add(new FieldError(field + ".eventFamily", "is required"));
                continue;
            }
            String family = families.canonical(item.get("eventFamily").asString()).orElse(null);
            if (family == null) {
                errors.add(new FieldError(field + ".eventFamily", "is not a signal family of the registry"));
                continue;
            }
            List<String> regions = new ArrayList<>();
            if (item.has("regionCodes") && item.get("regionCodes").isArray()) {
                for (JsonNode r : item.get("regionCodes")) {
                    if (!r.isString() || !REGION.matcher(r.asString()).matches()) {
                        errors.add(new FieldError(field + ".regionCodes", "must contain 040 region codes"));
                    } else {
                        regions.add(r.asString());
                    }
                }
            }
            double effect = WatchPair.DEFAULT_MIN_EFFECT;
            if (item.hasNonNull("minEffect")) {
                if (!item.get("minEffect").isNumber() || !(item.get("minEffect").asDouble() > 0)) {
                    errors.add(new FieldError(field + ".minEffect", "must be a positive number"));
                } else {
                    effect = item.get("minEffect").asDouble();
                }
            }
            Integer lag = null;
            if (item.hasNonNull("releaseLagDays")) {
                if (!item.get("releaseLagDays").canConvertToInt() || item.get("releaseLagDays").asInt() < 0) {
                    errors.add(new FieldError(field + ".releaseLagDays", "must be a non-negative integer"));
                } else {
                    lag = item.get("releaseLagDays").asInt();
                }
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> selector = item.hasNonNull("selector") && item.get("selector").isObject()
                    ? json.convertValue(item.get("selector"), Map.class) : null;
            out.add(new WatchPair(family, regions, selector, effect, lag).toMap());
        }
        return out;
    }

    /**
     * Removes a row.
     *
     * @throws ValidationException 400 {@code SERIES_REF_INVALID}
     * @throws NotFoundException 404 {@code SERIES_NOT_FOUND} when it is not registered
     */
    @Transactional
    public void delete(String refText) {
        SeriesRef ref = SeriesRef.parse(refText);
        AnalysisSeriesEntity row = rows.findById(ref.format()).orElseThrow(() -> new NotFoundException(
                ErrorCodes.SERIES_NOT_FOUND, "'" + ref.format() + "' is not registered."));
        rows.delete(row);
    }

    /** Records a detector run on a row. */
    @Transactional
    public void markRun(String ref, Instant at, String error) {
        rows.findById(ref).ifPresent(row -> {
            row.setLastRunAt(at);
            row.setLastError(error == null ? null : error.length() > 2000 ? error.substring(0, 2000) : error);
            rows.save(row);
        });
    }

    /** The {@code AnalysisSeries} JSON of a row. */
    public static Map<String, Object> view(AnalysisSeriesEntity row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", row.getRef());
        out.put("enabled", row.isEnabled());
        out.put("detectors", row.getDetectors());
        out.put("frequency", row.getFrequency());
        out.put("family", row.getFamily());
        out.put("regionCode", row.getRegionCode());
        out.put("watchPairs", row.getWatchPairs());
        out.put("origin", row.getOrigin());
        out.put("createdAt", row.getCreatedAt() == null ? null : row.getCreatedAt().toString());
        out.put("lastRunAt", row.getLastRunAt() == null ? null : row.getLastRunAt().toString());
        out.put("lastError", row.getLastError());
        return out;
    }

    /** The {@code AnalysisSeriesPage} JSON. */
    public static Map<String, Object> view(Page page) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(AnalysisSeriesService::view).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    private static String encode(String ref) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ref.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor of this list");
        }
    }
}
