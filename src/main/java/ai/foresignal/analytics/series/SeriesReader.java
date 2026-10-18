package ai.foresignal.analytics.series;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.api.SeriesQueryService;
import ai.foresignal.api.dto.SeriesInfoDto;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.adapters.maritime.PortWatchChokepointsAdapter;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.stats.H3Cells;
import ai.foresignal.stats.RegionRollupJob;
import ai.foresignal.stats.RegionRollupResolution;
import ai.foresignal.stats.RollupCursor;
import ai.foresignal.stats.RollupJob;
import ai.foresignal.stats.RollupResolution;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.trigger.window.CoverageGate;
import ai.foresignal.trigger.window.WindowSpec;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads any {@link SeriesRef} as regular points with {@code knownAt}, {@code coverageOk} and explicit gaps (prompt 119,
 * Intelligence-Design §4.3). Every detector reads through it, so they share one coverage-aware, look-ahead-free view.
 *
 * <ul>
 *   <li><b>{@code series:}</b> the newest version of each {@code series-point} whose {@code ingested_at ≤ asOf}
 *       (forecasts and tombstones left out); {@code knownAt} is that version's ingest time. A stored point is covered;
 *       a period with no point is a gap. {@code series:imf-portwatch-chokepoints/{portId}} reads the PortWatch daily
 *       transit total ({@code nTotal}) the same way.
 *   <li><b>{@code rollup:}</b> the {@code entities} measure of activity rollup rows (distinct entities; canonical
 *       events count once in region rows). A rolled-up bucket without a row is zero; a bucket the rollup has not
 *       reached is a gap. {@code knownAt} is the bucket's close time; coverage is the 100 {@link CoverageGate} judged
 *       from {@code fetch_log} history of the involved sources over the bucket.
 *   <li><b>{@code rule:}</b> the 100 anomaly rule's {@code activity_sample} counts and coverage flags of one group,
 *       summed to hours; {@code knownAt} is the bucket end.
 * </ul>
 *
 * <p>No caller filtering happens here: controllers apply visibility and {@code LOCAL_ONLY} egress.
 */
@Component
public class SeriesReader {

    /** The most buckets one read returns. */
    public static final int MAX_BUCKETS = 20_000;

    /** The coverage share a rollup bucket needs. */
    public static final double COVERAGE_MIN = 0.8;

    /** The PortWatch chokepoint source read as a series. */
    public static final String PORTWATCH = PortWatchChokepointsAdapter.ID;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final SeriesQueryService catalog;
    private final ObjectProvider<CoverageGate> coverage;
    private final SignalFamilyRegistry families;
    private final ObjectProvider<RegionRollupJob> regionRollup;
    private final ObjectProvider<RollupJob> rollup;
    private final SeqClockQuery seqClock;
    private final ObjectProvider<EgressPolicy> egress;
    private final H3Cells cells;
    private final JsonMapper json;
    private final Clock clock;
    private final Cache<String, Boolean> coverageCache = Caffeine.newBuilder()
            .maximumSize(200_000).expireAfterWrite(Duration.ofHours(6)).build();

    /** Creates the reader. */
    public SeriesReader(NamedParameterJdbcTemplate jdbc, SeriesQueryService catalog,
            ObjectProvider<CoverageGate> coverage, SignalFamilyRegistry families,
            ObjectProvider<RegionRollupJob> regionRollup, ObjectProvider<RollupJob> rollup, SeqClockQuery seqClock,
            ObjectProvider<EgressPolicy> egress, H3Cells cells, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.catalog = catalog;
        this.coverage = coverage;
        this.families = families;
        this.regionRollup = regionRollup;
        this.rollup = rollup;
        this.seqClock = seqClock;
        this.egress = egress;
        this.cells = cells;
        this.json = json;
        this.clock = clock;
    }

