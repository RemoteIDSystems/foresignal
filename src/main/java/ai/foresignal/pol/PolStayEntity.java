package ai.foresignal.pol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

/**
 * A stay of a pattern-of-life subject (prompt 114, table {@code pol_stay}): consecutive fixes within
 * {@code stayRadiusM} spanning at least {@code minDwell}. Stays outlive the TRACK window; {@code pol.retention}
 * (default 180 d) deletes them. {@code cluster_id} is null for an unclustered (noise) stay.
 */
@Entity
@Table(name = "pol_stay", indexes = {@Index(name = "pol_stay_subject_arrive_idx", columnList = "subject_id, arrive"), @Index(name = "pol_stay_record_idx", columnList = "record_ref")})
public class PolStayEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "subject_id", length = 26, nullable = false)
    private String subjectId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "arrive", nullable = false)
    private Instant arrive;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "depart", nullable = false)
    private Instant depart;

    @Column(name = "centroid", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point centroid;

    @Column(name = "place_id", length = 26)
    private String placeId;

    @Column(name = "cluster_id")
    private Integer clusterId;

    @Column(name = "fixes", nullable = false)
    private int fixes;

    @Column(name = "record_ref", length = 330)
    private String recordRef;

    /** For JPA. */
    protected PolStayEntity() {
        // for JPA
    }

    /** Creates a row with its id. */
    public PolStayEntity(String id) {
        this.id = id;
    }

    /** The ULID id. */
    public String getId() {
        return id;
    }

    /** The subject. */
    public String getSubjectId() {
        return subjectId;
    }

    /** Sets: the subject. */
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    /** The first fix of the stay. */
    public Instant getArrive() {
        return arrive;
    }

    /** Sets: the first fix of the stay. */
    public void setArrive(Instant arrive) {
        this.arrive = arrive;
    }

    /** The last fix of the stay. */
    public Instant getDepart() {
        return depart;
    }

    /** Sets: the last fix of the stay. */
    public void setDepart(Instant depart) {
        this.depart = depart;
    }

    /** The mean position of the stay's fixes. */
    public Point getCentroid() {
        return centroid;
    }

    /** Sets: the mean position of the stay's fixes. */
    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    /** The learned place, or null. */
    public String getPlaceId() {
        return placeId;
    }

    /** Sets: the learned place, or null. */
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    /** The DBSCAN cluster, or null for noise. */
    public Integer getClusterId() {
        return clusterId;
    }

    /** Sets: the DBSCAN cluster, or null for noise. */
    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    /** The number of fixes. */
    public int getFixes() {
        return fixes;
    }

    /** Sets: the number of fixes. */
    public void setFixes(int fixes) {
        this.fixes = fixes;
    }

    /** The record {@code {sourceId}/{externalId}} whose fixes formed the stay (for the 039 purge). */
    public String getRecordRef() {
        return recordRef;
    }

    /** Sets: the record {@code {sourceId}/{externalId}} whose fixes formed the stay (for the 039 purge). */
    public void setRecordRef(String recordRef) {
        this.recordRef = recordRef;
    }

}
