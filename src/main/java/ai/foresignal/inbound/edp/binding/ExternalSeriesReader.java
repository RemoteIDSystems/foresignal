package ai.foresignal.inbound.edp.binding;

import ai.foresignal.analytics.series.ExternalSeriesSource;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.analytics.series.SeriesValue;
import ai.foresignal.inbound.edp.EdpPeriods;
import ai.foresignal.inbound.edp.EdpProtocol;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The series of EDP observation streams bound with target {@code series} (prompt 138):
 * {@code series:ext-{id}/{kind}/{seriesKey}}, read by the 093/119 {@link SeriesReader} through
 * {@link ExternalSeriesSource}.
 *
 * <ul>
 *   <li>A point is the latest stored version of each period ({@code t}, an EDP period: a UTC interval
 *       {@code [start, end)}); its {@code knownAt} is the item's {@code effectiveKnownAt} ({@code max(knownAt,
 *       receipt)}, EXT-4), so as-of reads and look-ahead guards never see a value before Foresignal had it.
 *   <li>The frequency follows the periods ({@code day}, {@code week}, {@code month}; years, quarters and instants are
 *       irregular); the unit, signal family, region and release lag come from the binding, else the stream.
 *   <li>The transform ({@code none}, {@code log1p}, {@code diff}) is reported as the 117 transform hint
 *       ({@code level}, {@code log-level}, {@code difference}) that detectors apply; values are served as delivered.
 * </ul>
 */
@Component
public class ExternalSeriesReader implements ExternalSeriesSource {

    /** How far back registry candidates count points. */
    static final Duration LOOKBACK = Duration.ofDays(90);

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final ExternalBindingRepository bindings;
    private final BindingStreams streams;
    private final JsonMapper json;

    /** Creates the reader. */
    public ExternalSeriesReader(JdbcTemplate jdbc, ExternalBindingRepository bindings, BindingStreams streams,
            JsonMapper json) {
        this.jdbc = jdbc;
        this.bindings = bindings;
        this.streams = streams;
        this.json = json;
    }

    @Override
    public boolean handles(String sourceId) {
        return sourceId != null && sourceId.startsWith(EdpProtocol.SOURCE_PREFIX);
    }

    /** A bound series: the binding, its stream and the series key. */
    record Bound(ExternalBindingEntity binding, BoundStream stream, String seriesKey) {}