    /**
     * Metadata of a series without its points.
     *
     * @param frequency the native frequency
     * @param unit the unit, or null
     * @param signalFamily the signal family, or null
     * @param seriesFamily the 117 family, or null
     * @param transform the 117 transform hint, or null
     * @param releaseLagDays the publication lag
     * @param regionCode the region, or null
     * @param topicId the attention topic, or null
     * @param sources the involved sources
     */
    public record Meta(SeriesFrequency frequency, String unit, String signalFamily, String seriesFamily,
            String transform, double releaseLagDays, String regionCode, String topicId, Set<String> sources) {}

    /** True when the series exists (a catalog entry, rollup rows, or the anomaly rule). */
    public boolean exists(SeriesRef ref) {
        try {
            meta(ref);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * The metadata of a series.
     *
     * @throws NotFoundException 404 {@code SERIES_NOT_FOUND}
     */
    public Meta meta(SeriesRef ref) {
        return switch (ref.type()) {
            case SERIES -> seriesMeta(ref);
            case ROLLUP -> rollupMeta(ref);
            case RULE -> ruleMeta(ref).meta();
            case NEWS -> newsMeta(ref);
            case SOCIAL -> socialMeta(ref);
        };
    }

    /**
     * Reads a series.
     *
     * @param from the first period (inclusive); default: {@code to} minus 400 buckets of its frequency
     * @param to the end (exclusive); default: {@code asOf}, else now
     * @param asOf the knowledge cutoff, or null for everything stored now
     * @throws NotFoundException 404 {@code SERIES_NOT_FOUND}
     * @throws ValidationException when the range is empty or holds more than {@value #MAX_BUCKETS} buckets
     */
    public SeriesData read(SeriesRef ref, Instant from, Instant to, Instant asOf) {
        Meta meta = meta(ref);
        SeriesFrequency frequency = meta.frequency();
        Instant end = to != null ? to : asOf != null ? asOf : clock.instant();
        Instant start = from != null ? from : end.minus(frequency.approximateWidth().multipliedBy(400));
        if (!start.isBefore(end)) {
            throw ValidationException.forField("to", "must be after from");
        }
        if (frequency.regular()) {
            start = frequency.floor(start);
            long buckets = Duration.between(start, end).dividedBy(frequency.approximateWidth());
            if (buckets > MAX_BUCKETS) {
                throw ValidationException.forField("from", "the range holds " + buckets + " buckets, more than "
                        + MAX_BUCKETS);
            }
        }
        List<SeriesValue> points = switch (ref.type()) {
            case SERIES -> external(ref.sourceId()) != null
                    ? regularize(frequency, external(ref.sourceId()).points(ref, meta, start, end, asOf))
                    : PORTWATCH.equals(ref.sourceId()) ? portWatchPoints(ref, meta, start, end, asOf)
                    : seriesPoints(ref, meta, start, end, asOf);
            case ROLLUP -> rollupPoints(ref, start, end, asOf);
            case RULE -> rulePoints(ref, start, end, asOf);
            case NEWS -> newsPoints(ref, start, end, asOf);
            case SOCIAL -> socialPoints(ref, start, end, asOf);
        };
        long asOfSeq = asOf == null ? seqClock.seqAt(jdbc, clock.instant()) : seqClock.seqAt(jdbc, asOf);
        boolean localOnly = false;
        EgressPolicy policy = egress.getIfAvailable();
        if (policy != null) {
            for (String source : meta.sources()) {
                localOnly |= policy.isLocalOnly(source);
            }
        }
        List<Instant[]> gaps = frequency.regular() ? gapsOf(frequency, start, end, points) : List.of();
        return new SeriesData(ref, start, end, asOf, asOfSeq, frequency, meta.unit(), meta.signalFamily(),
                meta.seriesFamily(), meta.transform(), meta.releaseLagDays(), meta.regionCode(), meta.topicId(),
                meta.sources(), localOnly, points, gaps);
    }

    /** Drops cached coverage judgments (tests). */
    public void clearCache() {
        coverageCache.invalidateAll();
    }

    // ------------------------------------------------------------------------- series:

    /** The series of external (EDP) streams bound with target {@code series} (prompt 138), when present. */
    @org.springframework.beans.factory.annotation.Autowired
    private ObjectProvider<ExternalSeriesSource> externalSeries;

    /** The external series source answering for {@code sourceId}, or null. */
    private ExternalSeriesSource external(String sourceId) {
        ExternalSeriesSource source = externalSeries == null ? null : externalSeries.getIfAvailable();
        return source != null && source.handles(sourceId) ? source : null;
    }

    private Meta seriesMeta(SeriesRef ref) {
        ExternalSeriesSource external = external(ref.sourceId());
        if (external != null) {
            return external.meta(ref).orElseThrow(() -> notFound(ref));
        }
        if (PORTWATCH.equals(ref.sourceId())) {
            List<String> names = jdbc.query("SELECT payload->>'chokepoint' FROM record_head WHERE source_id = :source"
                    + " AND kind = :kind AND external_id LIKE :prefix ESCAPE '\\' LIMIT 1",
                    new MapSqlParameterSource().addValue("source", PORTWATCH)
                            .addValue("kind", PortWatchChokepointsAdapter.KIND)
                            .addValue("prefix", like(ref.seriesId()) + ":%"),
                    (rs, n) -> rs.getString(1));
            if (names.isEmpty()) {
                throw notFound(ref);
            }
            return new Meta(SeriesFrequency.DAY, "vessels", family(PORTWATCH, PortWatchChokepointsAdapter.KIND),
                    "count", "log-level", 7, null, null, Set.of(PORTWATCH));
        }
        SeriesInfoDto info = catalog.info(ref.sourceId(), ref.seriesId(), SeriesQueryService.Visibility.ALL)
                .orElseThrow(() -> notFound(ref));
        String signal = families.familyOfRecord(ref.sourceId(), SeriesPoint.KIND).orElse(null);
        if ("attention".equals(info.family()) || "tone".equals(info.family())) {
            signal = "attention";
        }
        return new Meta(SeriesFrequency.ofIsoPeriod(info.frequency()), info.unit(), signal, info.family(),
                info.transform(), info.releaseLagDays() == null ? 0 : info.releaseLagDays(), info.regionCode(),
                info.topicId(), Set.of(ref.sourceId()));
    }

    private List<SeriesValue> seriesPoints(SeriesRef ref, Meta meta, Instant from, Instant to, Instant asOf) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("source", ref.sourceId())
                .addValue("prefix", like(ref.seriesId()) + ":%")
                .addValue("kind", SeriesPoint.KIND)
                .addValue("from", Timestamp.from(from.minus(meta.frequency().approximateWidth())))
                .addValue("to", Timestamp.from(to));
        String knownClause = "";
        if (asOf != null) {
            knownClause = " AND v.ingested_at <= :asOf";
            params.addValue("asOf", Timestamp.from(asOf));
        }
        List<SeriesValue> out = new ArrayList<>();
        jdbc.query("SELECT DISTINCT ON (v.external_id) v.external_id, v.event_time, v.ingested_at, v.version,"
                + " v.tombstone, v.payload::text AS payload FROM record_version v WHERE v.source_id = :source"
                + " AND v.external_id LIKE :prefix ESCAPE '\\' AND (v.kind = :kind OR v.tombstone)"
                + " AND (v.event_time IS NULL OR (v.event_time >= :from AND v.event_time < :to))" + knownClause
                + " ORDER BY v.external_id, v.seq DESC", params, rs -> {
                    if (rs.getBoolean("tombstone")) {
                        return;
                    }
                    Map<String, Object> payload = parse(rs.getString("payload"));
                    if (!ref.seriesId().equals(payload.get("series")) || "forecast".equals(payload.get("status"))) {
                        return;
                    }
                    Instant eventTime = instant(rs.getObject("event_time", OffsetDateTime.class));
                    Instant start = parseInstant(payload.get("periodStart"), eventTime);
                    if (start == null || start.isBefore(from) || !start.isBefore(to)) {
                        return;
                    }
                    Instant end = parseInstant(payload.get("periodEnd"), meta.frequency().next(start));
                    Double value = SeriesPoint.number(payload.get("value"));
                    out.add(new SeriesValue(start, end, value,
                            instant(rs.getObject("ingested_at", OffsetDateTime.class)), true,
                            payload.get("period") == null ? null : payload.get("period").toString(),
                            rs.getInt("version"), payload.get("status") == null ? null
                                    : payload.get("status").toString()));
                });
        return regularize(meta.frequency(), out);
    }

    private List<SeriesValue> portWatchPoints(SeriesRef ref, Meta meta, Instant from, Instant to, Instant asOf) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("source", PORTWATCH)
                .addValue("kind", PortWatchChokepointsAdapter.KIND)
                .addValue("prefix", like(ref.seriesId()) + ":%")
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        String knownClause = "";
        if (asOf != null) {
            knownClause = " AND v.ingested_at <= :asOf";
            params.addValue("asOf", Timestamp.from(asOf));
        }
        List<SeriesValue> out = new ArrayList<>();
        jdbc.query("SELECT DISTINCT ON (v.external_id) v.external_id, v.event_time, v.ingested_at, v.version,"
                + " v.tombstone, v.payload->>'nTotal' AS total FROM record_version v WHERE v.source_id = :source"
                + " AND v.external_id LIKE :prefix ESCAPE '\\' AND (v.kind = :kind OR v.tombstone)"
                + " AND (v.event_time IS NULL OR (v.event_time >= :from AND v.event_time < :to))" + knownClause
                + " ORDER BY v.external_id, v.seq DESC", params, rs -> {
                    if (rs.getBoolean("tombstone")) {
                        return;
                    }
                    Instant start = instant(rs.getObject("event_time", OffsetDateTime.class));
                    String total = rs.getString("total");
                    if (start == null || total == null) {
                        return;
                    }
                    Double value;
                    try {
                        value = Double.valueOf(total);
                    } catch (NumberFormatException e) {
                        return;
                    }
                    out.add(new SeriesValue(start, SeriesFrequency.DAY.next(start), value,
                            instant(rs.getObject("ingested_at", OffsetDateTime.class)), true, null,
                            rs.getInt("version"), null));
                });
        return regularize(meta.frequency(), out);
    }

