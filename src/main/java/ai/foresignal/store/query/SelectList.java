package ai.foresignal.store.query;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Assembles the select list of a read query over {@link RecordRowMapper#LABELS}: labels given an
 * expression are selected from it, every other label as a typed {@code NULL}.
 */
final class SelectList {

    private final Map<String, String> expressions = new LinkedHashMap<>();

    /** Selects {@code label} from {@code expression}. */
    SelectList put(String label, String expression) {
        if (!RecordRowMapper.LABELS.containsKey(label)) {
            throw new IllegalArgumentException("Unknown label " + label);
        }
        expressions.put(label, expression);
        return this;
    }

    /** The SQL select list. */
    String sql() {
        StringJoiner out = new StringJoiner(", ");
        for (Map.Entry<String, String> label : RecordRowMapper.LABELS.entrySet()) {
            String expression = expressions.get(label.getKey());
            out.add((expression == null ? "NULL::" + label.getValue() : expression) + " AS " + label.getKey());
        }
        return out.toString();
    }
}
