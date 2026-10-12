package ai.foresignal.system;

import ai.foresignal.collect.CollectorScheduler;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SchedulerState;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.ingest.WriterState;
import ai.foresignal.ingest.WriterStatus;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import ai.foresignal.store.tiles.TileProperties;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamLimits;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Builds the {@code SystemInfo} document of {@code GET /v1/system} (prompt 019).
 *
 * <p>The anonymous subset is {@code version}, {@code apiVersion}, {@code startedAt},
 * {@code uptimeSeconds}, {@code status}, {@code features} and {@code limits}. The full document, for
 * any caller with a valid token, adds {@code database}, {@code records}, {@code sources},
 * {@code scheduler}, {@code ingestWriter}, {@code streams}, {@code notifications}, {@code history},
 * {@code catchUp} and {@code warnings}.
 *
 * <p>{@code status}: {@code down} when the database does not answer or the writer is stopped or has
 * lost its lock; {@code degraded} when the writer is holding for the clock, the node is catching up,
 * a source is failing or a warning-or-worse applies; otherwise {@code ok}.
 */
@Service
public class SystemInfoService {

    private static final Logger log = LoggerFactory.getLogger(SystemInfoService.class);

    /** The largest {@code limit} of {@code GET /v1/records} and {@code GET /v1/history} (014). */
    public static final int PAGE_MAX = 5_000;

    /** Push ingest caps per request (Architecture §5, "Ingest endpoint"; enforced from prompt 024). */
    public static final long INGEST_MAX_BYTES = 5L * 1024 * 1024;

    /** Push ingest records per request (Architecture §5). */
    public static final int INGEST_MAX_RECORDS = 10_000;

    private final ForesignalVersion version;
    private final ApiVersion apiVersion;
    private final List<FeatureContributor> contributors;
    private final JdbcTemplate jdbc;
    private final ReadinessCheck readiness;
    private final IngestWriter writer;
    private final IngestQueue queue;
    private final ChangeBus changeBus;
    private final SourceRegistry registry;
    private final SourceSettingsRepository sourceSettings;
    private final HealthService health;
    private final CollectorScheduler scheduler;
    private final SseHub hub;
    private final HistoryStats history;
    private final CatchUpMonitor catchUp;
    private final SystemWarnings warnings;
    private final OperatorGauges gauges;
    private final TileProperties tiles;
    private final Clock clock;
    private final Instant startedAt;

    /** Creates the service; {@code startedAt} is now. */
    public SystemInfoService(ForesignalVersion version, ApiVersion apiVersion, List<FeatureContributor> contributors,
            JdbcTemplate jdbc, ReadinessCheck readiness, IngestWriter writer, IngestQueue queue, ChangeBus changeBus,
            SourceRegistry registry, SourceSettingsRepository sourceSettings, HealthService health,
            CollectorScheduler scheduler, SseHub hub, HistoryStats history, CatchUpMonitor catchUp,
            SystemWarnings warnings, OperatorGauges gauges, TileProperties tiles, Clock clock) {
        this.version = version;
        this.apiVersion = apiVersion;
        this.contributors = List.copyOf(contributors);
        this.jdbc = jdbc;
        this.readiness = readiness;
        this.writer = writer;
        this.queue = queue;
        this.changeBus = changeBus;
        this.registry = registry;
        this.sourceSettings = sourceSettings;
        this.health = health;
        this.scheduler = scheduler;
        this.hub = hub;
        this.history = history;
        this.catchUp = catchUp;
        this.warnings = warnings;
        this.gauges = gauges;
        this.tiles = tiles;
        this.clock = clock;
        this.startedAt = clock.instant();
    }

