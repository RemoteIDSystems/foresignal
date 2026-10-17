package ai.foresignal.situation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * The situation lifecycle (prompt 118 §4), a pure function of the stored state, the members and the clock:
 *
 * <ul>
 *   <li>{@code emerging} → {@code active} when the core members come from at least two signal families
 *       <em>and</em> at least two independent evidence groups (116), or an operator sets it;
 *   <li>{@code active} (and {@code emerging}) → {@code dormant} after {@code dormancyWindow} without a new core
 *       member;
 *   <li>{@code dormant} → {@code active} on a new core member within {@code reopenWindow};
 *   <li>{@code dormant} → {@code closed} after {@value #CLOSE_AFTER_DAYS} days, or by an operator;
 *   <li>{@code closed} is final: a new situation starts instead.
 * </ul>
 *
 * <p>An {@code emerging} situation that never activates also goes dormant after {@code dormancyWindow}, so it
 * stops being a linker candidate and can close; the spec names only {@code active} → {@code dormant}.
 */
public final class SituationLifecycle {

    /** Formed, not yet corroborated across families and evidence groups. */
    public static final String EMERGING = "emerging";

    /** Corroborated and receiving members. */
    public static final String ACTIVE = "active";

    /** No new core member for {@code dormancyWindow}. */
    public static final String DORMANT = "dormant";

    /** Final. */
    public static final String CLOSED = "closed";

    /** Every status. */
    public static final List<String> ALL = List.of(EMERGING, ACTIVE, DORMANT, CLOSED);

    /** The statuses the linker may join. */
    public static final List<String> OPEN = List.of(EMERGING, ACTIVE, DORMANT);

    /** Days without activity after which a dormant situation closes. */
    public static final int CLOSE_AFTER_DAYS = 90;

    /** The component name and version stored with status decisions. */
    public static final String NAME = "situation-lifecycle";

    /** The lifecycle version. */
    public static final String VERSION = "1.0.0";

    private SituationLifecycle() {}

    /** True when the members meet the activation rule: ≥ 2 signal families and ≥ 2 independent evidence groups. */
    public static boolean meetsActivation(int coreFamilies, int independentGroups) {
        return coreFamilies >= 2 && independentGroups >= 2;
    }

    /**
     * The status after a membership change.
     *
     * @param current the stored status
     * @param coreFamilies distinct signal families of the core members
     * @param independentGroups independent evidence groups of the core members
     * @param newCoreMember true when the change added a core member
     * @return the new status (unchanged for {@code closed})
     */
    public static String afterMembers(String current, int coreFamilies, int independentGroups, boolean newCoreMember) {
        if (CLOSED.equals(current)) {
            return CLOSED;
        }
        if (DORMANT.equals(current)) {
            return newCoreMember ? ACTIVE : DORMANT;
        }
        if (EMERGING.equals(current) && meetsActivation(coreFamilies, independentGroups)) {
            return ACTIVE;
        }
        return current;
    }

    /**
     * The status the clock implies (the periodic sweep): open situations without activity for
     * {@code dormancyWindow} go dormant, dormant ones without activity for {@value #CLOSE_AFTER_DAYS} days close.
     *
     * @param current the stored status
     * @param lastActivityAt the newest core member observation
     * @param now the decision time
     * @param dormancyWindow the dormancy window
     */
    public static String afterTime(String current, Instant lastActivityAt, Instant now, Duration dormancyWindow) {
        if (CLOSED.equals(current) || lastActivityAt == null) {
            return current;
        }
        Duration quiet = Duration.between(lastActivityAt, now);
        if (DORMANT.equals(current)) {
            return quiet.compareTo(Duration.ofDays(CLOSE_AFTER_DAYS)) >= 0 ? CLOSED : DORMANT;
        }
        return quiet.compareTo(dormancyWindow) >= 0 ? DORMANT : current;
    }

    /**
     * True when an operator may move a situation from {@code current} to {@code target}: {@code active} from
     * emerging or dormant (activate, reopen), {@code closed} from any open status; never out of {@code closed}.
     */
    public static boolean operatorMayMove(String current, String target) {
        if (CLOSED.equals(current)) {
            return false;
        }
        return ACTIVE.equals(target) || CLOSED.equals(target);
    }

    /** The basis text of a status: families, evidence groups and the rule that applied. */
    public static String basis(String status, int coreFamilies, int independentGroups, boolean overridden) {
        String counts = coreFamilies + (coreFamilies == 1 ? " signal family, " : " signal families, ")
                + independentGroups + (independentGroups == 1 ? " independent evidence group" : " independent evidence groups");
        return switch (status) {
            case ACTIVE -> overridden ? counts + "; activated by an operator" : counts;
            case EMERGING -> counts + "; activation needs 2 families and 2 independent groups";
            case DORMANT -> counts + "; no new core member within the dormancy window";
            default -> counts + "; closed (final)";
        };
    }
}
