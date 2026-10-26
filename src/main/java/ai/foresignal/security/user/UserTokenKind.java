package ai.foresignal.security.user;

/** What a {@link UserTokenEntity} is for. */
public enum UserTokenKind {
    /** Consumed by {@code activateAccount} to set the first password of an invited user. */
    ACTIVATION,
    /** Consumed by {@code resetPassword} to set a new password. */
    RESET;

    /** The wire value, also the stored {@code user_token.kind}. */
    public String wireValue() {
        return name().toLowerCase();
    }

    /**
     * The kind with a wire value.
     *
     * @param value {@code activation} or {@code reset}
     * @return the kind
     * @throws IllegalArgumentException when no kind has that value
     */
    public static UserTokenKind fromWire(String value) {
        for (UserTokenKind kind : values()) {
            if (kind.wireValue().equals(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("unknown user token kind: " + value);
    }
}
