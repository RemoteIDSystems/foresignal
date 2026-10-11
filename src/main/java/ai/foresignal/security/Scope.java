package ai.foresignal.security;

/**
 * Token scopes, least to most privileged (Architecture §15).
 *
 * <p>Ordered so {@link #includes} is a simple ordinal comparison: a token's scope satisfies a
 * requirement when it is at least as privileged.
 */
public enum Scope {
    READ,
    AGENT,
    ADMIN;

    /** The wire value used in the API and in {@code api_token.scope}. */
    public String wireValue() {
        return name().toLowerCase();
    }

    /** True when a token with this scope satisfies {@code required}. */
    public boolean includes(Scope required) {
        return ordinal() >= required.ordinal();
    }

    /** Parses a wire value, case-insensitively. */
    public static Scope fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Scope is required");
        }
        return switch (value.trim().toLowerCase()) {
            case "read" -> READ;
            case "agent" -> AGENT;
            case "admin" -> ADMIN;
            default -> throw new IllegalArgumentException(
                    "Unknown scope '" + value + "'. Expected read, agent or admin.");
        };
    }
}
