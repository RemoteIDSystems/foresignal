package ai.foresignal.notify;

import ai.foresignal.store.NotificationEntity;
import ai.foresignal.store.NotificationRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Writes and reads {@code notification} rows (prompt 018).
 *
 * <p>{@link #write} runs a unit of work (persist a notification, enqueue its deliveries) in one
 * transaction while holding a process-wide lock, and publishes the persisted messages on the
 * {@link NotificationBus} after the commit, still under the lock. Notification ids are identity
 * values, so this is what keeps the SSE topics' cursors strictly increasing in publish order. A
 * second Foresignal process writing the same database would need an advisory lock instead (the same
 * single-node assumption as {@code AlertStore}).
 */
@Component
public class NotificationStore {

    private final NotificationRepository repository;
    private final NotificationBus bus;
    private final TransactionTemplate tx;
    private final ReentrantLock lock = new ReentrantLock(true);

    /** Creates the store. */
    public NotificationStore(NotificationRepository repository, NotificationBus bus,
            PlatformTransactionManager transactions) {
        this.repository = repository;
        this.bus = bus;
        this.tx = new TransactionTemplate(transactions);
    }

    /** Collects the messages a unit of work persisted, for publishing after commit. */
    public final class Writer {
        private final List<NotificationMessage> persisted = new ArrayList<>();

        /** Persists {@code message} and returns it with its id and creation time. */
        public NotificationMessage persist(NotificationMessage message) {
            NotificationEntity saved = repository.save(toEntity(message));
            NotificationMessage out = toMessage(saved);
            persisted.add(out);
            return out;
        }
    }

    /** Runs {@code work} in one transaction under the ordering lock, then publishes what it persisted. */
    public <T> T write(Function<Writer, T> work) {
        lock.lock();
        try {
            Writer writer = new Writer();
            T result = tx.execute(status -> work.apply(writer));
            for (NotificationMessage message : writer.persisted) {
                bus.publish(message);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Replaces the title, body and data of a stored notification (prompt 030: the enrichment summary
     * arrived). Nothing is republished; deliveries read the row when they send.
     *
     * @return true when the row exists
     */
    public boolean refresh(long id, String title, String body, Map<String, Object> data) {
        Boolean found = tx.execute(status -> repository.findById(id).map(e -> {
            e.setTitle(title);
            e.setBodyMarkdown(body);
            e.setData(data == null ? null : new LinkedHashMap<>(data));
            repository.save(e);
            return true;
        }).orElse(false));
        return Boolean.TRUE.equals(found);
    }

    /** The notification with {@code id}, if any. */
    public Optional<NotificationMessage> find(long id) {
        return repository.findById(id).map(NotificationStore::toMessage);
    }

    /** The {@code alert.fired} notification of an alert, if any. */
    public Optional<NotificationMessage> fired(long alertId) {
        return repository.findFirstByAlertIdAndSubtypeOrderByIdAsc(alertId, "alert.fired").map(NotificationStore::toMessage);
    }

    /** The entity form of {@code m} (its id is ignored; the database assigns one). */
    static NotificationEntity toEntity(NotificationMessage m) {
        NotificationEntity e = new NotificationEntity();
        e.setEvent(m.event());
        e.setSubtype(m.subtype());
        e.setSeverity(m.severity());
        e.setTitle(m.title());
        e.setShortTitle(m.shortTitle());
        e.setBodyMarkdown(m.bodyMarkdown());
        e.setUrl(m.url());
        List<Map<String, Object>> links = new ArrayList<>();
        for (NotificationMessage.Link link : m.links()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", link.label());
            item.put("url", link.url());
            links.add(item);
        }
        e.setLinks(links);
        e.setData(m.data() == null ? null : new LinkedHashMap<>(m.data()));
        e.setAlertId(m.alertId());
        e.setBriefingRunId(m.briefingRunId());
        e.setRuleId(m.ruleId());
        e.setTags(m.tags().toArray(String[]::new));
        e.setUpdateOf(m.updateOf());
        e.setOrigin(m.origin());
        e.setCreatedAt(m.createdAt());
        return e;
    }

    /** The message form of a stored row. */
    public static NotificationMessage toMessage(NotificationEntity e) {
        List<NotificationMessage.Link> links = new ArrayList<>();
        if (e.getLinks() != null) {
            for (Map<String, Object> item : e.getLinks()) {
                Object label = item.get("label");
                Object url = item.get("url");
                if (label != null && url != null) {
                    links.add(new NotificationMessage.Link(String.valueOf(label), String.valueOf(url)));
                }
            }
        }
        return new NotificationMessage(e.getId(), e.getEvent(), e.getSubtype(), e.getTitle(), e.getShortTitle(),
                e.getBodyMarkdown(), e.getSeverity(), e.getUrl(), links, e.getData(),
                e.getTags() == null ? List.of() : Arrays.asList(e.getTags()), List.of(), e.getUpdateOf(), e.getOrigin(),
                e.getCreatedAt(), e.getAlertId(), e.getRuleId(), e.getBriefingRunId());
    }
}
