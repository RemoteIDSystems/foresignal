package ai.foresignal.hypothesis;

import static ai.foresignal.hypothesis.HypothesisFixtures.HYPOTHESIS_OUTCOME;
import static ai.foresignal.hypothesis.HypothesisFixtures.outcomeSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The lifecycle of a hypothesis set (prompt 125 §5): structure validation, the ledger question created with the set
 * and its {@code indicator-bayes} forecast, resolution confirming and refuting hypotheses, review revisions (with the
 * lint and the archive after the horizon), and the band-change notification's 6-hour rate limit.
 */
@IntegrationTest
class HypothesisLifecycleIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired HypothesisService service;
    @Autowired HypothesisScheduler scheduler;
    @Autowired HypothesisRevisionRepository revisions;
    @Autowired HypothesisLogRepository logs;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry registry;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        registry.registerBuiltIns();
    }

    HypothesisSetEntity create(String body) {
        return service.create(HypothesisInputs.set(json.readTree(body)), "agent", "tester");
    }

    String hypothesisId(HypothesisSetEntity set, String label) {
        return service.hypothesesOf(set.getId()).stream().filter(h -> h.getLabel().equals(label)).findFirst()
                .orElseThrow().getId();
    }

    long bandNotifications() {
        return jdbc.queryForObject("select count(*) from notification where event = 'hypothesis.band_changed'",
                Long.class);
    }

    static void invalid(Runnable call, String code) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ValidationException.class,
                e -> assertThat(e.getCode()).isEqualTo(code));
    }

    @Test
    @DisplayName("structure: exactly one other, priors summing to 1, a basis per prior, speculative links complete")
    void structure() {
        String valid = outcomeSet("weekly", null, "", null, "");
        assertThat(HypothesisInputs.set(json.readTree(valid)).hypotheses()).hasSize(4);
        invalid(() -> HypothesisInputs.set(json.readTree(valid.replace("\"type\":\"other\"", "\"type\":\"outcome\""))),
                ErrorCodes.HYPOTHESIS_SET_INVALID);
        invalid(() -> HypothesisInputs.set(json.readTree(valid.replace("\"prior\":0.5", "\"prior\":0.6"))),
                ErrorCodes.HYPOTHESIS_SET_INVALID);
        invalid(() -> HypothesisInputs.set(json.readTree(valid.replace(",\"rationale\":\"Restraint during talks.\"",
                ""))), ErrorCodes.HYPOTHESIS_SET_INVALID);
        invalid(() -> HypothesisInputs.set(json.readTree(valid.replace("\"hypotheses\":[", "\"hypotheses\":[{\"label\""
                + ":\"advance knowledge\",\"statement\":\"The positioning reflected advance knowledge.\",\"type\":"
                + "\"speculative-link\",\"prior\":0.0,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"x\"},"
                + "\"linkedFindingIds\":[],\"alternatives\":[\"other\"]},"))), ErrorCodes.HYPOTHESIS_SET_INVALID);
        String unknownFinding = valid.replace("{\"label\":\"within 72 h\",\"statement\":\"A response follows within 72"
                + " hours.\",\"type\":\"outcome\"", "{\"label\":\"within 72 h\",\"statement\":\"A response follows within"
                + " 72 hours.\",\"type\":\"speculative-link\",\"linkedFindingIds\":[\"01JA8Q3V7S2K9W4T6Y1B5N0M3C\"],"
                + "\"coincidenceBaseline\":{\"placeboRate\":0.1,\"method\":\"placebo-event-study\"},"
                + "\"alternatives\":[\"3-7 days\",\"other\"]");
        invalid(() -> create(unknownFinding), ErrorCodes.HYPOTHESIS_SET_INVALID);
        invalid(() -> create(outcomeSet("weekly", null, "", HYPOTHESIS_OUTCOME, "")),
                ErrorCodes.HYPOTHESIS_SET_INVALID);
        assertThat(jdbc.queryForObject("select count(*) from hypothesis_set", Long.class)).isZero();
    }

    @Test
    @DisplayName("a set with a ledger question opens it, maps outcomes and records the first indicator-bayes forecast")
    void questionCreation() {
        HypothesisSetEntity set = create(outcomeSet("weekly", "2026-10-24", "", HYPOTHESIS_OUTCOME, ""));
        ForecastQuestionEntity q = ledger.find(set.getQuestionId()).orElseThrow();
        assertThat(q.getType()).isEqualTo("multiple");
        assertThat(q.labels()).containsExactly("within 72 h", "3-7 days", "none in 7 days", "other");
        assertThat(q.getHypothesisSetId()).isEqualTo(set.getId());
        assertThat(q.getResolverType()).isEqualTo("hypothesis-outcome");
        assertThat(q.getResolver()).containsEntry("setId", set.getId());
        assertThat(q.getDedupKey()).isEqualTo("hypothesis-set:" + set.getId());
        List<HypothesisRevisionEntity> rows = revisions.findBySetIdOrderByIdDesc(set.getId(), Limit.of(10));
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getCause()).isEqualTo("created");
        assertThat(rows.get(0).getUpdaterVersion()).isEqualTo("indicator-bayes 1.0.0");
        assertThat(rows.get(0).getLedgerForecastId()).isNotNull();
        ForecastEntity forecast = ledger.latestByForecaster(q.getId()).get(HypothesisUpdater.NAME);
        assertThat(forecast.getId()).isEqualTo(rows.get(0).getLedgerForecastId());
        assertThat(forecast.getDistribution().get("within 72 h")).isCloseTo(0.25, within(1e-9));
        assertThat(forecast.getDistribution().get("none in 7 days")).isCloseTo(0.5, within(1e-9));
        for (HypothesisEntity h : service.hypothesesOf(set.getId())) {
            assertThat(h.getProbability()).isCloseTo(h.getPrior(), within(1e-12));
            assertThat(h.getLedgerOutcome()).isEqualTo(h.getLabel());
        }
        assertThat(logs.findAll()).extracting(HypothesisLogEntity::getEvent).contains(HypothesisEvents.SET_CREATED);
    }

    @Test
    @DisplayName("resolution confirms the hypothesis mapped to the outcome, refutes the others and resolves the set")
    void resolution() {
        HypothesisSetEntity set = create(outcomeSet("weekly", "2026-10-24", HypothesisFixtures.manualIndicator(
                "Military air activity", "aviation-military", "M", HypothesisFixtures.QUICK_LR), HYPOTHESIS_OUTCOME,
                ""));
        ledger.resolveManually(set.getQuestionId(), new ForecastInputs.ResolutionSubmission("within 72 h", null,
                List.of("alert:1"), "Confirmed by the operator."), "operator", true);
        HypothesisSetEntity resolved = service.get(set.getId(), true);
        assertThat(resolved.getStatus()).isEqualTo("resolved");
        assertThat(resolved.getResolvedOutcome()).isEqualTo("within 72 h");
        for (HypothesisEntity h : service.hypothesesOf(set.getId())) {
            assertThat(h.getStatus()).as(h.getLabel()).isEqualTo("within 72 h".equals(h.getLabel()) ? "confirmed"
                    : "refuted");
        }
        assertThat(service.indicatorsOf(set.getId())).allMatch(i -> "retired".equals(i.getState()));
        assertThat(logs.findAll()).extracting(HypothesisLogEntity::getEvent).contains(HypothesisEvents.SET_RESOLVED);
        assertThatThrownBy(() -> service.review(set.getId(), HypothesisInputs.review(json.readTree(
                "{\"rationale\":\"Late review.\"}")), "tester", true))
                .isInstanceOf(ai.foresignal.api.error.ConflictException.class);
    }

    @Test
    @DisplayName("reviews record revisions with the reviewer's rationale; a failing lint writes nothing; past the"
            + " horizon a review archives a set without a question")
    void reviews() {
        HypothesisSetEntity set = create(outcomeSet("daily", "2026-10-20", "", null, ""));
        String quick = hypothesisId(set, "within 72 h");
        HypothesisRevisionEntity review = service.review(set.getId(), HypothesisInputs.review(json.readTree(
                "{\"rationale\":\"Coalition aircraft were reported over the Red Sea.\",\"probabilities\":{\"" + quick
                        + "\":0.6}}")), "tester", true);
        assertThat(review.getCause()).isEqualTo("review");
        assertThat(review.getRationaleOrigin()).isEqualTo("operator");
        assertThat(review.getRationale()).isEqualTo("Coalition aircraft were reported over the Red Sea.");
        Map<String, Double> after = new java.util.HashMap<>();
        for (HypothesisEntity h : service.hypothesesOf(set.getId())) {
            after.put(h.getLabel(), h.getProbability());
        }
        assertThat(after.get("within 72 h")).isCloseTo(0.6, within(1e-9));
        assertThat(after.get("3-7 days")).isCloseTo(0.2 * 0.4 / 0.75, within(1e-9));
        assertThat(after.get("none in 7 days")).isCloseTo(0.5 * 0.4 / 0.75, within(1e-9));
        assertThat(after.values().stream().mapToDouble(Double::doubleValue).sum()).isCloseTo(1.0, within(1e-9));
        HypothesisSetEntity reviewed = service.get(set.getId(), true);
        assertThat(reviewed.getLastReviewedAt()).isEqualTo(NOW);
        assertThat(reviewed.getNextReviewAt()).isEqualTo(NOW.plus(Duration.ofDays(1)));

        long before = revisions.countBySetId(set.getId());
        invalid(() -> service.review(set.getId(), HypothesisInputs.review(json.readTree(
                "{\"rationale\":\"A response is likely (20%) and we hold high confidence in it.\"}")), "tester", true),
                ErrorCodes.LINT_FAILED);
        assertThat(revisions.countBySetId(set.getId())).isEqualTo(before);

        clock.setInstant(Instant.parse("2026-10-22T12:00:00Z"));
        service.review(set.getId(), HypothesisInputs.review(json.readTree("{\"rationale\":\"The horizon passed.\"}")),
                "tester", true);
        assertThat(service.get(set.getId(), true).getStatus()).isEqualTo("archived");
        assertThat(service.hypothesesOf(set.getId())).allMatch(h -> "expired".equals(h.getStatus()));
    }

    @Test
    @DisplayName("band changes notify at most once per set per 6 hours; the latest change waits and is sent after")
    void bandChangeRateLimit() {
        HypothesisSetEntity set = create(outcomeSet("daily", null, "", null, ""));
        String quick = hypothesisId(set, "within 72 h");
        HypothesisRevisionEntity first = service.review(set.getId(), HypothesisInputs.review(json.readTree(
                "{\"rationale\":\"Strike preparations were reported.\",\"probabilities\":{\"" + quick + "\":0.6}}")),
                "tester", true);
        assertThat(first.isBandChanged()).isTrue();
        assertThat(bandNotifications()).isEqualTo(1);
        assertThat(logs.findAll()).extracting(HypothesisLogEntity::getEvent)
                .contains(HypothesisEvents.SET_BAND_CHANGED);

        clock.setInstant(NOW.plus(Duration.ofHours(1)));
        service.review(set.getId(), HypothesisInputs.review(json.readTree(
                "{\"rationale\":\"The preparations were an exercise.\",\"probabilities\":{\"" + quick + "\":0.3}}")),
                "tester", true);
        assertThat(bandNotifications()).as("inside the 6-hour window").isEqualTo(1);
        assertThat(service.get(set.getId(), true).getPendingBandChange()).isNotNull();
        assertThat(scheduler.flushBandChanges()).as("still inside the window").isZero();

        clock.setInstant(NOW.plus(Duration.ofHours(6)));
        assertThat(scheduler.flushBandChanges()).isEqualTo(1);
        assertThat(bandNotifications()).isEqualTo(2);
        assertThat(service.get(set.getId(), true).getPendingBandChange()).isNull();

        HypothesisSetEntity monthly = create(outcomeSet("monthly", null, "", null, ""));
        service.review(monthly.getId(), HypothesisInputs.review(json.readTree("{\"rationale\":\"Reporting.\","
                + "\"probabilities\":{\"" + hypothesisId(monthly, "within 72 h") + "\":0.6}}")), "tester", true);
        assertThat(bandNotifications()).as("monthly sets do not notify by default").isEqualTo(2);
    }
}
