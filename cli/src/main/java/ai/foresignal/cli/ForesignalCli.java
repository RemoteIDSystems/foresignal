package ai.foresignal.cli;

import ai.foresignal.cli.commands.AlertsCommand;
import ai.foresignal.cli.commands.AskCommand;
import ai.foresignal.cli.commands.BriefingsCommand;
import ai.foresignal.cli.commands.ChangesCommand;
import ai.foresignal.cli.commands.EdpCommand;
import ai.foresignal.cli.commands.ExportsCommand;
import ai.foresignal.cli.commands.LoginCommand;
import ai.foresignal.cli.commands.RecordsCommand;
import ai.foresignal.cli.commands.SourcesCommand;
import ai.foresignal.cli.commands.SystemCommand;
import ai.foresignal.cli.commands.TokensCommand;
import ai.foresignal.cli.commands.TriggersCommand;
import ai.foresignal.cli.config.ConfigStore;
import ai.foresignal.cli.config.Connection;
import ai.foresignal.cli.config.Profile;
import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.ApiException;
import ai.foresignal.cli.http.NetworkException;
import ai.foresignal.cli.output.OutputFormat;
import ai.foresignal.cli.output.Printer;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code foresignal} command: the entry point, the options every subcommand inherits ({@code --output},
 * {@code --profile}, {@code --url}, {@code --timeout}), connection resolution, and the mapping of failures to
 * {@link ExitCodes}.
 *
 * <p>Running a command group without a subcommand ({@code foresignal}, {@code foresignal sources}) is a usage
 * error: picocli reports "Missing required subcommand" and the exit code is 1.
 *
 * <p>Connection precedence: {@code --url} beats {@code FORESIGNAL_URL}, which beats the profile; the token comes
 * from {@code FORESIGNAL_TOKEN}, else the profile. The token is never printed.
 */
@Command(name = "foresignal",
        mixinStandardHelpOptions = true,
        versionProvider = ForesignalCli.Version.class,
        description = "Command-line client of the Foresignal intelligence broker.",
        subcommands = {
            LoginCommand.class,
            SystemCommand.class,
            SourcesCommand.class,
            RecordsCommand.class,
            ChangesCommand.class,
            AlertsCommand.class,
            TriggersCommand.class,
            AskCommand.class,
            BriefingsCommand.class,
            ExportsCommand.class,
            TokensCommand.class,
            EdpCommand.class,
            CommandLine.HelpCommand.class
        },
        exitCodeListHeading = "%nExit codes:%n",
        exitCodeList = {
            "0:Success",
            "1:Usage error or invalid input",
            "2:API error (the ProblemDetail code is printed)",
            "3:Network error"
        })
public final class ForesignalCli {

    /** The CLI version. */
    public static final String VERSION = "0.1.0-SNAPSHOT";

    /** Environment variable overriding the server URL. */
    public static final String ENV_URL = "FORESIGNAL_URL";

    /** Environment variable overriding the API token. */
    public static final String ENV_TOKEN = "FORESIGNAL_TOKEN";

