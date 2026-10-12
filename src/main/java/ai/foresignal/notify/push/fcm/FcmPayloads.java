package ai.foresignal.notify.push.fcm;

import ai.foresignal.notify.push.NativeContent;
import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushOptions;
import ai.foresignal.notify.push.apns.ApnsPayloads;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The FCM HTTP v1 message of a native push (prompt 032).
 *
 * <p>{@code {"message":{"token", "data":{"v":"1","id","a","s","ev","l","t","c"},
 * "android":{"priority","ttl","collapse_key"}, "apns"?}}}. Android receives a <b>data message</b>
 * (all values strings) that the app turns into a notification itself: {@code HIGH} priority for
 * warning and critical, {@code NORMAL} for info and under a quiet-hours downgrade. The app must
 * display a notification for every HIGH message, because FCM deprioritizes HIGH messages that show
 * nothing. {@code t} is the minimal (or opted-in short) text. An Apple device registered through
 * FCM also gets an {@code apns} block with the APNs headers and {@code aps} payload of
 * {@link ApnsPayloads}.
 */
public final class FcmPayloads {

    private FcmPayloads() {}

    /** {@code HIGH} for warning and critical, {@code NORMAL} for info or a downgrade. */
    public static String priority(PushMessage message, PushOptions options) {
        if (options.downgraded()) {
            return "NORMAL";
        }
        String severity = NativeContent.severity(message);
        return "warning".equals(severity) || "critical".equals(severity) ? "HIGH" : "NORMAL";
    }

    /** The string-valued {@code data}. */
    public static Map<String, String> data(PushMessage message, String contentLevel) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("v", "1");
        data.put("id", Long.toString(message.id() == null ? 0L : message.id()));
        if (message.alertId() != null) {
            data.put("a", Long.toString(message.alertId()));
        }
        data.put("s", NativeContent.severity(message));
        data.put("ev", message.event());
        String link = NativeContent.link(message);
        if (link != null) {
            data.put("l", link);
        }
        data.put("t", NativeContent.body(message, contentLevel));
        data.put("c", NativeContent.category(message));
        return data;
    }

    /** The whole request body for {@code target}. */
    public static Map<String, Object> message(NativeDeviceTarget target, PushMessage message, PushOptions options,
            Instant now) {
        Map<String, Object> android = new LinkedHashMap<>();
        android.put("priority", priority(message, options));
        android.put("ttl", options.ttl().toSeconds() + "s");
        android.put("collapse_key", NativeContent.collapseId(message));
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("token", target.token());
        inner.put("data", data(message, target.contentLevel()));
        inner.put("android", android);
        if (target.applePlatform()) {
            Map<String, Object> apns = new LinkedHashMap<>();
            apns.put("headers", ApnsPayloads.passThroughHeaders(message, options, now));
            apns.put("payload", ApnsPayloads.payload(message, options, target.contentLevel(), false, true));
            inner.put("apns", apns);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", inner);
        return out;
    }
}
