package ai.elaro.source.spi;

/**
 * One named credential an upstream needs, for example {@code apiKey} or {@code clientSecret}.
 *
 * <p>Only the names are public. Values are encrypted at rest and are never returned by any method a
 * controller can reach.
 *
 * @param name the key in the credentials map
 * @param description what the operator should paste here, and where to get it
 * @param required when true, the source cannot be enabled until it is set
 */
public record CredentialField(String name, String description, boolean required) {

    public CredentialField {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Credential field name is required");
        }
    }

    public static CredentialField required(String name, String description) {
        return new CredentialField(name, description, true);
    }

    public static CredentialField optional(String name, String description) {
        return new CredentialField(name, description, false);
    }
}
