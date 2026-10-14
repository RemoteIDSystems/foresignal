package ai.foresignal.source.adapters.cdc;

import java.util.Locale;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/** Small JSON-coercion helpers shared by the CDC NWSS parsers (prompt 095). */
final class CdcText {

    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");

    private CdcText() {}

    /** A trimmed, control-free string, or null when absent or blank. */
    static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String raw = node.asString();
        String clean = CONTROL.matcher(raw).replaceAll("").strip();
        return clean.isEmpty() ? null : clean;
    }

    /** A finite number, or null. */
    static Double number(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            double d = node.asDouble();
            return Double.isFinite(d) ? d : null;
        }
        String value = text(node);
        if (value == null) {
            return null;
        }
        try {
            double d = Double.parseDouble(value);
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** A long, or null. */
    static Long integral(JsonNode node) {
        Double value = number(node);
        return value == null ? null : Math.round(value);
    }

    /** A boolean, accepting true/false, yes/no and 1/0. */
    static Boolean bool(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        String value = text(node);
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.equals("true") || lower.equals("yes") || lower.equals("y") || lower.equals("1")) {
            return Boolean.TRUE;
        }
        if (lower.equals("false") || lower.equals("no") || lower.equals("n") || lower.equals("0")) {
            return Boolean.FALSE;
        }
        return null;
    }
}
