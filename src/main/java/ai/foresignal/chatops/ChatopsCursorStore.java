package ai.foresignal.chatops;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Reads and writes {@code chatops_cursor} (prompt 061). */
@Component
public class ChatopsCursorStore {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the store. */
    public ChatopsCursorStore(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** The cursor of {@code channelId}, if one was stored. */
    public Optional<String> get(UUID channelId) {
        List<String> rows = jdbc.queryForList("SELECT cursor FROM chatops_cursor WHERE channel_id = ?", String.class,
                channelId);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.getFirst());
    }

    /** Stores the cursor of {@code channelId}. */
    public void put(UUID channelId, String cursor) {
        jdbc.update("""
                INSERT INTO chatops_cursor (channel_id, cursor, updated_at) VALUES (?, ?, ?)
                ON CONFLICT (channel_id) DO UPDATE SET cursor = EXCLUDED.cursor, updated_at = EXCLUDED.updated_at
                """, channelId, cursor, Timestamp.from(clock.instant()));
    }

    /** Forgets the cursor of a deleted channel. */
    public void delete(UUID channelId) {
        jdbc.update("DELETE FROM chatops_cursor WHERE channel_id = ?", channelId);
    }
}
