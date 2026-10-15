package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** A recipe backtest summarises the stored intelligence events in range (prompt 103). */
@WebIntegrationTest
@Import(TestIngestSources.class)
class RecipeBacktestIT {

    @Autowired RecipeBacktest backtest;

    @Test
    @DisplayName("a recipe job returns counts by type")
    void counts() {
        var result = backtest.run(0, Long.MAX_VALUE);
        assertThat(result).containsKeys("countsByType", "events", "fromSeq", "toSeq");
    }
}
