package ai.foresignal.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.filter.AssignableTypeFilter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The originator registry (prompt 116): every cataloged source maps to an originator, aggregation and
 * independence groups resolve, citations name originators, operator settings and descriptors override, and
 * unknown sources fall back to themselves with a warning list.
 */
class OriginatorRegistryTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    static Map<String, Object> bundled() throws Exception {
        try (InputStream in = new ClassPathResource(OriginatorRegistry.RESOURCE).getInputStream()) {
            return JSON.readValue(in, new TypeReference<LinkedHashMap<String, Object>>() {});
        }
    }

    private static OriginatorRegistry registry() throws Exception {
        return OriginatorRegistry.of(bundled(), JSON);
    }

    /**
     * Every {@link SourceAdapter} under {@code ai.foresignal.source.adapters} that declares a static {@code String
     * ID} (the cataloged sources with a fixed id).
     */
    static Set<String> cataloged() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(SourceAdapter.class));
        Set<String> ids = new TreeSet<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("ai.foresignal.source.adapters")) {
            Class<?> type = Class.forName(definition.getBeanClassName());
            try {
                Field field = type.getDeclaredField("ID");
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                    field.setAccessible(true);
                    ids.add((String) field.get(null));
                }
            } catch (NoSuchFieldException e) {
                // not an adapter with a fixed id
            }
        }
        return ids;
    }

    @Test
    @DisplayName("every cataloged source maps to an originator the registry declares (or to itself by pattern)")
    void everyCatalogedSourceIsMapped() throws Exception {
        OriginatorRegistry registry = registry();
        Set<String> ids = cataloged();
        assertThat(ids).as("the scan found the adapters").contains("usgs-earthquakes", "gdacs", "acled",
                "nasa-firms");
        List<String> unmapped = new ArrayList<>();
        for (String id : ids) {
            if (!registry.mapped(id)) {
                unmapped.add(id);
            }
        }
        assertThat(unmapped).as("cataloged sources without an originator in intel/originators.json").isEmpty();
        Set<String> declared = new TreeSet<>();
        registry.originators().forEach(o -> declared.add(o.id()));
        for (String id : ids) {
            Originator originator = registry.resolve(id);
            assertThat(declared.contains(originator.id()) || originator.id().equals(id))
                    .as(id + " → " + originator.id()).isTrue();
        }
    }

    @Test
    @DisplayName("every originator a mapping or an aggregation names is declared, with a valid class")
    void referencesResolve() throws Exception {
        OriginatorRegistry registry = registry();
        Set<String> declared = new TreeSet<>();
        for (Originator originator : registry.originators()) {
            declared.add(originator.id());
            assertThat(Originator.CLASSES).contains(originator.originatorClass());
        }
        for (Originator originator : registry.originators()) {
            assertThat(declared).as("aggregates of " + originator.id()).containsAll(originator.aggregates());
        }
    }

    @Test
    @DisplayName("GDACS aggregates USGS, so they are never independent; USGS and EMSC are")
    void aggregationAndIndependence() throws Exception {
        OriginatorRegistry registry = registry();
        Originator usgs = registry.resolve("usgs-earthquakes");
        Originator emsc = registry.resolve("emsc-seismic");
        Originator gdacs = registry.resolve("gdacs");
        assertThat(usgs.id()).isEqualTo("usgs-anss");
        assertThat(gdacs.id()).isEqualTo("gdacs");
        assertThat(registry.aggregates(gdacs, usgs)).isTrue();
        assertThat(registry.aggregates(usgs, gdacs)).isFalse();
        assertThat(registry.independent(usgs, gdacs)).isFalse();
        assertThat(registry.independent(usgs, emsc)).isTrue();
        assertThat(registry.independent(gdacs, emsc)).isTrue();
    }

    @Test
    @DisplayName("sources in one independence group are never independent (community ADS-B networks)")
    void independenceGroups() throws Exception {
        OriginatorRegistry registry = registry();
        Originator a = registry.resolve("adsblol-mil");
        Originator b = registry.resolve("opensky-states");
        assertThat(a.root()).isEqualTo(b.root());
        assertThat(registry.independent(a, b)).isFalse();
    }

    @Test
    @DisplayName("family patterns map runtime slots, the longest prefix winning; $source keeps the source id")
    void patterns() throws Exception {
        OriginatorRegistry registry = registry();
        assertThat(registry.resolve("cap-de-dwd").id()).isEqualTo("cap-de-dwd");
        assertThat(registry.resolve("cap-de-dwd").originatorClass()).isEqualTo("agency");
        assertThat(registry.resolve("local-adsb-3").originatorClass()).isEqualTo("operator");
        assertThat(registry.resolve("adsbfi-region-2").id()).isEqualTo("adsb-community");
        assertThat(registry.upstreamRefs("cap-fi-fmi").eventIds()).extracting(UpstreamRefs.EventIdField::namespace)
                .containsExactly("cap-identifier");
    }

    @Test
    @DisplayName("citations resolve by domain or id; unknown citations stay unresolved")
    void citations() throws Exception {
        OriginatorRegistry registry = registry();
        assertThat(registry.byCitation("https://earthquake.usgs.gov/earthquakes/eventpage/us7000"))
                .map(Originator::id).contains("usgs-anss");
        assertThat(registry.byCitation("GDACS")).map(Originator::id).contains("gdacs");
        assertThat(registry.byCitation("https://www.example-news.org/story")).isEmpty();
        assertThat(OriginatorRegistry.normalizeCitation("https://www.Example.org/a")).isEqualTo("example.org");
        assertThat(OriginatorRegistry.normalizeCitation("  ")).isNull();
    }

    @Test
    @DisplayName("upstream references parse field@namespace, $externalId and cite:")
    void upstreamRefs() throws Exception {
        OriginatorRegistry registry = registry();
        UpstreamRefs gdacs = registry.upstreamRefs("gdacs");
        assertThat(gdacs.eventIds()).extracting(UpstreamRefs.EventIdField::field).containsExactly("eventId", "glide");
        assertThat(registry.upstreamRefs("gdelt-events").citations()).containsExactly("sourceUrl");
        assertThat(registry.upstreamRefs("nws-alerts").eventIds().get(0).field()).isEqualTo(UpstreamRefs.EXTERNAL_ID);
        assertThat(UpstreamRefs.parse(List.of("id")).eventIds().get(0).namespace()).isEqualTo("id");
        assertThat(UpstreamRefs.parse(null).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("unknown sources are their own originator (class aggregator) and are listed for the startup warning")
    @SuppressWarnings("unchecked")
    void unknownSourcesWarned() throws Exception {
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(OriginatorRegistry.SETTING, Map.class)).thenReturn(Optional.empty());
        SourceRegistry sources = mock(SourceRegistry.class);
        SourceDescriptor known = SourceDescriptor.builder("usgs-earthquakes").kind("earthquake").build();
        SourceDescriptor unknown = SourceDescriptor.builder("mystery-feed").kind("thing").build();
        when(sources.descriptors()).thenReturn(List.of(known, unknown));
        when(sources.contains("mystery-feed")).thenReturn(true);
        when(sources.descriptor("mystery-feed")).thenReturn(unknown);
        ObjectProvider<SourceRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sources);
        OriginatorRegistry registry = new OriginatorRegistry(JSON, settings, provider);
        assertThat(registry.unknownSources()).containsExactly("mystery-feed");
        Originator fallback = registry.resolve("mystery-feed");
        assertThat(fallback.id()).isEqualTo("mystery-feed");
        assertThat(fallback.originatorClass()).isEqualTo("aggregator");
        registry.warnUnknown();
    }

    @Test
    @DisplayName("the intel.originators setting and a descriptor's originator override the bundled file")
    @SuppressWarnings("unchecked")
    void overrides() throws Exception {
        AppSettingService settings = mock(AppSettingService.class);
        Map<String, Object> override = Map.of("originators", List.of(Map.of("id", "my-network", "class",
                "sensor-network")), "sources", Map.of("mystery-feed", "my-network"));
        when(settings.get(OriginatorRegistry.SETTING, Map.class)).thenReturn(Optional.of(override));
        SourceRegistry sources = mock(SourceRegistry.class);
        SourceDescriptor declared = SourceDescriptor.builder("own-feed").kind("thing").originator("usgs-anss")
                .upstreamRefFields("code@usgs-event").build();
        when(sources.contains("own-feed")).thenReturn(true);
        when(sources.descriptor("own-feed")).thenReturn(declared);
        ObjectProvider<SourceRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sources);
        OriginatorRegistry registry = new OriginatorRegistry(JSON, settings, provider);
        assertThat(registry.resolve("mystery-feed").id()).isEqualTo("my-network");
        assertThat(registry.resolve("mystery-feed").originatorClass()).isEqualTo("sensor-network");
        assertThat(registry.resolve("own-feed").id()).isEqualTo("usgs-anss");
        assertThat(registry.upstreamRefs("own-feed").eventIds().get(0).namespace()).isEqualTo("usgs-event");
        assertThat(registry.resolve("usgs-earthquakes").id()).isEqualTo("usgs-anss");
    }
}
