package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.hypothesis.HypothesisEntity;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * Cycle runs end to end with scripted fake providers (prompt 128): a digest-only assessment without a provider; all
 * four roles with a scripted provider (cited judgments, a red-team note and forecast under {@code llm-redteam:*},
 * forecasts under {@code llm:*:*} that never change hypothesis probabilities, a pending proposal); the forecaster's
 * outside view before its inside view, samples aggregated by median log-odds, one forecast per provider forecaster;
 * a budget refusal marking a role {@code skipped} while the assessment is still written; the assessment linked to its
 * predecessor with what changed, its manifest, delivery and SSE event.
 */
@IntegrationTest
@Import(AskTestSources.class)
class CycleRunIT extends CycleItSupport {

    static final TokenUsage USAGE = new TokenUsage(10, 5, 0);

    record Seed(SituationEntity situation, HypothesisSetEntity set, ForecastQuestionEntity setQuestion,
            ForecastQuestionEntity other) {}

    Seed seed() {
        SituationEntity s = situation("Red Sea shipping", "critical");
        HypothesisSetEntity set = hypothesisSet(s.getId());
        ForecastQuestionEntity q = question("Coalition strike on Houthi targets", set.getId(), s.getId());
        jdbc.update("UPDATE hypothesis_set SET question_id = ? WHERE id = ?", q.getId(), set.getId());
        ForecastQuestionEntity other = question("Port of Hodeidah closes", null, null);
        clock.setInstant(clock.instant().plus(Duration.ofMinutes(1)));
        return new Seed(s, set, q, other);
    }

    Map<String, Double> probabilities(String setId) {
        return hypotheses.hypothesesOf(setId).stream().collect(Collectors.toMap(HypothesisEntity::getId,
                HypothesisEntity::getProbability));
    }

    @Test
    @DisplayName("without a provider the run publishes a digest-only assessment, roles not-configured")
    void digestOnly() {
        seed();
        llmSettings.update(new LlmSettings(null, null, true, true, false, 200_000, false));
        AnalysisCycleEntity cycle = cycle("daily", "[\"analyst\",\"forecaster\"]", null, null);
        CycleRunEntity run = runNow(cycle);
        assertThat(run.getStatus()).isEqualTo(CycleVocabulary.DIGEST_ONLY);
        assertThat(run.getRoles()).extracting(r -> r.get("status")).containsOnly(CycleVocabulary.NOT_CONFIGURED);
        assertThat(run.getPacket()).containsEntry("version", CyclePacketBuilder.VERSION);
        AssessmentEntity a = assessmentRows.findById(run.getAssessmentId()).orElseThrow();
        assertThat(a.getStatus()).isEqualTo(CycleVocabulary.DIGEST_ONLY);
        assertThat(a.getBluf()).isNotEmpty().allSatisfy(j -> assertThat(j).containsEntry("textOrigin",
                "deterministic"));
        assertThat(a.getMarkdown()).contains(CycleVocabulary.FOOTER);
        assertThat(a.getHtml()).contains("<html");
        assertThat(a.getManifestId()).isNotNull();
        assertThat(a.getEffort()).isNull();
        // Both counts are scoped to this run's assessment: other tests in the shared context publish assessments.
        assertThat(jdbc.queryForObject("select count(*) from notification where event = 'assessment.published' "
                + "and data->>'assessmentId' = ?", Long.class, a.getId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from assessment_log where event = 'assessment.published' "
                + "and payload->>'assessmentId' = ?", Long.class, a.getId())).isEqualTo(1);
        assertThat(fakes.client(CHAT).requests()).isEmpty();

        AnalysisCycleEntity noRoles = cycle("daily", "[]", null, null);
        assertThat(runNow(noRoles).getStatus()).isEqualTo(CycleVocabulary.DIGEST_ONLY);
    }

    @Test
    @DisplayName("all four roles: cited judgments, a red-team forecast under its own id, forecasts per provider, a"
            + " pending proposal; hypothesis probabilities unchanged")
    void allRoles() {
        Seed seed = seed();
        provider("chat2", "openai-compatible", 1_000_000L);
        scripted(fakes.client(CHAT), USAGE);
        scripted(fakes.client("chat2"), USAGE);
        AnalystProfileEntity profile = profile(null);
        AnalysisCycleEntity cycle = cycle("daily", "[\"analyst\",\"red-team\",\"forecaster\",\"proposer\"]",
                profile.getId(), "\"forecasterProviders\":[\"chat\",\"chat2\"],\"samples\":3");
        Map<String, Double> before = probabilities(seed.set().getId());

        CycleRunEntity run = runNow(cycle);

        assertThat(run.getStatus()).as(String.valueOf(run.getRoles())).isEqualTo(CycleVocabulary.OK);
        AssessmentEntity a = assessmentRows.findById(run.getAssessmentId()).orElseThrow();
        assertThat(a.getStatus()).isEqualTo(CycleVocabulary.OK);
        assertThat(a.getBluf()).hasSize(3);
        Map<String, Object> first = a.getBluf().get(0);
        assertThat(first).containsEntry("textOrigin", "llm").containsEntry("estimativeTerm", "likely")
                .containsEntry("probability", 0.62);
        assertThat(first.get("questionId")).isNotNull();
        assertThat((List<?>) a.getBluf().get(1).get("citations")).hasSize(1);
        assertThat(a.getBluf().get(2)).containsEntry("downgraded", true).containsEntry("probability", null);
        assertThat(a.getEffort()).isEqualTo("medium");
        assertThat(a.getModel()).isEqualTo("fake-chat");
        assertThat(a.getPromptTemplates()).hasSize(4);
        assertThat(a.getMarkdown()).contains("### Red team").contains("Airbase activity is not observed.");

        String key = CycleRoles.profileKey(profile);
        List<ForecastEntity> redTeam = ledger.forecastsOf(seed.setQuestion().getId()).stream()
                .filter(f -> f.getForecasterId().equals(CycleRoles.redTeamId(key))).toList();
        assertThat(redTeam).singleElement().satisfies(f -> assertThat(f.getProbability()).isEqualTo(0.35));
        for (String provider : List.of("chat", "chat2")) {
            String id = CycleRoles.forecasterId(key, provider);
            assertThat(ledger.forecastsOf(seed.other().getId()).stream().filter(f -> f.getForecasterId().equals(id)))
                    .as(id).hasSize(1);
        }
        assertThat(probabilities(seed.set().getId())).isEqualTo(before);
        assertThat(jdbc.queryForList("select status from analyst_proposal where run_id = ?", String.class,
                run.getId())).containsExactly("pending");
        Map<String, Object> roles = a.getRoles();
        assertThat(roles).containsKeys("analyst", "red-team", "forecaster", "proposer");
        assertThat(((Number) ((Map<?, ?>) roles.get("forecaster")).get("forecastsSubmitted")).intValue())
                .isGreaterThanOrEqualTo(4);
        assertThat(run.getTokensUsed()).isPositive();
    }

