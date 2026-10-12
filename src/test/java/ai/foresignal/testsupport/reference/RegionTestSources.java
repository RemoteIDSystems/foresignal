package ai.foresignal.testsupport.reference;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The region tagging test context (prompt 040): the trigger sources of {@link TriggerTestSources}, plus
 *
 * <ul>
 *   <li>{@value #POINTS}: STANDARD points (kind {@value #POINT_KIND}), eager by default; {@code name} is a
 *       search field;
 *   <li>{@value #LAZY}: STANDARD points the tests switch to {@code lazy} or {@code off}.
 * </ul>
 *
 * <p>Both are PUSH sources with the default ALL policy, so every content change persists a version. Every
 * 040 integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(TriggerTestSources.class)
public class RegionTestSources {

    /** Eager STANDARD points. */
    public static final String POINTS = "test-region-points";

    /** STANDARD points for the lazy and off modes. */
    public static final String LAZY = "test-region-lazy";

    /** The kind of both. */
    public static final String POINT_KIND = "test-region-point";

    /** {@value #POINTS}. */
    @Bean
    public TestPushAdapter testRegionPointsAdapter() {
        return new TestPushAdapter(base(POINTS).build());
    }

    /** {@value #LAZY}. */
    @Bean
    public TestPushAdapter testRegionLazyAdapter() {
        return new TestPushAdapter(base(LAZY).build());
    }

    private static SourceDescriptor.Builder base(String id) {
        return SourceDescriptor.builder(id)
                .name("Region test source " + id)
                .attribution("Attribution of " + id)
                .kind(POINT_KIND)
                .mode(Mode.PUSH)
                .titleField("name")
                .searchFields("name")
                .defaultBudget(Budget.of(60, 10_000));
    }
}
