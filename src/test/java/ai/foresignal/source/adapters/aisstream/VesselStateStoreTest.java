package ai.foresignal.source.adapters.aisstream;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link VesselStateStore}: per-MMSI merging into complete snapshots (prompt 063, adapter rule 7).
 */
class VesselStateStoreTest {

    private static final Instant T0 = Instant.parse("2026-10-13T12:00:00Z");

    private MutableClock clock;
    private VesselStateStore store;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(T0);
        store = new VesselStateStore(clock);
    }

    private static List<AisMessage> messages(String fixture) {
        return AdapterFixtures.text(AisStreamAdapter.ID, fixture).lines()
                .filter(l -> !l.isBlank())
                .map(AisStreamMessageParser::parse)
                .filter(AisMessage.class::isInstance)
                .map(AisMessage.class::cast)
                .toList();
    }

    private static AisMessage position(String mmsi, Instant time, double lat, double lon, Double sog, Double cog) {
        return new AisMessage("PositionReport", mmsi, time, "A", true, lat, lon, sog, cog, 90, 0,
                null, null, null, null, null, null, null, null);
    }

    private static AisMessage staticData(String mmsi, Instant time, String name, String destination) {
        return new AisMessage("ShipStaticData", mmsi, time, "A", false, null, null, null, null, null, null,
                name, "CALL1", 9064748, 70, new AisMessage.Dimension(100, 20, 10, 10),
                new AisMessage.Eta(12, 24, 6, 0), 6.5, destination);
    }

    private static Object payload(NormalizedRecord record, String key) {
        return record.payload().get(key);
    }

    @Test
    @DisplayName("static data before any position is held; the first position emits the complete snapshot")
    void staticThenPosition() {
        List<AisMessage> fixture = messages("static-before-position.jsonl");

        assertThat(store.merge(fixture.get(0))).isEmpty();
        NormalizedRecord record = store.merge(fixture.get(1)).orElseThrow();

        assertThat(record.externalId()).isEqualTo("235200001");
        assertThat(record.kind()).isEqualTo(AisStreamAdapter.KIND);
        assertThat(payload(record, "name")).isEqualTo("LATE FIX");
        assertThat(payload(record, "callsign")).isEqualTo("LFX1");
        assertThat(payload(record, "imo")).isEqualTo(9123456);
        assertThat(payload(record, "shipType")).isEqualTo(70);
        assertThat(payload(record, "destination")).isEqualTo("LE HAVRE");
        assertThat(payload(record, "draught")).isEqualTo(7.5);
        assertThat(payload(record, "lengthM")).isEqualTo(150);
        assertThat(payload(record, "beamM")).isEqualTo(25);
        assertThat(payload(record, "eta")).as("next 2 January 03:04 after the static message")
                .isEqualTo("2027-01-02T03:04:00Z");
        assertThat(payload(record, "sog")).isEqualTo(10.0);
        assertThat(payload(record, "class")).isEqualTo("A");
        assertThat(payload(record, "mmsi")).isEqualTo("235200001");
        assertThat(((Point) record.geometry()).getY()).isEqualTo(50.70);
        assertThat(record.quality().positionSource()).isEqualTo(PositionSource.REPORTED);
        assertThat(record.quality().extra()).containsEntry(VesselStateStore.POSITION_AGE_KEY, 0L);
    }

    @Test
    @DisplayName("position then static: both snapshots are complete, the second with the static fields")
    void positionThenStatic() {
        NormalizedRecord first = store.merge(position("235000001", T0, 50.0, -1.0, 5.0, 90.0)).orElseThrow();
        NormalizedRecord second = store.merge(staticData("235000001", T0.plusSeconds(10), "ALPHA", "COWES")).orElseThrow();

        assertThat(first.payload()).doesNotContainKey("name");
        assertThat(second.payload()).containsEntry("name", "ALPHA").containsEntry("destination", "COWES")
                .containsEntry("sog", 5.0).containsEntry("cog", 90.0);
        assertThat(second.geometry()).as("the static message keeps the merged position").isNotNull();
        assertThat(second.observedAt()).isEqualTo(T0.plusSeconds(10));
        assertThat(second.quality().extra()).containsEntry(VesselStateStore.POSITION_AGE_KEY, 10L);
    }

    @Test
    @DisplayName("Class B parts A and B merge into one snapshot with the name, type, call sign and dimensions")
    void classBParts() {
        List<NormalizedRecord> out = new ArrayList<>();
        for (AisMessage m : messages("classb-parts.jsonl")) {
            store.merge(m).ifPresent(out::add);
        }

        NormalizedRecord last = out.get(out.size() - 1);
        assertThat(out).hasSize(3);
        assertThat(last.payload()).containsEntry("name", "PART A NAME").containsEntry("shipType", 36)
                .containsEntry("callsign", "PARTB").containsEntry("lengthM", 9).containsEntry("beamM", 3)
                .containsEntry("class", "B").doesNotContainKey("navStatus").doesNotContainKey("heading");
        assertThat(out.get(1).payload()).containsEntry("name", "PART A NAME").doesNotContainKey("callsign");
    }

    @Test
    @DisplayName("observedAt is the newest merged message, even when an older one arrives late")
    void observedAtIsNewest() {
        store.merge(position("235000002", T0.plusSeconds(30), 50.0, -1.0, 5.0, 90.0));
        NormalizedRecord late = store.merge(position("235000002", T0, 49.0, -2.0, 1.0, 10.0)).orElseThrow();

        assertThat(late.observedAt()).isEqualTo(T0.plusSeconds(30));
        assertThat(((Point) late.geometry()).getY()).as("a late position does not move the vessel back")
                .isEqualTo(50.0);
        assertThat(late.payload()).containsEntry("sog", 5.0);
    }

    @Test
    @DisplayName("a sentinel position after a fix emits null geometry, so the TRACK carry-forward applies")
    void sentinelAfterFix() {
        store.merge(position("235000003", T0, 50.0, -1.0, 5.0, 90.0));
        AisMessage sentinel = new AisMessage("PositionReport", "235000003", T0.plusSeconds(60), "A", true, null, null,
                4.0, null, null, null, null, null, null, null, null, null, null, null);

        NormalizedRecord record = store.merge(sentinel).orElseThrow();

        assertThat(record.geometry()).isNull();
        assertThat(record.payload()).containsEntry("sog", 4.0).doesNotContainKey("cog");
        assertThat(record.quality().extra()).containsEntry(VesselStateStore.POSITION_AGE_KEY, 60L);
    }

    @Test
    @DisplayName("a vessel whose only positions were sentinels is held")
    void neverAFix() {
        AisMessage sentinel = messages("sentinels.jsonl").get(0);

        assertThat(store.merge(sentinel)).isEmpty();
        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("a ShipStaticData without a destination clears it; a part A without a call sign does not")
    void clearing() {
        store.merge(position("235000004", T0, 50.0, -1.0, 5.0, 90.0));
        store.merge(staticData("235000004", T0.plusSeconds(1), "BETA", "RYDE"));
        AisMessage cleared = new AisMessage("ShipStaticData", "235000004", T0.plusSeconds(2), "A", false, null, null,
                null, null, null, null, "BETA", null, null, null, null, null, null, null);

        NormalizedRecord record = store.merge(cleared).orElseThrow();

        assertThat(record.payload()).doesNotContainKey("destination").doesNotContainKey("eta")
                .doesNotContainKey("imo").containsEntry("callsign", "CALL1").containsEntry("shipType", 70);
    }

    @Test
    @DisplayName("a vessel unseen for 6 h is evicted")
    void idleEviction() {
        store.merge(position("235000005", T0, 50.0, -1.0, 5.0, 90.0));
        clock.advance(Duration.ofHours(5));
        store.merge(position("235000006", T0, 50.0, -1.0, 5.0, 90.0));
        clock.advance(Duration.ofHours(1).plusSeconds(1));

        store.merge(position("235000007", T0, 50.0, -1.0, 5.0, 90.0));

        assertThat(store.contains("235000005")).isFalse();
        assertThat(store.contains("235000006")).isTrue();
        assertThat(store.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("the store is bounded: the vessel unseen longest goes first")
    void bounded() {
        VesselStateStore small = new VesselStateStore(clock, 3, Duration.ofHours(6));
        for (int i = 1; i <= 3; i++) {
            small.merge(position("23500001" + i, T0, 50.0, -1.0, 5.0, 90.0));
            clock.advance(Duration.ofSeconds(1));
        }
        small.merge(position("235000011", T0.plusSeconds(5), 50.0, -1.0, 5.0, 90.0));

        small.merge(position("235000019", T0, 50.0, -1.0, 5.0, 90.0));

        assertThat(small.size()).isEqualTo(3);
        assertThat(small.contains("235000012")).as("the eldest by last update").isFalse();
        assertThat(small.contains("235000011")).isTrue();
        assertThat(small.contains("235000019")).isTrue();
    }

    @Test
    @DisplayName("position-only updates keep one static hash (SnapshotContract)")
    void stableStaticHash() {
        store.merge(staticData("235000008", T0, "GAMMA", "PORTSMOUTH"));
        List<NormalizedRecord> snapshots = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Optional<NormalizedRecord> record = store.merge(position("235000008", T0.plusSeconds(30L * (i + 1)),
                    50.0 + i * 0.01, -1.0, 5.0 + i, 90.0 + i * 5));
            record.ifPresent(snapshots::add);
        }

        assertThat(snapshots).hasSize(4);
        SnapshotContract.assertStableStaticHash(new AisStreamAdapter("", new io.micrometer.core.instrument.simple
                .SimpleMeterRegistry(), clock).descriptor(), snapshots);
    }

    @Test
    @DisplayName("every payload key of every fixture is declared in fieldSpecs")
    void fieldSpecs() {
        List<NormalizedRecord> records = new ArrayList<>();
        for (String fixture : List.of("frames.jsonl", "sentinels.jsonl", "static-before-position.jsonl",
                "classb-parts.jsonl")) {
            for (AisMessage m : messages(fixture)) {
                store.merge(m).ifPresent(records::add);
            }
        }

        assertThat(records).isNotEmpty();
        FieldSpecContract.assertPayloadKeysDeclared(
                new AisStreamAdapter("", new io.micrometer.core.instrument.simple.SimpleMeterRegistry(), clock)
                        .descriptor(),
                records);
    }

    @Test
    @DisplayName("the ETA is the next occurrence; an impossible date is omitted")
    void nextEta() {
        Instant reference = Instant.parse("2026-10-13T12:00:00Z");

        assertThat(VesselStateStore.nextEta(new AisMessage.Eta(10, 13, 12, 0), reference))
                .isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(VesselStateStore.nextEta(new AisMessage.Eta(10, 13, 11, 59), reference))
                .isEqualTo(Instant.parse("2027-10-13T11:59:00Z"));
        assertThat(VesselStateStore.nextEta(new AisMessage.Eta(2, 29, 0, 0), reference))
                .as("neither 2026 nor 2027 has a 29 February").isNull();
        assertThat(VesselStateStore.nextEta(new AisMessage.Eta(2, 29, 0, 0), Instant.parse("2027-03-01T00:00:00Z")))
                .isEqualTo(Instant.parse("2028-02-29T00:00:00Z"));
        assertThat(VesselStateStore.nextEta(new AisMessage.Eta(2, 30, 0, 0), reference)).isNull();
    }
}
