package ai.foresignal.source.adapters.ais;

import ai.foresignal.collect.HealthDetailsContributor;
import ai.foresignal.collect.HealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The health of the keyless vessel sources (prompt 085).
 *
 * <ul>
 *   <li><strong>Rule:</strong> {@code ais-digitraffic} is {@code degraded} while its {@code vessels-v2/status}
 *       {@code updated} is more than 10 minutes old (Digitraffic's own coverage signal). Unreachable hosts need no rule:
 *       a failing connect already makes the source {@code failing}.
 *   <li><strong>Details</strong> ({@code SourceHealth.details}): {@code messagesPerMin}, {@code distinctMmsi10m},
 *       {@code lastMessageAt} and {@code vessels} for {@code ais-kystverket}, {@code ais-digitraffic} and every
 *       {@code local-ais-{n}} slot (the station statistics of an AIS-catcher) and {@code local-ais-aggregator}
 *       (prompt R2-020), plus Digitraffic's drop counters and status time.
 * </ul>
 */
@Component
public class AisSourcesHealth implements HealthRule, HealthDetailsContributor {

    private final KystverketAdapter kystverket;
    private final DigitrafficAisAdapter digitraffic;
    private final LocalAisFamily local;
    private final LocalAisAggregatorAdapter aggregator;
    private final Clock clock;

    /**
     * Creates the rule and contributor.
     *
     * @param kystverket the Kystverket source
     * @param digitraffic the Digitraffic source
     * @param local the AIS-catcher slots
     * @param aggregator the AIS aggregator's read (prompt R2-020)
     * @param clock the clock of the statistics windows
     */
    public AisSourcesHealth(KystverketAdapter kystverket, DigitrafficAisAdapter digitraffic, LocalAisFamily local,
            LocalAisAggregatorAdapter aggregator, Clock clock) {
        this.kystverket = kystverket;
        this.digitraffic = digitraffic;
        this.local = local;
        this.aggregator = aggregator;
        this.clock = clock;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        if (!DigitrafficAisAdapter.ID.equals(context.sourceId())) {
            return Optional.empty();
        }
        return digitraffic.statusProblem(context.now()).map(reason -> HealthVerdict.degraded(reason, null));
    }

    @Override
    public Map<String, Object> details(SourceDescriptor descriptor) {
        String id = descriptor.id();
        if (KystverketAdapter.ID.equals(id)) {
            return kystverket.healthDetails(clock.instant());
        }
        if (DigitrafficAisAdapter.ID.equals(id)) {
            return digitraffic.healthDetails(clock.instant());
        }
        if (LocalAisAggregatorAdapter.ID.equals(id)) {
            return aggregator.healthDetails(clock.instant());
        }
        if (id.startsWith(LocalAisAdapter.ID_PREFIX)) {
            return local.find(id).map(a -> a.healthDetails(clock.instant())).orElse(Map.of());
        }
        return Map.of();
    }
}
