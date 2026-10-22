package ai.foresignal.intel;

import ai.foresignal.evidence.EvidenceService;
import ai.foresignal.evidence.RecordRef;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

/**
 * Automatic corroboration of a lone event (prompt 154): a single-source agent, analyst or operator event
 * surfaces at once, then — instead of waiting for someone to pin members by hand — this searches the record
 * store for independent observations near it in space and time and reports them, so the event gathers
 * corroboration and can auto-upgrade the moment independent evidence groups accumulate.
 *
 * <p>Independence is counted exactly as the rest of the intelligence layer counts it, in evidence groups by
 * originator root ({@link EvidenceService}): at most one corroborator per independent root, and never a root the
 * event's own members already represent, so echoed re-reports of one originator never masquerade as corroboration.
 * The search reuses the same space/time predicate the correlation engine uses for presence
 * ({@code ST_DWithin} over {@code record_version}); it is a heuristic gatherer that proposes members, not the
 * deterministic comparison core, which still decides corroborated/contradicted against real evidence.
 */
@Component
public class AutoCorroboration {

    private final JdbcTemplate jdbc;
    private final EvidenceService evidence;

    /** Creates the search. */
    public AutoCorroboration(JdbcTemplate jdbc, EvidenceService evidence) {
        this.jdbc = jdbc;
        this.evidence = evidence;
    }

    /**
     * One independent corroborating record.
     *
     * @param sourceId the source
     * @param externalId the record
     * @param seq the version seq
     * @param version the per-entity version
     * @param kind the record kind (its signal family is derived by {@link SignalFamilies#forKind})
     * @param observedAt the observation time
     * @param root the independence root it represents
     */
    public record Corroborator(String sourceId, String externalId, long seq, int version, String kind,
            Instant observedAt, String root) {}

    /**
     * The independent records that corroborate an event whose members are {@code memberSeqs}, within
     * {@code withinKm} of the members' centroid and {@code window} around their observation span, at most
     * {@code limit} and at most one per independent originator root.
     */
    public List<Corroborator> corroborators(List<Long> memberSeqs, AutoCorroboration.Spec spec) {
        if (memberSeqs == null || memberSeqs.isEmpty()) {
            return List.of();
        }
        Centroid centroid = centroidOf(memberSeqs);
        if (centroid == null || centroid.count == 0) {
            // No geometry on the members: nothing to search around.
            return List.of();
        }
        Instant from = centroid.minObserved.minus(spec.window());
        Instant until = centroid.maxObserved.plus(spec.window());
        List<Row> candidates = query(centroid.lon / centroid.count, centroid.lat / centroid.count, from, until,
                spec.withinKm(), spec.scanLimit());
        if (candidates.isEmpty()) {
            return List.of();
        }
        Set<String> memberKeys = memberKeys(memberSeqs);
        Set<String> takenRoots = new LinkedHashSet<>(rootsOf(memberRefs(memberSeqs)));
        List<RecordRef> candidateRefs = new ArrayList<>();
        for (Row row : candidates) {
            candidateRefs.add(new RecordRef(row.sourceId, row.externalId, row.seq));
        }
        Map<String, EvidenceService.GroupKey> keys = evidence.groupKeys(candidateRefs);
        List<Corroborator> out = new ArrayList<>();
        for (Row row : candidates) {
            if (memberKeys.contains(EvidenceService.recordKey(row.sourceId, row.externalId))) {
                continue;
            }
            EvidenceService.GroupKey key = keys.get(EvidenceService.recordKey(row.sourceId, row.externalId));
            String root = key == null ? row.sourceId : key.root();
            if (!takenRoots.add(root)) {
                // An originator already represented among the members or an earlier corroborator: not independent.
                continue;
            }
            out.add(new Corroborator(row.sourceId, row.externalId, row.seq, row.version, row.kind, row.observedAt,
                    root));
            if (out.size() >= spec.limit()) {
                break;
            }
        }
        return out;
    }

    /**
     * The search window and caps.
     *
     * @param withinKm the search radius around the members' centroid, in kilometers
     * @param window the time window before and after the members' observation span
     * @param limit the most corroborators returned (one per independent root)
     */
    public record Spec(double withinKm, java.time.Duration window, int limit) {

