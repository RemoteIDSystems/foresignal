package ai.foresignal.ingest;

import java.util.Map;

/**
 * What a {@link HeadPurge} deleted.
 *
 * @param headsDeleted heads deleted per source id
 */
public record PurgeOutcome(Map<String, Integer> headsDeleted) {

    public PurgeOutcome {
        headsDeleted = headsDeleted == null ? Map.of() : Map.copyOf(headsDeleted);
    }
}
