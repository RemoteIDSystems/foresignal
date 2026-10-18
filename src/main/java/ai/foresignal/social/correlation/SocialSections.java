package ai.foresignal.social.correlation;

import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.social.SocialObservability;
import ai.foresignal.social.narrative.NarrativeStore;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The social sections of situations, cycle packets and assessments (prompt 137), always kept apart from physical
 * corroboration.
 *
 * <ul>
 *   <li>{@link #situation} — {@code SituationSocialSection}: the situation's top narratives with status, velocity,
 *       origin concentration, observed lead time and {@code foundBy}; its narrative evidence groups (counted
 *       separately from physical corroboration); its attention gap; and whether social coverage of its regions is
 *       adequate.</li>
 *   <li>{@link #versusObserved} — {@code SocialVersusObserved}: "What people are saying versus what we observe":
 *       narratives by status, attention gaps, notable out-of-sample lead times only, media reuse and coordination
 *       findings, with {@link #markdown} rendering it under the ICD 203 wording rules (no likelihood terms; checked
 *       with {@link EstimativeLanguage#lint}).</li>
 * </ul>
 */
@Component
public class SocialSections {

    /** The top narratives listed. */
    static final int TOP = 10;

    /** The findings listed per kind. */
    static final int FINDINGS = 20;

    private final NarrativeStore narratives;
    private final ComparisonStore comparisons;
    private final LeadTimeStatistics leadTimes;
    private final ObjectProvider<SocialObservability> observability;
    private final Clock clock;

    /** Creates the component. */
    public SocialSections(NarrativeStore narratives, ComparisonStore comparisons, LeadTimeStatistics leadTimes,
            ObjectProvider<SocialObservability> observability, Clock clock) {
        this.narratives = narratives;
        this.comparisons = comparisons;
        this.leadTimes = leadTimes;
        this.observability = observability;
        this.clock = clock;
    }

    /** The unmerged narratives linked to any of the situations, most independent origins first. */
    List<NarrativeStore.Narrative> linked(Collection<String> situationIds, int limit) {
        if (situationIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = narratives.jdbc().queryForList("SELECT id FROM social_narrative WHERE situation_ids && ?"
                + " AND merged_into IS NULL ORDER BY evidence_group_count DESC, last_activity_at DESC, id LIMIT ?",
                String.class, situationIds.toArray(String[]::new), limit);
        Map<String, NarrativeStore.Narrative> rows = narratives.narratives(ids);
        List<NarrativeStore.Narrative> out = new ArrayList<>();
        for (String id : ids) {
            if (rows.get(id) != null) {
                out.add(rows.get(id));
            }
        }
        return out;
    }

    /** The narrative row of a situation section. */
    Map<String, Object> narrativeRow(NarrativeStore.Narrative n) {
        Double lead = null;
        String foundBy = CorrelationVocabulary.ORGANIC;
        for (ComparisonStore.Comparison c : comparisons.byNarrative(n.id())) {
            if (CorrelationVocabulary.CUE.equals(c.foundBy())) {
                foundBy = CorrelationVocabulary.CUE;
            }
            if (CorrelationVocabulary.CORROBORATED.equals(c.status()) && c.leadTimes() != null
                    && c.leadTimes().get("leadOverPhysicalSeconds") instanceof Number l
                    && (lead == null || l.doubleValue() > lead)) {
                lead = l.doubleValue();
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("narrativeId", n.id());
        m.put("title", n.title());
        m.put("status", n.status());
        m.put("velocity", n.velocity());
        m.put("originConcentration", n.originConcentration());
        m.put("evidenceGroupCount", n.evidenceGroupCount());
        m.put("leadOverPhysicalSeconds", lead);
        m.put("foundBy", foundBy);
        return m;
    }

    /**
     * The {@code SituationSocialSection} of a situation.
     *
     * @param situationId the situation
     * @param regionCodes its regions
     */
    public Map<String, Object> situation(String situationId, List<String> regionCodes) {
        Instant now = clock.instant();
        List<NarrativeStore.Narrative> all = linked(List.of(situationId), 500);
        int groups = 0;
        List<Map<String, Object>> top = new ArrayList<>();
        for (NarrativeStore.Narrative n : all) {
            groups += n.evidenceGroupCount();
            if (top.size() < TOP) {
                top.add(narrativeRow(n));
            }
        }
        Map<String, Object> gap = null;
        List<Map<String, Object>> gaps = narratives.jdbc().queryForList("SELECT id, stats ->> 'expected' AS expected,"
                + " stats ->> 'observed' AS observed, stats -> 'platforms' AS platforms FROM analytic_finding WHERE"
                + " finding_type = ? AND status = 'open' AND stats ->> 'ref' = ? ORDER BY detected_at DESC LIMIT 1",
                CorrelationVocabulary.ATTENTION_GAP_FINDING, "situation:" + situationId);
        if (!gaps.isEmpty()) {
            Map<String, Object> row = gaps.get(0);
            gap = new LinkedHashMap<>();
            gap.put("findingId", row.get("id"));
            gap.put("expected", parse(row.get("expected")));
            gap.put("observed", parse(row.get("observed")));
            gap.put("platforms", platforms(row.get("platforms")));
        }
        boolean coverage = false;
        SocialObservability obs = observability.getIfAvailable();
        if (obs != null) {
            for (String region : regionCodes) {
                AbsenceCheck check = obs.canSeeAbsence(region, now.minus(Duration.ofHours(6)), now);
                if (check.ok()) {
                    coverage = true;
                    break;
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("narratives", top);
        out.put("socialEvidenceGroups", groups);
        out.put("attentionGap", gap);
        out.put("socialCoverageOk", coverage);
        out.put("updatedAt", now.toString());
        return out;
    }

    /**
     * The {@code SocialVersusObserved} section of a packet or assessment.
     *
     * @param situationIds the situations in scope (empty: every narrative active in the period)
     * @param since the period start
     * @param until the period end
     */
    public Map<String, Object> versusObserved(List<String> situationIds, Instant since, Instant until) {
        List<NarrativeStore.Narrative> rows;
        if (!situationIds.isEmpty()) {
            rows = linked(situationIds, 500);
        } else {
            List<String> ids = narratives.jdbc().queryForList("SELECT id FROM social_narrative WHERE merged_into IS NULL"
                    + " AND last_activity_at >= ? AND first_observed_at <= ? ORDER BY evidence_group_count DESC, id"
                    + " LIMIT 500", String.class, Timestamp.from(since), Timestamp.from(until));
            rows = new ArrayList<>(narratives.narratives(ids).values());
            rows.sort((a, b) -> Integer.compare(b.evidenceGroupCount(), a.evidenceGroupCount()));
        }
        Map<String, Integer> byStatus = new TreeMap<>();
        List<Map<String, Object>> notable = new ArrayList<>();
        for (NarrativeStore.Narrative n : rows) {
            byStatus.merge(n.status(), 1, Integer::sum);
            if (notable.size() < TOP && !"unassessed".equals(n.status())) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("narrativeId", n.id());
                m.put("title", n.title());
                m.put("status", n.status());
                m.put("situationId", n.situationIds().isEmpty() ? null : n.situationIds().get(0));
                notable.add(m);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("narrativesByStatus", byStatus);
        out.put("narratives", notable);
        out.put("attentionGapFindingIds", findings(CorrelationVocabulary.ATTENTION_GAP_FINDING, since, until));
        out.put("leadTimes", leadTimes.notable(null));
        out.put("mediaReuseFindingIds", findings("media-reuse", since, until));
        out.put("coordinationFindingIds", findings("social-coordination", since, until));
        out.put("caveats", List.of(Map.of("code", "SOCIAL_IS_REPORTING", "text", "What people say is reporting, not"
                + " observation: narratives never corroborate physical events, unobserved means no corresponding change"
                + " was seen where coverage was adequate, and only out-of-sample lead times are stated.")));
        return out;
    }

    private List<String> findings(String type, Instant since, Instant until) {
        return narratives.jdbc().queryForList("SELECT id FROM analytic_finding WHERE finding_type = ? AND status ="
                + " 'open' AND NOT private AND detected_at BETWEEN ? AND ? ORDER BY detected_at DESC LIMIT ?",
                String.class, type, Timestamp.from(since), Timestamp.from(until), FINDINGS);
    }

    /**
     * The markdown of "What people are saying versus what we observe". Deterministic; states counts, statuses and
     * measured lead times, never likelihood terms. Pure.
     */
    @SuppressWarnings("unchecked")
    public static String markdown(Map<String, Object> section) {
        StringBuilder md = new StringBuilder("*Narratives are what people say; statuses come only from comparison with"
                + " independent physical evidence, with detectability taken into account.*\n\n");
        Map<String, Object> byStatus = section.get("narrativesByStatus") instanceof Map<?, ?> m
                ? (Map<String, Object>) m : Map.of();
        if (byStatus.isEmpty()) {
            md.append("No narrative in scope.\n\n");
        } else {
            md.append("| Status | Narratives |\n|---|---|\n");
            byStatus.forEach((k, v) -> md.append("| ").append(k).append(" | ").append(v).append(" |\n"));
            md.append('\n');
        }
        for (Object o : section.get("narratives") instanceof List<?> l ? l : List.of()) {
            if (o instanceof Map<?, ?> n) {
                md.append("- `narrative:").append(n.get("narrativeId")).append("` ").append(n.get("title"))
                        .append(": ").append(describe(String.valueOf(n.get("status")))).append('\n');
            }
        }
        List<Object> lead = section.get("leadTimes") instanceof List<?> l ? (List<Object>) l : List.of();
        md.append("\n**Lead times (out of sample only).** ");
        if (lead.isEmpty()) {
            md.append("No kind of social signal has enough out-of-sample comparisons to state a lead.\n");
        } else {
            md.append('\n');
            for (Object o : lead) {
                if (o instanceof Map<?, ?> g && g.get("overPhysical") instanceof Map<?, ?> s && s.get("median") != null) {
                    md.append(String.format(Locale.ROOT, "- %s claims on %s%s: median %.0f minutes before the first"
                            + " physical observation (n = %s).%n", g.get("claimType"), g.get("platform"),
                            g.get("regionCode") == null ? "" : " in " + g.get("regionCode"),
                            ((Number) s.get("median")).doubleValue() / 60.0, s.get("n")));
                }
            }
        }
        md.append(String.format(Locale.ROOT, "%nAttention gaps: %d. Media reuse findings: %d. Coordination findings:"
                + " %d.%n", size(section.get("attentionGapFindingIds")), size(section.get("mediaReuseFindingIds")),
                size(section.get("coordinationFindingIds"))));
        return md.toString();
    }

    /** The lint warnings of the rendered section (ICD 203). */
    public static List<String> lint(String markdown) {
        List<String> out = new ArrayList<>();
        for (EstimativeLanguage.Violation v : EstimativeLanguage.lint(markdown, List.of())) {
            out.add(v.code() + ": " + v.message());
        }
        return out;
    }

    /** The plain wording of a status. */
    static String describe(String status) {
        return switch (status) {
            case "corroborated" -> "independent physical evidence matches";
            case "unobserved" -> "no corresponding change observed where coverage was adequate";
            case "contradicted" -> "an agency or sensor record, or reused media, contradicts it";
            case "mixed" -> "its claims split between matched and not matched";
            case "insufficient-coverage" -> "coverage could not have seen it";
            case "pending" -> "comparison window still open";
            default -> "not yet compared";
        };
    }

    private static int size(Object v) {
        return v instanceof Collection<?> c ? c.size() : 0;
    }

    private static Double parse(Object v) {
        try {
            return v == null ? null : Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<String> platforms(Object v) {
        List<String> out = new ArrayList<>();
        if (v != null) {
            String text = v.toString().replace("[", "").replace("]", "").replace("\"", "");
            for (String p : text.split(",")) {
                if (!p.isBlank()) {
                    out.add(p.strip());
                }
            }
        }
        return out;
    }
}
