package ai.foresignal.notify.push;

/**
 * Delivers one push to one target (prompt 031). Web Push is the first transport
 * ({@link WebPushTransport}); prompt 032 adds APNs and FCM as sibling beans without changing the
 * fan-out, the dispatch worker or the targets of this prompt.
 *
 * <p>A transport never throws for a delivery problem; it reports it as a {@link PushResult}.
 */
public interface PushTransport {

    /** The transport id ({@code webpush}, {@code apns}, {@code fcm}). */
    String id();

    /** True when this transport delivers to {@code target}. */
    boolean supports(PushTarget target);

    /** Sends {@code message} to {@code target}. */
    PushResult send(PushTarget target, PushMessage message, PushOptions options);
}
