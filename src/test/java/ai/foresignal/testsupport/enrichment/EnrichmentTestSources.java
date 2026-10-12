package ai.foresignal.testsupport.enrichment;

import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.trigger.EnrichmentAnchor;
import ai.foresignal.trigger.EnrichmentContextContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The enrichment test context (prompt 030): the Ask test context (trigger, records and ingest test
 * sources including {@code test-standard}, {@code test-ttl} and {@code test-track}, the private
 * {@code test-ask-secret}, {@code MutableClock}, {@code TestRecords} and the scripted fake LLM
 * providers) plus a {@link TestWeatherContributor} that adds an {@code alert.context.weather} section
 * while it is switched on. No model is called.
 */
@TestConfiguration
@Import(AskTestSources.class)
public class EnrichmentTestSources {

    /** The section key of the test contributor, as prompt 056 will name its own. */
    public static final String WEATHER_KEY = "alert.context.weather";

    /** The test contributor. */
    @Bean
    public TestWeatherContributor testWeatherContributor() {
        return new TestWeatherContributor();
    }

    /**
     * Adds a fixed {@value #WEATHER_KEY} section while {@link #enabled} is on; it fails on purpose
     * while {@link #failing} is on, to show a failing contributor is skipped.
     */
    public static final class TestWeatherContributor implements EnrichmentContextContributor {

        /** Whether a section is added. */
        public final AtomicBoolean enabled = new AtomicBoolean();

        /** Whether the contributor throws. */
        public final AtomicBoolean failing = new AtomicBoolean();

        @Override
        public String key() {
            return WEATHER_KEY;
        }

        @Override
        public int order() {
            return 10;
        }

        @Override
        public Optional<Object> contribute(EnrichmentAnchor anchor) {
            if (failing.get()) {
                throw new IllegalStateException("weather frames unavailable");
            }
            if (!enabled.get()) {
                return Optional.empty();
            }
            Map<String, Object> section = new LinkedHashMap<>();
            section.put("status", "ok");
            section.put("summary", "Heavy rain near alert " + anchor.alert().id());
            section.put("radarMaxDbz", 52);
            return Optional.of(section);
        }

        /** Switches the contributor off and stops it failing. */
        public void reset() {
            enabled.set(false);
            failing.set(false);
        }
    }
}
