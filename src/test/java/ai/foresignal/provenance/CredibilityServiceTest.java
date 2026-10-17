package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link CredibilityService} (prompt 109, task 5) over corroboration and deception payloads in the exact shape 099's
 * generators emit ({@code members[]}, {@code independentSources} as the list of distinct source ids,
 * {@code credibility}): every grade rule in order, the {@code grade} composition and the operator's reliability
 * override.
 */
class CredibilityServiceTest {

    CredibilityService service;
    SourceService sources;

    static SourceDescriptor source(String id, String reliability) {
        return SourceDescriptor.builder(id).name(id).kind("event").mode(Mode.PUSH).reliability(reliability).build();
    }

    @BeforeEach
    void setUp() {
        Map<String, SourceDescriptor> all = Map.of(
                "usgs", source("usgs", "B"),
                "emsc", source("emsc", "C"),
                "gdacs", source("gdacs", "C"),
                "gdelt", source("gdelt", "D"),
                "ais-a", source("ais-a", "C"),
                "ais-b", source("ais-b", "C"));
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
        when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));
        when(registry.find(any())).thenAnswer(i -> Optional.ofNullable(all.get(i.<String>getArgument(0)))
                .map(d -> new SourceRegistry.Registration(d, null, false)));
        service = new CredibilityService(null, registry);
        sources = mock(SourceService.class);
        when(sources.effectiveReliability(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)).reliability());
        @SuppressWarnings("unchecked")
        ObjectProvider<SourceService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sources);
        ReflectionTestUtils.setField(service, "sources", provider);
    }

    static RecordRow row(String sourceId, String externalId, String kind, int qaFlags) {
        return new RecordRow(sourceId, externalId, kind, 1, 1L, null, null, null, null, null, false, null, null,
                null, null, null, null, qaFlags, null, null, null, null, null, null, null, null, null, null);
    }

    /** A corroboration payload in 099's shape ({@code CorroborationGenerator.payload} keeps these members). */
    static String corroboration(int credibility, List<String> independent, String... members) {
        StringBuilder list = new StringBuilder();
        for (String member : members) {
            String[] parts = member.split("\\|");
            if (list.length() > 0) {
                list.append(',');
            }
            list.append("{\"sourceId\":\"").append(parts[0]).append("\",\"externalId\":\"").append(parts[1])
                    .append("\",\"observedAt\":\"2026-03-01T00:00:00Z\"}");
        }
        StringBuilder ids = new StringBuilder();
        for (String id : independent) {
            if (ids.length() > 0) {
                ids.append(',');
            }
            ids.append('"').append(id).append('"');
        }
        return "{\"members\":[" + list + "],\"independentSources\":[" + ids + "],\"credibility\":" + credibility
                + ",\"reliability\":\"F\"}";
    }

    /** A deception payload as {@code DeceptionGenerator.emit} writes it. */
    static String deception(String sourceId, String externalId) {
        return "{\"checks\":[{\"check\":\"implied-speed\",\"count\":1}],\"members\":[{\"sourceId\":\"" + sourceId
                + "\",\"externalId\":\"" + externalId + "\"}]}";
    }

    @Test
    @DisplayName("5: a member of an open deception record, and any aircraft or vessel record of the named entity")
    void deceptionIsFive() {
        CredibilityService.Lookup lookup = service.lookupOf(
                List.of(corroboration(2, List.of("ais-a", "ais-b"), "ais-a|123", "ais-b|123")),
                List.of(deception("ais-a", "123")), List.of("123"));
        assertThat(service.assess(row("ais-a", "123", "vessel", 0), lookup)).as("member").isEqualTo(5);
        assertThat(service.assess(row("ais-b", "123", "vessel", 0), lookup)).as("same MMSI, other source")
                .isEqualTo(5);
        assertThat(service.assess(row("usgs", "123", "earthquake", 0), lookup)).as("not an entity kind")
                .isNotEqualTo(5);
    }

    @Test
    @DisplayName("1: a cluster with three independent sources, one graded A or B")
    void confirmedIsOne() {
        CredibilityService.Lookup lookup = service.lookupOf(
                List.of(corroboration(2, List.of("usgs", "emsc", "gdacs"), "usgs|q1", "emsc|e1", "gdacs|g1")),
                List.of(), List.of());
        assertThat(service.assess(row("emsc", "e1", "earthquake", 0), lookup)).isEqualTo(1);
        assertThat(service.assess(row("usgs", "q1", "earthquake", 0), lookup)).isEqualTo(1);
    }

    @Test
    @DisplayName("independentSources is the list 099 emits: its size counts, not the member count")
    void independentSourcesIsAList() {
        // Four members from two sources: the list says two, so this is not a three-source confirmation.
        CredibilityService.Lookup lookup = service.lookupOf(
                List.of(corroboration(2, List.of("usgs", "emsc"), "usgs|q1", "usgs|q2", "emsc|e1", "emsc|e2")),
                List.of(), List.of());
        assertThat(service.assess(row("emsc", "e1", "earthquake", 0), lookup)).isEqualTo(2);
        assertThat(CredibilityService.independentSources(Map.of("independentSources", List.of("a", "b", "c")),
                List.of())).isEqualTo(3);
        assertThat(CredibilityService.independentSources(Map.of("independentSources", 4), List.of())).isEqualTo(4);
        assertThat(CredibilityService.independentSources(Map.of(), List.of(Map.of("sourceId", "a"),
                Map.of("sourceId", "a"), Map.of("sourceId", "b")))).isEqualTo(2);
    }

    @Test
    @DisplayName("three independent sources without an A or B source keep the cluster's own credibility")
    void threeWithoutHighReliability() {
        CredibilityService.Lookup lookup = service.lookupOf(
                List.of(corroboration(2, List.of("emsc", "gdacs", "gdelt"), "emsc|e1", "gdacs|g1", "gdelt|d1")),
                List.of(), List.of());
        assertThat(service.assess(row("gdelt", "d1", "event", 0), lookup)).isEqualTo(2);
    }

    @Test
    @DisplayName("3 for an A/B source without QA flags; 6 otherwise")
    void reliabilityFallbacks() {
        CredibilityService.Lookup none = service.lookupOf(List.of(), List.of(), List.of());
        assertThat(service.assess(row("usgs", "q9", "earthquake", 0), none)).isEqualTo(3);
        assertThat(service.assess(row("usgs", "q9", "earthquake", 4), none)).as("QA flags").isEqualTo(6);
        assertThat(service.assess(row("emsc", "e9", "earthquake", 0), none)).as("graded C").isEqualTo(6);
        assertThat(service.assess(row("unregistered", "x", "event", 0), none)).isEqualTo(6);
    }

    @Test
    @DisplayName("grade composes reliability and credibility; the operator override changes the reliability")
    void gradeAndOverride() {
        assertThat(service.grade("B", 2)).isEqualTo("B2");
        assertThat(service.reliability("emsc")).isEqualTo("C");
        when(sources.effectiveReliability("emsc")).thenReturn("A");
        assertThat(service.reliability("emsc")).isEqualTo("A");
        CredibilityService.Lookup none = service.lookupOf(List.of(), List.of(), List.of());
        assertThat(service.assess(row("emsc", "e9", "earthquake", 0), none)).isEqualTo(3);
    }
}
