package ai.foresignal.notify.push;

import java.util.Objects;
import java.util.UUID;

/**
 * A native app installation registered at {@code /v1/push/devices} (prompt 032): an iOS, iPadOS or
 * macOS app reached through APNs, or an Android (or Apple) app reached through FCM.
 *
 * @param deviceId the {@code push_device} row
 * @param transport {@code apns} or {@code fcm}
 * @param platform {@code ios}, {@code ipados}, {@code macos} or {@code android}
 * @param appId the bundle id or Android package name (the APNs topic)
 * @param environment {@code sandbox} or {@code production} for APNs, null for FCM
 * @param token the device token, decrypted (never logged or returned)
 * @param contentLevel {@code minimal} or {@code short}: how much alert text leaves through Apple or
 *     Google
 * @param lang the device's language, or null
 */
public record NativeDeviceTarget(UUID deviceId, String transport, String platform, String appId, String environment,
        String token, String contentLevel, String lang) implements PushTarget {

    /** The target type of native devices ({@code push_dispatch.target_type}). */
    public static final String TYPE = "native";

    /** The APNs transport id. */
    public static final String APNS = "apns";

    /** The FCM transport id. */
    public static final String FCM = "fcm";

    /** The default content level: no alert text through Apple or Google. */
    public static final String MINIMAL = "minimal";

    /** The opt-in content level: the 40-character short title. */
    public static final String SHORT = "short";

    /** Requires the id, transport, app and token; the content level defaults to {@link #MINIMAL}. */
    public NativeDeviceTarget {
        Objects.requireNonNull(deviceId, "deviceId");
        Objects.requireNonNull(transport, "transport");
        Objects.requireNonNull(appId, "appId");
        Objects.requireNonNull(token, "token");
        contentLevel = SHORT.equals(contentLevel) ? SHORT : MINIMAL;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String id() {
        return deviceId.toString();
    }

    /** True for an Apple platform ({@code ios}, {@code ipados}, {@code macos}). */
    public boolean applePlatform() {
        return "ios".equals(platform) || "ipados".equals(platform) || "macos".equals(platform);
    }

    @Override
    public String toString() {
        // Never print the device token.
        return "NativeDeviceTarget[" + deviceId + ", " + transport + "]";
    }
}
