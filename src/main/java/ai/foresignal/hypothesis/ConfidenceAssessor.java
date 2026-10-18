package ai.foresignal.hypothesis;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rule-based analytic confidence ({@code ConfidenceAssessor} {@value #VERSION}, prompt 125 §4.7). Confidence is a
 * separate judgment from probability: it rates the evidence, never the likelihood.
 *
 * <ul>
 *   <li><b>high</b>: at least two observed indicators of credibility M or H from at least two signal families, every
 *       such indicator's source graded A or B by 109 (or with no grade to judge), and no
 *       {@code LOW_REPORTING_DENSITY} region;</li>
 *   <li><b>moderate</b>: at least one observed indicator of credibility M or H;</li>
 *   <li><b>low</b>: otherwise.</li>
 * </ul>
 * A broken linchpin assumption lowers the level one step (so it also rules out high). Since 1.1.0 (prompt 133),
 * supporting news evidence that is skewed ({@code SKEWED_COVERAGE}: at least 75% from one lean bucket or from
 * state-affiliated outlets) <em>and</em> has no non-news corroboration lowers it one step more; skewed coverage with
 * corroboration changes nothing (coverage describes reporting, never truth). Since 1.2.0 (prompt 134), observed
 * indicators supported by products that share one origin (circular reporting) are counted once before the rules are
 * applied, and the basis reports {@code circularReporting} ({@value #CIRCULAR_REPORTING} or {@code ok}) with the number
 * of ACH rows that carry the caveat. Pure.
 */
public final class ConfidenceAssessor {

    /** The assessor version. */
    public static final String VERSION = "1.2.0";

    /** The reporting-density caveat of 121. */
    public static final String LOW_REPORTING_DENSITY = "LOW_REPORTING_DENSITY";

    private ConfidenceAssessor() {}

    /**
     * One observed indicator.
     *
     * @param indicatorId the indicator
     * @param family its signal family
     * @param credibility H, M or L
     * @param sourceReliability the 109 reliability grade of the source that observed it (A–F), or null when there is
     *     none to judge (manual observations, findings)
     */
    public record Observed(String indicatorId, String family, String credibility, String sourceReliability) {}

    /**
     * The assessor's inputs.
     *
     * @param observed the observed indicators that bear on the judgment
     * @param brokenLinchpin true when a linchpin assumption is broken
     * @param lowReportingDensity true when a region of the set is in the lowest reporting-density decile
     * @param evidenceQuality an evidence-quality note, or null
     * @param coverage a coverage note, or null
     * @param forecasterAgreement agreement between ledger forecasters (1 − spread), or null
     * @param skewedUncorroboratedNews true when the supporting news evidence is skewed and nothing outside the news
     *     corroborates it (prompt 133)
     * @param circularSupports the ACH rows of the set marked {@code CIRCULAR_REPORTING} (prompt 134)
     */
    public record Inputs(List<Observed> observed, boolean brokenLinchpin, boolean lowReportingDensity,
            String evidenceQuality, String coverage, Double forecasterAgreement, boolean skewedUncorroboratedNews,
            int circularSupports) {

        /** Copies the list. */
        public Inputs {
            observed = observed == null ? List.of() : List.copyOf(observed);
            circularSupports = Math.max(0, circularSupports);
        }

        /** Inputs without circular reporting (the 1.1.0 inputs). */
        public Inputs(List<Observed> observed, boolean brokenLinchpin, boolean lowReportingDensity,
                String evidenceQuality, String coverage, Double forecasterAgreement, boolean skewedUncorroboratedNews) {
            this(observed, brokenLinchpin, lowReportingDensity, evidenceQuality, coverage, forecasterAgreement,
                    skewedUncorroboratedNews, 0);
        }

        /** Inputs without a news-coverage judgment (the 1.0.0 inputs). */
        public Inputs(List<Observed> observed, boolean brokenLinchpin, boolean lowReportingDensity,
                String evidenceQuality, String coverage, Double forecasterAgreement) {
            this(observed, brokenLinchpin, lowReportingDensity, evidenceQuality, coverage, forecasterAgreement, false,
                    0);
        }
    }

    /** The coverage caveat of prompt 133. */
    public static final String SKEWED_COVERAGE = "SKEWED_COVERAGE";

    /** The provenance caveat of prompt 134. */
    public static final String CIRCULAR_REPORTING = "CIRCULAR_REPORTING";

    /**
     * The assessment.
     *
     * @param level {@code low}, {@code moderate} or {@code high}
     * @param basis the {@code confidenceBasis} map
     */
    public record Assessment(String level, Map<String, Object> basis) {}

    /** Assesses confidence. */
    public static Assessment assess(Inputs in) {
        int qualifying = 0;
        Set<String> qualifyingFamilies = new LinkedHashSet<>();
        Set<String> families = new LinkedHashSet<>();
        boolean sourcesOk = true;
        String worst = null;
        for (Observed o : in.observed()) {
            if (o.family() != null) {
                families.add(o.family());
            }
            if (o.sourceReliability() != null && (worst == null || o.sourceReliability().compareTo(worst) > 0)) {
                worst = o.sourceReliability();
            }
            if (!"H".equals(o.credibility()) && !"M".equals(o.credibility())) {
                continue;
            }
            qualifying++;
            if (o.family() != null) {
                qualifyingFamilies.add(o.family());
            }
            if (o.sourceReliability() != null && !"A".equals(o.sourceReliability())
                    && !"B".equals(o.sourceReliability())) {
                sourcesOk = false;
            }
        }
        String level;
        String rule;
        if (qualifying >= 2 && qualifyingFamilies.size() >= 2 && sourcesOk && !in.lowReportingDensity()) {
            level = HypothesisVocabulary.HIGH;
            rule = "high: two or more credible observed indicators from two or more families, sources A/B, adequate"
                    + " reporting density";
        } else if (qualifying >= 1) {
            level = HypothesisVocabulary.MODERATE;
            rule = "moderate: at least one credible observed indicator";
        } else {
            level = HypothesisVocabulary.LOW;
            rule = "low: no credible observed indicator";
        }
        if (in.brokenLinchpin()) {
            String lowered = HypothesisVocabulary.lower(level);
            rule = rule + "; lowered to " + lowered + " by a broken linchpin assumption";
            level = lowered;
        }
        if (in.skewedUncorroboratedNews()) {
            String lowered = HypothesisVocabulary.lower(level);
            rule = rule + "; lowered to " + lowered + " because the supporting news coverage is skewed and nothing"
                    + " outside the news corroborates it";
            level = lowered;
        }
        Map<String, Object> basis = new LinkedHashMap<>();
        basis.put("observedIndicators", in.observed().size());
        basis.put("families", families.size());
        basis.put("evidenceQuality", in.evidenceQuality());
        basis.put("sourceReliability", worst);
        basis.put("coverage", in.coverage());
        basis.put("reportingDensity", in.lowReportingDensity() ? LOW_REPORTING_DENSITY : "ok");
        basis.put("forecasterAgreement", in.forecasterAgreement());
        basis.put("brokenLinchpin", in.brokenLinchpin());
        basis.put("newsCoverage", in.skewedUncorroboratedNews() ? SKEWED_COVERAGE : "ok");
        basis.put("circularReporting", in.circularSupports() > 0 ? CIRCULAR_REPORTING : "ok");
        basis.put("circularSupports", in.circularSupports());
        basis.put("rule", rule);
        basis.put("assessor", "ConfidenceAssessor " + VERSION);
        return new Assessment(level, basis);
    }
}
