package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

/**
 * Evidence items (prompt 130): Admiralty grades, TLP inheritance from attachments, use as ACH evidence (125),
 * embedding for search (027) only for CLEAR and GREEN labels, investigation items, and the 116 originator.
 */
@IntegrationTest
@Import(AskTestSources.class)
class EvidenceItemIT extends CollabItSupport {

    @Autowired HypothesisService hypotheses;
    @Autowired OriginatorRegistry originators;

    String upload(Person p, String text, String tlp) throws Exception {
        return body(call(p.bearer(), multipart("/v1/attachments").file(new MockMultipartFile("file", "n.txt",
                "text/plain", text.getBytes(StandardCharsets.UTF_8))).param("tlp", tlp))).get("id").asString();
    }

    @Test
    @DisplayName("Admiralty grades are validated and shown; the originator is operator:{handle} or the cited source")
    void gradesAndOriginator() throws Exception {
        Person jo = person("jo-e", Scope.READ, null);
        MockHttpServletResponse created = call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Notice',"
                + "'sourceReliability':'B','informationCredibility':2,'url':'https://example.org/n',"
                + "'geometry':{'type':'Point','coordinates':[45.0,12.8]},'eventTime':'2026-10-12T08:00:00Z'}"));
        assertThat(created.getStatus()).isEqualTo(201);
        JsonNode item = body(created);
        assertThat(item.get("grade").asString()).isEqualTo("B2");
        assertThat(item.get("originator").asString()).isEqualTo("operator:jo-e");
        assertThat(item.get("caveats")).extracting(n -> n.get("code").asString()).contains("LINK_NOT_FETCHED");
        assertThat(item.get("geometry").get("type").asString()).isEqualTo("Point");
        JsonNode defaults = body(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Rumor'}")));
        assertThat(defaults.get("grade").asString()).isEqualTo("F6");
        assertThat(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'x','sourceReliability':'G'}"))
                .getStatus()).isEqualTo(400);
        assertThat(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'x','informationCredibility':7}"))
                .getStatus()).isEqualTo(400);
        assertThat(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'x','url':'file:///etc/passwd'}"))
                .getStatus()).isEqualTo(400);
        JsonNode cited = body(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'From the feed',"
                + "'citedSourceId':'" + AskTestSources.QUAKES + "'}")));
        assertThat(cited.get("originator").asString()).isEqualTo(originators.resolve(AskTestSources.QUAKES).id());
        MockHttpServletResponse filtered = call(jo.bearer(), get("/v1/evidence-items?sourceReliability=B"));
        assertThat(body(filtered).get("items")).hasSize(1);
    }

    @Test
    @DisplayName("an evidence item inherits the strictest attachment label and cannot ask for a weaker one")
    void tlpInheritance() throws Exception {
        Person jo = person("jo-t", Scope.READ, null);
        String red = upload(jo, "source names", "RED");
        JsonNode item = body(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Sensitive',"
                + "'attachmentIds':['" + red + "']}")));
        assertThat(item.get("tlp").asString()).isEqualTo("RED");
        MockHttpServletResponse weaker = call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Leak',"
                + "'attachmentIds':['" + red + "'],'tlp':'GREEN'}"));
        assertThat(weaker.getStatus()).isEqualTo(403);
        assertThat(body(weaker).get("code").asString()).isEqualTo("TLP_VIOLATION");
        assertThat(body(weaker).get("rule").asString()).contains("strictest");
    }

    @Test
    @DisplayName("an evidence item the caller sees is ACH evidence; one it cannot see is not found")
    void achEvidence() throws Exception {
        Person jo = person("jo-ach", Scope.AGENT, null);
        Person sam = person("sam-ach", Scope.AGENT, null);
        String mine = body(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Port notice',"
                + "'sourceReliability':'A','informationCredibility':1}"))).get("id").asString();
        String theirs = body(call(sam.bearer(), json(post("/v1/evidence-items"), "{'title':'Private tip'}")))
                .get("id").asString();
        HypothesisSetEntity set = hypotheses.create(HypothesisInputs.set(JSON.readTree("{\"question\":\"Closure"
                + " within 7 days?\",\"kind\":\"exclusive\",\"cadence\":\"daily\",\"regionCodes\":[\"YE\"],"
                + "\"hypotheses\":[{\"label\":\"yes\",\"statement\":\"Closes.\",\"type\":\"outcome\",\"prior\":0.5,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Even.\"}},{\"label\":\"no\",\"statement\":"
                + "\"Stays open.\",\"type\":\"outcome\",\"prior\":0.45,\"priorBasis\":{\"type\":\"operator\","
                + "\"rationale\":\"Even.\"}},{\"label\":\"other\",\"statement\":\"Other.\",\"type\":\"other\","
                + "\"prior\":0.05,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Residual.\"}}],"
                + "\"indicators\":[],\"assumptions\":[]}")), "operator", "tester");
        MockHttpServletResponse added = call(jo.bearer(), json(post("/v1/hypotheses/sets/" + set.getId()
                + "/evidence"), "{'source':'evidence-item','ref':'" + mine + "','description':'Notice posted',"
                + "'credibility':'H','relevance':'M'}"));
        assertThat(added.getStatus()).isEqualTo(201);
        assertThat(body(added).get("ref").asString()).isEqualTo("evidence-item:" + mine);
        MockHttpServletResponse hidden = call(jo.bearer(), json(post("/v1/hypotheses/sets/" + set.getId()
                + "/evidence"), "{'source':'evidence-item','ref':'evidence-item:" + theirs + "','description':'x',"
                + "'credibility':'L','relevance':'L'}"));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(body(hidden).get("code").asString()).isEqualTo("UNKNOWN_EVIDENCE_ITEM");
    }

    @Test
    @DisplayName("GREEN items are embedded with their attachments' text; AMBER items never reach the index")
    void embedding() throws Exception {
        Person jo = person("jo-emb", Scope.READ, null);
        String att = upload(jo, "Harbor master confirms dredging", "GREEN");
        String green = body(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Dredging','tlp':'GREEN',"
                + "'attachmentIds':['" + att + "']}"))).get("id").asString();
        String amber = body(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Quiet tip'}")))
                .get("id").asString();
        List<String> greenText = jdbc.queryForList("SELECT text FROM embedding_document WHERE doc_type ="
                + " 'evidence_item' AND doc_id = ?", String.class, green);
        assertThat(greenText).hasSize(1);
        assertThat(greenText.get(0)).contains("Harbor master confirms dredging");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE doc_type = 'evidence_item'"
                + " AND doc_id = ?", Long.class, amber)).isZero();
        call(jo.bearer(), mergePatch(patch("/v1/evidence-items/" + green), "{'tlp':'AMBER'}"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE doc_type = 'evidence_item'"
                + " AND doc_id = ?", Long.class, green)).isZero();
    }

    @Test
    @DisplayName("an evidence item is an investigation item, and a RED item raises the investigation's label")
    void investigationItem() throws Exception {
        Person jo = person("jo-inv", Scope.AGENT, null);
        String red = upload(jo, "names", "RED");
        String item = body(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Names',"
                + "'attachmentIds':['" + red + "']}"))).get("id").asString();
        String inv = body(call(jo.bearer(), json(post("/v1/investigations"), "{'title':'Case',"
                + "'visibility':'private'}"))).get("id").asString();
        MockHttpServletResponse added = call(jo.bearer(), json(post("/v1/investigations/" + inv + "/items"),
                "{'itemType':'evidence-item','ref':'" + item + "','note':'key evidence'}"));
        assertThat(added.getStatus()).isEqualTo(201);
        assertThat(body(added).get("objectRef").asString()).isEqualTo("evidence-item:" + item);
        JsonNode investigation = body(call(jo.bearer(), get("/v1/investigations/" + inv)));
        assertThat(investigation.get("tlp").asString()).isEqualTo("RED");
        JsonNode listed = body(call(jo.bearer(), get("/v1/evidence-items?investigationId=" + inv)));
        assertThat(listed.get("items")).extracting(n -> n.get("id").asString()).containsExactly(item);
    }
}
