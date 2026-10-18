package ai.foresignal.testsupport.mcp;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An in-test identity provider for MCP remote authorization tests (prompt 141): RSA, P-256 and Ed25519 signing keys,
 * their public JWKS, and a builder of signed access tokens. Nothing here talks to a real identity provider.
 */
public final class TestIdp {

    /** The RS256 key, kid {@code rsa-1}. */
    public final RSAKey rsa;

    /** The ES256 key, kid {@code ec-1}. */
    public final ECKey ec;

    /** The Ed25519 key, kid {@code ed-1}. */
    public final OctetKeyPair ed;

    /** A second RSA key the published JWKS does not hold at first, kid {@code rsa-2} (rotation tests). */
    public final RSAKey rotated;

    private TestIdp(RSAKey rsa, ECKey ec, OctetKeyPair ed, RSAKey rotated) {
        this.rsa = rsa;
        this.ec = ec;
        this.ed = ed;
        this.rotated = rotated;
    }

    /** Generates fresh keys. */
    public static TestIdp create() {
        try {
            return new TestIdp(new RSAKeyGenerator(2048).keyID("rsa-1").generate(),
                    new ECKeyGenerator(Curve.P_256).keyID("ec-1").generate(),
                    new OctetKeyPairGenerator(Curve.Ed25519).keyID("ed-1").generate(),
                    new RSAKeyGenerator(2048).keyID("rsa-2").generate());
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The public JWKS JSON of {@code keys}. */
    public static String jwks(JWK... keys) {
        return new JWKSet(Arrays.asList(keys)).toPublicJWKSet().toString();
    }

    /** The public JWKS JSON of the three initial keys (not {@link #rotated}). */
    public String jwks() {
        return jwks(rsa, ec, ed);
    }

    /** The public JWKS JSON of the three initial keys plus {@link #rotated}. */
    public String jwksWithRotated() {
        return jwks(rsa, ec, ed, rotated);
    }

    /** A token builder signing with {@link #rsa} (RS256). */
    public Token token(String issuer, String audience) {
        return new Token(issuer, audience, rsa, JWSAlgorithm.RS256);
    }

    /** An unsigned ({@code alg: none}) token with the given claims. */
    public static String unsigned(String issuer, String audience, Instant expiresAt) {
        return new PlainJWT(new JWTClaimsSet.Builder().issuer(issuer).audience(audience).subject("someone")
                .expirationTime(Date.from(expiresAt)).claim("scope", "foresignal:agent").build()).serialize();
    }

    /** A builder of one signed access token. */
    public static final class Token {

        private String issuer;
        private List<String> audience = new ArrayList<>();
        private String subject = "user-1";
        private Instant expiresAt;
        private Instant notBefore;
        private JWK key;
        private JWSAlgorithm alg;
        private String kid;
        private boolean kidSet;
        private byte[] hmacSecret;
        private final Map<String, Object> claims = new LinkedHashMap<>();

        Token(String issuer, String audience, JWK key, JWSAlgorithm alg) {
            this.issuer = issuer;
            if (audience != null) {
                this.audience.add(audience);
            }
            this.key = key;
            this.alg = alg;
        }

        /** Sets {@code iss}. */
        public Token issuer(String value) {
            this.issuer = value;
            return this;
        }

        /** Replaces {@code aud}. */
        public Token audience(String... values) {
            this.audience = new ArrayList<>(Arrays.asList(values));
            return this;
        }

        /** Sets {@code sub} (null for none). */
        public Token subject(String value) {
            this.subject = value;
            return this;
        }

        /** Sets {@code scope} (space-separated). */
        public Token scope(String value) {
            claims.put("scope", value);
            return this;
        }

        /** Sets {@code scp} as an array. */
        public Token scp(String... values) {
            claims.put("scp", List.of(values));
            return this;
        }

        /** Sets any claim. */
        public Token claim(String name, Object value) {
            claims.put(name, value);
            return this;
        }

        /** Sets {@code exp}. */
        public Token expiresAt(Instant value) {
            this.expiresAt = value;
            return this;
        }

        /** Sets {@code nbf}. */
        public Token notBefore(Instant value) {
            this.notBefore = value;
            return this;
        }

        /** Signs with {@code value} and {@code algorithm}. */
        public Token signWith(JWK value, JWSAlgorithm algorithm) {
            this.key = value;
            this.alg = algorithm;
            return this;
        }

        /** Signs with HS256 and {@code secret} (an algorithm Foresignal must refuse). */
        public Token hmac(byte[] secret) {
            this.hmacSecret = secret;
            this.alg = JWSAlgorithm.HS256;
            return this;
        }

        /** Overrides the header {@code kid} (null for none). */
        public Token kid(String value) {
            this.kid = value;
            this.kidSet = true;
            return this;
        }

        /** The compact signed JWT. */
        public String sign() {
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().issuer(issuer).subject(subject);
            if (!audience.isEmpty()) {
                builder.audience(audience);
            }
            if (expiresAt != null) {
                builder.expirationTime(Date.from(expiresAt));
            }
            if (notBefore != null) {
                builder.notBeforeTime(Date.from(notBefore));
            }
            claims.forEach(builder::claim);
            String headerKid = kidSet ? kid : (hmacSecret != null ? null : key.getKeyID());
            JWSHeader header = new JWSHeader.Builder(alg).keyID(headerKid).build();
            SignedJWT jwt = new SignedJWT(header, builder.build());
            try {
                jwt.sign(signer());
            } catch (JOSEException e) {
                throw new IllegalStateException(e);
            }
            return jwt.serialize();
        }

        private JWSSigner signer() throws JOSEException {
            if (hmacSecret != null) {
                return new MACSigner(hmacSecret);
            }
            return switch (key) {
                case RSAKey r -> new RSASSASigner(r);
                case ECKey e -> new ECDSASigner(e);
                case OctetKeyPair o -> new Ed25519Signer(o);
                default -> throw new IllegalStateException("unsupported key " + key.getKeyType());
            };
        }
    }
}
