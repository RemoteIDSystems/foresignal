package ai.foresignal.store.query;

import static ai.foresignal.testsupport.stats.StatsTestSources.OTHER_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code count=true} on {@code /v1/records}, {@code /v1/history} and {@code /v1/search} (prompt 038):
 * {@code {count, exact, watermarkSeq}} and no items, exact up to the cap and {@code exact: false}
 * above it (the cap is lowered through the package-private hooks, so no million rows are seeded).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(StatsTestSources.class)
class CountModeIT extends StatsItSupport {

    @Autowired MockMvc mvc;
    @Autowired RecordQueryService queries;
    @Autowired TokenService tokens;

    private void seedThree() {
        seed(POINTS, T0.plusSeconds(60), point("a", POINT_KIND, 10, 50, T0.plusSeconds(60), 1),
                point("b", POINT_KIND, 11, 51, T0.plusSeconds(60), 1), point("c", OTHER_KIND, 12, 52,
                        T0.plusSeconds(60), 1));
        seed(POINTS, T0.plusSeconds(120), point("a", POINT_KIND, 10, 50, T0.plusSeconds(120), 2));
    }

    private static CurrentQuery current(List<String> kinds) {
        return new CurrentQuery(List.of(POINTS), kinds, List.of(), null, null, null, null, TimeField.EFFECTIVE, null,
                true, ListOrder.OBSERVED, null, null, 100, null, null, GeometryEncoding.GEOJSON, false, Set.of());
    }

    private static HistoryQuery history() {
        return new HistoryQuery(List.of(POINTS), List.of(), List.of(), null, null, T0, null, TimeField.OBSERVED, null,
                100, null, null, GeometryEncoding.GEOJSON, false, Set.of());
    }

    @Test
    @DisplayName("records and history count exactly, filters applied, with the watermark")
    void exactCounts() throws Exception {
        seedThree();

        assertThat(queries.countCurrent(current(List.of()))).satisfies(r -> {
            assertThat(r.count()).isEqualTo(3);
            assertThat(r.exact()).isTrue();
            assertThat(r.watermarkSeq()).isEqualTo(changeBus.lastPublishedSeq());
        });
        assertThat(queries.countCurrent(current(List.of(OTHER_KIND))).count()).isEqualTo(1);
        assertThat(queries.countHistory(history()).count()).isEqualTo(4);

        mvc.perform(get("/v1/records").param("source", POINTS).param("count", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.exact").value(true))
                .andExpect(jsonPath("$.watermarkSeq").isNumber())
                .andExpect(jsonPath("$.items").doesNotExist());
        mvc.perform(get("/v1/history").param("source", POINTS).param("since", T0.toString()).param("count", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(4))
                .andExpect(jsonPath("$.exact").value(true));
        mvc.perform(get("/v1/records").param("source", POINTS).param("count", "true")
                        .param("asOf", T0.plus(Duration.ofMinutes(1)).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("above the cap the count is the cap and exact is false")
    void aboveTheCap() {
        seedThree();

        CountResult current = queries.countCurrent(current(List.of()), 2);
        assertThat(current.count()).isEqualTo(2);
        assertThat(current.exact()).isFalse();
        CountResult history = queries.countHistory(history(), 3);
        assertThat(history.count()).isEqualTo(3);
        assertThat(history.exact()).isFalse();
        assertThat(CountResult.of(1_000_001, 7)).isEqualTo(new CountResult(CountResult.CAP, false, 7));
        assertThat(CountResult.of(1_000_000, 7).exact()).isTrue();
        assertThat(CountResult.sql("SELECT 1 FROM t")).endsWith("LIMIT 1000001) c");
    }

    @Test
    @DisplayName("search counts text matches; mode=semantic is refused")
    void search() throws Exception {
        seedThree();
        String reader = TestTokens.bearer(TestTokens.create(tokens, "count-reader", Scope.READ));

        mvc.perform(get("/v1/search").header("Authorization", reader).param("q", "Name").param("mode", "text")
                        .param("count", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exact").value(true))
                .andExpect(jsonPath("$.watermarkSeq").isNumber())
                .andExpect(jsonPath("$.items").doesNotExist());
        mvc.perform(get("/v1/search").header("Authorization", reader).param("q", "Name").param("mode", "semantic")
                        .param("count", "true"))
                .andExpect(status().isBadRequest());
    }
}