    /** The enabled series binding behind {@code ref}, or empty. */
    Optional<Bound> bound(SeriesRef ref) {
        String seriesId = ref.seriesId();
        int slash = seriesId == null ? -1 : seriesId.indexOf('/');
        if (slash <= 0 || slash == seriesId.length() - 1) {
            return Optional.empty();
        }
        String kind = seriesId.substring(0, slash);
        String key = seriesId.substring(slash + 1);
        for (ExternalBindingEntity b : bindings.findBySourceIdOrderByCreatedAtAscIdAsc(ref.sourceId())) {
            if (!BindingVocabulary.SERIES.equals(b.getTarget()) || !b.routable()) {
                continue;
            }
            Optional<BoundStream> stream = streams.stream(b);
            if (stream.isPresent() && kind.equals(stream.get().kind())) {
                return Optional.of(new Bound(b, stream.get(), key));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<SeriesReader.Meta> meta(SeriesRef ref) {
        Optional<Bound> bound = bound(ref);
        if (bound.isEmpty()) {
            return Optional.empty();
        }
        Bound b = bound.get();
        List<Map<String, Object>> latest = jdbc.queryForList("SELECT payload->>'t' AS t, payload->>'unit' AS unit"
                + " FROM record_head WHERE source_id = ? AND kind = ? AND external_id LIKE ? ESCAPE '\\'"
                + " AND tombstone = false AND payload->>'seriesKey' = ? ORDER BY seq DESC LIMIT 1", ref.sourceId(),
                b.stream().kind(), BindingRecords.like(b.stream().streamId()) + ":%", b.seriesKey());
        if (latest.isEmpty()) {
            return Optional.empty();
        }
        Object t = latest.get(0).get("t");
        EdpPeriods.Period period = EdpPeriods.parse(t == null ? null : t.toString());
        Map<String, Object> config = b.binding().getConfig();
        String unit = BindingConfig.text(config, "unit");
        if (unit == null) {
            unit = b.stream().stream().unit();
        }
        if (unit == null && latest.get(0).get("unit") != null) {
            unit = latest.get(0).get("unit").toString();
        }
        Double lag = BindingConfig.number(config, "releaseLagDays");
        if (lag == null) {
            lag = b.stream().releaseLagDays();
        }
        return Optional.of(new SeriesReader.Meta(frequency(period), unit, BindingConfig.text(config, "family"),
                BindingConfig.text(config, "seriesFamily"), transformHint(BindingConfig.text(config, "transform")),
                lag == null ? 0 : lag, BindingConfig.text(config, "regionCode"), null, Set.of(ref.sourceId())));
    }

    @Override
    public List<SeriesValue> points(SeriesRef ref, SeriesReader.Meta meta, Instant from, Instant to, Instant asOf) {
        Optional<Bound> bound = bound(ref);
        if (bound.isEmpty()) {
            return List.of();
        }
        Bound b = bound.get();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ON (v.payload->>'t') v.version, v.ingested_at,"
                + " v.payload::text AS payload FROM record_version v WHERE v.source_id = ? AND v.kind = ?"
                + " AND v.external_id LIKE ? ESCAPE '\\' AND v.tombstone = false AND v.payload->>'seriesKey' = ?");
        List<Object> args = new ArrayList<>(List.of(ref.sourceId(), b.stream().kind(),
                BindingRecords.like(b.stream().streamId()) + ":%", b.seriesKey()));
        if (asOf != null) {
            sql.append(" AND v.ingested_at <= ?");
            args.add(Timestamp.from(asOf));
        }
        sql.append(" ORDER BY v.payload->>'t', v.seq DESC");
        List<SeriesValue> out = new ArrayList<>();
        jdbc.query(sql.toString(), rs -> {
            Map<String, Object> payload = json.readValue(rs.getString("payload"), MAP);
            OffsetDateTime ingested = rs.getObject("ingested_at", OffsetDateTime.class);
            SeriesValue point = point(payload, ingested == null ? null : ingested.toInstant(),
                    (Integer) rs.getObject("version"), from, to, asOf);
            if (point != null) {
                out.add(point);
            }
        }, args.toArray());
        return out;
    }

    /**
     * One stored observation as a point, or null when its period is unreadable, outside {@code [from, to)} or not
     * known by {@code asOf}. Pure.
     */
    static SeriesValue point(Map<String, Object> payload, Instant ingestedAt, Integer version, Instant from,
            Instant to, Instant asOf) {
        Object t = payload.get("t");
        EdpPeriods.Period period = EdpPeriods.parse(t == null ? null : t.toString());
        if (period == null || period.start().isBefore(from) || !period.start().isBefore(to)) {
            return null;
        }
        BoundItem item = new BoundItem(0, "", "", "", version, null, null, ingestedAt, null, null, payload);
        Instant knownAt = item.effective("effectiveKnownAt");
        if (knownAt == null) {
            knownAt = ingestedAt;
        }
        if (asOf != null && knownAt != null && knownAt.isAfter(asOf)) {
            return null;
        }
        Object value = payload.get("value");
        Double v = value instanceof Number n && Double.isFinite(n.doubleValue()) ? n.doubleValue() : null;
        Instant end = period.end().equals(period.start()) ? period.start() : period.end();
        return new SeriesValue(period.start(), end, v, knownAt, true, t.toString(), version, null);
    }

    @Override
    public List<Candidate> candidates(Instant now) {
        List<Candidate> out = new ArrayList<>();
        for (ExternalBindingEntity b : bindings.findByTargetOrderByCreatedAtAscIdAsc(BindingVocabulary.SERIES)) {
            if (!b.routable()) {
                continue;
            }
            Optional<BoundStream> stream = streams.stream(b);
            if (stream.isEmpty()) {
                continue;
            }
            BoundStream s = stream.get();
            jdbc.query("SELECT payload->>'seriesKey' AS key, max(payload->>'t') AS t, count(*) AS n FROM record_head"
                    + " WHERE source_id = ? AND kind = ? AND external_id LIKE ? ESCAPE '\\' AND tombstone = false"
                    + " AND observed_at >= ? GROUP BY 1", rs -> {
                        String key = rs.getString("key");
                        EdpPeriods.Period period = EdpPeriods.parse(rs.getString("t"));
                        String frequency = registryFrequency(period);
                        if (key != null && frequency != null) {
                            out.add(new Candidate(ref(b.getSourceId(), s.kind(), key), frequency,
                                    BindingConfig.text(b.getConfig(), "family"),
                                    BindingConfig.text(b.getConfig(), "regionCode"), rs.getDouble("n")));
                        }
                    }, b.getSourceId(), s.kind(), BindingRecords.like(s.streamId()) + ":%",
                    Timestamp.from(now.minus(LOOKBACK)));
        }
        return out;
    }

    /** {@code series:{sourceId}/{kind}/{seriesKey}}. */
    public static String ref(String sourceId, String kind, String seriesKey) {
        return SeriesRef.series(sourceId, kind + "/" + seriesKey).format();
    }

    /** The series frequency of a period's granularity. Pure. */
    static SeriesFrequency frequency(EdpPeriods.Period period) {
        if (period == null) {
            return SeriesFrequency.IRREGULAR;
        }
        return switch (period.granularity()) {
            case "day" -> SeriesFrequency.DAY;
            case "week" -> SeriesFrequency.WEEK;
            case "month" -> SeriesFrequency.MONTH;
            default -> SeriesFrequency.IRREGULAR;
        };
    }

    /** The 119 registry frequency of a period ({@code day} or {@code week}), or null when it cannot be analyzed. */
    static String registryFrequency(EdpPeriods.Period period) {
        if (period == null) {
            return null;
        }
        return switch (period.granularity()) {
            case "day" -> "day";
            case "week" -> "week";
            default -> null;
        };
    }

    /** The 117 transform hint of a binding transform. Pure. */
    static String transformHint(String transform) {
        if (transform == null) {
            return "level";
        }
        return switch (transform) {
            case "log1p" -> "log-level";
            case "diff" -> "difference";
            default -> "level";
        };
    }
}
