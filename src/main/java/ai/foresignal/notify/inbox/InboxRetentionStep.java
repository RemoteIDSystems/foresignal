package ai.foresignal.notify.inbox;

import ai.foresignal.notify.action.ActionService;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Retention of the inbox and action tables (prompt 033), after the operational tables:
 *
 * <ul>
 *   <li>{@code notification}: rows older than the {@code inbox} window (default 30 days) with no
 *       delivery left; sparse reads of deleted notifications; read cursors of devices unseen for 90
 *       days ({@link InboxService#purge});
 *   <li>{@code client_presence}: rows expired for more than a day;
 *   <li>{@code action_capability}: capabilities expired for more than {@value #CAPABILITY_KEEP_DAYS}
 *       days (kept that long for the audit trail of their results).
 * </ul>
 */
@Component
public class InboxRetentionStep implements RetentionStep {

    /** Days an expired capability is kept. */
    static final int CAPABILITY_KEEP_DAYS = 7;

    private final InboxService inbox;
    private final PresenceService presence;
    private final ActionService actions;

    /** Creates the step. */
    public InboxRetentionStep(InboxService inbox, PresenceService presence, ActionService actions) {
        this.inbox = inbox;
        this.presence = presence;
        this.actions = actions;
    }

    @Override
    public String name() {
        return "inbox";
    }

    @Override
    public int order() {
        return 710;
    }

    @Override
    public void run(RetentionRun run) {
        for (Map.Entry<String, Integer> entry : inbox.purge(run.now()).entrySet()) {
            run.deleted(entry.getKey(), entry.getValue());
        }
        run.deleted("client_presence", presence.purgeExpired(run.now().minus(Duration.ofDays(1))));
        run.deleted("action_capability", actions.purgeExpired(run.now().minus(Duration.ofDays(CAPABILITY_KEEP_DAYS))));
    }
}
