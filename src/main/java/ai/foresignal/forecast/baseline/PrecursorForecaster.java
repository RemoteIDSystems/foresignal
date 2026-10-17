package ai.foresignal.forecast.baseline;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code precursor} forecaster (prompt 122 §4): for a binary question opened from a {@code precursor-active}
 * finding, the finding's probability (the precursor's hit rate shrunk toward its base rate, prompt 120). The finding
 * must have existed at the forecast time.
 */
@Component
public class PrecursorForecaster implements BaselineForecaster {

    private final AnalyticFindingRepository findings;

    /** Creates the forecaster. */
    public PrecursorForecaster(AnalyticFindingRepository findings) {
        this.findings = findings;
    }

    @Override
    public String id() {
        return ForecasterRegistry.PRECURSOR;
    }

    @Override
    public boolean supports(ForecastQuestionEntity q) {
        return Ledger.BINARY.equals(q.getType()) && q.getFindingId() != null;
    }

    @Override
    public Outcome forecast(ForecastQuestionEntity q, Instant asOf, boolean retrospective) {
        if (!supports(q)) {
            return Outcome.skip("the question was not opened from a precursor-active finding");
        }
        AnalyticFindingEntity f = findings.findById(q.getFindingId()).orElse(null);
        if (f == null || !"precursor-active".equals(f.getFindingType())) {
            return Outcome.skip("the linked finding is not a precursor-active finding");
        }
        if (f.getCreatedAt().isAfter(asOf)) {
            return Outcome.skip("the finding did not exist at the forecast time");
        }
        Object p = f.getStats().get("probability");
        if (!(p instanceof Number n) || !Double.isFinite(n.doubleValue())) {
            return Outcome.skip("the finding carries no probability");
        }
        double probability = Math.max(0, Math.min(1, n.doubleValue()));
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("findingId", f.getId());
        inputs.put("precursorId", f.getPrecursorId());
        inputs.put("hits", f.getStats().get("hits"));
        inputs.put("n", f.getStats().get("n"));
        inputs.put("baseRate", f.getStats().get("baseRate"));
        inputs.put("shrinkWeight", f.getStats().get("shrinkWeight"));
        return Outcome.of(ForecastValue.binary(probability), "Validated precursor " + f.getPrecursorId() + ": hit rate "
                + f.getStats().get("hits") + " of " + f.getStats().get("n") + " shrunk toward the base rate "
                + f.getStats().get("baseRate") + " gives " + probability + ". An association, not a cause.", inputs);
    }
}
