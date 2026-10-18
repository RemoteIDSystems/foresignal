package ai.foresignal.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.DraftReportTool;
import ai.foresignal.mcp.tools.GetReportTool;
import ai.foresignal.mcp.tools.ListReportsTool;
import ai.foresignal.mcp.tools.SendReportTool;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

/**
 * The report and exchange API (prompt 132) over MockMvc: every operation's status codes and scopes, ETags and the
 * optimistic lock, block operations, the {@code report} object type (contract 1.3), the Problem codes
 * ({@code UNKNOWN_REPORT}, {@code UNKNOWN_PACKAGE}, {@code REPORT_VERSION_CONFLICT}, {@code PACKAGE_SIGNATURE_INVALID})
 * and the shapes of the four MCP tools.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ReportControllersIT extends ReportItSupport {

    @Autowired ListReportsTool listTool;
    @Autowired GetReportTool getTool;
    @Autowired DraftReportTool draftTool;
    @Autowired SendReportTool sendTool;
    @Autowired ObjectService objects;

    private static McpCaller mcp(Person p) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), p.handle() + "-mcp", p.scope(),
                TokenRestrictions.NONE, null, p.id(), false, false);
        return new McpCaller(token, new ReadCaller(p.scope(), false, false, ReadRestriction.NONE, false),
                Set.of(AskTestSources.SECRET), null);
    }

    @Test
    @DisplayName("create, read, list, patch, blocks and delete with scopes, ETags and the optimistic lock")
    void crudAndBlocks() throws Exception {
        Person jo = person("jo-api", Scope.READ, null);
        Person eve = person("eve-api", Scope.READ, null);
        assertThat(call(null, get("/v1/reports")).getStatus()).isEqualTo(401);
        assertThat(call(jo.bearer(), json(post("/v1/reports"), "{}")).getStatus()).isEqualTo(400);

        MockHttpServletResponse created = call(jo.bearer(), json(post("/v1/reports"), "{'title':'Daily brief',"
                + "'subtitle':'Red Sea','authors':['eve-api'],'blocks':[{'type':'markdown','markdown':'Intro.'}]}"));
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        assertThat(created.getHeader("Location")).startsWith("/v1/reports/");
        JsonNode r = body(created);
        String id = r.get("id").asString();
        assertThat(r.get("ref").asString()).isEqualTo("report:" + id);
        assertThat(r.get("visibility").asString()).isEqualTo("private");
        assertThat(r.get("tlp").asString()).isEqualTo("AMBER");
        assertThat(r.get("authors").size()).isEqualTo(2);
        assertThat(r.get("blocks").get(0).get("textOrigin").asString()).isEqualTo("author");
        assertThat(r.get("machineGenerated").asBoolean()).isFalse();

        MockHttpServletResponse read = call(jo.bearer(), get("/v1/reports/" + r.get("displayId").asString()));
        assertThat(read.getStatus()).isEqualTo(200);
        String etag = read.getHeader("ETag");
        assertThat(etag).isEqualTo("\"" + r.get("version").asInt() + "\"");
        assertThat(call(eve.bearer(), get("/v1/reports/" + id)).getStatus()).as("authors edit").isEqualTo(200);
        Person stranger = person("stranger-api", Scope.READ, null);
        MockHttpServletResponse hidden = call(stranger.bearer(), get("/v1/reports/" + id));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(code(hidden)).isEqualTo(ErrorCodes.UNKNOWN_REPORT);
        assertThat(body(call(stranger.bearer(), get("/v1/reports"))).get("items").size()).isZero();
        assertThat(body(call(jo.bearer(), get("/v1/reports").param("scope", "mine"))).get("items").size())
                .isEqualTo(1);
        assertThat(call(jo.bearer(), get("/v1/reports").param("status", "bogus")).getStatus()).isEqualTo(400);

        MockHttpServletResponse patched = call(jo.bearer(), mergePatch(patch("/v1/reports/" + id), "{'title':"
                + "'Daily brief (am)','status':'review'}").header("If-Match", etag));
        assertThat(patched.getStatus()).isEqualTo(200);
        assertThat(body(patched).get("status").asString()).isEqualTo("review");
        MockHttpServletResponse stale = call(jo.bearer(), mergePatch(patch("/v1/reports/" + id), "{'title':'X'}")
                .header("If-Match", etag));
        assertThat(stale.getStatus()).isEqualTo(409);
        assertThat(code(stale)).isEqualTo(ErrorCodes.REPORT_VERSION_CONFLICT);

        MockHttpServletResponse block = call(jo.bearer(), json(post("/v1/reports/" + id + "/blocks"),
                "{'type':'bluf','judgments':[{'text':'Disruption continues.'}]}"));
        assertThat(block.getStatus()).isEqualTo(201);
        String blockId = body(block).get("id").asString();
        assertThat(call(jo.bearer(), json(post("/v1/reports/" + id + "/blocks"), "{'type':'nope'}")).getStatus())
                .isEqualTo(400);
        MockHttpServletResponse first = call(jo.bearer(), json(post("/v1/reports/" + id + "/blocks").param("after",
                "missing"), "{'type':'markdown','markdown':'x'}"));
        assertThat(first.getStatus()).isEqualTo(404);
        MockHttpServletResponse changed = call(eve.bearer(), mergePatch(patch("/v1/reports/" + id + "/blocks/"
                + blockId), "{'title':'Bottom line'}"));
        assertThat(changed.getStatus()).isEqualTo(200);
        assertThat(body(changed).get("title").asString()).isEqualTo("Bottom line");
        assertThat(body(changed).get("updatedBy").get("handle").asString()).isEqualTo("eve-api");
        assertThat(call(jo.bearer(), mergePatch(patch("/v1/reports/" + id + "/blocks/" + blockId),
                "{'type':'markdown'}")).getStatus()).isEqualTo(400);

        String blockRef = "report:" + id + "#" + blockId;
        java.net.URI notePath = java.net.URI.create("/v1/objects/" + blockRef.replace("%", "%25").replace("/", "%2F")
                .replace("#", "%23") + "/annotations");
        MockHttpServletResponse note = call(eve.bearer(), json(post(notePath), "{'kind':'note','body':"
                + "'Source this judgment.'}"));
        assertThat(note.getStatus()).as(note.getContentAsString()).isEqualTo(201);
        assertThat(body(note).get("targetRef").asString()).isEqualTo(blockRef);
        assertThat(call(stranger.bearer(), json(post(notePath), "{'kind':'note','body':'x'}")).getStatus())
                .isEqualTo(404);

        JsonNode now = body(call(jo.bearer(), get("/v1/reports/" + id)));
        String introId = now.get("blocks").get(0).get("id").asString();
        assertThat(call(jo.bearer(), mergePatch(patch("/v1/reports/" + id), "{'blockOrder':['" + blockId + "','"
                + introId + "']}")).getStatus()).isEqualTo(200);
        assertThat(body(call(jo.bearer(), get("/v1/reports/" + id))).get("blocks").get(0).get("id").asString())
                .isEqualTo(blockId);
        assertThat(call(jo.bearer(), delete("/v1/reports/" + id + "/blocks/" + blockId)).getStatus())
                .isEqualTo(204);
        assertThat(call(jo.bearer(), delete("/v1/reports/" + id + "/blocks/" + blockId)).getStatus())
                .isEqualTo(404);

        assertThat(objects.find(ai.foresignal.object.ObjectRef.parse("report:" + id), new ResolveContext(jo.caller(),
                null))).isPresent();
        assertThat(objects.find(ai.foresignal.object.ObjectRef.parse("report:" + id),
                new ResolveContext(stranger.caller(), null))).isEmpty();
        assertThat(ObjectTypes.ALL).contains(ObjectTypes.REPORT, ObjectTypes.REMOTE);
        assertThat(ObjectTypes.CONTRACT_VERSION).isEqualTo("1.3");

        assertThat(call(eve.bearer(), delete("/v1/reports/" + id)).getStatus()).isEqualTo(403);
        assertThat(call(jo.bearer(), delete("/v1/reports/" + id)).getStatus()).isEqualTo(204);
        assertThat(call(jo.bearer(), get("/v1/reports/" + id)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("scopes: sending, links, packages and the sent list need agent; import needs admin; peers need a key")
    void scopes() throws Exception {
        Person reader = person("reader-scope", Scope.READ, null);
        Person agentPerson = person("agent-scope", Scope.AGENT, null);
        ReportEntity r = report(reader, "{'title':'Scoped'}");
        assertThat(call(reader.bearer(), json(post("/v1/reports/" + r.getId() + "/send"), "{'principals':[]}"))
                .getStatus()).isEqualTo(403);
        assertThat(call(reader.bearer(), json(post("/v1/reports/" + r.getId() + "/share-link"), "{}"))
                .getStatus()).isEqualTo(403);
        assertThat(call(reader.bearer(), json(post("/v1/exchange/packages"), "{}")).getStatus()).isEqualTo(403);
        assertThat(call(reader.bearer(), get("/v1/exchange/sent")).getStatus()).isEqualTo(403);
        assertThat(call(agentPerson.bearer(), multipart("/v1/exchange/import").file(new MockMultipartFile("file",
                "p.zip", "application/zip", new byte[] {1}))).getStatus()).isEqualTo(403);
        MockHttpServletResponse garbage = call(admin, multipart("/v1/exchange/import").file(new MockMultipartFile(
                "file", "p.zip", "application/zip", new byte[] {1, 2, 3})));
        assertThat(garbage.getStatus()).isEqualTo(400);
        assertThat(code(garbage)).isEqualTo(ErrorCodes.PACKAGE_SIGNATURE_INVALID);
        assertThat(call(admin, post("/v1/exchange/inbox").content(new byte[] {1})).getStatus()).isEqualTo(401);
        MockHttpServletResponse unknown = call(agentPerson.bearer(), get("/v1/exchange/packages/"
                + "01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(code(unknown)).isEqualTo(ErrorCodes.UNKNOWN_PACKAGE);
        assertThat(call(reader.bearer(), get("/v1/exchange/received/01JA8Q3V7S2K9W4T6Y1B5N0M3C")).getStatus())
                .isEqualTo(404);
        assertThat(call(reader.bearer(), get("/v1/exchange/received")).getStatus()).isEqualTo(200);
        assertThat(call(null, get("/v1/k/fss_unknown/report")).getStatus()).isEqualTo(401);
        assertThat(call(reader.bearer(), json(post("/v1/exchange/packages"), "{}")).getStatus()).isEqualTo(403);
        assertThat(call(agentPerson.bearer(), json(post("/v1/exchange/packages"), "{'objectRef':'situation:"
                + "01JA8Q3V7S2K9W4T6Y1B5N0M3C','recipients':'*'}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("MCP: list_reports and get_report (read), draft_report and send_report (agent)")
    void mcpTools() throws Exception {
        Person jo = person("jo-mcp", Scope.AGENT, null);
        Person bob = person("bob-mcp", Scope.READ, null);
        SituationEntity s = situation("MCP situation");
        assertThat(listTool.name()).isEqualTo("list_reports");
        assertThat(listTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(listTool.alwaysAuthenticated()).isTrue();
        assertThat(getTool.name()).isEqualTo("get_report");
        assertThat(getTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(draftTool.name()).isEqualTo("draft_report");
        assertThat(draftTool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(draftTool.alwaysAuthenticated()).isTrue();
        assertThat(sendTool.name()).isEqualTo("send_report");
        assertThat(sendTool.requiredScope()).isEqualTo(Scope.AGENT);

        JsonNode drafted = draftTool.call(new ToolContext(mcp(jo), null, "test"), node("{'source':{'type':"
                + "'situation','ref':'situation:" + s.getId() + "'},'template':'situation-brief'}")).structured();
        String id = drafted.get("id").asString();
        assertThat(drafted.get("template").asString()).isEqualTo("situation-brief");
        JsonNode page = listTool.call(new ToolContext(mcp(jo), null, "test"), node("{}")).structured();
        assertThat(page.get("items").size()).isEqualTo(1);
        JsonNode got = getTool.call(new ToolContext(mcp(jo), null, "test"), node("{'reportId':'" + id
                + "','markdown':true}")).structured();
        assertThat(got.get("id").asString()).isEqualTo(id);
        assertThat(got.get("markdown").isNull()).isTrue();

        publish(jo, reports.get(id));
        JsonNode send = sendTool.call(new ToolContext(mcp(jo), null, "test"), node("{'reportId':'" + id
                + "','principals':['bob-mcp']}")).structured();
        assertThat(send.get("recipients").get(0).get("grantee").asString()).isEqualTo("principal:" + bob.id());
        JsonNode afterPublish = getTool.call(new ToolContext(mcp(jo), null, "test"), node("{'reportId':'" + id
                + "','markdown':true}")).structured();
        assertThat(afterPublish.get("markdown").asString()).contains("TLP:AMBER");
        assertThat(listTool.call(new ToolContext(mcp(bob), null, "test"), node("{'scope':'shared'}")).structured()
                .get("items").size()).isEqualTo(1);
    }
}
