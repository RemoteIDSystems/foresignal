package ai.foresignal.chatops;

import ai.foresignal.notify.MarkdownRenderer;
import java.time.Duration;
import java.time.Instant;

/** Small text helpers of chat replies (prompt 061). Every substituted value is escaped as markdown. */
public final class ChatText {

    private ChatText() {}

    /** {@code text} escaped as Foresignal markdown (null as empty). */
    public static String esc(String text) {
        return text == null ? "" : MarkdownRenderer.escapeMarkdown(text);
    }

    /** A markdown link with an escaped label; {@code url} is used as given (Foresignal builds it). */
    public static String link(String label, String url) {
        return "[" + esc(label) + "](" + url.replace(")", "%29").replace(" ", "%20") + ")";
    }

    /** A compact age: {@code 45s}, {@code 12m}, {@code 5h}, {@code 3d}. */
    public static String age(Instant from, Instant now) {
        if (from == null || now == null) {
            return "?";
        }
        long seconds = Math.max(0, Duration.between(from, now).toSeconds());
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            return seconds / 60 + "m";
        }
        if (seconds < 86_400) {
            return seconds / 3600 + "h";
        }
        return seconds / 86_400 + "d";
    }

    /** {@code text} cut to {@code max} characters with an ellipsis, on a code point boundary. */
    public static String cut(String text, int max) {
        if (text == null || text.length() <= max) {
            return text == null ? "" : text;
        }
        int end = max - 1;
        if (end > 0 && Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + "…";
    }
}
