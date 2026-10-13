package ai.foresignal.chatops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.ask.AskActionService;
import ai.foresignal.ask.AskService;
import ai.foresignal.briefing.BriefingLinks;
import ai.foresignal.briefing.BriefingRepository;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.collect.HealthService;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.security.AccessPolicy;
import ai.foresignal.security.Scope;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stats.glance.GlanceService;
import ai.foresignal.testsupport.chatops.RecordingConnector;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

/**
 * The command router (prompt 061): the scope matrix (read chats cannot acknowledge, resolve, snooze or run
 * briefings, checked through the real {@link AccessPolicy}), help for unknown commands, strict argument parsing
 * and limits, silent rejection of unknown chats, the per-chat command limit, and private alerts hidden from chats
 * without {@code privateAccess}.
 */
class CommandRouterTest {

    static final Instant T = Instant.parse("2026-10-13T12:00:00Z");
    static final UUID CHANNEL = UUID.fromString("00000000-0000-0000-0000-000000000061");
    static final String SECRET = "secret-src";

    final Clock clock = Clock.fixed(T, ZoneOffset.UTC);
    final AlertService alerts = mock(AlertService.class);
    final ReadCallerResolver readCallers = mock(ReadCallerResolver.class);
    final AppSettingService settings = mock(AppSettingService.class);
    final PendingTokens pending = mock(PendingTokens.class);
    final DiscoveryMode discovery = mock(DiscoveryMode.class);
    final AuditService audit = mock(AuditService.class);
    final BriefingService briefings = mock(BriefingService.class);
    final BriefingLinks links = mock(BriefingLinks.class);
    final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    final RecordingConnector connector = new RecordingConnector(true);

    CommandRouter router;

    @BeforeEach
    void setUp() {
        when(settings.getOrDefault(eq(SettingKeys.SECURITY), eq(SecuritySettings.class), any()))
                .thenReturn(SecuritySettings.defaults());
        when(readCallers.excludedSources(any(ReadCaller.class)))
                .thenAnswer(inv -> ((ReadCaller) inv.getArgument(0)).privateAccess() ? Set.of() : Set.of(SECRET));
        when(links.alert(anyLong())).thenAnswer(inv -> "http://fs.test/v1/alerts/" + inv.getArgument(0));
        when(links.baseUrl()).thenReturn("http://fs.test");
        when(pending.button(any(), anyString(), anyString())).thenReturn("cc_test");
        ChatPrincipals principals = new ChatPrincipals(readCallers, settings, new AccessPolicy());
        ChatCommandService commands = new ChatCommandService(alerts, principals, mock(GlanceService.class),
                mock(HealthService.class), mock(AskService.class), mock(AskActionService.class), briefings,
                mock(BriefingRepository.class), links, pending, new ChatRateLimiter(clock), settings,
                ChatopsProperties.defaults(), JsonMapper.builder().build(), clock);
        router = new CommandRouter(commands, principals, pending, new ChatRateLimiter(clock), discovery, audit, meters,
                clock);
    }

    static ChatSession session(RecordingConnector connector, int commandRate, AllowedChat... chats) {
        ChannelSettings s = new ChannelSettings(CHANNEL, "ops", Map.of(), Map.of(), ZoneOffset.UTC, 0, T, "x", "ua");
        InteractiveConfig config = new InteractiveConfig(true, List.of(chats), 10, commandRate, false, false);
        return new ChatSession(new ChannelConfig(CHANNEL, "ops", "telegram", s, config), connector);
    }

    static AllowedChat chat(String id, Scope scope, boolean privateAccess) {
        return new AllowedChat(id, null, scope, List.of(), privateAccess);
    }

    static ChatRef from(String chatId) {
        return new ChatRef(CHANNEL, chatId, "42", "Ana", "100");
    }

    static AlertView alert(long id, String source) {
        return new AlertView(id, UUID.randomUUID(), "Quakes", 1, source, "e" + id, "quake", T.minusSeconds(300),
                T.minusSeconds(300), "M6 quake", "warning", "firing", 1, null, null, null, null, null, null, null, null,
                null, false, null, List.of(), null);
    }

    Optional<ChatOutcome> send(ChatSession session, String text) {
        return router.onMessage(session, from(session.config().interactive().allowedChats().getFirst().chatId()), text);
    }

