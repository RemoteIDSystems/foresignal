package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Saved searches (prompt 130): results over envelopes the caller can see (text, tags and filters), validation,
 * sharing, and new-match notifications at most hourly.
 */
@IntegrationTest
@Import(AskTestSources.class)
class SavedSearchIT extends CollabItSupport {

    String evidence(Person p, String title, String tlp) throws Exception {
        return body(call(p.bearer(), json(post("/v1/evidence-items"), "{'title':'" + title + "','tlp':'" + tlp
                + "'}"))).get("id").asString();
    }

    @Test
    @DisplayName("results are the matching envelopes the caller can see")
    void results() throws Exception {
        // jo shares the search below, and shareObject needs the agent scope.
        Person jo = person("jo-s", Scope.AGENT, null);
        Person sam = person("sam-s", Scope.READ, null);
        String mine = evidence(jo, "Berth closure at Aden", "AMBER");
        String green = evidence(jo, "Berth closure confirmed by harbor master", "GREEN");
        String theirs = evidence(sam, "Berth closure rumor", "AMBER");
        evidence(jo, "Unrelated weather note", "AMBER");
        MockHttpServletResponse created = call(jo.bearer(), json(post("/v1/saved-searches"), "{'name':'Berths',"
                + "'query':{'types':['evidence-item'],'text':'berth closure'}}"));
        assertThat(created.getStatus()).isEqualTo(201);
        String id = body(created).get("id").asString();
        JsonNode results = body(call(jo.bearer(), get("/v1/saved-searches/" + id + "/results")));
        assertThat(results.get("items")).extracting(i -> i.get("envelope").get("ref").asString())
                .contains("evidence-item:" + mine, "evidence-item:" + green)
                .doesNotContain("evidence-item:" + theirs);
        assertThat(results.get("ranAt").isNull()).isFalse();
        MockHttpServletResponse hidden = call(sam.bearer(), get("/v1/saved-searches/" + id + "/results"));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(body(hidden).get("code").asString()).isEqualTo("UNKNOWN_SAVED_SEARCH");
        MockHttpServletResponse shared = call(jo.bearer(), json(post(objectPath("saved-search:" + id, "/share")),
                "{'grantees':['principal:sam-s']}"));
        assertThat(shared.getStatus()).as(shared.getContentAsString()).isEqualTo(200);
        MockHttpServletResponse samResults = call(sam.bearer(), get("/v1/saved-searches/" + id + "/results"));
        assertThat(samResults.getStatus()).as(samResults.getContentAsString()).isEqualTo(200);
        JsonNode forSam = body(samResults);
        assertThat(forSam.get("items")).extracting(i -> i.get("envelope").get("ref").asString())
                .contains("evidence-item:" + theirs).doesNotContain("evidence-item:" + mine);
    }

    @Test
    @DisplayName("a tag-only query lists the tagged objects; a query needs text or tags")
    void tagsAndValidation() throws Exception {
        quake("s1", "Aden", 4.5, 45.0, 12.8);
        Person jo = person("jo-tagq", Scope.READ, null);
        String tag = body(call(jo.bearer(), json(post("/v1/tags"), "{'name':'watch'}"))).get("id").asString();
        String ref = "record:" + AskTestSources.QUAKES + "/s1";
        call(jo.bearer(), json(post("/v1/tags/" + tag + "/assign"), "{'refs':['" + ref + "']}"));
        String id = body(call(jo.bearer(), json(post("/v1/saved-searches"), "{'name':'Watched','query':{'tags':['"
                + tag + "'],'area':{'bbox':'40,10,50,20'}}}"))).get("id").asString();
        JsonNode results = body(call(jo.bearer(), get("/v1/saved-searches/" + id + "/results")));
        assertThat(results.get("items")).extracting(i -> i.get("envelope").get("ref").asString()).containsExactly(ref);
        assertThat(results.get("items").get(0).get("matchedBy").asString()).isEqualTo("filter");
        String far = body(call(jo.bearer(), json(post("/v1/saved-searches"), "{'name':'Far','query':{'tags':['"
                + tag + "'],'area':{'bbox':'-10,-10,0,0'}}}"))).get("id").asString();
        assertThat(body(call(jo.bearer(), get("/v1/saved-searches/" + far + "/results"))).get("items")).isEmpty();
        assertThat(call(jo.bearer(), json(post("/v1/saved-searches"), "{'name':'Empty','query':{}}")).getStatus())
                .isEqualTo(400);
        assertThat(call(jo.bearer(), json(post("/v1/saved-searches"), "{'name':'Bad','query':{'text':'x',"
                + "'types':['nonsense']}}")).getStatus()).isEqualTo(400);
        assertThat(body(call(jo.bearer(), get("/v1/saved-searches"))).get("items")).hasSize(2);
    }

    @Test
    @DisplayName("new matches notify the owner at most hourly; the first run only records the baseline")
    void hourlyNotifications() throws Exception {
        Person jo = person("jo-n", Scope.READ, null);
        evidence(jo, "Convoy sighted", "AMBER");
        String id = body(call(jo.bearer(), json(post("/v1/saved-searches"), "{'name':'Convoys','notify':true,"
                + "'query':{'types':['evidence-item'],'text':'convoy'}}"))).get("id").asString();
        assertThat(savedSearches.notifyDue()).isZero();
        assertThat(body(call(jo.bearer(), get("/v1/saved-searches/" + id))).get("lastMatchCount").asInt())
                .isEqualTo(1);
        evidence(jo, "Second convoy sighted", "AMBER");
        clock.advance(Duration.ofMinutes(15));
        assertThat(savedSearches.notifyDue()).isEqualTo(1);
        assertThat(inboxEvents(jo.id())).containsExactly(MeEvents.SAVED_SEARCH_MATCHES);
        evidence(jo, "Third convoy sighted", "AMBER");
        clock.advance(Duration.ofMinutes(15));
        assertThat(savedSearches.notifyDue()).isZero();
        clock.advance(Duration.ofMinutes(50));
        assertThat(savedSearches.notifyDue()).isEqualTo(1);
        assertThat(inboxEvents(jo.id())).containsExactly(MeEvents.SAVED_SEARCH_MATCHES,
                MeEvents.SAVED_SEARCH_MATCHES);
        call(jo.bearer(), mergePatch(patch("/v1/saved-searches/" + id), "{'notify':false}"));
        evidence(jo, "Fourth convoy sighted", "AMBER");
        clock.advance(Duration.ofHours(2));
        assertThat(savedSearches.notifyDue()).isZero();
        JsonNode inbox = body(call(jo.bearer(), get("/v1/inbox?category=saved-search")));
        assertThat(inbox.get("items")).hasSize(2);
        assertThat(inbox.get("items").get(0).get("ref").asString()).isEqualTo("saved-search:" + id);
    }
}
