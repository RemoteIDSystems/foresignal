package ai.foresignal.news;

import ai.foresignal.notify.NotificationTranslator;
import ai.foresignal.source.formats.news.NewsNames;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The source language of a news article (prompt NP-002), with a confidence.
 *
 * <ol>
 *   <li>The article's own {@code language} (an ISO 639 code from the feed, or a language name GDELT gives, through
 *       {@link NewsNames#languageCode}) is trusted: confidence {@value #DECLARED}.
 *   <li>Otherwise a cheap script detector: when one script dominates the letters of the text, the language that script
 *       almost always means (Hangul is Korean, Thai is Thai, Hebrew is Hebrew, Greek is Greek, kana is Japanese) at
 *       {@value #UNAMBIGUOUS}, and a script shared by several languages (Cyrillic, Arabic, Han, Devanagari) at
 *       {@value #SHARED} for its most common news language. Latin text gives no guess (confidence 0): too many
 *       languages share it to say which without a model.
 * </ol>
 */
public final class LanguageDetector {

    /** The confidence of a declared language. */
    public static final double DECLARED = 1.0;

    /** The confidence of a script that means one language. */
    public static final double UNAMBIGUOUS = 0.9;

    /** The confidence of a script several languages share. */
    public static final double SHARED = 0.6;

    /** The share of letters one script must have to count. */
    static final double DOMINANT = 0.6;

    private LanguageDetector() {}

    /**
     * A detected language.
     *
     * @param language the canonical BCP 47 tag, or null when unknown
     * @param confidence 0 to 1
     */
    public record Detection(String language, double confidence) {

        /** Nothing detected. */
        public static final Detection UNKNOWN = new Detection(null, 0);
    }

    /**
     * The language of an article.
     *
     * @param declared the article's {@code language} column, or null
     * @param text its title and summary
     */
    public static Detection detect(String declared, String text) {
        if (declared != null && !declared.isBlank()) {
            String code = NotificationTranslator.canonicalTag(NewsNames.languageCode(declared.strip()));
            if (code != null) {
                return new Detection(code, DECLARED);
            }
        }
        return byScript(text);
    }

    /** The cheap script detector alone. */
    public static Detection byScript(String text) {
        if (text == null || text.isBlank()) {
            return Detection.UNKNOWN;
        }
        Map<Character.UnicodeScript, Integer> counts = new LinkedHashMap<>();
        int letters = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (!Character.isLetter(cp)) {
                continue;
            }
            letters++;
            Character.UnicodeScript script = Character.UnicodeScript.of(cp);
            if (script == Character.UnicodeScript.HIRAGANA || script == Character.UnicodeScript.KATAKANA) {
                // Kana anywhere means Japanese, even beside Han.
                return new Detection("ja", UNAMBIGUOUS);
            }
            counts.merge(script, 1, Integer::sum);
        }
        if (letters == 0) {
            return Detection.UNKNOWN;
        }
        Character.UnicodeScript top = null;
        int best = 0;
        for (Map.Entry<Character.UnicodeScript, Integer> e : counts.entrySet()) {
            if (e.getValue() > best) {
                best = e.getValue();
                top = e.getKey();
            }
        }
        if (top == null || (double) best / letters < DOMINANT) {
            return Detection.UNKNOWN;
        }
        return switch (top) {
            case HANGUL -> new Detection("ko", UNAMBIGUOUS);
            case THAI -> new Detection("th", UNAMBIGUOUS);
            case HEBREW -> new Detection("he", UNAMBIGUOUS);
            case GREEK -> new Detection("el", UNAMBIGUOUS);
            case GEORGIAN -> new Detection("ka", UNAMBIGUOUS);
            case ARMENIAN -> new Detection("hy", UNAMBIGUOUS);
            case CYRILLIC -> new Detection("ru", SHARED);
            case ARABIC -> new Detection("ar", SHARED);
            case HAN -> new Detection("zh", SHARED);
            case DEVANAGARI -> new Detection("hi", SHARED);
            default -> Detection.UNKNOWN;
        };
    }
}
