package ai.foresignal.notify.action;

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
 * One single-use action capability ({@code action_capability}, prompt 033). Declared for Hibernate to
 * own the table; {@link ActionService} reads and writes it with native SQL so it can lock a row with
 * {@code SELECT … FOR UPDATE}.
 *
 * <p>Only the SHA-256 of the token is stored ({@code token_hash}, unique). {@code params} holds
 * {@code {duration}} for snooze and mute. {@code channel_id} and {@code delivery_id} name the delivery
 * the capability was rendered into; {@code device_id} the device key it was minted for by
 * {@code GET /v1/notifications/{id}} instead. {@code used_at}, {@code used_via} and {@code result}
 * record the one execution, or {@code superseded} when the alert was acknowledged or resolved by any
 * other path first.
 */
@Entity
@Table(name = "action_capability", indexes = {
        @Index(name = "action_capability_token_idx", columnList = "token_hash", unique = true),
        @Index(name = "action_capability_alert_idx", columnList = "alert_id"),
        @Index(name = "action_capability_expires_idx", columnList = "expires_at")})
public class ActionCapabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "alert_id", nullable = false)
    private long alertId;

    @Column(name = "action", nullable = false, length = 16)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'{}'::jsonb")
    @Column(name = "params", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> params;

    @Column(name = "channel_id")
    private UUID channelId;

    @Column(name = "delivery_id")
    private Long deliveryId;

    @Column(name = "device_id", length = 160)
    private String deviceId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "used_via", length = 200)
    private String usedVia;

    @Column(name = "result", length = 64)
    private String result;

    /** For JPA. */
    protected ActionCapabilityEntity() {}

    /** The id. */
    public Long getId() {
        return id;
    }

    /** The SHA-256 of the token, hex. */
    public String getTokenHash() {
        return tokenHash;
    }

    /** The alert. */
    public long getAlertId() {
        return alertId;
    }

    /** {@code ack}, {@code snooze} or {@code mute}. */
    public String getAction() {
        return action;
    }

    /** The parameters. */
    public Map<String, Object> getParams() {
        return params;
    }

    /** The channel, or null. */
    public UUID getChannelId() {
        return channelId;
    }

    /** The delivery, or null. */
    public Long getDeliveryId() {
        return deliveryId;
    }

    /** The device key, or null. */
    public String getDeviceId() {
        return deviceId;
    }

    /** When minted. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it expires. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** When used, or null. */
    public Instant getUsedAt() {
        return usedAt;
    }

    /** How it was used, or null. */
    public String getUsedVia() {
        return usedVia;
    }

    /** The result, or null. */
    public String getResult() {
        return result;
    }
}
