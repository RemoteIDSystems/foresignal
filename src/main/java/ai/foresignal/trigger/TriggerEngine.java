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
 *
 * <p><b>Prompt 036.</b> Anchored ("near me") rules evaluate their target side with the anchor's
 * current position from {@link AnchorPositions} (a stale anchor suspends them), and after each batch
 * and on a 5 s tick the {@link AnchorEvaluator} runs their anchor side; those alerts go through the
 * same {@link AlertPass} but advance no cursor and write no record versions. Durable-cursor catch-up
 * after a restart uses the current anchor position. Alerts carry {@code context.near} (place ids and
 * versions) and {@code context.anchor} (anchor seq and km-rounded distance); templates get
 * {@code {{place.name}}}, {@code {{place.distanceKm}}} and {@code {{anchor.distanceKm}}}, never
 * coordinates.
 *
 * <p><b>Prompt 056.</b> Before the transaction that inserts a chunk's alerts, the geolocated ones get
 * {@code context.weather} from {@link ai.foresignal.weather.AlertWeatherContext} (in parallel, each within its
 * 500 ms budget; a failure stores {@code {status: unavailable}}), unless the rule's {@code weatherContext} or the
 * global {@code weather.alertContext} is {@code off}. The context is stored with the alert before any event is
 * published, and its fields are template variables ({@code {{context.weather.summary}}} …).
 *
 * <p><b>Prompt 114.</b> A rule with {@code approach} conditions runs {@link RuleSqlBuilder#approachCandidates}
 * instead of its match statement, and {@link ApproachMatches} judges the approach on both sides of each version in
 * Java ({@code ai.foresignal.kinematics.ApproachEvaluator}) before the usual edge rules; its cooldown is applied by
 * the same Java seed the anchor side uses. Edge resolution is the poll or flush resolution of the sources.
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
    private final AnchorPositions anchors;
    private final AnchorEvaluator anchorEvaluator;
    private final org.springframework.beans.factory.ObjectProvider<PlaceLookup> places;

    /** Region tags for {@code {{near.*}}} and the alert's region snapshot (prompt 040); null in unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.reference.RegionTagger regionTagger;

    /** Alert weather context (prompt 056); null in unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.weather.AlertWeatherContext alertWeather;

    /** The {@code approach} condition evaluator (prompt 114); absent in unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.beans.factory.ObjectProvider<ai.foresignal.kinematics.ApproachEvaluator> approachEvaluator;

    /** Rules whose {@code weatherContext} is {@code off} (prompt 056). */
    private volatile Set<UUID> weatherOff = Set.of();
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
            Clock clock,
            AnchorPositions anchors,
            AnchorEvaluator anchorEvaluator,
            org.springframework.beans.factory.ObjectProvider<PlaceLookup> places) {
        this.anchors = anchors;
        this.anchorEvaluator = anchorEvaluator;
        this.places = places;
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
        Set<UUID> off = new HashSet<>();
        for (TriggerRuleEntity rule : repository.findByEnabledTrue()) {
            if (!ai.foresignal.trigger.window.RuleType.recordEngine(rule.getType())) {
                // Count, absence and anomaly rules are evaluated by the window engine (prompt 100), never
                // per version: their stored mode defaults to match, which would fire on every version.
                continue;
            }
            if ("off".equals(rule.getWeatherContext())) {
                off.add(rule.getId());
            }
            try {
                compiled.add(specs.compile(rule));
            } catch (RuntimeException e) {
                log.error("Trigger rule {} cannot be compiled and is skipped: {}", rule.getId(), e.getMessage());
                recordError(rule.getId(), e);
            }
        }
        rules = List.copyOf(compiled);
        weatherOff = Set.copyOf(off);
        anchors.watch(compiled.stream().filter(RuleSpec::anchored).map(RuleSpec::anchor).toList());
    }

    // ------------------------------------------------------------------------- bus

    @Override
    public synchronized void onBatch(ChangeBatch batch) {
        anchors.apply(batch);
        if (cursor < 0 || batch.toSeq() <= cursor) {
            return;
        }
        if (batch.fromSeq() - 1 > cursor) {
            evaluate(cursor, batch.toSeq(), rules);
            runAnchors();
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
        runAnchors();
    }

    /** The anchor side of moving-anchor rules, every 5 s (live-only anchor moves and staleness). */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 5_000, initialDelay = 5_000)
    public synchronized void anchorTick() {
        if (subscription != null) {
            runAnchors();
        }
    }

    /**
     * Runs the {@link AnchorEvaluator} for the anchors that changed and records its alerts (prompt
     * 036). Public so tests on a mutable clock can drive it.
     */
    public synchronized void runAnchors() {
        if (rules.stream().noneMatch(RuleSpec::anchored)) {
            anchors.drainDirty();
            return;
        }
        Instant now = clock.instant();
        List<AnchorEvaluator.Outcome> outcomes = anchorEvaluator.evaluate(rules, anchors.drainDirty(), now);
        if (outcomes.isEmpty()) {
            return;
        }
        int defaultCap = defaultCap();
        List<Plan> plans = new ArrayList<>();
        for (AnchorEvaluator.Outcome outcome : outcomes) {
            RuleSpec rule = outcome.rule();
            try {
                List<MatchRow> rows = withoutCooldown(rule, outcome.rows());
                if (rows.isEmpty()) {
                    continue;
                }
                AlertPass.State state = states.computeIfAbsent(rule.id(), id -> new AlertPass.State());
                if (!state.stormSeeded()) {
                    state.seedStorm(alerts.firedSince(rule.id(), now.minus(AlertPass.STORM_WINDOW)));
                }
                Set<String> snoozed = alerts.snoozedEntities(rule.id(), now);
                AlertPass.Context context = new AlertPass.Context(now, snoozed, defaultCap, false, key -> null);
                plans.add(new Plan(rule, AlertPass.run(rule, rows, List.of(), state, context),
                        new AnchorContext(outcome.anchorSeq(), outcome.anchor())));
            } catch (RuntimeException e) {
                log.error("Anchor alerts of rule {} failed: {}", rule.id(), e.getMessage());
                recordError(rule.id(), e);
            }
        }
        if (plans.isEmpty()) {
            return;
        }
        List<RuleStormEvent> storms = new ArrayList<>();
        Map<AlertPass.Fire, Map<String, Object>> weather = weather(plans);
        boolean disabledAny = alerts.write(writer -> {
            boolean disabled = false;
            for (Plan plan : plans) {
                disabled |= apply(writer, plan, now, storms, weather);
            }
            return disabled;
        });
        for (RuleStormEvent storm : storms) {
            events.publishEvent(storm);
        }
        if (disabledAny) {
            events.publishEvent(new TriggerRulesChangedEvent(null));
        }
    }

    /** Drops reverse rows inside the rule's cooldown of an alert already stored (the SQL seed's role). */
    private List<MatchRow> withoutCooldown(RuleSpec rule, List<MatchRow> rows) {
        if (rule.cooldown().isZero() || rows.isEmpty()) {
            return rows;
        }
        List<MatchRow> out = new ArrayList<>();
        for (MatchRow row : rows) {
            Object since = AlertStore.utc(row.observedAt().minus(rule.cooldown()));
            Boolean recent = rule.cooldownScope() == CooldownScope.RECORD
                    ? jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM alert WHERE rule_id = ? AND source_id = ?"
                            + " AND external_id = ? AND NOT suppressed AND NOT exercise AND observed_at > ?)", Boolean.class,
                            rule.id(), row.sourceId(), row.externalId(), since)
                    : jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM alert WHERE rule_id = ? AND NOT suppressed"
                            + " AND NOT exercise AND observed_at > ?)", Boolean.class, rule.id(), since);
            if (!Boolean.TRUE.equals(recent)) {
                out.add(row);
            }
        }
        return out;
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

    /**
     * One rule's decisions for a chunk.
     *
     * @param rule the rule
     * @param result the pass
     * @param anchor the anchor's seq and position for an anchored rule, or null
     */
    private record Plan(RuleSpec rule, AlertPass.Result result, AnchorContext anchor) {}

    /**
     * The anchor an anchored rule's alerts were judged against.
     *
     * @param seq the anchor's latest seq
     * @param point its position (never leaves the server: only distances are rendered)
     */
    private record AnchorContext(long seq, org.locationtech.jts.geom.Point point) {}

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
        Map<AlertPass.Fire, Map<String, Object>> weather = weather(plans);
        boolean disabledAny = alerts.write(writer -> {
            boolean disabled = false;
            for (Plan plan : plans) {
                disabled |= apply(writer, plan, now, storms, weather);
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
        org.locationtech.jts.geom.Point anchor = null;
        AnchorContext anchorContext = null;
        if (rule.anchored()) {
            anchor = anchors.current(rule.anchor(), now);
            if (anchor == null) {
                // A stale or unknown anchor suspends the rule (the evaluator records why).
                return null;
            }
            long seq = anchors.fix(rule.anchor().source(), rule.anchor().externalId())
                    .map(AnchorPositions.Fix::seq).orElse(0L);
            anchorContext = new AnchorContext(seq, anchor);
        }
        List<MatchRow> matches;
        List<ResolveRow> resolves = List.of();
        if (rule.hasApproach()) {
            // Prompt 114: approach conditions are judged in Java over the candidate versions; the cooldown seed
            // the SQL would add is applied by withoutCooldown.
            RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.approachCandidates(rule, from, to, now, anchor);
            List<ApproachMatches.Candidate> candidates =
                    reads.read(t -> t.query(sql.sql(), sql.params(), ApproachMatches.MAPPER));
            ApproachMatches.Result result = ApproachMatches.evaluate(rule, candidates, approachJudge());
            matches = withoutCooldown(rule, result.matches());
            resolves = result.resolves();
        } else {
            RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, from, to, now, true, anchor);
            matches = reads.read(t -> t.query(sql.sql(), sql.params(), MatchRow.MAPPER));
            if (rule.autoResolve() != null && rule.autoResolve().needsEdgePass() && rule.mode() != TriggerMode.EXIT) {
                RuleSqlBuilder.RuleSql inverse = RuleSqlBuilder.inverse(rule, from, to, anchor);
                resolves = reads.read(t -> t.query(inverse.sql(), inverse.params(), ResolveRow.MAPPER));
            }
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
        return new Plan(rule, AlertPass.run(rule, matches, resolves, state, context), anchorContext);
    }

    /** The approach judge of one evaluation pass (prompt 114); every approach is ambiguous without an evaluator. */
    private ApproachMatches.Judge approachJudge() {
        ai.foresignal.kinematics.ApproachEvaluator evaluator =
                approachEvaluator == null ? null : approachEvaluator.getIfAvailable();
        return evaluator == null ? ApproachMatches.UNKNOWN : evaluator.session()::evaluate;
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
                     WHERE rule_id = :rule AND NOT suppressed AND NOT exercise AND external_id = ANY(CAST(:ids AS text[]))
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

    /**
     * The weather contexts of the plans' unsuppressed, geolocated fires (prompt 056), computed before the insert
     * transaction; empty when alert weather context is unavailable or off.
     */
    private Map<AlertPass.Fire, Map<String, Object>> weather(List<Plan> plans) {
        ai.foresignal.weather.AlertWeatherContext service = alertWeather;
        if (service == null) {
            return Map.of();
        }
        List<AlertPass.Fire> fires = new ArrayList<>();
        List<ai.foresignal.weather.AlertWeatherContext.Job> jobs = new ArrayList<>();
        Boolean enabled = null;
        for (Plan plan : plans) {
            if (weatherOff.contains(plan.rule().id())) {
                continue;
            }
            for (AlertPass.Op op : plan.result().ops()) {
                if (op instanceof AlertPass.Fire fire && fire.suppressedReason() == null && fire.row().geometry() != null
                        && !fire.row().geometry().isBlank()) {
                    if (enabled == null) {
                        enabled = service.enabledFor(null);
                    }
                    if (!enabled) {
                        return Map.of();
                    }
                    fires.add(fire);
                    jobs.add(new ai.foresignal.weather.AlertWeatherContext.Job(fire.row().geometry(),
                            fire.row().observedAt()));
                }
            }
        }
        if (jobs.isEmpty()) {
            return Map.of();
        }
        try {
            List<Map<String, Object>> contexts = service.computeAll(jobs);
            Map<AlertPass.Fire, Map<String, Object>> out = new java.util.IdentityHashMap<>();
            for (int i = 0; i < fires.size() && i < contexts.size(); i++) {
                out.put(fires.get(i), contexts.get(i));
            }
            return out;
        } catch (RuntimeException e) {
            log.warn("Alert weather contexts failed; the alerts are stored without them: {}", e.getMessage());
            return Map.of();
        }
    }

    private boolean apply(AlertStore.Writer writer, Plan plan, Instant now, List<RuleStormEvent> storms,
            Map<AlertPass.Fire, Map<String, Object>> weather) {
        RuleSpec rule = plan.rule();
        AlertPass.Result result = plan.result();
        for (AlertPass.Op op : result.ops()) {
            switch (op) {
                case AlertPass.Fire fire -> insert(writer, rule, fire, now, plan.anchor(), weather.get(fire));
                case AlertPass.Resolve resolve -> writer.resolveOpen(rule.id(), resolve.row().sourceId(),
                        resolve.row().externalId(), resolve.row().seq(), resolve.row().resolvedBy(), resolve.row().seq(), now);
            }
        }
        if (result.cooldownSuppressed() > 0 || result.stormSuppressed() > 0) {
            // Prompt 115: the daily alert-quality trend counts the matches the cooldown and the rate cap dropped.
            UUID ruleId = rule.id();
            int byCooldown = result.cooldownSuppressed();
            int byRateCap = result.stormSuppressed();
            writer.afterCommit(() -> countSuppressed(ruleId, byCooldown, byRateCap, now));
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

    /** Adds suppressed matches to the rule's {@code rule_quality_daily} row of today (UTC); never fails a pass. */
    private void countSuppressed(UUID ruleId, int byCooldown, int byRateCap, Instant now) {
        try {
            jdbc.update(ai.foresignal.quality.RuleQualityDailyStore.COUNT_SUPPRESSED, ruleId,
                    java.sql.Date.valueOf(now.atZone(java.time.ZoneOffset.UTC).toLocalDate()), byCooldown, byRateCap,
                    java.sql.Timestamp.from(now));
        } catch (RuntimeException e) {
            log.debug("Could not count suppressed matches of rule {}: {}", ruleId, e.toString());
        }
    }

    private int defaultCap() {
        return settings.getOrDefault(SettingKeys.TRIGGERS, TriggerSettings.class, TriggerSettings::defaults)
                .defaultMaxAlertsPerHour();
    }

    private void insert(AlertStore.Writer writer, RuleSpec rule, AlertPass.Fire fire, Instant now,
            AnchorContext anchor, Map<String, Object> weather) {
        MatchRow row = fire.row();
        Map<String, Object> payload = row.payload() == null ? null : json.readValue(row.payload(), MAP);
        String title = registry.find(row.sourceId())
                .map(r -> TitleResolver.resolve(titleField(r.descriptor()), payload))
                .orElseGet(() -> TitleResolver.resolve((String) null, payload));
        AlertContexts.Built context = AlertContexts.build(rule, row, anchor == null ? null : anchor.point(),
                anchor == null ? null : anchor.seq(), places.getIfAvailable());
        ai.foresignal.reference.RegionTags region = region(row);
        Map<String, Object> contextJson = context.json();
        Map<String, String> vars = context.vars();
        if (weather != null) {
            // Prompt 056: context.weather, stored with the alert and usable in the rule's template.
            contextJson = contextJson == null ? new java.util.LinkedHashMap<>() : new java.util.LinkedHashMap<>(contextJson);
            contextJson.put("weather", weather);
            vars = new HashMap<>(vars);
            vars.putAll(ai.foresignal.weather.WeatherTemplateVars.of(weather));
        }
        String message = MessageTemplate.render(rule.template(), new MessageTemplate.Context(
                rule.name(), row.sourceId(), row.externalId(), row.kind(), title, rule.severity().wire(),
                row.observedAt(), row.eventTime(), row.validFrom(), payload, region),
                new MessageTemplate.Extras(java.time.ZoneOffset.UTC, now, vars, null));
        String snapshot = AlertSnapshots.build(row, payload, json);
        writer.insert(new AlertStore.NewAlert(rule, row, now, message, snapshot, fire.suppressedReason(),
                contextJson == null ? null : json.writeValueAsString(contextJson),
                region == null ? null : json.writeValueAsString(region.toMap())));
    }

    /**
     * The region tags of the firing version (prompt 040): lazy tagging at the version's geometry, stored in
     * {@code record_region} in a transaction of its own and snapshotted on the alert; null without a geometry,
     * for a source whose tagging is off, or when tagging fails (an alert never fails on its region).
     */
    private ai.foresignal.reference.RegionTags region(MatchRow row) {
        if (regionTagger == null || row.geometry() == null) {
            return null;
        }
        try {
            org.locationtech.jts.geom.Geometry geometry =
                    new ai.foresignal.source.formats.geojson.GeoJsonReader(1_000_000, 8).read(row.geometry());
            ai.foresignal.reference.RegionTags tags = regionTagger.tagRecord(row.sourceId(), row.externalId(), geometry);
            return tags == null || tags.isEmpty() ? null : tags;
        } catch (RuntimeException e) {
            log.debug("No region tags for {}:{}: {}", row.sourceId(), row.externalId(), e.getMessage());
            return null;
        }
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
