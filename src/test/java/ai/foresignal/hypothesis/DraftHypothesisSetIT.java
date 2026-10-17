package ai.foresignal.hypothesis;

import static ai.foresignal.hypothesis.HypothesisFixtures.QUICK_LR;
import static ai.foresignal.hypothesis.HypothesisFixtures.intelIndicator;
import static ai.foresignal.hypothesis.HypothesisFixtures.manualIndicator;
import static ai.foresignal.hypothesis.HypothesisFixtures.outcomeSet;
import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ToolResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * LLM drafting of hypothesis sets (prompt 125 §6) with a scripted fake provider: {@code get_base_rate} and
 * {@code find_analogs} must come before any proposal (an early proposal is refused), invalid rule specs come back as
 * warnings, the draft is returned for confirmation, and nothing is persisted.
 */
@IntegrationTest
@Import(AskTestSources.class)
class DraftHypothesisSetIT extends AskItSupport {

    @Autowired HypothesisDrafter drafter;
    @Autowired MockMvc mvc;

    static final String BASE_RATE = "{\"family\":\"conflict:battles\",\"regionCodes\":[\"YE\"]}";
    static final String ANALOGS = "{\"regionCode\":\"YE\"}";

    static String proposal(String indicators) {
        return "{\"set\":" + outcomeSet("daily", null, indicators, null, "") + "}";
    }

    static final String VALID_INDICATORS = intelIndicator("Maritime disruption", "maritime-disruption",
            "ais-behaviour", null, QUICK_LR) + "," + intelIndicator("Aviation anomaly", "aviation-anomaly",
            "aviation-ops", null, QUICK_LR) + "," + manualIndicator("Military air activity", "aviation-military", "M",
            QUICK_LR);

    long sets() {
        return jdbc.queryForObject("select count(*) from hypothesis_set", Long.class);
    }

    @SuppressWarnings("unchecked")
    static List<String> codes(Map<String, Object> draft) {
        return ((List<Map<String, Object>>) draft.get("warnings")).stream().map(w -> String.valueOf(w.get("code")))
                .toList();
    }

    @Test
    @DisplayName("base-rate and analog tools come first; the validated draft is returned and nothing is created")
    void toolsFirst() {
        chat().then(toolUse(call("c1", "get_base_rate", BASE_RATE)))
                .then(toolUse(call("c2", "find_analogs", ANALOGS)))
                .then(toolUse(call("c3", HypothesisDrafter.OUTPUT_TOOL, proposal(VALID_INDICATORS))))
                .then(text("Drafted."));
        Map<String, Object> draft = drafter.draft(new HypothesisDrafter.DraftRequest("Will the coalition respond?",
                null, List.of("YE")));
        assertThat((List<String>) draft.get("toolCalls")).startsWith("get_base_rate", "find_analogs")
                .contains(HypothesisDrafter.OUTPUT_TOOL);
        assertThat(codes(draft)).doesNotContain("TOOLS_ORDER", "NO_DRAFT", "INDICATOR_INVALID", "LINT_FAILED",
                "HYPOTHESIS_SET_INVALID", "INDICATOR_COUNT");
        assertThat(draft.get("valid")).isEqualTo(true);
        assertThat(draft.get("effort")).isEqualTo("medium");
        assertThat(draft.get("textOrigin")).isEqualTo("llm");
        Map<?, ?> set = (Map<?, ?>) draft.get("set");
        assertThat(set.get("question")).isEqualTo("Saudi or coalition kinetic response against Houthi targets within 7"
                + " days?");
        assertThat(set.get("regionCodes")).isEqualTo(List.of("YE"));
        assertThat(sets()).isZero();
        ChatRequest first = chat().requests().get(0);
        assertThat(first.tools()).extracting(t -> t.name()).contains("get_base_rate", "find_analogs",
                HypothesisDrafter.OUTPUT_TOOL);
    }

    @Test
    @DisplayName("a proposal before the base-rate and analog tools is refused, and the model is told to call them")
    void earlyProposalRefused() {
        chat().then(toolUse(call("c0", HypothesisDrafter.OUTPUT_TOOL, proposal(VALID_INDICATORS))))
                .then(toolUse(call("c1", "get_base_rate", BASE_RATE)))
                .then(toolUse(call("c2", "find_analogs", ANALOGS)))
                .then(toolUse(call("c3", HypothesisDrafter.OUTPUT_TOOL, proposal(VALID_INDICATORS))))
                .then(text("Drafted."));
        Map<String, Object> draft = drafter.draft(new HypothesisDrafter.DraftRequest("Will the coalition respond?",
                null, List.of()));
        List<ToolResult> refused = toolResults(chat().requests().get(1));
        assertThat(refused.getLast().content()).contains("refused");
        assertThat((List<String>) draft.get("toolCalls")).containsExactly("get_base_rate", "find_analogs",
                HypothesisDrafter.OUTPUT_TOOL);
        assertThat(codes(draft)).doesNotContain("TOOLS_ORDER", "NO_DRAFT");
        assertThat(sets()).isZero();
    }

    @Test
    @DisplayName("invalid rule specs and too few indicators come back as warnings; nothing is persisted")
    void invalidRuleSpecs() {
        String badRule = "{\"description\":\"Bad rule\",\"signal\":{\"type\":\"rule\",\"rule\":{\"name\":\"bad\","
                + "\"sources\":[\"no-such-source\"],\"conditions\":[{\"path\":\"mag\",\"op\":\"between\",\"value\":3}]}},"
                + "\"signalFamily\":\"seismic\",\"credibility\":\"M\",\"relevance\":\"M\",\"lr\":" + QUICK_LR + "}";
        chat().then(toolUse(call("c1", "get_base_rate", BASE_RATE)))
                .then(toolUse(call("c2", "find_analogs", ANALOGS)))
                .then(toolUse(call("c3", HypothesisDrafter.OUTPUT_TOOL, proposal(badRule))))
                .then(text("Drafted."));
        Map<String, Object> draft = drafter.draft(new HypothesisDrafter.DraftRequest("Watch for quakes.", null,
                List.of()));
        assertThat(codes(draft)).contains("INDICATOR_INVALID", "INDICATOR_COUNT");
        assertThat(draft.get("valid")).isEqualTo(false);
        assertThat(sets()).isZero();
        assertThat(jdbc.queryForObject("select count(*) from trigger_rule where name = 'bad'", Long.class)).isZero();
    }

    @Test
    @DisplayName("POST /v1/hypotheses/sets/draft is an agent operation that always needs a token")
    void scopes() throws Exception {
        String body = "{\"text\":\"Watch whether the Red Sea situation spreads to Hormuz\"}";
        assertThat(mvc.perform(post("/v1/hypotheses/sets/draft").header(HttpHeaders.AUTHORIZATION, reader)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getStatus())
                .isEqualTo(403);
        assertThat(mvc.perform(post("/v1/hypotheses/sets/draft").contentType(MediaType.APPLICATION_JSON)
                .content(body)).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(mvc.perform(post("/v1/hypotheses/sets/draft").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn().getResponse().getStatus())
                .isEqualTo(400);
    }
}
