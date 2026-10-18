package ai.foresignal.social.correlation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The status rules of a social–physical comparison ({@code SocialPhysicalComparator} 1.0.0, prompt 137). Pure.
 *
 * <ul>
 *   <li>{@code contradicted}: a matching agency or sensor record explicitly negates the claim, or the claim's media is
 *       a reuse finding;</li>
 *   <li>{@code corroborated}: at least one non-social evidence group matches (social evidence never counts,
 *       SOC-1);</li>
 *   <li>{@code pending}: nothing decisive yet and the window plus the family's publication lag is still open;</li>
 *   <li>{@code unobserved}: nothing matched and {@code canSeeAbsence} was ok over the full window (SOC-2);</li>
 *   <li>{@code insufficient-coverage}: nothing matched and {@code canSeeAbsence} failed.</li>
 * </ul>
 *
 * Corroboration may arrive during the window (status {@code corroborated} at once, still revisable until final);
 * absence is judged only once the window has closed.
 */
public final class SocialPhysicalComparator {

    private SocialPhysicalComparator() {}

    /**
     * Detectability over the claim's family, place and window ({@code canSeeAbsence}).
     *
     * @param p the combined detection probability
     * @param lo its lower bound
     * @param hi its upper bound
     * @param methods the methods used
     * @param ok the absence verdict
     * @param reason why not ok, or null
     */
    public record Detectability(double p, double lo, double hi, List<String> methods, boolean ok, String reason) {

        /** Copies the list. */
        public Detectability {
            methods = methods == null ? List.of() : List.copyOf(methods);
        }

        /** Unknown detectability (no estimate). */
        public static Detectability unknown(String reason) {
            return new Detectability(0, 0, 0, List.of(), false, reason);
        }

        /** The API shape. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("p", clamp(p));
            out.put("lo", clamp(lo));
            out.put("hi", clamp(hi));
            out.put("methods", methods);
            out.put("ok", ok);
            out.put("reason", reason);
            return out;
        }

        private static double clamp(double v) {
            return Double.isNaN(v) ? 0 : Math.max(0, Math.min(1, v));
        }
    }

    /**
     * Everything one evaluation needs.
     *
     * @param windowStart the window start
     * @param windowEnd the window end
     * @param finalAt the window end plus the publication lag
     * @param now the decision time
     * @param evidence the matching physical evidence (social left out by the source; left out again here)
     * @param detectability detectability over the window
     * @param mediaReuseRef the reuse finding of the claim's media, or null
     * @param socialFirst the earliest post supporting the claim
     * @param newsFirst the earliest news article of the narrative's stories, or null
     * @param triggerRef the cue trigger of a cue-found claim, or null
     */
    public record Inputs(Instant windowStart, Instant windowEnd, Instant finalAt, Instant now,
            List<PhysicalEvidenceSource.Evidence> evidence, Detectability detectability, String mediaReuseRef,
            Instant socialFirst, Instant newsFirst, String triggerRef) {

        /** Copies the list. */
        public Inputs {
            evidence = evidence == null ? List.of() : List.copyOf(evidence);
        }
    }

    /**
     * The outcome of one evaluation.
     *
     * @param status the comparison status
     * @param reason the deterministic reason
     * @param evidence the physical evidence kept (non-social)
     * @param contradiction the contradiction ({@code reason}, {@code ref}), or null
     * @param leadTimes the {@code SocialLeadTimes}
     * @param caveats the caveats
     */
    public record Decision(String status, String reason, List<PhysicalEvidenceSource.Evidence> evidence,
            Map<String, Object> contradiction, Map<String, Object> leadTimes, List<Map<String, Object>> caveats) {}

