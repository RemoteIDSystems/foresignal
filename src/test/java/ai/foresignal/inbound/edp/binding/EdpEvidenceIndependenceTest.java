package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceGrouper;
import ai.foresignal.evidence.EvidenceResolver;
import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.OriginatorRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Evidence independence of bound EDP events (prompt 138, EXT-2, EDP §3.4): independence is counted by originator,
 * never by provider; an originator that aggregates a built-in source's originator joins its evidence group, distinct
 * originators count separately, and a provider never redefines a curated originator.
 */
class EdpEvidenceIndependenceTest {

    /** A registry declaring the built-in USGS originator only. */
    private static OriginatorRegistry registry() {
        Map<String, Object> usgs = new LinkedHashMap<>();
        usgs.put("id", "usgs-anss");
        usgs.put("class", "sensor-network");
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("originators", List.of(usgs));
        doc.put("sources", Map.of("usgs-earthquakes", "usgs-anss"));
        return OriginatorRegistry.of(doc, BindingFixtures.JSON);
    }

    /** A member of the given originator. */
    private static EvidenceGrouper.Member member(String key, String source, Originator originator, long seq) {
        return new EvidenceGrouper.Member(key, source, originator, null, seq);
    }

    @Test
    @DisplayName("an EDP originator that aggregates a built-in originator adds no independent evidence group")
    void aggregatingProviderJoinsTheGroup() {
        OriginatorRegistry registry = registry();
        Originator edp = registry.registerExternal(new Originator("test-lab", "aggregator", null,
                List.of("usgs-anss"), null, List.of()));
        Originator usgs = registry.resolve("usgs-earthquakes");
        List<EvidenceGrouper.Group> groups = EvidenceGrouper.group(List.of(member("a", "usgs-earthquakes", usgs, 1),
                member("b", BindingFixtures.SOURCE, edp, 2)), registry);
        assertThat(groups).hasSize(1);
        assertThat(registry.aggregates(edp, usgs)).isTrue();
        assertThat(registry.independent(edp, usgs)).isFalse();
    }

    @Test
    @DisplayName("distinct originators count separately; two providers relaying one originator count once")
    void distinctOriginators() {
        OriginatorRegistry registry = registry();
        Originator independent = registry.registerExternal(new Originator("harbor-sensors", "sensor-network", null,
                List.of(), null, List.of()));
        Originator usgs = registry.resolve("usgs-earthquakes");
        assertThat(EvidenceGrouper.group(List.of(member("a", "usgs-earthquakes", usgs, 1),
                member("b", BindingFixtures.SOURCE, independent, 2)), registry)).hasSize(2);
        Originator viaOther = registry.originator("harbor-sensors").orElseThrow();
        assertThat(EvidenceGrouper.group(List.of(member("a", "ext-one", independent, 1),
                member("b", "ext-two", viaOther, 2)), registry)).hasSize(1);
    }

    @Test
    @DisplayName("an item-level origin distinct from the manifest's counts on its own")
    void itemLevelOrigin() {
        OriginatorRegistry registry = registry();
        BoundStream stream = BindingFixtures.stream("incidents");
        Map<String, Object> edp = new LinkedHashMap<>();
        edp.put("stream", "incidents");
        edp.put("itemType", "event");
        edp.put("originator", "ukmto");
        edp.put("originatorClass", "agency");
        BoundItem own = BindingFixtures.item("incidents", "i-1", "event", 5, Map.of("edp", edp));
        Originator item = ExternalOriginators.itemOriginator(own, stream.manifest());
        assertThat(item.id()).isEqualTo("ukmto");
        assertThat(item.originatorClass()).isEqualTo("agency");
        assertThat(item.aggregates()).isEmpty();
        registry.registerExternal(item);
        Originator usgs = registry.resolve("usgs-earthquakes");
        assertThat(EvidenceGrouper.group(List.of(member("a", "usgs-earthquakes", usgs, 1),
                member("b", BindingFixtures.SOURCE, registry.originator("ukmto").orElseThrow(), 2)), registry))
                .hasSize(2);
    }

