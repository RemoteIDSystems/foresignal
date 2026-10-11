package ai.foresignal.testsupport.ingest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SnapshotContractTest {

    private static final Instant T0 = Instant.parse("2026-10-11T12:00:00Z");

    private static final SourceDescriptor VESSELS =
            SourceDescriptor.builder("test-vessels")
                    .kind("vessel")
                    .historyTier(HistoryTier.TRACK)
                    .interval(Duration.ofSeconds(5), Duration.ofSeconds(10))
                    .defaultBudget(60, 10_000)
                    .recordTtl(Duration.ofMinutes(30))
                    .significantFields("name", "destination")
                    .defaultIngestPolicy(IngestPolicy.sampled(
                            Duration.ofSeconds(30), Duration.ofSeconds(1200), 250.0, false))
                    .build();

    private static NormalizedRecord vessel(long seconds, String destination, double sog) {
        NormalizedRecord.Builder builder =
                NormalizedRecord.builder("366123456", "vessel")
                        .observedAt(T0.plusSeconds(seconds))
                        .point(-122.4, 37.8 + seconds * 1e-4)
                        .put("name", "EVER GIVEN")
                        .put("sog", sog);
        return destination == null ? builder.build() : builder.put("destination", destination).build();
    }

    @Test
    @DisplayName("complete snapshots with moving kinematics pass")
    void completeSnapshotsPass() {
        assertThatCode(() -> SnapshotContract.assertStableStaticHash(
                        VESSELS, List.of(vessel(0, "OAKLAND", 10.0), vessel(10, "OAKLAND", 11.5), vessel(20, "OAKLAND", 9.0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a partial snapshot missing a significant key fails")
    void partialSnapshotFails() {
        assertThatThrownBy(() -> SnapshotContract.assertStableStaticHash(
                        VESSELS, List.of(vessel(0, "OAKLAND", 10.0), vessel(10, null, 11.5))))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("adapter rule 7");
    }
}
