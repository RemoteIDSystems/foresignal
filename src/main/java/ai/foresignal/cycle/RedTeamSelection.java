package ai.foresignal.cycle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chooses the hypothesis sets the red team challenges (prompt 128 §3): up to three active sets in scope with the most
 * movement in the period or the highest stakes. The sets that moved most come first (two places, ties by stakes);
 * the remaining places go to the highest-stakes sets not yet chosen (ties by movement, then id). Pure.
 */
public final class RedTeamSelection {

    /** The most sets challenged per run. */
    public static final int MAX_SETS = 3;

    private RedTeamSelection() {}

    /**
     * A candidate set.
     *
     * @param setId the set
     * @param movement the largest probability move in the period (0 when none)
     * @param stakes the linked situation's stakes (critical 3, warning 2, otherwise 1)
     */
    public record Candidate(String setId, double movement, double stakes) {}

    /** The chosen sets, at most {@code max}. */
    public static List<Candidate> choose(List<Candidate> candidates, int max) {
        Map<String, Candidate> chosen = new LinkedHashMap<>();
        List<Candidate> byMovement = new ArrayList<>(candidates);
        byMovement.sort(Comparator.comparingDouble(Candidate::movement).reversed()
                .thenComparing(Comparator.comparingDouble(Candidate::stakes).reversed())
                .thenComparing(Candidate::setId));
        int movers = Math.min(max, Math.max(1, max - 1));
        for (Candidate c : byMovement) {
            if (chosen.size() >= movers || c.movement() <= 0) {
                break;
            }
            chosen.putIfAbsent(c.setId(), c);
        }
        List<Candidate> byStakes = new ArrayList<>(candidates);
        byStakes.sort(Comparator.comparingDouble(Candidate::stakes).reversed()
                .thenComparing(Comparator.comparingDouble(Candidate::movement).reversed())
                .thenComparing(Candidate::setId));
        for (Candidate c : byStakes) {
            if (chosen.size() >= max) {
                break;
            }
            chosen.putIfAbsent(c.setId(), c);
        }
        return List.copyOf(chosen.values());
    }
}
