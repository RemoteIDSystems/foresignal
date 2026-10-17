package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Principals for every token (prompt 130 over 023): existing tokens each have a principal, the bootstrap admin token
 * belongs to {@code owner}, {@code GET /v1/me} describes the caller, and a disabled principal's tokens stop working.
 */
@IntegrationTest
@Import(AskTestSources.class)
class PrincipalMigrationIT extends CollabItSupport {

    @Test
    @DisplayName("every token has a principal; a token created without one gets one named after it")
    void everyTokenHasAPrincipal() {
        Long orphans = jdbc.queryForObject("SELECT count(*) FROM api_token t LEFT JOIN principal p ON p.id ="
                + " t.principal_id WHERE p.id IS NULL", Long.class);
        assertThat(orphans).isZero();
        String handle = jdbc.queryForObject("SELECT p.handle FROM api_token t JOIN principal p ON p.id ="
                + " t.principal_id WHERE t.name = 'ask-reader'", String.class);
        assertThat(handle).startsWith("ask-reader");
    }

    @Test
    @DisplayName("the bootstrap admin token belongs to the owner principal")
    void bootstrapBelongsToOwner() throws Exception {
        JsonNode me = body(call(admin, get("/v1/me")));
        assertThat(me.get("principal").get("handle").asString()).isEqualTo(PrincipalEntity.OWNER_HANDLE);
        assertThat(me.get("token").get("scope").asString()).isEqualTo("admin");
        assertThat(me.get("scopes")).extracting(JsonNode::asString).containsExactly("read", "agent", "admin");
    }

    @Test
    @DisplayName("GET /v1/me returns the principal in full, its groups, preferences and the default TLP")
    void me() throws Exception {
        Person jo = person("jo", Scope.AGENT, "Example Analysis");
        groups.create(node("{'name':'Red Sea desk'}"), jo.caller());
        MockHttpServletResponse response = call(jo.bearer(), get("/v1/me"));
        assertThat(response.getStatus()).isEqualTo(200);
        JsonNode me = body(response);
        assertThat(me.get("principal").get("id").asString()).isEqualTo(jo.id());
        assertThat(me.get("principal").get("organization").asString()).isEqualTo("Example Analysis");
        assertThat(me.get("token").get("tokenPrefix").asString()).startsWith("fs");
        assertThat(me.get("scopes")).extracting(JsonNode::asString).containsExactly("read", "agent");
        assertThat(me.get("groups").get(0).get("role").asString()).isEqualTo("manager");
        assertThat(me.get("notificationPreferences")).isEmpty();
        assertThat(me.get("defaultTlp").asString()).isEqualTo("AMBER");
        assertThat(call(null, get("/v1/me")).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a disabled principal's tokens stop working; the bootstrap token never does")
    void disabledPrincipal() throws Exception {
        Person jo = person("jo-disabled", Scope.READ, null);
        assertThat(call(jo.bearer(), get("/v1/me")).getStatus()).isEqualTo(200);
        MockHttpServletResponse disabled = call(admin, mergePatch(patch("/v1/principals/" + jo.id()),
                "{'disabled':true}"));
        assertThat(disabled.getStatus()).isEqualTo(200);
        assertThat(body(disabled).get("disabled").asBoolean()).isTrue();
        assertThat(call(jo.bearer(), get("/v1/me")).getStatus()).isEqualTo(401);
        call(admin, mergePatch(patch("/v1/principals/" + jo.id()), "{'disabled':false}"));
        assertThat(call(jo.bearer(), get("/v1/me")).getStatus()).isEqualTo(200);
    }
}