    @Test
    @DisplayName("read chats cannot acknowledge, resolve, snooze or run briefings; agent chats can acknowledge")
    void scopeMatrix() {
        ChatSession read = session(connector, 30, chat("10", Scope.READ, false));
        for (String command : List.of("/ack 5", "/resolve 5", "/snooze 5 1h", "/brief run Morning", "/confirm ABC234")) {
            ChatOutcome outcome = send(read, command).orElseThrow();
            assertThat(outcome.result()).as(command).isEqualTo(ChatOutcome.DENIED);
            assertThat(outcome.reply().markdown()).contains("read scope");
        }
        verify(alerts, never()).acknowledge(anyLong(), any(), anyString());
        verify(alerts, never()).resolve(anyLong(), any(), anyString());
        verify(alerts, never()).snooze(anyLong(), any(Instant.class));
        verify(briefings, never()).runNow(any());

        when(alerts.get(5)).thenReturn(alert(5, "quakes"));
        when(alerts.acknowledge(eq(5L), any(), anyString())).thenReturn(alert(5, "quakes"));
        ChatSession agent = session(connector, 30, chat("20", Scope.AGENT, false));
        assertThat(send(agent, "/ack 5").orElseThrow().result()).isEqualTo(ChatOutcome.OK);
        verify(alerts).acknowledge(eq(5L), eq(AlertService.Transition.NONE), eq("chat:" + CHANNEL + ":20"));
    }

    @Test
    @DisplayName("help lists only the commands of the chat's scope")
    void helpByScope() {
        ChatOutcome read = send(session(connector, 30, chat("10", Scope.READ, false)), "/help").orElseThrow();
        assertThat(read.reply().markdown()).contains("/alerts").contains("/ask").doesNotContain("/ack")
                .doesNotContain("/brief run");
        ChatOutcome agent = send(session(connector, 30, chat("20", Scope.AGENT, false)), "/help").orElseThrow();
        assertThat(agent.reply().markdown()).contains("/ack").contains("/snooze").contains("/brief run");
    }

    @Test
    @DisplayName("an unknown command gets a short help")
    void unknownCommand() {
        ChatOutcome outcome = send(session(connector, 30, chat("10", Scope.READ, false)), "/frobnicate now").orElseThrow();
        assertThat(outcome.result()).isEqualTo(ChatOutcome.INVALID);
        assertThat(outcome.reply().markdown()).contains("Unknown command").contains("/help").contains("/alerts");
        assertThat(connector.replies()).hasSize(1);
    }

