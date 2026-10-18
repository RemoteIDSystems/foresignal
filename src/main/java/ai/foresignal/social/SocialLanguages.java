package ai.foresignal.social;

import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Post languages (prompt 135): the platform's language tag first; otherwise Lingua (Apache-2.0,
 * {@code com.github.pemistahl:lingua} 1.2.2) restricted to the configured {@code social.languages} (default
 * {@code en, ar, he, fa, ru, uk, fr, es, de, tr, zh}), in low-accuracy mode so only those languages' small models are
 * loaded, lazily on first use. Volume series are kept per configured language; every other language counts as
 * {@value SocialVocabulary#OTHER_LANGUAGE}.
 */
@Component
public class SocialLanguages {

    private static final Logger log = LoggerFactory.getLogger(SocialLanguages.class);

    /** Texts shorter than this are not detected (Lingua guesses on them). */
    static final int MIN_DETECT_LENGTH = 12;

    private final AppSettingService settings;
    private volatile List<String> configured;
    private volatile LanguageDetector detector;
    private volatile boolean detectorFailed;

    /** Creates the component from the {@code social.languages} setting. */
    @Autowired
    public SocialLanguages(AppSettingService settings) {
        this.settings = settings;
    }

    /** Creates the component with fixed languages (tests). */
    public SocialLanguages(List<String> languages) {
        this.settings = null;
        this.configured = normalize(languages);
    }

    /** The configured languages (primary subtags). */
    public List<String> languages() {
        List<String> current = configured;
        if (current == null) {
            current = load();
            configured = current;
        }
        return current;
    }

    private List<String> load() {
        if (settings == null) {
            return SocialVocabulary.DEFAULT_LANGUAGES;
        }
        try {
            List<?> stored = settings.get(SettingKeys.SOCIAL_LANGUAGES, List.class).orElse(null);
            if (stored == null || stored.isEmpty()) {
                return SocialVocabulary.DEFAULT_LANGUAGES;
            }
            List<String> out = new ArrayList<>();
            for (Object o : stored) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
            return normalize(out);
        } catch (RuntimeException e) {
            return SocialVocabulary.DEFAULT_LANGUAGES;
        }
    }

    private static List<String> normalize(List<String> languages) {
        Set<String> out = new LinkedHashSet<>();
        if (languages != null) {
            for (String l : languages) {
                String p = SocialPosts.primaryLanguage(l);
                if (p != null) {
                    out.add(p);
                }
            }
        }
        return out.isEmpty() ? SocialVocabulary.DEFAULT_LANGUAGES : List.copyOf(out);
    }

    /** Drops the detector when {@code social.languages} changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.SOCIAL_LANGUAGES.equals(event.key())) {
            configured = null;
            detector = null;
            detectorFailed = false;
        }
    }

    /**
     * The language of a post: the platform tag's primary subtag when given, else the detected one.
     *
     * @param platformTag the platform's tag, or null
     * @param text the text, or null
     * @return a primary language subtag, or null when unknown
     */
    public String languageOf(String platformTag, String text) {
        String tagged = SocialPosts.primaryLanguage(platformTag);
        if (tagged != null) {
            return tagged;
        }
        return detect(text);
    }

    /**
     * Detects the language of a text among the configured languages.
     *
     * @return the primary subtag, or null when the text is short or no configured language fits
     */
    public String detect(String text) {
        if (text == null || text.strip().length() < MIN_DETECT_LENGTH) {
            return null;
        }
        LanguageDetector d = detector();
        if (d == null) {
            return null;
        }
        try {
            Language language = d.detectLanguageOf(text);
            if (language == null || language == Language.UNKNOWN) {
                return null;
            }
            return language.getIsoCode639_1().name().toLowerCase(Locale.ROOT);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The series bucket of a language: itself when configured, else {@value SocialVocabulary#OTHER_LANGUAGE}. */
    public String bucket(String language) {
        String p = SocialPosts.primaryLanguage(language);
        return p != null && languages().contains(p) ? p : SocialVocabulary.OTHER_LANGUAGE;
    }

    private LanguageDetector detector() {
        LanguageDetector d = detector;
        if (d != null || detectorFailed) {
            return d;
        }
        synchronized (this) {
            if (detector != null) {
                return detector;
            }
            List<Language> chosen = new ArrayList<>();
            for (String code : languages()) {
                for (Language l : Language.values()) {
                    if (l != Language.UNKNOWN && l.getIsoCode639_1().name().equalsIgnoreCase(code)) {
                        chosen.add(l);
                    }
                }
            }
            if (chosen.size() < 2) {
                // Lingua needs at least two languages; with one configured, everything is that language or other.
                detectorFailed = true;
                return null;
            }
            try {
                detector = LanguageDetectorBuilder.fromLanguages(chosen.toArray(new Language[0])).withLowAccuracyMode()
                        .build();
            } catch (RuntimeException | LinkageError e) {
                log.warn("Lingua could not start; social posts without a platform language tag stay unknown: {}",
                        e.toString());
                detectorFailed = true;
            }
            return detector;
        }
    }
}
