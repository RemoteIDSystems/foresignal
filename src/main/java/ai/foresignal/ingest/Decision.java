package ai.foresignal.ingest;

import java.util.Objects;

/**
 * One row of the §5.2 decision table, as applied to one observation.
 *
 * @param outcome what to do
 * @param reason why a version is persisted; non-null exactly when {@code outcome} is
 *     {@link Outcome#PERSIST}
 */
public record Decision(Outcome outcome, PersistReason reason) {

    public Decision {
        Objects.requireNonNull(outcome, "outcome");
        if ((outcome == Outcome.PERSIST) != (reason != null)) {
            throw new IllegalArgumentException(
                    "A persist reason is required for PERSIST and forbidden otherwise: " + outcome + "/" + reason);
        }
    }

    /** A PERSIST decision. */
    public static Decision persist(PersistReason reason) {
        return new Decision(Outcome.PERSIST, Objects.requireNonNull(reason, "reason"));
    }

    /** A non-persisting decision. */
    public static Decision of(Outcome outcome) {
        return new Decision(outcome, null);
    }

    /** True for PERSIST. */
    public boolean persists() {
        return outcome == Outcome.PERSIST;
    }
}
