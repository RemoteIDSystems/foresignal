package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.source.adapters.nvd.CveLedger;
import ai.foresignal.source.adapters.nvd.NvdCvesAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The prompt 076 sources end to end through the real fetch path and ingest writer: every source registers disabled
 * with a license; an EPSS score change inside a band creates no version, a band change exactly one; an NVD record
 * whose only change is {@code lastModified} is a touch (it drives {@code observedAt}, which is not hashed).
 *
 * <p>{@code nvd-cves} is used by no other test, so per-entity history holds in the shared context. The two tests use
 * different CVEs and reset the source's adapter state first.
 */
class CyberIngestIT extends WeatherIngestSupport {

    static final String NVD_ID = NvdCvesAdapter.ID;
    static final String NVD = "/services.nvd.nist.gov/rest/json/cves/2.0";
    static final String EPSS = "/api.first.org/data/v1/epss";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @AfterEach
    void disable() {
        configure(NVD_ID, false, Map.of());
    }

    @Autowired UpstreamClient client;

    /** Forgets the cursor, the EPSS ledger and the refresh markers of {@code nvd-cves}. */
    private void resetState() {
        var state = client.stateStore(NVD_ID);
        for (String key : List.of(NvdCvesAdapter.CURSOR_KEY, NvdCvesAdapter.WINDOW_KEY, CveLedger.STATE_KEY,
                NvdCvesAdapter.REFRESHED_ON_KEY, NvdCvesAdapter.REFRESH_FROM_KEY)) {
            state.remove(key);
        }
    }

    private static void serve(String path, byte[] body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    /** An NVD page with the fixture CVE {@code id}, its {@code lastModified} replaced when not null. */
    private static byte[] nvdPage(String id, String lastModified) {
        ObjectNode root = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("nvd-cves", "cves-page.json"));
        ArrayNode vulns = (ArrayNode) root.get("vulnerabilities");
        for (int i = vulns.size() - 1; i >= 0; i--) {
            ObjectNode cve = (ObjectNode) vulns.get(i).get("cve");
            if (!id.equals(cve.get("id").asString())) {
                vulns.remove(i);
            } else if (lastModified != null) {
                cve.put("lastModified", lastModified);
            }
        }
        root.put("totalResults", vulns.size()).put("resultsPerPage", vulns.size());
        return JSON.writeValueAsBytes(root);
    }

    private static byte[] emptyNvdPage() {
        return ("{\"resultsPerPage\": 0, \"startIndex\": 0, \"totalResults\": 0, \"format\": \"NVD_CVE\","
                + " \"version\": \"2.0\", \"vulnerabilities\": []}").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] epss(String cve, String epss, String percentile, String date) {
        return ("{\"status\": \"OK\", \"status-code\": 200, \"version\": \"1.0\", \"total\": 1, \"offset\": 0,"
                + " \"limit\": 100, \"data\": [{\"cve\": \"" + cve + "\", \"epss\": \"" + epss + "\", \"percentile\": \""
                + percentile + "\", \"date\": \"" + date + "\"}]}").getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("every 076 source is registered, disabled by default")
    void registered() {
        List<String> ids = List.of("cisa-kev", "abusech-feodo", "abusech-urlhaus", "nvd-cves", "ct-domain-watch");
        assertThat(sources.list()).filteredOn(s -> ids.contains(s.id())).hasSize(ids.size())
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
    }

    @Test
    @DisplayName("EPSS: a score change inside a band is a touch; a band change is exactly one version")
    void epssBands() {
        resetState();
        String cve = "CVE-2015-5477";
        String id = "epss:" + cve;
        clock.setInstant(Instant.parse("2026-10-13T20:00:00Z"));
        configure(NVD_ID, true, Map.of());
        serve(NVD, nvdPage(cve, null));
        serve(EPSS, AdapterFixtures.bytes("nvd-cves", "epss.json"));
        assertThat(fetch(NVD_ID).success()).isTrue();
        assertThat(versions(NVD_ID, cve)).isEqualTo(1);
        assertThat(versions(NVD_ID, id)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select payload->>'epssBand' from record_head where source_id = ? and"
                + " external_id = ?", String.class, NVD_ID, id)).isEqualTo("very-high");

        // Next day: the CVE is unchanged; the daily refresh scores it again, 0.99409 → 0.9902, still very-high, p99.
        clock.advance(Duration.ofDays(1));
        UPSTREAM.resetAll();
        serve(NVD, emptyNvdPage());
        serve(EPSS, epss(cve, "0.990200000", "0.999000000", "2026-10-14"));
        assertThat(fetch(NVD_ID).success()).isTrue();
        assertThat(versions(NVD_ID, id)).as("same bands: a touch").isEqualTo(1);
        assertThat(live("observed_at", NVD_ID, id)).isEqualTo(Instant.parse("2026-10-14T00:00:00Z"));

        // The day after: 0.45 is the high band.
        clock.advance(Duration.ofDays(1));
        UPSTREAM.resetAll();
        serve(NVD, emptyNvdPage());
        serve(EPSS, epss(cve, "0.450000000", "0.991000000", "2026-10-15"));
        assertThat(fetch(NVD_ID).success()).isTrue();
        assertThat(versions(NVD_ID, id)).as("a band change: one version").isEqualTo(2);
        assertThat(jdbc.queryForObject("select payload->>'epssBand' from record_head where source_id = ? and"
                + " external_id = ?", String.class, NVD_ID, id)).isEqualTo("high");
        assertThat(versions(NVD_ID, cve)).as("the CVE itself is untouched").isEqualTo(1);
    }

    @Test
    @DisplayName("NVD: a record whose only change is lastModified is a touch, not a version")
    void lastModifiedOnly() {
        resetState();
        String cve = "CVE-2025-59951";
        clock.setInstant(Instant.parse("2026-10-13T20:00:00Z"));
        configure(NVD_ID, true, Map.of());
        serve(NVD, nvdPage(cve, null));
        serve(EPSS, epss(cve, "0.000500000", "0.150000000", "2026-10-13"));
        assertThat(fetch(NVD_ID).success()).isTrue();
        assertThat(versions(NVD_ID, cve)).isEqualTo(1);

        clock.advance(Duration.ofHours(2));
        UPSTREAM.resetAll();
        serve(NVD, nvdPage(cve, "2026-10-13T21:30:00.000"));
        serve(EPSS, epss(cve, "0.000600000", "0.160000000", "2026-10-13"));
        assertThat(fetch(NVD_ID).success()).isTrue();
        assertThat(versions(NVD_ID, cve)).as("lastModified alone: a touch").isEqualTo(1);
        assertThat(live("observed_at", NVD_ID, cve)).isEqualTo(Instant.parse("2026-10-13T21:30:00Z"));
        assertThat(versions(NVD_ID, "epss:" + cve)).isEqualTo(1);
    }
}
