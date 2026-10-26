package ai.foresignal.security.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One audit log entry ({@code audit_event}, prompt 023).
 *
 * <p>Hibernate maps and validates the table (Flyway creates it); rows are inserted by {@link AuditService} with JDBC batches
 * from its bounded queue. {@code detail} never holds secrets, token values or request bodies. Prompt R2-013 adds the
 * user actor ({@code user_id}, {@code user_email}); the table refuses updates, so an entry is never rewritten.
 */
@Entity
@Table(name = "audit_event", indexes = {
    @Index(name = "audit_event_at_idx", columnList = "at DESC"),
    @Index(name = "audit_event_token_at_idx", columnList = "token_id, at DESC")
})
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "actor_kind", length = 16, nullable = false)
    private String actorKind;

    @Column(name = "token_id")
    private UUID tokenId;

    @Column(name = "token_name", length = 200)
    private String tokenName;

    @Column(name = "principal_id", length = 26)
    private String principalId;

    @Column(name = "scope", length = 16)
    private String scope;

    @Column(name = "method", length = 16)
    private String method;

    @Column(name = "path", length = 2048)
    private String path;

    @Column(name = "operation_id", length = 128)
    private String operationId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "target_type", length = 64)
    private String targetType;

    @Column(name = "target_id", length = 256)
    private String targetId;

    @Column(name = "remote_ip", length = 64)
    private String remoteIp;

    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'{}'::jsonb")
    @Column(name = "detail", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> detail;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email", length = 320)
    private String userEmail;

    protected AuditEventEntity() {
        // for JPA
    }

    /** The id. */
    public Long getId() {
        return id;
    }

    /** When it happened. */
    public Instant getAt() {
        return at;
    }

    /** Who acted. */
    public String getActorKind() {
        return actorKind;
    }

    /** The token, or null. */
    public UUID getTokenId() {
        return tokenId;
    }

    /** The detail object. */
    public Map<String, Object> getDetail() {
        return detail;
    }

    /** The signed-in user who acted, or null (prompt R2-013). */
    public UUID getUserId() {
        return userId;
    }

    /** That user's email when the entry was written, or null. */
    public String getUserEmail() {
        return userEmail;
    }
}
