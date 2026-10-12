package ai.foresignal.testsupport.stats;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The shared context of prompt 038's integration tests: prompt 019's retention sources (and with them
 * the collector and ingest sources, among them the SAMPLED TRACK {@code test-track}), plus
 *
 * <ul>
 *   <li>{@value #POINTS}: STANDARD, kinds {@value #POINT_KIND} and {@value #OTHER_KIND}, default
 *       retention 7 d, volatile field {@code updated}, title from {@code name};
 *   <li>{@value #SECRET}: the same, private, kind {@value #SECRET_KIND}.
 * </ul>
 *
 * <p>Every 038 IT imports exactly this, so they share one cached context.
 */
@TestConfiguration
@Import(RetentionTestSources.class)
public class StatsTestSources {

    /** STANDARD point source. */
    public static final String POINTS = "st-points";

    /** Private STANDARD point source. */
    public static final String SECRET = "st-secret";

    /** First kind of {@value #POINTS}. */
    public static final String POINT_KIND = "st-point";

    /** Second kind of {@value #POINTS}. */
    public static final String OTHER_KIND = "st-other";

    /** Kind of {@value #SECRET}. */
    public static final String SECRET_KIND = "st-secret-thing";

    /** {@value #POINTS}. */
    @Bean
    public TestPushAdapter stPointsAdapter() {
        return new TestPushAdapter(base(POINTS).kinds(POINT_KIND, OTHER_KIND).build());
    }

    /** {@value #SECRET}. */
    @Bean
    public TestPushAdapter stSecretAdapter() {
        return new TestPushAdapter(base(SECRET).kind(SECRET_KIND).privateSource(true).build());
    }

    private static SourceDescriptor.Builder base(String id) {
        return SourceDescriptor.builder(id)
                .name("Stats test source " + id)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000))
                .defaultRetention(Duration.ofDays(7))
                .volatileFields("updated")
                .titleField("name");
    }
}
