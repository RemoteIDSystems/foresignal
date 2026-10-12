package ai.foresignal.search;

import ai.foresignal.ingest.JsonValues;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The text a record is embedded as, and its hash (prompt 027).
 *
 * <p>Text = {@code "<kind> | <source name> | "} followed by the values of the descriptor's
 * {@code embedFields}, in declaration order, joined with {@code " | "}. A list contributes its
 * scalar elements and an object its scalar leaves, space-joined; empty values are left out. The
 * result is cut to {@value #MAX_CHARS} characters. A record none of whose embed fields has a value
 * has no text and is not embedded.
 */
public final class EmbeddingText {

    /** The longest text embedded. */
    public static final int MAX_CHARS = 2_000;

    private static final String SEPARATOR = " | ";

    private EmbeddingText() {}

    /**
     * The text of one record version.
     *
     * @param kind the record kind
     * @param sourceName the source's display name
     * @param payload the version's payload
     * @param embedFields the descriptor's {@code embedFields}
     * @return the text, or null when no embed field has a value
     */
    public static String of(String kind, String sourceName, Map<String, ?> payload, List<String> embedFields) {
        if (payload == null || embedFields == null || embedFields.isEmpty()) {
            return null;
        }
        List<String> values = new ArrayList<>();
        for (String field : embedFields) {
            StringBuilder flat = new StringBuilder();
            flatten(JsonValues.at(payload, field), flat);
            String value = flat.toString().strip();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        if (values.isEmpty()) {
            return null;
        }
        String text = kind + SEPARATOR + sourceName + SEPARATOR + String.join(SEPARATOR, values);
        return cut(text);
    }

    /** {@code text} cut to {@value #MAX_CHARS} characters, never splitting a surrogate pair. */
    public static String cut(String text) {
        if (text == null || text.length() <= MAX_CHARS) {
            return text;
        }
        int end = MAX_CHARS;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }

    /** The {@code text_hash} of {@code text}: the first 8 bytes of its SHA-256, as a signed long. */
    public static long hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest, 0, Long.BYTES).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static void flatten(Object value, StringBuilder out) {
        switch (value) {
            case null -> { }
            case Map<?, ?> map -> map.values().forEach(v -> flatten(v, out));
            case Collection<?> list -> list.forEach(v -> flatten(v, out));
            default -> {
                String text = JsonValues.text(value);
                if (text != null && !text.isBlank()) {
                    if (!out.isEmpty()) {
                        out.append(' ');
                    }
                    out.append(text.strip());
                }
            }
        }
    }
}
