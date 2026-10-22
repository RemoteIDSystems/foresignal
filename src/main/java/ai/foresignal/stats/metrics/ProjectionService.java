package ai.foresignal.stats.metrics;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.JsonValues;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.Touch;
import ai.foresignal.store.query.PgArrays;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Keeps the metric projections' gauges current (prompt 038) in a {@link PrometheusMeterRegistry} of
 * its own, rendered only by {@code GET /v1/metrics}: never a Spring bean, so neither the global
 * registry nor {@code /actuator/prometheus} ever sees these series.
 *
 * <ul>
 *   <li><b>Seeding:</b> at start (and whenever {@code app_setting['metrics']} changes) every current,
 *       unexpired, non-tombstoned record of a projected source is read from
 *       {@code record_head ⋈ record_live} with the merged payload ({@code payload || kinematic}).
 *   <li><b>Following:</b> committed ChangeBus batches and LiveBus batches re-read the touched entities'
 *       merged payload, so SAMPLED sources' live values count; touches move only the observed time.
 *   <li><b>Values:</b> numbers and numeric strings; anything else is skipped and counted in
 *       {@code foresignal.projection.coercion_failures{projection}} (global registry).
 *   <li><b>Series:</b> labeled {@code source} and {@code external_id}, or by the projection's labels
 *       (entities with the same label values share a series; the latest write wins). A series is dropped
 *       when its head is tombstoned, expires ({@code valid_until}, checked every minute) or is purged.
 *       Each projection keeps at most {@code maxSeries}; across all projections at most
 *       {@value #MAX_TOTAL_SERIES}, after which a {@code system.metrics_series_cap} event is raised
 *       once and no series is added.
 * </ul>
 */
@Component
public class ProjectionService implements ChangeListener, LiveListener, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ProjectionService.class);

    /** The global series cap. */
    public static final int MAX_TOTAL_SERIES = 10_000;

    /** The coercion failure counter (global registry), tagged {@code projection}. */
    public static final String COERCION_METRIC = "foresignal.projection.coercion_failures";

    /** The system event raised when the global cap is reached. */
    public static final String CAP_EVENT = "system.metrics_series_cap";

    /** The text exposition format 0.0.4. */
    public static final String TEXT_FORMAT = "text/plain; version=0.0.4; charset=utf-8";

    /** The OpenMetrics text format. */
    public static final String OPENMETRICS_FORMAT = "application/openmetrics-text; version=1.0.0; charset=utf-8";

    static final String MERGED = "SELECT h.source_id, h.external_id, h.kind, h.tombstone,"
            + " jsonb_strip_nulls(h.payload || l.kinematic)::text AS payload, l.observed_at, l.valid_until"
            + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final MetricSettingsService settings;
    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final NamedParameterJdbcTemplate jdbc;
    private final MeterRegistry meters;
    private final ApplicationEventPublisher events;
    private final JsonMapper json;
    private final Clock clock;
    private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    private volatile List<MetricProjection> projections = List.of();
    private volatile Map<String, List<MetricProjection>> bySource = Map.of();
    private final Map<SeriesKey, Series> series = new HashMap<>();
    private final Map<EntityKey, Map<String, Owner>> owners = new HashMap<>();
    private final Map<String, Integer> perProjection = new HashMap<>();
    private boolean capRaised;
    private int maxTotalSeries = MAX_TOTAL_SERIES;
    private volatile Subscription changes;
    private volatile Subscription live;

    /** Creates the service; nothing runs until {@link #start()}. */
    public ProjectionService(MetricSettingsService settings, ChangeBus changeBus, LiveBus liveBus,
            NamedParameterJdbcTemplate jdbc, MeterRegistry meters, ApplicationEventPublisher events, JsonMapper json,
            Clock clock) {
        this.settings = settings;
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.jdbc = jdbc;
        this.meters = meters;
        this.events = events;
        this.json = json;
        this.clock = clock;
    }

    /** A series: one projection and one set of label values. */
    private record SeriesKey(String projection, List<String> labels) {}

    /** One entity. */
    private record EntityKey(String sourceId, String externalId) {}

    /** An entity's part in a series of one projection. */
    private record Owner(SeriesKey key, Instant validUntil) {}

    /** The state behind one series' two gauges. */
    private static final class Series {
        final Set<EntityKey> owners = new LinkedHashSet<>();
        volatile double value = Double.NaN;
        volatile double observed = Double.NaN;
        Gauge valueGauge;
        Gauge timestampGauge;
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Loads the projections, seeds them and subscribes to both buses. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.METRIC_PROJECTIONS)
    public synchronized void start() {
        reload();
        if (changes == null) {
            changes = changeBus.subscribe("metric-projections", this, 1_024);
        }
        if (live == null) {
            live = liveBus.subscribe("metric-projections", this, 1_024);
        }
    }

    /** Unsubscribes. */
    @PreDestroy
    public synchronized void stop() {
        if (changes != null) {
            changes.close();
            changes = null;
        }
        if (live != null) {
            live.close();
            live = null;
        }
    }

    /** Reloads when {@code app_setting['metrics']} changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.METRICS.equals(event.key())) {
            reload();
        }
    }

    /** Forgets every series and reseeds (the test cleaner truncates the records behind them). */
    @Override
    public void resetState() {
        reload();
    }

    /** Drops every series, reads the projections and seeds them from the current records. */
    public synchronized void reload() {
        for (Series s : series.values()) {
            removeMeters(s);
        }
        series.clear();
        owners.clear();
        perProjection.clear();
        capRaised = false;
        List<MetricProjection> loaded;
        try {
            loaded = settings.get().projections();
        } catch (RuntimeException e) {
            log.warn("Could not read the metric projections", e);
            loaded = List.of();
        }
        projections = loaded;
        Map<String, List<MetricProjection>> index = new HashMap<>();
        for (MetricProjection p : loaded) {
            index.computeIfAbsent(p.source(), k -> new ArrayList<>()).add(p);
        }
        bySource = Map.copyOf(index);
        for (String sourceId : new TreeSet<>(index.keySet())) {
            try {
                seed(sourceId);
            } catch (RuntimeException e) {
                log.warn("Could not seed the metric projections of {}", sourceId, e);
            }
        }
    }

    private void seed(String sourceId) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("source", sourceId)
                .addValue("now", OffsetDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC));
        jdbc.query(MERGED + " WHERE h.source_id = :source AND NOT h.tombstone"
                + " AND (l.valid_until IS NULL OR l.valid_until > :now) ORDER BY h.external_id", p, rs -> {
                    apply(rs.getString(1), rs.getString(2), rs.getString(3), false, rs.getString(5),
                            instant(rs.getObject(6, OffsetDateTime.class)), instant(rs.getObject(7, OffsetDateTime.class)));
                });
    }

    // ------------------------------------------------------------------------- buses

    @Override
    public void onBatch(ChangeBatch batch) {
        if (!bySource.containsKey(batch.sourceId())) {
            return;
        }
        Set<String> refresh = new LinkedHashSet<>();
        synchronized (this) {
            for (ChangeSummary change : batch.changes()) {
                if (change.tombstone()) {
                    drop(new EntityKey(change.sourceId(), change.externalId()));
                    refresh.remove(change.externalId());
                } else {
                    refresh.add(change.externalId());
                }
            }
        }
        refresh(batch.sourceId(), refresh);
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        reload();
    }

    @Override
    public void onBatch(LiveBatch batch) {
        if (!bySource.containsKey(batch.sourceId())) {
            return;
        }
        Set<String> refresh = new LinkedHashSet<>();
        for (LiveUpdate update : batch.updates()) {
            refresh.add(update.externalId());
        }
        refresh(batch.sourceId(), refresh);
        synchronized (this) {
            for (Touch touch : batch.touches()) {
                touched(new EntityKey(touch.sourceId(), touch.externalId()), touch.observedAt(), touch.validUntil());
            }
        }
    }

    @Override
    public void onLiveDropped() {
        reload();
    }

    /** Re-reads the merged payload of {@code externalIds} of {@code sourceId} and applies it. */
    void refresh(String sourceId, Collection<String> externalIds) {
        if (externalIds.isEmpty()) {
            return;
        }
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("source", sourceId)
                .addValue("ids", PgArrays.text(new TreeSet<>(externalIds)));
        Set<String> seen = new HashSet<>();
        jdbc.query(MERGED + " WHERE h.source_id = :source AND h.external_id = ANY(CAST(:ids AS text[]))", p, rs -> {
            seen.add(rs.getString(2));
            apply(rs.getString(1), rs.getString(2), rs.getString(3), rs.getBoolean(4), rs.getString(5),
                    instant(rs.getObject(6, OffsetDateTime.class)), instant(rs.getObject(7, OffsetDateTime.class)));
        });
        synchronized (this) {
            for (String id : externalIds) {
                if (!seen.contains(id)) {
                    drop(new EntityKey(sourceId, id));
                }
            }
        }
    }

    // ------------------------------------------------------------------------- expiry and purges

    /**
     * Every minute: drops entities whose {@code valid_until} has passed, and entities whose head no
     * longer exists (purged by retention).
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void sweep() {
        Instant now = clock.instant();
        Map<String, Set<String>> tracked = new HashMap<>();
        synchronized (this) {
            for (Map.Entry<EntityKey, Map<String, Owner>> e : new ArrayList<>(owners.entrySet())) {
                boolean expired = e.getValue().values().stream()
                        .anyMatch(o -> o.validUntil() != null && !now.isBefore(o.validUntil()));
                if (expired) {
                    drop(e.getKey());
                } else {
                    tracked.computeIfAbsent(e.getKey().sourceId(), k -> new HashSet<>()).add(e.getKey().externalId());
                }
            }
        }
        for (Map.Entry<String, Set<String>> e : tracked.entrySet()) {
            try {
                Set<String> existing = new HashSet<>(jdbc.queryForList("SELECT external_id FROM record_head"
                        + " WHERE source_id = :source AND external_id = ANY(CAST(:ids AS text[])) AND NOT tombstone",
                        new MapSqlParameterSource().addValue("source", e.getKey())
                                .addValue("ids", PgArrays.text(new TreeSet<>(e.getValue()))), String.class));
                synchronized (this) {
                    for (String id : e.getValue()) {
                        if (!existing.contains(id)) {
                            drop(new EntityKey(e.getKey(), id));
                        }
                    }
                }
            } catch (RuntimeException ex) {
                log.debug("Could not check purged heads of {}", e.getKey(), ex);
            }
        }
    }

    // ------------------------------------------------------------------------- applying

    private synchronized void apply(String sourceId, String externalId, String kind, boolean tombstone,
            String payloadJson, Instant observedAt, Instant validUntil) {
        EntityKey entity = new EntityKey(sourceId, externalId);
        if (tombstone || (validUntil != null && !clock.instant().isBefore(validUntil))) {
            drop(entity);
            return;
        }
        Map<String, Object> payload;
        try {
            payload = payloadJson == null ? Map.of() : json.readValue(payloadJson, MAP);
        } catch (RuntimeException e) {
            payload = Map.of();
        }
        for (MetricProjection p : bySource.getOrDefault(sourceId, List.of())) {
            if (p.kind() != null && !p.kind().equals(kind)) {
                detach(entity, p.name());
                continue;
            }
            Double value = coerce(JsonValues.at(payload, p.path()));
            if (value == null) {
                meters.counter(COERCION_METRIC, "projection", p.name()).increment();
                detach(entity, p.name());
                continue;
            }
            SeriesKey key = new SeriesKey(p.name(), labelValues(p, sourceId, externalId, payload));
            Owner previous = owners.getOrDefault(entity, Map.of()).get(p.name());
            if (previous != null && !previous.key().equals(key)) {
                detach(entity, p.name());
            }
            Series s = series.get(key);
            if (s == null) {
                s = create(p, key);
                if (s == null) {
                    continue;
                }
            }
            s.owners.add(entity);
            s.value = value;
            s.observed = observedAt == null ? Double.NaN : observedAt.toEpochMilli() / 1000.0;
            owners.computeIfAbsent(entity, k -> new HashMap<>()).put(p.name(), new Owner(key, validUntil));
        }
    }

    private void touched(EntityKey entity, Instant observedAt, Instant validUntil) {
        Map<String, Owner> mine = owners.get(entity);
        if (mine == null) {
            return;
        }
        for (Map.Entry<String, Owner> e : new ArrayList<>(mine.entrySet())) {
            Series s = series.get(e.getValue().key());
            if (s != null && observedAt != null) {
                s.observed = observedAt.toEpochMilli() / 1000.0;
            }
            e.setValue(new Owner(e.getValue().key(), validUntil));
        }
    }

    /** A new series, or null when its projection or the global cap is full. */
    private Series create(MetricProjection p, SeriesKey key) {
        if (series.size() >= maxTotalSeries) {
            if (!capRaised) {
                capRaised = true;
                log.warn("Metric projections reached {} series; no further series are added", maxTotalSeries);
                try {
                    events.publishEvent(new SystemEvent(CAP_EVENT, "warning", "Metric projections hit the series cap",
                            "/v1/metrics holds " + maxTotalSeries + " series; no further series are added until"
                                    + " projections are narrowed.", "/v1/settings/metrics",
                            Map.of("cap", maxTotalSeries, "projection", p.name())));
                } catch (RuntimeException e) {
                    log.warn("Could not publish the series-cap event", e);
                }
            }
            return null;
        }
        int count = perProjection.getOrDefault(p.name(), 0);
        if (count >= p.maxSeries()) {
            return null;
        }
        Series s = new Series();
        List<Tag> tags = tags(p, key.labels());
        String help = p.help() == null ? "Projected payload field " + p.path() + " of " + p.source() : p.help();
        if (p.unit() != null) {
            help = help + " (" + p.unit() + ")";
        }
        s.valueGauge = Gauge.builder(p.metricName(), s, x -> x.value).description(help).tags(tags)
                .strongReference(true).register(registry);
        s.timestampGauge = Gauge.builder(p.timestampName(), s, x -> x.observed)
                .description("When the value of " + p.metricName() + " was observed, in Unix seconds").tags(tags)
                .strongReference(true).register(registry);
        series.put(key, s);
        perProjection.put(p.name(), count + 1);
        return s;
    }

    /**
     * Drops every series of one entity (prompt 039 entity purge). The purge's tombstone change drops
     * them too; this call makes the drop explicit and idempotent.
     *
     * @return true when the entity owned at least one series
     */
    public synchronized boolean dropEntity(String sourceId, String externalId) {
        EntityKey entity = new EntityKey(sourceId, externalId);
        boolean owned = owners.containsKey(entity);
        drop(entity);
        return owned;
    }

    /** Detaches {@code entity} from every projection. */
    private void drop(EntityKey entity) {
        Map<String, Owner> mine = owners.get(entity);
        if (mine == null) {
            return;
        }
        for (String projection : new ArrayList<>(mine.keySet())) {
            detach(entity, projection);
        }
    }

    /** Detaches {@code entity} from its series of {@code projection}; removes the series when it has no owner. */
    private void detach(EntityKey entity, String projection) {
        Map<String, Owner> mine = owners.get(entity);
        if (mine == null) {
            return;
        }
        Owner owner = mine.remove(projection);
        if (mine.isEmpty()) {
            owners.remove(entity);
        }
        if (owner == null) {
            return;
        }
        Series s = series.get(owner.key());
        if (s == null) {
            return;
        }
        s.owners.remove(entity);
        if (s.owners.isEmpty()) {
            removeMeters(s);
            series.remove(owner.key());
            perProjection.merge(projection, -1, Integer::sum);
        }
    }

    private void removeMeters(Series s) {
        if (s.valueGauge != null) {
            registry.remove(s.valueGauge);
        }
        if (s.timestampGauge != null) {
            registry.remove(s.timestampGauge);
        }
    }

    /** The label values: the projection's labels from the payload, else source and external id. */
    static List<String> labelValues(MetricProjection p, String sourceId, String externalId, Map<String, Object> payload) {
        if (p.labels().isEmpty()) {
            return List.of(sourceId, externalId);
        }
        List<String> out = new ArrayList<>(p.labels().size());
        for (MetricProjection.Label label : p.labels()) {
            String text = JsonValues.text(JsonValues.at(payload, label.path()));
            out.add(text == null ? "" : text);
        }
        return out;
    }

    private static List<Tag> tags(MetricProjection p, List<String> values) {
        List<Tag> tags = new ArrayList<>();
        if (p.labels().isEmpty()) {
            tags.add(Tag.of("source", values.get(0)));
            tags.add(Tag.of("external_id", values.get(1)));
        } else {
            for (int i = 0; i < p.labels().size(); i++) {
                tags.add(Tag.of(p.labels().get(i).name(), values.get(i)));
            }
        }
        return tags;
    }

    /** A finite number, or a string holding one; else null. */
    static Double coerce(Object value) {
        Double number = JsonValues.finiteNumber(value);
        if (number != null) {
            return number;
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                double d = Double.parseDouble(s.strip());
                return Double.isFinite(d) ? d : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    // ------------------------------------------------------------------------- reading

    /**
     * The exposition of the projections whose source {@code visible} accepts, in the format
     * {@code accept} asks for: OpenMetrics when it names {@code application/openmetrics-text}, else
     * the text format 0.0.4.
     */
    public Exposition scrape(String accept, java.util.function.Predicate<MetricProjection> visible) {
        boolean openMetrics = accept != null && accept.toLowerCase(java.util.Locale.ROOT).contains("application/openmetrics-text");
        String contentType = openMetrics ? OPENMETRICS_FORMAT : TEXT_FORMAT;
        Set<String> names = new LinkedHashSet<>();
        for (MetricProjection p : projections) {
            if (visible.test(p)) {
                names.add(p.metricName());
                names.add(p.timestampName());
            }
        }
        String body = names.isEmpty() ? (openMetrics ? "# EOF\n" : "") : registry.scrape(contentType, names);
        return new Exposition(contentType, body);
    }

    /**
     * A rendered exposition.
     *
     * @param contentType its media type
     * @param body the text
     */
    public record Exposition(String contentType, String body) {}

    /** Lowers the global cap (a test hook; {@value #MAX_TOTAL_SERIES} otherwise). */
    synchronized void maxTotalSeries(int cap) {
        this.maxTotalSeries = cap;
    }

    /** The current series count (tests and the system document). */
    public synchronized int seriesCount() {
        return series.size();
    }

    /** The projections in force. */
    public List<MetricProjection> projections() {
        return projections;
    }

    /** The current value of {@code projection}'s series with {@code labels}, or null (tests). */
    public synchronized Double value(String projection, List<String> labels) {
        Series s = series.get(new SeriesKey(projection, labels));
        return s == null ? null : s.value;
    }

    /** Values per series of {@code projection} (tests). */
    public synchronized Map<List<String>, Double> values(String projection) {
        Map<List<String>, Double> out = new LinkedHashMap<>();
        series.forEach((k, s) -> {
            if (k.projection().equals(projection)) {
                out.put(k.labels(), s.value);
            }
        });
        return out;
    }
}
