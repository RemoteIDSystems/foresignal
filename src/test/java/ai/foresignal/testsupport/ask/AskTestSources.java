package ai.foresignal.testsupport.ask;

import ai.foresignal.llm.LlmClientFactory;
import ai.foresignal.llm.LlmHttp;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Ask test context (prompt 028): the trigger and records test sources (watches, alerts, the
 * {@code test-private} source, {@code MutableClock}, {@code TestRecords}) plus
 *
 * <ul>
 *   <li>{@value #QUAKES}: kind {@value #QUAKE_KIND}, title field {@code place}, search field
 *       {@code place}, number field {@code mag};
 *   <li>{@value #SECRET}: private, kind {@value #QUAKE_KIND}, search field {@code place};
 *   <li>{@value #NOTICED}: kind {@value #QUAKE_KIND}, title and search field {@code place}, with the
 *       {@code llmNotice} {@value #NOTICE} (prompt 090);
 *   <li>{@link ScriptedLlmClients} as the primary {@link LlmClientFactory}: every provider answers with
 *       one {@link FakeLlmClient} per provider id that tests script and inspect. {@code LlmService}
 *       still wraps it, so budgets, usage and health apply as in production. No model is called.
 * </ul>
 *
 * <p>Every Ask integration test imports this one configuration, so they share one context per web
 * environment.
 */
@TestConfiguration
@Import(TriggerTestSources.class)
public class AskTestSources {

    /** A public quake source. */
    public static final String QUAKES = "test-ask-quakes";

    /** A private quake source. */
    public static final String SECRET = "test-ask-secret";

    /** A public source carrying an {@code llmNotice} (prompt 090). */
    public static final String NOTICED = "test-ask-noticed";

    /** The {@code llmNotice} of {@value #NOTICED}. */
    public static final String NOTICE = "Test notice: must not be used to determine eligibility.";

    /** The kind of all three. */
    public static final String QUAKE_KIND = "test-ask-quake";

    /** {@value #QUAKES}. */
    @Bean
    public TestPushAdapter testAskQuakesAdapter() {
        return new TestPushAdapter(base(QUAKES)
                .titleField("place")
                .searchFields("place")
                .fieldSpec("mag", new FieldSpec(FieldType.NUMBER, "1", "Magnitude", null, null, Set.of()))
                .build());
    }

    /** {@value #SECRET}. */
    @Bean
    public TestPushAdapter testAskSecretAdapter() {
        return new TestPushAdapter(base(SECRET).titleField("place").searchFields("place").privateSource(true).build());
    }

    /** {@value #NOTICED}. */
    @Bean
    public TestPushAdapter testAskNoticedAdapter() {
        return new TestPushAdapter(base(NOTICED).titleField("place").searchFields("place").llmNotice(NOTICE).build());
    }

    /** The scripted provider clients. */
    @Bean
    @Primary
    public ScriptedLlmClients scriptedLlmClients(LlmHttp http, JsonMapper json) {
        return new ScriptedLlmClients(http, json);
    }

    private static SourceDescriptor.Builder base(String id) {
        return SourceDescriptor.builder(id)
                .name("Ask test source " + id)
                .attribution("Attribution of " + id)
                .kind(QUAKE_KIND)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    /** An {@link LlmClientFactory} whose clients are scripted {@link FakeLlmClient}s, one per provider. */
    public static final class ScriptedLlmClients extends LlmClientFactory {

        private final Map<String, FakeLlmClient> clients = new ConcurrentHashMap<>();

        ScriptedLlmClients(LlmHttp http, JsonMapper json) {
            super(http, json);
        }

        @Override
        public LlmClient client(LlmProviderConfig config) {
            return client(config.id());
        }

        /** The fake of provider {@code providerId}, created on first use. */
        public FakeLlmClient client(String providerId) {
            return clients.computeIfAbsent(providerId, FakeLlmClient::new);
        }

        @Override
        public void evict(String providerId) {
            // the fakes hold their scripts across configuration changes
        }

        /** Forgets every fake and its script. */
        public void reset() {
            clients.clear();
        }
    }
}
