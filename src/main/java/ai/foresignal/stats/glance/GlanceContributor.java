package ai.foresignal.stats.glance;

import ai.foresignal.api.support.ReadCaller;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Adds a section to the glance (prompt 038; the SPI prompt 056 uses for weather at a place and 040
 * may use for {@code near} texts). Declare a bean; beans are asked in {@link org.springframework.core.Ordered}
 * order, and each section is kept only when its JSON fits {@link #budgetBytes()}. When the whole
 * document would exceed 2,048 bytes, sections are dropped from the last contributor backwards.
 * Sections are never shown to callers without a token.
 */
public interface GlanceContributor {

    /** The section key under {@code sections}. */
    String key();

    /** The most bytes of UTF-8 JSON the section may take. */
    int budgetBytes();

    /**
     * The section's value (a map, list, string or number), or empty to add nothing.
     *
     * @param context who is asking and for what
     */
    Optional<Object> contribute(Context context);

    /**
     * What a contributor may use.
     *
     * @param caller the caller (never anonymous here)
     * @param excludedSources sources the caller may not see
     * @param placeId the {@code place} parameter, or null
     * @param now the glance's clock reading
     */
    record Context(ReadCaller caller, Set<String> excludedSources, String placeId, Instant now) {}
}
