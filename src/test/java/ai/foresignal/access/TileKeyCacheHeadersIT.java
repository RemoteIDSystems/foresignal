package ai.foresignal.access;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cache headers through a tile key (prompt 050; Weather-Imagery-Design §6.1): the keyed URL takes the
 * canonical URL's rule. A content-addressed tile is {@code immutable}, {@code private} while
 * {@code openReads} is off and {@code public} while it is on; an alias gets the short max-age with
 * {@code Foresignal-Frame-Time}; the ETag equals the canonical response's and answers 304.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class TileKeyCacheHeadersIT extends RasterApiSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired TileKeyService tileKeys;

    @AfterEach
    void restoreSecurity() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private String key() {
        return tileKeys.create(JSON.readTree("{\"name\":\"radar\",\"prefixes\":[\"/v1/products/\",\"/v1/styles\"],"
                + "\"ttl\":\"7d\"}")).key();
    }

    private static String tile(String frameKey) {
        return "/products/" + OBS + "/tiles/" + frameKey + "/4/3/6.png?style=nws-reflectivity@1";
    }

    @Test
    @DisplayName("a content-addressed tile via a key is immutable and private while openReads is off")
    void privateWhenReadsAreClosed() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        String key = key();
        assertThat(get("/v1" + tile(frame.frameKey())).statusCode()).as("openReads is off").isEqualTo(401);
        HttpResponse<byte[]> keyed = getBytes("/v1/k/" + key + tile(frame.frameKey()));
        assertThat(keyed.statusCode()).isEqualTo(200);
        assertThat(header(keyed, "Cache-Control")).isEqualTo("private, max-age=31536000, immutable");
        assertThat(header(keyed, "Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(header(keyed, "Foresignal-Frame-Time")).isNull();
    }

    @Test
    @DisplayName("with openReads on, the keyed tile is public and immutable with the canonical ETag; 304 works")
    void publicWhenReadsAreOpen() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        settings.put(SettingKeys.SECURITY, new SecuritySettings(true));
        String key = key();
        HttpResponse<byte[]> canonical = getBytes("/v1" + tile(frame.frameKey()));
        HttpResponse<byte[]> keyed = getBytes("/v1/k/" + key + tile(frame.frameKey()));
        assertThat(keyed.statusCode()).isEqualTo(200);
        assertThat(header(keyed, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
        assertThat(header(keyed, "ETag")).isEqualTo(header(canonical, "ETag"));
        assertThat(keyed.body()).isEqualTo(canonical.body());
        assertThat(getBytes("/v1/k/" + key + tile(frame.frameKey()), "If-None-Match", header(keyed, "ETag"))
                .statusCode()).isEqualTo(304);
    }

    @Test
    @DisplayName("an alias through a key gets the short max-age and Foresignal-Frame-Time, like the canonical URL")
    void aliasGetsTheShortMaxAge() throws Exception {
        commitObservation(T, 0);
        settings.put(SettingKeys.SECURITY, new SecuritySettings(true));
        String key = key();
        HttpResponse<byte[]> keyed = getBytes("/v1/k/" + key + tile("latest"));
        assertThat(keyed.statusCode()).isEqualTo(200);
        assertThat(header(keyed, "Cache-Control")).isEqualTo("public, max-age=60");
        assertThat(header(keyed, "Foresignal-Frame-Time")).isEqualTo(T.toString());

        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        HttpResponse<byte[]> closed = getBytes("/v1/k/" + key + tile("latest"));
        assertThat(header(closed, "Cache-Control")).isEqualTo("private, max-age=60");
    }
}
