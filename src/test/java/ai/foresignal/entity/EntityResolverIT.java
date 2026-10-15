package ai.foresignal.entity;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The deterministic resolution rules of {@link EntityResolver} (prompt 098): anchored creation, a
 * provisional merge, a weak-key re-assignment and the callsign-only no-op.
 */
@IntegrationTest
class EntityResolverIT {

    private static final Instant NOW = Instant.parse("2026-10-15T00:00:00Z");

    @Autowired
    private EntityResolver resolver;

    @Autowired
    private EntityRepository entities;

    @Autowired
    private EntityKeyRepository keys;

    @Test
    void anchorKeyCreatesAnAnchoredEntityAndRelinks() {
        Resolution first = resolver.resolve(
                List.of(key("airframe", "S22:1234", EntityKeyRole.ANCHOR, 1.0f)), NOW, KeyOrigin.REGISTRY, "faa-registry:N123AB");
        assertThat(first).isNotNull();
        assertThat(entities.findById(first.entityId()).orElseThrow().isProvisional()).isFalse();

        Resolution again = resolver.resolve(
                List.of(key("airframe", "S22:1234", EntityKeyRole.ANCHOR, 1.0f)), NOW, KeyOrigin.REGISTRY, "faa-registry:N123AB");
        assertThat(again.entityId()).isEqualTo(first.entityId());
        assertThat(entities.count()).isEqualTo(1);
    }

    @Test
    void weakOnlyCreatesAProvisionalEntity() {
        Resolution resolution = resolver.resolve(
                List.of(key("icao24", "ae1234", EntityKeyRole.WEAK, 0.9f)), NOW, KeyOrigin.OBSERVED, "adsblol-mil:AE1234");
        assertThat(resolution).isNotNull();
        assertThat(entities.findById(resolution.entityId()).orElseThrow().isProvisional()).isTrue();
    }

    @Test
    void aWeakKeyOnAProvisionalEntityMergesIntoTheAnchor() {
        Resolution provisional = resolver.resolve(
                List.of(key("icao24", "ae1234", EntityKeyRole.WEAK, 0.9f)), NOW, KeyOrigin.OBSERVED, "adsblol-mil:AE1234");
        Resolution anchored = resolver.resolve(
                List.of(
                        key("airframe", "S22:1234", EntityKeyRole.ANCHOR, 1.0f),
                        key("icao24", "ae1234", EntityKeyRole.WEAK, 0.9f)),
                NOW, KeyOrigin.REGISTRY, "faa-registry:N123AB");

        assertThat(anchored.entityId()).isNotEqualTo(provisional.entityId());
        EntityEntity merged = entities.findById(provisional.entityId()).orElseThrow();
        assertThat(merged.getStatus()).isEqualTo("MERGED");
        assertThat(merged.getMergedInto()).isEqualTo(anchored.entityId());
        assertThat(resolver.canonical(provisional.entityId()).getId()).isEqualTo(anchored.entityId());
    }

    @Test
    void aWeakKeyMovedBetweenAnchoredEntitiesIsReassigned() {
        Resolution vesselA = resolver.resolve(
                List.of(key("imo", "9074729", EntityKeyRole.ANCHOR, 1.0f)), NOW, KeyOrigin.CO_ASSERTED, "aisstream:1");
        Resolution vesselB = resolver.resolve(
                List.of(key("imo", "8712345", EntityKeyRole.ANCHOR, 1.0f)), NOW, KeyOrigin.CO_ASSERTED, "aisstream:2");

        resolver.resolve(List.of(key("mmsi", "366123456", EntityKeyRole.WEAK, 0.8f)), NOW, KeyOrigin.OBSERVED, "aisstream:2");
        assertThat(keys.findOpen("mmsi", "366123456").orElseThrow().getEntityId()).isEqualTo(vesselB.entityId());

        resolver.resolve(List.of(key("mmsi", "366123456", EntityKeyRole.WEAK, 0.8f)), NOW.plusSeconds(60),
                KeyOrigin.OBSERVED, "aisstream:1");
        assertThat(keys.findOpen("mmsi", "366123456").orElseThrow().getEntityId()).isEqualTo(vesselA.entityId());
        assertThat(keys.findOpen("mmsi", "366123456").orElseThrow().getValidFrom()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void callsignAloneCreatesNothing() {
        Resolution resolution = resolver.resolve(
                List.of(key("callsign", "air:UAL1", EntityKeyRole.WEAK, 0.5f)), NOW, KeyOrigin.OBSERVED, "adsblol-mil:UAL1");
        assertThat(resolution).isNull();
        assertThat(entities.count()).isZero();
    }

    private static Key key(String type, String value, EntityKeyRole role, float confidence) {
        return new Key(type, value, role, confidence);
    }
}
