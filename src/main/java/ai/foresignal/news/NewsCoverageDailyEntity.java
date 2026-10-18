package ai.foresignal.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One day of news coverage of a topic or situation in one bucket (prompt 133, table {@code news_coverage_daily}): the
 * evidence groups and articles first seen that UTC day, by lean bucket ({@code left}, {@code center}, {@code right},
 * {@code unrated}, plus {@code state} for state-affiliated outlets). The {@code rollup:news/{scope}/{bucket}} series
 * read these rows. {@code id} is {@code {scopeType}|{scopeId}|{day}|{bucket}}; kept like other analytic series.
 */
@Entity
@Table(name = "news_coverage_daily", indexes = @Index(name = "news_coverage_daily_scope_idx",
        columnList = "scope_id, bucket, day"))
public class NewsCoverageDailyEntity {

    @Id
    @Column(name = "id", length = 200, nullable = false)
    private String id;

    @Column(name = "scope_type", length = 16, nullable = false)
    private String scopeType;

    @Column(name = "scope_id", length = 64, nullable = false)
    private String scopeId;

    @Column(name = "day", nullable = false)
    private Instant day;

    @Column(name = "bucket", length = 16, nullable = false)
    private String bucket;

    @Column(name = "evidence_groups", nullable = false)
    private int evidenceGroups;

    @Column(name = "articles", nullable = false)
    private int articles;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    /** For JPA. */
    protected NewsCoverageDailyEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
