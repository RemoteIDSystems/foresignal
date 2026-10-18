package ai.foresignal.media.outlet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * One rating of an outlet by a declared provider (prompt 133, table {@code media_rating}): the normalized value, the
 * provider's raw value and label, the provider's rating date, license, attribution, link and egress, and when it was
 * imported. One row per outlet, provider and dimension; a later import replaces it. Never written by a model.
 */
@Entity
@Table(name = "media_rating", uniqueConstraints = @UniqueConstraint(name = "media_rating_outlet_provider_dim_key",
        columnNames = {"outlet_id", "provider", "dimension"}),
        indexes = @Index(name = "media_rating_provider_idx", columnList = "provider"))
public class MediaRatingEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "outlet_id", length = 26, nullable = false)
    private String outletId;

    @Column(name = "provider", length = 32, nullable = false)
    private String provider;

    @Column(name = "dimension", length = 80, nullable = false)
    private String dimension;

    @Column(name = "value_norm")
    private Double valueNorm;

    @Column(name = "value_raw", length = 500)
    private String valueRaw;

    @Column(name = "label", length = 300)
    private String label;

    @Column(name = "rated_at")
    private Instant ratedAt;

    @Column(name = "licence", length = 300, nullable = false)
    private String license;

    @Column(name = "attribution", length = 500, nullable = false)
    private String attribution;

    @Column(name = "url", length = 2000)
    private String url;

    @Column(name = "egress", length = 16, nullable = false)
    private String egress;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    /** For JPA. */
    protected MediaRatingEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
