package ai.foresignal.api;

import ai.foresignal.api.dto.ApiTokenDto;
import ai.foresignal.api.dto.PairingCodeDto;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.pairing.PairingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Device pairing (prompt 023): {@code listPairingCodes}, {@code createPairingCode},
 * {@code deletePairingCode}, {@code getPairingCodeQr} (admin) and {@code claimPairingCode} (open;
 * the code is the credential).
 */
@RestController
@Tag(name = "Tokens")
public class PairingController {

    /** The QR code's media type. */
    public static final String SVG = "image/svg+xml";

    private final PairingService pairing;
    private final TokenService tokens;

    /** Creates the controller. */
    public PairingController(PairingService pairing, TokenService tokens) {
        this.pairing = pairing;
        this.tokens = tokens;
    }

    /** {@code listPairingCodes}: unclaimed, unexpired codes; code values are never listed. */
    @GetMapping("/v1/pairing-codes")
    @Operation(operationId = "listPairingCodes", tags = "Tokens")
    public List<PairingCodeDto> listPairingCodes() {
        return pairing.listOpen().stream().map(PairingCodeDto::of).toList();
    }

    /** {@code createPairingCode}: 201 with the code and QR payload, shown once. */
    @PostMapping("/v1/pairing-codes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createPairingCode", tags = "Tokens")
    public PairingCodeDto createPairingCode(@RequestBody JsonNode body, HttpServletRequest request) {
        AuthenticatedToken creator = CurrentToken.of(request).map(CurrentToken::token).orElse(null);
        PairingService.CreatedCode created = pairing.create(body, creator, requestBaseUrl(request));
        return PairingCodeDto.of(created.entity(), created.code(), created.qrPayload());
    }

    /** {@code deletePairingCode}: 204. */
    @DeleteMapping("/v1/pairing-codes/{pairingCodeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deletePairingCode", tags = "Tokens")
    public void deletePairingCode(@PathVariable String pairingCodeId) {
        pairing.delete(TokensController.uuid(pairingCodeId, "pairingCodeId"));
    }

    /** {@code getPairingCodeQr}: the SVG QR code of the payload, while the code is open. */
    @GetMapping(value = "/v1/pairing-codes/{pairingCodeId}/qr.svg", produces = SVG)
    @Operation(operationId = "getPairingCodeQr", tags = "Tokens")
    public ResponseEntity<String> getPairingCodeQr(@PathVariable String pairingCodeId) {
        String svg = pairing.qrSvg(TokensController.uuid(pairingCodeId, "pairingCodeId"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, SVG)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(svg);
    }

    /** {@code claimPairingCode}: 201 with a new token, shown once. */
    @PostMapping("/v1/pairing/claim")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "claimPairingCode", tags = "Tokens")
    public ApiTokenDto claimPairingCode(@RequestBody JsonNode body, HttpServletRequest request) {
        TokenService.CreatedToken created = pairing.claim(body, request.getRemoteAddr());
        return ApiTokenDto.of(created.entity(), created.principalHandle(), created.plaintext());
    }

    /** The request's own scheme, host and port (after trusted-proxy handling). */
    static String requestBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)
                || port <= 0;
        return scheme + "://" + request.getServerName() + (defaultPort ? "" : ":" + port);
    }
}
