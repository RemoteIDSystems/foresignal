package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One hour of social collection coverage of one platform in one 040 region (prompt 135, table
 * {@code social_coverage}): posts attributed to the region (platform geo, region names matched by tracked topics,
 * and from 136 text-derived places), the platform's denominator for the hour, and whether complete-stream collection
 * was healthy for the whole hour. {@code id} is {@code {platform}|{region}|{hour epoch seconds}}.
 */
@Entity
@Table(name = "social_coverage", indexes = @Index(name = "social_coverage_region_idx",
        columnList = "region_code, platform, hour"))
public class SocialCoverageEntity {

    @Id
    @Column(name = "id", length = 120, nullable = false)
    private String id;

    @Column(name = "platform", length = 32, nullable = false)
    private String platform;

    @Column(name = "region_code", length = 16, nullable = false)
    private String regionCode;

    @Column(name = "hour", nullable = false)
    private Instant hour;

    @Column(name = "posts_seen", nullable = false)
    private long postsSeen;

    @Column(name = "denominator")
    private Long denominator;

    @Column(name = "complete")
    private Boolean complete;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SocialCoverageEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
