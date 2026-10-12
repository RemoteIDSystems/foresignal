package ai.foresignal.security.pairing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.QrDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Device pairing end to end through the API (prompt 023). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class PairingIT {

    private static final Instant T0 = Instant.parse("2026-10-12T09:00:00Z");
    private static final String DEVICE = "{\"deviceName\":\"Pixel 9\",\"platform\":\"android\",\"appVersion\":\"1.0.0\"}";

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private TokenService tokens;
    @Autowired private PairingRateLimiter limiter;
    @Autowired private AppSettingService settings;
    @Autowired private AuditService audit;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MutableClock clock;
    @Autowired private JsonMapper json;

    private String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.invalidateCache();
        limiter.reset();
        admin = "Bearer " + adminTokens.rotate();
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    private JsonNode create(String body) throws Exception {
        MockHttpServletResponse response = perform(post("/v1/pairing-codes").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return json.readTree(response.getContentAsString());
    }

    private MockHttpServletResponse claim(String code, String ip) throws Exception {
        return perform(post("/v1/pairing/claim").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"device\":" + DEVICE + "}")
                .with(r -> {
                    r.setRemoteAddr(ip);
                    return r;
                }));
    }

    @Test
    @DisplayName("create returns the code (XXXX-XXXX) and the QR payload once; the list never shows the code")
    void createAndList() throws Exception {
        JsonNode created = create("{\"name\":\"Phone\",\"scope\":\"read\"}");

        String code = created.get("code").asString();
        assertThat(code).matches("^[0-9A-HJKMNP-TV-Z]{4}-[0-9A-HJKMNP-TV-Z]{4}$");
        assertThat(created.get("qrPayload").asString()).isEqualTo("foresignal://pair?u="
                + URLEncoder.encode("http://localhost", StandardCharsets.UTF_8) + "&c=" + code);
        assertThat(created.get("expiresAt").asString()).isEqualTo(T0.plus(Duration.ofMinutes(10)).toString());
        assertThat(created.get("scope").asString()).isEqualTo("read");

        JsonNode list = json.readTree(perform(get("/v1/pairing-codes").header(HttpHeaders.AUTHORIZATION, admin))
                .getContentAsString());
        assertThat(list).hasSize(1);
        assertThat(list.get(0).has("code")).isFalse();
        assertThat(list.get(0).toString()).doesNotContain(code);
    }

    @Test
    @DisplayName("a claim returns a read token (createdVia pairing) with the device; the code works once")
    void claimOnce() throws Exception {
        String code = create("{\"name\":\"Phone\",\"scope\":\"read\"}").get("code").asString();

        MockHttpServletResponse first = claim(code.toLowerCase().replace("-", ""), "10.0.0.5");
        assertThat(first.getStatus()).as(first.getContentAsString()).isEqualTo(201);
        JsonNode token = json.readTree(first.getContentAsString());
        assertThat(token.get("createdVia").asString()).isEqualTo("pairing");
        assertThat(token.get("scope").asString()).isEqualTo("read");
        assertThat(token.get("device").get("platform").asString()).isEqualTo("android");
        AuthenticatedToken authenticated = tokens.authenticate(token.get("token").asString()).orElseThrow();
        assertThat(authenticated.scope()).isEqualTo(Scope.READ);
        assertThat(authenticated.name()).isEqualTo("Phone");

        MockHttpServletResponse second = claim(code, "10.0.0.5");
        assertThat(second.getStatus()).isEqualTo(400);
        assertThat(json.readTree(second.getContentAsString()).get("code").asString()).isEqualTo("PAIRING_CODE_INVALID");
    }

    @Test
    @DisplayName("an expired code cannot be claimed and is no longer listed")
    void expiry() throws Exception {
        String code = create("{\"name\":\"Phone\",\"scope\":\"agent\",\"ttl\":\"1m\"}").get("code").asString();
        clock.advance(Duration.ofSeconds(61));

        assertThat(claim(code, "10.0.0.6").getStatus()).isEqualTo(400);
        assertThat(json.readTree(perform(get("/v1/pairing-codes").header(HttpHeaders.AUTHORIZATION, admin))
                .getContentAsString())).isEmpty();
    }

    @Test
    @DisplayName("admin is never pairable; ttl above 1h and unknown members are refused")
    void createValidation() throws Exception {
        for (String bad : new String[] {
            "{\"name\":\"Phone\",\"scope\":\"admin\"}",
            "{\"name\":\"Phone\",\"scope\":\"read\",\"ttl\":\"2h\"}",
            "{\"scope\":\"read\"}",
            "{\"name\":\"Phone\",\"scope\":\"read\",\"colour\":\"red\"}"}) {
            MockHttpServletResponse response = perform(post("/v1/pairing-codes").header(HttpHeaders.AUTHORIZATION, admin)
                    .contentType(MediaType.APPLICATION_JSON).content(bad));
            assertThat(response.getStatus()).as(bad).isEqualTo(400);
        }
    }

    @Test
    @DisplayName("five claims per minute per IP, then 429 RATE_LIMITED with Retry-After")
    void perIpLimit() throws Exception {
        for (int i = 0; i < PairingRateLimiter.PER_IP_PER_MINUTE; i++) {
            assertThat(claim("AAAA-AAAA", "10.1.1.1").getStatus()).isEqualTo(400);
        }
        MockHttpServletResponse limited = claim("AAAA-AAAA", "10.1.1.1");

        assertThat(limited.getStatus()).isEqualTo(429);
        assertThat(json.readTree(limited.getContentAsString()).get("code").asString()).isEqualTo("RATE_LIMITED");
        assertThat(Long.parseLong(limited.getHeader(HttpHeaders.RETRY_AFTER))).isPositive();
        assertThat(claim("AAAA-AAAA", "10.1.1.2").getStatus()).as("another IP").isEqualTo(400);
        clock.advance(Duration.ofMinutes(1));
        assertThat(claim("AAAA-AAAA", "10.1.1.1").getStatus()).as("after a minute").isEqualTo(400);
    }

    @Test
    @DisplayName("fifty claims per hour across all IPs, then 429")
    void globalLimit() throws Exception {
        for (int i = 0; i < PairingRateLimiter.GLOBAL_PER_HOUR; i++) {
            assertThat(claim("AAAA-AAAA", "10.2.0." + i).getStatus()).isEqualTo(400);
        }
        assertThat(claim("AAAA-AAAA", "10.3.0.1").getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("the QR SVG decodes back to qrPayload")
    void qrDecodes() throws Exception {
        JsonNode created = create("{\"name\":\"Phone\",\"scope\":\"read\"}");

        MockHttpServletResponse svg = perform(get("/v1/pairing-codes/" + created.get("id").asString() + "/qr.svg")
                .header(HttpHeaders.AUTHORIZATION, admin));

        assertThat(svg.getStatus()).isEqualTo(200);
        assertThat(svg.getContentType()).startsWith("image/svg+xml");
        assertThat(svg.getContentAsString()).doesNotContain("<script");
        assertThat(QrDecoder.decode(svg.getContentAsString())).isEqualTo(created.get("qrPayload").asString());
    }

    @Test
    @DisplayName("a deleted or claimed code has no QR and is not listed")
    void deleteAndClaimed() throws Exception {
        JsonNode deleted = create("{\"name\":\"Gone\",\"scope\":\"read\"}");
        JsonNode claimed = create("{\"name\":\"Used\",\"scope\":\"read\"}");

        assertThat(perform(delete("/v1/pairing-codes/" + deleted.get("id").asString())
                .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(204);
        assertThat(claim(claimed.get("code").asString(), "10.0.0.9").getStatus()).isEqualTo(201);

        for (JsonNode code : new JsonNode[] {deleted, claimed}) {
            assertThat(perform(get("/v1/pairing-codes/" + code.get("id").asString() + "/qr.svg")
                    .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(404);
        }
        assertThat(perform(delete("/v1/pairing-codes/" + deleted.get("id").asString())
                .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("every claim is audited, failures included, without the code")
    void claimsAreAudited() throws Exception {
        audit.flush();
        jdbc.update("DELETE FROM audit_event");
        String code = create("{\"name\":\"Phone\",\"scope\":\"read\"}").get("code").asString();

        claim("ZZZZ-ZZZZ", "10.4.0.1");
        claim(code, "10.4.0.1");
        audit.flush();

        var rows = jdbc.queryForList("SELECT actor_kind, status, remote_ip, detail::text AS detail FROM audit_event"
                + " WHERE operation_id = 'claimPairingCode' ORDER BY id");
        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(r -> r.get("status")).containsExactly(400, 201);
        assertThat(rows).allSatisfy(r -> {
            assertThat(r.get("actor_kind")).isEqualTo("pairing");
            assertThat(r.get("remote_ip")).isEqualTo("10.4.0.1");
            assertThat(r.get("detail").toString()).doesNotContain(code).doesNotContain("ZZZZ");
        });
    }

    @Test
    @DisplayName("pairing never needs or returns an admin token: the claimed token cannot reach admin endpoints")
    void pairedTokenIsLeastPrivilege() throws Exception {
        String code = create("{\"name\":\"Phone\",\"scope\":\"read\"}").get("code").asString();
        String token = json.readTree(claim(code, "10.5.0.1").getContentAsString()).get("token").asString();

        assertThat(perform(get("/v1/tokens").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)).getStatus())
                .isEqualTo(403);
    }
}
