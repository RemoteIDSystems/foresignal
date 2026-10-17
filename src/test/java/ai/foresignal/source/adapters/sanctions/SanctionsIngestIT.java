package ai.foresignal.source.adapters.sanctions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end sanctions ingest (prompt 091) against the real redirect shape: the UN export answers 302 to a
 * <strong>fixed</strong> blob path whose SAS query changes on every request, and the blob answers 304 to a
 * matching {@code If-None-Match}. Re-ingesting an unchanged list produces no versions and no blob download; an
 * amended {@code LAST_DAY_UPDATED} produces a SIGNIFICANT version; a delisted entry is tombstoned (FR-15); the
 * SAS signature never reaches the fetch log.
 */
class SanctionsIngestIT extends WeatherIngestSupport {

    static final String EXPORT_PATH = "/resources/xml/en/consolidated.xml";
    static final String BLOB_PATH = "/publiclegacyxmlfiles/EN/consolidatedLegacyByPRN.xml";
    static final Pattern FIRST_REFERENCE = Pattern.compile("<REFERENCE_NUMBER>([^<]+)</REFERENCE_NUMBER>");

    static final UUID BLOB_STUB = UUID.fromString("0b1e5a7c-0000-4000-8000-000000000091");
    static final UUID NOT_MODIFIED_STUB = UUID.fromString("0b1e5a7c-0000-4000-8000-000000000304");

    private int signature;

    @Test
    @DisplayName("unchanged: no versions and a 304; amended lastUpdated: a version; delisted: a tombstone")
    void lifecycle() {
        String full = new String(AdapterFixtures.bytes("un-sanctions", "consolidated.xml"), StandardCharsets.UTF_8);
        serve("\"v1\"", full);
        configure("un-sanctions", true, Map.of());
        FetchOutcome first = fetch("un-sanctions");
        int afterFirst = versions("un-sanctions", (PersistReason) null);
        assertThat(afterFirst).as(String.valueOf(first)).isGreaterThan(0);

        // a new SAS query, the same blob: a 304 and nothing new
        redirect();
        fetch("un-sanctions");
        assertThat(versions("un-sanctions", (PersistReason) null)).isEqualTo(afterFirst);
        UPSTREAM.verify(1, getRequestedFor(urlPathEqualTo(BLOB_PATH)).withHeader("If-None-Match", equalTo("\"v1\"")));

        // CDi.030's newest LAST_DAY_UPDATED moves forward in a new publication: one SIGNIFICANT version
        String amended = full.replace("dateGenerated=\"2026-10-13T23:00:01.949Z\"",
                        "dateGenerated=\"2026-10-14T23:00:01.949Z\"")
                .replace("<VALUE>2023-03-01</VALUE>", "<VALUE>2026-10-14</VALUE>");
        assertThat(amended).isNotEqualTo(full);
        serve("\"v2\"", amended);
        int significantBefore = versions("un-sanctions", PersistReason.SIGNIFICANT);
        fetch("un-sanctions");
        assertThat(versions("un-sanctions", PersistReason.SIGNIFICANT)).isGreaterThan(significantBefore);
        assertThat(versions("un-sanctions", "un:CDi.030")).isEqualTo(2);

        Matcher matcher = FIRST_REFERENCE.matcher(amended);
        assertThat(matcher.find()).isTrue();
        String removed = "un:" + matcher.group(1);
        serve("\"v3\"", amended.replaceFirst("(?s)<INDIVIDUAL>.*?</INDIVIDUAL>", ""));
        fetch("un-sanctions");
        assertThat(tombstoned("un-sanctions", removed)).isTrue();

        List<String> logged = jdbc.queryForList("select coalesce(url_redacted, '') || ' ' || coalesce(error, '')"
                + " from fetch_log where source_id = 'un-sanctions'", String.class);
        assertThat(logged).isNotEmpty().allSatisfy(row -> assertThat(row).doesNotContain("sig-"));
    }

    /** Points the export at the fixed blob path with a fresh SAS query. */
    private void redirect() {
        signature++;
        UPSTREAM.stubFor(get(urlPathEqualTo(EXPORT_PATH)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", UPSTREAM.baseUrl() + BLOB_PATH + "?sv=2024-05-04&se=2026-10-16T21%3A05%3A01Z"
                        + "&sr=b&sp=r&sig=sig-" + signature)));
    }

    /** Serves {@code body} as the blob with {@code etag}, answering 304 to a matching If-None-Match. */
    private void serve(String etag, String body) {
        redirect();
        // fixed stub ids, so a new publication replaces the previous one's 200 and 304 instead of adding to them;
        // WireMock 3 refuses a stub whose id is taken, so the previous publication's stubs are removed first
        UPSTREAM.getStubMappings().stream()
                .filter(m -> BLOB_STUB.equals(m.getId()) || NOT_MODIFIED_STUB.equals(m.getId()))
                .toList().forEach(UPSTREAM::removeStub);
        UPSTREAM.stubFor(get(urlPathEqualTo(BLOB_PATH)).withId(BLOB_STUB).atPriority(5).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withHeader("ETag", etag).withBody(body)));
        UPSTREAM.stubFor(get(urlPathEqualTo(BLOB_PATH)).withId(NOT_MODIFIED_STUB).atPriority(1).withHeader("If-None-Match", equalTo(etag))
                .willReturn(aResponse().withStatus(304).withHeader("ETag", etag)));
    }
}
