package ai.foresignal.intel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fills {@code intelligence_event.member_count} for events stored before the column existed (checkpoint 124), so
 * their summaries carry the real {@code memberCount} instead of 0. Idempotent: only rows where it is null are touched,
 * and {@link IntelEventService#rescore} keeps it current afterwards.
 */
@Component
public class IntelMemberCountBackfill {

    private static final Logger log = LoggerFactory.getLogger(IntelMemberCountBackfill.class);

    private final JdbcTemplate jdbc;

    /** Creates the backfill. */
    public IntelMemberCountBackfill(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Counts the members of every event whose count is unknown. */
    @EventListener(ApplicationReadyEvent.class)
    public void backfill() {
        try {
            int updated = jdbc.update("UPDATE intelligence_event e SET member_count = (SELECT count(*) FROM"
                    + " intelligence_event_member m WHERE m.event_id = e.id) WHERE e.member_count IS NULL");
            if (updated > 0) {
                log.info("Counted the members of {} intelligence events", updated);
            }
        } catch (RuntimeException e) {
            log.warn("Could not backfill intelligence event member counts: {}", e.toString());
        }
    }
}
