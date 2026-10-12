package ai.foresignal.notify.push.apns;

import ai.foresignal.notify.push.EcKeys;
import ai.foresignal.notify.push.Vapid;
import ai.foresignal.notify.push.provider.ProviderCredentials;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * APNs provider authentication tokens (prompt 032), one per provider.
 *
 * <p>A token is an ES256 JWT with header {@code {"alg":"ES256","kid":<keyId>}} and claims
 * {@code {"iss":<teamId>,"iat":<now>}}, signed with the provider's {@code .p8} key; the JDK's DER
 * ECDSA signature is converted to the raw {@code R ‖ S} JWS form. Apple rejects tokens older than an
 * hour ({@code 403 ExpiredProviderToken}) and refreshes more often than every 20 minutes
 * ({@code 429 TooManyProviderTokenUpdates}), so a token is reused for {@value #REFRESH_MINUTES}
 * minutes and then replaced. After {@code ExpiredProviderToken} the transport calls
 * {@link #forceRefresh} once: only the caller still holding the stale token mints a new one, so a
 * burst of rejected sends regenerates a single token. A changed provider (new key or settings, so a
 * new fingerprint) gets a new token at once.
 */
@Component
public class ApnsJwtCache {

    /** Minutes a token is reused. */
    public static final int REFRESH_MINUTES = 40;

    /** How long a token is reused. */
    public static final Duration REFRESH_AFTER = Duration.ofMinutes(REFRESH_MINUTES);

    /** A cached token. */
    record Entry(String fingerprint, String jwt, Instant issuedAt) {}

    private final Clock clock;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /** Creates the cache. */
    public ApnsJwtCache(Clock clock) {
        this.clock = clock;
    }

    /** The current token of {@code credentials}, minted when absent, stale or of an older key. */
    public String token(ProviderCredentials.Apns credentials) {
        Instant now = clock.instant();
        return entries.compute(credentials.providerId(), (id, entry) -> {
            if (entry != null && entry.fingerprint().equals(credentials.fingerprint())
                    && now.isBefore(entry.issuedAt().plus(REFRESH_AFTER))) {
                return entry;
            }
            return new Entry(credentials.fingerprint(), sign(credentials, now), now);
        }).jwt();
    }

    /**
     * Replaces {@code stale} after Apple rejected it as expired, unless another caller already did;
     * returns the token to retry with.
     */
    public String forceRefresh(ProviderCredentials.Apns credentials, String stale) {
        Instant now = clock.instant();
        return entries.compute(credentials.providerId(), (id, entry) -> {
            if (entry != null && entry.fingerprint().equals(credentials.fingerprint()) && !entry.jwt().equals(stale)) {
                return entry;
            }
            return new Entry(credentials.fingerprint(), sign(credentials, now), now);
        }).jwt();
    }

    /** Forgets every token (tests). */
    public void clear() {
        entries.clear();
    }

    /** A signed provider token issued at {@code iat}. */
    public static String sign(ProviderCredentials.Apns credentials, Instant iat) {
        String header = "{\"alg\":\"ES256\",\"kid\":\"" + credentials.keyId() + "\"}";
        String claims = "{\"iss\":\"" + credentials.teamId() + "\",\"iat\":" + iat.getEpochSecond() + "}";
        String input = EcKeys.b64(header.getBytes(StandardCharsets.UTF_8)) + "."
                + EcKeys.b64(claims.getBytes(StandardCharsets.UTF_8));
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(credentials.key());
            signature.update(input.getBytes(StandardCharsets.US_ASCII));
            return input + "." + EcKeys.b64(Vapid.derToRaw(signature.sign()));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not sign the APNs provider token: " + e.getClass().getSimpleName());
        }
    }
}
