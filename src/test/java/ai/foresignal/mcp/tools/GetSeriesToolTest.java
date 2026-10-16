package ai.foresignal.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.SeriesQueryService;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_series} (prompt 093, EGR-1): every read carries the caller's excluded sources and restriction, so a
 * private or LLM-excluded source never reaches the tool's output.
 */
class GetSeriesToolTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final SeriesQueryService series = mock(SeriesQueryService.class);
    private final GetSeriesTool tool = new GetSeriesTool(series, new McpOutputShaper(JSON, mock(SourceRegistry.class)));

    private static ToolContext context(Set<String> excluded, ReadRestriction restriction) {
        return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true, restriction), excluded,
                "127.0.0.1"), "s", "t");
    }

    @Test
    @DisplayName("the catalog is read with the caller's excluded sources and restriction")
    void catalogCarriesVisibility() {
        ReadRestriction restriction = new ReadRestriction(List.of("eia-930"), null, null, null, null, false, null);
        when(series.list(any(), any(), any(), anyInt(), any()))
                .thenReturn(new SeriesQueryService.Catalogue(List.of(), null));
        tool.call(context(Set.of("eia-petroleum"), restriction), JSON.readTree("{}"));
        ArgumentCaptor<SeriesQueryService.Visibility> captor = ArgumentCaptor.forClass(SeriesQueryService.Visibility.class);
        verify(series).list(eq(List.of()), isNull(), isNull(), eq(GetSeriesTool.DEFAULT_LIMIT), captor.capture());
        assertThat(captor.getValue().excluded()).containsExactly("eia-petroleum");
        assertThat(captor.getValue().restriction()).isSameAs(restriction);
        assertThat(captor.getValue().allows("eia-petroleum")).isFalse();
        assertThat(captor.getValue().allows("gie-agsi")).as("outside the restriction's sources").isFalse();
        assertThat(captor.getValue().allows("eia-930")).isTrue();
    }

    @Test
    @DisplayName("a series of a source the caller may not read is reported as missing")
    void hiddenSeriesIsMissing() {
        when(series.info(eq("eia-petroleum"), eq("eia:RWTC"), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tool.call(context(Set.of("eia-petroleum"), ReadRestriction.NONE),
                JSON.readTree("{\"source\":\"eia-petroleum\",\"series\":\"eia:RWTC\"}")))
                .isInstanceOf(McpToolException.class).hasMessageContaining("No series");
        ArgumentCaptor<SeriesQueryService.Visibility> captor = ArgumentCaptor.forClass(SeriesQueryService.Visibility.class);
        verify(series).info(eq("eia-petroleum"), eq("eia:RWTC"), captor.capture());
        assertThat(captor.getValue().allows("eia-petroleum")).isFalse();
    }

    @Test
    @DisplayName("the description spells a series id without a stray space")
    void description() {
        assertThat(tool.description()).contains("eia:RWTC").doesNotContain("eia: RWTC");
    }
}
