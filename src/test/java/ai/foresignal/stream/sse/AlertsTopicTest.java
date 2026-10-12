package ai.foresignal.stream.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@link AlertsTopic}: filters, replay events and identity (prompt 017). */
class AlertsTopicTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final SourceDescriptor SECRET = SourceDescriptor.builder("secret").name("Secret").kind("k").mode(Mode.PUSH)
            .defaultBudget(Budget.of(60, 10_000)).privateSource(true).build();

    AlertStore store;
    SourceRegistry registry;
    AlertsTopic topic;

    @BeforeEach
    void setUp() {
        store = mock(AlertStore.class);
        registry = mock(SourceRegistry.class);
        when(registry.descriptors()).thenReturn(List.of(SECRET));
        topic = new AlertsTopic(store, new AlertBus(new SimpleMeterRegistry()), new AlertMapper(), registry);
    }

    static AlertView alert(long rev, String state, Instant snoozedUntil, String feedback) {
        return new AlertView(rev, RULE, "r", 1, "s", "e", "k", T, T, "m", "warning", state, rev, null, null, null, null, null,
                feedback, null, null, snoozedUntil, false, null, List.of(), "{\"kind\":\"k\"}");
    }

    @Test
    @DisplayName("identity: alerts, cursor key a, after changes, read scope")
    void identity() {
        assertThat(topic.name()).isEqualTo("alerts");
        assertThat(topic.cursorKey()).isEqualTo("a");
        assertThat(topic.cursorOrder()).isGreaterThan(0);
        assertThat(topic.requiredScope()).isEqualTo(Scope.READ);
        assertThat(topic.filterType()).isEqualTo(AlertQuery.class);
        when(store.maxRev()).thenReturn(42L);
        assertThat(topic.head()).isEqualTo(42L);
    }

    @Test
    @DisplayName("connect parameters: severity, rule ids, sources, kinds, includeSuppressed; anonymous callers lose private sources")
    void query() {
        AlertQuery q = topic.parseQuery(new TopicParams("alerts.", Map.of(
                "minSeverity", List.of("critical"), "ruleId", List.of(RULE.toString()), "kind", List.of("k"),
                "includeSuppressed", List.of("true"))), StreamPrincipal.anonymous("1.2.3.4"));
        assertThat(q.minSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(q.ruleIds()).containsExactly(RULE);
        assertThat(q.kinds()).containsExactly("k");
        assertThat(q.includeSuppressed()).isTrue();
        assertThat(q.excludedSources()).containsExactly("secret");
        AlertQuery token = topic.parseQuery(new TopicParams("", Map.of()), new StreamPrincipal("token:x", Scope.ADMIN, "h"));
        assertThat(token.excludedSources()).isEmpty();
        assertThatThrownBy(() -> topic.parseQuery(new TopicParams("alerts.", Map.of("minSeverity", List.of("high"))),
                StreamPrincipal.anonymous("h"))).isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors().getFirst().field()).isEqualTo("alerts.minSeverity"));
        assertThatThrownBy(() -> topic.parseQuery(new TopicParams("", Map.of("ruleId", List.of("nope"))),
                StreamPrincipal.anonymous("h"))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("PATCH blocks: validated whole, unknown fields refused, null means everything")
    void json() {
        StreamPrincipal principal = StreamPrincipal.anonymous("h");
        AlertQuery q = topic.parseJson(JSON.readTree("{\"minSeverity\":\"warning\",\"kind\":[\"k\"],\"includeSuppressed\":true}"),
                principal);
        assertThat(q.minSeverity()).isEqualTo(Severity.WARNING);
        assertThat(q.includeSuppressed()).isTrue();
        assertThat(topic.parseJson(null, principal).minSeverity()).isNull();
        assertThatThrownBy(() -> topic.parseJson(JSON.readTree("{\"color\":1}"), principal)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> topic.parseJson(JSON.readTree("[]"), principal)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> topic.parseJson(JSON.readTree("{\"kind\":\"k\"}"), principal)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> topic.parseJson(JSON.readTree("{\"includeSuppressed\":1}"), principal))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> topic.parseJson(JSON.readTree("{\"minSeverity\":5}"), principal))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("replayed alerts are sent as their current state, in rev order, with id = rev")
    void replay() {
        when(store.afterRev(any(), anyLong(), anyInt())).thenReturn(List.of(
                alert(3, "firing", null, null), alert(4, "acknowledged", null, null), alert(5, "firing", T, null),
                alert(6, "firing", null, "noise"), alert(7, "resolved", null, null)));
        List<FifoEvent> sent = new ArrayList<>();
        ReplayOutcome outcome = topic.replay(new ReplayRequest(2, true), AlertQuery.all(), event -> sent.add(event));
        assertThat(outcome).isEqualTo(new ReplayOutcome.Done(7));
        assertThat(sent).extracting(FifoEvent::event)
                .containsExactly("alert", "alert.updated", "alert.updated", "alert.updated", "alert.resolved");
        assertThat(sent).extracting(FifoEvent::cursor).containsExactly(3L, 4L, 5L, 6L, 7L);
        assertThat(sent.get(1).data()).containsEntry("type", AlertEvent.ACKNOWLEDGED);
        assertThat(sent.get(2).data()).containsEntry("type", AlertEvent.SNOOZED);
        assertThat(topic.replay(new ReplayRequest(2, true), AlertQuery.all(), event -> false))
                .isInstanceOf(ReplayOutcome.CapReached.class);
    }
}
