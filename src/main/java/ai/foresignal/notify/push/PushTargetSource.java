package ai.foresignal.notify.push;

import ai.foresignal.notify.NotificationMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Enumerates the active targets of one kind (prompt 031). {@link WebPushTargetSource} lists verified
 * Web Push subscriptions; prompt 032 adds {@code NativeDeviceTargetSource}. {@code PushFanout} asks
 * every source bean for the targets a notification goes to, and the dispatch worker hands each
 * outcome back so the source can keep its own state.
 */
public interface PushTargetSource {

    /** The {@link PushTarget#type()} this source owns. */
    String targetType();

    /** The active targets whose filters accept {@code message}. */
    List<PushTarget> targets(NotificationMessage message);

    /** The target with {@code targetId} if it still exists and is active. */
    Optional<PushTarget> find(String targetId);

    /**
     * Records the outcome of a send to {@code target}: success time, failure counters, deletion on
     * {@link PushResult.Gone}. Must not throw.
     */
    void onOutcome(PushTarget target, PushResult result, Instant at);
}
