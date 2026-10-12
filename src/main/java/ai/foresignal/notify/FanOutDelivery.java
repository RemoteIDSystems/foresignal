package ai.foresignal.notify;

import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationDeliveryEntity;
import java.time.Instant;

/**
 * Takes over a claimed delivery of a fan-out channel type ({@link ChannelCapabilities#fanOut()},
 * prompt 031): instead of rendering one request, the delivery is split into per-target sends that
 * settle it later. {@code PushFanout} handles {@code webpush}; prompt 060 adds MQTT the same way.
 */
public interface FanOutDelivery {

    /** True when this handler takes deliveries of channels of {@code channelType}. */
    boolean handles(String channelType);

    /**
     * Fans {@code delivery} out. The handler owns the delivery's state from here: it leaves it
     * pending (not due, not leased) until every per-target send is terminal, or settles it at once
     * when there are no targets.
     *
     * @param message the message as rendered for the channel (templates, digest)
     * @param quietDowngrade true when quiet hours downgrade rather than defer this send
     */
    void fanOut(NotificationDeliveryEntity delivery, NotificationMessage message, NotificationChannelEntity channel,
            boolean quietDowngrade, Instant now);
}