    @Test
    @DisplayName("parsing is strict: limits, usage lines, the 2,000-character cap, plain text ignored")
    void argumentParsing() {
        assertThat(CommandRouter.parse("/Ack   5 ")).contains(new CommandRouter.Parsed("ack", List.of("5"), "5"));
        assertThat(CommandRouter.parse("/ask what  happened?").orElseThrow().rest()).isEqualTo("what  happened?");
        assertThat(CommandRouter.parse("hello")).isEmpty();
        assertThat(CommandRouter.parse("/")).isEmpty();
        assertThat(CommandRouter.parse("/<script>")).isEmpty();

        ChatSession agent = session(connector, 30, chat("20", Scope.AGENT, false));
        assertThat(send(agent, "/alerts 11").orElseThrow().result()).isEqualTo(ChatOutcome.INVALID);
        assertThat(send(agent, "/alerts 3 4 5").orElseThrow().result()).isEqualTo(ChatOutcome.INVALID);
        assertThat(send(agent, "/alerts loud").orElseThrow().result()).isEqualTo(ChatOutcome.INVALID);
        assertThat(send(agent, "/alert abc").orElseThrow().reply().markdown()).startsWith("Usage:");
        assertThat(send(agent, "/snooze 5").orElseThrow().result()).isEqualTo(ChatOutcome.INVALID);
        assertThat(send(agent, "/snooze 5 soon").orElseThrow().result()).isEqualTo(ChatOutcome.INVALID);
        assertThat(send(agent, "/snooze 5 8d").orElseThrow().reply().markdown()).contains("7 days");
        assertThat(send(agent, "/ask").orElseThrow().result()).isEqualTo(ChatOutcome.INVALID);
        assertThat(send(agent, "/ask " + "x".repeat(2_100)).orElseThrow().reply().markdown()).contains("too long");
        int before = connector.replies().size();
        assertThat(send(agent, "just chatting")).isEmpty();
        assertThat(connector.replies()).hasSize(before);

        when(alerts.list(any(), isNull(), anyInt())).thenReturn(List.of(alert(9, "quakes")));
        ChatOutcome listed = send(agent, "/alerts 3 critical").orElseThrow();
        ArgumentCaptor<AlertQuery> query = ArgumentCaptor.forClass(AlertQuery.class);
        verify(alerts).list(query.capture(), isNull(), eq(3));
        assertThat(query.getValue().minSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(query.getValue().states()).containsExactly("firing");
        assertThat(query.getValue().acknowledged()).isFalse();
        assertThat(listed.reply().markdown()).contains("`9`").contains("Quakes").contains("5m ago");
        assertThat(listed.reply().buttons()).hasSize(1);
        assertThat(listed.reply().buttons().getFirst()).extracting(ChatButton::label).containsExactly("Ack 9", "Resolve 9");
    }

    @Test
    @DisplayName("read chats get no Ack/Resolve buttons on /alerts")
    void readChatsGetNoButtons() {
        when(alerts.list(any(), isNull(), anyInt())).thenReturn(List.of(alert(9, "quakes")));
        ChatOutcome listed = send(session(connector, 30, chat("10", Scope.READ, false)), "/alerts").orElseThrow();
        assertThat(listed.reply().buttons()).isEmpty();
        verify(pending, never()).button(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("private alerts are hidden from chats without privateAccess")
    void privateAlertsHidden() {
        when(alerts.get(7)).thenReturn(alert(7, SECRET));
        ChatOutcome hidden = send(session(connector, 30, chat("10", Scope.AGENT, false)), "/alert 7").orElseThrow();
        assertThat(hidden.result()).isEqualTo(ChatOutcome.NOT_FOUND);
        assertThat(hidden.reply().markdown()).isEqualTo("No alert 7.");
        assertThat(send(session(connector, 30, chat("10", Scope.AGENT, false)), "/ack 7").orElseThrow().result())
                .isEqualTo(ChatOutcome.NOT_FOUND);
        verify(alerts, never()).acknowledge(anyLong(), any(), anyString());

        ChatOutcome shown = send(session(connector, 30, chat("11", Scope.AGENT, true)), "/alert 7").orElseThrow();
        assertThat(shown.result()).isEqualTo(ChatOutcome.OK);
        assertThat(shown.reply().markdown()).contains("Alert 7").contains("Quakes").doesNotContain("payload");

        when(alerts.list(any(), isNull(), anyInt())).thenReturn(List.of());
        send(session(connector, 30, chat("10", Scope.READ, false)), "/alerts");
        ArgumentCaptor<AlertQuery> query = ArgumentCaptor.forClass(AlertQuery.class);
        verify(alerts).list(query.capture(), isNull(), anyInt());
        assertThat(query.getValue().excludedSources()).contains(SECRET);
    }

    @Test
    @DisplayName("an unknown alert id is not found")
    void unknownAlert() {
        when(alerts.get(99)).thenThrow(NotFoundException.of("Alert", 99));
        assertThat(send(session(connector, 30, chat("10", Scope.READ, false)), "/alert 99").orElseThrow().result())
                .isEqualTo(ChatOutcome.NOT_FOUND);
    }

    @Test
    @DisplayName("unknown chats and users are ignored silently and counted")
    void unknownChatsIgnored() {
        ChatSession session = session(connector, 30, new AllowedChat("-100", null, Scope.AGENT, List.of("7"), false));
        assertThat(router.onMessage(session, new ChatRef(CHANNEL, "999", "42", "Eve", "1"), "/alerts")).isEmpty();
        assertThat(router.onMessage(session, new ChatRef(CHANNEL, "-100", "42", "Eve", "1"), "/ack 5")).isEmpty();
        assertThat(connector.replies()).isEmpty();
        verify(audit, never()).record(any());
        assertThat(meters.counter(CommandRouter.REJECTED_METRIC, "channel", CHANNEL.toString()).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("the per-chat command limit answers rate limited with a retry time")
    void commandRateLimit() {
        ChatSession session = session(connector, 2, chat("10", Scope.READ, false));
        assertThat(send(session, "/help").orElseThrow().result()).isEqualTo(ChatOutcome.OK);
        assertThat(send(session, "/help").orElseThrow().result()).isEqualTo(ChatOutcome.OK);
        ChatOutcome limited = send(session, "/help").orElseThrow();
        assertThat(limited.result()).isEqualTo(ChatOutcome.RATE_LIMITED);
        assertThat(limited.reply().markdown()).matches("rate limited, retry in \\d+ s");
    }

    @Test
    @DisplayName("every command is audited with the principal, the command, argument names and the result")
    void audited() {
        when(alerts.get(5)).thenReturn(alert(5, "quakes"));
        when(alerts.acknowledge(eq(5L), any(), anyString())).thenReturn(alert(5, "quakes"));
        send(session(connector, 30, chat("20", Scope.AGENT, false)), "/ack 5");
        ArgumentCaptor<AuditRecord> record = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).record(record.capture());
        assertThat(record.getValue().actorKind()).isEqualTo(CommandRouter.ACTOR_KIND);
        assertThat(record.getValue().tokenName()).isEqualTo("chat:" + CHANNEL + ":20");
        assertThat(record.getValue().scope()).isEqualTo("agent");
        assertThat(record.getValue().detail()).containsEntry("command", "ack").containsEntry("result", "ok")
                .containsEntry("arguments", List.of("id")).containsKey("durationMs");
        assertThat(record.getValue().detail()).doesNotContainKey("id").doesNotContainValue("5");
    }

    @Test
    @DisplayName("a chat principal is never admin and has a stable id")
    void principals() {
        AllowedChat chat = new AllowedChat("-100", "g", Scope.AGENT, List.of("7"), false);
        ChatPrincipal principal = ChatPrincipal.of(CHANNEL, chat, "7");
        assertThat(principal.name()).isEqualTo("chat:" + CHANNEL + ":-100:7");
        assertThat(principal.id()).isEqualTo(ChatPrincipal.of(CHANNEL, chat, "7").id());
        assertThat(principal.token().scope()).isEqualTo(Scope.AGENT);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new ChatPrincipal("chat:x", Scope.ADMIN, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
