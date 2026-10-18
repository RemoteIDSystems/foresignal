package ai.foresignal.briefing;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.sql.Timestamp;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Deletes briefing runs that started longer ago than the {@code briefings} retention (default 180
 * days; prompt 029), with their search documents. Runs still marked {@code running} are left alone.
 * Briefing definitions are never deleted by retention. Prompt 141: the audio media of the deleted runs
 * ({@code audio.mediaId}) is expired at the same time, so the media retention step (order 780) removes it.
 */
@Component
public class BriefingRetentionStep implements RetentionStep {

    @Override
    public String name() {
        return "briefing-runs";
    }

    @Override
    public int order() {
        return 720;
    }

    @Override
    public void run(RetentionRun run) {
        MapSqlParameterSource cutoff = new MapSqlParameterSource("t",
                Timestamp.from(run.now().minus(run.settings().briefings())));
        String expired = "SELECT id::text FROM briefing_run WHERE started_at < :t AND status <> 'running'";
        cutoff.addValue("now", Timestamp.from(run.now()));
        run.jdbc().update("UPDATE media SET expires_at = :now WHERE role = 'audio' AND (expires_at IS NULL OR"
                + " expires_at > :now) AND id IN (SELECT audio->>'mediaId' FROM briefing_run WHERE started_at < :t"
                + " AND status <> 'running' AND audio->>'mediaId' IS NOT NULL)", cutoff);
        run.deleted("embedding_document", run.jdbc().update(
                "DELETE FROM embedding_document WHERE doc_type = 'briefing' AND doc_id IN (" + expired + ")", cutoff));
        run.deleted("briefing_run", run.jdbc().update(
                "DELETE FROM briefing_run WHERE started_at < :t AND status <> 'running'", cutoff));
    }
}
