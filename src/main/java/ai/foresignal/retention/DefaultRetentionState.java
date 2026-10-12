package ai.foresignal.retention;

import ai.foresignal.ingest.SourceIngestProfiles;
import java.time.Duration;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The {@link RetentionState} until prompt 019 publishes real values: nothing has been purged
 * ({@code purgeSeq} and {@code lowestRetainedSeq} are 0), and the window is the one the ingest
 * profile already resolves (TRACK: {@code retention.trackWindow}; STANDARD: the source setting, else
 * the descriptor's {@code defaultRetention}, else {@code retention.default}, as
 * {@code SourceService.effectiveRetention} resolves it).
 */
@Component
public class DefaultRetentionState implements RetentionState {

    private final SourceIngestProfiles profiles;

    /** Creates the state over the ingest profiles. */
    public DefaultRetentionState(SourceIngestProfiles profiles) {
        this.profiles = profiles;
    }

    @Override
    public long purgeSeq(String sourceId) {
        return 0L;
    }

    @Override
    public long lowestRetainedSeq(Set<String> sources) {
        return 0L;
    }

    @Override
    public Duration window(String sourceId) {
        return profiles.get(sourceId).window();
    }
}
