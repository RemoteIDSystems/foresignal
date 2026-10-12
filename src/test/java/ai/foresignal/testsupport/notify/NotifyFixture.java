package ai.foresignal.testsupport.notify;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.json.JsonMapper;

/**
 * Helpers of the notification integration tests (prompt 018): channels from JSON (single quotes
 * allowed), alerts as {@link AlertView}s without running the trigger engine, and delivery queries.
 */
public final class NotifyFixture {

    /** A lenient mapper for test JSON written with single quotes. */
    public static final JsonMapper JSON = JsonMapper.builder().build();

    private final ChannelService channels;
    private final NotificationDeliveryRepository deliveries;

    /** Creates the fixture and resets the per-channel rate buckets. */
    public NotifyFixture(ChannelService channels, NotificationDeliveryRepository deliveries, ChannelRateLimiter limiter) {
        this.channels = channels;
        this.deliveries = deliveries;
        limiter.reset();
    }

    /** Creates a channel from JSON in which single quotes stand for double quotes. */
    public UUID channel(String json) {
        return channels.create(JSON.readTree(json.replace('\'', '"'))).channel().id();
    }

    /** Every delivery of {@code channel}, oldest first. */
    public List<NotificationDeliveryEntity> deliveries(UUID channel) {
        return deliveries.findAll().stream().filter(d -> d.getChannelId().equals(channel))
                .sorted(Comparator.comparing(NotificationDeliveryEntity::getId)).toList();
    }

    /** The pending deliveries of {@code channel}, oldest first. */
    public List<NotificationDeliveryEntity> pending(UUID channel) {
        return deliveries(channel).stream().filter(d -> d.getStatus() == DeliveryStatus.PENDING).toList();
    }

    /** An open alert as the trigger engine would publish it. */
    public static AlertView alert(long id, UUID ruleId, String ruleName, String severity, Instant at) {
        return new AlertView(id, ruleId, ruleName, 100 + id, "usgs-earthquakes", "us" + id, "earthquake", at, at,
                "M5." + id + " near Austin", severity, "firing", id, null, null, null, null, null, null, null, null, null,
                false, null, List.of("quake"), "{\"kind\":\"earthquake\",\"payload\":{\"mag\":5.1}}");
    }

    /** {@code alert} resolved at {@code at} by {@code by}. */
    public static AlertView resolved(AlertView a, Instant at, String by) {
        return new AlertView(a.id(), a.ruleId(), a.ruleName(), a.seq(), a.sourceId(), a.externalId(), a.kind(), a.firedAt(),
                a.observedAt(), a.message(), a.severity(), "resolved", a.rev() + 1, a.acknowledgedAt(), a.acknowledgedBy(),
                at, by, null, null, null, null, null, false, null, a.tags(), a.recordSnapshot());
    }

    /** {@code alert} acknowledged at {@code at}. */
    public static AlertView acknowledged(AlertView a, Instant at) {
        return new AlertView(a.id(), a.ruleId(), a.ruleName(), a.seq(), a.sourceId(), a.externalId(), a.kind(), a.firedAt(),
                a.observedAt(), a.message(), a.severity(), "acknowledged", a.rev() + 1, at, "admin", null, null, null, null,
                null, null, null, false, null, a.tags(), a.recordSnapshot());
    }
}
