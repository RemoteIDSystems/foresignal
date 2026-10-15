package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.entity.EntityLinkRepository;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** The entity API (prompt 098): search, resolve, records across sources, admin keys and merges, scopes. */
@IntegrationTest
@Import(TestIngestSources.class)
class EntityControllerIT {

    private static final List<String> SOURCES = List.of("it-ec-a", "it-ec-b", "it-ec-c");
    private static final Instant NOW = Instant.parse("2026-10-15T00:00:00Z");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SourceRegistry registry;

    @Autowired
    private IngestQueue queue;

    @Autowired
    private MutableClock clock;

    @Autowired
    private EntityLinkRepository links;

    @Autowired
    private AdminTokenService adminTokens;

    @Autowired
    private TokenService tokens;

    private String admin;
    private String reader;

    @BeforeEach
    void setUp() {
        admin = "Bearer " + adminTokens.rotate();
        reader = TestTokens.bearer(TestTokens.create(tokens, "entity-reader", Scope.READ));
        clock.setInstant(NOW);
        for (String id : SOURCES) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
            registry.registerDynamic(SourceDescriptor.builder(id)
                    .name("Entity controller test " + id)
                    .kind("aircraft")
                    .mode(Mode.PUSH)
                    .defaultBudget(Budget.of(60, 10_000))
                    .entityKey("icao24", "hex")
                    .build());
        }
    }

    @AfterEach
    void tearDown() {
        for (String id : SOURCES) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    @Test
    void searchResolveAndRecordsAcrossSources() throws Exception {
        TestRecords records = new TestRecords(queue, clock);
        for (String id : SOURCES) {
            records.ingest(id, NormalizedRecord.builder("x1", "aircraft").observedAt(NOW)
                    .put("hex", "ae1234").point(0, 0).build());
        }
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen("it-ec-a", "x1")).isPresent());
        String entityId = links.findOpen("it-ec-a", "x1").orElseThrow().getEntityId();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpenByEntityId(entityId)).hasSize(3));

        mvc.perform(get("/v1/entities").param("key", "icao24:ae1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(entityId));

        mvc.perform(get("/v1/entities/resolve").param("key", "icao24:ae1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entityId));

        mvc.perform(get("/v1/entities/{id}", entityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].keyType").value("icao24"))
                .andExpect(jsonPath("$.openLinkCounts['it-ec-a']").value(1));

        mvc.perform(get("/v1/entities/{id}/records", entityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3));
    }

    @Test
    void resolveAnUnknownKeyIs404() throws Exception {
        mvc.perform(get("/v1/entities/resolve").param("key", "icao24:ffffff"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void adminKeyAssertionAndRetraction() throws Exception {
        TestRecords records = new TestRecords(queue, clock);
        records.ingest("it-ec-a", NormalizedRecord.builder("k1", "aircraft").observedAt(NOW)
                .put("hex", "ae1234").point(0, 0).build());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen("it-ec-a", "k1")).isPresent());
        String entityId = links.findOpen("it-ec-a", "k1").orElseThrow().getEntityId();

        String body = mvc.perform(post("/v1/entities/{id}/keys", entityId)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyType\":\"registration\",\"keyValue\":\"N-123AB\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keyValue").value("N123AB"))
                .andExpect(jsonPath("$.origin").value("OPERATOR"))
                .andReturn().getResponse().getContentAsString();
        long keyId = com.jayway.jsonpath.JsonPath.<Number>read(body, "$.id").longValue();

        mvc.perform(delete("/v1/entities/{id}/keys/{keyId}", entityId, keyId)
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retracted").value(true));
    }

    @Test
    void aReadTokenCannotAssertAKey() throws Exception {
        TestRecords records = new TestRecords(queue, clock);
        records.ingest("it-ec-a", NormalizedRecord.builder("s1", "aircraft").observedAt(NOW)
                .put("hex", "ae1234").point(0, 0).build());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen("it-ec-a", "s1")).isPresent());
        String entityId = links.findOpen("it-ec-a", "s1").orElseThrow().getEntityId();

        mvc.perform(post("/v1/entities/{id}/keys", entityId)
                        .header("Authorization", reader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyType\":\"registration\",\"keyValue\":\"N-123AB\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void mergeMovesTheOtherEntitysKeys() throws Exception {
        TestRecords records = new TestRecords(queue, clock);
        records.ingest("it-ec-a", NormalizedRecord.builder("m1", "aircraft").observedAt(NOW)
                .put("hex", "ae1234").point(0, 0).build());
        records.ingest("it-ec-b", NormalizedRecord.builder("m2", "aircraft").observedAt(NOW)
                .put("hex", "ae9999").point(0, 0).build());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen("it-ec-a", "m1")).isPresent());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen("it-ec-b", "m2")).isPresent());
        String target = links.findOpen("it-ec-a", "m1").orElseThrow().getEntityId();
        String other = links.findOpen("it-ec-b", "m2").orElseThrow().getEntityId();

        mvc.perform(post("/v1/entities/{id}/merge", target)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otherId\":\"" + other + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(target));

        // A merged id returns the canonical entity with requestedId set.
        mvc.perform(get("/v1/entities/{id}", other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(target))
                .andExpect(jsonPath("$.requestedId").value(other));
    }
}
