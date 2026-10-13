package ai.foresignal.chatops;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of interactive chat channels ({@code foresignal.chatops.*}, prompt 061).
 *
 * @param scheduled whether connectors start on their own; tests turn it off and drive
 *     {@link ChatConnector#start} and {@link ChatConnector#poll} themselves
 * @param asyncCommands whether commands run on their own virtual threads (a slow {@code /ask} does not hold up
 *     the poll); tests turn it off so a poll returns after its replies
 * @param longPoll the long-poll timeout of Telegram {@code getUpdates} and Matrix {@code /sync} (50 s)
 * @param initialBackoff the first wait after a failed poll (1 s), doubling up to {@code maxBackoff}
 * @param maxBackoff the longest wait between attempts (5 min), also used while a connector is blocked
 * @param replyWait how long {@code /brief run} waits before replying "running" (30 s)
 * @param staleAfter Telegram messages older than this are ignored (2 min)
 * @param reconcileInterval how often running connectors are reconciled with the stored channels
 */
@ConfigurationProperties("foresignal.chatops")
public record ChatopsProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("true") boolean asyncCommands,
        @DefaultValue("50s") Duration longPoll,
        @DefaultValue("1s") Duration initialBackoff,
        @DefaultValue("5m") Duration maxBackoff,
        @DefaultValue("30s") Duration replyWait,
        @DefaultValue("2m") Duration staleAfter,
        @DefaultValue("30s") Duration reconcileInterval) {

    /** Fills missing values. */
    public ChatopsProperties {
        longPoll = longPoll == null ? Duration.ofSeconds(50) : longPoll;
        initialBackoff = initialBackoff == null ? Duration.ofSeconds(1) : initialBackoff;
        maxBackoff = maxBackoff == null ? Duration.ofMinutes(5) : maxBackoff;
        replyWait = replyWait == null ? Duration.ofSeconds(30) : replyWait;
        staleAfter = staleAfter == null ? Duration.ofMinutes(2) : staleAfter;
        reconcileInterval = reconcileInterval == null ? Duration.ofSeconds(30) : reconcileInterval;
    }

    /** The shipped settings. */
    public static ChatopsProperties defaults() {
        return new ChatopsProperties(true, true, null, null, null, null, null, null);
    }
}
