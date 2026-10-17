package ai.foresignal.forecast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.benchmark.BenchmarkGate;
import ai.foresignal.benchmark.BenchmarkRunEntity;
import ai.foresignal.benchmark.BenchmarkRunRepository;
import ai.foresignal.benchmark.BenchmarkThresholds;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ensemble.EnsembleMath;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.forecast.score.ScoringService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Ledger integrity (checkpoint 124, focus area 6; invariant LED-1) beyond what {@code ForecastControllersIT} already
 * asserts (supersede not edit, a client {@code madeAt} refused, annulment closing the question):
 *
 * <ul>
 *   <li>a stored forecast cannot be changed through the API or through its entity, and a second submission supersedes
 *       it;
 *   <li>{@code asOfSeq} is server-set: a client value is refused and the stored one is the seq clock at submission;
 *   <li>an annulled question is never scored, also when its scores are recomputed;
 *   <li>time-averaged scoring carries the standing forecast forward to every later day;
 *   <li>the LLM cap applies only to {@code llm} forecasters and a Platt fit only to the forecaster (and category) it was
 *       fitted for.
 * </ul>
 */
@IntegrationTest
@Import(TestIngestSources.class)
class LedgerIntegrityIT {

    /** A UTC midnight. */
    static final Instant D0 = Instant.parse("2026-10-12T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;
    @Autowired ForecasterRegistry registry;
    @Autowired ForecastService ledger;
    @Autowired ForecastRepository forecasts;
    @Autowired ForecastScoreRepository scoreRows;
    @Autowired ScoringService scoring;
    @Autowired ForecastCalibrationRepository calibrations;
    @Autowired BenchmarkRunRepository benchmarkRuns;
    @Autowired BenchmarkThresholds thresholds;
    @Autowired BenchmarkGate gate;

    String read;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(D0.plus(Duration.ofHours(1)));
        registry.registerBuiltIns();
        gate.invalidate();
        read = TestTokens.bearer(TestTokens.create(tokens, "li-read", Scope.READ));
        admin = TestTokens.bearer(TestTokens.create(tokens, "li-admin", Scope.ADMIN));
    }

    /** A manual binary question open from {@link #D0} for four days. */
    ForecastQuestionEntity question(String category) {
        ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft("Will the ceasefire hold?",
                Ledger.BINARY, Map.of(), "Yes if no party announces its end.",
                ResolverSpec.validate(Map.of("type", "manual"), Ledger.BINARY), D0, D0.plus(Duration.ofDays(4)),
                D0.plus(Duration.ofDays(5)), category, List.of(), Map.of(), null, false);
        return ledger.openQuestion(draft, "agent", "test").question();
    }

    ForecasterEntity forecaster(String id, String type) {
        return registry.ensure(id, type, "Test " + id, "1.0.0", Map.of());
    }

