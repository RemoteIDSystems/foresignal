package ai.foresignal.testsupport.enrichment;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.ask.CitationVerifier;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.search.SearchService;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.enrichment.EnrichmentTestSources.TestWeatherContributor;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.EnrichmentContextBuilder;
import ai.foresignal.trigger.EnrichmentContextContributor;
import ai.foresignal.trigger.EnrichmentProperties;
import ai.foresignal.trigger.EnrichmentService;
import ai.foresignal.trigger.EnrichmentWorker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Shared fixtures of the enrichment integration tests (prompt 030), on top of {@link AskItSupport}
 * (the scripted fake providers with {@value AskItSupport#CHAT} assigned, records through
 * {@code TestRecords}, alerts through the trigger engine): an anchor point in Tokyo Bay, quake and
 * neighbor helpers that ingest at explicit times, enrich rules, destinations, metric readers and
 * stand-in channels. The enrichment worker is not running unless a test starts it; it is stopped
 * after every test. No model is called and no notification leaves the test.
 */
public abstract class EnrichmentItSupport extends AskItSupport {

    /** The anchor longitude (Tokyo Bay). */
    public static final double LON = 139.80;

    /** The anchor latitude. */
    public static final double LAT = 35.60;

    @Autowired protected EnrichmentService enrichment;
    @Autowired protected EnrichmentContextBuilder contexts;
    @Autowired protected EnrichmentWorker worker;
    @Autowired protected EnrichmentProperties enrichmentProperties;
    @Autowired protected TestWeatherContributor weather;
    @Autowired protected ObjectProvider<EnrichmentContextContributor> contributors;
    @Autowired protected ChannelService channels;
    @Autowired protected NotificationDeliveryRepository deliveries;
    @Autowired protected ChannelRateLimiter limiter;
    @Autowired protected DeliveryWorker deliveryWorker;
    @Autowired protected MeterRegistry meters;
    @Autowired protected AlertService alertService;
    @Autowired protected EgressPolicy egress;
    @Autowired protected RecordQueryService recordQueries;
    @Autowired protected RecordMapper recordMapper;
    @Autowired protected McpOutputShaper shaper;
    @Autowired protected SearchService search;
    @Autowired protected SearchStatusService searchStatus;
    @Autowired protected NamedParameterJdbcTemplate namedJdbc;
    @Autowired protected TriggerRuleRepository ruleRepository;
    @Autowired protected CitationVerifier verifier;
    @Autowired protected LlmService llm;

    protected NotifyFixture notify;

    /** The contributor off, channels' rate buckets reset, the worker stopped. */
    @BeforeEach
    void setUpEnrichment() {
        weather.reset();
        worker.stop();
        notify = new NotifyFixture(channels, deliveries, limiter);
    }

    /** Stops the worker and switches the contributor off. */
    @AfterEach
    void tearDownEnrichment() {
        worker.stop();
        weather.reset();
    }

    /** {@code T} plus {@code seconds} (negative for the past). */
    protected static Instant t(long seconds) {
        return T.plusSeconds(seconds);
    }

    /** {@code T} minus {@code minutes}. */
    protected static Instant minutesAgo(long minutes) {
        return T.minus(Duration.ofMinutes(minutes));
    }

    /**
     * An enrich rule on {@value AskTestSources#QUAKES} for magnitude ≥ 5.
     *
     * @param radiusKm {@code enrichRadiusKm}
     * @param window {@code enrichWindow}, for example {@code 10m}
     */
    protected UUID enrichRule(String name, double radiusKm, String window) {
        return fixture.rule("{'name':'" + name + "','sources':['" + AskTestSources.QUAKES + "'],"
                + "'conditions':[{'path':'mag','op':'>=','value':5}],'enrich':true,'enrichRadiusKm':" + radiusKm
                + ",'enrichWindow':'" + window + "'}");
    }

    /** A rule like {@link #enrichRule} without {@code enrich}. */
    protected UUID plainRule(String name) {
        return fixture.rule("{'name':'" + name + "','sources':['" + AskTestSources.QUAKES + "'],"
                + "'conditions':[{'path':'mag','op':'>=','value':5}]}");
    }

    /** A {@value AskTestSources#QUAKES} record observed (and received) at {@code at}; no geometry when lon is null. */
    protected void quakeAt(String externalId, String place, double mag, Instant at, Double lon, Double lat) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, AskTestSources.QUAKE_KIND).observedAt(at)
                .put("place", place).put("mag", mag);
        if (lon != null) {
            b.point(lon, lat);
        }
        records.ingestAt(AskTestSources.QUAKES, at, b.build());
    }

    /** A record of {@code sourceId} and {@code kind} with {@code fields}, observed at {@code at} at a point. */
    protected void recordAt(String sourceId, String kind, String externalId, Instant at, double lon, double lat,
            Map<String, Object> fields) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, kind).observedAt(at).point(lon, lat);
        fields.forEach(b::put);
        records.ingestAt(sourceId, at, b.build());
    }

    /** The alert of {@code rule} on entity {@code externalId}, once the engine has caught up. */
    protected AlertView alertFor(UUID rule, String externalId) {
        return fixture.alerts(rule).stream().filter(a -> a.externalId().equals(externalId)).reduce((a, b) -> b)
                .orElseThrow(() -> new AssertionError("no alert of " + rule + " for " + externalId));
    }

    /** The non-local chat destination. */
    protected static EgressPolicy.LlmDestination remote() {
        return new EgressPolicy.LlmDestination(CHAT, "openai-compatible", false);
    }

    /** A local destination (Ollama). */
    protected static EgressPolicy.LlmDestination local() {
        return new EgressPolicy.LlmDestination(LOCAL, "ollama", true);
    }

    /** Turns {@code llm.allowPrivateLocal} (prompt 039) on or off, keeping the other LLM settings. */
    protected void allowPrivateLocal(boolean on) {
        LlmSettings s = llmSettings.get();
        llmSettings.update(new LlmSettings(s.chatProvider(), s.embeddingProvider(), s.askEnabled(),
                s.enrichmentEnabled(), s.logContent(), s.askMaxTokens(), on));
    }

    /** Settings with enrichment {@code enabled} and {@code chatProvider} (null for none). */
    protected void enrichmentSettings(String chatProvider, boolean enabled) {
        llmSettings.update(new LlmSettings(chatProvider, null, true, enabled, false, 200_000, false));
    }

    /** The count of {@code metric} with tag {@code reason}, 0 when never counted. */
    protected double count(String metric, String reason) {
        Counter c = reason == null ? meters.find(metric).counter() : meters.find(metric).tag("reason", reason).counter();
        return c == null ? 0 : c.count();
    }

    /** A Slack stand-in channel for alerts that waits {@code waitSeconds} for the summary. */
    protected UUID slackChannel(String name, String webhookUrl, int waitSeconds) {
        return notify.channel("{'name':'" + name + "','type':'slack','events':['alert'],'waitForEnrichmentSeconds':"
                + waitSeconds + ",'secrets':{'webhookUrl':'" + webhookUrl + "'}}");
    }

    /** A Discord stand-in channel for alerts (edit in place on, no wait). */
    protected UUID discordChannel(String name, String webhookUrl) {
        return notify.channel("{'name':'" + name + "','type':'discord','events':['alert'],'secrets':{'webhookUrl':'"
                + webhookUrl + "'}}");
    }
}
