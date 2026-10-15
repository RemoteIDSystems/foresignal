package ai.foresignal.derive;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Read access to current records for a {@link DerivedGenerator} (prompt 052): {@code record_head ⋈
 * record_live} only (invariant HIST-2), never history. Generators never write the store; they emit
 * through {@link DerivedContext}.
 */
public interface DerivedRecords {

    /**
     * Every head of {@code sourceId}, tombstones included, optionally of one kind, ordered by external id.
     *
     * @param sourceId the source
     * @param kind the kind, or null for every kind
     */
    List<HeadSnapshot> heads(String sourceId, String kind);

    /**
     * The current, non-tombstoned records of the given sources and kinds, ordered by source and external id.
     *
     * @param sources the sources (at least one)
     * @param kinds the kinds, or empty for every kind
     * @param limit the most rows returned
     */
    List<HeadSnapshot> current(Collection<String> sources, Collection<String> kinds, int limit);

    /**
     * The current records of the given sources and kinds whose live geometry intersects {@code bbox}
     * (prompt 099: a regional generator loads only its area), ordered by source and external id.
     *
     * @param sources the sources (at least one)
     * @param kinds the kinds, or empty for every kind
     * @param bbox the bounding box in 4326, or null for no restriction
     * @param limit the most rows returned
     */
    List<HeadSnapshot> current(Collection<String> sources, Collection<String> kinds, org.locationtech.jts.geom.Envelope bbox,
            int limit);

    /** One current record, tombstones included, or empty when the entity has no head. */
    Optional<HeadSnapshot> get(String sourceId, String externalId);
}
