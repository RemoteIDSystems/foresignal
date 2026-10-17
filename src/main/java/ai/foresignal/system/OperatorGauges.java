package ai.foresignal.system;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.stream.sse.SseHub;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Operator gauges (prompt 019), refreshed every 30 s from cached queries rather than per scrape:
 *
 * <ul>
 *   <li>{@code foresignal.source.data_age_seconds{source}}: now − {@code source_health.last_data_at}
 *       (NaN before a source's first data);
 *   <li>{@code foresignal.alerts.active{severity}}: unresolved, unsuppressed alerts (017 lifecycle);
 *   <li>{@code foresignal.alerts.open{severity}}: unresolved, unacknowledged, unsuppressed alerts
 *       ({@code state = firing}; prompt 038, the glance's open counts);
 *   <li>{@code foresignal.records.current}: non-tombstoned heads;
 *   <li>{@code foresignal.sse.clients{stream}}: open SSE clients ({@code all}, {@code changes},
 *       {@code events}, {@code alerts});
 *   <li>{@code foresignal.history.rows{tier}}: estimated history rows per tier.
 * </ul>
 */
@Component
public class OperatorGauges {

    private static final Logger log = LoggerFactory.getLogger(OperatorGauges.class);

    /** The alert severities, pre-registered so a zero count is visible. */
    static final List<String> SEVERITIES = List.of("info", "warning", "critical");

    /** The stream tags. */
    static final List<String> STREAMS = List.of("all", "changes", "events", "alerts");

    private final JdbcTemplate jdbc;
    private final SourceRegistry registry;
    private final SseHub hub;
    private final HistoryStats history;
    private final MeterRegistry meters;
    private final Clock clock;

    private final Map<String, Double> dataAge = new ConcurrentHashMap<>();
    private final Map<String, Double> alerts = new ConcurrentHashMap<>();
    private final Map<String, Double> openAlerts = new ConcurrentHashMap<>();
    private final Map<String, Double> clients = new ConcurrentHashMap<>();
    private final Map<String, Double> historyRows = new ConcurrentHashMap<>();
    private final Set<String> gaugedSources = ConcurrentHashMap.newKeySet();
    private volatile long currentRecords;
    private volatile boolean registered;

    /** Creates the gauges. */
    public OperatorGauges(JdbcTemplate jdbc, SourceRegistry registry, SseHub hub, HistoryStats history,
            MeterRegistry meters, Clock clock) {
        this.jdbc = jdbc;
        this.registry = registry;
        this.hub = hub;
        this.history = history;
        this.meters = meters;
        this.clock = clock;
    }

    /** Registers the fixed gauges and takes the first reading. */
    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        if (!registered) {
            registered = true;
            for (String severity : SEVERITIES) {
                alerts.put(severity, 0.0);
                Gauge.builder("foresignal.alerts.active", alerts, m -> m.getOrDefault(severity, 0.0))
                        .description("Unresolved alerts").tag("severity", severity).register(meters);
                openAlerts.put(severity, 0.0);
                Gauge.builder("foresignal.alerts.open", openAlerts, m -> m.getOrDefault(severity, 0.0))
                        .description("Unresolved, unacknowledged alerts").tag("severity", severity).register(meters);
            }
            for (String stream : STREAMS) {
                clients.put(stream, 0.0);
                Gauge.builder("foresignal.sse.clients", clients, m -> m.getOrDefault(stream, 0.0))
                        .description("Open SSE clients").tag("stream", stream).register(meters);
            }
            for (HistoryTier tier : HistoryTier.values()) {
                String tag = tier.name().toLowerCase(java.util.Locale.ROOT);
                historyRows.put(tag, 0.0);
                Gauge.builder("foresignal.history.rows", historyRows, m -> m.getOrDefault(tag, 0.0))
                        .description("Estimated history rows").tag("tier", tag).register(meters);
            }
            Gauge.builder("foresignal.records.current", this, g -> g.currentRecords)
                    .description("Current (non-tombstoned) records").register(meters);
        }
        refresh();
    }

    /** Re-reads every input; each part fails on its own. */
    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void refresh() {
        Instant now = clock.instant();
        try {
            refreshDataAge(now);
        } catch (RuntimeException e) {
            log.debug("Could not refresh the data-age gauges", e);
        }
        try {
            Map<String, Double> counts = new HashMap<>();
            SEVERITIES.forEach(s -> counts.put(s, 0.0));
            jdbc.query("SELECT severity, count(*) FROM alert WHERE state <> 'resolved' AND NOT suppressed AND NOT exercise GROUP BY severity",
                    rs -> {
                        counts.put(rs.getString(1), (double) rs.getLong(2));
                    });
            alerts.putAll(counts);
            Map<String, Double> open = new HashMap<>();
            SEVERITIES.forEach(s -> open.put(s, 0.0));
            jdbc.query("SELECT severity, count(*) FROM alert WHERE state = 'firing' AND NOT suppressed AND NOT exercise GROUP BY severity",
                    rs -> {
                        open.put(rs.getString(1), (double) rs.getLong(2));
                    });
            openAlerts.putAll(open);
        } catch (RuntimeException e) {
            log.debug("Could not refresh the alert gauges", e);
        }
        try {
            Long count = jdbc.queryForObject("SELECT count(*) FROM record_head WHERE NOT tombstone", Long.class);
            currentRecords = count == null ? 0 : count;
        } catch (RuntimeException e) {
            log.debug("Could not refresh the current-records gauge", e);
        }
        SseHub.StreamStats stats = hub.stats();
        clients.put("all", (double) stats.clients());
        clients.put("changes", (double) stats.changeSubscribers());
        clients.put("events", (double) stats.eventSubscribers());
        clients.put("alerts", (double) stats.alertSubscribers());
        try {
            for (HistoryStats.Tier tier : history.snapshot().tiers()) {
                historyRows.put(tier.tier().name().toLowerCase(java.util.Locale.ROOT), (double) tier.rows());
            }
        } catch (RuntimeException e) {
            log.debug("Could not refresh the history gauges", e);
        }
    }

    private void refreshDataAge(Instant now) {
        Map<String, Instant> last = new HashMap<>();
        jdbc.query("SELECT source_id, last_data_at FROM source_health", rs -> {
            Timestamp at = rs.getTimestamp(2);
            if (at != null) {
                last.put(rs.getString(1), at.toInstant());
            }
        });
        for (SourceDescriptor descriptor : registry.descriptors()) {
            String id = descriptor.id();
            Instant at = last.get(id);
            dataAge.put(id, at == null ? Double.NaN : Math.max(0, Duration.between(at, now).toMillis() / 1000.0));
            if (gaugedSources.add(id)) {
                Gauge.builder("foresignal.source.data_age_seconds", dataAge, m -> m.getOrDefault(id, Double.NaN))
                        .description("Seconds since the source last delivered records").tag("source", id)
                        .register(meters);
            }
        }
    }

    /** The cached count of current records (for {@code /v1/system.records.current}). */
    public long currentRecords() {
        return currentRecords;
    }

    /** The cached data age of {@code sourceId} in seconds, NaN when unknown. */
    public double dataAgeSeconds(String sourceId) {
        return dataAge.getOrDefault(sourceId, Double.NaN);
    }
}
