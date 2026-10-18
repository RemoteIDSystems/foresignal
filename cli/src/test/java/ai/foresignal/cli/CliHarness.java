package ai.foresignal.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

/**
 * Runs the CLI in-process with captured standard output and error, a private configuration directory, a
 * controlled environment, a deterministic random generator, and a sleeper that records delays instead of waiting.
 */
public final class CliHarness {

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();
    private final Map<String, String> env = new HashMap<>();
    private final List<Duration> sleeps = new ArrayList<>();
    private final Path configDir;

    /**
     * Creates a harness.
     *
     * @param configDir the configuration directory (need not exist)
     */
    public CliHarness(Path configDir) {
        this.configDir = configDir;
    }

    /**
     * A harness whose environment points at a server.
     *
     * @param configDir the configuration directory
     * @param url       the server URL ({@code FORESIGNAL_URL})
     * @param token     the token ({@code FORESIGNAL_TOKEN}), or null
     * @return the harness
     */
    public static CliHarness against(Path configDir, String url, String token) {
        CliHarness harness = new CliHarness(configDir);
        harness.env(ForesignalCli.ENV_URL, url);
        if (token != null) {
            harness.env(ForesignalCli.ENV_TOKEN, token);
        }
        return harness;
    }

    /**
     * Sets an environment variable.
     *
     * @param name  the name
     * @param value the value
     * @return this harness
     */
    public CliHarness env(String name, String value) {
        env.put(name, value);
        return this;
    }

    /**
     * Runs the CLI.
     *
     * @param args the arguments
     * @return the exit code
     */
    public int run(String... args) {
        CliContext ctx = CliContext.builder()
                .out(new PrintWriter(out, true))
                .err(new PrintWriter(err, true))
                .env(env)
                .configDir(configDir)
                .sleeper(sleeps::add)
                .random(new SplittableRandom(42))
                .build();
        return ForesignalCli.run(ctx, args);
    }

    /**
     * Captured standard output.
     *
     * @return the text
     */
    public String out() {
        return out.toString();
    }

    /**
     * Captured standard error.
     *
     * @return the text
     */
    public String err() {
        return err.toString();
    }

    /**
     * Non-empty standard output lines.
     *
     * @return the lines
     */
    public List<String> outLines() {
        return out.toString().lines().filter(l -> !l.isBlank()).toList();
    }

    /**
     * The recorded sleeps.
     *
     * @return the delays in order
     */
    public List<Duration> sleeps() {
        return sleeps;
    }

    /**
     * The configuration directory.
     *
     * @return the directory
     */
    public Path configDir() {
        return configDir;
    }
}
