package ai.foresignal.search;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Retention for {@code embedding_document} (prompt 027, the 019 step mechanism): deletes record
 * documents whose head no longer exists (purged by retention or removed), in chunks. Runs after the
 * head purges (order 400); a head purged asynchronously by the writer after this step is cleaned on
 * the next run. Briefing and alert documents follow their own prompts (029, 030).
 */
@Component
public class EmbeddingRetentionStep implements RetentionStep {

    /** Documents deleted per statement. */
    static final int CHUNK = 10_000;

    static final String DELETE_ORPHANS = """
            DELETE FROM embedding_document WHERE (doc_type, doc_id) IN (
              SELECT d.doc_type, d.doc_id FROM embedding_document d
               WHERE d.doc_type = 'record'
                 AND NOT EXISTS (SELECT 1 FROM record_head h
                                  WHERE h.source_id = d.source_id
                                    AND h.external_id = substring(d.doc_id FROM char_length(d.source_id) + 2))
               LIMIT :limit)
            """;

    @Override
    public String name() {
        return "embedding-documents";
    }

    @Override
    public int order() {
        return 450;
    }

    @Override
    public void run(RetentionRun run) {
        long total = 0;
        int deleted;
        do {
            deleted = run.jdbc().update(DELETE_ORPHANS, new MapSqlParameterSource("limit", CHUNK));
            total += deleted;
        } while (deleted == CHUNK);
        run.deleted("embedding_document", total);
    }
}
