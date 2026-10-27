package ai.foresignal.api;

import ai.foresignal.api.dto.ApiTokenDto;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.MyTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code listMyTokens}, {@code createMyToken} and {@code revokeMyToken} (prompt R2-025): a signed-in user's own API
 * tokens. They need a session ({@code 401} for an API token), so a token can never mint another token.
 */
@RestController
@Tag(name = "Tokens")
public class MyTokensController {

    private final MyTokenService myTokens;
    private final TokenService tokens;

    /**
     * Creates the controller.
     *
     * @param myTokens the self-service tokens
     * @param tokens principal handles for the views
     */
    public MyTokensController(MyTokenService myTokens, TokenService tokens) {
        this.myTokens = myTokens;
        this.tokens = tokens;
    }

    /** {@code listMyTokens}: the caller's tokens, newest first; values are never returned. */
    @GetMapping("/v1/me/tokens")
    @Operation(operationId = "listMyTokens", tags = "Tokens")
    public List<ApiTokenDto> list(HttpServletRequest request) {
        AuthenticatedSession session = session(request);
        String handle = tokens.principalHandle(session.principalId());
        return myTokens.list(session).stream().map(t -> ApiTokenDto.of(t, handle)).toList();
    }

    /** {@code createMyToken}: 201 with the value, shown once. */
    @PostMapping("/v1/me/tokens")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createMyToken", tags = "Tokens")
    public ApiTokenDto create(@RequestBody JsonNode body, HttpServletRequest request) {
        TokenService.CreatedToken created = myTokens.create(session(request), body);
        return ApiTokenDto.of(created.entity(), created.principalHandle(), created.plaintext());
    }

    /** {@code revokeMyToken}: 204; 404 for a token the caller does not own. */
    @DeleteMapping("/v1/me/tokens/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "revokeMyToken", tags = "Tokens")
    public void revoke(@PathVariable String id, HttpServletRequest request) {
        AuthenticatedSession session = session(request);
        UUID tokenId;
        try {
            tokenId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Token", id);
        }
        myTokens.revoke(session, tokenId);
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
