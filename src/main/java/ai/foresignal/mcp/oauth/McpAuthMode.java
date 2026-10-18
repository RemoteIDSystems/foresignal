package ai.foresignal.mcp.oauth;

import java.util.Locale;
import java.util.Optional;

/** {@code mcp.auth.mode} (prompt 141): which credentials {@code /mcp} accepts. */
public enum McpAuthMode {

    /** Foresignal API tokens only (the default; open reads apply as on REST). */
    TOKEN,

    /** Access tokens from the operator's identity provider only. */
    OAUTH,

    /** Both: identity-provider access tokens and Foresignal API tokens. */
    BOTH;

    /** The wire value: {@code token}, {@code oauth} or {@code both}. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** True when identity-provider tokens are accepted. */
    public boolean oauth() {
        return this != TOKEN;
    }

    /** True when Foresignal API tokens are accepted. */
    public boolean apiTokens() {
        return this != OAUTH;
    }

    /** The mode with wire value {@code value} (case-insensitive), or empty. */
    public static Optional<McpAuthMode> fromWireValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (McpAuthMode mode : values()) {
            if (mode.wireValue().equalsIgnoreCase(value.strip())) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }
}