    /** One point per bucket (the latest-known when two land in one), in time order. */
    static List<SeriesValue> regularize(SeriesFrequency frequency, List<SeriesValue> points) {
        TreeMap<Instant, SeriesValue> byStart = new TreeMap<>();
        for (SeriesValue p : points) {
            Instant key = frequency.regular() ? frequency.floor(p.periodStart()) : p.periodStart();
            SeriesValue existing = byStart.get(key);
            if (existing == null || (p.periodStart().isAfter(existing.periodStart()))) {
                byStart.put(key, p);
            }
        }
        return new ArrayList<>(byStart.values());
    }

    // ------------------------------------------------------------------------- social: (prompt 135)

    /** The social aggregate tables (prompt 135), when the social package is present. */
    @org.springframework.beans.factory.annotation.Autowired
    private ObjectProvider<ai.foresignal.social.SocialVolumeStore> socialVolume;

    private ai.foresignal.social.SocialVolumeStore socialStore(SeriesRef ref) {
        ai.foresignal.social.SocialVolumeStore store = socialVolume == null ? null : socialVolume.getIfAvailable();
        if (store == null) {
            throw notFound(ref);
        }
        return store;
    }

    private Meta socialMeta(SeriesRef ref) {
        ai.foresignal.social.SocialVolumeStore store = socialStore(ref);
        String scope = ref.seriesId();
        boolean all = ai.foresignal.social.SocialVocabulary.SCOPE_ALL.equals(scope);
        int colon = scope.indexOf(':');
        boolean exists = all ? store.hasDenominators(ref.sourceId())
                : store.exists(scope.substring(0, colon), scope.substring(colon + 1), ref.sourceId());
        if (!exists) {
            throw notFound(ref);
        }
        Set<String> sources = new java.util.TreeSet<>();
        for (ai.foresignal.social.SocialPlatformCatalog.Definition d
                : ai.foresignal.social.SocialPlatformCatalog.all()) {
            if (d.network().equals(ref.sourceId())) {
                sources.add(d.id());
            }
        }
        String topicId = !all && scope.startsWith(ai.foresignal.social.SocialVocabulary.SCOPE_TOPIC + ":")
                ? scope.substring(colon + 1) : null;
        String unit = "denominator".equals(ref.kind()) || "posts".equals(ref.kind()) ? "posts" : ref.kind();
        return new Meta(SeriesFrequency.HOUR, unit, "attention", "count", "log-level", 0, null, topicId, sources);
    }

