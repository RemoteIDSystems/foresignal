package ai.foresignal.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Protects the parts of a notification that must never be translated (prompt 141): fenced code blocks and
 * inline code spans (templates put record values there), URLs (bare, autolinks and markdown link targets) and
 * unrendered template placeholders. Each protected span becomes a numbered token {@code ⟦n⟧}; the model is told
 * to copy tokens verbatim, and {@link Masked#restore} puts the originals back.
 *
 * <p>Restoring is strict: every token must come back exactly once and no other token may appear, otherwise the
 * translation is rejected (the caller then sends the original). This also stops a model, or text injected into
 * the content, from inventing or dropping a URL or a code span.
 */
public final class TranslationMask {

    /** The token opener (U+27E6). */
    static final char OPEN = '⟦';

    /** The token closer (U+27E7). */
    static final char CLOSE = '⟧';

    /**
     * The protected spans, leftmost first: fenced code, inline code, a markdown link target {@code ](url)}, an
     * autolink {@code <scheme:…>}, a bare URL, a template placeholder {@code {{…}}}.
     */
    static final Pattern PROTECTED = Pattern.compile(
            "```[\\s\\S]*?```"
                    + "|`[^`\\n]+`"
                    + "|\\]\\([^)\\s]*\\)"
                    + "|<[a-zA-Z][a-zA-Z0-9+.-]*:[^>\\s]*>"
                    + "|\\b(?:https?|mailto|ftp|geo|tel):[^\\s<>()\\[\\]`]+"
                    + "|\\{\\{[^}]*\\}\\}");

    /** Any token-shaped text, including ones the model invented. */
    static final Pattern ANY_TOKEN = Pattern.compile(OPEN + "(\\d+)" + CLOSE);

    private TranslationMask() {}

    /**
     * Masks {@code text}.
     *
     * @param text the text, never null
     * @return the masked text and the protected originals, in token order
     */
    public static Masked mask(String text) {
        Matcher m = PROTECTED.matcher(text);
        StringBuilder out = new StringBuilder(text.length());
        List<String> tokens = new ArrayList<>();
        int last = 0;
        while (m.find()) {
            out.append(stripTokens(text.substring(last, m.start())));
            out.append(OPEN).append(tokens.size()).append(CLOSE);
            tokens.add(m.group());
            last = m.end();
        }
        out.append(stripTokens(text.substring(last)));
        return new Masked(out.toString(), List.copyOf(tokens));
    }

    /** {@code text} with any pre-existing token brackets replaced, so content cannot forge a token. */
    private static String stripTokens(String text) {
        return text.replace(OPEN, '[').replace(CLOSE, ']');
    }

    /**
     * A masked text.
     *
     * @param text the text with tokens in place of the protected spans
     * @param tokens the protected originals; token {@code n} is {@code tokens.get(n)}
     */
    public record Masked(String text, List<String> tokens) {

        /** Copies the tokens. */
        public Masked {
            tokens = List.copyOf(tokens);
        }

        /**
         * The translation with the protected originals put back, or null when the translation lost, repeated or
         * invented a token.
         */
        public String restore(String translated) {
            if (translated == null) {
                return null;
            }
            int[] seen = new int[tokens.size()];
            Matcher m = ANY_TOKEN.matcher(translated);
            StringBuilder out = new StringBuilder(translated.length() + 64);
            int last = 0;
            while (m.find()) {
                int index;
                try {
                    index = Integer.parseInt(m.group(1));
                } catch (NumberFormatException e) {
                    return null;
                }
                if (index < 0 || index >= tokens.size() || seen[index]++ > 0) {
                    return null;
                }
                out.append(translated, last, m.start()).append(tokens.get(index));
                last = m.end();
            }
            for (int count : seen) {
                if (count != 1) {
                    return null;
                }
            }
            out.append(translated.substring(last));
            return out.toString();
        }
    }
}
