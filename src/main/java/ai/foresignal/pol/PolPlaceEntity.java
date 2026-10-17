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
 * A learned place of a pattern-of-life subject (prompt 114, table {@code pol_place}): a DBSCAN cluster of stays with
 * its p90 radius, visits, total dwell and the 168-bucket hour-of-week dwell histogram (Monday 00:00 operator time
 * first). Places outlive the TRACK window; the operator may label one (for example "home").
 */
@Entity
@Table(name = "pol_place", indexes = {@Index(name = "pol_place_subject_idx", columnList = "subject_id")})
public class PolPlaceEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "subject_id", length = 26, nullable = false)
    private String subjectId;

    @Column(name = "cluster_id", nullable = false)
    private int clusterId;

    @Column(name = "centroid", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point centroid;

    @Column(name = "radius_m", nullable = false)
    private double radiusM;

    @Column(name = "visits", nullable = false)
    private int visits;

    @Column(name = "total_dwell_minutes", nullable = false)
    private double totalDwellMinutes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "hour_of_week", columnDefinition = "integer[]")
    private int[] hourOfWeek = new int[168];

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    @Column(name = "label", length = 100)
    private String label;

    /** For JPA. */
    protected PolPlaceEntity() {
        // for JPA
    }

    /** Creates a row with its id. */
    public PolPlaceEntity(String id) {
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

    /** The DBSCAN cluster of the last recompute. */
    public int getClusterId() {
        return clusterId;
    }

    /** Sets: the DBSCAN cluster of the last recompute. */
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    /** The mean of the member stays' centroids. */
    public Point getCentroid() {
        return centroid;
    }

    /** Sets: the mean of the member stays' centroids. */
    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    /** The p90 distance of member stays from the centroid, meters. */
    public double getRadiusM() {
        return radiusM;
    }

    /** Sets: the p90 distance of member stays from the centroid, meters. */
    public void setRadiusM(double radiusM) {
        this.radiusM = radiusM;
    }

    /** The number of stays. */
    public int getVisits() {
        return visits;
    }

    /** Sets: the number of stays. */
    public void setVisits(int visits) {
        this.visits = visits;
    }

    /** The total dwell in minutes. */
    public double getTotalDwellMinutes() {
        return totalDwellMinutes;
    }

    /** Sets: the total dwell in minutes. */
    public void setTotalDwellMinutes(double totalDwellMinutes) {
        this.totalDwellMinutes = totalDwellMinutes;
    }

    /** Dwell minutes per hour of week (168 buckets). */
    public int[] getHourOfWeek() {
        return hourOfWeek;
    }

    /** Sets: dwell minutes per hour of week (168 buckets). */
    public void setHourOfWeek(int[] hourOfWeek) {
        this.hourOfWeek = hourOfWeek;
    }

    /** The first arrival. */
    public Instant getFirstSeen() {
        return firstSeen;
    }

    /** Sets: the first arrival. */
    public void setFirstSeen(Instant firstSeen) {
        this.firstSeen = firstSeen;
    }

    /** The last departure. */
    public Instant getLastSeen() {
        return lastSeen;
    }

    /** Sets: the last departure. */
    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    /** The operator label, or null. */
    public String getLabel() {
        return label;
    }

    /** Sets: the operator label, or null. */
    public void setLabel(String label) {
        this.label = label;
    }

}
