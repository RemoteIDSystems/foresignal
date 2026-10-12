package ai.foresignal.trigger;

/**
 * One condition operator (prompt 017). The {@link OperatorRegistry} holds the built-ins
 * ({@link Operators}) plus every {@code Operator} bean, so a later prompt (036's {@code inWatchlist})
 * adds one without touching the engine, the SQL builder or the gate.
 *
 * <p>An operator has one meaning in two places, and both must agree exactly
 * ({@code operator-truth-table.json} checks it):
 *
 * <ul>
 *   <li>{@link #sql}: a boolean SQL fragment over a payload path, used by {@link RuleSqlBuilder}. It
 *       may evaluate to SQL {@code NULL} when the value is missing or of another JSON type; the
 *       builder wraps the whole predicate in {@code COALESCE(…, false)}.
 *   <li>{@link #evaluate}: the same test in Java, used by {@link TriggerPersistGate}, which reports
 *       {@link Evaluation#AMBIGUOUS} exactly where the SQL would yield {@code NULL}.
 * </ul>
 *
 * <p>Condition values are JSON-like Java values: {@code BigDecimal} for numbers after
 * {@link #normalize}, {@code String}, {@code Boolean} and {@code List} of those.
 */
public interface Operator {

    /** The operator as written in a condition, for example {@code >=} or {@code in}. */
    String name();

    /**
     * Checks a condition value and returns its normalized form (numbers as {@code BigDecimal}).
     *
     * @param value the value as parsed from JSON
     * @throws IllegalArgumentException with a message for the field error when the value does not
     *     suit the operator
     */
    Object normalize(Object value);

    /**
     * The SQL fragment for one condition over one table alias.
     *
     * @param term the path expressions of the alias, and the way to bind values
     * @param value the normalized value
     */
    String sql(SqlTerm term, Object value);

    /**
     * The Java test.
     *
     * @param actual the payload value at the path (JSON-typed, null when missing or JSON null)
     * @param value the normalized condition value
     */
    Evaluation evaluate(Object actual, Object value);
}
