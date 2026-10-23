package ai.foresignal.stream.sse;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import java.util.Set;
import java.util.function.LongFunction;

/**
 * The filter of the {@code notifications} and {@code system} topics (prompt 018). The spec defines
 * no filter fields yet; anonymous callers never see notifications about private sources.
 *
 * <p>Under a read restriction with data members (prompt 035) a notification passes only when it is
 * about a record ({@code data.sourceId}) the restriction allows: source and kind from the data, and,
 * for a spatial restriction, the geometry of the alert's record snapshot (looked up by
 * {@code data.alertId}). Operational notifications without a source (channel failures, rule
 * suppression, provider errors) are hidden from such callers (fail closed).
 *
 * @param excludedSources sources whose notifications are hidden from this caller
 * @param restriction the caller's read restriction (never null)
 * @param snapshots the record snapshot (JSON text) of an alert id, or null when unknown; used only
 *     for spatial restrictions
 */
public record NotificationFilter(Set<String> excludedSources, ReadRestriction restriction,
        LongFunction<String> snapshots) {

    /** Copies; a null restriction becomes {@link ReadRestriction#NONE}. */
    public NotificationFilter {
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
        snapshots = snapshots == null ? id -> null : snapshots;
    }

    /** An unrestricted filter (the shape before prompt 035). */
    public NotificationFilter(Set<String> excludedSources) {
        this(excludedSources, ReadRestriction.NONE, null);
    }

    /** True when the caller may see {@code message}. */
    public boolean matches(NotificationMessage message) {
        if (ai.foresignal.notify.ChannelService.PERSONAL_EVENT.equals(message.event())
                || ai.foresignal.notify.ChannelTypeSpec.PERSONAL_COPY_EVENTS.contains(message.event())) {
            // A principal's personal notification (prompt 130), or its copy for subscribed instance channels, is
            // never on an instance-wide topic.
            return false;
        }
        Object source = message.data() == null ? null : message.data().get("sourceId");
        if (source != null && excludedSources.contains(String.valueOf(source))) {
            return false;
        }
        if (!restriction.hasDataRestrictions()) {
            return true;
        }
        if (source == null) {
            return false;
        }
        Object kind = message.data().get("kind");
        String sourceId = String.valueOf(source);
        String kindValue = kind == null ? null : String.valueOf(kind);
        if (!restriction.allowsSource(sourceId) || (kind != null && !restriction.allowsKind(kindValue))) {
            return false;
        }
        if (kind == null && restriction.kinds() != null) {
            return false;
        }
        if (restriction.region() == null) {
            return true;
        }
        Object alertId = message.data().get("alertId");
        if (!(alertId instanceof Number number)) {
            return false;
        }
        return RestrictionClamp.testSnapshot(restriction, sourceId, kindValue, snapshots.apply(number.longValue()));
    }
}
