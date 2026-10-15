package ai.foresignal.trigger.window;

import ai.foresignal.derive.DerivedRunner;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.SourceSettingsRepository;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Drives the windowed-rule evaluator (prompt 100). When any windowed rule is enabled it ensures the
 * {@code rule-windows} DERIVED source is enabled, then asks the DERIVED runtime for an evaluation
 * tick; the runtime runs {@link RuleWindowsGenerator#onTick}, which evaluates the due rules.
 *
 * <p>This replaces the prompt's own virtual-thread loop with the 052 runtime's per-source thread; the
 * cadence is the {@code rule-windows} tick (30 s), and each rule is throttled to its own
 * {@code step} by {@link WindowEngine}.
 */
@Component
public class WindowScheduler {

    private final TriggerRuleRepository rules;
    private final SourceService sources;
    private final SourceSettingsRepository settings;
    private final DerivedRunner runner;

    /** Creates the scheduler. */
    public WindowScheduler(TriggerRuleRepository rules, SourceService sources, SourceSettingsRepository settings,
            DerivedRunner runner) {
        this.rules = rules;
        this.sources = sources;
        this.settings = settings;
        this.runner = runner;
    }

    /** Checks for windowed rules and drives an evaluation tick. */
    @Scheduled(fixedDelayString = "PT5S", initialDelayString = "PT10S")
    public void tick() {
        boolean any = false;
        for (TriggerRuleEntity rule : rules.findByEnabledTrue()) {
            if (WindowEngine.windowed(rule)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        ensureEnabled();
        if (runner.isStarted() && runner.running().contains(RuleWindowsGenerator.ID)) {
            runner.tickNow(RuleWindowsGenerator.ID);
        }
    }

    private void ensureEnabled() {
        if (!settings.existsById(RuleWindowsGenerator.ID)
                || !settings.findById(RuleWindowsGenerator.ID).map(s -> s.isEnabled()).orElse(false)) {
            sources.update(RuleWindowsGenerator.ID,
                    new SourceSettingsUpdate(true, null, java.util.Map.of(), Budget.of(1, 1), null, false, null, false));
        }
    }
}
