package ai.foresignal.stream.sse;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.error.FieldError;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The composite {@code id:} of {@code /v1/events/stream}: {@code key=cursor} pairs joined with
 * {@code ;}, one per subscribed topic, in a fixed order, e.g. {@code c=1234;a=56;n=7}. Keys are the
 * topics' {@link EventTopic#cursorKey()}s.
 */
public final class CompositeCursor {

    /** The accepted form (the spec's {@code Last-Event-ID} pattern). */
    static final Pattern FORM = Pattern.compile("^[a-z][a-z0-9.-]*=[0-9]+(;[a-z][a-z0-9.-]*=[0-9]+)*$");

    private CompositeCursor() {}

    /**
     * Formats {@code cursors} (key to cursor) in iteration order.
     *
     * @param cursors the cursors, already in the fixed order
     * @return the id text
     */
    public static String format(Map<String, Long> cursors) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Long> entry : cursors.entrySet()) {
            if (!out.isEmpty()) {
                out.append(';');
            }
            out.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return out.toString();
    }

    /**
     * Parses a composite id.
     *
     * @param field the parameter name, for the error ({@code Last-Event-ID} or {@code lastEventId})
     * @param text the id, or null/blank for none
     * @return key to cursor, empty for none
     * @throws ValidationException {@code 400 INVALID_CURSOR} when malformed or a key repeats
     */
    public static Map<String, Long> parse(String field, String text) {
        Map<String, Long> cursors = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return cursors;
        }
        String value = text.strip();
        if (!FORM.matcher(value).matches()) {
            throw invalid(field, "must look like c=1234;a=56");
        }
        for (String pair : value.split(";")) {
            int eq = pair.indexOf('=');
            String key = pair.substring(0, eq);
            long cursor;
            try {
                cursor = Long.parseLong(pair.substring(eq + 1));
            } catch (NumberFormatException e) {
                throw invalid(field, "cursor of '" + key + "' is out of range");
            }
            if (cursors.put(key, cursor) != null) {
                throw invalid(field, "key '" + key + "' appears twice");
            }
        }
        return cursors;
    }

    /**
     * Parses the single-seq id of {@code /v1/changes/stream}.
     *
     * @return the seq, or null for none
     * @throws ValidationException {@code 400 INVALID_CURSOR} when it is not a non-negative integer
     */
    public static Long parseSeq(String field, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            long seq = Long.parseLong(text.strip());
            if (seq < 0) {
                throw invalid(field, "must be a non-negative change seq");
            }
            return seq;
        } catch (NumberFormatException e) {
            throw invalid(field, "must be a change seq");
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(
                ErrorCodes.INVALID_CURSOR, "The resume cursor is not valid.", List.of(new FieldError(field, message)));
    }
}
