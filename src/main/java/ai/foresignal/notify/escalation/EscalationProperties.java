package ai.foresignal.notify.escalation;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of the escalation worker ({@code foresignal.escalation.*}, prompt 033).
 *
 * @param scheduled whether the worker polls on its own; tests turn it off and call {@code runOnce()}
 * @param pollInterval how often due escalations are claimed (15 s)
 * @param batchSize the most escalations one poll claims
 */
@ConfigurationProperties("foresignal.escalation")
public record EscalationProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("15s") Duration pollInterval,
        @DefaultValue("50") int batchSize) {

    /** Fills missing values. */
    public EscalationProperties {
        pollInterval = pollInterval == null ? Duration.ofSeconds(15) : pollInterval;
        batchSize = batchSize < 1 ? 50 : batchSize;
    }
}
