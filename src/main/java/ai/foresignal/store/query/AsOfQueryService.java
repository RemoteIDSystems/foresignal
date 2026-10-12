package ai.foresignal.store.query;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;

/**
 * Transaction-time replay: {@code GET /v1/records?asOf=T}, implemented exactly as Core-Data-Model
 * §12 specifies. From prompt 037 steps 1–3 and the {@code approximate} flag are planned by the
 * shared {@link AsOfResolver}, which asOf tiles, tracks and frames use too.
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
    static final Duration TRACK_LOWER_MARGIN = AsOfResolver.TRACK_LOWER_MARGIN;

    /** Margin added to the horizon. */
    static final Duration HORIZON_MARGIN = RetentionResolver.HORIZON_MARGIN;

    private final ReadQueryRunner runner;
    private final AsOfResolver resolver;

    /** Creates the service. */
    public AsOfQueryService(ReadQueryRunner runner, AsOfResolver resolver) {
        this.runner = runner;
        this.resolver = resolver;
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
     * @param restriction the caller's read restriction (prompt 035; never null)
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
            Set<String> excludedSources,
            ReadRestriction restriction) {

        /** An unrestricted query (the shape before prompt 035). */
        public AsOfQuery(Instant asOf, List<String> sources, List<String> kinds, List<String> externalIds,
                BboxFilter bbox, NearFilter near, String cursor, int limit, FieldSelection fields,
                GeometryOptions geometry, GeometryEncoding encoding, boolean includeObserver,
                Set<String> excludedSources) {
            this(asOf, sources, kinds, externalIds, bbox, near, cursor, limit, fields, geometry, encoding,
                    includeObserver, excludedSources, ReadRestriction.NONE);
        }

        /** Normalizes nulls. */
        public AsOfQuery {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            externalIds = externalIds == null ? List.of() : List.copyOf(externalIds);
            fields = fields == null ? FieldSelection.ALL : fields;
            geometry = geometry == null ? GeometryOptions.DEFAULT : geometry;
            encoding = encoding == null ? GeometryEncoding.GEOJSON : encoding;
            excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
            restriction = restriction == null ? ReadRestriction.NONE : restriction;
        }

        String canonical() {
            return String.join("|", "asof", String.valueOf(asOf), String.join(",", sources), String.join(",", kinds),
                    String.join(",", externalIds), bbox == null ? "" : bbox.canonical(),
                    near == null ? "" : near.canonical(), restriction.canonical());
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

    /**
     * Runs the query: the plan comes from the shared {@link AsOfResolver} (prompt 037), then one
     * query per involved tier.
     */
    public AsOfSlice list(AsOfQuery q) {
        AsOfPlan plan = resolver.plan(q.asOf(), q.sources(), q.kinds(), q.excludedSources());
        String fingerprint = CursorCodec.fingerprint(q.canonical());
        Cursor.ById cursor = CursorCodec.decode(q.cursor(), Cursor.ById.class, fingerprint);
        Instant t = q.asOf();
        List<RecordRow> rows = runner.read(jdbc -> {
            List<RecordRow> out = new ArrayList<>();
            for (Map.Entry<HistoryTier, List<SourceDescriptor>> tier : plan.sources().entrySet()) {
                SqlParams p = new SqlParams();
                String sql = tierSql(q, tier.getKey(), tier.getValue(), plan, cursor, p);
                out.addAll(jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE));
            }
            return out;
        });
        List<RecordRow> merged = new ArrayList<>(rows);
        merged.sort(Comparator.comparing(RecordRow::sourceId, AsOfQueryService::compareIds)
                .thenComparing(RecordRow::externalId, AsOfQueryService::compareIds));
        String next = null;
        if (merged.size() > q.limit()) {
            merged = new ArrayList<>(merged.subList(0, q.limit()));
            RecordRow last = merged.getLast();
            next = CursorCodec.encode(new Cursor.ById(last.sourceId(), last.externalId(), null), fingerprint);
        }
        return new AsOfSlice(Collections.unmodifiableList(merged), next, plan.watermarkSeq(), t, plan.approximate(),
                plan.horizon());
    }

    /** {@code now − window + effectiveKeyframe + 1 h} for one source ({@link AsOfResolver#horizon}). */
    public Instant horizon(String sourceId, Instant now) {
        return resolver.horizon(sourceId, now);
    }

    private static String tierSql(
            AsOfQuery q,
            HistoryTier tier,
            List<SourceDescriptor> sources,
            AsOfPlan plan,
            Cursor.ById cursor,
            SqlParams p) {
        long lower = plan.lower(tier);
        long upper = plan.s();
        Instant t = plan.t();
        StringJoiner values = new StringJoiner(", ", "(VALUES ", ")");
        for (SourceDescriptor descriptor : sources) {
            // TTL plus the fidelity widening (prompt 044 widens it over downsampled ranges).
            Duration ttl = plan.validity(descriptor.id());
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
        RestrictionClamp.addTo(outer, "c.source_id", "c.kind", "c.geom", "c.payload", q.restriction(), p);
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
