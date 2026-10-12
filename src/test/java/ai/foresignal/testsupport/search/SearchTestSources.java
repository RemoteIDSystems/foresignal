package ai.foresignal.testsupport.search;

import ai.foresignal.llm.LlmClientFactory;
import ai.foresignal.llm.LlmHttp;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.llm.client.EmbeddingResponse;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.LlmModelInfo;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

/**
 * The search test context (prompt 027): the read-API sources of {@link RecordsTestSources} (ingest
 * sources, {@code MutableClock}, {@code TestRecords}) plus
 *
 * <ul>
 *   <li>{@value #NEWS}: embeds {@code headline} and {@code summary}; title field {@code headline};
 *       significant {@code headline}, {@code summary} and {@code severity};
 *   <li>{@value #BULLETIN}: a second embeddable source of kind {@value #BULLETIN_KIND}, embedding {@code body};
 *   <li>{@value #OPT_OUT}: embeds {@code headline}, for the {@code embed=false} tests;
 *   <li>{@value #PRIVATE_EMBED}: private, and declares {@code embedFields} (never embedded);
 *   <li>{@value #PLAIN}: no {@code embedFields}, search fields {@code name}; only text search finds it;
 *   <li>{@link FakeEmbeddingClients} as the primary {@link LlmClientFactory}: every provider answers
 *       with a {@link FakeLlmClient} (deterministic vectors of the requested width), counts the texts
 *       it embeds per model id, and can be made to fail.
 * </ul>
 *
 * <p>Every search integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(RecordsTestSources.class)
public class SearchTestSources {

    /** Embeds headline and summary. */
    public static final String NEWS = "test-search-news";

    /** The kind of {@value #NEWS}. */
    public static final String NEWS_KIND = "test-news";

    /** Embeds body. */
    public static final String BULLETIN = "test-search-bulletin";

    /** The kind of {@value #BULLETIN}. */
    public static final String BULLETIN_KIND = "test-bulletin";

    /** Embeds headline; tests turn {@code embed} off. */
    public static final String OPT_OUT = "test-search-optout";

    /** Private, with embedFields. */
    public static final String PRIVATE_EMBED = "test-search-private";

    /** No embedFields. */
    public static final String PLAIN = "test-search-plain";

    /** The kind of {@value #PLAIN}. */
    public static final String PLAIN_KIND = "test-plain";

    /** {@value #NEWS}. */
    @Bean
    public TestPushAdapter testSearchNewsAdapter() {
        return new TestPushAdapter(base(NEWS, NEWS_KIND)
                .titleField("headline")
                .searchFields("headline")
                .embedFields("headline", "summary")
                .significantFields("headline", "summary", "severity")
                .build());
    }

    /** {@value #BULLETIN}. */
    @Bean
    public TestPushAdapter testSearchBulletinAdapter() {
        return new TestPushAdapter(base(BULLETIN, BULLETIN_KIND).embedFields("body").build());
    }

    /** {@value #OPT_OUT}. */
    @Bean
    public TestPushAdapter testSearchOptOutAdapter() {
        return new TestPushAdapter(base(OPT_OUT, NEWS_KIND).embedFields("headline").build());
    }

    /** {@value #PRIVATE_EMBED}. */
    @Bean
    public TestPushAdapter testSearchPrivateAdapter() {
        return new TestPushAdapter(base(PRIVATE_EMBED, NEWS_KIND).embedFields("headline").privateSource(true).build());
    }

    /** {@value #PLAIN}. */
    @Bean
    public TestPushAdapter testSearchPlainAdapter() {
        return new TestPushAdapter(base(PLAIN, PLAIN_KIND).searchFields("name").build());
    }

    /**
     * DNS without the network (checkpoint 041): the search ITs' providers keep the default Ollama base URL, whose
     * host the outbound URL policy would otherwise look up in real DNS.
     */
    @Bean
    @Primary
    public ai.foresignal.net.HostResolver offlineHostResolver() {
        return ai.foresignal.testsupport.net.TestOutbound.offlineResolver();
    }

    /** The fake provider clients. */
    @Bean
    @Primary
    public FakeEmbeddingClients fakeEmbeddingClients(LlmHttp http, JsonMapper json) {
        return new FakeEmbeddingClients(http, json);
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Search test source " + id)
                .attribution("Attribution of " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    /**
     * An {@link LlmClientFactory} whose clients are {@link FakeLlmClient}s. {@code LlmService} still
     * wraps them, so budgets, usage and health apply as in production.
     */
    public static final class FakeEmbeddingClients extends LlmClientFactory {

        private final Map<String, AtomicLong> textsByModel = new ConcurrentHashMap<>();
        private final Set<String> failing = ConcurrentHashMap.newKeySet();

        FakeEmbeddingClients(LlmHttp http, JsonMapper json) {
            super(http, json);
        }

        @Override
        public LlmClient client(LlmProviderConfig config) {
            return new Counting(config.id(), config.embeddingDimensions() == null ? 8 : config.embeddingDimensions());
        }

        @Override
        public void evict(String providerId) {
            // nothing cached
        }

        /** Makes every call to {@code providerId} fail with a connection error. */
        public void fail(String providerId) {
            failing.add(providerId);
        }

        /** Texts embedded with {@code modelId} so far. */
        public long texts(String modelId) {
            AtomicLong n = textsByModel.get(modelId);
            return n == null ? 0 : n.get();
        }

        /** Forgets counters and failures. */
        public void reset() {
            textsByModel.clear();
            failing.clear();
        }

        private final class Counting implements LlmClient {

            private final FakeLlmClient delegate;

            Counting(String providerId, int dimensions) {
                this.delegate = new FakeLlmClient(providerId, dimensions);
            }

            private void check() {
                if (failing.contains(delegate.providerId())) {
                    throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, delegate.providerId(),
                            "fake provider " + delegate.providerId() + " is down");
                }
            }

            @Override
            public String providerId() {
                return delegate.providerId();
            }

            @Override
            public ChatResponse chat(ChatRequest request) {
                check();
                return delegate.chat(request);
            }

            @Override
            public EmbeddingResponse embed(List<String> texts) {
                check();
                return delegate.embed(texts);
            }

            @Override
            public EmbeddingResponse embed(List<String> texts, EmbeddingModelSpec spec) {
                check();
                textsByModel.computeIfAbsent(spec.modelId(), k -> new AtomicLong()).addAndGet(texts.size());
                return delegate.embed(texts, spec);
            }

            @Override
            public List<LlmModelInfo> models() {
                return delegate.models();
            }
        }
    }
}
