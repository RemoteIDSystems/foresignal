package ai.foresignal.security.restrict;

/**
 * Resolves one family of keys carried in a {@code /v1/k/{key}/…} path (Conventions "Key-in-path
 * URLs"): feed keys {@code fsf_} (prompt 035), tile keys {@code fsk_} (050) and share-link keys
 * {@code fss_} (112) each provide one bean, and {@link PathKeyFilter} picks the resolver by prefix.
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

    /** No such key: {@code 401 INVALID_FEED_KEY}. */
    record Invalid() implements Resolution {}

    /** The key existed but expired or was revoked: {@code 410}. */
    record Gone() implements Resolution {}
}
