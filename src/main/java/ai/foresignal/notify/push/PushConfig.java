package ai.foresignal.notify.push;

import ai.foresignal.inbound.edp.EdpHttpClient;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * The default beans of the push subsystem (prompt 031): TLS and the payload budget. Endpoint DNS is
 * the outbound URL policy's {@code HostResolver} (prompt 035).
 */
@Configuration
public class PushConfig {

    /** Platform trust store plus an RFC 6125 name check against the endpoint host. */
    @Bean
    public PushTls pushTls() {
        return host -> {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {
                new EdpHttpClient.HostCheckingTrustManager(EdpHttpClient.platformTrustManager(), host)}, null);
            return context;
        };
    }

    /** The TLS of APNs, FCM and the OAuth token endpoint: the platform default (prompt 032). */
    @Bean
    public NativePushTls nativePushTls() {
        return NativePushHttp.platformTls();
    }

    /** The APNs and FCM hosts of {@code foresignal.push.native.*} (prompt 032). */
    @Bean
    public NativePushEndpoints nativePushEndpoints(NativePushProperties properties) {
        return NativePushEndpoints.of(properties);
    }

    /** The 3993-byte plaintext budget. */
    @Bean
    public PushPayloadBudget pushPayloadBudget(JsonMapper json) {
        return new PushPayloadBudget(json);
    }
}
