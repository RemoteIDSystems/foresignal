package ai.foresignal.inbound.edp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Manifests to descriptors and manifest updates (prompt 024): the conformance manifests validate as
 * labeled, the descriptor carries the manifest's fields (egress from {@code redistribution},
 * originator and access copied, atomic kinds hidden), and updates are classified as additive or
 * breaking (invariant EXT-1).
 */
class EdpManifestServiceTest {

    static EdpManifestModel model(String name) {
        return EdpFixtures.manifest(name).model();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hormuz-example.json", "push-layer.json", "pull-atomic.json"})
    @DisplayName("the valid conformance manifests validate and build a descriptor the registry accepts")
    void validManifests(String name) {
        SourceDescriptor descriptor = EdpDescriptorBuilder.build(model(name), 60);
        assertThat(ai.foresignal.source.DescriptorValidator.problems(descriptor, null))
                .allSatisfy(p -> assertThat(p).contains("has no adapter implementation"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"protocol-too-new.json", "empty-transport.json", "duplicate-stream-ids.json",
            "layer-observation.json", "remote-ref.json", "long-pattern.json"})
    @DisplayName("the invalid conformance manifests are refused with errors")
    void invalidManifests(String name) {
        EdpManifestValidator.ManifestCheck check =
                EdpFixtures.manifestValidator().validate(EdpFixtures.json("manifests/invalid/" + name));
        assertThat(check.valid()).isFalse();
        assertThat(check.errors()).isNotEmpty();
        assertThat(check.protocolUnsupported()).isEqualTo("protocol-too-new.json".equals(name));
    }

    @Test
    @DisplayName("the §2 example: ext- id, push and pull, licence, egress OPEN, originator, access, title and fields")
    void hormuzDescriptor() {
        SourceDescriptor d = EdpDescriptorBuilder.build(model("hormuz-example.json"), 60);
        assertThat(d.id()).isEqualTo("ext-hormuz-transits");
        assertThat(d.custom()).isTrue();
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.mode()).isEqualTo(Mode.POLLING);
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(1));
        assertThat(d.credentialFields()).singleElement().satisfies(c -> {
            assertThat(c.name()).isEqualTo("token");
            assertThat(c.required()).isFalse();
        });
        assertThat(d.kinds()).containsExactlyInAnyOrder("transit-count", "maritime-incident");
        assertThat(d.hiddenKinds()).containsExactly("transit-count");
        assertThat(d.licence()).isEqualTo("CC-BY-4.0");
        assertThat(d.attribution()).isEqualTo("Example OSINT Lab");
        assertThat(d.egress()).isEqualTo(Egress.OPEN);
        assertThat(d.originator()).isEqualTo("example-osint-lab");
        assertThat(d.access().route()).isEqualTo("scrape");
        assertThat(d.access().fragility()).isEqualTo("high");
        assertThat(d.access().termsSummary()).contains("public dashboard");
        assertThat(d.titleField()).isEqualTo("category");
        assertThat(d.significantFields()).containsExactly("category");
        assertThat(d.volatileFields()).contains(EdpItemMapper.PAYLOAD_TIMES);
        assertThat(d.fieldSpecs().get("category").type()).isEqualTo(FieldType.STRING);
        assertThat(d.fieldSpecs().get("vessel").kinds()).containsExactly("maritime-incident");
        assertThat(d.kindRetention().get("maritime-incident")).isEqualTo(Duration.ofDays(1825));
        assertThat(d.kindPresentation().get("maritime-incident")).containsEntry("color", "#d9480f");
        assertThat(d.kindDisplay().get("maritime-incident").icon()).isEqualTo("ship");
        assertThat(d.tileProperties()).containsExactly("category");
        assertThat(d.zeroResultsNormal()).isTrue();
    }

    @Test
    @DisplayName("a push-only provider is PUSH; redistribution local-only or unknown is LOCAL_ONLY; private is copied")
    void egressFromRedistribution() {
        SourceDescriptor push = EdpDescriptorBuilder.build(model("push-layer.json"), 30);
        assertThat(push.mode()).isEqualTo(Mode.PUSH);
        assertThat(push.defaultBudget().perMinute()).isEqualTo(30);
        assertThat(push.hiddenKinds()).containsExactlyInAnyOrder("harbor-note", "harbor-forecast");
        assertThat(push.recordTtl()).isEqualTo(Duration.ofHours(1));

        for (String redistribution : List.of("local-only", "unknown")) {
            ObjectNode manifest = (ObjectNode) EdpFixtures.json("manifests/valid/push-layer.json");
            ((ObjectNode) manifest.get("source").get("licence")).put("redistribution", redistribution);
            ((ObjectNode) manifest.get("source")).put("private", true);
            SourceDescriptor d = EdpDescriptorBuilder.build(EdpManifestModel.parse(manifest), 60);
            assertThat(d.egress()).as(redistribution).isEqualTo(Egress.LOCAL_ONLY);
            assertThat(d.privateSource()).isTrue();
        }
    }

