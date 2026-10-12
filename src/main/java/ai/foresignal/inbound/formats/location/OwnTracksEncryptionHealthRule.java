package ai.foresignal.inbound.formats.location;

import ai.foresignal.collect.HealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthVerdict;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Degrades a source that received OwnTracks encrypted payloads (prompt 039): Foresignal cannot read
 * them, so the operator must turn payload encryption off in the app. The warning holds for
 * {@link #WINDOW} after the last encrypted payload, and is in memory only (a restart clears it until
 * the next one arrives).
 */
@Component
public class OwnTracksEncryptionHealthRule implements HealthRule {

    /** The health warning text. */
    public static final String WARNING = "OwnTracks payload encryption is not supported; disable it";

    /** How long one encrypted payload keeps the source degraded. */
    public static final Duration WINDOW = Duration.ofHours(24);

    private final Map<String, Instant> lastEncrypted = new ConcurrentHashMap<>();

    /** Notes that {@code sourceId} received an encrypted payload at {@code at}. */
    public void recordEncrypted(String sourceId, Instant at) {
        if (sourceId != null && at != null) {
            lastEncrypted.merge(sourceId, at, (a, b) -> a.isAfter(b) ? a : b);
        }
    }

    /** When {@code sourceId} last received an encrypted payload, if ever since start. */
    public Optional<Instant> lastEncrypted(String sourceId) {
        return Optional.ofNullable(lastEncrypted.get(sourceId));
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        Instant last = lastEncrypted.get(context.sourceId());
        if (last == null || context.now() == null || last.plus(WINDOW).isBefore(context.now())) {
            return Optional.empty();
        }
        return Optional.of(HealthVerdict.degraded(WARNING, null));
    }
}
