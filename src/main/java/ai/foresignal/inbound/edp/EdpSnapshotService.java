package ai.foresignal.inbound.edp;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Snapshot completeness and absence deletions (docs/External-Data-Protocol.md §3.2, invariant
 * EXT-5).
 *
 * <p>Each page of a snapshot stages the external ids it carried. Absence tombstones (reason
 * {@code ABSENT_FROM_SNAPSHOT}) are produced only when the final page arrives and every one of these
 * holds: pages {@code 1..final} all arrived; no page had a rejected item of the snapshot's stream
 * (a rejected item's id was not staged, so its entity would be removed wrongly); every item of the
 * stream belonged to the snapshot's scope; the snapshot completed within {@code snapshotWindow} of
 * its first page; the stream (or the source) reported no status other than {@code ok} since the
 * first page; and the absent share of the scope's current entities is at most
 * {@code maxAbsentRatio}. Above that ratio the deletion is held, the source turns degraded and the
 * snapshot is kept. Anything else discards the snapshot and removes nothing.
 */
@Service
public class EdpSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(EdpSnapshotService.class);

    /** The tombstone reason stored in the payload. */
    public static final String ABSENT_FROM_SNAPSHOT = "ABSENT_FROM_SNAPSHOT";

    private final EdpSnapshotRepository snapshots;
    private final EdpStatusStore status;
    private final EdpProperties properties;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    /** Creates the service. */
    public EdpSnapshotService(EdpSnapshotRepository snapshots, EdpStatusStore status, EdpProperties properties,
            JdbcTemplate jdbc, PlatformTransactionManager transactions) {
        this.snapshots = snapshots;
        this.status = status;
        this.properties = properties;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
    }

    /**
     * Processes one page of a snapshot.
     *
     * @param sourceId the source
     * @param manifest its manifest
     * @param ref the batch's {@code snapshot} object
     * @param finalPage true when this page ends the snapshot ({@code final: true}, or a pull page
     *     whose {@code nextCursor} is null)
     * @param valid the batch's valid items
     * @param rejected the batch's rejected items
     * @param receivedAt when the page arrived
     * @return the absence tombstones to ingest with this page; empty unless the snapshot completed
     *     cleanly within its limits
     */
    public List<NormalizedRecord> page(String sourceId, EdpManifestModel manifest, EdpBatchChecks.EdpSnapshotRef ref,
            boolean finalPage, List<EdpValidItem> valid, List<EdpItemRejection> rejected, Instant receivedAt) {
        List<NormalizedRecord> tombstones = tx.execute(s -> pageInTransaction(
                sourceId, manifest, ref, finalPage, valid, rejected, receivedAt));
        return tombstones == null ? List.of() : tombstones;
    }

    private List<NormalizedRecord> pageInTransaction(String sourceId, EdpManifestModel manifest,
            EdpBatchChecks.EdpSnapshotRef ref, boolean finalPage, List<EdpValidItem> valid,
            List<EdpItemRejection> rejected, Instant receivedAt) {
        EdpManifestModel.Stream stream = manifest.stream(ref.stream());
        String key = EdpSnapshotEntity.key(sourceId, ref.id());
        EdpSnapshotEntity snapshot = snapshots.findById(key)
                .orElseGet(() -> new EdpSnapshotEntity(sourceId, ref.id(), ref.stream(), ref.scope(), receivedAt));
        if (snapshot.isHeld()) {
            // A held deletion waits for the operator; later pages with the same id change nothing.
            return List.of();
        }
        if (stream == null || !stream.snapshot()) {
            snapshot.fail("stream " + ref.stream() + " is not a snapshot stream");
        }
        if (!java.util.Objects.equals(snapshot.getStream(), ref.stream())
                || !java.util.Objects.equals(snapshot.getScope(), ref.scope())) {
            snapshot.fail("the snapshot's stream or scope changed between pages");
        }
        if (stream != null && stream.snapshotScope() == null && ref.scope() != null) {
            snapshot.fail("stream " + stream.id() + " has no snapshotScope, so a snapshot cannot name a scope");
        }
        Duration elapsed = Duration.between(snapshot.getFirstPageAt(), receivedAt);
        if (elapsed.compareTo(properties.snapshotWindow()) > 0) {
            snapshot.fail("the snapshot took longer than " + properties.snapshotWindow());
        }
        for (EdpItemRejection rejection : rejected) {
            if (ref.stream().equals(rejection.stream())) {
                snapshot.fail("page " + ref.page() + " had a rejected item (index " + rejection.index() + ")");
                break;
            }
        }
        List<String> ids = new ArrayList<>();
        if (stream != null) {
            for (EdpValidItem item : valid) {
                if (item.stream() == null || !item.stream().id().equals(stream.id())
                        || "status".equals(item.type()) || "delete".equals(item.type())) {
                    continue;
                }
                if (stream.snapshotScope() != null && !scopeMatches(item.item(), stream.snapshotScope(), ref.scope())) {
                    snapshot.fail("item " + item.id() + " is outside the snapshot's scope");
                    continue;
                }
                ids.add(item.externalId());
            }
        }
        if (status.unhealthySince(sourceId, ref.stream(), snapshot.getFirstPageAt())) {
            snapshot.fail("the provider reported an unhealthy status during the snapshot");
        }
        snapshot.addPage(ref.page());
        if (finalPage) {
            snapshot.setFinalPage(ref.page());
        }
        snapshots.save(snapshot);
        for (String id : ids) {
            jdbc.update("insert into edp_snapshot_stage (snapshot_key, external_id) values (?, ?)"
                    + " on conflict (snapshot_key, external_id) do nothing", key, id);
        }
        if (!finalPage) {
            return List.of();
        }
        return complete(snapshot, stream, receivedAt);
    }

    /** The final page arrived: check completeness and the absent ratio, then emit or hold. */
    private List<NormalizedRecord> complete(EdpSnapshotEntity snapshot, EdpManifestModel.Stream stream,
            Instant receivedAt) {
        SortedSet<Integer> pages = snapshot.pages();
        Integer last = snapshot.getFinalPage();
        if (snapshot.getFailedReason() == null && (last == null || pages.size() != last || pages.first() != 1
                || pages.last().intValue() != last)) {
            snapshot.fail("pages " + pages + " do not cover 1.." + last);
        }
        if (snapshot.getFailedReason() != null) {
            log.info("Discarding snapshot {} of {}: {}", snapshot.getSnapshotId(), snapshot.getSourceId(),
                    snapshot.getFailedReason());
            discard(snapshot);
            return List.of();
        }
        List<String> heads = currentIds(snapshot.getSourceId(), stream, snapshot.getScope());
        Set<String> staged = new HashSet<>(jdbc.queryForList(
                "select external_id from edp_snapshot_stage where snapshot_key = ?", String.class, snapshot.getKey()));
        List<String> absent = heads.stream().filter(id -> !staged.contains(id)).toList();
        if (!heads.isEmpty() && (double) absent.size() / heads.size() > properties.maxAbsentRatio()) {
            snapshot.setHeld(true);
            snapshots.save(snapshot);
            status.report(snapshot.getSourceId(), EdpStats.SNAPSHOT_KEY, "degraded", "snapshot " + snapshot.getSnapshotId()
                    + " of stream " + stream.id() + " would remove " + absent.size() + " of " + heads.size()
                    + " entities; the deletion is held for the operator", receivedAt);
            log.warn("Holding snapshot {} of {}: {} of {} entities absent", snapshot.getSnapshotId(),
                    snapshot.getSourceId(), absent.size(), heads.size());
            return List.of();
        }
        List<NormalizedRecord> tombstones = new ArrayList<>(absent.size());
        for (String id : absent) {
            tombstones.add(NormalizedRecord.builder(id, stream.kind())
                    .observedAt(receivedAt)
                    .tombstone(true)
                    .put("reason", ABSENT_FROM_SNAPSHOT)
                    .put("snapshotId", snapshot.getSnapshotId())
                    .build());
        }
        discard(snapshot);
        return tombstones;
    }

    /**
     * Releases a held deletion once, at the operator's request: the absence tombstones are computed
     * against the scope's current entities now, the snapshot is discarded and the source's
     * snapshot status is cleared.
     *
     * @return the tombstones to ingest
     * @throws ai.foresignal.api.error.NotFoundException when the source has no held snapshot of that id
     */
    public List<NormalizedRecord> releaseHeld(String sourceId, String snapshotId, EdpManifestModel manifest,
            Instant now) {
        List<NormalizedRecord> tombstones = tx.execute(s -> {
            EdpSnapshotEntity snapshot = snapshots.findById(EdpSnapshotEntity.key(sourceId, snapshotId))
                    .filter(EdpSnapshotEntity::isHeld)
                    .orElseThrow(() -> ai.foresignal.api.error.NotFoundException.of("Held snapshot", snapshotId));
            EdpManifestModel.Stream stream = manifest.stream(snapshot.getStream());
            List<NormalizedRecord> out = new ArrayList<>();
            if (stream != null) {
                Set<String> staged = new HashSet<>(jdbc.queryForList(
                        "select external_id from edp_snapshot_stage where snapshot_key = ?", String.class,
                        snapshot.getKey()));
                for (String id : currentIds(sourceId, stream, snapshot.getScope())) {
                    if (!staged.contains(id)) {
                        out.add(NormalizedRecord.builder(id, stream.kind()).observedAt(now).tombstone(true)
                                .put("reason", ABSENT_FROM_SNAPSHOT).put("snapshotId", snapshotId).build());
                    }
                }
            }
            discard(snapshot);
            return out;
        });
        status.clear(sourceId, EdpStats.SNAPSHOT_KEY);
        return tombstones == null ? List.of() : tombstones;
    }

    /** The external ids of the scope's current (not tombstoned) entities. */
    List<String> currentIds(String sourceId, EdpManifestModel.Stream stream, String scope) {
        String prefix = stream.id() + ":";
        if (stream.snapshotScope() == null || scope == null) {
            return jdbc.queryForList("select external_id from record_head where source_id = ? and kind = ?"
                    + " and not tombstone and starts_with(external_id, ?)", String.class, sourceId, stream.kind(), prefix);
        }
        return jdbc.queryForList("select external_id from record_head where source_id = ? and kind = ?"
                + " and not tombstone and starts_with(external_id, ?) and payload ->> ? = ?", String.class, sourceId,
                stream.kind(), prefix, stream.snapshotScope(), scope);
    }

    private void discard(EdpSnapshotEntity snapshot) {
        jdbc.update("delete from edp_snapshot_stage where snapshot_key = ?", snapshot.getKey());
        snapshots.deleteById(snapshot.getKey());
    }

    private static boolean scopeMatches(JsonNode item, String property, String scope) {
        JsonNode value = item.path("properties").get(property);
        String text = value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
        return java.util.Objects.equals(text, scope);
    }
}
