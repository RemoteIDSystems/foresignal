package ai.foresignal.notify;

import java.util.Map;
import java.util.Objects;

/**
 * A system condition to notify the operator about, published as a Spring application event by
 * components outside {@code notify} (prompt 019: retention consumer lag, canary failure). The
 * {@link NotificationRouter} turns it into a {@code system} notification for every channel that
 * subscribes to {@code system}.
 *
 * @param subtype the notification subtype, for example {@code system.retention_consumer_lag}
 * @param severity {@code info}, {@code warning} or {@code critical}
 * @param title the title
 * @param body the markdown body, already escaped by the publisher where it embeds free text
 * @param path a Foresignal API path the notification links to (appended to the public base URL),
 *     or null
 * @param data structured details stored with the notification
 */
public record SystemEvent(String subtype, String severity, String title, String body, String path,
        Map<String, Object> data) {

    public SystemEvent {
        Objects.requireNonNull(subtype, "subtype");
        severity = severity == null ? "warning" : severity;
        Objects.requireNonNull(title, "title");
        data = data == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(data));
    }
}
