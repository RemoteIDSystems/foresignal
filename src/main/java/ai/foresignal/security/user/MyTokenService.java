package ai.foresignal.security.user;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * A signed-in user's own API tokens (prompt R2-025): {@code createMyToken}, {@code listMyTokens} and
 * {@code revokeMyToken}, over {@link TokenService}: the same {@code api_token} row, format and restrictions as an
 * admin-made token, owned by the user's {@code person} principal and marked {@code createdVia: self}.
 *
 * <p>A token may not reach further than its owner: its scope is at most {@link TokenOwnership#maxScope} of the user's
 * permissions and the private grant needs an admin-derived session ({@code 403 TOKEN_EXCEEDS_USER} otherwise). Its
 * restrictions only narrow; on every request the owner's current data restriction narrows it further and the owner's
 * current permissions cap it again ({@link TokenOwnership}).
 */
@Service
public class MyTokenService {

    private final TokenService tokens;

    /**
     * Creates the service.
     *
     * @param tokens the token subsystem
     */
    public MyTokenService(TokenService tokens) {
        this.tokens = tokens;
    }

    /**
     * {@code createMyToken}.
     *
     * @param session the caller
     * @param body the {@code ApiTokenInput}
     * @return the created token, with its value shown once
     * @throws ValidationException an invalid input, or a {@code principalId} (the owner is always the caller)
     * @throws ForesignalException {@code 403 TOKEN_EXCEEDS_USER} for a scope or private grant beyond the caller's
     */
    @Transactional
    public TokenService.CreatedToken create(AuthenticatedSession session, JsonNode body) {
        if (body != null && body.isObject() && body.get("principalId") != null && !body.get("principalId").isNull()) {
            throw ValidationException.forField("principalId", "is not accepted: your tokens are always your own");
        }
        TokenService.TokenSpec parsed = tokens.parse(body, TokenCreatedVia.SELF);
        EffectivePermissions held = session.permissions();
        Scope max = TokenOwnership.maxScope(held);
        if (!max.includes(parsed.scope())) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.TOKEN_EXCEEDS_USER, "Your roles allow tokens"
                    + " of scope " + max.wireValue() + " at most; " + parsed.scope().wireValue() + " was asked for.");
        }
        if (parsed.privateAccess() && !TokenOwnership.mayGrantPrivate(held)) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.TOKEN_EXCEEDS_USER, "You do not see private"
                    + " sources, so a token of yours cannot either.");
        }
        return tokens.create(new TokenService.TokenSpec(parsed.name(), parsed.scope(), parsed.expiresAt(),
                parsed.restrictions(), null, TokenCreatedVia.SELF, session.principalId(), parsed.privateAccess(),
                parsed.licencePolicy()));
    }

    /**
     * {@code listMyTokens}: the caller's tokens, newest first, revoked ones included; never a value.
     *
     * @param session the caller
     * @return the tokens
     */
    @Transactional(readOnly = true)
    public List<ApiTokenEntity> list(AuthenticatedSession session) {
        return tokens.listOwned(session.principalId());
    }

    /**
     * {@code revokeMyToken}: revokes one of the caller's tokens; the token filter drops it at once.
     *
     * @param session the caller
     * @param id the token
     * @throws ai.foresignal.api.error.NotFoundException when the caller owns no such token
     */
    @Transactional
    public void revoke(AuthenticatedSession session, UUID id) {
        tokens.revokeOwned(id, session.principalId());
    }
}
