package ai.foresignal.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.intel.EventScorer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * EVD-1 (prompt 116): duplicates never inflate independence. Re-reports of one originator form one evidence
 * group, so the number of groups, the scorer's confidence and evidence quality do not grow with the number of
 * re-reports.
 */
class DuplicateInflationTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("n re-reports of one originator are one evidence group for every n")
    void groupsDoNotGrowWithReReports() throws Exception {
        OriginatorRegistry registry = OriginatorRegistry.of(OriginatorRegistryTest.bundled(), JSON);
        for (int n = 1; n <= 20; n++) {
            List<EvidenceGrouper.Member> members = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                members.add(new EvidenceGrouper.Member("m" + i, "usgs-earthquakes", registry.resolve("usgs-earthquakes"),
                        null, i));
            }
            assertThat(EvidenceGrouper.group(members, registry)).as("n = " + n).hasSize(1);
        }
    }

    @Test
    @DisplayName("aggregators that republish an originator add no group, however many of them report")
    void republishersAddNothing() throws Exception {
        OriginatorRegistry registry = OriginatorRegistry.of(OriginatorRegistryTest.bundled(), JSON);
        List<EvidenceGrouper.Member> members = new ArrayList<>();
        members.add(new EvidenceGrouper.Member("1", "usgs-earthquakes", registry.resolve("usgs-earthquakes"), null, 1));
        members.add(new EvidenceGrouper.Member("2", "gdacs", registry.resolve("gdacs"), null, 2));
        members.add(new EvidenceGrouper.Member("3", "gdelt-events", registry.resolve("gdelt-events"),
                "earthquake.usgs.gov", 3));
        assertThat(EvidenceGrouper.group(members, registry)).hasSize(1);
    }

    @Test
    @DisplayName("the scorer's confidence is the same for one report and for twenty echoes of it")
    void scoreDoesNotGrow() {
        double single = EventScorer.score(List.of(new EventScorer.MemberView("seismic", 0.7, "usgs", true, null,
                "group:G", "usgs")), 0).confidence();
        List<EventScorer.MemberView> echoes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            echoes.add(new EventScorer.MemberView("seismic", 0.7, "usgs", true, null, "group:G", "usgs"));
        }
        assertThat(EventScorer.score(echoes, 0).confidence()).isEqualTo(single);
    }
}
