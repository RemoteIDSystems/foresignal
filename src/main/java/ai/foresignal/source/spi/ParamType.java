package ai.foresignal.source.spi;

/**
 * The types an adapter parameter can have.
 *
 * <p>The first seven match the {@code ParamSpec.type} enum in the OpenAPI document. The last two
 * are internal: they exist to make two specific attacks unexpressible rather than merely
 * discouraged (see {@link #LAN_URL} and {@link #INBOX_PATH}).
 */
public enum ParamType {
    STRING("string"),
    INTEGER("integer"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    DURATION("duration"),
    /** {@code minLon,minLat,maxLon,maxLat}. */
    BBOX("bbox"),
    STRING_LIST("string-list"),

    /**
     * A URL on the local network, for a receiver the operator runs themselves.
     *
     * <p>Validation resolves the host once and requires a loopback, RFC 1918, link-local or ULA
     * address, then stores the resolved IP. The client connects to that pinned IP with the original
     * {@code Host} header and redirects disabled, so a name that resolves differently on the second
     * lookup cannot be used to reach anything else. That is what defeats DNS rebinding.
     */
    LAN_URL("lan-url"),

    /**
     * A path under the configured inbox directory.
     *
     * <p>Validation requires the realpath to sit under {@code FORESIGNAL_INBOX_DIR}, and
     * {@link ai.foresignal.source.InboxFiles} re-checks it per file and per scan while opening with
     * {@code NOFOLLOW_LINKS}, so a symlink planted after validation cannot redirect a read.
     */
    INBOX_PATH("inbox-path");

    private final String wireValue;

    ParamType(String wireValue) {
        this.wireValue = wireValue;
    }

    /** The value used in the API and in the OpenAPI document. */
    public String wireValue() {
        return wireValue;
    }

    /** Parses a wire value, case-insensitively. */
    public static ParamType fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Param type is required");
        }
        String normalized = value.trim().toLowerCase();
        for (ParamType type : values()) {
            if (type.wireValue.equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown param type '" + value + "'");
    }
}
