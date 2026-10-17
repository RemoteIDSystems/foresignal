package ai.foresignal.federation.compact;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link CompactOutbox} (prompt 113): priority order (critical, warning, info; newest first within a class), TTL
 * expiry, dedup by message id with a resend only on a severity change or a resolution, the per-transport byte
 * budget over a sliding window, and undoing a deferred send.
 */
class CompactOutboxTest {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    static MeshCborMessage message(int id, int type, int severity, Instant at, int expiryMinutes) {
        byte[] messageId = {0, 0, 0, 0, 0, 0, 0, (byte) id};
        return new MeshCborMessage(1, type, null, severity, "kind", null, null, at.getEpochSecond(), "event " + id,
                null, new byte[] {9, 9, 9, 9}, messageId, expiryMinutes);
    }

    static List<Integer> ids(List<CompactOutbox.Entry> entries) {
        return entries.stream().map(e -> (int) e.message().messageId()[7]).toList();
    }

    @Test
    @DisplayName("critical first, then warning, then info; newest first within a class")
    void priority() {
        CompactOutbox outbox = new CompactOutbox();
        assertThat(outbox.offer(message(1, 1, 0, NOW, 60), NOW)).isTrue();
        assertThat(outbox.offer(message(2, 1, 2, NOW, 60), NOW.plusSeconds(1))).isTrue();
        assertThat(outbox.offer(message(3, 1, 1, NOW, 60), NOW.plusSeconds(2))).isTrue();
        assertThat(outbox.offer(message(4, 1, 2, NOW, 60), NOW.plusSeconds(3))).isTrue();
        assertThat(outbox.offer(message(5, 5, 0, NOW, 60), NOW.plusSeconds(4))).isTrue();
        assertThat(ids(outbox.pending(NOW.plusSeconds(5)))).containsExactly(4, 2, 3, 5, 1);
        assertThat(ids(outbox.since(NOW.plusSeconds(3), NOW.plusSeconds(5)))).containsExactly(4, 5);
        assertThat(outbox.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("a message expires at key 7 plus key 12 and is dropped; an expired one is never queued")
    void ttl() {
        CompactOutbox outbox = new CompactOutbox();
        outbox.offer(message(1, 1, 1, NOW, 10), NOW);
        outbox.offer(message(2, 1, 1, NOW, 60), NOW);
        assertThat(ids(outbox.pending(NOW.plus(Duration.ofMinutes(9))))).containsExactlyInAnyOrder(1, 2);
        assertThat(ids(outbox.pending(NOW.plus(Duration.ofMinutes(10))))).containsExactly(2);
        assertThat(outbox.offer(message(3, 1, 1, NOW.minus(Duration.ofHours(2)), 60), NOW)).isFalse();
        assertThat(outbox.offer(new MeshCborMessage(1, 1, null, 1, null, null, null, NOW.getEpochSecond(), null, null),
                NOW)).as("no message id").isFalse();
    }

    @Test
    @DisplayName("dedup by message id: a repeat is ignored; a severity change or a resolution is resent")
    void dedup() {
        CompactOutbox outbox = new CompactOutbox();
        String transport = "mesh:test";
        Duration hour = Duration.ofHours(1);
        assertThat(outbox.offer(message(1, 1, 1, NOW, 60), NOW)).isTrue();
        assertThat(ids(outbox.take(transport, 10_000, hour, NOW))).containsExactly(1);
        assertThat(outbox.offer(message(1, 1, 1, NOW, 60), NOW.plusSeconds(5))).isFalse();
        assertThat(outbox.take(transport, 10_000, hour, NOW.plusSeconds(5))).isEmpty();

        assertThat(outbox.offer(message(1, 1, 2, NOW, 60), NOW.plusSeconds(10))).as("severity change").isTrue();
        List<CompactOutbox.Entry> resent = outbox.take(transport, 10_000, hour, NOW.plusSeconds(10));
        assertThat(ids(resent)).containsExactly(1);
        assertThat(resent.getFirst().message().severity()).isEqualTo(2);

        assertThat(outbox.offer(message(1, MeshCborProfile.TYPE_RESOLVED, 2, NOW, 60), NOW.plusSeconds(20)))
                .as("resolution").isTrue();
        List<CompactOutbox.Entry> resolved = outbox.take(transport, 10_000, hour, NOW.plusSeconds(20));
        assertThat(resolved).singleElement()
                .satisfies(e -> assertThat(e.message().type()).isEqualTo(MeshCborProfile.TYPE_RESOLVED));
        assertThat(outbox.offer(message(1, MeshCborProfile.TYPE_RESOLVED, 2, NOW, 60), NOW.plusSeconds(30)))
                .as("a repeated resolution").isFalse();
        assertThat(outbox.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("the byte budget per window: only what fits, highest priority first; the window slides")
    void budget() {
        CompactOutbox outbox = new CompactOutbox();
        for (int i = 1; i <= 5; i++) {
            outbox.offer(message(i, 1, i == 3 ? 2 : 0, NOW, 600), NOW.plusSeconds(i));
        }
        int size = outbox.pending(NOW.plusSeconds(10)).getFirst().encoded().length;
        Duration hour = Duration.ofHours(1);
        List<CompactOutbox.Entry> first = outbox.take("mesh:a", size * 2, hour, NOW.plusSeconds(10));
        assertThat(ids(first)).containsExactly(3, 5);
        assertThat(outbox.take("mesh:a", size * 2, hour, NOW.plusSeconds(20))).as("budget spent").isEmpty();
        assertThat(ids(outbox.take("mesh:b", size, hour, NOW.plusSeconds(20)))).as("per transport").containsExactly(3);
        List<CompactOutbox.Entry> later = outbox.take("mesh:a", size * 2, hour, NOW.plusSeconds(11).plus(hour));
        assertThat(ids(later)).as("the window slid").containsExactly(4, 2);
    }

    @Test
    @DisplayName("a deferred send is undone: the message and its bytes return to the transport")
    void unsend() {
        CompactOutbox outbox = new CompactOutbox();
        outbox.offer(message(1, 1, 1, NOW, 60), NOW);
        int size = outbox.pending(NOW).getFirst().encoded().length;
        List<CompactOutbox.Entry> taken = outbox.take("mesh:a", size, Duration.ofHours(1), NOW);
        assertThat(taken).hasSize(1);
        outbox.unsend("mesh:a", taken.getFirst());
        assertThat(ids(outbox.take("mesh:a", size, Duration.ofHours(1), NOW.plusSeconds(1)))).containsExactly(1);
    }

    @Test
    @DisplayName("over capacity, the lowest-priority oldest message is evicted")
    void capacity() {
        CompactOutbox outbox = new CompactOutbox(2);
        outbox.offer(message(1, 1, 0, NOW, 60), NOW);
        outbox.offer(message(2, 1, 2, NOW, 60), NOW.plusSeconds(1));
        outbox.offer(message(3, 1, 0, NOW, 60), NOW.plusSeconds(2));
        assertThat(ids(outbox.pending(NOW.plusSeconds(3)))).containsExactly(2, 3);
        outbox.clear();
        assertThat(outbox.size()).isZero();
    }
}
