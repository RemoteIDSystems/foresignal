package ai.foresignal.ask;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.llm.client.ToolResult;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

/**
 * Proposed actions (prompt 028; invariant EGR-3): a model only proposes; an {@code agent} token
 * confirms (or {@code allowActions} with agent scope executes at once); a {@code read} token cannot
 * confirm; another token cannot see the action; rejection discards; executions are audited.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AskTestSources.class)
class AskActionsIT extends AskItSupport {

    static final String WATCH_RULE = "{\"rule\":{\"name\":\"Big quakes\",\"sources\":[\"" + AskTestSources.QUAKES
            + "\"],\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]},\"reason\":\"You asked to be told.\"}";

    @Autowired MockMvc mvc;
    @Autowired AuditService audit;
    @Autowired AlertService alerts;

    JsonNode perform(String bearer, MockHttpServletRequestBuilder request, int expectedStatus) throws Exception {
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        MvcResult result = mvc.perform(request).andReturn();
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(result.getResponse().getStatus()).as(content).isEqualTo(expectedStatus);
        return content.isEmpty() ? null : JSON.readTree(content);
    }

    JsonNode ask(String bearer, boolean allowActions) throws Exception {
        return perform(bearer, post("/v1/ask").contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"Tell me about big quakes\",\"allowActions\":" + allowActions + "}"), 200);
    }

    void proposeWatch() {
        chat().then(toolUse(call("p1", "propose_watch", WATCH_RULE))).then(text("I proposed a watch."));
    }

    long watches() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM trigger_rule", Long.class);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("propose_watch only creates a pending action; the model is told pending_confirmation")
    void proposeCreatesPending() throws Exception {
        proposeWatch();

        JsonNode response = ask(agent, false);

        JsonNode action = response.get("proposedActions").get(0);
        assertThat(action.get("status").asString()).isEqualTo("pending");
        assertThat(action.get("type").asString()).isEqualTo("create_watch");
        assertThat(action.get("description").asString()).contains("Big quakes").contains("You asked to be told.");
        assertThat(action.at("/payload/rule/name").asString()).isEqualTo("Big quakes");
        assertThat(watches()).isZero();
        ToolResult result = toolResults(chat().requests().get(1)).getFirst();
        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("pending_confirmation").contains(action.get("id").asString());
    }

    @Test
    @DisplayName("confirming with an agent token creates the watch through the REST service and audits it")
    void confirmCreatesTrigger() throws Exception {
        proposeWatch();
        String actionId = ask(agent, false).at("/proposedActions/0/id").asString();
        audit.flush();
        jdbc.update("DELETE FROM audit_event");

        JsonNode confirmed = perform(agent, post("/v1/ask/actions/" + actionId + "/confirm"), 200);

        assertThat(confirmed.get("status").asString()).isEqualTo("executed");
        assertThat(confirmed.at("/result/name").asString()).isEqualTo("Big quakes");
        assertThat(confirmed.get("executedAt").isNull()).isFalse();
        assertThat(watches()).isEqualTo(1);
        audit.flush();
        Long audited = jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE target_type = 'ask_action'"
                + " AND target_id = ?", Long.class, actionId);
        assertThat(audited).isEqualTo(1);

        perform(agent, post("/v1/ask/actions/" + actionId + "/confirm"), 409);
    }

    @Test
    @DisplayName("a read token cannot confirm (403), even its own proposal")
    void readTokenCannotConfirm() throws Exception {
        proposeWatch();
        String actionId = ask(reader, false).at("/proposedActions/0/id").asString();

        perform(reader, post("/v1/ask/actions/" + actionId + "/confirm"), 403);
        perform(reader, post("/v1/ask/actions/" + actionId + "/reject"), 403);

        assertThat(watches()).isZero();
    }

    @Test
    @DisplayName("allowActions with an agent token executes at once and still records the action")
    void allowActionsAsAgent() throws Exception {
        proposeWatch();

        JsonNode response = ask(agent, true);

        JsonNode action = response.get("proposedActions").get(0);
        assertThat(action.get("status").asString()).isEqualTo("executed");
        assertThat(watches()).isEqualTo(1);
        JsonNode thread = perform(agent, get("/v1/ask/threads/" + response.get("threadId").asString()), 200);
        assertThat(thread.at("/actions/0/status").asString()).isEqualTo("executed");
    }

    @Test
    @DisplayName("allowActions with a read token leaves the action pending")
    void allowActionsAsReader() throws Exception {
        proposeWatch();

        JsonNode response = ask(reader, true);

        assertThat(response.at("/proposedActions/0/status").asString()).isEqualTo("pending");
        assertThat(watches()).isZero();
    }

    @Test
    @DisplayName("reject discards; a rejected action cannot be confirmed (409 ACTION_NOT_PENDING)")
    void reject() throws Exception {
        proposeWatch();
        String actionId = ask(agent, false).at("/proposedActions/0/id").asString();

        JsonNode rejected = perform(agent, post("/v1/ask/actions/" + actionId + "/reject"), 200);
        assertThat(rejected.get("status").asString()).isEqualTo("rejected");

        JsonNode problem = perform(agent, post("/v1/ask/actions/" + actionId + "/confirm"), 409);
        assertThat(problem.get("code").asString()).isEqualTo("ACTION_NOT_PENDING");
        assertThat(watches()).isZero();
    }

    @Test
    @DisplayName("another non-admin token gets 404 for the action and the thread; deleting a thread needs agent")
    void otherTokenNotFound() throws Exception {
        proposeWatch();
        JsonNode response = ask(agent, false);
        String actionId = response.at("/proposedActions/0/id").asString();

        perform(otherAgent, post("/v1/ask/actions/" + actionId + "/confirm"), 404);
        perform(otherAgent, post("/v1/ask/actions/" + actionId + "/reject"), 404);
        perform(agent, post("/v1/ask/actions/" + UUID.randomUUID() + "/confirm"), 404);
        perform(agent, post("/v1/ask/actions/not-a-uuid/confirm"), 404);
        assertThat(watches()).isZero();

        String threadId = response.get("threadId").asString();
        perform(otherAgent, delete("/v1/ask/threads/" + threadId), 404);
        perform(reader, delete("/v1/ask/threads/" + threadId), 403);
        perform(agent, delete("/v1/ask/threads/" + threadId), 204);
        perform(agent, get("/v1/ask/threads/" + threadId), 404);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ask_action", Long.class)).isZero();
    }

    @Test
    @DisplayName("propose_alert_status resolves the alert only when confirmed")
    void alertStatusResolvesOnConfirm() throws Exception {
        UUID rule = watch("M6", 6);
        quake("big", "Tokyo Bay", 6.5, 139.8, 35.6);
        AlertView alert = alertOf(rule);
        chat().then(toolUse(call("p1", "propose_alert_status",
                        "{\"alertId\":" + alert.id() + ",\"action\":\"resolve\",\"feedback\":\"useful\"}")))
                .then(text("I proposed resolving it [alert:" + alert.id() + "]."));

        String actionId = ask(agent, false).at("/proposedActions/0/id").asString();
        assertThat(alerts.get(alert.id()).state()).isNotEqualTo("resolved");

        JsonNode confirmed = perform(agent, post("/v1/ask/actions/" + actionId + "/confirm"), 200);

        assertThat(confirmed.get("status").asString()).isEqualTo("executed");
        AlertView after = alerts.get(alert.id());
        assertThat(after.state()).isEqualTo("resolved");
        assertThat(after.resolvedBy()).isEqualTo("token:ask-agent");
    }

    @Test
    @DisplayName("an invalid proposal is a tool error and stores no action")
    void invalidProposal() throws Exception {
        chat().then(toolUse(call("p1", "propose_watch", "{\"rule\":{\"name\":\"\",\"conditions\":[]}}")))
                .then(toolUse(call("p2", "propose_ack", "{\"alertId\":987654}")))
                .then(text("Could not propose."));

        JsonNode response = ask(agent, false);

        assertThat(response.get("proposedActions")).isEmpty();
        assertThat(toolResults(chat().requests().get(1)).getFirst().isError()).isTrue();
        assertThat(toolResults(chat().requests().get(2)).getLast().isError()).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ask_action", Long.class)).isZero();
    }
}
