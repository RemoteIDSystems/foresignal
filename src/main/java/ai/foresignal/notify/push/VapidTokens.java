package ai.foresignal.notify.push;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Signed VAPID tokens per push-service origin (prompt 031): {@code exp} is 12 h ahead and a token is
 * reused for 11 h, so one origin costs one signature per 11 hours. The cache key includes the
 * contact and the public key, so changing {@code push.contact} or the key takes effect at once.
 */
@Component
public class VapidTokens {

    /** How far ahead {@code exp} is set. */
    public static final Duration LIFETIME = Duration.ofHours(12);

    /** How long a token is reused. */
    public static final Duration REUSE = Duration.ofHours(11);

    private record Cached(String header, Instant reuseUntil) {}

    private final VapidKeyService keys;
    private final Clock clock;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    /** Creates the cache. */
    public VapidTokens(VapidKeyService keys, Clock clock) {
        this.keys = keys;
        this.clock = clock;
    }

    /** The {@code Authorization} header value for a request to {@code endpoint}. */
    public String authorization(URI endpoint, String contact) {
        VapidKeyService.Keys pair = keys.keys();
        String origin = Vapid.origin(endpoint);
        String cacheKey = origin + "\n" + contact + "\n" + pair.publicB64();
        Instant now = clock.instant();
        Cached cached = cache.get(cacheKey);
        if (cached != null && now.isBefore(cached.reuseUntil())) {
            return cached.header();
        }
        long exp = now.plus(LIFETIME).getEpochSecond();
        String header = Vapid.authorization(Vapid.jwt(pair.privateKey(), origin, exp, contact), pair.publicB64());
        cache.put(cacheKey, new Cached(header, now.plus(REUSE)));
        return header;
    }

    /** Forgets every cached token (tests). */
    public void clear() {
        cache.clear();
    }
}
