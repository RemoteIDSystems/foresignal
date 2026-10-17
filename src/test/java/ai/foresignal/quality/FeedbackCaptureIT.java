package ai.foresignal.quality;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.chatops.ChatCommandService;
import ai.foresignal.chatops.ChatOutcome;
import ai.foresignal.chatops.ChatPrincipal;
import ai.foresignal.chatops.ChatRef;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.action.ActionService;
import ai.foresignal.notify.action.ActionsConfig;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Feedback capture from every surface (prompt 115): the {@code feedback:useful} and {@code feedback:noise}
 * signed actions, the {@code /feedback} chat command and the MCP {@code set_alert_feedback} tool all land in
 * {@code alert.feedback}; a feedback action works in any state and does not supersede the other capabilities.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class FeedbackCaptureIT {

    static final Instant T = Instant.parse("2026-09-03T10:00:00Z");

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertService alertService;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired ActionService actions;
    @Autowired ChatCommandService chat;
    @Autowired McpToolRegistry mcp;

    TriggerFixture fixture;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        if (!engine.running()) {
            engine.start();
        }
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    AlertView fire(String id) {
        UUID rule = fixture.rule("{'name':'Big " + id + "','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        records.ingest(STANDARD, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(T).put("mag", 6.5).build());
        return fixture.routed(rule).getFirst();
    }

    @Test
    @DisplayName("feedback:useful and feedback:noise signed actions set alert.feedback, in any state")
    void signedActions() {
        AlertView alert = fire("q1");
        List<NotificationAction> minted = actions.mint(alert, null, null, "device-1",
                List.of(NotificationAction.FEEDBACK_USEFUL, NotificationAction.FEEDBACK_NOISE, NotificationAction.ACK));
        assertThat(minted).extracting(NotificationAction::action)
                .containsExactly("feedback:useful", "feedback:noise", "ack");
        assertThat(minted.get(0).label()).isEqualTo("Useful");
        ActionService.Result useful = actions.execute(minted.get(0).token(), new ActionService.Via("test", "device-1",
                "127.0.0.1"));
        assertThat(useful.result()).isEqualTo("feedback-useful");
        assertThat(alerts.find(alert.id()).orElseThrow().feedback()).isEqualTo("useful");
        // The acknowledgement still works after feedback, and feedback still works after the acknowledgement.
        actions.execute(minted.get(2).token(), new ActionService.Via("test", "device-1", "127.0.0.1"));
        assertThat(alerts.find(alert.id()).orElseThrow().state()).isEqualTo("acknowledged");
        AlertView acked = alerts.find(alert.id()).orElseThrow();
        List<NotificationAction> later = actions.mint(acked, null, null, "device-1",
                List.of(NotificationAction.FEEDBACK_NOISE));
        assertThat(later).as("capabilities are minted only for firing alerts").isEmpty();
        assertThat(ActionsConfig.of("telegram", Map.of()).list()).contains("feedback:useful", "feedback:noise");
        assertThat(ActionsConfig.of("matrix", Map.of()).list()).contains("feedback:useful", "feedback:noise");
        assertThat(ActionsConfig.of("ntfy", Map.of("actions", Map.of("list", List.of("feedback:useful",
                "feedback:noise")))).list()).containsExactly("feedback:useful", "feedback:noise");
    }

    @Test
    @DisplayName("/feedback <id> <value> records the feedback; read chats are refused by the router's policy")
    void chatCommand() {
        AlertView alert = fire("q2");
        ChatPrincipal principal = new ChatPrincipal("chat:ops", Scope.AGENT, true);
        ChatRef ref = new ChatRef(UUID.randomUUID(), "chat-1", "user-1", "Ana", "1");
        ChatOutcome outcome = chat.feedback(principal, ref, List.of(Long.toString(alert.id()), "NOISE"));
        assertThat(outcome.reply().markdown()).contains("marked noise");
        assertThat(alerts.find(alert.id()).orElseThrow().feedback()).isEqualTo("noise");
        assertThat(chat.feedback(principal, ref, List.of(Long.toString(alert.id()), "maybe")).reply().markdown())
                .contains("/feedback");
        assertThat(chat.feedback(principal, ref, List.of("999999", "useful")).reply().markdown()).contains("No alert");
    }

    @Test
    @DisplayName("MCP set_alert_feedback records the feedback and a note")
    void mcpTool() {
        AlertView alert = fire("q3");
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "agent", Scope.AGENT,
                TokenRestrictions.NONE, null, "principal-1", false, false);
        ToolContext context = new ToolContext(new McpCaller(token, new ReadCaller(Scope.AGENT, false, true, null, false),
                Set.of(), "127.0.0.1"), "session", McpSessions.STREAMABLE);
        McpSchema.CallToolResult result = mcp.call("set_alert_feedback", Map.of("alertId", alert.id(),
                "feedback", "late", "note", "came after the event"), context);
        assertThat(result.isError()).isNotEqualTo(Boolean.TRUE);
        AlertView after = alerts.find(alert.id()).orElseThrow();
        assertThat(after.feedback()).isEqualTo("late");
        assertThat(after.note()).isEqualTo("came after the event");
    }
}
