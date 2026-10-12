package ai.foresignal.search;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes {@link SearchIndexState} ({@code app_setting['search.index']}) and owns the slot
 * lock.
 *
 * <p>Reads go to the table, not to {@link AppSettingService}'s cache: the state changes inside the
 * reindex's own transactions and is read concurrently by the worker and every search, and a value
 * cached from before a commit would never be refreshed. Writes go through
 * {@link AppSettingService#put} as for every other setting.
 *
 * <p>Writers of vector columns (the worker, the backfill, {@code embedDocument}) hold the
 * {@linkplain #slots() read lock} while they resolve a slot and write it; the reindex holds the
 * write lock while it resets, drops or swaps a slot, so a writer never writes a column that is being
 * dropped.
 */
@Component
public class SearchIndexStateStore {

    private final AppSettingService settings;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final ReadWriteLock slots = new ReentrantReadWriteLock();

    /** Creates the store. */
    public SearchIndexStateStore(AppSettingService settings, JdbcTemplate jdbc, JsonMapper json) {
        this.settings = settings;
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The current state ({@link SearchIndexState#EMPTY} when never written). */
    public SearchIndexState get() {
        List<String> values = jdbc.queryForList("SELECT value::text FROM app_setting WHERE key = ?", String.class,
                SettingKeys.SEARCH_INDEX);
        if (values.isEmpty() || values.getFirst() == null) {
            return SearchIndexState.EMPTY;
        }
        return json.readValue(values.getFirst(), SearchIndexState.class);
    }

    /** Replaces the state in one settings write. */
    public void put(SearchIndexState state) {
        settings.put(SettingKeys.SEARCH_INDEX, state);
    }

    /** The slot lock: read to write vectors, write to change slots. */
    public ReadWriteLock slots() {
        return slots;
    }
}
