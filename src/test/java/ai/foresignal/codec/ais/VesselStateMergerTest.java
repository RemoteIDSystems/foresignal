package ai.foresignal.codec.ais;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The shared per-MMSI merge (prompt 085): static plus position into one snapshot with prompt 063's payload keys, the
 * static hash stable across repeated positions, held static data, quality, observers, time bases, bounds and MMSI
 * validation.
 */
class VesselStateMergerTest {

    private static final Instant T = Instant.parse("2026-10-13T13:20:00Z");
    private static final Observer STATION = Observer.of("2573485");

    private static AisMessage.ClassAPosition position(int mmsi, double lat, double lon, double sog) {
        return new AisMessage.ClassAPosition(1, 0, mmsi, 0, 0, sog, true, lon, lat, 45.0, 44, 0, 0, false, 0);
    }

    /** A descriptor with the vessel defaults, for the contracts. */
    private static SourceDescriptor descriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder("vessel-test").name("vessel test").kind("vessel")
                .mode(Mode.STREAMING).observedAtMode(ObservedAtMode.UPSTREAM);
        VesselDescriptors.apply(b);
        return b.build();
    }

    @Test
    @DisplayName("static data before a position is held; the first position emits a complete snapshot with 063's keys")
    void staticThenPosition() {
        VesselStateMerger merger = new VesselStateMerger(true);

        assertThat(merger.merge(AisGoldens.decode("type5"), T, STATION)).isEmpty();
        NormalizedRecord r = merger.merge(position(257_123_450, 59.9, 10.5, 12.3), T.plusSeconds(10), STATION)
                .orElseThrow();

        assertThat(r.externalId()).isEqualTo("257123450");
        assertThat(r.kind()).isEqualTo("vessel");
        assertThat(r.observedAt()).isEqualTo(T.plusSeconds(10));
        assertThat(r.payload()).containsEntry("mmsi", "257123450").containsEntry("name", "NORDIC STAR")
                .containsEntry("callsign", "LAXY7").containsEntry("imo", 9_134_270).containsEntry("shipType", 70)
                .containsEntry("class", "A").containsEntry("destination", "BERGEN").containsEntry("navStatus", 0)
                .containsEntry("draught", 8.4).containsEntry("lengthM", 150).containsEntry("beamM", 22)
                .containsEntry("eta", "2026-10-14T21:30:00Z").containsEntry("sog", 12.3).containsEntry("cog", 45.0)
                .containsEntry("heading", 44);
        assertThat(r.payload()).doesNotContainKeys("rot", "posAcc", "accuracy");
        assertThat(r.quality().positionSource()).isEqualTo(PositionSource.REPORTED);
        assertThat(r.quality().accuracyMeters()).isEqualTo(VesselStateMerger.ACCURACY_HIGH_M);
        assertThat(r.quality().extra()).containsEntry(VesselStateMerger.ROT_KEY, 0)
                .containsEntry(VesselStateMerger.POS_ACC_KEY, true);
        assertThat(r.observers()).containsExactly(STATION);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), List.of(r));
    }

    @Test
    @DisplayName("repeated positions keep one static hash (adapter rule 7)")
    void stableStaticHash() {
        VesselStateMerger merger = new VesselStateMerger(true);
        merger.merge(AisGoldens.decode("type5"), T, null);
        List<NormalizedRecord> snapshots = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            merger.merge(position(257_123_450, 59.9 + i * 0.001, 10.5, 12.3 + i), T.plusSeconds(10L * (i + 1)), null)
                    .ifPresent(snapshots::add);
        }
        assertThat(snapshots).hasSize(5);
        SnapshotContract.assertStableStaticHash(descriptor(), snapshots);
    }

    @Test
    @DisplayName("Class B: 24A and 24B parts merge without erasing each other; 18 sets class B and no navStatus")
    void classB() {
        VesselStateMerger merger = new VesselStateMerger(true);
        merger.merge(AisGoldens.decode("type18"), T, null);
        merger.merge(AisGoldens.decode("type24a"), T.plusSeconds(1), null);
        NormalizedRecord r = merger.merge(AisGoldens.decode("type24b"), T.plusSeconds(2), null).orElseThrow();

        assertThat(r.payload()).containsEntry("name", "LITTLE WAVE").containsEntry("callsign", "LH2345")
                .containsEntry("shipType", 36).containsEntry("lengthM", 9).containsEntry("beamM", 3)
                .containsEntry("class", "B").doesNotContainKey("navStatus").doesNotContainKey("heading");
        assertThat(r.quality().extra()).doesNotContainKey(VesselStateMerger.ROT_KEY);
    }

    @Test
    @DisplayName("type 19 carries position and identity in one message")
    void type19() {
        NormalizedRecord r = new VesselStateMerger(true).merge(AisGoldens.decode("type19"), T, null).orElseThrow();
        assertThat(r.payload()).containsEntry("name", "SEA BREEZE").containsEntry("shipType", 37)
                .containsEntry("lengthM", 12).containsEntry("class", "B");
        assertThat(r.quality().accuracyMeters()).isEqualTo(VesselStateMerger.ACCURACY_LOW_M);
    }

    @Test
    @DisplayName("type 27 is accurate to 185 m; its class is kept from earlier messages")
    void longRange() {
        VesselStateMerger merger = new VesselStateMerger(true);
        NormalizedRecord r = merger.merge(AisGoldens.decode("type27"), T, null).orElseThrow();
        assertThat(r.quality().accuracyMeters()).isEqualTo(VesselStateMerger.ACCURACY_LONG_RANGE_M);
        assertThat(r.payload()).doesNotContainKey("class").containsEntry("sog", 12.0);
    }

    @Test
    @DisplayName("a sentinel position after a fix emits null geometry; a vessel that never had a fix is held")
    void sentinelPositions() {
        VesselStateMerger merger = new VesselStateMerger(true);
        assertThat(merger.merge(AisGoldens.decode("type1-sentinels"), T, null)).isEmpty();

        merger.merge(position(257_123_451, 60.0, 10.0, 1.0), T.plusSeconds(1), null);
        NormalizedRecord r = merger.merge(AisGoldens.decode("type1-sentinels"), T.plusSeconds(2), null).orElseThrow();
        assertThat(r.geometry()).isNull();
        assertThat(r.payload()).doesNotContainKeys("sog", "cog", "heading", "navStatus");
    }

    @Test
    @DisplayName("late messages never roll the state back; observedAt is the newest time merged")
    void noRollback() {
        VesselStateMerger merger = new VesselStateMerger(true);
        merger.merge(position(257_123_450, 60.0, 10.0, 5.0), T.plusSeconds(20), null);
        NormalizedRecord r = merger.merge(position(257_123_450, 61.0, 11.0, 6.0), T.plusSeconds(10), null)
                .orElseThrow();
        assertThat(r.observedAt()).isEqualTo(T.plusSeconds(20));
        assertThat(r.payload()).containsEntry("sog", 5.0);
    }

    @Test
    @DisplayName("a RECEIVED merger leaves observedAt to the framework")
    void receivedTime() {
        NormalizedRecord r = new VesselStateMerger(false).merge(position(257_123_450, 60.0, 10.0, 5.0), T, null)
                .orElseThrow();
        assertThat(r.observedAt()).isNull();
    }

    @Test
    @DisplayName("MMSI 0 and values outside 100000000-999999999 are refused and counted; base stations are not vessels")
    void mmsi() {
        VesselStateMerger merger = new VesselStateMerger(true);
        assertThat(merger.merge(position(0, 60.0, 10.0, 1.0), T, null)).isEmpty();
        assertThat(merger.merge(position(99_999_999, 60.0, 10.0, 1.0), T, null)).isEmpty();
        assertThat(merger.rejectedMmsi()).isEqualTo(2);
        assertThat(merger.merge(AisGoldens.decode("type4"), T, null)).isEmpty();
        assertThat(merger.rejectedMmsi()).as("a base station is not counted as an invalid vessel").isEqualTo(2);
        assertThat(VesselStateMerger.validMmsi(100_000_000)).isTrue();
        assertThat(VesselStateMerger.validMmsi(999_999_999)).isTrue();
        assertThat(VesselStateMerger.validMmsi(1_000_000_000L)).isFalse();
        assertThat(merger.merge(new VesselUpdate.Position("12", T, null, 60.0, 10.0, null, null, null, null, null,
                null, false), null, T)).isEmpty();
    }

    @Test
    @DisplayName("bounded: the least recently updated vessel is evicted, and one idle for 6 h is forgotten")
    void bounds() {
        VesselStateMerger merger = new VesselStateMerger(true, 2, Duration.ofHours(6));
        merger.merge(position(257_000_001, 60.0, 10.0, 1.0), T, null);
        merger.merge(position(257_000_002, 60.0, 10.0, 1.0), T, null);
        merger.merge(position(257_000_003, 60.0, 10.0, 1.0), T, null);
        assertThat(merger.size()).isEqualTo(2);
        assertThat(merger.contains("257000001")).isFalse();

        merger.merge(position(257_000_004, 60.0, 10.0, 1.0), T.plus(Duration.ofHours(7)), null);
        assertThat(merger.size()).isEqualTo(1);
        assertThat(VesselStateMerger.DEFAULT_MAX_ENTRIES).isEqualTo(100_000);
        assertThat(VesselStateMerger.DEFAULT_TTL).isEqualTo(Duration.ofHours(6));
    }

    @Test
    @DisplayName("seeding stores static data silently; the next position carries it")
    void seed() {
        VesselStateMerger merger = new VesselStateMerger(true);
        assertThat(merger.seed(new VesselUpdate.Static("230018360", T, null, true, "RESCUE KOTKA", "OJ7975", null,
                51, AisMessage.Dimension.ofOrNull(10, 7, 2, 2), null, 0.8, "SAR"), T)).isTrue();
        Optional<NormalizedRecord> r = merger.merge(new VesselUpdate.Position("230018360", T.plusSeconds(5), null,
                60.4, 26.9, 0.0, null, null, 0, null, false, false), null, T.plusSeconds(5));
        assertThat(r).hasValueSatisfying(x -> assertThat(x.payload()).containsEntry("name", "RESCUE KOTKA")
                .containsEntry("lengthM", 17).containsEntry("beamM", 4).containsEntry("destination", "SAR"));
        assertThat(merger.seed(new VesselUpdate.Static("1", T, null, true, null, null, null, null, null, null, null,
                null), T)).isFalse();
    }

    @Test
    @DisplayName("the ETA is the next occurrence at or after the message time; an impossible date is omitted")
    void eta() {
        assertThat(VesselStateMerger.nextEta(new AisMessage.Eta(1, 2, 3, 4), T))
                .isEqualTo(Instant.parse("2027-01-02T03:04:00Z"));
        assertThat(VesselStateMerger.nextEta(new AisMessage.Eta(2, 30, 0, 0), T)).isNull();
    }
}
