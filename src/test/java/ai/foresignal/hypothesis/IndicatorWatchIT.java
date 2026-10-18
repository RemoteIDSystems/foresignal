package ai.foresignal.hypothesis;

import static ai.foresignal.hypothesis.HypothesisFixtures.HYPOTHESIS_OUTCOME;
import static ai.foresignal.hypothesis.HypothesisFixtures.QUICK_LR;
import static ai.foresignal.hypothesis.HypothesisFixtures.intelIndicator;
import static ai.foresignal.hypothesis.HypothesisFixtures.outcomeSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.collect.HealthService;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityDeclarations;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Limit;
import tools.jackson.databind.json.JsonMapper;

/**
 * Indicators that watch for themselves (prompt 125 §2): the watch rule and its tags, an alert observing the indicator
 * with exactly one revision and one ledger forecast, an echo of the same record suppressed, {@code expected_by} with
 * coverage giving {@code absent} and without it {@code unknown-coverage} and no revision, retirement disabling the
 * rule, and a {@code question} signal following a 122 resolution. Coverage is decided by a stand-in for the 116
 * judgment, so each branch is exercised deterministically.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
class IndicatorWatchIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired HypothesisService service;
    @Autowired IndicatorWatchService watch;
    @Autowired RevisionWriter writer;
    @Autowired IndicatorRepository indicators;
    @Autowired HypothesisSetRepository sets;
    @Autowired CollectionGapRepository gaps;
    @Autowired HypothesisRevisionRepository revisions;
    @Autowired AchEvidenceRepository evidence;
    @Autowired TriggerRuleService rules;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry registry;
    @Autowired ApplicationContext context;
    @Autowired JsonMapper json;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        registry.registerBuiltIns();
    }

    HypothesisSetEntity create(String indicatorsJson, String ledgerQuestion) {
        return service.create(HypothesisInputs.set(json.readTree(outcomeSet("daily", "2026-10-24", indicatorsJson,
                ledgerQuestion, ""))), "agent", "tester");
    }

    IndicatorEntity indicator(HypothesisSetEntity set, String description) {
        return service.indicatorsOf(set.getId()).stream().filter(i -> i.getDescription().equals(description))
                .findFirst().orElseThrow();
    }

    double probability(HypothesisSetEntity set, String label) {
        return service.hypothesesOf(set.getId()).stream().filter(h -> h.getLabel().equals(label)).findFirst()
                .orElseThrow().getProbability();
    }

    static AlertView alert(long id, IndicatorEntity indicator, String externalId, long seq) {
        return new AlertView(id, indicator.getWatchRuleId(), "Indicator: " + indicator.getDescription(), seq,
                "foresignal-intel", externalId, "intelligence-event", NOW, NOW, "intel event", "info", "firing", 1,
                null, null, null, null, null, null, null, null, null, false, null,
                IndicatorWatchService.tags(indicator.getId()), null, null, null, null, false, null, null);
    }

    HypothesisScheduler scheduler(boolean covered) {
        HypothesisAnalysis stand = new HypothesisAnalysis(service, gaps, context.getBeanProvider(
                ObservabilityService.class), context.getBeanProvider(ObservabilityDeclarations.class),
                context.getBeanProvider(SourceRegistry.class), context.getBeanProvider(HealthService.class), clock) {
            @Override
            public Detect detect(IndicatorEntity i, HypothesisSetEntity s, Instant from, Instant to) {
                return covered ? new Detect("ais-behaviour", 0.9, true, new AbsenceCheck(true, 0.9, null))
                        : new Detect("ais-behaviour", 0.3, true, new AbsenceCheck(false, 0.3,
                                AbsenceCheck.LOW_DETECTABILITY));
            }
        };
        return new HypothesisScheduler(service, stand, writer, indicators, sets, clock, true);
    }

    @Test
    @DisplayName("an intel-event indicator owns an enabled watch rule tagged indicator, origin-hypothesis and its id")
    void watchRule() {
        HypothesisSetEntity set = create(intelIndicator("Maritime disruption", "maritime-disruption", "ais-behaviour",
                null, QUICK_LR), null);
        IndicatorEntity i = indicator(set, "Maritime disruption");
        assertThat(i.getWatchRuleId()).isNotNull();
        TriggerRuleEntity rule = rules.load(i.getWatchRuleId());
        assertThat(rule.isEnabled()).isTrue();
        assertThat(rule.getTags()).contains("indicator", "origin-hypothesis",
                "ind-" + i.getId().toLowerCase(Locale.ROOT));
        assertThat(rule.getName()).isEqualTo("Indicator: Maritime disruption");
        assertThat(i.getState()).isEqualTo("pending");
    }

    @Test
    @DisplayName("an alert observes the indicator: one revision, one ledger forecast, an ACH row; an echo adds nothing")
    void alertObserves() {
        HypothesisSetEntity set = create(intelIndicator("Maritime disruption", "maritime-disruption", "ais-behaviour",
                null, QUICK_LR) + "," + intelIndicator("Aviation anomaly", "aviation-anomaly", "aviation-ops", null,
                QUICK_LR), HYPOTHESIS_OUTCOME);
        IndicatorEntity maritime = indicator(set, "Maritime disruption");
        long forecastsBefore = ledger.forecastsOf(set.getQuestionId()).stream()
                .filter(f -> HypothesisUpdater.NAME.equals(f.getForecasterId())).count();
        double before = probability(set, "within 72 h");

        watch.onAlert(AlertEvent.of(AlertEvent.FIRED, alert(77, maritime, "EVT1", 1)));

        IndicatorEntity observed = indicators.findById(maritime.getId()).orElseThrow();
        assertThat(observed.getState()).isEqualTo("observed");
        assertThat(observed.getObservedEvidence()).containsEntry("source", "alert")
                .containsEntry("evidenceKey", "ref:record:foresignal-intel/EVT1");
        List<HypothesisRevisionEntity> rows = revisions.findBySetIdOrderByIdDesc(set.getId(), Limit.of(10));
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getCause()).isEqualTo("indicator-observed");
        assertThat(rows.get(0).getActor()).isEqualTo("indicator-bayes");
        assertThat(rows.get(0).getDrivers()).isNotEmpty();
        assertThat(rows.get(0).getLedgerForecastId()).isNotNull();
        assertThat(ledger.forecastsOf(set.getQuestionId()).stream()
                .filter(f -> HypothesisUpdater.NAME.equals(f.getForecasterId())).count())
                .isEqualTo(forecastsBefore + 1);
        assertThat(probability(set, "within 72 h")).isGreaterThan(before);
        assertThat(rules.load(maritime.getWatchRuleId()).isEnabled()).as("an observed indicator stops watching")
                .isFalse();
        assertThat(evidence.findBySetIdOrderByIdAsc(set.getId())).singleElement()
                .satisfies(row -> assertThat(row.getSource()).isEqualTo("indicator"));

        // The same record seen by another indicator is one piece of evidence: suppressed, attached, nothing moves.
        IndicatorEntity aviation = indicator(set, "Aviation anomaly");
        double afterFirst = probability(set, "within 72 h");
        watch.onAlert(AlertEvent.of(AlertEvent.FIRED, alert(78, aviation, "EVT1", 2)));
        List<HypothesisRevisionEntity> after = revisions.findBySetIdOrderByIdDesc(set.getId(), Limit.of(10));
        assertThat(after).hasSize(3);
        assertThat(after.get(0).getDrivers()).allMatch(d -> Boolean.TRUE.equals(d.get("suppressed"))
                && HypothesisUpdater.SAME_GROUP.equals(d.get("suppressionReason")));
        assertThat(probability(set, "within 72 h")).isCloseTo(afterFirst, within(1e-12));
        assertThat(evidence.findBySetIdOrderByIdAsc(set.getId())).singleElement()
                .satisfies(row -> assertThat(row.getRefs()).hasSize(2));

        // A repeated alert for an indicator that is no longer waiting changes nothing.
        watch.onAlert(AlertEvent.of(AlertEvent.FIRED, alert(79, maritime, "EVT2", 3)));
        assertThat(revisions.countBySetId(set.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("expected_by with coverage makes the indicator absent and revises the set with its absence LRs")
    void expectedWithCoverage() {
        HypothesisSetEntity set = create(intelIndicator("Maritime disruption", "maritime-disruption", "ais-behaviour",
                "2026-10-17T13:00:00Z", QUICK_LR), null);
        double before = probability(set, "within 72 h");
        clock.setInstant(NOW.plus(Duration.ofHours(2)));
        assertThat(scheduler(true).checkExpected()).isEqualTo(1);
        IndicatorEntity i = indicator(set, "Maritime disruption");
        assertThat(i.getState()).isEqualTo("absent");
        assertThat(i.getPDetect()).isCloseTo(0.9, within(1e-12));
        HypothesisRevisionEntity latest = revisions.findBySetIdOrderByIdDesc(set.getId(), Limit.of(1)).get(0);
        assertThat(latest.getCause()).isEqualTo("indicator-absent");
        assertThat(latest.getDrivers()).anyMatch(d -> Double.valueOf(0.9).equals(d.get("pDetect")));
        assertThat(probability(set, "within 72 h")).isLessThan(before);
        assertThat(latest.getRationale()).contains("absent with coverage");
    }

    @Test
    @DisplayName("expected_by without coverage is unknown-coverage and writes no revision")
    void expectedWithoutCoverage() {
        HypothesisSetEntity set = create(intelIndicator("Maritime disruption", "maritime-disruption", "ais-behaviour",
                "2026-10-17T13:00:00Z", QUICK_LR), null);
        long before = revisions.countBySetId(set.getId());
        clock.setInstant(NOW.plus(Duration.ofHours(2)));
        assertThat(scheduler(false).checkExpected()).isEqualTo(1);
        IndicatorEntity i = indicator(set, "Maritime disruption");
        assertThat(i.getState()).isEqualTo("unknown-coverage");
        assertThat(i.getObservedEvidence()).containsEntry("reason", AbsenceCheck.LOW_DETECTABILITY);
        assertThat(revisions.countBySetId(set.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("retiring an indicator disables its watch rule and keeps its history")
    void retire() {
        HypothesisSetEntity set = create(intelIndicator("Maritime disruption", "maritime-disruption", "ais-behaviour",
                null, QUICK_LR), null);
        IndicatorEntity i = indicator(set, "Maritime disruption");
        IndicatorEntity retired = service.retireIndicator(i.getId(), true);
        assertThat(retired.getState()).isEqualTo("retired");
        assertThat(retired.getWatchRuleId()).isEqualTo(i.getWatchRuleId());
        assertThat(rules.load(i.getWatchRuleId()).isEnabled()).isFalse();
        watch.onAlert(AlertEvent.of(AlertEvent.FIRED, alert(80, retired, "EVT9", 9)));
        assertThat(indicators.findById(i.getId()).orElseThrow().getState()).isEqualTo("retired");
    }

    @Test
    @DisplayName("a question signal is observed when its question resolves to its outcome")
    void questionSignal() {
        ForecastService.Opened other = ledger.openQuestion(new ForecastInputs.QuestionDraft("Will the strait close?",
                "binary", Map.of(), "Yes if a closure is announced.", ResolverSpec.validate(Map.of("type", "manual"),
                        "binary"), NOW, NOW.plus(Duration.ofDays(7)), NOW.plus(Duration.ofDays(8)), "maritime",
                List.of(), Map.of(), null, false), "agent", "tester");
        String questionId = other.question().getId();
        HypothesisSetEntity set = create("{\"description\":\"Strait closure announced\",\"signal\":{\"type\":"
                + "\"question\",\"question\":{\"questionId\":\"" + questionId + "\",\"outcome\":\"yes\"}},"
                + "\"signalFamily\":\"ais-behaviour\",\"credibility\":\"H\",\"relevance\":\"H\",\"lr\":" + QUICK_LR
                + "}", null);
        assertThat(indicator(set, "Strait closure announced").getWatchRuleId()).isNull();
        ledger.resolveManually(questionId, new ForecastInputs.ResolutionSubmission("yes", null, List.of("alert:1"),
                "Announced."), "operator", true);
        assertThat(indicator(set, "Strait closure announced").getState()).isEqualTo("observed");
        assertThat(revisions.findBySetIdOrderByIdDesc(set.getId(), Limit.of(1)).get(0).getCause())
                .isEqualTo("indicator-observed");
    }
}
