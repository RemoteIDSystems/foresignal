package ai.foresignal.notify.push;

import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;

/**
 * The TLS context of a connection to a push service that is pinned to a checked address (prompt
 * 031). The production bean ({@link PushConfig}) validates the chain with the platform trust store
 * and the leaf's names against the endpoint host (RFC 6125), because the connection targets an IP
 * address while the certificate names the host. Tests substitute a context that trusts the
 * WireMock stand-in's self-signed certificate.
 */
@FunctionalInterface
public interface PushTls {

    /** The context for a connection whose certificate must name {@code host}. */
    SSLContext context(String host) throws GeneralSecurityException;
}
