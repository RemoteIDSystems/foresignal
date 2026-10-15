package ai.foresignal.entity;

import ai.foresignal.ingest.IngestStateReset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * An in-memory expansion of each entity's open registry keys (prompt 098), read by the
 * {@code inWatchlist} operator for {@code entity:<ulid>} items. Refreshed on
 * {@link EntityKeysChangedEvent}, so an {@code entity:} watchlist item follows a key re-assignment.
 *
 * <p>Callsign keys are left out: an {@code air:} callsign is a flight-session link whose value is
 * domain-prefixed, so it never equals a raw payload callsign.
 */
@Component
public class EntityKeyExpansion implements IngestStateReset {

    /** The key types an {@code entity:} watchlist item expands to. */
    static final Set<String> EXPANDED_TYPES = Set.of("icao24", "registration", "mmsi", "imo");

    private final JdbcTemplate jdbc;
    private final Map<String, Set<String>> byEntity = new ConcurrentHashMap<>();
    private volatile boolean loaded;

    /** Creates the expansion; it loads when the application is ready. */
    public EntityKeyExpansion(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Loads every entity's open keys. */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void loadAll() {
        Map<String, Set<String>> fresh = new HashMap<>();
        jdbc.query("SELECT entity_id, key_value FROM entity_key WHERE valid_to IS NULL AND key_type = ANY(?)",
                rs -> {
                    String entityId = rs.getString("entity_id");
                    fresh.computeIfAbsent(entityId, id -> new HashSet<>()).add(rs.getString("key_value"));
                },
                typesArray());
        byEntity.clear();
        byEntity.putAll(fresh);
        loaded = true;
    }

    /** Reloads one entity's open keys after they changed. */
    @EventListener
    public void onEntityKeysChanged(EntityKeysChangedEvent event) {
        Set<String> values = new HashSet<>(jdbc.queryForList(
                "SELECT key_value FROM entity_key WHERE entity_id = ? AND valid_to IS NULL AND key_type = ANY(?)",
                String.class, event.entityId(), typesArray()));
        if (values.isEmpty()) {
            byEntity.remove(event.entityId());
        } else {
            byEntity.put(event.entityId(), values);
        }
    }

    /** Reloads everything (the test cleaner truncated the tables). */
    @Override
    public void resetState() {
        loadAll();
    }

    /** The union of the given entities' open registry key values. */
    public Set<String> valuesFor(Collection<String> entityIds) {
        if (!loaded) {
            loadAll();
        }
        Set<String> values = new LinkedHashSet<>();
        for (String entityId : entityIds) {
            Set<String> mine = byEntity.get(entityId);
            if (mine != null) {
                values.addAll(mine);
            }
        }
        return values;
    }

    private static String typesArray() {
        return ai.foresignal.store.query.PgArrays.text(EXPANDED_TYPES);
    }
}
