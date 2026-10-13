package ai.foresignal.derive;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Emit-on-change, monotonic {@code observedAt} and redundant tombstones (prompt 052). */
class EmitLedgerTest {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");

    private static NormalizedRecord record(String id, Instant at, Map<String, Object> payload) {
        return NormalizedRecord.builder(id, "place-weather").observedAt(at).putAll(payload).build();
    }

    @Test
    @DisplayName("an identical payload is skipped, whatever the number types")
    void identicalSkipped() {
        EmitLedger ledger = new EmitLedger();
        assertThat(ledger.admit(record("place:home", T, Map.of("km", 8, "status", "ok")))).isNotNull();
        assertThat(ledger.admit(record("place:home", T.plusSeconds(120), Map.of("km", 8.0, "status", "ok")))).isNull();
        assertThat(ledger.admit(record("place:home", T.plusSeconds(240), Map.of("km", 9, "status", "ok")))).isNotNull();
    }

    @Test
    @DisplayName("observedAt never goes back: an earlier frame after a tick is raised to the tick")
    void observedAtRaised() {
        EmitLedger ledger = new EmitLedger();
        ledger.admit(record("place:home", T.plusSeconds(60), Map.of("status", "stale")));
        NormalizedRecord admitted = ledger.admit(record("place:home", T, Map.of("status", "ok")));
        assertThat(admitted.observedAt()).isEqualTo(T.plusSeconds(60));
        assertThat(admitted.payload()).containsEntry("status", "ok");
    }

    @Test
    @DisplayName("tombstones are emitted once, and only for entities that exist")
    void tombstones() {
        EmitLedger ledger = new EmitLedger();
        assertThat(ledger.admit(NormalizedRecord.tombstone("place:none", "place-weather", T))).isNull();
        ledger.admit(record("place:home", T, Map.of("status", "ok")));
        assertThat(ledger.admit(NormalizedRecord.tombstone("place:home", "place-weather", T.plusSeconds(1)))).isNotNull();
        assertThat(ledger.admit(NormalizedRecord.tombstone("place:home", "place-weather", T.plusSeconds(2)))).isNull();
        assertThat(ledger.admit(record("place:home", T.plusSeconds(3), Map.of("status", "ok")))).isNotNull();
    }

    @Test
    @DisplayName("seeding from heads makes an unchanged first emit after a restart a no-op; forget retries")
    void seededFromHeads() {
        EmitLedger ledger = new EmitLedger();
        ledger.seed(List.of(new HeadSnapshot("wx-places", "place:home", "place-weather", 10, 3, T, false,
                Map.of("status", "ok", "km", 8), null)));
        assertThat(ledger.size()).isEqualTo(1);
        assertThat(ledger.admit(record("place:home", T.plusSeconds(60), Map.of("km", 8, "status", "ok")))).isNull();
        ledger.forget("place-weather", "place:home");
        assertThat(ledger.admit(record("place:home", T.plusSeconds(60), Map.of("km", 8, "status", "ok")))).isNotNull();
    }
}
