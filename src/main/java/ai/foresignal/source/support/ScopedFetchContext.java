package ai.foresignal.source.support;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;

/**
 * A {@link FetchContext} whose adapter state is namespaced (prompt 075): every key a helper reads or writes is
 * stored as {@code {scope}/{key}}, while the network, clock, budget and everything else are the source's own.
 *
 * <p>For a source that runs one stateful helper several times per fetch, for example {@code nhc-cyclone-cones},
 * which queries four ArcGIS layers with {@code ArcGisQueryClient}: without a scope the layers would overwrite each
 * other's cached metadata ({@code arcgis:meta}).
 *
 * <p>The reserved health keys ({@link AdapterStateStore#DEGRADED_REASON_KEY},
 * {@link AdapterStateStore#FAILING_REASON_KEY}, {@link AdapterStateStore#NEWEST_INPUT_KEY},
 * {@link AdapterStateStore#MAX_INPUT_GAP_KEY}) pass through unscoped, so a helper that reports a health problem
 * still reaches the source's health rules.
 */
public final class ScopedFetchContext implements FetchContext {

    private final FetchContext delegate;
    private final AdapterStateStore state;

    /**
     * Wraps {@code delegate}.
     *
     * @param delegate the source's own context
     * @param scope the namespace, non-blank, without {@code /}
     */
    public ScopedFetchContext(FetchContext delegate, String scope) {
        this.delegate = Objects.requireNonNull(delegate);
        this.state = scoped(delegate.state(), scope);
    }

    /**
     * {@code state} with every non-reserved key stored as {@code {scope}/{key}}.
     *
     * @param state the source's state
     * @param scope the namespace, non-blank, without {@code /}
     */
    public static AdapterStateStore scoped(AdapterStateStore state, String scope) {
        Objects.requireNonNull(state);
        if (scope == null || scope.isBlank() || scope.contains("/")) {
            throw new IllegalArgumentException("A state scope must be non-blank and contain no '/': " + scope);
        }
        String prefix = scope.strip() + "/";
        return new AdapterStateStore() {
            @Override
            public Optional<String> get(String key) {
                return state.get(key(key));
            }

            @Override
            public void put(String key, String value) {
                state.put(key(key), value);
            }

            @Override
            public void remove(String key) {
                state.remove(key(key));
            }

            private String key(String key) {
                return reserved(key) ? key : prefix + key;
            }
        };
    }

    /** True for the health keys that are never scoped. */
    static boolean reserved(String key) {
        return AdapterStateStore.DEGRADED_REASON_KEY.equals(key) || AdapterStateStore.FAILING_REASON_KEY.equals(key)
                || AdapterStateStore.NEWEST_INPUT_KEY.equals(key) || AdapterStateStore.MAX_INPUT_GAP_KEY.equals(key);
    }

    @Override
    public String sourceId() {
        return delegate.sourceId();
    }

    @Override
    public SourceDescriptor descriptor() {
        return delegate.descriptor();
    }

    @Override
    public SourceHttp http() {
        return delegate.http();
    }

    @Override
    public ValidatedParams params() {
        return delegate.params();
    }

    @Override
    public Map<String, String> credentials() {
        return delegate.credentials();
    }

    @Override
    public Clock clock() {
        return delegate.clock();
    }

    @Override
    public Optional<Instant> lastSuccessAt() {
        return delegate.lastSuccessAt();
    }

    /** The scoped state. */
    @Override
    public AdapterStateStore state() {
        return state;
    }

    @Override
    public RasterSink rasters() {
        return delegate.rasters();
    }

    @Override
    public MediaSink media() {
        return delegate.media();
    }

    @Override
    public InboxAccess inbox() {
        return delegate.inbox();
    }

    @Override
    public void chargeRequest(String label) throws AdapterException {
        delegate.chargeRequest(label);
    }

    @Override
    public Logger log() {
        return delegate.log();
    }

    @Override
    public Instant now() {
        return delegate.now();
    }
}
