package ai.foresignal.testsupport.raster;

import ai.foresignal.testsupport.collect.CollectorTestSources;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The raster test context (prompt 042): prompt 010's collector sources (with the
 * {@code MutableClock} and {@code TestRecords}) plus the {@link SyntheticRasterAdapter}, from prompt 049
 * the SatDump station {@code satdump-itest}, and from prompt 052 the DERIVED source {@code test-derived}. Every 042
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

    /** The test-only DERIVED source of the prompt 052 runtime ITs (installed disabled like every source). */
    @Bean
    public TestDerivedGenerator testDerivedGenerator() {
        return new TestDerivedGenerator();
    }

    /** The SatDump station of the prompt 049 ITs: {@code satdump-itest}, installed disabled like every source. */
    public static final String SATDUMP_STATION = "itest";

    /**
     * A SatDump station source for the prompt 049 ITs (no new context: it joins this one), with the real
     * places and live-image beans.
     */
    @Bean
    public ai.foresignal.source.adapters.satdump.SatDumpAdapter satDumpTestStation(
            org.springframework.beans.factory.ObjectProvider<ai.foresignal.source.spi.PlacePoints> places,
            org.springframework.beans.factory.ObjectProvider<ai.foresignal.source.spi.LiveImageHolder> liveImages) {
        return new ai.foresignal.source.adapters.satdump.SatDumpAdapter(SATDUMP_STATION, places::getIfAvailable,
                liveImages::getIfAvailable);
    }
}
