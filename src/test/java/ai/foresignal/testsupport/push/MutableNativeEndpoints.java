package ai.foresignal.testsupport.push;

import ai.foresignal.notify.push.NativePushEndpoints;

/**
 * APNs and FCM hosts that a test points at its WireMock stand-ins (prompt 032). Unset hosts are
 * unroutable, so a test that forgets to set one never reaches Apple or Google.
 */
public class MutableNativeEndpoints implements NativePushEndpoints {

    private static final String NOWHERE = "https://127.0.0.1:9";

    private volatile String apnsProduction = NOWHERE;
    private volatile String apnsSandbox = NOWHERE;
    private volatile String fcm = NOWHERE;

    /** Points both APNs environments and FCM at {@code base}-style URLs. */
    public void set(String apnsProduction, String apnsSandbox, String fcm) {
        this.apnsProduction = apnsProduction;
        this.apnsSandbox = apnsSandbox;
        this.fcm = fcm;
    }

    /** Back to the unroutable defaults. */
    public void clear() {
        set(NOWHERE, NOWHERE, NOWHERE);
    }

    @Override
    public String apns(String environment) {
        return "sandbox".equals(environment) ? apnsSandbox : apnsProduction;
    }

    @Override
    public String fcm() {
        return fcm;
    }
}
