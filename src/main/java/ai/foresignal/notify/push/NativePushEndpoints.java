package ai.foresignal.notify.push;

/**
 * Where native pushes go (prompt 032): Apple's APNs hosts by environment and Google's FCM host. The
 * production bean ({@link #of(NativePushProperties)}) reads {@code foresignal.push.native.*}, whose
 * defaults are the official hosts; tests substitute WireMock stand-ins.
 */
public interface NativePushEndpoints {

    /** The APNs base URL of {@code environment} ({@code sandbox} or {@code production}). */
    String apns(String environment);

    /** The FCM HTTP v1 base URL. */
    String fcm();

    /** The endpoints of {@code properties}. */
    static NativePushEndpoints of(NativePushProperties properties) {
        return new NativePushEndpoints() {
            @Override
            public String apns(String environment) {
                return "sandbox".equals(environment) ? properties.apnsSandboxUrl() : properties.apnsProductionUrl();
            }

            @Override
            public String fcm() {
                return properties.fcmUrl();
            }
        };
    }
}
