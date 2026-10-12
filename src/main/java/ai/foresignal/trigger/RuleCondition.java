package ai.foresignal.trigger;

import ai.foresignal.ingest.JsonValues;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One compiled condition: a payload path, an operator and its normalized value.
 *
 * @param path the dot path into the payload
 * @param operator the operator
 * @param value the normalized value ({@link Operator#normalize})
 */
public record RuleCondition(String path, Operator operator, Object value) {

    /** Requires every part. */
    public RuleCondition {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(operator, "operator");
    }

    /** The path split into its steps, as bound for {@code #>}. */
    public List<String> steps() {
        return List.of(path.split("\\."));
    }

    /** The first step, which {@code volatileFields} are compared against. */
    public String root() {
        int dot = path.indexOf('.');
        return dot < 0 ? path : path.substring(0, dot);
    }

    /** Evaluates the condition against a whole payload. */
    public Evaluation evaluate(Map<String, ?> payload) {
        return operator.evaluate(JsonValues.at(payload, path), value);
    }

    /** Evaluates the condition against the value already read at {@link #path}. */
    public Evaluation evaluateValue(Object actual) {
        return operator.evaluate(actual, value);
    }
}