    @Test
    @DisplayName("additive changes are not breaking; kinds, item types, modes, schemas, streams and URLs are")
    void breakingChanges() {
        JsonNode original = EdpFixtures.json("manifests/valid/hormuz-example.json");
        EdpManifestModel before = EdpManifestModel.parse(original);

        ObjectNode additive = (ObjectNode) original.deepCopy();
        ((ObjectNode) additive.get("source")).put("description", "changed");
        ((ObjectNode) additive.get("streams").get(1).get("presentation")).put("color", "#000000");
        assertThat(EdpManifestService.breakingChanges(before, EdpManifestModel.parse(additive))).isEmpty();

        ObjectNode kind = (ObjectNode) original.deepCopy();
        ((ObjectNode) kind.get("streams").get(0)).put("kind", "transit-total");
        assertThat(EdpManifestService.breakingChanges(before, EdpManifestModel.parse(kind)))
                .containsExactly("stream transits kind changed");

        ObjectNode schema = (ObjectNode) original.deepCopy();
        ((ObjectNode) schema.get("streams").get(1).get("payloadSchema")).putArray("required").add("vessel");
        assertThat(EdpManifestService.breakingChanges(before, EdpManifestModel.parse(schema)))
                .containsExactly("stream incidents payloadSchema changed");

        ObjectNode mode = (ObjectNode) original.deepCopy();
        ((ObjectNode) mode.get("streams").get(1)).put("mode", "atomic");
        assertThat(EdpManifestService.breakingChanges(before, EdpManifestModel.parse(mode)))
                .containsExactly("stream incidents mode changed");

        ObjectNode removed = (ObjectNode) original.deepCopy();
        ((tools.jackson.databind.node.ArrayNode) removed.get("streams")).remove(0);
        assertThat(EdpManifestService.breakingChanges(before, EdpManifestModel.parse(removed)))
                .containsExactly("stream transits removed");

        ObjectNode url = (ObjectNode) original.deepCopy();
        ((ObjectNode) url.get("transport").get("pull")).put("itemsUrl", "https://elsewhere.example.org/items");
        assertThat(EdpManifestService.breakingChanges(before, EdpManifestModel.parse(url)))
                .containsExactly("transport.pull.itemsUrl changed");
    }

    @Test
    @DisplayName("the pull adapter builds its page URL and auth headers from the manifest and the stored credential")
    void pullRequests() throws Exception {
        assertThat(EdpPullAdapter.pageUri("https://p.example/items", null).toString())
                .isEqualTo("https://p.example/items?limit=500");
        assertThat(EdpPullAdapter.pageUri("https://p.example/items?feed=a", "c 2").toString())
                .isEqualTo("https://p.example/items?feed=a&cursor=c+2&limit=500");
        assertThat(EdpPullAdapter.authHeaders(new EdpManifestModel.Pull("u", 60, "bearer", null),
                java.util.Map.of("token", "t"))).containsEntry("Authorization", "Bearer t");
        assertThat(EdpPullAdapter.authHeaders(new EdpManifestModel.Pull("u", 60, "header", "X-Key"),
                java.util.Map.of("value", "v"))).containsEntry("X-Key", "v");
        assertThat(EdpPullAdapter.authHeaders(new EdpManifestModel.Pull("u", 60, "basic", null),
                java.util.Map.of("username", "a", "password", "b")))
                .containsEntry("Authorization", "Basic YTpi");
        assertThat(EdpPullAdapter.authHeaders(new EdpManifestModel.Pull("u", 60, "none", null), null))
                .doesNotContainKey("Authorization");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> EdpPullAdapter.authHeaders(
                new EdpManifestModel.Pull("u", 60, "bearer", null), java.util.Map.of()))
                .isInstanceOf(ai.foresignal.source.spi.AdapterException.class);
    }
}
