package ai.foresignal.cycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the deterministic packet of a cycle run ({@code cycle-packet} 1.0.0, prompt 128 §2) from the raw inputs the
 * {@link CyclePacketReader} read: it applies the scope, ranks each section (score, then recency, then reference),
 * and produces two views of the same packet. The <strong>stored</strong> packet is complete; the <strong>LLM</strong>
 * packet keeps the 30 best-ranked items per section (the 102 cap), records what it cut in {@code truncatedForLlm},
 * and leaves private items out unless the provider may receive them. Pure: the same inputs give the same packet.
 */
public final class CyclePacketBuilder {

    /** The packet format. */
    public static final String VERSION = "cycle-packet 1.0.0";

    /** The ranked sections, as {@code group.name} (a bare name is a top-level list). */
    public static final List<String> SECTIONS = List.of("situations.new", "situations.activated",
            "situations.escalated", "situations.dormant", "situations.closed", "hypotheses.revisions",
            "hypotheses.bandChanges", "hypotheses.indicatorsObserved", "hypotheses.indicatorsAbsent",
            "hypotheses.indicatorsUnknownCoverage", "hypotheses.assumptionsBroken", "hypotheses.assumptionsQuestioned",
            "hypotheses.dueForReview", "findings", "forecasts.opened", "forecasts.due", "forecasts.resolved",
            "forecasts.scorecardChanges", "outlooks.indexChanges", "outlooks.windowsOutsideP10P90", "verdicts");

    private CyclePacketBuilder() {}

    /**
     * The raw inputs.
     *
     * @param since the period start
     * @param until the period end
     * @param firstRun true when {@code since} is the cadence's natural lookback
     * @param sections the raw items per section (keys of {@link #SECTIONS})
     * @param failingSources sources failing now
     * @param degradedSources sources degraded now
     * @param lowDensityRegions regions flagged {@code LOW_REPORTING_DENSITY}
     * @param collectionGaps the 125 collection gaps, each with {@code setId}, {@code setCadence},
     *     {@code situationId}, {@code regionCodes} and {@code private} beside the {@code CollectionGap} fields
     * @param previousJudgments the previous assessment's key judgments
     */
    public record Inputs(Instant since, Instant until, boolean firstRun, Map<String, List<CyclePacketItem>> sections,
            List<String> failingSources, List<String> degradedSources, List<String> lowDensityRegions,
            List<Map<String, Object>> collectionGaps, List<Map<String, Object>> previousJudgments) {

        /** Copies the members. */
        public Inputs {
            sections = sections == null ? Map.of() : Map.copyOf(sections);
            failingSources = failingSources == null ? List.of() : List.copyOf(failingSources);
            degradedSources = degradedSources == null ? List.of() : List.copyOf(degradedSources);
            lowDensityRegions = lowDensityRegions == null ? List.of() : List.copyOf(lowDensityRegions);
            collectionGaps = collectionGaps == null ? List.of() : List.copyOf(collectionGaps);
            previousJudgments = previousJudgments == null ? List.of() : List.copyOf(previousJudgments);
        }
    }

    /**
     * A built packet.
     *
     * @param stored the complete packet ({@code CyclePacket}), as stored on the run
     * @param llm the capped packet the LLM roles see
     * @param items the scoped, ranked items per section (complete)
     * @param privateInput true when any scoped item or gap was private
     * @param refs every scoped object reference, for citations and the manifest
     */
    public record Packet(Map<String, Object> stored, Map<String, Object> llm, Map<String, List<CyclePacketItem>> items,
            boolean privateInput, List<String> refs) {

        /** The ranked items of {@code section}. */
        public List<CyclePacketItem> section(String section) {
            return items.getOrDefault(section, List.of());
        }

        /** The total number of ranked items. */
        public int size() {
            return items.values().stream().mapToInt(List::size).sum();
        }
    }

