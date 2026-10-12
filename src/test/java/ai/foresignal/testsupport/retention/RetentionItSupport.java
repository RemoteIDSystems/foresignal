package ai.foresignal.testsupport.retention;

import static org.awaitility.Awaitility.await;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.retention.RetentionJob;
import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.history.PartitionManager;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.TestPartitions;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerEngine;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Shared fields and helpers of prompt 019's retention ITs. Records are seeded only through
 * {@link TestRecords}; the clock is the application's {@link MutableClock}.
 */
public abstract class RetentionItSupport {

    /** The start of every scenario. */
    public static final Instant T0 = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired protected TestRecords records;
    @Autowired protected MutableClock clock;
    @Autowired protected RetentionJob job;
    @Autowired protected RetentionStateStore state;
    @Autowired protected TriggerEngine engine;
    @Autowired protected ChangeBus changeBus;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected AppSettingService settings;
    @Autowired protected PartitionManager partitions;
    @Autowired protected RetentionTestSources.TestConsumer consumer;
    @Autowired protected RetentionTestSources.BlockingStep blocking;

    /** Starts every scenario at {@link #T0} with no clamp and the default settings. */
    @BeforeEach
    void resetRetentionFixture() {
        settings.invalidateCache();
        clock.setInstant(T0);
        consumer.clear();
        blocking.release();
    }

    /** Puts the partition layout back ahead of the allocator for the next class. */
    @AfterEach
    void restorePartitions() {
        blocking.release();
        TestPartitions.restore(partitions);
    }

    /** Ingests at {@code at} (the clock is moved there first, so it is also the ingest time). */
    protected IngestOutcome ingestAt(String sourceId, Instant at, NormalizedRecord... batch) {
        clock.setInstant(at);
        return records.ingestAt(sourceId, at, batch);
    }

    /** A record with one payload field. */
    protected static NormalizedRecord record(String externalId, String kind, Instant observedAt, Object value) {
        return NormalizedRecord.builder(externalId, kind).observedAt(observedAt).put("v", value).build();
    }

    /** Waits until the trigger engine has evaluated everything published, so it does not clamp. */
    protected void awaitTriggerCursor() {
        await().atMost(Duration.ofSeconds(30)).until(() -> engine.cursor() >= changeBus.lastPublishedSeq());
    }

    /**
     * Moves the allocator to {@code seq} and the trigger cursor with it, so the engine does not walk
     * the gap chunk by chunk.
     */
    protected void jumpSeq(long seq) {
        awaitTriggerCursor();
        jdbc.queryForObject("SELECT setval('record_seq', ?)", Long.class, seq);
        settings.put(SettingKeys.TRIGGER_CURSOR, seq);
        engine.resetState();
    }

    /** Runs retention at {@code now}, after the trigger engine has caught up. */
    protected RetentionRunResult runAt(Instant now) {
        awaitTriggerCursor();
        clock.setInstant(now);
        return job.run();
    }

    /** Versions of one entity. */
    protected long versions(String sourceId, String externalId) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, sourceId, externalId);
    }

    /** Versions of one source. */
    protected long versions(String sourceId) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ?", Long.class, sourceId);
    }

    /** True when the entity has a head. */
    protected boolean head(String sourceId, String externalId) {
        return jdbc.queryForObject("SELECT count(*) FROM record_head WHERE source_id = ? AND external_id = ?",
                Long.class, sourceId, externalId) > 0;
    }

    /** True when the entity has a live row. */
    protected boolean live(String sourceId, String externalId) {
        return jdbc.queryForObject("SELECT count(*) FROM record_live WHERE source_id = ? AND external_id = ?",
                Long.class, sourceId, externalId) > 0;
    }
}
