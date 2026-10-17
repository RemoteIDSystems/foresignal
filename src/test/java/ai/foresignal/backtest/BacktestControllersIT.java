package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.BacktestController;
import ai.foresignal.api.PrecursorTestController;
import ai.foresignal.api.RuleAnalysisController;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.RunBacktestTool;
import ai.foresignal.mcp.tools.TestPrecursorTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

/** The backtest, precursor and rule-analysis controllers and MCP tools (prompt 103). */
@WebIntegrationTest
@Import(TestIngestSources.class)
class BacktestControllersIT {

    @Autowired BacktestController backtests;
    @Autowired PrecursorTestController precursorTests;
    @Autowired RuleAnalysisController analysis;
    @Autowired BacktestJobService jobs;
    @Autowired TriggerRuleService rules;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired IntelligenceEventRepository events;
    @Autowired RunBacktestTool runBacktest;
    @Autowired TestPrecursorTool testPrecursor;
    @Autowired JsonMapper json;

    private UUID seededRule(String name) {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        UUID ruleId = rules.create(json.readTree("{\"name\":\"" + name + "\",\"sources\":[\"test-standard\"]}"))
                .rule().getId();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder(name, TestIngestSources.STANDARD_KIND).observedAt(now).point(-95, 32)
                        .put("name", name).build());
        return ruleId;
    }

    private static AuthenticatedToken token(Scope scope) {
        return new AuthenticatedToken(UUID.randomUUID(), "bot-" + scope.wireValue(), scope, TokenRestrictions.NONE,
                null, "principal-1", false);
    }

    private static MockHttpServletRequest request(AuthenticatedToken token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CurrentToken.ATTRIBUTE, new CurrentToken(token, false));
        return request;
    }

    private static ToolContext context(AuthenticatedToken token) {
        return new ToolContext(new McpCaller(token, new ReadCaller(token.scope(), false, true), Set.of(), "10.0.0.2"),
                "session", McpSessions.STREAMABLE);
    }

    @Test
    @DisplayName("a backtest is submitted, listed, read back and finishes with progress 1")
    void backtestsCrud() {
        UUID ruleId = seededRule("r");
        Map<String, Object> job = backtests.createBacktest(json.readTree("{\"type\":\"rule\",\"targetId\":\""
                + ruleId + "\",\"fromSeq\":0}"), new MockHttpServletRequest());
        assertThat(job).containsKeys("id", "definition", "from", "to");
        String id = String.valueOf(job.get("id"));
        assertThat(backtests.getBacktest(id)).containsEntry("type", "rule");
        assertThat(backtests.getJob(id)).containsEntry("id", id);
        assertThat(backtests.listBacktests(null, "rule", null, "10")).containsKey("items");
        BacktestJobEntity done = RuleBacktestIT.await(jobs, id);
        assertThat(done.getStatus()).isEqualTo("done");
        assertThat(done.getProgress()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("an unknown target is 404 and a rule job without a target is 400")
    void targets() {
        assertThatThrownBy(() -> backtests.createBacktest(json.readTree("{\"type\":\"precursor\",\"targetId\":\"nope\"}"),
                new MockHttpServletRequest())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> backtests.createBacktest(json.readTree("{\"type\":\"rule\"}"),
                new MockHttpServletRequest())).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("one queued or running job per token is 409, across REST and MCP")
    void onePerToken() {
        AuthenticatedToken token = token(Scope.AGENT);
        UUID ruleId = seededRule("busy");
        Map<String, Object> first = backtests.createBacktest(json.readTree("{\"type\":\"quality\"}"), request(token));
        // Hold the job in "running" so the conflict does not depend on the worker's timing: let the worker finish
        // first (await claims and runs it if the pool has not), so nothing can move it out of "running" afterwards.
        String firstId = String.valueOf(first.get("id"));
        RuleBacktestIT.await(jobs, firstId);
        BacktestJobEntity held = jobsRepository.findById(firstId).orElseThrow();
        held.setStatus("running");
        jobsRepository.save(held);
        assertThatThrownBy(() -> backtests.createBacktest(json.readTree("{\"type\":\"quality\"}"), request(token)))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> runBacktest.call(context(token), json.readTree("{\"type\":\"rule\",\"targetId\":\""
                + ruleId + "\"}"))).isInstanceOf(ConflictException.class);
        String precursorId = String.valueOf(precursorTests.createPrecursorTest(json.readTree(
                "{\"name\":\"p\",\"antecedent\":{\"intelEventType\":\"seismic\"},\"consequent\":{\"intelEventType\":"
                        + "\"seismic\"},\"maxLag\":\"PT6H\"}"), request(token)).get("id"));
        assertThatThrownBy(() -> precursorTests.testPrecursorTest(precursorId, null, request(token)))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> testPrecursor.call(context(token), json.readTree("{\"id\":\"" + precursorId + "\"}")))
                .isInstanceOf(ConflictException.class);
        jobs.cancel(firstId);
    }

    @Autowired BacktestJobRepository jobsRepository;

    @Test
    @DisplayName("a precursor test is created, validated and run as a job over the requested range")
    @SuppressWarnings("unchecked")
    void precursorJob() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant start = clock.instant().minus(Duration.ofDays(5));
        for (int i = 0; i < 12; i++) {
            Instant t = start.plus(Duration.ofHours(10L * i + (i % 3)));
            RecipeBacktestIT.event(events, "maritime-disruption", "r", t);
            RecipeBacktestIT.event(events, "seismic", "r", t.plus(Duration.ofMinutes(30)));
        }
        var test = precursorTests.createPrecursorTest(json.readTree(
                "{\"name\":\"p\",\"antecedent\":{\"intelEventType\":\"maritime-disruption\"},"
                        + "\"consequent\":{\"intelEventType\":\"seismic\"},\"minLag\":\"PT0S\",\"maxLag\":\"PT6H\"}"),
                new MockHttpServletRequest());
        assertThat(test).containsEntry("name", "p");
        String id = String.valueOf(test.get("id"));
        assertThatThrownBy(() -> precursorTests.createPrecursorTest(json.readTree("{\"name\":\"bad\",\"maxLag\":\"6h\"}"),
                new MockHttpServletRequest())).isInstanceOf(ValidationException.class);

        Map<String, Object> job = precursorTests.testPrecursorTest(id, json.readTree("{\"from\":\""
                + start.minus(Duration.ofHours(1)) + "\",\"to\":\"" + clock.instant() + "\"}"),
                new MockHttpServletRequest());
        assertThat(job).containsEntry("rangeFrom", start.minus(Duration.ofHours(1)));
        BacktestJobEntity done = RuleBacktestIT.await(jobs, String.valueOf(job.get("id")));
        assertThat(done.getStatus()).isEqualTo("done");
        assertThat(done.getResult()).containsEntry("nAntecedent", 12).containsEntry("hits", 12)
                .containsKeys("reverse", "multipleTesting", "lift", "pValue", "verdict");
        assertThat((Map<String, Object>) done.getResult().get("multipleTesting")).containsEntry("testsLast30d", 1);
        assertThat(done.getCaveats()).anyMatch(c -> BacktestCaveat.NOT_GROUND_TRUTH.equals(c.get("code")));
    }

    @Test
    @DisplayName("quality and suggestions answer, and simulate submits a job for agents")
    void analysisEndpoints() {
        UUID ruleId = seededRule("r2");
        assertThat(analysis.getTriggerQuality(ruleId.toString(), null)).containsKeys("alerts", "duplicateWindow");
        assertThat(analysis.getTriggerSuggestions(ruleId.toString(), null, null, null, null,
                new MockHttpServletRequest()))
                .containsKey("suggestions").doesNotContainKey("jobId");
        assertThat(analysis.getTriggerSuggestions(ruleId.toString(), null, "true", null, null,
                request(token(Scope.AGENT))))
                .containsKey("jobId");
    }

    @Test
    @DisplayName("an oversized result is truncated and flagged")
    void truncation() {
        Map<String, Object> big = new HashMap<>();
        big.put("wouldFire", 1);
        big.put("samples", List.of("x".repeat(1_100_000)));
        Map<String, Object> truncated = jobs.truncate(big);
        assertThat(truncated).containsEntry("truncated", true).doesNotContainKey("samples");
        assertThat(jobs.truncate(Map.of("wouldFire", 1))).doesNotContainKey("truncated");
    }

    @Test
    @DisplayName("the MCP tools return the queued job")
    void mcpShapes() {
        UUID ruleId = seededRule("mcp");
        ToolResult result = runBacktest.call(context(token(Scope.AGENT)), json.readTree("{\"type\":\"rule\","
                + "\"targetId\":\"" + ruleId + "\",\"fromSeq\":0}"));
        assertThat(result.structured().path("type").asString()).isEqualTo("rule");
        assertThat(result.structured().path("createdBy").asString()).isNotBlank();
        assertThat(runBacktest.inputSchema().toString()).contains("fromSeq").contains("from");
    }
}
