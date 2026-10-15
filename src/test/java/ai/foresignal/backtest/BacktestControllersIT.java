package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.BacktestController;
import ai.foresignal.api.PrecursorTestController;
import ai.foresignal.api.RuleAnalysisController;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

/** The backtest, precursor and rule-analysis controllers (prompt 103). */
@WebIntegrationTest
@Import(TestIngestSources.class)
class BacktestControllersIT {

    @Autowired BacktestController backtests;
    @Autowired PrecursorTestController precursorTests;
    @Autowired RuleAnalysisController analysis;
    @Autowired TriggerRuleService rules;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("a backtest is submitted, listed and read back")
    void backtestsCrud() {
        UUID ruleId = rules.create(json.readTree("{\"name\":\"r\",\"sources\":[\"test-standard\"]}")).rule().getId();
        Map<String, Object> job = backtests.createBacktest(json.readTree("{\"type\":\"rule\",\"targetId\":\""
                + ruleId + "\",\"fromSeq\":0,\"toSeq\":10}"), new MockHttpServletRequest());
        assertThat(job).containsKey("id");
        assertThat(backtests.getBacktest(String.valueOf(job.get("id")))).containsEntry("type", "rule");
        assertThat(backtests.listBacktests(null, null, null, "10")).containsKey("items");
    }

    @Test
    @DisplayName("one queued or running job per token is 409")
    void onePerToken() {
        String token = UUID.randomUUID().toString();
        backtests.createBacktest(json.readTree("{\"type\":\"quality\"}"), request(token));
        assertThatThrownBy(() -> backtests.createBacktest(json.readTree("{\"type\":\"quality\"}"), request(token)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("a precursor test is created and its quality/suggestions endpoints answer")
    void precursorAndAnalysis() {
        var test = precursorTests.createPrecursorTest(json.readTree(
                "{\"name\":\"p\",\"antecedent\":{\"intelEventType\":\"maritime-disruption\"},"
                        + "\"consequent\":{\"intelEventType\":\"seismic\"},\"minLag\":\"PT0S\",\"maxLag\":\"PT6H\","
                        + "\"spatial\":{\"withinKm\":100}}"));
        assertThat(test).containsEntry("name", "p");
        UUID ruleId = rules.create(json.readTree("{\"name\":\"r2\",\"sources\":[\"test-standard\"]}")).rule().getId();
        assertThat(analysis.getTriggerQuality(ruleId.toString(), null)).containsKey("alerts");
        assertThat(analysis.getTriggerSuggestions(ruleId.toString(), null, null, new MockHttpServletRequest()))
                .containsKey("suggestions");
    }

    private static MockHttpServletRequest request(String tokenId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("foresignal.tokenId", tokenId);
        return request;
    }
}
