package ai.foresignal.mapping;

import ai.foresignal.api.error.FieldError;
import java.util.List;

/**
 * The prompts of {@code suggestMapping} (prompt 059). The system prompt is stable (no per-request values), so a
 * provider's prompt cache can reuse it; the sample goes in the user turn between {@value #OPEN} and
 * {@value #CLOSE} and is declared untrusted.
 */
public final class MappingSuggestPrompts {

    /** Opens the untrusted sample. */
    public static final String OPEN = "<sample>";

    /** Closes the untrusted sample. */
    public static final String CLOSE = "</sample>";

    /** The longest sample text sent. */
    public static final int MAX_SAMPLE_CHARS = 16_000;

    /** The system prompt, from its versioned template under {@code llm/templates/} (prompt 116). */
    public static final String SYSTEM = ai.foresignal.llm.PromptTemplates.text(
            ai.foresignal.llm.PromptTemplates.MAPPING_SUGGEST, ai.foresignal.llm.PromptTemplates.V1);

    private MappingSuggestPrompts() {}

    /**
     * The first user turn.
     *
     * @param hints the operator's hints and the target source's facts, as plain lines
     * @param sample the sample document as JSON text
     */
    public static String user(List<String> hints, String sample) {
        StringBuilder out = new StringBuilder();
        if (!hints.isEmpty()) {
            out.append("Hints from the operator and the target source:\n");
            hints.forEach(h -> out.append("- ").append(h).append('\n'));
            out.append('\n');
        }
        String text = sample.length() > MAX_SAMPLE_CHARS ? sample.substring(0, MAX_SAMPLE_CHARS) + "…" : sample;
        // The delimiters cannot be closed early from inside the sample.
        text = text.replace(CLOSE, "</ sample>").replace(OPEN, "< sample>");
        out.append("Draft a RecordMapping for this payload.\n").append(OPEN).append('\n').append(text).append('\n')
                .append(CLOSE);
        return out.toString();
    }

    /** A repair turn listing what failed. */
    public static String repair(List<FieldError> errors) {
        StringBuilder out = new StringBuilder("The draft did not validate:\n");
        errors.stream().limit(40).forEach(e -> out.append("- ").append(e.field()).append(": ").append(e.message())
                .append('\n'));
        out.append("Return the corrected JSON object only.");
        return out.toString();
    }
}
