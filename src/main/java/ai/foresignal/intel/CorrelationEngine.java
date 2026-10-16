package ai.foresignal.intel;

import ai.foresignal.collect.HealthService;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.visibility.SourceVisibility;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The deterministic correlation engine (prompt 101). A {@link ChangeBus} subscriber with a durable
 * {@code intel.cursor} that catches up in chunks: for each enabled recipe it clusters the seed
 * versions of its kinds, adds join members within the recipe's distance/region/entity and time
 * windows, subtracts contradictions, scores the confidence with {@link EventScorer}, dedups against
 * open events and writes the event, its members and its log. It runs on its own virtual thread and
 * never blocks ingest.
 *
 * <p>Re-running the same seq range yields the same events: the inputs are stored versions only.
 */
@Component
public class CorrelationEngine implements ChangeListener, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(CorrelationEngine.class);

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "intel-correlation";

    /** The seq chunk processed per pass. */
    static final long CHUNK = 10_000;

    /** The default join distance when a recipe names none, km. */
    static final double DEFAULT_KM = 150;

    /** The default seed window when a recipe names none. */
    static final Duration DEFAULT_WINDOW = Duration.ofHours(12);

    /** The types whose open events dedup scans. */
    static final List<String> OPEN = List.of("emerging", "active", "reopened");

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
    private final SourceVisibility visibility;
    private final ai.foresignal.reference.RegionTagger regions;
    private final IntelEventBus bus;
    private final Clock clock;

    private volatile Subscription subscription;
    private volatile Thread thread;
    private volatile boolean running;
    private volatile long cursor = -1;

    /** The annotators that add context to new events (prompt 105); empty when none registered. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private List<EventAnnotator> annotators = List.of();

    /** The derived-record heads the annotators read (prompt 105), or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.derive.DerivedRecords derivedRecords;

    /** Creates the engine. */
    public CorrelationEngine(ChangeBus changeBus, JdbcTemplate jdbc, JsonMapper json, AppSettingService settings,
            RecipeRegistry recipes, IntelligenceEventRepository events, IntelEventMemberRepository members,
            IntelEventLogRepository logs, HealthService health,
            @Lazy SourceVisibility visibility, ai.foresignal.reference.RegionTagger regions, IntelEventBus bus,
            Clock clock) {
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

    /** Processes every version between the cursor and the watermark. */
    public void catchUp() {
        long watermark = changeBus.lastPublishedSeq();
        long from = cursor();
        while (from < watermark && running) {
            long to = Math.min(from + CHUNK, watermark);
            for (RecipeDefinition recipe : recipes.enabledDefinitions()) {
                try {
                    correlate(recipe, from, to);
                } catch (RuntimeException e) {
                    log.warn("Recipe {} failed on seq {}..{}: {}", recipe.id(), from, to, e.toString());
                }
            }
            from = to;
            storeCursor(from);
        }
    }

    // ------------------------------------------------------------------------- correlation

    private void correlate(RecipeDefinition recipe, long from, long to) {
        List<MemberRow> rows = versions(recipe.allKinds(), from, to);
        if (rows.isEmpty()) {
            return;
        }
        double seedKm = seedDistance(recipe);
        Duration seedWindow = seedWindow(recipe);
        List<List<MemberRow>> clusters = cluster(rows, recipe.seed().kinds(), seedKm, seedWindow);
        for (List<MemberRow> cluster : clusters) {
            Candidate candidate = build(recipe, cluster);
            if (candidate == null) {
                continue;
            }
            persist(recipe, candidate, to);
        }
    }

    /** A candidate event before persistence. */
    record Candidate(String type, String title, String severity, Instant start, Instant end, Geometry geom,
            Map<String, Object> detector, EventScorer.Score score, List<MemberLine> members, int contradictions,
            List<Map<String, Object>> alternatives, String explanation, boolean privateEvent) {}

    /** One member line. */
    record MemberLine(String role, String memberType, String sourceId, String externalId, long seq, Integer version,
            String kind, Instant observedAt, String signalFamily, double weight, Map<String, Object> joinReason,
            Map<String, Object> snapshot) {}

    private Candidate build(RecipeDefinition recipe, List<MemberRow> cluster) {
        List<MemberLine> lines = new ArrayList<>();
        Set<String> families = new LinkedHashSet<>();
        List<EventScorer.MemberView> views = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        Instant start = null;
        Instant end = null;
        boolean anyPrivate = false;
        List<Geometry> geometries = new ArrayList<>();
        for (MemberRow row : cluster) {
            String family = seedFamily(row.kind());
            lines.add(line("supporting", row, family, 0.6, Map.of("clause", "seed")));
            families.add(family);
            views.add(view(row, family, 0.6));
            keys.add(memberKey("record", row.sourceId(), row.externalId(), row.seq()));
            start = min(start, row.observedAt());
            end = max(end, row.observedAt());
            anyPrivate = anyPrivate || privateSource(row.sourceId());
            if (row.geom() != null) {
                geometries.add(row.geom());
            }
        }
        List<MemberRow> extra = new ArrayList<>(cluster);
        for (RecipeDefinition.Join join : recipe.joins()) {
            for (MemberRow row : extra) {
                if (!join.kinds().contains(row.kind())) {
                    continue;
                }
                String family = join.signalFamily() == null ? seedFamily(row.kind()) : join.signalFamily();
                lines.add(line(join.role() == null ? "context" : join.role(), row, family, join.weight(),
                        Map.of("clause", "join")));
                families.add(family);
                views.add(view(row, family, join.weight()));
                keys.add(memberKey("record", row.sourceId(), row.externalId(), row.seq()));
                anyPrivate = anyPrivate || privateSource(row.sourceId());
            }
        }
        if (lines.size() < recipe.minMembers() || families.size() < recipe.minSignalFamilies()) {
            return null;
        }
        int contradictions = 0;
        List<Map<String, Object>> alternatives = new ArrayList<>();
        for (RecipeDefinition.Contradiction contradiction : recipe.contradictions()) {
            for (String source : contradiction.healthOf()) {
                if (health(source) != HealthStatus.HEALTHY) {
                    contradictions++;
                    alternatives.add(Map.of("hypothesis", contradiction.alternative() == null ? "unknown" : contradiction.alternative(),
                            "basis", "source " + source + " is not healthy", "supportingMemberIds", List.of()));
                }
            }
        }
        EventScorer.Score score = EventScorer.score(views, contradictions);
        Geometry geom = geometry(geometries);
        boolean active = lines.size() >= recipe.activateAt().members()
                && (recipe.activateAt().confidence() == null || score.confidence() >= recipe.activateAt().confidence());
        if (!active) {
            return null;
        }
        String severity = recipe.severityMap().getOrDefault("default", "warning");
        String title = title(lines, families, geom);
        String explanation = explanation(lines, families, geom);
        Map<String, Object> detector = Map.of("name", EventScorer.NAME, "version", EventScorer.VERSION,
                "recipeId", recipe.id(), "recipeVersion", recipe.version());
        return new Candidate(recipe.type(), title, severity, start, end, geom, detector, score, lines, contradictions,
                alternatives, explanation, anyPrivate);
    }

    private void persist(RecipeDefinition recipe, Candidate candidate, long watermark) {
        Set<String> memberKeys = new LinkedHashSet<>();
        for (MemberLine line : candidate.members()) {
            memberKeys.add(memberKey(line.memberType(), line.sourceId(), line.externalId(), line.seq()));
        }
        IntelligenceEventEntity open = findOpen(candidate.type(), candidate.geom(), candidate.start(), candidate.end(),
                memberKeys, recipe.dedup());
        Instant now = clock.instant();
        boolean created = open == null;
        IntelligenceEventEntity event = created
                ? new IntelligenceEventEntity(Ulids.next(now), candidate.type(), candidate.title(), candidate.start())
                : open;
        event.setTitle(candidate.title());
        event.setSeverity(candidate.severity());
        event.setStatus("active");
        event.setStartTime(candidate.start());
        event.setEndTime(null);
        event.setLastEvidenceAt(candidate.end() == null ? now : candidate.end());
        event.setGeom(candidate.geom());
        event.setCentroid(candidate.geom() == null ? null : candidate.geom().getCentroid());
        event.setConfidence(candidate.score().confidence());
        event.setConfidenceLabel(candidate.score().label());
        event.setConfidenceMethod(EventScorer.METHOD);
        event.setEvidenceQuality(candidate.score().evidenceQuality());
        event.setDetector(candidate.detector());
        event.setRecipeId(recipe.id());
        event.setRecipeVersion(recipe.version());
        event.setOrigin("recipe");
        event.setExplanation(candidate.explanation());
        event.setAlternatives(candidate.alternatives());
        event.setPrivateEvent(event.isPrivateEvent() || candidate.privateEvent());
        event.setRegionTags(regionTags(candidate.geom()));
        if (created) {
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);
        events.save(event);
        int added = 0;
        for (MemberLine line : candidate.members()) {
            if (memberExists(event.getId(), line)) {
                continue;
            }
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
        applyAnnotators(event, now);
        IntelEventLogEntity logRow = new IntelEventLogEntity(event.getId(), now, "recipe:" + recipe.id(),
                created ? "created" : "member_added");
        logRow.setAfter(Map.of("members", added, "confidence", candidate.score().confidence()));
        logs.save(logRow);
        bus.publish(new IntelEvent(logRow.getId(), event.getId(), created ? IntelEvent.CREATED : IntelEvent.UPDATED,
                event.getVersion(), IntelEventMapper.summary(event), null));
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
                for (Map<String, Object> alternative : result.alternatives()) {
                    // The annotator runs on every recipe hit: an alternative with the same hypothesis replaces
                    // the stored one instead of piling up (prompt 105 post-review).
                    Object hypothesis = alternative.get("hypothesis");
                    alternatives.removeIf(a -> hypothesis != null && hypothesis.equals(a.get("hypothesis")));
                    alternatives.add(alternative);
                }
                for (Map<String, Object> member : result.contextMembers()) {
                    saveContextMember(event.getId(), member, now);
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
            events.save(event);
        }
    }

    /** Adds a context member if it is not already attached. */
    private void saveContextMember(String eventId, Map<String, Object> member, Instant now) {
        String memberType = String.valueOf(member.getOrDefault("memberType", "record"));
        String sourceId = String.valueOf(member.get("sourceId"));
        String externalId = String.valueOf(member.get("externalId"));
        long seq = member.get("seq") instanceof Number n ? n.longValue() : 0L;
        if (!members.findByEventIdAndMemberTypeAndSourceIdAndExternalIdAndSeq(eventId, memberType, sourceId, externalId,
                seq).isEmpty()) {
            return;
        }
        IntelEventMemberEntity entity = new IntelEventMemberEntity(eventId, memberType, sourceId, externalId, seq);
        entity.setRole(String.valueOf(member.getOrDefault("role", "context")));
        entity.setVersion(member.get("version") instanceof Number n ? n.intValue() : 0);
        entity.setKind(String.valueOf(member.getOrDefault("kind", "")));
        entity.setObservedAt(parseInstant(member.get("observedAt")));
        entity.setJoinReason(asMap(member.get("joinReason")));
        entity.setSignalFamily(member.get("signalFamily") == null ? null : String.valueOf(member.get("signalFamily")));
        entity.setWeight(member.get("weight") instanceof Number n ? n.doubleValue() : 0.0);
        entity.setSnapshot(asMap(member.get("snapshot")));
        entity.setAddedAt(now);
        entity.setAddedBy("annotator");
        members.save(entity);
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

    private List<MemberRow> versions(List<String> kinds, long from, long to) {
        if (kinds.isEmpty()) {
            return List.of();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < kinds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }
        String sql = "SELECT seq, source_id, external_id, kind, version, observed_at, ST_AsText(geom) AS wkt,"
                + " payload::text AS payload FROM record_version"
                + " WHERE seq > ? AND seq <= ? AND tombstone = false AND persist_reason <> 8 AND kind IN ("
                + placeholders + ") ORDER BY seq";
        List<Object> args = new ArrayList<>();
        args.add(from);
        args.add(to);
        args.addAll(kinds);
        List<MemberRow> out = new ArrayList<>();
        jdbc.query(sql, rs -> {
            String text = rs.getString("payload");
            Map<String, Object> payload = text == null ? Map.of() : json.readValue(text, MAP);
            Instant observed = rs.getObject("observed_at", java.time.OffsetDateTime.class).toInstant();
            out.add(new MemberRow(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                    rs.getInt("version"), rs.getLong("seq"), observed, wkt(rs.getString("wkt")), payload));
        }, args.toArray());
        return out;
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

    /** Greedy clustering of the seed rows by distance and time. */
    static List<List<MemberRow>> cluster(List<MemberRow> rows, List<String> seedKinds, double km, Duration window) {
        List<List<MemberRow>> clusters = new ArrayList<>();
        List<Coordinate> centroids = new ArrayList<>();
        List<Instant> lastTimes = new ArrayList<>();
        List<MemberRow> sorted = new ArrayList<>(rows);
        sorted.removeIf(row -> !seedKinds.contains(row.kind()));
        sorted.sort(java.util.Comparator.comparing(MemberRow::observedAt));
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
        // Add the non-seed rows near each cluster (within the seed distance).
        for (List<MemberRow> cluster : clusters) {
            Coordinate centroid = centroidOf(cluster);
            for (MemberRow row : rows) {
                if (seedKinds.contains(row.kind()) || cluster.contains(row)) {
                    continue;
                }
                Coordinate coordinate = coordinate(row);
                if (centroid == null || coordinate == null
                        || Geo.haversineKm(coordinate.y, coordinate.x, centroid.y, centroid.x) <= km) {
                    cluster.add(row);
                }
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

    private IntelligenceEventEntity findOpen(String type, Geometry geom, Instant start, Instant end, Set<String> keys,
            RecipeDefinition.Dedup dedup) {
        for (IntelligenceEventEntity open : events.findByTypeAndStatusInOrderByUpdatedAtDesc(type, OPEN)) {
            Set<String> openKeys = new LinkedHashSet<>();
            for (IntelEventMemberEntity member : members.findByEventIdOrderByIdAsc(open.getId())) {
                openKeys.add(memberKey(member.getMemberType(), member.getSourceId(), member.getExternalId(),
                        member.getSeq()));
            }
            if (Dedup.merges(type, geom, start, end, keys, open, openKeys, dedup.radiusKm(),
                    dedup.window() == null ? Duration.ofHours(6) : dedup.window())) {
                return open;
            }
        }
        return null;
    }

    private boolean memberExists(String eventId, MemberLine line) {
        return !members.findByEventIdAndMemberTypeAndSourceIdAndExternalIdAndSeq(eventId, line.memberType(),
                line.sourceId(), line.externalId(), line.seq()).isEmpty();
    }

    private EventScorer.MemberView view(MemberRow row, String family, double weight) {
        boolean healthy = health(row.sourceId()) == HealthStatus.HEALTHY;
        return new EventScorer.MemberView(family, weight, row.sourceId(), healthy, null);
    }

    private MemberLine line(String role, MemberRow row, String family, double weight, Map<String, Object> clause) {
        Map<String, Object> joinReason = new LinkedHashMap<>(clause);
        joinReason.put("matchedKeys", List.of(row.sourceId() + ":" + row.externalId()));
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sourceId", row.sourceId());
        snapshot.put("externalId", row.externalId());
        snapshot.put("kind", row.kind());
        snapshot.put("observedAt", row.observedAt() == null ? null : row.observedAt().toString());
        snapshot.put("payload", row.payload());
        return new MemberLine(role, "record", row.sourceId(), row.externalId(), row.seq(), row.version(), row.kind(),
                row.observedAt(), family, weight, joinReason, snapshot);
    }

    private HealthStatus health(String sourceId) {
        try {
            return health.health(sourceId).status();
        } catch (RuntimeException e) {
            return HealthStatus.IDLE;
        }
    }

    private boolean privateSource(String sourceId) {
        try {
            return visibility.privateSourceIds().contains(sourceId);
        } catch (RuntimeException e) {
            return false;
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

    private static Geometry geometry(List<Geometry> geometries) {
        if (geometries.isEmpty()) {
            return null;
        }
        GeometryCollectionish collection = new GeometryCollectionish(geometries);
        return collection.union();
    }

    /** Small helper to union member geometries into the affected area. */
    private record GeometryCollectionish(List<Geometry> parts) {

        Geometry union() {
            Geometry[] array = parts.toArray(Geometry[]::new);
            Geometry combined = array[0];
            for (int i = 1; i < array.length; i++) {
                combined = combined.union(array[i]);
            }
            return combined.buffer(0.05);
        }
    }

    private static Coordinate coordinate(MemberRow row) {
        Geometry geom = row.geom();
        if (geom == null || geom.isEmpty()) {
            return null;
        }
        Point centroid = geom.getCentroid();
        return new Coordinate(centroid.getX(), centroid.getY());
    }

    private static String memberKey(String memberType, String sourceId, String externalId, long seq) {
        return memberType + "|" + sourceId + "|" + externalId + "|" + seq;
    }

    private static String seedFamily(String kind) {
        if (kind == null) {
            return SignalFamilies.CONFLICT;
        }
        return switch (kind) {
            case "ais-gap", "loiter", "rendezvous", "chokepoint-uturn", "chokepoint-activity" -> SignalFamilies.AIS_BEHAVIOUR;
            case "emergency-squawk", "holding-pattern" -> SignalFamilies.AVIATION_BEHAVIOUR;
            case "jamming-cell" -> SignalFamilies.GNSS_INTERFERENCE;
            case "outage", "internet-outage", "bgp-prefix-state" -> SignalFamilies.INTERNET_MEASUREMENT;
            case "grid-frequency" -> SignalFamilies.GRID_FREQUENCY;
            case "earthquake", "shakemap-mmi", "earthquake-impact" -> SignalFamilies.SEISMIC;
            case "fire-detection", "fire-cluster" -> SignalFamilies.FIRE;
            case "chokepoint-transit", "nav-warning" -> SignalFamilies.NAV_WARNING;
            case "sigmets", "tfr", "ground-stop", "delay" -> SignalFamilies.AVIATION_RESTRICTION;
            default -> SignalFamilies.CONFLICT;
        };
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

    private static String title(List<MemberLine> lines, Set<String> families, Geometry geom) {
        return lines.size() + " signals from " + independentSources(lines) + " sources: "
                + String.join(", ", families);
    }

    private static String explanation(List<MemberLine> lines, Set<String> families, Geometry geom) {
        return lines.size() + " signals from " + independentSources(lines) + " independent sources ("
                + String.join(", ", families) + "); " + lines.get(0).role() + " evidence pinned to versions.";
    }

    private static int independentSources(List<MemberLine> lines) {
        Set<String> sources = new TreeSet<>();
        for (MemberLine line : lines) {
            sources.add(line.sourceId());
        }
        return sources.size();
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
