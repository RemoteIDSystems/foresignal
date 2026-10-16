package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The foresignal-manifest.json reader (prompt 107). */
class ForesignalManifestTest {

    private static final String VALID = "{\"schema\":1,\"satellite\":\"METEOR-M2 3\",\"norad\":57166,"
            + "\"timestamp\":\"2026-10-15T18:02:00Z\",\"products\":[{\"name\":\"rgb\",\"instrument\":\"MSU-MR\","
            + "\"file\":\"MSU-MR/rgb.tif\",\"projection\":\"equirec\",\"bounds\":[-180,-90,180,90],"
            + "\"composite\":\"rgb_MSU-MR\"}]}";

    @Test
    @DisplayName("parses a valid manifest")
    void valid() throws FormatException {
        ForesignalManifest.Manifest m = ForesignalManifest.parse(VALID.getBytes(StandardCharsets.UTF_8));
        assertThat(m.schema()).isEqualTo(1);
        assertThat(m.satellite()).isEqualTo("METEOR-M2 3");
        assertThat(m.norad()).isEqualTo(57166L);
        assertThat(m.timestamp()).isEqualTo(Instant.parse("2026-10-15T18:02:00Z"));
        assertThat(m.products()).hasSize(1);
        assertThat(m.products().get(0).file()).isEqualTo("MSU-MR/rgb.tif");
        assertThat(m.products().get(0).bounds()).containsExactly(-180, -90, 180, 90);
    }

    @Test
    @DisplayName("an unknown schema is rejected")
    void unknownSchema() {
        assertThatThrownBy(() -> ForesignalManifest.parse(VALID.replace("\"schema\":1", "\"schema\":2")
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(FormatException.class);
    }

    @Test
    @DisplayName("an unsafe file path is rejected")
    void unsafeFile() {
        assertThatThrownBy(() -> ForesignalManifest.parse(VALID.replace("MSU-MR/rgb.tif", "../../etc/passwd")
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(FormatException.class);
    }

    @Test
    @DisplayName("a manifest with no products is rejected")
    void noProducts() {
        assertThatThrownBy(() -> ForesignalManifest.parse(("{\"schema\":1,\"satellite\":\"X\","
                + "\"timestamp\":\"2026-10-15T00:00:00Z\",\"products\":[]}").getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FormatException.class);
    }
}
