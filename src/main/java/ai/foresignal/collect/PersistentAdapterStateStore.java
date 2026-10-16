package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterStateStore;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * An adapter's state, kept in {@code source_health.adapter_state}.
 *
 * <p>Every write here goes in immediately, as its own auto-committed statement (this class is not a Spring
 * bean, so it joins no surrounding transaction). A polling fetch run by the {@link FetchExecutor} does not
 * write here directly: its context wraps this store in a {@link StagedAdapterStateStore}, which holds the
 * fetch's cursors and validators until the fetch's records are written and then commits them in one
 * statement through {@link #apply}. Streams, tests and direct callers write through.
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
    public void remove(String key) {
        Map<String, String> state = new LinkedHashMap<>(load());
        if (state.remove(key) != null) {
            save(state);
        }
    }

    /**
     * Applies a fetch's staged writes in one atomic statement: removes {@code removes}, then sets {@code puts}
     * (F1 of the 2026-10 review). The merge happens in the database ({@code (state - removes) || puts}), so a
     * health key written through during the fetch is never overwritten by a stale copy.
     *
     * @param puts the keys to set, with their values
     * @param removes the keys to remove; a key in both is set
     */
    public void apply(Map<String, String> puts, java.util.Set<String> removes) {
        if (puts.isEmpty() && removes.isEmpty()) {
            return;
        }
        String json = jsonMapper.writeValueAsString(puts);
        String[] removed = removes.toArray(String[]::new);
        jdbc.update(con -> {
            java.sql.PreparedStatement ps = con.prepareStatement(
                    """
                    insert into source_health (source_id, consecutive_failures, recent_record_counts, adapter_state)
                    values (?, 0, '[]'::jsonb, ?::jsonb)
                    on conflict (source_id) do update set adapter_state =
                        (coalesce(source_health.adapter_state, '{}'::jsonb) - ?::text[]) || ?::jsonb
                    """);
            ps.setString(1, sourceId);
            ps.setString(2, json);
            ps.setArray(3, con.createArrayOf("text", removed));
            ps.setString(4, json);
            return ps;
        });
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
