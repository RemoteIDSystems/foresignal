package ai.foresignal.inbound.edp.binding;

import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.Ledger;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * When an external forecast item becomes a ledger forecast, and with what value (prompt 138, Intelligence-Design
 * §8.6). Pure.
 *
 * <ul>
 *   <li><strong>Cadence:</strong> a forecast is recorded on the first item for a question, on a material move (a
 *       probability change of at least {@value BindingVocabulary#MIN_PROBABILITY_CHANGE}, or a quantile moving by at
 *       least that share of the previous 5–95 % range), and otherwise at least daily.
 *   <li><strong>Orientation:</strong> {@code inverse} links record {@code 1 − p}; quantile forecasts cannot be
 *       inverted.
 *   <li><strong>Question types:</strong> a probability needs a binary question, quantiles a numeric one.
 * </ul>
 */
public final class ExternalForecastPolicy {

    /** How long a standing external forecast is carried before it is recorded again unchanged. */
    public static final Duration DAILY = Duration.ofDays(1);

    private ExternalForecastPolicy() {}

    /**
     * The value to record for an item on a question, or null when the item cannot be recorded there.
     *
     * @param questionType the ledger question type
     * @param probability the item's probability, or null
     * @param quantiles the item's quantiles, or null
     * @param orientation {@code same} or {@code inverse}
     */
    public static ForecastInputs.ForecastValue value(String questionType, Double probability,
            Map<String, Double> quantiles, String orientation) {
        boolean inverse = BindingVocabulary.INVERSE.equals(orientation);
        if (Ledger.BINARY.equals(questionType) && probability != null) {
            double p = Math.max(0, Math.min(1, probability));
            return ForecastInputs.ForecastValue.binary(inverse ? 1 - p : p);
        }
        if (Ledger.NUMERIC.equals(questionType) && quantiles != null && !quantiles.isEmpty() && !inverse) {
            Map<String, Double> q = new LinkedHashMap<>();
            for (String key : Ledger.QUANTILES) {
                if (quantiles.get(key) != null) {
                    q.put(key, quantiles.get(key));
                }
            }
            return ForecastInputs.ForecastValue.quantiles(q);
        }
        return null;
    }

    /** Why a value could not be built, for the binding's effects. */
    public static String unsupported(String questionType, Double probability, Map<String, Double> quantiles,
            String orientation) {
        if (BindingVocabulary.INVERSE.equals(orientation) && probability == null) {
            return "an inverse link needs a probability; quantile forecasts cannot be inverted";
        }
        return "a " + questionType + " question cannot take " + (probability != null ? "a probability"
                : quantiles != null ? "quantiles" : "this forecast");
    }

    /**
     * True when {@code next} should be recorded after the forecaster's {@code previous} forecast made at
     * {@code previousAt}.
     *
     * @param previous the standing forecast's value, or null when there is none
     * @param previousAt when it was made, or null
     * @param next the candidate value
     * @param now the recording time
     */
    public static boolean shouldRecord(ForecastInputs.ForecastValue previous, Instant previousAt,
            ForecastInputs.ForecastValue next, Instant now) {
        if (previous == null || previousAt == null) {
            return true;
        }
        if (!now.isBefore(previousAt.plus(DAILY))) {
            return true;
        }
        if (next.probability() != null && previous.probability() != null) {
            return Math.abs(next.probability() - previous.probability())
                    >= BindingVocabulary.MIN_PROBABILITY_CHANGE - 1e-12;
        }
        if (next.quantiles() != null && previous.quantiles() != null) {
            Double lo = previous.quantiles().get("p05");
            Double hi = previous.quantiles().get("p95");
            double range = lo == null || hi == null ? 0 : Math.abs(hi - lo);
            double threshold = BindingVocabulary.MIN_PROBABILITY_CHANGE * (range > 0 ? range : 1);
            for (Map.Entry<String, Double> e : next.quantiles().entrySet()) {
                Double before = previous.quantiles().get(e.getKey());
                if (before == null || e.getValue() == null || Math.abs(e.getValue() - before) >= threshold) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
