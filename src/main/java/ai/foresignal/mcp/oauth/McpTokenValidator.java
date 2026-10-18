package ai.foresignal.mcp.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Verifies an access token from the operator's identity provider for {@code /mcp} (prompt 141), with Nimbus
 * JOSE+JWT against the keys of {@link JwksCache}. Checks, in order:
 *
 * <ol>
 *   <li>a compact JWS (an encrypted or unsigned token is refused);
 *   <li>the header {@code alg} is in {@code mcp.auth.allowedAlgs} and supported ({@code RS256}, {@code ES256},
 *       {@code EdDSA} or {@code Ed25519}; never {@code none} or an HMAC algorithm, so a public key can never be
 *       used as an HMAC secret);
 *   <li>{@code iss} equals {@code mcp.auth.issuer} exactly (before any key is fetched);
 *   <li>the signature verifies with a key of the identity provider's JWKS chosen by {@code kid} (and key type);
 *       an unknown {@code kid} triggers one {@link JwksCache#refresh} (at most once per minute);
 *   <li>{@code aud} contains the audience (RFC 8707; a trailing slash is not significant);
 *   <li>{@code exp} is present and not passed, and {@code nbf} (when present) is reached, both with
 *       {@link #CLOCK_SKEW} of tolerance;
 *   <li>a subject: {@code sub}, else {@code client_id} or {@code azp} (client-credentials tokens).
 * </ol>
 *
 * <p>The token is never stored, logged or forwarded; refusals name what failed, never the token.
 */
@Component
public class McpTokenValidator {

    /** The tolerance on {@code exp} and {@code nbf}. */
    public static final Duration CLOCK_SKEW = Duration.ofSeconds(60);

    private final JwksCache jwks;
    private final Clock clock;

    /** Creates the validator. */
    public McpTokenValidator(JwksCache jwks, Clock clock) {
        this.jwks = jwks;
        this.clock = clock;
    }

    /**
     * A verified token.
     *
     * @param issuer the {@code iss}
     * @param subject the subject
     * @param displayName {@code name}, else {@code preferred_username}, else {@code email}, or null
     * @param scopes the scopes in {@code mcp.auth.scopeClaim}
     * @param expiresAt the {@code exp}
     */
    public record Verified(String issuer, String subject, String displayName, Set<String> scopes, Instant expiresAt) {}

    /** A token that is not valid here; the message says why and never contains the token. */
    public static final class InvalidTokenException extends Exception {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public InvalidTokenException(String message) {
            super(message);
        }
    }

    /**
     * Verifies {@code token}.
     *
     * @param token the compact JWT
     * @param settings {@code mcp.auth}
     * @param audience the audience the token must name (null refuses every token: no resource URI is known)
     * @return the verified token
     * @throws InvalidTokenException when any check fails
     */
    public Verified verify(String token, McpAuthSettings settings, String audience) throws InvalidTokenException {
        if (settings.issuer() == null || settings.issuer().isBlank()) {
            throw new InvalidTokenException("mcp.auth.issuer is not configured");
        }
        if (audience == null || audience.isBlank()) {
            throw new InvalidTokenException("no audience is configured (set mcp.auth.audience or server.publicBaseUrl)");
        }
        SignedJWT jwt;
        JWTClaimsSet claims;
        try {
            jwt = SignedJWT.parse(token);
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new InvalidTokenException("the token is not a signed JWT");
        }
        JWSHeader header = jwt.getHeader();
        JWSAlgorithm alg = header.getAlgorithm();
        if (!allowed(alg, settings.allowedAlgs())) {
            throw new InvalidTokenException("the algorithm " + alg + " is not allowed");
        }
        if (!settings.issuer().equals(claims.getIssuer())) {
            throw new InvalidTokenException("the issuer does not match");
        }
        verifySignature(jwt, settings);
        checkAudience(claims.getAudience(), audience);
        Instant expiresAt = checkLifetime(claims);
        String subject = subject(claims);
        Object scopeValue = claims.getClaim(settings.scopeClaim());
        return new Verified(claims.getIssuer(), subject, displayName(claims), McpScopeMapper.scopes(scopeValue),
                expiresAt);
    }

    /** True when {@code alg} is supported and listed in {@code allowedAlgs}. */
    static boolean allowed(JWSAlgorithm alg, List<String> allowedAlgs) {
        if (alg == null || !McpAuthSettings.SUPPORTED_ALGS.contains(alg.getName())) {
            return false;
        }
        return allowedAlgs.contains(alg.getName());
    }

    private void verifySignature(SignedJWT jwt, McpAuthSettings settings) throws InvalidTokenException {
        String kid = jwt.getHeader().getKeyID();
        JWSAlgorithm alg = jwt.getHeader().getAlgorithm();
        JWKSet keys;
        try {
            keys = jwks.keys(settings);
        } catch (JwksCache.JwksUnavailableException e) {
            throw new InvalidTokenException(e.getMessage());
        }
        List<JWK> candidates = candidates(keys, kid, alg);
        if (candidates.isEmpty() && kid != null && keys.getKeyByKeyId(kid) == null) {
            Optional<JWKSet> refreshed = jwks.refresh(settings);
            if (refreshed.isPresent()) {
                candidates = candidates(refreshed.get(), kid, alg);
            }
        }
        if (candidates.isEmpty()) {
            throw new InvalidTokenException(kid == null ? "no key of the identity provider fits the token"
                    : "the key " + kid + " is not among the identity provider's keys");
        }
        for (JWK key : candidates) {
            try {
                if (jwt.verify(verifier(key, alg))) {
                    return;
                }
            } catch (JOSEException e) {
                // A key that cannot verify this algorithm: try the next candidate.
            }
        }
        throw new InvalidTokenException("the signature does not verify");
    }

    /** The keys that may have signed a token with {@code kid} and {@code alg}: matching id, type, use and alg. */
    static List<JWK> candidates(JWKSet keys, String kid, JWSAlgorithm alg) {
        List<JWK> out = new ArrayList<>();
        for (JWK key : keys.getKeys()) {
            if (kid != null && !kid.equals(key.getKeyID())) {
                continue;
            }
            if (key.getKeyUse() != null && !KeyUse.SIGNATURE.equals(key.getKeyUse())) {
                continue;
            }
            if (key.getAlgorithm() != null && !key.getAlgorithm().getName().equals(alg.getName())
                    && !(isEdDsa(alg) && isEdDsa(key.getAlgorithm()))) {
                continue;
            }
            if (fits(key, alg)) {
                out.add(key);
            }
        }
        return out;
    }

    private static boolean isEdDsa(com.nimbusds.jose.Algorithm alg) {
        return "EdDSA".equals(alg.getName()) || "Ed25519".equals(alg.getName());
    }

    /** True when {@code key}'s type and curve can verify {@code alg}. */
    static boolean fits(JWK key, JWSAlgorithm alg) {
        return switch (alg.getName()) {
            case "RS256" -> key instanceof RSAKey;
            case "ES256" -> key instanceof ECKey ec && Curve.P_256.equals(ec.getCurve());
            case "EdDSA", "Ed25519" -> key instanceof OctetKeyPair okp && Curve.Ed25519.equals(okp.getCurve());
            default -> false;
        };
    }

    private static JWSVerifier verifier(JWK key, JWSAlgorithm alg) throws JOSEException {
        return switch (key) {
            case RSAKey rsa -> new RSASSAVerifier(rsa);
            case ECKey ec -> new ECDSAVerifier(ec);
            case OctetKeyPair okp -> new Ed25519Verifier(okp);
            default -> throw new JOSEException("unsupported key type for " + alg);
        };
    }

    /** Requires {@code audience} among {@code aud}; a trailing slash on either side is not significant. */
    static void checkAudience(List<String> aud, String audience) throws InvalidTokenException {
        String wanted = trimSlash(audience.strip());
        if (aud != null) {
            for (String value : aud) {
                if (value != null && trimSlash(value.strip()).equals(wanted)) {
                    return;
                }
            }
        }
        throw new InvalidTokenException("the token is not for this resource (aud does not contain " + audience + ")");
    }

    private Instant checkLifetime(JWTClaimsSet claims) throws InvalidTokenException {
        Instant now = clock.instant();
        Date exp = claims.getExpirationTime();
        if (exp == null) {
            throw new InvalidTokenException("the token has no exp");
        }
        if (now.isAfter(exp.toInstant().plus(CLOCK_SKEW))) {
            throw new InvalidTokenException("the token has expired");
        }
        Date nbf = claims.getNotBeforeTime();
        if (nbf != null && now.plus(CLOCK_SKEW).isBefore(nbf.toInstant())) {
            throw new InvalidTokenException("the token is not valid yet");
        }
        return exp.toInstant();
    }

    private static String subject(JWTClaimsSet claims) throws InvalidTokenException {
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            subject = stringClaim(claims, "client_id");
        }
        if (subject == null || subject.isBlank()) {
            subject = stringClaim(claims, "azp");
        }
        if (subject == null || subject.isBlank()) {
            throw new InvalidTokenException("the token names no subject (sub, client_id or azp)");
        }
        return subject;
    }

    private static String displayName(JWTClaimsSet claims) {
        for (String claim : List.of("name", "preferred_username", "email")) {
            String value = stringClaim(claims, claim);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String stringClaim(JWTClaimsSet claims, String name) {
        Map<String, Object> all = claims.getClaims();
        return all.get(name) instanceof String text ? text : null;
    }

    private static String trimSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
