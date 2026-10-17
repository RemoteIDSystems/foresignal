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
 * Resolves {@code forecast:{id}} (prompt 129): one ledger forecast with its forecaster (the issuer), probability, ICD
 * 203 term and knowledge cutoff. It follows its question's visibility.
 */
@Component
public class ForecastObjectResolver implements ObjectResolver {

    private final ForecastRepository forecasts;
    private final ForecastQuestionRepository questions;

    /** Creates the resolver. */
    public ForecastObjectResolver(ForecastRepository forecasts, ForecastQuestionRepository questions) {
        this.forecasts = forecasts;
        this.questions = questions;
    }

    @Override
    public String type() {
        return ObjectTypes.FORECAST;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return forecasts.findById(ref.id()).filter(f -> context.existedAt(f.getMadeAt())).flatMap(f -> questions
                .findById(f.getQuestionId())
                .filter(q -> !q.isPrivateQuestion() || context.seesPrivate())
                .map(q -> {
                    StringBuilder s = new StringBuilder(f.getForecasterId());
                    if (f.getProbability() != null) {
                        s.append(": ").append(Math.round(f.getProbability() * 100)).append("% (")
                                .append(EstimativeLanguage.term(f.getProbability())).append(')');
                    }
                    if (f.getKnowledgeCutoff() != null) {
                        s.append(", knowledge cutoff ").append(f.getKnowledgeCutoff());
                    }
                    return ObjectEnvelope.builder(ref)
                            .title("Forecast on " + q.getTitle())
                            .summary(s.toString())
                            .status(q.getStatus())
                            .time(f.getMadeAt(), null, f.getMadeAt())
                            .privateObject(q.isPrivateQuestion())
                            .provenance(List.of(), f.getManifestId(), f.getAsOfSeq())
                            .self("/v1/forecasts/questions/" + q.getId() + "/forecasts")
                            .build();
                }));
    }
}
