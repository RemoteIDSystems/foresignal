package ai.foresignal.source.formats.aprs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link AprsPacketDedup} (prompt 082): the 30 s window, the path-free key and the bound. */
class AprsPacketDedupTest {

    private static final Instant T = Instant.parse("2026-10-14T00:30:00Z");

    @Test
    @DisplayName("a packet registered within 30 s is seen; after 30 s, or with other content, it is not")
    void window() {
        AprsPacketDedup dedup = new AprsPacketDedup();
        dedup.register("N0CALL-9", "=3745.00N/12225.00W>mobile", T);

        assertThat(dedup.seenRecently("N0CALL-9", "=3745.00N/12225.00W>mobile", T.plusSeconds(30))).isTrue();
        assertThat(dedup.seenRecently("N0CALL-9", "=3745.00N/12225.00W>mobile", T.plusSeconds(31))).isFalse();
        assertThat(dedup.seenRecently("N0CALL-9", "=3745.00N/12225.00W>moved", T.plusSeconds(1))).isFalse();
        assertThat(dedup.seenRecently("N0CALL-8", "=3745.00N/12225.00W>mobile", T.plusSeconds(1))).isFalse();
        assertThat(dedup.seenRecently(null, "x", T)).isFalse();
    }

    @Test
    @DisplayName("the key is the SHA-256 of source:info, so the path is not part of it")
    void key() {
        assertThat(AprsPacketDedup.key("N0CALL", "info")).isEqualTo(AprsRecords.sha256("N0CALL:info")).hasSize(64);
    }

    @Test
    @DisplayName("at most 50,000 keys are held; the oldest is evicted")
    void bounded() {
        AprsPacketDedup dedup = new AprsPacketDedup();
        for (int i = 0; i <= AprsPacketDedup.MAX_ENTRIES; i++) {
            dedup.register("N0CALL", "packet " + i, T);
        }

        assertThat(dedup.size()).isEqualTo(AprsPacketDedup.MAX_ENTRIES);
        assertThat(dedup.seenRecently("N0CALL", "packet 0", T)).isFalse();
        assertThat(dedup.seenRecently("N0CALL", "packet " + AprsPacketDedup.MAX_ENTRIES, T)).isTrue();
    }
}
