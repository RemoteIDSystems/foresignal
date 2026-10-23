package ai.foresignal.social.correlation;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes comparisons, their revisions and cues (prompt 137, tables {@code social_comparison},
 * {@code social_comparison_revision} and {@code social_cue}) with SQL.
 */
@Component
public class ComparisonStore {

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final TypeReference<List<Object>> LIST = new TypeReference<>() {};

    private static final String COMPARISON_COLUMNS = "id, claim_id, narrative_id, claim_type, signal_family,"
            + " place::text AS place, place_lon, place_lat, region_code, window_start, window_end, status,"
            + " physical_evidence::text AS physical_evidence, detectability::text AS detectability,"
            + " lead_times::text AS lead_times, contradiction::text AS contradiction, caveats::text AS caveats,"
            + " found_by, cue_id, trigger_ref, platforms, evidence_group_count, question_id, revision, final_at,"
            + " manifest_id, evaluated_at, created_at";

    private static final String CUE_COLUMNS = "id, trigger_type, trigger_ref, backend, queries::text AS queries,"
            + " platforms, window_start, window_end, status, status_reason, results::text AS results, narrative_ids,"
            + " cost_usd, max_cost_usd, input::text AS input, created_by, created_at, started_at, completed_at,"
            + " manifest_id, external_results::text AS external_results";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the store. */
    public ComparisonStore(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The JDBC template (tests and queries of other components). */
    public JdbcTemplate jdbc() {
        return jdbc;
    }

    /**
     * A comparison row.
     *
     * @param id the id
     * @param claimId the claim
     * @param narrativeId the narrative
     * @param claimType the lexicon claim type
     * @param signalFamily the signal family
     * @param place the {@code ClaimPlace}
     * @param lon the place longitude, or null
     * @param lat the place latitude, or null
     * @param regionCode the place's region, or null
     * @param windowStart the window start
     * @param windowEnd the window end
     * @param status the status
     * @param physicalEvidence the {@code SocialPhysicalEvidence} list
     * @param detectability the detectability object
     * @param leadTimes the {@code SocialLeadTimes}
     * @param contradiction the contradiction, or null
     * @param caveats the caveats
     * @param foundBy {@code organic} or {@code cue}
     * @param cueId the cue, or null
     * @param triggerRef the cue's trigger, or null
     * @param platforms the narrative's networks (the claim's platform mix)
     * @param evidenceGroupCount the claim's independent origins
     * @param questionId the 122 question, or null
     * @param revision the revision number
     * @param finalAt the window end plus the publication lag
     * @param manifestId the manifest, or null
     * @param evaluatedAt the last evaluation
     * @param createdAt the creation time
     */
    public record Comparison(String id, String claimId, String narrativeId, String claimType, String signalFamily,
            Map<String, Object> place, Double lon, Double lat, String regionCode, Instant windowStart,
            Instant windowEnd, String status, List<Object> physicalEvidence, Map<String, Object> detectability,
            Map<String, Object> leadTimes, Map<String, Object> contradiction, List<Object> caveats, String foundBy,
            String cueId, String triggerRef, List<String> platforms, int evidenceGroupCount, String questionId,
            int revision, Instant finalAt, String manifestId, Instant evaluatedAt, Instant createdAt) {

        /** True once no revision can follow. */
        public boolean isFinal(Instant now) {
            return CorrelationVocabulary.isFinal(status) && !now.isBefore(finalAt);
        }
    }

    /**
     * A revision row.
     *
     * @param comparisonId the comparison
     * @param revision the number
     * @param at when
     * @param status the status after it
     * @param previousStatus the status before it, or null
     * @param reason why
     * @param refs the matching physical evidence refs
     * @param manifestId the manifest, or null
     */
    public record Revision(String comparisonId, int revision, Instant at, String status, String previousStatus,
            String reason, List<String> refs, String manifestId) {}

    /**
     * A cue row.
     *
     * @param id the id
     * @param triggerType the trigger type
     * @param triggerRef the trigger ref, or null
     * @param backend {@code platform-apis} or {@code xai-x-search}
     * @param queries the {@code SocialCueQuery} list
     * @param platforms the adapters
     * @param windowStart the window start
     * @param windowEnd the window end, or null
     * @param status the status
     * @param statusReason the reason, or null
     * @param results the per-platform results
     * @param narrativeIds the narratives its posts joined
     * @param costUsd the money spent
     * @param maxCostUsd the cap
     * @param input the request (place, region, bbox, entity, situation, terms, families)
     * @param createdBy {@code system} or the principal
     * @param createdAt the creation time
     * @param startedAt the start, or null
     * @param completedAt the end, or null
     * @param manifestId the manifest, or null
     * @param externalResults what each EDP provider's {@code transport.search} returned
     */
    public record Cue(String id, String triggerType, String triggerRef, String backend, List<Object> queries,
            List<String> platforms, Instant windowStart, Instant windowEnd, String status, String statusReason,
            List<Object> results, List<String> narrativeIds, BigDecimal costUsd, BigDecimal maxCostUsd,
            Map<String, Object> input, String createdBy, Instant createdAt, Instant startedAt, Instant completedAt,
            String manifestId, List<Object> externalResults) {}

    // ------------------------------------------------------------------------- comparisons

    /** One comparison. */
    public Optional<Comparison> comparison(String id) {
        return jdbc.query("SELECT " + COMPARISON_COLUMNS + " FROM social_comparison WHERE id = ?",
                (rs, i) -> comparison(rs), id).stream().findFirst();
    }

    /** The comparison of a claim. */
    public Optional<Comparison> byClaim(String claimId) {
        return jdbc.query("SELECT " + COMPARISON_COLUMNS + " FROM social_comparison WHERE claim_id = ?",
                (rs, i) -> comparison(rs), claimId).stream().findFirst();
    }

    /** The comparisons of a narrative. */
    public List<Comparison> byNarrative(String narrativeId) {
        return jdbc.query("SELECT " + COMPARISON_COLUMNS + " FROM social_comparison WHERE narrative_id = ? ORDER BY"
                + " created_at, id", (rs, i) -> comparison(rs), narrativeId);
    }

    /** Comparisons still open for revision at {@code now}, oldest evaluation first. */
    public List<Comparison> open(Instant now, int limit) {
        return jdbc.query("SELECT " + COMPARISON_COLUMNS + " FROM social_comparison WHERE status = 'pending' OR"
                + " final_at > ? ORDER BY evaluated_at, id LIMIT ?", (rs, i) -> comparison(rs), Timestamp.from(now),
                limit);
    }

    /**
     * A page of comparisons, newest evaluation first.
     *
     * @param where extra SQL conditions on alias {@code c}, or empty
     * @param args their arguments
     * @param after the cursor position {@code [evaluatedAt, id]}, or null
     * @param limit the page size (one more row is read)
     */
    public List<Comparison> page(String where, List<Object> args, Object[] after, int limit) {
        List<Object> a = new ArrayList<>(args);
        StringBuilder sql = new StringBuilder("SELECT " + COMPARISON_COLUMNS + " FROM social_comparison c WHERE true");
        if (where != null && !where.isEmpty()) {
            sql.append(" AND ").append(where);
        }
        if (after != null) {
            sql.append(" AND (evaluated_at < ? OR (evaluated_at = ? AND id < ?))");
            a.add(after[0]);
            a.add(after[0]);
            a.add(after[1]);
        }
        sql.append(" ORDER BY evaluated_at DESC, id DESC LIMIT ?");
        a.add(limit + 1);
        return jdbc.query(sql.toString(), (rs, i) -> comparison(rs), a.toArray());
    }

    /** Final comparisons evaluated at or after {@code since} (lead-time statistics and base rates). */
    public List<Comparison> finalSince(Instant since, int limit) {
        return jdbc.query("SELECT " + COMPARISON_COLUMNS + " FROM social_comparison WHERE status <> 'pending' AND"
                + " evaluated_at >= ? ORDER BY evaluated_at, id LIMIT ?", (rs, i) -> comparison(rs),
                Timestamp.from(since == null ? Instant.EPOCH : since), limit);
    }

    /** Inserts a comparison. */
    public void insert(Comparison c) {
        jdbc.update("INSERT INTO social_comparison (id, claim_id, narrative_id, claim_type, signal_family, place,"
                        + " place_lon, place_lat, region_code, window_start, window_end, status, physical_evidence,"
                        + " detectability, lead_times, contradiction, caveats, found_by, cue_id, trigger_ref, platforms,"
                        + " evidence_group_count, question_id, revision, final_at, manifest_id, evaluated_at, created_at)"
                        + " VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb),"
                        + " CAST(? AS jsonb), CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.id(), c.claimId(), c.narrativeId(), c.claimType(), c.signalFamily(), write(c.place()), c.lon(),
                c.lat(), c.regionCode(), ts(c.windowStart()), ts(c.windowEnd()), c.status(),
                write(c.physicalEvidence()), write(c.detectability()), write(c.leadTimes()), write(c.contradiction()),
                write(c.caveats()), c.foundBy(), c.cueId(), c.triggerRef(), array(c.platforms()),
                c.evidenceGroupCount(), c.questionId(), c.revision(), ts(c.finalAt()), c.manifestId(),
                ts(c.evaluatedAt()), ts(c.createdAt()));
    }

    /** Updates the evaluated members of a comparison. */
    public void update(Comparison c) {
        jdbc.update("UPDATE social_comparison SET status = ?, physical_evidence = CAST(? AS jsonb), detectability ="
                        + " CAST(? AS jsonb), lead_times = CAST(? AS jsonb), contradiction = CAST(? AS jsonb), caveats ="
                        + " CAST(? AS jsonb), platforms = ?, evidence_group_count = ?, question_id = ?, revision = ?,"
                        + " final_at = ?, manifest_id = ?, evaluated_at = ?, window_start = ?, window_end = ? WHERE id = ?",
                c.status(), write(c.physicalEvidence()), write(c.detectability()), write(c.leadTimes()),
                write(c.contradiction()), write(c.caveats()), array(c.platforms()), c.evidenceGroupCount(),
                c.questionId(), c.revision(), ts(c.finalAt()), c.manifestId(), ts(c.evaluatedAt()),
                ts(c.windowStart()), ts(c.windowEnd()), c.id());
    }

    /** Sets the ledger question of a comparison. */
    public void setQuestion(String id, String questionId) {
        jdbc.update("UPDATE social_comparison SET question_id = ? WHERE id = ?", questionId, id);
    }

    /** Appends a revision. */
    public void insertRevision(String id, Revision r) {
        jdbc.update("INSERT INTO social_comparison_revision (id, comparison_id, revision, at, status, previous_status,"
                        + " reason, physical_evidence_refs, manifest_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", id,
                r.comparisonId(), r.revision(), ts(r.at()), r.status(), r.previousStatus(), clip(r.reason(), 1000),
                array(r.refs()), r.manifestId());
    }

    /** The revisions of a comparison, oldest first. */
    public List<Revision> revisions(String comparisonId) {
        return jdbc.query("SELECT comparison_id, revision, at, status, previous_status, reason, physical_evidence_refs,"
                        + " manifest_id FROM social_comparison_revision WHERE comparison_id = ? ORDER BY revision",
                (rs, i) -> new Revision(rs.getString(1), rs.getInt(2), instant(rs, "at"), rs.getString(4),
                        rs.getString(5), rs.getString(6), strings(rs.getArray(7)), rs.getString(8)), comparisonId);
    }

    // ------------------------------------------------------------------------- cues

    /** One cue. */
    public Optional<Cue> cue(String id) {
        return jdbc.query("SELECT " + CUE_COLUMNS + " FROM social_cue WHERE id = ?", (rs, i) -> cue(rs), id).stream()
                .findFirst();
    }

    /**
     * A page of cues, newest first.
     *
     * @param where extra SQL conditions, or empty
     * @param args their arguments
     * @param after the cursor position {@code [createdAt, id]}, or null
     * @param limit the page size (one more row is read)
     */
    public List<Cue> cuePage(String where, List<Object> args, Object[] after, int limit) {
        List<Object> a = new ArrayList<>(args);
        StringBuilder sql = new StringBuilder("SELECT " + CUE_COLUMNS + " FROM social_cue WHERE true");
        if (where != null && !where.isEmpty()) {
            sql.append(" AND ").append(where);
        }
        if (after != null) {
            sql.append(" AND (created_at < ? OR (created_at = ? AND id < ?))");
            a.add(after[0]);
            a.add(after[0]);
            a.add(after[1]);
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ?");
        a.add(limit + 1);
        return jdbc.query(sql.toString(), (rs, i) -> cue(rs), a.toArray());
    }

    /** Cues created at or after {@code since}. */
    public int cuesSince(Instant since) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM social_cue WHERE created_at >= ?", Integer.class,
                Timestamp.from(since));
        return n == null ? 0 : n;
    }

