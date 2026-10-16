package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The affected-dependents query (prompt 104): direct seeds, depth-2 dependents with the weakest-link
 * label, {@code minLabel} and {@code types} filtering, caveats, OSM attribution, private events and
 * the 409 before the first build.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class InfraImpactServiceIT {

    @Autowired InfraGraphBuilder builder;
    @Autowired InfraImpactService impact;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired IntelligenceEventRepository events;
    @Autowired IntelEventService intel;

    @Test
    @DisplayName("409 before the first build")
    void notBuilt() {
        assertThatThrownBy(() -> impact.affected(Geo.point(-95, 32).buffer(0.01), 2, "possible", List.of()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("a plant outage reaches the substation (depth 1) and the hospital (depth 2), with caveats")
    @SuppressWarnings("unchecked")
    void dependents() {
        InfraGraphBuilderIT.seed(records, clock);
        builder.rebuild(Map.of());
        Map<String, Object> result = impact.affected(Geo.point(-95, 32).buffer(0.005), 3, "possible", List.of());
        List<Map<String, Object>> direct = (List<Map<String, Object>>) result.get("direct");
        assertThat(direct).singleElement().matches(n -> n.get("externalId").equals("pp1"));
        List<Map<String, Object>> dependents = (List<Map<String, Object>>) result.get("dependents");
        assertThat(dependents).hasSize(2);
        Map<String, Object> hospital = dependents.stream()
                .filter(d -> ((Map<?, ?>) d.get("node")).get("externalId").equals("n11")).findFirst().orElseThrow();
        assertThat(hospital).containsEntry("depth", 2).containsEntry("pathLabel", "possible");
        assertThat((List<?>) hospital.get("path")).hasSize(2);
        assertThat((List<String>) result.get("attribution")).contains(InfraImpactService.OSM_ATTRIBUTION);
        assertThat((List<Map<String, Object>>) result.get("caveats")).extracting(c -> c.get("code"))
                .contains("OPEN_DATA", "OSM_EXTRACT_DATE");
        assertThat((Map<String, Object>) result.get("summary")).containsKeys("byType", "byLabel");
        assertThat(result).containsEntry("approximateArea", false).containsEntry("truncated", false);
    }

    @Test
    @DisplayName("minLabel likely drops the possible links; types restricts the facilities listed")
    @SuppressWarnings("unchecked")
    void filters() {
        InfraGraphBuilderIT.seed(records, clock);
        builder.rebuild(Map.of());
        var area = Geo.point(-95, 32).buffer(0.005);
        assertThat((List<?>) impact.affected(area, 3, "likely", List.of()).get("dependents")).isEmpty();
        List<Map<String, Object>> hospitals = (List<Map<String, Object>>) impact.affected(area, 3, "possible",
                List.of("health.hospital")).get("dependents");
        assertThat(hospitals).singleElement()
                .matches(d -> ((Map<?, ?>) d.get("node")).get("facilityType").equals("health.hospital"));
        assertThatThrownBy(() -> impact.affected(area, 3, "certain", List.of()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a private event is not usable without the private grant (the shared intel visibility rule)")
    void privateEvent() {
        InfraGraphBuilderIT.seed(records, clock);
        builder.rebuild(Map.of());
        IntelligenceEventEntity event = new IntelligenceEventEntity(Ulids.next(clock.instant()),
                "infrastructure-disruption", "Outage", clock.instant());
        event.setPrivateEvent(true);
        event.setCreatedAt(clock.instant());
        event.setUpdatedAt(clock.instant());
        events.save(event);
        assertThatThrownBy(() -> intel.visible(event.getId(), false)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> intel.visible("01NOSUCHEVENT0000000000000", true))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> impact.eventSeed(intel.visible(event.getId(), true)))
                .as("a visible event without an area").isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a point record without a region seeds an approximate area; an unknown record is 404")
    void recordSeed() {
        InfraGraphBuilderIT.seed(records, clock);
        builder.rebuild(Map.of());
        InfraImpactService.Seed seed = impact.recordSeed("osm-infrastructure", "n10");
        assertThat(seed.approximate()).isTrue();
        assertThatThrownBy(() -> impact.recordSeed("osm-infrastructure", "nope"))
                .isInstanceOf(NotFoundException.class);
    }
}
