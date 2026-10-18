package ai.foresignal.social.correlation;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.social.narrative.NarrativeViews;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The API and MCP views of comparisons and cues (prompt 137, OpenAPI {@code SocialComparison},
 * {@code SocialComparisonPage}, {@code SocialCue}, {@code SocialCuePage}).
 */
@Component
public class CorrelationViews {

    /** The default page size. */
    public static final int DEFAULT_LIMIT = 100;

    /** The largest page size. */
    public static final int MAX_LIMIT = 500;

    private final ComparisonStore store;

    /** Creates the views. */
    public CorrelationViews(ComparisonStore store) {
        this.store = store;
    }

    /**
     * The {@code SocialComparison} view.
     *
     * @param c the comparison
     * @param revisions its revisions, or null for the list view
     */
    public static Map<String, Object> comparison(ComparisonStore.Comparison c,
            List<ComparisonStore.Revision> revisions) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.id());
        out.put("ref", "social-comparison:" + c.id());
        out.put("claimId", c.claimId());
        out.put("narrativeId", c.narrativeId());
        out.put("signalFamily", c.signalFamily());
        out.put("place", c.place());
        out.put("window", window(c.windowStart(), c.windowEnd()));
        out.put("status", c.status());
        out.put("physicalEvidence", c.physicalEvidence());
        out.put("detectability", c.detectability());
        out.put("leadTimes", c.leadTimes());
        out.put("contradiction", c.contradiction());
        out.put("foundBy", c.foundBy());
        out.put("cueId", c.cueId());
        out.put("questionId", c.questionId());
        out.put("revision", c.revision());
        out.put("finalAt", c.finalAt() == null ? null : c.finalAt().toString());
        out.put("comparator", SocialPhysicalComparator.detectorRef());
        out.put("manifestId", c.manifestId());
        out.put("evaluatedAt", c.evaluatedAt().toString());
        if (revisions != null) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ComparisonStore.Revision r : revisions) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("revision", r.revision());
                m.put("at", r.at().toString());
                m.put("status", r.status());
                m.put("previousStatus", r.previousStatus());
                m.put("reason", r.reason());
                m.put("physicalEvidenceRefs", r.refs());
                m.put("manifestId", r.manifestId());
                list.add(m);
            }
            out.put("revisions", list);
        }
        out.put("caveats", c.caveats());
        return out;
    }

    /** The {@code SocialCue} view. */
    public static Map<String, Object> cue(ComparisonStore.Cue c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.id());
        Map<String, Object> trigger = new LinkedHashMap<>();
        trigger.put("type", c.triggerType());
        trigger.put("ref", c.triggerRef());
        out.put("trigger", trigger);
        out.put("backend", c.backend());
        out.put("queries", c.queries());
        out.put("platforms", c.platforms());
        out.put("window", window(c.windowStart(), c.windowEnd()));
        out.put("status", c.status());
        out.put("statusReason", c.statusReason());
        out.put("results", c.results());
        out.put("narrativeIds", c.narrativeIds());
        out.put("costUsd", c.costUsd() == null ? 0 : c.costUsd().doubleValue());
        out.put("maxCostUsd", c.maxCostUsd() == null ? null : c.maxCostUsd().doubleValue());
        out.put("createdBy", c.createdBy());
        out.put("createdAt", c.createdAt().toString());
        out.put("startedAt", c.startedAt() == null ? null : c.startedAt().toString());
        out.put("completedAt", c.completedAt() == null ? null : c.completedAt().toString());
        out.put("manifestId", c.manifestId());
        return out;
    }

    private static Map<String, Object> window(Instant start, Instant end) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("start", start.toString());
        out.put("end", end == null ? null : end.toString());
        return out;
    }

    /** One comparison with its revisions; 404 {@code UNKNOWN_COMPARISON}. */
    public Map<String, Object> detail(String id) {
        ComparisonStore.Comparison c = require(id);
        return comparison(c, store.revisions(c.id()));
    }

    /** The comparison or 404 {@code UNKNOWN_COMPARISON}. */
    public ComparisonStore.Comparison require(String id) {
        return (id == null ? java.util.Optional.<ComparisonStore.Comparison>empty() : store.comparison(id))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_COMPARISON, "No social comparison '" + id
                        + "'."));
    }

    /** The comparison of a claim with its revisions, or null. */
    public Map<String, Object> byClaim(String claimId) {
        return store.byClaim(claimId).map(c -> comparison(c, store.revisions(c.id()))).orElse(null);
    }

    /** The comparisons of a narrative with their revisions. */
    public List<Map<String, Object>> byNarrative(String narrativeId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ComparisonStore.Comparison c : store.byNarrative(narrativeId)) {
            out.add(comparison(c, store.revisions(c.id())));
        }
        return out;
    }

    /**
     * A page of comparisons, newest evaluation first.
     *
     * @param statuses statuses (empty for all)
     * @param situationId a situation linked to the comparison's narrative, or null
     * @param narrativeId a narrative, or null
     * @param claimId a claim, or null
     * @param family a signal family, or null
     * @param region a 040 region (also matches its admin-1 regions), or null
     * @param since the earliest evaluation, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     */
    public NarrativeViews.Page comparisons(List<String> statuses, String situationId, String narrativeId,
            String claimId, String family, String region, Instant since, String cursor, int limit) {
        List<String> where = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (!statuses.isEmpty()) {
            where.add("status = ANY(?)");
            args.add(statuses.toArray(String[]::new));
        }
        if (situationId != null) {
            where.add("narrative_id IN (SELECT id FROM social_narrative WHERE ? = ANY(situation_ids))");
            args.add(situationId);
        }
        if (narrativeId != null) {
            where.add("narrative_id = ?");
            args.add(narrativeId);
        }
        if (claimId != null) {
            where.add("claim_id = ?");
            args.add(claimId);
        }
        if (family != null) {
            where.add("signal_family = ?");
            args.add(family);
        }
        if (region != null) {
            where.add("(region_code = ? OR region_code LIKE ?)");
            args.add(region);
            args.add(region + "-%");
        }
        if (since != null) {
            where.add("evaluated_at >= ?");
            args.add(Timestamp.from(since));
        }
        List<ComparisonStore.Comparison> rows = store.page(String.join(" AND ", where), args,
                NarrativeViews.decode(cursor), limit);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < rows.size() && i < limit; i++) {
            items.add(comparison(rows.get(i), null));
        }
        String next = rows.size() > limit ? NarrativeViews.encode(rows.get(limit - 1).evaluatedAt(),
                rows.get(limit - 1).id()) : null;
        return new NarrativeViews.Page(items, next);
    }

    /** One cue; 404 {@code UNKNOWN_CUE}. */
    public Map<String, Object> cueDetail(String id) {
        return cue((id == null ? java.util.Optional.<ComparisonStore.Cue>empty() : store.cue(id))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_CUE, "No social cue '" + id + "'.")));
    }

    /** A page of cues, newest first. */
    public NarrativeViews.Page cues(List<String> statuses, Instant since, String cursor, int limit) {
        List<String> where = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (!statuses.isEmpty()) {
            where.add("status = ANY(?)");
            args.add(statuses.toArray(String[]::new));
        }
        if (since != null) {
            where.add("created_at >= ?");
            args.add(Timestamp.from(since));
        }
        List<ComparisonStore.Cue> rows = store.cuePage(String.join(" AND ", where), args, NarrativeViews.decode(cursor),
                limit);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < rows.size() && i < limit; i++) {
            items.add(cue(rows.get(i)));
        }
        String next = rows.size() > limit ? NarrativeViews.encode(rows.get(limit - 1).createdAt(),
                rows.get(limit - 1).id()) : null;
        return new NarrativeViews.Page(items, next);
    }
}
