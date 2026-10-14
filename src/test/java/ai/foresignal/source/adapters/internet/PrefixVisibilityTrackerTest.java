package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link PrefixVisibilityTracker} (prompt 092): announce/withdraw/peer-down, AS_SET, unexpected origin. */
class PrefixVisibilityTrackerTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final List<String> WATCHED = List.of("1.1.1.0/24", "1.1.1.128/25");

    @Test
    @DisplayName("visibility is the banded share of peers announcing, and a withdrawal lowers it")
    void visibility() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 13335", NOW.minusSeconds(60));
        tracker.announce("rrc01", "64501", "64501 13335", NOW.minusSeconds(60));
        assertThat(tracker.snapshot(Set.of(13335), WATCHED, NOW, false).visibility()).isEqualTo(1.0);

        tracker.withdraw("rrc01", "64501", NOW);
        assertThat(tracker.snapshot(Set.of(13335), WATCHED, NOW, false).visibility()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("a down peer leaves the denominator; a stale peer ages out")
    void peerState() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 13335", NOW.minusSeconds(60));
        tracker.announce("rrc01", "64501", "64501 13335", NOW.minusSeconds(60));
        tracker.peerState("rrc01", "64501", true, NOW);
        assertThat(tracker.snapshot(Set.of(13335), WATCHED, NOW, false).visibility()).isEqualTo(1.0);
        assertThat(tracker.snapshot(null, WATCHED, NOW, true).status()).isEqualTo("no-data");
    }

    @Test
    @DisplayName("an unexpected origin is flagged; AS_SET members are flattened and flagged; more-specifics listed")
    void origins() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 64599", NOW.minusSeconds(60));
        PrefixVisibilityTracker.Snapshot snapshot = tracker.snapshot(Set.of(13335), WATCHED, NOW, false);
        assertThat(snapshot.unexpectedOrigin()).isTrue();
        assertThat(snapshot.origins()).anyMatch(o -> o.asn() == 64599 && !o.asSet());
        assertThat(snapshot.moreSpecifics()).contains("1.1.1.128/25");

        PrefixVisibilityTracker asSet = new PrefixVisibilityTracker("1.1.1.0/24");
        asSet.announce("rrc00", "64500", "64500 {64501,64502}", NOW.minusSeconds(60));
        assertThat(asSet.snapshot(null, WATCHED, NOW, false).origins())
                .anyMatch(o -> o.asSet() && (o.asn() == 64501 || o.asn() == 64502));
    }
}
