package ai.foresignal.security.restrict;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ReadRestriction#intersect} (prompt R2-013): a user's data restriction on top of a session's restriction. */
class ReadRestrictionIntersectTest {

    @Test
    @DisplayName("nothing to intersect with leaves the restriction as it is")
    void noneIsIdentity() {
        ReadRestriction r = new ReadRestriction(List.of("a"), null, null, null, List.of("GET /v1/records"), true, 30);
        assertThat(r.intersect(null)).isSameAs(r);
        assertThat(r.intersect(ReadRestriction.NONE)).isSameAs(r);
    }

    @Test
    @DisplayName("sources and kinds are intersected; null on one side means the other side's set")
    void sets() {
        ReadRestriction mine = new ReadRestriction(List.of("a", "b"), null, null, null, null, false, null);
        ReadRestriction theirs = new ReadRestriction(List.of("b", "c"), List.of("k"), null, null, null, false, null);
        ReadRestriction both = mine.intersect(theirs);
        assertThat(both.sources()).containsExactly("b");
        assertThat(both.kinds()).containsExactly("k");
        assertThat(ReadRestriction.NONE.intersect(theirs).sources()).containsExactlyInAnyOrder("b", "c");
        ReadRestriction disjoint = new ReadRestriction(List.of("z"), null, null, null, null, false, null);
        assertThat(mine.intersect(disjoint).sources()).as("an empty set lets nothing through").isEmpty();
    }

    @Test
    @DisplayName("regions are intersected into an area; a one-sided region is kept as it was")
    void regions() {
        ReadRestriction mine = new ReadRestriction(null, null, List.of(0.0, 0.0, 10.0, 10.0), null, null, false, null);
        ReadRestriction theirs = new ReadRestriction(null, null, List.of(5.0, 5.0, 20.0, 20.0), null, null, false,
                null);
        ReadRestriction both = mine.intersect(theirs);
        assertThat(both.bbox()).isNull();
        var e = both.regionEnvelope();
        assertThat(e.getMinX()).isEqualTo(5.0);
        assertThat(e.getMaxX()).isEqualTo(10.0);
        assertThat(e.getMinY()).isEqualTo(5.0);
        assertThat(e.getMaxY()).isEqualTo(10.0);
        assertThat(ReadRestriction.NONE.intersect(theirs).bbox()).containsExactly(5.0, 5.0, 20.0, 20.0);
        ReadRestriction far = new ReadRestriction(null, null, List.of(50.0, 50.0, 60.0, 60.0), null, null, false, null);
        assertThat(mine.intersect(far).region().isEmpty()).as("disjoint regions let nothing through").isTrue();
    }

    @Test
    @DisplayName("endpoints, rate and the private-source default stay this restriction's")
    void otherMembersKept() {
        ReadRestriction mine = new ReadRestriction(null, null, null, null, List.of("GET /v1/records"), true, 30);
        ReadRestriction theirs = new ReadRestriction(List.of("a"), null, null, null, null, false, null);
        ReadRestriction both = mine.intersect(theirs);
        assertThat(both.endpoints()).containsExactly("GET /v1/records");
        assertThat(both.maxRequestsPerMinute()).isEqualTo(30);
        assertThat(both.excludePrivate()).isTrue();
        assertThat(both.sources()).containsExactly("a");
    }
}
