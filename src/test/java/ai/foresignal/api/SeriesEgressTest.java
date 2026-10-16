package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The series reads' egress decision (prompt 093, EGR-2): a source {@link EgressPolicy} refuses to send the caller on
 * the read's path (a {@code LOCAL_ONLY} source for a caller not allowed local egress) joins its excluded sources.
 */
class SeriesEgressTest {

    private static final EgressPolicy.Decision ALLOW = new EgressPolicy.Decision(true, null, null);
    private static final EgressPolicy.Decision LOCAL_ONLY =
            new EgressPolicy.Decision(false, EgressPolicy.REASON_LOCAL_ONLY, null);

    private final EgressPolicy policy = mock(EgressPolicy.class);
    private final SourceRegistry registry = mock(SourceRegistry.class);
    private final SeriesEgress egress = new SeriesEgress(policy, registry);

    @Test
    @DisplayName("a LOCAL_ONLY source the policy refuses on the read path is excluded; the others are kept")
    void localOnlyRefused() {
        when(registry.ids()).thenReturn(Set.of("fred", "eia-930", "eia-petroleum"));
        when(policy.decide(any(EgressPath.class), anyString(), isNull(), isNull(), any())).thenReturn(ALLOW);
        when(policy.decide(eq(EgressPath.API_READ), eq("fred"), isNull(), isNull(), any())).thenReturn(LOCAL_ONLY);
        ReadRestriction restriction = ReadRestriction.NONE;
        SeriesQueryService.Visibility visibility = egress.visibility(new ReadCaller(Scope.READ, false, true,
                restriction), Set.of("eia-petroleum"), restriction, EgressPath.API_READ);
        assertThat(visibility.excluded()).containsExactlyInAnyOrder("fred", "eia-petroleum");
        assertThat(visibility.allows("fred")).isFalse();
        assertThat(visibility.allows("eia-930")).isTrue();
        verify(policy, never()).decide(any(), eq("eia-petroleum"), any(), any(), any());
    }

    @Test
    @DisplayName("the decision is asked on the given path for the caller as a viewer; anonymous when no caller")
    void pathAndViewer() {
        when(registry.ids()).thenReturn(Set.of("fred"));
        when(policy.decide(any(EgressPath.class), anyString(), isNull(), isNull(), any())).thenReturn(ALLOW);
        egress.excluded(new ReadCaller(Scope.AGENT, false, true, ReadRestriction.NONE, true), Set.of(),
                EgressPath.MCP);
        ArgumentCaptor<EgressPolicy.EgressContext> context = ArgumentCaptor.forClass(EgressPolicy.EgressContext.class);
        verify(policy).decide(eq(EgressPath.MCP), eq("fred"), isNull(), isNull(), context.capture());
        assertThat(context.getValue().viewer().scope()).isEqualTo(Scope.AGENT);
        assertThat(context.getValue().viewer().privateAccess()).isTrue();
        assertThat(context.getValue().includePrivate()).isFalse();

        assertThat(egress.excluded(null, null, EgressPath.API_READ)).isEmpty();
    }
}
