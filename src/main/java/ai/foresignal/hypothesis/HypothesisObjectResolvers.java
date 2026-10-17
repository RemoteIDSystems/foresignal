package ai.foresignal.hypothesis;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The resolvers of prompt 125's objects (prompt 129): {@code hypothesis-set:{id}}, {@code hypothesis:{id}} and
 * {@code indicator:{id}}. Hypotheses and indicators follow their set's visibility; private sets are invisible to
 * callers without private access.
 */
@Configuration
public class HypothesisObjectResolvers {

    /** {@code hypothesis-set:{id}}. */
    @Bean
    public ObjectResolver hypothesisSetObjectResolver(HypothesisSetRepository sets) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.HYPOTHESIS_SET;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return visibleSet(sets, ref.id(), context).map(s -> ObjectEnvelope.builder(ref)
                        .title(s.getQuestion())
                        .summary(s.getDescription())
                        .status(s.getStatus())
                        .time(s.getCreatedAt(), null, s.getLastRevisionAt())
                        .privateObject(s.isPrivateSet())
                        .self("/v1/hypotheses/sets/" + s.getId())
                        .build());
            }
        };
    }

    /** {@code hypothesis:{id}}. */
    @Bean
    public ObjectResolver hypothesisObjectResolver(HypothesisRepository hypotheses, HypothesisSetRepository sets) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.HYPOTHESIS;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return hypotheses.findById(ref.id()).filter(h -> context.existedAt(h.getCreatedAt()))
                        .flatMap(h -> visibleSet(sets, h.getSetId(), context).map(s -> ObjectEnvelope.builder(ref)
                                .title(h.getLabel() + ": " + h.getStatement())
                                .summary(Math.round(h.getProbability() * 100) + "% (" + h.getEstimativeTerm()
                                        + ") in " + s.getQuestion())
                                .status(h.getStatus())
                                .time(h.getCreatedAt(), null, h.getUpdatedAt())
                                .privateObject(s.isPrivateSet())
                                .self("/v1/hypotheses/sets/" + s.getId())
                                .build()));
            }
        };
    }

    /** {@code indicator:{id}}. */
    @Bean
    public ObjectResolver indicatorObjectResolver(IndicatorRepository indicators, HypothesisSetRepository sets) {
        return new ObjectResolver() {
            @Override
            public String type() {
                return ObjectTypes.INDICATOR;
            }

            @Override
            public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
                return indicators.findById(ref.id()).filter(i -> context.existedAt(i.getCreatedAt()))
                        .flatMap(i -> visibleSet(sets, i.getSetId(), context).map(s -> ObjectEnvelope.builder(ref)
                                .title(i.getDescription())
                                .summary("Indicator for " + s.getQuestion())
                                .status(i.getState())
                                .time(i.getCreatedAt(), i.getExpectedBy(), i.getLastStateChangeAt())
                                .privateObject(s.isPrivateSet())
                                .self("/v1/indicators?setId=" + s.getId())
                                .build()));
            }
        };
    }

    static Optional<HypothesisSetEntity> visibleSet(HypothesisSetRepository sets, String id, ResolveContext context) {
        return sets.findById(id).filter(s -> !s.isPrivateSet() || context.seesPrivate())
                .filter(s -> context.existedAt(s.getCreatedAt()));
    }
}
