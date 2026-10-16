package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * A recipe backtest reports the stored intelligence events that started in the requested range
 * (prompt 103): events outside the range, of another recipe, merged or private are left out.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class RecipeBacktestIT {

    private static final Instant T0 = Instant.parse("2026-02-01T00:00:00Z");

    @Autowired RecipeBacktest backtest;
    @Autowired IntelligenceEventRepository events;

    /** Saves an event of {@code recipe} starting at {@code start}. */
    static IntelligenceEventEntity event(IntelligenceEventRepository events, String type, String recipe,
            Instant start) {
        IntelligenceEventEntity event = new IntelligenceEventEntity(Ulids.next(start), type, type + " event", start);
        event.setRecipeId(recipe);
        event.setCreatedAt(start);
        event.setUpdatedAt(start);
        return events.save(event);
    }

    @Test
    @DisplayName("a recipe job returns only the events of the range and recipe")
    @SuppressWarnings("unchecked")
    void inRange() {
        event(events, "seismic", "r1", T0.plus(Duration.ofHours(1)));
        event(events, "seismic", "r1", T0.minus(Duration.ofDays(2)));
        event(events, "seismic", "r2", T0.plus(Duration.ofHours(2)));
        IntelligenceEventEntity hidden = event(events, "seismic", "r1", T0.plus(Duration.ofHours(3)));
        hidden.setPrivateEvent(true);
        events.save(hidden);
        IntelligenceEventEntity merged = event(events, "seismic", "r1", T0.plus(Duration.ofHours(4)));
        merged.setMergedInto(hidden.getId());
        events.save(merged);

        Map<String, Object> result = backtest.run("r1", T0, T0.plus(Duration.ofDays(1)), JobMonitor.NONE);
        assertThat(result).containsEntry("eventCount", 1L).containsEntry("replay", RecipeBacktest.REPLAY);
        assertThat((List<Map<String, Object>>) result.get("events")).hasSize(1)
                .allSatisfy(e -> assertThat(e).containsEntry("type", "seismic").containsKey("members"));

        Map<String, Object> every = backtest.run(null, T0, T0.plus(Duration.ofDays(1)), JobMonitor.NONE);
        assertThat(every).containsEntry("eventCount", 2L);
    }
}
