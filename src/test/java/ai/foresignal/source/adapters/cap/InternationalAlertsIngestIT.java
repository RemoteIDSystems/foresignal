package ai.foresignal.source.adapters.cap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end ingest for the prompt 089 international alerts: a CAP {@code Alert}, {@code Update} and
 * {@code Cancel} from one reviewed feed reach the store through the real fetch path and writer as three versions
 * of one record, the last a tombstone.
 */
class InternationalAlertsIngestIT extends WeatherIngestSupport {

    static final String INDEX_PATH = "/DWD/warnungen/cap-feed/de/atom.xml";
    static final String EXTERNAL_ID = "test@example.org:test-alert-1";

    private static String doc(String name) {
        return new String(AdapterFixtures.bytes("cap-de-dwd-de", name), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Alert, Update and Cancel become three versions of one record, the last a tombstone")
    void capUpdateChain() {
        String index = "<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><title>test</title>"
                + entry("a", "/capdoc/alert.xml") + entry("b", "/capdoc/update.xml") + entry("c", "/capdoc/cancel.xml")
                + "</feed>";
        UPSTREAM.stubFor(get(urlPathEqualTo(INDEX_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml;charset=UTF-8").withBody(index)));
        UPSTREAM.stubFor(get(urlPathEqualTo("/capdoc/alert.xml")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/cap+xml;charset=UTF-8").withBody(doc("chain-alert.xml"))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/capdoc/update.xml")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/cap+xml;charset=UTF-8").withBody(doc("chain-update.xml"))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/capdoc/cancel.xml")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/cap+xml;charset=UTF-8").withBody(doc("chain-cancel.xml"))));

        configure("cap-de-dwd-de", true, Map.of());
        fetch("cap-de-dwd-de");

        assertThat(versions("cap-de-dwd-de", EXTERNAL_ID)).isEqualTo(3);
        assertThat(tombstoned("cap-de-dwd-de", EXTERNAL_ID)).isTrue();
    }

    private static String entry(String id, String path) {
        return "<entry><title>" + id + "</title><id>" + id + "</id><updated>2026-10-14T12:00:00Z</updated>"
                + "<link href=\"" + UPSTREAM.baseUrl() + path + "\"/></entry>";
    }
}