    /** The ranking order: score descending, then newest first, then reference. */
    static final Comparator<CyclePacketItem> ORDER = Comparator.comparingDouble(CyclePacketItem::score).reversed()
            .thenComparing(CyclePacketItem::at, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(CyclePacketItem::ref).thenComparing(CyclePacketItem::change);

    /**
     * Builds the packet.
     *
     * @param in the raw inputs
     * @param scope the scope
     * @param cadence the cycle cadence ({@code cadence-match} and review due)
     * @param llmPrivateAllowed true when the LLM provider may receive private items
     */
    public static Packet build(Inputs in, CycleScope scope, String cadence, boolean llmPrivateAllowed) {
        Map<String, List<CyclePacketItem>> ranked = new LinkedHashMap<>();
        boolean privateInput = false;
        Set<String> refs = new LinkedHashSet<>();
        for (String section : SECTIONS) {
            List<CyclePacketItem> kept = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (CyclePacketItem item : in.sections().getOrDefault(section, List.of())) {
                if (!item.admittedBy(scope, cadence)) {
                    continue;
                }
                if ("hypotheses.dueForReview".equals(section) && !reviewDue(item.setCadence(), cadence)) {
                    continue;
                }
                if (!seen.add(item.ref() + "|" + item.change() + "|" + item.at())) {
                    continue;
                }
                kept.add(item);
            }
            kept.sort(ORDER);
            for (CyclePacketItem item : kept) {
                privateInput |= item.privateItem();
                refs.add(item.ref());
            }
            ranked.put(section, List.copyOf(kept));
        }
        List<Map<String, Object>> gaps = new ArrayList<>();
        List<Map<String, Object>> gapsForLlm = new ArrayList<>();
        for (Map<String, Object> gap : in.collectionGaps()) {
            String setId = text(gap.get("setId"));
            boolean admitted = scope.admits(CycleScope.strings(gap.get("regionCodes")), null,
                    text(gap.get("situationId")), setId, text(gap.get("setCadence")), List.of(), null, cadence);
            if (!admitted) {
                continue;
            }
            boolean priv = Boolean.TRUE.equals(gap.get("private"));
            privateInput |= priv;
            Map<String, Object> view = gapView(gap);
            gaps.add(view);
            if (!priv || llmPrivateAllowed) {
                gapsForLlm.add(view);
            }
        }
        Map<String, Object> stored = skeleton(in, cadence);
        Map<String, Object> llm = skeleton(in, cadence);
        Map<String, Integer> truncated = new LinkedHashMap<>();
        for (String section : SECTIONS) {
            List<CyclePacketItem> items = ranked.get(section);
            List<Map<String, Object>> full = new ArrayList<>();
            List<Map<String, Object>> capped = new ArrayList<>();
            int rank = 0;
            int llmRank = 0;
            int cut = 0;
            for (CyclePacketItem item : items) {
                rank++;
                full.add(item.toMap(rank));
                if (item.privateItem() && !llmPrivateAllowed) {
                    continue;
                }
                if (llmRank < CycleVocabulary.LLM_SECTION_CAP) {
                    llmRank++;
                    capped.add(item.toMap(llmRank));
                } else {
                    cut++;
                }
            }
            put(stored, section, full);
            put(llm, section, capped);
            if (cut > 0) {
                truncated.put(section, cut);
            }
        }
        stored.put("collectionGaps", gaps);
        llm.put("collectionGaps", gapsForLlm.size() > CycleVocabulary.LLM_SECTION_CAP
                ? gapsForLlm.subList(0, CycleVocabulary.LLM_SECTION_CAP) : gapsForLlm);
        if (gapsForLlm.size() > CycleVocabulary.LLM_SECTION_CAP) {
            truncated.put("collectionGaps", gapsForLlm.size() - CycleVocabulary.LLM_SECTION_CAP);
        }
        stored.put("truncatedForLlm", truncated);
        llm.put("truncatedForLlm", truncated);
        return new Packet(stored, llm, ranked, privateInput, List.copyOf(refs));
    }

    /** True when a set of {@code setCadence} is reviewed by a cycle of {@code cycleCadence} (daily ⊂ weekly ⊂ monthly). */
    static boolean reviewDue(String setCadence, String cycleCadence) {
        if (cycleCadence == null || CycleVocabulary.ON_DEMAND.equals(cycleCadence)) {
            return true;
        }
        int set = setCadence == null ? -1 : CycleVocabulary.CADENCES.indexOf(setCadence);
        int cycle = CycleVocabulary.CADENCES.indexOf(cycleCadence);
        return set < 0 || cycle < 0 || set <= cycle;
    }

    private static Map<String, Object> skeleton(Inputs in, String cadence) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("version", VERSION);
        out.put("cadence", cadence);
        out.put("since", in.since().toString());
        out.put("until", in.until().toString());
        out.put("firstRun", in.firstRun());
        Map<String, Object> coverage = new LinkedHashMap<>();
        coverage.put("failingSources", in.failingSources());
        coverage.put("degradedSources", in.degradedSources());
        coverage.put("lowReportingDensityRegions", in.lowDensityRegions());
        out.put("coverage", coverage);
        out.put("previousJudgments", in.previousJudgments());
        out.put("social", null);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void put(Map<String, Object> packet, String section, List<Map<String, Object>> items) {
        int dot = section.indexOf('.');
        if (dot < 0) {
            packet.put(section, items);
            return;
        }
        String group = section.substring(0, dot);
        Map<String, Object> holder = (Map<String, Object>) packet.computeIfAbsent(group, k -> new LinkedHashMap<>());
        holder.put(section.substring(dot + 1), items);
    }

    /** The {@code CollectionGap} fields of a gap row (scope attributes dropped). */
    static Map<String, Object> gapView(Map<String, Object> gap) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : List.of("id", "setId", "indicatorId", "potentialEig", "actualEig", "pDetect",
                "recommendations", "createdAt", "updatedAt")) {
            if (gap.containsKey(key)) {
                out.put(key, gap.get(key));
            }
        }
        out.putIfAbsent("recommendations", List.of());
        return out;
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
