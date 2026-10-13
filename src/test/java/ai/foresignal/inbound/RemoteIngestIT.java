package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.CollectorProperties;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.PolicyAwareRecordSink;
import ai.foresignal.inbound.format.IngestFormatRegistry;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.satdump.SatDumpAdapter;
import ai.foresignal.source.adapters.satdump.SatDumpDescriptor;
import ai.foresignal.source.adapters.satdump.SatDumpStation;
import ai.foresignal.source.adapters.system.SystemCanaryAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code POST /v1/ingest/{sourceId}/rasters} and {@code /media} over HTTP (prompt 054, Weather-Imagery-Design
 * §4.7 b): a bundle is written to the upload area and processed by the inbox loop like a pass folder; a
 * 26 MB part is {@code 413 MEDIA_TOO_LARGE}; path-traversal names are refused; another source is
 * {@code 404}; an ingest key (the source's {@code ingestKey} credential) or an admin token is required; and
 * {@code media}/{@code rasters} never reach an ingest format.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class RemoteIngestIT {

    static final String SOURCE = "satdump-" + RasterTestSources.SATDUMP_STATION;
    static final String KEY = "eik_remote054testkey0123456789abcdefABCDEF";
    static final String PASS_FOLDER = "2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz";
    static final String PRODUCT = "satdump-itest-rgb-msu-mr-221-composite";
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort int port;
    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;
    @Autowired FetchContextFactory contexts;
    @Autowired IngestQueue queue;
    @Autowired CollectorProperties collector;
    @Autowired RasterPaths rasterPaths;
    @Autowired MeterRegistry meters;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProductRegistry products;
    @Autowired TokenService tokens;
    @Autowired RemoteMediaRegistry receivers;
    @Autowired IngestFormatRegistry formats;

    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-12T18:20:00Z"));
        sources.update(SOURCE, new SourceSettingsUpdate(false, null, Map.of(SatDumpDescriptor.LOCATION, "29.76,-95.37",
                SatDumpDescriptor.API_URL, "http://192.168.1.60:8081"), Budget.of(600, 100_000), null, false, null, false));
        sources.setCredentials(SOURCE, Map.of(SatDumpDescriptor.INGEST_KEY, KEY));
        admin = TestTokens.bearer(TestTokens.create(tokens, "remote-it-" + UUID.randomUUID(), Scope.ADMIN));
    }

    @AfterEach
    void tearDown() {
        products.find(PRODUCT).filter(ProductRegistry.Registered::dynamic).ifPresent(p -> products.removeDynamic(PRODUCT));
    }

    /** One multipart part. */
    record Part(String name, String filename, String contentType, byte[] body) {}

    static Path producer(String relative) {
        try {
            return Path.of(RemoteIngestIT.class.getResource("/fixtures/satdump/1.2.2/pass/" + PASS_FOLDER + "/" + relative)
                    .toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    static Part file(String relative) throws Exception {
        Path p = producer(relative);
        return new Part("files", p.getFileName().toString(), "application/octet-stream", Files.readAllBytes(p));
    }

    static Part meta(String json) {
        return new Part("meta", null, "application/json", json.getBytes(StandardCharsets.UTF_8));
    }

    HttpResponse<String> post(String source, String segment, List<Part> parts, String... headers) throws Exception {
        String boundary = "----foresignal054" + UUID.randomUUID();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (Part part : parts) {
            body.writeBytes(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + part.name() + "\""
                    + (part.filename() == null ? "" : "; filename=\"" + part.filename() + "\"") + "\r\nContent-Type: "
                    + part.contentType() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            body.writeBytes(part.body());
            body.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        body.writeBytes(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v1/ingest/" + source
                + "/" + segment)).timeout(Duration.ofSeconds(60))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()));
        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    static String bundleMeta() {
        return "{\"passFolder\":\"" + PASS_FOLDER + "\",\"files\":[\"dataset.json\",\"product.cbor\","
                + "\"rgb_MSU-MR_221_Composite.png\",\"rgb_MSU-MR_221_Composite_projected.tif\"]}";
    }

    List<Part> bundle() throws Exception {
        List<Part> parts = new ArrayList<>();
        parts.add(meta(bundleMeta()));
        parts.add(file("dataset.json"));
        parts.add(file("MSU-MR/product.cbor"));
        parts.add(file("MSU-MR/rgb_MSU-MR_221_Composite.png"));
        parts.add(file("MSU-MR/rgb_MSU-MR_221_Composite_projected.tif"));
        return parts;
    }

    PolicyAwareRecordSink sink() {
        PolicyAwareRecordSink sink = new PolicyAwareRecordSink(registry.descriptor(SOURCE), IngestPolicy.PolicyMode.ALL,
                queue::submit, new PolicyAwareRecordSink.FlushListener() {
                    @Override
                    public void flushed(FetchLogEntry log, int records, IngestOutcome outcome) {}

                    @Override
                    public void failed(FetchLogEntry log, int records, Throwable error) {}
                }, clock, collector.stream(), "test", meters);
        sink.start();
        return sink;
    }

    SatDumpStation station(PolicyAwareRecordSink sink) throws Exception {
        return ((SatDumpAdapter) registry.get(SOURCE).adapterIfPresent().orElseThrow())
                .newStation(contexts.forStream(registry.descriptor(SOURCE), null, null), sink);
    }

    @Test
    @DisplayName("a bundle with the source's ingest key: 202, written under incoming/, processed by the next scan like a pass folder")
    void bundleProcessed() throws Exception {
        HttpResponse<String> response = post(SOURCE, "rasters", bundle(), "X-Foresignal-Ingest-Key", KEY);
        assertThat(response.statusCode()).as(response.body()).isEqualTo(202);
        JsonNode body = JSON.readTree(response.body());
        String uploadId = body.get("uploadId").asString();
        assertThat(body.get("passFolder").asString()).isEqualTo(PASS_FOLDER);
        Path upload = rasterPaths.root().resolve("satdump/itest/incoming").resolve(uploadId).resolve(PASS_FOLDER);
        assertThat(upload.resolve("dataset.json")).exists();
        assertThat(upload.resolve("MSU-MR/rgb_MSU-MR_221_Composite_projected.tif")).exists();

        PolicyAwareRecordSink sink = sink();
        SatDumpStation s = station(sink);
        s.scanInboxOnce();
        assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();
        String passId = "itest:meteor-m2-3:" + Instant.parse("2026-10-12T18:02:10Z").getEpochSecond() / 60;
        assertThat(jdbc.queryForObject("select payload->>'status' from record_head where source_id = ? and external_id = ?",
                String.class, SOURCE, passId)).isEqualTo("decoded");
        assertThat(jdbc.queryForObject("select payload->>'geometrySource' from record_head where source_id = ? and"
                + " external_id = ?", String.class, SOURCE, passId)).isEqualTo("footprint");
        assertThat(jdbc.queryForObject("select count(*) from raster_frame where product_id = ?", Integer.class, PRODUCT))
                .isEqualTo(1);
        // Pushed with an admin token too.
        assertThat(post(SOURCE, "rasters", bundle(), "Authorization", admin).statusCode()).isEqualTo(202);
    }

    @Test
    @DisplayName("a 26 MB part is 413 MEDIA_TOO_LARGE")
    void tooLarge() throws Exception {
        List<Part> parts = new ArrayList<>();
        parts.add(meta("{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"big.tif\"]}"));
        parts.add(file("dataset.json"));
        parts.add(new Part("files", "big.tif", "image/tiff", new byte[26 * 1024 * 1024]));
        HttpResponse<String> response = post(SOURCE, "rasters", parts, "Authorization", admin);
        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(response.body()).contains("MEDIA_TOO_LARGE");
    }

    @Test
    @DisplayName("path-traversal names are refused, in meta and in part file names")
    void traversal() throws Exception {
        List<Part> badMeta = List.of(meta("{\"passFolder\":\"../../etc\",\"files\":[\"dataset.json\"]}"),
                file("dataset.json"));
        HttpResponse<String> response = post(SOURCE, "rasters", badMeta, "Authorization", admin);
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("VALIDATION_FAILED");

        List<Part> badPart = List.of(meta("{\"passFolder\":\"p\",\"files\":[\"dataset.json\"]}"),
                new Part("files", "../dataset.json", "application/json", Files.readAllBytes(producer("dataset.json"))));
        assertThat(post(SOURCE, "rasters", badPart, "Authorization", admin).statusCode()).isEqualTo(400);
        try (var incoming = Files.list(rasterPaths.root().resolve("satdump"))) {
            assertThat(incoming.map(p -> p.getFileName().toString()).toList()).doesNotContain("etc");
        }
    }

    @Test
    @DisplayName("a source that does not accept remote media is 404 NOT_FOUND, never an ingest format")
    void wrongSource() throws Exception {
        HttpResponse<String> rasters = post(SystemCanaryAdapter.ID, "rasters", bundle(), "Authorization", admin);
        assertThat(rasters.statusCode()).isEqualTo(404);
        assertThat(rasters.body()).contains("NOT_FOUND").doesNotContain("FORMAT_UNKNOWN");
        HttpResponse<String> media = post(SystemCanaryAdapter.ID, "media", List.of(meta("{}")), "Authorization", admin);
        assertThat(media.statusCode()).isEqualTo(404);
        assertThat(media.body()).doesNotContain("FORMAT_UNKNOWN");
    }

    @Test
    @DisplayName("an ingest key is required: none is 401, a wrong key is 401, the key of the source is accepted")
    void keyRequired() throws Exception {
        assertThat(post(SOURCE, "rasters", bundle()).statusCode()).isEqualTo(401);
        assertThat(post(SOURCE, "rasters", bundle(), "X-Foresignal-Ingest-Key", KEY + "x").statusCode()).isEqualTo(401);
        assertThat(post(SOURCE, "rasters", bundle(), "Authorization", "Bearer " + KEY).statusCode()).isEqualTo(202);
        // The key works only on this source's media and rasters paths, not as a general ingest key.
        HttpResponse<String> native_ = HTTP.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port
                + "/v1/ingest/" + SOURCE)).header("X-Foresignal-Ingest-Key", KEY).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"records\":[]}")).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(native_.statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("media and rasters are reserved: no format can take them, and the listing marks them reserved")
    void reserved() throws Exception {
        assertThat(formats.isReserved("media")).isTrue();
        assertThat(formats.isReserved("rasters")).isTrue();
        assertThat(formats.find("media")).isEmpty();
        HttpResponse<String> listing = HTTP.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port
                + "/v1/ingest/formats")).header("Authorization", admin).GET().build(), HttpResponse.BodyHandlers.ofString());
        JsonNode list = JSON.readTree(listing.body());
        boolean found = false;
        for (JsonNode f : list) {
            if ("rasters".equals(f.path("id").asString())) {
                found = true;
                assertThat(f.path("reserved").asBoolean()).isTrue();
                assertThat(f.path("description").asString()).contains("remote SatDump push");
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("/media: 404 while the station is not running; with its receiver registered, 202 with the media and pass ids")
    void media() throws Exception {
        byte[] png = Files.readAllBytes(producer("MSU-MR/rgb_MSU-MR_221_Composite.png"));
        List<Part> parts = List.of(meta("{\"composite\":\"msu_mr_rgb_221\",\"timestamp\":\"2026-10-12T18:02:10Z\","
                + "\"satellite\":\"METEOR-M2 3\"}"), new Part("file", "composite.png", "image/png", png));
        assertThat(post(SOURCE, "media", parts, "X-Foresignal-Ingest-Key", KEY).statusCode()).isEqualTo(404);

        PolicyAwareRecordSink sink = sink();
        SatDumpStation s = station(sink);
        receivers.register(SOURCE, s);
        try {
            HttpResponse<String> response = post(SOURCE, "media", parts, "X-Foresignal-Ingest-Key", KEY);
            assertThat(response.statusCode()).as(response.body()).isEqualTo(202);
            JsonNode body = JSON.readTree(response.body());
            String passId = "itest:meteor-m2-3:" + Instant.parse("2026-10-12T18:02:10Z").getEpochSecond() / 60;
            assertThat(body.get("passId").asString()).isEqualTo(passId);
            assertThat(body.get("sha256").asString()).hasSize(64);
            assertThat(jdbc.queryForObject("select record_external_id from media where id = ?", String.class,
                    body.get("mediaId").asString())).isEqualTo(passId);

            HttpResponse<String> notAnImage = post(SOURCE, "media", List.of(parts.get(0),
                    new Part("file", "x.png", "image/png", "not an image".getBytes(StandardCharsets.UTF_8))),
                    "X-Foresignal-Ingest-Key", KEY);
            assertThat(notAnImage.statusCode()).isEqualTo(400);
            HttpResponse<String> badMeta = post(SOURCE, "media", List.of(meta("{\"composite\":\"c\"}"), parts.get(1)),
                    "X-Foresignal-Ingest-Key", KEY);
            assertThat(badMeta.statusCode()).isEqualTo(400);
        } finally {
            receivers.unregister(SOURCE, s);
            assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();
        }
        assertThat(jdbc.queryForObject("select payload->>'status' from record_head where source_id = ? and external_id = ?",
                String.class, SOURCE, "itest:meteor-m2-3:" + Instant.parse("2026-10-12T18:02:10Z").getEpochSecond() / 60))
                .isEqualTo("decoded");
    }

    @Test
    @DisplayName("a body that is not multipart is 415")
    void notMultipart() throws Exception {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port
                + "/v1/ingest/" + SOURCE + "/rasters")).header("Authorization", admin).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}")).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(415);
        assertThatThrownBy(() -> formats.find("rasters").orElseThrow()).isInstanceOf(java.util.NoSuchElementException.class);
    }
}
