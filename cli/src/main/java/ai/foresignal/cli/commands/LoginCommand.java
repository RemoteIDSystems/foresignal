package ai.foresignal.cli.commands;

import ai.foresignal.cli.CliUsageException;
import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.config.ConfigStore;
import ai.foresignal.cli.config.Connection;
import ai.foresignal.cli.output.OutputFormat;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal login --url URL --token TOKEN [--profile NAME]}: stores a profile in
 * {@code ~/.config/foresignal/config.json} (owner-only permissions on POSIX) and makes it current. {@code --token}
 * without a value prompts for it without echo. Nothing is sent to the server, and the token is never printed.
 */
@Command(name = "login", description = "Save a server URL and API token as a named profile.")
public final class LoginCommand implements Callable<Integer> {

    @ParentCommand
    ForesignalCli root;

    @Option(names = "--token", required = true, interactive = true, arity = "0..1",
            description = "API token (fsr_…); give the option without a value to be prompted.")
    char[] token;

    /** Creates the command (picocli). */
    public LoginCommand() {
    }

    /**
     * Saves the profile.
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        String url = root.urlOption();
        if (url == null || url.isBlank()) {
            throw new CliUsageException("Missing required option: '--url=<url>'");
        }
        String normalized = Connection.normalizeUrl(url);
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            throw new CliUsageException("--url must start with http:// or https://");
        }
        if (token == null || token.length == 0 || new String(token).isBlank()) {
            throw new CliUsageException("--token must not be empty");
        }
        String name = root.profileName() == null ? ConfigStore.DEFAULT_PROFILE : root.profileName();
        Path file = root.config().saveProfile(name, normalized, new String(token).trim());
        java.util.Arrays.fill(token, '\0');
        if (root.printer().format() == OutputFormat.table) {
            root.printer().text("Saved profile '" + name + "' for " + normalized + " in " + file);
        } else {
            ObjectNode out = root.json().createObjectNode();
            out.put("profile", name);
            out.put("url", normalized);
            out.put("file", file.toString());
            root.printer().print(out);
        }
        return ExitCodes.OK;
    }
}
