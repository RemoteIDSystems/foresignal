package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.media.ImageGuard;
import ai.foresignal.raster.RasterPaths;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link RemoteBundles} (prompt 054): {@code meta} validation (safe names, listed files, extensions,
 * {@code dataset.json}), the 25 MB and 200 MB caps, the image guard, the layout (product directory from
 * {@code product} or the single product of {@code dataset.json}) and the atomic move into
 * {@code rasters/satdump/{station}/incoming/{uploadId}/}.
 */
class RemoteBundlesTest {

    static final byte[] DATASET = "{\"satellite\":\"METEOR-M2 3\",\"timestamp\":1791828130.0,\"products\":[\"MSU-MR\"]}"
            .getBytes(StandardCharsets.UTF_8);

    @TempDir Path data;

    RemoteBundles bundles() {
        return new RemoteBundles(new RasterPaths(data), new ImageGuard(25L * 1024 * 1024, null), JsonMapper.builder().build(),
                Clock.fixed(Instant.parse("2026-10-12T18:30:00Z"), ZoneOffset.UTC));
    }

    static RemoteBundles.Part part(String name, byte[] bytes) {
        return new RemoteBundles.Part(name, bytes.length, () -> new ByteArrayInputStream(bytes));
    }

    static byte[] meta(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("a valid bundle lands atomically in incoming/{uploadId}/{passFolder}, files in the dataset's single product")
    void writes() throws Exception {
        RemoteBundles b = bundles();
        RemoteBundles.Meta meta = b.meta(meta("{\"passFolder\":\"2026-10-12_18-02 meteor\",\"files\":[\"dataset.json\","
                + "\"product.cbor\"]}"));
        RemoteBundles.Accepted accepted = b.write("home", meta, List.of(part("dataset.json", DATASET),
                part("product.cbor", new byte[] {(byte) 0xA0})));
        assertThat(accepted.passFolder()).isEqualTo("2026-10-12_18-02 meteor");
        Path folder = data.resolve("rasters/satdump/home/incoming").resolve(accepted.uploadId())
                .resolve("2026-10-12_18-02 meteor");
        assertThat(folder.resolve("dataset.json")).exists();
        assertThat(folder.resolve("MSU-MR/product.cbor")).exists();
        try (var staging = Files.list(data.resolve("rasters/tmp"))) {
            assertThat(staging.toList()).as("nothing left in staging").isEmpty();
        }
    }

    @Test
    @DisplayName("meta.product overrides the product directory")
    void productOverride() throws Exception {
        RemoteBundles b = bundles();
        RemoteBundles.Meta meta = b.meta(meta("{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"a.cbor\"],"
                + "\"product\":\"AVHRR\"}"));
        RemoteBundles.Accepted accepted = b.write("home", meta, List.of(part("dataset.json", DATASET),
                part("a.cbor", new byte[] {1})));
        assertThat(data.resolve("rasters/satdump/home/incoming").resolve(accepted.uploadId()).resolve("p/AVHRR/a.cbor"))
                .exists();
    }

    @Test
    @DisplayName("unsafe names, unlisted or missing parts, bad extensions and a missing dataset.json are 400")
    void validation() {
        RemoteBundles b = bundles();
        for (String bad : new String[] {
            "{\"passFolder\":\"../x\",\"files\":[\"dataset.json\"]}",
            "{\"passFolder\":\"a/b\",\"files\":[\"dataset.json\"]}",
            "{\"passFolder\":\".hidden\",\"files\":[\"dataset.json\"]}",
            "{\"passFolder\":\"p\",\"files\":[\"../dataset.json\"]}",
            "{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"x.sh\"]}",
            "{\"passFolder\":\"p\",\"files\":[\"product.cbor\"]}",
            "{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"dataset.json\"]}",
            "{\"passFolder\":\"p\",\"files\":[]}",
            "{\"passFolder\":\"p\",\"files\":[\"dataset.json\"],\"product\":\"..\"}",
            "[1]", "not json"}) {
            assertThatThrownBy(() -> b.meta(meta(bad))).as(bad).isInstanceOf(ValidationException.class);
        }
        RemoteBundles.Meta meta = b.meta(meta("{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"a.cbor\"]}"));
        assertThatThrownBy(() -> b.write("home", meta, List.of(part("dataset.json", DATASET))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("not valid");
        assertThatThrownBy(() -> b.write("home", meta, List.of(part("dataset.json", DATASET), part("a.cbor", new byte[1]),
                part("b.cbor", new byte[1])))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> b.write("home", meta, List.of(part("dataset.json", DATASET),
                part("../a.cbor", new byte[1])))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a 26 MB part or a bundle over 200 MB is 413 MEDIA_TOO_LARGE, judged from the declared sizes")
    void sizes() {
        RemoteBundles b = bundles();
        RemoteBundles.Meta meta = b.meta(meta("{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"big.tif\"]}"));
        RemoteBundles.Part big = new RemoteBundles.Part("big.tif", 26L * 1024 * 1024,
                () -> { throw new AssertionError("a part over the cap is never read"); });
        assertThatThrownBy(() -> b.write("home", meta, List.of(part("dataset.json", DATASET), big)))
                .isInstanceOfSatisfying(ForesignalException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.MEDIA_TOO_LARGE);
                });
        StringBuilder files = new StringBuilder("\"dataset.json\"");
        java.util.List<RemoteBundles.Part> parts = new java.util.ArrayList<>(List.of(part("dataset.json", DATASET)));
        for (int i = 0; i < 9; i++) {
            files.append(",\"f").append(i).append(".cbor\"");
            parts.add(new RemoteBundles.Part("f" + i + ".cbor", 24L * 1024 * 1024, () -> new ByteArrayInputStream(new byte[0])));
        }
        RemoteBundles.Meta many = b.meta(meta("{\"passFolder\":\"p\",\"files\":[" + files + "]}"));
        assertThatThrownBy(() -> b.write("home", many, parts)).isInstanceOfSatisfying(ForesignalException.class,
                e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.MEDIA_TOO_LARGE));
    }

    @Test
    @DisplayName("an image the guard refuses is 400; a BigTIFF is left to the pipeline")
    void guard() throws Exception {
        RemoteBundles b = bundles();
        RemoteBundles.Meta meta = b.meta(meta("{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"x.png\"]}"));
        assertThatThrownBy(() -> b.write("home", meta, List.of(part("dataset.json", DATASET),
                part("x.png", "not a png".getBytes(StandardCharsets.UTF_8))))).isInstanceOf(ValidationException.class);
        RemoteBundles.Meta tif = b.meta(meta("{\"passFolder\":\"p\",\"files\":[\"dataset.json\",\"x.tif\"]}"));
        byte[] bigTiff = ai.foresignal.testsupport.raster.GeoTiffFixtures.bigTiffHeader();
        assertThat(b.write("home", tif, List.of(part("dataset.json", DATASET), part("x.tif", bigTiff))).uploadId())
                .isNotBlank();
    }

    @Test
    @DisplayName("safe names")
    void safeNames() {
        assertThat(RemoteBundles.safe("2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz")).isTrue();
        assertThat(RemoteBundles.safe("a..b")).isFalse();
        assertThat(RemoteBundles.safe(" lead")).isFalse();
        assertThat(RemoteBundles.safe("tab\tname")).isFalse();
        assertThat(RemoteBundles.safe(null)).isFalse();
        assertThat(RemoteBundles.safe("x".repeat(129))).isFalse();
    }
}
