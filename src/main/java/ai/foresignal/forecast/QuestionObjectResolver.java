package ai.foresignal.forecast;

import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code question:{id}} (prompt 129): a 122 ledger question with its ensemble probability and ICD 203 term in
 * the summary. Private questions are invisible to callers without private access.
 */
@Component
public class QuestionObjectResolver implements ObjectResolver {

    private final ForecastQuestionRepository questions;

    /** Creates the resolver. */
    public QuestionObjectResolver(ForecastQuestionRepository questions) {
        this.questions = questions;
    }

    @Override
    public String type() {
        return ObjectTypes.QUESTION;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return questions.findById(ref.id())
                .filter(q -> !q.isPrivateQuestion() || context.seesPrivate())
                .filter(q -> context.existedAt(q.getCreatedAt()))
                .map(q -> ObjectEnvelope.builder(ref)
                        .title(q.getTitle())
                        .summary(summary(q.getEnsembleProbability()))
                        .status(q.getStatus())
                        .time(q.getOpensAt(), q.getClosesAt(), q.getUpdatedAt())
                        .privateObject(q.isPrivateQuestion())
                        .provenance(List.of(), q.getManifestId(), null)
                        .self("/v1/forecasts/questions/" + q.getId())
                        .build());
    }

    /** The ensemble line of a question summary, or null without a probability. */
    static String summary(Double p) {
        if (p == null) {
            return null;
        }
        return "foresignal-ensemble: " + Math.round(p * 100) + "% (" + EstimativeLanguage.term(p) + ")";
    }
}
