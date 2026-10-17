package ai.foresignal.notify.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * {@code /v1/actions/{token}} over HTTP (prompt 033): GET never acts, the confirm page's headers and
 * escaping, JSON versus HTML results, the cross-site Origin refusal, the rate limit, and that the
 * token never appears in a response, the audit log or a problem's instance.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(NotifyTestConfig.class)
class ActionEndpointIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired ActionService actions;
    @Autowired ActionRateLimiter limiter;
    @Autowired AuthFailureBackoff backoff;
    @Autowired AlertStore alerts;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter channelLimiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired TriggerRuleRepository rules;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;
    UUID channel;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        limiter.reset();
        backoff.reset();
        fixture = new ActionFixture(jdbc, rules);
        channel = new NotifyFixture(channels, deliveries, channelLimiter).channel(
                "{'name':'Ops','type':'ntfy','config':{'serverUrl':'https://ntfy.example.invalid','topic':'t'}}");
    }

    NotificationAction mint(long alert, String action) {
        return actions.mint(alerts.find(alert).orElseThrow(), channel, 1L, null, List.of(action)).getFirst();
    }

    static MockHttpServletRequestBuilder from(MockHttpServletRequestBuilder request, String ip) {
        return request.with(r -> {
            r.setRemoteAddr(ip);
            return r;
        });
    }

    @Test
    @DisplayName("GET shows a confirm page with the security headers and never changes the alert")
    void getNeverActs() throws Exception {
        long alert = fixture.alert(fixture.rule("Quakes", T), "critical", T);
        NotificationAction ack = mint(alert, "ack");
        for (int i = 0; i < 3; i++) {
            MvcResult result = mvc.perform(get(new java.net.URI("/v1/actions/" + ack.token())))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(header().string("Referrer-Policy", "no-referrer"))
                    .andExpect(header().string("X-Robots-Tag", "noindex"))
                    .andExpect(header().string("Content-Security-Policy",
                            "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'"))
                    .andReturn();
            String page = result.getResponse().getContentAsString();
            assertThat(result.getResponse().getContentType()).startsWith("text/html");
            assertThat(page).contains("<form method=\"post\">").contains("Acknowledge").doesNotContain("<script")
                    .doesNotContain(ack.token());
        }
        assertThat(fixture.state(alert)).isEqualTo("firing");
        assertThat(jdbc.queryForObject("select count(*) from action_capability where used_at is not null", Long.class))
                .isZero();
    }

    @Test
    @DisplayName("hostile rule names and messages are escaped on the confirm page")
    void escaping() throws Exception {
        UUID rule = fixture.rule("<script>alert(1)</script>\"'&", T);
        long alert = fixture.alert(rule, "warning", T);
        jdbc.update("update alert set message = ? where id = ?", "<img src=x onerror=alert(2)>", alert);
        String page = mvc.perform(get("/v1/actions/" + mint(alert, "snooze").token()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(page).doesNotContain("<script>").doesNotContain("<img")
                .contains("&lt;script&gt;alert(1)&lt;/script&gt;").contains("&lt;img src=x onerror=alert(2)&gt;");
    }

    @Test
    @DisplayName("POST with Accept: application/json returns ActionResult; a form post gets an HTML page")
    void postJsonAndHtml() throws Exception {
        long a = fixture.alert(fixture.rule("A", T), "warning", T);
        long b = fixture.alert(fixture.rule("B", T), "warning", T);
        String body = mvc.perform(post("/v1/actions/" + mint(a, "ack").token()).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains("\"action\":\"ack\"").contains("\"alertId\":" + a).contains("\"result\":\"acknowledged\"");
        String page = mvc.perform(post("/v1/actions/" + mint(b, "snooze").token())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(page).contains("snoozed");
    }

    @Test
    @DisplayName("a used or unknown token is 410: an HTML page, or a problem whose instance hides the token")
    void gone() throws Exception {
        long a = fixture.alert(fixture.rule("A", T), "warning", T);
        String token = mint(a, "ack").token();
        mvc.perform(post("/v1/actions/" + token).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        String problem = mvc.perform(post("/v1/actions/" + token).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone()).andReturn().getResponse().getContentAsString();
        assertThat(problem).contains("ACTION_EXPIRED").doesNotContain(token);
        String page = mvc.perform(get("/v1/actions/" + token).accept(MediaType.TEXT_HTML))
                .andExpect(status().isGone()).andReturn().getResponse().getContentAsString();
        assertThat(page).contains("expired").doesNotContain(token);
    }

    @Test
    @DisplayName("a foreign Origin is refused with 403 and nothing happens; no Origin or the same origin is accepted")
    void origin() throws Exception {
        long a = fixture.alert(fixture.rule("A", T), "warning", T);
        String token = mint(a, "ack").token();
        mvc.perform(post("/v1/actions/" + token).header(HttpHeaders.ORIGIN, "https://evil.example")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
        mvc.perform(post("/v1/actions/" + token).header(HttpHeaders.ORIGIN, "null")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
        assertThat(fixture.state(a)).isEqualTo("firing");
        mvc.perform(post("/v1/actions/" + token).header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        assertThat(fixture.state(a)).isEqualTo("acknowledged");
        long b = fixture.alert(fixture.rule("B", T), "warning", T);
        mvc.perform(post("/v1/actions/" + mint(b, "ack").token()).header(HttpHeaders.ORIGIN, "http://localhost")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("30 requests per minute per client IP, GET and POST together; another IP is unaffected")
    void rateLimit() throws Exception {
        String unknown = "eac_" + "A".repeat(22) + "." + "A".repeat(11);
        for (int i = 0; i < ActionRateLimiter.PER_IP_PER_MINUTE; i++) {
            // Each unknown link is also a failed credential (035, ActionBackoffIT); clearing that backoff keeps
            // this test on the 30-per-minute limiter alone.
            backoff.reset();
            mvc.perform(from(get("/v1/actions/" + unknown), "198.51.100.7")).andExpect(status().isGone());
        }
        mvc.perform(from(post("/v1/actions/" + unknown), "198.51.100.7").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
        mvc.perform(from(get("/v1/actions/" + unknown), "198.51.100.8")).andExpect(status().isGone());
        clock.advance(java.time.Duration.ofMinutes(1));
        backoff.reset();
        mvc.perform(from(get("/v1/actions/" + unknown), "198.51.100.7")).andExpect(status().isGone());
    }

    @Test
    @DisplayName("the endpoint is open: no token needed, and a token is not required to act")
    void open() throws Exception {
        long a = fixture.alert(fixture.rule("A", T), "warning", T);
        mvc.perform(get("/v1/actions/" + mint(a, "ack").token())).andExpect(status().isOk());
    }
}
