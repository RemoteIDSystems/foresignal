package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.mapping.MappingCompiler;
import ai.foresignal.mapping.MappingContext;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.ObservedAtMode;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

/** {@link PresetCatalog} (prompt 059): loading, applying and rendering the setup. */
class PresetCatalogTest {

    @Test
    @DisplayName("the eight presets load, and every preset mapping compiles against its own kinds and time base")
    void loadAndCompile() {
        assertThat(PresetCatalog.ids()).containsExactly("airrohr", "aiscatcher", "alertmanager", "chirpstack-v4", "ecowitt",
                "gmc", "tts-v3", "wu-pws");
        MappingCompiler compiler = new MappingCompiler();
        for (MappingPreset preset : PresetCatalog.all()) {
            if (preset.mapping() == null) {
                assertThat(preset.format()).isEqualTo("alertmanager");
                continue;
            }
            LinkedHashSetHolder kinds = new LinkedHashSetHolder();
            preset.document().path("kinds").forEach(k -> kinds.set.add(k.asString()));
            ObservedAtMode mode = ObservedAtMode.valueOf(preset.document().path("observedAtMode").asString());
            assertThat(compiler.compile(preset.mapping(), new MappingContext(kinds.set, mode, true)).warnings())
                    .as(preset.id()).isEmpty();
        }
    }

    /** A mutable holder for the lambda. */
    static final class LinkedHashSetHolder {
        final LinkedHashSet<String> set = new LinkedHashSet<>();
    }

    @Test
    @DisplayName("descriptor defaults are parsed: LATEST weather, SAMPLED vessels with thresholds, motion and retention")
    void descriptorDefaults() {
        assertThat(PresetCatalog.find("ecowitt").orElseThrow().ingestPolicy()).isEqualTo(
                new IngestPolicy.Latest(Duration.ofMinutes(5), null));
        MappingPreset ais = PresetCatalog.find("aiscatcher").orElseThrow();
        assertThat(ais.ingestPolicy()).isInstanceOf(IngestPolicy.Sampled.class);
        assertThat(ais.ingestPolicy().keyframe()).isEqualTo(Duration.ofSeconds(1200));
        assertThat(ais.retention()).isEqualTo(Duration.ofDays(3));
        assertThat(ais.thresholds()).hasSize(2);
        assertThat(ais.motion().speedField()).isEqualTo("sog");
        assertThat(ais.mergeCurrent()).isTrue();
        assertThat(PresetCatalog.find("chirpstack-v4").orElseThrow().ingestPolicy()).isNull();
        assertThat(PresetCatalog.find("nope")).isEmpty();
    }

    @Test
    @DisplayName("apply fills absent members, keeps given ones, places the location and the alertmanager options")
    void apply() {
        ObjectNode body = PresetContract.JSON.createObjectNode().put("id", "custom-x").put("preset", "chirpstack-v4")
                .put("private", false);
        body.putObject("presetParams").put("lon", -97.7431).put("lat", 30.2672).put("exactLocation", true);
        ObjectNode merged = PresetCatalog.apply(body, PresetCatalog.find("chirpstack-v4").orElseThrow());
        assertThat(merged.has("presetParams")).isFalse();
        assertThat(merged.get("private").asBoolean()).as("the input wins").isFalse();
        assertThat(merged.get("mapping").get("geometry").get("fallback").get(0).asDouble()).isEqualTo(-97.7431);
        assertThat(merged.get("volatileFields").get(0).asString()).isEqualTo("fCnt");

        ObjectNode am = PresetCatalog.apply(PresetContract.JSON.createObjectNode().put("id", "custom-am")
                .set("presetParams", PresetContract.JSON.createObjectNode().put("tombstoneOnResolve", true).put("lon", 1.234)
                        .put("lat", 5.678)), PresetCatalog.find("alertmanager").orElseThrow());
        ObjectNode options = (ObjectNode) am.get("formats").get("alertmanager");
        assertThat(options.get("resolved").asString()).isEqualTo("tombstone");
        assertThat(options.get("receiverPrefix").asBoolean()).isTrue();
        assertThat(options.get("station").get(0).asDouble()).isEqualTo(1.23);
        assertThat(am.has("mapping")).isFalse();

        assertThatThrownBy(() -> PresetCatalog.apply(PresetContract.JSON.createObjectNode().put("id", "custom-y")
                .set("presetParams", PresetContract.JSON.createObjectNode().put("lat", 1).put("tint", "red")),
                PresetCatalog.find("gmc").orElseThrow()))
                .isInstanceOf(ValidationException.class);
        assertThat(PresetCatalog.round(-97.745)).isEqualTo(-97.74);
    }

    @Test
    @DisplayName("setup: the listing keeps {sourceId} and {key}; creation fills them, with host, port and path")
    void setup() {
        MappingPreset ecowitt = PresetCatalog.find("ecowitt").orElseThrow();
        Map<String, Object> listing = PresetCatalog.setup(ecowitt, "https://fs.example.org:8443/", null, null);
        assertThat(listing.get("urlTemplate")).isEqualTo("https://fs.example.org:8443/v1/ingest/{sourceId}/k/{key}/ecowitt");
        assertThat(listing.get("url")).isNull();
        Map<String, Object> created = PresetCatalog.setup(ecowitt, "http://192.168.1.10:8080", "custom-wx", "eik_abc");
        assertThat(created.get("url")).isEqualTo("http://192.168.1.10:8080/v1/ingest/custom-wx/k/eik_abc/ecowitt");
        assertThat((String) created.get("instructions")).contains("`192.168.1.10`").contains("`8080`")
                .contains("`/v1/ingest/custom-wx/k/eik_abc/ecowitt`");
        Map<String, Object> wu = PresetCatalog.setup(PresetCatalog.find("wu-pws").orElseThrow(), "https://fs.example.org",
                "custom-wu", "eik_k");
        assertThat((String) wu.get("instructions")).contains("`443`").contains("PASSWORD=eik_k");
        assertThat(PresetCatalog.view(ecowitt, "http://h").get("allowsGet")).isEqualTo(false);
    }
}
