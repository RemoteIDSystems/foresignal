package ai.foresignal.forecast.baseline;

import ai.foresignal.baserate.BaseRateDefinition;
import ai.foresignal.baserate.BaseRateEntity;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.precursor.Candidate;
import ai.foresignal.precursor.PrecursorEntity;
import ai.foresignal.precursor.PrecursorRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.statistics.distribution.PoissonDistribution;
import org.springframework.stereotype.Component;

/**
 * The {@code base-rate} forecaster (prompt 122 §4): the outside view from the 121 base-rate library.
 *
 * <ul>
 *   <li><b>Reference class:</b> the family of the resolver's {@code referenceFamily} selector, else the linked
 *       precursor's consequent family, else a {@code family:{name}} tag; the regions of {@code area.regionCodes} (or
 *       the selector's), worldwide when none; conditional on the linked precursor's antecedent family within its
 *       longest lag when one is linked.</li>
 *   <li><b>Window:</b> the question's window of {@code D} days uses the base rate of the nearest library window
 *       ({@code P7D} up to 10 days, {@code P28D} up to 50, else {@code P90D}), scaled to {@code D} days assuming
 *       independent windows: {@code p = 1 − (1 − p₁)^(D / w)} for {@code event-exists}, and a Poisson count with mean
 *       {@code ratePerWindow · D / w} for {@code count-threshold}.</li>
 *   <li>It forecasts nothing, and says why, for multiple, ordered and numeric questions, for resolvers that are not
 *       event counts, for geometric areas and when no history covers the reference class.</li>
 * </ul>
 * Live forecasts read the stored (or 24-hour cached) rate; retrospective ones compute it from the weeks before the
 * question opened ({@link BaseRateService#computeAsOf}).
 */
@Component
public class BaseRateForecaster implements BaselineForecaster {

    private final BaseRateService baseRates;
    private final PrecursorRepository precursors;

    /** Creates the forecaster. */
    public BaseRateForecaster(BaseRateService baseRates, PrecursorRepository precursors) {
        this.baseRates = baseRates;
        this.precursors = precursors;
    }

    @Override
    public String id() {
        return ForecasterRegistry.BASE_RATE;
    }

    @Override
    public boolean supports(ForecastQuestionEntity q) {
        return Ledger.BINARY.equals(q.getType()) || Ledger.MULTIPLE.equals(q.getType());
    }

    /**
     * The library definition of a question, or a skip reason.
     *
     * @param definition the definition map, or null
     * @param windowDays the question's window in days
     * @param libraryDays the library window's length in days
     * @param skip why there is none, or null
     */
    public record Plan(Map<String, Object> definition, double windowDays, int libraryDays, String skip) {}

    /** Plans the reference class of a question (pure). */
    public static Plan plan(ForecastQuestionEntity q, PrecursorEntity precursor) {
        if (!Ledger.BINARY.equals(q.getType())) {
            return new Plan(null, 0, 0, "no base rate maps onto the outcomes of a " + q.getType() + " question");
        }
        String type = q.getResolverType();
        Map<String, Object> resolver = q.getResolver();
        if (!"event-exists".equals(type) && !("count-threshold".equals(type) && resolver.get("selector") != null)) {
            return new Plan(null, 0, 0, "the " + type + " resolver is not an event count, so no base rate applies");
        }
        Map<String, Object> selector = JsonValues.child(resolver, "selector");
        String family = JsonValues.text(selector, "referenceFamily");
        if (family == null && precursor != null) {
            family = JsonValues.text(Candidate.selector(precursor.getConsequent()), "referenceFamily");
        }
        if (family == null) {
            for (String tag : q.getTags()) {
                if (tag.startsWith("family:") && tag.length() > 7) {
                    family = tag.substring(7);
                    break;
                }
            }
        }
        if (family == null) {
            return new Plan(null, 0, 0, "no reference family: use a referenceFamily selector, link a precursor with"
                    + " a family, or tag the question family:{name}");
        }
        Map<String, Object> area = JsonValues.child(resolver, "area");
        List<String> regions = new ArrayList<>();
        if (area != null) {
            if (area.get("regionCodes") == null) {
                return new Plan(null, 0, 0, "the question's area is not region-based, so no base rate applies");
            }
            regions.addAll(JsonValues.strings(area, "regionCodes"));
        } else {
            regions.addAll(JsonValues.strings(selector, "regionCodes"));
        }
        Instant[] window = ResolverSpec.window(resolver, q.getResolveBy());
        double days = Math.max(1.0, Duration.between(window[0], window[1]).toMinutes() / 1440.0);
        String libraryWindow = days <= 10 ? "P7D" : days <= 50 ? "P28D" : "P90D";
        int libraryDays = days <= 10 ? 7 : days <= 50 ? 28 : 91;
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("family", family);
        if (!regions.isEmpty()) {
            definition.put("regionCodes", regions);
        }
        definition.put("window", libraryWindow);
        if (precursor != null) {
            String antecedent = JsonValues.text(Candidate.selector(precursor.getAntecedent()), "referenceFamily");
            if (antecedent != null) {
                long within = Math.max(1, Math.min(365, (long) Math.ceil(precursor.getLagMaxSeconds() / 86_400.0)));
                definition.put("condition", Map.of("family", antecedent, "withinDays", within));
            }
        }
        return new Plan(definition, days, libraryDays, null);
    }

