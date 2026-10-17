package ai.foresignal.trigger.window;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * {@code rule-windows} (prompt 100): the DERIVED source that owns the {@code window-state} and
 * {@code anomaly} records. Its {@code onTick} evaluates the enabled windowed rules through
 * {@link WindowEngine}, and it tombstones the records of a rule that was deleted.
 *
 * <p>It makes no upstream request, has no {@code recordTtl}, emits only on change and reconciles
 * from its own heads at startup. The step-aligned driving is {@link WindowScheduler}'s.
 */
@Component
public class RuleWindowsGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = WindowEngine.SOURCE_ID;

    /** The window-state kind. */
    public static final String STATE_KIND = WindowEngine.STATE_KIND;

    /** The anomaly kind. */
    public static final String ANOMALY_KIND = WindowEngine.ANOMALY_KIND;

    private final SourceDescriptor descriptor = buildDescriptor();
    private final WindowEngine engine;
    private final Set<UUID> knownRules = new LinkedHashSet<>();

    /** Creates the generator. */
    public RuleWindowsGenerator(@Lazy WindowEngine engine) {
        this.engine = engine;
    }

    /** The descriptor. */
    public static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Windowed rules and anomalies (derived)")
                .description("The state of count, absence and anomaly rules, and their evaluated anomaly"
                        + " buckets, as ordinary records so rules, SSE and later layers use them unchanged.")
                .kinds(STATE_KIND, ANOMALY_KIND)
                .attribution("Derived by Foresignal from stored versions and live rows")
                .licence("derived")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(90))
                .param(ParamSpec.optional("step", ParamType.DURATION,
                        "How often due windowed rules are evaluated", Duration.ofSeconds(30)))
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(), Set.of(), Set.of(), Duration.ofSeconds(30), false);
    }

    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        knownRules.clear();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        WindowEngine.Emitter emitter = new WindowEngine.Emitter() {
            @Override
            public void emit(NormalizedRecord record) {
                ctx.emit(record);
            }

            @Override
            public void tombstone(String kind, String externalId) {
                ctx.tombstone(kind, externalId);
            }
        };
        Set<UUID> current = new LinkedHashSet<>();
        for (WindowEngine.WindowRule rule : engine.enabled()) {
            current.add(rule.spec().id());
        }
        for (UUID gone : new LinkedHashSet<>(knownRules)) {
            if (!current.contains(gone)) {
                engine.forget(gone, emitter);
            }
        }
        knownRules.clear();
        knownRules.addAll(current);
        engine.evaluate(now, emitter);
    }
}
