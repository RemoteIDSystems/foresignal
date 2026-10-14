package ai.foresignal.collect;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import org.springframework.stereotype.Service;

/**
 * The operator's "fetch once now" (Product Charter FR-6; {@code POST /v1/sources/{id}/fetch},
 * whose endpoint arrives in prompt 013).
 *
 * <p>Runs one {@link FetchTrigger#MANUAL} fetch through the {@link FetchExecutor}, which ingests
 * normally and writes the fetch's {@code fetch_log} row, <strong>even while the source is
 * disabled</strong>: a manual fetch is one of the three things allowed to cause an upstream
 * request, alongside an enabled source and a probe. It consumes budget like any fetch.
 *
 * <p>A source <em>halted</em> by its upstream's usage policy (prompt 064, {@code UpstreamHaltException}) is
 * released by a manual fetch: the hold exists so that a human investigates before Foresignal asks again, and
 * fetch-now is that human's decision. An ordinary backoff (the upstream's own {@code Retry-After}) still
 * refuses with {@code IN_BACKOFF}.
 */
@Service
public class ManualFetchService {

    private final SourceRegistry registry;
    private final FetchExecutor executor;
    private final CredentialsAccess credentials;
    private final BackoffRegistry backoff;

    /** Creates the service. */
    public ManualFetchService(
            SourceRegistry registry, FetchExecutor executor, CredentialsAccess credentials, BackoffRegistry backoff) {
        this.registry = registry;
        this.executor = executor;
        this.credentials = credentials;
        this.backoff = backoff;
    }

    /**
     * Fetches {@code sourceId} once now.
     *
     * @throws ai.foresignal.api.error.NotFoundException (404) for an unknown source
     * @throws ConflictException (409) {@code NOT_SUPPORTED_FOR_MODE} for streaming, push and derived
     *     sources; {@code CREDENTIALS_REQUIRED} when a required credential is missing;
     *     {@code ALREADY_RUNNING} when a fetch of the source is in flight; {@code IN_BACKOFF} while
     *     the source is in backoff
     * @throws BudgetExceededProblem (429) when the budget refused the request
     */
    public FetchOutcome fetchNow(String sourceId) {
        return fetchNow(sourceId, false);
    }

    /**
     * Fetches {@code sourceId} once now, optionally releasing the complete-set reconciliation's shrink
     * guard (prompt 094).
     *
     * @param sourceId the source
     * @param allowShrink true to let reconciliation tombstone more than {@code reconcile.maxAbsentRatio}
     *     of a reference dataset, once
     */
    public FetchOutcome fetchNow(String sourceId, boolean allowShrink) {
        SourceRegistry.Registration registration = registry.get(sourceId);
        SourceDescriptor descriptor = registration.descriptor();
        if (descriptor.mode() != Mode.POLLING
                || !(registration.adapterIfPresent().orElse(null) instanceof PollingAdapter)) {
            throw new ConflictException(
                    ConflictException.NOT_SUPPORTED_FOR_MODE,
                    "'" + sourceId + "' is a " + descriptor.mode().wireValue()
                            + " source; a manual fetch is not supported for this source mode"
                            + (descriptor.mode() == Mode.STREAMING ? " (use probe for streams)." : "."));
        }
        if (descriptor.requiresCredentials() && !credentials.isSet(sourceId)) {
            throw new ConflictException(
                    ConflictException.CREDENTIALS_REQUIRED,
                    "'" + sourceId + "' needs credentials before it can be fetched.");
        }
        if (executor.isRunning(sourceId)) {
            throw alreadyRunning(sourceId);
        }
        backoff.clearHalt(sourceId);
        if (backoff.isInBackoff(sourceId)) {
            throw inBackoff(sourceId);
        }

        FetchOutcome outcome = executor.execute(sourceId, FetchTrigger.MANUAL, allowShrink);
        if (FetchOutcome.SKIP_OVERLAP.equals(outcome.skipReason())) {
            throw alreadyRunning(sourceId);
        }
        if (FetchOutcome.SKIP_BACKOFF.equals(outcome.skipReason())) {
            throw inBackoff(sourceId);
        }
        if (FetchOutcome.SKIP_HOST_PACING.equals(outcome.skipReason())) {
            // Prompt 080: the host's request gate stayed busy; nothing was sent. Reported like a backoff.
            throw new ConflictException(ConflictException.IN_BACKOFF,
                    "'" + sourceId + "' could not get a request slot on its upstream host (" + outcome.error()
                            + "); try again shortly.");
        }
        if (outcome.status() == FetchStatus.REFUSED) {
            throw new BudgetExceededProblem(
                    "The budget for '" + sourceId + "' is spent; the fetch was not sent.");
        }
        return outcome;
    }

    private static ConflictException alreadyRunning(String sourceId) {
        return new ConflictException(
                ConflictException.ALREADY_RUNNING, "A fetch of '" + sourceId + "' is already running.");
    }

    private ConflictException inBackoff(String sourceId) {
        return new ConflictException(
                ConflictException.IN_BACKOFF,
                "'" + sourceId + "' is in backoff until " + backoff.backoffUntil(sourceId) + ".");
    }
}
