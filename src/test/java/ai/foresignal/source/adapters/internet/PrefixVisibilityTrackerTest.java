package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link PrefixVisibilityTracker} (prompt 092): announce/withdraw/peer-down, the origin as the last AS, AS_SET,
 * unexpected origin, more-specifics and the no-data status.
 */
class PrefixVisibilityTrackerTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    @Test
    @DisplayName("visibility is the banded share of peers announcing, and a withdrawal lowers it")
    void visibility() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 13335", NOW.minusSeconds(60));
        tracker.announce("rrc01", "64501", "64501 13335", NOW.minusSeconds(60));
        assertThat(tracker.snapshot(Set.of(13335), NOW, false).visibility()).isEqualTo(1.0);

        tracker.withdraw("rrc01", "64501", NOW);
        assertThat(tracker.snapshot(Set.of(13335), NOW, false).visibility()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("a down peer leaves the denominator; a stale peer ages out; a peer that never carried it is ignored")
    void peerState() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 13335", NOW.minusSeconds(60));
        tracker.withdraw("rrc01", "64501", NOW.minusSeconds(60));
        assertThat(tracker.snapshot(Set.of(13335), NOW, false).visibility()).isEqualTo(0.5);
        tracker.peerState("rrc01", "64501", true, NOW);
        assertThat(tracker.snapshot(Set.of(13335), NOW, false).visibility()).isEqualTo(1.0);
        tracker.peerState("rrc02", "64999", false, NOW);
        assertThat(tracker.snapshot(Set.of(13335), NOW, false).visibility()).as("an unrelated peer").isEqualTo(1.0);

        PrefixVisibilityTracker stale = new PrefixVisibilityTracker("1.1.1.0/24");
        stale.announce("rrc00", "64500", "64500 13335", NOW.minus(PrefixVisibilityTracker.RECENT).minusSeconds(1));
        stale.withdraw("rrc01", "64501", NOW.minusSeconds(60));
        assertThat(stale.snapshot(null, NOW, false).visibility()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("while disconnected the status is no-data and the last visibility is kept")
    void noData() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 13335", NOW.minusSeconds(60));
        PrefixVisibilityTracker.Snapshot down = tracker.snapshot(Set.of(13335), NOW, true);
        assertThat(down.status()).isEqualTo("no-data");
        assertThat(down.visibility()).isEqualTo(1.0);
        assertThat(tracker.snapshot(Set.of(13335), NOW, false).status()).isEqualTo("live");
    }

    @Test
    @DisplayName("the origin is the last AS of the path; transit ASes are not origins")
    void originIsLastAs() {
        assertThat(PrefixVisibilityTracker.originsOf("64500 3356 13335"))
                .containsExactly(new PrefixVisibilityTracker.Origin(13335, false));
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 3356 13335", NOW.minusSeconds(60));
        PrefixVisibilityTracker.Snapshot snapshot = tracker.snapshot(Set.of(13335), NOW, false);
        assertThat(snapshot.unexpectedOrigin()).as("transit 64500 and 3356 are not origins").isFalse();
        assertThat(snapshot.origins()).containsExactly(new PrefixVisibilityTracker.Origin(13335, false));
    }

    @Test
    @DisplayName("an unexpected origin is flagged; a trailing AS_SET is flattened and flagged")
    void origins() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announce("rrc00", "64500", "64500 64599", NOW.minusSeconds(60));
        PrefixVisibilityTracker.Snapshot snapshot = tracker.snapshot(Set.of(13335), NOW, false);
        assertThat(snapshot.unexpectedOrigin()).isTrue();
        assertThat(snapshot.origins()).containsExactly(new PrefixVisibilityTracker.Origin(64599, false));
        assertThat(tracker.snapshot(null, NOW, false).unexpectedOrigin()).as("no expectation, no flag").isFalse();

        assertThat(PrefixVisibilityTracker.originsOf("64500 {64501,64502}")).containsExactly(
                new PrefixVisibilityTracker.Origin(64501, true), new PrefixVisibilityTracker.Origin(64502, true));
        assertThat(PrefixVisibilityTracker.originsOf("64500 {64501,64502} 13335"))
                .as("an AS_SET in transit is not the origin")
                .containsExactly(new PrefixVisibilityTracker.Origin(13335, false));
        PrefixVisibilityTracker asSet = new PrefixVisibilityTracker("1.1.1.0/24");
        asSet.announce("rrc00", "64500", "64500 {64501,13335}", NOW.minusSeconds(60));
        assertThat(asSet.snapshot(Set.of(13335), NOW, false).unexpectedOrigin())
                .as("an AS_SET holding an expected origin").isFalse();
    }

    @Test
    @DisplayName("more-specifics are those announced under the prefix, dropped on withdrawal, capped at 20")
    void moreSpecifics() {
        PrefixVisibilityTracker tracker = new PrefixVisibilityTracker("1.1.1.0/24");
        tracker.announceMoreSpecific("1.1.1.128/25", "rrc00", "64500", NOW.minusSeconds(60));
        assertThat(tracker.snapshot(null, NOW, false).moreSpecifics()).containsExactly("1.1.1.128/25");
        tracker.withdrawMoreSpecific("1.1.1.128/25", "rrc00", "64500");
        assertThat(tracker.snapshot(null, NOW, false).moreSpecifics()).isEmpty();
        for (int i = 0; i < 30; i++) {
            tracker.announceMoreSpecific("1.1.1." + i + "/32", "rrc00", "64500", NOW);
        }
        assertThat(tracker.snapshot(null, NOW, false).moreSpecifics()).hasSize(20);
        assertThat(List.copyOf(tracker.snapshot(null, NOW, false).moreSpecifics())).isSorted();
    }
}
