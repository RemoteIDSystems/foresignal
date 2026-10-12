package ai.foresignal.briefing;

import java.util.Map;
import java.util.Objects;

/**
 * One section a {@link BriefingSectionContributor} adds to a digest (prompt 029).
 *
 * @param key the key under {@code DigestStats.sections}, for example {@code weatherAtPlaces}
 * @param data the section's statistics (JSON-serializable)
 * @param markdown the section's markdown, appended to the digest (starting with its own {@code ##}
 *     heading), or null for none
 * @param llmContext what the LLM is given for the section (JSON-serializable), or null for {@code data}
 */
public record BriefingSection(String key, Map<String, Object> data, String markdown, Object llmContext) {

    /** Requires the key; copies the data. */
    public BriefingSection {
        Objects.requireNonNull(key, "key");
        data = data == null ? Map.of() : data;
    }
}
