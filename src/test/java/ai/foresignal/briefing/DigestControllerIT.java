package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

/**
 * {@code GET /v1/digest} over HTTP (prompt 029): defaults, the {@code next} cursor round trip,
 * ETag and 304, the read scope, and the scope filters with private sources counted but not listed.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class DigestControllerIT extends BriefingItSupport {

    @Autowired MockMvc mvc;

    BriefingHttp http;

    @BeforeEach
    void setUpHttp() {
        http = new BriefingHttp(mvc);
    }

    JsonNode digest(String query, String bearer) throws Exception {
        return http.call("GET", "/v1/digest" + query, bearer, null, 200);
    }

    @Test
    @DisplayName("defaults: the last 24 hours, counts, active entities, notable records and the next cursor")
    void defaults() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        quake("q2", "Kobe", 4.2);

        JsonNode d = digest("", reader);

        assertThat(d.get("fromSeq").asLong()).isZero();
        long toSeq = d.get("toSeq").asLong();
        assertThat(toSeq).isEqualTo(headSeq(BriefingTestSources.QUAKES, "q2"));
        assertThat(d.get("gap").asBoolean()).isFalse();
        assertThat(d.get("fromTime").asString()).isNotBlank();
        assertThat(d.get("toTime").asString()).isEqualTo(clock.instant().toString());
        assertThat(d.at("/counts/0/sourceId").asString()).isEqualTo(BriefingTestSources.QUAKES);
        assertThat(d.at("/counts/0/new").asLong()).isEqualTo(2);
        assertThat(d.at("/active/0/entities").asLong()).isEqualTo(2);
        assertThat(d.at("/notable/0/items/0/externalId").asString()).isEqualTo("q1");
        assertThat(d.at("/notable/0/items/0/seq").asLong()).isEqualTo(headSeq(BriefingTestSources.QUAKES, "q1"));
        assertThat(d.get("health").isArray()).as("includeHealth defaults to true").isTrue();
        assertThat(d.at("/next/sinceSeq").asLong()).isEqualTo(toSeq);
        assertThat(d.at("/next/sinceAlertId").isNumber()).isTrue();
        assertThat(d.get("alerts").get("bySeverity").has("critical")).isTrue();
    }

    @Test
    @DisplayName("the next cursor round trip: nothing new, then only what arrived since")
    void cursorRoundTrip() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        JsonNode first = digest("", reader);
        long sinceSeq = first.at("/next/sinceSeq").asLong();
        long sinceAlert = first.at("/next/sinceAlertId").asLong();

        JsonNode empty = digest("?sinceSeq=" + sinceSeq + "&sinceAlertId=" + sinceAlert, reader);
        assertThat(empty.get("counts")).isEmpty();
        assertThat(empty.get("notable")).isEmpty();
        assertThat(empty.at("/next/sinceSeq").asLong()).isEqualTo(sinceSeq);

        clock.advance(Duration.ofMinutes(5));
        quake("q2", "Kobe", 4.2);
        JsonNode next = digest("?sinceSeq=" + sinceSeq + "&sinceAlertId=" + sinceAlert, reader);
        assertThat(next.at("/counts/0/new").asLong()).isEqualTo(1);
        assertThat(next.at("/notable/0/items/0/externalId").asString()).isEqualTo("q2");
        assertThat(next.at("/next/sinceSeq").asLong()).isGreaterThan(sinceSeq);
    }

    @Test
    @DisplayName("ETag: 304 while nothing changed for the same parameters, 200 after an ingest")
    void etag() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        MvcResult first = http.perform(BriefingHttp.request("GET", "/v1/digest?sinceSeq=0", reader, null), 200);
        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotBlank();

        http.perform(BriefingHttp.request("GET", "/v1/digest?sinceSeq=0", reader, null)
                .header(HttpHeaders.IF_NONE_MATCH, etag), 304);
        http.perform(BriefingHttp.request("GET", "/v1/digest?sinceSeq=0&limit=3", reader, null)
                .header(HttpHeaders.IF_NONE_MATCH, etag), 200);

        quake("q2", "Kobe", 4.2);
        MvcResult changed = http.perform(BriefingHttp.request("GET", "/v1/digest?sinceSeq=0", reader, null)
                .header(HttpHeaders.IF_NONE_MATCH, etag), 200);
        assertThat(changed.getResponse().getHeader(HttpHeaders.ETAG)).isNotEqualTo(etag);
    }

    @Test
    @DisplayName("read scope: open while openReads is on, a token needed when it is off")
    void readScope() throws Exception {
        digest("", null);
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        http.call("GET", "/v1/digest", null, null, 401);
        digest("", reader);
    }

    @Test
    @DisplayName("scope filters; private sources counted but not listed for a read token, listed for an agent")
    void scopeFilters() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        secretQuake("s1", "Hidden Lake", 7.0);
        records.ingest(ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD,
                ai.foresignal.source.spi.NormalizedRecord.builder("x", ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND)
                        .observedAt(clock.instant()).put("v", 1).build());

        JsonNode filtered = digest("?kind=" + BriefingTestSources.QUAKE_KIND, reader);
        assertThat(filtered.get("counts").toString()).contains(BriefingTestSources.QUAKES)
                .doesNotContain(ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD);
        assertThat(filtered.get("counts").toString()).as("private counted").contains(BriefingTestSources.SECRET);
        assertThat(filtered.get("notable").toString()).doesNotContain("Hidden Lake").contains("Tokyo Bay");

        JsonNode bySource = digest("?source=" + BriefingTestSources.QUAKES, reader);
        assertThat(bySource.get("counts")).hasSize(1);

        JsonNode outside = digest("?kind=" + BriefingTestSources.QUAKE_KIND + "&bbox=0,0,10,10", reader);
        assertThat(outside.get("counts")).isEmpty();

        JsonNode asAgent = digest("?kind=" + BriefingTestSources.QUAKE_KIND, agent);
        assertThat(asAgent.get("notable").toString()).contains("Hidden Lake");

        JsonNode problem = http.call("GET", "/v1/digest?source=" + BriefingTestSources.SECRET, reader, null, 400);
        assertThat(problem.get("errors").toString()).contains("unknown source");
        http.call("GET", "/v1/digest?kind=nope", reader, null, 400);
        http.call("GET", "/v1/digest?limit=21", reader, null, 400);
        http.call("GET", "/v1/digest?places=home", reader, null, 400);
        http.call("GET", "/v1/digest?bbox=1,2,3", reader, null, 400);
    }
}
