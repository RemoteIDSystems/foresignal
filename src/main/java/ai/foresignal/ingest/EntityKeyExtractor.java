package ai.foresignal.ingest;

import java.util.Map;

/**
 * Extracts entity identifiers (ICAO 24-bit address, MMSI, IMO, registration, callsign …) from an
 * observation's payload, so the entity registry (prompt 098) can link records across sources
 * (Gap-Analysis §8.A item 2).
 *
 * <p>A shared hook: the normalizer calls it for every accepted observation, and the writer
 * (prompt 009) hands the result of persisted entities to every {@link EntityKeyListener} after
 * commit. {@link DescriptorEntityKeyExtractor} is the default implementation.
 */
public interface EntityKeyExtractor {

    /**
     * The entity keys of {@code payload}: key type to normalized value. Empty when the source
     * declares none or none are present; never null.
     */
    Map<String, String> extract(SourceIngestProfile profile, Map<String, ?> payload);
}
