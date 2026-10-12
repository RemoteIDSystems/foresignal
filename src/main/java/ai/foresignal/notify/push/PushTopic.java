package ai.foresignal.notify.push;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The RFC 8030 §5.4 {@code Topic} of a push (prompt 031): {@code base64url(SHA-256(key))[0:32]}, so
 * the push service replaces an undelivered message about the same entity instead of queueing both.
 * The result is at most 32 characters of the URL-safe base64 alphabet, as §5.4 requires.
 *
 * <p>Keys: {@code alert:<ruleId>:<source>:<externalId>} for alerts, {@code briefing:<briefingId>}
 * for briefings, {@code system:<code>} for system events.
 */
public final class PushTopic {

    /** The longest topic RFC 8030 allows. */
    public static final int MAX_LENGTH = 32;

    private PushTopic() {}

    /** The topic of {@code key}. */
    public static String derive(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return EcKeys.b64(digest).substring(0, MAX_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** The topic of an alert of {@code ruleId} on {@code source}/{@code externalId}. */
    public static String alert(String ruleId, String source, String externalId) {
        return derive("alert:" + ruleId + ":" + source + ":" + externalId);
    }

    /** The topic of a briefing. */
    public static String briefing(String briefingId) {
        return derive("briefing:" + briefingId);
    }

    /** The topic of a system event code. */
    public static String system(String code) {
        return derive("system:" + code);
    }
}
