package ai.foresignal.trigger;

import java.util.Optional;

/**
 * Adds a named section to an alert's enrichment context (prompt 030 SPI). Prompt 056 contributes
 * {@code alert.context.weather} through it.
 *
 * <p>Contributors are Spring beans, called in ascending {@link #order()} for every enrichment. A
 * section is plain JSON-serializable data (maps, lists, strings, numbers); it is sent to the model
 * under {@code sections.<key>} and trimmed with the other context when the 24 KB cap is reached.
 * A contributor that throws is skipped and the enrichment continues without its section.
 * Contributors must respect {@link EnrichmentAnchor#allows(String)} for any record they include.
 */
public interface EnrichmentContextContributor {

    /** The section key, for example {@code alert.context.weather}. */
    String key();

    /** The position among contributors; lower runs first. */
    default int order() {
        return 0;
    }

    /**
     * The section for {@code anchor}, or empty when there is nothing to add.
     *
     * @param anchor the alert, anchor geometry and time, radius, window and destination
     */
    Optional<Object> contribute(EnrichmentAnchor anchor);
}
