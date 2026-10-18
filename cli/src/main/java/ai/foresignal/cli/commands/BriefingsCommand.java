package ai.foresignal.cli.commands;

import static ai.foresignal.cli.commands.Pages.seg;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.OutputFormat;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.JsonNode;

/**
 * {@code foresignal briefings …}: {@code listBriefings} and {@code runBriefing}.
 */
@Command(name = "briefings", description = "List briefings and run one now.")
public final class BriefingsCommand {

    private static final Column[] BRIEFING_COLUMNS = {
        Column.of("id"), Column.of("name"), Column.of("enabled"), Column.of("schedule"), Column.of("mode"),
        new Column("NEXT RUN", "nextRunAt"), new Column("LAST STATUS", "lastRunStatus")
    };

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public BriefingsCommand() {
    }

    /**
     * {@code briefings list}: {@code GET /v1/briefings}.
     *
     * @return the exit code
     */
    @Command(name = "list", description = "List briefings.")
    public int list() {
        root.printer().print(root.api().get("/v1/briefings", null), BRIEFING_COLUMNS);
        return ExitCodes.OK;
    }

    /**
     * {@code briefings run}: {@code POST /v1/briefings/{briefingId}/run}; prints the run's Markdown in
     * {@code table} output.
     *
     * @param briefingId the briefing id
     * @return the exit code
     */
    @Command(name = "run", description = "Run a briefing now (agent).")
    public int run(@Parameters(paramLabel = "BRIEFING_ID") String briefingId) {
        JsonNode run = root.api().post("/v1/briefings/" + seg(briefingId) + "/run", null, null);
        if (root.printer().format() == OutputFormat.table) {
            root.notice("run " + run.path("id").asString("-") + ": " + run.path("status").asString("?")
                    + (run.hasNonNull("error") ? " (" + run.path("error").asString() + ")" : ""));
            root.printer().text(run.path("contentMarkdown").asString(""));
        } else {
            root.printer().print(run);
        }
        return ExitCodes.OK;
    }
}
