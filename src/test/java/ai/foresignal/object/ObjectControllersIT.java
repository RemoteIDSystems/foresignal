package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.AnnotateTool;
import ai.foresignal.mcp.tools.GetObjectTool;
import ai.foresignal.mcp.tools.GetRelatedTool;
import ai.foresignal.mcp.tools.GetWorkspaceContextTool;
import ai.foresignal.mcp.tools.SetWorkspaceFocusTool;
import ai.foresignal.security.Scope;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/** The prompt 129 endpoints, their scopes and problem codes, and the MCP tool shapes. */
@IntegrationTest
@Import(AskTestSources.class)
class ObjectControllersIT extends ObjectItSupport {

    @Autowired GetObjectTool getObjectTool;
    @Autowired GetRelatedTool getRelatedTool;
    @Autowired AnnotateTool annotateTool;
    @Autowired GetWorkspaceContextTool workspaceContextTool;
    @Autowired SetWorkspaceFocusTool focusTool;

    static final String Q1 = "record:" + AskTestSources.QUAKES + "/q1";

    @Test
    @DisplayName("contract, envelopes, batch resolve, dossier ETag and related objects")
    void objectReads() throws Exception {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        JsonNode contract = body(call(reader, get("/v1/contract")));
        assertThat(contract.get("contractVersion").asString()).isEqualTo(ObjectTypes.CONTRACT_VERSION);
        assertThat(contract.get("objectTypes")).hasSize(ObjectTypes.ALL.size());
        // The relation types grow (158 added the five ownership and affiliation types): the contract lists them all.
        assertThat(contract.get("relationTypes")).extracting(JsonNode::asString)
                .containsExactlyElementsOf(ObjectTypes.RELATION_TYPES);

        MockHttpServletResponse envelope = call(reader, getObject(Q1, ""));
        assertThat(envelope.getStatus()).isEqualTo(200);
        assertThat(envelope.getHeader("ETag")).isNotBlank();
        assertThat(body(envelope).get("ref").asString()).isEqualTo(Q1);
        assertThat(body(envelope).get("private").asBoolean()).isFalse();

        MockHttpServletResponse bad = call(reader, get(URI.create("/v1/objects/bogus:1")));
        assertThat(bad.getStatus()).isEqualTo(400);
        assertThat(body(bad).get("code").asString()).isEqualTo("OBJECT_REF_INVALID");
        MockHttpServletResponse missing = call(reader, getObject("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", ""));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_OBJECT");

        JsonNode resolved = body(call(reader, json(post("/v1/objects/resolve"),
                "{'refs':['" + Q1 + "','situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C']}")));
        assertThat(resolved.get("items")).hasSize(1);
        assertThat(resolved.get("missing").get(0).asString()).isEqualTo("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C");

        MockHttpServletResponse dossier = call(reader, getObject(Q1, "/dossier?include=overview,timeline"));
        assertThat(dossier.getStatus()).isEqualTo(200);
        String etag = dossier.getHeader("ETag");
        assertThat(body(dossier).get("sections").propertyNames()).containsExactly("overview", "timeline");
        MockHttpServletResponse notModified = call(reader, getObject(Q1, "/dossier?include=overview,timeline")
                .header("If-None-Match", etag));
        assertThat(notModified.getStatus()).isEqualTo(304);

        MockHttpServletResponse related = call(reader, getObject(Q1, "/related?types=member-of&direction=in"));
        assertThat(related.getStatus()).isEqualTo(200);
        assertThat(body(related).get("ref").asString()).isEqualTo(Q1);
        assertThat(call(reader, getObject(Q1, "/related?types=likes")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("graph, common dependencies, context summary, precision, sensors and context links")
    void graphAndSummaries() throws Exception {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        quake("q2", "Hodeidah", 4.5, 42.9, 14.8);
        assertThat(call(reader, json(post("/v1/graph"), "{'refs':['" + Q1 + "'],'depth':2}")).getStatus())
                .isEqualTo(200);
        assertThat(call(reader, json(post("/v1/graph"), "{'refs':[]}")).getStatus()).isEqualTo(400);
        assertThat(call(reader, json(post("/v1/graph/common-dependencies"), "{'refs':['" + Q1 + "','record:"
                + AskTestSources.QUAKES + "/q2']}")).getStatus()).isEqualTo(200);
        assertThat(call(reader, json(post("/v1/context/summary"), "{'viewport':{'bbox':[42,12,46,16]},'timeRange':"
                + "{'start':'2026-10-12T00:00:00Z'}}")).getStatus()).isEqualTo(200);
        assertThat(call(reader, get("/v1/analytics/precision")).getStatus()).isEqualTo(200);
        assertThat(call(reader, get("/v1/analytics/precision?component=vibes")).getStatus()).isEqualTo(400);
        assertThat(call(reader, get("/v1/sensors")).getStatus()).isEqualTo(400);
        assertThat(call(reader, get("/v1/sensors?bbox=42,12,46,16&kinds=scene")).getStatus()).isEqualTo(200);
        JsonNode link = body(call(reader, json(post("/v1/context-links"), "{'context':{'focusRef':'" + Q1 + "'}}")));
        assertThat(link.get("fragment").asString()).startsWith("#fs=");
    }

    @Test
    @DisplayName("operator relations need agent scope; projected relations are never deleted by hand")
    void relations() throws Exception {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        SituationEntity s = situation("Red Sea shipping disruption", "warning");
        String body = "{'fromRef':'" + Q1 + "','toRef':'situation:" + s.getId() + "','type':'causes','label':"
                + "'confirmed'}";
        assertThat(call(reader, json(post("/v1/relations"), body)).getStatus()).isEqualTo(403);
        MockHttpServletResponse created = call(agent, json(post("/v1/relations"), body));
        assertThat(created.getStatus()).isEqualTo(201);
        JsonNode relation = body(created);
        assertThat(relation.get("label").asString()).isEqualTo("possible");
        assertThat(relation.get("projected").asBoolean()).isFalse();
        assertThat(relation.get("provenance").get("operator").asString()).isNotBlank();
        MockHttpServletResponse byAdmin = call(admin, json(post("/v1/relations"), body));
        assertThat(body(byAdmin).get("label").asString()).isEqualTo("confirmed");
        assertThat(call(agent, json(post("/v1/relations"), "{'fromRef':'" + Q1 + "','toRef':'situation:"
                + "01JA8Q3V7S2K9W4T6Y1B5N0M3C','type':'related-to'}")).getStatus()).isEqualTo(404);
        assertThat(call(agent, json(post("/v1/relations"), "{'fromRef':'" + Q1 + "','toRef':'situation:" + s.getId()
                + "','type':'located-in'}")).getStatus()).isEqualTo(400);

        assertThat(call(agent, delete("/v1/relations/" + relation.get("id").asString())).getStatus()).isEqualTo(204);
        assertThat(call(agent, delete("/v1/relations/" + relation.get("id").asString())).getStatus()).isEqualTo(404);

        finding("Loitering cluster", "cusum", 43, 14, false, null);
        String findingId = jdbc.queryForObject("SELECT id FROM analytic_finding LIMIT 1", String.class);
        projector.project("finding:" + findingId);
        String projected = relationRows.findByOwnerKey("finding:" + findingId).get(0).getId();
        MockHttpServletResponse conflict = call(agent, delete("/v1/relations/" + projected));
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(body(conflict).get("code").asString()).isEqualTo("RELATION_NOT_EDITABLE");
    }

    @Test
    @DisplayName("annotations: a token is required, verdicts need agent, patches are merge patches")
    void annotationEndpoints() throws Exception {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        String path = objectPath(Q1) + "/annotations";
        assertThat(call(null, json(post(URI.create(path)), "{'kind':'note','body':'x'}")).getStatus())
                .isEqualTo(401);
        MockHttpServletResponse note = call(reader, json(post(URI.create(path)), "{'kind':'note','body':'Check'}"));
        assertThat(note.getStatus()).isEqualTo(201);
        String id = body(note).get("id").asString();
        assertThat(call(reader, json(post(URI.create(path)), "{'kind':'verdict','verdict':'confirmed'}")).getStatus())
                .isEqualTo(403);
        assertThat(call(agent, json(post(URI.create(path)), "{'kind':'verdict','verdict':'confirmed'}")).getStatus())
                .isEqualTo(201);
        JsonNode list = body(call(reader, get(URI.create(path))));
        assertThat(list.get("items")).hasSize(2);
        assertThat(call(reader, json(patch("/v1/annotations/" + id), "{'body':'json'}")).getStatus()).isEqualTo(415);
        MockHttpServletResponse patched = call(reader, mergePatch(patch("/v1/annotations/" + id),
                "{'body':'Port notice confirms'}"));
        assertThat(patched.getStatus()).isEqualTo(200);
        assertThat(body(patched).get("body").asString()).isEqualTo("Port notice confirms");
        assertThat(call(otherAgent, delete("/v1/annotations/" + id)).getStatus()).isEqualTo(403);
        assertThat(call(reader, delete("/v1/annotations/" + id)).getStatus()).isEqualTo(204);
        MockHttpServletResponse gone = call(reader, delete("/v1/annotations/" + id));
        assertThat(gone.getStatus()).isEqualTo(404);
        assertThat(body(gone).get("code").asString()).isEqualTo("UNKNOWN_ANNOTATION");
    }

    @Test
    @DisplayName("workspaces: create, list, share with agent scope once CLEAR, summary 409 without a viewport")
    void workspaceEndpoints() throws Exception {
        MockHttpServletResponse created = call(agent, json(post("/v1/workspaces"), "{'name':'Red Sea watch'}"));
        assertThat(created.getStatus()).isEqualTo(201);
        String id = body(created).get("id").asString();
        assertThat(body(call(agent, get("/v1/workspaces"))).get("items")).hasSize(1);
        assertThat(body(call(reader, get("/v1/workspaces"))).get("items")).isEmpty();
        MockHttpServletResponse summary = call(agent, get("/v1/workspaces/" + id + "/summary"));
        assertThat(summary.getStatus()).isEqualTo(409);
        assertThat(body(summary).get("code").asString()).isEqualTo("WORKSPACE_CONTEXT_INCOMPLETE");
        assertThat(call(agent, json(post("/v1/workspaces"), "{'name':'Big','layout':{'blob':'" + "x".repeat(70_000)
                + "'}}")).getStatus()).isEqualTo(413);
        // Prompt 130: a share link is public, so the workspace's TLP label (AMBER by default) must first be CLEAR.
        MockHttpServletResponse amber = call(agent, json(post("/v1/workspaces/" + id + "/share"), "{'maxUses':5}"));
        assertThat(amber.getStatus()).isEqualTo(403);
        assertThat(body(amber).get("code").asString()).isEqualTo("TLP_VIOLATION");
        assertThat(call(agent, json(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                URI.create(objectPath("workspace:" + id) + "/acl")), "{'entries':[],'tlp':'CLEAR'}")).getStatus())
                .isEqualTo(200);
        MockHttpServletResponse share = call(agent, json(post("/v1/workspaces/" + id + "/share"), "{'maxUses':5}"));
        assertThat(share.getStatus()).isEqualTo(201);
        JsonNode link = body(share);
        assertThat(link.get("targetType").asString()).isEqualTo("workspace");
        assertThat(link.get("key").asString()).startsWith("fss_");
        MockHttpServletResponse opened = call(null, get("/v1/k/" + link.get("key").asString()));
        assertThat(opened.getStatus()).isEqualTo(200);
        assertThat(body(opened).get("data").get("workspaceId").asString()).isEqualTo(id);
        assertThat(body(opened).get("data").has("layout")).isFalse();
        assertThat(call(agent, delete("/v1/workspaces/" + id)).getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("MCP: get_object, get_related, annotate, get_workspace_context and set_workspace_focus")
    void mcpShapes() throws Exception {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        String agentPrincipal = principalOf(agent);
        JsonNode env = getObjectTool.call(new ToolContext(mcp(Scope.READ, false, null), null, "test"),
                JSON.readTree("{\"ref\":\"" + Q1 + "\"}")).structured();
        assertThat(env.get("ref").asString()).isEqualTo(Q1);
        JsonNode related = getRelatedTool.call(new ToolContext(mcp(Scope.READ, false, null), null, "test"),
                JSON.readTree("{\"ref\":\"" + Q1 + "\",\"limit\":5}")).structured();
        assertThat(related.has("items")).isTrue();
        JsonNode note = annotateTool.call(new ToolContext(mcp(Scope.READ, false, "p-mcp"), null, "test"),
                JSON.readTree("{\"ref\":\"" + Q1 + "\",\"body\":\"From the agent\"}")).structured();
        assertThat(note.get("kind").asString()).isEqualTo("note");
        assertThat(getObjectTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(focusTool.requiredScope()).isEqualTo(Scope.AGENT);

        MockHttpServletResponse created = call(agent, json(post("/v1/workspaces"), "{'name':'Agent focus'}"));
        String id = body(created).get("id").asString();
        JsonNode focused = focusTool.call(new ToolContext(mcp(Scope.AGENT, false, agentPrincipal), "s1", "test"),
                JSON.readTree("{\"workspaceId\":\"" + id + "\",\"ref\":\"" + Q1 + "\"}")).structured();
        assertThat(focused.get("contextVersion").asLong()).isEqualTo(2);
        assertThat(focused.get("context").get("focusRef").asString()).isEqualTo(Q1);
        JsonNode context = workspaceContextTool.call(new ToolContext(mcp(Scope.READ, false, agentPrincipal), null,
                "test"), JSON.readTree("{\"workspaceId\":\"" + id + "\"}")).structured();
        assertThat(context.get("context").get("selectedRefs").get(0).asString()).isEqualTo(Q1);
        assertThat(context.has("clients")).isTrue();
    }
}
