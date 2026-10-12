package ai.foresignal.net;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.PushSettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.push.MutableResolver;
import ai.foresignal.testsupport.push.PushTestConfig;
import ai.foresignal.testsupport.push.TestSubscriber;
import ai.foresignal.testsupport.security.TestTokens;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * The outbound URL policy retrofitted onto the operator-supplied URLs (prompt 035), over HTTP:
 * channel, LLM provider and push validation refuse metadata and link-local destinations with 400
 * {@code URL_NOT_ALLOWED}; a DNS rebinding between save and send is blocked at send; redirects are
 * not followed. DNS is the shared {@link MutableResolver} stand-in ({@link PushTestConfig}, so this
 * class shares the push ITs' cached context), destinations are the WireMock stub on 127.0.0.1, and
 * nothing real is contacted.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PushTestConfig.class)
class OutboundRetrofitIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired MutableResolver dns;
    @Autowired MutableClock clock;
    @Autowired PinnedHttp pinned;

    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        dns.clear();
        settings.invalidateCache();
        pinned.reset();
        admin = "Bearer " + adminTokens.rotate();
    }

    ResultActions send(MockHttpServletRequestBuilder request, String bearer, String body) throws Exception {
        MockHttpServletRequestBuilder r = request.header(HttpHeaders.AUTHORIZATION, bearer);
        if (body != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
        }
        return mvc.perform(r);
    }

    String createdId(ResultActions result) throws Exception {
        return JSON.readTree(result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .get("id").asString();
    }

    /** A stub URL reached through the host name {@code host} (which the test makes resolve). */
    static String via(String host, String path) {
        return "http://" + host + ":" + WIREMOCK.getPort() + path;
    }

    // ---------- validation on save ----------

    @Test
    @DisplayName("channels: metadata, link-local and names resolving to them are 400 URL_NOT_ALLOWED on the field")
    void channelValidation() throws Exception {
        dns.set("meta.test", "169.254.169.254");
        dns.set("metadata6.test", "fd00:ec2::254");
        for (String url : List.of("http://169.254.169.254/latest/meta-data/", "http://meta.test/hook",
                "http://[fe80::1]:8080/hook", "http://100.100.100.200/", "http://[::ffff:169.254.169.254]/x",
                "http://0.0.0.0:8080/")) {
            send(post("/v1/channels"), admin, "{'name':'w','type':'webhook','config':{'url':'" + url + "'}}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("URL_NOT_ALLOWED")))
                    .andExpect(jsonPath("$.errors[0].field", is("config.url")));
        }
        send(post("/v1/channels"), admin, "{'name':'s','type':'slack','secrets':{'webhookUrl':"
                + "'https://metadata6.test/services/T/B/SECRETPATH'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("URL_NOT_ALLOWED")))
                .andExpect(jsonPath("$.errors[0].field", is("secrets.webhookUrl")))
                .andExpect(r -> assertThat(r.getResponse().getContentAsString()).doesNotContain("SECRETPATH"));
        send(post("/v1/channels"), admin, "{'name':'m','type':'email','config':{'smtpHost':'meta.test',"
                + "'from':'fs@example.org','to':['ops@example.org']}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("URL_NOT_ALLOWED")))
                .andExpect(jsonPath("$.errors[0].field", is("config.smtpHost")));
        // LAN destinations stay allowed for channels (LAN_ALLOWED), and so does a name that does not resolve yet.
        dns.set("gotify.lan", "192.168.1.5");
        createdId(send(post("/v1/channels"), admin, "{'name':'g','type':'webhook','config':{'url':'http://gotify.lan:8080/m'}}"));
        createdId(send(post("/v1/channels"), admin, "{'name':'n','type':'webhook','config':{'url':'https://later.test/m'}}"));
    }

    @Test
    @DisplayName("LLM providers: a metadata or link-local baseUrl is 400 URL_NOT_ALLOWED on baseUrl")
    void llmValidation() throws Exception {
        dns.set("meta.test", "fd00:ec2::254");
        for (String url : List.of("http://169.254.169.254", "http://meta.test:11434", "http://[fe80::1]:11434",
                "http://2130706433:11434")) {
            send(post("/v1/llm/providers"), admin, "{'id':'bad','type':'ollama','baseUrl':'" + url + "'}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field", is("baseUrl")));
        }
        send(post("/v1/llm/providers"), admin, "{'id':'bad','type':'ollama','baseUrl':'http://169.254.169.254:11434'}")
                .andExpect(jsonPath("$.code", is("URL_NOT_ALLOWED")));
        dns.set("ollama", "172.18.0.4");
        createdId(send(post("/v1/llm/providers"), admin, "{'id':'local','type':'ollama','baseUrl':'http://ollama:11434'}"));
    }

    @Test
    @DisplayName("push subscriptions: metadata and link-local endpoints are 400 URL_NOT_ALLOWED even when allowlisted")
    void pushValidation() throws Exception {
        dns.set("meta.test", "169.254.169.254");
        settings.put(SettingKeys.SERVER, new ServerSettings(null, null, null,
                new PushSettings(null, List.of("meta.test", "169.254.0.0/16"), null)));
        settings.invalidateCache();
        TokenService.CreatedToken reader = TestTokens.create(tokens, "phone", Scope.READ);
        for (String endpoint : List.of("https://meta.test/push/x", "https://[fe80::1]/push/x",
                "https://169.254.169.254/push/x")) {
            send(post("/v1/push/subscriptions"), TestTokens.bearer(reader), new TestSubscriber(endpoint).body(null)
                    .replace('"', '\''))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("URL_NOT_ALLOWED")))
                    .andExpect(jsonPath("$.errors[0].field", is("endpoint")));
        }
    }

    @Test
    @DisplayName("outbound overrides: GET/PUT /v1/settings/outbound; a denied host is refused on save")
    void overrides() throws Exception {
        dns.set("hook.test", "127.0.0.1");
        send(put("/v1/settings/outbound"), admin, "{'denyHosts':['hook.test'],'denyCidrs':['203.0.113.0/24']}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.denyHosts[0]", is("hook.test")));
        send(get("/v1/settings/outbound"), admin, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.denyCidrs[0]", is("203.0.113.0/24")))
                .andExpect(jsonPath("$.allowHosts.length()", is(0)));
        send(post("/v1/channels"), admin, "{'name':'w','type':'webhook','config':{'url':'" + via("hook.test", "/h") + "'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("URL_NOT_ALLOWED")));
        send(put("/v1/settings/outbound"), admin, "{'allowCidrs':['not-a-block']}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors[0].field", is("allowCidrs[0]")));
        send(put("/v1/settings/outbound"), admin, "{}").andExpect(status().isOk());
        createdId(send(post("/v1/channels"), admin, "{'name':'w','type':'webhook','config':{'url':'"
                + via("hook.test", "/h") + "'}}"));
    }

    // ---------- rebinding between save and send ----------

    @Test
    @DisplayName("channels: a host rebound to the metadata address after saving is refused at send; nothing is sent")
    void channelRebinding() throws Exception {
        WIREMOCK.stubFor(WireMock.post(urlEqualTo("/hook")).willReturn(aResponse().withStatus(200)));
        dns.set("hook.test", "127.0.0.1");
        String id = createdId(send(post("/v1/channels"), admin,
                "{'name':'w','type':'webhook','config':{'url':'" + via("hook.test", "/hook") + "'}}"));

        send(post("/v1/channels/" + id + "/test"), admin, "").andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
        // The connection went to the checked address, with the original Host.
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/hook"))
                .withHeader("Host", equalTo("hook.test:" + WIREMOCK.getPort())));

        dns.set("hook.test", "169.254.169.254");
        send(post("/v1/channels/" + id + "/test"), admin, "").andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", allOf(containsString("URL_NOT_ALLOWED"), containsString("169.254.169.254"))));
        dns.set("hook.test", "10.0.0.1", "169.254.170.2");
        send(post("/v1/channels/" + id + "/test"), admin, "").andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)));
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/hook")));
    }

    @Test
    @DisplayName("LLM providers: a baseUrl host rebound to link-local after saving is refused at send; nothing is sent")
    void llmRebinding() throws Exception {
        WIREMOCK.stubFor(WireMock.post(urlPathEqualTo("/api/chat"))
                .willReturn(okJson("{\"model\":\"qwen3:14b\",\"message\":{\"role\":\"assistant\",\"content\":\"ok\"},"
                        + "\"done\":true,\"done_reason\":\"stop\",\"prompt_eval_count\":12,\"eval_count\":1}")));
        dns.set("llm.test", "127.0.0.1");
        send(post("/v1/llm/providers"), admin, "{'id':'rebind','type':'ollama','baseUrl':'" + via("llm.test", "")
                + "','chatModel':'qwen3:14b','requestsPerMinute':60}").andExpect(status().isCreated());

        send(post("/v1/llm/providers/rebind/test"), admin, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/api/chat"))
                .withHeader("Host", equalTo("llm.test:" + WIREMOCK.getPort())));

        dns.set("llm.test", "169.254.169.254");
        send(post("/v1/llm/providers/rebind/test"), admin, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", containsString("URL_NOT_ALLOWED")));
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/api/chat")));
    }

    // ---------- redirects ----------

    @Test
    @DisplayName("channels: a redirect to the metadata address is not followed; the 3xx is the failure")
    void channelRedirectNotFollowed() throws Exception {
        WIREMOCK.stubFor(WireMock.post(urlEqualTo("/redirect")).willReturn(aResponse().withStatus(302)
                .withHeader("Location", "http://169.254.169.254/latest/meta-data/iam/security-credentials/")));
        String id = createdId(send(post("/v1/channels"), admin,
                "{'name':'w','type':'webhook','config':{'url':'" + url("/redirect") + "'}}"));
        send(post("/v1/channels/" + id + "/test"), admin, "").andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(302)));
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/redirect")));
    }

    @Test
    @DisplayName("LLM providers: a redirect is not followed")
    void llmRedirectNotFollowed() throws Exception {
        WIREMOCK.stubFor(WireMock.post(urlPathEqualTo("/api/chat")).willReturn(aResponse().withStatus(307)
                .withHeader("Location", "http://169.254.169.254/api/chat")));
        send(post("/v1/llm/providers"), admin, "{'id':'redir','type':'ollama','baseUrl':'" + baseUrl()
                + "','chatModel':'qwen3:14b','requestsPerMinute':60}").andExpect(status().isCreated());
        send(post("/v1/llm/providers/redir/test"), admin, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)));
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo("/api/chat")));
    }
}
