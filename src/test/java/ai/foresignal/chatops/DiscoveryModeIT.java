package ai.foresignal.chatops;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.chatops.ChatopsItSupport;
import ai.foresignal.testsupport.chatops.RecordingConnector;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * {@code discoveryMode} (prompt 061): {@code /whoami} from any chat answers with only that chat's id and the
 * sender's id while it is on, never when it is off, and it turns itself off after 10 minutes (the stored
 * {@code discoveryMode} becomes false) on the {@code MutableClock}.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class DiscoveryModeIT extends ChatopsItSupport {

    @Autowired CommandRouter router;

    ChatSession session(UUID channel, RecordingConnector connector) {
        return new ChatSession(config(channel), connector);
    }

    @Test
    @DisplayName("/whoami answers any chat only while discovery mode is on, and discovery turns itself off")
    void whoami() {
        UUID channel = telegram("[{'chatId':'123','scope':'read'}]", ",'discoveryMode':true");
        RecordingConnector connector = new RecordingConnector(true);
        ChatRef stranger = new ChatRef(channel, "-555", "777", "Eve", "1");

        assertThat(router.onMessage(session(channel, connector), stranger, "/whoami")).isPresent();
        assertThat(connector.lastText()).contains("-555").contains("777").doesNotContain("123");
        assertThat(manager.status(channelRows.findById(channel).orElseThrow()).get("discoveryModeUntil"))
                .isEqualTo(T.plus(DiscoveryMode.WINDOW).toString());
        // Anything but /whoami from an unknown chat is still ignored.
        assertThat(router.onMessage(session(channel, connector), stranger, "/alerts")).isEmpty();

        clock.setInstant(T.plus(Duration.ofMinutes(9)));
        assertThat(router.onMessage(session(channel, connector), stranger, "/whoami")).isPresent();

        clock.setInstant(T.plus(Duration.ofMinutes(11)));
        int before = connector.replies().size();
        assertThat(router.onMessage(session(channel, connector), stranger, "/whoami")).isEmpty();
        assertThat(connector.replies()).hasSize(before);
        assertThat(channelRows.findById(channel).orElseThrow().getConfig().get("discoveryMode")).isEqualTo(false);
        // A stale config that still says true does not reopen the window.
        clock.setInstant(T.plus(Duration.ofMinutes(12)));
        ChannelConfig stale = new ChannelConfig(channel, "ops", "telegram", config(channel).settings(),
                new InteractiveConfig(true, config(channel).interactive().allowedChats(), 10, 30, false, true));
        assertThat(router.onMessage(new ChatSession(stale, connector), stranger, "/whoami")).isEmpty();
    }

    @Test
    @DisplayName("/whoami gets no answer while discovery mode is off")
    void off() {
        UUID channel = telegram("[{'chatId':'123','scope':'read'}]", "");
        RecordingConnector connector = new RecordingConnector(true);
        assertThat(router.onMessage(session(channel, connector), new ChatRef(channel, "-555", "777", "Eve", "1"),
                "/whoami")).isEmpty();
        assertThat(connector.replies()).isEmpty();
    }
}
