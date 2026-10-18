package ai.foresignal.briefing;

import ai.foresignal.notify.MarkdownRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The plain-text rendition of a briefing as it is spoken (prompt 141), and its split into speech requests.
 *
 * <ul>
 *   <li>{@link #of}: the title, then the markdown rendered as plain text ({@link MarkdownRenderer.Format#PLAIN})
 *       with link targets and bare URLs dropped (a URL read aloud is noise), table rows read as comma-separated
 *       cells and separator rows removed, and runs of blank lines collapsed.
 *   <li>{@link #chunks}: pieces of at most {@code max} characters, cut at a paragraph break, else a sentence end,
 *       else a space, else hard; for MP3, whose frames concatenate.
 *   <li>{@link #truncate}: the text cut to {@code max} characters at the same kind of boundary, with
 *       {@link #TRUNCATION_NOTE} appended; for Opus in Ogg, whose streams do not concatenate.
 * </ul>
 */
public final class SpeechText {

    /** The note spoken at the end of a truncated rendition. */
    public static final String TRUNCATION_NOTE = "The audio ends here; the full briefing is in the text.";

    private static final Pattern LINK = Pattern.compile("\\[([^\\]]*)]\\(([^)\\s]*)\\)");

    private static final Pattern URL = Pattern.compile("\\(?\\b(?:https?|mailto):[^\\s)]+\\)?");

    private static final Pattern TABLE_SEPARATOR = Pattern.compile("^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?\\s*$");

    private static final Pattern BLANK_LINES = Pattern.compile("\\n{3,}");

    private SpeechText() {}

    /**
     * The spoken rendition of a run.
     *
     * @param title the title read first, or null
     * @param markdown the run's markdown
     */
    public static String of(String title, String markdown) {
        String md = markdown == null ? "" : markdown;
        md = LINK.matcher(md).replaceAll(m -> java.util.regex.Matcher.quoteReplacement(m.group(1)));
        StringBuilder lines = new StringBuilder();
        for (String line : md.split("\\R", -1)) {
            if (TABLE_SEPARATOR.matcher(line).matches()) {
                continue;
            }
            String stripped = line.strip();
            if (stripped.startsWith("|") && stripped.endsWith("|") && stripped.length() > 1) {
                List<String> cells = new ArrayList<>();
                for (String cell : stripped.substring(1, stripped.length() - 1).split("\\|")) {
                    if (!cell.isBlank()) {
                        cells.add(cell.strip());
                    }
                }
                line = String.join(", ", cells) + ".";
            }
            lines.append(line).append('\n');
        }
        String plain = MarkdownRenderer.render(lines.toString(), MarkdownRenderer.Format.PLAIN);
        plain = URL.matcher(plain).replaceAll("");
        plain = plain.replace("\r", "").replaceAll("[ \\t]+\\n", "\n");
        plain = BLANK_LINES.matcher(plain).replaceAll("\n\n").strip();
        if (title == null || title.isBlank()) {
            return plain;
        }
        return plain.isEmpty() ? title.strip() + "." : title.strip() + ".\n\n" + plain;
    }

    /**
     * {@code text} in pieces of at most {@code max} characters, each cut at the best boundary.
     *
     * @throws IllegalArgumentException when {@code max} is under 100
     */
    public static List<String> chunks(String text, int max) {
        if (max < 100) {
            throw new IllegalArgumentException("max must be at least 100");
        }
        List<String> out = new ArrayList<>();
        String rest = text == null ? "" : text.strip();
        while (rest.length() > max) {
            int cut = boundary(rest, max);
            String piece = rest.substring(0, cut).strip();
            if (!piece.isEmpty()) {
                out.add(piece);
            }
            rest = rest.substring(cut).strip();
        }
        if (!rest.isEmpty()) {
            out.add(rest);
        }
        return out;
    }

    /**
     * {@code text} cut to at most {@code max} characters at the best boundary, with {@link #TRUNCATION_NOTE}
     * appended (the note counts toward {@code max}); {@code text} itself when it fits.
     *
     * @throws IllegalArgumentException when {@code max} cannot hold the note and some text
     */
    public static String truncate(String text, int max) {
        String t = text == null ? "" : text.strip();
        if (t.length() <= max) {
            return t;
        }
        int room = max - TRUNCATION_NOTE.length() - 2;
        if (room < 50) {
            throw new IllegalArgumentException("max is too small for the truncation note");
        }
        String head = t.substring(0, boundary(t, room)).strip();
        return head + "\n\n" + TRUNCATION_NOTE;
    }

    /**
     * {@code text}, cut at the best boundary if needed, always followed by {@link #TRUNCATION_NOTE}, within
     * {@code max} characters (the last request of a capped MP3 rendition).
     *
     * @throws IllegalArgumentException when {@code max} cannot hold the note and some text
     */
    public static String withNote(String text, int max) {
        String t = text == null ? "" : text.strip();
        int room = max - TRUNCATION_NOTE.length() - 2;
        if (room < 50) {
            throw new IllegalArgumentException("max is too small for the truncation note");
        }
        String head = t.length() <= room ? t : t.substring(0, boundary(t, room)).strip();
        return head + "\n\n" + TRUNCATION_NOTE;
    }

    /** True when {@code text} needs more than one request of {@code max} characters. */
    public static boolean exceeds(String text, int max) {
        return text != null && text.strip().length() > max;
    }

    /**
     * Where to cut {@code text} so the first piece has at most {@code max} characters: after the last paragraph
     * break in the second half of the window, else after the last sentence end, else at the last space, else at
     * {@code max} (never inside a surrogate pair).
     */
    static int boundary(String text, int max) {
        int floor = max / 2;
        int paragraph = text.lastIndexOf("\n\n", max - 1);
        if (paragraph >= floor) {
            return paragraph + 2;
        }
        int sentence = -1;
        for (int i = Math.min(max, text.length()) - 1; i >= floor; i--) {
            char c = text.charAt(i);
            if ((c == '.' || c == '!' || c == '?' || c == '\n') && (i + 1 >= text.length()
                    || Character.isWhitespace(text.charAt(i + 1)))) {
                sentence = i + 1;
                break;
            }
        }
        if (sentence > 0) {
            return sentence;
        }
        int space = text.lastIndexOf(' ', max - 1);
        if (space >= floor) {
            return space + 1;
        }
        int cut = Math.min(max, text.length());
        if (cut < text.length() && Character.isLowSurrogate(text.charAt(cut)) && cut > 0) {
            cut--;
        }
        return cut;
    }
}
