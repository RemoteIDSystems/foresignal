package ai.foresignal.cycle;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * The wording rules on generated assessments (prompt 128, LED-3): a key judgment that mixes a likelihood term with a
 * confidence level (and names the wrong term for its number) is regenerated once; when the regeneration still fails
 * it is downgraded with a warning; when it passes it is kept. Numbers and terms agree in the final text, and the
 * confidence statement never carries a likelihood term.
 */
@IntegrationTest
@Import(AskTestSources.class)
class AssessmentLintIT extends CycleItSupport {

    static final String FAILING = "A coalition response is very likely with high confidence.";

    ForecastQuestionEntity seed() {
        ForecastQuestionEntity q = question("Coalition strike on Houthi targets", null, null);
        clock.setInstant(clock.instant().plus(Duration.ofMinutes(1)));
        return q;
    }

    void script(String regenerated) {
        fakes.client(CHAT).otherwise((ChatRequest request) -> {
            if (request.tools().isEmpty()) {
                return text(regenerated);
            }
            if (turnsSinceUser(request) > 0) {
                return text("done");
            }
            String q = idAfter(lastUser(request), "question:");
            return toolUse(call("a1", CycleOutputTools.RECORD_JUDGMENTS, "{\"judgments\":[{\"text\":\"" + FAILING
                    + "\",\"probability\":0.62,\"confidenceStatement\":\"Confidence is high; a strike is likely.\","
                    + "\"citations\":[\"question:" + q + "\"],\"questionId\":\"" + q + "\"}]}"));
        });
    }

    long regenerations() {
        return fakes.client(CHAT).requests().stream().filter(r -> r.tools().isEmpty()).count();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("regenerated once, still failing, then downgraded with a warning; number and term agree")
    void downgraded() {
        seed();
        script("A response is almost certain with moderate confidence.");
        CycleRunEntity run = runNow(cycle("daily", "[\"analyst\"]", profile(null).getId(), null));
        AssessmentEntity a = assessmentRows.findById(run.getAssessmentId()).orElseThrow();
        assertThat(regenerations()).isEqualTo(1);
        Map<String, Object> j = a.getBluf().get(0);
        String text = String.valueOf(j.get("text"));
        assertThat(j).containsEntry("downgraded", true).containsEntry("estimativeTerm", "likely");
        assertThat(text).isEqualTo("A response is likely.");
        assertThat(EstimativeLanguage.lint(text, List.of(new EstimativeLanguage.Claim(0.62, 0, text.length()))))
                .isEmpty();
        assertThat((List<String>) j.get("warnings")).anyMatch(w -> w.contains("still fails"))
                .anyMatch(w -> w.startsWith("downgraded"));
        assertThat(String.valueOf(j.get("confidenceStatement"))).doesNotContain("likely");
        assertThat(a.getLintWarnings()).isNotEmpty();
        assertThat(a.getMarkdown()).contains("A response is likely. Probability: 62% (likely).");
    }

    @Test
    @DisplayName("a regeneration that passes the lint is kept")
    void regenerated() {
        seed();
        script("A coalition response is likely.");
        CycleRunEntity run = runNow(cycle("daily", "[\"analyst\"]", profile(null).getId(), null));
        Map<String, Object> j = assessmentRows.findById(run.getAssessmentId()).orElseThrow().getBluf().get(0);
        assertThat(regenerations()).isEqualTo(1);
        assertThat(j).containsEntry("text", "A coalition response is likely.").containsEntry("downgraded", false);
    }
}