    ForecastEntity forecast(ForecastQuestionEntity q, ForecasterEntity f, double p) {
        return ledger.record(ledger.find(q.getId()).orElseThrow(), f, ForecastValue.binary(p), "Test.", List.of(),
                List.of(), Map.of(), null, false);
    }

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearer)).andReturn().getResponse();
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a stored forecast has no update path: no API route edits it and its entity's columns never update")
    void forecastsImmutable() throws Exception {
        ForecastQuestionEntity q = question("conflict");
        ForecasterEntity op = forecaster("operator:li", "operator");
        ForecastEntity first = forecast(q, op, 0.6);
        String path = "/v1/forecasts/questions/" + q.getId() + "/forecasts";
        for (MockHttpServletRequestBuilder edit : List.of(put(path + "/" + first.getId()), patch(path + "/"
                + first.getId()), delete(path + "/" + first.getId()), put(path), patch(path), delete(path))) {
            int status = call(admin, edit.contentType(MediaType.APPLICATION_JSON).content("{\"probability\":0.1}"))
                    .getStatus();
            assertThat(status).as("no route edits a forecast").isIn(404, 405);
        }
        ForecastEntity loaded = forecasts.findById(first.getId()).orElseThrow();
        loaded.setProbability(0.1);
        loaded.setRationale("Edited.");
        forecasts.saveAndFlush(loaded);
        Map<String, Object> row = jdbc.queryForMap("SELECT probability, rationale, made_at FROM forecast WHERE id = ?",
                first.getId());
        assertThat(((Number) row.get("probability")).doubleValue()).isEqualTo(0.6);
        assertThat(row.get("rationale")).isEqualTo("Test.");

        clock.advance(Duration.ofMinutes(5));
        ForecastEntity second = forecast(q, op, 0.3);
        assertThat(second.getSupersedesId()).isEqualTo(first.getId());
        assertThat(jdbc.queryForObject("SELECT probability FROM forecast WHERE id = ?", Double.class, first.getId()))
                .isEqualTo(0.6);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM forecast WHERE question_id = ? AND forecaster_id = ?",
                Long.class, q.getId(), op.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("madeAt and asOfSeq are server-set: client values are refused; stored ones are the clock and seq clock")
    void serverSetTimes() throws Exception {
        ForecastQuestionEntity q = question("conflict");
        String path = "/v1/forecasts/questions/" + q.getId() + "/forecasts";
        for (String bad : List.of("{\"probability\":0.6,\"asOfSeq\":1}", "{\"probability\":0.6,\"asofSeq\":1}",
                "{\"probability\":0.6,\"madeAt\":\"2020-01-01T00:00:00Z\"}", "{\"probability\":0.6,\"supersedesId\":\""
                        + Ulids.next(D0) + "\"}")) {
            MockHttpServletResponse refused = call(read, post(path).contentType(MediaType.APPLICATION_JSON)
                    .content(bad));
            assertThat(refused.getStatus()).as(bad).isEqualTo(400);
            assertThat(body(refused).get("code").asString()).isEqualTo("FORECAST_INVALID");
        }
        Long seqBefore = jdbc.queryForObject("SELECT coalesce(max(seq), 0) FROM record_version", Long.class);
        JsonNode made = body(call(read, post(path).contentType(MediaType.APPLICATION_JSON)
                .content("{\"probability\":0.6}")));
        assertThat(made.get("madeAt").asString()).isEqualTo(clock.instant().toString());
        assertThat(made.get("asOfSeq").asLong()).isEqualTo(seqBefore);
        Map<String, Object> row = jdbc.queryForMap("SELECT made_at, asof_seq FROM forecast WHERE id = ?",
                made.get("id").asString());
        assertThat(((java.sql.Timestamp) row.get("made_at")).toInstant()).isEqualTo(clock.instant());
        assertThat(((Number) row.get("asof_seq")).longValue()).isEqualTo(seqBefore);
    }

    @Test
    @DisplayName("an annulled question is never scored, also when scores are recomputed")
    void annulledNeverScored() {
        ForecastQuestionEntity q = question("conflict");
        forecast(q, forecaster("operator:li", "operator"), 0.8);
        ForecastQuestionEntity annulled = ledger.annul(q.getId(), "OPERATOR", "Duplicate.", "operator", true);
        assertThat(annulled.getStatus()).isEqualTo(ForecastStatus.ANNULLED);
        assertThat(scoring.scoreQuestion(annulled)).isEmpty();
        // A stale score row (as an older scorer version might have left) is dropped, never recomputed into a score.
        ForecastScoreEntity stale = new ForecastScoreEntity(q.getId(), "operator:li");
        stale.setForecasterType("operator");
        stale.setCategory("conflict");
        stale.setQuestionType(Ledger.BINARY);
        stale.setResolvedAt(clock.instant());
        stale.setNForecasts(1);
        stale.setScorerVersion("0.0.1");
        stale.setComputedAt(clock.instant());
        scoreRows.saveAndFlush(stale);
        scoring.rescoreOutdated();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM forecast_score WHERE question_id = ?", Long.class,
                q.getId())).isZero();
        assertThat(scoring.rows()).noneMatch(s -> s.getQuestionId().equals(q.getId()));
    }

    @Test
    @DisplayName("time-averaged Brier carries each standing forecast forward to every later day")
    void timeAveragedCarriesForward() {
        ForecastQuestionEntity q = question("conflict");
        ForecasterEntity op = forecaster("operator:li", "operator");
        forecast(q, op, 0.2);
        clock.setInstant(D0.plus(Duration.ofDays(2)).plus(Duration.ofHours(1)));
        forecast(q, op, 0.8);
        clock.setInstant(D0.plus(Duration.ofDays(4)).plus(Duration.ofHours(1)));
        Map<String, Object> resolution = new LinkedHashMap<>();
        resolution.put("outcome", "yes");
        resolution.put("value", null);
        resolution.put("resolvedAt", D0.plus(Duration.ofDays(4)).toString());
        resolution.put("resolvedBy", "operator");
        resolution.put("early", false);
        resolution.put("evidence", List.of("alert:1"));
        ForecastQuestionEntity resolved = ledger.finishResolution(ledger.find(q.getId()).orElseThrow(), resolution);
        List<ForecastScoreEntity> rows = scoring.scoreQuestion(resolved);
        ForecastScoreEntity mine = rows.stream().filter(s -> s.getForecasterId()
                .equals(op.getId())).findFirst().orElseThrow();
        // Days 0 and 1 carry 0.2 forward, days 2 and 3 carry 0.8: (0.64 + 0.64 + 0.04 + 0.04) / 4.
        assertThat(mine.getNDays()).isEqualTo(4);
        assertThat(mine.getTimeAveragedBrier()).isCloseTo(0.34, within(1e-12));
        assertThat(mine.getBrier()).isCloseTo(0.04, within(1e-12));
    }

    /** A finished benchmark run in which every component the LLM ensemble weight depends on passed. */
    void llmGateOpen() {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String component : thresholds.dependencies(BenchmarkGate.LLM_ENSEMBLE_WEIGHT)) {
            results.add(Map.of("component", component, "status", "pass"));
        }
        BenchmarkRunEntity run = new BenchmarkRunEntity(Ulids.next(clock.instant()), "synthetic", clock.instant());
        run.setStatus("pass");
        run.setSuiteVersion("1.0.0");
        run.setScorerVersion("1.0.0");
        run.setThresholdsVersion("1.0.0");
        run.setTrigger("manual");
        run.setResults(results);
        run.setStartedAt(clock.instant());
        run.setFinishedAt(clock.instant());
        benchmarkRuns.saveAndFlush(run);
        gate.invalidate();
        assertThat(gate.llmEnsembleWeightAllowed()).isTrue();
    }

    @Test
    @DisplayName("the LLM cap applies only to llm forecasters; a Platt fit only to its own forecaster and category")
    @SuppressWarnings("unchecked")
    void capsAndPlattOnlyForTheirForecasters() {
        llmGateOpen();
        ForecastQuestionEntity q = question("conflict");
        ForecasterEntity op = forecaster("operator:li", "operator");
        ForecasterEntity llm = forecaster("llm:li", "llm");
        ForecasterEntity other = forecaster("operator:other", "operator");
        // The LLM has a fit in this category; the operator has one only in another category; "other" has none.
        ForecastCalibrationEntity llmFit = new ForecastCalibrationEntity(llm.getId(), "conflict", clock.instant());
        llmFit.setPlatt(0.5, 0.2, 60, clock.instant());
        calibrations.save(llmFit);
        ForecastCalibrationEntity elsewhere = new ForecastCalibrationEntity(op.getId(), "markets", clock.instant());
        elsewhere.setPlatt(2.0, -1.0, 60, clock.instant());
        calibrations.saveAndFlush(elsewhere);

        forecast(q, op, 0.99);
        forecast(q, other, 0.01);
        forecast(q, llm, 0.99);

        ForecastEntity ensemble = ledger.latestByForecaster(q.getId()).get(ForecasterRegistry.ENSEMBLE);
        assertThat(ensemble).isNotNull();
        Map<String, Map<String, Object>> members = new LinkedHashMap<>();
        for (Map<String, Object> m : (List<Map<String, Object>>) ensemble.getInputs().get("members")) {
            members.put(String.valueOf(m.get("forecasterId")), m);
        }
        assertThat(members).containsKeys(op.getId(), other.getId(), llm.getId());
        assertThat(((Number) members.get(op.getId()).get("capped")).doubleValue()).isEqualTo(0.99);
        assertThat(members.get(op.getId()).get("recalibrated")).isNull();
        assertThat(((Number) members.get(other.getId()).get("capped")).doubleValue()).isEqualTo(0.01);
        assertThat(members.get(other.getId()).get("recalibrated")).isNull();
        assertThat(((Number) members.get(llm.getId()).get("capped")).doubleValue()).isEqualTo(EnsembleMath.LLM_HIGH);
        assertThat(((Number) members.get(llm.getId()).get("recalibrated")).doubleValue()).isCloseTo(
                EnsembleMath.platt(0.5, 0.2, EnsembleMath.LLM_HIGH), within(1e-12));
        assertThat(ensemble.isRecalibrated()).isTrue();
    }
}
