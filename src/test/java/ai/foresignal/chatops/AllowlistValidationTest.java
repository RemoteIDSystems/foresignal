package ai.foresignal.chatops;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.types.MatrixChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.security.Scope;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Validation of the interactive members of {@code telegram} and {@code matrix} channels (prompt 061): a group
 * without {@code userIds} is rejected, {@code admin} scope is rejected, at most 50 chats, and the scalar members.
 */
class AllowlistValidationTest {

    static Map<String, Object> config(Object chats) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("chatId", "1");
        config.put("interactive", true);
        config.put("allowedChats", chats);
        return config;
    }

    static Map<String, Object> chat(String id, String scope) {
        Map<String, Object> chat = new LinkedHashMap<>();
        chat.put("chatId", id);
        chat.put("scope", scope);
        return chat;
    }

    static List<String> fields(List<FieldError> errors) {
        return errors.stream().map(FieldError::field).toList();
    }

    @Test
    @DisplayName("a Telegram group without userIds is rejected; with userIds it is accepted")
    void groupNeedsUsers() {
        List<FieldError> errors = InteractiveConfig.validate(TelegramChannel.ID, config(List.of(chat("-1001234", "read"))));
        assertThat(fields(errors)).containsExactly("config.allowedChats[0].userIds");
        assertThat(errors.getFirst().message()).contains("any member could command");

        Map<String, Object> group = chat("-1001234", "read");
        group.put("userIds", List.of("42", 43));
        assertThat(InteractiveConfig.validate(TelegramChannel.ID, config(List.of(group)))).isEmpty();
        assertThat(InteractiveConfig.validate(TelegramChannel.ID, config(List.of(chat("12345", "agent"))))).isEmpty();
    }

    @Test
    @DisplayName("admin scope is rejected, and so is any scope other than read or agent")
    void adminRejected() {
        List<FieldError> admin = InteractiveConfig.validate(TelegramChannel.ID, config(List.of(chat("1", "admin"))));
        assertThat(fields(admin)).containsExactly("config.allowedChats[0].scope");
        assertThat(admin.getFirst().message()).contains("admin is never possible");
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, config(List.of(chat("1", "owner"))))))
                .containsExactly("config.allowedChats[0].scope");
        Map<String, Object> noScope = new LinkedHashMap<>(Map.of("chatId", "1"));
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, config(List.of(noScope)))))
                .containsExactly("config.allowedChats[0].scope");
        // A stored admin entry (never accepted by validation) is left out when read.
        assertThat(InteractiveConfig.of(config(List.of(chat("1", "admin")))).allowedChats()).isEmpty();
    }

    @Test
    @DisplayName("at most 50 chats")
    void fiftyEntryCap() {
        List<Object> chats = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            chats.add(chat(Integer.toString(i), "read"));
        }
        assertThat(InteractiveConfig.validate(TelegramChannel.ID, config(chats))).isEmpty();
        chats.add(chat("51", "read"));
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, config(chats)))).containsExactly("config.allowedChats");
    }

    @Test
    @DisplayName("ids, duplicates, members and the scalar settings are checked")
    void shapes() {
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, config(List.of(chat("@me", "read"))))))
                .containsExactly("config.allowedChats[0].chatId");
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, config(List.of(chat("5", "read"), chat("5", "agent"))))))
                .containsExactly("config.allowedChats[1].chatId");
        Map<String, Object> extra = chat("5", "read");
        extra.put("token", "x");
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, config(List.of(extra)))))
                .containsExactly("config.allowedChats[0].token");
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, config("nope")))).containsExactly("config.allowedChats");
        Map<String, Object> scalars = config(List.of());
        scalars.put("interactive", "yes");
        scalars.put("commandRateLimit", 0);
        scalars.put("askRateLimitPerHour", -1);
        scalars.put("discoveryMode", 1);
        assertThat(fields(InteractiveConfig.validate(TelegramChannel.ID, scalars))).containsExactlyInAnyOrder(
                "config.interactive", "config.commandRateLimit", "config.askRateLimitPerHour", "config.discoveryMode");
    }

    @Test
    @DisplayName("Matrix rooms and user ids use Matrix identifiers; rooms need no userIds at save time")
    void matrix() {
        assertThat(InteractiveConfig.validate(MatrixChannel.ID, config(List.of(chat("!abc:example.org", "agent"))))).isEmpty();
        assertThat(fields(InteractiveConfig.validate(MatrixChannel.ID, config(List.of(chat("-100", "agent"))))))
                .containsExactly("config.allowedChats[0].chatId");
        Map<String, Object> users = chat("!abc:example.org", "read");
        users.put("userIds", List.of("ana"));
        assertThat(fields(InteractiveConfig.validate(MatrixChannel.ID, config(List.of(users)))))
                .containsExactly("config.allowedChats[0].userIds");
        assertThat(new MatrixChannel().validate(config(List.of(chat("!abc:example.org", "admin"))), Map.of()))
                .isNotEmpty();
        assertThat(new TelegramChannel().validate(config(List.of(chat("-5", "read"))), Map.of())).isNotEmpty();
    }

    @Test
    @DisplayName("the stored config reads leniently with defaults")
    void reading() {
        Map<String, Object> group = chat("-100", "agent");
        group.put("userIds", List.of(42L));
        group.put("privateAccess", true);
        InteractiveConfig read = InteractiveConfig.of(config(List.of(group)));
        assertThat(read.interactive()).isTrue();
        assertThat(read.askRateLimitPerHour()).isEqualTo(InteractiveConfig.DEFAULT_ASK_RATE);
        assertThat(read.commandRateLimit()).isEqualTo(InteractiveConfig.DEFAULT_COMMAND_RATE);
        AllowedChat chat = read.chat("-100");
        assertThat(chat.scope()).isEqualTo(Scope.AGENT);
        assertThat(chat.userIds()).containsExactly("42");
        assertThat(chat.privateAccess()).isTrue();
        assertThat(chat.allows("42")).isTrue();
        assertThat(chat.allows("43")).isFalse();
        assertThat(InteractiveConfig.of(null).interactive()).isFalse();
    }
}
