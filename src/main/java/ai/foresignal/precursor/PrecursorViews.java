package ai.foresignal.precursor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The wire forms of the precursor library (prompt 120): {@code Precursor}, {@code PrecursorBatch},
 * {@code PrecursorCandidate} and the {@code PrecursorEvent} summary, plus the conversions between stored rows and
 * {@link Candidate}s and the deterministic description.
 */
public final class PrecursorViews {

    private PrecursorViews() {}

    /** The {@code Precursor} of a library row. */
    public static Map<String, Object> precursor(PrecursorEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("kind", p.getKind());
        out.put("antecedent", p.getAntecedent());
        out.put("consequent", p.getConsequent());
        out.put("spatial", p.getSpatial() == null || p.getSpatial().isEmpty() ? Map.of("rule", "none") : p.getSpatial());
        out.put("regionCodes", p.getRegionCodes());
        out.put("lagMin", PrecursorRules.iso(Duration.ofSeconds(p.getLagMinSeconds())));
        out.put("lagMax", PrecursorRules.iso(Duration.ofSeconds(p.getLagMaxSeconds())));
        out.put("bestLag", p.getBestLagSeconds() == null ? null
                : PrecursorRules.iso(Duration.ofSeconds(p.getBestLagSeconds())));
        out.put("lagDistribution", p.getLagDistribution());
        out.put("hitRate", p.getHitRate());
        out.put("hitRateInterval", p.getHitRateLower() == null || p.getHitRateUpper() == null ? null
                : PrecursorMiner.interval(new double[] {p.getHitRateLower(), p.getHitRateUpper()}));
        out.put("baseRate", p.getBaseRate());
        out.put("lift", p.getLift());
        out.put("correlation", p.getCorrelation());
        out.put("correlationSign", p.getCorrelationSign());
        out.put("nAntecedent", p.getNAntecedent());
        out.put("discovery", p.getDiscovery());
        out.put("validation", p.getValidation());
        out.put("lastRetest", p.getLastRetest());
        out.put("batchId", p.getBatchId());
        out.put("batchSize", p.getBatchSize());
        out.put("status", p.getStatus());
        out.put("statusReason", p.getStatusReason());
        out.put("operatorValidated", p.isOperatorValidated());
        out.put("detector", p.getDetector());
        out.put("firstValidatedAt", text(p.getFirstValidatedAt()));
        out.put("lastTestedAt", text(p.getLastTestedAt()));
        out.put("origin", p.getOrigin());
        out.put("description", p.getDescription() == null ? "" : p.getDescription());
        out.put("associationNote", PrecursorText.ASSOCIATION_NOTE);
        out.put("caveats", p.getCaveats());
        out.put("history", p.getHistory());
        out.put("manifestId", p.getManifestId());
        out.put("createdAt", text(p.getCreatedAt()));
        out.put("updatedAt", text(p.getUpdatedAt()));
        return out;
    }

    /** The precursor summary of the {@code PrecursorEvent} payload. */
    public static Map<String, Object> summary(PrecursorEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("kind", p.getKind());
        out.put("status", p.getStatus());
        out.put("statusReason", p.getStatusReason());
        out.put("description", p.getDescription() == null ? "" : p.getDescription());
        out.put("lift", p.getLift());
        return out;
    }