    @Option(names = {"-o", "--output"}, scope = ScopeType.INHERIT, defaultValue = "table",
            description = "Output format: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    OutputFormat output = OutputFormat.table;

    @Option(names = "--profile", scope = ScopeType.INHERIT,
            description = "Configuration profile (default: the current profile, else 'default').")
    String profile;

    @Option(names = "--url", scope = ScopeType.INHERIT,
            description = "Server base URL for this call (overrides FORESIGNAL_URL and the profile).")
    String url;

    @Option(names = "--timeout", scope = ScopeType.INHERIT, defaultValue = "60",
            description = "Request timeout in seconds (default: ${DEFAULT-VALUE}).")
    int timeoutSeconds = 60;

    private final CliContext ctx;
    private final JsonMapper json = JsonMapper.builder().build();

    /** Creates the command for the real process. */
    public ForesignalCli() {
        this(CliContext.system());
    }

    /**
     * Creates the command over a context.
     *
     * @param ctx the process context
     */
    public ForesignalCli(CliContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Runs the CLI and exits the JVM with its exit code.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        int code = run(CliContext.system(), args);
        System.exit(code);
    }

    /**
     * Runs the CLI without exiting (tests and embedding).
     *
     * @param ctx  the process context
     * @param args the arguments
     * @return the exit code
     */
    public static int run(CliContext ctx, String... args) {
        return commandLine(ctx).execute(args);
    }

    /**
     * Builds the configured {@link CommandLine}: output writers, case-insensitive enum values, and the exception
     * handlers that implement the exit codes.
     *
     * @param ctx the process context
     * @return the command line
     */
    public static CommandLine commandLine(CliContext ctx) {
        ForesignalCli root = new ForesignalCli(ctx);
        CommandLine cl = new CommandLine(root);
        cl.setOut(ctx.out());
        cl.setErr(ctx.err());
        cl.setCaseInsensitiveEnumValuesAllowed(true);
        cl.setParameterExceptionHandler(ForesignalCli::onParameterException);
        cl.setExecutionExceptionHandler((ex, commandLine, parseResult) -> onExecutionException(ctx, ex));
        return cl;
    }

    private static int onParameterException(CommandLine.ParameterException ex, String[] args) {
        CommandLine cl = ex.getCommandLine();
        PrintWriter err = cl.getErr();
        err.println("error: " + ex.getMessage());
        if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, err)) {
            err.println("Run '" + cl.getCommandSpec().qualifiedName() + " --help' for usage.");
        }
        err.flush();
        return ExitCodes.USAGE;
    }

    /**
     * Maps a failure to its exit code and prints it on standard error.
     *
     * @param ctx the process context
     * @param ex  the failure
     * @return the exit code
     */
    static int onExecutionException(CliContext ctx, Exception ex) {
        Throwable t = ex;
        while (t instanceof CommandLine.ExecutionException && t.getCause() != null) {
            t = t.getCause();
        }
        PrintWriter err = ctx.err();
        int code;
        if (t instanceof ApiException api) {
            err.println("error: " + api.code() + " (HTTP " + api.status() + ")"
                    + (api.title() == null ? "" : ": " + api.title()));
            if (api.detail() != null && !api.detail().equals(api.title())) {
                err.println("  " + api.detail());
            }
            for (String line : api.fieldErrors()) {
                err.println("  - " + line);
            }
            code = ExitCodes.API_ERROR;
        } else if (t instanceof NetworkException net) {
            err.println("error: network: " + net.getMessage());
            code = ExitCodes.NETWORK;
        } else if (t instanceof CliUsageException usage) {
            err.println("error: " + usage.getMessage());
            code = ExitCodes.USAGE;
        } else {
            err.println("error: " + t);
            code = ExitCodes.USAGE;
        }
        err.flush();
        return code;
    }

    /**
     * The process context.
     *
     * @return the context
     */
    public CliContext ctx() {
        return ctx;
    }

    /**
     * The shared JSON mapper.
     *
     * @return the mapper
     */
    public JsonMapper json() {
        return json;
    }

    /**
     * The selected profile name, or null for the current one.
     *
     * @return the {@code --profile} value
     */
    public String profileName() {
        return profile;
    }

    /**
     * The {@code --url} value.
     *
     * @return the URL given on the command line, or null
     */
    public String urlOption() {
        return url;
    }

    /**
     * The request timeout.
     *
     * @return the {@code --timeout} value
     */
    public Duration timeout() {
        return Duration.ofSeconds(Math.max(1, timeoutSeconds));
    }

    /**
     * The configuration store.
     *
     * @return the store over the context's configuration directory
     */
    public ConfigStore config() {
        return new ConfigStore(ctx.configDir(), ctx.err());
    }

    /**
     * Resolves the URL and token for this call.
     *
     * @return the connection
     * @throws CliUsageException when no URL is configured anywhere
     */
    public Connection connection() {
        Optional<Profile> selected = config().profile(profile);
        if (profile != null && selected.isEmpty() && ctx.env(ENV_URL).isEmpty() && url == null) {
            throw new CliUsageException("No profile '" + profile + "' in " + config().file()
                    + "; run 'foresignal login --profile " + profile + " --url URL --token TOKEN'");
        }
        String base = url != null ? url
                : ctx.env(ENV_URL).orElseGet(() -> selected.map(Profile::url).orElse(null));
        if (base == null || base.isBlank()) {
            throw new CliUsageException("No server URL configured: run 'foresignal login --url URL --token TOKEN' "
                    + "or set " + ENV_URL);
        }
        String token = ctx.env(ENV_TOKEN).orElseGet(() -> selected.map(Profile::token).orElse(null));
        return new Connection(base, token);
    }

    /**
     * An API client for this call.
     *
     * @return the client
     */
    public ApiClient api() {
        return new ApiClient(connection(), ctx.httpClient(), timeout(), json);
    }

    /**
     * A printer in the selected output format.
     *
     * @return the printer
     */
    public Printer printer() {
        return new Printer(ctx.out(), output, json);
    }

    /**
     * Prints a notice on standard error.
     *
     * @param message the notice
     */
    public void notice(String message) {
        ctx.err().println(message);
        ctx.err().flush();
    }

    /**
     * Reads a JSON file given on the command line ({@code -f rule.json}); {@code -} reads standard input.
     *
     * @param file the file
     * @return the document
     * @throws CliUsageException when the file cannot be read or is not JSON
     */
    public JsonNode readJsonFile(Path file) {
        try {
            String text = "-".equals(file.toString())
                    ? new String(System.in.readAllBytes(), StandardCharsets.UTF_8)
                    : Files.readString(file, StandardCharsets.UTF_8);
            return json.readTree(text);
        } catch (IOException e) {
            throw new CliUsageException("Cannot read " + file + ": " + e.getMessage(), e);
        } catch (JacksonException e) {
            throw new CliUsageException(file + " is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }

    /** Supplies {@code --version}. */
    public static final class Version implements CommandLine.IVersionProvider {

        /**
         * The version lines.
         *
         * @return {@code foresignal <version>}
         */
        @Override
        public String[] getVersion() {
            return new String[] {"foresignal " + VERSION};
        }
    }
}
