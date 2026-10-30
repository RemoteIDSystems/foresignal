package ai.foresignal.news;

import ai.foresignal.llm.LlmSpendWindow;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

/**
 * News translation at ingest (prompt NP-002; OpenAPI {@code TranslationSettingsInput}), stored under
 * {@link ai.foresignal.settings.SettingKeys#TRANSLATION} and served by {@code GET/PUT /v1/settings/translation}.
 *
 * @param enabled the master switch; off, nothing is translated and reads carry the originals only
 * @param targetLanguages the canonical BCP 47 languages foreign news is translated into, in preference order
 * @param fields the translated fields: {@value #TITLE} and {@value #EXCERPT}
 * @param engine the engine; only {@value #ENGINE_LLM}
 * @param budget the translation-only spend cap
 * @param minSourceLanguageConfidence the least confidence in the detected source language at which to translate
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TranslationSettings(boolean enabled, List<String> targetLanguages, List<String> fields, String engine,
        Budget budget, Double minSourceLanguageConfidence) {

    /** The {@code title} field. */
    public static final String TITLE = "title";

    /** The {@code excerpt} field (an article's feed summary). */
    public static final String EXCERPT = "excerpt";

    /** The LLM engine. */
    public static final String ENGINE_LLM = "llm";

    /** The most target languages. */
    public static final int MAX_TARGETS = 10;

    /** The shipped confidence floor. */
    public static final double DEFAULT_CONFIDENCE = 0.8;

    /**
     * The translation-only spend cap.
     *
     * @param window the UTC window the cap applies to
     * @param capUsd USD per window, or null for no separate cap
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Budget(String window, BigDecimal capUsd) {

        /** Fills the default window ({@code day}). */
        public Budget {
            window = window == null || window.isBlank() ? LlmSpendWindow.DAY.wireValue() : window;
        }

        /** The window as an enum. */
        public LlmSpendWindow spendWindow() {
            for (LlmSpendWindow w : LlmSpendWindow.values()) {
                if (w.wireValue().equals(window)) {
                    return w;
                }
            }
            return LlmSpendWindow.DAY;
        }
    }

    /** Fills missing members with the shipped defaults. */
    public TranslationSettings {
        targetLanguages = targetLanguages == null ? List.of() : List.copyOf(targetLanguages);
        fields = fields == null ? List.of(TITLE, EXCERPT) : List.copyOf(fields);
        engine = engine == null || engine.isBlank() ? ENGINE_LLM : engine;
        budget = budget == null ? new Budget(null, null) : budget;
        minSourceLanguageConfidence = minSourceLanguageConfidence == null ? DEFAULT_CONFIDENCE
                : minSourceLanguageConfidence;
    }

    /** The shipped defaults: off, no target languages. */
    public static TranslationSettings defaults() {
        return new TranslationSettings(false, null, null, null, null, null);
    }

    /** True when translation is on and has somewhere to translate to. */
    public boolean active() {
        return enabled && !targetLanguages.isEmpty();
    }

    /** True when {@code field} is translated. */
    public boolean translates(String field) {
        return fields.contains(field);
    }
}
