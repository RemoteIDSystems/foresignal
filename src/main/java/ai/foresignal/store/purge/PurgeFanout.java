package ai.foresignal.store.purge;

import ai.foresignal.search.DocInput;
import ai.foresignal.stats.metrics.ProjectionService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Erases one purged entity from the stores derived from its versions (prompt 039), after the writer
 * committed the {@code EntityPurge}. Each step runs in its own transaction (an after-commit handler's
 * writes are otherwise lost, 027), so a failing step never undoes an earlier one.
 *
 * <p>Steps: delete the {@code embedding_document} rows of the record and of its alerts; redact the
 * persisted notifications of its alerts; redact its alerts' {@code record_snapshot} and
 * {@code message} to {@code {"redacted":true}} (with the enrichment summary and context), or delete the
 * alerts when {@code purgeAlerts}; delete its {@code record_region} rows when that table exists
 * (prompt 040 creates it; until then this is a no-op); drop its metric series; and count, without
 * editing, the Ask messages and briefing runs whose citations reference it.
 */
@Component
public class PurgeFanout {

    /** The value redacted JSON columns and messages are set to. */
    public static final String REDACTED = "{\"redacted\":true}";

    private static final Logger log = LoggerFactory.getLogger(PurgeFanout.class);

    private static final String ALERT_IDS = "SELECT id FROM alert WHERE source_id = ? AND external_id = ?";

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ObjectProvider<ProjectionService> projections;
    private final JsonMapper json;

    /** Creates the fan-out. */
    public PurgeFanout(JdbcTemplate jdbc, PlatformTransactionManager transactions,
            ObjectProvider<ProjectionService> projections, JsonMapper json) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.projections = projections;
        this.json = json;
    }

    /**
     * What the fan-out did.
     *
     * @param embeddingsDeleted embedding rows deleted
     * @param notificationsRedacted notifications redacted
     * @param alertsRedacted alerts redacted
     * @param alertsDeleted alerts deleted
     * @param regionRowsDeleted {@code record_region} rows deleted
     * @param metricSeriesDropped true when the entity owned metric series
     * @param citing the citing documents
     */
    public record Result(int embeddingsDeleted, int notificationsRedacted, int alertsRedacted, int alertsDeleted,
            int regionRowsDeleted, boolean metricSeriesDropped, PurgeReport.CitingDocuments citing) {}

    /** Runs every step for {@code sourceId}/{@code externalId}. */
    public Result run(String sourceId, String externalId, boolean purgeAlerts) {
        int embeddings = deleteEmbeddings(sourceId, externalId);
        int notifications = redactNotifications(sourceId, externalId);
        int redacted = 0;
        int deleted = 0;
        if (purgeAlerts) {
            deleted = deleteAlerts(sourceId, externalId);
        } else {
            redacted = redactAlerts(sourceId, externalId);
        }
        int regions = deleteRegionRows(sourceId, externalId);
        boolean metrics = dropMetricSeries(sourceId, externalId);
        PurgeReport.CitingDocuments citing = countCitations(sourceId, externalId);
        return new Result(embeddings, notifications, redacted, deleted, regions, metrics, citing);
    }

    /** Deletes the record's embedding rows and those of its enriched alerts. */
    public int deleteEmbeddings(String sourceId, String externalId) {
        return inTx(() -> jdbc.update("DELETE FROM embedding_document WHERE (doc_type = ? AND doc_id = ?)"
                        + " OR (doc_type = ? AND doc_id IN (SELECT id::text FROM alert WHERE source_id = ?"
                        + " AND external_id = ?))",
                DocInput.RECORD, DocInput.recordId(sourceId, externalId), DocInput.ALERT, sourceId, externalId));
    }

    /** Redacts the persisted notifications (018/033) of the entity's alerts. */
    public int redactNotifications(String sourceId, String externalId) {
        return inTx(() -> jdbc.update("UPDATE notification SET title = '[redacted]', short_title = NULL,"
                + " body_markdown = NULL, links = NULL, data = CAST(? AS jsonb) WHERE alert_id IN (" + ALERT_IDS + ")",
                REDACTED, sourceId, externalId));
    }

    /** Redacts the entity's alerts: snapshot, message, enrichment summary, citations and context. */
    public int redactAlerts(String sourceId, String externalId) {
        return inTx(() -> jdbc.update("UPDATE alert SET record_snapshot = CAST(? AS jsonb), message = ?,"
                        + " summary = NULL, summary_citations = NULL, context = NULL"
                        + " WHERE source_id = ? AND external_id = ?",
                REDACTED, REDACTED, sourceId, externalId));
    }

    /** Deletes the entity's alerts and their escalation state. */
    public int deleteAlerts(String sourceId, String externalId) {
        return inTx(() -> {
            jdbc.update("DELETE FROM alert_escalation WHERE alert_id IN (" + ALERT_IDS + ")", sourceId, externalId);
            return jdbc.update("DELETE FROM alert WHERE source_id = ? AND external_id = ?", sourceId, externalId);
        });
    }

    /** Deletes the entity's {@code record_region} rows (prompt 040); 0 while the table does not exist. */
    public int deleteRegionRows(String sourceId, String externalId) {
        String table = jdbc.queryForObject("SELECT to_regclass('record_region')::text", String.class);
        if (table == null) {
            return 0;
        }
        return inTx(() -> jdbc.update("DELETE FROM record_region WHERE source_id = ? AND external_id = ?",
                sourceId, externalId));
    }

    /** Drops the entity's metric series (038); false when it had none or projections are not running. */
    public boolean dropMetricSeries(String sourceId, String externalId) {
        ProjectionService service = projections.getIfAvailable();
        if (service == null) {
            return false;
        }
        try {
            return service.dropEntity(sourceId, externalId);
        } catch (RuntimeException e) {
            log.warn("Dropping the metric series of a purged entity failed", e);
            return false;
        }
    }

    /** Counts the Ask messages and briefing runs whose citations reference the entity. */
    public PurgeReport.CitingDocuments countCitations(String sourceId, String externalId) {
        String containment = json.writeValueAsString(List.of(Map.of("id", DocInput.recordId(sourceId, externalId))));
        Integer ask = jdbc.queryForObject(
                "SELECT count(*) FROM ask_message WHERE citations @> CAST(? AS jsonb)", Integer.class, containment);
        Integer runs = jdbc.queryForObject(
                "SELECT count(*) FROM briefing_run WHERE citations @> CAST(? AS jsonb)", Integer.class, containment);
        return new PurgeReport.CitingDocuments(ask == null ? 0 : ask, runs == null ? 0 : runs);
    }

    private int inTx(java.util.function.Supplier<Integer> work) {
        Integer n = tx.execute(status -> work.get());
        return n == null ? 0 : n;
    }
}
