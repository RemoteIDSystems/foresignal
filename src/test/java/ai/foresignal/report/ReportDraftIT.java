package ai.foresignal.report;

import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.collab.EvidenceItemEntity;
import ai.foresignal.collab.EvidenceItemService;
import ai.foresignal.collab.TagEntity;
import ai.foresignal.collab.Visibility;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.security.Scope;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Report drafts (prompt 132, {@code draftReport}): drafts from a situation, an investigation, a chronicle, a tag and a
 * selection hold the expected blocks, are deterministic and need no LLM; the optional narrative is a labeled
 * {@code llm} block whose citations were verified (fake provider), needs agent scope and a configured provider.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ReportDraftIT extends ReportItSupport {

    @Autowired InvestigationService investigations;
    @Autowired EvidenceItemService evidence;
    @Autowired ChronicleService chronicles;

    private JsonNode draft(Person who, String body) throws Exception {
        MockHttpServletResponse response = call(who.bearer(), json(post("/v1/reports/draft"), body));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response);
    }

    /** The deterministic content of a draft: each block's type, title, text origin and text, without ids. */
    private static List<String> content(JsonNode report) {
        List<String> out = new ArrayList<>();
        for (JsonNode b : report.get("blocks")) {
            out.add(b.get("type").asString() + "|" + b.path("title").asString("") + "|"
                    + b.get("textOrigin").asString() + "|" + b.path("markdown").asString("") + "|"
                    + b.path("judgments").toString() + "|" + b.path("items").size());
        }
        return out;
    }

    @Test
    @DisplayName("a situation brief holds key judgments, summary, timeline, members, hypotheses and forecasts, without"
            + " an LLM, the same every time")
    void situationBrief() throws Exception {
        Person jo = person("jo-draft", Scope.AGENT, null);
        SituationEntity s = situation("Red Sea shipping disruption");
        HypothesisSetEntity set = hypothesisSet(s.getId());
        ForecastQuestionEntity q = question("Strike on Hodeidah port", set.getId(), s.getId());
        String body = "{'source':{'type':'situation','ref':'situation:" + s.getId() + "'},"
                + "'template':'situation-brief'}";

        JsonNode first = draft(jo, body);
        assertThat(first.get("displayId").asString()).matches("RPT-\\d{4}-\\d{4,}");
        assertThat(first.get("status").asString()).isEqualTo("draft");
        assertThat(first.get("template").asString()).isEqualTo("situation-brief");
        assertThat(first.get("title").asString()).contains("Red Sea shipping disruption");
        assertThat(first.get("owner").get("handle").asString()).isEqualTo("jo-draft");
        assertThat(first.get("source").get("ref").asString()).isEqualTo("situation:" + s.getId());
        assertThat(first.get("machineGenerated").asBoolean()).isTrue();
        assertThat(blockTypes(first)).contains("bluf", "markdown", "timeline", "objects", "hypotheses", "forecasts",
                "coverage");
        for (JsonNode b : first.get("blocks")) {
            assertThat(b.get("textOrigin").asString()).isEqualTo("deterministic");
            assertThat(b.get("id").asString()).matches("[A-Za-z0-9_-]{1,64}");
        }
        JsonNode bluf = block(first, "bluf");
        boolean setJudgment = false;
        boolean questionJudgment = false;
        for (JsonNode j : bluf.get("judgments")) {
            if (j.path("hypothesisSetRef").asString("").equals("hypothesis-set:" + set.getId())) {
                setJudgment = true;
                assertThat(j.get("probability").asDouble()).isBetween(0.0, 1.0);
                assertThat(j.get("icdTerm").asString()).isNotBlank();
            }
            if (j.path("questionRef").asString("").equals("question:" + q.getId())) {
                questionJudgment = true;
            }
        }
        assertThat(setJudgment).isTrue();
        assertThat(questionJudgment).isTrue();
        assertThat(block(first, "hypotheses").get("snapshot").get("question").asString())
                .isEqualTo("Coalition response within 7 days?");
        assertThat(block(first, "forecasts").get("questions").get(0).get("questionRef").asString())
                .isEqualTo("question:" + q.getId());
        assertThat(block(first, "objects").get("items").get(0).get("ref").asString())
                .isEqualTo("situation:" + s.getId());

        JsonNode second = draft(jo, body);
        assertThat(content(second)).isEqualTo(content(first));
        assertThat(second.get("id").asString()).isNotEqualTo(first.get("id").asString());
        assertThat(chat().requests()).as("no LLM call without narrative").isEmpty();
    }

    @Test
    @DisplayName("an investigation summary holds its timeline, cited objects and graded evidence items")
    void investigationSummary() throws Exception {
        Person jo = person("jo-inv", Scope.AGENT, null);
        SituationEntity s = situation("Port closure");
        InvestigationEntity inv = investigations.create(node("{'title':'Port closure case','summary':'Why the port"
                + " closed.'}"), "token:jo-inv");
        access.register("investigation:" + inv.getId(), jo.id(), Visibility.PRIVATE, null);
        investigations.addObjectItem(inv.getId(), "object", "situation:" + s.getId(), "Context", Map.of("ref",
                "situation:" + s.getId(), "type", "situation", "title", "Port closure"), false, "token:jo-inv");
        EvidenceItemEntity e = evidence.create(node("{'title':'Harbor master notice','sourceReliability':'B',"
                + "'informationCredibility':2}"), jo.caller());
        investigations.addObjectItem(inv.getId(), "evidence-item", "evidence-item:" + e.getId(), null, Map.of(),
                false, "token:jo-inv");

        JsonNode r = draft(jo, "{'source':{'type':'investigation','ref':'investigation:" + inv.getId() + "'},"
                + "'template':'investigation-summary'}");
        assertThat(r.get("title").asString()).contains("Port closure case");
        assertThat(blockTypes(r)).contains("markdown", "timeline", "objects", "evidence", "coverage");
        JsonNode ev = block(r, "evidence").get("items").get(0);
        assertThat(ev.get("evidenceItemId").asString()).isEqualTo(e.getId());
        assertThat(ev.get("grade").asString()).isEqualTo("B2");
        assertThat(block(r, "timeline").get("items").size()).isEqualTo(2);

        Person other = person("other-inv", Scope.AGENT, null);
        MockHttpServletResponse hidden = call(other.bearer(), json(post("/v1/reports/draft"),
                "{'source':{'type':'investigation','ref':'investigation:" + inv.getId() + "'}}"));
        assertThat(hidden.getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("a chronicle recap holds its summary, thread highlights, a map per major layer and linked sets")
    void chronicleRecap() throws Exception {
        Person jo = person("jo-chr", Scope.AGENT, null);
        SituationEntity s = situation("Gulf tensions");
        HypothesisSetEntity set = hypothesisSet(s.getId());
        ChronicleEntity c = chronicles.create(node("{'name':'Gulf 2026','area':{'geometry':{'type':'Polygon',"
                + "'coordinates':[[[47,23],[57,23],[57,31],[47,31],[47,23]]]}},'situationIds':['" + s.getId() + "'],"
                + "'hypothesisSetIds':['" + set.getId() + "'],'layers':[{'name':'Situation','selector':{'type':"
                + "'situations','situationIds':['" + s.getId() + "']},'capture':{'mode':'full'}}]}"), jo.caller(),
                ChronicleVocabulary.ORIGIN_OPERATOR, null);

        JsonNode r = draft(jo, "{'source':{'type':'chronicle','ref':'chronicle:" + c.getId() + "'},"
                + "'template':'chronicle-recap'}");
        assertThat(r.get("title").asString()).contains("Gulf 2026");
        assertThat(blockTypes(r)).contains("markdown", "timeline", "map", "hypotheses", "coverage");
        assertThat(block(r, "map").get("title").asString()).isEqualTo("Map: Situation");
        assertThat(block(r, "map").get("area").get("type").asString()).isEqualTo("Polygon");
        assertThat(block(r, "hypotheses").get("hypothesisSetRef").asString()).isEqualTo("hypothesis-set:"
                + set.getId());
        assertThat(r.get("area").get("type").asString()).isEqualTo("Polygon");
    }

    @Test
    @DisplayName("a tag or a selection groups the visible objects by type and notes the ones left out")
    void tagAndSelection() throws Exception {
        Person jo = person("jo-tag", Scope.AGENT, null);
        SituationEntity s = situation("Convoy movements");
        quake("q-tag", "Near the coast", 4.2, 43.1, 14.8);
        secret("s-tag", "Home", 43.2, 14.9);
        TagEntity tag = tagService.create(node("{'name':'convoys'}"), jo.caller());
        tagService.bulk(tag.getId(), List.of("situation:" + s.getId(), "record:" + AskTestSources.QUAKES + "/q-tag"),
                true, jo.caller());

        JsonNode byTag = draft(jo, "{'source':{'type':'tag','ref':'tag:" + tag.getId() + "'}}");
        assertThat(byTag.get("title").asString()).contains("convoys");
        List<String> titles = new ArrayList<>();
        for (JsonNode b : byTag.get("blocks")) {
            if ("objects".equals(b.get("type").asString())) {
                titles.add(b.get("title").asString());
            }
        }
        assertThat(titles).contains("Records", "Situations");

        JsonNode selection = draft(jo, "{'source':{'type':'selection','refs':['situation:" + s.getId() + "','record:"
                + AskTestSources.SECRET + "/s-tag']}}");
        JsonNode coverage = block(selection, "coverage");
        boolean unresolved = false;
        for (JsonNode c : coverage.get("caveats")) {
            unresolved |= "UNRESOLVED_REFERENCES".equals(c.get("code").asString());
        }
        assertThat(unresolved).as("the private record is invisible to the author and left out").isTrue();
        for (JsonNode b : selection.get("blocks")) {
            if ("objects".equals(b.get("type").asString())) {
                for (JsonNode item : b.get("items")) {
                    assertThat(item.get("ref").asString()).doesNotContain(AskTestSources.SECRET);
                }
            }
        }
    }

    @Test
    @DisplayName("narrative=true adds an llm block whose citations were verified; it needs agent scope and a provider")
    void narrative() throws Exception {
        Person jo = person("jo-llm", Scope.AGENT, null);
        Person reader = person("reader-llm", Scope.READ, null);
        SituationEntity s = situation("Strait closure");
        String sref = "situation:" + s.getId();
        chat().then(toolUse(ai.foresignal.testsupport.llm.FakeLlmClient.call("c1", ReportNarrativeWriter.OUTPUT_TOOL, "{\"markdown\":\"The closure [[" + sref
                + "]] continues. A second front [[finding:01JA8Q3V7S2K9W4T6Y1B5N0M3C]] is claimed.\","
                + "\"citations\":[\"" + sref + "\",\"finding:01JA8Q3V7S2K9W4T6Y1B5N0M3C\"]}")))
                .then(text("Done."));
        String body = "{'source':{'type':'situation','ref':'" + sref + "'},'narrative':true}";

        JsonNode r = draft(jo, body);
        JsonNode last = r.get("blocks").get(r.get("blocks").size() - 1);
        assertThat(last.get("type").asString()).isEqualTo("markdown");
        assertThat(last.get("textOrigin").asString()).isEqualTo("llm");
        assertThat(last.get("citations").size()).isEqualTo(1);
        assertThat(last.get("citations").get(0).asString()).isEqualTo(sref);
        assertThat(last.get("markdown").asString()).contains("could not be verified")
                .doesNotContain("[[finding:01JA8Q3V7S2K9W4T6Y1B5N0M3C]]");
        assertThat(chat().requests()).isNotEmpty();

        MockHttpServletResponse refused = call(reader.bearer(), json(post("/v1/reports/draft"), body));
        assertThat(refused.getStatus()).isEqualTo(403);

        llmSettings.update(new LlmSettings(null, null, false, false, false, 200_000, false));
        MockHttpServletResponse noProvider = call(jo.bearer(), json(post("/v1/reports/draft"), body));
        assertThat(noProvider.getStatus()).isEqualTo(409);
        assertThat(code(noProvider)).isEqualTo(ErrorCodes.LLM_NOT_CONFIGURED);
    }

    @Test
    @DisplayName("invalid sources and templates are refused")
    void validation() throws Exception {
        Person jo = person("jo-val", Scope.AGENT, null);
        assertThat(call(jo.bearer(), json(post("/v1/reports/draft"), "{'source':{'type':'nope'}}")).getStatus())
                .isEqualTo(400);
        assertThat(call(jo.bearer(), json(post("/v1/reports/draft"), "{'source':{'type':'situation'}}"))
                .getStatus()).isEqualTo(400);
        assertThat(call(jo.bearer(), json(post("/v1/reports/draft"), "{'source':{'type':'selection','refs':[]},"
                + "'template':'weekly-assessment'}")).getStatus()).isEqualTo(400);
        assertThat(call(jo.bearer(), json(post("/v1/reports/draft"), "{'source':{'type':'situation','ref':"
                + "'situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C'}}")).getStatus()).isEqualTo(404);
    }
}
