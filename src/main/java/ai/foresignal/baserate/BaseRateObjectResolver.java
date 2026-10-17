package ai.foresignal.baserate;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Resolves {@code base-rate:{id}} (prompt 129): a 121 base rate with its window, rate and period. */
@Component
public class BaseRateObjectResolver implements ObjectResolver {

    private final BaseRateRepository rates;

    /** Creates the resolver. */
    public BaseRateObjectResolver(BaseRateRepository rates) {
        this.rates = rates;
    }

    @Override
    public String type() {
        return ObjectTypes.BASE_RATE;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return rates.findById(ref.id()).filter(r -> context.existedAt(r.getComputedAt())).map(r -> ObjectEnvelope
                .builder(ref)
                .title(r.getName())
                .summary(String.format(java.util.Locale.ROOT, "P(at least one per %s) = %.3f over %d windows",
                        r.getWindow(), r.getPAtLeastOne(), r.getNWindows()))
                .time(r.getPeriodFrom() == null ? null : r.getPeriodFrom().atStartOfDay().toInstant(ZoneOffset.UTC),
                        r.getPeriodTo() == null ? null : r.getPeriodTo().atStartOfDay().toInstant(ZoneOffset.UTC),
                        r.getComputedAt())
                .provenance(List.of(), r.getManifestId(), null)
                .self("/v1/base-rates/" + r.getId())
                .build());
    }
}