    /**
     * The hourly social volume of a scope on one network (complete and paged collection only; sampled paths never
     * feed series), or its denominator. Hours from the first stored hour to the last closed one are covered; on a
     * platform with complete streams an hour whose collection was incomplete is a coverage gap. An hour is known at
     * its close.
     */
    private List<SeriesValue> socialPoints(SeriesRef ref, Instant from, Instant to, Instant asOf) {
        ai.foresignal.social.SocialVolumeStore store = socialStore(ref);
        String scope = ref.seriesId();
        boolean all = ai.foresignal.social.SocialVocabulary.SCOPE_ALL.equals(scope);
        Map<Instant, Long> values = new TreeMap<>();
        if (all) {
            values.putAll(store.denominators(ref.sourceId(), ref.groupKey(), from, to));
        } else {
            int colon = scope.indexOf(':');
            for (ai.foresignal.social.SocialVolumeStore.VolumeRow r : store.volume(scope.substring(0, colon),
                    scope.substring(colon + 1), ref.sourceId(), ref.groupKey(), from, to)) {
                if (ai.foresignal.social.SocialVocabulary.feedsBaselines(r.collectionType())) {
                    values.merge(r.hour(), r.metric(ref.kind()), Long::sum);
                }
            }
        }
        Map<Instant, Boolean> complete = store.completeHours(ref.sourceId(), from, to);
        List<SeriesValue> out = new ArrayList<>();
        if (values.isEmpty() && complete.isEmpty()) {
            return out;
        }
        Instant first = values.isEmpty() ? from : ((TreeMap<Instant, Long>) values).firstKey();
        Instant now = clock.instant();
        for (Instant t = SeriesFrequency.HOUR.floor(from); t.isBefore(to); t = SeriesFrequency.HOUR.next(t)) {
            Instant end = SeriesFrequency.HOUR.next(t);
            if (t.isBefore(first) || end.isAfter(now) || (asOf != null && end.isAfter(asOf))) {
                continue;
            }
            Boolean ok = complete.get(t);
            boolean covered = complete.isEmpty() || ok == null || ok;
            out.add(new SeriesValue(t, end, covered ? (double) values.getOrDefault(t, 0L) : null, end, covered, null,
                    null, null));
        }
        return out;
    }

