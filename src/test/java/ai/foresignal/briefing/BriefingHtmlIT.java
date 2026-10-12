package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * The HTML renditions of briefing runs (prompt 029): {@code <script>} and event handlers injected
 * through record titles never survive, the strict Content-Security-Policy header, {@code Accept} and
 * {@code ?format} negotiation, the latest run, and per-run ETags.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class BriefingHtmlIT extends BriefingItSupport {

    static final String HOSTILE = "<script>alert(1)</script><img src=x onerror=alert(2)> [x](javascript:alert(3))";

    @Autowired MockMvc mvc;

    BriefingHttp http;
    UUID briefingId;
    String runId;

    @BeforeEach
    void setUpRun() {
        http = new BriefingHttp(mvc);
        quake("evil", HOSTILE, 6.0);
        briefingId = id(briefings.create(json(digestBody("Hostile <b>brief</b>", "0 7 * * *", null)), agentCaller()));
        Map<String, Object> run = briefings.runNow(briefingId);
        runId = run.get("id").toString();
    }

    String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Accept: text/html returns a sanitized page with the CSP header; record text stays inert")
    void sanitizedHtml() throws Exception {
        MvcResult result = http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/" + runId,
                reader, null).header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,*/*;q=0.8"), 200);
        String html = body(result);

        assertThat(result.getResponse().getContentType()).startsWith(MediaType.TEXT_HTML_VALUE);
        assertThat(result.getResponse().getHeader("Content-Security-Policy"))
                .isEqualTo("default-src 'none'; style-src 'unsafe-inline'; img-src 'self' data:");
        assertThat(result.getResponse().getHeader(HttpHeaders.ETAG)).isNotBlank();
        assertThat(html).startsWith("<!DOCTYPE html>").contains("<table>").contains("<h2>Notable</h2>")
                .contains("<title>Hostile &lt;b&gt;brief&lt;/b&gt;</title>");
        assertThat(html).doesNotContain("<script").doesNotContain("<img").doesNotContain("<iframe");
        assertThat(html).doesNotContainPattern("<[^>]*\\son[a-z]+\\s*=").doesNotContainPattern("href=\"\\s*javascript:");
        assertThat(html).as("the hostile title is shown as text").contains("&lt;script&gt;");
        assertThat(html).containsPattern("<a [^>]*rel=\"[^\"]*nofollow");
    }

    @Test
    @DisplayName("?format=html works without an Accept header; JSON is the default; format=xml is 400")
    void negotiation() throws Exception {
        MvcResult html = http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/" + runId
                + "?format=html", reader, null), 200);
        assertThat(html.getResponse().getContentType()).startsWith(MediaType.TEXT_HTML_VALUE);

        MvcResult json = http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/" + runId,
                reader, null), 200);
        assertThat(json.getResponse().getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(json.getResponse().getHeader("Content-Security-Policy")).isNull();
        assertThat(body(json)).contains("\"contentMarkdown\"");

        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/" + runId + "?format=xml",
                reader, null), 400);
        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/" + UUID.randomUUID(),
                reader, null), 404);
        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/nope", reader, null), 404);
    }

    @Test
    @DisplayName("the latest run as HTML and JSON; 404 before any run; per-run ETag with 304")
    void latestRun() throws Exception {
        MvcResult latest = http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId
                + "/runs/latest?format=html", null, null), 200);
        assertThat(latest.getResponse().getHeader("Content-Security-Policy")).isNotNull();
        assertThat(body(latest)).contains("Foresignal briefing run " + runId);

        MvcResult latestJson = http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/latest",
                reader, null), 200);
        assertThat(body(latestJson)).contains(runId);
        String etag = latestJson.getResponse().getHeader(HttpHeaders.ETAG);
        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + briefingId + "/runs/latest", reader, null)
                .header(HttpHeaders.IF_NONE_MATCH, etag), 304);
        String htmlEtag = latest.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(htmlEtag).isNotEqualTo(etag);

        UUID fresh = id(briefings.create(json(digestBody("Fresh", "0 7 * * *", null)), agentCaller()));
        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + fresh + "/runs/latest", reader, null), 404);
        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + UUID.randomUUID() + "/runs/latest", reader, null),
                404);
    }

    @Test
    @DisplayName("wantsHtml ranks text/html against JSON by quality")
    void acceptRanking() {
        assertThat(ai.foresignal.api.BriefingsControllerAccess.wantsHtml(null, "text/html")).isTrue();
        assertThat(ai.foresignal.api.BriefingsControllerAccess.wantsHtml(null, "application/json, text/html;q=0.5")).isFalse();
        assertThat(ai.foresignal.api.BriefingsControllerAccess.wantsHtml(null, "*/*")).isFalse();
        assertThat(ai.foresignal.api.BriefingsControllerAccess.wantsHtml("json", "text/html")).isFalse();
        assertThat(ai.foresignal.api.BriefingsControllerAccess.wantsHtml(null, "not a media type")).isFalse();
    }
}
