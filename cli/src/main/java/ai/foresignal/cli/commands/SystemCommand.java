package ai.foresignal.cli.commands;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * {@code foresignal system}: {@code GET /v1/system} ({@code getSystem}, open).
 */
@Command(name = "system", description = "Show the server's version, status and counters.")
public final class SystemCommand implements Callable<Integer> {

    @ParentCommand
    ForesignalCli root;

    /** Creates the command (picocli). */
    public SystemCommand() {
    }

    /**
     * Prints the system information.
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        root.printer().print(root.api().get("/v1/system", null));
        return ExitCodes.OK;
    }
}
