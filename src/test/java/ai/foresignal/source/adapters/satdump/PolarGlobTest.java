package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link PolarGlob} (prompt 054): {@code **}, {@code *}, {@code ?}, case, and the refused globs. */
class PolarGlobTest {

    @Test
    @DisplayName("the default **/*.tif matches a .tif at any depth, the pass folder itself included, ignoring case")
    void defaultGlob() {
        PolarGlob glob = PolarGlob.orDefault(null);
        assertThat(glob.toString()).isEqualTo("**/*.tif");
        assertThat(glob.matches("MSU-MR/rgb_MSU-MR_221_Composite_projected.tif")).isTrue();
        assertThat(glob.matches("a/b/c.TIF")).isTrue();
        assertThat(glob.matches("top.tif")).isTrue();
        assertThat(glob.matches("MSU-MR/rgb.png")).isFalse();
        assertThat(glob.matches("MSU-MR/x.tiff")).isFalse();
        assertThat(glob.matches(null)).isFalse();
    }

    @Test
    @DisplayName("* stays in one directory, ? is one character, the rest is literal")
    void wildcards() {
        PolarGlob glob = PolarGlob.compile("MSU-MR/*_projected.ti?");
        assertThat(glob.matches("MSU-MR/rgb_projected.tif")).isTrue();
        assertThat(glob.matches("MSU-MR/sub/rgb_projected.tif")).isFalse();
        assertThat(glob.matches("AVHRR/rgb_projected.tif")).isFalse();
        assertThat(PolarGlob.compile("a.b").matches("axb")).as("a dot is literal").isFalse();
        assertThat(PolarGlob.compile("**").matches("any/thing.at/all")).isTrue();
    }

    @Test
    @DisplayName("empty, absolute, .., backslash and over-long globs are refused")
    void refused() {
        for (String bad : new String[] {"", "  ", "/etc/*.tif", "../*.tif", "a/../b.tif", "a\\b.tif", "x".repeat(201)}) {
            assertThatThrownBy(() -> PolarGlob.compile(bad)).as(bad).isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(PolarGlob.tiff("A.TIFF")).isTrue();
        assertThat(PolarGlob.tiff("a.png")).isFalse();
    }
}