    @Test
    @DisplayName("the manifest originator carries its class and aggregates; items naming none use it")
    void manifestOriginator() {
        BoundStream stream = BindingFixtures.stream("incidents");
        Originator declared = ExternalOriginators.manifestOriginator(stream.manifest());
        assertThat(declared.id()).isEqualTo("test-lab");
        assertThat(declared.originatorClass()).isEqualTo("aggregator");
        assertThat(declared.aggregates()).containsExactly("usgs-anss");
        BoundItem plain = BindingFixtures.item("incidents", "i-2", "event", 6, Map.of());
        assertThat(ExternalOriginators.itemOriginator(plain, stream.manifest())).isEqualTo(declared);
    }

    @Test
    @DisplayName("a provider never redefines a curated originator")
    void curatedWins() {
        OriginatorRegistry registry = registry();
        Originator claimed = registry.registerExternal(new Originator("usgs-anss", "crowd", null, List.of(), null,
                List.of()));
        assertThat(claimed.originatorClass()).isEqualTo("sensor-network");
        assertThat(registry.externalOriginators()).isEmpty();
    }

    @Test
    @DisplayName("upstream ids: upstreamId and derivedFrom are matched as one list")
    void upstreamIds() {
        Map<String, Object> edp = new LinkedHashMap<>();
        edp.put("stream", "incidents");
        edp.put("upstreamId", "ukmto-042");
        edp.put("derivedFrom", List.of("incidents:i-0", "ukmto-042"));
        BoundItem item = BindingFixtures.item("incidents", "i-3", "event", 7, Map.of("edp", edp));
        Map<String, Object> payload = CanonicalFamilyBindingTarget.payloadWithUpstream(item);
        assertThat(payload.get(CanonicalFamilyBindingTarget.UPSTREAM_FIELD))
                .isEqualTo(List.of("ukmto-042", "incidents:i-0"));
        assertThat(item.payload()).doesNotContainKey(CanonicalFamilyBindingTarget.UPSTREAM_FIELD);
    }

    @Test
    @DisplayName("family rules: bundled families take overrides; unknown families get a 3600 s / 25 km proximity rule")
    void familyRules() {
        Map<String, Object> seismic = new LinkedHashMap<>();
        seismic.put("family", "seismic");
        seismic.put("inputs", List.of(Map.of("kind", "earthquake")));
        seismic.put("match", "proximity");
        seismic.put("maxDtSeconds", 120);
        seismic.put("maxKm", 100);
        Map<String, Object> story = new LinkedHashMap<>();
        story.put("family", "news-story");
        story.put("inputs", List.of(Map.of("kind", "news-article")));
        story.put("match", "story");
        CanonicalRules rules = new CanonicalRules(Map.of("families", List.of(seismic, story)));
        CanonicalFamilyBindingTarget target = new CanonicalFamilyBindingTarget(rules,
                Mockito.mock(EvidenceResolver.class), Mockito.mock(ExternalOriginators.class));
        CanonicalRules.FamilyRule bundled = target.rule(Map.of("family", "seismic"));
        assertThat(bundled.maxDtSeconds()).isEqualTo(120);
        CanonicalRules.FamilyRule overridden = target.rule(Map.of("family", "seismic", "maxKm", 30));
        assertThat(overridden.maxKm()).isEqualTo(30);
        assertThat(overridden.maxDtSeconds()).isEqualTo(120);
        CanonicalRules.FamilyRule unknown = target.rule(Map.of("family", "maritime-incident"));
        assertThat(unknown.match()).isEqualTo("proximity");
        assertThat(unknown.maxDtSeconds()).isEqualTo(CanonicalFamilyBindingTarget.DEFAULT_MAX_DT_SECONDS);
        assertThat(unknown.maxKm()).isEqualTo(CanonicalFamilyBindingTarget.DEFAULT_MAX_KM);
        BoundStream stream = BindingFixtures.stream("incidents");
        assertThat(target.validate(Map.of("family", "news-story"), stream)).isNotEmpty();
        assertThat(target.validate(Map.of(), stream)).extracting("field").contains("config.family");
        assertThat(target.validate(Map.of("family", "maritime-incident"), stream)).isEmpty();
    }
}
