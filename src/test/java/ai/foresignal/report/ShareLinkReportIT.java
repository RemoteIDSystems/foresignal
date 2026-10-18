package ai.foresignal.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.security.Scope;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Read-only report links (prompt 132, 112 share keys): a published CLEAR report is served as HTML under
 * {@code /v1/k/{key}/report}; AMBER is refused with {@code TLP_VIOLATION}; GREEN needs {@code exchange.allowGreenLinks};
 * an unpublished report has no link; a retracted or relabeled report stops being served.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ShareLinkReportIT extends ReportItSupport {

    @AfterEach
    void resetSetting() {
        settings.put(SettingKeys.EXCHANGE_ALLOW_GREEN_LINKS, false);
    }

    private MockHttpServletResponse link(Person who, ReportEntity r) throws Exception {
        return call(who.bearer(), json(post("/v1/reports/" + r.getId() + "/share-link"), "{}"));
    }

    @Test
    @DisplayName("a CLEAR report is served read-only through its key; AMBER is refused")
    void clearAllowedAmberRefused() throws Exception {
        Person jo = person("jo-link", Scope.AGENT, null);
        ReportEntity clear = report(jo, "{'title':'Public advisory','tlp':'CLEAR','blocks':[{'type':'markdown',"
                + "'markdown':'Avoid the strait.'}]}");
        MockHttpServletResponse early = link(jo, clear);
        assertThat(early.getStatus()).isEqualTo(409);
        assertThat(code(early)).isEqualTo(ErrorCodes.REPORT_NOT_PUBLISHED);
        publish(jo, clear);

        MockHttpServletResponse created = link(jo, clear);
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        JsonNode share = body(created);
        String key = share.get("key").asString();
        assertThat(key).startsWith("fss_");
        assertThat(share.get("targetType").asString()).isEqualTo("report");
        assertThat(share.get("url").asString()).endsWith("/v1/k/" + key + "/report");

        MockHttpServletResponse page = call(null, get("/v1/k/" + key + "/report"));
        assertThat(page.getStatus()).isEqualTo(200);
        assertThat(page.getContentType()).startsWith("text/html");
        assertThat(page.getHeader("Content-Security-Policy")).contains("default-src 'none'");
        assertThat(page.getContentAsString()).contains("Public advisory").contains("TLP:CLEAR");
        MockHttpServletResponse json = call(null, get("/v1/k/" + key));
        assertThat(json.getStatus()).isEqualTo(200);
        assertThat(body(json).get("targetType").asString()).isEqualTo("report");

        ReportEntity amber = report(jo, "{'title':'Internal','blocks':[{'type':'markdown','markdown':'Restricted.'}]}");
        publish(jo, amber);
        MockHttpServletResponse refused = link(jo, amber);
        assertThat(refused.getStatus()).isEqualTo(403);
        assertThat(code(refused)).isEqualTo(ErrorCodes.TLP_VIOLATION);
        assertThat(body(refused).get("rule").asString()).contains("AMBER");

        Person reader = person("reader-link", Scope.READ, null);
        assertThat(link(reader, clear).getStatus()).isEqualTo(403);

        call(jo.bearer(), json(post("/v1/reports/" + clear.getId() + "/retract"), "{'reason':'Outdated'}"));
        assertThat(call(null, get("/v1/k/" + key + "/report")).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("GREEN needs exchange.allowGreenLinks; a report relabeled stricter stops being served")
    void greenSetting() throws Exception {
        Person jo = person("jo-green", Scope.AGENT, null);
        ReportEntity green = report(jo, "{'title':'Community notice','tlp':'GREEN','blocks':[{'type':'markdown',"
                + "'markdown':'Shared within the community.'}]}");
        publish(jo, green);
        assertThat(link(jo, green).getStatus()).isEqualTo(403);
        settings.put(SettingKeys.EXCHANGE_ALLOW_GREEN_LINKS, true);
        MockHttpServletResponse created = link(jo, green);
        assertThat(created.getStatus()).isEqualTo(201);
        String key = body(created).get("key").asString();
        assertThat(call(null, get("/v1/k/" + key + "/report")).getStatus()).isEqualTo(200);

        call(jo.bearer(), mergePatch(patch("/v1/reports/" + green.getId()), "{'status':'draft'}"));
        assertThat(call(jo.bearer(), mergePatch(patch("/v1/reports/" + green.getId()), "{'tlp':'AMBER'}"))
                .getStatus()).isEqualTo(200);
        assertThat(call(null, get("/v1/k/" + key + "/report")).getStatus()).isEqualTo(404);
    }
}
