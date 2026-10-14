package ai.foresignal.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Lets {@code notification_delivery.status} hold {@code SUPPRESSED} (prompt 033) and {@code UNACKED} (prompt 084)
 * on databases created before them.
 *
 * <p>Hibernate created the column with a check constraint listing the enum values of the time
 * ({@code PENDING}, {@code DELIVERED}, {@code FAILED}), and {@code ddl-auto: update} never rewrites a
 * constraint. The constraint is dropped once at start; the enum ({@code DeliveryStatus}) is what
 * keeps the values valid. On a fresh database Hibernate creates it with every value, and dropping it
 * changes nothing that matters. Prompt 144's Flyway baseline owns this from then on.
 */
@Component
public class DeliveryStatusConstraint {

    private static final Logger log = LoggerFactory.getLogger(DeliveryStatusConstraint.class);

    static final String DROP =
            "alter table if exists notification_delivery drop constraint if exists notification_delivery_status_check";

    private final JdbcTemplate jdbc;

    /** Creates the fixer. */
    public DeliveryStatusConstraint(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Drops the stale check constraint, before any background service starts. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void relax() {
        try {
            jdbc.execute(DROP);
        } catch (RuntimeException e) {
            log.warn("Could not relax the notification_delivery status constraint: {}", e.getMessage());
        }
    }
}
