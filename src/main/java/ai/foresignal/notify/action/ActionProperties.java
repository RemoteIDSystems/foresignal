package ai.foresignal.notify.action;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of the action pollers ({@code foresignal.actions.*}, prompt 033).
 *
 * @param scheduled whether the Telegram, Matrix and Pushover pollers run on their own; tests turn it
 *     off and call {@code pollOnce}
 * @param longPoll the long-poll timeout of Telegram {@code getUpdates} and Matrix {@code /sync}; must
 *     stay below the channel request timeout (15 s)
 * @param receiptInterval how often Pushover receipts are polled (Pushover asks for at least 5 s)
 * @param reconcileInterval how often the set of polled channels is reconciled with the channels
 */
@ConfigurationProperties("foresignal.actions")
public record ActionProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("10s") Duration longPoll,
        @DefaultValue("60s") Duration receiptInterval,
        @DefaultValue("30s") Duration reconcileInterval) {

    /** Fills missing values and keeps the receipt interval at least 5 s. */
    public ActionProperties {
        longPoll = longPoll == null ? Duration.ofSeconds(10) : longPoll;
        receiptInterval = receiptInterval == null || receiptInterval.compareTo(Duration.ofSeconds(5)) < 0
                ? Duration.ofSeconds(60) : receiptInterval;
        reconcileInterval = reconcileInterval == null ? Duration.ofSeconds(30) : reconcileInterval;
    }
}
