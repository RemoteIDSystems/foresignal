package ai.foresignal.testsupport.briefing;

import ai.foresignal.briefing.BriefingRepository;
import ai.foresignal.briefing.BriefingRunRepository;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

/**
 * Shared fixtures of the briefing integration tests (prompt 029), on top of {@link AskItSupport}
 * (tokens, the scripted fake providers, records through {@code TestRecords}, alerts through the
 * trigger engine): no chat provider assigned by default, a reset retention stub, quake helpers for
 * {@link BriefingTestSources}, briefing bodies, and stand-in channels. No model is called and no
 * notification leaves the test.
 */
public abstract class BriefingItSupport extends AskItSupport {

    @Autowired protected BriefingService briefings;
    @Autowired protected BriefingRepository briefingRepository;
    @Autowired protected BriefingRunRepository runRepository;
    @Autowired protected StubRetentionState retentionState;
    @Autowired protected ChannelService channels;
    @Autowired protected NotificationDeliveryRepository deliveries;
    @Autowired protected ChannelRateLimiter limiter;

    protected NotifyFixture notify;

    /** No chat provider (digest only) unless a test assigns one; retention overrides cleared. */
    @BeforeEach
    void setUpBriefings() {
        llmSettings.update(LlmSettings.DEFAULTS);
        retentionState.reset();
        notify = new NotifyFixture(channels, deliveries, limiter);
    }

    /** Assigns the non-local fake provider as the chat provider. */
    protected void useChatProvider() {
        assign(CHAT);
    }

    /** A {@value BriefingTestSources#QUAKES} record observed now. */
    protected void quake(String externalId, String place, double mag) {
        records.ingest(BriefingTestSources.QUAKES, NormalizedRecord.builder(externalId, BriefingTestSources.QUAKE_KIND)
                .observedAt(clock.instant()).put("place", place).put("mag", mag).point(139.8, 35.6).build());
    }

    /** A {@value BriefingTestSources#SECRET} record observed now. */
    protected void secretQuake(String externalId, String place, double mag) {
        records.ingest(BriefingTestSources.SECRET, NormalizedRecord.builder(externalId, BriefingTestSources.QUAKE_KIND)
                .observedAt(clock.instant()).put("place", place).put("mag", mag).point(139.8, 35.6).build());
    }

    /** A watch on {@value BriefingTestSources#QUAKES} for magnitude ≥ {@code mag}. */
    protected UUID quakeWatch(String name, double mag, String severity) {
        return fixture.rule("{'name':'" + name + "','sources':['" + BriefingTestSources.QUAKES + "'],'severity':'"
                + severity + "','conditions':[{'path':'mag','op':'>=','value':" + mag + "}]}");
    }

    /** A digest briefing body over the quake sources. */
    protected static String digestBody(String name, String schedule, String extra) {
        return "{\"name\":\"" + name + "\",\"schedule\":\"" + schedule + "\",\"timezone\":\"UTC\","
                + "\"scope\":{\"kinds\":[\"" + BriefingTestSources.QUAKE_KIND + "\"]},\"lookback\":\"24h\""
                + (extra == null ? "" : "," + extra) + "}";
    }

    /** {@code text} as JSON. */
    protected static JsonNode json(String text) {
        return JSON.readTree(text);
    }

    /** A caller with {@code scope} (admin, agent or read) and no private-source exclusions beyond the gate. */
    protected static McpCaller caller(Scope scope, Set<String> excluded) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "test-" + scope.wireValue(), scope, null,
                null, null, false);
        return new McpCaller(token, new ReadCaller(scope, false, true), excluded, "127.0.0.1");
    }

    /** An admin caller. */
    protected static McpCaller admin() {
        return caller(Scope.ADMIN, Set.of());
    }

    /** An agent caller. */
    protected static McpCaller agentCaller() {
        return caller(Scope.AGENT, Set.of());
    }

    /** A Slack stand-in channel subscribed to briefings, posting to {@code webhookUrl}. */
    protected UUID slackChannel(String name, String webhookUrl) {
        return notify.channel("{'name':'" + name + "','type':'slack','events':['briefing'],'secrets':{'webhookUrl':'"
                + webhookUrl + "'}}");
    }

    /** The id of a created briefing response. */
    protected static UUID id(java.util.Map<String, Object> briefing) {
        return UUID.fromString(String.valueOf(briefing.get("id")));
    }
}
