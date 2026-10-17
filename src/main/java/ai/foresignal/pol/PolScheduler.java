package ai.foresignal.pol;

import ai.foresignal.derive.DerivedRunner;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.SourceSettingsRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps the pattern-of-life sources running while subjects exist (prompt 114): every minute, each of
 * {@code pattern-of-life} and {@code pattern-of-life-private} is enabled when an enabled subject of its class
 * exists; the DERIVED runtime then runs its 60-second tick ({@link PolGenerator#onTick}). {@link #kick} enables and
 * ticks a source at once after an API change. Off when {@code foresignal.pol.scheduled} is false (tests drive
 * {@link PolEngine#tick} themselves).
 */
@Component
public class PolScheduler {

    private final PolSubjectRepository subjects;
    private final SourceService sources;
    private final SourceSettingsRepository settings;
    private final DerivedRunner runner;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public PolScheduler(PolSubjectRepository subjects, SourceService sources, SourceSettingsRepository settings,
            DerivedRunner runner, @Value("${foresignal.pol.scheduled:true}") boolean scheduled) {
        this.subjects = subjects;
        this.sources = sources;
        this.settings = settings;
        this.runner = runner;
        this.scheduled = scheduled;
    }

    /** The periodic check. */
    @Scheduled(fixedDelayString = "PT60S", initialDelayString = "PT20S")
    public void scheduledCheck() {
        if (scheduled) {
            ensure();
        }
    }

    /** Enables each source whose class has an enabled subject. */
    public void ensure() {
        if (!subjects.findByEnabledTrueAndPrivateSubject(false).isEmpty()) {
            ensureEnabled(PolGenerator.ID);
        }
        if (!subjects.findByEnabledTrueAndPrivateSubject(true).isEmpty()) {
            ensureEnabled(PolPrivateGenerator.ID);
        }
    }

    /** Enables and ticks both sources now (after a subject was created, deleted or asked to recompute). */
    public void kick() {
        if (!scheduled) {
            return;
        }
        ensure();
        for (String id : new String[] {PolGenerator.ID, PolPrivateGenerator.ID}) {
            if (runner.isStarted() && runner.running().contains(id)) {
                runner.tickNow(id);
            }
        }
    }

    private void ensureEnabled(String id) {
        if (!settings.findById(id).map(s -> s.isEnabled()).orElse(false)) {
            sources.update(id, new SourceSettingsUpdate(true, null, Map.of(), Budget.of(1, 1), null, false, null, false));
        }
    }
}
