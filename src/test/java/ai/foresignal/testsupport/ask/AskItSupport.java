package ai.foresignal.testsupport.ask;

import ai.foresignal.llm.LlmHealthTracker;
import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ToolResult;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ask.AskTestSources.ScriptedLlmClients;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared fixtures of the Ask integration tests (prompt 028): scripted fake providers created through
 * the real {@link LlmProviderService} and assigned through {@link LlmSettingsService}, tokens of each
 * scope, records seeded only through {@link TestRecords}, alerts through the trigger engine, and helpers
 * that read what the fake provider was sent. No test calls a real model.
 */
public abstract class AskItSupport {

    /** The test clock's instant. */
    public static final Instant T = Instant.parse("2026-10-12T12:00:00Z");

    /** The non-local chat provider (openai-compatible). */
    public static final String CHAT = "chat";

    /** The local chat provider (ollama). */
    public static final String LOCAL = "local";

    protected static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired protected TestRecords records;
    @Autowired protected LlmProviderService providers;
    @Autowired protected LlmSettingsService llmSettings;
    @Autowired protected LlmHealthTracker health;
    @Autowired protected AppSettingService settings;
    @Autowired protected ScriptedLlmClients fakes;
    @Autowired protected MutableClock clock;
    @Autowired protected TokenService tokens;
    @Autowired protected AdminTokenService adminTokens;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected TriggerRuleService rules;
    @Autowired protected TriggerEngine engine;
    @Autowired protected ChangeBus changeBus;
    @Autowired protected AlertStore alertStore;
    @Autowired protected AlertBus alertBus;

    protected String reader;
    protected String agent;
    protected String otherAgent;
    /** An agent token with the 039 private grant ({@code privateAccess}). */
    protected String privateAgent;
    protected String admin;
    protected TriggerFixture fixture;

    /** Fresh settings, fakes, clock and tokens; the non-local provider assigned for chat. */
    @BeforeEach
    void setUpAsk() {
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        fakes.reset();
        clock.setInstant(T);
        reader = TestTokens.bearer(TestTokens.create(tokens, "ask-reader", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "ask-agent", Scope.AGENT));
        otherAgent = TestTokens.bearer(TestTokens.create(tokens, "ask-other", Scope.AGENT));
        privateAgent = TestTokens.bearer(TestTokens.create(tokens, "ask-private-agent", Scope.AGENT, true));
        admin = "Bearer " + adminTokens.rotate();
        fixture = new TriggerFixture(rules, engine, changeBus, alertStore, alertBus);
        provider(CHAT, "openai-compatible", 1_000_000L);
        assign(CHAT);
    }

    /** Closes the fixture and restores the security setting. */
    @AfterEach
    void tearDownAsk() {
        fixture.close();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    /**
     * Creates a chat provider: {@code openai-compatible} (not local) or {@code ollama} (local, on a loopback
     * literal so prompt 039's LAN classification holds without DNS).
     */
    protected void provider(String id, String type, long tokensPerDay) {
        String baseUrl = "ollama".equals(type) ? ",\"baseUrl\":\"http://127.0.0.1:11434\""
                : ",\"baseUrl\":\"http://llm.test\"";
        providers.create(JSON.readTree("{\"id\":\"" + id + "\",\"type\":\"" + type + "\"" + baseUrl
                + ",\"chatModel\":\"fake-chat\",\"requestsPerMinute\":1000,\"tokensPerDay\":" + tokensPerDay + "}"));
    }

    /** Assigns {@code id} as the chat provider with Ask enabled. */
    protected void assign(String id) {
        assign(id, false);
    }

    /** Assigns {@code id} as the chat provider with Ask enabled and {@code allowPrivateLocal} (prompt 039). */
    protected void assign(String id, boolean allowPrivateLocal) {
        llmSettings.update(new LlmSettings(id, null, true, true, false, 200_000, allowPrivateLocal));
    }

    /** The fake of the assigned non-local provider. */
    protected FakeLlmClient chat() {
        return fakes.client(CHAT);
    }

    /** A {@value AskTestSources#QUAKES} record. */
    protected void quake(String externalId, String place, double mag, double lon, double lat) {
        records.ingest(AskTestSources.QUAKES, NormalizedRecord.builder(externalId, AskTestSources.QUAKE_KIND)
                .observedAt(clock.instant()).put("place", place).put("mag", mag).point(lon, lat).build());
    }

    /** A {@value AskTestSources#SECRET} record. */
    protected void secret(String externalId, String place, double lon, double lat) {
        records.ingest(AskTestSources.SECRET, NormalizedRecord.builder(externalId, AskTestSources.QUAKE_KIND)
                .observedAt(clock.instant()).put("place", place).put("mag", 7.0).point(lon, lat).build());
    }

    /** The persisted head seq of a record. */
    protected long headSeq(String sourceId, String externalId) {
        Long seq = jdbc.queryForObject("SELECT seq FROM record_head WHERE source_id = ? AND external_id = ?", Long.class,
                sourceId, externalId);
        return seq == null ? -1 : seq;
    }

    /** A watch on {@value AskTestSources#QUAKES} for magnitude ≥ {@code mag}; returns its id. */
    protected UUID watch(String name, double mag) {
        return fixture.rule("{'name':'" + name + "','sources':['" + AskTestSources.QUAKES + "'],"
                + "'conditions':[{'path':'mag','op':'>=','value':" + mag + "}]}");
    }

    /** The newest alert of {@code rule} once the engine has caught up. */
    protected AlertView alertOf(UUID rule) {
        fixture.settle();
        List<AlertView> list = fixture.alerts(rule);
        return list.getLast();
    }

    /** Every tool result the fake provider received, in order. */
    protected static List<ToolResult> toolResults(ChatRequest request) {
        List<ToolResult> out = new ArrayList<>();
        for (ChatMessage message : request.messages()) {
            if (message instanceof ChatMessage.ToolResults results) {
                out.addAll(results.results());
            }
        }
        return out;
    }

    /** The {@code data} of the last tool result in {@code request}. */
    protected static JsonNode lastData(ChatRequest request) {
        List<ToolResult> results = toolResults(request);
        return JSON.readTree(results.getLast().content()).get("data");
    }

    /** Every text sent to the provider over every request: system, turns and tool results. */
    protected static String everythingSent(List<ChatRequest> requests) {
        StringBuilder out = new StringBuilder();
        for (ChatRequest request : requests) {
            out.append(request.system()).append('\n');
            for (ChatMessage message : request.messages()) {
                switch (message) {
                    case ChatMessage.User user -> out.append(user.text()).append('\n');
                    case ChatMessage.Assistant assistant -> out.append(assistant.text()).append('\n');
                    case ChatMessage.ToolResults results -> results.results()
                            .forEach(r -> out.append(r.content()).append('\n'));
                }
            }
        }
        return out.toString();
    }
}
