package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Repeater health from adverts (prompt 083): {@code ok → late → silent} at 13 h and 26 h, each transition emitted once by
 * the 15-minute re-evaluation, nothing emitted while the status holds, and a fresh advert back to {@code ok}.
 */
class RepeaterHealthTest {

    private static final Instant T = Instant.parse("2026-10-14T00:00:00Z");

    private static MeshCoreFrames.Contact repeater(Instant lastAdvert) {
        return new MeshCoreFrames.Contact("5f6a9ca3f1cc" + "0".repeat(52), MeshCoreProtocol.ADV_TYPE_REPEATER, 0, 0,
                "Synthetic Ridge Rpt", lastAdvert, null, null, null);
    }

    @Test
    @DisplayName("the thresholds: ok under 13 h, late under 26 h, silent after; null is silent")
    void thresholds() {
        RepeaterHealth health = RepeaterHealth.defaults();
        assertThat(health.status(T, T)).isEqualTo(RepeaterHealth.OK);
        assertThat(health.status(T, T.plus(Duration.ofHours(13)).minusSeconds(1))).isEqualTo(RepeaterHealth.OK);
        assertThat(health.status(T, T.plus(Duration.ofHours(13)))).isEqualTo(RepeaterHealth.LATE);
        assertThat(health.status(T, T.plus(Duration.ofHours(26)))).isEqualTo(RepeaterHealth.SILENT);
        assertThat(health.status(null, T)).isEqualTo(RepeaterHealth.SILENT);
        assertThat(new RepeaterHealth(null, null)).isEqualTo(RepeaterHealth.defaults());
        assertThatThrownBy(() -> new RepeaterHealth(Duration.ofHours(5), Duration.ofHours(4)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RepeaterHealth(Duration.ZERO, Duration.ofHours(4)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ok → late → silent each emit exactly once over a day of 15-minute re-evaluations")
    void transitionsEmitOnce() {
        MeshCoreEventMapper mapper = new MeshCoreEventMapper("meshcore-local", false, RepeaterHealth.defaults());
        NormalizedRecord first = mapper.onContact(repeater(T), T, T);
        assertThat(first.payload()).containsEntry("advertStatus", RepeaterHealth.OK);
        int late = 0;
        int silent = 0;
        int other = 0;
        for (Instant now = T; !now.isAfter(T.plus(Duration.ofHours(30))); now = now.plus(RepeaterHealth.REEVALUATE_EVERY)) {
            for (NormalizedRecord record : mapper.reevaluate(now)) {
                switch ((String) record.payload().get("advertStatus")) {
                    case RepeaterHealth.LATE -> late++;
                    case RepeaterHealth.SILENT -> silent++;
                    default -> other++;
                }
            }
        }
        assertThat(late).isEqualTo(1);
        assertThat(silent).isEqualTo(1);
        assertThat(other).isZero();
        List<NormalizedRecord> fresh = List.of(mapper.onContact(repeater(T.plus(Duration.ofHours(31))),
                T.plus(Duration.ofHours(31)), T.plus(Duration.ofHours(31))));
        assertThat(fresh.get(0).payload()).containsEntry("advertStatus", RepeaterHealth.OK);
        assertThat(mapper.reevaluate(T.plus(Duration.ofHours(31)).plusSeconds(900))).isEmpty();
    }

    @Test
    @DisplayName("other node types never get an advertStatus and are never re-emitted")
    void onlyRepeaters() {
        MeshCoreEventMapper mapper = new MeshCoreEventMapper("meshcore-local", false, RepeaterHealth.defaults());
        NormalizedRecord chat = mapper.onContact(new MeshCoreFrames.Contact("7b065359ae9c" + "0".repeat(52),
                MeshCoreProtocol.ADV_TYPE_CHAT, 0, 0, "Synthetic Hiker", T, 37.0, -122.0, null), T, T);
        assertThat(chat.payload()).doesNotContainKey("advertStatus");
        assertThat(mapper.reevaluate(T.plus(Duration.ofDays(3)))).isEmpty();
    }
}
