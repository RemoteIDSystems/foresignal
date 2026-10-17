package ai.foresignal.evidence;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * The quality flags of a canonical event (prompt 116), the "adversarial evidence evaluator": they say how the
 * evidence is shaped, never that anything is false.
 *
 * <ul>
 *   <li>{@code ECHO}: at least {@value #ECHO_MIN} observations in one evidence group within
 *       {@link #ECHO_WINDOW} (by observed time): many re-reports of one origin;
 *   <li>{@code TIMESTAMP_CONFLICT}: member event times disagree by more than 3 × the family tolerance;
 *   <li>{@code SINGLE_ORIGINATOR}: one evidence group only;
 *   <li>{@code LATE_REVISION}: a member's event time or location changed after first publication.
 * </ul>
 */
public final class QualityFlags {

    /** Many re-reports of one originator. */
    public static final String ECHO = "ECHO";

    /** Member event times disagree. */
    public static final String TIMESTAMP_CONFLICT = "TIMESTAMP_CONFLICT";

    /** One evidence group only. */
    public static final String SINGLE_ORIGINATOR = "SINGLE_ORIGINATOR";

    /** A member was revised after first publication. */
    public static final String LATE_REVISION = "LATE_REVISION";

    /** Every flag, in the spec's order. */
    public static final List<String> ALL = List.of(ECHO, TIMESTAMP_CONFLICT, SINGLE_ORIGINATOR, LATE_REVISION);

    /** Observations in one group that make an echo. */
    public static final int ECHO_MIN = 5;

    /** The window an echo is counted in. */
    public static final Duration ECHO_WINDOW = Duration.ofHours(2);

    /** The multiple of the family time tolerance beyond which event times conflict. */
    public static final double CONFLICT_FACTOR = 3.0;

    private QualityFlags() {}

    /**
     * One member as the flags see it.
     *
     * @param group its evidence group key
     * @param eventTime its event time, or null
     * @param observedAt its observed time, or null
     * @param revised true when its event time or location changed after first publication
     */
    public record Member(String group, Instant eventTime, Instant observedAt, boolean revised) {}

    /** The flags of an event with these members under {@code rule}, in {@link #ALL} order. */
    public static List<String> compute(CanonicalRules.FamilyRule rule, List<Member> members) {
        TreeSet<String> flags = new TreeSet<>((a, b) -> Integer.compare(ALL.indexOf(a), ALL.indexOf(b)));
        Map<String, List<Instant>> byGroup = new LinkedHashMap<>();
        Instant minEvent = null;
        Instant maxEvent = null;
        for (Member member : members) {
            if (member.observedAt() != null) {
                byGroup.computeIfAbsent(member.group(), k -> new ArrayList<>()).add(member.observedAt());
            } else {
                byGroup.computeIfAbsent(member.group(), k -> new ArrayList<>());
            }
            if (member.eventTime() != null) {
                minEvent = minEvent == null || member.eventTime().isBefore(minEvent) ? member.eventTime() : minEvent;
                maxEvent = maxEvent == null || member.eventTime().isAfter(maxEvent) ? member.eventTime() : maxEvent;
            }
            if (member.revised()) {
                flags.add(LATE_REVISION);
            }
        }
        for (List<Instant> times : byGroup.values()) {
            if (echo(times)) {
                flags.add(ECHO);
                break;
            }
        }
        if (minEvent != null && rule != null) {
            double spread = Duration.between(minEvent, maxEvent).toMillis() / 1000.0;
            if (spread > CONFLICT_FACTOR * rule.timeTolerance()) {
                flags.add(TIMESTAMP_CONFLICT);
            }
        }
        if (byGroup.size() == 1) {
            flags.add(SINGLE_ORIGINATOR);
        }
        return new ArrayList<>(flags);
    }

    /** True when {@value #ECHO_MIN} of {@code times} fall within one {@link #ECHO_WINDOW}. */
    static boolean echo(List<Instant> times) {
        if (times.size() < ECHO_MIN) {
            return false;
        }
        List<Instant> sorted = new ArrayList<>(times);
        sorted.sort(null);
        for (int i = 0; i + ECHO_MIN - 1 < sorted.size(); i++) {
            if (!Duration.between(sorted.get(i), sorted.get(i + ECHO_MIN - 1)).minus(ECHO_WINDOW).isPositive()) {
                return true;
            }
        }
        return false;
    }
}
