package ai.foresignal.net;

import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;

/**
 * The TLS context of a connection pinned to a checked address (prompt 035). The connection targets
 * an IP address while the certificate names the host, so the production bean ({@link NetConfig})
 * validates the chain with the platform trust store and the leaf's names against the original host
 * (RFC 6125). Tests may substitute a context that trusts a WireMock stand-in's certificate.
 */
@FunctionalInterface
public interface PinnedTls {

    /** The context for a connection whose certificate must name {@code host}. */
    SSLContext context(String host) throws GeneralSecurityException;
}
