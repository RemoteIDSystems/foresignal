package ai.foresignal.entity;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.EntitySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Links records to canonical entities by consuming the {@link ChangeBus} (prompt 098), keeps the
 * {@code entity} durable cursor and copies declared attributes onto the entity.
 *
 * <p>Processes only {@code FIRST}, {@code REAPPEAR}, {@code SIGNIFICANT} and {@code TOMBSTONE}
 * versions (keys are significant fields, so other reasons cannot change them). On a cold start it
 * backfills the current heads of every source that declares {@code entityKeys}; afterwards it
 * starts from the durable cursor and catches up.
 */
@Component
public class EntityLinker {

    private static final Logger log = LoggerFactory.getLogger(EntityLinker.class);

    /** The subscriber name on the change bus. */
    static final String SUBSCRIBER = "entity-linker";

    private static final int QUEUE_CAPACITY = 64;

    private static final Map<String, Integer> DISPLAY_PRECEDENCE = Map.of(
            "registration", 1, "mmsi", 2, "imo", 3, "norad", 4, "cospar", 5, "icao24", 6, "airframe", 7, "callsign", 8);

    private final ChangeBus changeBus;
    private final JdbcTemplate jdbc;
    private final SourceRegistry registry;
    private final EntityResolver resolver;
    private final EntityRepository entities;
    private final EntityKeyRepository keys;
    private final EntityLinkRepository links;
    private final AppSettingService settings;
    private final Clock clock;
    private final JsonMapper json;
    private final MeterRegistry meters;

    private Subscription subscription;
    private volatile long cursor = -1;

    /** Released once the startup backfill or catch-up has finished; the listener waits on it. */
    private volatile CountDownLatch ready = new CountDownLatch(0);

    /** Creates the linker. */
    public EntityLinker(
            ChangeBus changeBus,
            JdbcTemplate jdbc,
            SourceRegistry registry,
            EntityResolver resolver,
            EntityRepository entities,
            EntityKeyRepository keys,
            EntityLinkRepository links,
            AppSettingService settings,
            Clock clock,
            JsonMapper json,
            MeterRegistry meters) {
        this.changeBus = changeBus;
        this.jdbc = jdbc;
        this.registry = registry;
        this.resolver = resolver;
        this.entities = entities;
        this.keys = keys;
        this.links = links;
        this.settings = settings;
        this.clock = clock;
        this.json = json;
        this.meters = meters;
    }

    /**
     * Starts the linker once the application is ready, without blocking startup: it subscribes at
     * once (so nothing published from now on is missed), then backfills or catches up to the
     * subscription's starting watermark on its own virtual thread. The listener waits for that to
     * finish; batches it already covered are skipped, and a lagging queue is recovered by the normal
     * gap catch-up.
     */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void start() {
        long stored = loadCursor();
        CountDownLatch latch = new CountDownLatch(1);
        ready = latch;
        long watermark = changeBus.lastPublishedSeq();
        subscription = changeBus.subscribe(SUBSCRIBER, new Listener(), QUEUE_CAPACITY);
        Thread.ofVirtual().name("entity-linker-startup").start(() -> {
            try {
                if (stored < 0) {
                    backfillHeads();
                } else if (stored < watermark) {
                    catchUp(stored, watermark);
                }
                cursor = Math.max(stored, watermark);
                storeCursor(cursor);
                log.info("Entity linker started at seq {}", cursor);
            } catch (RuntimeException e) {
                // The stored cursor is left as it was, so the next start retries the same range.
                cursor = stored;
                log.error("Entity linker startup catch-up failed; continuing from seq {}", stored, e);
            } finally {
                latch.countDown();
            }
        });
    }

