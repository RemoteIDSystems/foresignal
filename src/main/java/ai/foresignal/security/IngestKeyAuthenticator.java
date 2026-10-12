package ai.foresignal.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves an ingest-key credential on an {@link AccessRequirement.IngestKeyOrAdmin} path (prompt
 * 024). Implemented by {@code ai.foresignal.inbound.IngestAuthenticator}; {@link TokenFilter} calls
 * it whenever the request carries no API token (or an {@code eik_} bearer).
 */
public interface IngestKeyAuthenticator {

    /**
     * Reads and checks the request's ingest key.
     *
     * @return {@link Result#none()} when the request carries no ingest credential at all
     */
    Result authenticate(HttpServletRequest request);

    /**
     * The outcome.
     *
     * @param caller the authenticated caller, or null
     * @param rejection why a presented credential was refused, or null
     */
    record Result(IngestCaller caller, String rejection) {

        /** No ingest credential was presented. */
        public static Result none() {
            return new Result(null, null);
        }

        /** An accepted caller. */
        public static Result accepted(IngestCaller caller) {
            return new Result(caller, null);
        }

        /** A refused credential. */
        public static Result rejected(String reason) {
            return new Result(null, reason);
        }

        /** True when a caller was accepted. */
        public boolean accepted() {
            return caller != null;
        }

        /** True when a credential was presented and refused. */
        public boolean refused() {
            return rejection != null;
        }
    }
}
