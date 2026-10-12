package ai.foresignal.reference;

import ai.foresignal.store.RecordHeadId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * The region tags of one record (table {@code record_region}, prompt 040), keyed like
 * {@code record_head}. Written only by {@link RegionTagStore} (the {@link RegionTagWorker} and lazy
 * tagging), never by the ingest writer, and part of no hash (Core-Data-Model §1 I1, §4). The indexes
 * {@code (iso2)} and {@code (iso2, admin1)} are created by {@code SpatialIndexInitializer}.
 * {@code tagged_lon}/{@code tagged_lat} is the representative point the tags were computed for; a mover
 * is tagged again once it is more than the retag distance from it.
 */
@Entity
@Table(name = "record_region")
public class RecordRegionEntity {

    @EmbeddedId
    private RecordHeadId id;

    @Column(name = "iso2", length = 2)
    private String iso2;

    @Column(name = "admin1", length = 32)
    private String admin1;

    @Column(name = "admin2", length = 5)
    private String admin2;

    @Column(name = "sea", length = 200)
    private String sea;

    @Column(name = "near_id")
    private Long nearId;

    @Column(name = "near_name", length = 200)
    private String nearName;

    @Column(name = "near_km")
    private Double nearKm;

    @Column(name = "near_bearing")
    private Double nearBearing;

    @Column(name = "tagged_lon", nullable = false)
    private double taggedLon;

    @Column(name = "tagged_lat", nullable = false)
    private double taggedLat;

    @Column(name = "tagged_at", nullable = false)
    private Instant taggedAt;

    /** For JPA. */
    protected RecordRegionEntity() {}

    /** The record key. */
    public RecordHeadId getId() {
        return id;
    }

    /** The country, or null. */
    public String getIso2() {
        return iso2;
    }

    /** The first-level region, or null. */
    public String getAdmin1() {
        return admin1;
    }

    /** The US county, or null. */
    public String getAdmin2() {
        return admin2;
    }

    /** The sea area, or null. */
    public String getSea() {
        return sea;
    }

    /** The nearest place's GeoNames id, or null. */
    public Long getNearId() {
        return nearId;
    }

    /** The nearest place's name, or null. */
    public String getNearName() {
        return nearName;
    }

    /** The distance to the nearest place in km, or null. */
    public Double getNearKm() {
        return nearKm;
    }

    /** The bearing from the nearest place, or null. */
    public Double getNearBearing() {
        return nearBearing;
    }

    /** The tagged longitude. */
    public double getTaggedLon() {
        return taggedLon;
    }

    /** The tagged latitude. */
    public double getTaggedLat() {
        return taggedLat;
    }

    /** When it was tagged. */
    public Instant getTaggedAt() {
        return taggedAt;
    }
}
