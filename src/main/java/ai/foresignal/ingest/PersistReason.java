package ai.foresignal.ingest;

/**
 * Why a version was persisted, stored as {@code record_version.persist_reason}
 * (Core-Data-Model §5.2).
 *
 * <p>The numeric codes are persisted and must never change.
 */
public enum PersistReason {
    /** The entity had no head. */
    FIRST(1),
    /** The head was a tombstone, or the live row had expired. */
    REAPPEAR(2),
    /** The static (significant-content) hash changed. */
    SIGNIFICANT(3),
    /** Upstream said the entity is gone. */
    TOMBSTONE(4),
    /** Policy ALL and the full hash changed. */
    CHANGE(5),
    /** SAMPLED: enough time passed and the entity deviated or crossed a threshold. */
    SAMPLE(6),
    /** The keyframe or retention keyframe came due and live state differs from persisted. */
    KEYFRAME(7),
    /** The keyframe came due with live state equal to persisted; skipped by triggers. */
    HEARTBEAT(8),
    /** A trigger rule's predicate flipped against the persisted state. */
    TRIGGER_EDGE(9);

    private final short code;

    PersistReason(int code) {
        this.code = (short) code;
    }

    /** The stored {@code smallint}. */
    public short code() {
        return code;
    }

    /**
     * The reason stored as {@code code}.
     *
     * @throws IllegalArgumentException for an unknown code
     */
    public static PersistReason fromCode(int code) {
        for (PersistReason reason : values()) {
            if (reason.code == code) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown persist reason code " + code);
    }
}
