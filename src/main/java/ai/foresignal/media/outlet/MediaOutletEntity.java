package ai.foresignal.media.outlet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A news outlet (prompt 133, table {@code media_outlet}). Declares the schema for Hibernate; {@link OutletRegistry}
 * reads and writes the rows with SQL. Organization names only ({@code owner_org}, {@code parent_org}), never people.
 * An outlet merged into another keeps its row with {@code merged_into} set.
 */
@Entity
@Table(name = "media_outlet", uniqueConstraints = @UniqueConstraint(name = "media_outlet_primary_domain_key",
        columnNames = "primary_domain"), indexes = @Index(name = "media_outlet_country_idx", columnList = "country"))
public class MediaOutletEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "primary_domain", length = 253, nullable = false)
    private String primaryDomain;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "domains", columnDefinition = "text[]", nullable = false)
    private String[] domains = new String[0];

    @Column(name = "country", length = 2)
    private String country;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "languages", columnDefinition = "text[]")
    private String[] languages = new String[0];

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "owner_org", length = 300)
    private String ownerOrg;

    @Column(name = "parent_org", length = 300)
    private String parentOrg;

    @Column(name = "wikidata_qid", length = 20)
    private String wikidataQid;

    @Column(name = "originator_id", length = 120)
    private String originatorId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "aggregates", columnDefinition = "text[]")
    private String[] aggregates = new String[0];

    @Column(name = "rated", nullable = false)
    private boolean rated;

    @Column(name = "merged_into", length = 26)
    private String mergedInto;

    @Column(name = "wikidata_checked_at")
    private Instant wikidataCheckedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected MediaOutletEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }

    /** The display name. */
    public String getName() {
        return name;
    }
}
