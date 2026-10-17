package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.benchmark.BenchmarkGate;
import ai.foresignal.test.MutableClock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * The intel board (prompt 128 §6): at most 8 KB however large the inputs, in the documented truncation order (texts
 * first, then the oldest indicators, band changes, due questions and lower situations, then benchmarks and headlines);
 * a stable ETag while nothing changes; private objects excluded for callers without private access.
 */
class IntelBoardTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    static List<Map<String, Object>> many(String kind, int n, int textLength) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put(kind + "Id", "01JA8Q3V7S2K9W4T6Y1B5N0M" + String.format("%02d", i % 100));
            m.put("title", "x".repeat(textLength));
            m.put("at", NOW.toString());
            out.add(m);
        }
        return out;
    }

    static IntelBoardBuilder.Inputs inputs(int n, int text) {
        List<Map<String, Object>> latest = List.of(Map.of("cadence", "daily", "assessmentId", "A1", "headline",
                "h".repeat(text), "createdAt", NOW.toString()));
        List<Map<String, Object>> bench = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            bench.add(new LinkedHashMap<>(Map.of("component", "component-" + i, "status", i % 3 == 0 ? "fail" : "pass")));
        }
        return new IntelBoardBuilder.Inputs(NOW, many("situation", 10, text), many("band", n, text),
                many("indicator", n, text), many("question", n, text), latest,
                Map.of("window", "P90D"), bench);
    }

    @Test
    @DisplayName("a small board is untouched")
    void small() {
        Map<String, Object> board = IntelBoardBuilder.build(inputs(2, 20), JSON);
        assertThat((List<?>) board.get("truncated")).isEmpty();
        assertThat((List<?>) board.get("situations")).hasSize(IntelBoardBuilder.MAX_SITUATIONS);
        assertThat((List<?>) board.get("bandChanges")).hasSize(2);
        assertThat(IntelBoardBuilder.size(board, JSON)).isLessThanOrEqualTo(IntelBoardBuilder.MAX_BYTES);
    }

    @Test
    @DisplayName("large inputs stay within 8 KB: texts cut first, then the oldest indicators, band changes and dues")
    @SuppressWarnings("unchecked")
    void large() {
        Map<String, Object> board = IntelBoardBuilder.build(inputs(200, 400), JSON);
        assertThat(IntelBoardBuilder.size(board, JSON)).isLessThanOrEqualTo(IntelBoardBuilder.MAX_BYTES);
        List<String> truncated = (List<String>) board.get("truncated");
        assertThat(truncated).contains("situations", "indicatorsObserved");
        assertThat(truncated.indexOf("situations")).isLessThan(truncated.indexOf("indicatorsObserved"));
        List<Map<String, Object>> situations = (List<Map<String, Object>>) board.get("situations");
        assertThat(situations).isNotEmpty();
        assertThat(String.valueOf(situations.get(0).get("title"))).hasSizeLessThanOrEqualTo(120);
        assertThat(String.valueOf(situations.get(0).get("situationId"))).hasSize(26);
        List<Map<String, Object>> indicators = (List<Map<String, Object>>) board.get("indicatorsObserved");
        List<Map<String, Object>> due = (List<Map<String, Object>>) board.get("forecastsDue");
        assertThat(indicators.size()).isLessThanOrEqualTo(due.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("extreme inputs fall back to emptying lists, benchmarks and headlines, and still fit")
    void extreme() {
        Map<String, Object> board = IntelBoardBuilder.build(new IntelBoardBuilder.Inputs(NOW,
                many("situation", 5, 3000), List.of(), List.of(), List.of(), List.of(Map.of("cadence", "daily",
                        "assessmentId", "A1", "headline", "h".repeat(9000), "createdAt", NOW.toString())),
                null, List.of(new LinkedHashMap<>(Map.of("component", "c", "status", "pass")))), JSON);
        assertThat(IntelBoardBuilder.size(board, JSON)).isLessThanOrEqualTo(IntelBoardBuilder.MAX_BYTES);
        assertThat((List<Object>) board.get("truncated")).contains("latestAssessments");
    }

    @SuppressWarnings("unchecked")
    static IntelBoardService service(NamedParameterJdbcTemplate jdbc, MutableClock clock) {
        CycleStateReader state = mock(CycleStateReader.class);
        when(state.scorecard(any())).thenReturn(Map.of("window", "P90D"));
        ObjectProvider<BenchmarkGate> gates = mock(ObjectProvider.class);
        when(gates.getIfAvailable()).thenReturn(null);
        return new IntelBoardService(jdbc, state, gates, JSON, clock);
    }

    @Test
    @DisplayName("the ETag is stable while nothing changes, even as generatedAt moves")
    void etag() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        MutableClock clock = new MutableClock(NOW);
        IntelBoardService service = service(jdbc, clock);
        IntelBoardService.Board first = service.board(false);
        assertThat(service.board(false)).isSameAs(first);
        clock.setInstant(NOW.plusSeconds(IntelBoardService.CACHE_SECONDS + 1));
        IntelBoardService.Board second = service.board(false);
        assertThat(second.body()).isNotEqualTo(first.body());
        assertThat(second.etag()).isEqualTo(first.etag());
        service.clearCache();
        assertThat(service.board(false).etag()).isEqualTo(first.etag());
    }

    @Test
    @DisplayName("callers without private access query without private objects; private callers with them")
    void privateExclusion() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        IntelBoardService service = service(jdbc, new MutableClock(NOW));
        service.build(false, NOW);
        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc, atLeastOnce()).query(anyString(), params.capture(), any(RowCallbackHandler.class));
        assertThat(params.getAllValues()).allSatisfy(p -> assertThat(((MapSqlParameterSource) p).getValue("private"))
                .isEqualTo(false));
        NamedParameterJdbcTemplate jdbc2 = mock(NamedParameterJdbcTemplate.class);
        service(jdbc2, new MutableClock(NOW)).build(true, NOW);
        ArgumentCaptor<SqlParameterSource> params2 = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc2, atLeastOnce()).query(anyString(), params2.capture(), any(RowCallbackHandler.class));
        assertThat(params2.getAllValues()).allSatisfy(p -> assertThat(((MapSqlParameterSource) p).getValue("private"))
                .isEqualTo(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("the calibration block carries the ensemble and the operator only when present")
    void calibration() {
        Map<String, Object> scorecard = new LinkedHashMap<>();
        scorecard.put("ensemble", Map.of("brierSkill", 0.12, "calibration", "well calibrated over 40 resolved",
                "n", 40));
        scorecard.put("operator", null);
        Map<String, Object> c = IntelBoardService.calibration(scorecard);
        assertThat(c).containsEntry("window", "P90D").containsEntry("operator", null);
        assertThat((Map<String, Object>) c.get("ensemble")).containsEntry("n", 40);
    }
}
