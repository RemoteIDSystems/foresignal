package ai.foresignal.testsupport.briefing;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The briefing test context (prompt 029): the Ask test context (trigger, records and ingest test
 * sources, {@code MutableClock}, {@code TestRecords}, the stub retention state and the scripted fake
 * LLM providers) plus
 *
 * <ul>
 *   <li>{@value #QUAKES}: kind {@value #QUAKE_KIND}, title field {@code place}, rank field {@code mag};
 *   <li>{@value #SECRET}: private, kind {@value #QUAKE_KIND}, title field {@code place}, rank field
 *       {@code mag}.
 * </ul>
 *
 * <p>Every briefing integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(AskTestSources.class)
public class BriefingTestSources {

    /** A public quake source ranked by magnitude. */
    public static final String QUAKES = "test-brief-quakes";

    /** A private quake source ranked by magnitude. */
    public static final String SECRET = "test-brief-secret";

    /** The kind of both. */
    public static final String QUAKE_KIND = "test-brief-quake";

    /** {@value #QUAKES}. */
    @Bean
    public TestPushAdapter testBriefQuakesAdapter() {
        return new TestPushAdapter(base(QUAKES).build());
    }

    /** {@value #SECRET}. */
    @Bean
    public TestPushAdapter testBriefSecretAdapter() {
        return new TestPushAdapter(base(SECRET).privateSource(true).build());
    }

    private static SourceDescriptor.Builder base(String id) {
        return SourceDescriptor.builder(id)
                .name("Briefing test source " + id)
                .attribution("Attribution of " + id)
                .kind(QUAKE_KIND)
                .mode(Mode.PUSH)
                .titleField("place")
                .searchFields("place")
                .rankField("mag")
                .defaultBudget(Budget.of(60, 10_000));
    }
}
