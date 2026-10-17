package ai.foresignal.collab;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Delivers a notification to one principal (prompt 130): an item in the principal's inbox ({@code GET /v1/inbox}),
 * an event on the principal's {@code me} SSE topic, and a delivery through 018's worker to every enabled personal
 * channel whose preference carries the event type. When called inside a transaction, delivery waits for its commit,
 * so a rolled-back share notifies no one. The TLP label is printed at the head of every channel message.
 */
@Component
public class PersonalNotifier {

    private static final Logger log = LoggerFactory.getLogger(PersonalNotifier.class);

    private final InboxItemRepository items;
    private final MeTopic topic;
    private final PrincipalNotificationRepository preferences;
    private final NotificationService notifications;
    private final JsonMapper json;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final ReentrantLock ordering = new ReentrantLock();

    /** Creates the component. */
    public PersonalNotifier(InboxItemRepository items, MeTopic topic, PrincipalNotificationRepository preferences,
            NotificationService notifications, JsonMapper json, PlatformTransactionManager transactions, Clock clock) {
        this.items = items;
        this.topic = topic;
        this.preferences = preferences;
        this.notifications = notifications;
        this.json = json;
        this.tx = new TransactionTemplate(transactions);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
    }

    /**
     * One personal notification.
     *
     * @param principalId the addressee
     * @param event a {@link MeEvents} event
     * @param title the title
     * @param body the markdown body, or null
     * @param ref the object it is about, or null
     * @param actorId the acting principal, or null
     * @param tlp the object's TLP label, or null
     * @param payload the {@code me} topic payload
     */
    public record Notice(String principalId, String event, String title, String body, String ref, String actorId,
            String tlp, Map<String, Object> payload) {}

    /** Sends {@code notice}, after the current transaction commits when there is one. */
    public void send(Notice notice) {
        if (notice == null || notice.principalId() == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deliver(notice);
                }
            });
        } else {
            deliver(notice);
        }
    }

    /** Writes the inbox item, publishes it on {@code me} and enqueues the channel deliveries. */
    void deliver(Notice notice) {
        try {
            ordering.lock();
            try {
                InboxItemEntity item = tx.execute(status -> {
                    InboxItemEntity row = new InboxItemEntity(notice.principalId(), notice.event(),
                            MeEvents.category(notice.event()), notice.title(), clock.instant());
                    row.setBody(notice.body());
                    row.setObjectRef(notice.ref());
                    row.setActorId(notice.actorId());
                    row.setTlp(notice.tlp());
                    row.setPayload(plain(notice.payload()));
                    InboxItemEntity saved = items.saveAndFlush(row);
                    channels(notice);
                    return saved;
                });
                if (item != null) {
                    topic.publish(item);
                }
            } finally {
                ordering.unlock();
            }
        } catch (RuntimeException e) {
            log.warn("Personal notification {} to {} failed: {}", notice.event(), notice.principalId(), e.toString());
        }
    }

    private void channels(Notice notice) {
        String type = MeEvents.preferenceType(notice.event());
        if (type == null) {
            return;
        }
        for (PrincipalNotificationEntity pref : preferences.findByPrincipalIdOrderByCreatedAtAscIdAsc(
                notice.principalId())) {
            if (!pref.isEnabled() || !pref.getEventTypes().contains(type)) {
                continue;
            }
            String label = notice.tlp() == null ? "" : "[TLP:" + notice.tlp() + "] ";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("principalId", notice.principalId());
            data.put("ref", notice.ref());
            data.put("tlp", notice.tlp());
            NotificationMessage message = NotificationMessage.builder(ChannelService.PERSONAL_EVENT, notice.event(),
                    label + notice.title(), "info")
                    .body(notice.body() == null ? null : label + notice.body())
                    .data(data)
                    .build();
            notifications.publishPersonal(message, pref.getChannelId());
        }
    }

    private Map<String, Object> plain(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        return json.convertValue(payload, new TypeReference<Map<String, Object>>() {});
    }
}
