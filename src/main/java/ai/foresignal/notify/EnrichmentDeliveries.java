package ai.foresignal.notify;

import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.EnrichmentProperties;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * What a stored enrichment summary does to an alert's notifications (prompt 030; Architecture §17
 * and §23). Called by the {@link NotificationRouter} for each {@code enriched} alert event, after it
 * has handled the alert's {@code fired} event, so the deliveries it looks for exist.
 *
 * <ol>
 *   <li><b>The persisted notification</b> (the {@code alert.fired} row the inbox reads) gets the
 *       summary in its {@code data} and its default body re-rendered, so lifecycle updates built on it
 *       keep the summary too.
 *   <li><b>Still waiting:</b> the alert's pending sends held by the channel's enrichment wait
 *       ({@code deferred_reason=enrichment}, {@code next_attempt_at} in the future) are due now. The
 *       delivery worker reads the latest alert when it builds the message, so they go out with the
 *       summary.
 *   <li><b>Already sent without it:</b> for each delivered send of that notification that went out
 *       before the summary was stored, less than {@link EnrichmentProperties#editWindow()} (1 h) ago, to
 *       a channel whose type edits in place ({@code discord}, {@code telegram}, {@code matrix}) with
 *       {@code edit_in_place} on and a stored provider message reference, one {@code update} delivery is
 *       enqueued that edits the message to include the summary. Other channels get nothing more: no
 *       follow-up message.
 * </ol>
 */
@Component
public class EnrichmentDeliveries {

    static final String PULL_FORWARD = """
            update notification_delivery set next_attempt_at = :now
             where notification_id = :notification and status = 'PENDING' and kind = 'send'
               and deferred_reason = :reason and next_attempt_at > :now and folded_into is null
               and (leased_until is null or leased_until <= :now)
            """;

    private final NotificationStore notifications;
    private final NotificationDeliveryRepository deliveries;
    private final NotificationChannelRepository channels;
    private final ChannelTypeRegistry types;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final EnrichmentProperties properties;
    private final Clock clock;

    /** Creates the component. */
    public EnrichmentDeliveries(NotificationStore notifications, NotificationDeliveryRepository deliveries,
            NotificationChannelRepository channels, ChannelTypeRegistry types, NamedParameterJdbcTemplate jdbc, PlatformTransactionManager transactions, EnrichmentProperties properties,
            Clock clock) {
        this.notifications = notifications;
        this.deliveries = deliveries;
        this.channels = channels;
        this.types = types;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * What happened.
     *
     * @param notificationId the alert's {@code alert.fired} notification, or null when there is none
     * @param pulledForward waiting sends made due now
     * @param edits update deliveries enqueued
     */
    public record Result(Long notificationId, int pulledForward, int edits) {}

    /**
     * Applies a stored summary.
     *
     * @param alert the enriched alert
     * @param data the alert JSON of the notification, with the summary
     * @param title the default title
     * @param body the default body, with the summary
     */
    public Result summaryArrived(AlertView alert, Map<String, Object> data, String title, String body) {
        if (alert.summary() == null) {
            return new Result(null, 0, 0);
        }
        Optional<NotificationMessage> fired = notifications.fired(alert.id());
        if (fired.isEmpty()) {
            return new Result(null, 0, 0);
        }
        long notificationId = fired.get().id();
        notifications.refresh(notificationId, title, body, data);
        Instant now = clock.instant();
        Instant enrichedAt = alert.enrichedAt() == null ? now : alert.enrichedAt();
        Result result = tx.execute(status -> {
            int pulled = jdbc.update(PULL_FORWARD, new MapSqlParameterSource("notification", notificationId)
                    .addValue("reason", DeferredReason.ENRICHMENT)
                    .addValue("now", Timestamp.from(now)));
            int edits = 0;
            List<NotificationDeliveryEntity> rows = deliveries.findByNotificationIdOrderByIdAsc(notificationId);
            Set<UUID> pendingEdits = new HashSet<>();
            for (NotificationDeliveryEntity d : rows) {
                if (DeliveryKind.UPDATE.equals(d.getKind()) && d.getStatus() == DeliveryStatus.PENDING) {
                    pendingEdits.add(d.getChannelId());
                }
            }
            Set<UUID> done = new HashSet<>();
            for (NotificationDeliveryEntity original : rows) {
                if (!editable(original, now, enrichedAt) || pendingEdits.contains(original.getChannelId())
                        || !done.add(original.getChannelId())) {
                    continue;
                }
                Optional<NotificationChannelEntity> channel = channels.findById(original.getChannelId());
                if (channel.isEmpty() || !channel.get().isEnabled() || !channel.get().isEditInPlace()) {
                    continue;
                }
                Optional<ChannelType> type = types.find(channel.get().getType());
                if (type.isEmpty() || !type.get().capabilities().editInPlace()) {
                    continue;
                }
                NotificationDeliveryEntity edit = new NotificationDeliveryEntity(original.getChannelId(), notificationId,
                        original.getEvent(), now);
                edit.setKind(DeliveryKind.UPDATE);
                edit.setAlertId(alert.id());
                edit.setNote("adds the enrichment summary");
                // Quiet hours and the pause are applied by the delivery worker when it sends.
                deliveries.save(edit);
                edits++;
            }
            return new Result(notificationId, pulled, edits);
        });
        return result == null ? new Result(notificationId, 0, 0) : result;
    }

    /** A delivered, unfolded send with a message reference, sent before the summary and recently enough. */
    private boolean editable(NotificationDeliveryEntity d, Instant now, Instant enrichedAt) {
        return d.getStatus() == DeliveryStatus.DELIVERED
                && DeliveryKind.SEND.equals(d.getKind())
                && d.getFoldedInto() == null
                && d.getProviderMessageRef() != null
                && d.getDeliveredAt() != null
                && d.getDeliveredAt().isBefore(enrichedAt)
                && d.getDeliveredAt().isAfter(now.minus(properties.editWindow()));
    }
}
