package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.ChunkedTestAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Chunked fetch and back-pressure (prompt 094): 12,000 records arrive as three chunks that share one
 * {@code run_id}, and each chunk's row is committed before the adapter is allowed to emit the next.
 */
@IntegrationTest
@Import({ChunkedFetchIT.Config.class, MutableClock.TestConfig.class})
class ChunkedFetchIT extends WireMockSupport {

    private static final String ID = ChunkedTestAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    /** Registers the chunked adapter. */
    @TestConfiguration
    static class Config {
        @Bean
        ChunkedTestAdapter chunkedTestAdapter() {
            return new ChunkedTestAdapter();
        }
    }

    @Autowired private ChunkedTestAdapter adapter;
    @Autowired private FetchExecutor executor;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        adapter.set(List.of());
    }

    @Test
    @DisplayName("12,000 records ingest as three chunks sharing one run_id")
    void threeChunksOneRun() {
        List<NormalizedRecord> records = new ArrayList<>(12_000);
        for (int i = 0; i < 12_000; i++) {
            records.add(NormalizedRecord.builder("e" + i, ChunkedTestAdapter.KIND).put("n", i).build());
        }
        adapter.set(records);
        List<Integer> rowsAfterChunk = Collections.synchronizedList(new ArrayList<>());
        adapter.afterChunk(index -> rowsAfterChunk.add(fetchRows()));

        FetchOutcome outcome = executor.execute(ID, FetchTrigger.MANUAL);

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.recordsSeen()).isEqualTo(12_000);

        List<String> runIds = jdbc.queryForList(
                "select run_id from fetch_log where source_id = ? and run_id is not null", String.class, ID);
        assertThat(runIds).hasSize(3).allMatch(runIds.get(0)::equals);

        Integer versions = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ?", Integer.class, ID);
        assertThat(versions).isEqualTo(12_000);

        // Back-pressure: the first chunk is committed before the adapter emits the second.
        assertThat(rowsAfterChunk).containsExactly(1, 2, 3);
    }

    private int fetchRows() {
        Integer count = jdbc.queryForObject(
                "select count(*) from fetch_log where source_id = ?", Integer.class, ID);
        return count == null ? 0 : count;
    }
}
