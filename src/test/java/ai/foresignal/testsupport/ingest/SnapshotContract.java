package ai.foresignal.testsupport.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.ContentHasher;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.ingest.NormalizedBatch;
import ai.foresignal.ingest.Normalizer;
import ai.foresignal.ingest.Observation;
import ai.foresignal.ingest.SourceIngestProfile;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter rule 7 as an assertion: every record an adapter emits for one entity is a
 * <em>complete</em> snapshot, so snapshots that differ only in kinematic or volatile content share
 * one static hash (Core-Data-Model §3).
 *
 * <p>Adapters from prompt 011 on call {@link #assertStableStaticHash} with snapshots of one entity
 * whose significant content is unchanged. A protocol with split messages (AIS, MQTT) that forgot
 * to merge per entity fails here, because a partial payload drops significant keys.
 */
public final class SnapshotContract {

    private SnapshotContract() {}

    /**
     * Normalizes each snapshot under {@code descriptor}'s profile and asserts they are all accepted,
     * all describe one entity, and all share one static hash.
     *
     * @param descriptor the adapter's descriptor
     * @param snapshotsOfOneEntity at least two records of the same entity with unchanged significant
     *     content
     */
    public static void assertStableStaticHash(
            SourceDescriptor descriptor, List<NormalizedRecord> snapshotsOfOneEntity) {
        assertThat(snapshotsOfOneEntity).as("snapshots").hasSizeGreaterThanOrEqualTo(2);
        SourceIngestProfile profile = SourceIngestProfile.forDescriptor(descriptor);

        List<IngestRecord> in = new ArrayList<>();
        long arrival = 0;
        for (NormalizedRecord record : snapshotsOfOneEntity) {
            // Receive each record when it was observed, so no clamp or time base interferes.
            Instant received = record.observedAt() != null ? record.observedAt() : Instant.now();
            in.add(new IngestRecord(record, received, arrival++));
        }
        NormalizedBatch batch = new Normalizer().normalize(profile, in);
        assertThat(batch.rejections()).as("rejected snapshots of %s", descriptor.id()).isEmpty();

        List<Observation> observations = batch.observations();
        assertThat(observations).extracting(Observation::externalId).as("one entity").containsOnly(
                observations.getFirst().externalId());

        long expected = ContentHasher.staticHash(observations.getFirst(), profile);
        for (Observation observation : observations) {
            assertThat(ContentHasher.staticHash(observation, profile))
                    .as(
                            "static hash of %s at %s; a changed static hash means a snapshot is missing or"
                                    + " changing significant content (adapter rule 7)",
                            observation.externalId(),
                            observation.observedAt())
                    .isEqualTo(expected);
        }
    }
}
