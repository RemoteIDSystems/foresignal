package ai.foresignal.testsupport.raster;

import ai.foresignal.testsupport.collect.CollectorTestSources;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The raster test context (prompt 042): prompt 010's collector sources (with the
 * {@code MutableClock} and {@code TestRecords}) plus the {@link SyntheticRasterAdapter}. Every 042
 * integration test is a {@code @WebIntegrationTest} importing exactly this, so they share one cached
 * context (checkpoint 041: count contexts before adding one).
 */
@TestConfiguration
@Import(CollectorTestSources.class)
public class RasterTestSources {

    /** The synthetic product source. */
    @Bean
    public SyntheticRasterAdapter syntheticRasterAdapter() {
        return new SyntheticRasterAdapter();
    }
}