        /** How many nearby rows the search examines before ranking by independence. */
        int scanLimit() {
            return Math.max(50, limit() * 20);
        }
    }

    private record Centroid(double lon, double lat, int count, Instant minObserved, Instant maxObserved) {}

    private Centroid centroidOf(List<Long> seqs) {
        double[] acc = {0, 0};
        int[] n = {0};
        Instant[] span = {null, null};
        RowCallbackHandler handler = rs -> {
            acc[0] += rs.getDouble("lon");
            acc[1] += rs.getDouble("lat");
            n[0]++;
            Instant observed = rs.getObject("observed_at", java.time.OffsetDateTime.class).toInstant();
            span[0] = span[0] == null || observed.isBefore(span[0]) ? observed : span[0];
            span[1] = span[1] == null || observed.isAfter(span[1]) ? observed : span[1];
        };
        jdbc.query("SELECT ST_X(ST_Centroid(geom)) AS lon, ST_Y(ST_Centroid(geom)) AS lat, observed_at"
                        + " FROM record_version WHERE seq = ANY(?::bigint[]) AND geom IS NOT NULL",
                handler, (Object) seqs.toArray(Long[]::new));
        if (n[0] == 0 || span[0] == null) {
            return null;
        }
        return new Centroid(acc[0], acc[1], n[0], span[0], span[1]);
    }

    private record Row(String sourceId, String externalId, long seq, int version, String kind, Instant observedAt) {}

    private List<Row> query(double lon, double lat, Instant from, Instant until, double withinKm, int scanLimit) {
        List<Row> out = new ArrayList<>();
        RowCallbackHandler handler = rs -> out.add(new Row(rs.getString("source_id"), rs.getString("external_id"),
                rs.getLong("seq"), rs.getInt("version"), rs.getString("kind"),
                rs.getObject("observed_at", java.time.OffsetDateTime.class).toInstant()));
        jdbc.query("SELECT seq, source_id, external_id, kind, version, observed_at FROM record_version"
                        + " WHERE tombstone = false AND persist_reason <> 8 AND geom IS NOT NULL"
                        + " AND observed_at >= ? AND observed_at <= ?"
                        + " AND ST_DWithin(geom::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)"
                        + " ORDER BY observed_at LIMIT ?",
                handler, Timestamp.from(from), Timestamp.from(until), lon, lat, withinKm * 1000.0, scanLimit);
        return out;
    }

    private Set<String> memberKeys(List<Long> seqs) {
        Set<String> out = new LinkedHashSet<>();
        RowCallbackHandler handler = rs -> out.add(EvidenceService.recordKey(rs.getString("source_id"),
                rs.getString("external_id")));
        jdbc.query("SELECT source_id, external_id FROM record_version WHERE seq = ANY(?::bigint[])",
                handler, (Object) seqs.toArray(Long[]::new));
        return out;
    }

    private List<RecordRef> memberRefs(List<Long> seqs) {
        List<RecordRef> out = new ArrayList<>();
        RowCallbackHandler handler = rs -> out.add(new RecordRef(rs.getString("source_id"),
                rs.getString("external_id"), rs.getLong("seq")));
        jdbc.query("SELECT source_id, external_id, seq FROM record_version WHERE seq = ANY(?::bigint[])",
                handler, (Object) seqs.toArray(Long[]::new));
        return out;
    }

    private Set<String> rootsOf(List<RecordRef> refs) {
        Set<String> roots = new LinkedHashSet<>();
        if (refs.isEmpty()) {
            return roots;
        }
        Map<String, EvidenceService.GroupKey> keys = evidence.groupKeys(refs);
        for (RecordRef ref : refs) {
            EvidenceService.GroupKey key = keys.get(EvidenceService.recordKey(ref.sourceId(), ref.externalId()));
            roots.add(key == null ? ref.sourceId() : key.root());
        }
        return roots;
    }

    /** A minimal member snapshot of a corroborator (prompt 154), matching the shape pinned members carry. */
    public static Map<String, Object> snapshot(Corroborator c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", c.sourceId());
        out.put("externalId", c.externalId());
        out.put("kind", c.kind());
        out.put("version", c.version());
        out.put("seq", c.seq());
        out.put("observedAt", c.observedAt() == null ? null : c.observedAt().toString());
        return out;
    }
}
