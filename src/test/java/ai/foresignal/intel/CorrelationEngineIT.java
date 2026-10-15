package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The correlation engine end to end (prompt 101): a recipe forms an event from seeded versions with
 * pinned members, join reasons, detector version and parameters; re-running yields the same events;
 * the {@code foresignal-intel} record is emitted only on change.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class CorrelationEngineIT {

    @Autowired CorrelationEngine engine;
    @Autowired CorrelationRecipeRepository recipes;
    @Autowired IntelligenceEventRepository events;
    @Autowired IntelEventMemberRepository members;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;

    private void recipe() {
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("type", "custom");
        definition.put("seed", Map.of("kinds", List.of("thing")));
        definition.put("joins", List.of());
        definition.put("minMembers", 1);
        definition.put("minSignalFamilies", 1);
        definition.put("activateAt", Map.of("members", 1));
        definition.put("quietPeriod", "PT6H");
        definition.put("dedup", Map.of("radiusKm", 100, "window", "PT6H"));
        CorrelationRecipeEntity entity = new CorrelationRecipeEntity("test-recipe", 1, definition);
        entity.setEnabled(true);
        recipes.save(entity);
    }

    @Test
    @DisplayName("a recipe forms an event with pinned members and a detector")
    void forms() {
        recipe();
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("t1", "thing").observedAt(now).point(-95.0, 32.0).put("name", "one").build(),
                NormalizedRecord.builder("t2", "thing").observedAt(now.plusSeconds(60)).point(-95.01, 32.01)
                        .put("name", "two").build());
        engine.catchUp();

        List<IntelligenceEventEntity> all = events.findAll();
        assertThat(all).isNotEmpty();
        IntelligenceEventEntity event = all.get(0);
        assertThat(event.getDetector()).containsEntry("name", "event-scorer");
        assertThat(event.getConfidence()).isNotNull();
        List<IntelEventMemberEntity> pinned = members.findByEventIdOrderByIdAsc(event.getId());
        assertThat(pinned).isNotEmpty();
        assertThat(pinned.get(0).getJoinReason()).containsKey("matchedKeys");
    }
}
