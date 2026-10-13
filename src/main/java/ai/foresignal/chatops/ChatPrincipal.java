package ai.foresignal.chatops;

import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * The principal behind an allowlisted chat (and user), prompt 061:
 * {@code chat:{channelId}:{chatId}[:{userId}]} with the chat's scope and private grant.
 *
 * <p>It acts through the same service-layer checks as an API token: {@link #token()} is the
 * {@link AuthenticatedToken} the MCP and Ask callers (023) take, with a stable id derived from the name, so
 * an Ask thread or proposed action created from a chat stays visible to that chat only. It is authenticated,
 * so it may use {@code alwaysAuthenticated} services such as Ask. Its scope is never {@code admin}.
 *
 * @param name {@code chat:{channelId}:{chatId}} or {@code chat:{channelId}:{chatId}:{userId}}
 * @param scope {@link Scope#READ} or {@link Scope#AGENT}
 * @param privateAccess the 039 private grant
 */
public record ChatPrincipal(String name, Scope scope, boolean privateAccess) {

    /** The prefix of every chat principal name. */
    public static final String PREFIX = "chat:";

    /** Requires the name and a non-admin scope. */
    public ChatPrincipal {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(scope, "scope");
        if (scope == Scope.ADMIN) {
            throw new IllegalArgumentException("A chat principal can never have admin scope");
        }
    }

    /** The principal of {@code chat} for the sender {@code userId} (named per user when the chat lists users). */
    public static ChatPrincipal of(UUID channelId, AllowedChat chat, String userId) {
        String name = PREFIX + channelId + ":" + chat.chatId()
                + (chat.userIds().isEmpty() || userId == null ? "" : ":" + userId);
        return new ChatPrincipal(name, chat.scope(), chat.privateAccess());
    }

    /** The stable id of this principal, used where a token id is recorded (Ask threads and actions). */
    public UUID id() {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * This principal as the authenticated token the service layer takes. It has no 023 principal row, so its
     * {@code principalId} is null (audit and Ask rows record the name as the token name instead).
     */
    public AuthenticatedToken token() {
        return new AuthenticatedToken(id(), name, scope, TokenRestrictions.NONE, null, null, false, privateAccess);
    }
}
