package ai.foresignal.config;

import java.time.Clock;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supplies the application clock.
 *
 * <p>Every component that needs the current time injects this {@link Clock} rather than calling
 * {@code Instant.now()}, so tests can advance time deterministically.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}
