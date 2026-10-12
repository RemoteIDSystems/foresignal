package ai.foresignal.inbound.edp;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Turns one EDP batch into records, shared by push and pull (prompt 024): validates every item in
 * the protocol's order, applies {@code status} items to the provider's health (no record), maps the
 * other valid items with {@link EdpItemMapper}, and runs the snapshot rules, whose absence
 * tombstones join the batch.
 *
 * <p>Status items are applied before the snapshot page is processed, so an unhealthy status in the
 * same batch already disqualifies the snapshot.
 */
@Component
public class EdpBatchProcessor {

    private final EdpItemValidator validator;
    private final EdpItemMapper mapper;
    private final EdpStatusStore status;
    private final EdpSnapshotService snapshots;

    /** Creates the processor. */
    public EdpBatchProcessor(EdpItemValidator validator, EdpItemMapper mapper, EdpStatusStore status,
            EdpSnapshotService snapshots) {
        this.validator = validator;
        this.mapper = mapper;
        this.status = status;
        this.snapshots = snapshots;
    }

    /**
     * What one batch produced.
     *
     * @param records the records to ingest (items, then absence tombstones)
     * @param indexes the batch index of each record ({@code -1} for absence tombstones)
     * @param valid the valid items
     * @param rejected the rejected items
     * @param items how many items the batch carried
     * @param tombstones how many absence tombstones the snapshot produced
     */
    public record Processed(List<NormalizedRecord> records, List<Integer> indexes, List<EdpValidItem> valid,
            List<EdpItemRejection> rejected, int items, int tombstones) {}

    /**
     * Processes {@code batch}.
     *
     * @param sourceId the source
     * @param manifest its manifest
     * @param schemas its compiled payload schemas
     * @param batch the batch
     * @param endOfPull true when this is a pull page whose {@code nextCursor} is null
     * @param receivedAt when it arrived
     */
    public Processed process(String sourceId, EdpManifestModel manifest, Map<String, CompiledPayloadSchema> schemas,
            EdpBatchReader.EdpBatch batch, boolean endOfPull, Instant receivedAt) {
        EdpItemValidator.ItemCheck check = validator.validate(batch.items(), manifest, schemas, receivedAt);
        List<NormalizedRecord> records = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        for (EdpValidItem item : check.valid()) {
            if ("status".equals(item.type())) {
                JsonNode message = item.item().get("message");
                status.report(sourceId, item.stream() == null ? EdpStats.SOURCE_KEY : item.stream().id(),
                        item.item().path("health").asString("ok"),
                        message == null || message.isNull() ? null : message.asString(), receivedAt);
            }
        }
        for (EdpValidItem item : check.valid()) {
            if ("status".equals(item.type())) {
                continue;
            }
            NormalizedRecord record = mapper.map(item, manifest, receivedAt);
            if (record != null) {
                records.add(record);
                indexes.add(item.index());
            }
        }
        int tombstones = 0;
        EdpBatchChecks.EdpSnapshotRef ref = EdpBatchChecks.snapshot(batch.header());
        if (ref != null) {
            List<NormalizedRecord> absent = snapshots.page(sourceId, manifest, ref, ref.finalPage() || endOfPull,
                    check.valid(), check.rejected(), receivedAt);
            for (NormalizedRecord tombstone : absent) {
                records.add(tombstone);
                indexes.add(-1);
            }
            tombstones = absent.size();
        }
        return new Processed(records, indexes, check.valid(), check.rejected(), batch.items().size(), tombstones);
    }

    /** Folds a processed batch into the provider's stats. */
    public void recordActivity(String sourceId, Processed processed, List<EdpItemRejection> writerRejections,
            Instant receivedAt) {
        Map<String, long[]> counts = new LinkedHashMap<>();
        for (EdpValidItem item : processed.valid()) {
            if (item.stream() != null) {
                counts.computeIfAbsent(item.stream().id(), k -> new long[2])[0]++;
            }
        }
        List<EdpItemRejection> all = new ArrayList<>(processed.rejected());
        all.addAll(writerRejections);
        for (EdpItemRejection rejection : all) {
            if (rejection.stream() != null) {
                counts.computeIfAbsent(rejection.stream(), k -> new long[2])[1]++;
            }
        }
        if (counts.isEmpty() && all.isEmpty()) {
            return;
        }
        status.update(sourceId, stats -> {
            counts.forEach((stream, c) -> {
                EdpStats.StreamStats previous = stats.streams().get(stream);
                long items = (previous == null ? 0 : previous.items()) + c[0];
                long rejected = (previous == null ? 0 : previous.rejected()) + c[1];
                Instant last = c[0] > 0 ? receivedAt : previous == null ? null : previous.lastItemAt();
                stats.streams().put(stream, new EdpStats.StreamStats(items, rejected, last));
            });
            if (!all.isEmpty()) {
                List<EdpItemRejection> errors = new ArrayList<>(stats.lastValidationErrors());
                errors.addAll(all);
                int from = Math.max(0, errors.size() - EdpStats.MAX_ERRORS);
                stats.lastValidationErrors().clear();
                stats.lastValidationErrors().addAll(errors.subList(from, errors.size()));
            }
            return stats;
        });
    }
}
