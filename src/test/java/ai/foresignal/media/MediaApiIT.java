package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.media.ImageFixtures;
import ai.foresignal.testsupport.media.MediaTestFiles;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import ai.foresignal.testsupport.security.TestTokens;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
 * The {@code Media} operations over HTTP (prompt 043): immutable caching headers, {@code public}
 * versus {@code private}, {@code 304} on {@code If-None-Match}, the lazy single-flight thumbnail,
 * {@code 404 UNKNOWN_MEDIA} and {@code 410 MEDIA_EXPIRED}, {@code 400} widths, private sources, the
 * {@code Media} schema, and the READ scope when {@code openReads=false}.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class MediaApiIT {

    static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    static final String SOURCE = SyntheticRasterAdapter.SOURCE;
    static final String IMMUTABLE = "max-age=31536000, immutable";
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    static final SpecSchemas SCHEMAS = new SpecSchemas();

    @LocalServerPort int port;
    @Autowired MediaStore store;
    @Autowired MediaQueries queries;
    @Autowired MediaPaths paths;
    @Autowired MediaRetentionStep retention;
    @Autowired ThumbnailService thumbnails;
    @Autowired AppSettingService settings;
    @Autowired SourceVisibility visibility;
    @Autowired TokenService tokens;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        MediaTestFiles.clear(paths);
    }

    private HttpRequest request(String path, String... headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20)).GET();
        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }
        return request.build();
    }

    private HttpResponse<byte[]> get(String path, String... headers) throws Exception {
        return HTTP.send(request(path, headers), HttpResponse.BodyHandlers.ofByteArray());
    }

    private JsonNode body(HttpResponse<byte[]> response) {
        return json.readTree(new String(response.body(), java.nio.charset.StandardCharsets.UTF_8));
    }

    private String header(HttpResponse<?> response, String name) {
        return response.headers().firstValue(name).orElseThrow();
    }

    private MediaRef stored(byte[] png) {
        return store.store(MediaInput.of(png, "image/png", MediaRole.IMAGE).withSourceId(SOURCE)
                .withRecord(SOURCE, "ext/1 a").withLicence("US-PD"));
    }

    @Test
    @DisplayName("getMedia streams the file with a strong sha256 ETag, Content-Length and public immutable caching")
    void immutableCaching() throws Exception {
        byte[] png = ImageFixtures.png(80, 40);
        MediaRef ref = stored(png);
        HttpResponse<byte[]> response = get("/v1/media/" + ref.id());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(png);
        assertThat(header(response, "Content-Type")).isEqualTo("image/png");
        assertThat(header(response, "Content-Length")).isEqualTo(Integer.toString(png.length));
        assertThat(header(response, "ETag")).isEqualTo("\"" + ref.sha256() + "\"");
        assertThat(header(response, "Cache-Control")).isEqualTo("public, " + IMMUTABLE);

        HttpResponse<byte[]> notModified = get("/v1/media/" + ref.id(), "If-None-Match", "\"" + ref.sha256() + "\"");
        assertThat(notModified.statusCode()).isEqualTo(304);
        assertThat(notModified.body()).isEmpty();
        assertThat(header(notModified, "Cache-Control")).isEqualTo("public, " + IMMUTABLE);
        assertThat(get("/v1/media/" + ref.id(), "If-None-Match", "\"other\"").statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("with openReads off a READ token gets private caching, and no token gets 401")
    void privateCachingAndScope() throws Exception {
        MediaRef ref = stored(ImageFixtures.png(30, 30));
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        assertThat(get("/v1/media/" + ref.id()).statusCode()).isEqualTo(401);
        assertThat(get("/v1/media/" + ref.id() + "/meta").statusCode()).isEqualTo(401);
        assertThat(get("/v1/media/" + ref.id() + "/thumbnail?w=256").statusCode()).isEqualTo(401);
        String reader = TestTokens.bearer(TestTokens.create(tokens, "media-reader", Scope.READ));
        HttpResponse<byte[]> response = get("/v1/media/" + ref.id(), "Authorization", reader);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Cache-Control")).isEqualTo("private, " + IMMUTABLE);
        HttpResponse<byte[]> thumb = get("/v1/media/" + ref.id() + "/thumbnail?w=256", "Authorization", reader);
        assertThat(header(thumb, "Cache-Control")).isEqualTo("private, " + IMMUTABLE);
    }

    @Test
    @DisplayName("media of a private source is hidden from callers without the private grant")
    void privateSource() throws Exception {
        MediaRef ref = stored(ImageFixtures.png(31, 31));
        visibility.setOperatorPrivate(SOURCE, true);
        try {
            HttpResponse<byte[]> hidden = get("/v1/media/" + ref.id());
            assertThat(hidden.statusCode()).isEqualTo(404);
            assertThat(body(hidden).get("code").asString()).isEqualTo("UNKNOWN_MEDIA");
        } finally {
            visibility.setOperatorPrivate(SOURCE, false);
        }
    }

    @Test
    @DisplayName("the first thumbnail request renders once however many arrive together; later ones reuse it")
    void lazySingleFlightThumbnail() throws Exception {
        MediaRef ref = stored(ImageFixtures.png(900, 600));
        long rendersBefore = thumbnails.renders();
        List<CompletableFuture<HttpResponse<byte[]>>> calls = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            calls.add(HTTP.sendAsync(request("/v1/media/" + ref.id() + "/thumbnail?w=256"),
                    HttpResponse.BodyHandlers.ofByteArray()));
        }
        String etag = null;
        for (CompletableFuture<HttpResponse<byte[]>> call : calls) {
            HttpResponse<byte[]> response = call.get();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(header(response, "Content-Type")).isEqualTo("image/jpeg");
            assertThat(header(response, "Cache-Control")).isEqualTo("public, " + IMMUTABLE);
            if (etag == null) {
                etag = header(response, "ETag");
            }
            assertThat(header(response, "ETag")).isEqualTo(etag);
        }
        assertThat(thumbnails.renders() - rendersBefore).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM media WHERE variant_of = ? AND variant_width = 256",
                Long.class, ref.id())).isEqualTo(1);
        assertThat(get("/v1/media/" + ref.id() + "/thumbnail?w=256", "If-None-Match", etag).statusCode())
                .isEqualTo(304);
        HttpResponse<byte[]> large = get("/v1/media/" + ref.id() + "/thumbnail?w=1024");
        assertThat(header(large, "ETag")).as("900 px is linked, not upscaled").isEqualTo("\"" + ref.sha256() + "\"");
        assertThat(header(large, "Content-Type")).isEqualTo("image/png");
    }

    @Test
    @DisplayName("thumbnail widths other than 256 and 1024 are 400 VALIDATION_FAILED")
    void thumbnailWidths() throws Exception {
        MediaRef ref = stored(ImageFixtures.png(20, 20));
        for (String query : new String[] {"?w=512", "?w=abc", ""}) {
            HttpResponse<byte[]> response = get("/v1/media/" + ref.id() + "/thumbnail" + query);
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(body(response).get("code").asString()).isEqualTo("VALIDATION_FAILED");
        }
    }

    @Test
    @DisplayName("unknown ids are 404 UNKNOWN_MEDIA; expired and deleted ones 410 MEDIA_EXPIRED")
    void codes() throws Exception {
        HttpResponse<byte[]> unknown = get("/v1/media/01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        assertThat(unknown.statusCode()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("UNKNOWN_MEDIA");
        assertThat(get("/v1/media/not-a-ulid").statusCode()).isEqualTo(404);
        assertThat(get("/v1/media/01JA8Q3V7S2K9W4T6Y1B5N0M3C/meta").statusCode()).isEqualTo(404);

        MediaRef expired = store.store(MediaInput.of(ImageFixtures.png(22, 22), "image/png", MediaRole.MAP)
                .withExpiresAt(T.minusSeconds(1)));
        HttpResponse<byte[]> gone = get("/v1/media/" + expired.id());
        assertThat(gone.statusCode()).isEqualTo(410);
        assertThat(body(gone).get("code").asString()).isEqualTo("MEDIA_EXPIRED");
        assertThat(get("/v1/media/" + expired.id() + "/thumbnail?w=256").statusCode()).isEqualTo(410);

        retention.apply(T);
        assertThat(queries.find(expired.id())).isEmpty();
        HttpResponse<byte[]> deleted = get("/v1/media/" + expired.id() + "/meta");
        assertThat(deleted.statusCode()).isEqualTo(410);
        assertThat(body(deleted).get("code").asString()).isEqualTo("MEDIA_EXPIRED");
    }

    @Test
    @DisplayName("getMediaMeta returns the row as a Media document with the record ref, license and links")
    void meta() throws Exception {
        MediaRef ref = stored(ImageFixtures.png(64, 48));
        HttpResponse<byte[]> response = get("/v1/media/" + ref.id() + "/meta");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode media = body(response);
        assertThat(SCHEMAS.validateComponent("Media", media)).isEmpty();
        assertThat(media.get("id").asString()).isEqualTo(ref.id());
        assertThat(media.get("sha256").asString()).isEqualTo(ref.sha256());
        assertThat(media.get("width").asInt()).isEqualTo(64);
        assertThat(media.get("role").asString()).isEqualTo("image");
        assertThat(media.get("sourceId").asString()).isEqualTo(SOURCE);
        assertThat(media.get("recordRef").asString()).isEqualTo("record:" + SOURCE + "/ext%2F1%20a");
        assertThat(media.get("licence").asString()).isEqualTo("US-PD");
        assertThat(media.get("expiresAt").isNull()).isTrue();
        assertThat(media.get("links").get("thumbnail256").asString())
                .endsWith("/v1/media/" + ref.id() + "/thumbnail?w=256");

        MediaRow thumb = thumbnails.thumbnail(ref.id(), 256);
        JsonNode thumbMeta = body(get("/v1/media/" + thumb.id() + "/meta"));
        assertThat(SCHEMAS.validateComponent("Media", thumbMeta)).isEmpty();
        assertThat(thumbMeta.get("variantOf").asString()).isEqualTo(ref.id());
        assertThat(thumbMeta.get("variantWidth").asInt()).isEqualTo(256);
        assertThat(thumbMeta.get("role").asString()).isEqualTo("thumbnail");
    }
}
