package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Investigations under access lists (prompt 130): owners, members and statuses, display-id lookups in every path,
 * private investigations invisible to others, shared-with-me through principals and groups, ownership transfer, TLP
 * bounds and audit entries.
 */
@IntegrationTest
@Import(AskTestSources.class)
class InvestigationSharingIT extends CollabItSupport {

    @Test
    @DisplayName("the owner shares an editor role; statuses open, active, closed and archived; members are listed")
    void ownersMembersAndStatuses() throws Exception {
        Person jo = person("jo-i", Scope.AGENT, null);
        Person sam = person("sam-i", Scope.AGENT, null);
        JsonNode created = body(call(jo.bearer(), json(post("/v1/investigations"), "{'title':'Port closures'}")));
        String id = created.get("id").asString();
        assertThat(created.get("owner").get("handle").asString()).isEqualTo("jo-i");
        assertThat(created.get("visibility").asString()).isEqualTo("instance");
        assertThat(created.get("tlp").asString()).isEqualTo("AMBER");
        assertThat(call(sam.bearer(), get("/v1/investigations/" + id)).getStatus()).isEqualTo(200);
        assertThat(call(sam.bearer(), json(patch("/v1/investigations/" + id), "{'status':'active'}")).getStatus())
                .isEqualTo(403);
        call(jo.bearer(), json(post(objectPath("investigation:" + id, "/share")), "{'grantees':['principal:sam-i'],"
                + "'role':'editor'}"));
        assertThat(call(sam.bearer(), json(patch("/v1/investigations/" + id), "{'status':'active',"
                + "'workspaceId':'01JA8Q3V7S2K9W4T6Y1B5N0W01','questionIds':['01JA8Q3V7S2K9W4T6Y1B5N0Q01']}"))
                .getStatus()).isEqualTo(200);
        JsonNode read = body(call(jo.bearer(), get("/v1/investigations/" + id)));
        assertThat(read.get("status").asString()).isEqualTo("active");
        assertThat(read.get("workspaceId").asString()).isEqualTo("01JA8Q3V7S2K9W4T6Y1B5N0W01");
        assertThat(read.get("members")).extracting(m -> m.get("granteeDisplay").asString()).containsExactly("sam-i");
        assertThat(call(jo.bearer(), json(patch("/v1/investigations/" + id), "{'status':'archived'}")).getStatus())
                .isEqualTo(200);
        assertThat(call(jo.bearer(), json(patch("/v1/investigations/" + id), "{'status':'bogus'}")).getStatus())
                .isEqualTo(400);
        assertThat(call(sam.bearer(), org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/v1/investigations/" + id)).getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("display ids work in every path, including object references and exports with the TLP label")
    void displayIds() throws Exception {
        Person jo = person("jo-disp", Scope.AGENT, null);
        JsonNode created = body(call(jo.bearer(), json(post("/v1/investigations"), "{'title':'Case one'}")));
        String display = created.get("displayId").asString();
        assertThat(body(call(jo.bearer(), get("/v1/investigations/" + display))).get("id").asString())
                .isEqualTo(created.get("id").asString());
        assertThat(call(jo.bearer(), json(patch("/v1/investigations/" + display), "{'title':'Case 1'}"))
                .getStatus()).isEqualTo(200);
        MockHttpServletResponse export = call(jo.bearer(), get("/v1/investigations/" + display
                + "/export?format=md"));
        assertThat(export.getContentAsString()).startsWith("**TLP:AMBER**");
        assertThat(export.getHeader("Foresignal-TLP")).isEqualTo("AMBER");
        JsonNode envelope = body(call(jo.bearer(), get(objectPath("investigation:" + display, ""))));
        assertThat(envelope.get("ref").asString()).isEqualTo("investigation:" + created.get("id").asString());
        assertThat(envelope.get("tlp").asString()).isEqualTo("AMBER");
        assertThat(body(call(jo.bearer(), get(objectPath("investigation:" + display, "/acl")))).get("owner")
                .get("handle").asString()).isEqualTo("jo-disp");
    }

    @Test
    @DisplayName("a private investigation is invisible to others; scope mine, shared and all; shared with me")
    void privateAndListings() throws Exception {
        Person jo = person("jo-p", Scope.AGENT, null);
        Person sam = person("sam-p", Scope.READ, null);
        Person kim = person("kim-p", Scope.READ, null);
        String id = body(call(jo.bearer(), json(post("/v1/investigations"), "{'title':'Quiet case',"
                + "'visibility':'private'}"))).get("id").asString();
        MockHttpServletResponse hidden = call(kim.bearer(), get("/v1/investigations/" + id));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(body(hidden).get("code").asString()).isEqualTo("UNKNOWN_INVESTIGATION");
        assertThat(call(kim.bearer(), get(objectPath("investigation:" + id, ""))).getStatus()).isEqualTo(404);
        assertThat(body(call(kim.bearer(), get("/v1/investigations"))).get("items")).isEmpty();
        assertThat(body(call(jo.bearer(), get("/v1/investigations?scope=mine"))).get("items")).hasSize(1);

        String group = body(call(jo.bearer(), json(post("/v1/groups"), "{'name':'desk','members':[{'principalId':'"
                + kim.handle() + "'}]}"))).get("id").asString();
        call(jo.bearer(), json(post(objectPath("investigation:" + id, "/share")), "{'grantees':['principal:"
                + sam.id() + "','group:" + group + "'],'role':'viewer','message':'FYI'}"));
        assertThat(inboxEvents(sam.id())).contains(MeEvents.SHARE_RECEIVED);
        assertThat(inboxEvents(kim.id())).contains(MeEvents.SHARE_RECEIVED);
        JsonNode samShared = body(call(sam.bearer(), get("/v1/shared-with-me?type=investigation")));
        assertThat(samShared.get("items")).hasSize(1);
        assertThat(samShared.get("items").get(0).get("via").asString()).isEqualTo("principal");
        assertThat(samShared.get("items").get(0).get("role").asString()).isEqualTo("viewer");
        JsonNode kimShared = body(call(kim.bearer(), get("/v1/shared-with-me")));
        assertThat(kimShared.get("items").get(0).get("via").asString()).isEqualTo("group");
        assertThat(kimShared.get("items").get(0).get("groupId").asString()).isEqualTo(group);
        assertThat(body(call(sam.bearer(), get("/v1/investigations?scope=shared"))).get("items")).hasSize(1);
        assertThat(body(call(sam.bearer(), get("/v1/investigations?scope=mine"))).get("items")).isEmpty();
        assertThat(body(call(jo.bearer(), get("/v1/my?type=investigation"))).get("items")).hasSize(1);
        assertThat(body(call(jo.bearer(), get("/v1/shared-with-me"))).get("items")).isEmpty();
    }

    @Test
    @DisplayName("only owners transfer ownership; RED forbids instance visibility; every change is audited")
    void aclTransferTlpAndAudit() throws Exception {
        Person jo = person("jo-x", Scope.AGENT, null);
        Person sam = person("sam-x", Scope.AGENT, null);
        String id = body(call(jo.bearer(), json(post("/v1/investigations"), "{'title':'Handover',"
                + "'visibility':'private'}"))).get("id").asString();
        String ref = "investigation:" + id;
        assertThat(call(jo.bearer(), json(put(objectPath(ref, "/acl")), "{'entries':[{'grantee':'principal:sam-x',"
                + "'role':'editor'}]}")).getStatus()).isEqualTo(200);
        assertThat(call(sam.bearer(), json(put(objectPath(ref, "/acl")), "{'entries':[],'owner':'sam-x'}"))
                .getStatus()).isEqualTo(403);
        assertThat(call(sam.bearer(), json(put(objectPath(ref, "/acl")), "{'entries':[{'grantee':'principal:sam-x',"
                + "'role':'owner'}]}")).getStatus()).isEqualTo(403);
        MockHttpServletResponse red = call(jo.bearer(), json(put(objectPath(ref, "/acl")), "{'entries':[],"
                + "'visibility':'instance','tlp':'RED'}"));
        assertThat(red.getStatus()).isEqualTo(403);
        assertThat(body(red).get("code").asString()).isEqualTo("TLP_VIOLATION");
        assertThat(body(red).get("rule").asString()).contains("RED");
        MockHttpServletResponse unknown = call(jo.bearer(), json(post(objectPath(ref, "/share")),
                "{'grantees':['principal:nobody']}"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("UNKNOWN_PRINCIPAL");
        MockHttpServletResponse transferred = call(jo.bearer(), json(put(objectPath(ref, "/acl")),
                "{'entries':[{'grantee':'principal:jo-x','role':'editor'}],'owner':'sam-x'}"));
        assertThat(transferred.getStatus()).isEqualTo(200);
        assertThat(body(transferred).get("owner").get("handle").asString()).isEqualTo("sam-x");
        assertThat(call(sam.bearer(), get(objectPath(ref, "/acl"))).getStatus()).isEqualTo(200);
        assertThat(auditOf(CollabAudit.TARGET_ACL)).extracting(r -> r.get("target_id")).contains(ref);
        assertThat(auditOf(CollabAudit.TARGET_ACL).toString()).contains("ownerTo");
        assertThat(call(jo.bearer(), get(objectPath("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "/acl"))).getStatus())
                .isEqualTo(400);
    }
}
