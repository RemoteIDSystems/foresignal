package ai.foresignal.notify.push;

import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;

/**
 * The TLS context of connections to APNs, FCM and the OAuth token endpoint (prompt 032). The
 * production bean ({@link NativePushHttp#platformTls()}) is the JDK default, which validates against
 * the platform trust store; APNs moved to the USERTrust RSA root in 2025, which {@code cacerts}
 * carries, and nothing is pinned. Tests substitute a context that trusts the WireMock stand-in.
 */
@FunctionalInterface
public interface NativePushTls {

    /** The context for native push connections. */
    SSLContext context() throws GeneralSecurityException;
}
