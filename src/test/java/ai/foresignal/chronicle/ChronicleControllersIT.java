package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.AddChronicleLayerTool;
import ai.foresignal.mcp.tools.CreateChronicleTool;
import ai.foresignal.mcp.tools.GetChronicleStateTool;
import ai.foresignal.mcp.tools.GetChronicleThreadTool;
import ai.foresignal.mcp.tools.ListChroniclesTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * The chronicle API (prompt 131) over MockMvc: every operation's status codes and scopes, ETags and {@code If-Match},
 * the Problem codes ({@code UNKNOWN_CHRONICLE}, {@code UNKNOWN_CHRONICLE_LAYER}, {@code CHRONICLE_CAP_EXCEEDED},
 * {@code CHRONICLE_STATE_INVALID}, {@code CHRONICLE_LAYER_INVALID}, {@code CHRONICLE_CONFIRMATION_REQUIRED},
 * {@code CHRONICLE_VERSION_STALE}), and the shapes of the five MCP tools.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleControllersIT extends ChronicleItSupport {

    @Autowired ListChroniclesTool listTool;
    @Autowired GetChronicleThreadTool threadTool;
    @Autowired GetChronicleStateTool stateTool;
    @Autowired CreateChronicleTool createTool;
    @Autowired AddChronicleLayerTool addLayerTool;

    static final String LAYER = "{'name':'Standard','selector':{'type':'records','sources':['"
            + TestIngestSources.STANDARD + "']},'capture':{'mode':'full'}}";

    private static String code(MockHttpServletResponse response) throws Exception {
        return body(response).get("code").asString();
    }

    @Test
    @DisplayName("create, read, patch and delete with scopes, ETags, If-Match and confirmation")
    void crud() throws Exception {
        Person jo = person("jo-api", Scope.AGENT);
        Person reader = person("reader-api", Scope.READ);
        assertThat(call(null, get("/v1/chronicles")).getStatus()).isEqualTo(401);
        assertThat(call(reader.bearer(), json(post("/v1/chronicles"), "{'name':'No'}")).getStatus()).isEqualTo(403);
        MockHttpServletResponse bad = call(jo.bearer(), json(post("/v1/chronicles"), "{'name':'Bad','layers':"
                + "[{'name':'x','selector':{'type':'nope'},'capture':{'mode':'full'}}]}"));
        assertThat(bad.getStatus()).isEqualTo(400);
        assertThat(code(bad)).isEqualTo(ErrorCodes.CHRONICLE_LAYER_INVALID);

        MockHttpServletResponse created = call(jo.bearer(), json(post("/v1/chronicles"),
                "{'name':'Middle East war 2026','description':'Regional monitoring','area':{'regionCodes':['YE','SA']},"
                        + "'layers':[" + LAYER + "]}"));
        assertThat(created.getStatus()).isEqualTo(201);
        assertThat(created.getHeader("Location")).startsWith("/v1/chronicles/");
        JsonNode c = body(created);
        String id = c.get("id").asString();
        assertThat(c.get("displayId").asString()).matches("CHR-\\d{4}-\\d{4,}");
        assertThat(c.get("ref").asString()).isEqualTo("chronicle:" + id);
        assertThat(c.get("status").asString()).isEqualTo("proposed");
        assertThat(c.get("owner").get("handle").asString()).isEqualTo("jo-api");
        assertThat(c.get("visibility").asString()).isEqualTo("private");
        assertThat(c.get("tlp").asString()).isEqualTo("AMBER");
        assertThat(c.get("sizeCapBytes").asLong()).isEqualTo(20L * 1024 * 1024 * 1024);
        assertThat(c.get("layers").get(0).get("estimate").has("bytesPerDay")).isTrue();
        assertThat(c.get("workspaceId").asString()).hasSize(26);
        assertThat(c.get("origin").asString()).isEqualTo("operator");

        MockHttpServletResponse read = call(jo.bearer(), get("/v1/chronicles/" + id));
        assertThat(read.getStatus()).isEqualTo(200);
        String etag = read.getHeader("ETag");
        assertThat(etag).isEqualTo("\"" + c.get("version").asInt() + "\"");
        assertThat(call(reader.bearer(), get("/v1/chronicles/" + id)).getStatus()).isEqualTo(404);
        assertThat(code(call(jo.bearer(), get("/v1/chronicles/01JA8Q3V7S2K9W4T6Y1B5N0M3C"))))
                .isEqualTo(ErrorCodes.UNKNOWN_CHRONICLE);

        MockHttpServletResponse patched = call(jo.bearer(), mergePatch(patch("/v1/chronicles/" + id), "{'name':"
                + "'Renamed','retention':'P365D'}").header("If-Match", etag));
        assertThat(patched.getStatus()).isEqualTo(200);
        assertThat(body(patched).get("name").asString()).isEqualTo("Renamed");
        assertThat(body(patched).get("retention").asString()).isEqualTo("PT8760H");
        MockHttpServletResponse stale = call(jo.bearer(), mergePatch(patch("/v1/chronicles/" + id), "{'name':'X'}")
                .header("If-Match", etag));
        assertThat(stale.getStatus()).isEqualTo(412);
        assertThat(code(stale)).isEqualTo(ErrorCodes.CHRONICLE_VERSION_STALE);

        JsonNode list = body(call(jo.bearer(), get("/v1/chronicles").param("status", "proposed")));
        assertThat(list.get("items").size()).isEqualTo(1);
        assertThat(call(jo.bearer(), get("/v1/chronicles").param("status", "bogus")).getStatus()).isEqualTo(400);

        MockHttpServletResponse noConfirm = call(jo.bearer(), delete("/v1/chronicles/" + id));
        assertThat(noConfirm.getStatus()).isEqualTo(400);
        assertThat(code(noConfirm)).isEqualTo(ErrorCodes.CHRONICLE_CONFIRMATION_REQUIRED);
        assertThat(call(jo.bearer(), delete("/v1/chronicles/" + id).param("confirm", c.get("displayId").asString()))
                .getStatus()).isEqualTo(204);
        assertThat(call(jo.bearer(), get("/v1/chronicles/" + id)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("layers, lifecycle, estimate, thread, replay endpoints and suggestions")
    void layersLifecycleAndReplay() throws Exception {
        Person jo = person("jo-api2", Scope.AGENT);
        String id = body(call(jo.bearer(), json(post("/v1/chronicles"), "{'name':'Lifecycle','layers':[" + LAYER
                + "]}"))).get("id").asString();
        String base = "/v1/chronicles/" + id;

        MockHttpServletResponse layer = call(jo.bearer(), json(post(base + "/layers"), "{'name':'Events','selector':"
                + "{'type':'records','kinds':['" + TestIngestSources.STANDARD_KIND + "']},'capture':"
                + "{'mode':'events-only'}}"));
        assertThat(layer.getStatus()).isEqualTo(201);
        String layerId = body(layer).get("id").asString();
        MockHttpServletResponse badLayer = call(jo.bearer(), json(post(base + "/layers"), "{'name':'Frames',"
                + "'selector':{'type':'records','sources':['" + TestIngestSources.STANDARD + "']},'capture':"
                + "{'mode':'frames','everyNthFrame':2}}"));
        assertThat(code(badLayer)).isEqualTo(ErrorCodes.CHRONICLE_LAYER_INVALID);
        MockHttpServletResponse renamed = call(jo.bearer(), mergePatch(patch(base + "/layers/" + layerId),
                "{'name':'Event versions','capture':{'mode':'downsampled','interval':'PT5M'}}"));
        assertThat(renamed.getStatus()).isEqualTo(200);
        assertThat(body(renamed).get("capture").get("interval").asString()).isEqualTo("PT5M");
        assertThat(code(call(jo.bearer(), mergePatch(patch(base + "/layers/01JA8Q3V7S2K9W4T6Y1B5N0M3C"),
                "{'name':'x'}")))).isEqualTo(ErrorCodes.UNKNOWN_CHRONICLE_LAYER);

        assertThat(code(call(jo.bearer(), post(base + "/pause")))).isEqualTo(ErrorCodes.CHRONICLE_STATE_INVALID);
        MockHttpServletResponse activated = call(jo.bearer(), json(post(base + "/activate"),
                "{'backfill':{'from':'" + T.minusSeconds(3600) + "'}}"));
        assertThat(activated.getStatus()).isEqualTo(200);
        assertThat(body(activated).get("status").asString()).isEqualTo("active");
        standard("api-1", T.plusSeconds(30), 42, 12, 1);
        standard("api-1", T.plusSeconds(90), 42.2, 12.2, 2);
        capture.catchUp();

        JsonNode estimate = body(call(jo.bearer(), get(base + "/estimate")));
        assertThat(estimate.get("layers").size()).isEqualTo(2);
        assertThat(estimate.has("totalBytesPerDay")).isTrue();

        JsonNode thread = body(call(jo.bearer(), get(base + "/thread").param("limit", "2")));
        assertThat(thread.get("items").size()).isEqualTo(2);
        assertThat(thread.get("nextCursor").isNull()).isFalse();
        long newest = eventRows.findByChronicleIdOrderByIdAsc(id).getLast().getId();
        assertThat(call(jo.bearer(), json(post(base + "/thread/read"), "{'upToId':" + newest + "}")).getStatus())
                .isEqualTo(204);
        assertThat(body(call(jo.bearer(), get(base + "/thread"))).get("unreadCount").asInt()).isZero();
        assertThat(call(jo.bearer(), get(base + "/thread").param("types", "nonsense")).getStatus()).isEqualTo(400);

        assertThat(call(jo.bearer(), get(base + "/state")).getStatus()).isEqualTo(400);
        JsonNode state = body(call(jo.bearer(), get(base + "/state").param("at", T.plusSeconds(60).toString())));
        assertThat(state.get("layers").size()).isEqualTo(2);
        MockHttpServletResponse tile = call(jo.bearer(), get(base + "/tiles/4/9/7.mvt")
                .param("at", T.plusSeconds(120).toString()));
        assertThat(tile.getStatus()).isEqualTo(200);
        assertThat(tile.getContentType()).startsWith("application/vnd.mapbox-vector-tile");
        assertThat(tile.getContentAsByteArray()).isNotEmpty();
        JsonNode frames = body(call(jo.bearer(), get(base + "/frames").param("from", T.toString())
                .param("to", T.plusSeconds(180).toString()).param("step", "PT1M")));
        assertThat(frames.get("frames").size()).isEqualTo(3);
        assertThat(call(jo.bearer(), get(base + "/frames").param("step", "soon")).getStatus()).isEqualTo(400);
        JsonNode track = body(call(jo.bearer(), get(base + "/tracks/" + TestIngestSources.STANDARD + "/api-1")));
        assertThat(track.get("type").asString()).isEqualTo("Feature");
        assertThat(call(jo.bearer(), get(base + "/tracks/" + TestIngestSources.STANDARD + "/nobody")).getStatus())
                .isEqualTo(404);
        assertThat(call(jo.bearer(), get(base + "/series")).getStatus()).isEqualTo(400);
        assertThat(body(call(jo.bearer(), get(base + "/series").param("ref", "series:eia-steo/BREPUUS")))
                .get("points").size()).isZero();
        JsonNode summary = body(call(jo.bearer(), get(base + "/summary")));
        assertThat(summary.get("layers").size()).isEqualTo(2);
        assertThat(summary.has("storage")).isTrue();

        MockHttpServletResponse export = call(jo.bearer(), json(post(base + "/export"), "{'format':'geoparquet'}"));
        assertThat(export.getStatus()).isEqualTo(202);
        assertThat(call(jo.bearer(), json(post(base + "/export"), "{'format':'csv'}")).getStatus()).isEqualTo(400);

        assertThat(body(call(jo.bearer(), post(base + "/pause"))).get("status").asString()).isEqualTo("paused");
        assertThat(body(call(jo.bearer(), post(base + "/resume"))).get("status").asString()).isEqualTo("active");
        assertThat(body(call(jo.bearer(), post(base + "/stop"))).get("status").asString()).isEqualTo("stopped");
        MockHttpServletResponse archived = call(jo.bearer(), post(base + "/archive"));
        assertThat(archived.getStatus()).isEqualTo(202);
        assertThat(body(archived).get("status").asString()).isEqualTo("archived");
        MockHttpServletResponse restored = call(jo.bearer(), post(base + "/restore"));
        assertThat(restored.getStatus()).isEqualTo(202);
        assertThat(body(restored).get("status").asString()).isEqualTo("stopped");
        assertThat(call(jo.bearer(), delete(base + "/layers/" + layerId).param("purge", "true")).getStatus())
                .isEqualTo(204);

        assertThat(code(call(jo.bearer(), json(post("/v1/chronicles/suggest"),
                "{'situationId':'01JA8Q3V7S2K9W4T6Y1B5N0M3C'}")))).isEqualTo(ErrorCodes.UNKNOWN_SITUATION);
        JsonNode suggestion = body(call(jo.bearer(), json(post("/v1/chronicles/suggest"),
                "{'area':{'regionCodes':['YE']},'topic':'red sea'}")));
        assertThat(suggestion.get("draft").get("name").asString()).isEqualTo("red sea");
        assertThat(suggestion.get("textOrigin").asString()).isEqualTo("deterministic");
        assertThat(call(reader, json(post("/v1/chronicles/suggest"), "{'area':{'regionCodes':['YE']}}"))
                .getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("MCP: list_chronicles, get_chronicle_thread, get_chronicle_state, create_chronicle, add_chronicle_layer")
    void mcpShapes() throws Exception {
        Person jo = person("jo-mcp", Scope.AGENT);
        ToolContext agentContext = new ToolContext(mcp(Scope.AGENT, jo.id()), "s1", "test");
        ToolContext readContext = new ToolContext(mcp(Scope.READ, jo.id()), null, "test");

        JsonNode created = createTool.call(agentContext, node("{'name':'From an agent','activate':true}")).structured();
        String id = created.get("id").asString();
        assertThat(created.get("status").asString()).isEqualTo("active");
        assertThat(created.get("origin").asString()).isEqualTo("agent");
        JsonNode layer = addLayerTool.call(agentContext, node("{'chronicleId':'" + id + "','name':'Standard',"
                + "'selector':{'type':'records','sources':['" + TestIngestSources.STANDARD + "']},'capture':"
                + "{'mode':'full'}}")).structured();
        assertThat(layer.get("addedAtSeq").isNull()).isFalse();
        standard("mcp-1", T.plusSeconds(10), 42, 12, 1);
        capture.catchUp();

        JsonNode list = listTool.call(readContext, node("{}")).structured();
        assertThat(list.get("items").size()).isEqualTo(1);
        JsonNode thread = threadTool.call(readContext, node("{'chronicleId':'" + id + "'}")).structured();
        assertThat(thread.get("items").size()).isPositive();
        JsonNode state = stateTool.call(readContext, node("{'chronicleId':'" + id + "','at':'"
                + T.plusSeconds(60) + "'}")).structured();
        assertThat(state.get("layers").get(0).get("items").size()).isEqualTo(1);

        assertThat(listTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(threadTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(stateTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(createTool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(addLayerTool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(listTool.name()).isEqualTo("list_chronicles");
        assertThat(threadTool.name()).isEqualTo("get_chronicle_thread");
        assertThat(stateTool.name()).isEqualTo("get_chronicle_state");
        assertThat(createTool.name()).isEqualTo("create_chronicle");
        assertThat(addLayerTool.name()).isEqualTo("add_chronicle_layer");
    }

    static McpCaller mcp(Scope scope, String principal) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "chronicle-mcp", scope,
                TokenRestrictions.NONE, null, principal, false, false);
        return new McpCaller(token, new ReadCaller(scope, false, false, ReadRestriction.NONE, false),
                Set.of(AskTestSources.SECRET), null);
    }
}
