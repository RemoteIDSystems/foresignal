package ai.foresignal.notify.push;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Native push mechanics ({@code foresignal.push.native.*}, prompt 032). The endpoints are Apple's
 * and Google's; tests point them at WireMock stand-ins. Credentials are never configured here: the
 * operator enters their own APNs key and Firebase service account through {@code /v1/push/providers}.
 *
 * @param apnsProductionUrl the APNs production host, {@code https://api.push.apple.com}
 * @param apnsSandboxUrl the APNs development host, {@code https://api.sandbox.push.apple.com}
 * @param fcmUrl the FCM HTTP v1 host, {@code https://fcm.googleapis.com}
 * @param deviceTestsPerHour test pushes one device may receive per hour ({@code testPushDevice})
 * @param expirySweepInterval how often devices of expired, revoked or deleted tokens are removed
 * @param providerRetry how long a dispatch waits after a provider credential error
 */
@ConfigurationProperties("foresignal.push.native")
public record NativePushProperties(
        @DefaultValue("https://api.push.apple.com") String apnsProductionUrl,
        @DefaultValue("https://api.sandbox.push.apple.com") String apnsSandboxUrl,
        @DefaultValue("https://fcm.googleapis.com") String fcmUrl,
        @DefaultValue("5") int deviceTestsPerHour,
        @DefaultValue("1h") Duration expirySweepInterval,
        @DefaultValue("15m") Duration providerRetry) {

    /** Fills missing values with the defaults. */
    public NativePushProperties {
        apnsProductionUrl = strip(apnsProductionUrl, "https://api.push.apple.com");
        apnsSandboxUrl = strip(apnsSandboxUrl, "https://api.sandbox.push.apple.com");
        fcmUrl = strip(fcmUrl, "https://fcm.googleapis.com");
        deviceTestsPerHour = deviceTestsPerHour < 1 ? 5 : deviceTestsPerHour;
        expirySweepInterval = expirySweepInterval == null ? Duration.ofHours(1) : expirySweepInterval;
        providerRetry = providerRetry == null ? Duration.ofMinutes(15) : providerRetry;
    }

    /** The defaults. */
    public static NativePushProperties defaults() {
        return new NativePushProperties(null, null, null, 0, null, null);
    }

    private static String strip(String url, String fallback) {
        String value = url == null || url.isBlank() ? fallback : url.strip();
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
