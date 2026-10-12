package ai.foresignal.trigger;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.settings.TriggerSettings;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.Subscription;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Evaluates trigger rules against persisted versions and records alerts (prompt 017, task 3;
 * Architecture §8.3, Core-Data-Model §11).
 *
 * <p><b>Loop.</b> One {@code ChangeBus} subscription (queue 1,024) delivers batches on its own
 * virtual thread. The durable cursor {@code app_setting['trigger.cursor']} is the last seq
 * evaluated; batches with {@code toSeq ≤ cursor} are ignored, and anything between the cursor and a
 * batch (a start, an {@code onGap}, rolled-back allocations) is caught up from the database in
 * 10,000-seq chunks. A batch that follows the cursor directly is evaluated only with the rules whose
 * source and kind filters can match it.
 *
 * <p><b>One chunk.</b> Each rule's statement ({@link RuleSqlBuilder}) runs in its own read
 * transaction under the statement timeout, followed by its auto-resolve statement; a rule that fails
 * is logged, gets {@code last_error}, and is skipped without stalling the others. {@link AlertPass}
 * then decides, in seq order, what to record. Finally one transaction ({@link AlertStore#write})
 * inserts the alerts, resolves the inverse edges, updates storm counters and advances the cursor, so
 * every (rule, version) is evaluated exactly once, including across restarts. Events are published
 * after commit; storm events go out as {@link RuleStormEvent}s.
 */
@Component
public class TriggerEngine implements ChangeListener, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(TriggerEngine.class);

    /** Seqs per catch-up chunk. */
    public static final long CHUNK = 10_000;

    /** The engine's change-bus queue. */
    public static final int QUEUE = 1_024;

    static final String READ_CURSOR = "SELECT value::text FROM app_setting WHERE key = ?";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final ChangeBus changeBus;
    private final TriggerRuleRepository repository;
    private final RuleSpecs specs;
    private final ReadQueryRunner reads;
    private final AlertStore alerts;
    private final AppSettingService settings;
    private final SourceRegistry registry;
    private final ApplicationEventPublisher events;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<UUID, AlertPass.State> states = new ConcurrentHashMap<>();
    private volatile List<RuleSpec> rules = List.of();
    private volatile Subscription subscription;
    private long cursor = -1;

    /** Creates the engine. */
    public TriggerEngine(
            ChangeBus changeBus,
            TriggerRuleRepository repository,
            RuleSpecs specs,
            ReadQueryRunner reads,
            AlertStore alerts,
            AppSettingService settings,
            SourceRegistry registry,
            ApplicationEventPublisher events,
            JdbcTemplate jdbc,
            JsonMapper json,
            Clock clock) {
        this.changeBus = changeBus;
        this.repository = repository;
        this.specs = specs;
        this.reads = reads;
        this.alerts = alerts;
        this.settings = settings;
        this.registry = registry;
        this.events = events;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Loads the rules and the cursor, subscribes, and catches up to the bus watermark. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.TRIGGER_ENGINE)
    public synchronized void start() {
        if (subscription != null) {
            return;
        }
        reloadRules();
        cursor = loadCursor();
        subscription = changeBus.subscribe("trigger-engine", this, QUEUE);
        catchUp();
        log.info("Trigger engine started at cursor {} with {} enabled rules", cursor, rules.size());
    }

    /** Unsubscribes; the cursor stays where the last committed chunk left it. */
    @PreDestroy
    public synchronized void stop() {
        Subscription current = subscription;
        subscription = null;
        if (current != null) {
            current.close();
        }
    }

    /** True while subscribed. */
    public boolean running() {
        return subscription != null;
    }

    /** Re-reads the cursor and the rules, and forgets per-rule state (the test cleaner calls it). */
    @Override
    public synchronized void resetState() {
        states.clear();
        reloadRules();
        cursor = loadCursor();
    }

    /** The last evaluated seq. */
    public synchronized long cursor() {
        return cursor;
    }

    /** Rebuilds the rule list after a change and drops the changed rule's pass state. */
    @EventListener
    public void onRulesChanged(TriggerRulesChangedEvent event) {
        if (event.ruleId() == null) {
            states.clear();
        } else {
            states.remove(event.ruleId());
        }
        reloadRules();
    }

    private void reloadRules() {
        List<RuleSpec> compiled = new ArrayList<>();
        for (TriggerRuleEntity rule : repository.findByEnabledTrue()) {
            try {
                compiled.add(specs.compile(rule));
            } catch (RuntimeException e) {
                log.error("Trigger rule {} cannot be compiled and is skipped: {}", rule.getId(), e.getMessage());
                recordError(rule.getId(), e);
            }
        }
        rules = List.copyOf(compiled);
    }

    // ------------------------------------------------------------------------- bus

    @Override
    public synchronized void onBatch(ChangeBatch batch) {
        if (cursor < 0 || batch.toSeq() <= cursor) {
            return;
        }
        if (batch.fromSeq() - 1 > cursor) {
            evaluate(cursor, batch.toSeq(), rules);
            return;
        }
        Set<String> kinds = new HashSet<>();
        for (ChangeSummary change : batch.changes()) {
            kinds.add(change.kind());
        }
        List<RuleSpec> applicable = new ArrayList<>();
        for (RuleSpec rule : rules) {
            if (rule.watchesSource(batch.sourceId()) && (rule.kinds().isEmpty() || kinds.stream().anyMatch(rule::watchesKind))) {
                applicable.add(rule);
            }
        }
        evaluate(cursor, batch.toSeq(), applicable);
    }

    @Override
    public synchronized void onGap(long lastDeliveredSeq) {
        catchUp();
    }

    /** Evaluates everything between the cursor and the bus watermark. */
    public synchronized void catchUp() {
        if (cursor < 0) {
            cursor = loadCursor();
        }
        long head = changeBus.lastPublishedSeq();
        if (head > cursor) {
            evaluate(cursor, head, rules);
        }
    }

    private void evaluate(long from, long to, List<RuleSpec> candidates) {
        long lo = from;
        while (lo < to) {
            long hi = Math.min(to, lo + CHUNK);
            processChunk(lo, hi, candidates);
            lo = hi;
        }
    }

    // ------------------------------------------------------------------------- one chunk

    /** One rule's decisions for a chunk. */
    private record Plan(RuleSpec rule, AlertPass.Result result) {}

    private void processChunk(long from, long to, List<RuleSpec> candidates) {
        Instant now = clock.instant();
        int defaultCap = settings.getOrDefault(SettingKeys.TRIGGERS, TriggerSettings.class, TriggerSettings::defaults)
                .defaultMaxAlertsPerHour();
        List<Plan> plans = new ArrayList<>();
        for (RuleSpec rule : candidates) {
            try {
                Plan plan = plan(rule, from, to, now, defaultCap);
                if (plan != null) {
                    plans.add(plan);
                }
            } catch (RuntimeException e) {
                log.error("Trigger rule {} failed on seq {}..{} and is skipped: {}", rule.id(), from + 1, to, e.getMessage());
                recordError(rule.id(), e);
            }
        }
        List<RuleStormEvent> storms = new ArrayList<>();
        boolean disabledAny = alerts.write(writer -> {
            boolean disabled = false;
            for (Plan plan : plans) {
                disabled |= apply(writer, plan, now, storms);
            }
            settings.put(SettingKeys.TRIGGER_CURSOR, to);
            return disabled;
        });
        cursor = to;
        for (RuleStormEvent storm : storms) {
            events.publishEvent(storm);
        }
        if (disabledAny) {
            events.publishEvent(new TriggerRulesChangedEvent(null));
        }
    }

    private Plan plan(RuleSpec rule, long from, long to, Instant now, int defaultCap) {
        RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, from, to, now, true);
        List<MatchRow> matches = reads.read(t -> t.query(sql.sql(), sql.params(), MatchRow.MAPPER));
        List<ResolveRow> resolves = List.of();
        if (rule.autoResolve() != null && rule.autoResolve().needsEdgePass() && rule.mode() != TriggerMode.EXIT) {
            RuleSqlBuilder.RuleSql inverse = RuleSqlBuilder.inverse(rule, from, to);
            resolves = reads.read(t -> t.query(inverse.sql(), inverse.params(), ResolveRow.MAPPER));
        }
        if (matches.isEmpty() && resolves.isEmpty()) {
            return null;
        }
        AlertPass.State state = states.computeIfAbsent(rule.id(), id -> new AlertPass.State());
        if (!state.stormSeeded()) {
            state.seedStorm(alerts.firedSince(rule.id(), now.minus(AlertPass.STORM_WINDOW)));
        }
        Set<String> snoozed = matches.isEmpty() ? Set.of() : alerts.snoozedEntities(rule.id(), now);
        Map<String, String> seeds = rule.hysteresis() ? edgeSeeds(rule, matches) : Map.of();
        AlertPass.Context context = new AlertPass.Context(now, snoozed, defaultCap, false, seeds::get);
        return new Plan(rule, AlertPass.run(rule, matches, resolves, state, context));
    }

    /** The edge state each entity's latest unsuppressed alert implies, for hysteresis rules. */
    private Map<String, String> edgeSeeds(RuleSpec rule, List<MatchRow> matches) {
        Set<String> externalIds = new HashSet<>();
        for (MatchRow row : matches) {
            externalIds.add(row.externalId());
        }
        Map<String, String> seeds = new HashMap<>();
        reads.read(t -> {
            t.query("""
                    SELECT DISTINCT ON (source_id, external_id) source_id, external_id, state FROM alert
                     WHERE rule_id = :rule AND NOT suppressed AND external_id = ANY(CAST(:ids AS text[]))
                     ORDER BY source_id, external_id, id DESC""",
                    new MapSqlParameterSource("rule", rule.id())
                            .addValue("ids", ai.foresignal.store.query.PgArrays.text(new ArrayList<>(externalIds))),
                    rs -> {
                        String edge = AlertPass.impliedEdge(rule.mode(), !"resolved".equals(rs.getString(3)));
                        if (edge != null) {
                            seeds.put(rs.getString(1) + '\u0000' + rs.getString(2), edge);
                        }
                    });
            return null;
        });
        return seeds;
    }

    private boolean apply(AlertStore.Writer writer, Plan plan, Instant now, List<RuleStormEvent> storms) {
        RuleSpec rule = plan.rule();
        AlertPass.Result result = plan.result();
        for (AlertPass.Op op : result.ops()) {
            switch (op) {
                case AlertPass.Fire fire -> insert(writer, rule, fire, now);
                case AlertPass.Resolve resolve -> writer.resolveOpen(rule.id(), resolve.row().sourceId(),
                        resolve.row().externalId(), resolve.row().seq(), resolve.row().resolvedBy(), resolve.row().seq(), now);
            }
        }
        if (result.stormSuppressed() == 0) {
            return false;
        }
        Long total = writer.jdbc().queryForObject(
                "UPDATE trigger_rule SET suppressed_count = suppressed_count + :n, last_storm_at = :now"
                        + (result.disable() ? ", enabled = false" : "") + " WHERE id = :id RETURNING suppressed_count",
                new MapSqlParameterSource("n", result.stormSuppressed())
                        .addValue("now", AlertStore.utc(now))
                        .addValue("id", rule.id()),
                Long.class);
        if (result.stormEventDue()) {
            storms.add(new RuleStormEvent(rule.id(), rule.name(), now, rule.stormCap(defaultCap()),
                    total == null ? 0 : total, result.disable()));
        }
        if (result.disable()) {
            log.warn("Trigger rule {} ('{}') hit its storm cap and was disabled", rule.id(), rule.name());
        }
        return result.disable();
    }

    private int defaultCap() {
        return settings.getOrDefault(SettingKeys.TRIGGERS, TriggerSettings.class, TriggerSettings::defaults)
                .defaultMaxAlertsPerHour();
    }

    private void insert(AlertStore.Writer writer, RuleSpec rule, AlertPass.Fire fire, Instant now) {
        MatchRow row = fire.row();
        Map<String, Object> payload = row.payload() == null ? null : json.readValue(row.payload(), MAP);
        String title = registry.find(row.sourceId())
                .map(r -> TitleResolver.resolve(titleField(r.descriptor()), payload))
                .orElseGet(() -> TitleResolver.resolve((String) null, payload));
        String message = MessageTemplate.render(rule.template(), new MessageTemplate.Context(
                rule.name(), row.sourceId(), row.externalId(), row.kind(), title, rule.severity().wire(),
                row.observedAt(), row.eventTime(), row.validFrom(), payload));
        String snapshot = AlertSnapshots.build(row, payload, json);
        writer.insert(new AlertStore.NewAlert(rule, row, now, message, snapshot, fire.suppressedReason()));
    }

    private static String titleField(SourceDescriptor descriptor) {
        return descriptor.titleField();
    }

    // ------------------------------------------------------------------------- cursor and errors

    private long loadCursor() {
        List<String> values = jdbc.queryForList(READ_CURSOR, String.class, SettingKeys.TRIGGER_CURSOR);
        if (!values.isEmpty() && values.getFirst() != null) {
            try {
                return Long.parseLong(values.getFirst().strip());
            } catch (NumberFormatException e) {
                log.warn("Ignoring an unreadable trigger cursor '{}'", values.getFirst());
            }
        }
        // A fresh installation starts at the current head: rules evaluate what arrives from now on.
        Long max = jdbc.queryForObject("SELECT coalesce(max(seq), 0) FROM record_version", Long.class);
        long start = max == null ? 0 : max;
        settings.put(SettingKeys.TRIGGER_CURSOR, start);
        return start;
    }

    private void recordError(UUID ruleId, RuntimeException e) {
        try {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            jdbc.update("UPDATE trigger_rule SET last_error = ?, last_error_at = ? WHERE id = ?",
                    message.length() > 2000 ? message.substring(0, 2000) : message,
                    AlertStore.utc(clock.instant()), ruleId);
        } catch (RuntimeException inner) {
            log.warn("Could not record the error of trigger rule {}: {}", ruleId, inner.getMessage());
        }
    }
}
