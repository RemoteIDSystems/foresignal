package ai.foresignal.inbound.edp;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * A lifecycle change of an EDP provider (prompt 138), published by {@link EdpManifestService} as a Spring application
 * event: the SSE topic {@code external} turns it into {@code external.source.state}, and the binding service disables
 * the bindings of streams a breaking manifest update changed.
 *
 * @param sourceId the provider ({@code ext-…})
 * @param state the provider's state after the change ({@code pending}, {@code active}, {@code suspended}, or null
 *     once deleted)
 * @param change {@value #REGISTERED}, {@value #APPROVED}, {@value #SUSPENDED}, {@value #MANIFEST_CHANGED} or
 *     {@value #DELETED}
 * @param breakingChanges the breaking changes of a manifest update (empty otherwise)
 * @param changedStreams the stream ids a breaking manifest update added, removed or changed (empty otherwise)
 * @param at when it happened
 */
public record ExternalSourceChangedEvent(String sourceId, String state, String change, List<String> breakingChanges,
        Set<String> changedStreams, Instant at) {

    /** A provider was registered. */
    public static final String REGISTERED = "registered";

    /** A provider was approved. */
    public static final String APPROVED = "approved";

    /** A provider was suspended. */
    public static final String SUSPENDED = "suspended";

    /** A provider's manifest changed. */
    public static final String MANIFEST_CHANGED = "manifest-changed";

    /** A provider was deleted. */
    public static final String DELETED = "deleted";

    /** Copies the collections. */
    public ExternalSourceChangedEvent {
        breakingChanges = breakingChanges == null ? List.of() : List.copyOf(breakingChanges);
        changedStreams = changedStreams == null ? Set.of() : Set.copyOf(changedStreams);
    }

    /** A change without breaking details. */
    public static ExternalSourceChangedEvent of(String sourceId, String state, String change, Instant at) {
        return new ExternalSourceChangedEvent(sourceId, state, change, List.of(), Set.of(), at);
    }

    /** True for a manifest update that returned streams to review. */
    public boolean breaking() {
        return MANIFEST_CHANGED.equals(change) && !breakingChanges.isEmpty();
    }
}
