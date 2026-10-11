package ai.foresignal.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * The {@code CredentialsUpdate} schema. Write-only: values are encrypted at rest and never returned.
 *
 * @param values credential values keyed by the descriptor's credential field names
 */
public record CredentialsUpdate(@NotNull Map<String, String> values) {

    /** Hides the values, so a logged or printed request never shows a secret. */
    @Override
    public String toString() {
        return "CredentialsUpdate[values=" + (values == null ? "null" : values.keySet()) + "]";
    }
}
