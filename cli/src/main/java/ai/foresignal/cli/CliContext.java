package ai.foresignal.cli;

import ai.foresignal.cli.sse.Sleeper;
import java.io.PrintWriter;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Everything a command touches outside its own arguments: standard output and error, the environment, the
 * configuration directory, the HTTP client, the clock, sleeping and randomness. {@link #system()} gives the real
 * process; tests build one with {@link #builder()} so that nothing reads the real home directory, environment or
 * wall clock.
 */
public final class CliContext {

    /** System property that overrides the configuration directory (tests and packagers). */
    public static final String CONFIG_DIR_PROPERTY = "foresignal.config.dir";

    /** Environment variable that overrides the configuration directory. */
    public static final String CONFIG_DIR_ENV = "FORESIGNAL_CONFIG_DIR";

    private final PrintWriter out;
    private final PrintWriter err;
    private final Map<String, String> env;
    private final Path configDir;
    private final Supplier<HttpClient> httpClient;
    private final Clock clock;
    private final Sleeper sleeper;
    private final RandomGenerator random;

    private CliContext(Builder b) {
        this.out = Objects.requireNonNull(b.out, "out");
        this.err = Objects.requireNonNull(b.err, "err");
        this.env = Map.copyOf(b.env);
        this.configDir = b.configDir != null ? b.configDir : defaultConfigDir(this.env);
        this.httpClient = b.httpClient != null ? b.httpClient : CliContext::defaultHttpClient;
        this.clock = b.clock != null ? b.clock : Clock.systemUTC();
        this.sleeper = b.sleeper != null ? b.sleeper : Sleeper.SYSTEM;
        this.random = b.random != null ? b.random : RandomGenerator.getDefault();
    }

    /**
     * The real process: {@code System.out}, {@code System.err}, the real environment and the default
     * configuration directory.
     *
     * @return the context of this process
     */
    public static CliContext system() {
        return builder()
                .out(new PrintWriter(System.out, true, StandardCharsets.UTF_8))
                .err(new PrintWriter(System.err, true, StandardCharsets.UTF_8))
                .env(System.getenv())
                .build();
    }

    /**
     * Starts a builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The configuration directory: the {@value #CONFIG_DIR_PROPERTY} system property, else the
     * {@value #CONFIG_DIR_ENV} variable, else {@code ~/.config/foresignal}.
     *
     * @param env the environment
     * @return the directory holding {@code config.json}
     */
    public static Path defaultConfigDir(Map<String, String> env) {
        String property = System.getProperty(CONFIG_DIR_PROPERTY);
        if (property != null && !property.isBlank()) {
            return Path.of(property);
        }
        String variable = env.get(CONFIG_DIR_ENV);
        if (variable != null && !variable.isBlank()) {
            return Path.of(variable);
        }
        return Path.of(System.getProperty("user.home"), ".config", "foresignal");
    }

    private static HttpClient defaultHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Standard output: command results only.
     *
     * @return the output writer
     */
    public PrintWriter out() {
        return out;
    }

    /**
     * Standard error: notices, warnings and errors.
     *
     * @return the error writer
     */
    public PrintWriter err() {
        return err;
    }

    /**
     * One environment variable.
     *
     * @param name the variable name
     * @return its value, empty when unset or blank
     */
    public Optional<String> env(String name) {
        String value = env.get(name);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    /**
     * The configuration directory.
     *
     * @return the directory holding {@code config.json}
     */
    public Path configDir() {
        return configDir;
    }

    /**
     * A new HTTP client (one per command run).
     *
     * @return the HTTP client
     */
    public HttpClient httpClient() {
        return httpClient.get();
    }

    /**
     * The clock.
     *
     * @return the clock
     */
    public Clock clock() {
        return clock;
    }

    /**
     * How the CLI waits between polls and reconnects.
     *
     * @return the sleeper
     */
    public Sleeper sleeper() {
        return sleeper;
    }

    /**
     * Randomness for backoff jitter.
     *
     * @return the random generator
     */
    public RandomGenerator random() {
        return random;
    }

    /** Builds a {@link CliContext}. */
    public static final class Builder {
        private PrintWriter out;
        private PrintWriter err;
        private Map<String, String> env = Map.of();
        private Path configDir;
        private Supplier<HttpClient> httpClient;
        private Clock clock;
        private Sleeper sleeper;
        private RandomGenerator random;

        private Builder() {
        }

        /**
         * Sets standard output.
         *
         * @param out the writer
         * @return this builder
         */
        public Builder out(PrintWriter out) {
            this.out = out;
            return this;
        }

        /**
         * Sets standard error.
         *
         * @param err the writer
         * @return this builder
         */
        public Builder err(PrintWriter err) {
            this.err = err;
            return this;
        }

        /**
         * Sets the environment.
         *
         * @param env the variables
         * @return this builder
         */
        public Builder env(Map<String, String> env) {
            this.env = env;
            return this;
        }

        /**
         * Sets the configuration directory.
         *
         * @param configDir the directory
         * @return this builder
         */
        public Builder configDir(Path configDir) {
            this.configDir = configDir;
            return this;
        }

        /**
         * Sets the HTTP client factory.
         *
         * @param httpClient the factory
         * @return this builder
         */
        public Builder httpClient(Supplier<HttpClient> httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets the clock.
         *
         * @param clock the clock
         * @return this builder
         */
        public Builder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Sets the sleeper.
         *
         * @param sleeper the sleeper
         * @return this builder
         */
        public Builder sleeper(Sleeper sleeper) {
            this.sleeper = sleeper;
            return this;
        }

        /**
         * Sets the random generator.
         *
         * @param random the generator
         * @return this builder
         */
        public Builder random(RandomGenerator random) {
            this.random = random;
            return this;
        }

        /**
         * Builds the context.
         *
         * @return the context
         */
        public CliContext build() {
            return new CliContext(this);
        }
    }
}
