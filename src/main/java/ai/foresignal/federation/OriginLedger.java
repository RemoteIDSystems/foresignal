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
    public record Claim(Verdict verdict, String ownerSourceId, String localExternalId) {}

    /**
     * Claims {@code originRef} at {@code originSeq} for {@code arrivingSourceId}.
     *
     * @param aliveSources the peer sources currently enabled; an owner outside this set loses ownership
     */
    public Claim claim(String originRef, String arrivingSourceId, String localExternalId, long originSeq,
            Collection<String> aliveSources) {
        String[] alive = aliveSources.toArray(String[]::new);
        Timestamp now = Timestamp.from(clock.instant());
        List<String[]> applied = jdbc.query("""
                INSERT INTO federation_origin (origin_ref, owner_source_id, local_external_id, origin_seq, updated_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (origin_ref) DO UPDATE SET
                    origin_seq = EXCLUDED.origin_seq,
                    updated_at = EXCLUDED.updated_at,
                    owner_source_id = CASE WHEN federation_origin.owner_source_id = ANY(?)
                        THEN federation_origin.owner_source_id ELSE EXCLUDED.owner_source_id END
                WHERE federation_origin.origin_seq < EXCLUDED.origin_seq
                RETURNING owner_source_id, local_external_id
                """, ps -> {
                    ps.setString(1, originRef);
                    ps.setString(2, arrivingSourceId);
                    ps.setString(3, localExternalId);
                    ps.setLong(4, originSeq);
                    ps.setTimestamp(5, now);
                    ps.setArray(6, ps.getConnection().createArrayOf("text", alive));
                }, (rs, n) -> new String[] {rs.getString(1), rs.getString(2)});
        if (!applied.isEmpty()) {
            return new Claim(Verdict.APPLY, applied.getFirst()[0], applied.getFirst()[1]);
        }
        List<Long> stored = jdbc.queryForList("SELECT origin_seq FROM federation_origin WHERE origin_ref = ?",
                Long.class, originRef);
        boolean same = !stored.isEmpty() && stored.getFirst() == originSeq;
        return new Claim(same ? Verdict.DUPLICATE : Verdict.STALE, null, localExternalId);
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
