package ai.foresignal.notify.push.fcm;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.push.EcKeys;
import ai.foresignal.notify.push.NativePushHttp;
import ai.foresignal.notify.push.NativePushTransport.NativePushException;
import ai.foresignal.notify.push.provider.ProviderCredentials;
import ai.foresignal.notify.push.provider.PushProviderService;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * OAuth 2.0 access tokens of FCM service accounts (prompt 032), with the JDK only: Google's
 * server-to-server JWT bearer flow, so {@code firebase-admin} and the Google auth library are not
 * needed.
 *
 * <p>The assertion is an RS256 JWT {@code {"alg":"RS256","typ":"JWT"}} /
 * {@code {iss: clientEmail, scope: SCOPE, aud: tokenUri, iat, exp: iat + 3600}} signed with the
 * service account's key, POSTed as
 * {@code grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=<jwt>} to the token URI.
 * The access token is cached until {@code expires_in} minus {@value #EARLY_SECONDS} seconds;
 * {@link #invalidate} drops it after FCM answers 401. The token URI passes the outbound URL policy
 * ({@code LAN_ALLOWED}, prompt 035) before every request.
 */
@Component
public class FcmAccessTokenCache {

    /** The FCM HTTP v1 OAuth scope. */
    public static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    /** The JWT bearer grant type. */
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    /** Seconds before {@code expires_in} at which a token is replaced. */
    public static final int EARLY_SECONDS = 300;

    /** The assertion lifetime (Google's maximum). */
    static final Duration ASSERTION_LIFETIME = Duration.ofHours(1);

    /** The HTTP client key of token requests. */
    static final String CLIENT_KEY = "oauth";

    /** A cached access token. */
    record Entry(String fingerprint, String accessToken, Instant refreshAt) {}

    private final NativePushHttp http;
    private final JsonMapper json;
    private final Clock clock;
    private final OutboundUrlPolicy policy;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /** Creates the cache. */
    public FcmAccessTokenCache(NativePushHttp http, JsonMapper json, Clock clock, OutboundUrlPolicy policy) {
        this.policy = policy;
        this.http = http;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A valid access token of {@code credentials}, fetched when absent, near expiry or of an older
     * key.
     *
     * @throws NativePushException when the token endpoint refuses or cannot be reached
     */
    public String token(ProviderCredentials.Fcm credentials) {
        Entry entry = entries.get(credentials.providerId());
        Instant now = clock.instant();
        if (entry != null && entry.fingerprint().equals(credentials.fingerprint()) && now.isBefore(entry.refreshAt())) {
            return entry.accessToken();
        }
        synchronized (this) {
            entry = entries.get(credentials.providerId());
            if (entry != null && entry.fingerprint().equals(credentials.fingerprint())
                    && now.isBefore(entry.refreshAt())) {
                return entry.accessToken();
            }
            Entry fresh = fetch(credentials, now);
            entries.put(credentials.providerId(), fresh);
            return fresh.accessToken();
        }
    }

    /** Drops the cached token of a provider (FCM answered 401). */
    public void invalidate(String providerId) {
        entries.remove(providerId);
    }

    /** Forgets every token (tests). */
    public void clear() {
        entries.clear();
    }

    private Entry fetch(ProviderCredentials.Fcm credentials, Instant now) {
        String form = "grant_type=" + URLEncoder.encode(GRANT_TYPE, StandardCharsets.UTF_8) + "&assertion="
                + URLEncoder.encode(assertion(credentials, now), StandardCharsets.UTF_8);
        NativePushHttp.Response response;
        URI tokenUri;
        try {
            tokenUri = URI.create(credentials.tokenUri());
        } catch (IllegalArgumentException e) {
            throw new NativePushException("the OAuth token endpoint is not a valid URL");
        }
        // 035: the outbound URL policy, checked before every token request. NativePushHttp keeps one
        // HTTP/2 client per key with the platform TLS checks against the URL host, so the request is not
        // rewritten to the address; it resolves again at once, which the JVM's DNS cache answers with
        // the checked address.
        if (policy.check(tokenUri, PushProviderService.TOKEN_URI_PROFILE) instanceof OutboundUrlPolicy.Denied denied) {
            throw new NativePushException("the OAuth token endpoint is not allowed (" + denied.code() + "): "
                    + denied.reason());
        }
        try {
            response = http.post(CLIENT_KEY, tokenUri,
                    Map.of("content-type", "application/x-www-form-urlencoded", "accept", "application/json"),
                    form.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | IllegalArgumentException e) {
            throw new NativePushException("the OAuth token endpoint could not be reached ("
                    + e.getClass().getSimpleName() + ")");
        }
        JsonNode body = null;
        try {
            body = json.readTree(response.body());
        } catch (RuntimeException e) {
            // handled below
        }
        if (response.status() != 200 || body == null || !body.path("access_token").isString()) {
            String error = body == null ? null : body.path("error").isString() ? body.path("error").asString() : null;
            throw new NativePushException("the OAuth token endpoint answered " + response.status()
                    + (error != null && error.matches("^[a-z_]{1,64}$") ? " " + error : ""));
        }
        long expiresIn = body.path("expires_in").isNumber() ? body.path("expires_in").asLong() : 3600;
        Instant refreshAt = now.plusSeconds(Math.max(0, expiresIn - EARLY_SECONDS));
        return new Entry(credentials.fingerprint(), body.get("access_token").asString(), refreshAt);
    }

    /** The signed assertion issued at {@code iat}. */
    public static String assertion(ProviderCredentials.Fcm credentials, Instant iat) {
        String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String claims = "{\"iss\":" + quote(credentials.clientEmail()) + ",\"scope\":" + quote(SCOPE) + ",\"aud\":"
                + quote(credentials.tokenUri()) + ",\"iat\":" + iat.getEpochSecond() + ",\"exp\":"
                + iat.plus(ASSERTION_LIFETIME).getEpochSecond() + "}";
        String input = EcKeys.b64(header.getBytes(StandardCharsets.UTF_8)) + "."
                + EcKeys.b64(claims.getBytes(StandardCharsets.UTF_8));
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(credentials.key());
            signature.update(input.getBytes(StandardCharsets.US_ASCII));
            return input + "." + EcKeys.b64(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new NativePushException("the OAuth assertion could not be signed");
        }
    }

    private static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