    /** True when a cue of this trigger was created at or after {@code since}. */
    public boolean cuedSince(String triggerRef, Instant since) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM social_cue WHERE trigger_ref = ? AND created_at >= ?",
                Integer.class, triggerRef, Timestamp.from(since));
        return n != null && n > 0;
    }

    /** Queued cues, oldest first. */
    public List<Cue> queued(int limit) {
        return jdbc.query("SELECT " + CUE_COLUMNS + " FROM social_cue WHERE status = 'queued' ORDER BY created_at, id"
                + " LIMIT ?", (rs, i) -> cue(rs), limit);
    }

    /** Finished cues whose narratives may still be joining (completed at or after {@code since}). */
    public List<Cue> completedSince(Instant since, int limit) {
        return jdbc.query("SELECT " + CUE_COLUMNS + " FROM social_cue WHERE status = 'done' AND completed_at >= ?"
                + " ORDER BY completed_at, id LIMIT ?", (rs, i) -> cue(rs), Timestamp.from(since), limit);
    }

    /** Inserts a cue. */
    public void insertCue(Cue c) {
        jdbc.update("INSERT INTO social_cue (id, trigger_type, trigger_ref, backend, queries, platforms, window_start,"
                        + " window_end, status, status_reason, results, narrative_ids, cost_usd, max_cost_usd, input,"
                        + " created_by, created_at, started_at, completed_at, manifest_id, external_results) VALUES (?, ?,"
                        + " ?, ?, CAST(? AS jsonb), ?, ?, ?, ?, ?, CAST(? AS jsonb), ?, ?, ?, CAST(? AS jsonb), ?, ?, ?, ?,"
                        + " ?, CAST(? AS jsonb))",
                c.id(), c.triggerType(), c.triggerRef(), c.backend(), write(c.queries()), array(c.platforms()),
                ts(c.windowStart()), ts(c.windowEnd()), c.status(), clip(c.statusReason(), 500), write(c.results()),
                array(c.narrativeIds()), c.costUsd(), c.maxCostUsd(), write(c.input()), clip(c.createdBy(), 200),
                ts(c.createdAt()), ts(c.startedAt()), ts(c.completedAt()), c.manifestId(),
                write(c.externalResults() == null ? List.of() : c.externalResults()));
    }

    /** Updates the run members of a cue. */
    public void updateCue(Cue c) {
        jdbc.update("UPDATE social_cue SET queries = CAST(? AS jsonb), platforms = ?, status = ?, status_reason = ?,"
                        + " results = CAST(? AS jsonb), narrative_ids = ?, cost_usd = ?, started_at = ?, completed_at = ?,"
                        + " manifest_id = ?, external_results = CAST(? AS jsonb) WHERE id = ?", write(c.queries()),
                array(c.platforms()), c.status(), clip(c.statusReason(), 500), write(c.results()),
                array(c.narrativeIds()), c.costUsd(), ts(c.startedAt()), ts(c.completedAt()), c.manifestId(),
                write(c.externalResults() == null ? List.of() : c.externalResults()), c.id());
    }

    /** The narratives whose members include posts of a cue ({@code payload.cueId}). */
    public List<String> narrativesOfCue(String cueId) {
        return jdbc.queryForList("SELECT DISTINCT m.narrative_id FROM social_narrative_member m JOIN record_head h ON"
                + " h.source_id = m.source_id AND h.external_id = m.external_id WHERE h.payload ->> 'cueId' = ?"
                + " ORDER BY 1 LIMIT 500", String.class, cueId);
    }

    /** The cue whose posts make up all of a narrative's members, or null when any member was found otherwise. */
    public String cueOfNarrative(String narrativeId) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT h.payload ->> 'cueId' AS cue, h.payload ->>"
                + " 'collection' AS collection FROM social_narrative_member m LEFT JOIN record_head h ON h.source_id ="
                + " m.source_id AND h.external_id = m.external_id WHERE m.narrative_id = ? LIMIT 1000", narrativeId);
        String cue = null;
        for (Map<String, Object> row : rows) {
            Object c = row.get("cue");
            if (c == null || !"cue".equals(String.valueOf(row.get("collection")))) {
                return null;
            }
            cue = cue == null ? c.toString() : cue;
        }
        return cue;
    }

    // ------------------------------------------------------------------------- mapping

    private Comparison comparison(ResultSet rs) throws SQLException {
        return new Comparison(rs.getString("id"), rs.getString("claim_id"), rs.getString("narrative_id"),
                rs.getString("claim_type"), rs.getString("signal_family"), readMap(rs.getString("place")),
                (Double) rs.getObject("place_lon"), (Double) rs.getObject("place_lat"), rs.getString("region_code"),
                instant(rs, "window_start"), instant(rs, "window_end"), rs.getString("status"),
                readList(rs.getString("physical_evidence")), readMap(rs.getString("detectability")),
                readMap(rs.getString("lead_times")), readMap(rs.getString("contradiction")),
                readList(rs.getString("caveats")), rs.getString("found_by"), rs.getString("cue_id"),
                rs.getString("trigger_ref"), strings(rs.getArray("platforms")), rs.getInt("evidence_group_count"),
                rs.getString("question_id"), rs.getInt("revision"), instant(rs, "final_at"),
                rs.getString("manifest_id"), instant(rs, "evaluated_at"), instant(rs, "created_at"));
    }

    private Cue cue(ResultSet rs) throws SQLException {
        return new Cue(rs.getString("id"), rs.getString("trigger_type"), rs.getString("trigger_ref"),
                rs.getString("backend"), readList(rs.getString("queries")), strings(rs.getArray("platforms")),
                instant(rs, "window_start"), instant(rs, "window_end"), rs.getString("status"),
                rs.getString("status_reason"), readList(rs.getString("results")), strings(rs.getArray("narrative_ids")),
                rs.getBigDecimal("cost_usd"), rs.getBigDecimal("max_cost_usd"), readMap(rs.getString("input")),
                rs.getString("created_by"), instant(rs, "created_at"), instant(rs, "started_at"),
                instant(rs, "completed_at"), rs.getString("manifest_id"), readList(rs.getString("external_results")));
    }

    private Map<String, Object> readMap(String text) {
        return text == null ? null : json.readValue(text, MAP);
    }

    private List<Object> readList(String text) {
        return text == null ? new ArrayList<>() : json.readValue(text, LIST);
    }

    private String write(Object value) {
        return value == null ? null : json.writeValueAsString(value);
    }

    static List<String> strings(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        Object[] values = (Object[]) array.getArray();
        List<String> out = new ArrayList<>(values.length);
        for (Object v : values) {
            if (v != null) {
                out.add(String.valueOf(v));
            }
        }
        return out;
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }

    private static Timestamp ts(Instant at) {
        return at == null ? null : Timestamp.from(at);
    }

    static String[] array(Collection<String> values) {
        return values == null ? new String[0] : values.toArray(String[]::new);
    }

    static String clip(String text, int max) {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }
}
