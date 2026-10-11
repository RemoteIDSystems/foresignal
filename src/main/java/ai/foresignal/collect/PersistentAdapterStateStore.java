package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterStateStore;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * An adapter's state, kept in {@code source_health.adapter_state}.
 *
 * <p>Writes go in immediately, in their own small transaction. That matters for conditional GET:
 * the point of storing an ETag is that the *next* run can send it, and if the write were tied to
 * the fetch's transaction a failure later in the run would discard it and the next request would
 * re-download the whole body. The same reasoning applies to an upstream cursor — losing it means
 * re-reading a window we already consumed.
 *
 * <p>One instance per source, created by {@link UpstreamClient}.
 */
public class PersistentAdapterStateStore implements AdapterStateStore {

    private static final Logger log = LoggerFactory.getLogger(PersistentAdapterStateStore.class);

    private static final TypeReference<Map<String, String>> STATE_TYPE = new TypeReference<>() {};

    private final String sourceId;
    private final JdbcTemplate jdbc;
    private final JsonMapper jsonMapper;

    public PersistentAdapterStateStore(String sourceId, JdbcTemplate jdbc, JsonMapper jsonMapper) {
        this.sourceId = sourceId;
        this.jdbc = jdbc;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(load().get(key));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void put(String key, String value) {
        if (value == null) {
            remove(key);
            return;
        }
        Map<String, String> state = new LinkedHashMap<>(load());
        state.put(key, value);
        save(state);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void remove(String key) {
        Map<String, String> state = new LinkedHashMap<>(load());
        if (state.remove(key) != null) {
            save(state);
        }
    }

    /** Every stored key, for diagnostics. */
    public Map<String, String> all() {
        return load();
    }

    private Map<String, String> load() {
        try {
            String json =
                    jdbc.query(
                            "select adapter_state::text from source_health where source_id = ?",
                            rs -> rs.next() ? rs.getString(1) : null,
                            sourceId);
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            return jsonMapper.readValue(json, STATE_TYPE);
        } catch (RuntimeException e) {
            // Adapter state is a cache, not a source of truth: an unreadable value must not stop a
            // fetch, it should just make the next request unconditional.
            log.warn("Could not read the adapter state for {}; treating it as empty", sourceId, e);
            return Map.of();
        }
    }

    private void save(Map<String, String> state) {
        String json = jsonMapper.writeValueAsString(state);
        jdbc.update(
                """
                insert into source_health (source_id, consecutive_failures, recent_record_counts, adapter_state)
                values (?, 0, '[]'::jsonb, ?::jsonb)
                on conflict (source_id) do update set adapter_state = excluded.adapter_state
                """,
                sourceId,
                json);
    }
}
