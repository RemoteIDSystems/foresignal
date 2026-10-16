package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * STATE_MIRROR bookkeeping (prompt 110, Core-Data-Model §10.2) through {@link MirrorState}: out-of-order updates
 * are ignored by {@code observedAt}, expiry removes at {@code validUntil} unless replaced, tombstones and records
 * that stop passing remove only what was mirrored, and a resync forgets everything.
 */
class StateMirrorTest {

    static final Instant T = Instant.parse("2026-10-16T12:00:00Z");

    static SinkRecord record(String id, Instant observedAt, Instant validUntil, boolean tombstone) {
        return new SinkRecord("src", id, "aircraft", 0L, 1, observedAt, validUntil, tombstone, null, Map.of(), null,
                null);
    }

    @Test
    @DisplayName("an update older than the mirrored observedAt is stale; equal or newer is applied")
    void lastWriterWins() {
        MirrorState mirror = new MirrorState();
        assertThat(mirror.apply(record("a", T, null, false), true)).isEqualTo(MirrorState.Action.UPSERT);
        assertThat(mirror.apply(record("a", T.minusSeconds(5), null, false), true))
                .isEqualTo(MirrorState.Action.STALE);
        assertThat(mirror.apply(record("a", T, null, false), true)).isEqualTo(MirrorState.Action.UPSERT);
        assertThat(mirror.apply(record("a", T.plusSeconds(5), null, false), true))
                .isEqualTo(MirrorState.Action.UPSERT);
        assertThat(mirror.apply(record("a", null, null, false), true)).as("no observedAt keeps the stamp")
                .isEqualTo(MirrorState.Action.UPSERT);
        assertThat(mirror.apply(record("a", T.plusSeconds(1), null, false), true))
                .as("still stamped T+5").isEqualTo(MirrorState.Action.STALE);
    }

    @Test
    @DisplayName("a record is removed at validUntil, unless a newer update replaced it")
    void expiry() {
        MirrorState mirror = new MirrorState();
        mirror.apply(record("e", T, T.plusSeconds(60), false), true);
        mirror.apply(record("f", T, T.plusSeconds(60), false), true);
        mirror.apply(record("f", T.plusSeconds(30), T.plusSeconds(120), false), true);
        assertThat(mirror.nextExpiry()).isEqualTo(T.plusSeconds(60));
        assertThat(mirror.expire(T.plusSeconds(59))).isEmpty();
        assertThat(mirror.expire(T.plusSeconds(60))).containsExactly(new MirrorState.Removal("src", "e"));
        assertThat(mirror.contains("src", "f")).as("replaced before the first expiry").isTrue();
        assertThat(mirror.expire(T.plusSeconds(120))).containsExactly(new MirrorState.Removal("src", "f"));
        assertThat(mirror.size()).isZero();
    }

    @Test
    @DisplayName("tombstones and refused updates remove only mirrored records")
    void removal() {
        MirrorState mirror = new MirrorState();
        assertThat(mirror.apply(record("t", T, null, true), true)).as("never mirrored")
                .isEqualTo(MirrorState.Action.IGNORE);
        mirror.apply(record("t", T, null, false), true);
        assertThat(mirror.apply(record("t", T.plusSeconds(1), null, true), true))
                .isEqualTo(MirrorState.Action.REMOVE);
        assertThat(mirror.apply(record("r", T, null, false), false)).as("refused, never mirrored")
                .isEqualTo(MirrorState.Action.IGNORE);
        mirror.apply(record("r", T, null, false), true);
        assertThat(mirror.apply(record("r", T.plusSeconds(1), null, false), false)).as("left the filter")
                .isEqualTo(MirrorState.Action.REMOVE);
    }

    @Test
    @DisplayName("clear (a resync after onLiveDropped or a ChangeBus gap) forgets records and expiries")
    void resync() {
        MirrorState mirror = new MirrorState();
        mirror.apply(record("a", T, T.plusSeconds(10), false), true);
        mirror.clear();
        assertThat(mirror.size()).isZero();
        assertThat(mirror.nextExpiry()).isNull();
        assertThat(mirror.apply(record("a", T.minusSeconds(100), null, false), true))
                .as("an older snapshot row applies after a resync").isEqualTo(MirrorState.Action.UPSERT);
    }
}
