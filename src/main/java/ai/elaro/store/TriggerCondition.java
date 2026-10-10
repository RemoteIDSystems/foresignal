package ai.elaro.store;

/**
 * One condition in a rule, stored inside the {@code conditions} jsonb array.
 *
 * @param path path into the record payload, for example {@code properties.mag}
 * @param op comparison operator, for example {@code gt} or {@code eq}
 * @param value the value to compare against; type depends on the operator
 */
public record TriggerCondition(String path, String op, Object value) {}
