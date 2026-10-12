package ai.foresignal.retention;

import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.PurgeOutcome;
import ai.foresignal.ingest.PurgeSpec;
import ai.foresignal.ingest.SourceIngestProfiles;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Step 4 (Core-Data-Model §13, invariant I7): purges heads whose every version is gone.
 *
 * <p>For each candidate whose source was not skipped, a read query first checks that some head
 * qualifies ({@code h.seq < floorSeq AND l.last_received_at < staleBefore}); then <strong>one</strong>
 * {@code HeadPurge} command carries a {@link PurgeSpec} per qualifying (source, kind) to the writer,
 * which deletes heads and live rows in its own transactions (I1). Retention never writes heads or
 * live rows itself.
 *
 * <p>{@code staleBefore = min(now − window, source_health.last_data_at − touchMin)}: a 304 streak
 * leaves {@code last_data_at} at the last full response, so entities of that response never age out
 * while the upstream keeps answering "not modified".
 *
 * <p>Sources with at least one purged head get {@code purgeSeq} = the watermark the writer read when
 * the purge completed.
 */
@Component
public class HeadPurgeStep implements RetentionStep {

    static final String PRECHECK = """
            SELECT EXISTS (SELECT 1 FROM record_head h JOIN record_live l USING (source_id, external_id)
                            WHERE h.source_id = :source AND h.seq < :floor AND l.last_received_at < :stale%s)
            """;

    private final IngestQueue queue;
    private final SourceIngestProfiles profiles;
    private final RetentionResolver resolver;
    private final RetentionProperties properties;

    /** Creates the step. */
    public HeadPurgeStep(IngestQueue queue, SourceIngestProfiles profiles, RetentionResolver resolver,
            RetentionProperties properties) {
        this.queue = queue;
        this.profiles = profiles;
        this.resolver = resolver;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "head-purge";
    }

    @Override
    public int order() {
        return 400;
    }

    @Override
    public void run(RetentionRun run) {
        List<PurgeSpec> specs = new ArrayList<>();
        for (RetentionRun.PurgeCandidate candidate : run.candidates()) {
            if (run.skipped(candidate.sourceId()) || candidate.floorSeq() <= 0) {
                continue;
            }
            Instant stale = staleBefore(run, candidate.sourceId(), candidate.window());
            MapSqlParameterSource params = new MapSqlParameterSource("source", candidate.sourceId())
                    .addValue("floor", candidate.floorSeq())
                    .addValue("stale", Timestamp.from(stale));
            String kindClause = "";
            if (candidate.kind() != null) {
                kindClause = " AND h.kind = :kind";
                params.addValue("kind", candidate.kind());
            }
            Boolean any = run.jdbc().queryForObject(PRECHECK.formatted(kindClause), params, Boolean.class);
            if (Boolean.TRUE.equals(any)) {
                specs.add(new PurgeSpec(candidate.sourceId(), candidate.kind(), candidate.floorSeq(), stale));
            }
        }
        if (specs.isEmpty()) {
            return;
        }
        PurgeOutcome outcome = await(queue.submitPurge(specs));
        for (Map.Entry<String, Integer> entry : outcome.headsDeleted().entrySet()) {
            run.headsPurged(entry.getKey(), entry.getValue(), outcome.watermarkSeq());
            run.deleted("record_head", entry.getValue());
        }
    }

    /** {@code min(now − window, last_data_at − touchMin)}; {@code now − window} without health. */
    Instant staleBefore(RetentionRun run, String sourceId, Duration window) {
        Instant byWindow = run.now().minus(window);
        List<Timestamp> lastData = run.jdbc().queryForList(
                "SELECT last_data_at FROM source_health WHERE source_id = :source",
                new MapSqlParameterSource("source", sourceId), Timestamp.class);
        if (lastData.isEmpty() || lastData.getFirst() == null) {
            return byWindow;
        }
        Duration touchMin = resolver.isRegistered(sourceId) ? profiles.get(sourceId).touchMin() : Duration.ZERO;
        Instant byData = lastData.getFirst().toInstant().minus(touchMin);
        return byData.isBefore(byWindow) ? byData : byWindow;
    }

    private PurgeOutcome await(java.util.concurrent.CompletableFuture<PurgeOutcome> future) {
        try {
            return future.get(properties.purgeTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the head purge", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("The head purge failed: " + e.getCause(), e.getCause());
        } catch (TimeoutException e) {
            throw new IllegalStateException("The head purge did not finish within " + properties.purgeTimeout(), e);
        }
    }
}
