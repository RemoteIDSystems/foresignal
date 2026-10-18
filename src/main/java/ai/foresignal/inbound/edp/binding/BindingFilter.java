package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.trigger.IdentityOperator;
import ai.foresignal.trigger.Operator;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.trigger.RuleCondition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * A binding's optional filter (prompt 138): property conditions in the 017 condition grammar
 * ({@code [{path, op, value}]}, ANDed, at most {@value #MAX_CONDITIONS}), evaluated against the record payload (an EDP
 * item's {@code properties} are top-level keys of the payload, next to the item type's own fields).
 */
@Component
public class BindingFilter {

    /** Most conditions in one filter. */
    public static final int MAX_CONDITIONS = 20;

    private final OperatorRegistry operators;

    /** Creates the filter compiler. */
    public BindingFilter(OperatorRegistry operators) {
        this.operators = operators;
    }

    /**
     * Checks a filter's shape and operators.
     *
     * @param filter the conditions, or null for none
     * @param field the field name for errors
     * @return the problems found (empty when valid)
     */
    public List<FieldError> validate(List<?> filter, String field) {
        List<FieldError> errors = new ArrayList<>();
        if (filter == null) {
            return errors;
        }
        if (filter.size() > MAX_CONDITIONS) {
            errors.add(new FieldError(field, "holds more than " + MAX_CONDITIONS + " conditions"));
            return errors;
        }
        int i = 0;
        for (Object item : filter) {
            String prefix = field + "[" + i++ + "]";
            if (!(item instanceof Map<?, ?> m)) {
                errors.add(new FieldError(prefix, "must be an object with path, op and value"));
                continue;
            }
            for (Object key : m.keySet()) {
                if (!Set.of("path", "op", "value").contains(String.valueOf(key))) {
                    errors.add(new FieldError(prefix + "." + key, "is not a condition member"));
                }
            }
            Object op = m.get("op");
            if (!(op instanceof String name) || name.isBlank()) {
                errors.add(new FieldError(prefix + ".op", "is required"));
                continue;
            }
            Optional<Operator> operator = operators.find(name);
            if (operator.isEmpty()) {
                errors.add(new FieldError(prefix + ".op", "unknown operator '" + name + "'"));
                continue;
            }
            Object path = m.get("path");
            if (path != null && (!(path instanceof String p) || p.isBlank() || p.length() > 200)) {
                errors.add(new FieldError(prefix + ".path", "must be a dot path of at most 200 characters"));
                continue;
            }
            if (path == null && !(operator.get() instanceof IdentityOperator)) {
                errors.add(new FieldError(prefix + ".path", "is required for operator '" + name + "'"));
                continue;
            }
            try {
                operator.get().normalize(m.get("value"));
            } catch (RuntimeException e) {
                errors.add(new FieldError(prefix + ".value", e.getMessage() == null ? "is not valid" : e.getMessage()));
            }
        }
        return errors;
    }

    /** The conditions in their stored shape ({@code {path, op, value}} maps in order). */
    public static List<Object> normalize(List<?> filter) {
        if (filter == null) {
            return null;
        }
        List<Object> out = new ArrayList<>();
        for (Object item : filter) {
            if (item instanceof Map<?, ?> m) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("path", m.get("path"));
                c.put("op", m.get("op"));
                c.put("value", m.get("value"));
                out.add(c);
            }
        }
        return out;
    }

    /**
     * The predicate of a stored filter; {@link RecordPredicate#of} of no conditions (always true) when there is none.
     *
     * @param filter the stored conditions, or null
     * @param sourceId the provider (for identity operators)
     */
    public RecordPredicate compile(List<?> filter, String sourceId) {
        List<RuleCondition> conditions = new ArrayList<>();
        if (filter != null) {
            for (Object item : filter) {
                if (!(item instanceof Map<?, ?> m) || !(m.get("op") instanceof String name)) {
                    continue;
                }
                Operator op = operators.find(name).orElseThrow(() -> new IllegalStateException(
                        "unknown operator '" + name + "'"));
                Object value = op.normalize(m.get("value"));
                String path = m.get("path") == null ? null : String.valueOf(m.get("path"));
                if (path == null && op instanceof IdentityOperator io) {
                    conditions.add(new RuleCondition(null, op, value, io.identityPaths(value, List.of(sourceId))));
                } else {
                    conditions.add(new RuleCondition(path, op, value));
                }
            }
        }
        return RecordPredicate.of(conditions);
    }

    /** True when {@code item} passes {@code predicate}. */
    public static boolean matches(RecordPredicate predicate, BoundItem item) {
        return predicate.testConditions(item.sourceId(), item.payload());
    }
}
