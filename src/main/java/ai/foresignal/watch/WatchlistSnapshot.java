package ai.foresignal.watch;

import ai.foresignal.ingest.IngestStateReset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Every watchlist in memory (prompt 036): {@code listId → (identity, normalization, values)}, read by
 * the {@code inWatchlist} operators' Java evaluation (the persist gate, streams). Refreshed per list on
 * {@link WatchlistChangedEvent}, so an edit takes effect on each entity's next observation.
 */
@Component
public class WatchlistSnapshot implements IngestStateReset {

    /**
     * One list.
     *
     * @param identity what it identifies
     * @param normalize how values are normalized
     * @param values the normalized values
     */
    public record ListSnapshot(Identity identity, Identity.Normalize normalize, Set<String> values) {

        /** Copies the values. */
        public ListSnapshot {
            values = Set.copyOf(values);
        }
    }

    private final JdbcTemplate jdbc;
    private final Map<UUID, ListSnapshot> lists = new ConcurrentHashMap<>();
    private volatile boolean loaded;

    /** Creates the snapshot; it loads when the application is ready. */
    public WatchlistSnapshot(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Loads every list. */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void loadAll() {
        Map<UUID, ListSnapshot> fresh = new HashMap<>();
        for (UUID id : jdbc.queryForList("SELECT id FROM watch_list", UUID.class)) {
            load(id).ifPresent(s -> fresh.put(id, s));
        }
        lists.clear();
        lists.putAll(fresh);
        loaded = true;
    }

    /** Reloads one list after it changed. */
    @EventListener
    public void onChanged(WatchlistChangedEvent event) {
        refresh(event.listId());
    }

    /** Reloads one list (or forgets it when it no longer exists). */
    public void refresh(UUID listId) {
        Optional<ListSnapshot> snapshot = load(listId);
        if (snapshot.isPresent()) {
            lists.put(listId, snapshot.get());
        } else {
            lists.remove(listId);
        }
    }

    /** Reloads everything (the test cleaner truncated the tables). */
    @Override
    public void resetState() {
        loadAll();
    }

    /** The list, if it exists. */
    public Optional<ListSnapshot> list(UUID listId) {
        if (!loaded) {
            loadAll();
        }
        return Optional.ofNullable(lists.get(listId));
    }

    /** True when {@code normalized} is on the list (false for an unknown list). */
    public boolean contains(UUID listId, String normalized) {
        return list(listId).map(s -> s.values().contains(normalized)).orElse(false);
    }

    private Optional<ListSnapshot> load(UUID id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT identity, normalize FROM watch_list WHERE id = ?", id);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Identity identity = Identity.fromWire((String) rows.getFirst().get("identity"));
        Identity.Normalize normalize = Identity.Normalize.fromWire((String) rows.getFirst().get("normalize"));
        Set<String> values = new HashSet<>(jdbc.queryForList("SELECT value FROM watch_list_item WHERE list_id = ?",
                String.class, id));
        return Optional.of(new ListSnapshot(identity, normalize, values));
    }
}
