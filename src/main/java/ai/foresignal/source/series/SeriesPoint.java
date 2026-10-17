package ai.foresignal.source.series;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The builder every energy and markets adapter uses to emit one series data point (prompt 093).
 *
 * <p>A point is a record without geometry: {@code kind = series-point}, {@code externalId = {seriesId}:{period}},
 * {@code eventTime = periodStart}. Its payload carries the series id and name, the dataset, a fixed frequency code
 * ({@code PT15S|PT30M|PT1H|P1D|P1W|P1M}), the upstream period label, ISO start and end, the numeric value, a UCUM
 * unit and a status. Each series also has one {@code series-latest} record, {@code {seriesId}:latest}, carrying the
 * newest non-forecast point, so edge rules work on a single entity per series.
 *
 * <p>Numbers arrive as strings from EIA, GIE and Ember; {@link #number(Object)} parses them and rejects NaN and
 * infinity, so a malformed value becomes null rather than a poisoned hash.
 */
public final class SeriesPoint {

    /** The point record kind. */
    public static final String KIND = "series-point";

    /** The per-series catalogue record kind. */
    public static final String LATEST_KIND = "series-latest";

    /** The accepted frequency codes ({@code P3M} and {@code P1Y} added by prompt 117 for FRED and the World Bank). */
    public static final Set<String> FREQUENCIES = Set.of("PT15S", "PT15M", "PT30M", "PT1H", "P1D", "P1W", "P1M",
            "P3M", "P1Y");

    /** The series analytic class's default raw retention (Conventions "Analytic retention"). */
    public static final Duration RETENTION = Duration.ofDays(3650);

    /** The most {@code extra} keys a payload carries. */
    public static final int MAX_EXTRA_KEYS = 16;

    private SeriesPoint() {}

    /**
     * A number from an upstream value, which may be a JSON number or a numeric string (EIA, GIE and Ember send
     * strings).
     *
     * @return the finite value, or null when it is missing, blank, unparseable, NaN or infinite
     */
    public static Double number(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            double d = n.doubleValue();
            return Double.isFinite(d) ? d : null;
        }
        if (raw instanceof String s) {
            String text = s.strip();
            if (text.isEmpty()) {
                return null;
            }
            try {
                double d = Double.parseDouble(text);
                return Double.isFinite(d) ? d : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** One {@code series-point} record, its external id derived from {@code seriesId} and {@code period}. */
    public static NormalizedRecord point(
            String seriesId,
            String seriesName,
            String dataset,
            String frequency,
            String period,
            Instant periodStart,
            Instant periodEnd,
            Double value,
            String unit,
            String area,
            Map<String, Object> extra,
            String status,
            Instant observedAt) {
        Map<String, Object> payload = base(seriesId, seriesName, dataset, frequency, area);
        payload.put("period", period);
        payload.put("periodStart", periodStart.toString());
        payload.put("periodEnd", periodEnd.toString());
        payload.put("value", value);
        payload.put("unit", unit);
        payload.put("status", status);
        putExtra(payload, extra);
        return NormalizedRecord.builder(seriesId + ":" + period, KIND)
                .observedAt(observedAt)
                .eventTime(periodStart)
                .putAll(payload)
                .build();
    }

    /** The per-series {@code series-latest} record, {@code {seriesId}:latest}, carrying the newest non-forecast point. */
    public static NormalizedRecord latest(
            String seriesId,
            String seriesName,
            String dataset,
            String frequency,
            String period,
            Double value,
            String unit,
            String area,
            String status,
            Instant observedAt) {
        Map<String, Object> payload = base(seriesId, seriesName, dataset, frequency, area);
        payload.put("period", period);
        payload.put("value", value);
        payload.put("unit", unit);
        payload.put("status", status);
        return NormalizedRecord.builder(seriesId + ":latest", LATEST_KIND)
                .observedAt(observedAt)
                .putAll(payload)
                .build();
    }

    /**
     * One {@code series-latest} record per series, carrying the newest <em>non-forecast</em> point. A series whose
     * only points are forecasts gets no latest record.
     */
    public static List<NormalizedRecord> latestPerSeries(List<NormalizedRecord> points) {
        return latestPerSeries(points, point -> null);
    }

    /**
     * One {@code series-latest} record per series, carrying the newest <em>non-forecast</em> point, with the series
     * metadata of prompt 117 ({@link SeriesMeta}) added to its payload.
     *
     * @param points the points of one fetch
     * @param metadata the metadata of the series a point belongs to; null leaves that series' latest record without
     *     metadata
     */
    public static List<NormalizedRecord> latestPerSeries(List<NormalizedRecord> points,
            java.util.function.Function<NormalizedRecord, SeriesMeta> metadata) {
        Map<String, NormalizedRecord> newest = new java.util.LinkedHashMap<>();
        for (NormalizedRecord point : points) {
            if ("forecast".equals(String.valueOf(point.payload().get("status")))) {
                continue;
            }
            String series = String.valueOf(point.payload().get("series"));
            NormalizedRecord prior = newest.get(series);
            if (prior == null || isNewer(point, prior)) {
                newest.put(series, point);
            }
        }
        List<NormalizedRecord> out = new java.util.ArrayList<>();
        for (NormalizedRecord point : newest.values()) {
            SeriesMeta meta = metadata == null ? null : metadata.apply(point);
            NormalizedRecord latest = latest(
                    str(point, "series"),
                    str(point, "seriesName"),
                    str(point, "dataset"),
                    str(point, "frequency"),
                    str(point, "period"),
                    number(point.payload().get("value")),
                    str(point, "unit"),
                    str(point, "area"),
                    str(point, "status"),
                    point.observedAt());
            out.add(meta == null ? latest : meta.applyTo(latest));
        }
        return out;
    }

    /**
     * {@code record} with one more payload entry (prompt 117: a series' own attribution line on its
     * {@code series-latest}). A null {@code value} returns the record unchanged.
     */
    public static NormalizedRecord withPayload(NormalizedRecord record, String key, Object value) {
        if (value == null) {
            return record;
        }
        Map<String, Object> payload = new LinkedHashMap<>(record.payload());
        payload.put(key, value);
        return new NormalizedRecord(record.externalId(), record.kind(), record.observedAt(), record.eventTime(),
                record.geometry(), record.validUntil(), record.validFrom(), payload, record.tombstone(),
                record.quality(), record.observers());
    }

    private static boolean isNewer(NormalizedRecord candidate, NormalizedRecord prior) {
        Instant a = candidate.eventTime();
        Instant b = prior.eventTime();
        if (a == null) {
            return false;
        }
        return b == null || a.isAfter(b);
    }

    private static String str(NormalizedRecord record, String key) {
        Object value = record.payload().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Map<String, Object> base(
            String seriesId, String seriesName, String dataset, String frequency, String area) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("series", seriesId);
        payload.put("seriesName", seriesName);
        payload.put("dataset", dataset);
        payload.put("frequency", frequency);
        payload.put("area", area);
        return payload;
    }

    private static void putExtra(Map<String, Object> payload, Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return;
        }
        Map<String, Object> flat = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : extra.entrySet()) {
            if (flat.size() >= MAX_EXTRA_KEYS) {
                break;
            }
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof Number n && !isFinite(n)) {
                continue;
            }
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                flat.put(entry.getKey(), value);
            }
        }
        if (!flat.isEmpty()) {
            payload.put("extra", flat);
        }
    }

    private static boolean isFinite(Number n) {
        if (n instanceof Double d) {
            return Double.isFinite(d);
        }
        if (n instanceof Float f) {
            return Float.isFinite(f);
        }
        return true;
    }

    /**
     * The descriptor defaults the prompt fixes for every series source: STANDARD tier, ALL policy, no TTL,
     * {@code ttlAnchor = OBSERVED}, {@code titleField = seriesName}, {@code searchFields = [seriesName, area]} and
     * the {@code series} analytic retention.
     *
     * <p>The caller sets the {@code observedAtMode} (UPSTREAM for feeds without forecasts, RECEIVED for the ones
     * that mix forecasts and actuals); it is never switched per record.
     */
    public static SourceDescriptor.Builder seriesDefaults(SourceDescriptor.Builder builder) {
        return builder.historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .recordTtl(null)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .titleField("seriesName")
                .searchFields("seriesName", "area")
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION);
    }

    /** Declares the {@code fieldSpecs} of every payload key both series kinds carry. */
    public static void declareFieldSpecs(SourceDescriptor.Builder builder) {
        String series = "The series id, matching the record's series payload field";
        builder.fieldSpec("series", new FieldSpec(FieldType.STRING, null, "Series", series, null, Set.of()));
        builder.fieldSpec("seriesName", new FieldSpec(FieldType.STRING, null, "Series name",
                "Human-readable name of the series", null, Set.of()));
        builder.fieldSpec("dataset",
                new FieldSpec(FieldType.STRING, null, "Dataset", "Upstream dataset or route the point came from",
                        null, Set.of()));
        builder.fieldSpec("frequency", new FieldSpec(FieldType.STRING, null, "Frequency",
                "Fixed frequency code: PT15S, PT15M, PT30M, PT1H, P1D, P1W, P1M, P3M or P1Y", null, Set.of()));
        builder.fieldSpec("period", new FieldSpec(FieldType.STRING, null, "Period",
                "The upstream period label, for example 2026-10-09 or 2026-10-09T14", null, Set.of()));
        builder.fieldSpec("periodStart", new FieldSpec(FieldType.TIMESTAMP, null, "Period start",
                "ISO-8601 start of the measurement period; also the eventTime", null, Set.of()));
        builder.fieldSpec("periodEnd", new FieldSpec(FieldType.TIMESTAMP, null, "Period end",
                "ISO-8601 end of the measurement period; also the observedAt for UPSTREAM sources", null, Set.of()));
        builder.fieldSpec("value", new FieldSpec(FieldType.NUMBER, null, "Value",
                "The measured value; the unit is in the unit field", null, Set.of()));
        builder.fieldSpec("unit", new FieldSpec(FieldType.STRING, null, "Unit",
                "UCUM code of the value's unit, for example [bbl_us], MW.h, TW.h, %, g/(kW.h) or Hz", null,
                Set.of()));
        builder.fieldSpec("area", new FieldSpec(FieldType.STRING, null, "Area",
                "Area the series covers: a country code, a region or a respondent", null, Set.of()));
        builder.fieldSpec("status", new FieldSpec(FieldType.STRING, null, "Status",
                "final, preliminary, estimated or forecast", null, Set.of()));
        builder.fieldSpec("family", new FieldSpec(FieldType.STRING, null, "Family",
                "Series family (prompt 117): price, volume, open-interest, position, rate, attention, tone, count,"
                        + " index or structural", null, Set.of()));
        builder.fieldSpec("transform", new FieldSpec(FieldType.STRING, null, "Transform",
                "Transform hint for analysis (prompt 117): log-return, log-level, difference or level", null,
                Set.of()));
        builder.fieldSpec("releaseLagDays", new FieldSpec(FieldType.NUMBER, "d", "Release lag",
                "Typical delay between the period and its publication (prompt 117); the version's ingest time is its"
                        + " knownAt", null, Set.of()));
        builder.fieldSpec("egress", new FieldSpec(FieldType.STRING, null, "Egress",
                "The source's egress (OPEN or LOCAL_ONLY) when the record was made (prompt 117)", null, Set.of()));
        builder.fieldSpec("regionCode", new FieldSpec(FieldType.STRING, null, "Region",
                "The 040 region code the series covers, when it covers one (prompt 117)", null, Set.of()));
        builder.fieldSpec("topicId", new FieldSpec(FieldType.STRING, null, "Attention topic",
                "The attention topic the series belongs to (prompt 117)", null, Set.of()));
        builder.fieldSpec("attribution", new FieldSpec(FieldType.STRING, null, "Attribution",
                "The credit line the series' provider asks for, when it differs per series (prompt 117)", null,
                Set.of()));
        builder.fieldSpec("extra", new FieldSpec(FieldType.OBJECT, null, "Extra",
                "Flat scalar extras (at most " + MAX_EXTRA_KEYS + " keys), for example gasInStorageTWh or sharePct",
                null, Set.of()));
    }

    /** The display hints both series kinds share. */
    public static void declareKindDisplay(SourceDescriptor.Builder builder) {
        builder.kindDisplay(KIND, new ai.foresignal.source.spi.KindDisplay("{seriesName} {value} {unit}", null,
                "#1971C2", null));
        builder.kindDisplay(LATEST_KIND, new ai.foresignal.source.spi.KindDisplay("{seriesName} {value} {unit}",
                null, "#1971C2", null));
    }
}
