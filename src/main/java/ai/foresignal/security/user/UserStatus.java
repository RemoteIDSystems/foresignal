package ai.foresignal.security.user;

/** The lifecycle state of a {@link UserEntity} (OpenAPI {@code User.status}, an extensible enum). */
public enum UserStatus {
    /** Created by an admin; no password yet, waiting for activation. */
    INVITED,
    /** Activated; may sign in. */
    ACTIVE,
    /** Sign-in blocked and sessions revoked; configuration retained. */
    SUSPENDED,
    /** Permanently switched off; configuration retained. */
    DISABLED;

    /** The wire value, also the stored {@code user.status}. */
    public String wireValue() {
        return name().toLowerCase();
    }

    /**
     * The status with a wire value.
     *
     * @param value {@code invited}, {@code active}, {@code suspended} or {@code disabled}
     * @return the status
     * @throws IllegalArgumentException when no status has that value
     */
    public static UserStatus fromWire(String value) {
        for (UserStatus status : values()) {
            if (status.wireValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("unknown user status: " + value);
    }
}
