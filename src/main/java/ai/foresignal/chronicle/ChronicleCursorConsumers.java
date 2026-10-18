package ai.foresignal.chronicle;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.retention.RetentionConsumerProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The durable cursors of the active chronicles as retention consumers (prompt 131, Conventions "durable cursors"):
 * one consumer named {@code chronicle.cursor.{id}} per active chronicle, so version retention is clamped to each
 * chronicle's capture position, including the clamp's release-after-2×-window rule with its alarm. A chronicle that is
 * paused, stopped or archived does not hold retention back. When the clamp is released and versions are lost, the
 * capture loop records the interval as a capture gap.
 */
@Component
public class ChronicleCursorConsumers implements RetentionConsumerProvider {

    private final JdbcTemplate jdbc;

    /** Creates the provider. */
    public ChronicleCursorConsumers(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<RetentionConsumer> retentionConsumers() {
        List<RetentionConsumer> out = new ArrayList<>();
        jdbc.query("SELECT id, cursor_seq FROM chronicle WHERE status = ? AND cursor_seq IS NOT NULL ORDER BY id", rs -> {
            String name = ChronicleVocabulary.cursorName(rs.getString(1));
            long cursor = rs.getLong(2);
            out.add(new RetentionConsumer() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public OptionalLong cursorSeq() {
                    return OptionalLong.of(cursor);
                }
            });
        }, ChronicleVocabulary.ACTIVE);
        return out;
    }
}
