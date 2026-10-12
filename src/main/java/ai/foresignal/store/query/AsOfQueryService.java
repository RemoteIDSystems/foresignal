package ai.foresignal.store.query;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.ingest.SourceIngestProfiles;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;

/**
 * Transaction-time replay: {@code GET /v1/records?asOf=T}, implemented exactly as Core-Data-Model
 * §12 specifies.
 *
 * <ol>
 *   <li>A {@code source} or {@code kind} filter is required ({@code 400 ASOF_FILTER_REQUIRED}).
 *   <li>{@code horizon(source) = now − window + effectiveKeyframe + 1 h}, with
 *       {@code effectiveKeyframe = min(policy keyframe, window/4)} and the window from
 *       {@link RetentionState#window}. {@code T} before the horizon of any involved source is
 *       {@code 400 ASOF_BEYOND_RETENTION} (with {@code horizon}); {@code T} in the future is
 *       {@code 400 ASOF_INVALID}.
 *   <li>{@code S = S(T)} through {@code seq_clock}.
 *   <li>One query per tier: TRACK with {@code L = S(T − ttl − 2 min)}, STANDARD with {@code L = 0}:
 *       the latest version per entity with {@code seq BETWEEN L AND S}, kept when not a tombstone and
 *       still valid at {@code T} (explicit {@code valid_until} on STANDARD, else
 *       {@code ingested_at + ttl}; TRACK ignores the explicit value). bbox, near and kind apply to
 *       the chosen version. Keyset on {@code (source_id, external_id)}, merged across the two tier
 *       queries in Java.
 *   <li>The response carries {@code asOf}, {@code watermarkSeq = S}, {@code approximate} (a SAMPLED
 *       or LATEST source is involved) and {@code horizon}.
 * </ol>
 *
 * <p>Granularity is one batch. Valid-time asOf is not offered; {@code /v1/history} serves it.
 */
@Service
public class AsOfQueryService {

    /** Margin past the TTL when choosing the TRACK lower seq bound. */
    static final Duration TRACK_LOWER_MARGIN = Duration.ofMinutes(2);

    /** Margin added to the horizon. */
    static final Duration HORIZON_MARGIN = RetentionResolver.HORIZON_MARGIN;

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final SourceRegistry registry;
    private final SourceIngestProfiles profiles;
    private final RetentionState retention;
    private final Clock clock;

    /** Creates the service. */
    public AsOfQueryService(
            ReadQueryRunner runner,
            SeqClockQuery seqClock,
            SourceRegistry registry,
            SourceIngestProfiles profiles,
            RetentionState retention,
            Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.registry = registry;
        this.profiles = profiles;
        this.retention = retention;
        this.clock = clock;
    }

    /**
     * An asOf listing.
     *
     * @param asOf the transaction time T
     * @param sources source filter
     * @param kinds kind filter
     * @param externalIds external id filter
     * @param bbox spatial filter, or null
     * @param near radius filter, or null
     * @param cursor the previous page's cursor, or null
     * @param limit page size
     * @param fields the projection
     * @param geometry geometry output options
     * @param encoding geometry text encoding
     * @param includeObserver include observers
     * @param excludedSources sources the caller may not see
     */
    public record AsOfQuery(
            Instant asOf,
            List<String> sources,
            List<String> kinds,
            List<String> externalIds,
            BboxFilter bbox,
            NearFilter near,
            String cursor,
            int limit,
            FieldSelection fields,
            GeometryOptions geometry,
            GeometryEncoding encoding,
            boolean includeObserver,
            Set<String> excludedSources) {

        /** Normalizes nulls. */
        public AsOfQuery {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            externalIds = externalIds == null ? List.of() : List.copyOf(externalIds);
            fields = fields == null ? FieldSelection.ALL : fields;
            geometry = geometry == null ? GeometryOptions.DEFAULT : geometry;
            encoding = encoding == null ? GeometryEncoding.GEOJSON : encoding;
            excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        }

        String canonical() {
            return String.join("|", "asof", String.valueOf(asOf), String.join(",", sources), String.join(",", kinds),
                    String.join(",", externalIds), bbox == null ? "" : bbox.canonical(),
                    near == null ? "" : near.canonical());
        }
    }

