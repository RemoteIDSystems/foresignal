package ai.foresignal.security.restrict;

/**
 * Resolves one family of keys carried in a {@code /v1/k/{key}/…} path (Conventions "Key-in-path
 * URLs"): feed keys {@code fsf_} (prompt 035), tile keys {@code fsk_} (050) and share-link keys
 * {@code fss_} (112) each provide one bean, and {@link PathKeyFilter} picks the resolver by prefix.
 *
 * <p>The refusal codes are per family: feed keys answer {@code 401 INVALID_FEED_KEY},
 * {@code 410 FEED_KEY_GONE} and {@code 403 RESTRICTED} (the defaults); tile keys answer
 * {@code 401 INVALID_TILE_KEY} for unknown, expired and revoked keys alike and
 * {@code 403 INSUFFICIENT_SCOPE} outside their prefixes (prompt 050).
 */
public interface PathKeyResolver {

    /** The key prefix this resolver owns, for example {@code fsf_}. */
    String prefix();

    /**
     * Looks up {@code key} (which starts with {@link #prefix()}) in constant time against its stored
     * hash.
     *
     * @return the outcome; never null
     */
    Resolution resolve(String key);

    /** The problem code of an unknown key ({@code 401}); feed keys by default. */
    default String invalidCode() {
        return ai.foresignal.api.error.ErrorCodes.INVALID_FEED_KEY;
    }

    /** The status of an expired or revoked key; {@code 410} by default. */
    default org.springframework.http.HttpStatus goneStatus() {
        return org.springframework.http.HttpStatus.GONE;
    }

    /** The problem code of an expired or revoked key; {@code FEED_KEY_GONE} by default. */
    default String goneCode() {
        return ai.foresignal.api.error.ErrorCodes.FEED_KEY_GONE;
    }

    /** The problem code ({@code 403}) of a canonical path outside the key's prefixes; {@code RESTRICTED} by default. */
    default String outsidePrefixCode() {
        return ai.foresignal.api.error.ErrorCodes.RESTRICTED;
    }

    /** The outcome of a lookup. */
    sealed interface Resolution {}

    /**
     * An active key.
     *
     * @param principal the principal to bind (kind, id, name, scope and restriction)
     * @param prefixes the canonical path prefixes it may read (for example {@code /v1/records})
     */
    record Valid(RequestPrincipal principal, java.util.List<String> prefixes) implements Resolution {

        /** Copies the prefixes. */
        public Valid {
            prefixes = java.util.List.copyOf(prefixes);
        }
    }

    /** No such key: {@code 401} with {@link #invalidCode()}. */
    record Invalid() implements Resolution {}

    /** The key existed but expired or was revoked: {@link #goneStatus()} with {@link #goneCode()}. */
    record Gone() implements Resolution {}
}
