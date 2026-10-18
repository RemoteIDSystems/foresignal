package ai.foresignal.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.security.Scope;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Publishing reports (prompt 132, RPT-1): pinned references and frozen snapshots, renditions with the TLP banner and
 * the automated-analysis footer, immutable revisions, the optimistic lock, and retraction notices to recipients.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ReportPublishIT extends ReportItSupport {

    @Test
    @DisplayName("publishing pins records at their seq and freezes envelopes; a later version changes nothing")
    void pinnedAndFrozen() throws Exception {
        Person jo = person("jo-pub", Scope.AGENT, null);
        quake("q-pin", "Offshore", 4.0, 43.1, 14.8);
        long seq = headSeq(AskTestSources.QUAKES, "q-pin");
        ReportEntity r = report(jo, "{'title':'Quake note','blocks':[{'type':'objects','items':[{'ref':'record:"
                + AskTestSources.QUAKES + "/q-pin'}]},{'type':'markdown','markdown':'See [[record:"
                + AskTestSources.QUAKES + "/q-pin]].'}]}");

        ReportRevisionEntity rev = publish(jo, r);
        assertThat(rev.getRevision()).isEqualTo(1);
        assertThat(rev.getPinnedRefs()).contains("record:" + AskTestSources.QUAKES + "/q-pin@" + seq);
        assertThat(rev.getBundleSha256()).matches("[0-9a-f]{64}");
        JsonNode bundle = JSON.valueToTree(rev.getBundle());
        assertThat(bundle.get("revision").asInt()).isEqualTo(1);
        assertThat(bundle.get("tlp").asString()).isEqualTo("AMBER");
        assertThat(bundle.get("envelopes").size()).isGreaterThanOrEqualTo(1);
        JsonNode pinnedItem = bundle.get("report").get("blocks").get(0).get("items").get(0);
        assertThat(pinnedItem.get("ref").asString()).endsWith("@" + seq);
        assertThat(pinnedItem.get("envelope").get("title").asString()).contains("Offshore");

        clock.setInstant(clock.instant().plus(Duration.ofMinutes(5)));
        quake("q-pin", "Renamed place", 5.5, 43.1, 14.8);
        MockHttpServletResponse json = call(jo.bearer(), get("/v1/reports/" + r.getId() + "/rendition")
                .param("format", "json").param("revision", "1"));
        assertThat(json.getStatus()).isEqualTo(200);
        JsonNode again = body(json);
        assertThat(again.get("report").get("blocks").get(0).get("items").get(0).get("envelope").get("title")
                .asString()).contains("Offshore");
        assertThat(revisionRows.findByReportIdAndRevision(r.getId(), 1).orElseThrow().getBundleSha256())
                .isEqualTo(rev.getBundleSha256());
    }

    @Test
    @DisplayName("renditions carry the TLP banner on every page; machine-generated blocks add the footer")
    void renditions() throws Exception {
        Person jo = person("jo-ren", Scope.AGENT, null);
        ReportEntity authored = report(jo, "{'title':'Analyst note','tlp':'GREEN','blocks':[{'type':'markdown',"
                + "'markdown':'Written by hand. <script>alert(1)</script>'}]}");
        publish(jo, authored);
        MockHttpServletResponse html = call(jo.bearer(), get("/v1/reports/" + authored.getId() + "/rendition")
                .param("format", "html"));
        assertThat(html.getStatus()).isEqualTo(200);
        assertThat(html.getContentType()).startsWith("text/html");
        assertThat(html.getHeader("Content-Security-Policy")).contains("default-src 'none'");
        assertThat(html.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        String page = html.getContentAsString();
        assertThat(page.split("TLP:GREEN", -1).length - 1).isGreaterThanOrEqualTo(3);
        assertThat(page).contains("tlp-top").contains("tlp-bottom").doesNotContain("<script>")
                .doesNotContain(ReportVocabulary.FOOTER);
        String md = call(jo.bearer(), get("/v1/reports/" + authored.getId() + "/rendition").param("format", "md"))
                .getContentAsString();
        assertThat(md).startsWith("> **TLP:GREEN").doesNotContain(ReportVocabulary.FOOTER);

        SituationEntity s = situation("Footer case");
        ReportEntity drafted = drafter.draft(node("{'source':{'type':'situation','ref':'situation:" + s.getId()
                + "'}}"), jo.caller());
        publish(jo, drafted);
        String draftedMd = call(jo.bearer(), get("/v1/reports/" + drafted.getId() + "/rendition")
                .param("format", "md")).getContentAsString();
        assertThat(draftedMd).contains(ReportVocabulary.FOOTER).contains("TLP:AMBER");
        JsonNode sources = null;
        for (JsonNode b : JSON.valueToTree(revisionRows.findFirstByReportIdOrderByRevisionDesc(drafted.getId())
                .orElseThrow().getBundle()).get("report").get("blocks")) {
            if ("sources".equals(b.get("type").asString())) {
                sources = b;
            }
        }
        assertThat(sources).as("the sources block is regenerated at publication").isNotNull();
        assertThat(sources.get("textOrigin").asString()).isEqualTo("deterministic");

        MockHttpServletResponse preview = call(jo.bearer(), get("/v1/reports/" + drafted.getId() + "/rendition")
                .param("format", "md").param("draft", "true"));
        assertThat(preview.getContentAsString()).contains("draft preview");
        assertThat(call(jo.bearer(), get("/v1/reports/" + drafted.getId() + "/rendition")).getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("revisions are immutable; the draft is reopened for the next revision; stale versions conflict")
    void immutableRevisions() throws Exception {
        Person jo = person("jo-imm", Scope.AGENT, null);
        ReportEntity r = report(jo, "{'title':'Weekly','blocks':[{'type':'markdown','markdown':'First.'}]}");
        int version = reports.get(r.getId()).getVersion();
        MockHttpServletResponse stale = call(jo.bearer(), mergePatch(patch("/v1/reports/" + r.getId()),
                "{'title':'X','version':" + (version + 7) + "}"));
        assertThat(stale.getStatus()).isEqualTo(409);
        assertThat(code(stale)).isEqualTo(ErrorCodes.REPORT_VERSION_CONFLICT);
        MockHttpServletResponse staleEtag = call(jo.bearer(), mergePatch(patch("/v1/reports/" + r.getId()),
                "{'title':'X'}").header("If-Match", "\"" + (version + 7) + "\""));
        assertThat(staleEtag.getStatus()).isEqualTo(409);

        ReportRevisionEntity first = publish(jo, r);
        String firstMarkdown = first.getMarkdown();
        MockHttpServletResponse notDraft = call(jo.bearer(), mergePatch(patch("/v1/reports/" + r.getId()),
                "{'title':'Changed'}"));
        assertThat(notDraft.getStatus()).isEqualTo(409);
        assertThat(call(jo.bearer(), json(post("/v1/reports/" + r.getId() + "/blocks"),
                "{'type':'markdown','markdown':'More.'}")).getStatus()).isEqualTo(409);
        assertThat(call(jo.bearer(), post("/v1/reports/" + r.getId() + "/publish")).getStatus()).isEqualTo(409);

        assertThat(call(jo.bearer(), mergePatch(patch("/v1/reports/" + r.getId()), "{'status':'draft'}"))
                .getStatus()).isEqualTo(200);
        MockHttpServletResponse added = call(jo.bearer(), json(post("/v1/reports/" + r.getId() + "/blocks"),
                "{'type':'markdown','markdown':'Second.'}"));
        assertThat(added.getStatus()).isEqualTo(201);
        MockHttpServletResponse second = call(jo.bearer(), post("/v1/reports/" + r.getId() + "/publish"));
        assertThat(second.getStatus()).isEqualTo(201);
        assertThat(body(second).get("revision").asInt()).isEqualTo(2);

        JsonNode list = body(call(jo.bearer(), get("/v1/reports/" + r.getId() + "/revisions")));
        assertThat(list.get("items").get(0).get("revision").asInt()).isEqualTo(2);
        assertThat(list.get("items").get(1).get("revision").asInt()).isEqualTo(1);
        assertThat(revisionRows.findByReportIdAndRevision(r.getId(), 1).orElseThrow().getMarkdown())
                .isEqualTo(firstMarkdown).doesNotContain("Second.");
        assertThat(call(jo.bearer(), get("/v1/reports/" + r.getId() + "/rendition").param("format", "md"))
                .getContentAsString()).contains("Second.");
    }

    @Test
    @DisplayName("retraction marks the revision withdrawn and notifies everyone it was sent to")
    void retraction() throws Exception {
        Person jo = person("jo-ret", Scope.AGENT, null);
        Person bob = person("bob-ret", Scope.READ, null);
        ReportEntity r = report(jo, "{'title':'Port assessment','blocks':[{'type':'markdown','markdown':'Text.'}]}");
        assertThat(call(jo.bearer(), json(post("/v1/reports/" + r.getId() + "/retract"), "{'reason':'x'}"))
                .getStatus()).isEqualTo(409);
        publish(jo, r);
        MockHttpServletResponse sent = call(jo.bearer(), json(post("/v1/reports/" + r.getId() + "/send"),
                "{'principals':['bob-ret'],'message':'Please read.'}"));
        assertThat(sent.getStatus()).as(sent.getContentAsString()).isEqualTo(201);
        assertThat(body(sent).get("recipients").get(0).get("grantee").asString()).isEqualTo("principal:" + bob.id());
        assertThat(call(bob.bearer(), get("/v1/reports/" + r.getId())).getStatus()).isEqualTo(200);

        MockHttpServletResponse retracted = call(jo.bearer(), json(post("/v1/reports/" + r.getId() + "/retract"),
                "{'reason':'Superseded by new imagery'}"));
        assertThat(retracted.getStatus()).isEqualTo(200);
        assertThat(body(retracted).get("status").asString()).isEqualTo("retracted");
        assertThat(body(retracted).get("retractionReason").asString()).isEqualTo("Superseded by new imagery");
        assertThat(reports.get(r.getId()).getStatus()).isEqualTo("retracted");
        assertThat(inboxEvents(bob.id())).containsSubsequence(MeEvents.REPORT_RECEIVED, MeEvents.REPORT_RETRACTED);
        MockHttpServletResponse twice = call(jo.bearer(), json(post("/v1/reports/" + r.getId() + "/retract"),
                "{'reason':'again'}"));
        assertThat(twice.getStatus()).isEqualTo(409);
        assertThat(code(twice)).isEqualTo(ErrorCodes.REPORT_NOT_PUBLISHED);
        assertThat(call(bob.bearer(), json(post("/v1/reports/" + r.getId() + "/retract"), "{'reason':'x'}"))
                .getStatus()).isIn(403, 404);
    }

    @Test
    @DisplayName("a map block is rendered and stored as an attachment, or carries a caveat when it cannot be")
    void mapBlock() throws Exception {
        Person jo = person("jo-map", Scope.AGENT, null);
        ReportEntity r = report(jo, "{'title':'Map report','blocks':[{'type':'map','bbox':[42,12,45,16]},"
                + "{'type':'map'}]}");
        ReportRevisionEntity rev = publish(jo, r);
        JsonNode blocks = JSON.valueToTree(rev.getBundle()).get("report").get("blocks");
        JsonNode withBox = blocks.get(0);
        assertThat(withBox.hasNonNull("attachmentId") || withBox.hasNonNull("caveat")).isTrue();
        assertThat(blocks.get(1).get("caveat").asString()).contains("no area");
    }
}