    /**
     * An asOf page.
     *
     * @param rows the chosen versions
     * @param nextCursor the next page's cursor, or null
     * @param seq {@code S(T)}, returned as {@code watermarkSeq}
     * @param asOf the echoed T
     * @param approximate true when a SAMPLED or LATEST source is involved
     * @param horizon the earliest supported T over the involved sources, or null when none
     */
    public record AsOfSlice(
            List<RecordRow> rows, String nextCursor, long seq, Instant asOf, boolean approximate, Instant horizon) {}

    /**
     * Rejects the parameters asOf cannot be combined with ({@code 400 ASOF_INVALID}).
     *
     * @param explicitOrderObserved {@code order=observed} was passed explicitly
     */
    public static void checkCombination(
            boolean asOfSeq, boolean explicitOrderObserved, boolean sortDistance, boolean includeExpired, boolean timeBounds) {
        List<String> conflicts = new ArrayList<>();
        if (asOfSeq) {
            conflicts.add("asOfSeq");
        }
        if (explicitOrderObserved) {
            conflicts.add("order=observed");
        }
        if (sortDistance) {
            conflicts.add("sort=distance");
        }
        if (includeExpired) {
            conflicts.add("includeExpired");
        }
        if (timeBounds) {
            conflicts.add("since/until");
        }
        if (!conflicts.isEmpty()) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID,
                    "asOf cannot be combined with " + String.join(", ", conflicts) + ".", List.of());
        }
    }

    /** Runs the query. */
    public AsOfSlice list(AsOfQuery q) {
        if (q.sources().isEmpty() && q.kinds().isEmpty()) {
            throw new ValidationException(ErrorCodes.ASOF_FILTER_REQUIRED,
                    "asOf needs a source or kind filter.", List.of());
        }
        Instant now = clock.instant();
        if (q.asOf().isAfter(now)) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID, "asOf must not be in the future.", List.of());
        }
        List<SourceDescriptor> involved = involvedSources(q);
        Instant horizon = null;
        boolean approximate = false;
        for (SourceDescriptor descriptor : involved) {
            Instant h = horizon(descriptor.id(), now);
            if (horizon == null || h.isAfter(horizon)) {
                horizon = h;
            }
            IngestPolicy.PolicyMode mode = profiles.get(descriptor.id()).policy().mode();
            approximate |= mode == IngestPolicy.PolicyMode.SAMPLED || mode == IngestPolicy.PolicyMode.LATEST;
        }
        if (horizon != null && q.asOf().isBefore(horizon)) {
            throw new ValidationException(ErrorCodes.ASOF_BEYOND_RETENTION,
                            "asOf " + q.asOf() + " is before the retention horizon " + horizon + ".", List.of())
                    .withProperty("horizon", horizon.toString());
        }
        String fingerprint = CursorCodec.fingerprint(q.canonical());
        Cursor.ById cursor = CursorCodec.decode(q.cursor(), Cursor.ById.class, fingerprint);
        Map<HistoryTier, List<SourceDescriptor>> byTier = new EnumMap<>(HistoryTier.class);
        for (SourceDescriptor descriptor : involved) {
            byTier.computeIfAbsent(descriptor.historyTier(), t -> new ArrayList<>()).add(descriptor);
        }
        Instant t = q.asOf();
        record Result(long seq, List<RecordRow> rows) {}
        Result result = runner.read(jdbc -> {
            long s = seqClock.seqAt(jdbc, t);
            List<RecordRow> rows = new ArrayList<>();
            for (Map.Entry<HistoryTier, List<SourceDescriptor>> tier : byTier.entrySet()) {
                long lower = 0L;
                if (tier.getKey() == HistoryTier.TRACK) {
                    lower = Long.MAX_VALUE;
                    for (SourceDescriptor descriptor : tier.getValue()) {
                        Duration ttl = descriptor.recordTtl();
                        long l = ttl == null ? 0L : seqClock.seqAt(jdbc, t.minus(ttl).minus(TRACK_LOWER_MARGIN));
                        lower = Math.min(lower, l);
                    }
                }
                SqlParams p = new SqlParams();
                String sql = tierSql(q, tier.getKey(), tier.getValue(), lower, s, t, cursor, p);
                rows.addAll(jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE));
            }
            return new Result(s, rows);
        });
        List<RecordRow> merged = new ArrayList<>(result.rows());
        merged.sort(Comparator.comparing(RecordRow::sourceId, AsOfQueryService::compareIds)
                .thenComparing(RecordRow::externalId, AsOfQueryService::compareIds));
        String next = null;
        if (merged.size() > q.limit()) {
            merged = new ArrayList<>(merged.subList(0, q.limit()));
            RecordRow last = merged.getLast();
            next = CursorCodec.encode(new Cursor.ById(last.sourceId(), last.externalId(), null), fingerprint);
        }
        return new AsOfSlice(Collections.unmodifiableList(merged), next, result.seq(), t, approximate, horizon);
    }

    /** {@code now − window + effectiveKeyframe + 1 h} for one source. */
    public Instant horizon(String sourceId, Instant now) {
        // The math is RetentionResolver's (prompt 019); the window comes from RetentionState so a
        // test's stub window still applies.
        return RetentionResolver.horizon(now, retention.window(sourceId), profiles.get(sourceId).keyframe());
    }

    private List<SourceDescriptor> involvedSources(AsOfQuery q) {
        Set<String> ids = new LinkedHashSet<>();
        if (!q.sources().isEmpty()) {
            for (String id : q.sources()) {
                ids.add(registry.descriptor(id).id());
            }
        } else {
            for (SourceDescriptor descriptor : registry.descriptors()) {
                for (String kind : q.kinds()) {
                    if (descriptor.kinds().contains(kind)) {
                        ids.add(descriptor.id());
                    }
                }
            }
        }
        ids.removeAll(q.excludedSources());
        List<SourceDescriptor> result = new ArrayList<>();
        for (String id : ids) {
            result.add(registry.descriptor(id));
        }
        return result;
    }

    private static String tierSql(
            AsOfQuery q,
            HistoryTier tier,
            List<SourceDescriptor> sources,
            long lower,
            long upper,
            Instant t,
            Cursor.ById cursor,
            SqlParams p) {
        StringJoiner values = new StringJoiner(", ", "(VALUES ", ")");
        for (SourceDescriptor descriptor : sources) {
            Duration ttl = descriptor.recordTtl();
            values.add("(" + p.text(descriptor.id()) + ", CAST(" + p.add(ttl == null ? null : ttl.toMillis() + " milliseconds")
                    + " AS interval))");
        }
        List<String> inner = new ArrayList<>();
        inner.add("v.history_tier = CAST(" + p.add((int) tier.code()) + " AS smallint)");
        inner.add("v.seq BETWEEN " + p.bigint(lower) + " AND " + p.bigint(upper));
        RecordQueryService.addIn(inner, "v.external_id", q.externalIds(), p);
        if (cursor != null) {
            inner.add("(v.source_id COLLATE \"C\", v.external_id COLLATE \"C\") > (" + p.text(cursor.s()) + ", "
                    + p.text(cursor.e()) + ")");
        }
        String chosen = "SELECT DISTINCT ON (v.source_id, v.external_id) v.*, s.ttl AS asof_ttl"
                + " FROM record_version v JOIN " + values + " s(source_id, ttl) ON s.source_id = v.source_id"
                + RecordQueryService.where(inner)
                + " ORDER BY v.source_id, v.external_id, v.seq DESC";
        String at = p.instant(t);
        List<String> outer = new ArrayList<>();
        outer.add("NOT c.tombstone");
        if (tier == HistoryTier.TRACK) {
            outer.add("(c.asof_ttl IS NULL OR c.ingested_at + c.asof_ttl > " + at + ")");
        } else {
            outer.add("(c.valid_until_explicit > " + at + " OR (c.valid_until_explicit IS NULL AND (c.asof_ttl IS NULL OR "
                    + "c.ingested_at + c.asof_ttl > " + at + ")))");
        }
        RecordQueryService.addIn(outer, "c.kind", q.kinds(), p);
        if (q.bbox() != null) {
            outer.add(RecordQueryService.bboxClause("c.geom", q.bbox(), p));
        }
        if (q.near() != null) {
            outer.add(RecordQueryService.nearClause("c.geom", q.near(), p));
        }
        String select = RecordQueryService.versionSelect("c", q.fields(), q.geometry(), q.encoding(), q.includeObserver(), p);
        return "SELECT " + select + " FROM (" + chosen + ") c" + RecordQueryService.where(outer)
                + " ORDER BY c.source_id COLLATE \"C\", c.external_id COLLATE \"C\" LIMIT " + (q.limit() + 1);
    }

    /**
     * Orders ids for the merge exactly as the tier queries do: both sort and page with
     * {@code COLLATE "C"} (byte order), which {@link String#compareTo} matches for every character
     * outside the supplementary planes.
     */
    private static int compareIds(String a, String b) {
        return a.compareTo(b);
    }
}
