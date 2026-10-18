package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.GetRelatedChroniclesTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * The chronicle-network endpoints (prompt 134): tokens, scopes, ownership, Problem codes and the MCP tool's shape.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleNetworkControllersIT extends NetworkItSupport {

    private static final String UNKNOWN = "01JA8Q3V7S2K9W4T6Y1B5N0M3Z";

    @Autowired GetRelatedChroniclesTool relatedTool;

    @AfterEach
    void federationOff() {
        federatedDiscovery(false);
    }

    private static McpCaller mcp(Person p) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), p.handle() + "-mcp", p.scope(),
                TokenRestrictions.NONE, null, p.id(), false, false);
        return new McpCaller(token, new ReadCaller(p.scope(), false, false, ReadRestriction.NONE, false),
                Set.of(AskTestSources.SECRET), null);
    }

    @Test
    @DisplayName("related and discoverability: tokens, visibility, ownership and the TLP rule")
    void relatedAndDiscoverability() throws Exception {
        Person alice = person("alice-api", Scope.AGENT, null);
        Person editor = person("eve-api", Scope.AGENT, null);
        Person reader = person("rita-api", Scope.READ, null);
        ChronicleEntity a = active(alice, "Front A");

        assertThat(call(null, get("/v1/chronicles/" + a.getId() + "/related")).getStatus()).isEqualTo(401);
        MockHttpServletResponse hidden = call(reader.bearer(), get("/v1/chronicles/" + a.getId() + "/related"));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(code(hidden)).isEqualTo("UNKNOWN_CHRONICLE");
        MockHttpServletResponse own = call(alice.bearer(), get("/v1/chronicles/" + a.getDisplayId()
                + "/related?minStrength=medium&includeDismissed=true"));
        assertThat(own.getStatus()).isEqualTo(200);
        assertThat(body(own).get("items")).isEmpty();
        assertThat(body(own).get("nextCursor").isNull()).isTrue();
        assertThat(call(alice.bearer(), get("/v1/chronicles/" + a.getId() + "/related?minStrength=huge"))
                .getStatus()).isEqualTo(400);

        assertThat(call(reader.bearer(), json(put("/v1/chronicles/" + a.getId() + "/discoverability"),
                "{'discoverability':'instance'}")).getStatus()).as("agent scope").isEqualTo(403);
        shares.share("chronicle:" + a.getId(), node("{'grantees':['principal:" + editor.id() + "'],'role':'editor'}"),
                alice.caller(), "test");
        MockHttpServletResponse notOwner = call(editor.bearer(), json(put("/v1/chronicles/" + a.getId()
                + "/discoverability"), "{'discoverability':'instance'}"));
        assertThat(notOwner.getStatus()).isEqualTo(403);
        MockHttpServletResponse set = call(alice.bearer(), json(put("/v1/chronicles/" + a.getId()
                + "/discoverability"), "{'discoverability':'network','revealTitle':true}"));
        assertThat(set.getStatus()).as(set.getContentAsString()).isEqualTo(200);
        JsonNode effective = body(set);
        assertThat(effective.get("discoverability").asString()).isEqualTo("network");
        assertThat(effective.get("revealTitle").asBoolean()).isTrue();
        assertThat(effective.get("revealOwner").asBoolean()).isFalse();
        assertThat(effective.get("maxAllowed").asString()).isEqualTo("network");
        assertThat(body(call(alice.bearer(), get("/v1/chronicles/" + a.getId()))).get("discoverability").asString())
                .isEqualTo("network");

        shares.replace("chronicle:" + a.getId(), node("{'entries':[],'tlp':'AMBER+STRICT'}"), alice.caller(), "test");
        assertThat(body(call(alice.bearer(), get("/v1/chronicles/" + a.getId()))).get("discoverability").asString())
                .as("lowered by the stricter label").isEqualTo("instance");
        MockHttpServletResponse refused = call(alice.bearer(), json(put("/v1/chronicles/" + a.getId()
                + "/discoverability"), "{'discoverability':'network'}"));
        assertThat(refused.getStatus()).isEqualTo(409);
        assertThat(code(refused)).isEqualTo("DISCOVERABILITY_NOT_ALLOWED");
        assertThat(body(refused).get("rule").asString()).contains("AMBER+STRICT");
        assertThat(call(alice.bearer(), json(put("/v1/chronicles/" + a.getId() + "/discoverability"),
                "{'discoverability':'everyone'}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("links, clusters, timelines and requests: unknown ids are 404 with their codes")
    void unknowns() throws Exception {
        Person alice = person("alice-unk", Scope.AGENT, null);
        MockHttpServletResponse dismiss = call(alice.bearer(), post("/v1/chronicle-network/links/" + UNKNOWN
                + "/dismiss"));
        assertThat(dismiss.getStatus()).isEqualTo(404);
        assertThat(code(dismiss)).isEqualTo("UNKNOWN_LINK");
        assertThat(code(call(alice.bearer(), post("/v1/chronicle-network/links/" + UNKNOWN + "/request-access"))))
                .isEqualTo("UNKNOWN_LINK");
        assertThat(call(alice.bearer(), get("/v1/chronicle-network/clusters")).getStatus()).isEqualTo(200);
        assertThat(code(call(alice.bearer(), get("/v1/chronicle-network/clusters/" + UNKNOWN))))
                .isEqualTo("UNKNOWN_CLUSTER");
        assertThat(code(call(alice.bearer(), get("/v1/chronicle-network/clusters/" + UNKNOWN + "/timeline"))))
                .isEqualTo("UNKNOWN_CLUSTER");
        assertThat(call(alice.bearer(), get("/v1/chronicle-network/requests?direction=incoming&status=pending"))
                .getStatus()).isEqualTo(200);
        assertThat(call(alice.bearer(), get("/v1/chronicle-network/requests?direction=sideways")).getStatus())
                .isEqualTo(400);
        MockHttpServletResponse grant = call(alice.bearer(), post("/v1/chronicle-network/requests/" + UNKNOWN
                + "/grant"));
        assertThat(grant.getStatus()).isEqualTo(404);
        assertThat(code(grant)).isEqualTo("UNKNOWN_ACCESS_REQUEST");
        assertThat(call(null, get("/v1/chronicle-network/clusters")).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("dismissing is for the owner of either side and hides the link unless asked for")
    void dismiss() throws Exception {
        Person alice = person("alice-dis", Scope.AGENT, null);
        Person bob = person("bob-dis", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        ChronicleEntity b = active(bob, "Front B");
        war("z", 60, 30.0);
        discoverable(alice, a, "instance");
        sketchAndDiscover();
        String linkId = linkRows.findAll().get(0).getId();
        Person carol = person("carol-dis", Scope.READ, null);
        assertThat(call(carol.bearer(), post("/v1/chronicle-network/links/" + linkId + "/dismiss")).getStatus())
                .isEqualTo(404);
        assertThat(call(bob.bearer(), post("/v1/chronicle-network/links/" + linkId + "/dismiss")).getStatus())
                .isEqualTo(204);
        assertThat(body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"))).get("items")).isEmpty();
        assertThat(body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related?includeDismissed=true")))
                .get("items")).hasSize(1);
        sketchAndDiscover();
        assertThat(linkRows.findById(linkId).orElseThrow().getStatus()).as("a dismissal holds")
                .isEqualTo(NetworkVocabulary.DISMISSED);
    }

    @Test
    @DisplayName("salts are admin-only, need network.federated, and are never returned")
    void salts() throws Exception {
        Person alice = person("alice-salt-api", Scope.AGENT, null);
        String body = "{'networkId':'gulf-net','saltRef':'agreed-out-of-band-0123456789'}";
        assertThat(call(alice.bearer(), json(put("/v1/chronicle-network/salts"), body)).getStatus()).isEqualTo(403);
        MockHttpServletResponse off = call(admin, json(put("/v1/chronicle-network/salts"), body));
        assertThat(off.getStatus()).isEqualTo(409);
        assertThat(code(off)).isEqualTo("FEDERATED_DISCOVERY_DISABLED");
        federatedDiscovery(true);
        MockHttpServletResponse on = call(admin, json(put("/v1/chronicle-network/salts"), body));
        assertThat(on.getStatus()).isEqualTo(200);
        JsonNode status = body(on);
        assertThat(status.get("networkId").asString()).isEqualTo("gulf-net");
        assertThat(status.get("saltId").asString()).startsWith("net-");
        assertThat(status.has("saltRef")).isFalse();
        assertThat(on.getContentAsString()).doesNotContain("agreed-out-of-band");
        assertThat(call(admin, json(put("/v1/chronicle-network/salts"), "{'networkId':'X','saltRef':'short'}"))
                .getStatus()).isEqualTo(400);
        Long stored = jdbc.queryForObject("SELECT count(*) FROM chronicle_salt WHERE kind = 'network'", Long.class);
        assertThat(stored).isEqualTo(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("MCP get_related_chronicles is a read tool with the related page's shape")
    void mcpTool() {
        Person alice = person("alice-mcp", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        assertThat(relatedTool.name()).isEqualTo("get_related_chronicles");
        assertThat(relatedTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(relatedTool.alwaysAuthenticated()).isTrue();
        assertThat(relatedTool.inputSchema()).containsEntry("required", java.util.List.of("chronicleId"));
        assertThat((Map<String, Object>) relatedTool.inputSchema().get("properties"))
                .containsKeys("chronicleId", "minStrength", "includeDismissed", "cursor", "limit");
        JsonNode page = relatedTool.call(new ToolContext(mcp(alice), null, "test"), node("{'chronicleId':'"
                + a.getId() + "'}")).structured();
        assertThat(page.has("items")).isTrue();
        assertThat(page.has("nextCursor")).isTrue();
    }
}
