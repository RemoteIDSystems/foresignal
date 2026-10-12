package ai.foresignal.net;

import ai.foresignal.inbound.edp.EdpHttpClient;
import java.net.InetAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The default beans of the outbound URL policy (prompt 035): DNS and the pinned-connection TLS. */
@Configuration
public class NetConfig {

    /**
     * The system resolver. The JVM caches its positive answers (30 s by default), so a sender that
     * cannot pin and resolves again right after the check gets the answer that was checked.
     */
    @Bean
    public HostResolver hostResolver() {
        return InetAddress::getAllByName;
    }

    /** Platform trust store plus an RFC 6125 name check against the original host. */
    @Bean
    public PinnedTls pinnedTls() {
        return host -> {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {
                new EdpHttpClient.HostCheckingTrustManager(EdpHttpClient.platformTrustManager(), host)}, null);
            return context;
        };
    }
}
