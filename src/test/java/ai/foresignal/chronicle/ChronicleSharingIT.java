package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.collab.ShareService;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Chronicles under 130 access lists and TLP (prompt 131): a chronicle is invisible (404) until shared; a viewer reads
 * its thread, replay and workspace and may export, but not change it; it appears in shared-with-me; TLP bounds every
 * share and is printed on exports; the SSE topic follows the same rule; private data reaches only callers with private
 * access.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleSharingIT extends ChronicleItSupport {

    @Autowired ShareService shares;
    @Autowired ChronicleTopic topic;

    private ChronicleEntity captured(Person owner, String tlp) {
        ChronicleEntity c = activate(owner, create(owner, "{'name':'Shared thread','tlp':'" + tlp + "','layers':["
                + standardLayer("Standard", "{'mode':'full'}") + "]}"));
        standard("s1", T.plusSeconds(10), 42, 12, 1);
        capture.catchUp();
        return chronicleRows.findById(c.getId()).orElseThrow();
    }

    @Test
    @DisplayName("invisible until shared; a viewer reads the thread, the replay and the workspace but cannot change it")
    void aclOnThreadAndReplay() throws Exception {
        Person jo = person("jo-share", Scope.AGENT);
        Person bob = person("bob-share", Scope.AGENT);
        ChronicleEntity c = captured(jo, "AMBER");
        String base = "/v1/chronicles/" + c.getId();

        assertThat(call(bob.bearer(), get(base)).getStatus()).isEqualTo(404);
        assertThat(body(call(bob.bearer(), get(base + "/thread"))).get("code").asString())
                .isEqualTo(ErrorCodes.UNKNOWN_CHRONICLE);
        assertThat(call(bob.bearer(), get(base + "/state").param("at", T.plusSeconds(60).toString())).getStatus())
                .isEqualTo(404);
        assertThat(call(bob.bearer(), json(post(base + "/export"), "{'format':'json-bundle'}")).getStatus())
                .isEqualTo(404);
        ChronicleEventEntity item = eventRows.findByChronicleIdOrderByIdAsc(c.getId()).getFirst();
        ChronicleFilter bobs = new ChronicleFilter(bob.caller(), java.util.Set.of());
        assertThat(topic.passes(item, bobs, new HashMap<>())).isFalse();

        shares.share("chronicle:" + c.getId(), node("{'grantees':['principal:" + bob.id() + "'],'role':'viewer'}"),
                jo.caller(), "shareObject");

        assertThat(call(bob.bearer(), get(base)).getStatus()).isEqualTo(200);
        assertThat(call(bob.bearer(), get("/v1/chronicles/" + c.getDisplayId())).getStatus()).isEqualTo(200);
        JsonNode thread = body(call(bob.bearer(), get(base + "/thread")));
        assertThat(thread.get("items").size()).isPositive();
        assertThat(thread.get("unreadCount").asInt()).isEqualTo(thread.get("items").size());
        JsonNode state = body(call(bob.bearer(), get(base + "/state").param("at", T.plusSeconds(60).toString())));
        assertThat(state.get("layers").get(0).get("items").size()).isEqualTo(1);
        assertThat(call(bob.bearer(), get("/v1/workspaces/" + c.getWorkspaceId())).getStatus()).isEqualTo(200);
        assertThat(topic.passes(item, bobs, new HashMap<>())).isTrue();
        MockHttpServletResponse change = call(bob.bearer(), mergePatch(patch(base), "{'name':'Renamed'}"));
        assertThat(change.getStatus()).isEqualTo(403);
        assertThat(call(bob.bearer(), post(base + "/stop")).getStatus()).isEqualTo(403);

        JsonNode shared = body(call(bob.bearer(), get("/v1/shared-with-me")));
        boolean listed = false;
        for (JsonNode i : shared.get("items")) {
            listed |= ("chronicle:" + c.getId()).equals(i.get("envelope").get("ref").asString());
        }
        assertThat(listed).isTrue();
        JsonNode mine = body(call(bob.bearer(), get("/v1/chronicles").param("scope", "shared")));
        assertThat(mine.get("items").size()).isEqualTo(1);
        assertThat(body(call(bob.bearer(), get("/v1/chronicles").param("scope", "mine"))).get("items").size())
                .isZero();
    }

    @Test
    @DisplayName("TLP bounds shares and visibility, and exports carry the label")
    void tlpOnSharesAndExports() throws Exception {
        Person jo = person("jo-tlp", Scope.AGENT);
        Person bob = person("bob-tlp", Scope.AGENT);
        MockHttpServletResponse red = call(jo.bearer(), json(post("/v1/chronicles"),
                "{'name':'Red instance','tlp':'RED','visibility':'instance'}"));
        assertThat(red.getStatus()).isEqualTo(403);
        assertThat(body(red).get("code").asString()).isEqualTo(ErrorCodes.TLP_VIOLATION);

        ChronicleEntity c = captured(jo, "RED");
        shares.share("chronicle:" + c.getId(), node("{'grantees':['principal:" + bob.id() + "'],'role':'viewer'}"),
                jo.caller(), "shareObject");
        MockHttpServletResponse export = call(bob.bearer(), json(post("/v1/chronicles/" + c.getId() + "/export"),
                "{'format':'json-bundle'}"));
        assertThat(export.getStatus()).isEqualTo(202);
        assertThat(export.getHeader("Foresignal-TLP")).isEqualTo("RED");
        JsonNode job = body(export);
        assertThat(job.get("format").asString()).isEqualTo("json-bundle");
        assertThat(job.get("spec").get("tlp").asString()).isEqualTo("RED");
        assertThat(job.get("status").asString()).isIn("QUEUED", "RUNNING", "DONE");
    }

    @Test
    @DisplayName("private data is captured, but only callers with private access see it in the replay and the thread")
    void privateDataStaysPrivate() throws Exception {
        Person jo = person("jo-private", Scope.ADMIN);
        Person bob = person("bob-private", Scope.READ);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Private','visibility':'instance','tlp':'GREEN',"
                + "'layers':[{'name':'Secret','selector':{'type':'records','sources':['" + AskTestSources.SECRET
                + "']},'capture':{'mode':'full'}}]}"));
        clock.setInstant(T.plusSeconds(10));
        secret("hidden", "Hidden place", 42, 12);
        capture.catchUp();
        assertThat(capturedSeqs(c.getId(), "hidden")).hasSize(1);
        String base = "/v1/chronicles/" + c.getId();

        JsonNode bobState = body(call(bob.bearer(), get(base + "/state").param("at", T.plusSeconds(60).toString())));
        assertThat(bobState.get("layers").get(0).get("items").size()).isZero();
        JsonNode bobThread = body(call(bob.bearer(), get(base + "/thread")
                .param("types", ChronicleVocabulary.ITEM_FIRST_APPEARANCE)));
        assertThat(bobThread.get("items").size()).isZero();

        JsonNode adminState = body(call(admin, get(base + "/state").param("at", T.plusSeconds(60).toString())));
        assertThat(adminState.get("layers").get(0).get("items").size()).isEqualTo(1);
        JsonNode adminThread = body(call(admin, get(base + "/thread")
                .param("types", ChronicleVocabulary.ITEM_FIRST_APPEARANCE)));
        assertThat(adminThread.get("items").size()).isEqualTo(1);
    }
}
