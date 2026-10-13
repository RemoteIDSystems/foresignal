package ai.foresignal.feed;

/**
 * Text safety for the XML documents of prompt 058 (Atom and KML).
 *
 * <p>{@link #clean} removes what XML 1.0 cannot carry at all (control characters other than tab, line
 * feed and carriage return, lone surrogates, U+FFFE and U+FFFF), so an upstream string can never make a
 * document ill-formed. {@link #html} also escapes {@code <}, {@code >}, {@code &} and {@code "} for HTML,
 * for text that a client renders as HTML (Google Earth balloons); the XML writer then escapes the result
 * once more, so the client sees the literal characters.
 */
public final class XmlText {

    private XmlText() {}

    /** {@code text} without characters XML 1.0 forbids; null stays null. */
    public static String clean(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder out = null;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean ok;
            if (Character.isHighSurrogate(c)) {
                ok = i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1));
                if (ok) {
                    if (out != null) {
                        out.append(c).append(text.charAt(i + 1));
                    }
                    i++;
                    continue;
                }
            } else if (Character.isLowSurrogate(c)) {
                ok = false;
            } else {
                ok = c == '\t' || c == '\n' || c == '\r' || (c >= 0x20 && c != 0xFFFE && c != 0xFFFF);
            }
            if (!ok) {
                if (out == null) {
                    out = new StringBuilder(text.length());
                    out.append(text, 0, i);
                }
            } else if (out != null) {
                out.append(c);
            }
        }
        return out == null ? text : out.toString();
    }

    /** {@link #clean} plus HTML escaping of {@code <}, {@code >}, {@code &} and {@code "}; null becomes empty. */
    public static String html(String text) {
        String cleaned = clean(text);
        if (cleaned == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(cleaned.length() + 16);
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            switch (c) {
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '&' -> out.append("&amp;");
                case '"' -> out.append("&quot;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
