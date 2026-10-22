package ai.foresignal.entity;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The deterministic resolution rules of {@link EntityResolver} (prompt 098): anchored creation, a
 * provisional merge, a weak-key re-assignment, the two-entity conflict (precedence, never a merge or a
 * key move), registry keys outranking observed ones, a deterministic link key and the callsign-only
 * no-op.
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

    @Autowired
    private MeterRegistry meters;

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
    void aWeakKeyCoAssertedWithAnotherAnchorIsReassigned() {
        Resolution vesselA = resolver.resolve(
                List.of(key("imo", "9074729", EntityKeyRole.ANCHOR, 1.0f), key("mmsi", "366123456", EntityKeyRole.WEAK, 0.8f)),
                NOW, KeyOrigin.CO_ASSERTED, "aisstream:1");
        assertThat(keys.findOpen("mmsi", "366123456").orElseThrow().getEntityId()).isEqualTo(vesselA.entityId());

        // The MMSI moves to a new hull: the old key closes and reopens on the new anchored entity.
        Resolution vesselB = resolver.resolve(
                List.of(key("imo", "9161235", EntityKeyRole.ANCHOR, 1.0f), key("mmsi", "366123456", EntityKeyRole.WEAK, 0.8f)),
                NOW.plusSeconds(60), KeyOrigin.CO_ASSERTED, "aisstream:2");

        assertThat(vesselB.entityId()).isNotEqualTo(vesselA.entityId());
        EntityKeyEntity open = keys.findOpen("mmsi", "366123456").orElseThrow();
        assertThat(open.getEntityId()).isEqualTo(vesselB.entityId());
        assertThat(open.getValidFrom()).isEqualTo(NOW.plusSeconds(60));
        assertThat(keys.findByEntityIdOrderByIdDesc(vesselA.entityId()))
                .filteredOn(k -> k.getKeyType().equals("mmsi"))
                .singleElement()
                .satisfies(k -> assertThat(k.getValidTo()).isEqualTo(NOW.plusSeconds(60)));
        assertThat(entities.findById(vesselA.entityId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void weakKeysOnTwoEntitiesLinkByPrecedenceAndNeverMergeOrMoveAKey() {
        // X: anchored by the FAA registry, holding registration N123AB.
        Resolution x = resolver.resolve(
                List.of(
                        key("airframe", "S22:1234", EntityKeyRole.ANCHOR, 1.0f),
                        key("registration", "N123AB", EntityKeyRole.WEAK, 0.9f),
                        key("icao24", "a00001", EntityKeyRole.WEAK, 0.9f)),
                NOW, KeyOrigin.REGISTRY, "faa-registry:N123AB");
        // Y: provisional, from an ADS-B feed's icao24.
        Resolution y = resolver.resolve(
                List.of(key("icao24", "ae9999", EntityKeyRole.WEAK, 0.9f)), NOW, KeyOrigin.CO_ASSERTED, "adsblol:ae9999");
        double conflictsBefore = conflicts();

        // A record carrying Y's icao24 and X's registration.
        Resolution linked = resolver.resolve(
                List.of(
                        key("registration", "N123AB", EntityKeyRole.WEAK, 0.9f),
                        key("icao24", "ae9999", EntityKeyRole.WEAK, 0.9f)),
                NOW.plusSeconds(60), KeyOrigin.CO_ASSERTED, "adsblol:ae9999");

        // icao24 outranks registration: linked to Y, and nothing else changes.
        assertThat(linked.entityId()).isEqualTo(y.entityId());
        assertThat(linked.linkKey().keyType()).isEqualTo("icao24");
        assertThat(conflicts()).isEqualTo(conflictsBefore + 1);
        EntityKeyEntity registration = keys.findOpen("registration", "N123AB").orElseThrow();
        assertThat(registration.getEntityId()).isEqualTo(x.entityId());
        assertThat(registration.getOrigin()).isEqualTo("REGISTRY");
        assertThat(registration.getValidTo()).isNull();
        assertThat(entities.findById(x.entityId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
        assertThat(entities.findById(y.entityId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
        assertThat(keys.findOpenByEntityId(y.entityId())).extracting(EntityKeyEntity::getKeyType)
                .containsExactly("icao24");
    }

    @Test
    void twoProvisionalEntitiesInConflictAreNotMerged() {
        Resolution first = resolver.resolve(
                List.of(key("icao24", "aaa111", EntityKeyRole.WEAK, 0.9f)), NOW, KeyOrigin.CO_ASSERTED, "adsb:1");
        Resolution second = resolver.resolve(
                List.of(key("registration", "N55", EntityKeyRole.WEAK, 0.9f)), NOW, KeyOrigin.CO_ASSERTED, "adsb:2");

        Resolution linked = resolver.resolve(
                List.of(key("registration", "N55", EntityKeyRole.WEAK, 0.9f),
                        key("icao24", "aaa111", EntityKeyRole.WEAK, 0.9f)),
                NOW.plusSeconds(60), KeyOrigin.CO_ASSERTED, "adsb:3");

        assertThat(linked.entityId()).isEqualTo(first.entityId());
        assertThat(entities.findById(first.entityId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
        assertThat(entities.findById(second.entityId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
        assertThat(keys.findOpen("registration", "N55").orElseThrow().getEntityId()).isEqualTo(second.entityId());
        assertThat(keys.findOpen("icao24", "aaa111").orElseThrow().getEntityId()).isEqualTo(first.entityId());
    }

    @Test
    void anObservedKeyNeverSupersedesARegistryKeyOfTheSameType() {
        Resolution x = resolver.resolve(
                List.of(
                        key("airframe", "S22:1234", EntityKeyRole.ANCHOR, 1.0f),
                        key("registration", "N123AB", EntityKeyRole.WEAK, 0.9f),
                        key("icao24", "a00001", EntityKeyRole.WEAK, 0.9f)),
                NOW, KeyOrigin.REGISTRY, "faa-registry:N123AB");
        double conflictsBefore = conflicts();

        // An ADS-B record: the registry's hex, but a different registration in its "r" field.
        Resolution linked = resolver.resolve(
                List.of(key("icao24", "a00001", EntityKeyRole.WEAK, 0.9f),
                        key("registration", "N999ZZ", EntityKeyRole.WEAK, 0.9f)),
                NOW.plusSeconds(60), KeyOrigin.CO_ASSERTED, "adsblol:a00001");

        assertThat(linked.entityId()).isEqualTo(x.entityId());
        assertThat(keys.findOpen("registration", "N123AB").orElseThrow().getEntityId()).isEqualTo(x.entityId());
        assertThat(keys.findOpen("registration", "N999ZZ")).isEmpty();
        assertThat(conflicts()).isEqualTo(conflictsBefore + 1);
    }

    @Test
    void aLaterRegistrySnapshotSupersedesItsOwnKey() {
        Resolution x = resolver.resolve(
                List.of(key("airframe", "S22:1234", EntityKeyRole.ANCHOR, 1.0f), key("icao24", "a00001", EntityKeyRole.WEAK, 0.9f)),
                NOW, KeyOrigin.REGISTRY, "faa-registry:N123AB");
        resolver.resolve(
                List.of(key("airframe", "S22:1234", EntityKeyRole.ANCHOR, 1.0f), key("icao24", "a00002", EntityKeyRole.WEAK, 0.9f)),
                NOW.plusSeconds(86_400), KeyOrigin.REGISTRY, "faa-registry:N123AB");

        assertThat(keys.findOpen("icao24", "a00001")).isEmpty();
        assertThat(keys.findOpen("icao24", "a00002").orElseThrow().getEntityId()).isEqualTo(x.entityId());
    }

    @Test
    void theLinkKeyDoesNotDependOnTheOrderTheKeysArrive() {
        Key hex = key("icao24", "ae1234", EntityKeyRole.WEAK, 0.9f);
        Key tail = key("registration", "N123AB", EntityKeyRole.WEAK, 0.9f);
        Resolution first = resolver.resolve(List.of(tail, hex), NOW, KeyOrigin.CO_ASSERTED, "a:1");
        Resolution second = resolver.resolve(List.of(hex, tail), NOW, KeyOrigin.CO_ASSERTED, "b:1");

        assertThat(second.entityId()).isEqualTo(first.entityId());
        assertThat(first.linkKey()).isEqualTo(second.linkKey());
        assertThat(first.linkKey().keyType()).isEqualTo("icao24");
    }

    private double conflicts() {
        return meters.counter("foresignal.entity.conflicts").count();
    }

    @Test
    void callsignAloneCreatesNothing() {
        Resolution resolution = resolver.resolve(
                List.of(key("callsign", "air:UAL1", EntityKeyRole.WEAK, 0.5f)), NOW, KeyOrigin.OBSERVED, "adsblol-mil:UAL1");
        assertThat(resolution).isNull();
        assertThat(entities.count()).isZero();
    }

    @Test
    void anLeiAnchorsAnOrganizationDeterministically() {
        Resolution first = resolver.resolve(
                List.of(key("lei", "5493001KJTIIGC8Y1R12", EntityKeyRole.ANCHOR, 1.0f)),
                NOW, KeyOrigin.REGISTRY, "gleif:5493001KJTIIGC8Y1R12");
        assertThat(first).isNotNull();
        assertThat(first.entityType()).isEqualTo(EntityType.ORGANIZATION);
        assertThat(entities.findById(first.entityId()).orElseThrow().isProvisional()).isFalse();

        Resolution again = resolver.resolve(
                List.of(key("lei", "5493001KJTIIGC8Y1R12", EntityKeyRole.ANCHOR, 1.0f)),
                NOW.plusSeconds(3600), KeyOrigin.REGISTRY, "gleif:5493001KJTIIGC8Y1R12");
        assertThat(again.entityId()).isEqualTo(first.entityId());
        assertThat(entities.count()).isEqualTo(1);
    }

    @Test
    void aCompanyNumberAnchorsAnOrganization() {
        Resolution resolution = resolver.resolve(
                List.of(key("company-number", "GB:01234567", EntityKeyRole.ANCHOR, 1.0f)),
                NOW, KeyOrigin.REGISTRY, "companies-house:01234567");
        assertThat(resolution.entityType()).isEqualTo(EntityType.ORGANIZATION);
        assertThat(entities.findById(resolution.entityId()).orElseThrow().isProvisional()).isFalse();
    }

    @Test
    void anOfficerIdAnchorsAPersonAndItsWeakNameKeyLinksBack() {
        Resolution officer = resolver.resolve(
                List.of(key("officer-id", "GB:P7V9ABCD", EntityKeyRole.ANCHOR, 0.9f),
                        key("person-key", "jane doe|1980-01-02|gb", EntityKeyRole.WEAK, 0.5f)),
                NOW, KeyOrigin.REGISTRY, "companies-house:P7V9ABCD");
        assertThat(officer.entityType()).isEqualTo(EntityType.PERSON);
        assertThat(entities.findById(officer.entityId()).orElseThrow().isProvisional()).isFalse();

        // The same person later seen only by the weak name key resolves to the anchored officer entity.
        Resolution weak = resolver.resolve(
                List.of(key("person-key", "jane doe|1980-01-02|gb", EntityKeyRole.WEAK, 0.5f)),
                NOW.plusSeconds(60), KeyOrigin.OBSERVED, "leak:jane");
        assertThat(weak.entityId()).isEqualTo(officer.entityId());
    }

    private static Key key(String type, String value, EntityKeyRole role, float confidence) {
        return new Key(type, value, role, confidence);
    }
}
