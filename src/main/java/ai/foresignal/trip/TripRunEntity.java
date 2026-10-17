package ai.foresignal.trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One pre-trip check of a trip occurrence (prompt 114, table {@code trip_run}): the hazards found, ordered along the
 * route, the rendered briefing and the delivery status ({@code delivered}, {@code skipped}, {@code dry-run},
 * {@code failed}).
 */
@Entity
@Table(name = "trip_run", indexes = {@Index(name = "trip_run_trip_idx", columnList = "trip_id, created_at"), @Index(name = "trip_run_occurrence_idx", columnList = "trip_id, occurrence")})
public class TripRunEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "trip_id", length = 26, nullable = false)
    private String tripId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "occurrence", nullable = false)
    private Instant occurrence;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "clear", nullable = false)
    private boolean clear;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hazards")
    private List<Map<String, Object>> hazards = new ArrayList<>();

    @Column(name = "briefing", columnDefinition = "text")
    private String briefing;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected TripRunEntity() {
        // for JPA
    }

    /** Creates a row with its id. */
    public TripRunEntity(String id) {
        this.id = id;
    }

    /** The ULID id. */
    public String getId() {
        return id;
    }

    /** The trip. */
    public String getTripId() {
        return tripId;
    }

    /** Sets: the trip. */
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    /** The occurrence's departure time. */
    public Instant getOccurrence() {
        return occurrence;
    }

    /** Sets: the occurrence's departure time. */
    public void setOccurrence(Instant occurrence) {
        this.occurrence = occurrence;
    }

    /** {@code delivered}, {@code skipped}, {@code dry-run} or {@code failed}. */
    public String getStatus() {
        return status;
    }

    /** Sets: {@code delivered}, {@code skipped}, {@code dry-run} or {@code failed}. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** True when no hazard was found ("route clear"). */
    public boolean isClear() {
        return clear;
    }

    /** Sets: true when no hazard was found ("route clear"). */
    public void setClear(boolean clear) {
        this.clear = clear;
    }

    /** The {@code TripHazard} objects. */
    public List<Map<String, Object>> getHazards() {
        return hazards;
    }

    /** Sets: the {@code TripHazard} objects. */
    public void setHazards(List<Map<String, Object>> hazards) {
        this.hazards = hazards;
    }

    /** The markdown trip briefing. */
    public String getBriefing() {
        return briefing;
    }

    /** Sets: the markdown trip briefing. */
    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    /** When it was delivered, or null. */
    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    /** Sets: when it was delivered, or null. */
    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    /** When the check ran. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets: when the check ran. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