    // ------------------------------------------------------------------------- rollup:news/ (prompt 133)

    private Meta newsMeta(SeriesRef ref) {
        Integer rows = jdbc.queryForObject("SELECT count(*) FROM (SELECT 1 FROM news_coverage_daily WHERE scope_id ="
                + " :scope AND bucket = :bucket LIMIT 1) x", new MapSqlParameterSource().addValue("scope",
                ref.seriesId()).addValue("bucket", ref.groupKey()), Integer.class);
        if (rows == null || rows == 0) {
            throw notFound(ref);
        }
        boolean topic = !ref.seriesId().matches("^[0-9A-HJKMNP-TV-Z]{26}$");
        return new Meta(SeriesFrequency.DAY, "evidence groups", "attention", "count", "log-level", 0, null,
                topic ? ref.seriesId() : null, Set.of(ai.foresignal.source.formats.news.NewsArticles.GDELT_SOURCE));
    }

    /**
     * The daily news coverage of a topic or situation in one bucket: evidence groups first seen that UTC day. Days
     * from the scope's first computed day to the last computed one are covered (a day without a row is 0); a day is
     * known at its close.
     */
    private List<SeriesValue> newsPoints(SeriesRef ref, Instant from, Instant to, Instant asOf) {
        Map<Instant, Long> values = new TreeMap<>();
        Instant[] range = new Instant[2];
        jdbc.query("SELECT day, evidence_groups FROM news_coverage_daily WHERE scope_id = :scope AND bucket = :bucket"
                + " ORDER BY day", new MapSqlParameterSource().addValue("scope", ref.seriesId()).addValue("bucket",
                ref.groupKey()), rs -> {
                    Instant day = rs.getTimestamp("day").toInstant();
                    values.put(day, rs.getLong("evidence_groups"));
                    range[0] = range[0] == null ? day : range[0];
                    range[1] = day;
                });
        List<SeriesValue> out = new ArrayList<>();
        if (range[0] == null) {
            return out;
        }
        Instant now = clock.instant();
        for (Instant t = SeriesFrequency.DAY.floor(from); t.isBefore(to); t = SeriesFrequency.DAY.next(t)) {
            Instant end = SeriesFrequency.DAY.next(t);
            if (t.isBefore(range[0]) || t.isAfter(range[1]) || end.isAfter(now)) {
                continue;
            }
            if (asOf != null && end.isAfter(asOf)) {
                continue;
            }
            out.add(new SeriesValue(t, end, (double) values.getOrDefault(t, 0L), end, true, null, null, null));
        }
        return out;
    }

