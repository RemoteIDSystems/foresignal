package ai.foresignal.social.correlation;

import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The 122 ledger questions of comparisons (prompt 137): for each organically found comparison opened as
 * {@code pending}, a binary question "Independent confirmation of {claim type} at {place} within {window}?" with a
 * {@code manual} resolver that the comparison itself resolves when it becomes final ({@code corroborated} → yes;
 * {@code unobserved} or {@code contradicted} → no; {@code insufficient-coverage} → annulled {@code COVERAGE_GAP}). The
 * {@code social-claim} forecaster ({@link SocialClaimForecaster}) records its forecast when the question opens, so
 * claim reliability is scored like any other forecaster. Cue-found claims open no question: they were found by
 * looking for the event that would confirm them.
 */
@Component
public class ComparisonQuestions {

    private static final Logger log = LoggerFactory.getLogger(ComparisonQuestions.class);

    private static final DateTimeFormatter TITLE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC);

    /** The decided comparisons read for the base rate. */
    static final int HISTORY = 20_000;

    private final ObjectProvider<ForecastService> ledger;
    private final ObjectProvider<ForecasterRegistry> forecasters;
    private final ComparisonStore store;

    /** Creates the component. */
    public ComparisonQuestions(ObjectProvider<ForecastService> ledger, ObjectProvider<ForecasterRegistry> forecasters,
            ComparisonStore store) {
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.store = store;
    }

    /** The forecaster {@code social-claim}, registered on first use. */
    public static ForecasterEntity ensureForecaster(ForecasterRegistry registry) {
        return registry.ensure(CorrelationVocabulary.FORECASTER, "base-rate", "Social claim reliability",
                CorrelationVocabulary.FORECASTER_VERSION, Map.of("component", CorrelationVocabulary.FORECASTER + " "
                        + CorrelationVocabulary.FORECASTER_VERSION, "prior", "Jeffreys Beta(0.5, 0.5)", "minCell",
                        SocialClaimForecaster.MIN_CELL));
    }

    /**
     * The question draft of a comparison. Pure.
     *
     * @param c the comparison
     * @param now the decision time
     */
    public static ForecastInputs.QuestionDraft draft(ComparisonStore.Comparison c, Instant now) {
        String place = c.place() != null && c.place().get("name") != null ? c.place().get("name").toString()
                : c.regionCode() == null ? "the reported place" : c.regionCode();
        String title = "Independent confirmation of " + c.claimType() + " at " + place + " within "
                + TITLE_TIME.format(c.windowStart()) + " to " + TITLE_TIME.format(c.windowEnd()) + "?";
        if (title.length() > 300) {
            title = title.substring(0, 297) + "...";
        }
        Instant closes = now.plus(Duration.ofHours(1));
        if (c.windowEnd().isAfter(now.plus(Duration.ofMinutes(1))) && c.windowEnd().isBefore(closes)) {
            closes = c.windowEnd();
        }
        String criteria = "Resolves yes when Foresignal's social–physical comparison " + c.id() + " of claim "
                + c.claimId() + " becomes corroborated: at least one non-social evidence group (" + c.signalFamily()
                + ") matches the place within the family tolerance between " + c.windowStart() + " and " + c.windowEnd()
                + " (UTC). Resolves no when it becomes unobserved (no match although coverage could have seen it) or"
                + " contradicted. Annulled when coverage was insufficient. Social posts never count as confirmation.";
        if (criteria.length() > ForecastInputs.MAX_CRITERIA) {
            criteria = criteria.substring(0, ForecastInputs.MAX_CRITERIA);
        }
        Map<String, Object> resolver = ResolverSpec.validate(new LinkedHashMap<>(Map.of("type", "manual")),
                Ledger.BINARY);
        List<String> tags = new ArrayList<>(List.of("social", c.claimType(), c.signalFamily()));
        return new ForecastInputs.QuestionDraft(title, Ledger.BINARY, Map.of(), criteria, resolver, now, closes,
                c.finalAt().plus(Duration.ofDays(1)), CorrelationVocabulary.QUESTION_CATEGORY, tags, Map.of(),
                "social-comparison:" + c.id(), false);
    }

    /**
     * Opens the question of a new pending comparison and records the {@code social-claim} forecast; null when the
     * ledger is unavailable or the comparison is not eligible.
     */
    public String open(ComparisonStore.Comparison c, Instant now) {
        ForecastService service = ledger.getIfAvailable();
        ForecasterRegistry registry = forecasters.getIfAvailable();
        if (service == null || registry == null || !CorrelationVocabulary.PENDING.equals(c.status())
                || !CorrelationVocabulary.ORGANIC.equals(c.foundBy())) {
            return null;
        }
        try {
            ForecastService.Opened opened = service.openQuestion(draft(c, now), "system",
                    CorrelationVocabulary.COMPARATOR);
            ForecastQuestionEntity q = opened.question();
            if (opened.created() && q.acceptsForecasts(now)) {
                SocialClaimForecaster.Estimate e = SocialClaimForecaster.estimate(history(), c.claimType(),
                        c.platforms(), c.regionCode(), c.evidenceGroupCount());
                Map<String, Object> inputs = new LinkedHashMap<>(e.toMap());
                inputs.put("comparisonId", c.id());
                inputs.put("claimType", c.claimType());
                inputs.put("platforms", SocialClaimForecaster.mix(c.platforms()));
                inputs.put("regionCode", c.regionCode());
                inputs.put("evidenceGroupBand", SocialClaimForecaster.band(c.evidenceGroupCount()));
                service.record(q, ensureForecaster(registry), ForecastInputs.ForecastValue.binary(e.probability()),
                        String.format(java.util.Locale.ROOT, "Base rate of independent confirmation: %d of %d decided"
                                + " comparisons (%s), Jeffreys posterior mean %.2f", e.corroborated(), e.n(), e.level(),
                                e.probability()), List.of(Map.of("ref", "claim:" + c.claimId())), null, inputs, null,
                        false);
            }
            return q.getId();
        } catch (RuntimeException ex) {
            log.warn("The ledger question of comparison {} failed: {}", c.id(), ex.toString());
            return null;
        }
    }

    /**
     * Resolves the question of a final comparison: yes, no, or annulled.
     *
     * @return the outcome ({@code yes}, {@code no}, {@code annulled}) or null when nothing was done
     */
    public String resolve(ComparisonStore.Comparison c, List<String> evidenceRefs) {
        ForecastService service = ledger.getIfAvailable();
        if (service == null || c.questionId() == null || !CorrelationVocabulary.isFinal(c.status())) {
            return null;
        }
        try {
            ForecastQuestionEntity q = service.find(c.questionId()).orElse(null);
            if (q == null || q.finished()) {
                return null;
            }
            String outcome = outcome(c.status());
            if (outcome == null) {
                service.annul(q, "COVERAGE_GAP", "Coverage could not have seen the claimed event (comparison "
                        + c.id() + ")", CorrelationVocabulary.COMPARATOR);
                return "annulled";
            }
            List<String> evidence = new ArrayList<>(evidenceRefs == null ? List.of() : evidenceRefs);
            evidence.add("social-comparison:" + c.id());
            service.resolveManually(q.getId(), new ForecastInputs.ResolutionSubmission(outcome, null, evidence,
                    "Comparison " + c.id() + " became " + c.status() + "."), CorrelationVocabulary.COMPARATOR, true);
            return outcome;
        } catch (RuntimeException ex) {
            log.warn("The ledger question {} of comparison {} could not be resolved: {}", c.questionId(), c.id(),
                    ex.toString());
            return null;
        }
    }

    /** The question outcome of a final status: yes, no, or null (annulled). */
    public static String outcome(String status) {
        return switch (status) {
            case CorrelationVocabulary.CORROBORATED -> "yes";
            case CorrelationVocabulary.UNOBSERVED, CorrelationVocabulary.CONTRADICTED -> "no";
            default -> null;
        };
    }

    /** The decided organic comparisons as forecaster history. */
    List<SocialClaimForecaster.Outcome> history() {
        List<SocialClaimForecaster.Outcome> out = new ArrayList<>();
        for (ComparisonStore.Comparison c : store.finalSince(Instant.EPOCH, HISTORY)) {
            String o = outcome(c.status());
            if (o != null && CorrelationVocabulary.ORGANIC.equals(c.foundBy())) {
                out.add(new SocialClaimForecaster.Outcome(c.claimType(), c.platforms(), c.regionCode(),
                        c.evidenceGroupCount(), "yes".equals(o)));
            }
        }
        return out;
    }
}
