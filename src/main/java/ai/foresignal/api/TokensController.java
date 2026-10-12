package ai.foresignal.api;

import ai.foresignal.api.dto.ApiTokenDto;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.ticket.TicketStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code listTokens}, {@code createToken}, {@code revokeToken} (admin),
 * {@code updateCurrentTokenDevice} and {@code createStreamTicket} (read, always authenticated)
 * (prompt 023).
 */
@RestController
@Tag(name = "Tokens")
public class TokensController {

    private final TokenService tokens;
    private final TicketStore tickets;

    /** Creates the controller. */
    public TokensController(TokenService tokens, TicketStore tickets) {
        this.tokens = tokens;
        this.tickets = tickets;
    }

    /** {@code listTokens}: every token, newest first; values are never returned. */
    @GetMapping("/v1/tokens")
    @Operation(operationId = "listTokens", tags = "Tokens")
    public List<ApiTokenDto> listTokens() {
        Map<String, String> handles = new LinkedHashMap<>();
        return tokens.list().stream()
                .map(t -> ApiTokenDto.of(t, handles.computeIfAbsent(t.getPrincipalId(), tokens::principalHandle)))
                .toList();
    }

    /** {@code createToken}: 201 with the value, shown once. */
    @PostMapping("/v1/tokens")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createToken", tags = "Tokens")
    public ApiTokenDto createToken(@RequestBody JsonNode body) {
        TokenService.CreatedToken created = tokens.create(body);
        return ApiTokenDto.of(created.entity(), created.principalHandle(), created.plaintext());
    }

    /** {@code revokeToken}: 204; 409 {@code BOOTSTRAP_TOKEN} for the bootstrap token. */
    @DeleteMapping("/v1/tokens/{tokenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "revokeToken", tags = "Tokens")
    public void revokeToken(@PathVariable String tokenId) {
        tokens.revoke(uuid(tokenId, "tokenId"));
    }

    /** {@code updateCurrentTokenDevice}: the calling token reports its own device. */
    @PutMapping("/v1/tokens/current/device")
    @Operation(operationId = "updateCurrentTokenDevice", tags = "Tokens")
    public ApiTokenDto updateCurrentTokenDevice(@RequestBody JsonNode body, HttpServletRequest request) {
        CurrentToken current = current(request);
        ApiTokenEntity updated = tokens.updateDevice(current.id(), body);
        return ApiTokenDto.of(updated, tokens.principalHandle(updated.getPrincipalId()));
    }

    /** {@code createStreamTicket}: 201 with a ticket inheriting the calling token. */
    @PostMapping("/v1/stream-tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createStreamTicket", tags = "Tokens")
    public Map<String, Object> createStreamTicket(@RequestBody JsonNode body, HttpServletRequest request) {
        TicketStore.MintedTicket minted = tickets.mint(current(request).token(), body);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ticket", minted.ticket());
        out.put("purpose", minted.purpose().wireValue());
        out.put("expiresAt", minted.expiresAt());
        if (!minted.paths().isEmpty()) {
            out.put("paths", minted.paths());
        }
        return out;
    }

    /** The calling token; the filter guarantees one on these always-authenticated paths. */
    private static CurrentToken current(HttpServletRequest request) {
        return CurrentToken.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This endpoint requires a bearer token."));
    }

    static UUID uuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField(field, "must be a UUID");
        }
    }
}
