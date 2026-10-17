package ai.foresignal.forecast.eval;

import org.springframework.context.annotation.Import;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.ReferenceImportEntity;
import ai.foresignal.baserate.reference.ReferenceImportRepository;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Retrospective evaluation (prompt 122 §7, LED-2): the base-rate baseline evaluated as of each question's opening
 * reads only earlier history (a later record changes nothing), an LLM forecaster skips questions opened before its
 * provider's knowledge cutoff and counts them under {@code LEAKAGE_RISK}, and without a cutoff the request is refused
 * with 409 {@code KNOWLEDGE_CUTOFF_REQUIRED}.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class RetrospectiveEvaluationIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");
    static final Instant OPENED = Instant.parse("2025-01-06T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired MutableClock clock;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry registry;
    @Autowired EvaluationService evaluations;
    @Autowired ReferenceImportRepository imports;
    @Autowired BaseRateService baseRates;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    String agent;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        registry.registerBuiltIns();
        baseRates.clearCache();
        agent = TestTokens.bearer(TestTokens.create(tokens, "eval-agent", Scope.AGENT));
        ReferenceImportEntity ucdp = imported("k-ucdp", List.of("2023", "2024"));
        for (LocalDate w = ReferenceFamilies.yearChunk(2023)[0]; w.isBefore(ReferenceFamilies.yearChunk(2024)[1]);
                w = w.plusWeeks(2)) {
            history(w, 2, ucdp.getId());
        }
    }

    ReferenceImportEntity imported(String key, List<String> done) {
        ReferenceImportEntity e = new ReferenceImportEntity(Ulids.next(NOW), "ucdp-ged", key, "test", NOW);
        e.setStatus("done");
        e.setLicense("CC-BY-4.0");
        e.setEgress("OPEN");
        e.setProgress(Map.of("done", done));
        return imports.save(e);
    }

    void history(LocalDate week, long count, String importId) {
        jdbc.update("INSERT INTO reference_history (family, region_code, week_start, source, count, fatalities,"
                + " licence, egress, import_id) VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?)", ReferenceFamilies.BATTLES, "YE",
                Date.valueOf(week), ReferenceFamilies.UCDP, count, "CC-BY-4.0", "OPEN", importId);
    }

    /** A question opened in the past and resolved yes. */
    ForecastQuestionEntity resolvedQuestion(Instant opensAt, String category) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "event-exists");
        r.put("selector", Map.of("referenceFamily", ReferenceFamilies.BATTLES));
        r.put("area", Map.of("regionCodes", List.of("YE")));
        r.put("window", Map.of("start", opensAt.toString(), "end", opensAt.plus(Duration.ofDays(28)).toString()));
        ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft("Battles in Yemen?", Ledger.BINARY,
                Map.of(), "Yes if battles are recorded.", ResolverSpec.validate(r, Ledger.BINARY), opensAt,
                opensAt.plus(Duration.ofDays(1)), opensAt.plus(Duration.ofDays(28)), category, List.of(), Map.of(),
                null, false);
        ForecastQuestionEntity q = ledger.openQuestion(draft, "agent", "test").question();
        Map<String, Object> resolution = new LinkedHashMap<>();
        resolution.put("outcome", "yes");
        resolution.put("value", null);
        resolution.put("resolvedAt", opensAt.plus(Duration.ofDays(28)).toString());
        resolution.put("resolvedBy", "resolver:event-exists/1.0.0");
        resolution.put("early", false);
        resolution.put("evidence", List.of("occurrence:x"));
        return ledger.finishResolution(q, resolution);
    }

    Map<String, Object> definition(String forecasterId, String cutoff) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("forecasterId", forecasterId);
        d.put("from", "2025-01-01T00:00:00Z");
        d.put("to", "2026-01-01T00:00:00Z");
        d.put("categories", List.of());
        d.put("maxQuestions", null);
        d.put("knowledgeCutoff", cutoff);
        return d;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> entry(Map<String, Object> result) {
        Map<String, Object> card = (Map<String, Object>) result.get("scorecard");
        assertThat(card.get("retrospective")).isEqualTo(true);
        return ((List<Map<String, Object>>) card.get("entries")).get(0);
    }

    @Test
    @DisplayName("as-of enforcement: the base rate is computed from weeks before opens_at; later history changes nothing")
    void asOfEnforcement() {
        resolvedQuestion(OPENED, "conflict");
        Map<String, Object> first = evaluations.run(definition(ForecasterRegistry.BASE_RATE, null), JobMonitor.NONE);
        assertThat(first.get("questionsConsidered")).isEqualTo(1);
        assertThat(first.get("questionsEvaluated")).isEqualTo(1);
        Object brier = entry(first).get("brier");
        assertThat(brier).isNotNull();
        // Covered, much busier history after the question opened.
        ReferenceImportEntity later = imported("k-later", List.of("2025"));
        for (LocalDate w = ReferenceFamilies.yearChunk(2025)[0]; w.isBefore(ReferenceFamilies.yearChunk(2025)[1]);
                w = w.plusWeeks(1)) {
            history(w, 50, later.getId());
        }
        baseRates.clearCache();
        Map<String, Object> second = evaluations.run(definition(ForecasterRegistry.BASE_RATE, null), JobMonitor.NONE);
        assertThat(entry(second).get("brier")).isEqualTo(brier);
        // Nothing was written to the ledger: only the question itself.
        assertThat(jdbc.queryForObject("SELECT count(*) FROM forecast", Long.class)).isZero();
    }

    @Test
    @DisplayName("an LLM forecaster skips questions opened before its cutoff and counts them under LEAKAGE_RISK")
    void leakage() {
        registry.ensure("llm:test", "llm", "Test LLM", "1.0.0", Map.of("provider", "test-provider"));
        resolvedQuestion(OPENED, "conflict");
        resolvedQuestion(Instant.parse("2025-08-04T00:00:00Z"), "conflict");
        Map<String, Object> result = evaluations.run(definition("llm:test", "2025-06-01"), JobMonitor.NONE);
        @SuppressWarnings("unchecked")
        Map<String, Object> skipped = (Map<String, Object>) result.get("skipped");
        assertThat(skipped.get("leakageRisk")).isEqualTo(1);
        // The remaining question has no LLM runner yet (prompt 128 registers it).
        assertThat(skipped.get("other")).isEqualTo(1);
        assertThat(result.get("questionsEvaluated")).isEqualTo(0);
        assertThat(String.valueOf(result.get("caveats"))).contains("LEAKAGE_RISK");
    }

    @Test
    @DisplayName("without a provider cutoff an LLM evaluation is refused with 409; with one it runs as a 103 job")
    void cutoffRequired() throws Exception {
        registry.ensure("llm:test", "llm", "Test LLM", "1.0.0", Map.of("provider", "test-provider"));
        String body = "{\"forecasterId\":\"llm:test\",\"from\":\"2025-01-01T00:00:00Z\",\"to\":\"2026-01-01T00:00:00Z\"}";
        MockHttpServletResponse refused = post(agent, "/v1/forecasts/evaluations", body);
        assertThat(refused.getStatus()).isEqualTo(409);
        assertThat(json(refused).get("code").asString()).isEqualTo("KNOWLEDGE_CUTOFF_REQUIRED");
        jdbc.update("INSERT INTO llm_provider (id, type, base_url, options, requests_per_minute, tokens_per_day,"
                + " enabled, knowledge_cutoff, consecutive_failures, created_at, updated_at) VALUES (?, 'ollama',"
                + " 'http://ollama:11434', CAST('{}' AS jsonb), 10, 1000, true, ?, 0, ?, ?)", "test-provider",
                Date.valueOf(LocalDate.of(2025, 6, 1)), Timestamp.from(NOW), Timestamp.from(NOW));
        resolvedQuestion(OPENED, "conflict");
        MockHttpServletResponse accepted = post(agent, "/v1/forecasts/evaluations", body);
        assertThat(accepted.getStatus()).isEqualTo(202);
        JsonNode evaluation = json(accepted);
        assertThat(evaluation.get("forecasterId").asString()).isEqualTo("llm:test");
        String id = evaluation.get("id").asString();
        JsonNode done = null;
        for (int i = 0; i < 100; i++) {
            done = json(mvc.perform(get("/v1/forecasts/evaluations/" + id).header(HttpHeaders.AUTHORIZATION, agent))
                    .andReturn().getResponse());
            if ("done".equals(done.get("status").asString()) || "failed".equals(done.get("status").asString())) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(done.get("status").asString()).isEqualTo("done");
        assertThat(done.get("skipped").get("leakageRisk").asInt()).isEqualTo(1);
        assertThat(mvc.perform(get("/v1/forecasts/evaluations/" + Ulids.next(NOW)).header(HttpHeaders.AUTHORIZATION,
                agent)).andReturn().getResponse().getStatus()).isEqualTo(404);
        // An unknown forecaster is 404.
        assertThat(post(agent, "/v1/forecasts/evaluations", body.replace("llm:test", "nobody")).getStatus())
                .isEqualTo(404);
    }

    MockHttpServletResponse post(String bearer, String path, String body) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
                .header(HttpHeaders.AUTHORIZATION, bearer).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse();
    }

    JsonNode json(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }
}
