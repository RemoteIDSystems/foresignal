package ai.foresignal.chatops;

import ai.foresignal.source.HealthStatus;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The live state of one interactive channel's connector (prompt 061), read by
 * {@code getChannelInteractiveStatus} and {@code Channel.interactive}. Thread-safe: the poll thread writes,
 * API threads read.
 */
public final class ConnectorState {

    private volatile boolean connected;
    private volatile Instant lastPollAt;
    private volatile String lastError;
    private volatile HealthStatus status = HealthStatus.IDLE;
    private volatile String statusReason;
    private final Map<String, String> inactiveChats = new ConcurrentHashMap<>();

    /** True while the last poll succeeded. */
    public boolean connected() {
        return connected;
    }

    /** When the platform last answered a poll, or null. */
    public Instant lastPollAt() {
        return lastPollAt;
    }

    /** The last error, or null. */
    public String lastError() {
        return lastError;
    }

    /** The health. */
    public HealthStatus status() {
        return status;
    }

    /** Why the health is not {@code healthy}, or null. */
    public String statusReason() {
        return statusReason;
    }

    /** The chats that are ignored, with the reason (an encrypted Matrix room). */
    public Map<String, String> inactiveChats() {
        return Map.copyOf(new LinkedHashMap<>(inactiveChats));
    }

    /** Records a successful poll at {@code at}; healthy unless a chat is ignored. */
    public void polled(Instant at) {
        connected = true;
        lastPollAt = at;
        if (status != HealthStatus.FAILING) {
            if (inactiveChats.isEmpty()) {
                status = HealthStatus.HEALTHY;
                statusReason = null;
            } else {
                status = HealthStatus.DEGRADED;
                statusReason = String.join("; ", new java.util.TreeSet<>(inactiveChats.values()));
            }
        }
    }

    /** Records a failed poll; the health is {@code degraded} until the next success. */
    public void failed(String error) {
        connected = false;
        lastError = error;
        if (status != HealthStatus.FAILING) {
            status = HealthStatus.DEGRADED;
            statusReason = error;
        }
    }

    /** The connector needs the operator: {@code degraded} with {@code reason}. */
    public void blocked(String reason) {
        connected = false;
        lastError = reason;
        status = HealthStatus.DEGRADED;
        statusReason = reason;
    }

    /** The credentials were refused: {@code failing}, and the connector stopped. */
    public void authFailed(String reason) {
        connected = false;
        lastError = reason;
        status = HealthStatus.FAILING;
        statusReason = reason;
    }

    /** The connector stopped normally (disabled, deleted or the kill switch). */
    public void stopped() {
        connected = false;
        status = HealthStatus.IDLE;
        statusReason = null;
    }

    /** Marks {@code chatId} as ignored for {@code reason}. */
    public void deactivate(String chatId, String reason) {
        inactiveChats.put(chatId, reason);
    }

    /** Marks {@code chatId} as served again. */
    public void activate(String chatId) {
        inactiveChats.remove(chatId);
    }

    /** True when {@code chatId} is served. */
    public boolean active(String chatId) {
        return !inactiveChats.containsKey(chatId);
    }
}
