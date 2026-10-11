package ai.foresignal.store;

import ai.foresignal.source.spi.HistoryTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * One persisted version of one upstream record: a read-only mapping of the partitioned
 * {@code record_version} table (Core-Data-Model §8, §9).
 *
 * <p>{@code seq} is the spine of the whole system: the single writer allocates it, so commit order
 * equals {@code seq} order, which makes it a reliable cursor for SSE resumption, trigger catch-up
 * and {@code /v1/changes}. It is global across history tiers.
 *
 * <p><strong>The DDL is not Hibernate's.</strong> {@code HistorySchemaInitializer} creates the
 * table, partitioned by {@code LIST (history_tier)} and then {@code RANGE (seq)}, before the
 * {@code EntityManagerFactory} exists, and Hibernate sees it as an existing table through
 * {@code hibernate.hbm2ddl.extra_physical_table_types=PARTITIONED TABLE}. The database primary key
 * is {@code (seq, history_tier)}, because a partitioned table's key must include the partition
 * key; mapping only {@code seq} as the {@code @Id} is intentional and correct, since {@code seq} is
 * unique on its own. For the same reason this class declares no unique constraint and no index:
 * the partitioned indexes are created by the initializer. Field order follows the column order of
 * that DDL, and {@code SchemaParityIT} checks the two agree.
 *
 * <p>The entity is {@link Immutable}: Hibernate reads these rows but never writes them (invariant
 * I9). The ingest writer inserts them with native JDBC batches.
 */
@Entity
@Immutable
@Table(name = "record_version")
@SequenceGenerator(name = "record_seq", sequenceName = "record_seq", allocationSize = 1)
public class RecordVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "record_seq")
    @Column(name = "seq", nullable = false)
    private Long seq;

    /** The writer's batch timestamp; never decreases with {@code seq} (invariant I3). */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    /** When upstream last observed or updated this state (Core-Data-Model §2). */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    /** When Foresignal received the response, message or push carrying this observation. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    /** When a discrete real-world event happened, or null for pure state. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "event_time")
    private Instant eventTime;

    /** The effective expiry: explicit, or derived from the TTL and its anchor. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_until")
    private Instant validUntil;

    /** The expiry the upstream stated, if it stated one. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_until_explicit")
    private Instant validUntilExplicit;

    /** The {@code seq} of this entity's previous persisted version, or null for the first. */
    @Column(name = "prev_seq")
    private Long prevSeq;

    /** The {@code fetch_log.id} of the batch that wrote this version. Deliberately not a foreign key. */
    @Column(name = "batch_id")
    private Long batchId;

    /** 1-based, per {@code (source_id, external_id)}. */
    @Column(name = "version", nullable = false)
    private int version;

    /** {@link HistoryTier#code()}; also the list-partition key. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "history_tier", nullable = false)
    private Short historyTier;

    /** Why the writer persisted this version; the enum arrives with the policy engine (prompt 008). */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "persist_reason", nullable = false)
    private Short persistReason;

    /** Quality-assurance flag bits set by the normalizer. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "qa_flags", nullable = false)
    private Short qaFlags;

    /** True when the upstream says this record is gone. */
    @Column(name = "tombstone", nullable = false)
    private boolean tombstone;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "kind", length = 64, nullable = false)
    private String kind;

    /** Always SRID 4326. */
    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quality")
    private Map<String, Object> quality;

    /** The contributing receivers, at most 8 (Core-Data-Model §14), as a JSON array. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "observer")
    private List<Map<String, Object>> observer;

    /** When the state starts to apply, if the upstream says (Gap-Analysis §8.A item 2). */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_from")
    private Instant validFrom;

    protected RecordVersionEntity() {
        // for JPA
    }

    /** The global sequence number of this version. */
    public Long getSeq() {
        return seq;
    }

    /** The writer's batch timestamp. */
    public Instant getIngestedAt() {
        return ingestedAt;
    }

    /** When upstream last observed this state. */
    public Instant getObservedAt() {
        return observedAt;
    }

    /** When Foresignal received it. */
    public Instant getReceivedAt() {
        return receivedAt;
    }

    /** When the real-world event happened, or null. */
    public Instant getEventTime() {
        return eventTime;
    }

    /** The effective expiry, or null. */
    public Instant getValidUntil() {
        return validUntil;
    }

    /** The upstream-stated expiry, or null. */
    public Instant getValidUntilExplicit() {
        return validUntilExplicit;
    }

    /** The previous version's seq, or null. */
    public Long getPrevSeq() {
        return prevSeq;
    }

    /** The {@code fetch_log.id} of the writing batch, or null. */
    public Long getBatchId() {
        return batchId;
    }

    /** The 1-based version number within the entity. */
    public int getVersion() {
        return version;
    }

    /** The stored tier code. */
    public Short getHistoryTierCode() {
        return historyTier;
    }

    /** The tier, decoded from its stored code. */
    public HistoryTier getHistoryTier() {
        return HistoryTier.fromCode(historyTier);
    }

    /** The stored persist-reason code. */
    public Short getPersistReason() {
        return persistReason;
    }

    /** The QA flag bits. */
    public Short getQaFlags() {
        return qaFlags;
    }

    /** Whether this version marks the record as gone. */
    public boolean isTombstone() {
        return tombstone;
    }

    /** The owning source's id. */
    public String getSourceId() {
        return sourceId;
    }

    /** The source's own id for the record. */
    public String getExternalId() {
        return externalId;
    }

    /** The record kind. */
    public String getKind() {
        return kind;
    }

    /** The geometry, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** The payload as stored. */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /** The quality object, or null. */
    public Map<String, Object> getQuality() {
        return quality;
    }

    /** The contributing receivers, or null. */
    public List<Map<String, Object>> getObserver() {
        return observer;
    }

    /** When the state starts to apply, or null. */
    public Instant getValidFrom() {
        return validFrom;
    }
}
