package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The tunable evidence-quality thresholds and the cross-modality requirement (prompt 154): the strong / moderate
 * gates are config, and strong can be made to need several distinct sensing modalities.
 */
class EventScorerModalityTest {

    private static EventScorer.MemberView member(String family, String root, String modality) {
        return new EventScorer.MemberView(family, 0.8, "src-" + root, true, 0.8, null, root, modality);
    }

    @Test
    @DisplayName("the default thresholds keep the historical strong rule: 3 families from 2 independent groups")
    void defaultStrong() {
        List<EventScorer.MemberView> members = List.of(member("conflict", "r1", Modality.SOCIAL),
                member("fire", "r1", Modality.SOCIAL), member("seismic", "r2", Modality.SENSOR));
        assertThat(EventScorer.score(members, 0).evidenceQuality()).isEqualTo("strong");
    }

    @Test
    @DisplayName("a modality requirement withholds strong until enough distinct modalities are present")
    void modalityGate() {
        List<EventScorer.MemberView> oneModality = List.of(member("conflict", "r1", Modality.SOCIAL),
                member("fire", "r1", Modality.SOCIAL), member("seismic", "r2", Modality.SOCIAL));
        EventScorer.Quality needsTwo = new EventScorer.Quality(3, 2, 2, 2);
        assertThat(EventScorer.score(oneModality, 0, needsTwo).evidenceQuality()).isEqualTo("moderate");

        List<EventScorer.MemberView> threeModalities = List.of(member("conflict", "r1", Modality.SOCIAL),
                member("fire", "r1", Modality.IMAGERY), member("seismic", "r2", Modality.SENSOR));
        EventScorer.Quality needsThree = new EventScorer.Quality(3, 2, 2, 3);
        assertThat(EventScorer.score(threeModalities, 0, needsThree).evidenceQuality()).isEqualTo("strong");
    }

    @Test
    @DisplayName("lowered thresholds make a two-family event strong")
    void loweredThresholds() {
        List<EventScorer.MemberView> members = List.of(member("conflict", "r1", Modality.SENSOR),
                member("fire", "r2", Modality.SENSOR));
        assertThat(EventScorer.score(members, 0).evidenceQuality()).isEqualTo("moderate");
        EventScorer.Quality lower = new EventScorer.Quality(2, 2, 2, 0);
        assertThat(EventScorer.score(members, 0, lower).evidenceQuality()).isEqualTo("strong");
    }
}
