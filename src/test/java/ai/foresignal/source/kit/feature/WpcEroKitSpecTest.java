package ai.foresignal.source.kit.feature;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.KitOverlay;
import ai.foresignal.source.kit.KitSpecs;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The WPC Excessive Rainfall Outlook feature kit (prompt 089): the five specs load through prompt 073's family,
 * validate, and the real fixture maps categories and validity.
 */
class WpcEroKitSpecTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Set<String> CATEGORIES = Set.of("MRGL", "SLGT", "MDT", "HIGH");

    private static List<FeatureSourceSpec> specs() {
        return new KitSpecs(KitOverlay.disabled(), "").features().stream()
                .filter(f -> f.id().startsWith("wpc-ero-")).toList();
    }

    @Test
    @DisplayName("the five day specs load, validate and describe an ArcGIS rainfall-outlook layer")
    void specsValid() {
        List<FeatureSourceSpec> ero = specs();
        assertThat(ero).hasSize(5).extracting(FeatureSourceSpec::id)
                .containsExactlyInAnyOrder("wpc-ero-d1", "wpc-ero-d2", "wpc-ero-d3", "wpc-ero-d4", "wpc-ero-d5");
        for (FeatureSourceSpec spec : ero) {
            assertThat(FeatureSpecValidator.problems(spec, false)).as(spec.id()).isEmpty();
            assertThat(spec.protocol()).as(spec.id()).isEqualTo("arcgis");
            assertThat(spec.kind()).as(spec.id()).isEqualTo("rainfall-outlook");
            assertThat(spec.tombstonesAbsent()).as(spec.id()).isTrue();
            assertThat(spec.query().where()).as(spec.id()).isEqualTo("1=1");
            assertThat(spec.time().observedAt()).as(spec.id()).isEqualTo("issueTime");
            assertThat(spec.time().validFrom()).as(spec.id()).isEqualTo("startTime");
            assertThat(spec.time().validUntil()).as(spec.id()).isEqualTo("endTime");
            assertThat(spec.fields().get("dn").valueMap()).as(spec.id())
                    .containsEntry("1", "MRGL").containsEntry("4", "HIGH");
        }
    }

    @Test
    @DisplayName("a day-1 descriptor validates and its sources ship disabled")
    void descriptorValid() {
        for (SourceAdapter a : new FeatureKitFamily(specs(), "", null).adapters()) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
            assertThat(a.descriptor().licence()).isNotBlank();
        }
    }

    @Test
    @DisplayName("the real day-1 fixture maps the category and the validity fields")
    void realFixture() {
        FeatureSourceSpec d1 = specs().stream().filter(f -> f.id().equals("wpc-ero-d1")).findFirst().orElseThrow();
        JsonNode layer = JSON.readTree(AdapterFixtures.bytes("wpc-ero-d1", "layer.geojson"));
        int accepted = 0;
        for (JsonNode feature : layer.get("features")) {
            FeatureMapper.Mapped mapped = FeatureMapper.map(d1, feature);
            if (!mapped.accepted()) {
                continue;
            }
            accepted++;
            assertThat(mapped.record().payload().get("category")).isIn(CATEGORIES);
            assertThat(mapped.record().validFrom()).isNotNull();
            assertThat(mapped.record().validUntil()).isNotNull();
            assertThat(mapped.record().observedAt()).isNotNull();
            assertThat(mapped.record().geometry()).isNotNull();
        }
        assertThat(accepted).isGreaterThan(0);
    }

    @Test
    @DisplayName("a category that vanishes from two complete reads is tombstoned; a day with no risk area is normal")
    void vanishedCategoryTombstoned() {
        FeatureSourceSpec d1 = specs().stream().filter(f -> f.id().equals("wpc-ero-d1")).findFirst().orElseThrow();
        assertThat(specs()).allMatch(s -> Boolean.TRUE.equals(s.zeroResultsNormal()));
        JsonNode layer = JSON.readTree(AdapterFixtures.bytes("wpc-ero-d1", "layer.geojson"));
        List<String> ids = new java.util.ArrayList<>();
        for (JsonNode feature : layer.get("features")) {
            FeatureMapper.Mapped mapped = FeatureMapper.map(d1, feature);
            if (mapped.accepted() && !ids.contains(mapped.record().externalId())) {
                ids.add(mapped.record().externalId());
            }
        }
        assertThat(ids).as("the fixture needs two categories").hasSizeGreaterThanOrEqualTo(2);
        String vanished = ids.getLast();
        List<String> remaining = ids.subList(0, ids.size() - 1);

        java.util.Map<String, String> values = new java.util.HashMap<>();
        ai.foresignal.source.spi.AdapterStateStore state = new ai.foresignal.source.spi.AdapterStateStore() {
            @Override
            public java.util.Optional<String> get(String key) {
                return java.util.Optional.ofNullable(values.get(key));
            }

            @Override
            public void put(String key, String value) {
                values.put(key, value);
            }

            @Override
            public void remove(String key) {
                values.remove(key);
            }
        };
        var complete = ai.foresignal.source.support.AbsenceTombstoner.Outcome.COMPLETE;
        assertThat(ai.foresignal.source.support.AbsenceTombstoner.update(state, complete, ids)).isEmpty();
        assertThat(ai.foresignal.source.support.AbsenceTombstoner.update(state, complete, remaining))
                .as("one missing read is not enough").isEmpty();
        List<String> gone = ai.foresignal.source.support.AbsenceTombstoner.update(state, complete, remaining);
        assertThat(gone).containsExactly(vanished);
        assertThat(ai.foresignal.source.support.AbsenceTombstoner.tombstones(gone, d1.kind(), java.time.Instant.now()))
                .singleElement().satisfies(t -> {
                    assertThat(t.tombstone()).isTrue();
                    assertThat(t.externalId()).isEqualTo(vanished);
                    assertThat(t.kind()).isEqualTo("rainfall-outlook");
                });
    }
}
