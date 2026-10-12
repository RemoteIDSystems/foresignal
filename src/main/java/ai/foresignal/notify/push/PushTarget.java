package ai.foresignal.notify.push;

/**
 * One device or browser a push can be sent to (prompt 031). Sealed so a {@link PushTransport} can
 * switch over the kinds exhaustively: {@link WebPushTarget} (031) and {@link NativeDeviceTarget}
 * (APNs and FCM devices, 032).
 */
public sealed interface PushTarget permits WebPushTarget, NativeDeviceTarget {

    /** The {@code push_dispatch.target_type} of this kind ({@code webpush}, …). */
    String type();

    /** The target's id, unique within its type ({@code push_dispatch.target_id}). */
    String id();

    /** The payload language (the target's own, else {@code push.defaultLang}), or null. */
    String lang();
}
