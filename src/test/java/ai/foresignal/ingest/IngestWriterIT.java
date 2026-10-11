package ai.foresignal.ingest;

import static ai.foresignal.testsupport.ingest.TestIngestSources.DYNAMIC;
import static ai.foresignal.testsupport.ingest.TestIngestSources.DYNAMIC_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST;
import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.UsageMeter;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.store.history.PartitionManager;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.Subscription;
import ai.foresignal.test.DatabaseCleaner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.json.JsonMapper;

/**
 * The single ingest writer against a real PostGIS (Core-Data-Model §6, §10.1; invariants I1, I2,
 * I3, I10). Seeds only through {@link TestRecords}, except where a test deliberately breaks the
 * database (a missing live row, a held table lock).
 */
@IntegrationTest
@Import(TestIngestSources.class)
class IngestWriterIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired TestRecords records;
    @Autowired IngestWriter writer;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired ChangeBus changeBus;
    @Autowired LiveBus liveBus;
    @Autowired MeterRegistry meters;
    @Autowired SourceRegistry registry;
    @Autowired DatabaseCleaner cleaner;
    @Autowired DataSource dataSource;
    @Autowired JsonMapper jsonMapper;
    @Autowired ApplicationContext context;

    private final List<IngestWriter> extraWriters = new ArrayList<>();
    private final List<Subscription> subscriptions = new ArrayList<>();

    @BeforeEach
    void setClock() {
        clock.setInstant(T);
    }

    @AfterEach
    void tearDown() {
        extraWriters.forEach(IngestWriter::stop);
        subscriptions.forEach(Subscription::close);
        TestIngestSources.unregisterDynamic(registry);
        writer.setPurgeChunk(IngestWriter.DEFAULT_PURGE_CHUNK);
        assertHeadsAndLiveRowsPair();
    }

    // ------------------------------------------------------------------ helpers

    static NormalizedRecord.Builder rec(String id, String kind, Instant observedAt) {
        return NormalizedRecord.builder(id, kind).observedAt(observedAt);
    }

    static Instant t(long seconds) {
        return T.plusSeconds(seconds);
    }

    /** I10: every head has exactly one live row, and the reverse. */
    private void assertHeadsAndLiveRowsPair() {
        Integer headsWithoutLive = jdbc.queryForObject(
                "SELECT count(*) FROM record_head h LEFT JOIN record_live l USING (source_id, external_id)"
                        + " WHERE l.external_id IS NULL",
                Integer.class);
        Integer liveWithoutHead = jdbc.queryForObject(
                "SELECT count(*) FROM record_live l LEFT JOIN record_head h USING (source_id, external_id)"
                        + " WHERE h.external_id IS NULL",
                Integer.class);
        assertThat(headsWithoutLive).as("heads without a live row").isZero();
        assertThat(liveWithoutHead).as("live rows without a head").isZero();
    }

    private List<Map<String, Object>> versions(String source, String id) {
        return jdbc.queryForList(
                """
                SELECT seq, version, prev_seq, persist_reason, qa_flags, tombstone, payload::text AS payload,
                       geom IS NULL AS geom_null, ST_SRID(geom) AS srid, ingested_at, observed_at, batch_id
                  FROM record_version WHERE source_id = ? AND external_id = ? ORDER BY seq
                """,
                source,
                id);
    }

    private List<Integer> reasons(String source, String id) {
        return versions(source, id).stream().map(v -> ((Number) v.get("persist_reason")).intValue()).toList();
    }

    private Map<String, Object> head(String source, String id) {
        return jdbc.queryForMap(
                """
                SELECT seq, version, tombstone, payload::text AS payload, hash_recipe, static_hash,
                       persisted_geom IS NULL AS geom_null, ST_SRID(persisted_geom) AS srid
                  FROM record_head WHERE source_id = ? AND external_id = ?
                """,
                source,
                id);
    }

    private Map<String, Object> live(String source, String id) {
        return jdbc.queryForMap(
                """
                SELECT observed_at, last_received_at, valid_until, dirty, kinematic::text AS kinematic, qa_flags,
                       geom IS NULL AS geom_null, ST_SRID(geom) AS srid
                  FROM record_live WHERE source_id = ? AND external_id = ?
                """,
                source,
                id);
    }

    private int count(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> json(Object text) {
        return jsonMapper.readValue((String) text, Map.class);
    }

    private Map<String, Object> currentPayload(String source, String id) {
        return json(jdbc.queryForObject(
                "SELECT jsonb_strip_nulls(h.payload || l.kinematic)::text FROM record_head h"
                        + " JOIN record_live l USING (source_id, external_id) WHERE h.source_id = ? AND h.external_id = ?",
                String.class,
                source,
                id));
    }

    static Instant instant(Object value) {
        if (value instanceof Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        return (Instant) value;
    }

    static long number(Object value) {
        return ((Number) value).longValue();
    }

    private double counter(String name) {
        return meters.counter(name).count();
    }

    private IngestWriter newWriter(IngestQueue queue, IngestProperties.Writer settings, ChangeBus changes, LiveBus live) {
        IngestWriter created = new IngestWriter(
                queue,
                jdbc,
                context.getBean(PlatformTransactionManager.class),
                context.getBean(PartitionManager.class),
                context.getBean(SourceIngestProfiles.class),
                context.getBean(PersistGate.class),
                changes,
                live,
                context.getBean(UsageMeter.class),
                context.getBeanProvider(EntityKeyListener.class),
                context.getBeanProvider(EntityKeyExtractor.class),
                context.getBeanProvider(JdbcConnectionDetails.class),
                clock,
                meters,
                jsonMapper,
                new IngestProperties(4, settings));
        extraWriters.add(created);
        return created;
    }

    private IngestWriterLock newLock() {
        JdbcConnectionDetails details = context.getBean(JdbcConnectionDetails.class);
        return new IngestWriterLock(details.getJdbcUrl(), details.getUsername(), details.getPassword());
    }

    // ------------------------------------------------------------------ tests

    @Nested
    @DisplayName("versions, head and live")
    class Versions {

        @Test
        @DisplayName("5 observations of one entity in one batch under ALL give 5 chained versions; head and live equal the 5th")
        void fiveInOneBatch() {
            NormalizedRecord[] five = IntStream.range(0, 5)
                    .mapToObj(i -> rec("e1", STANDARD_KIND, t(i)).put("n", i).build())
                    .toArray(NormalizedRecord[]::new);

            IngestOutcome outcome = records.ingestAt(STANDARD, t(10), five);

            assertThat(outcome.seen()).isEqualTo(5);
            assertThat(outcome.persisted()).isEqualTo(5);
            assertThat(outcome.persisted(PersistReason.FIRST)).isEqualTo(1);
            assertThat(outcome.persisted(PersistReason.SIGNIFICANT)).isEqualTo(4);
            assertThat(outcome.toSeq() - outcome.fromSeq()).isEqualTo(4);
            assertThat(outcome.versionBytes()).isGreaterThan(5 * 120);

            List<Map<String, Object>> versions = versions(STANDARD, "e1");
            assertThat(versions).hasSize(5);
            for (int i = 0; i < 5; i++) {
                Map<String, Object> v = versions.get(i);
                assertThat(number(v.get("version"))).isEqualTo(i + 1);
                assertThat(json(v.get("payload"))).containsEntry("n", i);
                if (i == 0) {
                    assertThat(v.get("prev_seq")).isNull();
                } else {
                    assertThat(number(v.get("prev_seq"))).isEqualTo(number(versions.get(i - 1).get("seq")));
                    assertThat(number(v.get("seq"))).isGreaterThan(number(versions.get(i - 1).get("seq")));
                }
            }
            assertThat(reasons(STANDARD, "e1")).containsExactly(1, 3, 3, 3, 3);

            Map<String, Object> head = head(STANDARD, "e1");
            assertThat(number(head.get("seq"))).isEqualTo(number(versions.get(4).get("seq")));
            assertThat(number(head.get("version"))).isEqualTo(5);
            assertThat(json(head.get("payload"))).containsEntry("n", 4);
            Map<String, Object> live = live(STANDARD, "e1");
            assertThat(instant(live.get("observed_at"))).isEqualTo(t(4));
            assertThat(instant(live.get("last_received_at"))).isEqualTo(t(10));
            assertThat(live.get("dirty")).isEqualTo(false);

            Map<String, Object> fetch = jdbc.queryForMap("SELECT * FROM fetch_log WHERE id = ?", versions.get(0).get("batch_id"));
            assertThat(number(fetch.get("versions_written"))).isEqualTo(5);
            assertThat(number(fetch.get("records_seen"))).isEqualTo(5);
            assertThat(number(fetch.get("from_seq"))).isEqualTo(outcome.fromSeq());
            assertThat(number(fetch.get("to_seq"))).isEqualTo(outcome.toSeq());
            assertThat(instant(fetch.get("ingested_at"))).isEqualTo(T);
        }

        @Test
        @DisplayName("a LATE observation has no side effects and is counted")
        void lateHasNoSideEffects() {
            records.ingestAt(STANDARD, t(10), rec("late", STANDARD_KIND, t(10)).put("v", 1).build());

            IngestOutcome outcome = records.ingestAt(STANDARD, t(20), rec("late", STANDARD_KIND, t(5)).put("v", 2).build());

            assertThat(outcome.late()).isEqualTo(1);
            assertThat(outcome.persisted()).isZero();
            assertThat(versions(STANDARD, "late")).hasSize(1);
            assertThat(json(head(STANDARD, "late").get("payload"))).containsEntry("v", 1);
            Map<String, Object> live = live(STANDARD, "late");
            assertThat(instant(live.get("observed_at"))).isEqualTo(t(10));
            assertThat(instant(live.get("last_received_at"))).isEqualTo(t(10));
            assertThat(count("SELECT count(*) FROM fetch_log WHERE late = 1")).isEqualTo(1);
        }

        @Test
        @DisplayName("a future observed_at is clamped to received_at; FUTURE_CLAMPED only beyond the skew tolerance")
        void clamp() {
            Instant received = t(60);
            IngestOutcome near = records.ingestAt(STANDARD, received, rec("near", STANDARD_KIND, received.plusSeconds(60)).build());
            IngestOutcome far = records.ingestAt(STANDARD, received, rec("far", STANDARD_KIND, received.plus(Duration.ofMinutes(10))).build());

            assertThat(near.clamped()).isZero();
            assertThat(far.clamped()).isEqualTo(1);
            Map<String, Object> nearVersion = versions(STANDARD, "near").get(0);
            Map<String, Object> farVersion = versions(STANDARD, "far").get(0);
            assertThat(instant(nearVersion.get("observed_at"))).isEqualTo(received);
            assertThat(instant(farVersion.get("observed_at"))).isEqualTo(received);
            assertThat(number(nearVersion.get("qa_flags")) & QaFlags.FUTURE_CLAMPED).isZero();
            assertThat(number(farVersion.get("qa_flags")) & QaFlags.FUTURE_CLAMPED).isEqualTo(QaFlags.FUTURE_CLAMPED);
        }

        @Test
        @DisplayName("geometry is stored with SRID 4326 on the version, the head and the live row")
        void srid() {
            records.ingestAt(STANDARD, T, rec("geo", STANDARD_KIND, T).point(10.0, 50.0).build());

            assertThat(number(versions(STANDARD, "geo").get(0).get("srid"))).isEqualTo(4326);
            assertThat(number(head(STANDARD, "geo").get("srid"))).isEqualTo(4326);
            assertThat(number(live(STANDARD, "geo").get("srid"))).isEqualTo(4326);
        }

        @Test
        @DisplayName("tombstones: an existing head is tombstoned; a missing or already tombstoned head is a SKIP")
        void tombstones() {
            records.ingestAt(STANDARD, T, rec("gone", STANDARD_KIND, T).point(10.0, 50.0).put("v", 1).build());

            IngestOutcome tombstoned = records.ingestAt(STANDARD, t(10), NormalizedRecord.tombstone("gone", STANDARD_KIND, t(10)));
            assertThat(tombstoned.persisted(PersistReason.TOMBSTONE)).isEqualTo(1);
            Map<String, Object> head = head(STANDARD, "gone");
            assertThat(head.get("tombstone")).isEqualTo(true);
            assertThat(json(head.get("payload"))).isEmpty();
            assertThat(head.get("geom_null")).isEqualTo(true);
            Map<String, Object> live = live(STANDARD, "gone");
            assertThat(live.get("geom_null")).isEqualTo(true);
            assertThat(json(live.get("kinematic"))).isEmpty();

            IngestOutcome missing = records.ingestAt(STANDARD, t(20), NormalizedRecord.tombstone("never", STANDARD_KIND, t(20)));
            assertThat(missing.skipped()).isEqualTo(1);
            assertThat(count("SELECT count(*) FROM record_head WHERE external_id = 'never'")).isZero();

            IngestOutcome again = records.ingestAt(STANDARD, t(30), NormalizedRecord.tombstone("gone", STANDARD_KIND, t(30)));
            assertThat(again.skipped()).isEqualTo(1);
            assertThat(reasons(STANDARD, "gone")).containsExactly(1, 4);
        }
    }

    @Nested
    @DisplayName("policies")
    class Policies {

        @Test
        @DisplayName("TTL re-observation is a touch; a fixed observed_at refreshes last_received_at once touchMin passed, throttled before")
        void touchAndThrottle() {
            Instant observed = T.minus(Duration.ofHours(1));
            records.ingestAt(TTL, T, rec("q", TTL_KIND, observed).put("mag", 5).build());

            IngestOutcome early = records.ingestAt(TTL, t(30), rec("q", TTL_KIND, observed).put("mag", 5).build());
            assertThat(early.touchesThrottled()).isEqualTo(1);
            assertThat(instant(live(TTL, "q").get("last_received_at"))).isEqualTo(T);

            IngestOutcome due = records.ingestAt(TTL, t(90), rec("q", TTL_KIND, observed).put("mag", 5).build());
            assertThat(due.touched()).isEqualTo(1);
            Map<String, Object> live = live(TTL, "q");
            assertThat(instant(live.get("last_received_at"))).isEqualTo(t(90));
            assertThat(instant(live.get("valid_until"))).isEqualTo(t(90).plus(Duration.ofMinutes(10)));
            assertThat(instant(live.get("observed_at"))).isEqualTo(observed);

            IngestOutcome again = records.ingestAt(TTL, t(100), rec("q", TTL_KIND, observed).put("mag", 5).build());
            assertThat(again.touchesThrottled()).isEqualTo(1);
            assertThat(versions(TTL, "q")).hasSize(1);
        }

        @Test
        @DisplayName("an unchanged entity past its keyframe persists a HEARTBEAT")
        void heartbeat() {
            records.ingestAt(TTL, T, rec("hb", TTL_KIND, T).put("mag", 5).build());
            IngestOutcome outcome = records.ingestAt(TTL, t(580), rec("hb", TTL_KIND, t(580)).put("mag", 5).build());

            assertThat(outcome.persisted(PersistReason.HEARTBEAT)).isEqualTo(1);
            assertThat(reasons(TTL, "hb")).containsExactly(1, 8);
        }

        @Test
        @DisplayName("the retention keyframe is due at window/4: KEYFRAME when live is dirty, HEARTBEAT when clean")
        void retentionKeyframe() {
            Duration quarter = Duration.ofDays(7).dividedBy(4);
            records.ingestAt(LATEST, T, rec("k", LATEST_KIND, T).put("name", "a").put("v", 1).build());
            IngestOutcome live = records.ingestAt(LATEST, t(10), rec("k", LATEST_KIND, t(10)).put("name", "a").put("v", 2).build());
            assertThat(live.live()).isEqualTo(1);

            Instant before = T.plus(quarter).minusSeconds(60);
            IngestOutcome notYet = records.ingestAt(LATEST, before, rec("k", LATEST_KIND, before).put("name", "a").put("v", 2).build());
            assertThat(notYet.persisted()).isZero();

            Instant due = T.plus(quarter);
            IngestOutcome keyframe = records.ingestAt(LATEST, due, rec("k", LATEST_KIND, due).put("name", "a").put("v", 2).build());
            assertThat(keyframe.persisted(PersistReason.KEYFRAME)).isEqualTo(1);
            assertThat(live(LATEST, "k").get("dirty")).isEqualTo(false);

            Instant next = due.plus(quarter);
            IngestOutcome heartbeat = records.ingestAt(LATEST, next, rec("k", LATEST_KIND, next).put("name", "a").put("v", 2).build());
            assertThat(heartbeat.persisted(PersistReason.HEARTBEAT)).isEqualTo(1);
            assertThat(reasons(LATEST, "k")).containsExactly(1, 7, 8);
        }

        private NormalizedRecord.Builder aircraft(long second, Double lat, int alt) {
            NormalizedRecord.Builder b = rec("ac", TRACK_KIND, t(second))
                    .put("callsign", "ABC123")
                    .put("squawk", "1200")
                    .put("alt", alt)
                    .put("gs", 0)
                    .put("track", 90);
            if (lat != null) {
                b.point(10.0, lat);
            }
            return b;
        }

        @Test
        @DisplayName("SAMPLED persists a SAMPLE on deviation and on a threshold, and LIVE before minInterval")
        void sampled() {
            assertThat(records.ingestAt(TRACK, t(0), aircraft(0, 50.0, 10000).build()).persisted(PersistReason.FIRST)).isEqualTo(1);

            IngestOutcome moved = records.ingestAt(TRACK, t(30), aircraft(30, 50.02, 10000).build());
            assertThat(moved.live()).as("2 km away but before minInterval").isEqualTo(1);

            IngestOutcome deviated = records.ingestAt(TRACK, t(70), aircraft(70, 50.02, 10000).build());
            assertThat(deviated.persisted(PersistReason.SAMPLE)).as("deviation").isEqualTo(1);

            IngestOutcome climbed = records.ingestAt(TRACK, t(140), aircraft(140, 50.02, 11500).build());
            assertThat(climbed.persisted(PersistReason.SAMPLE)).as("alt threshold").isEqualTo(1);

            IngestOutcome small = records.ingestAt(TRACK, t(150), aircraft(150, 50.02, 11600).build());
            assertThat(small.live()).isEqualTo(1);
            assertThat(reasons(TRACK, "ac")).containsExactly(1, 6, 6);
        }

        @Test
        @DisplayName("TRACK null geometry carries the last position forward with POSITION_STALE and no SIGNIFICANT")
        void carryForward() {
            records.ingestAt(TRACK, t(0), aircraft(0, 50.0, 10000).build());

            IngestOutcome lost = records.ingestAt(TRACK, t(30), aircraft(30, null, 10100).build());
            assertThat(lost.live()).isEqualTo(1);
            Map<String, Object> live = live(TRACK, "ac");
            assertThat(live.get("geom_null")).isEqualTo(false);
            assertThat(number(live.get("qa_flags")) & QaFlags.POSITION_STALE).isEqualTo(QaFlags.POSITION_STALE);

            IngestOutcome keyframe = records.ingestAt(TRACK, t(250), aircraft(250, null, 10100).build());
            assertThat(keyframe.persisted(PersistReason.KEYFRAME)).isEqualTo(1);
            List<Map<String, Object>> versions = versions(TRACK, "ac");
            assertThat(reasons(TRACK, "ac")).containsExactly(1, 7);
            assertThat(versions.get(1).get("geom_null")).isEqualTo(false);
            assertThat(number(versions.get(1).get("qa_flags")) & QaFlags.POSITION_STALE).isEqualTo(QaFlags.POSITION_STALE);
        }

        @Test
        @DisplayName("LATEST persists only significant changes; other changes are LIVE")
        void latest() {
            records.ingestAt(LATEST, T, rec("l", LATEST_KIND, T).put("name", "a").put("v", 1).build());
            assertThat(records.ingestAt(LATEST, t(120), rec("l", LATEST_KIND, t(120)).put("name", "a").put("v", 2).build()).live())
                    .isEqualTo(1);
            assertThat(records.ingestAt(LATEST, t(180), rec("l", LATEST_KIND, t(180)).put("name", "b").put("v", 2).build())
                            .persisted(PersistReason.SIGNIFICANT))
                    .isEqualTo(1);
            assertThat(reasons(LATEST, "l")).containsExactly(1, 3);
        }

        @Test
        @DisplayName("LIVE then PERSIST then LIVE keeps jsonb_strip_nulls(h.payload || l.kinematic) right when a key drops")
        void mergedPayload() {
            records.ingestAt(LATEST, T, rec("m", LATEST_KIND, T).put("name", "a").put("x", 1).put("y", 1).build());
            assertThat(currentPayload(LATEST, "m")).isEqualTo(Map.of("name", "a", "x", 1, "y", 1));

            records.ingestAt(LATEST, t(10), rec("m", LATEST_KIND, t(10)).put("name", "a").put("x", 2).build());
            assertThat(currentPayload(LATEST, "m")).isEqualTo(Map.of("name", "a", "x", 2));
            assertThat(live(LATEST, "m").get("dirty")).isEqualTo(true);

            records.ingestAt(LATEST, t(20), rec("m", LATEST_KIND, t(20)).put("name", "b").put("x", 2).build());
            assertThat(currentPayload(LATEST, "m")).isEqualTo(Map.of("name", "b", "x", 2));
            assertThat(live(LATEST, "m").get("dirty")).isEqualTo(false);

            records.ingestAt(LATEST, t(30), rec("m", LATEST_KIND, t(30)).put("name", "b").build());
            assertThat(currentPayload(LATEST, "m")).isEqualTo(Map.of("name", "b"));
            assertThat(reasons(LATEST, "m")).containsExactly(1, 3);
        }
    }

    @Nested
    @DisplayName("lazy recipe migration")
    class Recipes {

        @Test
        @DisplayName("adding volatileFields gives 0 versions on re-fetch")
        void addingVolatileFields() {
            TestIngestSources.registerDynamic(registry, List.of(), List.of());
            records.ingestAt(DYNAMIC, T, rec("q", DYNAMIC_KIND, T).put("mag", 5).put("updated", "t1").build());
            assertThat(records.ingestAt(DYNAMIC, t(60), rec("q", DYNAMIC_KIND, t(60)).put("mag", 5).put("updated", "t2").build())
                            .persisted())
                    .isEqualTo(1);

            TestIngestSources.registerDynamic(registry, List.of("updated"), List.of());
            IngestOutcome refetch =
                    records.ingestAt(DYNAMIC, t(120), rec("q", DYNAMIC_KIND, t(120)).put("mag", 5).put("updated", "t3").build());

            assertThat(refetch.persisted()).isZero();
            assertThat(number(head(DYNAMIC, "q").get("hash_recipe")))
                    .isEqualTo(ContentHasher.recipe(List.of("updated"), List.of()));
        }

        @Test
        @DisplayName("adding a significant field causes no version storm; every head's hash_recipe is updated")
        void addingSignificantField() {
            TestIngestSources.registerDynamic(registry, List.of(), List.of());
            NormalizedRecord[] three = IntStream.range(0, 3)
                    .mapToObj(i -> rec("d" + i, DYNAMIC_KIND, T).put("mag", i).put("depth", 10).build())
                    .toArray(NormalizedRecord[]::new);
            assertThat(records.ingestAt(DYNAMIC, T, three).persisted()).isEqualTo(3);

            TestIngestSources.registerDynamic(registry, List.of(), List.of("mag"));
            IngestOutcome refetch = records.ingestAt(DYNAMIC, t(60), three);

            assertThat(refetch.persisted()).isZero();
            int recipe = ContentHasher.recipe(List.of(), List.of("mag"));
            assertThat(count("SELECT count(*) FROM record_head WHERE source_id = ? AND hash_recipe = ?", DYNAMIC, recipe))
                    .isEqualTo(3);
            assertThat(json(live(DYNAMIC, "d1").get("kinematic"))).isEqualTo(Map.of("depth", 10));
            assertThat(currentPayload(DYNAMIC, "d1")).isEqualTo(Map.of("mag", 1, "depth", 10));
        }
    }

    @Nested
    @DisplayName("head purge")
    class Purge {

        @Test
        @DisplayName("purges only heads below floorSeq and stale before staleBefore, re-enqueuing between chunks")
        void purge() throws Exception {
            records.ingestAt(STANDARD, T,
                    rec("p1", STANDARD_KIND, T).build(),
                    rec("p2", STANDARD_KIND, T).build(),
                    rec("p3", STANDARD_KIND, T).build(),
                    rec("p4", STANDARD_KIND, T).build());
            records.ingestAt(STANDARD, T.plus(Duration.ofHours(2)), rec("p5", STANDARD_KIND, T.plus(Duration.ofHours(2))).build());
            long floor = number(head(STANDARD, "p4").get("seq"));
            writer.setPurgeChunk(2);
            double requeuedBefore = counter("foresignal.ingest.purge.requeued");

            CompletableFuture<PurgeOutcome> future = context.getBean(IngestQueue.class)
                    .submitPurge(List.of(new PurgeSpec(STANDARD, floor, T.plus(Duration.ofHours(1)))));
            PurgeOutcome outcome = future.get(30, TimeUnit.SECONDS);

            assertThat(outcome.headsDeleted()).containsEntry(STANDARD, 3);
            assertThat(jdbc.queryForList("SELECT external_id FROM record_head ORDER BY external_id", String.class))
                    .containsExactly("p4", "p5");
            assertThat(count("SELECT count(*) FROM record_live")).isEqualTo(2);
            assertThat(counter("foresignal.ingest.purge.requeued")).isGreaterThanOrEqualTo(requeuedBefore + 1);
        }
    }

    @Nested
    @DisplayName("lifecycle and state")
    class Lifecycle {

        @Test
        @DisplayName("a second writer with exclusive=true refuses to start while the lock is held")
        void exclusiveLock() {
            try (IngestWriterLock holder = newLock()) {
                assertThat(holder.tryAcquire()).isTrue();
                IngestWriter second = newWriter(
                        new IngestQueue(4, meters), IngestProperties.Writer.defaults().withExclusive(true), changeBus, liveBus);
                assertThatThrownBy(second::start)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage(IngestWriterLock.HELD_MESSAGE);
                assertThat(second.status().state()).isEqualTo(WriterState.STOPPED);
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException(e);
            }

            IngestWriter exclusive = newWriter(
                    new IngestQueue(4, meters), IngestProperties.Writer.defaults().withExclusive(true), changeBus, liveBus);
            exclusive.start();
            assertThat(exclusive.status().state()).isEqualTo(WriterState.RUNNING);
            try (IngestWriterLock other = newLock()) {
                assertThat(other.tryAcquire()).isFalse();
                exclusive.stop();
                assertThat(other.tryAcquire()).isTrue();
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException(e);
            }
        }

        @Test
        @DisplayName("a restart with an earlier clock holds batches (CLOCK_BEHIND) until the clock catches up")
        void clockBehind() throws Exception {
            records.ingestAt(STANDARD, T, rec("c1", STANDARD_KIND, T).build());
            clock.setInstant(T.minus(Duration.ofMinutes(10)));
            IngestQueue queue = new IngestQueue(4, meters);
            IngestWriter restarted = newWriter(
                    queue,
                    IngestProperties.Writer.defaults().withExclusive(false).withClockRecheck(Duration.ofMillis(100)),
                    changeBus,
                    liveBus);
            restarted.start();
            assertThat(restarted.status().state()).isEqualTo(WriterState.CLOCK_BEHIND);
            assertThat(restarted.status().warnings()).anyMatch(w -> w.contains("behind"));

            CompletableFuture<IngestOutcome> held = queue.submit(
                    STANDARD,
                    FetchLogEntry.ok(STANDARD, FetchTrigger.MANUAL, T, T),
                    List.of(new IngestRecord(rec("c2", STANDARD_KIND, T).build(), T, 1)),
                    Duration.ofSeconds(5));
            Thread.sleep(500);
            assertThat(held).isNotDone();

            clock.setInstant(T.plusSeconds(1));
            IngestOutcome outcome = held.get(10, TimeUnit.SECONDS);
            assertThat(outcome.persisted()).isEqualTo(1);
            assertThat(restarted.status().state()).isEqualTo(WriterState.RUNNING);
            assertThat(instant(versions(STANDARD, "c2").get(0).get("ingested_at"))).isAfterOrEqualTo(T);
        }

        @Test
        @DisplayName("resetState after a truncate restarts the writer state and the ChangeBus watermark")
        void resetAfterTruncate() {
            IngestOutcome first = records.ingestAt(STANDARD, T,
                    rec("r1", STANDARD_KIND, T).build(), rec("r2", STANDARD_KIND, T).build(), rec("r3", STANDARD_KIND, T).build());
            assertThat(writer.status().lastSeq()).isEqualTo(first.toSeq());
            assertThat(changeBus.lastPublishedSeq()).isEqualTo(first.toSeq());
            assertThat(writer.status().lastIngestedAt()).isEqualTo(T);

            cleaner.clean();

            assertThat(writer.status().lastSeq()).isZero();
            assertThat(writer.status().lastIngestedAt()).isNull();
            assertThat(writer.lastAllocatedSeq()).isZero();
            assertThat(changeBus.lastPublishedSeq()).isZero();
            assertThat(records.ingestAt(STANDARD, T, rec("r4", STANDARD_KIND, T).build()).fromSeq()).isEqualTo(1);
        }

        @Test
        @DisplayName("a transient lock timeout (55P03) is retried and the batch then succeeds")
        void transientRetry() throws Exception {
            double before = counter("foresignal.ingest.batch.retries");
            CompletableFuture<IngestOutcome> future;
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    statement.execute("LOCK TABLE record_live IN ACCESS EXCLUSIVE MODE");
                }
                future = records.submitAt(STANDARD, T, List.of(rec("locked", STANDARD_KIND, T).build()));
                await().atMost(Duration.ofSeconds(10))
                        .until(() -> counter("foresignal.ingest.batch.retries") >= before + 1);
                assertThat(future).isNotDone();
                connection.rollback();
                connection.setAutoCommit(true);
            }
            assertThat(TestRecords.await(future).persisted()).isEqualTo(1);
        }

        @Test
        @DisplayName("a NOT NULL failure fails only that batch; the next one succeeds above the lost seq")
        void notNullFailure() {
            FetchLogEntry broken = new FetchLogEntry(
                    STANDARD, null, FetchStatus.OK, T, T, null, 0, null, null, null, null, null, null, null, null, null);
            CompletableFuture<IngestOutcome> failed =
                    records.submit(STANDARD, broken, T, List.of(rec("bad", STANDARD_KIND, T).build()));
            assertThatThrownBy(() -> TestRecords.await(failed))
                    .satisfies(e -> assertThat(IngestWriter.sqlState(e)).isEqualTo("23502"));

            IngestOutcome next = records.ingestAt(STANDARD, T, rec("good", STANDARD_KIND, T).build());
            assertThat(next.persisted()).isEqualTo(1);
            assertThat(next.fromSeq()).as("the failed batch consumed seq 1").isGreaterThan(1);
            assertThat(jdbc.queryForList("SELECT external_id FROM record_head", String.class)).containsExactly("good");
        }

        @Test
        @DisplayName("a batch with zero versions still writes its seq_clock row")
        void seqClockOnEmptyBatch() {
            IngestOutcome outcome = records.ingestAt(STANDARD, T);

            assertThat(outcome.seen()).isZero();
            Map<String, Object> row = jdbc.queryForMap("SELECT minute, first_seq FROM seq_clock");
            assertThat(instant(row.get("minute"))).isEqualTo(T.truncatedTo(ChronoUnit.MINUTES));
            assertThat(number(row.get("first_seq"))).isEqualTo(1);
            Map<String, Object> fetch = jdbc.queryForMap("SELECT versions_written, from_seq FROM fetch_log");
            assertThat(number(fetch.get("versions_written"))).isZero();
            assertThat(fetch.get("from_seq")).isNull();
        }

        @Test
        @DisplayName("a missing live row is rebuilt from its head and counted")
        void repairsMissingLive() {
            records.ingestAt(STANDARD, T, rec("lonely", STANDARD_KIND, T).put("v", 1).build());
            jdbc.update("DELETE FROM record_live WHERE source_id = ? AND external_id = ?", STANDARD, "lonely");
            double before = counter("foresignal.ingest.repair.live_missing");

            IngestOutcome outcome = records.ingestAt(STANDARD, t(10), rec("lonely", STANDARD_KIND, T).put("v", 1).build());

            assertThat(outcome.persisted()).isZero();
            assertThat(outcome.touchesThrottled()).isEqualTo(1);
            assertThat(counter("foresignal.ingest.repair.live_missing")).isEqualTo(before + 1);
            Map<String, Object> live = live(STANDARD, "lonely");
            assertThat(live.get("dirty")).isEqualTo(false);
            assertThat(instant(live.get("observed_at"))).isEqualTo(T);
        }
    }

    @Nested
    @DisplayName("buses")
    class Buses {

        @Test
        @DisplayName("change is published before live, and both before the future completes")
        void changeBeforeLive() throws Exception {
            List<String> order = new CopyOnWriteArrayList<>();
            List<ChangeBatch> changes = new CopyOnWriteArrayList<>();
            List<LiveBatch> lives = new CopyOnWriteArrayList<>();
            ChangeBus recordingChanges = new ChangeBus(() -> 0L, meters) {
                @Override
                public void publish(ChangeBatch batch) {
                    order.add("change");
                    changes.add(batch);
                    super.publish(batch);
                }
            };
            LiveBus recordingLive = new LiveBus(meters) {
                @Override
                public void publish(LiveBatch batch) {
                    order.add("live");
                    lives.add(batch);
                    super.publish(batch);
                }
            };
            IngestQueue queue = new IngestQueue(4, meters);
            IngestWriter recording = newWriter(queue, IngestProperties.Writer.defaults().withExclusive(false), recordingChanges, recordingLive);
            recording.start();

            queue.submit(LATEST, FetchLogEntry.ok(LATEST, FetchTrigger.MANUAL, T, T),
                            List.of(new IngestRecord(rec("e2", LATEST_KIND, T).put("name", "a").put("v", 1).build(), T, 1)),
                            Duration.ofSeconds(5))
                    .thenRun(() -> order.add("complete"))
                    .get(10, TimeUnit.SECONDS);
            queue.submit(LATEST, FetchLogEntry.ok(LATEST, FetchTrigger.MANUAL, t(10), t(10)),
                            List.of(
                                    new IngestRecord(rec("e1", LATEST_KIND, t(10)).put("name", "x").build(), t(10), 2),
                                    new IngestRecord(rec("e2", LATEST_KIND, t(10)).put("name", "a").put("v", 2).build(), t(10), 3)),
                            Duration.ofSeconds(5))
                    .thenRun(() -> order.add("complete"))
                    .get(10, TimeUnit.SECONDS);

            assertThat(order).containsExactly("change", "complete", "change", "live", "complete");
            assertThat(changes.get(1).changes()).singleElement().satisfies(c -> {
                assertThat(c.externalId()).isEqualTo("e1");
                assertThat(c.persistReason()).isEqualTo(PersistReason.FIRST);
                assertThat(c.prevSeq()).isNull();
            });
            assertThat(lives.get(0).updates()).singleElement().satisfies(u -> {
                assertThat(u.externalId()).isEqualTo("e2");
                assertThat(u.properties()).containsEntry("v", 2L);
                assertThat(u.version()).isEqualTo(1);
            });
            assertThat(recordingChanges.lastPublishedSeq()).isEqualTo(changes.get(1).toSeq());
        }

        @Test
        @DisplayName("seq is strictly increasing across concurrent submitters, as seen by a bus subscriber")
        void seqMonotonicUnderConcurrency() throws Exception {
            List<ChangeBatch> seen = new CopyOnWriteArrayList<>();
            subscriptions.add(changeBus.subscribe("it-monotonic", new ChangeListener() {
                @Override
                public void onBatch(ChangeBatch batch) {
                    seen.add(batch);
                }

                @Override
                public void onGap(long lastDeliveredSeq) {
                    throw new AssertionError("unexpected gap after " + lastDeliveredSeq);
                }
            }, 1_000));
            ExecutorService pool = Executors.newFixedThreadPool(4);
            List<CompletableFuture<IngestOutcome>> futures = new CopyOnWriteArrayList<>();
            try {
                List<java.util.concurrent.Future<?>> submitters = new ArrayList<>();
                for (int thread = 0; thread < 4; thread++) {
                    int t = thread;
                    submitters.add(pool.submit(() -> {
                        for (int b = 0; b < 10; b++) {
                            List<NormalizedRecord> batch = new ArrayList<>();
                            for (int r = 0; r < 3; r++) {
                                batch.add(rec("c-" + t + "-" + b + "-" + r, STANDARD_KIND, T).build());
                            }
                            futures.add(records.submitAt(STANDARD, T, batch));
                        }
                    }));
                }
                for (java.util.concurrent.Future<?> submitter : submitters) {
                    submitter.get(30, TimeUnit.SECONDS);
                }
            } finally {
                pool.shutdown();
            }
            futures.forEach(TestRecords::await);
            await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(seen).hasSize(40));

            long previous = 0;
            for (ChangeBatch batch : seen) {
                assertThat(batch.fromSeq()).isGreaterThan(previous);
                long inBatch = previous;
                for (var change : batch.changes()) {
                    assertThat(change.seq()).isGreaterThan(inBatch);
                    inBatch = change.seq();
                }
                assertThat(inBatch).isEqualTo(batch.toSeq());
                previous = batch.toSeq();
            }
        }
    }
}
