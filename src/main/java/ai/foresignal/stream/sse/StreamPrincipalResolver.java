package ai.foresignal.stream.sse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the {@link StreamPrincipal} of a stream request, once, at connect (and for each filter
 * PATCH).
 *
 * <p>The default ({@link HeaderStreamPrincipalResolver}) reads only the {@code Authorization}
 * header; tokens are never accepted in query strings. Prompt 023 plugs reconnect-aware stream
 * tickets in here by providing a {@code @Primary} resolver that maps a ticket to the principal of
 * the token that issued it.
 */
public interface StreamPrincipalResolver {

    /** The principal of {@code request}. */
    StreamPrincipal resolve(HttpServletRequest request);
}
