package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.CollectorProperties;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.PolicyAwareRecordSink;
import ai.foresignal.config.ForesignalProperties;
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
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.GeoTiffFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import io.micrometer.core.instrument.MeterRegistry;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Polar overlays end to end (prompt 054): a ready pass folder of the SatDump 1.2.2 producer fixture,
 * through the real stream context (inbox, raster pipeline, media store, adapter state) and the real
 * writer, becomes a frame of {@code satdump-itest-rgb-msu-mr-221-composite} with a footprint whose tiles
 * render; the {@code satellite-pass} geometry goes from the station point to the footprint; the GeoTIFF is
 * copied as provenance; {@code polarGlob} is honored and a symlinked GeoTIFF that escapes the inbox is
 * never read.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class PolarOverlayIT {

    static final String SOURCE = "satdump-" + RasterTestSources.SATDUMP_STATION;
    static final String PRODUCT = "satdump-itest-rgb-msu-mr-221-composite";
    static final Instant DATASET_TIME = Instant.parse("2026-10-12T18:02:10Z");
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @LocalServerPort int port;
    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;
    @Autowired FetchContextFactory contexts;
    @Autowired IngestQueue queue;
    @Autowired CollectorProperties collector;
    @Autowired ForesignalProperties properties;
    @Autowired RasterPaths rasterPaths;
    @Autowired MeterRegistry meters;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProductRegistry products;
    @Autowired TokenService tokens;

    Path station;
    String inboxPath;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(Instant.parse("2026-10-12T18:20:00Z"));
        // the shared remote-upload area may hold bundles for this test's pass (see SatDumpFixtures.clearUploads)
        SatDumpFixtures.clearUploads(rasterPaths.root(), "itest");
        inboxPath = "satdump/itest-" + UUID.randomUUID();
        station = Files.createDirectories(properties.inboxDir().resolve(inboxPath));
    }

    @AfterEach
    void tearDown() {
        products.find(PRODUCT).filter(ProductRegistry.Registered::dynamic).ifPresent(p -> products.removeDynamic(PRODUCT));
    }

    void configure(Map<String, Object> extra) {
        Map<String, Object> params = new HashMap<>(Map.of(SatDumpDescriptor.INBOX_PATH, inboxPath,
                SatDumpDescriptor.LOCATION, "29.76,-95.37"));
        params.putAll(extra);
        sources.update(SOURCE, new SourceSettingsUpdate(false, null, params, Budget.of(600, 100_000), null, false, null,
                false));
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
        StreamContext ctx = contexts.forStream(registry.descriptor(SOURCE), null, null);
        return ((SatDumpAdapter) registry.get(SOURCE).adapterIfPresent().orElseThrow()).newStation(ctx, sink);
    }

    void scan(SatDumpStation s) {
        for (int i = 0; i < 3; i++) {
            s.scanInboxOnce();
            clock.setInstant(clock.instant().plusSeconds(30));
        }
    }

    String passId() {
        return "itest:meteor-m2-3:" + DATASET_TIME.getEpochSecond() / 60;
    }

    HttpResponse<byte[]> get(String url, String bearer) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create(url)).header("Authorization", bearer)
                .timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    @Test
    @DisplayName("the producer fixture: frame and footprint, tiles render, pass geometry upgraded to the footprint, provenance copied, symlink ignored")
    void overlay() throws Exception {
        configure(Map.of());
        Path pass = station.resolve("2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz");
        SatDumpFixtures.copyProducer(SatDumpFixtures.PRODUCER_PASS, pass);
        // A symlinked GeoTIFF pointing outside the inbox: never followed, never an overlay.
        Path outside = Files.createTempFile("polar-outside-", ".tif");
        Files.write(outside, GeoTiffFixtures.wgs84(-100, 35, 0.5).size(10, 10).build());
        Files.createSymbolicLink(pass.resolve("MSU-MR/evil_projected.tif"), outside);

        PolicyAwareRecordSink sink = sink();
        SatDumpStation s = station(sink);
        scan(s);
        assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();

        assertThat(products.find(PRODUCT)).hasValueSatisfying(p -> {
            assertThat(p.dynamic()).isTrue();
            assertThat(p.sourceId()).isEqualTo(SOURCE);
            assertThat(p.spec().temporal().type()).isEqualTo("pass");
            assertThat(p.spec().spatial().coverage()).isEqualTo("swath");
        });
        assertThat(products.find("satdump-itest-evil")).isEmpty();
        // raster_frame has no frame_key column: the key is {timeKey of valid_time}~{sha8} (FrameKeys, RasterFrameRow).
        Map<String, Object> frame = jdbc.queryForMap("select status, to_char(valid_time at time zone 'UTC',"
                + " 'YYYYMMDD\"T\"HH24MISS\"Z\"') || '~' || left(sha256, 8) as frame_key, ST_NumGeometries(footprint) as parts,"
                + " ST_XMin(footprint) as xmin, ST_YMax(footprint) as ymax from raster_frame where product_id = ?", PRODUCT);
        assertThat(frame.get("status")).isEqualTo("complete");
        assertThat(((Number) frame.get("parts")).intValue()).isEqualTo(1);
        assertThat(((Number) frame.get("xmin")).doubleValue()).isBetween(-110.0, -100.0);
        assertThat(((Number) frame.get("ymax")).doubleValue()).isEqualTo(42.0);
        assertThat(jdbc.queryForObject("select count(*) from record_version where source_id = ? and external_id = ?",
                Integer.class, SOURCE, PRODUCT)).isEqualTo(1);

        String geometryType = jdbc.queryForObject("select ST_GeometryType(persisted_geom) from record_head where"
                + " source_id = ? and external_id = ?", String.class, SOURCE, passId());
        assertThat(geometryType).isIn("ST_Polygon", "ST_MultiPolygon");
        assertThat(jdbc.queryForObject("select payload->>'geometrySource' from record_head where source_id = ? and"
                + " external_id = ?", String.class, SOURCE, passId())).isEqualTo("footprint");
        String tiles = jdbc.queryForObject("select p->>'tilesTemplate' from record_head, jsonb_array_elements(payload->"
                + "'products') p where source_id = ? and external_id = ? and p->>'productId' = ?", String.class, SOURCE,
                passId(), PRODUCT);
        assertThat(tiles).contains("/v1/products/" + PRODUCT + "/tiles/" + frame.get("frame_key") + "/{z}/{x}/{y}.png");

        // z4/3/6 covers 112.5..90 W, 21.9..40.98 N: the swath band crosses it.
        String path = tiles.substring(tiles.indexOf("/v1/products/")).replace("{z}", "4").replace("{x}", "3")
                .replace("{y}", "6");
        HttpResponse<byte[]> tile = get("http://localhost:" + port + path,
                TestTokens.bearer(TestTokens.create(tokens, "polar-it", Scope.ADMIN)));
        assertThat(tile.statusCode()).isEqualTo(200);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(tile.body()));
        int opaque = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    opaque++;
                }
            }
        }
        assertThat(opaque).as("the swath draws, the fill outside it is transparent").isBetween(1000, 256 * 256 - 1000);

        Path provenance = rasterPaths.root().resolve("satdump").resolve("itest").resolve(passId().replace(':', '_'))
                .resolve(SatDumpFixtures.PRODUCER_TIFF);
        assertThat(provenance).exists();
        assertThat(rasterPaths.root().resolve("satdump/itest").resolve(passId().replace(':', '_'))
                .resolve("MSU-MR/evil_projected.tif")).doesNotExist();
        Files.deleteIfExists(outside);
    }

    @Test
    @DisplayName("an RF session first puts the pass at the station point; the decoded folder upgrades it to the footprint")
    void geometryUpgrade() throws Exception {
        configure(Map.of());
        PolicyAwareRecordSink sink = sink();
        SatDumpStation s = station(sink);
        Instant aos = Instant.parse("2026-10-12T18:01:50Z");
        clock.setInstant(aos);
        ApiSnapshot tracking = SatDumpApiParser.parse(SatDumpFixtures.text("api/autotrack.json"));
        s.observe(tracking, aos).forEach(sink::accept);
        clock.setInstant(aos.plusSeconds(65));
        s.observe(SatDumpApiParser.parse(SatDumpFixtures.text("api/autotrack-idle.json")), clock.instant())
                .forEach(sink::accept);
        SatDumpFixtures.copyProducer(SatDumpFixtures.PRODUCER_PASS, station.resolve("pass"));
        clock.setInstant(aos.plusSeconds(120));
        scan(s);
        assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();
        String id = "itest:meteor-m2-3:" + aos.getEpochSecond() / 60;
        java.util.List<String> types = jdbc.queryForList("select ST_GeometryType(geom) from record_version where"
                + " source_id = ? and external_id = ? order by seq", String.class, SOURCE, id);
        assertThat(types).hasSize(3);
        assertThat(types.subList(0, 2)).as("receiving and recorded at the station point")
                .containsExactly("ST_Point", "ST_Point");
        assertThat(types.get(2)).as("decoded: the footprint").isIn("ST_Polygon", "ST_MultiPolygon");
    }

    @Test
    @DisplayName("polarGlob is honored: with no match there is no frame, and the pass gets the approximate swath")
    void globHonored() throws Exception {
        configure(Map.of(SatDumpDescriptor.POLAR_GLOB, "nothing/*.tif"));
        SatDumpFixtures.copyProducer(SatDumpFixtures.PRODUCER_PASS, station.resolve("pass"));
        PolicyAwareRecordSink sink = sink();
        scan(station(sink));
        assertThat(sink.flushAndClose(Duration.ofSeconds(30))).isTrue();
        assertThat(products.find(PRODUCT)).isEmpty();
        assertThat(jdbc.queryForObject("select payload->>'geometrySource' from record_head where source_id = ? and"
                + " external_id = ?", String.class, SOURCE, passId())).isEqualTo("swath");
    }
}
