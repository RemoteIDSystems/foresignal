package ai.foresignal.retention;

import ai.foresignal.store.NotificationDeliveryRepository;
import java.sql.Timestamp;
import java.time.Duration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Step 7 (Core-Data-Model §13, Architecture §11): the operational tables.
 *
 * <ul>
 *   <li>{@code alert}: resolved or suppressed alerts fired before {@code now − alerts}. Open alerts
 *       are kept whatever their age, because the trigger engine seeds cooldowns from them (017).
 *   <li>{@code source_usage}: minutes before {@code now − usage}.
 *   <li>{@code notification_delivery}: delivered or failed rows older than 7 days.
 *   <li>{@code notification}: rows created before {@code now − notifications} with no delivery left.
 * </ul>
 */
@Component
public class OperationalTablesStep implements RetentionStep {

    /** How long settled deliveries are kept. */
    static final Duration SETTLED_DELIVERY_KEEP = Duration.ofDays(7);

    private final NotificationDeliveryRepository deliveries;
    private final TransactionTemplate tx;

    /** Creates the step. */
    public OperationalTablesStep(NotificationDeliveryRepository deliveries, TransactionTemplate tx) {
        this.deliveries = deliveries;
        this.tx = tx;
    }

    @Override
    public String name() {
        return "operational-tables";
    }

    @Override
    public int order() {
        return 700;
    }

    @Override
    public void run(RetentionRun run) {
        var settings = run.settings();
        run.deleted("alert", run.jdbc().update(
                "DELETE FROM alert WHERE fired_at < :t AND (state = 'resolved' OR suppressed)",
                at(run, settings.alerts())));
        run.deleted("source_usage", run.jdbc().update(
                "DELETE FROM source_usage WHERE minute < :t", at(run, settings.usage())));
        Integer settled = tx.execute(status -> deliveries.deleteSettledBefore(run.now().minus(SETTLED_DELIVERY_KEEP)));
        run.deleted("notification_delivery", settled == null ? 0 : settled);
        run.deleted("notification", run.jdbc().update("""
                DELETE FROM notification n WHERE n.created_at < :t
                   AND NOT EXISTS (SELECT 1 FROM notification_delivery d WHERE d.notification_id = n.id)
                """, at(run, settings.notifications())));
    }

    private static MapSqlParameterSource at(RetentionRun run, Duration window) {
        return new MapSqlParameterSource("t", Timestamp.from(run.now().minus(window)));
    }
}
