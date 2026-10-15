package ai.foresignal.source.adapters.faaregistry;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code faa-registry} against a stubbed archive through the real upstream client (prompt 098). */
@IntegrationTest
class FaaRegistryAdapterTest extends WireMockSupport {

    private static final Instant NOW = Instant.parse("2026-10-15T06:00:00Z");
    private static final String PATH = "/registry.faa.gov/database/ReleasableAircraft.zip";
    private static final String LAST_MODIFIED = "Wed, 14 Oct 2026 23:30:00 GMT";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ForesignalProperties properties;

    @Autowired
    private InstallIdService install;

    private AdapterHttpHarness harness;
    private FaaRegistryAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000, 500));
        adapter = new FaaRegistryAdapter(baseUrl());
        harness.reset(adapter.id(), FaaRegistryAdapter.STATE);
    }

    @Test
    void downloadsAndParsesTheArchive() throws Exception {
        serveArchive(200);

        List<NormalizedRecord> collected = new ArrayList<>();
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));

        assertThat(result.complete()).isTrue();
        assertThat(collected).hasSize(2);
        assertThat(collected.get(0).payload()).containsEntry("nNumber", "123AB");
        assertThat(collected.get(0).payload()).doesNotContainKey("NAME");
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), collected);
    }

    @Test
    void aConditionalGetReturnsNotModified() throws Exception {
        serveArchive(200);
        adapter.fetch(harness.context(adapter, Map.of(), null, null, records -> {}));

        WIREMOCK.stubFor(get(urlPathEqualTo(PATH))
                .withHeader("If-Modified-Since", equalTo(LAST_MODIFIED))
                .willReturn(aResponse().withStatus(304)));

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of(), null, null, records -> {}));
        assertThat(second.notModified()).isTrue();
    }

    private static void serveArchive(int status) {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/zip")
                .withHeader("Last-Modified", LAST_MODIFIED)
                .withBody(archive())));
    }

    private static byte[] archive() {
        String masterHeader = "N-NUMBER,SERIAL NUMBER,MFR MDL CODE,YEAR MFR,TYPE REGISTRANT,NAME,STREET,CITY,STATE,"
                + "MODE S CODE HEX,";
        String acftrefHeader = "CODE,MFR,MODEL,TYPE-ACFT,TYPE-ENG,NO-ENG,NO-SEATS,AC-WEIGHT,";
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(out)) {
                zip.putNextEntry(new ZipEntry("MASTER.txt"));
                zip.write((masterHeader + "\n"
                        + "123AB,1234,S22,1975,1,JOHN DOE,1 MAIN ST,AUSTIN,TX,AB1234,\n"
                        + "456CD,5678,S22,1980,3,ACME LLC,2 ELM ST,DALLAS,TX,CD5678,\n")
                        .getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry("ACFTREF.txt"));
                zip.write((acftrefHeader + "\n" + "S22,CESSNA,172,4,1,1,4,CLASS 1,\n").getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
