package ai.foresignal.security.restrict;

import ai.foresignal.security.Scope;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Who is making a request and what it may read (prompt 035): a token, a feed key, a tile key (prompt
 * 050) or an anonymous caller, each with its effective {@link ReadRestriction}.
 *
 * <p>Request-scoped: {@code TokenFilter} (and {@code PathKeyFilter} for {@code /v1/k/{key}/…})
 * stores it under {@link #ATTRIBUTE}, so it survives the async dispatch of an SSE response and is
 * read by every {@code @RestrictedRead} path, by the MCP tool context and by the rate limiter.
 *
 * @param kind token, feed key, tile key or anonymous
 * @param id the token, feed-key or tile-key id; null when anonymous
 * @param name the token, feed-key or tile-key name; null when anonymous
 * @param scope the scope; null when anonymous (feed and tile keys are {@code read})
 * @param restriction the effective restriction (never null)
 * @param privateAccess the 039 private grant of the token or feed key; always false when anonymous
 */
public record RequestPrincipal(Kind kind, UUID id, String name, Scope scope, ReadRestriction restriction,
        boolean privateAccess) {

    /** The request attribute holding it. */
    public static final String ATTRIBUTE = "ai.foresignal.requestPrincipal";

    /** What kind of caller this is. */
    public enum Kind {
        /** A bearer token (or a ticket of one). */
        TOKEN,
        /** A path-bound feed key ({@code fsf_}, prompt 035). */
        FEED_KEY,
        /** A path-carrier tile key ({@code fsk_}, prompt 050) for header-less map clients. */
        TILE_KEY,
        /** No credential: a data read while {@code openReads} is on. */
        ANONYMOUS
    }

    /** Requires the kind; a null restriction becomes {@link ReadRestriction#NONE}. */
    public RequestPrincipal {
        Objects.requireNonNull(kind, "kind");
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
        if (kind == Kind.ANONYMOUS) {
            privateAccess = false;
        }
    }

    /** A principal without the private grant (the shape before prompt 039). */
    public RequestPrincipal(Kind kind, UUID id, String name, Scope scope, ReadRestriction restriction) {
        this(kind, id, name, scope, restriction, false);
    }

    /** An anonymous caller under {@code restriction}. */
    public static RequestPrincipal anonymous(ReadRestriction restriction) {
        return new RequestPrincipal(Kind.ANONYMOUS, null, null, null, restriction);
    }

    /** The principal of {@code request}, if a filter set one. */
    public static Optional<RequestPrincipal> of(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute(ATTRIBUTE);
        return value instanceof RequestPrincipal p ? Optional.of(p) : Optional.empty();
    }

    /** The effective restriction of {@code request}; {@link ReadRestriction#NONE} when no principal was set. */
    public static ReadRestriction restrictionOf(HttpServletRequest request) {
        return of(request).map(RequestPrincipal::restriction).orElse(ReadRestriction.NONE);
    }

    /** Stores this principal on {@code request}. */
    public void bind(HttpServletRequest request) {
        request.setAttribute(ATTRIBUTE, this);
    }

    /** True for an anonymous caller. */
    public boolean isAnonymous() {
        return kind == Kind.ANONYMOUS;
    }

    /** True for a feed key. */
    public boolean isFeedKey() {
        return kind == Kind.FEED_KEY;
    }

    /** True for a tile key (prompt 050). */
    public boolean isTileKey() {
        return kind == Kind.TILE_KEY;
    }

    /**
     * True for any key a {@code PathKeyFilter} bound from a {@code /v1/k/{key}/…} path (a feed key or a
     * tile key): read-only, no {@code CurrentToken}, its own grant on the principal.
     */
    public boolean isPathKey() {
        return kind == Kind.FEED_KEY || kind == Kind.TILE_KEY;
    }

    /**
     * The rate-limit and fairness key: {@code token:<id>}, {@code feed:<id>}, {@code tilekey:<id>}
     * (prompt 050), or null for an anonymous caller (keyed by client IP instead).
     */
    public String clientKey() {
        return switch (kind) {
            case TOKEN -> "token:" + id;
            case FEED_KEY -> "feed:" + id;
            case TILE_KEY -> "tilekey:" + id;
            case ANONYMOUS -> null;
        };
    }
}
