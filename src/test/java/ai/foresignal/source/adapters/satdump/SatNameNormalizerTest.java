package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link SatNames} against the bundled table {@code satdump/sat-names.json} (prompt 049,
 * Weather-Imagery-Design §4.4): names, aliases and NORAD numbers normalize to one satKey, and the
 * operator's {@code satNames} overrides apply on top.
 */
class SatNameNormalizerTest {

    static final SatNames NAMES = SatNames.bundled();

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "METEOR-M2 3, meteor-m2-3", "Meteor-M2-3, meteor-m2-3", "57166, meteor-m2-3", "meteor_m2-3, meteor-m2-3",
            "METEOR-M2 4, meteor-m2-4", "59051, meteor-m2-4",
            "METOP-B, metop-b", "38771, metop-b", "MetOp-C, metop-c", "43689, metop-c",
            "GOES 19, goes-19", "GOES-19, goes-19", "60133, goes-19", "GOES-18, goes-18", "51850, goes-18",
            "GOES-16, goes-16", "41866, goes-16",
            "ELEKTRO-L 3, elektro-l3", "44903, elektro-l3", "Elektro-L N4, elektro-l4", "55506, elektro-l4",
            "NOAA 20, noaa-20", "'  FengYun 3D ', fengyun-3d"})
    void normalizes(String name, String key) {
        assertThat(NAMES.key(name)).isEqualTo(key);
    }

    @Test
    @DisplayName("blank names are unknown; an unknown number is its own key")
    void edges() {
        assertThat(NAMES.key(null)).isEqualTo("unknown");
        assertThat(NAMES.key("   ")).isEqualTo("unknown");
        assertThat(NAMES.key("---")).isEqualTo("unknown");
        assertThat(NAMES.key("12345")).isEqualTo("12345");
    }

    @Test
    @DisplayName("display names and NORAD numbers come from the table")
    void entries() {
        assertThat(NAMES.displayName("meteor-m2-3", "METEOR-M2 3")).isEqualTo("Meteor-M2-3");
        assertThat(NAMES.displayName("noaa-20", "NOAA 20")).isEqualTo("NOAA 20");
        assertThat(NAMES.norad("meteor-m2-3")).isEqualTo(57166);
        assertThat(NAMES.norad("noaa-20")).isNull();
        assertThat(NAMES.entries()).extracting(SatNames.Entry::key).contains("meteor-m2-3", "meteor-m2-4", "metop-b",
                "metop-c", "goes-19", "goes-18", "goes-16", "elektro-l3", "elektro-l4");
    }

    @Test
    @DisplayName("satNames overrides: a name or a NORAD number maps to the operator's key")
    void overrides() {
        SatNames custom = NAMES.withOverrides(Map.of("Meteor M2-3 (LRPT)", "meteor-m2-3", "40069", "meteor-m2",
                "GOES 19", "goes-east"));
        assertThat(custom.key("Meteor M2-3 (LRPT)")).isEqualTo("meteor-m2-3");
        assertThat(custom.key("40069")).isEqualTo("meteor-m2");
        assertThat(custom.key("GOES 19")).isEqualTo("goes-east");
        assertThat(NAMES.key("GOES 19")).isEqualTo("goes-19");
        assertThat(NAMES.withOverrides(Map.of())).isSameAs(NAMES);
    }

    @Test
    @DisplayName("the satNames parameter must be a JSON object of strings")
    void parameter() {
        assertThat(SatDumpDescriptor.satNames("{\"A\":\"b\"}")).containsEntry("A", "b");
        assertThat(SatDumpDescriptor.satNames(" ")).isEmpty();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> SatDumpDescriptor.satNames("[1]"))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> SatDumpDescriptor.satNames("{\"A\":1}"))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> SatDumpDescriptor.satNames("{oops"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
