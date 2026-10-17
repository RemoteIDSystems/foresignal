package ai.foresignal.situation;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The self-refreshing situation brief (prompt 118 §5) with a scripted fake provider: typed claims are kept and only
 * citations of packet items or tool results survive; refreshes are debounced and capped per day; a failure keeps the
 * previous brief; the brief never changes members, status or severity; with briefs off nothing is called and an
 * explicit request is 409 {@code LLM_NOT_CONFIGURED}.
 */
@IntegrationTest
@Import(AskTestSources.class)
class SituationBriefIT extends AskItSupport {

    static final String QUAKES = AskTestSources.QUAKES;

    @Autowired SituationService service;
    @Autowired SituationBriefService briefs;
    @Autowired SituationLogRepository logs;
    @Autowired SituationMemberRepository members;

    String situationId;
    long seq;

    @BeforeEach
    void situation() {
        settings.put(SettingKeys.SITUATION_BRIEFS_ENABLED, true);
        quake("q1", "Off the coast", 6.1, 45.0, 12.0);
        seq = headSeq(QUAKES, "q1");
        situationId = service.create(JSON.readTree("{\"title\":\"Coastal quake\",\"theme\":\"weather\",\"members\":["
                + "{\"memberType\":\"record\",\"ref\":\"" + QUAKES + "/q1\",\"seq\":" + seq + "}]}"), "tester",
                "operator", false).getId();
    }

    String note(String extraCitation) {
        return "{\"title\":\"Brief\",\"body\":\"A strong quake off the coast.\",\"priority\":\"normal\",\"claims\":["
                + "{\"text\":\"A magnitude 6.1 quake\",\"basis\":\"evidence\",\"citations\":[\"" + QUAKES + ":q1@" + seq
                + "\",\"" + extraCitation + "\"]},"
                + "{\"text\":\"Aftershocks may follow\",\"basis\":\"speculation\",\"citations\":[]}]}";
    }

    @Test
    @DisplayName("typed claims are stored with valid citations only; members, status and severity are unchanged")
    void typedClaimsAndCitations() {
        SituationEntity before = service.get(situationId);
        chat().then(toolUse(call("c1", "record_note", note("bogus:x@1")))).then(text("done"));

        assertThat(briefs.generate(situationId, true)).isEqualTo(SituationBriefService.Outcome.STORED);

        SituationEntity after = service.get(situationId);
        assertThat(after.getBrief()).contains("**evidence:** A magnitude 6.1 quake [" + QUAKES + ":q1@" + seq + "]")
                .contains("**speculation:** Aftershocks may follow").doesNotContain("bogus:x@1");
        assertThat(after.getBriefModel()).isNotNull();
        assertThat(after.getBriefGeneratedAt()).isEqualTo(clock.instant());
        assertThat(after.getStatus()).isEqualTo(before.getStatus());
        assertThat(after.getSeverity()).isEqualTo(before.getSeverity());
        assertThat(members.findBySituationIdOrderByAddedAtAscIdAsc(situationId)).hasSize(1);
        SituationLogEntity row = logs.findBySituationIdOrderByIdAsc(situationId).stream()
                .filter(r -> "brief".equals(r.getAction())).findFirst().orElseThrow();
        assertThat(row.getAfter()).containsEntry("status", "ok").containsEntry("citationsDropped", 1);
        assertThat(row.getBefore()).containsEntry("brief", null);
        // The packet reached the model delimited as data, with the member's citation form.
        String sent = everythingSent(chat().requests());
        assertThat(sent).contains("\"tool\":\"get_situation\"").contains(QUAKES + ":q1@" + seq);
    }

    @Test
    @DisplayName("a refresh within briefMinInterval is debounced; after it a new brief is written")
    void debounce() {
        chat().then(toolUse(call("c1", "record_note", note("x")))).then(text("done"))
                .then(toolUse(call("c2", "record_note", note("x")))).then(text("done"));
        assertThat(briefs.generate(situationId, true)).isEqualTo(SituationBriefService.Outcome.STORED);
        int calls = chat().requests().size();

        clock.setInstant(clock.instant().plus(Duration.ofMinutes(10)));
        assertThat(briefs.generate(situationId, false)).isEqualTo(SituationBriefService.Outcome.DEBOUNCED);
        assertThat(chat().requests()).hasSize(calls);
        assertThat(service.get(situationId).isBriefPending()).isTrue();

        clock.setInstant(clock.instant().plus(Duration.ofMinutes(25)));
        assertThat(briefs.retryPending()).isEqualTo(1);
        assertThat(service.get(situationId).isBriefPending()).isFalse();
        assertThat(chat().requests().size()).isGreaterThan(calls);
    }

    @Test
    @DisplayName("the daily cap stops further briefs that day")
    void dailyCap() {
        settings.put(SettingKeys.SITUATION_BRIEFS_MAX_PER_DAY, 1);
        chat().then(toolUse(call("c1", "record_note", note("x")))).then(text("done"));
        assertThat(briefs.generate(situationId, true)).isEqualTo(SituationBriefService.Outcome.STORED);
        int calls = chat().requests().size();
        assertThat(briefs.generate(situationId, true)).isEqualTo(SituationBriefService.Outcome.CAPPED);
        assertThat(chat().requests()).hasSize(calls);
        assertThatThrownBy(() -> briefs.request(situationId, false)).isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.LLM_BUDGET_EXCEEDED);
    }

    @Test
    @DisplayName("a failure keeps the previous brief and logs it")
    void failureKeepsPrevious() {
        chat().then(toolUse(call("c1", "record_note", note("x")))).then(text("done"));
        assertThat(briefs.generate(situationId, true)).isEqualTo(SituationBriefService.Outcome.STORED);
        String previous = service.get(situationId).getBrief();

        chat().then(request -> {
            throw new IllegalStateException("provider down");
        });
        clock.setInstant(clock.instant().plus(Duration.ofHours(1)));
        assertThat(briefs.generate(situationId, false)).isEqualTo(SituationBriefService.Outcome.FAILED);
        assertThat(service.get(situationId).getBrief()).isEqualTo(previous);
        List<SituationLogEntity> rows = logs.findBySituationIdOrderByIdAsc(situationId).stream()
                .filter(r -> "brief".equals(r.getAction())).toList();
        assertThat(rows.getLast().getAfter()).containsEntry("status", "failed");
    }

    @Test
    @DisplayName("with briefs off nothing is called and an explicit request is 409 LLM_NOT_CONFIGURED")
    void disabled() {
        settings.put(SettingKeys.SITUATION_BRIEFS_ENABLED, false);
        assertThat(briefs.generate(situationId, true)).isEqualTo(SituationBriefService.Outcome.DISABLED);
        briefs.consider(situationId, true, true, 10);
        assertThat(chat().requests()).isEmpty();
        assertThatThrownBy(() -> briefs.request(situationId, false)).isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.LLM_NOT_CONFIGURED);
    }
}
