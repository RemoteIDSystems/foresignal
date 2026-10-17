package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.ObservationFacts;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-corroboration} payloads (prompt 099, produced from canonical events since 116): credibility and
 * independence come from evidence groups, never from the number of records or sources.
 */
@SuppressWarnings("unchecked")
class CorroborationTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");

    private static EvidenceStore.StoredEvent event(int groups, List<String> flags) {
        return new EvidenceStore.StoredEvent("01JAAAAAAAAAAAAAAAAAAAAAAA", "seismic", T0, -95.0, 32.0, List.of("US"),
                5.0, T0, "record:usgs-earthquakes/a@1", 3, groups, flags, List.of("usgs-earthquakes", "gdacs",
                        "emsc-seismic"), "open", null, "1.0.0", T0, T0);
    }

    private static EvidenceStore.StoredLink link(String source, String id, String group) {
        ObservationFacts facts = new ObservationFacts(source, id, 1, 1, T0, T0, -95.0, 32.0, 5.0, null, null, null,
                null, null, List.of(), null);
        return new EvidenceStore.StoredLink("L" + id, "01JAAAAAAAAAAAAAAAAAAAAAAA", facts, source, source, null, group,
                1.0, Map.of("rule", "proximity"), false, T0);
    }

    @Test
    @DisplayName("USGS, GDACS (which republishes USGS) and EMSC are two evidence groups: credibility 2, two sources")
    void groupsDecideCredibility() {
        List<EvidenceStore.StoredLink> links = List.of(link("usgs-earthquakes", "a", "G1"), link("gdacs", "b", "G1"),
                link("emsc-seismic", "c", "G2"));
        List<EvidenceStore.StoredGroup> groups = List.of(new EvidenceStore.StoredGroup("G1", "E", "usgs", 2, 1),
                new EvidenceStore.StoredGroup("G2", "E", "emsc", 1, 1));
        Map<String, Object> payload = CorroborationGenerator.payload(event(2, List.of()), links, groups, s -> true);
        assertThat(payload).containsEntry("credibility", 2).containsEntry("evidenceGroups", 2)
                .containsEntry("canonicalEventId", "01JAAAAAAAAAAAAAAAAAAAAAAA").containsEntry("reliability", "F");
        assertThat((List<Object>) payload.get("independentSources")).containsExactly("usgs-earthquakes", "emsc-seismic");
        assertThat((List<Object>) payload.get("independentOriginators")).containsExactly("usgs", "emsc");
        assertThat((List<?>) payload.get("members")).hasSize(3);
    }

    @Test
    @DisplayName("re-reports of one originator are one group: credibility 3 however many records there are")
    void echoesNeverRaiseCredibility() {
        List<EvidenceStore.StoredLink> links = List.of(link("gdelt-events", "a", "G1"), link("gdelt-events", "b", "G1"),
                link("gdelt-events", "c", "G1"), link("gdelt-events", "d", "G1"), link("gdelt-events", "e", "G1"));
        List<EvidenceStore.StoredGroup> groups = List.of(new EvidenceStore.StoredGroup("G1", "E", "cite:example.org", 5, 1));
        Map<String, Object> payload = CorroborationGenerator.payload(event(1, List.of("ECHO", "SINGLE_ORIGINATOR")),
                links, groups, s -> true);
        assertThat(payload).containsEntry("credibility", 3).containsEntry("evidenceGroups", 1);
        assertThat((List<Object>) payload.get("independentSources")).containsExactly("gdelt-events");
        assertThat((List<Object>) payload.get("qualityFlags")).containsExactly("ECHO", "SINGLE_ORIGINATOR");
    }

    @Test
    @DisplayName("members from sources that may not be served are left out; none left means no record")
    void unservedSourcesAreLeftOut() {
        List<EvidenceStore.StoredLink> links = List.of(link("local-ais-1", "a", "G1"), link("usgs-earthquakes", "b", "G2"));
        List<EvidenceStore.StoredGroup> groups = List.of(new EvidenceStore.StoredGroup("G1", "E", "local-ais-1", 1, 1),
                new EvidenceStore.StoredGroup("G2", "E", "usgs", 1, 1));
        Map<String, Object> payload = CorroborationGenerator.payload(event(2, List.of()), links, groups,
                s -> !s.startsWith("local-"));
        assertThat((List<?>) payload.get("members")).hasSize(1);
        assertThat(CorroborationGenerator.payload(event(1, List.of()), List.of(link("local-ais-1", "a", "G1")), groups,
                s -> false)).isNull();
    }

    @Test
    @DisplayName("canonical families map to 099's record id families")
    void families() {
        assertThat(CorroborationGenerator.Family.ofCanonical("seismic")).isEqualTo(CorroborationGenerator.Family.EARTHQUAKE);
        assertThat(CorroborationGenerator.Family.ofCanonical("outage").id).isEqualTo("outage");
        assertThat(CorroborationGenerator.Family.ofCanonical("weather-alert")).isNull();
    }
}
