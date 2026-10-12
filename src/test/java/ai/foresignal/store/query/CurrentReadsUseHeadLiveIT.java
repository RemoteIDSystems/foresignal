package ai.foresignal.store.query;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Invariant HIST-2 (Core I5): current-state reads touch only {@code record_head ⋈ record_live},
 * and {@code record_live.observed_at} has no index, so live touches stay HOT updates.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class CurrentReadsUseHeadLiveIT {

    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("the current-state FROM clause joins record_head and record_live only")
    void currentFromClause() {
        assertThat(RecordQueryService.CURRENT_FROM)
                .contains("record_head h JOIN record_live l")
                .doesNotContain("record_version");
        assertThat(RecordQueryService.CURRENT_PAYLOAD).isEqualTo("jsonb_strip_nulls(h.payload || l.kinematic)");
    }

    @Test
    @DisplayName("no index on record_live covers observed_at")
    void noObservedAtIndex() {
        List<String> indexes = jdbc.queryForList(
                "SELECT indexdef FROM pg_indexes WHERE tablename = 'record_live'", String.class);
        assertThat(indexes).isNotEmpty();
        assertThat(indexes).noneMatch(definition -> definition.contains("observed_at"));
    }

    @Test
    @DisplayName("the planner reads only record_head and record_live for a current listing")
    void plannerTouchesOnlyHeadAndLive() {
        List<String> plan = jdbc.queryForList(
                "EXPLAIN SELECT h.source_id" + RecordQueryService.CURRENT_FROM
                        + " WHERE NOT h.tombstone ORDER BY l.observed_at DESC LIMIT 10",
                String.class);
        String text = String.join("\n", plan);
        assertThat(text).contains("record_head").contains("record_live").doesNotContain("record_version");
    }
}
