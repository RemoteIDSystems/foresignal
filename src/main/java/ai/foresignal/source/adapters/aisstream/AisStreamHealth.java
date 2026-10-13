package ai.foresignal.source.adapters.aisstream;

import ai.foresignal.collect.HealthDetailsContributor;
import ai.foresignal.collect.HealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * aisstream's health (prompt 063): the report-fast half of the silence watchdog, and the health details.
 *
 * <ul>
 *   <li><strong>Rule:</strong> {@code degraded}, "connected but silent for N s", once the open connection
 *       has gone {@link AisStreamAdapter#SILENCE_REPORT} (120 s) without a recognized AIS envelope. The
 *       connection itself is recycled only at {@link AisStreamAdapter#SILENCE_RECYCLE} (300 s), by the
 *       session's watchdog: a dead feed reads as dead quickly, but recovery never becomes a reconnect cycle
 *       against aisstream's connection limits.
 *   <li><strong>Details:</strong> {@code compressionEnabled} (from the subscription confirmation),
 *       {@code perMessageDeflate} and {@code vessels}, in {@code SourceHealth.details}.
 * </ul>
 */
@Component
public class AisStreamHealth implements HealthRule, HealthDetailsContributor {

    private final AisStreamAdapter adapter;

    /** Creates the rule for {@code adapter}. */
    public AisStreamHealth(AisStreamAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        if (!AisStreamAdapter.ID.equals(context.sourceId())) {
            return Optional.empty();
        }
        Optional<Duration> silent = adapter.silentFor(context.now());
        if (silent.isEmpty() || silent.get().compareTo(AisStreamAdapter.SILENCE_REPORT) < 0) {
            return Optional.empty();
        }
        return Optional.of(HealthVerdict.degraded("connected but silent for " + silent.get().toSeconds() + " s", null));
    }

    @Override
    public Map<String, Object> details(SourceDescriptor descriptor) {
        if (!AisStreamAdapter.ID.equals(descriptor.id())) {
            return Map.of();
        }
        return adapter.healthDetails();
    }
}
