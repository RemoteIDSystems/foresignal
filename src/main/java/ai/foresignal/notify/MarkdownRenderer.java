package ai.foresignal.notify;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts Foresignal's markdown subset to each destination's format (prompt 018).
 *
 * <p>The subset: headings ({@code #} to {@code ###} at the start of a line), {@code **bold**},
 * {@code *italic*} or {@code _italic_}, {@code ~~strike~~}, {@code [label](url)}, bullet lines
 * ({@code - } or {@code * }), inline {@code `code`} and fenced code blocks. A backslash escapes the
 * next character. Anything else is text, and text is escaped for the target so that a payload value
 * such as {@code <script>} or {@code a_b*c} arrives literally.
 *
 * <p>An {@code _} only opens italics at the start of a word, so {@code snake_case_names} stay text.
 * An opener without a closer is text.
 */
public final class MarkdownRenderer {

    /** Target formats. */
    public enum Format {
        /** Plain text: markers removed, links as {@code label (url)}. */
        PLAIN,
        /** Slack mrkdwn ({@code &}, {@code <}, {@code >} escaped; links as {@code <url|label>}). */
        SLACK,
        /** Discord markdown (markdown characters backslash-escaped). */
        DISCORD,
        /** Telegram HTML ({@code b}, {@code i}, {@code s}, {@code code}, {@code pre}, {@code a} only). */
        TELEGRAM_HTML,
        /** Matrix {@code org.matrix.custom.html}. */
        MATRIX_HTML,
        /** Adaptive Card TextBlock markdown (bold, italic, links and bullets only). */
        ADAPTIVE_CARD,
        /** Zulip markdown. */
        ZULIP,
        /** Google Chat card text ({@code b}, {@code i}, {@code strike}, {@code a}, {@code br}). */
        GOOGLE_CHAT,
        /** Email HTML. */
        EMAIL_HTML
    }

    private MarkdownRenderer() {}

    /** Renders {@code markdown} (null renders as empty) in {@code format}. */
    public static String render(String markdown, Format format) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        List<Block> blocks = parseBlocks(markdown);
        return switch (format) {
            case MATRIX_HTML, EMAIL_HTML -> html(blocks, format);
            default -> lines(blocks, format);
        };
    }

    /**
     * Escapes plain text as Foresignal markdown, so substituted values (an alert message, a payload
     * value) arrive literally in a markdown body.
     */
    public static String escapeMarkdown(String text) {
        return text == null ? "" : markdownEscape(text);
    }

    /** Escapes plain text for {@code format} (used for titles placed in formatted fields). */
    public static String escape(String text, Format format) {
        return text == null ? "" : escapeText(text, format);
    }

    // ------------------------------------------------------------------------- blocks

    private sealed interface Block permits Heading, Bullet, Line, Code, Blank {}

    private record Heading(int level, List<Inline> inlines) implements Block {}

    private record Bullet(List<Inline> inlines) implements Block {}

    private record Line(List<Inline> inlines) implements Block {}

    private record Code(String text) implements Block {}

    private record Blank() implements Block {}

    private static List<Block> parseBlocks(String markdown) {
        List<Block> blocks = new ArrayList<>();
        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.strip();
            if (trimmed.startsWith("```")) {
                StringBuilder code = new StringBuilder();
                int j = i + 1;
                while (j < lines.length && !lines[j].strip().startsWith("```")) {
                    if (code.length() > 0) {
                        code.append('\n');
                    }
                    code.append(lines[j]);
                    j++;
                }
                blocks.add(new Code(code.toString()));
                i = j;
                continue;
            }
            if (trimmed.isEmpty()) {
                blocks.add(new Blank());
            } else if (trimmed.startsWith("### ")) {
                blocks.add(new Heading(3, parseInline(trimmed.substring(4))));
            } else if (trimmed.startsWith("## ")) {
                blocks.add(new Heading(2, parseInline(trimmed.substring(3))));
            } else if (trimmed.startsWith("# ")) {
                blocks.add(new Heading(1, parseInline(trimmed.substring(2))));
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                blocks.add(new Bullet(parseInline(trimmed.substring(2))));
            } else {
                blocks.add(new Line(parseInline(line)));
            }
        }
        // Trailing blank lines carry nothing.
        while (!blocks.isEmpty() && blocks.getLast() instanceof Blank) {
            blocks.removeLast();
        }
        return blocks;
    }

    // ------------------------------------------------------------------------- inlines

    private sealed interface Inline permits Text, Bold, Italic, Strike, InlineCode, Link {}

    private record Text(String text) implements Inline {}

    private record Bold(List<Inline> children) implements Inline {}

    private record Italic(List<Inline> children) implements Inline {}

    private record Strike(List<Inline> children) implements Inline {}

    private record InlineCode(String text) implements Inline {}

    private record Link(List<Inline> children, String url) implements Inline {}

    static List<Inline> parseInline(String s) {
        List<Inline> out = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                text.append(s.charAt(i + 1));
                i += 2;
                continue;
            }
            int consumed = 0;
            Inline node = null;
            if (c == '`') {
                int close = s.indexOf('`', i + 1);
                if (close > i + 1) {
                    node = new InlineCode(s.substring(i + 1, close));
                    consumed = close + 1 - i;
                }
            } else if (s.startsWith("**", i)) {
                int close = s.indexOf("**", i + 2);
                if (close > i + 2) {
                    node = new Bold(parseInline(s.substring(i + 2, close)));
                    consumed = close + 2 - i;
                }
            } else if (s.startsWith("~~", i)) {
                int close = s.indexOf("~~", i + 2);
                if (close > i + 2) {
                    node = new Strike(parseInline(s.substring(i + 2, close)));
                    consumed = close + 2 - i;
                }
            } else if ((c == '*' || c == '_') && opensEmphasis(s, i)) {
                int close = closingEmphasis(s, i + 1, c);
                if (close > i + 1) {
                    node = new Italic(parseInline(s.substring(i + 1, close)));
                    consumed = close + 1 - i;
                }
            } else if (c == '[') {
                int mid = s.indexOf("](", i + 1);
                int close = mid < 0 ? -1 : s.indexOf(')', mid + 2);
                if (mid > i + 1 && close > mid + 2) {
                    node = new Link(parseInline(s.substring(i + 1, mid)), s.substring(mid + 2, close).strip());
                    consumed = close + 1 - i;
                }
            }
            if (node == null) {
                text.append(c);
                i++;
            } else {
                if (text.length() > 0) {
                    out.add(new Text(text.toString()));
                    text.setLength(0);
                }
                out.add(node);
                i += consumed;
            }
        }
        if (text.length() > 0) {
            out.add(new Text(text.toString()));
        }
        return out;
    }

    private static boolean opensEmphasis(String s, int i) {
        boolean wordBefore = i > 0 && Character.isLetterOrDigit(s.charAt(i - 1));
        boolean spaceAfter = i + 1 >= s.length() || Character.isWhitespace(s.charAt(i + 1));
        return !wordBefore && !spaceAfter;
    }

    private static int closingEmphasis(String s, int from, char marker) {
        for (int k = from; k < s.length(); k++) {
            if (s.charAt(k) == marker && !Character.isWhitespace(s.charAt(k - 1))) {
                boolean wordAfter = k + 1 < s.length() && Character.isLetterOrDigit(s.charAt(k + 1));
                if (!wordAfter) {
                    return k;
                }
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------------- rendering

    private static String lines(List<Block> blocks, Format f) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) {
                out.append(f == Format.GOOGLE_CHAT ? "<br>" : "\n");
            }
            Block block = blocks.get(i);
            switch (block) {
                case Blank b -> { }
                case Heading h -> out.append(heading(h, f));
                case Bullet b -> out.append(bulletPrefix(f)).append(inlines(b.inlines(), f));
                case Line l -> out.append(inlines(l.inlines(), f));
                case Code c -> out.append(codeBlock(c.text(), f));
            }
        }
        return out.toString();
    }

    private static String heading(Heading h, Format f) {
        String inner = inlines(h.inlines(), f);
        return switch (f) {
            case PLAIN, ADAPTIVE_CARD -> f == Format.PLAIN ? inner : "**" + inner + "**";
            case SLACK -> "*" + inner + "*";
            case DISCORD -> "**" + inner + "**";
            case TELEGRAM_HTML, GOOGLE_CHAT -> "<b>" + inner + "</b>";
            case ZULIP -> "#".repeat(h.level()) + " " + inner;
            default -> inner;
        };
    }

    private static String bulletPrefix(Format f) {
        return switch (f) {
            case DISCORD, ADAPTIVE_CARD, ZULIP -> "- ";
            default -> "• ";
        };
    }

    private static String codeBlock(String code, Format f) {
        return switch (f) {
            case SLACK, DISCORD, ZULIP -> "```\n" + code.replace("```", "'''") + "\n```";
            case TELEGRAM_HTML -> "<pre>" + htmlEscape(code) + "</pre>";
            case GOOGLE_CHAT -> htmlEscape(code).replace("\n", "<br>");
            default -> code;
        };
    }

    private static String inlines(List<Inline> nodes, Format f) {
        StringBuilder out = new StringBuilder();
        for (Inline node : nodes) {
            switch (node) {
                case Text t -> out.append(escapeText(t.text(), f));
                case Bold b -> out.append(wrap(inlines(b.children(), f), boldMarks(f)));
                case Italic it -> out.append(wrap(inlines(it.children(), f), italicMarks(f)));
                case Strike s -> out.append(wrap(inlines(s.children(), f), strikeMarks(f)));
                case InlineCode c -> out.append(code(c.text(), f));
                case Link l -> out.append(link(l, f));
            }
        }
        return out.toString();
    }

    private static String wrap(String inner, String[] marks) {
        return marks[0] + inner + marks[1];
    }

    private static String[] boldMarks(Format f) {
        return switch (f) {
            case PLAIN -> new String[] {"", ""};
            case SLACK -> new String[] {"*", "*"};
            case TELEGRAM_HTML, GOOGLE_CHAT -> new String[] {"<b>", "</b>"};
            case MATRIX_HTML, EMAIL_HTML -> new String[] {"<strong>", "</strong>"};
            default -> new String[] {"**", "**"};
        };
    }

    private static String[] italicMarks(Format f) {
        return switch (f) {
            case PLAIN -> new String[] {"", ""};
            case SLACK, ADAPTIVE_CARD -> new String[] {"_", "_"};
            case TELEGRAM_HTML, GOOGLE_CHAT -> new String[] {"<i>", "</i>"};
            case MATRIX_HTML, EMAIL_HTML -> new String[] {"<em>", "</em>"};
            default -> new String[] {"*", "*"};
        };
    }

    private static String[] strikeMarks(Format f) {
        return switch (f) {
            case PLAIN, ADAPTIVE_CARD -> new String[] {"", ""};
            case SLACK -> new String[] {"~", "~"};
            case TELEGRAM_HTML -> new String[] {"<s>", "</s>"};
            case GOOGLE_CHAT -> new String[] {"<strike>", "</strike>"};
            case MATRIX_HTML -> new String[] {"<del>", "</del>"};
            case EMAIL_HTML -> new String[] {"<s>", "</s>"};
            default -> new String[] {"~~", "~~"};
        };
    }

    private static String code(String text, Format f) {
        return switch (f) {
            case PLAIN, ADAPTIVE_CARD -> text;
            case SLACK -> "`" + slackEscape(text).replace("`", "'") + "`";
            case DISCORD, ZULIP -> "`" + text.replace("`", "'") + "`";
            case GOOGLE_CHAT -> htmlEscape(text);
            default -> "<code>" + htmlEscape(text) + "</code>";
        };
    }

    private static String link(Link l, Format f) {
        String label = inlines(l.children(), f);
        String url = l.url();
        return switch (f) {
            case PLAIN -> label.equals(url) ? url : label + " (" + url + ")";
            case SLACK -> "<" + slackEscape(url).replace("|", "%7C") + "|" + label.replace("|", "¦") + ">";
            case TELEGRAM_HTML, MATRIX_HTML, EMAIL_HTML, GOOGLE_CHAT ->
                    "<a href=\"" + htmlEscape(url).replace("\"", "&quot;") + "\">" + label + "</a>";
            default -> "[" + label + "](" + url.replace(")", "%29").replace(" ", "%20") + ")";
        };
    }

    private static String html(List<Block> blocks, Format f) {
        StringBuilder out = new StringBuilder();
        boolean inList = false;
        boolean lineOpen = false;
        for (Block block : blocks) {
            if (!(block instanceof Bullet) && inList) {
                out.append("</ul>");
                inList = false;
            }
            switch (block) {
                case Bullet b -> {
                    if (lineOpen) {
                        out.append("<br>");
                        lineOpen = false;
                    }
                    if (!inList) {
                        out.append("<ul>");
                        inList = true;
                    }
                    out.append("<li>").append(inlines(b.inlines(), f)).append("</li>");
                }
                case Heading h -> {
                    lineOpen = false;
                    out.append("<h").append(h.level() + 2).append('>').append(inlines(h.inlines(), f))
                            .append("</h").append(h.level() + 2).append('>');
                }
                case Line l -> {
                    if (lineOpen) {
                        out.append("<br>");
                    }
                    out.append(inlines(l.inlines(), f));
                    lineOpen = true;
                }
                case Code c -> {
                    lineOpen = false;
                    out.append("<pre><code>").append(htmlEscape(c.text())).append("</code></pre>");
                }
                case Blank b -> {
                    if (lineOpen) {
                        out.append("<br>");
                    }
                    lineOpen = true;
                }
            }
        }
        if (inList) {
            out.append("</ul>");
        }
        return out.toString();
    }

    // ------------------------------------------------------------------------- escaping

    private static String escapeText(String text, Format f) {
        return switch (f) {
            case PLAIN, ADAPTIVE_CARD -> text;
            case SLACK -> slackEscape(text);
            case DISCORD, ZULIP -> markdownEscape(text);
            default -> htmlEscape(text);
        };
    }

    /** Slack mrkdwn: only {@code &}, {@code <} and {@code >} are control characters. */
    static String slackEscape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** HTML text: {@code &}, {@code <}, {@code >}. */
    static String htmlEscape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Backslash-escapes markdown control characters. */
    static String markdownEscape(String text) {
        StringBuilder out = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ("\\*_~`|[]".indexOf(c) >= 0) {
                out.append('\\');
            }
            out.append(c);
        }
        return out.toString();
    }
}
