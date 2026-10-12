package ai.foresignal.inbound.edp;

import ai.foresignal.ingest.IngestStateReset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and updates {@code external_source.stats} (prompt 024), with an in-memory copy of the
 * provider-reported status so the health rule stays cheap.
 *
 * <p>Status items set a provider's health without creating records; they are kept per stream and
 * for the source as a whole, and the most recent unhealthy report time is what the snapshot rules
 * consult (an unhealthy stream never completes a snapshot, §3.2).
 */
@Component
public class EdpStatusStore implements IngestStateReset {

    private final ExternalSourceRepository repository;
    private final JsonMapper json;
    private final TransactionTemplate tx;
    private final Map<String, Map<String, EdpStats.StatusReport>> status = new ConcurrentHashMap<>();

    /** Creates the store. */
    public EdpStatusStore(ExternalSourceRepository repository, JsonMapper json, PlatformTransactionManager transactions) {
        this.repository = repository;
        this.json = json;
        this.tx = new TransactionTemplate(transactions);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void resetState() {
        status.clear();
    }

    /** The stats of {@code row}. */
    public EdpStats read(ExternalSourceEntity row) {
        if (row.getStats() == null || row.getStats().isBlank()) {
            return EdpStats.empty();
        }
        return json.readValue(row.getStats(), EdpStats.class);
    }

    /** Applies {@code change} to the stored stats of {@code sourceId} in its own transaction. */
    public void update(String sourceId, UnaryOperator<EdpStats> change) {
        tx.executeWithoutResult(status -> repository.findById(sourceId).ifPresent(row -> {
            EdpStats updated = change.apply(read(row));
            row.setStats(json.writeValueAsString(updated));
            repository.save(row);
            this.status.put(sourceId, Map.copyOf(updated.status()));
        }));
    }

    /**
     * Records a provider-reported health.
     *
     * @param sourceId the source
     * @param key the stream id, or {@link EdpStats#SOURCE_KEY}
     * @param health the reported health
     * @param message the provider's message
     * @param at when it was reported
     */
    public void report(String sourceId, String key, String health, String message, Instant at) {
        update(sourceId, stats -> {
            EdpStats.StatusReport previous = stats.status().get(key);
            Instant unhealthyAt = !"ok".equals(health) ? at : previous == null ? null : previous.lastUnhealthyAt();
            stats.status().put(key, new EdpStats.StatusReport(health, message, at, unhealthyAt));
            return stats;
        });
    }

    /** Clears one status key (a released snapshot hold). */
    public void clear(String sourceId, String key) {
        update(sourceId, stats -> {
            stats.status().remove(key);
            return stats;
        });
    }

    /** The current status reports of {@code sourceId}, loaded on first use. */
    public Map<String, EdpStats.StatusReport> status(String sourceId) {
        return status.computeIfAbsent(sourceId, id -> repository.findById(id)
                .map(row -> Map.copyOf(read(row).status()))
                .orElse(Map.of()));
    }

    /** Forgets a deleted source. */
    public void forget(String sourceId) {
        status.remove(sourceId);
    }

    /**
     * True when {@code stream} (or the source as a whole) reported anything but {@code ok} at or
     * after {@code since}.
     */
    public boolean unhealthySince(String sourceId, String stream, Instant since) {
        Map<String, EdpStats.StatusReport> reports = status(sourceId);
        for (String key : List.of(stream, EdpStats.SOURCE_KEY)) {
            EdpStats.StatusReport report = reports.get(key);
            if (report != null && report.lastUnhealthyAt() != null && !report.lastUnhealthyAt().isBefore(since)) {
                return true;
            }
        }
        return false;
    }

    /** The current unhealthy reports of {@code sourceId}, most severe first. */
    public List<Map.Entry<String, EdpStats.StatusReport>> unhealthy(String sourceId) {
        List<Map.Entry<String, EdpStats.StatusReport>> out = new ArrayList<>();
        for (Map.Entry<String, EdpStats.StatusReport> entry : status(sourceId).entrySet()) {
            if (entry.getValue().unhealthy()) {
                out.add(entry);
            }
        }
        out.sort((a, b) -> Integer.compare(severity(b.getValue().health()), severity(a.getValue().health())));
        return out;
    }

    /** The worst unhealthy report, if any. */
    public Optional<Map.Entry<String, EdpStats.StatusReport>> worst(String sourceId) {
        List<Map.Entry<String, EdpStats.StatusReport>> all = unhealthy(sourceId);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    /** {@code blocked} and {@code geo-restricted} stop data; {@code degraded} and {@code layout-changed} damage it. */
    static int severity(String health) {
        return switch (health == null ? "" : health) {
            case "blocked", "geo-restricted" -> 2;
            case "degraded", "layout-changed" -> 1;
            default -> 0;
        };
    }
}