    // ------------------------------------------------------------------------- rollup:

    private Meta rollupMeta(SeriesRef ref) {
        Set<String> sources = ref.allSources() ? Set.of() : Set.of(ref.sourceId());
        String family = ref.allSources() ? families.familyOfRecord(null, ref.kind()).orElse(null)
                : family(ref.sourceId(), ref.kind());
        SeriesFrequency frequency = "hour".equals(ref.resolution()) ? SeriesFrequency.HOUR : SeriesFrequency.DAY;
        if (ref.cell() != null) {
            long cell;
            try {
                cell = cells.parse(ref.cell());
            } catch (RuntimeException e) {
                throw notFound(ref);
            }
            if (!cells.valid(cell)) {
                throw notFound(ref);
            }
        } else if (!ref.allSources()) {
            Integer rows = jdbc.queryForObject("SELECT count(*) FROM (SELECT 1 FROM activity_rollup WHERE source_id ="
                    + " :source AND kind = :kind AND region_code = :region LIMIT 1) x", new MapSqlParameterSource()
                    .addValue("source", ref.sourceId()).addValue("kind", ref.kind()).addValue("region", ref.region()),
                    Integer.class);
            if ((rows == null || rows == 0) && !sourceDeclaresKind(ref.sourceId(), ref.kind())) {
                throw notFound(ref);
            }
        }
        return new Meta(frequency, "count", family, "count", "log-level", 0, ref.region(), null, sources);
    }

