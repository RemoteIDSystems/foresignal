package ai.foresignal.notify.action;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Action capabilities and {@link AuthFailureBackoff} (prompts 033, 035): an unknown capability is a
 * failed credential check; past five within 60 s the refusal becomes {@code 429 RATE_LIMITED} with a
 * doubling {@code Retry-After}, a valid capability from the same address is still accepted and resets
 * it, and other addresses are unaffected.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(NotifyTestConfig.class)
@TestPropertySource(properties = "foresignal.ratelimit.auth-backoff-enabled=true")
class ActionBackoffIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final String UNKNOWN = "eac_" + "A".repeat(22) + "." + "A".repeat(11);
    static final String IP = "198.51.100.40";

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

    void failFiveTimes() throws Exception {
        for (int i = 0; i < AuthFailureBackoff.THRESHOLD; i++) {
            mvc.perform(from(get("/v1/actions/" + UNKNOWN), IP)).andExpect(status().isGone());
        }
    }

    @Test
    @DisplayName("five unknown capabilities are 410; further ones are 429 with Retry-After 1, then 2")
    void backoffAfterFive() throws Exception {
        failFiveTimes();

        mvc.perform(from(post("/v1/actions/" + UNKNOWN), IP).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "1"))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
        mvc.perform(from(get("/v1/actions/" + UNKNOWN), IP))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "2"));
    }

    @Test
    @DisplayName("a valid capability is accepted mid-backoff and resets the address")
    void validCapabilityNeverBlocked() throws Exception {
        failFiveTimes();
        mvc.perform(from(get("/v1/actions/" + UNKNOWN), IP)).andExpect(status().isTooManyRequests());

        long alert = fixture.alert(fixture.rule("Quakes", T), "critical", T);
        NotificationAction ack = mint(alert, "ack");
        mvc.perform(from(post("/v1/actions/" + ack.token()), IP).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(from(get("/v1/actions/" + UNKNOWN), IP)).andExpect(status().isGone());
    }

    @Test
    @DisplayName("another address is unaffected")
    void perAddress() throws Exception {
        failFiveTimes();
        mvc.perform(from(get("/v1/actions/" + UNKNOWN), IP)).andExpect(status().isTooManyRequests());

        mvc.perform(from(get("/v1/actions/" + UNKNOWN), "198.51.100.41")).andExpect(status().isGone());
    }
}