    /** {@code 1 − (1 − p)^(days / libraryDays)}. */
    public static double scale(double pAtLeastOne, double days, double libraryDays) {
        double p = Math.max(0, Math.min(1, pAtLeastOne));
        return 1 - Math.pow(1 - p, days / libraryDays);
    }

    /** {@code P(N op threshold)} for a Poisson count with mean {@code mean}. */
    public static double poisson(double mean, String op, double threshold) {
        if (mean <= 0) {
            return ResolverSpec.compare(0, op, threshold) ? 1.0 : 0.0;
        }
        PoissonDistribution d = PoissonDistribution.of(mean);
        int k = (int) Math.floor(threshold);
        boolean integral = threshold == Math.floor(threshold);
        return switch (op) {
            case "gte" -> integral ? d.survivalProbability(k - 1) : d.survivalProbability(k);
            case "gt" -> d.survivalProbability(k);
            case "lt" -> integral ? d.cumulativeProbability(k - 1) : d.cumulativeProbability(k);
            case "lte" -> d.cumulativeProbability(k);
            case "eq" -> integral ? d.probability(k) : 0.0;
            default -> throw new IllegalArgumentException("unknown op " + op);
        };
    }

    @Override
    public Outcome forecast(ForecastQuestionEntity q, Instant asOf, boolean retrospective) {
        PrecursorEntity precursor = q.getPrecursorId() == null ? null
                : precursors.findById(q.getPrecursorId()).orElse(null);
        Plan plan = plan(q, precursor);
        if (plan.skip() != null) {
            return Outcome.skip(plan.skip());
        }
        double pAtLeastOne;
        double rate;
        int nWindows;
        String referenceClass;
        List<String> ids = new ArrayList<>();
        String key;
        Map<String, Object> interval = new LinkedHashMap<>();
        if (retrospective) {
            BaseRateService.Computed c = baseRates.computeAsOf(BaseRateDefinition.parse(plan.definition()), asOf);
            pAtLeastOne = c.result().pAtLeastOne();
            rate = c.result().ratePerWindow();
            nWindows = c.result().nWindows();
            referenceClass = c.definition().referenceClass();
            key = c.key();
            interval.put("lower", c.result().lower());
            interval.put("upper", c.result().upper());
        } else {
            BaseRateService.QueryResult r = baseRates.query(plan.definition());
            if (r.stored() != null) {
                BaseRateEntity e = r.stored();
                pAtLeastOne = e.getPAtLeastOne();
                rate = e.getRatePerWindow();
                nWindows = e.getNWindows();
                referenceClass = BaseRateDefinition.parse(e.getDefinition()).referenceClass();
                key = e.getDefinitionKey();
                ids.add(e.getId());
                interval.put("lower", e.getIntervalLower());
                interval.put("upper", e.getIntervalUpper());
            } else {
                BaseRateService.Computed c = r.computed();
                pAtLeastOne = c.result().pAtLeastOne();
                rate = c.result().ratePerWindow();
                nWindows = c.result().nWindows();
                referenceClass = c.definition().referenceClass();
                key = c.key();
                interval.put("lower", c.result().lower());
                interval.put("upper", c.result().upper());
            }
        }
        if (nWindows == 0) {
            return Outcome.skip("no covered history for the reference class " + referenceClass);
        }
        double p;
        String how;
        if ("count-threshold".equals(q.getResolverType())) {
            String op = q.getResolver().get("op").toString();
            double threshold = ((Number) q.getResolver().get("threshold")).doubleValue();
            double mean = rate * plan.windowDays() / plan.libraryDays();
            p = poisson(mean, op, threshold);
            how = "a Poisson count with mean " + round(mean) + " over " + round(plan.windowDays()) + " days, P(count "
                    + op + " " + threshold + ")";
        } else {
            p = scale(pAtLeastOne, plan.windowDays(), plan.libraryDays());
            how = "P(at least one) " + round(pAtLeastOne) + " per " + plan.libraryDays() + " days scaled to "
                    + round(plan.windowDays()) + " days";
        }
        p = Math.max(0, Math.min(1, p));
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("baseRateIds", ids);
        inputs.put("definitionKey", key);
        inputs.put("definition", plan.definition());
        inputs.put("pAtLeastOne", pAtLeastOne);
        inputs.put("ratePerWindow", rate);
        inputs.put("nWindows", nWindows);
        inputs.put("interval", interval);
        inputs.put("windowDays", plan.windowDays());
        inputs.put("asOf", asOf.toString());
        String rationale = "Outside view: " + referenceClass + " (" + nWindows + " windows). " + how + " gives "
                + round(p) + ".";
        return Outcome.of(ForecastValue.binary(p), rationale, inputs);
    }

    private static double round(double v) {
        return Math.round(v * 10_000) / 10_000.0;
    }
}
