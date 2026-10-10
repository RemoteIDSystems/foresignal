package ai.elaro.testsupport.adapters;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Registers the fake adapters in a test context.
 *
 * <p>Imported by any test that needs a registry with known contents. Prompts 005–008 reuse this, so
 * the collector, budget, health and ingest tests all run against the same five sources.
 *
 * <p>A {@code @TestConfiguration} rather than {@code @Component}s: the fakes live under
 * {@code ai.elaro.testsupport}, which component scanning would otherwise pick up and add to
 * <em>every</em> integration test's registry, including ones asserting on the real source list.
 */
@TestConfiguration
public class FakeAdapters {

    /** The polling source most tests use. */
    public static final String POLLING_ID = "fake-polling";

    /** The streaming source. */
    public static final String STREAMING_ID = "fake-streaming";

    /** The push source. */
    public static final String PUSH_ID = "fake-push";

    @Bean
    public FakePollingAdapter fakePollingAdapter() {
        return FakePollingAdapter.withId(POLLING_ID);
    }

    @Bean
    public FakeStreamingAdapter fakeStreamingAdapter() {
        return FakeStreamingAdapter.withId(STREAMING_ID);
    }

    @Bean
    public FakePushAdapter fakePushAdapter() {
        return FakePushAdapter.withId(PUSH_ID);
    }

    @Bean
    public FakeFamily fakeFamily() {
        return new FakeFamily();
    }
}
