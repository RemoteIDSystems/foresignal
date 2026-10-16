package ai.foresignal.api;

import ai.foresignal.provenance.SigningKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The instance Ed25519 signing key (OpenAPI tag {@code System}, prompt 109): the JWKS is published
 * anonymously so any JOSE library can verify a detached JWS, and an admin may rotate it. The private
 * key never leaves the server.
 */
@RestController
@Tag(name = "System")
public class SigningController {

    private final SigningKeyService signing;

    /** Creates the controller. */
    public SigningController(SigningKeyService signing) {
        this.signing = signing;
    }

    /** {@code getJwks}: the JWKS at the well-known location (open). */
    @GetMapping("/.well-known/jwks.json")
    @Operation(operationId = "getJwks", tags = "System")
    public ResponseEntity<Map<String, Object>> getJwks() {
        return jwks();
    }

    /** {@code getSystemJwks}: the same JWKS under the system namespace (open). */
    @GetMapping("/v1/system/jwks")
    @Operation(operationId = "getSystemJwks", tags = "System")
    public ResponseEntity<Map<String, Object>> getSystemJwks() {
        return jwks();
    }

    /** {@code rotateSigningKey}: retire the active key and activate a new one (admin). */
    @PostMapping("/v1/system/signing-key/rotate")
    @Operation(operationId = "rotateSigningKey", tags = "System")
    public Map<String, Object> rotateSigningKey() {
        return signing.rotateView();
    }

    private ResponseEntity<Map<String, Object>> jwks() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("public, max-age=3600");
        return ResponseEntity.ok().headers(headers).body(signing.jwks());
    }
}
