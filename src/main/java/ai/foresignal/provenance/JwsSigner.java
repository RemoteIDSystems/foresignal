package ai.foresignal.provenance;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Detached JWS over the instance's Ed25519 key (prompt 109; RFC 7515 Appendix F, RFC 7797):
 * {@code signing input = ASCII(BASE64URL(header)) || '.' || payload}, output
 * {@code BASE64URL(header)..BASE64URL(signature)}. The header sets {@code b64:false} with
 * {@code crit:["b64"]}. {@code signing.legacyAlg} emits {@code "alg":"EdDSA"} for older verifiers.
 */
@Component
public class JwsSigner {

    private static final Base64.Encoder URL = Base64.getUrlEncoder().withoutPadding();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final SigningKeyService signing;
    private final boolean legacyAlg;
    private final String issuer;

    /** Creates the signer. */
    public JwsSigner(SigningKeyService signing, @Value("${foresignal.signing.legacy-alg:false}") boolean legacyAlg,
            @Value("${foresignal.instance-id:foresignal}") String issuer) {
        this.signing = signing;
        this.legacyAlg = legacyAlg;
        this.issuer = issuer == null || issuer.isBlank() ? "foresignal" : issuer;
    }

    /**
     * Signs {@code payload} as a detached JWS.
     *
     * @param typ the {@code typ} header (for example {@code text/markdown} or {@code application/json})
     * @return {@code BASE64URL(header)..BASE64URL(signature)}
     */
    public String signDetached(byte[] payload, String typ) {
        // kid and private key come from one row in one read, so a rotation cannot pair one key's kid with
        // another key's signature.
        SigningKeyService.Material key = signing.activeMaterial();
        PrivateKey privateKey = key.privateKey();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", legacyAlg ? "EdDSA" : SigningKeyService.ALG);
        header.put("kid", key.kid());
        header.put("typ", typ);
        header.put("b64", false);
        header.put("crit", List.of("b64"));
        header.put("iat", java.time.Instant.now().getEpochSecond());
        header.put("iss", issuer);
        String headerB64 = URL.encodeToString(JSON.writeValueAsString(header).getBytes(StandardCharsets.US_ASCII));
        byte[] signingInput = signingInput(headerB64, payload);
        byte[] signature = sign(privateKey, signingInput);
        return headerB64 + ".." + URL.encodeToString(signature);
    }

    /** The signing input: {@code ASCII(headerB64) || '.' || payload}. */
    static byte[] signingInput(String headerB64, byte[] payload) {
        byte[] prefix = (headerB64 + ".").getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[prefix.length + payload.length];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(payload, 0, out, prefix.length, payload.length);
        return out;
    }

    private static byte[] sign(PrivateKey key, byte[] input) {
        try {
            Signature signature = Signature.getInstance(SigningKeyService.ALG);
            signature.initSign(key);
            signature.update(input);
            return signature.sign();
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign", e);
        }
    }
}
