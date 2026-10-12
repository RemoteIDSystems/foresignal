package ai.foresignal.ask;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.sql.Timestamp;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Deletes Ask threads with no activity for longer than the {@code ask} retention (default 90 days;
 * Architecture §11 step 3, prompt 028), with their messages and actions.
 */
@Component
public class AskRetentionStep implements RetentionStep {

    @Override
    public String name() {
        return "ask-threads";
    }

    @Override
    public int order() {
        return 710;
    }

    @Override
    public void run(RetentionRun run) {
        MapSqlParameterSource cutoff =
                new MapSqlParameterSource("t", Timestamp.from(run.now().minus(run.settings().ask())));
        String expired = "SELECT id FROM ask_thread WHERE updated_at < :t";
        run.deleted("ask_action", run.jdbc().update("DELETE FROM ask_action WHERE thread_id IN (" + expired + ")", cutoff));
        run.deleted("ask_message", run.jdbc().update("DELETE FROM ask_message WHERE thread_id IN (" + expired + ")", cutoff));
        run.deleted("ask_thread", run.jdbc().update("DELETE FROM ask_thread WHERE updated_at < :t", cutoff));
    }
}
