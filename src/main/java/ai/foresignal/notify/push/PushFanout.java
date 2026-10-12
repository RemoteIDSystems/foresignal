package ai.foresignal.notify.push;

import ai.foresignal.notify.FanOutDelivery;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationDeliveryEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Splits a claimed delivery of the built-in Push channel into one {@code push_dispatch} row per
 * matching target (prompt 031).
 *
 * <p>Every {@link PushTargetSource} bean lists its active targets whose filters accept the message
 * ({@link WebPushTargetSource} now, native devices in 032). The transport-neutral
 * {@link PushMessage} is built once and stored with each row, so the {@link PushDispatchWorker}
 * sends and retries each target independently. The parent delivery stays pending, not due, until
 * every dispatch is terminal; with no targets it is delivered at once.
 */
@Component
public class PushFanout implements FanOutDelivery {

    private static final Logger log = LoggerFactory.getLogger(PushFanout.class);

    private final List<PushTargetSource> sources;
    private final PushContentFactory content;
    private final PushDispatchStore store;

    /** Creates the fan-out. */
    public PushFanout(List<PushTargetSource> sources, PushContentFactory content, PushDispatchStore store) {
        this.sources = List.copyOf(sources);
        this.content = content;
        this.store = store;
    }

    @Override
    public boolean handles(String channelType) {
        return PushChannelType.ID.equals(channelType);
    }

    @Override
    public void fanOut(NotificationDeliveryEntity delivery, NotificationMessage message,
            NotificationChannelEntity channel, boolean quietDowngrade, Instant now) {
        fanOut(delivery, message, channel, quietDowngrade, now, List.of());
    }

    /**
     * Fans out with the delivery's action capabilities (prompt 033): Web Push targets get them as
     * buttons; native targets get the actionable category and fetch their own capabilities from
     * {@code GET /v1/notifications/{id}}.
     */
    @Override
    public void fanOut(NotificationDeliveryEntity delivery, NotificationMessage message,
            NotificationChannelEntity channel, boolean quietDowngrade, Instant now, List<NotificationAction> actions) {
        List<PushTarget> targets = targets(message);
        int inserted = store.fanOut(delivery, targets, content.of(message, actions).toMap(), quietDowngrade, now);
        log.debug("Delivery {} fanned out to {} push targets", delivery.getId(), inserted);
    }

    /** The active targets of every source that accept {@code message}, without duplicates. */
    public List<PushTarget> targets(NotificationMessage message) {
        Set<String> seen = new LinkedHashSet<>();
        List<PushTarget> out = new ArrayList<>();
        for (PushTargetSource source : sources) {
            try {
                for (PushTarget target : source.targets(message)) {
                    if (seen.add(target.type() + ":" + target.id())) {
                        out.add(target);
                    }
                }
            } catch (RuntimeException e) {
                log.error("Push target source {} failed; its targets miss this notification", source.targetType(), e);
            }
        }
        return out;
    }
}
