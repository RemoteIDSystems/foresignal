package ai.foresignal.social;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Text folding for social term matching (prompt 135): case- and diacritic-insensitive, word-boundary aware per
 * script.
 *
 * <p>{@link #fold} decomposes (NFKD), drops combining marks (Latin accents, Arabic harakat and the hamza carried by
 * alef forms, Hebrew niqqud), removes the Arabic tatweel, unifies alef maksura with ya and ta marbuta with ha, folds
 * case through upper then lower case (so {@code ß} becomes {@code ss} and the final sigma {@code ς} becomes
 * {@code σ}), and turns every run of characters that are neither letters nor digits into one space. Phrases therefore
 * match across punctuation and hyphen variants ("Bab el-Mandeb" = "bab el mandeb"), and a hashtag in a text matches
 * its bare word.
 *
 * <p>Word boundaries: a match must start and end at a space or the text's edge, except for terms in scripts written
 * without spaces (Han, Hiragana, Katakana, Hangul syllables are spaced and keep boundaries; Thai, Lao, Khmer and
 * Myanmar do not), which match anywhere. Arabic and Hebrew terms may also follow their written proclitics (Arabic
 * {@code و ف ب ك ل ال}; Hebrew {@code ו ה ב כ ל מ ש}), which are attached to the next word in those scripts.
 */
public final class TextFolding {

    /** Arabic proclitic prefixes allowed before an Arabic term (folded). */
    static final List<String> ARABIC_PREFIXES = List.of("وال", "فال", "بال", "كال", "لل", "ال", "و", "ف", "ب", "ك",
            "ل");

    /** Hebrew prefix letters allowed (up to three, in any combination) before a Hebrew term. */
    static final String HEBREW_PREFIX_LETTERS = "והבכלמש";

    private TextFolding() {}

    /**
     * Folds a text or a term.
     *
     * @param text the text, or null
     * @return the folded text (letters, digits and single spaces; no leading or trailing space), or empty
     */
    public static String fold(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFKD);
        StringBuilder sb = new StringBuilder(decomposed.length());
        boolean space = true;
        for (int i = 0; i < decomposed.length(); ) {
            int cp = decomposed.codePointAt(i);
            i += Character.charCount(cp);
            int type = Character.getType(cp);
            if (type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK || cp == 0x0640 || cp == 0x200C || cp == 0x200D) {
                continue;
            }
            if (cp == 0x0649) {
                cp = 0x064A;
            } else if (cp == 0x0629) {
                cp = 0x0647;
            }
            if (Character.isLetterOrDigit(cp)) {
                String folded = new String(Character.toChars(cp)).toUpperCase(Locale.ROOT).toLowerCase(Locale.ROOT);
                if (folded.indexOf('ς') >= 0) {
                    folded = folded.replace('ς', 'σ');
                }
                sb.append(folded);
                space = false;
            } else if (!space) {
                sb.append(' ');
                space = true;
            }
        }
        int len = sb.length();
        if (len > 0 && sb.charAt(len - 1) == ' ') {
            sb.setLength(len - 1);
        }
        return sb.toString();
    }

    /** True when a code point belongs to a script written without spaces between words. */
    public static boolean unspacedScript(int cp) {
        Character.UnicodeScript script;
        try {
            script = Character.UnicodeScript.of(cp);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return switch (script) {
            case HAN, HIRAGANA, KATAKANA, THAI, LAO, KHMER, MYANMAR, TIBETAN -> true;
            default -> false;
        };
    }

    /** The script of a term for boundary rules: its first letter's script. */
    static Character.UnicodeScript scriptOf(String folded) {
        for (int i = 0; i < folded.length(); ) {
            int cp = folded.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isLetter(cp)) {
                try {
                    return Character.UnicodeScript.of(cp);
                } catch (IllegalArgumentException e) {
                    return Character.UnicodeScript.COMMON;
                }
            }
        }
        return Character.UnicodeScript.COMMON;
    }

    /**
     * True when a match of {@code term} at {@code [start, end)} of the folded {@code text} sits on word boundaries.
     *
     * @param text the folded text
     * @param term the folded term
     * @param start the match start (char index)
     * @param end the match end (char index, exclusive)
     */
    public static boolean onBoundaries(String text, String term, int start, int end) {
        if (term.isEmpty()) {
            return false;
        }
        boolean leadFree = unspacedScript(term.codePointAt(0));
        boolean trailFree = unspacedScript(term.codePointBefore(term.length()));
        boolean trailOk = trailFree || end >= text.length() || text.charAt(end) == ' ';
        if (!trailOk) {
            return false;
        }
        if (leadFree || start == 0 || text.charAt(start - 1) == ' ') {
            return true;
        }
        Character.UnicodeScript script = scriptOf(term);
        if (script == Character.UnicodeScript.ARABIC) {
            for (String prefix : ARABIC_PREFIXES) {
                int p = start - prefix.length();
                if (p >= 0 && text.startsWith(prefix, p) && (p == 0 || text.charAt(p - 1) == ' ')) {
                    return true;
                }
            }
            return false;
        }
        if (script == Character.UnicodeScript.HEBREW) {
            int p = start;
            for (int n = 0; n < 3 && p > 0 && HEBREW_PREFIX_LETTERS.indexOf(text.charAt(p - 1)) >= 0; n++) {
                p--;
                if (p == 0 || text.charAt(p - 1) == ' ') {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /** A handle folded for comparison: no leading {@code @}, lower case. */
    public static String handle(String handle) {
        if (handle == null) {
            return "";
        }
        String h = handle.strip();
        while (h.startsWith("@")) {
            h = h.substring(1);
        }
        return h.toLowerCase(Locale.ROOT);
    }

    /** A hashtag folded for comparison: no leading {@code #}, folded. */
    public static String hashtag(String tag) {
        if (tag == null) {
            return "";
        }
        String t = tag.strip();
        while (t.startsWith("#")) {
            t = t.substring(1);
        }
        return fold(t).replace(" ", "");
    }
}
