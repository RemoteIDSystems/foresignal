package ai.foresignal.testsupport.collect;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The collector test sources, on top of {@link TestIngestSources} (which brings prompt 004's fakes
 * and the {@code MutableClock}).
 *
 * <ul>
 *   <li>{@value #POLL}: polling, 1 s interval, 2 s fetch timeout, a license, ALL;
 *   <li>{@value #KEYED}: polling with a required {@code apiKey} credential, 2 s fetch timeout.
 * </ul>
 *
 * <p>Both are {@link ControllablePollingAdapter}s, so a test can block a fetch, fail it, or call a
 * stubbed upstream through {@code ctx.http()}.
 */
@TestConfiguration
@Import(TestIngestSources.class)
public class CollectorTestSources {

    public static final String POLL = "collect-poll";

    public static final String KEYED = "collect-keyed";

    public static final String KIND = "fake-record";

    public static final String LICENCE = "CC-BY-4.0";

    @Bean
    public ControllablePollingAdapter collectPollAdapter() {
        return new ControllablePollingAdapter(base(POLL).build());
    }

    @Bean
    public ControllablePollingAdapter collectKeyedAdapter() {
        return new ControllablePollingAdapter(
                base(KEYED).credential(CredentialField.required("apiKey", "The upstream key")).build());
    }

    private static SourceDescriptor.Builder base(String id) {
        return FakePollingAdapter.defaultDescriptor(id)
                .interval(Duration.ofSeconds(1), Duration.ofSeconds(1))
                .fetchTimeout(Duration.ofSeconds(2))
                .licence(LICENCE)
                .defaultBudget(Budget.of(600, 100_000));
    }

    /** A simple valid record of {@value #KIND} observed at {@code observedAt}. */
    public static NormalizedRecord record(String externalId, Instant observedAt) {
        return NormalizedRecord.builder(externalId, KIND)
                .observedAt(observedAt)
                .point(-79.38, 43.65)
                .put("name", externalId)
                .build();
    }
}
