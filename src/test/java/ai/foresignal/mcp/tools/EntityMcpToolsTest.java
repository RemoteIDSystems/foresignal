package ai.foresignal.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.entity.EntityLinkRepository;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The entity MCP tools (prompt 098): {@code get_entity} and {@code find_entity}. */
@IntegrationTest
@Import(TestIngestSources.class)
class EntityMcpToolsTest {

    private static final String ID = "it-mcp-entity";
    private static final Instant NOW = Instant.parse("2026-10-15T00:00:00Z");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private GetEntityTool getEntity;

    @Autowired
    private FindEntityTool findEntity;

    @Autowired
    private SourceRegistry registry;

    @Autowired
    private IngestQueue queue;

    @Autowired
    private MutableClock clock;

    @Autowired
    private EntityLinkRepository links;

    @AfterEach
    void unregister() {
        if (registry.contains(ID)) {
            registry.unregisterDynamic(ID);
        }
    }

    @Test
    void getEntityReturnsKeysAndCappedRecords() throws Exception {
        String entityId = seed();
        ToolContext context = context();

        ToolResult result = getEntity.call(context, JSON.readTree("{\"id\":\"" + entityId + "\"}"));
        JsonNode structured = result.structured();
        assertThat(structured.get("id").asString()).isEqualTo(entityId);
        assertThat(structured.get("recordsTotal").asInt()).isEqualTo(1);
        assertThat(structured.get("keys").get(0).get("keyType").asString()).isEqualTo("icao24");
    }

    @Test
    void findEntityResolvesByKey() throws Exception {
        seed();
        ToolResult result = findEntity.call(context(), JSON.readTree("{\"key\":\"icao24:ae1234\"}"));
        assertThat(result.structured().get("items").get(0).get("type").asString()).isEqualTo("aircraft");
    }

    @Test
    void toolMetadata() {
        assertThat(getEntity.name()).isEqualTo("get_entity");
        assertThat(findEntity.name()).isEqualTo("find_entity");
        assertThat(getEntity.requiredScope()).isEqualTo(Scope.READ);
        assertThat(findEntity.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getEntity.inputSchema().toString()).contains("key");
        assertThat(findEntity.inputSchema().toString()).contains("limit");
    }

    private String seed() {
        if (registry.contains(ID)) {
            registry.unregisterDynamic(ID);
        }
        registry.registerDynamic(SourceDescriptor.builder(ID)
                .name("Entity MCP test")
                .kind("aircraft")
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000))
                .entityKey("icao24", "hex")
                .build());
        clock.setInstant(NOW);
        new TestRecords(queue, clock).ingest(ID, NormalizedRecord.builder("a1", "aircraft").observedAt(NOW)
                .put("hex", "ae1234").point(0, 0).build());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(ID, "a1")).isPresent());
        return links.findOpen(ID, "a1").orElseThrow().getEntityId();
    }

    private static ToolContext context() {
        return new ToolContext(
                new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1"), "s", "t");
    }
}