    /**
     * Waits until the startup backfill or catch-up has finished (tests and diagnostics).
     *
     * @param timeout the longest wait
     * @return true when startup has finished
     */
    public boolean awaitReady(Duration timeout) {
        try {
            return ready.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void awaitStartup() {
        while (!awaitReady(Duration.ofMinutes(1)) && !Thread.currentThread().isInterrupted()) {
            log.debug("Entity linker still waiting for its startup catch-up");
        }
    }

    /** Stops the linker (tests). */
    public synchronized void stop() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    /** The last seq fully processed, or -1 when none. */
    public long cursor() {
        return cursor;
    }

    private final class Listener implements ChangeListener {

        @Override
        public void onBatch(ChangeBatch batch) {
            awaitStartup();
            if (batch.toSeq() <= cursor) {
                return;
            }
            process(batch.changes());
            advance(batch.toSeq());
        }

        @Override
        public void onGap(long lastSeq) {
            awaitStartup();
            long watermark = changeBus.lastPublishedSeq();
            catchUp(Math.max(lastSeq, cursor), watermark);
            advance(watermark);
        }
    }

    private void process(List<ChangeSummary> changes) {
        List<ChangeSummary> relevant = new ArrayList<>();
        for (ChangeSummary change : changes) {
            if (isRelevant(change)) {
                relevant.add(change);
            }
        }
        if (relevant.isEmpty()) {
            return;
        }
        for (ChangeSummary change : relevant) {
            processOne(change);
        }
    }

    private static boolean isRelevant(ChangeSummary change) {
        return relevantReason(change.persistReason());
    }

    private static boolean relevantReason(PersistReason reason) {
        return reason == PersistReason.FIRST
                || reason == PersistReason.REAPPEAR
                || reason == PersistReason.SIGNIFICANT
                || reason == PersistReason.TOMBSTONE;
    }

    private void processOne(ChangeSummary change) {
        Optional<SourceDescriptor> descriptor = registry.find(change.sourceId()).map(reg -> reg.descriptor());
        if (descriptor.isEmpty() || descriptor.get().entityKeys().isEmpty()) {
            return;
        }
        SourceDescriptor d = descriptor.get();
        if (d.privateSource() && !entitySettings().includePrivate()) {
            return;
        }
        if (change.tombstone()) {
            links.findOpen(change.sourceId(), change.externalId()).ifPresent(link -> {
                link.setValidTo(change.observedAt());
                links.save(link);
            });
            return;
        }
        Map<String, Object> payload = loadPayload(change.seq());
        if (payload.isEmpty()) {
            return;
        }
        List<Key> extracted = extract(d, payload);
        if (extracted.isEmpty()) {
            return;
        }
        KeyOrigin origin = "faa-registry".equals(change.sourceId()) ? KeyOrigin.REGISTRY : KeyOrigin.CO_ASSERTED;
        String originRef = change.sourceId() + ":" + change.externalId();
        Resolution resolution = resolver.resolve(extracted, change.observedAt(), origin, originRef);
        if (resolution == null) {
            return;
        }
        upsertLink(change, resolution);
        copyAttributes(resolution.entityId(), d, payload);
        updateDisplayName(resolution.entityId());
    }

    private void upsertLink(ChangeSummary change, Resolution resolution) {
        Key linkKey = resolution.linkKey();
        Optional<EntityLinkEntity> existing = links.findOpen(change.sourceId(), change.externalId());
        if (existing.isPresent()) {
            EntityLinkEntity link = existing.get();
            boolean same = link.getEntityId().equals(resolution.entityId())
                    && link.getKeyType().equals(linkKey.keyType())
                    && link.getKeyValue().equals(linkKey.keyValue());
            if (same) {
                return;
            }
            link.setValidTo(change.observedAt());
            links.save(link);
        }
        EntityLinkEntity link = new EntityLinkEntity();
        link.setEntityId(resolution.entityId());
        link.setSourceId(change.sourceId());
        link.setExternalId(change.externalId());
        link.setKeyType(linkKey.keyType());
        link.setKeyValue(linkKey.keyValue());
        link.setConfidence(linkKey.confidence());
        link.setValidFrom(change.observedAt());
        link.setFirstSeq(change.seq());
        links.save(link);
    }

    private void copyAttributes(String entityId, SourceDescriptor descriptor, Map<String, Object> payload) {
        Map<String, Object> declared = EntityKeys.attributes(descriptor, payload);
        if (declared.isEmpty()) {
            return;
        }
        entities.findById(entityId).ifPresent(entity -> {
            Map<String, Object> attributes = new LinkedHashMap<>(entity.getAttributes());
            boolean registrySource = "faa-registry".equals(descriptor.id());
            declared.forEach((name, value) -> {
                if (registrySource || !attributes.containsKey(name)) {
                    attributes.put(name, value);
                }
            });
            if (attributes.size() > 512) {
                return;
            }
            entity.setAttributes(attributes);
            entity.setUpdatedAt(clock.instant());
            entities.save(entity);
        });
    }

    private void updateDisplayName(String entityId) {
        entities.findById(entityId).ifPresent(entity -> {
            String name = displayName(entity);
            if (name != null && !name.equals(entity.getDisplayName())) {
                entity.setDisplayName(name);
                entity.setUpdatedAt(clock.instant());
                entities.save(entity);
            }
        });
    }

    private String displayName(EntityEntity entity) {
        List<EntityKeyEntity> open = keys.findOpenByEntityId(entity.getId());
        EntityKeyEntity best = null;
        int bestRank = Integer.MAX_VALUE;
        for (EntityKeyEntity key : open) {
            int rank = DISPLAY_PRECEDENCE.getOrDefault(key.getKeyType(), 9);
            if (rank < bestRank) {
                bestRank = rank;
                best = key;
            }
        }
        if (best != null && ("registration".equals(best.getKeyType()) || "mmsi".equals(best.getKeyType()))) {
            return best.getKeyValue();
        }
        Object name = entity.getAttributes().get("name");
        if (name != null) {
            return String.valueOf(name);
        }
        return best == null ? null : best.getKeyValue();
    }

    // ------------------------------------------------------------------ backfill / catch-up

    /** Scans the current heads of every source that declares {@code entityKeys} (cold start, tests). */
    void backfillHeads() {
        for (String sourceId : entitySourceIds()) {
            SourceDescriptor descriptor = registry.find(sourceId).map(r -> r.descriptor()).orElse(null);
            if (descriptor == null) {
                continue;
            }
            if (descriptor.privateSource() && !entitySettings().includePrivate()) {
                continue;
            }
            backfillSource(descriptor);
        }
    }

    private void backfillSource(SourceDescriptor descriptor) {
        String sql = "SELECT external_id, payload::text, observed_at FROM record_head WHERE source_id = ?"
                + " AND tombstone = false ORDER BY external_id LIMIT ? OFFSET ?";
        int offset = 0;
        int chunk = 2000;
        while (true) {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, descriptor.id(), chunk, offset);
            if (rows.isEmpty()) {
                return;
            }
            for (Map<String, Object> row : rows) {
                Map<String, Object> payload = parse((String) row.get("payload"));
                Instant observedAt = toInstant(row.get("observed_at"));
                List<Key> extracted = extract(descriptor, payload);
                if (extracted.isEmpty()) {
                    continue;
                }
                KeyOrigin origin = "faa-registry".equals(descriptor.id()) ? KeyOrigin.REGISTRY : KeyOrigin.CO_ASSERTED;
                String originRef = descriptor.id() + ":" + row.get("external_id");
                Resolution resolution = resolver.resolve(extracted, observedAt, origin, originRef);
                if (resolution == null) {
                    continue;
                }
                ChangeSummary synthetic = new ChangeSummary(
                        -1, null, descriptor.historyTier(), descriptor.id(),
                        String.valueOf(row.get("external_id")), descriptor.kinds().iterator().next(),
                        1, PersistReason.FIRST, observedAt, null, null, false, 0, null, null, null);
                upsertLink(synthetic, resolution);
                copyAttributes(resolution.entityId(), descriptor, payload);
                updateDisplayName(resolution.entityId());
            }
            offset += chunk;
            if (rows.size() < chunk) {
                return;
            }
        }
    }

    private void catchUp(long fromExclusive, long toInclusive) {
        catchUp(fromExclusive, toInclusive, 2000);
    }

    /**
     * Replays the versions in {@code (fromExclusive, toInclusive]} in chunks of {@code chunk} rows
     * (package-private so the tests can use a small chunk).
     *
     * @return the last seq scanned, or {@code fromExclusive} when there was nothing
     */
    long catchUp(long fromExclusive, long toInclusive, int chunk) {
        String sql = "SELECT seq, source_id, external_id, kind, observed_at, persist_reason, tombstone, payload::text"
                + " FROM record_version WHERE seq > ? AND seq <= ? ORDER BY seq LIMIT ?";
        long after = fromExclusive;
        Set<String> entitySources = new HashSet<>(entitySourceIds());
        while (after < toInclusive) {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, after, toInclusive, chunk);
            if (rows.isEmpty()) {
                return after;
            }
            for (Map<String, Object> row : rows) {
                // Advance past every scanned row, entity source or not: a chunk with no entity rows must
                // still move the scan forward, or it would be read again forever.
                long seq = ((Number) row.get("seq")).longValue();
                after = seq;
                String sourceId = (String) row.get("source_id");
                if (!entitySources.contains(sourceId)) {
                    continue;
                }
                Object code = row.get("persist_reason");
                if (code instanceof Number number && !relevantReason(PersistReason.fromCode(number.intValue()))) {
                    continue;
                }
                SourceDescriptor descriptor = registry.find(sourceId).map(r -> r.descriptor()).orElse(null);
                if (descriptor == null || (descriptor.privateSource() && !entitySettings().includePrivate())) {
                    continue;
                }
                boolean tombstone = Boolean.TRUE.equals(row.get("tombstone"));
                String externalId = String.valueOf(row.get("external_id"));
                Instant observedAt = toInstant(row.get("observed_at"));
                if (tombstone) {
                    links.findOpen(sourceId, externalId).ifPresent(link -> {
                        link.setValidTo(observedAt);
                        links.save(link);
                    });
                    continue;
                }
                Map<String, Object> payload = parse((String) row.get("payload"));
                List<Key> extracted = extract(descriptor, payload);
                if (extracted.isEmpty()) {
                    continue;
                }
                KeyOrigin origin = "faa-registry".equals(sourceId) ? KeyOrigin.REGISTRY : KeyOrigin.CO_ASSERTED;
                Resolution resolution = resolver.resolve(extracted, observedAt, origin, sourceId + ":" + externalId);
                if (resolution == null) {
                    continue;
                }
                ChangeSummary synthetic = new ChangeSummary(seq, null, descriptor.historyTier(), sourceId, externalId,
                        String.valueOf(row.get("kind")), 1, PersistReason.FIRST, observedAt, null, null, false, 0,
                        null, null, null);
                upsertLink(synthetic, resolution);
                copyAttributes(resolution.entityId(), descriptor, payload);
                updateDisplayName(resolution.entityId());
            }
            if (rows.size() < chunk) {
                return after;
            }
        }
        return after;
    }

