package ai.foresignal.notify.push;

import java.time.Duration;
import java.util.Objects;

/**
 * Delivery options of one push (prompt 031), mapped by Web Push to RFC 8030 headers.
 *
 * @param ttl how long the push service keeps an undelivered message ({@code TTL}, §5.2)
 * @param urgency the {@code Urgency} (§5.3)
 * @param topic the {@code Topic} (§5.4): an undelivered message with the same topic is replaced; or null
 * @param silent whether the notification should not make a sound (quiet-hours downgrade)
 * @param downgraded whether quiet hours downgraded this push (APNs priority 5 and {@code passive},
 *     FCM {@code NORMAL}; prompt 032)
 * @param dispatchId the {@code push_dispatch} row being sent, or null outside the dispatch worker
 *     (APNs {@code apns-id}; prompt 032)
 */
public record PushOptions(Duration ttl, Urgency urgency, String topic, boolean silent, boolean downgraded,
        Long dispatchId) {

    /** TTL of alerts. */
    public static final Duration ALERT_TTL = Duration.ofSeconds(86_400);

    /** TTL of briefings. */
    public static final Duration BRIEFING_TTL = Duration.ofSeconds(43_200);

    /** TTL of system events. */
    public static final Duration SYSTEM_TTL = Duration.ofSeconds(3_600);

    /** TTL of the verification push. */
    public static final Duration VERIFY_TTL = Duration.ofSeconds(600);

    /** Requires a TTL and an urgency. */
    public PushOptions {
        Objects.requireNonNull(ttl, "ttl");
        Objects.requireNonNull(urgency, "urgency");
    }

    /** Options without a downgrade flag or dispatch id (031 form). */
    public PushOptions(Duration ttl, Urgency urgency, String topic, boolean silent) {
        this(ttl, urgency, topic, silent, false, null);
    }

    /** These options for the dispatch row {@code id}. */
    public PushOptions withDispatchId(Long id) {
        return new PushOptions(ttl, urgency, topic, silent, downgraded, id);
    }

    /** The TTL of an event: alert 86400 s, briefing 43200 s, system 3600 s, verify 600 s. */
    public static Duration ttlOf(String event) {
        return switch (event == null ? "" : event) {
            case "alert" -> ALERT_TTL;
            case "briefing" -> BRIEFING_TTL;
            case "verify" -> VERIFY_TTL;
            default -> SYSTEM_TTL;
        };
    }

    /**
     * The options of a message: TTL by event, urgency by severity, and under a quiet-hours downgrade
     * {@code very-low} and silent.
     */
    public static PushOptions of(PushMessage message, boolean downgraded) {
        if ("verify".equals(message.event())) {
            return new PushOptions(VERIFY_TTL, Urgency.HIGH, null, false);
        }
        Urgency urgency = downgraded ? Urgency.VERY_LOW : Urgency.of(message.severity());
        return new PushOptions(ttlOf(message.event()), urgency, message.topic(), downgraded || message.silent(),
                downgraded, null);
    }
}
