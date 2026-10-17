package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The red team's sets (prompt 128 §3): the two that moved most, then the highest stakes, three at most. */
class RedTeamSelectionTest {

    @Test
    @DisplayName("movers first, then stakes; never more than three")
    void choose() {
        List<RedTeamSelection.Candidate> chosen = RedTeamSelection.choose(List.of(
                new RedTeamSelection.Candidate("calm-critical", 0, 3),
                new RedTeamSelection.Candidate("mover-1", 0.3, 1),
                new RedTeamSelection.Candidate("mover-2", 0.2, 1),
                new RedTeamSelection.Candidate("mover-3", 0.1, 2),
                new RedTeamSelection.Candidate("calm-info", 0, 1)), RedTeamSelection.MAX_SETS);
        assertThat(chosen).extracting(RedTeamSelection.Candidate::setId)
                .containsExactly("mover-1", "mover-2", "calm-critical");
    }

    @Test
    @DisplayName("without movement the highest stakes are taken")
    void stakesOnly() {
        List<RedTeamSelection.Candidate> chosen = RedTeamSelection.choose(List.of(
                new RedTeamSelection.Candidate("b", 0, 1), new RedTeamSelection.Candidate("a", 0, 2)), 3);
        assertThat(chosen).extracting(RedTeamSelection.Candidate::setId).containsExactly("a", "b");
        assertThat(RedTeamSelection.choose(List.of(), 3)).isEmpty();
    }
}
