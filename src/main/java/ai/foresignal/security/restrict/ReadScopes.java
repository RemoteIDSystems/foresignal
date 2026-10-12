package ai.foresignal.security.restrict;

/**
 * Resolves a {@link ReadScope} into the caller's restriction (prompt 036; implemented by the saved
 * views module). Places are never anonymous: {@code place=} and {@code inWatchlist=}, and a view
 * that references a place or a watchlist, need a token ({@code 401} for anonymous callers and feed
 * keys).
 */
public interface ReadScopes {

    /**
     * {@code base} narrowed by {@code scope}.
     *
     * @param base the caller's restriction
     * @param scope the request's narrowing parameters
     * @param tokenPresent true when the caller presented a token (not anonymous, not a feed key)
     * @throws ai.foresignal.api.error.ForesignalException {@code 401} without a token where one is
     *     needed; {@code 404 UNKNOWN_PLACE}, {@code UNKNOWN_VIEW} or {@code UNKNOWN_WATCHLIST}
     */
    ReadRestriction narrow(ReadRestriction base, ReadScope scope, boolean tokenPresent);
}
