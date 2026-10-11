package ai.foresignal.api.dto;

/**
 * The {@code CredentialField} schema: one credential a source accepts. Never carries a value.
 *
 * @param name the key used in {@code PUT /v1/sources/{id}/credentials}
 * @param description what the credential is
 * @param required whether the source cannot be enabled without it
 */
public record CredentialFieldDto(String name, String description, boolean required) {}
