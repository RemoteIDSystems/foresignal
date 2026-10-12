package ai.foresignal.api;

import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stats.glance.GlanceSettings;
import ai.foresignal.store.AlertEntity;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
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
 * {@code GET /v1/glance} over HTTP (prompt 038): strong ETag and 304, the 15 s private cache headers,
 * the text form, scopes (anonymous under {@code openReads}, 401 without it), the private-source gate
 * and {@code place}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(StatsTestSources.class)
class GlanceControllerIT extends StatsItSupport {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired AlertRepository alerts;
    @Autowired GlanceController controller;

    String reader;
    String agent;

    @BeforeEach
    void setUp() {
        controller.clearCache();
        reader = TestTokens.bearer(TestTokens.create(tokens, "glance-reader", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "glance-agent", Scope.AGENT));
    }

    @AfterEach
    void restore() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        controller.clearCache();
    }

    private AlertEntity alert(String source, String severity, Instant at, double lon, double lat) {
        AlertEntity a = new AlertEntity(UUID.randomUUID(), 1L, source, "x-" + severity, at);
        a.setKind(POINT_KIND);
        a.setSeverity(severity);
        a.setMessage("Alert " + severity + " from " + source);
        a.setRecordSnapshot(Map.of("geometry", Map.of("type", "Point", "coordinates", List.of(lon, lat))));
        return alerts.saveAndFlush(a);
    }

    @Test
    @DisplayName("strong ETag, If-None-Match → 304, Cache-Control private, max-age=15; within 2 KB")
    void etagAndCaching() throws Exception {
        alert(POINTS, "critical", T0, 10, 50);
        MvcResult first = mvc.perform(get("/v1/glance").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=15, private"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.alerts.open.critical").value(1))
                .andExpect(jsonPath("$.alerts.latest[0].severity").value("critical"))
                .andReturn();
        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).startsWith("\"");
        assertThat(first.getResponse().getContentAsString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8))
                .hasSizeLessThanOrEqualTo(2048);

        mvc.perform(get("/v1/glance").header(HttpHeaders.AUTHORIZATION, reader).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
        alert(POINTS, "warning", T0.plusSeconds(1), 10, 50);
        mvc.perform(get("/v1/glance").header(HttpHeaders.AUTHORIZATION, reader).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified()); // served from the 15 s cache
        controller.clearCache();
        mvc.perform(get("/v1/glance").header(HttpHeaders.AUTHORIZATION, reader).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts.open.warning").value(1));
    }

    @Test
    @DisplayName("format=text and Accept: text/plain give the ASCII form")
    void text() throws Exception {
        String body = mvc.perform(get("/v1/glance").param("format", "text").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body.split("\n")).hasSizeLessThanOrEqualTo(8).allSatisfy(l -> assertThat(l).hasSizeLessThanOrEqualTo(40));
        assertThat(body).startsWith("FORESIGNAL ");
        mvc.perform(get("/v1/glance").accept(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8"));
        mvc.perform(get("/v1/glance").param("format", "xml")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("anonymous under openReads: no counts and no private-source alerts; 401 once openReads is off")
    void scopes() throws Exception {
        alert(SECRET, "critical", T0, 10, 50);
        alert(POINTS, "info", T0.plusSeconds(1), 10, 50);

        mvc.perform(get("/v1/glance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts.open").doesNotExist())
                .andExpect(jsonPath("$.sources").doesNotExist())
                .andExpect(jsonPath("$.activity").doesNotExist())
                .andExpect(jsonPath("$.alerts.latest.length()").value(1))
                .andExpect(jsonPath("$.alerts.latest[0].severity").value("info"))
                .andExpect(jsonPath("$.lastSeq").isNumber());

        settings.put(SettingKeys.GLANCE, new GlanceSettings(true));
        controller.clearCache();
        mvc.perform(get("/v1/glance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts.open.info").value(1))
                .andExpect(jsonPath("$.alerts.open.critical").value(0))
                .andExpect(jsonPath("$.activity.versionsLastHour").isNumber());

        mvc.perform(get("/v1/glance").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts.open.critical").value(1));

        settings.put(SettingKeys.SECURITY, new SecuritySettings(false, null, null, null));
        mvc.perform(get("/v1/glance")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/glance").header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("place narrows the alerts to a 036 place and needs a token")
    void place() throws Exception {
        mvc.perform(post("/v1/places").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"home\", \"name\": \"Home\", \"shape\": \"point\", \"geometry\": {\"type\":"
                                + " \"Point\", \"coordinates\": [10, 50]}, \"radiusKm\": 25}"))
                .andExpect(status().is2xxSuccessful());
        alert(POINTS, "critical", T0, 10.01, 50.01);
        alert(POINTS, "warning", T0.plusSeconds(1), 40, 10);

        mvc.perform(get("/v1/glance").param("place", "home").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts.open.critical").value(1))
                .andExpect(jsonPath("$.alerts.open.warning").value(0))
                .andExpect(jsonPath("$.alerts.latest.length()").value(1));
        mvc.perform(get("/v1/glance").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.alerts.open.warning").value(1));
        mvc.perform(get("/v1/glance").param("place", "home")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/glance").param("place", "nowhere").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isNotFound());
    }
}