    private boolean sourceDeclaresKind(String sourceId, String kind) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM (SELECT 1 FROM record_head WHERE source_id = :source"
                + " AND kind = :kind LIMIT 1) x", new MapSqlParameterSource().addValue("source", sourceId)
                .addValue("kind", kind), Integer.class);
        return n != null && n > 0;
    }

    private List<SeriesValue> rollupPoints(SeriesRef ref, Instant from, Instant to, Instant asOf) {
        boolean hourly = "hour".equals(ref.resolution());
        SeriesFrequency frequency = hourly ? SeriesFrequency.HOUR : SeriesFrequency.DAY;
        short resolution;
        Long cell = null;
        RollupCursor cursor;
        if (ref.cell() != null) {
            resolution = hourly ? RollupResolution.HOUR.code() : RollupResolution.DAY.code();
            cell = cells.parse(ref.cell());
            RollupJob job = rollup.getIfAvailable();
            cursor = job == null ? null : job.cursor();
        } else {
            resolution = hourly ? RegionRollupResolution.HOUR.code() : RegionRollupResolution.DAY.code();
            RegionRollupJob job = regionRollup.getIfAvailable();
            cursor = job == null ? null : job.cursor();
        }
        if (cursor == null) {
            return List.of();
        }
        RollupResolution width = hourly ? RollupResolution.HOUR : RollupResolution.DAY;
        Instant rolledFrom = cursor.start(width);
        Instant rolledTo = cursor.end(width);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("resolution", resolution)
                .addValue("source", ref.sourceId())
                .addValue("kind", ref.kind())
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        String keyClause;
        if (cell != null) {
            keyClause = " AND cell = :cell";
            params.addValue("cell", cell);
        } else {
            keyClause = " AND region_code = :region";
            params.addValue("region", ref.region());
        }
        Map<Instant, Long> values = new TreeMap<>();
        jdbc.query("SELECT bucket_start, entities FROM activity_rollup WHERE resolution = :resolution AND source_id ="
                + " :source AND kind = :kind" + keyClause + " AND bucket_start >= :from AND bucket_start < :to",
                params, rs -> {
                    values.put(rs.getTimestamp("bucket_start").toInstant(), rs.getLong("entities"));
                });
        Set<String> sources = ref.allSources() ? Set.of() : Set.of(ref.sourceId());
        List<SeriesValue> out = new ArrayList<>();
        for (Instant t = frequency.floor(from); t.isBefore(to); t = frequency.next(t)) {
            Instant end = frequency.next(t);
            if (t.isBefore(rolledFrom) || rolledTo == null || end.isAfter(rolledTo)) {
                continue;
            }
            if (asOf != null && end.isAfter(asOf)) {
                continue;
            }
            boolean ok = covered(sources, ref.kind(), t, end);
            out.add(new SeriesValue(t, end, ok ? (double) values.getOrDefault(t, 0L) : null, end, ok, null, null,
                    null));
        }
        return out;
    }

    /** The 100 coverage gate over a past bucket, cached. */
    boolean covered(Set<String> sources, String kind, Instant from, Instant to) {
        CoverageGate gate = coverage.getIfAvailable();
        if (gate == null) {
            return true;
        }
        String key = String.join(",", new java.util.TreeSet<>(sources)) + "|" + kind + "|" + from + "|" + to;
        Boolean cached = coverageCache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        boolean ok;
        try {
            ok = gate.evaluateAsOf(sources, Set.of(kind), from, to, Duration.between(from, to), COVERAGE_MIN).ok();
        } catch (RuntimeException e) {
            ok = false;
        }
        coverageCache.put(key, ok);
        return ok;
    }

    // ------------------------------------------------------------------------- rule:

    private record RuleInfo(Meta meta, Duration bucket) {}

    private RuleInfo ruleMeta(SeriesRef ref) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT type, window_spec::text AS spec, tags::text AS tags,"
                + " sources::text AS sources, kinds::text AS kinds FROM trigger_rule WHERE id = :id",
                new MapSqlParameterSource("id", ref.ruleId()));
        if (rows.isEmpty() || !"anomaly".equals(rows.get(0).get("type"))) {
            throw notFound(ref);
        }
        Map<String, Object> row = rows.get(0);
        Map<String, Object> specMap = row.get("spec") == null ? null : parse(String.valueOf(row.get("spec")));
        WindowSpec spec = WindowSpec.fromStored(specMap);
        Duration bucket = spec == null ? Duration.ofHours(1) : spec.bucket();
        List<String> tags = strings(row.get("tags"));
        List<String> sources = strings(row.get("sources"));
        List<String> kinds = strings(row.get("kinds"));
        Optional<String> family = families.familyOfTags(tags);
        if (family.isEmpty() && !kinds.isEmpty()) {
            family = families.familyOfRecord(sources.isEmpty() ? null : sources.get(0), kinds.get(0));
        }
        return new RuleInfo(new Meta(SeriesFrequency.HOUR, "count", family.orElse(null), "count", "log-level", 0,
                ref.regionCode(), null, new LinkedHashSet<>(sources)), bucket);
    }

    private List<SeriesValue> rulePoints(SeriesRef ref, Instant from, Instant to, Instant asOf) {
        RuleInfo info = ruleMeta(ref);
        Map<Instant, long[]> hours = new TreeMap<>();
        Map<Instant, Boolean> covered = new TreeMap<>();
        Duration bucket = info.bucket();
        jdbc.query("SELECT bucket_start, count, coverage_ok FROM activity_sample WHERE rule_id = :rule AND group_key ="
                        + " :group AND bucket_start >= :from AND bucket_start < :to ORDER BY bucket_start",
                new MapSqlParameterSource().addValue("rule", ref.ruleId()).addValue("group", ref.groupKey())
                        .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to)),
                rs -> {
                    Instant start = rs.getTimestamp("bucket_start").toInstant();
                    if (asOf != null && start.plus(bucket).isAfter(asOf)) {
                        return;
                    }
                    Instant hour = SeriesFrequency.HOUR.floor(start);
                    long[] acc = hours.computeIfAbsent(hour, k -> new long[2]);
                    acc[0] += rs.getInt("count");
                    acc[1]++;
                    covered.merge(hour, rs.getBoolean("coverage_ok"), Boolean::logicalAnd);
                });
        long perHour = Math.max(1, Duration.ofHours(1).dividedBy(bucket.isZero() ? Duration.ofHours(1) : bucket));
        List<SeriesValue> out = new ArrayList<>();
        for (Map.Entry<Instant, long[]> e : hours.entrySet()) {
            boolean ok = covered.getOrDefault(e.getKey(), false) && e.getValue()[1] >= perHour;
            Instant end = e.getKey().plus(Duration.ofHours(1));
            out.add(new SeriesValue(e.getKey(), end, ok ? (double) e.getValue()[0] : null, end, ok, null, null, null));
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    /** The gaps of a regular series: buckets without a usable point, merged. */
    static List<Instant[]> gapsOf(SeriesFrequency frequency, Instant from, Instant to, List<SeriesValue> points) {
        Map<Instant, SeriesValue> byStart = new LinkedHashMap<>();
        for (SeriesValue p : points) {
            byStart.put(frequency.floor(p.periodStart()), p);
        }
        List<Instant[]> gaps = new ArrayList<>();
        Instant open = null;
        Instant t = frequency.floor(from);
        while (t.isBefore(to)) {
            SeriesValue p = byStart.get(t);
            boolean missing = p == null || !p.usable();
            Instant next = frequency.next(t);
            if (missing && open == null) {
                open = t;
            } else if (!missing && open != null) {
                gaps.add(new Instant[] {open, t});
                open = null;
            }
            t = next;
        }
        if (open != null) {
            gaps.add(new Instant[] {open, t});
        }
        return gaps;
    }

    private String family(String sourceId, String kind) {
        return families.familyOfRecord(sourceId, kind).orElse(null);
    }

    private static NotFoundException notFound(SeriesRef ref) {
        return new NotFoundException(ErrorCodes.SERIES_NOT_FOUND, "No series '" + ref.format() + "'.");
    }

    private Map<String, Object> parse(String text) {
        try {
            return text == null ? Map.of() : json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    private List<String> strings(Object text) {
        if (text == null) {
            return List.of();
        }
        try {
            List<String> out = new ArrayList<>();
            for (var node : json.readTree(text.toString())) {
                if (node.isString()) {
                    out.add(node.asString());
                }
            }
            return out;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private static String like(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static Instant parseInstant(Object value, Instant fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
