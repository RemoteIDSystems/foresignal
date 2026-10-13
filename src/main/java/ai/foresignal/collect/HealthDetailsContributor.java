package ai.foresignal.collect;

import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Map;

/**
 * Adds adapter-specific entries to a source's health {@code details} (OpenAPI {@code SourceHealth.details},
 * prompt 063), contributed as a Spring bean.
 *
 * <p>The first contributor is {@code aisstream}, which reports whether the upstream confirmed compression
 * ({@code compressionEnabled}). Entries of every contributor are merged in bean order; a later key replaces
 * an earlier one. A contributor that throws is logged and skipped, so one faulty contributor cannot hide a
 * source's health.
 */
@FunctionalInterface
public interface HealthDetailsContributor {

    /**
     * The details of one source.
     *
     * @return key to JSON-serializable value; empty when the contributor has nothing for this source
     */
    Map<String, Object> details(SourceDescriptor descriptor);
}
