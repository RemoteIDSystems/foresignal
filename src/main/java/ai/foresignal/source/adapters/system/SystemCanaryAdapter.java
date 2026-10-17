package ai.foresignal.source.adapters.system;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.PushAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * The built-in {@code system-canary} PUSH source (prompt 019): the watchdog's opt-in canary submits
 * one synthetic record to it and requires its change on the change bus within 30 s, proving the
 * ingest path (queue, writer, commit, publish) end to end.
 *
 * <p>STANDARD with a 1-day default retention; hidden from tiles. Its one entity
 * ({@value #EXTERNAL_ID}) changes on every run, so every run persists a version.
 */
@Component
public class SystemCanaryAdapter implements PushAdapter {

    /** The source id. */
    public static final String ID = "system-canary";

    /** The one kind. */
    public static final String KIND = "system-canary";

    /** The one entity. */
    public static final String EXTERNAL_ID = "canary";

    private static final SourceDescriptor DESCRIPTOR = SourceDescriptor.builder(ID)
            .name("System canary")
            .description("Synthetic records submitted by the watchdog's ingest canary to prove the ingest path end"
                    + " to end. Nothing else writes here.")
            .kind(KIND)
            .attribution("Foresignal")
            // Foresignal's own synthetic data: the operator-owned terms (local only), so it resolves to catalog
            // terms instead of the restrictive unknown ones (prompt 109, checkpoint 124).
            .licence("operator-owned")
            .mode(Mode.PUSH)
            .defaultBudget(Budget.of(60, 10_000))
            .historyTier(HistoryTier.STANDARD)
            .defaultIngestPolicy(IngestPolicy.all())
            .defaultRetention(Duration.ofDays(1))
            .build();

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }
}
