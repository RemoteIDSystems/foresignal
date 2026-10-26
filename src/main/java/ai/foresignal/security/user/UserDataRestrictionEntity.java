package ai.foresignal.security.user;

import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * A user's data restriction ({@code user_data_restriction}, prompt R2-013): the data members of an API token's
 * {@code ReadRestriction} (sources, kinds, bbox, area, places), stored with the same column types as
 * {@code api_token}. A null member means "any". The row is deleted by the database with the user.
 */
@Entity
@Table(name = "user_data_restriction")
public class UserDataRestrictionEntity {

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_sources", columnDefinition = "text[]")
    private String[] allowedSources;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_kinds", columnDefinition = "text[]")
    private String[] allowedKinds;

    @Column(name = "allowed_bbox", columnDefinition = "geometry(Polygon,4326)")
    private Polygon allowedBbox;

    @Column(name = "allowed_area", columnDefinition = "geometry(Geometry,4326)")
    private Geometry allowedArea;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_place_ids", columnDefinition = "text[]")
    private String[] allowedPlaceIds;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected UserDataRestrictionEntity() {}

    /**
     * A restriction row for {@code userId}.
     *
     * @param userId the user
     * @param now when it is written
     */
    public UserDataRestrictionEntity(UUID userId, Instant now) {
        this.userId = userId;
        this.updatedAt = now;
    }

    /** The user. */
    public UUID getUserId() {
        return userId;
    }

    /** The restriction, as the data members of a {@link TokenRestrictions}. */
    public TokenRestrictions getRestrictions() {
        List<Double> bbox = null;
        if (allowedBbox != null) {
            Envelope e = allowedBbox.getEnvelopeInternal();
            bbox = List.of(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
        }
        java.util.Map<String, Object> area = allowedArea == null ? null
                : new ai.foresignal.source.formats.geojson.GeoJsonWriter().write(allowedArea);
        return new TokenRestrictions(list(allowedSources), list(allowedKinds), bbox, null, area, null, null,
                list(allowedPlaceIds), null, null);
    }

    /**
     * Stores the data members of {@code restrictions}; anything else in it is ignored.
     *
     * @param restrictions the restriction
     * @param now when it is written
     */
    public void setRestrictions(TokenRestrictions restrictions, Instant now) {
        TokenRestrictions r = TokenRestrictions.orNone(restrictions);
        this.allowedSources = array(r.sources());
        this.allowedKinds = array(r.kinds());
        this.allowedBbox = r.bbox() == null ? null : polygon(r.bbox());
        this.allowedArea = ReadRestriction.area(r.area());
        this.allowedPlaceIds = array(r.placeIds());
        this.updatedAt = now;
    }

    /** When it was last written. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static Polygon polygon(List<Double> bbox) {
        double minLon = bbox.get(0);
        double minLat = bbox.get(1);
        double maxLon = bbox.get(2);
        double maxLat = bbox.get(3);
        return WGS84.createPolygon(new Coordinate[] {
            new Coordinate(minLon, minLat), new Coordinate(maxLon, minLat), new Coordinate(maxLon, maxLat),
            new Coordinate(minLon, maxLat), new Coordinate(minLon, minLat)
        });
    }

    private static List<String> list(String[] values) {
        return values == null ? null : Arrays.asList(values);
    }

    private static String[] array(List<String> values) {
        return values == null ? null : values.toArray(String[]::new);
    }
}
