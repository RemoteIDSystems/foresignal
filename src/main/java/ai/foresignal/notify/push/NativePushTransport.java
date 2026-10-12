package ai.foresignal.notify.push;

import ai.foresignal.notify.push.provider.ProviderCredentials;

/**
 * A native push transport (prompt 032): APNs or FCM. Besides the {@link PushTransport} contract (the
 * provider is looked up by the device's app and environment), it can send through a given provider
 * and mint a credential without sending, which {@code testPushProvider} uses.
 */
public interface NativePushTransport extends PushTransport {

    /** Sends {@code message} to {@code target} with {@code credentials}. Never throws for a push problem. */
    PushResult sendVia(ProviderCredentials credentials, NativeDeviceTarget target, PushMessage message,
            PushOptions options);

    /**
     * Mints a fresh credential (an APNs provider JWT, an FCM OAuth access token) to prove the key
     * works.
     *
     * @throws NativePushException with a message free of secrets when it cannot
     */
    void mint(ProviderCredentials credentials);

    /** A credential could not be minted; the message never contains key material. */
    final class NativePushException extends RuntimeException {

        /** Creates the exception. */
        public NativePushException(String message) {
            super(message);
        }
    }
}
