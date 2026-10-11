package ai.foresignal.source;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * What {@code GET /v1/sources} returns for one source: what it is, plus how it is configured.
 *
 * <p>Credential <em>values</em> are deliberately absent; only {@link #credentialsSet} says whether
 * any are stored. There is no code path from a controller to a decrypted credential (see
 * {@link CredentialsAccess}).
 *
 * <p>Health arrives in prompt 006 as an additional component; it is not here yet rather than being
 * a null that callers must tolerate.
 *
 * @param descriptor the fixed description
 * @param enabled whether the source is being collected
 * @param interval the effective poll interval
 * @param params the effective parameters, with defaults applied
 * @param budget the effective budget
 * @param retention the effective retention window, or null when the global default applies
 * @param ingestPolicy the effective ingest policy
 * @param credentialsSet true when credentials are stored
 * @param updatedAt when the settings last changed
 */
public record SourceView(
        SourceDescriptor descriptor,
        boolean enabled,
        Duration interval,
        Map<String, Object> params,
        Budget budget,
        Duration retention,
        IngestPolicy ingestPolicy,
        boolean credentialsSet,
        Instant updatedAt) {

    public SourceView {
        params = params == null ? Map.of() : Map.copyOf(params);
    }

    public String id() {
        return descriptor.id();
    }

    /** True when the source is enabled but is missing a required credential. */
    public boolean isMissingRequiredCredentials() {
        return descriptor.requiresCredentials() && !credentialsSet;
    }
}
