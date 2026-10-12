package ai.foresignal.config;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Maps the bootstrap variable {@code FORESIGNAL_LOG_FORMAT} onto
 * {@code logging.structured.format.console} (prompt 019).
 *
 * <p>{@code ecs} (the default under the {@code docker} profile), {@code gelf} or {@code logstash}
 * select Boot's structured console logging; {@code text} (the default otherwise) keeps the
 * human-readable pattern. An explicit {@code logging.structured.format.console} wins. Runs after the
 * config data processor, so the active profiles are known, and before the logging system starts.
 *
 * <p>Trace and span ids from Micrometer Tracing are in the MDC either way; ECS output carries them as
 * fields.
 */
public class LogFormatEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /** The bootstrap variable. */
    public static final String VARIABLE = "FORESIGNAL_LOG_FORMAT";

    /** Boot's property. */
    static final String PROPERTY = "logging.structured.format.console";

    /** The plain-text value. */
    static final String TEXT = "text";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.containsProperty(PROPERTY)) {
            return;
        }
        String format = resolve(environment.getProperty(VARIABLE),
                Arrays.asList(environment.getActiveProfiles()).contains("docker"));
        if (format != null) {
            environment.getPropertySources().addLast(
                    new MapPropertySource("foresignalLogFormat", Map.of(PROPERTY, format)));
        }
    }

    /**
     * The structured format to use, or null for plain text.
     *
     * @param configured {@code FORESIGNAL_LOG_FORMAT}, or null
     * @param docker whether the {@code docker} profile is active
     */
    static String resolve(String configured, boolean docker) {
        String value = configured == null || configured.isBlank()
                ? (docker ? "ecs" : TEXT)
                : configured.strip().toLowerCase(Locale.ROOT);
        return TEXT.equals(value) ? null : value;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
