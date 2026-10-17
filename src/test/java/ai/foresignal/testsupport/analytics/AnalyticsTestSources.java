package ai.foresignal.testsupport.analytics;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The shared context of prompt 119's integration tests: prompt 038's stats sources (and with them the retention,
 * collector and ingest sources), the coverage helper, plus
 *
 * <ul>
 *   <li>{@value #SERIES}: a PUSH series source (kinds {@code series-point} and {@code series-latest}), retention 3650 d;
 *   <li>{@value #LOCAL}: the same with egress {@code LOCAL_ONLY}.
 * </ul>
 */
@TestConfiguration
@Import({StatsTestSources.class, CoverageSupport.Config.class})
public class AnalyticsTestSources {

    /** The series source. */
    public static final String SERIES = "an-series";

    /** The LOCAL_ONLY series source. */
    public static final String LOCAL = "an-local";

    /** {@value #SERIES}. */
    @Bean
    public TestPushAdapter anSeriesAdapter() {
        return new TestPushAdapter(base(SERIES).build());
    }

    /** {@value #LOCAL}. */
    @Bean
    public TestPushAdapter anLocalAdapter() {
        return new TestPushAdapter(base(LOCAL).egress(Egress.LOCAL_ONLY).build());
    }

    private static SourceDescriptor.Builder base(String id) {
        return SourceDescriptor.builder(id)
                .name("Analytics test series " + id)
                .mode(Mode.PUSH)
                .kinds(SeriesPoint.KIND, SeriesPoint.LATEST_KIND)
                .defaultBudget(Budget.of(60, 10_000))
                .defaultRetention(Duration.ofDays(3650))
                .titleField("seriesName");
    }
}