    /**
     * The document.
     *
     * @param full true for a caller with a valid token
     */
    public Map<String, Object> build(boolean full) {
        Instant now = clock.instant();
        boolean dbUp = readiness.databaseUp();
        WriterStatus writerStatus = writer.status();
        // The last scheduled reading: refreshing here could wait on the trigger engine mid-chunk.
        CatchUpMonitor.CatchUp catching = catchUp.last();
        HistoryStats.Snapshot historySnapshot = dbUp ? safeHistory() : null;
        List<SystemWarnings.Warning> warningList = dbUp ? warnings.warnings(historySnapshot) : List.of();
        List<SourceHealthView> sourceHealth = dbUp ? safeHealth() : List.of();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("version", version.version());
        out.put("apiVersion", apiVersion.version());
        out.put("startedAt", startedAt.toString());
        out.put("uptimeSeconds", Math.max(0L, Duration.between(startedAt, now).getSeconds()));
        out.put("status", status(dbUp, writerStatus, catching, sourceHealth, warningList));
        out.put("features", features());
        out.put("limits", limits());
        if (!full) {
            return out;
        }
        out.put("database", database(dbUp));
        Map<String, Object> records = new LinkedHashMap<>();
        records.put("current", gauges.currentRecords());
        long versions = 0;
        if (historySnapshot != null) {
            for (HistoryStats.Tier tier : historySnapshot.tiers()) {
                versions += tier.rows();
            }
        }
        records.put("versions", versions);
        records.put("lastSeq", changeBus.lastPublishedSeq());
        out.put("records", records);
        out.put("sources", sources(sourceHealth));
        SchedulerState state = scheduler.state();
        Map<String, Object> sched = new LinkedHashMap<>();
        sched.put("running", state.running());
        sched.put("scheduledSources", state.scheduledSources().size());
        sched.put("activeFetches", state.activeFetches());
        sched.put("activeStreams", state.activeStreams());
        sched.put("ingestQueueDepth", queue.depth());
        out.put("scheduler", sched);
        Map<String, Object> ingest = new LinkedHashMap<>();
        ingest.put("state", writerStatus.state().name());
        ingest.put("warnings", writerStatus.warnings());
        ingest.put("lastSeq", writerStatus.lastSeq());
        ingest.put("lastIngestedAt", writerStatus.lastIngestedAt() == null ? null : writerStatus.lastIngestedAt().toString());
        ingest.put("queueDepth", writerStatus.queueDepth());
        out.put("ingestWriter", ingest);
        SseHub.StreamStats stats = hub.stats();
        Map<String, Object> streams = new LinkedHashMap<>();
        streams.put("clients", stats.clients());
        streams.put("maxClients", stats.maxClients());
        streams.put("changeSubscribers", stats.changeSubscribers());
        streams.put("alertSubscribers", stats.alertSubscribers());
        streams.put("eventSubscribers", stats.eventSubscribers());
        out.put("streams", streams);
        out.put("notifications", dbUp ? notifications(now) : Map.of());
        out.put("history", history(historySnapshot));
        Map<String, Object> catchUpBlock = new LinkedHashMap<>();
        catchUpBlock.put("triggerLagSeq", catching.triggerLagSeq());
        catchUpBlock.put("ingestQueueDepth", catching.ingestQueueDepth());
        catchUpBlock.put("catchingUp", catching.catchingUp());
        out.put("catchUp", catchUpBlock);
        out.put("warnings", warningList.stream().map(w -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", w.code());
            m.put("severity", w.severity());
            m.put("message", w.message());
            return m;
        }).toList());
        return out;
    }

    /** Every contributor's flags; a key is true when any contributor says so. */
    public Map<String, Boolean> features() {
        Map<String, Boolean> out = new TreeMap<>();
        for (FeatureContributor contributor : contributors) {
            contributor.features().forEach((key, value) -> out.merge(key, Boolean.TRUE.equals(value), Boolean::logicalOr));
        }
        return out;
    }

    private Map<String, Object> limits() {
        StreamLimits stream = hub.limits();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pageMax", PAGE_MAX);
        out.put("historyPageMax", PAGE_MAX);
        out.put("sseClientBuffer", stream.clientBuffer());
        out.put("liveMaxEntities", stream.liveMaxEntities());
        out.put("ingestMaxBytes", INGEST_MAX_BYTES);
        out.put("ingestMaxRecords", INGEST_MAX_RECORDS);
        out.put("tileMaxFeatures", tiles.maxFeatures());
        return out;
    }

    private static String status(boolean dbUp, WriterStatus writer, CatchUpMonitor.CatchUp catching,
            List<SourceHealthView> sources, List<SystemWarnings.Warning> warnings) {
        if (!dbUp || writer.state() == WriterState.STOPPED || writer.state() == WriterState.LOCK_LOST) {
            return "down";
        }
        boolean failing = sources.stream().anyMatch(s -> s.status() == HealthStatus.FAILING);
        boolean warned = warnings.stream().anyMatch(w -> !"info".equals(w.severity()));
        if (writer.state() == WriterState.CLOCK_BEHIND || catching.catchingUp() || failing || warned) {
            return "degraded";
        }
        return "ok";
    }

    private Map<String, Object> database(boolean up) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", up ? "up" : "down");
        if (up) {
            try {
                out.put("sizeBytes", jdbc.queryForObject("SELECT pg_database_size(current_database())", Long.class));
                out.put("postgisVersion", jdbc.queryForObject("SELECT postgis_lib_version()", String.class));
            } catch (RuntimeException e) {
                log.debug("Could not read the database size or PostGIS version", e);
            }
        }
        return out;
    }

    private Map<String, Object> sources(List<SourceHealthView> sourceHealth) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", registry.ids().size());
        int enabled = 0;
        try {
            for (SourceSettingsEntity row : sourceSettings.findAll()) {
                if (row.isEnabled() && registry.contains(row.getSourceId())) {
                    enabled++;
                }
            }
        } catch (RuntimeException e) {
            log.debug("Could not count enabled sources", e);
        }
        out.put("enabled", enabled);
        out.put("healthy", sourceHealth.stream().filter(s -> s.status() == HealthStatus.HEALTHY).count());
        out.put("degraded", sourceHealth.stream().filter(s -> s.status() == HealthStatus.DEGRADED).count());
        out.put("failing", sourceHealth.stream().filter(s -> s.status() == HealthStatus.FAILING).count());
        return out;
    }

    private Map<String, Object> notifications(Instant now) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("channels", jdbc.queryForObject("SELECT count(*) FROM notification_channel", Long.class));
            out.put("pendingDeliveries",
                    jdbc.queryForObject("SELECT count(*) FROM notification_delivery WHERE status = 'PENDING'", Long.class));
            out.put("failedLast24h", jdbc.queryForObject(
                    "SELECT count(*) FROM notification_delivery WHERE status = 'FAILED' AND created_at >= ?", Long.class,
                    Timestamp.from(now.minus(Duration.ofHours(24)))));
        } catch (RuntimeException e) {
            log.debug("Could not count notifications", e);
        }
        return out;
    }

    private static Map<String, Object> history(HistoryStats.Snapshot snapshot) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (snapshot == null) {
            out.put("tiers", List.of());
            return out;
        }
        out.put("tiers", snapshot.tiers().stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tier", t.tier().name());
            m.put("partitions", t.partitions());
            m.put("rows", t.rows());
            m.put("bytes", t.bytes());
            m.put("headroomSeq", t.headroomSeq());
            m.put("windowDays", t.windowDays());
            return m;
        }).toList());
        out.put("versionsPerMinute", snapshot.versionsPerMinute());
        out.put("projectedBytes", snapshot.projectedBytes());
        return out;
    }

    private HistoryStats.Snapshot safeHistory() {
        try {
            return history.snapshot();
        } catch (RuntimeException e) {
            log.debug("Could not read history statistics", e);
            return null;
        }
    }

    private List<SourceHealthView> safeHealth() {
        try {
            return health.all(Optional.empty());
        } catch (RuntimeException e) {
            log.debug("Could not read source health", e);
            return List.of();
        }
    }
}
