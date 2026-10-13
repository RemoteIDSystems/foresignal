package ai.foresignal.chatops;

import ai.foresignal.security.Scope;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One allowlisted chat of an interactive channel (OpenAPI {@code AllowedChat}, prompt 061).
 *
 * @param chatId the Telegram chat id as a string (negative for groups) or the Matrix room id
 * @param name a label for the operator, or null
 * @param scope {@link Scope#READ} or {@link Scope#AGENT}; never {@link Scope#ADMIN}
 * @param userIds the senders allowed in this chat (Telegram user ids, Matrix MXIDs); empty allows every
 *     member, which validation permits only for one-to-one chats
 * @param privateAccess the 039 private grant: the chat may see private sources
 */
public record AllowedChat(String chatId, String name, Scope scope, List<String> userIds, boolean privateAccess) {

    /** Requires the chat id and scope; copies the user ids. */
    public AllowedChat {
        Objects.requireNonNull(chatId, "chatId");
        Objects.requireNonNull(scope, "scope");
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
    }

    /** True when {@code userId} may send commands in this chat. */
    public boolean allows(String userId) {
        return userIds.isEmpty() || (userId != null && userIds.contains(userId));
    }

    /** The API form ({@code AllowedChat}). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("chatId", chatId);
        out.put("name", name);
        out.put("scope", scope.wireValue());
        out.put("userIds", userIds);
        out.put("privateAccess", privateAccess);
        return out;
    }
}
