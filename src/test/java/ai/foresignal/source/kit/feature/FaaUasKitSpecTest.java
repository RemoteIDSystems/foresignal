package ai.foresignal.source.kit.feature;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.KitSpecLoader;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The FAA UAS restriction layers as prompt 073 feature-kit specs ({@code kits/features/faa-uas.json}, prompt 087):
 * the file loads and validates through 073's loader and family, the ids are valid and the catalog's, every descriptor
 * validates, and each field map covers the captured layer's attributes and maps its first page.
 */
class FaaUasKitSpecTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** Source id → the fixture prefix of its captured layer (fixtures/faa-uas/{prefix}-layer.json, -page.json). */
    static final Map<String, String> LAYERS = Map.of(
            "faa-uas-nsufr", "DoD_Mar_13",
            "faa-uas-nsufr-parttime", "Part_Time_National_Security_UAS_Flight_Restrictions",
            "faa-uas-recreational-sites", "Recreational_Flyer_Fixed_Sites",
            "faa-uas-stadiums", "Stadiums",
            "faa-uas-prohibited", "Prohibited_Areas",
            "faa-uas-defense-tfr", "National_Defense_Airspace_TFR_Areas");

    private static List<FeatureSourceSpec> specs() {
        List<String> problems = new ArrayList<>();
        List<KitSpecLoader.Loaded<FeatureSourceSpec>> loaded = KitSpecLoader.classpath("kits/features",
                FeatureSourceSpec.class, problems).stream().filter(l -> l.origin().endsWith("faa-uas.json")).toList();
        assertThat(problems).isEmpty();
        assertThat(FeatureSpecValidator.problems(loaded, Set.of(), null)).isEmpty();
        return loaded.stream().map(KitSpecLoader.Loaded::spec).toList();
    }

    @Test
    @DisplayName("the six specs load and validate, with the catalog's ids, and ship as kit adapters")
    void loads() {
        List<FeatureSourceSpec> specs = specs();
        assertThat(specs).extracting(FeatureSourceSpec::id).containsExactlyInAnyOrderElementsOf(LAYERS.keySet());
        for (FeatureSourceSpec spec : specs) {
            assertThat(FeatureSpecValidator.ID_PATTERN.matcher(spec.id()).matches()).as(spec.id()).isTrue();
            assertThat(spec.protocol()).isEqualTo(FeatureSourceSpec.ARCGIS);
            assertThat(spec.tombstonesAbsent()).as(spec.id()).isTrue();
            assertThat(spec.license()).isEqualTo("US-PD");
            assertThat(spec.intervalDuration()).isEqualTo(Duration.ofHours(24));
            assertThat(spec.time().observedAtReceived()).isTrue();
            assertThat(spec.constants()).containsKey("restrictionType");
            assertThat(spec.url()).startsWith("https://services6.arcgis.com/ssFJjBXIUyZDrSYZ/ArcGIS/rest/services/"
                    + LAYERS.get(spec.id()) + "/FeatureServer/0");
            assertThat(spec.kind()).isEqualTo(spec.id().equals("faa-uas-recreational-sites") ? "uas-site"
                    : "uas-restriction");
        }
        List<SourceAdapter> adapters = new FeatureKitFamily(specs, "", null).adapters();
        for (SourceAdapter a : adapters) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
            assertThat(a.descriptor().defaultBudget()).isEqualTo(Budget.of(10, 100));
            assertThat(a.descriptor().defaultRetention()).isEqualTo(Duration.ofDays(90));
        }
        assertThat(FeatureSpecValidator.ID_PATTERN.matcher("faa-other").matches()).isFalse();
        assertThat(FeatureSpecValidator.ID_PATTERN.matcher("feature-x").matches()).isTrue();
    }

    @Test
    @DisplayName("every mapped attribute exists in the captured layer, and the first page maps with ids and fields")
    void fieldMapsCoverTheFixtures() {
        for (FeatureSourceSpec spec : specs()) {
            String prefix = LAYERS.get(spec.id());
            JsonNode layer = JSON.readTree(AdapterFixtures.bytes("faa-uas", prefix + "-layer.json"));
            Set<String> attributes = new TreeSet<>();
            layer.get("fields").forEach(f -> attributes.add(f.get("name").asString()));
            assertThat(attributes).as(spec.id()).containsAll(spec.fields().keySet());

            JsonNode page = JSON.readTree(AdapterFixtures.bytes("faa-uas", prefix + "-page.json"));
            List<NormalizedRecord> records = new ArrayList<>();
            for (JsonNode feature : page.get("features")) {
                FeatureMapper.Mapped mapped = FeatureMapper.map(spec, feature);
                assertThat(mapped.accepted()).as(spec.id() + ": " + mapped.reason()).isTrue();
                records.add(mapped.record());
            }
            assertThat(records).as(spec.id()).hasSize(3).allSatisfy(r -> {
                assertThat(r.payload()).containsKeys("name", "state", "restrictionType");
                assertThat(r.geometry()).isNotNull();
            });
        }
    }

    @Test
    @DisplayName("national security restrictions: Surface is 0 ft, an AGL ceiling text is feet, the id is OBJECTID")
    void nationalSecurityValues() {
        FeatureSourceSpec spec = specs().stream().filter(s -> s.id().equals("faa-uas-nsufr")).findFirst().orElseThrow();
        JsonNode feature = JSON.readTree(AdapterFixtures.bytes("faa-uas", "DoD_Mar_13-page.json")).get("features")
                .get(0);
        NormalizedRecord r = FeatureMapper.map(spec, feature).record();
        assertThat(r.externalId()).isEqualTo("1");
        assertThat(r.payload()).containsEntry("floorFt", 0).containsEntry("ceilingFt", 400)
                .containsEntry("name", "Marine Corps Air Station New River").containsEntry("facility", "MCAS New River 2")
                .containsEntry("reason", "National Security").containsEntry("state", "NC")
                .containsEntry("restrictionType", "national-security");
    }
}
