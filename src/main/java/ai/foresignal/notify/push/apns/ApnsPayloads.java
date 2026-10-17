package ai.foresignal.notify.push.apns;

import ai.foresignal.notify.push.NativeContent;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushOptions;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.json.JsonMapper;

/**
 * The APNs request of a native push (prompt 032): headers and the JSON body.
 *
 * <p>Body: {@code {"aps":{"alert":{"title","body"},"sound":"default","mutable-content":1,
 * "interruption-level","thread-id","category","badge"?},"e":{"v":1,"id","a","s","ev","l"}}}. It is
 * always a visible alert with {@code mutable-content}, so the Notification Service Extension can
 * fetch the details; never a background push ({@code content-available}), which iOS throttles and
 * drops for force-quit apps. {@code interruption-level}: info {@code passive}, warning
 * {@code active}, critical {@code time-sensitive} when the provider's app has that capability, else
 * {@code active}; never {@code critical} (it needs an Apple entitlement). A quiet-hours downgrade is
 * {@code passive} without a sound.
 *
 * <p>Headers: {@code apns-topic} (the app), {@code apns-push-type: alert}, {@code apns-priority} 10
 * (5 under a downgrade), {@code apns-expiration} (now + TTL: alert 24 h, briefing 12 h, system 1 h),
 * {@code apns-collapse-id} and {@code apns-id} (the dispatch row as a UUID).
 */
public final class ApnsPayloads {

    /** The largest APNs payload of an alert, in bytes. */
    public static final int MAX_BYTES = 4096;

    private ApnsPayloads() {}

    /** The {@code interruption-level} of a severity. */
    public static String interruptionLevel(String severity, boolean downgraded, boolean timeSensitive) {
        if (downgraded) {
            return "passive";
        }
        return switch (severity == null ? "info" : severity) {
            case "critical" -> timeSensitive ? "time-sensitive" : "active";
            case "warning" -> "active";
            default -> "passive";
        };
    }

    /** The {@code aps} dictionary. */
    public static Map<String, Object> aps(PushMessage message, PushOptions options, String contentLevel,
            boolean timeSensitive) {
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("title", NativeContent.title(message, contentLevel));
        alert.put("body", NativeContent.body(message, contentLevel));
        Map<String, Object> aps = new LinkedHashMap<>();
        aps.put("alert", alert);
        if (!options.downgraded() && !options.silent()) {
            aps.put("sound", "default");
        }
        aps.put("mutable-content", 1);
        // Prompt 115: an exercise is at most "active", never time-sensitive.
        aps.put("interruption-level", message.exercise()
                ? (options.downgraded() ? "passive" : "active")
                : interruptionLevel(message.severity(), options.downgraded(), timeSensitive));
        aps.put("thread-id", NativeContent.threadId(message));
        aps.put("category", NativeContent.category(message));
        if (message.badge() != null) {
            aps.put("badge", message.badge());
        }
        return aps;
    }

    /** The Foresignal ids ({@code e}); {@code l} only when {@code withLink}. */
    public static Map<String, Object> ids(PushMessage message, boolean withLink) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("v", 1);
        e.put("id", message.id() == null ? 0L : message.id());
        e.put("a", message.alertId());
        e.put("s", NativeContent.severity(message));
        e.put("ev", message.event());
        if (message.exercise()) {
            e.put("exercise", true);
        }
        String link = NativeContent.link(message);
        if (withLink && link != null) {
            e.put("l", link);
        }
        return e;
    }

    /** The whole body. */
    public static Map<String, Object> payload(PushMessage message, PushOptions options, String contentLevel,
            boolean timeSensitive, boolean withLink) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("aps", aps(message, options, contentLevel, timeSensitive));
        out.put("e", ids(message, withLink));
        return out;
    }

    /**
     * The UTF-8 body within {@value #MAX_BYTES} bytes: as built, else without the deep link, else
     * with the minimal text and no link.
     */
    public static byte[] fit(JsonMapper json, PushMessage message, PushOptions options, String contentLevel,
            boolean timeSensitive) {
        byte[] out = json.writeValueAsBytes(payload(message, options, contentLevel, timeSensitive, true));
        if (out.length <= MAX_BYTES) {
            return out;
        }
        out = json.writeValueAsBytes(payload(message, options, contentLevel, timeSensitive, false));
        if (out.length <= MAX_BYTES) {
            return out;
        }
        return json.writeValueAsBytes(payload(message, options, "minimal", timeSensitive, false));
    }

    /** {@code apns-priority}: 10, or 5 under a quiet-hours downgrade. */
    public static String priority(PushOptions options) {
        return options.downgraded() ? "5" : "10";
    }

    /** {@code apns-expiration}: now plus the TTL, in epoch seconds. */
    public static String expiration(PushOptions options, Instant now) {
        return Long.toString(now.plus(options.ttl()).getEpochSecond());
    }

    /** The {@code apns-id} of a dispatch row (name-based, so retries of one row reuse it), else random. */
    public static UUID apnsId(Long dispatchId) {
        return dispatchId == null ? UUID.randomUUID()
                : UUID.nameUUIDFromBytes(("foresignal:push_dispatch:" + dispatchId).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * The headers FCM passes through to APNs for Apple devices registered via FCM: priority, push
     * type, expiration and collapse id (FCM sets the topic, authorization and id itself).
     */
    public static Map<String, String> passThroughHeaders(PushMessage message, PushOptions options, Instant now) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("apns-push-type", "alert");
        headers.put("apns-priority", priority(options));
        headers.put("apns-expiration", expiration(options, now));
        headers.put("apns-collapse-id", NativeContent.collapseId(message));
        return headers;
    }

    /** Every header of a direct APNs request. */
    public static Map<String, String> headers(String jwt, String appId, PushMessage message, PushOptions options,
            Instant now) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("authorization", "bearer " + jwt);
        headers.put("apns-topic", appId);
        headers.putAll(passThroughHeaders(message, options, now));
        headers.put("apns-id", apnsId(options.dispatchId()).toString());
        headers.put("content-type", "application/json");
        return headers;
    }
}
