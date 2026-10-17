package ai.foresignal.federation;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Deduplication of federated records across paths (prompt 113, table {@code federation_origin}).
 *
 * <p>On arrival the ledger is upserted only when the item's origin seq is greater than the stored one; otherwise the
 * item is a duplicate (equal seq: the same version again, so a re-sent batch is idempotent) or stale (lower seq:
 * {@value FederationRules#DROP_DEDUP}). Origin seqs are monotonic per origin instance, so this orders updates that
 * arrive over different paths.
 *
 * <p><strong>Ownership:</strong> the first peer source to claim an origin ref owns it, and every later update of
 * that origin is written into the owner's source under the same local external id, so the same origin over two
 * peers yields one local record. Ownership moves to the arriving source at the next arrival once the owner is no
 * longer alive (its peer was disabled or deleted).
 *
 * <p><strong>Failed writes:</strong> the claim is atomic, so two paths cannot both apply the same origin seq, but the
 * write that follows happens on the ingest writer's thread. A caller whose write fails hands the claim's
 * {@link Release} back to {@link #release(Release)}, which restores the previous entry when nothing newer was claimed
 * since; without it a re-send of the same version would count as a duplicate and the version would be lost
 * (checkpoint 124).
 */
@Component
public class OriginLedger {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the ledger. */
    public OriginLedger(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** What an arrival may do. */
    public enum Verdict {
        /** Apply it (into {@link Claim#ownerSourceId()}). */
        APPLY,
        /** The same origin seq was already applied. */
        DUPLICATE,
        /** A newer origin seq was already applied. */
        STALE
    }

    /**
     * The outcome of a claim.
     *
     * @param verdict apply, duplicate or stale
     * @param ownerSourceId the source to write into when applied (the arriving source, or the live owner)
     * @param localExternalId the local external id of the record
     */
    public record Claim(Verdict verdict, String ownerSourceId, String localExternalId, Release release) {}

    /**
     * What undoes an applied claim when its write fails.
     *
     * @param originRef the origin ref
     * @param claimedSeq the origin seq this claim stored
     * @param previousSeq the origin seq stored before, or null when the claim created the entry
     * @param previousOwner the owner stored before, or null when the claim created the entry
     */
    public record Release(String originRef, long claimedSeq, Long previousSeq, String previousOwner) {}

    /**
     * Claims {@code originRef} at {@code originSeq} for {@code arrivingSourceId}.
     *
     * @param aliveSources the peer sources currently enabled; an owner outside this set loses ownership
     */
    public Claim claim(String originRef, String arrivingSourceId, String localExternalId, long originSeq,
            Collection<String> aliveSources) {
        String[] alive = aliveSources.toArray(String[]::new);
        Timestamp now = Timestamp.from(clock.instant());
        List<Claim> applied = jdbc.query("""
                WITH prev AS (
                    SELECT origin_seq, owner_source_id FROM federation_origin WHERE origin_ref = ?
                )
                INSERT INTO federation_origin (origin_ref, owner_source_id, local_external_id, origin_seq, updated_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (origin_ref) DO UPDATE SET
                    origin_seq = EXCLUDED.origin_seq,
                    updated_at = EXCLUDED.updated_at,
                    owner_source_id = CASE WHEN federation_origin.owner_source_id = ANY(?)
                        THEN federation_origin.owner_source_id ELSE EXCLUDED.owner_source_id END
                WHERE federation_origin.origin_seq < EXCLUDED.origin_seq
                RETURNING owner_source_id, local_external_id, (SELECT origin_seq FROM prev),
                    (SELECT owner_source_id FROM prev)
                """, ps -> {
                    ps.setString(1, originRef);
                    ps.setString(2, originRef);
                    ps.setString(3, arrivingSourceId);
                    ps.setString(4, localExternalId);
                    ps.setLong(5, originSeq);
                    ps.setTimestamp(6, now);
                    ps.setArray(7, ps.getConnection().createArrayOf("text", alive));
                }, (rs, n) -> {
                    long previous = rs.getLong(3);
                    Long previousSeq = rs.wasNull() ? null : previous;
                    return new Claim(Verdict.APPLY, rs.getString(1), rs.getString(2),
                            new Release(originRef, originSeq, previousSeq, rs.getString(4)));
                });
        if (!applied.isEmpty()) {
            return applied.getFirst();
        }
        List<Long> stored = jdbc.queryForList("SELECT origin_seq FROM federation_origin WHERE origin_ref = ?",
                Long.class, originRef);
        boolean same = !stored.isEmpty() && stored.getFirst() == originSeq;
        return new Claim(same ? Verdict.DUPLICATE : Verdict.STALE, null, localExternalId, null);
    }

    /**
     * Undoes an applied claim whose write failed: restores the previous seq and owner (or removes an entry the
     * claim created), but only while the entry still holds the claimed seq, so a newer arrival is never undone.
     *
     * @param release the claim's release, or null (nothing to undo)
     */
    public void release(Release release) {
        if (release == null) {
            return;
        }
        if (release.previousSeq() == null) {
            jdbc.update("DELETE FROM federation_origin WHERE origin_ref = ? AND origin_seq = ?",
                    release.originRef(), release.claimedSeq());
        } else {
            jdbc.update("UPDATE federation_origin SET origin_seq = ?, owner_source_id = ?, updated_at = ?"
                    + " WHERE origin_ref = ? AND origin_seq = ?", release.previousSeq(), release.previousOwner(),
                    Timestamp.from(clock.instant()), release.originRef(), release.claimedSeq());
        }
    }

    /** Undoes every applied claim of {@code releases} (see {@link #release(Release)}). */
    public void releaseAll(Collection<Release> releases) {
        for (Release release : releases) {
            release(release);
        }
    }

    /** The stored origin seq of {@code originRef}, or -1 when none. */
    public long storedSeq(String originRef) {
        List<Long> stored = jdbc.queryForList("SELECT origin_seq FROM federation_origin WHERE origin_ref = ?",
                Long.class, originRef);
        return stored.isEmpty() ? -1L : stored.getFirst();
    }

    /** The owner of {@code originRef}, or null when none. */
    public String owner(String originRef) {
        List<String> owners = jdbc.queryForList("SELECT owner_source_id FROM federation_origin WHERE origin_ref = ?",
                String.class, originRef);
        return owners.isEmpty() ? null : owners.getFirst();
    }
}
