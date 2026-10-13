package ai.foresignal.chatops;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.chatops.ChatopsItSupport;
import ai.foresignal.testsupport.chatops.RecordingConnector;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Every command from an allowlisted chat is audited (prompt 061): actor kind {@code chat}, the chat principal as
 * the token name, its scope, the command, its argument names (never their values), the result and the duration;
 * messages from chats that are not allowlisted are not.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class ChatAuditIT extends ChatopsItSupport {

    @Autowired CommandRouter router;

    final UUID channel = UUID.randomUUID();

    ChatSession session(RecordingConnector connector, AllowedChat... chats) {
        ChannelSettings s = new ChannelSettings(channel, "ops", Map.of(), Map.of(), ZoneOffset.UTC, 0, T, "x", "ua");
        return new ChatSession(new ChannelConfig(channel, "ops", "telegram", s,
                new InteractiveConfig(true, List.of(chats), 10, 30, false, false)), connector);
    }

    @Test
    @DisplayName("each command is audited with the principal name, command, argument names and result")
    void everyCommandAudited() {
        long alert = alerts.alert(alerts.rule("Quakes", T), "warning", T);
        RecordingConnector connector = new RecordingConnector(true);
        ChatSession session = session(connector, new AllowedChat("5001", null, Scope.AGENT, List.of(), false),
                new AllowedChat("6001", null, Scope.READ, List.of(), false));
        String agent = "chat:" + channel + ":5001";
        String reader = "chat:" + channel + ":6001";

        router.onMessage(session, new ChatRef(channel, "5001", "5001", "Ana", "1"), "/help");
        router.onMessage(session, new ChatRef(channel, "5001", "5001", "Ana", "2"), "/alerts 3");
        router.onMessage(session, new ChatRef(channel, "5001", "5001", "Ana", "3"), "/ack " + alert);
        router.onMessage(session, new ChatRef(channel, "6001", "6001", "Bo", "4"), "/resolve " + alert);
        router.onMessage(session, new ChatRef(channel, "6001", "6001", "Bo", "5"), "/nonsense");
        router.onMessage(session, new ChatRef(channel, "7", "7", "Eve", "6"), "/alerts");
        audit.flush();

        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT actor_kind, token_name, scope, target_type, detail->>'command' AS command,
                       detail->>'result' AS result, detail->'arguments' AS arguments, detail->>'durationMs' AS duration,
                       detail::text AS detail
                  FROM audit_event WHERE actor_kind = 'chat' ORDER BY id""");
        assertThat(rows).hasSize(5);
        assertThat(rows).extracting(r -> r.get("token_name")).containsExactly(agent, agent, agent, reader, reader);
        assertThat(rows).extracting(r -> r.get("command")).containsExactly("help", "alerts", "ack", "resolve", "nonsense");
        assertThat(rows).extracting(r -> r.get("result")).containsExactly("ok", "ok", "ok", "denied", "invalid");
        assertThat(rows).extracting(r -> r.get("scope")).containsExactly("agent", "agent", "agent", "read", "read");
        assertThat(rows).allSatisfy(r -> {
            assertThat(r.get("target_type")).isEqualTo(CommandRouter.AUDIT_TARGET);
            assertThat(r.get("duration")).isNotNull();
        });
        assertThat(String.valueOf(rows.get(1).get("arguments"))).contains("n");
        assertThat(String.valueOf(rows.get(2).get("arguments"))).contains("id");
        assertThat(String.valueOf(rows.get(2).get("detail"))).doesNotContain("\"" + alert + "\"");
        assertThat(alerts.acknowledgedBy(alert)).isEqualTo(agent);
        assertThat(alerts.state(alert)).isEqualTo("acknowledged");
    }
}