    @Test
    @DisplayName("the forecaster takes the outside view before the inside view and records the median log-odds")
    @SuppressWarnings("unchecked")
    void forecasterSamples() {
        Seed seed = seed();
        scripted(fakes.client(CHAT), USAGE);
        AnalystProfileEntity profile = profile(null);
        AnalysisCycleEntity cycle = cycle("daily", "[\"forecaster\"]", profile.getId(), "\"samples\":3");
        runNow(cycle);
        String id = CycleRoles.forecasterId(CycleRoles.profileKey(profile), CHAT);
        ForecastEntity f = ledger.forecastsOf(seed.other().getId()).stream().filter(x -> x.getForecasterId().equals(id))
                .findFirst().orElseThrow();
        assertThat(f.getProbability()).isCloseTo(0.3, within(1e-9));
        assertThat(f.getInputs()).containsEntry("samples", 3).containsEntry("samplesUsed", 3)
                .containsEntry("aggregation", "median log-odds").containsEntry("provider", CHAT);
        List<List<String>> orders = (List<List<String>>) f.getInputs().get("toolOrder");
        assertThat(orders).hasSize(3).allSatisfy(order -> {
            assertThat(order).containsSubsequence("get_base_rate", "find_analogs",
                    CycleOutputTools.RECORD_OUTSIDE_VIEW, CycleOutputTools.RECORD_FORECAST);
        });
        assertThat(f.getRationale()).contains("Resolution criteria (verbatim): " + seed.other().getResolutionCriteria());
        assertThat(f.getClaims()).isNotEmpty();
    }

    @Test
    @DisplayName("a role that meets the cycle's token budget is skipped and the assessment is still written")
    void budgetRefusal() {
        seed();
        scripted(fakes.client(CHAT), new TokenUsage(400, 200, 0));
        AnalystProfileEntity profile = profile(null);
        AnalysisCycleEntity cycle = cycle("daily", "[\"analyst\",\"red-team\"]", profile.getId(),
                "\"maxTokensPerCycle\":500");
        CycleRunEntity run = runNow(cycle);
        Map<String, Map<String, Object>> byRole = run.getRoles().stream().collect(Collectors.toMap(
                r -> String.valueOf(r.get("role")), r -> r));
        assertThat(byRole.get("red-team")).containsEntry("status", CycleVocabulary.SKIPPED);
        assertThat(String.valueOf(byRole.get("red-team").get("reason"))).contains("max_tokens_per_cycle");
        assertThat(run.getStatus()).isIn(CycleVocabulary.PARTIAL, CycleVocabulary.DIGEST_ONLY);
        assertThat(run.getAssessmentId()).isNotNull();
        assertThat(assessmentRows.findById(run.getAssessmentId())).isPresent();
    }

    @Test
    @DisplayName("the next assessment links the previous one and starts its period where the last run ended")
    void buildsOnPredecessor() {
        seed();
        AnalysisCycleEntity cycle = cycle("daily", "[]", null, null);
        CycleRunEntity first = runNow(cycle);
        clock.setInstant(clock.instant().plus(Duration.ofDays(1)));
        CycleRunEntity second = runNow(cycle);
        assertThat(second.getPreviousRunId()).isEqualTo(first.getId());
        assertThat(second.getPeriodStart()).isEqualTo(first.getPeriodEnd());
        assertThat(second.getPacket()).containsEntry("firstRun", false);
        AssessmentEntity a = assessmentRows.findById(second.getAssessmentId()).orElseThrow();
        assertThat(a.getPreviousAssessmentId()).isEqualTo(first.getAssessmentId());
        assertThat(a.getMarkdown()).contains("assessment:" + first.getAssessmentId());
    }

    @Test
    @DisplayName("an on-demand situation assessment has the situation sections and the on-demand cadence")
    void onDemand() {
        Seed seed = seed();
        scripted(fakes.client(CHAT), USAGE);
        CycleRunEntity queued = runner.queueSituation(seed.situation().getId(), List.of("analyst"), "low");
        CycleRunEntity run = runner.execute(queued.getId());
        AssessmentEntity a = assessmentRows.findById(run.getAssessmentId()).orElseThrow();
        assertThat(a.getCadence()).isEqualTo(CycleVocabulary.ON_DEMAND);
        assertThat(a.getSituationId()).isEqualTo(seed.situation().getId());
        assertThat(a.getSections()).extracting(s -> s.get("key")).contains("key-insights", "outlook", "risks");
        assertThat(a.getEffort()).isEqualTo("low");
    }
}
