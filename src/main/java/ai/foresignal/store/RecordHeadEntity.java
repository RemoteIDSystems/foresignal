package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * The persisted state of one upstream record (Core-Data-Model §8).
 *
 * <p>The head holds the full state of the version the writer last <em>persisted</em>: its payload,
 * quality, geometry ({@code persisted_geom}) and static hash. It is written only on PERSIST, on a
 * hash-recipe migration and on a purge, so it stays cold. The hot, frequently touched state
 * (latest observation time, live geometry, full hash, kinematics) lives in
 * {@link RecordLiveEntity}, one row per head (invariant I10). Current-state reads join the two
 * (invariant I5).
 *
 * <p>Indexed only by its primary key and {@code (kind)}. {@code persisted_geom} deliberately has
 * no spatial index: spatial queries go through {@code record_live.geom}.
 */
@Entity
@Table(name = "record_head", indexes = @Index(name = "record_head_kind_idx", columnList = "kind"))
public class RecordHeadEntity {

    @EmbeddedId
    private RecordHeadId id;

    @Column(name = "kind", length = 64, nullable = false)
    private String kind;

    /** The {@code record_version.seq} this head reflects. */
    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "version", nullable = false)
    private int version;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "event_time")
    private Instant eventTime;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_until_explicit")
    private Instant validUntilExplicit;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "tombstone", nullable = false)
    private boolean tombstone;

    /** Hash of the significant content of the persisted version (Core-Data-Model §4). */
    @Column(name = "static_hash", nullable = false)
    private long staticHash;

    /** The hash recipe version {@link #staticHash} was computed with. */
    @Column(name = "hash_recipe", nullable = false)
    private int hashRecipe;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "persisted_observed_at", nullable = false)
    private Instant persistedObservedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "persisted_received_at", nullable = false)
    private Instant persistedReceivedAt;

    /** The persisted version's geometry, SRID 4326. Not indexed, by design. */
    @Column(name = "persisted_geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry persistedGeom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quality")
    private Map<String, Object> quality;

    /** Flattened text from the payload, so queries can filter on words without reading jsonb. */
    @Column(name = "search_text", columnDefinition = "text")
    private String searchText;

    /** The display title, filled by prompt 008's title resolver. */
    @Column(name = "title", length = 256)
    private String title;

    protected RecordHeadEntity() {
        // for JPA
    }

    /** Creates a head for the given record; the caller fills every non-null column. */
    public RecordHeadEntity(RecordHeadId id) {
        this.id = id;
    }

    /** The record's identity. */
    public RecordHeadId getId() {
        return id;
    }

    /** Sets the record's identity. */
    public void setId(RecordHeadId id) {
        this.id = id;
    }

    /** The record kind. */
    public String getKind() {
        return kind;
    }

    /** Sets the record kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** The seq of the persisted version. */
    public long getSeq() {
        return seq;
    }

    /** Sets the seq of the persisted version. */
    public void setSeq(long seq) {
        this.seq = seq;
    }

    /** The persisted version number. */
    public int getVersion() {
        return version;
    }

    /** Sets the persisted version number. */
    public void setVersion(int version) {
        this.version = version;
    }

    /** When the real-world event happened, or null. */
    public Instant getEventTime() {
        return eventTime;
    }

    /** Sets the event time. */
    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    /** The upstream-stated expiry, or null. */
    public Instant getValidUntilExplicit() {
        return validUntilExplicit;
    }

    /** Sets the upstream-stated expiry. */
    public void setValidUntilExplicit(Instant validUntilExplicit) {
        this.validUntilExplicit = validUntilExplicit;
    }

    /** When the state starts to apply, or null. */
    public Instant getValidFrom() {
        return validFrom;
    }

    /** Sets when the state starts to apply. */
    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    /** Whether the record is gone upstream. */
    public boolean isTombstone() {
        return tombstone;
    }

    /** Sets the tombstone flag. */
    public void setTombstone(boolean tombstone) {
        this.tombstone = tombstone;
    }

    /** The static hash of the persisted version. */
    public long getStaticHash() {
        return staticHash;
    }

    /** Sets the static hash. */
    public void setStaticHash(long staticHash) {
        this.staticHash = staticHash;
    }

    /** The hash recipe version. */
    public int getHashRecipe() {
        return hashRecipe;
    }

    /** Sets the hash recipe version. */
    public void setHashRecipe(int hashRecipe) {
        this.hashRecipe = hashRecipe;
    }

    /** The persisted version's observed time. */
    public Instant getPersistedObservedAt() {
        return persistedObservedAt;
    }

    /** Sets the persisted version's observed time. */
    public void setPersistedObservedAt(Instant persistedObservedAt) {
        this.persistedObservedAt = persistedObservedAt;
    }

    /** The persisted version's received time. */
    public Instant getPersistedReceivedAt() {
        return persistedReceivedAt;
    }

    /** Sets the persisted version's received time. */
    public void setPersistedReceivedAt(Instant persistedReceivedAt) {
        this.persistedReceivedAt = persistedReceivedAt;
    }

    /** The persisted geometry, or null. */
    public Geometry getPersistedGeom() {
        return persistedGeom;
    }

    /** Sets the persisted geometry. */
    public void setPersistedGeom(Geometry persistedGeom) {
        this.persistedGeom = persistedGeom;
    }

    /** The persisted payload. */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /** Sets the persisted payload. */
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    /** The persisted quality object, or null. */
    public Map<String, Object> getQuality() {
        return quality;
    }

    /** Sets the persisted quality object. */
    public void setQuality(Map<String, Object> quality) {
        this.quality = quality;
    }

    /** The flattened search text, or null. */
    public String getSearchText() {
        return searchText;
    }

    /** Sets the flattened search text. */
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    /** The display title, or null. */
    public String getTitle() {
        return title;
    }

    /** Sets the display title. */
    public void setTitle(String title) {
        this.title = title;
    }
}
