package ai.foresignal.quality;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The tuning workflow (prompt 115): a suggestion is persisted and deduplicated; a rule edit marks it STALE and
 * apply answers 409; apply goes through {@code updateTrigger} and writes the tuning log; undo restores the rule
 * and refuses after a later edit; dismiss.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class TuningWorkflowIT {

    static final Instant T = Instant.parse("2026-09-05T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertService alertService;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired TuningWorkflowService tuning;
    @Autowired TuningSuggestionRepository suggestions;
    @Autowired RuleRevisions revisions;

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

    /** A warning rule whose ten labels are 70% useful and 30% late: a SEVERITY_UP suggestion. */
    UUID seeded() {
        UUID rule = fixture.rule("{'name':'Late','sources':['" + STANDARD + "'],'mode':'match','severity':'warning',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        for (int i = 0; i < 10; i++) {
            records.ingest(STANDARD, NormalizedRecord.builder("q" + i, STANDARD_KIND).observedAt(T)
                    .put("mag", 6.0 + i / 10.0).build());
        }
        List<AlertView> fired = fixture.routed(rule);
        assertThat(fired).hasSize(10);
        for (int i = 0; i < 10; i++) {
            alertService.setFeedback(fired.get(i).id(), new AlertService.Transition(null, i < 3 ? "late" : "useful"));
        }
        return rule;
    }

    TuningSuggestionEntity severityUp(UUID rule) {
        return tuning.generate(rule, T.minus(Duration.ofDays(1))).stream()
                .filter(s -> "SEVERITY_UP".equals(s.getKind())).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("generation persists once per (rule, kind, patch) and records the rule revision")
    void persistedAndDeduplicated() {
        UUID rule = seeded();
        TuningSuggestionEntity first = severityUp(rule);
        TuningSuggestionEntity again = severityUp(rule);
        assertThat(again.getId()).isEqualTo(first.getId());
        assertThat(suggestions.findByRuleIdOrderByCreatedAtDescIdDesc(rule).stream()
                .filter(s -> "SEVERITY_UP".equals(s.getKind()))).hasSize(1);
        assertThat(first.getStatus()).isEqualTo(TuningSuggestionEntity.OPEN);
        assertThat(first.getRuleRev()).isEqualTo(revisions.revision(rule));
        assertThat(first.getPatch()).containsEntry("severity", "critical");
        assertThat(tuning.list(rule, "OPEN")).extracting(TuningSuggestionEntity::getId).contains(first.getId());
    }

    @Test
    @DisplayName("a rule edit marks open suggestions STALE and apply answers 409 SUGGESTION_STALE")
    void staleAfterEdit() {
        UUID rule = seeded();
        TuningSuggestionEntity suggestion = severityUp(rule);
        Map<String, Object> input = revisions.input(rule);
        input.put("name", "Late (renamed)");
        rules.update(rule, JSON.valueToTree(input));
        assertThat(suggestions.findById(suggestion.getId()).orElseThrow().getStatus())
                .isEqualTo(TuningSuggestionEntity.STALE);
        assertThatThrownBy(() -> tuning.apply(rule, suggestion.getId(), null, "token:t"))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((ConflictException) e).getCode()).isEqualTo(ErrorCodes.SUGGESTION_STALE));
        // A snooze is not a definition change.
        TuningSuggestionEntity reopened = severityUp(rule);
        assertThat(reopened.getStatus()).isEqualTo(TuningSuggestionEntity.OPEN);
        rules.snooze(rule, T.plus(Duration.ofHours(1)));
        assertThat(suggestions.findById(reopened.getId()).orElseThrow().getStatus())
                .isEqualTo(TuningSuggestionEntity.OPEN);
    }

    @Test
    @DisplayName("apply goes through updateTrigger and logs before and after; undo restores and refuses after an edit")
    void applyAndUndo() {
        UUID rule = seeded();
        TuningSuggestionEntity suggestion = severityUp(rule);
        RuleTuningLogEntity entry = tuning.apply(rule, suggestion.getId(), "reviewed", "token:t");
        assertThat(rules.load(rule).getSeverity()).isEqualTo("critical");
        assertThat(entry.getBefore()).containsEntry("severity", "warning");
        assertThat(entry.getAfter()).containsEntry("severity", "critical");
        assertThat(entry.getAppliedBy()).isEqualTo("token:t");
        assertThat(entry.getNote()).isEqualTo("reviewed");
        assertThat(suggestions.findById(suggestion.getId()).orElseThrow().getStatus())
                .isEqualTo(TuningSuggestionEntity.APPLIED);
        assertThat(tuning.log(rule, null, 10)).extracting(RuleTuningLogEntity::getId).containsExactly(entry.getId());
        assertThatThrownBy(() -> tuning.apply(rule, suggestion.getId(), null, "token:t"))
                .isInstanceOf(ConflictException.class);

        RuleTuningLogEntity undone = tuning.undo(rule, entry.getId(), "token:u");
        assertThat(undone.getUndoneBy()).isEqualTo("token:u");
        assertThat(rules.load(rule).getSeverity()).isEqualTo("warning");
        assertThat(suggestions.findById(suggestion.getId()).orElseThrow().getStatus())
                .isEqualTo(TuningSuggestionEntity.OPEN);
        assertThatThrownBy(() -> tuning.undo(rule, entry.getId(), "token:u")).isInstanceOf(ConflictException.class);

        RuleTuningLogEntity second = tuning.apply(rule, suggestion.getId(), null, "token:t");
        Map<String, Object> input = revisions.input(rule);
        input.put("name", "edited later");
        rules.update(rule, JSON.valueToTree(input));
        assertThatThrownBy(() -> tuning.undo(rule, second.getId(), "token:u"))
                .isInstanceOf(ConflictException.class);
        assertThat(rules.load(rule).getSeverity()).isEqualTo("critical");
    }

    @Test
    @DisplayName("dismiss marks the suggestion DISMISSED with a reason; a second dismissal is 409")
    void dismiss() {
        UUID rule = seeded();
        TuningSuggestionEntity suggestion = severityUp(rule);
        TuningSuggestionEntity dismissed = tuning.dismiss(rule, suggestion.getId(), "wanted as is", "token:t");
        assertThat(dismissed.getStatus()).isEqualTo(TuningSuggestionEntity.DISMISSED);
        assertThat(dismissed.getDecisionReason()).isEqualTo("wanted as is");
        assertThat(severityUp(rule).getStatus()).as("regeneration leaves a dismissal alone")
                .isEqualTo(TuningSuggestionEntity.DISMISSED);
        assertThatThrownBy(() -> tuning.dismiss(rule, suggestion.getId(), null, "token:t"))
                .isInstanceOf(ConflictException.class);
        assertThat(TuningWorkflowService.view(dismissed)).containsEntry("status", "DISMISSED")
                .containsEntry("ruleId", rule.toString());
    }
}