    /** The {@code PrecursorBatch} of a batch row. */
    public static Map<String, Object> batch(PrecursorBatchEntity b) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", b.getId());
        out.put("jobId", b.getJobId());
        out.put("mode", b.getMode());
        out.put("trigger", b.getTrigger());
        out.put("status", b.getStatus());
        out.put("startedAt", text(b.getStartedAt()));
        out.put("finishedAt", text(b.getFinishedAt()));
        out.put("candidatesGenerated", b.getCandidatesGenerated());
        out.put("tested", b.getTested());
        out.put("capped", b.getCapped());
        out.put("insufficient", b.getInsufficient());
        out.put("survivors", b.getSurvivors());
        out.put("validated", b.getValidated());
        out.put("q", b.getQ());
        out.put("method", b.getMethod());
        out.put("windows", b.getWindows());
        out.put("error", b.getError());
        out.put("manifestId", b.getManifestId());
        return out;
    }

    /** The {@code PrecursorCandidate} of an operator candidate row. */
    public static Map<String, Object> candidate(PrecursorCandidateEntity c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.getId());
        out.put("name", c.getName());
        out.put("kind", c.getKind());
        out.put("antecedent", c.getAntecedent());
        out.put("consequent", c.getConsequent());
        out.put("spatial", c.getSpatial() == null || c.getSpatial().isEmpty() ? Map.of("rule", "none") : c.getSpatial());
        out.put("lagMin", PrecursorRules.iso(Duration.ofSeconds(c.getLagMinSeconds())));
        out.put("lagMax", PrecursorRules.iso(Duration.ofSeconds(c.getLagMaxSeconds())));
        out.put("regionCodes", c.getRegionCodes());
        out.put("createdBy", c.getCreatedBy());
        out.put("createdAt", text(c.getCreatedAt()));
        return out;
    }

    /**
     * The mining {@link Candidate} of an operator candidate row: its regions narrow both selectors that name none.
     *
     * @param c the row
     * @param frequency the series pair frequency, or null for an event candidate
     * @return the candidate
     */
    public static Candidate candidate(PrecursorCandidateEntity c, String frequency) {
        Map<String, Object> antecedent = withRegions(c.getAntecedent(), c.getRegionCodes());
        Map<String, Object> consequent = withRegions(c.getConsequent(), c.getRegionCodes());
        return new Candidate(c.getKind(), antecedent, consequent, c.getSpatial() == null || c.getSpatial().isEmpty()
                ? Map.of("rule", "none") : c.getSpatial(), Duration.ofSeconds(c.getLagMinSeconds()),
                Duration.ofSeconds(c.getLagMaxSeconds()), c.getRegionCodes(), "operator", c.getId(), 0, 0, frequency);
    }

    /** The {@link Candidate} of a library row (for re-tests and the live watch). */
    public static Candidate candidate(PrecursorEntity p) {
        Object frequency = p.getDetector() != null && p.getDetector().get("params") instanceof Map<?, ?> params
                ? params.get("frequency") : null;
        return new Candidate(p.getKind(), p.getAntecedent(), p.getConsequent(), p.getSpatial(),
                Duration.ofSeconds(p.getLagMinSeconds()), Duration.ofSeconds(p.getLagMaxSeconds()), p.getRegionCodes(),
                p.getOrigin(), p.getCandidateId(), p.getNAntecedent(), 0, frequency == null ? null
                        : frequency.toString());
    }

    /**
     * The deterministic description of a library row from its headline statistics.
     *
     * @param p the row (headline statistics already applied)
     * @param from the window the headline statistics come from
     * @param to its end
     * @param validated true when validated on that window
     * @return the description
     */
    public static String describe(PrecursorEntity p, Instant from, Instant to, boolean validated) {
        String antecedent = label(p.getAntecedent());
        String consequent = label(p.getConsequent());
        if (Candidate.EVENT.equals(p.getKind())) {
            double[] interval = p.getHitRateLower() == null || p.getHitRateUpper() == null ? new double[] {0, 1}
                    : new double[] {p.getHitRateLower(), p.getHitRateUpper()};
            return PrecursorText.event(p.getRegionCodes(), antecedent, consequent,
                    Duration.ofSeconds(p.getLagMaxSeconds()), p.getHits(), p.getNAntecedent(), interval,
                    p.getBaseRate() == null ? Double.NaN : p.getBaseRate(), from, to, validated);
        }
        Object pValue = p.getValidation() != null ? p.getValidation().get("p") : p.getDiscovery().get("p");
        return PrecursorText.series(p.getRegionCodes(), antecedent, consequent, p.getBestLagSeconds() == null ? null
                : Duration.ofSeconds(p.getBestLagSeconds()), p.getCorrelation() == null ? Double.NaN
                : p.getCorrelation(), pValue instanceof Number n ? n.doubleValue() : Double.NaN, from, to, validated);
    }

    /** The label of a side. */
    static String label(Map<String, Object> side) {
        if (side == null) {
            return "?";
        }
        if (side.get("label") != null) {
            return side.get("label").toString();
        }
        Map<String, Object> selector = Candidate.selector(side);
        if (selector != null) {
            return PrecursorOccurrences.label(selector);
        }
        return String.valueOf(side.get("seriesRef"));
    }

    /** A page of items. */
    public static Map<String, Object> page(List<Map<String, Object>> items, String nextCursor) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", nextCursor);
        return out;
    }

    private static Map<String, Object> withRegions(Map<String, Object> side, List<String> regions) {
        Map<String, Object> out = new LinkedHashMap<>(side == null ? Map.of() : side);
        Map<String, Object> selector = Candidate.selector(out);
        if (selector != null && regions != null && !regions.isEmpty() && selector.get("regionCodes") == null) {
            Map<String, Object> copy = new LinkedHashMap<>(selector);
            copy.put("regionCodes", new ArrayList<>(regions));
            out.put("selector", copy);
        }
        return out;
    }

    private static String text(Instant at) {
        return at == null ? null : at.toString();
    }
}
