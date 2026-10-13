package ai.foresignal.llm.client;

import java.util.Locale;

/**
 * Why an LLM call is made. Logged with every call and used to choose the default effort.
 */
public enum Purpose {
    /** Ask Foresignal (028). */
    ASK(Effort.MEDIUM),
    /** Scheduled briefings (029). */
    BRIEFING(Effort.MEDIUM),
    /** Alert enrichment (030). */
    ENRICHMENT(Effort.LOW),
    /** Semantic search: query embedding and re-ranking (027). */
    SEARCH(Effort.LOW),
    /** The provider test endpoint. */
    TEST(Effort.LOW),
    /** Diagnostics ("doctor") prompts. */
    DOCTOR(Effort.MEDIUM),
    /** The intelligence layer's analyst calls (101 onward). */
    ANALYST(Effort.MEDIUM),
    /** LLM-suggested draft RecordMappings (059); wire value {@code mapping-suggest}. */
    MAPPING_SUGGEST(Effort.LOW);

    private final Effort defaultEffort;

    Purpose(Effort defaultEffort) {
        this.defaultEffort = defaultEffort;
    }

    /** The effort used when neither the request nor the provider options set one. */
    public Effort defaultEffort() {
        return defaultEffort;
    }

    /** The wire value, for example {@code ask} or {@code mapping-suggest}. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
