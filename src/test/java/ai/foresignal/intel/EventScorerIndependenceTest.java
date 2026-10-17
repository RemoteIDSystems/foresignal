package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.intel.EventScorer.MemberView;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code event-scorer} 1.1.0 (prompt 116, EVD-1): members are collapsed by evidence group before families are
 * counted, so echoed reports of one originator never raise confidence or evidence quality. The 1.0.0 behavior
 * (every member counted, independence by source id) is documented here for comparison.
 */
class EventScorerIndependenceTest {

    private static MemberView grouped(String family, String source, double weight, String group, String root) {
        return new MemberView(family, weight, source, true, null, group, root);
    }

    @Test
    @DisplayName("the version is 1.1.0 and the method string follows it")
    void version() {
        assertThat(EventScorer.VERSION).isEqualTo("1.1.0");
        assertThat(EventScorer.PREVIOUS_VERSION).isEqualTo("1.0.0");
        assertThat(EventScorer.METHOD).isEqualTo("event-scorer-1.1.0");
    }

    @Test
    @DisplayName("twelve echoes in one evidence group score exactly like one report")
    void echoesNeverRaiseConfidence() {
        EventScorer.Score one = EventScorer.score(List.of(grouped("seismic", "gdelt", 0.6, "group:G1", "gdelt")), 0);
        List<MemberView> echoes = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            echoes.add(grouped("seismic", "gdelt", 0.6, "group:G1", "gdelt"));
        }
        EventScorer.Score twelve = EventScorer.score(echoes, 0);
        assertThat(twelve.confidence()).isCloseTo(one.confidence(), Offset.offset(1e-12));
        assertThat(twelve.evidenceQuality()).isEqualTo(one.evidenceQuality());
    }

    @Test
    @DisplayName("two members of one group contribute once, the stronger; a second family in that group adds nothing")
    void strongerMemberOfAGroupCounts() {
        EventScorer.Score score = EventScorer.score(List.of(grouped("seismic", "usgs", 0.3, "group:G1", "usgs"),
                grouped("seismic", "gdacs", 0.8, "group:G1", "usgs"),
                grouped("disaster-alert", "gdacs", 0.5, "group:G1", "usgs")), 0);
        assertThat(score.confidence()).isCloseTo(0.8 * EventScorer.DEFAULT_RELIABILITY, Offset.offset(1e-12));
        assertThat(score.evidenceQuality()).isEqualTo("weak");
    }

    @Test
    @DisplayName("strong needs ≥ 3 families from ≥ 2 independent evidence groups; one root is never strong")
    void strongNeedsIndependentGroups() {
        List<MemberView> oneRoot = List.of(grouped("ais-behaviour", "a", 0.5, "root:x", "x"),
                grouped("nav-warning", "b", 0.5, "group:G2", "x"), grouped("gnss-interference", "c", 0.5, "group:G3", "x"));
        assertThat(EventScorer.score(oneRoot, 0).evidenceQuality()).isEqualTo("moderate");
        List<MemberView> twoRoots = List.of(grouped("ais-behaviour", "a", 0.5, "group:G1", "x"),
                grouped("nav-warning", "b", 0.5, "group:G2", "y"), grouped("gnss-interference", "c", 0.5, "group:G3", "y"));
        assertThat(EventScorer.score(twoRoots, 0).evidenceQuality()).isEqualTo("strong");
    }

    @Test
    @DisplayName("1.0.0 vs 1.1.0: without evidence groups members count as before; with them echoes collapse")
    void versionComparison() {
        // Members without evidence identity: each is its own group, judged by source (the 1.0.0 result).
        List<MemberView> plain = List.of(new MemberView("ais-behaviour", 0.5, "a", true, null),
                new MemberView("nav-warning", 0.5, "b", true, null),
                new MemberView("gnss-interference", 0.5, "b", true, null));
        assertThat(EventScorer.score(plain, 0).evidenceQuality()).isEqualTo("strong");
        // The same three families, all reported by one originator's echo group: 1.0.0 called this strong
        // (three families, two source ids); 1.1.0 counts one group, so one family and weak.
        List<MemberView> echoed = List.of(grouped("ais-behaviour", "a", 0.5, "group:G", "r"),
                grouped("nav-warning", "b", 0.5, "group:G", "r"), grouped("gnss-interference", "b", 0.5, "group:G", "r"));
        EventScorer.Score score = EventScorer.score(echoed, 0);
        assertThat(score.evidenceQuality()).isEqualTo("weak");
        assertThat(score.confidence()).isCloseTo(0.5 * EventScorer.DEFAULT_RELIABILITY, Offset.offset(1e-12));
    }

    @Test
    @DisplayName("member views round-trip through their manifest maps")
    void viewsRoundTrip() {
        MemberView view = grouped("seismic", "usgs", 0.7, "group:G1", "usgs");
        assertThat(EventScorerViews.fromMaps(List.of(EventScorerViews.toMap(view)))).containsExactly(view);
    }
}
