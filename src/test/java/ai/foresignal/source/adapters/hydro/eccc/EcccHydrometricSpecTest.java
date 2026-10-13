package ai.foresignal.source.adapters.hydro.eccc;

import static ai.foresignal.source.adapters.hydro.HydroTestSupport.features;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.kit.feature.FeatureMapper;
import ai.foresignal.source.kit.feature.FeatureSourceSpec;
import ai.foresignal.source.kit.feature.FeatureSpecValidator;
import ai.foresignal.source.kit.feature.KitFeatureAdapter;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import tools.jackson.databind.JsonNode;

/**
 * {@code eccc-hydrometric}'s feature spec on the two 2026-10-13 {@code hydrometric-realtime} pages around Ottawa:
 * the field mapping, the moving {@code datetime} window, the {@code latestPerKey} choice of the newest row per
 * station ({@link KitFeatureAdapter#newer}), and the descriptor with its required box.
 */
class EcccHydrometricSpecTest {

    static final Instant NOW = Instant.parse("2026-10-13T22:14:07.5Z");
    static final Envelope OTTAWA = new Envelope(-76, -75, 45, 45.6);

    static List<NormalizedRecord> mapped() {
        FeatureSourceSpec spec = EcccHydrometricAdapter.spec(OTTAWA, NOW);
        List<JsonNode> rows = new ArrayList<>(features("eccc-hydrometric", "items-page1.json"));
        rows.addAll(features("eccc-hydrometric", "items-page2.json"));
        return rows.stream().map(f -> FeatureMapper.map(spec, f)).filter(FeatureMapper.Mapped::accepted)
                .map(FeatureMapper.Mapped::record).toList();
    }

    @Test
    @DisplayName("the spec: OGC collection, the box, datetime two hours back, newest first, latestPerKey")
    void spec() {
        FeatureSourceSpec spec = EcccHydrometricAdapter.spec(OTTAWA, NOW);
        assertThat(spec.protocol()).isEqualTo("ogc");
        assertThat(spec.collection()).isEqualTo("hydrometric-realtime");
        assertThat(spec.query().bbox()).containsExactly(-76.0, 45.0, -75.0, 45.6);
        assertThat(spec.query().datetime()).isEqualTo("2026-10-13T20:14:07Z/..");
        assertThat(spec.query().filters()).containsEntry("sortby", "-DATETIME");
        assertThat(spec.keepsLatestPerKey()).isTrue();
        assertThat(FeatureSpecValidator.problems(KitFeatureAdapter.rebased(spec, ""), false))
                .allMatch(p -> p.startsWith("id:"));
    }

    @Test
    @DisplayName("ten rows of seven stations map with level, discharge and the measurement time")
    void rows() {
        List<NormalizedRecord> records = mapped();
        assertThat(records).hasSize(10).allSatisfy(r -> assertThat(r.kind()).isEqualTo("water-observation"));
        NormalizedRecord payne = records.getFirst();
        assertThat(payne.externalId()).isEqualTo("02LB022");
        assertThat(payne.observedAt()).isEqualTo(Instant.parse("2026-10-13T21:45:00Z")).isEqualTo(payne.eventTime());
        assertThat(payne.payload()).containsEntry("stationName", "PAYNE RIVER NEAR BERWICK")
                .containsEntry("province", "ON").containsEntry("levelM", 6.198).containsEntry("dischargeM3s", 1.15)
                .doesNotContainKeys("levelSymbol", "dischargeSymbol");
        assertThat(records.stream().map(NormalizedRecord::externalId).distinct()).hasSize(7);
        FieldSpecContract.assertPayloadKeysDeclared(new EcccHydrometricAdapter("", () -> null).descriptor(),
                records);
    }

    @Test
    @DisplayName("latestPerKey keeps the newer of two rows of a station, whichever comes first")
    void latestPerKey() {
        List<NormalizedRecord> records = mapped();
        NormalizedRecord newer = records.get(0);
        NormalizedRecord older = records.stream().filter(r -> r.externalId().equals("02LB022")
                && r.observedAt().equals(Instant.parse("2026-10-13T21:40:00Z"))).findFirst().orElseThrow();
        assertThat(KitFeatureAdapter.newer(newer, older)).isTrue();
        assertThat(KitFeatureAdapter.newer(older, newer)).isFalse();
        assertThat(KitFeatureAdapter.newer(newer, newer)).isFalse();
    }

    @Test
    @DisplayName("descriptor: valid, the required box parameter, 3 h TTL; checkParams; the snapshot contract")
    void descriptorAndSnapshot() {
        EcccHydrometricAdapter adapter = new EcccHydrometricAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().params()).extracting(ParamSpec::name).containsExactly("bbox");
        assertThat(adapter.descriptor().params().getFirst().required()).isTrue();
        assertThat(adapter.descriptor().recordTtl()).isEqualTo(Duration.ofHours(3));
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey("bbox");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", OTTAWA)))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", new Envelope(-90, -75, 40, 45.6)))))
                .containsKey("bbox");
        NormalizedRecord r = EnvTestSupport.byId(mapped(), "02LA004");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(), List.of(r, EnvTestSupport.later(r,
                Duration.ofMinutes(5))));
    }
}
