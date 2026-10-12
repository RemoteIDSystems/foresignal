package ai.foresignal.notify.push;

import java.util.Objects;
import java.util.UUID;

/**
 * An RFC 8030 push subscription: a browser (PWA, operator console) or a UnifiedPush distributor,
 * which speak the same wire protocol (prompt 031).
 *
 * @param subscriptionId the {@code push_subscription} row
 * @param endpoint the push resource URL (a capability; never logged or returned in full)
 * @param p256dh the subscription's public key, base64url (65-byte point)
 * @param auth the subscription's auth secret, base64url (16 bytes)
 * @param payloadFormat {@code foresignal} or {@code declarative}
 * @param lang the subscription's language, or null for {@code push.defaultLang}
 */
public record WebPushTarget(UUID subscriptionId, String endpoint, String p256dh, String auth, String payloadFormat,
        String lang) implements PushTarget {

    /** The target type of Web Push subscriptions. */
    public static final String TYPE = "webpush";

    /** Requires the id, endpoint and keys; the format defaults to {@code foresignal}. */
    public WebPushTarget {
        Objects.requireNonNull(subscriptionId, "subscriptionId");
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(p256dh, "p256dh");
        Objects.requireNonNull(auth, "auth");
        payloadFormat = payloadFormat == null ? PushPayloads.FORESIGNAL : payloadFormat;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String id() {
        return subscriptionId.toString();
    }

    @Override
    public String toString() {
        // Never print the endpoint or keys.
        return "WebPushTarget[" + subscriptionId + "]";
    }
}
