package ai.foresignal.exercise;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.notify.ChannelTypeRegistry;
import ai.foresignal.notify.NotificationChannel;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.AlertContexts;
import ai.foresignal.trigger.AlertSnapshots;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.MessageTemplate;
import ai.foresignal.trigger.PlaceLookup;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exercise replay runs (prompt 115): a past window of retained history replayed through selected rules to
 * opted-in channels as clearly marked exercise alerts, so the operator can rehearse the alert chain end to end
 * without touching real alert state.
 *
 * <ul>
 *   <li><b>Bounds:</b> times translate to seqs through {@code seq_clock} ({@code S(T)}); a window starting below
 *       any involved source's retained floor is refused with {@code 400 REPLAY_BEYOND_RETENTION}, naming the
 *       source and its earliest replayable time. The window is at most {@code exercise.maxWindow} (7 d), and one
 *       run is active at a time ({@code 409 EXERCISE_RUNNING}).
 *   <li><b>Evaluation:</b> {@link ExerciseReplay} (the 103 engine in run-local state).
 *   <li><b>Pacing and delivery:</b> on a virtual thread, matches are emitted in seq order with the
 *       {@link ExerciseSpeed} pacing ({@link ReplayPacer}), cancellation checked between matches. Each match
 *       becomes an alert with {@code exercise=true}, {@code exercise_run_id}, the version's {@code observed_at}
 *       and record snapshot, published on the alert bus and delivered by the normal 018 pipeline, which routes it
 *       only to channels with {@code acceptExercise} or named in the run ({@link ExerciseMarking#routes}).
 *   <li><b>Dry run:</b> the projected counts per rule and channel, nothing delivered or stored.
 *   <li><b>Lifecycle:</b> {@link #purge} deletes a run's alerts and notifications; {@link ExerciseRetentionStep}
 *       expires exercise alerts after {@code exercise.alertRetention} (7 d). Start, cancel and purge are audited,
 *       and a {@code system} notification (itself marked exercise) announces the start and the finish.
 * </ul>
 */
@Service
public class ExerciseService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseService.class);

    /** The default longest window. */
    public static final Duration DEFAULT_MAX_WINDOW = Duration.ofDays(7);

    /** The default exercise alert retention. */
    public static final Duration DEFAULT_ALERT_RETENTION = Duration.ofDays(7);

    /** The audit target type of exercise runs. */
    public static final String AUDIT_TARGET = "exercise";

    /** The default speed. */
    public static final ExerciseSpeed DEFAULT_SPEED = ExerciseSpeed.X60;

    /** The longest run name. */
    public static final int MAX_NAME = 200;

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final ExerciseRunRepository runs;
    private final ExerciseReplay replay;
    private final TriggerRuleService rules;
    private final AlertStore alerts;
    private final NotificationService notifications;
    private final NotificationChannelRepository channels;
    private final ChannelTypeRegistry types;
    private final RetentionStateStore retention;
    private final SeqClockQuery seqClock;
    private final ReadQueryRunner reads;
    private final ChangeBus changeBus;
    private final AuditService audit;
    private final AppSettingService settings;
    private final SourceRegistry registry;
    private final ObjectProvider<PlaceLookup> places;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final JsonMapper json;
    private final Clock clock;
    private final Set<String> canceled = ConcurrentHashMap.newKeySet();
    private final Object startLock = new Object();
    private volatile ReplayPacer.Sleeper sleeper = ReplayPacer.THREAD_SLEEP;

    /** Creates the service. */
    public ExerciseService(ExerciseRunRepository runs, ExerciseReplay replay, TriggerRuleService rules,
            AlertStore alerts, NotificationService notifications, NotificationChannelRepository channels,
            ChannelTypeRegistry types, RetentionStateStore retention, SeqClockQuery seqClock, ReadQueryRunner reads,
            ChangeBus changeBus, AuditService audit, AppSettingService settings, SourceRegistry registry,
            ObjectProvider<PlaceLookup> places, NamedParameterJdbcTemplate jdbc,
            PlatformTransactionManager transactions, JsonMapper json, Clock clock) {
        this.runs = runs;
        this.replay = replay;
        this.rules = rules;
        this.alerts = alerts;
        this.notifications = notifications;
        this.channels = channels;
        this.types = types;
        this.retention = retention;
        this.seqClock = seqClock;
        this.reads = reads;
        this.changeBus = changeBus;
        this.audit = audit;
        this.settings = settings;
        this.registry = registry;
        this.places = places;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.json = json;
        this.clock = clock;
    }

    /** Replaces the sleeper of later runs (tests advance a clock instead of sleeping). */
    public void setSleeper(ReplayPacer.Sleeper value) {
        this.sleeper = value == null ? ReplayPacer.THREAD_SLEEP : value;
    }

    /**
     * A validated {@code ExerciseRunCreate}.
     *
     * @param name the name
     * @param rules the rules to replay
     * @param tag the tag the rules were selected by, or null
     * @param fromSeq exclusive lower bound
     * @param toSeq inclusive upper bound
     * @param from the start in time
     * @param to the end in time
     * @param speed the pacing
     * @param channelIds the channels named in the run
     * @param enrich whether 030 enrichment may run
     * @param dryRun true for a projection only
     */
    public record Request(String name, List<TriggerRuleEntity> rules, String tag, long fromSeq, long toSeq, Instant from,
            Instant to, ExerciseSpeed speed, List<String> channelIds, boolean enrich, boolean dryRun) {}

    /**
     * {@code createExercise}: validates, then projects (dry run) or persists and starts the run.
     *
     * @param body the {@code ExerciseRunCreate} body
     * @param actor who asked ({@code token:<name>})
     * @return the run, with {@code dryRun} set for a projection
     * @throws ValidationException {@code 400}, including {@code REPLAY_BEYOND_RETENTION}
     * @throws ConflictException {@code 409 EXERCISE_RUNNING}
     */
    public ExerciseRunEntity create(JsonNode body, String actor) {
        Request request = parse(body);
        if (request.dryRun()) {
            return dryRun(request, actor);
        }
        ExerciseRunEntity run;
        synchronized (startLock) {
            if (runs.existsByStatusIn(List.of(ExerciseRunEntity.PLANNED, ExerciseRunEntity.RUNNING))) {
                throw new ConflictException(ErrorCodes.EXERCISE_RUNNING, "Another exercise is running; one runs at a"
                        + " time. Cancel it or wait for it to finish.");
            }
            run = entity(request, actor, Ulids.next(clock.instant()));
            run = runs.saveAndFlush(run);
        }
        audit(run, "start", actor);
        String id = run.getId();
        Thread.ofVirtual().name("exercise-" + id).start(() -> execute(id));
        return run;
    }

    /** The projection of {@code request}: counts per rule and channel, nothing stored or delivered. */
    ExerciseRunEntity dryRun(Request request, String actor) {
        ExerciseRunEntity run = entity(request, actor, Ulids.next(clock.instant()));
        ExerciseReplay.Plan plan = replay.plan(request.rules(), request.fromSeq(), request.toSeq(), () -> false);
        run.setStats(stats(plan, projection(plan, request.channelIds())));
        return run;
    }

    /** One run, or 404. */
    public ExerciseRunEntity get(String id) {
        return runs.findById(id).orElseThrow(() -> NotFoundException.of("Exercise run", id));
    }

    /**
     * Runs, newest first.
     *
     * @param status only this status, or null
     * @param beforeId only runs with a smaller id (the cursor), or null
     * @param limit the page size
     */
    public List<ExerciseRunEntity> list(String status, String beforeId, int limit) {
        if (status != null && !ExerciseRunEntity.STATUSES.contains(status)) {
            throw ValidationException.forField("status", "must be one of " + String.join(", ",
                    ExerciseRunEntity.STATUSES));
        }
        return runs.findAll().stream()
                .filter(r -> status == null || status.equals(r.getStatus()))
                .filter(r -> beforeId == null || r.getId().compareTo(beforeId) < 0)
                .sorted(Comparator.comparing(ExerciseRunEntity::getId).reversed())
                .limit(limit)
                .toList();
    }

    /**
     * {@code cancelExercise}: stops a planned or running replay between matches.
     *
     * @throws ConflictException {@code 409 EXERCISE_NOT_RUNNING} when the run is finished
     */
    public ExerciseRunEntity cancel(String id, String actor) {
        ExerciseRunEntity run = get(id);
        if (!run.active()) {
            throw new ConflictException(ErrorCodes.EXERCISE_NOT_RUNNING, "Exercise run " + id + " is "
                    + run.getStatus() + "; only a planned or running exercise can be canceled.");
        }
        canceled.add(id);
        run.setStatus(ExerciseRunEntity.CANCELED);
        run.setFinishedAt(clock.instant());
        run = runs.saveAndFlush(run);
        audit(run, "cancel", actor);
        return run;
    }

    /**
     * {@code purgeExerciseAlerts}: deletes the run's exercise alerts and their notifications, deliveries, push
     * dispatches, action capabilities and CAP messages.
     *
     * @return the alerts deleted
     * @throws ConflictException {@code 409 EXERCISE_RUNNING} while the run is still active
     */
    public int purge(String id, String actor) {
        ExerciseRunEntity run = get(id);
        if (run.active()) {
            throw new ConflictException(ErrorCodes.EXERCISE_RUNNING, "Exercise run " + id + " is still "
                    + run.getStatus() + "; cancel it before purging its alerts.");
        }
        int deleted = deleteAlerts("a.exercise AND a.exercise_run_id = :run", new MapSqlParameterSource("run", id));
        audit(run, "purge", actor, Map.of("alertsDeleted", deleted));
        return deleted;
    }

    /**
     * Deletes exercise alerts fired before {@code cutoff} and everything hanging off them (the retention of
     * {@code exercise.alertRetention}).
     *
     * @return the alerts deleted
     */
    public int expire(Instant cutoff) {
        return deleteAlerts("a.exercise AND a.fired_at < :cutoff",
                new MapSqlParameterSource("cutoff", Timestamp.of(cutoff)));
    }

    /** The effective {@code exercise.alertRetention}. */
    public Duration alertRetention() {
        return duration(SettingKeys.EXERCISE_ALERT_RETENTION, DEFAULT_ALERT_RETENTION);
    }

    /** The effective {@code exercise.maxWindow}. */
    public Duration maxWindow() {
        return duration(SettingKeys.EXERCISE_MAX_WINDOW, DEFAULT_MAX_WINDOW);
    }

    /** Runs left planned or running by an earlier process failed with it. */
    @EventListener(ApplicationReadyEvent.class)
    public void failInterrupted() {
        try {
            for (ExerciseRunEntity run : runs.findByStatusIn(List.of(ExerciseRunEntity.PLANNED,
                    ExerciseRunEntity.RUNNING))) {
                Map<String, Object> stats = new LinkedHashMap<>(run.getStats());
                stats.put("error", "Foresignal restarted while the exercise was running.");
                run.setStats(stats);
                run.setStatus(ExerciseRunEntity.FAILED);
                run.setFinishedAt(clock.instant());
                runs.save(run);
            }
        } catch (RuntimeException e) {
            log.warn("Could not fail interrupted exercise runs: {}", e.toString());
        }
    }

    // ------------------------------------------------------------------------- the run

    /**
     * Replays run {@code id} to completion on the calling thread (the virtual thread {@link #create} starts).
     * Does nothing when the run is not planned.
     */
    public void execute(String id) {
        ExerciseRunEntity run = runs.findById(id).orElse(null);
        if (run == null || !ExerciseRunEntity.PLANNED.equals(run.getStatus())) {
            return;
        }
        run.setStatus(ExerciseRunEntity.RUNNING);
        run.setStartedAt(clock.instant());
        run = runs.saveAndFlush(run);
        system(run, "system.exercise_started", "Exercise started: " + run.getName(),
                "Exercise **" + escape(run.getName()) + "** is replaying " + run.getRuleIds().size() + " rule(s).");
        Map<String, Object> stats;
        String status;
        try {
            List<TriggerRuleEntity> selected = loadRules(run.getRuleIds());
            ExerciseReplay.Plan plan = replay.plan(selected, run.getFromSeq(), run.getToSeq(), () -> isCanceled(id));
            Map<String, Integer> projected = projection(plan, run.getChannelIds());
            ExerciseRunEntity current = run;
            AtomicInteger delivered = new AtomicInteger();
            List<ReplayPacer.Paced<ExerciseReplay.Match>> items = new ArrayList<>();
            for (ExerciseReplay.Match match : plan.matches()) {
                items.add(new ReplayPacer.Paced<>(match.ingestedAt(), match));
            }
            ReplayPacer.Outcome outcome = new ReplayPacer(ExerciseSpeed.fromWire(run.getSpeed()), sleeper)
                    .run(items, () -> isCanceled(id), match -> {
                        insert(current, match);
                        delivered.incrementAndGet();
                    });
            stats = stats(plan, projected);
            stats.put("alertsRaised", delivered.get());
            status = outcome.canceled() || isCanceled(id) ? ExerciseRunEntity.CANCELED : ExerciseRunEntity.DONE;
        } catch (RuntimeException e) {
            log.warn("Exercise run {} failed: {}", id, e.toString());
            stats = new LinkedHashMap<>(run.getStats());
            stats.putIfAbsent("versionsScanned", 0L);
            stats.put("error", String.valueOf(e.getMessage()));
            status = ExerciseRunEntity.FAILED;
        }
        ExerciseRunEntity latest = runs.findById(id).orElse(run);
        latest.setStats(stats);
        if (!ExerciseRunEntity.CANCELED.equals(latest.getStatus())) {
            latest.setStatus(status);
        }
        if (latest.getFinishedAt() == null) {
            latest.setFinishedAt(clock.instant());
        }
        latest = runs.saveAndFlush(latest);
        canceled.remove(id);
        system(latest, "system.exercise_finished", "Exercise " + latest.getStatus().toLowerCase(java.util.Locale.ROOT)
                + ": " + latest.getName(), "Exercise **" + escape(latest.getName()) + "** finished with status "
                + latest.getStatus() + ".");
    }

    private boolean isCanceled(String id) {
        if (canceled.contains(id)) {
            return true;
        }
        return runs.findById(id).map(r -> ExerciseRunEntity.CANCELED.equals(r.getStatus())).orElse(true);
    }

    /** Inserts the exercise alert of one match and publishes it on the alert bus. */
    void insert(ExerciseRunEntity run, ExerciseReplay.Match match) {
        RuleSpec rule = match.rule();
        MatchRow row = match.row();
        Map<String, Object> payload = row.payload() == null ? null : json.readValue(row.payload(), MAP);
        String title = registry.find(row.sourceId())
                .map(r -> TitleResolver.resolve(r.descriptor().titleField(), payload))
                .orElseGet(() -> TitleResolver.resolve((String) null, payload));
        AlertContexts.Built built = AlertContexts.build(rule, row, null, null, places.getIfAvailable());
        Instant now = clock.instant();
        String message = MessageTemplate.render(rule.template(), new MessageTemplate.Context(rule.name(),
                row.sourceId(), row.externalId(), row.kind(), title, rule.severity().wire(), row.observedAt(),
                row.eventTime(), row.validFrom(), payload), new MessageTemplate.Extras(ZoneOffset.UTC, now,
                built.vars(), null));
        Map<String, Object> context = built.json() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(built.json());
        context.putAll(exerciseContext(run));
        String snapshot = AlertSnapshots.build(row, payload, json);
        alerts.write(w -> w.insertExercise(new AlertStore.NewAlert(rule, row, now, message, snapshot, null,
                json.writeValueAsString(context), null), run.getId()));
    }

    /** The exercise members of an exercise alert's context. */
    static Map<String, Object> exerciseContext(ExerciseRunEntity run) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put(ExerciseMarking.CONTEXT_FLAG, true);
        out.put(ExerciseMarking.CONTEXT_RUN, run.getId());
        Map<String, Object> window = new LinkedHashMap<>();
        window.put("from", run.getRangeFrom() == null ? null : run.getRangeFrom().toString());
        window.put("to", run.getRangeTo() == null ? null : run.getRangeTo().toString());
        out.put(ExerciseMarking.CONTEXT_WINDOW, window);
        out.put(ExerciseMarking.CONTEXT_CHANNELS, List.copyOf(run.getChannelIds()));
        out.put(ExerciseMarking.CONTEXT_ENRICH, run.isEnrich());
        return out;
    }

    /**
     * The channels each alert of {@code plan} would reach: enabled channels of an available type that accept the
     * alert by their own filters and opted in or were named.
     */
    Map<String, Integer> projection(ExerciseReplay.Plan plan, List<String> named) {
        List<NotificationChannel> candidates = new ArrayList<>();
        for (NotificationChannelEntity entity : channels.findByEnabledTrue()) {
            if (types.find(entity.getType()).isEmpty()) {
                continue;
            }
            NotificationChannel channel = NotificationChannel.of(entity, List.of());
            if (ExerciseMarking.routes(channel, named)) {
                candidates.add(channel);
            }
        }
        Map<String, Integer> out = new TreeMap<>();
        for (NotificationChannel channel : candidates) {
            out.put(channel.id().toString(), 0);
        }
        for (ExerciseReplay.Match match : plan.matches()) {
            NotificationMessage probe = NotificationMessage.builder(NotificationMessage.ALERT, "alert.fired",
                    match.rule().name(), match.rule().severity().wire()).ruleId(match.rule().id()).exercise(true)
                    .build();
            for (NotificationChannel channel : candidates) {
                if (channel.accepts(probe)) {
                    out.merge(channel.id().toString(), 1, Integer::sum);
                }
            }
        }
        return out;
    }

    /** The {@code ExerciseStats} of a plan. */
    static Map<String, Object> stats(ExerciseReplay.Plan plan, Map<String, Integer> byChannel) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("versionsScanned", plan.versionsScanned());
        out.put("alertsByRule", new LinkedHashMap<>(plan.alertsByRule()));
        out.put("notificationsByChannel", new LinkedHashMap<>(byChannel));
        out.put("suppressedByCooldown", plan.suppressedByCooldown());
        out.put("suppressedByRateCap", plan.suppressedByRateCap());
        out.put("skippedRules", new ArrayList<>(plan.skippedRules()));
        out.put("caveats", new ArrayList<>(plan.caveats()));
        return out;
    }

    // ------------------------------------------------------------------------- validation

    /** Validates an {@code ExerciseRunCreate} body and resolves its rules, window and channels. */
    Request parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a JSON object.");
        }
        List<FieldError> errors = new ArrayList<>();
        String name = text(body, "name", errors);
        if (name == null || name.isBlank()) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > MAX_NAME) {
            errors.add(new FieldError("name", "must be at most " + MAX_NAME + " characters"));
        }
        List<String> ruleIds = strings(body, "ruleIds", errors);
        String tag = text(body, "tag", errors);
        if (ruleIds.isEmpty() && (tag == null || tag.isBlank())) {
            errors.add(new FieldError("ruleIds", "give ruleIds or a tag"));
        } else if (!ruleIds.isEmpty() && tag != null && !tag.isBlank()) {
            errors.add(new FieldError("tag", "give ruleIds or a tag, not both"));
        }
        ExerciseSpeed speed = DEFAULT_SPEED;
        String speedText = text(body, "speed", errors);
        if (speedText != null) {
            try {
                speed = ExerciseSpeed.fromWire(speedText);
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("speed", e.getMessage()));
            }
        }
        List<String> channelIds = strings(body, "channelIds", errors);
        for (String channelId : channelIds) {
            try {
                if (!channels.existsById(UUID.fromString(channelId))) {
                    errors.add(new FieldError("channelIds", "'" + channelId + "' names no channel"));
                }
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("channelIds", "'" + channelId + "' is not a channel id"));
            }
        }
        boolean enrich = bool(body, "enrich", errors);
        boolean dryRun = bool(body, "dryRun", errors);
        boolean timeRange = body.hasNonNull("from") || body.hasNonNull("to");
        boolean seqRange = body.hasNonNull("fromSeq") || body.hasNonNull("toSeq");
        if (timeRange == seqRange) {
            errors.add(new FieldError("from", "give from and to, or fromSeq and toSeq"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The exercise is not valid.", errors);
        }
        List<TriggerRuleEntity> selected = new ArrayList<>();
        if (!ruleIds.isEmpty()) {
            for (String ruleId : new LinkedHashSet<>(ruleIds)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(ruleId);
                } catch (IllegalArgumentException e) {
                    throw ValidationException.forField("ruleIds", "'" + ruleId + "' is not a rule id");
                }
                selected.add(rules.load(uuid));
            }
        } else {
            for (TriggerRuleService.RuleView view : rules.list(tag.strip(), null)) {
                selected.add(view.rule());
            }
            if (selected.isEmpty()) {
                throw ValidationException.forField("tag", "no rule has the tag '" + tag.strip() + "'");
            }
        }
        long[] seqs = new long[2];
        Instant[] times = new Instant[2];
        window(body, timeRange, seqs, times);
        checkRetention(selected, seqs[0]);
        return new Request(name.strip(), selected, ruleIds.isEmpty() ? tag.strip() : null, seqs[0], seqs[1], times[0],
                times[1], speed, channelIds, enrich, dryRun);
    }

    /** Resolves the window into seqs and times and checks its length. */
    private void window(JsonNode body, boolean timeRange, long[] seqs, Instant[] times) {
        Instant now = clock.instant();
        long watermark = changeBus.lastPublishedSeq();
        Duration max = maxWindow();
        if (timeRange) {
            Instant from = instant(body, "from");
            Instant to = instant(body, "to");
            if (from == null || to == null) {
                throw ValidationException.forField(from == null ? "from" : "to", "is required with a time window");
            }
            if (to.isAfter(now)) {
                to = now;
            }
            if (!from.isBefore(to)) {
                throw ValidationException.forField("from", "must be before to (and before now)");
            }
            if (Duration.between(from, to).compareTo(max) > 0) {
                throw ValidationException.forField("to", "the window is longer than exercise.maxWindow ("
                        + Durations.format(max) + ")");
            }
            Instant start = from;
            Instant end = to;
            seqs[0] = reads.read(t -> seqClock.seqAt(t, start));
            seqs[1] = Math.min(watermark, reads.read(t -> seqClock.seqAt(t, end)));
            times[0] = from;
            times[1] = to;
        } else {
            Long fromSeq = seq(body, "fromSeq");
            Long toSeq = seq(body, "toSeq");
            if (fromSeq == null || toSeq == null) {
                throw ValidationException.forField(fromSeq == null ? "fromSeq" : "toSeq",
                        "is required with a seq window");
            }
            toSeq = Math.min(toSeq, watermark);
            if (fromSeq >= toSeq) {
                throw ValidationException.forField("fromSeq", "must be below toSeq (and the watermark)");
            }
            seqs[0] = fromSeq;
            seqs[1] = toSeq;
            times[0] = timeOfSeq(fromSeq);
            times[1] = timeOfSeq(toSeq);
            if (times[0] != null && times[1] != null && Duration.between(times[0], times[1]).compareTo(max) > 0) {
                throw ValidationException.forField("toSeq", "the window is longer than exercise.maxWindow ("
                        + Durations.format(max) + ")");
            }
        }
        if (seqs[0] >= seqs[1]) {
            throw ValidationException.forField(timeRange ? "from" : "fromSeq", "no history was stored in the window");
        }
    }

    /**
     * Refuses a window starting below the retained floor of any source a rule reads (every source with a floor
     * for a rule without a source filter).
     *
     * @throws ValidationException {@code REPLAY_BEYOND_RETENTION}
     */
    void checkRetention(List<TriggerRuleEntity> selected, long fromSeq) {
        Map<String, Long> floors = retention.floors();
        Set<String> involved = new java.util.TreeSet<>();
        boolean all = false;
        for (TriggerRuleEntity rule : selected) {
            if (rule.getSources() == null || rule.getSources().isEmpty()) {
                all = true;
            } else {
                involved.addAll(rule.getSources());
            }
        }
        if (all) {
            involved.addAll(floors.keySet());
        }
        for (String source : involved) {
            long floor = retention.floorSeq(source);
            // Versions with seq > fromSeq are replayed; every version below the floor is gone.
            if (floor > 0 && fromSeq + 1 < floor) {
                Instant earliest = timeOfSeq(floor);
                throw new ValidationException(ErrorCodes.REPLAY_BEYOND_RETENTION, "The window starts below the"
                        + " retained history of source " + source + "; its earliest replayable time is "
                        + (earliest == null ? "seq " + floor : earliest.toString()) + ".",
                        List.of(new FieldError("from", "source " + source + " is retained from "
                                + (earliest == null ? "seq " + floor : earliest.toString()))));
            }
        }
    }

    private List<TriggerRuleEntity> loadRules(List<String> ids) {
        List<TriggerRuleEntity> out = new ArrayList<>();
        for (String id : ids) {
            try {
                out.add(rules.load(UUID.fromString(id)));
            } catch (RuntimeException e) {
                log.info("Exercise rule {} is gone; it is not replayed", id);
            }
        }
        return out;
    }

    private ExerciseRunEntity entity(Request request, String actor, String id) {
        ExerciseRunEntity run = new ExerciseRunEntity(id, request.name());
        run.setRuleIds(request.rules().stream().map(r -> r.getId().toString()).toList());
        run.setTag(request.tag());
        run.setRangeFrom(request.from());
        run.setRangeTo(request.to());
        run.setFromSeq(request.fromSeq());
        run.setToSeq(request.toSeq());
        run.setSpeed(request.speed().wire());
        run.setChannelIds(request.channelIds());
        run.setEnrich(request.enrich());
        run.setCreatedBy(actor);
        run.setCreatedAt(clock.instant());
        return run;
    }

    /**
     * The minute in which {@code seq} was allocated ({@code seq_clock}, minute resolution), or null when the clock
     * has no minute that early.
     */
    Instant timeOfSeq(long seq) {
        List<OffsetDateTime> minutes = reads.read(t -> t.getJdbcTemplate().queryForList(
                "SELECT minute FROM seq_clock WHERE first_seq <= ? ORDER BY minute DESC LIMIT 1", OffsetDateTime.class,
                seq));
        return minutes.isEmpty() || minutes.getFirst() == null ? null : minutes.getFirst().toInstant();
    }

    // ------------------------------------------------------------------------- deletes, audit, notifications

    private int deleteAlerts(String where, MapSqlParameterSource p) {
        Integer deleted = tx.execute(status -> {
            String ids = "SELECT a.id FROM alert a WHERE " + where;
            String notes = "SELECT n.id FROM notification n WHERE n.alert_id IN (" + ids + ")";
            String deliveries = "SELECT d.id FROM notification_delivery d WHERE d.alert_id IN (" + ids + ")"
                    + " OR d.notification_id IN (" + notes + ")";
            jdbc.update("DELETE FROM push_dispatch WHERE delivery_id IN (" + deliveries + ")", p);
            jdbc.update("DELETE FROM notification_delivery WHERE id IN (" + deliveries + ")", p);
            jdbc.update("DELETE FROM notification_read WHERE notification_id IN (" + notes + ")", p);
            jdbc.update("DELETE FROM notification WHERE id IN (" + notes + ")", p);
            jdbc.update("DELETE FROM action_capability WHERE alert_id IN (" + ids + ")", p);
            jdbc.update("DELETE FROM cap_message WHERE alert_id IN (" + ids + ")", p);
            jdbc.update("DELETE FROM alert_escalation WHERE alert_id IN (" + ids + ")", p);
            jdbc.update("DELETE FROM alert_signal WHERE alert_id IN (" + ids + ")", p);
            return jdbc.update("DELETE FROM alert a WHERE " + where, p);
        });
        return deleted == null ? 0 : deleted;
    }

    private void audit(ExerciseRunEntity run, String action, String actor) {
        audit(run, action, actor, Map.of());
    }

    private void audit(ExerciseRunEntity run, String action, String actor, Map<String, Object> extra) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("action", action);
        detail.put("name", run.getName());
        detail.put("status", run.getStatus());
        detail.put("ruleIds", run.getRuleIds());
        detail.put("channelIds", run.getChannelIds());
        detail.put("actor", actor);
        detail.putAll(extra);
        try {
            audit.record(new AuditRecord(clock.instant(), AuditRecord.SYSTEM, null, actor, null, null, null,
                    "/v1/exercises/" + run.getId(), operation(action), null, AUDIT_TARGET, run.getId(), null, detail));
        } catch (RuntimeException e) {
            log.warn("Could not audit exercise {} {}: {}", action, run.getId(), e.toString());
        }
    }

    private static String operation(String action) {
        return switch (action) {
            case "cancel" -> "cancelExercise";
            case "purge" -> "purgeExerciseAlerts";
            default -> "createExercise";
        };
    }

    /** A {@code system} notification about the run, itself marked exercise, to channels subscribed to system events. */
    private void system(ExerciseRunEntity run, String subtype, String title, String body) {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> exercise = new LinkedHashMap<>();
        exercise.put("runId", run.getId());
        exercise.put("from", run.getRangeFrom() == null ? null : run.getRangeFrom().toString());
        exercise.put("to", run.getRangeTo() == null ? null : run.getRangeTo().toString());
        data.put(ExerciseMarking.DATA_MEMBER, exercise);
        data.put("status", run.getStatus());
        data.put("stats", run.getStats());
        try {
            notifications.publish(ExerciseMarking.mark(NotificationMessage.builder(NotificationMessage.SYSTEM, subtype,
                    title, "info").body(body).data(data).exercise(true).build()), null);
        } catch (RuntimeException e) {
            log.warn("Could not publish {} for exercise {}: {}", subtype, run.getId(), e.toString());
        }
    }

    private Duration duration(String key, Duration def) {
        try {
            String value = settings.get(key, String.class).orElse(null);
            return value == null || value.isBlank() ? def : Durations.parsePositive(value);
        } catch (RuntimeException e) {
            return def;
        }
    }

    private static String escape(String text) {
        return ai.foresignal.notify.MarkdownRenderer.escapeMarkdown(text);
    }

    private static String text(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        return node.asString();
    }

    private static boolean bool(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return false;
        }
        if (!node.isBoolean()) {
            errors.add(new FieldError(field, "must be true or false"));
            return false;
        }
        return node.asBoolean();
    }

    private static List<String> strings(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return out;
        }
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                errors.add(new FieldError(field, "must be an array of strings"));
                return new ArrayList<>();
            }
            out.add(item.asString().strip());
        }
        return out;
    }

    private static Instant instant(JsonNode body, String field) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(node.asString()).toInstant();
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(field, "must be an RFC 3339 date-time");
        }
    }

    private static Long seq(JsonNode body, String field) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.canConvertToLong() || node.asLong() < 0) {
            throw ValidationException.forField(field, "must be a non-negative integer");
        }
        return node.asLong();
    }

    /** Binds an instant as {@code timestamptz}. */
    private static final class Timestamp {
        private Timestamp() {}

        static OffsetDateTime of(Instant instant) {
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
    }
}
