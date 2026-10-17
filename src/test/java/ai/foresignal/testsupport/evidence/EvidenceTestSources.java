package ai.foresignal.testsupport.evidence;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Push sources for the prompt 116 evidence tests, each declaring its originator (so the tests never depend on
 * the real adapters' validation): {@value #USGS} (earthquakes, {@code usgs-anss}), {@value #EMSC} (earthquakes,
 * {@code emsc}), {@value #GDACS} (disasters, {@code gdacs}, which aggregates USGS) and {@value #GDELT} (conflict
 * events, {@code gdelt}, citing their outlet through {@code sourceUrl}).
 */
@TestConfiguration
@Import(TestIngestSources.class)
public class EvidenceTestSources {

    /** Earthquakes originated by {@code usgs-anss}. */
    public static final String USGS = "test-ev-usgs";

    /** Earthquakes originated by {@code emsc}. */
    public static final String EMSC = "test-ev-emsc";

    /** Disasters republished by {@code gdacs}. */
    public static final String GDACS = "test-ev-gdacs";

    /** Conflict events machine-coded by {@code gdelt}. */
    public static final String GDELT = "test-ev-gdelt";

    /** {@value #USGS}. */
    @Bean
    public TestIngestSources.TestPushAdapter evidenceUsgs() {
        return new TestIngestSources.TestPushAdapter(base(USGS, "earthquake").originator("usgs-anss").build());
    }

    /** {@value #EMSC}. */
    @Bean
    public TestIngestSources.TestPushAdapter evidenceEmsc() {
        return new TestIngestSources.TestPushAdapter(base(EMSC, "earthquake").originator("emsc").build());
    }

    /** {@value #GDACS}. */
    @Bean
    public TestIngestSources.TestPushAdapter evidenceGdacs() {
        return new TestIngestSources.TestPushAdapter(base(GDACS, "disaster").originator("gdacs")
                .upstreamRefFields("eventId@gdacs-event").build());
    }

    /** {@value #GDELT}. */
    @Bean
    public TestIngestSources.TestPushAdapter evidenceGdelt() {
        return new TestIngestSources.TestPushAdapter(base(GDELT, "conflict-event").originator("gdelt")
                .upstreamRefFields("cite:sourceUrl").build());
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id).name("Evidence test source " + id).kind(kind).mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }
}
