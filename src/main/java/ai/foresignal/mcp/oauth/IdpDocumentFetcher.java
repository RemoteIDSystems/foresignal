package ai.foresignal.mcp.oauth;

import java.io.IOException;
import java.net.URI;

/**
 * Fetches one JSON document from the identity provider (prompt 141): an authorization server metadata document
 * (RFC 8414), an OpenID Connect discovery document, or a JWKS. The production implementation is
 * {@link OutboundIdpDocumentFetcher}; tests substitute their own.
 */
@FunctionalInterface
public interface IdpDocumentFetcher {

    /**
     * Fetches {@code uri} with {@code GET} and no credentials.
     *
     * @param uri the document's URL
     * @return the body of a {@code 200} answer
     * @throws IOException when the URL is refused, the host cannot be reached, the status is not {@code 200} or the
     *     body is too large
     */
    String fetch(URI uri) throws IOException;
}
