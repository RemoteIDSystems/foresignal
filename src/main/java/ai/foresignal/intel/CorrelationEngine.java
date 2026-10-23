package ai.foresignal.intel;

import ai.foresignal.api.support.NearFilter;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The deterministic correlation engine (prompt 101). A {@link ChangeBus} subscriber with a durable
 * {@code intel.cursor} that catches up in chunks: for each enabled recipe it clusters the seed
 * versions of its kinds, adds join members within the recipe's distance or region and time windows,
 * evaluates contradictions, scores the confidence with {@link EventScorer}, dedups against open (and
 * recently resolved) events and writes the event, its members and its log in one transaction. It runs
 * on its own virtual thread and never blocks ingest.
 *
 * <p><strong>Chunks and look-back.</strong> A chunk reads the new versions {@code (from, to]} plus a
 * look-back of older versions ({@code seq ≤ from}) whose observation time lies within the recipe's
 * widest window of the new ones, so signals on either side of a chunk boundary (or a restart) still
 * correlate. A cluster is only written when it holds at least one new version; existing members are
 * never added twice, so re-running a seq range yields the same events and the same membership.
 *
 * <p><strong>Failures.</strong> When a recipe throws, the cursor does not advance past the chunk; the
 * chunk is retried on the next pass, the recipe's {@code last_error} is set, and after
 * {@value #ALARM_AFTER} consecutive failures one {@code system.intel_correlation_failed} system event
 * is raised. Disabling the failing recipe lets the cursor move on.
 *
 * <p><strong>Contradictions are coverage-aware</strong> (principle 4). A {@code kinds} contradiction
 * adds contradicting members. A {@code healthOf} source contradicts only when it is running and
 * {@link HealthStatus#HEALTHY}, covers the area (its reported coverage region holds the area, or it
 * reported there during the previous {@value #PRESENCE_DAYS} days) and observed nothing near the event
 * during its window. An unhealthy source that covers the area adds its alternative as a known coverage
 * gap without lowering confidence; a disabled, idle or non-covering source is ignored. The inputs used
 * are stored in {@code detector.params.contradictionInputs}.
 *
 * <p><strong>Lifecycle.</strong> A candidate meeting {@code minMembers}/{@code minSignalFamilies} is
 * {@code emerging}; it becomes {@code active} once {@code activateAt} holds over all its members. An open
 * recipe event with no new evidence for {@code quietPeriod} is resolved; new evidence within
 * {@code reopenWindow} of a resolved event reopens it (logged {@code reopened}).
 */
@Component
public class CorrelationEngine implements ChangeListener, DisposableBean, ai.foresignal.ingest.IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(CorrelationEngine.class);

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "intel-correlation";

    /** The system-event subtype raised when a chunk keeps failing. */
    public static final String FAILURE_EVENT = "system.intel_correlation_failed";

    /** The seq chunk processed per pass. */
    static final long CHUNK = 10_000;

    /** The default join distance when a recipe names none, km. */
    static final double DEFAULT_KM = 150;

    /** The default seed and join window when a recipe names none. */
    static final Duration DEFAULT_WINDOW = Duration.ofHours(12);

    /** The most look-back versions read per chunk and recipe. */
    static final int LOOKBACK_LIMIT = 20_000;

    /** Consecutive failed passes over one chunk before the system event. */
    static final int ALARM_AFTER = 3;

    /** The presence window that shows a {@code healthOf} source covers an area, days. */
    static final int PRESENCE_DAYS = 7;

    /** The widest seq range {@code POST /recipes/{id}/test} dry-runs (larger runs are 103 backtests). */
    public static final long MAX_TEST_RANGE = 100_000;

    /** The open statuses dedup scans. */
    static final List<String> OPEN = List.of("emerging", "active");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ChangeBus changeBus;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final AppSettingService settings;
    private final RecipeRegistry recipes;
    private final IntelligenceEventRepository events;
    private final IntelEventMemberRepository members;
    private final IntelEventLogRepository logs;
    private final HealthService health;
    private final IntelVisibility visibility;
    private final ai.foresignal.reference.RegionTagger regions;
    private final IntelEventBus bus;
    private final IntelEventService eventService;
    private final TransactionTemplate transactions;
    private final ApplicationEventPublisher publisher;
    private final Clock clock;

    private volatile Subscription subscription;
    private volatile Thread thread;
    private volatile boolean running;
    private volatile long cursor = -1;
    private long failedChunk = -1;
    private int consecutiveFailures;

    /** The annotators that add context to new events (prompt 105); empty when none registered. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private List<EventAnnotator> annotators = List.of();

    /** Evidence identity (prompt 116), or null when the evidence layer is absent. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.evidence.EvidenceService evidence;

    /** The originator registry (prompt 154), to resolve a member's sensing modality, or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.evidence.OriginatorRegistry originators;

    /** The corroboration policy (prompt 154): tunable quality thresholds, or null (the defaults then apply). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private IntelCorroborationService corroboration;

    /** Analysis manifests (prompt 116), or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.manifest.ManifestService manifests;

    /** The derived-record heads the annotators read (prompt 105), or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.derive.DerivedRecords derivedRecords;

    /** Outlet reliability of news-article members (prompt 133), or null when the news layer is absent. */
    private ai.foresignal.news.NewsReliability newsReliability;

    /** Installs the outlet reliability of news-article members (prompt 133: {@code media.reliabilityMap}). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setNewsReliability(ai.foresignal.news.NewsReliability newsReliability) {
        this.newsReliability = newsReliability;
    }

    /** The reliability of a member: the outlet grade of a news article, else null (the scorer's default). */
    private Double memberReliability(String sourceId, String externalId) {
        ai.foresignal.news.NewsReliability r = newsReliability;
        if (r == null) {
            return null;
        }
        try {
            return r.reliability(sourceId, externalId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Creates the engine. */
    public CorrelationEngine(ChangeBus changeBus, JdbcTemplate jdbc, JsonMapper json, AppSettingService settings,
            RecipeRegistry recipes, IntelligenceEventRepository events, IntelEventMemberRepository members,
            IntelEventLogRepository logs, HealthService health, IntelVisibility visibility,
            ai.foresignal.reference.RegionTagger regions, IntelEventBus bus, IntelEventService eventService,
            TransactionTemplate transactions, ApplicationEventPublisher publisher, Clock clock) {
        this.changeBus = changeBus;
        this.jdbc = jdbc;
        this.json = json;
        this.settings = settings;
        this.recipes = recipes;
        this.events = events;
        this.members = members;
        this.logs = logs;
        this.health = health;
        this.visibility = visibility;
        this.regions = regions;
        this.bus = bus;
        this.eventService = eventService;
        this.transactions = transactions;
        this.publisher = publisher;
        this.clock = clock;
    }

    /** Starts the subscriber and the catch-up thread once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.INTEL)
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        subscription = changeBus.subscribe(SUBSCRIBER, this, 1024);
        thread = Thread.ofVirtual().name("intel-correlation").start(this::loop);
    }

    /** Stops the engine. */
    @Override
    public synchronized void destroy() {
        running = false;
        if (subscription != null) {
            subscription.close();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        // The catch-up loop advances the cursor; nothing to do per batch.
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        // The loop catches up from the durable cursor.
    }

    /** True when the engine is running. */
    public boolean running() {
        return running;
    }

    /** The durable cursor. */
    public long cursor() {
        if (cursor < 0) {
            cursor = settings.get(SettingKeys.INTEL_CURSOR, Long.class).orElse(0L);
        }
        return cursor;
    }

    /**
     * Restarts the cursor at 0 after the record history and {@code record_seq} were reset (a cached or stored cursor
     * would point past every new record and nothing would correlate). Synchronized with {@link #catchUp()}: a pass in
     * flight during the reset finishes first, and the cursor it stored for the old history is removed.
     */
    @Override
    public synchronized void resetState() {
        cursor = 0;
        // A missing cursor already means 0, so the reset removes it rather than writing 0 back: the cleaner leaves
        // app_setting empty apart from the cursors whose absence means "start at the head" (trigger, region).
        settings.remove(SettingKeys.INTEL_CURSOR);
    }

    /**
     * Moves the durable cursor to {@code seq} (tests, and a restart simulated in place): the next
     * pass re-reads from there. Membership stays exactly-once because existing members are never added again.
     * Public for the retention test support, which moves it past a {@code record_seq} jump (checkpoint 124).
     */
    public synchronized void rewind(long seq) {
        storeCursor(seq);
    }

    /** The catch-up loop. */
    private void loop() {
        while (running) {
            try {
                catchUp();
            } catch (RuntimeException e) {
                log.warn("Correlation catch-up failed: {}", e.toString());
            }
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    /**
     * Processes every version between the cursor and the watermark, then resolves quiet events. A chunk
     * on which any recipe fails is not passed: the cursor stays before it and the pass ends.
     */
    public synchronized void catchUp() {
        long watermark = changeBus.lastPublishedSeq();
        long from = cursor();
        if (from > watermark) {
            // Only possible after the record history and record_seq were reset (a restore, or the test cleaner racing
            // a pass that stored its old cursor): the stored cursor describes a history that no longer exists, so the pass
            // starts again from the beginning (membership stays exactly-once).
            from = 0;
            storeCursor(from);
        }
        List<RecipeDefinition> enabled = recipes.enabledDefinitions();
        while (from < watermark && running) {
            long to = Math.min(from + CHUNK, watermark);
            List<String> failures = new ArrayList<>();
            for (RecipeDefinition recipe : enabled) {
                Instant at = clock.instant();
                try {
                    List<Candidate> produced = correlate(recipe, from, to, true);
                    recipes.recordRun(recipe.id(), at, null, produced.isEmpty() ? null : runManifest(recipe, from, to,
                            produced.size()));
                } catch (RuntimeException e) {
                    log.warn("Recipe {} failed on seq {}..{}: {}", recipe.id(), from, to, e.toString());
                    failures.add(recipe.id() + ": " + e);
                    recipes.recordRun(recipe.id(), at, e.toString());
                }
            }
            if (!failures.isEmpty()) {
                chunkFailed(from, to, failures);
                return;
            }
            consecutiveFailures = 0;
            failedChunk = -1;
            from = to;
            storeCursor(from);
        }
        resolveQuiet(enabled, clock.instant());
    }

    /** Counts a failed pass over {@code (from, to]} and raises the system event once the count reaches the limit. */
    private void chunkFailed(long from, long to, List<String> failures) {
        if (failedChunk != from) {
            failedChunk = from;
            consecutiveFailures = 0;
        }
        consecutiveFailures++;
        if (consecutiveFailures == ALARM_AFTER) {
            try {
                publisher.publishEvent(new SystemEvent(FAILURE_EVENT, "warning",
                        "Intelligence correlation is stuck",
                        "Correlation failed " + ALARM_AFTER + " times on seq " + from + ".." + to
                                + " and the cursor is held there: " + String.join("; ", failures)
                                + ". Fix or disable the failing recipe (PUT /v1/intel/recipes/{id}).",
                        "/v1/intel/recipes", Map.of("fromSeq", from, "toSeq", to, "failures", failures)));
            } catch (RuntimeException e) {
                log.warn("Could not raise the correlation failure event: {}", e.toString());
            }
        }
    }

    /**
     * The manifest of a recipe run that produced candidates (prompt 116): the engine and scorer versions, the
     * recipe, its seed distance and the seq range read as the input query.
     */
    private String runManifest(RecipeDefinition recipe, long from, long to, int candidates) {
        if (manifests == null) {
            return null;
        }
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("recipeId", recipe.id());
        config.put("recipeVersion", recipe.version());
        config.put("scorer", EventScorer.NAME + "/" + EventScorer.VERSION);
        config.put("seedKm", seedKm(recipe));
        config.put("chunk", CHUNK);
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("table", "record_version");
        query.put("kinds", new ArrayList<>(recipe.allKinds()));
        query.put("fromSeq", from);
        query.put("toSeq", to);
        query.put("candidates", candidates);
        try {
            return manifests.record(ai.foresignal.manifest.ManifestRequest.of(RECIPE_COMPONENT, EventScorer.VERSION)
                    .config(config).registries("originators.json", "canonical-rules.json").asOf(to).query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record the run manifest of recipe {}: {}", recipe.id(), e.toString());
            return null;
        }
    }

    /** The component name of recipe-run manifests. */
    public static final String RECIPE_COMPONENT = "correlation-engine";

    // ------------------------------------------------------------------------- dry run

    /**
     * Dry-runs {@code recipeId} over {@code (fromSeq, toSeq]} without writing anything (prompt 101,
     * {@code POST /v1/intel/recipes/{id}/test}): the would-be events, deduplicated among themselves.
     *
     * @param privateAccess whether candidates built from private sources may be returned
     * @return the candidates
     */
    public List<Candidate> dryRun(RecipeDefinition recipe, long fromSeq, long toSeq, boolean privateAccess) {
        List<Candidate> out = new ArrayList<>();
        for (Candidate candidate : correlate(recipe, fromSeq, toSeq, false)) {
            if (candidate.privateEvent() && !privateAccess) {
                continue;
            }
            boolean merged = false;
            for (int i = 0; i < out.size(); i++) {
                Candidate kept = out.get(i);
                if (Dedup.merges(candidate.type(), candidate.geom(), candidate.start(), candidate.end(),
                        candidate.memberKeys(), shell(kept), kept.memberKeys(), recipe.dedup().radiusKm(),
                        dedupWindow(recipe))) {
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                out.add(candidate);
            }
        }
        return out;
    }

    private static IntelligenceEventEntity shell(Candidate candidate) {
        IntelligenceEventEntity shell = new IntelligenceEventEntity("dry-run", candidate.type(), candidate.title(),
                candidate.start() == null ? Instant.EPOCH : candidate.start());
        shell.setGeom(candidate.geom());
        shell.setEndTime(candidate.end());
        return shell;
    }

    // ------------------------------------------------------------------------- correlation

    /** Correlates one recipe over {@code (from, to]}; writes when {@code persist}, and returns the candidates. */
    List<Candidate> correlate(RecipeDefinition recipe, long from, long to, boolean persist) {
        List<MemberRow> fresh = versions(recipe.allKinds(), from, to);
        if (fresh.isEmpty()) {
            return List.of();
        }
        Set<Long> freshSeqs = new java.util.HashSet<>();
        Instant minObserved = null;
        Instant maxObserved = null;
        for (MemberRow row : fresh) {
            freshSeqs.add(row.seq());
            minObserved = min(minObserved, row.observedAt());
            maxObserved = max(maxObserved, row.observedAt());
        }
        Duration span = lookback(recipe);
        List<MemberRow> rows = new ArrayList<>(lookbackVersions(recipe.allKinds(), from,
                minObserved.minus(span), maxObserved.plus(span)));
        rows.addAll(fresh);
        double seedKm = seedDistance(recipe);
        Duration seedWindow = seedWindow(recipe);
        Map<Long, ai.foresignal.reference.RegionTags> regionCache = new HashMap<>();
        Map<String, ai.foresignal.evidence.EvidenceService.GroupKey> evidenceKeys = evidenceKeys(rows);
        List<Candidate> out = new ArrayList<>();
        for (List<MemberRow> cluster : cluster(rows, recipe.seed().kinds(), seedKm, seedWindow)) {
            Candidate candidate = build(recipe, cluster, rows, from, to, regionCache, evidenceKeys);
            if (candidate == null || !candidate.touches(freshSeqs)) {
                continue;
            }
            out.add(candidate);
            if (persist) {
                persist(recipe, candidate);
            }
        }
        return out;
    }

    /**
     * A candidate event before persistence.
     *
     * @param active true when {@code activateAt} holds on the candidate alone
     */
    public record Candidate(String type, String title, String severity, Instant start, Instant end, Geometry geom,
            Map<String, Object> detector, EventScorer.Score score, List<MemberLine> members, int contradictions,
            List<Map<String, Object>> alternatives, String explanation, boolean privateEvent, boolean active) {

        /** The pin keys of the members. */
        public Set<String> memberKeys() {
            Set<String> keys = new LinkedHashSet<>();
            for (MemberLine line : members) {
                keys.add(memberKey(line.memberType(), line.sourceId(), line.externalId(), line.seq()));
            }
            return keys;
        }

        /** True when any member is one of {@code seqs}. */
        boolean touches(Set<Long> seqs) {
            for (MemberLine line : members) {
                if (seqs.contains(line.seq())) {
                    return true;
                }
            }
            return false;
        }

        /** The candidate as a would-be {@code IntelligenceEvent} (dry runs). */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("type", type);
            out.put("title", title);
            out.put("severity", severity);
            out.put("status", active ? "active" : "emerging");
            out.put("confidence", score.confidence());
            out.put("confidenceLabel", score.label());
            out.put("confidenceMethod", EventScorer.METHOD);
            // A dry run has no persisted history, so its would-be confidence is the raw score (never calibrated).
            out.put("calibrated", false);
            out.put("evidenceQuality", score.evidenceQuality());
            out.put("startTime", start);
            out.put("endTime", end);
            out.put("private", privateEvent);
            out.put("detector", detector);
            out.put("explanation", explanation);
            List<Map<String, Object>> memberViews = new ArrayList<>();
            for (MemberLine line : members) {
                Map<String, Object> member = new LinkedHashMap<>();
                member.put("role", line.role());
                member.put("memberType", line.memberType());
                member.put("sourceId", line.sourceId());
                member.put("externalId", line.externalId());
                member.put("seq", line.seq());
                member.put("version", line.version());
                member.put("kind", line.kind());
                member.put("observedAt", line.observedAt());
                member.put("joinReason", line.joinReason());
                member.put("signalFamily", line.signalFamily());
                member.put("weight", line.weight());
                memberViews.add(member);
            }
            out.put("evidence", Map.of("memberCount", members.size(), "members", memberViews));
            out.put("inference", Map.of("type", type, "severity", severity, "confidence", score.confidence(),
                    "confidenceLabel", score.label(), "geometry", geom == null ? "" : geom.toText()));
            out.put("speculation", Map.of("alternatives", alternatives));
            return out;
        }
    }

    /** One member line. */
    record MemberLine(String role, String memberType, String sourceId, String externalId, long seq, Integer version,
            String kind, Instant observedAt, String signalFamily, double weight, Map<String, Object> joinReason,
            Map<String, Object> snapshot) {}

    private Candidate build(RecipeDefinition recipe, List<MemberRow> cluster, List<MemberRow> rows, long from, long to,
            Map<Long, ai.foresignal.reference.RegionTags> regionCache,
            Map<String, ai.foresignal.evidence.EvidenceService.GroupKey> evidenceKeys) {
        List<MemberLine> lines = new ArrayList<>();
        Set<String> families = new LinkedHashSet<>();
        List<EventScorer.MemberView> views = new ArrayList<>();
        Set<String> pinned = new LinkedHashSet<>();
        Instant start = null;
        Instant end = null;
        boolean anyPrivate = false;
        List<Geometry> geometries = new ArrayList<>();
        for (MemberRow row : cluster) {
            String family = SignalFamilies.forKind(row.kind());
            lines.add(line("supporting", row, family, RecipeRegistry.SEED_WEIGHT, Map.of("clause", "seed")));
            families.add(family);
            views.add(view(row, family, RecipeRegistry.SEED_WEIGHT, evidenceKeys));
            pinned.add(memberKey("record", row.sourceId(), row.externalId(), row.seq()));
            start = min(start, row.observedAt());
            end = max(end, row.observedAt());
            anyPrivate = anyPrivate || visibility.isPrivateSource(row.sourceId());
            if (row.geom() != null) {
                geometries.add(row.geom());
            }
        }
        Coordinate centroid = centroidOf(cluster);
        ai.foresignal.reference.RegionTags clusterRegion = null;
        // Joins: each row joins a cluster at most once, never twice as seed and join, and only within the
        // clause's distance or region and its time window. A row without geometry never joins.
        for (int j = 0; j < recipe.joins().size(); j++) {
            RecipeDefinition.Join join = recipe.joins().get(j);
            Duration before = join.before() == null ? DEFAULT_WINDOW : join.before();
            Duration after = join.after() == null ? DEFAULT_WINDOW : join.after();
            for (MemberRow row : rows) {
                if (!join.kinds().contains(row.kind())
                        || pinned.contains(memberKey("record", row.sourceId(), row.externalId(), row.seq()))) {
                    continue;
                }
                if (row.observedAt().isBefore(start.minus(before)) || row.observedAt().isAfter(end.plus(after))) {
                    continue;
                }
                Map<String, Object> reason = new LinkedHashMap<>();
                reason.put("clause", "join[" + j + "]");
                Coordinate coordinate = coordinate(row);
                Double distance = coordinate == null ? null : nearestKm(cluster, coordinate);
                if (join.sharedAttribute() != null) {
                    // Prompt 157: a shared-attribute join matches on an attribute (address, phone, officer, shared
                    // id), not geography; geometry is not required, only the time window above.
                    Object value = sharedAttributeMatch(row, cluster, join.sharedAttribute());
                    if (value == null) {
                        continue;
                    }
                    reason.put("sharedAttribute", join.sharedAttribute());
                    reason.put("sharedValue", String.valueOf(value));
                } else if (join.sameRegion() != null) {
                    if (coordinate == null) {
                        continue;
                    }
                    if (clusterRegion == null) {
                        clusterRegion = regionOf(cluster, regionCache);
                    }
                    String tag = sameRegion(join.sameRegion(), clusterRegion, regionOf(row, regionCache));
                    if (tag == null) {
                        continue;
                    }
                    reason.put("regionTag", tag);
                } else {
                    if (coordinate == null) {
                        continue;
                    }
                    double km = join.withinKm() == null ? DEFAULT_KM : join.withinKm();
                    if (distance == null || distance > km) {
                        continue;
                    }
                }
                reason.put("distanceKm", distance);
                reason.put("dtMinutes", nearestMinutes(cluster, row.observedAt()));
                String family = join.signalFamily() == null ? SignalFamilies.forKind(row.kind()) : join.signalFamily();
                String role = join.role() == null ? "context" : join.role();
                lines.add(line(role, row, family, join.weight(), reason));
                families.add(family);
                views.add(view(row, family, join.weight(), evidenceKeys));
                pinned.add(memberKey("record", row.sourceId(), row.externalId(), row.seq()));
                anyPrivate = anyPrivate || visibility.isPrivateSource(row.sourceId());
                if (row.geom() != null) {
                    geometries.add(row.geom());
                }
            }
        }
        if (lines.size() < recipe.minMembers() || families.size() < recipe.minSignalFamilies()) {
            return null;
        }
        Evaluated contradictions = contradictions(recipe, cluster, rows, centroid, start, end, to, pinned);
        for (MemberLine line : contradictions.lines()) {
            lines.add(line);
            anyPrivate = anyPrivate || visibility.isPrivateSource(line.sourceId());
        }
        EventScorer.Score score = EventScorer.score(views, contradictions.count(), qualityThresholds());
        Geometry geom = geometry(geometries);
        boolean active = views.size() >= recipe.activateAt().members()
                && (recipe.activateAt().confidence() == null || score.confidence() >= recipe.activateAt().confidence())
                && meetsModalities(views, recipe.activateAt().modalities());
        String severity = recipe.severityMap().getOrDefault("default", "warning");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("fromSeq", from);
        params.put("toSeq", to);
        params.put("seedKm", seedKm(recipe));
        params.put("contradictionInputs", contradictions.inputs());
        Map<String, Object> detector = new LinkedHashMap<>();
        detector.put("name", EventScorer.NAME);
        detector.put("version", EventScorer.VERSION);
        detector.put("recipeId", recipe.id());
        detector.put("recipeVersion", recipe.version());
        detector.put("params", params);
        return new Candidate(recipe.type(), title(lines, families), severity, start, end, geom, detector, score,
                lines, contradictions.count(), contradictions.alternatives(), explanation(lines, families, start, end),
                anyPrivate, active);
    }

    /** What the contradiction clauses found. */
    private record Evaluated(int count, List<MemberLine> lines, List<Map<String, Object>> alternatives,
            Map<String, Object> inputs) {}

    private Evaluated contradictions(RecipeDefinition recipe, List<MemberRow> cluster, List<MemberRow> rows,
            Coordinate centroid, Instant start, Instant end, long to, Set<String> pinned) {
        int count = 0;
        List<MemberLine> lines = new ArrayList<>();
        List<Map<String, Object>> alternatives = new ArrayList<>();
        Map<String, Object> inputs = new TreeMap<>();
        double defaultKm = seedDistance(recipe);
        for (int c = 0; c < recipe.contradictions().size(); c++) {
            RecipeDefinition.Contradiction contradiction = recipe.contradictions().get(c);
            String hypothesis = contradiction.alternative() == null ? "unknown" : contradiction.alternative();
            double km = contradiction.withinKm() == null ? defaultKm : contradiction.withinKm();
            Duration window = contradiction.window() == null ? seedWindow(recipe) : contradiction.window();
            if (!contradiction.kinds().isEmpty()) {
                List<String> matched = new ArrayList<>();
                for (MemberRow row : rows) {
                    if (!contradiction.kinds().contains(row.kind())
                            || pinned.contains(memberKey("record", row.sourceId(), row.externalId(), row.seq()))) {
                        continue;
                    }
                    Coordinate coordinate = coordinate(row);
                    Double distance = coordinate == null ? null : nearestKm(cluster, coordinate);
                    if (distance == null || distance > km || row.observedAt().isBefore(start.minus(window))
                            || row.observedAt().isAfter(end.plus(window))) {
                        continue;
                    }
                    Map<String, Object> reason = new LinkedHashMap<>();
                    reason.put("clause", "contradiction[" + c + "]");
                    reason.put("distanceKm", distance);
                    reason.put("dtMinutes", nearestMinutes(cluster, row.observedAt()));
                    lines.add(line("contradicting", row, SignalFamilies.forKind(row.kind()), 0, reason));
                    pinned.add(memberKey("record", row.sourceId(), row.externalId(), row.seq()));
                    matched.add(row.sourceId() + ":" + row.externalId());
                }
                if (!matched.isEmpty()) {
                    count++;
                    alternatives.add(alternative(hypothesis, matched.size() + " contradicting record(s) of "
                            + String.join("/", contradiction.kinds()) + " within " + (int) km + " km", "contradiction",
                            matched));
                }
            }
            for (String source : contradiction.healthOf()) {
                String verdict = healthOf(source, centroid, km, start, end, window, to);
                inputs.put(source, verdict);
                switch (verdict) {
                    case "healthy-observed-nothing" -> {
                        count++;
                        alternatives.add(alternative(hypothesis, "source " + source
                                + " is healthy, covers the area and observed nothing within " + (int) km + " km",
                                "healthOf", List.of()));
                    }
                    case "degraded", "failing" -> alternatives.add(alternative(hypothesis, "known coverage gap: source "
                            + source + " is " + verdict + " (not counted as contradiction)", "coverage-gap", List.of()));
                    default -> {
                        // not running, no coverage, no area, or healthy and observing: neither contradiction nor gap.
                    }
                }
            }
        }
        return new Evaluated(count, lines, alternatives, inputs);
    }

    /**
     * The verdict on one {@code healthOf} source: {@code not-running} (disabled, idle or unknown),
     * {@code no-area}, {@code no-coverage}, {@code degraded}/{@code failing} (a known coverage gap),
     * {@code healthy-observed} or {@code healthy-observed-nothing} (the only contradiction).
     */
    private String healthOf(String source, Coordinate centroid, double km, Instant start, Instant end,
            Duration window, long to) {
        SourceHealthView view;
        try {
            view = health.health(source);
        } catch (RuntimeException e) {
            view = null;
        }
        HealthStatus status = view == null || view.status() == null ? HealthStatus.IDLE : view.status();
        if (!status.isRunning()) {
            return "not-running";
        }
        if (centroid == null) {
            return "no-area";
        }
        if (!covers(source, view, centroid, km, start, to)) {
            return "no-coverage";
        }
        if (status != HealthStatus.HEALTHY) {
            return status.value();
        }
        return reportedNear(source, centroid, km, start.minus(window), end.plus(window), to)
                ? "healthy-observed" : "healthy-observed-nothing";
    }

    /** True when the source's reported region holds the point, or it reported near it recently. */
    private boolean covers(String source, SourceHealthView view, Coordinate centroid, double km, Instant start,
            long to) {
        if (view != null && view.coverage() != null && view.coverage().region() != null) {
            List<Double> region = view.coverage().region();
            return centroid.x >= region.get(0) && centroid.y >= region.get(1) && centroid.x <= region.get(2)
                    && centroid.y <= region.get(3);
        }
        return reportedNear(source, centroid, km, start.minus(Duration.ofDays(PRESENCE_DAYS)), start, to);
    }

    /** True when {@code source} has a version within {@code km} of the point, observed in the window, at seq ≤ {@code to}. */
    private boolean reportedNear(String source, Coordinate centroid, double km, Instant from, Instant until, long to) {
        List<Integer> hit = jdbc.queryForList("SELECT 1 FROM record_version WHERE source_id = ? AND seq <= ?"
                + " AND observed_at >= ? AND observed_at <= ? AND geom IS NOT NULL"
                + " AND " + new NearFilter(centroid.x, centroid.y, km).prefilterSql("geom")
                + " AND fs_dwithin(geom, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?) LIMIT 1",
                Integer.class, source, to, Timestamp.from(from), Timestamp.from(until), centroid.x, centroid.y,
                km * 1000.0);
        return !hit.isEmpty();
    }

    private static Map<String, Object> alternative(String hypothesis, String basis, String clause,
            List<String> supporting) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("hypothesis", hypothesis);
        out.put("basis", basis);
        out.put("supportingMemberIds", supporting);
        out.put("clause", clause);
        return out;
    }

    private void persist(RecipeDefinition recipe, Candidate candidate) {
        Written written = transactions.execute(status -> write(recipe, candidate));
        if (written == null) {
            return;
        }
        if (written.announce() != null) {
            bus.publish(written.announce());
        }
        // Prompt 151: a recipe update that crossed a trust threshold also announces a first-class upgrade.
        if (written.upgrade() != null) {
            bus.publish(written.upgrade());
        }
    }

    /**
     * The stream events a write announces after commit.
     *
     * @param announce the created / updated event, or null when nothing changed
     * @param upgrade the first-class {@code upgraded} event (prompt 151), or null
     */
    private record Written(IntelEvent announce, IntelEvent upgrade) {}

    /** Writes one candidate in the caller's transaction; returns the stream events to announce after commit. */
    private Written write(RecipeDefinition recipe, Candidate candidate) {
        Set<String> memberKeys = candidate.memberKeys();
        Duration window = dedupWindow(recipe);
        IntelligenceEventEntity event = findOpen(candidate, memberKeys, recipe.dedup().radiusKm(), window);
        boolean reopened = false;
        if (event == null) {
            event = findReopenable(candidate, memberKeys, recipe);
            reopened = event != null;
        }
        Instant now = clock.instant();
        boolean created = event == null;
        List<MemberLine> newLines = new ArrayList<>();
        for (MemberLine line : candidate.members()) {
            if (created || !memberExists(event.getId(), line)) {
                newLines.add(line);
            }
        }
        if (!created && newLines.isEmpty()) {
            // Nothing new for an existing event (open or resolved): re-running a range never changes
            // membership or fields, and old evidence never reopens a resolved event.
            return null;
        }
        TrustCrossing.Level trustBefore = created ? null : TrustCrossing.of(event);
        if (created) {
            event = new IntelligenceEventEntity(Ulids.next(now), candidate.type(), candidate.title(), candidate.start());
            event.setCreatedAt(now);
            event.setStatus("emerging");
            event.setSeverity(candidate.severity());
            event.setOrigin("recipe");
        }
        Map<String, Object> before = created ? null : IntelEventMapper.summary(event);
        event.setStartTime(min(event.getStartTime(), candidate.start()));
        event.setEndTime(null);
        event.setLastEvidenceAt(max(event.getLastEvidenceAt(), candidate.end() == null ? now : candidate.end()));
        Geometry geom = event.getGeom() == null ? candidate.geom()
                : candidate.geom() == null ? event.getGeom() : unionAll(List.of(event.getGeom(), candidate.geom()));
        event.setGeom(geom);
        event.setCentroid(geom == null ? null : geom.getCentroid());
        event.setDetector(candidate.detector());
        event.setRecipeId(recipe.id());
        event.setRecipeVersion(recipe.version());
        event.setAlternatives(mergeAlternatives(event.getAlternatives(), candidate.alternatives()));
        event.setPrivateEvent(event.isPrivateEvent() || candidate.privateEvent());
        event.setRegionTags(regionTags(geom));
        event.setUpdatedAt(now);
        events.save(event);
        int added = 0;
        for (MemberLine line : newLines) {
            IntelEventMemberEntity member = new IntelEventMemberEntity(event.getId(), line.memberType(), line.sourceId(),
                    line.externalId(), line.seq());
            member.setRole(line.role());
            member.setVersion(line.version());
            member.setKind(line.kind());
            member.setObservedAt(line.observedAt());
            member.setJoinReason(line.joinReason());
            member.setSignalFamily(line.signalFamily());
            member.setWeight(line.weight());
            member.setSnapshot(line.snapshot());
            member.setAddedAt(now);
            member.setAddedBy("recipe:" + recipe.id());
            members.save(member);
            added++;
        }
        members.flush();
        // Rescore over all members, not the newest candidate, and derive title and explanation from them.
        eventService.rescore(event);
        List<IntelEventMemberEntity> all = members.findByEventIdOrderByIdAsc(event.getId());
        long supporting = all.stream().filter(m -> !"contradicting".equals(m.getRole())).count();
        boolean active = supporting >= recipe.activateAt().members() && (recipe.activateAt().confidence() == null
                || (event.getConfidence() != null && event.getConfidence() >= recipe.activateAt().confidence()))
                && membersSpanModalities(all, recipe.activateAt().modalities());
        if (active) {
            event.setStatus("active");
        } else if (created || reopened) {
            event.setStatus("emerging");
        }
        event.setTitle(titleOf(all));
        event.setExplanation(explanationOf(all, event.getStartTime(), event.getLastEvidenceAt()));
        applyAnnotators(event, now);
        // Annotators may add context members after the rescore (checkpoint 124: memberCount).
        event.setMemberCount((int) members.countByEventId(event.getId()));
        events.save(event);
        String action = created ? "created" : reopened ? "reopened" : "member_added";
        IntelEventLogEntity logRow = new IntelEventLogEntity(event.getId(), now, "recipe:" + recipe.id(), action);
        if (before != null) {
            logRow.setBefore(new LinkedHashMap<>(before));
        }
        logRow.setAfter(Map.of("members", added, "confidence", event.getConfidence() == null ? 0.0 : event.getConfidence(),
                "status", event.getStatus()));
        logs.save(logRow);
        IntelEvent announce = new IntelEvent(logRow.getId(), event.getId(),
                created ? IntelEvent.CREATED : IntelEvent.UPDATED, event.getVersion(),
                IntelEventMapper.summary(event), null);
        return new Written(announce, upgrade(event, trustBefore, recipe, now));
    }

    /**
     * The first-class {@code upgraded} stream event (prompt 151) when a recipe update crossed a trust threshold, with
     * its own log entry for a distinct cursor; null when nothing was upgraded.
     */
    private IntelEvent upgrade(IntelligenceEventEntity event, TrustCrossing.Level before, RecipeDefinition recipe,
            Instant now) {
        List<Map<String, Object>> crossings = TrustCrossing.crossings(before, TrustCrossing.of(event));
        if (crossings.isEmpty()) {
            return null;
        }
        IntelEventLogEntity logRow = new IntelEventLogEntity(event.getId(), now, "recipe:" + recipe.id(), "upgraded");
        logRow.setAfter(Map.of("upgrade", crossings));
        logs.save(logRow);
        Map<String, Object> summary = new LinkedHashMap<>(IntelEventMapper.summary(event));
        summary.put("upgrade", crossings);
        return new IntelEvent(logRow.getId(), event.getId(), IntelEvent.UPGRADED, event.getVersion(), summary, null);
    }

    /**
     * The alternatives after an update: the clause-tagged ones a recipe re-evaluates on every pass
     * ({@code healthOf}, {@code coverage-gap}) are replaced by the candidate's; the others are kept, and
     * the candidate's are added once each.
     */
    static List<Map<String, Object>> mergeAlternatives(List<Map<String, Object>> existing,
            List<Map<String, Object>> fresh) {
        List<Map<String, Object>> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Map<String, Object> alternative : existing == null ? List.<Map<String, Object>>of() : existing) {
            Object clause = alternative.get("clause");
            if ("healthOf".equals(clause) || "coverage-gap".equals(clause)) {
                continue;
            }
            if (seen.add(alternative.get("hypothesis") + "|" + alternative.get("basis"))) {
                out.add(alternative);
            }
        }
        for (Map<String, Object> alternative : fresh) {
            if (seen.add(alternative.get("hypothesis") + "|" + alternative.get("basis"))) {
                out.add(alternative);
            }
        }
        return out;
    }

    /** Resolves open recipe events with no new evidence for their recipe's quiet period. */
    void resolveQuiet(List<RecipeDefinition> enabled, Instant now) {
        for (RecipeDefinition recipe : enabled) {
            for (IntelligenceEventEntity open : events.findByTypeAndStatusInOrderByUpdatedAtDesc(recipe.type(), OPEN)) {
                if (!recipe.id().equals(open.getRecipeId()) || open.getLastEvidenceAt() == null
                        || open.getLastEvidenceAt().plus(recipe.quietPeriod()).isAfter(now)) {
                    continue;
                }
                IntelEvent announced = transactions.execute(status -> {
                    IntelligenceEventEntity event = events.findById(open.getId()).orElse(null);
                    if (event == null || !OPEN.contains(event.getStatus())) {
                        return null;
                    }
                    Map<String, Object> before = IntelEventMapper.summary(event);
                    event.setStatus("resolved");
                    event.setEndTime(now);
                    event.setUpdatedAt(now);
                    events.save(event);
                    IntelEventLogEntity logRow = new IntelEventLogEntity(event.getId(), now, "recipe:" + recipe.id(),
                            "status");
                    logRow.setBefore(new LinkedHashMap<>(before));
                    logRow.setAfter(Map.of("status", "resolved", "reason", "quiet period " + recipe.quietPeriod()));
                    logs.save(logRow);
                    return new IntelEvent(logRow.getId(), event.getId(), IntelEvent.RESOLVED, event.getVersion(),
                            IntelEventMapper.summary(event), null);
                });
                if (announced != null) {
                    bus.publish(announced);
                }
            }
        }
    }

    /**
     * Runs every annotator that applies to {@code event}, storing its context and context members. It runs on
     * every recipe hit, so it is idempotent: context sections are replaced, context members are added once,
     * and an alternative replaces any stored alternative with the same hypothesis.
     */
    private void applyAnnotators(IntelligenceEventEntity event, Instant now) {
        if (annotators.isEmpty() || derivedRecords == null || event.getGeom() == null || event.getGeom().isEmpty()) {
            return;
        }
        Instant base = event.getStartTime() == null ? now : event.getStartTime();
        Instant end = event.getEndTime() != null ? event.getEndTime()
                : event.getLastEvidenceAt() != null ? event.getLastEvidenceAt() : now;
        EventAnnotator.AnnotatorContext ctx = new EventAnnotator.AnnotatorContext(event.getGeom(),
                base.minus(Duration.ofHours(3)), end.plus(Duration.ofHours(3)), derivedRecords);
        Map<String, Object> context = event.getContext() == null ? new LinkedHashMap<>()
                : new LinkedHashMap<>(event.getContext());
        List<Map<String, Object>> alternatives = new ArrayList<>(event.getAlternatives());
        boolean changed = false;
        for (EventAnnotator annotator : annotators) {
            if (!annotator.types().contains(event.getType())) {
                continue;
            }
            try {
                EventAnnotator.Result result = annotator.annotate(event, ctx);
                if (result == null || result.isEmpty()) {
                    continue;
                }
                if (result.context() != null) {
                    context.putAll(result.context());
                }
                alternatives = mergeAlternatives(alternatives, result.alternatives());
                for (Map<String, Object> member : result.contextMembers()) {
                    if (saveContextMember(event.getId(), member, now)) {
                        event.setPrivateEvent(true);
                    }
                }
                changed = true;
            } catch (RuntimeException e) {
                log.warn("annotator {} failed for {}: {}", annotator.id(), event.getId(), e.getMessage());
            }
        }
        if (changed) {
            event.setContext(context);
            event.setAlternatives(alternatives);
            event.setUpdatedAt(now);
        }
    }

    /** Adds a context member if it is not already attached; true when it came from a private source. */
    private boolean saveContextMember(String eventId, Map<String, Object> member, Instant now) {
        String memberType = String.valueOf(member.getOrDefault("memberType", "record"));
        String sourceId = String.valueOf(member.get("sourceId"));
        String externalId = String.valueOf(member.get("externalId"));
        long seq = member.get("seq") instanceof Number n ? n.longValue() : 0L;
        boolean privateSource = visibility.isPrivateSource(sourceId);
        if (!members.findByEventIdAndMemberTypeAndSourceIdAndExternalIdAndSeq(eventId, memberType, sourceId, externalId,
                seq).isEmpty()) {
            return privateSource;
        }
        IntelEventMemberEntity entity = new IntelEventMemberEntity(eventId, memberType, sourceId, externalId, seq);
        entity.setRole(String.valueOf(member.getOrDefault("role", "context")));
        entity.setVersion(member.get("version") instanceof Number n ? n.intValue() : 0);
        entity.setKind(String.valueOf(member.getOrDefault("kind", "")));
        entity.setObservedAt(parseInstant(member.get("observedAt")));
        entity.setJoinReason(asMap(member.get("joinReason")));
        entity.setSignalFamily(member.get("signalFamily") == null ? null : String.valueOf(member.get("signalFamily")));
        entity.setWeight(member.get("weight") instanceof Number n ? n.doubleValue() : 0.0);
        entity.setSnapshot(SnapshotCaps.cap(asMap(member.get("snapshot")), SnapshotCaps.MEMBER_MAX_BYTES, json));
        entity.setAddedAt(now);
        entity.setAddedBy("annotator");
        members.save(entity);
        return privateSource;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    private static Instant parseInstant(Object value) {
        if (value == null || "null".equals(value)) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------- helpers

    /** A row of a version. */
    record MemberRow(String sourceId, String externalId, String kind, int version, long seq, Instant observedAt,
            Geometry geom, Map<String, Object> payload) {}

    private static final String SELECT = "SELECT seq, source_id, external_id, kind, version, observed_at,"
            + " ST_AsText(geom) AS wkt, payload::text AS payload FROM record_version";

    private List<MemberRow> versions(List<String> kinds, long from, long to) {
        if (kinds.isEmpty()) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        args.add(from);
        args.add(to);
        args.addAll(kinds);
        return query(SELECT + " WHERE seq > ? AND seq <= ? AND tombstone = false AND persist_reason <> 8 AND kind IN ("
                + placeholders(kinds.size()) + ") ORDER BY seq", args);
    }

    /** Older versions ({@code seq ≤ from}) of {@code kinds} observed in {@code [observedFrom, observedTo]}. */
    private List<MemberRow> lookbackVersions(List<String> kinds, long from, Instant observedFrom, Instant observedTo) {
        if (kinds.isEmpty() || from <= 0) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        args.add(from);
        args.add(Timestamp.from(observedFrom));
        args.add(Timestamp.from(observedTo));
        args.addAll(kinds);
        args.add(LOOKBACK_LIMIT);
        return query(SELECT + " WHERE seq <= ? AND observed_at >= ? AND observed_at <= ? AND tombstone = false"
                + " AND persist_reason <> 8 AND kind IN (" + placeholders(kinds.size()) + ") ORDER BY seq DESC LIMIT ?",
                args);
    }

    private List<MemberRow> query(String sql, List<Object> args) {
        List<MemberRow> out = new ArrayList<>();
        jdbc.query(sql, rs -> {
            String text = rs.getString("payload");
            Map<String, Object> payload = text == null ? Map.of() : json.readValue(text, MAP);
            Instant observed = rs.getObject("observed_at", java.time.OffsetDateTime.class).toInstant();
            out.add(new MemberRow(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                    rs.getInt("version"), rs.getLong("seq"), observed, wkt(rs.getString("wkt")), payload));
        }, args.toArray());
        out.sort(Comparator.comparingLong(MemberRow::seq));
        return out;
    }

    private static String placeholders(int n) {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < n; i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }
        return placeholders.toString();
    }

    private static Geometry wkt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            Geometry geometry = new WKTReader(Geo.factory()).read(text);
            geometry.setSRID(Geo.SRID);
            return geometry;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Greedy clustering of the seed rows by distance and time. Only seed kinds form clusters; joins and
     * contradictions are evaluated against each cluster afterwards.
     */
    static List<List<MemberRow>> cluster(List<MemberRow> rows, List<String> seedKinds, double km, Duration window) {
        List<List<MemberRow>> clusters = new ArrayList<>();
        List<Coordinate> centroids = new ArrayList<>();
        List<Instant> lastTimes = new ArrayList<>();
        List<MemberRow> sorted = new ArrayList<>(rows);
        sorted.removeIf(row -> !seedKinds.contains(row.kind()));
        sorted.sort(Comparator.comparing(MemberRow::observedAt).thenComparingLong(MemberRow::seq));
        for (MemberRow row : sorted) {
            Coordinate coordinate = coordinate(row);
            int target = -1;
            for (int i = 0; i < clusters.size(); i++) {
                if (coordinate != null && centroids.get(i) != null
                        && Geo.haversineKm(coordinate.y, coordinate.x, centroids.get(i).y, centroids.get(i).x) <= km
                        && Duration.between(lastTimes.get(i), row.observedAt()).abs().compareTo(window) <= 0) {
                    target = i;
                    break;
                }
            }
            if (target < 0) {
                clusters.add(new ArrayList<>());
                centroids.add(coordinate);
                lastTimes.add(row.observedAt());
                target = clusters.size() - 1;
            }
            clusters.get(target).add(row);
            if (row.observedAt().isAfter(lastTimes.get(target))) {
                lastTimes.set(target, row.observedAt());
                centroids.set(target, coordinate);
            }
        }
        return clusters;
    }

    private static Coordinate centroidOf(List<MemberRow> cluster) {
        double lon = 0;
        double lat = 0;
        int n = 0;
        for (MemberRow row : cluster) {
            Coordinate coordinate = coordinate(row);
            if (coordinate != null) {
                lon += coordinate.x;
                lat += coordinate.y;
                n++;
            }
        }
        return n == 0 ? null : new Coordinate(lon / n, lat / n);
    }

    /** The smallest distance from {@code point} to a seed of the cluster, km; null when no seed has geometry. */
    static Double nearestKm(List<MemberRow> cluster, Coordinate point) {
        Double best = null;
        for (MemberRow row : cluster) {
            Coordinate coordinate = coordinate(row);
            if (coordinate == null) {
                continue;
            }
            double km = Geo.haversineKm(point.y, point.x, coordinate.y, coordinate.x);
            best = best == null ? km : Math.min(best, km);
        }
        return best;
    }

    private static long nearestMinutes(List<MemberRow> cluster, Instant at) {
        long best = Long.MAX_VALUE;
        for (MemberRow row : cluster) {
            best = Math.min(best, Math.abs(Duration.between(row.observedAt(), at).toMinutes()));
        }
        return best == Long.MAX_VALUE ? 0 : best;
    }

    private ai.foresignal.reference.RegionTags regionOf(MemberRow row, Map<Long, ai.foresignal.reference.RegionTags> cache) {
        return cache.computeIfAbsent(row.seq(), seq -> {
            try {
                return row.geom() == null ? ai.foresignal.reference.RegionTags.EMPTY : regions.tag(row.geom());
            } catch (RuntimeException e) {
                return ai.foresignal.reference.RegionTags.EMPTY;
            }
        });
    }

    private ai.foresignal.reference.RegionTags regionOf(List<MemberRow> cluster,
            Map<Long, ai.foresignal.reference.RegionTags> cache) {
        for (MemberRow row : cluster) {
            ai.foresignal.reference.RegionTags tags = regionOf(row, cache);
            if (tags.iso2() != null || tags.admin1() != null) {
                return tags;
            }
        }
        return ai.foresignal.reference.RegionTags.EMPTY;
    }

    /** The shared region tag for {@code sameRegion: admin1|country} (admin1 falls back to country), or null. */
    static String sameRegion(String level, ai.foresignal.reference.RegionTags a, ai.foresignal.reference.RegionTags b) {
        if (a == null || b == null) {
            return null;
        }
        if ("admin1".equals(level) && a.admin1() != null && a.admin1().equals(b.admin1())) {
            return a.admin1();
        }
        if ("admin1".equals(level) && a.admin1() != null && b.admin1() != null) {
            return null;
        }
        return a.iso2() != null && a.iso2().equals(b.iso2()) ? a.iso2() : null;
    }

    private IntelligenceEventEntity findOpen(Candidate candidate, Set<String> keys, double radiusKm, Duration window) {
        for (IntelligenceEventEntity open : events.findByTypeAndStatusInOrderByUpdatedAtDesc(candidate.type(), OPEN)) {
            if (open.getMergedInto() != null) {
                continue;
            }
            if (Dedup.merges(candidate.type(), candidate.geom(), candidate.start(), candidate.end(), keys, open,
                    pinKeys(open.getId()), radiusKm, window)) {
                return open;
            }
        }
        return null;
    }

    /** A resolved event of the same recipe type whose end lies within {@code reopenWindow} of the candidate. */
    private IntelligenceEventEntity findReopenable(Candidate candidate, Set<String> keys, RecipeDefinition recipe) {
        Instant earliestEnd = candidate.start().minus(recipe.reopenWindow());
        for (IntelligenceEventEntity resolved : events.findByTypeAndStatusAndEndTimeGreaterThanEqualOrderByUpdatedAtDesc(
                candidate.type(), "resolved", earliestEnd)) {
            if (resolved.getMergedInto() != null || !recipe.id().equals(resolved.getRecipeId())) {
                continue;
            }
            if (Dedup.merges(candidate.type(), candidate.geom(), candidate.start(), candidate.end(), keys, resolved,
                    pinKeys(resolved.getId()), recipe.dedup().radiusKm(), recipe.reopenWindow())) {
                return resolved;
            }
        }
        return null;
    }

    private Set<String> pinKeys(String eventId) {
        Set<String> keys = new LinkedHashSet<>();
        for (IntelEventMemberEntity member : members.findByEventIdOrderByIdAsc(eventId)) {
            keys.add(memberKey(member.getMemberType(), member.getSourceId(), member.getExternalId(), member.getSeq()));
        }
        return keys;
    }

    private boolean memberExists(String eventId, MemberLine line) {
        return !members.findByEventIdAndMemberTypeAndSourceIdAndExternalIdAndSeq(eventId, line.memberType(),
                line.sourceId(), line.externalId(), line.seq()).isEmpty();
    }

    /**
     * The scorer's view of a member (116: with its evidence group, so re-reports of one originator contribute
     * once).
     */
    private EventScorer.MemberView view(MemberRow row, String family, double weight,
            Map<String, ai.foresignal.evidence.EvidenceService.GroupKey> evidenceKeys) {
        boolean healthy = health(row.sourceId()) == HealthStatus.HEALTHY;
        ai.foresignal.evidence.EvidenceService.GroupKey key =
                evidenceKeys.get(ai.foresignal.evidence.EvidenceService.recordKey(row.sourceId(), row.externalId()));
        return new EventScorer.MemberView(family, weight, row.sourceId(), healthy,
                memberReliability(row.sourceId(), row.externalId()),
                key == null || !key.linked() ? null : key.key(), key == null ? null : key.root(),
                modalityOf(row.sourceId()));
    }

    /** The sensing modality of a source (prompt 154), or null when the originator registry is absent. */
    private String modalityOf(String sourceId) {
        if (originators == null || sourceId == null) {
            return null;
        }
        try {
            return Modality.of(originators.resolve(sourceId).originatorClass());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The event quality thresholds in force: the corroboration policy's, else the defaults. */
    private EventScorer.Quality qualityThresholds() {
        return corroboration == null ? EventScorer.Quality.DEFAULT : corroboration.quality();
    }

    /** True when the candidate's members span at least {@code required} distinct sensing modalities (0 = off). */
    private static boolean meetsModalities(List<EventScorer.MemberView> views, Integer required) {
        if (required == null || required <= 0) {
            return true;
        }
        Set<String> modalities = new LinkedHashSet<>();
        for (EventScorer.MemberView view : views) {
            if (view.modality() != null) {
                modalities.add(view.modality());
            }
        }
        return modalities.size() >= required;
    }

    /** True when the event's supporting members span at least {@code required} distinct modalities (0 = off). */
    private boolean membersSpanModalities(List<IntelEventMemberEntity> all, Integer required) {
        if (required == null || required <= 0) {
            return true;
        }
        Set<String> modalities = new LinkedHashSet<>();
        for (IntelEventMemberEntity member : all) {
            if (!"contradicting".equals(member.getRole())) {
                modalities.add(modalityOf(member.getSourceId()));
            }
        }
        modalities.remove(null);
        return modalities.size() >= required;
    }

    /** The evidence identity of every row (prompt 116), in one query; empty without the evidence service. */
    private Map<String, ai.foresignal.evidence.EvidenceService.GroupKey> evidenceKeys(List<MemberRow> rows) {
        if (evidence == null || rows.isEmpty()) {
            return Map.of();
        }
        List<ai.foresignal.evidence.RecordRef> refs = new ArrayList<>();
        Set<String> seen = new java.util.HashSet<>();
        for (MemberRow row : rows) {
            if (seen.add(ai.foresignal.evidence.EvidenceService.recordKey(row.sourceId(), row.externalId()))) {
                refs.add(new ai.foresignal.evidence.RecordRef(row.sourceId(), row.externalId(), row.seq()));
            }
        }
        try {
            return evidence.groupKeys(refs);
        } catch (RuntimeException e) {
            log.warn("Evidence groups unavailable for correlation; members count separately: {}", e.toString());
            return Map.of();
        }
    }

    private MemberLine line(String role, MemberRow row, String family, double weight, Map<String, Object> clause) {
        Map<String, Object> joinReason = new LinkedHashMap<>(clause);
        joinReason.put("matchedKeys", List.of(row.sourceId() + ":" + row.externalId()));
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sourceId", row.sourceId());
        snapshot.put("externalId", row.externalId());
        snapshot.put("kind", row.kind());
        snapshot.put("version", row.version());
        snapshot.put("seq", row.seq());
        snapshot.put("observedAt", row.observedAt() == null ? null : row.observedAt().toString());
        snapshot.put("payload", row.payload());
        return new MemberLine(role, "record", row.sourceId(), row.externalId(), row.seq(), row.version(), row.kind(),
                row.observedAt(), family, weight, joinReason,
                SnapshotCaps.cap(snapshot, SnapshotCaps.MEMBER_MAX_BYTES, json));
    }

    private HealthStatus health(String sourceId) {
        try {
            SourceHealthView view = health.health(sourceId);
            return view == null || view.status() == null ? HealthStatus.IDLE : view.status();
        } catch (RuntimeException e) {
            return HealthStatus.IDLE;
        }
    }

    private List<String> regionTags(Geometry geom) {
        try {
            ai.foresignal.reference.RegionTags tags = regions.tag(geom);
            List<String> out = new ArrayList<>();
            if (tags.iso2() != null) {
                out.add(tags.iso2());
            }
            if (tags.admin1() != null) {
                out.add(tags.admin1());
            }
            return out;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    static Geometry geometry(List<Geometry> geometries) {
        if (geometries.isEmpty()) {
            return null;
        }
        return unionAll(geometries).buffer(0.05);
    }

    /**
     * The union of {@code geometries} in one unary union. Folding pairwise fails once a point and a
     * polygon apart union to a GeometryCollection, which {@link Geometry#union(Geometry)} refuses
     * ("Operation does not support GeometryCollection arguments"); the unary union accepts any mix.
     */
    static Geometry unionAll(List<Geometry> geometries) {
        return Geo.factory().buildGeometry(geometries).union();
    }

    private static Coordinate coordinate(MemberRow row) {
        Geometry geom = row.geom();
        if (geom == null || geom.isEmpty()) {
            return null;
        }
        Point centroid = geom.getCentroid();
        return new Coordinate(centroid.getX(), centroid.getY());
    }

    static String memberKey(String memberType, String sourceId, String externalId, long seq) {
        return memberType + "|" + sourceId + "|" + externalId + "|" + seq;
    }

    /** The non-null values at {@code path} across the cluster's members (prompt 157, the shared-attribute join). */
    static java.util.Set<Object> sharedAttributeValues(List<MemberRow> cluster, String path) {
        java.util.Set<Object> values = new java.util.HashSet<>();
        for (MemberRow member : cluster) {
            Object value = ai.foresignal.ingest.JsonValues.at(member.payload(), path);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    /**
     * The candidate's value at {@code path} when it is shared with at least one cluster member (prompt 157), else
     * null — the decision of a shared-attribute join.
     */
    static Object sharedAttributeMatch(MemberRow candidate, List<MemberRow> cluster, String path) {
        Object value = ai.foresignal.ingest.JsonValues.at(candidate.payload(), path);
        if (value == null) {
            return null;
        }
        return sharedAttributeValues(cluster, path).contains(value) ? value : null;
    }

    private static double seedKm(RecipeDefinition recipe) {
        return seedDistance(recipe);
    }

    private static double seedDistance(RecipeDefinition recipe) {
        for (RecipeDefinition.Join join : recipe.joins()) {
            if (join.withinKm() != null) {
                return join.withinKm();
            }
        }
        return DEFAULT_KM;
    }

    private static Duration seedWindow(RecipeDefinition recipe) {
        for (RecipeDefinition.Join join : recipe.joins()) {
            if (join.after() != null) {
                return join.after().plus(Duration.ofMinutes(1));
            }
        }
        return DEFAULT_WINDOW;
    }

    /** The widest window of the recipe: the look-back a chunk needs so boundary signals correlate. */
    static Duration lookback(RecipeDefinition recipe) {
        Duration widest = seedWindow(recipe);
        for (RecipeDefinition.Join join : recipe.joins()) {
            widest = longest(widest, join.before() == null ? DEFAULT_WINDOW : join.before());
            widest = longest(widest, join.after() == null ? DEFAULT_WINDOW : join.after());
        }
        for (RecipeDefinition.Contradiction contradiction : recipe.contradictions()) {
            if (contradiction.window() != null) {
                widest = longest(widest, contradiction.window());
            }
        }
        return widest.multipliedBy(2);
    }

    private static Duration longest(Duration a, Duration b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    private static Duration dedupWindow(RecipeDefinition recipe) {
        return recipe.dedup().window() == null ? Duration.ofHours(6) : recipe.dedup().window();
    }

    private static String title(List<MemberLine> lines, Set<String> families) {
        Set<String> sources = new TreeSet<>();
        for (MemberLine line : lines) {
            if (!"contradicting".equals(line.role())) {
                sources.add(line.sourceId());
            }
        }
        return counted(supportingCount(lines), "signal") + " from " + counted(sources.size(), "source") + ": "
                + String.join(", ", families);
    }

    /** {@code n} and {@code noun}, plural unless {@code n} is 1: "1 signal", "3 signals". */
    static String counted(long n, String noun) {
        return n + " " + noun + (n == 1 ? "" : "s");
    }

    private static long supportingCount(List<MemberLine> lines) {
        return lines.stream().filter(l -> !"contradicting".equals(l.role())).count();
    }

    private static String explanation(List<MemberLine> lines, Set<String> families, Instant start, Instant end) {
        Set<String> sources = new TreeSet<>();
        for (MemberLine line : lines) {
            if (!"contradicting".equals(line.role())) {
                sources.add(line.sourceId());
            }
        }
        long hours = start == null || end == null ? 0 : Duration.between(start, end).toHours();
        return counted(supportingCount(lines), "signal") + " from " + counted(sources.size(), "independent source")
                + " (" + String.join(", ", families) + ") over " + hours + " h; evidence pinned to versions.";
    }

    /** The title derived from all stored members. */
    static String titleOf(List<IntelEventMemberEntity> all) {
        Set<String> sources = new TreeSet<>();
        Set<String> families = new TreeSet<>();
        long n = 0;
        for (IntelEventMemberEntity member : all) {
            if ("contradicting".equals(member.getRole())) {
                continue;
            }
            n++;
            sources.add(member.getSourceId());
            if (member.getSignalFamily() != null) {
                families.add(member.getSignalFamily());
            }
        }
        return counted(n, "signal") + " from " + counted(sources.size(), "source") + ": " + String.join(", ", families);
    }

    /** The deterministic explanation derived from all stored members. */
    static String explanationOf(List<IntelEventMemberEntity> all, Instant start, Instant last) {
        Set<String> sources = new TreeSet<>();
        Set<String> families = new TreeSet<>();
        long n = 0;
        long contradicting = 0;
        for (IntelEventMemberEntity member : all) {
            if ("contradicting".equals(member.getRole())) {
                contradicting++;
                continue;
            }
            n++;
            sources.add(member.getSourceId());
            if (member.getSignalFamily() != null) {
                families.add(member.getSignalFamily());
            }
        }
        long hours = start == null || last == null ? 0 : Math.max(0, Duration.between(start, last).toHours());
        return counted(n, "signal") + " from " + counted(sources.size(), "independent source") + " ("
                + String.join(", ", families) + ") over " + hours + " h"
                + (contradicting == 0 ? "" : "; " + contradicting + " contradicting")
                + "; evidence pinned to versions.";
    }

    private void storeCursor(long seq) {
        cursor = seq;
        settings.put(SettingKeys.INTEL_CURSOR, seq);
    }

    private static Instant min(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }

    private static Instant max(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }
}
