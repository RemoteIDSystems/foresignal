package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterStateStore;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The adapter state of one polling fetch run by the {@link FetchExecutor}: writes are staged in memory and
 * reach the persistent store only when the executor {@linkplain #commit commits} them, after the fetch's
 * records were written by the ingest writer. A failed fetch {@linkplain #discard discards} them.
 *
 * <p>Without this, a cursor, a watermark, a live set or a conditional-GET validator written during the fetch
 * was durable at once, before the framework had written the fetch's records; a failure after that point
 * (a parse error, a timeout, the writer failing the batch) lost the window between the old and the new
 * cursor for good (F1 and F2 of the 2026-10 review).
 *
 * <p>Semantics:
 *
 * <ul>
 *   <li>Reads see the fetch's own staged writes first, then the persistent store.
 *   <li>Keys under {@link AdapterStateStore#HEALTH_KEY_PREFIX} and values written with
 *       {@link #putDurably} go to the persistent store at once: a health reason must reach health even when
 *       the fetch then fails, and spent upstream credits stay spent.
 *   <li>After {@link #commit} or {@link #discard} the store is closed: a late write from an adapter thread
 *       that outlived its fetch timeout is dropped, never applied.
 *   <li>{@link #commit} writes every staged change in one atomic statement when the delegate is a
 *       {@link PersistentAdapterStateStore}.
 * </ul>
 *
 * <p>Thread-safe: an adapter may use its context from several threads.
 */
public final class StagedAdapterStateStore implements AdapterStateStore {

    private static final Logger log = LoggerFactory.getLogger(StagedAdapterStateStore.class);

    private final AdapterStateStore delegate;

    /** Staged values; a key mapped to {@link Optional#empty()} is staged for removal. */
    private final Map<String, Optional<String>> staged = new LinkedHashMap<>();

    private boolean closed;

    /**
     * Wraps {@code delegate}.
     *
     * @param delegate the persistent store the staged writes are committed to
     */
    public StagedAdapterStateStore(AdapterStateStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<String> get(String key) {
        synchronized (this) {
            Optional<String> local = staged.get(key);
            if (local != null) {
                return local;
            }
        }
        return delegate.get(key);
    }

    @Override
    public void put(String key, String value) {
        if (value == null) {
            remove(key);
            return;
        }
        if (immediate(key)) {
            putDurably(key, value);
            return;
        }
        synchronized (this) {
            if (closed) {
                log.debug("Dropped a state write of {} made after the fetch ended", key);
                return;
            }
            staged.put(key, Optional.of(value));
        }
    }

    @Override
    public void remove(String key) {
        if (immediate(key)) {
            synchronized (this) {
                staged.remove(key);
            }
            delegate.remove(key);
            return;
        }
        synchronized (this) {
            if (closed) {
                log.debug("Dropped a state removal of {} made after the fetch ended", key);
                return;
            }
            staged.put(key, Optional.empty());
        }
    }

    /** Writes {@code value} to the persistent store at once, replacing any staged value of the key. */
    @Override
    public void putDurably(String key, String value) {
        if (value == null) {
            synchronized (this) {
                staged.remove(key);
            }
            delegate.remove(key);
            return;
        }
        synchronized (this) {
            staged.remove(key);
        }
        delegate.put(key, value);
    }

    /**
     * Writes every staged change to the persistent store and closes this store. Called by the executor once
     * the fetch's records are durably written (or the fetch had nothing to write).
     */
    public void commit() {
        Map<String, String> puts = new LinkedHashMap<>();
        Set<String> removes = new LinkedHashSet<>();
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            staged.forEach((key, value) -> {
                if (value.isPresent()) {
                    puts.put(key, value.get());
                } else {
                    removes.add(key);
                }
            });
            staged.clear();
        }
        if (delegate instanceof PersistentAdapterStateStore persistent) {
            persistent.apply(puts, removes);
            return;
        }
        removes.forEach(delegate::remove);
        puts.forEach(delegate::put);
    }

    /** Drops every staged change and closes this store. Called by the executor when the fetch failed. */
    public synchronized void discard() {
        if (!staged.isEmpty()) {
            log.debug("Discarded {} staged state write(s) of a failed fetch", staged.size());
        }
        closed = true;
        staged.clear();
    }

    /** The staged changes, a removal as an empty value; for tests and diagnostics. */
    public synchronized Map<String, Optional<String>> staged() {
        return Map.copyOf(staged);
    }

    /** True once the store was committed or discarded. */
    public synchronized boolean isClosed() {
        return closed;
    }

    private static boolean immediate(String key) {
        return key != null && key.startsWith(HEALTH_KEY_PREFIX);
    }
}
