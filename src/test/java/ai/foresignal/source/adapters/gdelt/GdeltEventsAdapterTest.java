package ai.foresignal.source.adapters.gdelt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code gdelt-events} against a stubbed {@code storage.googleapis.com} (prompt 066): newest file only, the
 * {@code lastFile} hand-off, size and MD5 checks, the ZIP rules, skipped-file coverage, and that every request goes to
 * the bucket path rebuilt from a validated file name.
 */
@IntegrationTest
class GdeltEventsAdapterTest extends WireMockSupport {

    private static final String ID = GdeltEventsAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-13T14:35:00Z");
    private static final String FILE = "20261013143000.export.CSV.zip";
    private static final String LASTUPDATE_PATH = GdeltEventsAdapter.PATH + "lastupdate.txt";
    private static final String ZIP_PATH = GdeltEventsAdapter.PATH + FILE;
    private static final String ETAG_KEY = SourceHttp.ETAG_STATE_PREFIX + GdeltEventsAdapter.LASTUPDATE_STATE_KEY;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private SimpleMeterRegistry meters;
    private GdeltEventsAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, GdeltEventsAdapter.LAST_FILE, GdeltEventsAdapter.PENDING_FILE, ETAG_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + GdeltEventsAdapter.LASTUPDATE_STATE_KEY);
        meters = new SimpleMeterRegistry();
        adapter = new GdeltEventsAdapter(baseUrl(), new CountryCentroids(), meters);
    }

    private DefaultFetchContext context(Instant lastSuccess) {
        return harness.context(adapter, Map.of(), null, lastSuccess);
    }

    private AdapterStateStore state() {
        return harness.client.stateStore(ID);
    }

    private static void serveLastUpdate(String body) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(LASTUPDATE_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain").withHeader("ETag", "\"v1\"").withBody(body)));
    }

    private static void serveZip(String path, byte[] zip) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/zip").withBody(zip)));
    }

    private static void serveFixtures() {
        serveLastUpdate(AdapterFixtures.text(ID, "lastupdate.txt"));
        serveZip(ZIP_PATH, AdapterFixtures.bytes(ID, FILE));
    }

    /** A {@code lastupdate.txt} whose export line declares {@code zip}'s real size and MD5. */
    private static String lastUpdateFor(String file, byte[] zip) {
        return zip.length + " " + GdeltEventsAdapter.md5(zip) + " http://data.gdeltproject.org/gdeltv2/" + file + "\n"
                + "135393 7e40ab6dc4a8b33ad2e8db4de2ce9dd2 http://data.gdeltproject.org/gdeltv2/"
                + file.replace("export", "mentions") + "\n"
                + "6652460 879e365ee5a07c725e485f31eb24b5c7 http://data.gdeltproject.org/gdeltv2/"
                + file.replace("export.CSV", "gkg.csv") + "\n";
    }

    private static List<String> requestedUrls() {
        return WIREMOCK.getAllServeEvents().stream().map(ServeEvent::getRequest).map(r -> r.getUrl()).toList();
    }

    @Test
    @DisplayName("the first file: 183 events, a pending hand-off, and only bucket paths requested")
    void firstFile() throws Exception {
        serveFixtures();

        FetchResult result = adapter.fetch(context(null));

        assertThat(result.notModified()).isFalse();
        assertThat(result.records()).hasSize(183);
        assertThat(result.coverage().isEmpty()).isTrue();
        assertThat(state().get(GdeltEventsAdapter.PENDING_FILE)).contains(FILE + "@" + NOW);
        assertThat(state().get(GdeltEventsAdapter.LAST_FILE)).isEmpty();
        assertThat(requestedUrls()).containsExactlyInAnyOrder(LASTUPDATE_PATH, ZIP_PATH);
    }

    @Test
    @DisplayName("production URLs are HTTPS on storage.googleapis.com, rebuilt from the file name")
    void productionHost() {
        GdeltEventsAdapter production = new GdeltEventsAdapter("", null, null);
        URI index = production.lastUpdateUri();
        URI export = production.exportUri(FILE);
        assertThat(index).hasToString("https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/lastupdate.txt");
        assertThat(export).hasToString("https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/" + FILE);
        assertThatThrownBy(() -> production.exportUri("../" + FILE)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> production.exportUri(null)).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("an export URL on a foreign host is never requested; the bucket path is")
    void foreignHost() throws Exception {
        serveLastUpdate(AdapterFixtures.text(ID, "lastupdate-foreign-host.txt"));
        serveZip(ZIP_PATH, AdapterFixtures.bytes(ID, FILE));

        assertThat(adapter.fetch(context(null)).records()).hasSize(183);
        assertThat(requestedUrls()).containsExactlyInAnyOrder(LASTUPDATE_PATH, ZIP_PATH)
                .noneMatch(url -> url.contains("example") || url.contains("mirror"));
    }

    @Test
    @DisplayName("the same lastFile: not modified, and no ZIP request")
    void sameLastFile() throws Exception {
        serveFixtures();
        state().put(GdeltEventsAdapter.LAST_FILE, FILE);

        FetchResult result = adapter.fetch(context(null));

        assertThat(result.notModified()).isTrue();
        assertThat(result.records()).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(ZIP_PATH)));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(LASTUPDATE_PATH)));
    }

    @Test
    @DisplayName("a 304 for lastupdate.txt is not modified, with no ZIP request")
    void conditional() throws Exception {
        serveFixtures();
        adapter.fetch(context(null));
        assertThat(state().get(ETAG_KEY)).contains("\"v1\"");
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(LASTUPDATE_PATH)).atPriority(1)
                .withHeader("If-None-Match", equalTo("\"v1\"")).willReturn(aResponse().withStatus(304)));
        harness.clock.advance(Duration.ofMinutes(15));

        FetchResult second = adapter.fetch(context(NOW.plusSeconds(1)));

        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(ZIP_PATH)));
        assertThat(state().get(GdeltEventsAdapter.LAST_FILE)).contains(FILE);
    }

    @Test
    @DisplayName("a pending file is promoted after a success covering its run, then not fetched again")
    void promoted() throws Exception {
        serveFixtures();
        adapter.fetch(context(null));
        harness.clock.advance(Duration.ofMinutes(15));

        FetchResult second = adapter.fetch(context(NOW.plusSeconds(2)));

        assertThat(second.notModified()).isTrue();
        assertThat(state().get(GdeltEventsAdapter.LAST_FILE)).contains(FILE);
        assertThat(state().get(GdeltEventsAdapter.PENDING_FILE)).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(ZIP_PATH)));
    }

    @Test
    @DisplayName("a pending file whose run was never ingested is discarded and fetched again")
    void discarded() throws Exception {
        serveFixtures();
        adapter.fetch(context(null));
        harness.clock.advance(Duration.ofMinutes(15));

        FetchResult second = adapter.fetch(context(NOW.minusSeconds(60)));

        assertThat(second.records()).hasSize(183);
        assertThat(state().get(GdeltEventsAdapter.LAST_FILE)).isEmpty();
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(ZIP_PATH)));
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(LASTUPDATE_PATH)).withoutHeader("If-None-Match"));
    }

    @Test
    @DisplayName("an MD5 mismatch is a format error; lastFile is unchanged and the validators are forgotten")
    void md5Mismatch() {
        serveLastUpdate(AdapterFixtures.text(ID, "bad-md5.txt"));
        serveZip(ZIP_PATH, AdapterFixtures.bytes(ID, FILE));
        state().put(GdeltEventsAdapter.LAST_FILE, "20261013141500.export.CSV.zip");

        assertThatThrownBy(() -> adapter.fetch(context(null))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("MD5");

        assertThat(state().get(GdeltEventsAdapter.LAST_FILE)).contains("20261013141500.export.CSV.zip");
        assertThat(state().get(GdeltEventsAdapter.PENDING_FILE)).isEmpty();
        assertThat(state().get(ETAG_KEY)).isEmpty();
    }

    @Test
    @DisplayName("a size mismatch is a format error, and lastFile is unchanged")
    void sizeMismatch() {
        byte[] zip = AdapterFixtures.bytes(ID, FILE);
        serveLastUpdate(lastUpdateFor(FILE, zip).replaceFirst("^" + zip.length, String.valueOf(zip.length + 1)));
        serveZip(ZIP_PATH, zip);

        assertThatThrownBy(() -> adapter.fetch(context(null))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("declared " + (zip.length + 1));
        assertThat(state().get(GdeltEventsAdapter.LAST_FILE)).isEmpty();
        assertThat(state().get(GdeltEventsAdapter.PENDING_FILE)).isEmpty();
    }

    @Test
    @DisplayName("a ZIP with two entries is refused")
    void twoEntries() {
        byte[] zip = AdapterFixtures.bytes(ID, "zip-two-entries.zip");
        serveLastUpdate(lastUpdateFor(FILE, zip));
        serveZip(ZIP_PATH, zip);

        assertThatThrownBy(() -> adapter.fetch(context(null))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("more than one entry");
        assertThat(state().get(GdeltEventsAdapter.PENDING_FILE)).isEmpty();
    }

    @Test
    @DisplayName("a ZIP whose entry has another name, or is a directory, is refused")
    void wrongEntry() throws IOException {
        byte[] renamed = zip("20261013141500.export.CSV", AdapterFixtures.bytes(ID, "export-40.tsv"));
        serveLastUpdate(lastUpdateFor(FILE, renamed));
        serveZip(ZIP_PATH, renamed);
        assertThatThrownBy(() -> adapter.fetch(context(null))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("other than 20261013143000.export.CSV");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("20261013143000.export.CSV/"));
            zip.closeEntry();
        }
        assertThatThrownBy(() -> ExportZip.parse(out.toByteArray(), "20261013143000.export.CSV",
                ExportZip.MAX_UNCOMPRESSED_BYTES, GdeltExportParser.Options.DEFAULTS, null))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("directory");
        assertThatThrownBy(() -> ExportZip.parse("not a zip".getBytes(StandardCharsets.UTF_8),
                "20261013143000.export.CSV", ExportZip.MAX_UNCOMPRESSED_BYTES, GdeltExportParser.Options.DEFAULTS,
                null)).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a 300 MB entry is refused at the 200 MB cap while streaming")
    void zipBomb() throws IOException {
        String file = "20261013144500.export.CSV.zip";
        byte[] line = new byte[1024];
        Arrays.fill(line, (byte) 'a');
        line[line.length - 1] = '\n';
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.setLevel(9);
            zip.putNextEntry(new ZipEntry("20261013144500.export.CSV"));
            for (int i = 0; i < 300 * 1024; i++) {
                zip.write(line);
            }
            zip.closeEntry();
        }
        byte[] bomb = out.toByteArray();
        assertThat(bomb.length).as("highly compressible").isLessThan(5 * 1024 * 1024);
        serveLastUpdate(lastUpdateFor(file, bomb));
        serveZip(GdeltEventsAdapter.PATH + file, bomb);

        assertThatThrownBy(() -> adapter.fetch(context(null))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("zip bomb");
        assertThat(state().get(GdeltEventsAdapter.PENDING_FILE)).isEmpty();
    }

    @Test
    @DisplayName("a file 30 minutes after lastFile: one file skipped, coverage 1 of 2, counted")
    void skippedCoverage() throws Exception {
        serveFixtures();
        state().put(GdeltEventsAdapter.LAST_FILE, "20261013140000.export.CSV.zip");

        FetchResult result = adapter.fetch(context(null));

        assertThat(result.coverage().expected()).isEqualTo(2);
        assertThat(result.coverage().actual()).isEqualTo(1);
        assertThat(meters.counter(GdeltEventsAdapter.FILES_SKIPPED).count()).isEqualTo(1.0);

        assertThat(GdeltEventsAdapter.filesDue(Instant.parse("2026-10-13T14:15:00Z"),
                Instant.parse("2026-10-13T14:30:00Z"))).isEqualTo(1);
        assertThat(GdeltEventsAdapter.filesDue(Instant.parse("2026-10-13T12:30:00Z"),
                Instant.parse("2026-10-13T14:30:00Z"))).isEqualTo(8);
        assertThat(GdeltEventsAdapter.filesDue(Instant.parse("2026-10-13T14:45:00Z"),
                Instant.parse("2026-10-13T14:30:00Z"))).isEqualTo(1);
    }

    @Test
    @DisplayName("the next file 15 minutes later: coverage 1 of 1, nothing skipped")
    void nextFile() throws Exception {
        serveFixtures();
        state().put(GdeltEventsAdapter.LAST_FILE, "20261013141500.export.CSV.zip");

        FetchResult result = adapter.fetch(context(null));

        assertThat(result.coverage().expected()).isEqualTo(1);
        assertThat(result.coverage().actual()).isEqualTo(1);
        assertThat(meters.counter(GdeltEventsAdapter.FILES_SKIPPED).count()).isZero();
    }

    @Test
    @DisplayName("parameters reach the parser: root codes, minimum mentions, requireGeo and countries")
    void params() throws Exception {
        serveFixtures();
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(GdeltEventsAdapter.ROOT_CODES_PARAM,
                List.of("18", "19", "20"), GdeltEventsAdapter.COUNTRIES_PARAM, List.of("us")), null, null));
        assertThat(result.records()).isNotEmpty().allSatisfy(r -> {
            assertThat(r.payload().get("eventRootCode")).isIn("18", "19", "20");
            assertThat(r.payload().get("actionGeoCountry")).isEqualTo("US");
        });

        GdeltExportParser.Options options = GdeltEventsAdapter.options(ValidatedParams.of(Map.of(
                GdeltEventsAdapter.MIN_MENTIONS_PARAM, 3, GdeltEventsAdapter.REQUIRE_GEO_PARAM, false)));
        assertThat(options.minMentions()).isEqualTo(3);
        assertThat(options.requireGeo()).isFalse();
        assertThat(options.rootCodes()).containsExactlyInAnyOrderElementsOf(
                GdeltExportParser.Options.DEFAULT_ROOT_CODES);
        assertThat(GdeltEventsAdapter.options(ValidatedParams.empty())).isEqualTo(GdeltExportParser.Options.DEFAULTS);
    }

    @Test
    @DisplayName("no request ever fetches a SOURCEURL")
    void neverFetchesSourceUrl() throws Exception {
        serveFixtures();
        FetchResult result = adapter.fetch(context(null));
        assertThat(result.records()).anySatisfy(r -> assertThat(r.payload()).containsKey("sourceUrl"));
        WIREMOCK.verify(2, anyRequestedFor(anyUrl()));
        assertThat(requestedUrls()).allMatch(url -> url.startsWith(GdeltEventsAdapter.PATH));
    }

    @Test
    @DisplayName("descriptor: valid defaults, STANDARD/ALL, TTL 24 h from receipt, 90 d, text fields")
    void descriptor() {
        var d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("gdelt-events");
        assertThat(d.kinds()).containsExactly("conflict-event");
        assertThat(d.requiresCredentials()).isFalse();
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.defaultIngestPolicy()).isEqualTo(IngestPolicy.all());
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(24));
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(90));
        assertThat(d.kindRetention()).containsEntry("conflict-event", Duration.ofDays(90));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 400));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.embedFields()).containsExactly("summary");
        assertThat(d.titleField()).isEqualTo("summary");
        assertThat(d.searchFields()).containsExactly("summary", "actionGeoName", "actor1Name", "actor2Name");
        assertThat(d.rankField()).isEqualTo("numArticles");
        assertThat(d.tileProperties()).containsExactly("eventRootCode", "quadClass", "goldstein", "numArticles",
                "geoPrecision");
        assertThat(d.zeroResultsNormal()).isFalse();
        assertThat(d.termsUrl()).isEqualTo("https://www.gdeltproject.org/about.html");
        assertThat(d.attribution()).isEqualTo("The GDELT Project (https://www.gdeltproject.org)");
        assertThat(d.licence()).contains("unrestricted use and redistribution with citation and a link");
        assertThat(d.param(GdeltEventsAdapter.ROOT_CODES_PARAM).defaultValue())
                .isEqualTo(List.of("13", "14", "15", "16", "17", "18", "19", "20"));
        assertThat(d.param(GdeltEventsAdapter.ROOT_CODES_PARAM).allowed()).hasSize(20);
        assertThat(d.param(GdeltEventsAdapter.MIN_MENTIONS_PARAM).defaultValue()).isEqualTo(1);
        assertThat(d.param(GdeltEventsAdapter.REQUIRE_GEO_PARAM).defaultValue()).isEqualTo(true);
    }

    @Test
    @DisplayName("checkParams")
    void checks() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GdeltEventsAdapter.ROOT_CODES_PARAM, List.of()))))
                .containsKey(GdeltEventsAdapter.ROOT_CODES_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GdeltEventsAdapter.MIN_MENTIONS_PARAM, 0))))
                .containsKey(GdeltEventsAdapter.MIN_MENTIONS_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GdeltEventsAdapter.COUNTRIES_PARAM,
                List.of("US", "USA"))))).containsKey(GdeltEventsAdapter.COUNTRIES_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GdeltEventsAdapter.ROOT_CODES_PARAM, List.of("18"),
                GdeltEventsAdapter.MIN_MENTIONS_PARAM, 2, GdeltEventsAdapter.COUNTRIES_PARAM, List.of("UK", "is")))))
                .isEmpty();
    }

    private static byte[] zip(String entry, byte[] content) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(entry));
            zip.write(content);
            zip.closeEntry();
        }
        return out.toByteArray();
    }
}
