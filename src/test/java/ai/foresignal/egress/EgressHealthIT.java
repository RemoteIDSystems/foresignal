package ai.foresignal.egress;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.test.IntegrationTest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Egress health and {@code testEgressRoute} (prompt R2-018).
 *
 * <p>The stub server stands in for an HTTP proxy that answers for the exit-IP service itself, at a host that does not
 * resolve here, so an answer proves the call went through the route. The probe is pointed at that host with
 * {@link EgressProbe#probeUrl}; the configured service is restored afterwards.
 *
 * <p>Asserted: a test reports and records the exit IP; a route whose proxy refuses connections becomes unreachable
 * after three failures, raises {@code system.egress_unhealthy} once, is skipped by the selector, and raises
 * {@code system.egress_recovered} when a call gets through again; an unavailable route's test fails without a call.
 */
@IntegrationTest
@AutoConfigureMockMvc
@RecordApplicationEvents
class EgressHealthIT extends EgressItSupport {

    private static final String CHECKIP_HOST = "checkip.egress.invalid";

    @Autowired MockMvc mvc;
    @Autowired EgressProbe probe;
    @Autowired ApplicationEvents events;

    @AfterEach
    void restoreProbe() {
        probe.resetProbe(EgressProbe.DEFAULT_PROBE_URL);
    }

    private void pointProbeAtStub(String answer) {
        probe.probeUrl(URI.create("http://" + CHECKIP_HOST + "/"));
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/"))
                .willReturn(aResponse().withStatus(200).withBody(answer)));
    }

    /** A loopback port nothing listens on: a proxy there refuses every connection. */
    private static int closedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    private long alerts(String subtype) {
        return events.stream(SystemEvent.class).filter(e -> e.subtype().equals(subtype)).count();
    }

    @Test
    @DisplayName("testEgressRoute reports the exit IP the route presents, and records it on the route")
    void testReportsTheExitIp() throws Exception {
        pointProbeAtStub("203.0.113.7\n");
        EgressRoute route = httpRoute("exit-test", port());

        mvc.perform(post("/v1/egress/" + route.id() + "/test").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.exitIp").value("203.0.113.7"))
                .andExpect(jsonPath("$.latencyMs").isNumber())
                .andExpect(jsonPath("$.checkedAt").isNotEmpty());

        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/")).withHeader("Host", equalTo(CHECKIP_HOST)));
        assertThat(store.find(route.id()).orElseThrow().exitIp()).isEqualTo("203.0.113.7");
        mvc.perform(get("/v1/egress/" + route.id() + "/health").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reachable").value(true))
                .andExpect(jsonPath("$.exitIp").value("203.0.113.7"))
                .andExpect(jsonPath("$.consecutiveFailures").value(0))
                .andExpect(jsonPath("$.sources").isArray());
    }

    @Test
    @DisplayName("an answer that is not an address fails the test and records no exit IP")
    void answerMustBeAnAddress() {
        pointProbeAtStub("<html>captive portal</html>");
        EgressRoute route = httpRoute("portal", port());

        Map<String, Object> result = probe.test(route);

        assertThat(result).containsEntry("ok", false).containsEntry("exitIp", null);
        assertThat(store.find(route.id()).orElseThrow().exitIp()).isNull();
    }

    @Test
    @DisplayName("an unhealthy route is skipped by the selector and alerts once; a success raises the recovery")
    void unhealthyRouteIsSkippedAndAlerts() throws Exception {
        pointProbeAtStub("203.0.113.8");
        EgressRoute dead = httpRoute("a-dead", closedPort());
        EgressRoute alive = httpRoute("b-alive", port());
        rotation(EgressSettings.ROUND_ROBIN);

        for (int i = 0; i < EgressHealthRegistry.FAILURE_THRESHOLD; i++) {
            assertThat(probe.test(dead)).containsEntry("ok", false);
        }

        assertThat(alerts("system.egress_unhealthy")).isEqualTo(1);
        assertThat(health.usable(dead.id())).isFalse();
        mvc.perform(get("/v1/egress/" + dead.id() + "/health").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reachable").value(false))
                .andExpect(jsonPath("$.consecutiveFailures").value(EgressHealthRegistry.FAILURE_THRESHOLD))
                .andExpect(jsonPath("$.backoffUntil").isNotEmpty())
                .andExpect(jsonPath("$.lastError").isNotEmpty());

        for (int i = 0; i < 4; i++) {
            EgressRouter.Decision decision = router.decide("egress-health-test", null);
            assertThat(decision.routing()).as("the unhealthy route is skipped").isNotNull();
            assertThat(decision.routing().route().id()).isEqualTo(alive.id());
        }

        probe.test(dead);
        assertThat(alerts("system.egress_unhealthy")).as("alerts once, not per failure").isEqualTo(1);

        health.success(dead, Duration.ofMillis(12), null);
        assertThat(alerts("system.egress_recovered")).isEqualTo(1);
        assertThat(health.usable(dead.id())).isTrue();
    }

    @Test
    @DisplayName("a route this host cannot use fails its test without a call")
    void unavailableRouteTestFails() {
        pointProbeAtStub("203.0.113.9");
        EgressRoute wireguard = routes.create(json("{'name':'wg','type':'wireguard',"
                + "'secrets':{'wireguardConfig':'[Interface]\\nPrivateKey = x\\n'}}"));

        Map<String, Object> result = probe.test(wireguard);

        assertThat(result).containsEntry("ok", false);
        assertThat((String) result.get("message")).isNotBlank();
        assertThat(WIREMOCK.findAll(getRequestedFor(urlPathEqualTo("/")))).isEmpty();
    }
}
