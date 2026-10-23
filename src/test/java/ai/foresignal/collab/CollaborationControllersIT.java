package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.CommentTool;
import ai.foresignal.mcp.tools.ListSharedWithMeTool;
import ai.foresignal.mcp.tools.ShareObjectTool;
import ai.foresignal.mcp.tools.TagObjectsTool;
import ai.foresignal.mcp.tools.WhoamiTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * The prompt 130 endpoints over MockMvc: scopes, problem codes, audit entries, preferences with personal channels,
 * the {@code me} topic replay and the five MCP tools.
 */
@IntegrationTest
@Import(AskTestSources.class)
class CollaborationControllersIT extends CollabItSupport {

    @Autowired WhoamiTool whoami;
    @Autowired ListSharedWithMeTool sharedTool;
    @Autowired TagObjectsTool tagTool;
    @Autowired CommentTool commentTool;
    @Autowired ShareObjectTool shareTool;
    @Autowired NotificationChannelRepository channelRows;
    @Autowired MeTopic meTopic;

    static McpCaller mcp(Person p) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), p.handle() + "-mcp", p.scope(),
                TokenRestrictions.NONE, null, p.id(), false, false);
        return new McpCaller(token, new ReadCaller(p.scope(), false, false, ReadRestriction.NONE, false), Set.of(),
                null);
    }

    @Test
    @DisplayName("principals: admin creates and patches, anyone with a token lists summaries, tokens per principal")
    void principals() throws Exception {
        assertThat(call(null, get("/v1/principals")).getStatus()).isEqualTo(401);
        assertThat(call(reader, get("/v1/principals")).getStatus()).isEqualTo(200);
        String body = "{'handle':'jsmith','displayName':'Jo Smith','kind':'person','email':'jo@example.org',"
                + "'organization':'Example','timezone':'Europe/London'}";
        assertThat(call(reader, json(post("/v1/principals"), body)).getStatus()).isEqualTo(403);
        MockHttpServletResponse created = call(admin, json(post("/v1/principals"), body));
        assertThat(created.getStatus()).isEqualTo(201);
        String id = body(created).get("id").asString();
        MockHttpServletResponse taken = call(admin, json(post("/v1/principals"), body));
        assertThat(taken.getStatus()).isEqualTo(409);
        assertThat(body(taken).get("code").asString()).isEqualTo("PRINCIPAL_HANDLE_TAKEN");
        assertThat(call(admin, json(post("/v1/principals"), "{'handle':'Bad Handle','displayName':'x','kind':"
                + "'person'}")).getStatus()).isEqualTo(400);
        JsonNode summary = body(call(reader, get("/v1/principals/jsmith")));
        assertThat(summary.has("email")).isFalse();
        assertThat(summary.get("displayName").asString()).isEqualTo("Jo Smith");
        assertThat(body(call(admin, get("/v1/principals/" + id))).get("email").asString()).isEqualTo("jo@example.org");
        assertThat(call(admin, mergePatch(patch("/v1/principals/" + id), "{'handle':'other'}")).getStatus())
                .isEqualTo(400);
        assertThat(call(admin, mergePatch(patch("/v1/principals/" + id), "{'timezone':'Mars/Olympus'}"))
                .getStatus()).isEqualTo(400);
        MockHttpServletResponse token = call(admin, json(post("/v1/principals/jsmith/tokens"), "{'name':'laptop',"
                + "'scope':'read'}"));
        assertThat(token.getStatus()).isEqualTo(201);
        assertThat(body(token).get("principalId").asString()).isEqualTo(id);
        String bearer = "Bearer " + body(token).get("token").asString();
        assertThat(body(call(bearer, get("/v1/me"))).get("principal").get("handle").asString()).isEqualTo("jsmith");
        assertThat(body(call(bearer, get("/v1/principals/" + id))).get("email").asString())
                .isEqualTo("jo@example.org");
        MockHttpServletResponse missing = call(reader, get("/v1/principals/nobody"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_PRINCIPAL");
        assertThat(auditOf(CollabAudit.TARGET_PRINCIPAL)).extracting(r -> r.get("operation_id"))
                .contains("createPrincipal", "createPrincipalToken");
    }

    @Test
    @DisplayName("groups: agent creates, managers change, others are forbidden, unknown groups are 404")
    void groupsEndpoints() throws Exception {
        Person jo = person("jo-g", Scope.AGENT, null);
        Person sam = person("sam-g", Scope.AGENT, null);
        assertThat(call(reader, json(post("/v1/groups"), "{'name':'desk'}")).getStatus()).isEqualTo(403);
        JsonNode group = body(call(jo.bearer(), json(post("/v1/groups"), "{'name':'desk'}")));
        String id = group.get("id").asString();
        assertThat(group.get("members").get(0).get("role").asString()).isEqualTo("manager");
        assertThat(call(sam.bearer(), mergePatch(patch("/v1/groups/" + id), "{'name':'mine now'}")).getStatus())
                .isEqualTo(403);
        JsonNode added = body(call(jo.bearer(), json(post("/v1/groups/" + id + "/members"),
                "{'members':[{'principalId':'sam-g'}]}")));
        assertThat(added.get("memberCount").asInt()).isEqualTo(2);
        assertThat(body(call(reader, get("/v1/groups?member=sam-g"))).get("items")).hasSize(1);
        JsonNode removed = body(call(jo.bearer(), delete("/v1/groups/" + id + "/members?principalId=" + sam.id())));
        assertThat(removed.get("memberCount").asInt()).isEqualTo(1);
        assertThat(call(jo.bearer(), json(post("/v1/groups/" + id + "/members"),
                "{'members':[{'principalId':'nobody'}]}")).getStatus()).isEqualTo(404);
        Person kim = person("kim-g", Scope.AGENT, null);
        Person lee = person("lee-g", Scope.AGENT, null);
        assertThat(body(call(jo.bearer(), json(post("/v1/groups/" + id + "/members"),
                "{'members':[{'principalId':'kim-g'},{'principalId':'lee-g'},{'principalId':'sam-g'}]}")))
                .get("memberCount").asInt()).isEqualTo(4);
        assertThat(call(kim.bearer(), delete("/v1/groups/" + id + "/members?principalId=lee-g")).getStatus())
                .isEqualTo(403);
        assertThat(body(call(kim.bearer(), delete("/v1/groups/" + id + "/members?principalId=kim-g")))
                .get("memberCount").asInt()).isEqualTo(3);
        assertThat(call(jo.bearer(), delete("/v1/groups/" + id + "/members?principalId=lee-g,nobody")).getStatus())
                .isEqualTo(404);
        assertThat(body(call(jo.bearer(), delete("/v1/groups/" + id + "/members?principalId=lee-g,"
                + sam.id()))).get("memberCount").asInt()).isEqualTo(1);
        assertThat(body(call(jo.bearer(), json(post("/v1/groups/" + id + "/members"),
                "{'members':[{'principalId':'kim-g'},{'principalId':'lee-g'}]}"))).get("memberCount").asInt())
                .isEqualTo(3);
        assertThat(body(call(jo.bearer(), delete("/v1/groups/" + id + "/members?principalId=kim-g&principalId="
                + lee.id()))).get("memberCount").asInt()).isEqualTo(1);
        assertThat(call(jo.bearer(), delete("/v1/groups/" + id)).getStatus()).isEqualTo(204);
        MockHttpServletResponse gone = call(reader, get("/v1/groups/" + id));
        assertThat(gone.getStatus()).isEqualTo(404);
        assertThat(body(gone).get("code").asString()).isEqualTo("UNKNOWN_GROUP");
    }

    @Test
    @DisplayName("preferences own personal channels that never take the instance fan-out; the inbox needs a token")
    void preferencesAndInbox() throws Exception {
        Person jo = person("jo-pref", Scope.READ, null);
        MockHttpServletResponse saved = call(jo.bearer(), json(put("/v1/me/notification-preferences"),
                "{'items':[{'eventTypes':['mention','share'],'channel':{'type':'ntfy','config':{'topic':'jo-fs'}},"
                        + "'quietHours':{'start':'22:00','end':'07:00'}}]}"));
        assertThat(saved.getStatus()).isEqualTo(200);
        JsonNode pref = body(saved).get(0);
        UUID channelId = UUID.fromString(pref.get("channel").get("channelId").asString());
        NotificationChannelEntity channel = channelRows.findById(channelId).orElseThrow();
        assertThat(channel.getOwnerPrincipalId()).isEqualTo(jo.id());
        assertThat(channel.getEvents()).containsExactly("personal");
        assertThat(body(call(jo.bearer(), get("/v1/me/notification-preferences")))).hasSize(1);
        assertThat(call(jo.bearer(), json(put("/v1/me/notification-preferences"), "{'items':[{'eventTypes':"
                + "['gossip'],'channel':{'type':'ntfy','config':{'topic':'x'}}}]}")).getStatus()).isEqualTo(400);
        assertThat(call(jo.bearer(), json(put("/v1/me/notification-preferences"), "{'items':[]}")).getStatus())
                .isEqualTo(200);
        assertThat(channelRows.findById(channelId)).isEmpty();
        assertThat(call(null, get("/v1/inbox")).getStatus()).isEqualTo(401);
        assertThat(call(jo.bearer(), get("/v1/inbox?category=nonsense")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("tags, attachments, evidence items and saved searches need a token")
    void tokensRequired() throws Exception {
        for (String path : List.of("/v1/tags", "/v1/evidence-items", "/v1/saved-searches", "/v1/shared-with-me",
                "/v1/my", "/v1/groups", "/v1/me/notification-preferences")) {
            assertThat(call(null, get(path)).getStatus()).as(path).isEqualTo(401);
            assertThat(call(reader, get(path)).getStatus()).as(path).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("MCP: whoami, list_shared_with_me, tag_objects, comment and share_object; the me topic replays")
    void mcpShapes() throws Exception {
        quake("m1", "Aden", 4.5, 45.0, 12.8);
        Person jo = person("jo-mcp", Scope.AGENT, null);
        Person sam = person("sam-mcp", Scope.READ, null);
        JsonNode me = whoami.call(new ToolContext(mcp(jo), null, "test"), JSON.readTree("{}")).structured();
        assertThat(me.get("principal").get("handle").asString()).isEqualTo("jo-mcp");

        String ref = "record:" + AskTestSources.QUAKES + "/m1";
        JsonNode tagged = tagTool.call(new ToolContext(mcp(jo), null, "test"), JSON.readTree("{\"tag\":\"aden\","
                + "\"refs\":[\"" + ref + "\"]}")).structured();
        assertThat(tagged.get("changed").asInt()).isEqualTo(1);

        String inv = body(call(jo.bearer(), json(post("/v1/investigations"), "{'title':'MCP case','visibility':"
                + "'private'}"))).get("id").asString();
        JsonNode acl = shareTool.call(new ToolContext(mcp(jo), null, "test"), JSON.readTree("{\"ref\":"
                + "\"investigation:" + inv + "\",\"grantees\":[\"principal:sam-mcp\"],\"role\":\"commenter\"}"))
                .structured();
        assertThat(acl.get("entries").get(0).get("role").asString()).isEqualTo("commenter");
        assertThat(auditOf(CollabAudit.TARGET_ACL)).extracting(r -> r.get("operation_id"))
                .contains("mcp:share_object");

        JsonNode shared = sharedTool.call(new ToolContext(mcp(sam), null, "test"), JSON.readTree("{}"))
                .structured();
        assertThat(shared.get("items")).extracting(i -> i.get("envelope").get("ref").asString())
                .contains("investigation:" + inv);
        JsonNode comment = commentTool.call(new ToolContext(mcp(sam), null, "test"), JSON.readTree("{\"ref\":"
                + "\"investigation:" + inv + "\",\"body\":\"Looks right, @jo-mcp\"}")).structured();
        assertThat(comment.get("mentions").get(0).asString()).isEqualTo("jo-mcp");

        List<FifoEvent> events = new ArrayList<>();
        meTopic.replay(new ReplayRequest(0, false), new MeFilter(jo.id()), e -> events.add(e));
        assertThat(events).extracting(FifoEvent::event).contains(MeEvents.MENTION);
        List<FifoEvent> samEvents = new ArrayList<>();
        meTopic.replay(new ReplayRequest(0, false), new MeFilter(sam.id()), e -> samEvents.add(e));
        assertThat(samEvents).extracting(FifoEvent::event).containsExactly(MeEvents.SHARE_RECEIVED);
        assertThat(samEvents.get(0).data().get("role")).isEqualTo("commenter");
    }
}