    /** Decides a comparison. */
    public static Decision decide(Inputs in) {
        List<PhysicalEvidenceSource.Evidence> kept = new ArrayList<>();
        for (PhysicalEvidenceSource.Evidence e : in.evidence()) {
            if (!e.social()) {
                kept.add(e);
            }
        }
        List<PhysicalEvidenceSource.Evidence> supporting = kept.stream().filter(e -> !e.negates()).toList();
        PhysicalEvidenceSource.Evidence negating = kept.stream().filter(PhysicalEvidenceSource.Evidence::negates)
                .findFirst().orElse(null);
        Map<String, Object> lead = leadTimes(in.socialFirst(), supporting, in.newsFirst());
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(Map.of("code", CorrelationVocabulary.OBSERVED_LEAD_TIME, "text", "A single comparison gives an"
                + " observed lead time, never predictive value; lead statistics use out-of-sample comparisons only."));
        if (in.triggerRef() != null && !supporting.isEmpty()
                && supporting.stream().allMatch(e -> e.ref().equals(in.triggerRef()))) {
            caveats.add(Map.of("code", CorrelationVocabulary.CUE_TRIGGER_MATCH, "text", "This claim was found by"
                    + " searching for the event that matches it; the match is by construction and is not independent"
                    + " confirmation of that event."));
        }
        if (in.mediaReuseRef() != null) {
            return new Decision(CorrelationVocabulary.CONTRADICTED, "the claim's image was first seen earlier or"
                    + " elsewhere (" + in.mediaReuseRef() + ")", kept, contradiction(CorrelationVocabulary.MEDIA_REUSE,
                    in.mediaReuseRef()), lead, caveats);
        }
        if (negating != null && supporting.isEmpty()) {
            return new Decision(CorrelationVocabulary.CONTRADICTED, "a " + negating.originatorClass() + " record ("
                    + negating.ref() + ") reports normal conditions for the place and time", kept,
                    contradiction(CorrelationVocabulary.NEGATING_RECORD, negating.ref()), lead, caveats);
        }
        if (!supporting.isEmpty()) {
            Set<String> groups = new LinkedHashSet<>();
            supporting.forEach(e -> groups.add(e.originator()));
            return new Decision(CorrelationVocabulary.CORROBORATED, supporting.size() + " independent physical"
                    + " observation(s) from " + groups.size() + " originator(s) match in place and window", kept, null,
                    lead, caveats);
        }
        if (in.now().isBefore(in.windowEnd())) {
            return new Decision(CorrelationVocabulary.PENDING, "the window is still open and nothing matched yet", kept,
                    null, lead, caveats);
        }
        Detectability d = in.detectability() == null ? Detectability.unknown("no estimate") : in.detectability();
        if (d.ok()) {
            return new Decision(CorrelationVocabulary.UNOBSERVED, String.format(java.util.Locale.ROOT, "no"
                    + " corresponding change observed although coverage could have seen it (p = %.2f)", d.p()), kept,
                    null, lead, caveats);
        }
        return new Decision(CorrelationVocabulary.INSUFFICIENT, "nothing matched and coverage could not have seen it ("
                + (d.reason() == null ? "low detectability" : d.reason()) + ")", kept, null, lead, caveats);
    }

    /**
     * The {@code SocialLeadTimes}: first observations and the signed lead of social first observation over each
     * (positive = social first). Physical first excludes agency records, which are the official first.
     */
    public static Map<String, Object> leadTimes(Instant socialFirst, Collection<PhysicalEvidenceSource.Evidence> evidence,
            Instant newsFirst) {
        Instant physical = null;
        Instant official = null;
        for (PhysicalEvidenceSource.Evidence e : evidence) {
            if (e.firstObservedAt() == null || e.negates() || e.social()) {
                continue;
            }
            if (e.official()) {
                official = official == null || e.firstObservedAt().isBefore(official) ? e.firstObservedAt() : official;
            } else {
                physical = physical == null || e.firstObservedAt().isBefore(physical) ? e.firstObservedAt() : physical;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("socialFirst", socialFirst == null ? null : socialFirst.toString());
        out.put("physicalFirst", physical == null ? null : physical.toString());
        out.put("newsFirst", newsFirst == null ? null : newsFirst.toString());
        out.put("officialFirst", official == null ? null : official.toString());
        out.put("leadOverPhysicalSeconds", lead(socialFirst, physical));
        out.put("leadOverNewsSeconds", lead(socialFirst, newsFirst));
        out.put("leadOverOfficialSeconds", lead(socialFirst, official));
        return out;
    }

    /** The lead of {@code social} over {@code other} in seconds (positive = social first), or null. */
    public static Double lead(Instant social, Instant other) {
        if (social == null || other == null) {
            return null;
        }
        return (double) Duration.between(social, other).toSeconds();
    }

    /**
     * The narrative status from its comparisons' statuses (136 vocabulary): {@code unassessed} without comparisons,
     * {@code pending} while none is final, the single final status when they agree (an insufficient-coverage claim
     * does not split an otherwise agreeing narrative), else {@code mixed}.
     */
    public static String narrativeStatus(Collection<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return "unassessed";
        }
        Set<String> finals = new LinkedHashSet<>();
        for (String s : statuses) {
            if (CorrelationVocabulary.isFinal(s)) {
                finals.add(s);
            }
        }
        if (finals.isEmpty()) {
            return CorrelationVocabulary.PENDING;
        }
        if (finals.size() == 1) {
            return finals.iterator().next();
        }
        Set<String> decisive = new LinkedHashSet<>(finals);
        decisive.remove(CorrelationVocabulary.INSUFFICIENT);
        return decisive.size() == 1 ? decisive.iterator().next() : CorrelationVocabulary.MIXED;
    }

    private static Map<String, Object> contradiction(String reason, String ref) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reason", reason);
        out.put("ref", ref);
        return out;
    }

    /** The {@code SocialPhysicalEvidence} view of evidence. */
    public static List<Object> evidenceView(List<PhysicalEvidenceSource.Evidence> evidence) {
        List<Object> out = new ArrayList<>();
        for (PhysicalEvidenceSource.Evidence e : evidence) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ref", e.ref());
            m.put("family", e.family());
            m.put("originator", e.originator());
            m.put("firstObservedAt", e.firstObservedAt() == null ? null : e.firstObservedAt().toString());
            m.put("distanceKm", e.distanceKm());
            m.put("negates", e.negates());
            out.add(m);
        }
        return out;
    }

    /** The {@code DetectorRef} of the comparator. */
    public static Map<String, Object> detectorRef() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", CorrelationVocabulary.COMPARATOR);
        out.put("version", CorrelationVocabulary.COMPARATOR_VERSION);
        out.put("params", Map.of("rules", CompareRules.bundled().version()));
        return out;
    }
}
