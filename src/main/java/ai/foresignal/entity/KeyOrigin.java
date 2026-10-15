package ai.foresignal.entity;

/** How a key or link came to be asserted (prompt 098). */
public enum KeyOrigin {
    /** A registry snapshot (for example the FAA releasable aircraft database). */
    REGISTRY,
    /** Several keys asserted together by one record. */
    CO_ASSERTED,
    /** A single observed key. */
    OBSERVED,
    /** An operator assertion through the admin API. */
    OPERATOR;

    /** The wire value stored in the database and served by the API. */
    public String wire() {
        return name();
    }
}
