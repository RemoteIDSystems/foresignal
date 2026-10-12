package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

/** {@code FORESIGNAL_LOG_FORMAT} to {@code logging.structured.format.console} (prompt 019). */
class LogFormatEnvironmentPostProcessorTest {

    final LogFormatEnvironmentPostProcessor processor = new LogFormatEnvironmentPostProcessor();

    @Test
    @DisplayName("ecs by default under docker, text (no structured format) otherwise")
    void defaults() {
        assertThat(LogFormatEnvironmentPostProcessor.resolve(null, true)).isEqualTo("ecs");
        assertThat(LogFormatEnvironmentPostProcessor.resolve(null, false)).isNull();
        assertThat(LogFormatEnvironmentPostProcessor.resolve(" ", false)).isNull();
    }

    @Test
    @DisplayName("an explicit value wins over the profile; text means plain")
    void explicit() {
        assertThat(LogFormatEnvironmentPostProcessor.resolve("text", true)).isNull();
        assertThat(LogFormatEnvironmentPostProcessor.resolve("LOGSTASH", false)).isEqualTo("logstash");
    }

    @Test
    @DisplayName("sets the property from the variable, and never overrides an explicit property")
    void environment() {
        MockEnvironment env = new MockEnvironment().withProperty(LogFormatEnvironmentPostProcessor.VARIABLE, "gelf");
        processor.postProcessEnvironment(env, new SpringApplication());
        assertThat(env.getProperty("logging.structured.format.console")).isEqualTo("gelf");

        MockEnvironment explicit = new MockEnvironment()
                .withProperty(LogFormatEnvironmentPostProcessor.VARIABLE, "gelf")
                .withProperty("logging.structured.format.console", "ecs");
        processor.postProcessEnvironment(explicit, new SpringApplication());
        assertThat(explicit.getProperty("logging.structured.format.console")).isEqualTo("ecs");

        MockEnvironment docker = new MockEnvironment();
        docker.setActiveProfiles("docker");
        processor.postProcessEnvironment(docker, new SpringApplication());
        assertThat(docker.getProperty("logging.structured.format.console")).isEqualTo("ecs");
    }
}
