package ai.foresignal.trigger;

import ai.foresignal.ingest.JsonValues;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One compiled condition: a payload path, an operator and its normalized value.
 *
 * <p>Prompt 036 adds <em>identity conditions</em>: an {@code inWatchlist}/{@code notInWatchlist}
 * condition without a path reads, per source, the payload field the source's descriptor declares
 * for the watchlist's identity ({@code entityKeys}). Such a condition has a null {@link #path} and
 * a non-null {@link #identityPaths}; a source missing from that map never matches.
 *
 * @param path the dot path into the payload, or null for an identity condition
 * @param operator the operator
 * @param value the normalized value ({@link Operator#normalize})
 * @param identityPaths source id to dot path, for an identity condition; null otherwise
 */
public record RuleCondition(String path, Operator operator, Object value, Map<String, String> identityPaths) {

    /** Requires the operator, and a path unless identity paths are given. */
    public RuleCondition {
        Objects.requireNonNull(operator, "operator");
        identityPaths = identityPaths == null ? null : Map.copyOf(identityPaths);
        if (path == null && identityPaths == null) {
            throw new NullPointerException("path");
        }
    }

    /** A condition on one payload path. */
    public RuleCondition(String path, Operator operator, Object value) {
        this(path, operator, value, null);
    }

    /** True for an identity condition (no fixed path; the path depends on the source). */
    public boolean identity() {
        return path == null;
    }

    /** The path this condition reads for a record of {@code sourceId}, or null when it reads none. */
    public String pathFor(String sourceId) {
        return path != null ? path : identityPaths.get(sourceId);
    }

    /** The path split into its steps, as bound for {@code #>}; empty for an identity condition. */
    public List<String> steps() {
        return path == null ? List.of() : steps(path);
    }

    /** {@code dotPath} split into its steps. */
    public static List<String> steps(String dotPath) {
        return List.of(dotPath.split("\\."));
    }

    /** The first step, which {@code volatileFields} are compared against; null for an identity condition. */
    public String root() {
        if (path == null) {
            return null;
        }
        int dot = path.indexOf('.');
        return dot < 0 ? path : path.substring(0, dot);
    }

    /** Evaluates the condition against a whole payload (a fixed-path condition). */
    public Evaluation evaluate(Map<String, ?> payload) {
        return operator.evaluate(path == null ? null : JsonValues.at(payload, path), value);
    }

    /** Evaluates the condition against the payload of a record of {@code sourceId}. */
    public Evaluation evaluate(String sourceId, Map<String, ?> payload) {
        String p = pathFor(sourceId);
        return operator.evaluate(p == null || payload == null ? null : JsonValues.at(payload, p), value);
    }

    /** Evaluates the condition against the value already read at {@link #path}. */
    public Evaluation evaluateValue(Object actual) {
        return operator.evaluate(actual, value);
    }
}
