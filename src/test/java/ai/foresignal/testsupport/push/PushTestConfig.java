package ai.foresignal.testsupport.push;

import ai.foresignal.notify.push.NativePushTls;
import ai.foresignal.notify.push.PushTls;
import ai.foresignal.test.MutableClock;
import java.net.Socket;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * The shared configuration of the push integration tests (prompt 031): a {@link MutableClock}, a
 * {@link MutableResolver} in place of DNS, and a TLS context that accepts the WireMock push
 * service's self-signed certificate (test only; production checks the platform trust store and the
 * host name). Every push IT imports exactly this, so they share one cached context. Prompt 032 adds
 * the same trust for native push and {@link MutableNativeEndpoints} for the APNs and FCM hosts.
 */
@TestConfiguration
@Import(MutableClock.TestConfig.class)
public class PushTestConfig {

    /**
     * DNS answers set by each test; the {@link ai.foresignal.net.HostResolver} of the outbound URL
     * policy (prompt 035), and so of the push code, the channels and the LLM clients in this context.
     */
    @Bean
    @Primary
    public MutableResolver mutableResolver() {
        return new MutableResolver();
    }

    /** Accepts any certificate: the push service is a local WireMock stand-in. */
    @Bean
    @Primary
    public PushTls testPushTls() {
        return host -> {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new TrustAll()}, null);
            return context;
        };
    }

    /** Accepts any certificate for APNs, FCM and the token endpoint: WireMock stand-ins (032). */
    @Bean
    @Primary
    public NativePushTls testNativePushTls() {
        return () -> {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new TrustAll()}, null);
            return context;
        };
    }

    /** APNs and FCM hosts set by each native push test (032). */
    @Bean
    @Primary
    public MutableNativeEndpoints mutableNativeEndpoints() {
        return new MutableNativeEndpoints();
    }

    /** Trusts every server certificate (tests only). */
    static final class TrustAll extends X509ExtendedTrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {}

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
