package ai.foresignal.forecast.baseline;

import ai.foresignal.baserate.analog.AnalogService;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code analog} forecaster (prompt 122 §4): the 121 analog outcome distribution, for a binary question that links
 * a situation (situation analogs) or names one region (region-period analogs over the last 14 days). The outcome is
 * {@code {family}/P{days}D} from a {@code referenceFamily} selector and the question's window, or {@code critical}
 * for a {@code situation-status} question with {@code minSeverity: critical}. Analog search reads current signatures,
 * so it does not run in retrospective evaluation.
 */
@Component
public class AnalogForecaster implements BaselineForecaster {

    private final AnalogService analogs;
    private final ForecasterRegistry registry;

    /** Creates the forecaster. */
    public AnalogForecaster(AnalogService analogs, ForecasterRegistry registry) {
        this.analogs = analogs;
        this.registry = registry;
    }

    @Override
    public String id() {
        return ForecasterRegistry.ANALOG;
    }

    @Override
    public boolean supports(ForecastQuestionEntity q) {
        return Ledger.BINARY.equals(q.getType());
    }

    /** The analog target outcome of a question, or null. */
    public static String outcome(ForecastQuestionEntity q) {
        Map<String, Object> resolver = q.getResolver();
        if ("situation-status".equals(q.getResolverType()) && "critical".equals(resolver.get("minSeverity"))) {
            return "critical";
        }
        Map<String, Object> selector = JsonValues.child(resolver, "selector");
        String family = JsonValues.text(selector, "referenceFamily");
        if (family == null) {
            return null;
        }
        Instant[] window = ResolverSpec.window(resolver, q.getResolveBy());
        if (window == null) {
            return null;
        }
        long days = Math.max(1, Math.min(365, Math.round(Duration.between(window[0], window[1]).toHours() / 24.0)));
        return family + "/P" + days + "D";
    }

    @Override
    public Outcome forecast(ForecastQuestionEntity q, Instant asOf, boolean retrospective) {
        if (!supports(q)) {
            return Outcome.skip("analogs forecast binary questions only");
        }
        if (retrospective) {
            return Outcome.skip("analog search reads current signatures and cannot be run as of a past time");
        }
        String outcome = outcome(q);
        if (outcome == null) {
            return Outcome.skip("no analog outcome: use a referenceFamily selector or a situation-status question"
                    + " with minSeverity critical");
        }
        ForecasterEntity config = registry.find(id()).orElse(null);
        int k = config == null ? 10 : (int) Math.max(1, Math.min(50, config.configNumber("k", 10)));
        Map<String, Object> result;
        String target;
        if (q.getSituationId() != null) {
            result = analogs.forSituation(q.getSituationId(), k, outcome, true);
            target = "situation " + q.getSituationId();
        } else {
            Map<String, Object> area = JsonValues.child(q.getResolver(), "area");
            List<String> regions = area == null ? List.of() : JsonValues.strings(area, "regionCodes");
            if (regions.size() != 1) {
                return Outcome.skip("analogs need a linked situation or exactly one region");
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("regionCode", regions.get(0));
            body.put("to", asOf.toString());
            body.put("from", asOf.minus(Duration.ofDays(14)).toString());
            body.put("k", k);
            body.put("outcome", outcome);
            result = analogs.forRegion(body);
            target = "region " + regions.get(0);
        }
        Map<String, Object> distribution = JsonValues.child(result, "outcomeDistribution");
        Object p = distribution == null ? null : distribution.get("probability");
        Object count = distribution == null ? null : distribution.get("analogCount");
        if (!(p instanceof Number n) || !(count instanceof Number c) || c.intValue() == 0) {
            return Outcome.skip("no analogs with a known outcome");
        }
        List<String> ids = new ArrayList<>();
        Object list = result.get("analogs");
        if (list instanceof List<?> items) {
            for (Object item : items) {
                if (item instanceof Map<?, ?> m && m.get("situationId") != null) {
                    ids.add(m.get("situationId").toString());
                }
            }
        }
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("analogIds", ids);
        inputs.put("outcome", outcome);
        inputs.put("analogCount", c.intValue());
        inputs.put("baseRate", distribution.get("baseRate"));
        inputs.put("manifestId", result.get("manifestId"));
        double probability = Math.max(0, Math.min(1, n.doubleValue()));
        return Outcome.of(ForecastValue.binary(probability), "Historical analogs of " + target + ": " + outcome
                + " followed in " + distribution.get("hits") + " of " + c.intValue() + " comparable cases, shrunk"
                + " toward the base rate " + distribution.get("baseRate") + ": " + probability + ".", inputs);
    }
}
