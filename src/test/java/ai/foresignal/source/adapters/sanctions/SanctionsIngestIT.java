package ai.foresignal.source.adapters.sanctions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end sanctions ingest (prompt 091): re-ingesting an unchanged UN list produces no versions, and a
 * genuinely delisted entry is tombstoned (FR-15).
 */
class SanctionsIngestIT extends WeatherIngestSupport {

    static final String EXPORT_PATH = "/resources/xml/en/consolidated.xml";
    static final Pattern FIRST_REFERENCE = Pattern.compile("<REFERENCE_NUMBER>([^<]+)</REFERENCE_NUMBER>");

    @Test
    @DisplayName("an unchanged list adds no versions; a delisted entry is tombstoned")
    void delisting() {
        String full = new String(AdapterFixtures.bytes("un-sanctions", "consolidated.xml"), StandardCharsets.UTF_8);
        serve("v1", full);
        configure("un-sanctions", true, Map.of());
        fetch("un-sanctions");
        int afterFirst = versions("un-sanctions", (PersistReason) null);
        assertThat(afterFirst).isGreaterThan(0);

        fetch("un-sanctions");
        assertThat(versions("un-sanctions", (PersistReason) null)).isEqualTo(afterFirst);

        Matcher matcher = FIRST_REFERENCE.matcher(full);
        assertThat(matcher.find()).isTrue();
        String removed = "un:" + matcher.group(1);
        String shortened = full.replaceFirst("<INDIVIDUAL>.*?</INDIVIDUAL>", "");
        serve("v2", shortened);
        fetch("un-sanctions");
        assertThat(tombstoned("un-sanctions", removed)).isTrue();
    }

    private static void serve(String version, String body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(EXPORT_PATH)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", UPSTREAM.baseUrl() + "/blob/" + version + ".xml?sv=secret")));
        UPSTREAM.stubFor(get(urlPathEqualTo("/blob/" + version + ".xml")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withBody(body)));
    }
}