    /** Closes {@code air:} callsign links whose record has not been observed within the gap. */
    @Scheduled(fixedDelay = 600_000, initialDelay = 600_000)
    public void closeStaleCallsignLinks() {
        Instant staleBefore = clock.instant().minus(entitySettings().callsignGap());
        List<Object[]> stale = links.findStaleAirLinks(staleBefore, 10_000);
        String source = null;
        List<String> ids = new ArrayList<>();
        for (Object[] row : stale) {
            String s = String.valueOf(row[0]);
            if (source == null || !source.equals(s)) {
                flushClose(source, ids);
                source = s;
                ids = new ArrayList<>();
            }
            ids.add(String.valueOf(row[1]));
        }
        flushClose(source, ids);
    }

    private void flushClose(String source, List<String> ids) {
        if (source != null && !ids.isEmpty()) {
            links.closeOpenLinks(source, ids, clock.instant());
        }
    }

    /** Extracts a payload's keys, counting each rejected one ({@code foresignal.entity.invalid_keys}). */
    private List<Key> extract(SourceDescriptor descriptor, Map<String, Object> payload) {
        return EntityKeys.extract(descriptor, payload, type -> EntityResolver.invalidKeys(meters, type).increment());
    }

    private List<String> entitySourceIds() {
        List<String> ids = new ArrayList<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (!descriptor.entityKeys().isEmpty()) {
                ids.add(descriptor.id());
            }
        }
        return ids;
    }

    private Map<String, Object> loadPayload(long seq) {
        List<String> rows = jdbc.queryForList(
                "SELECT payload::text FROM record_version WHERE seq = ? LIMIT 1", String.class, seq);
        return rows.isEmpty() ? Map.of() : parse(rows.get(0));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        try {
            return json.readValue(text, Map.class);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    private static Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        return Instant.parse(String.valueOf(value));
    }

    private EntitySettings entitySettings() {
        return settings.get(SettingKeys.ENTITY, EntitySettings.class).orElseGet(EntitySettings::defaults);
    }

    private long loadCursor() {
        List<String> values = jdbc.queryForList(
                "SELECT value::text FROM app_setting WHERE key = ?", String.class, SettingKeys.ENTITY_CURSOR);
        if (values.isEmpty()) {
            return -1;
        }
        try {
            return Long.parseLong(values.get(0).replace("\"", "").strip());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void advance(long seq) {
        if (seq > cursor) {
            cursor = seq;
            storeCursor(seq);
        }
    }

    private void storeCursor(long seq) {
        settings.put(SettingKeys.ENTITY_CURSOR, seq);
    }

    /** Counts a dropped attribution for observability. */
    void count(String name) {
        meters.counter(name).increment();
    }
}
