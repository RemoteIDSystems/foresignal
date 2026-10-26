package ai.foresignal.notify;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A system condition to notify the operator about, published as a Spring application event by
 * components outside {@code notify} (prompt 019: retention consumer lag, canary failure). The
 * {@link NotificationRouter} turns it into a {@code system} notification for every channel that
 * subscribes to {@code system}, or for the channels its {@link Targets} name (prompt R2-014: resource and
 * operational alerts go to the admin's alert channels).
 *
 * @param subtype the notification subtype, for example {@code system.retention_consumer_lag}
 * @param severity {@code info}, {@code warning} or {@code critical}
 * @param title the title
 * @param body the markdown body, already escaped by the publisher where it embeds free text
 * @param path a Foresignal API path the notification links to (appended to the public base URL),
 *     or null
 * @param data structured details stored with the notification
 * @param targets which channels receive it
 */
public record SystemEvent(String subtype, String severity, String title, String body, String path,
        Map<String, Object> data, Targets targets) {

    public SystemEvent {
        Objects.requireNonNull(subtype, "subtype");
        severity = severity == null ? "warning" : severity;
        Objects.requireNonNull(title, "title");
        data = data == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(data));
        targets = targets == null ? Targets.SUBSCRIBERS : targets;
    }

    /** An event for every channel that subscribes to {@code system} (the behavior before prompt R2-014). */
    public SystemEvent(String subtype, String severity, String title, String body, String path,
            Map<String, Object> data) {
        this(subtype, severity, title, body, path, data, Targets.SUBSCRIBERS);
    }

    /**
     * The channels a system event is delivered to (prompt R2-014).
     *
     * @param subscribers whether every enabled instance channel subscribed to {@code system} receives it
     * @param channels further channels that receive it whatever their event filters, personal channels included
     */
    public record Targets(boolean subscribers, Set<UUID> channels) {

        /** Every instance channel subscribed to {@code system}, and nothing else. */
        public static final Targets SUBSCRIBERS = new Targets(true, Set.of());

        public Targets {
            channels = channels == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(channels));
        }

        /**
         * Exactly {@code channels}, whatever they subscribe to.
         *
         * @param channels the channels
         * @return the targets
         */
        public static Targets only(Collection<UUID> channels) {
            return new Targets(false, channels == null ? Set.of() : Set.copyOf(channels));
        }

        /**
         * The {@code system} subscribers and {@code channels} as well.
         *
         * @param channels the extra channels
         * @return the targets
         */
        public static Targets subscribersAnd(Collection<UUID> channels) {
            return new Targets(true, channels == null ? Set.of() : Set.copyOf(channels));
        }
    }
}
