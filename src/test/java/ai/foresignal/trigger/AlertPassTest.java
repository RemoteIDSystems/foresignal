package ai.foresignal.trigger;

import static ai.foresignal.trigger.TestRules.box;
import static ai.foresignal.trigger.TestRules.rule;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.store.TriggerMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link AlertPass}: the exact Java pass (prompt 017, task 3). */
class AlertPassTest {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    private long seq;

    MatchRow row(String id, Instant observedAt, String edge) {
        seq++;
        return new MatchRow(seq, null, "s", id, "k", 1, (short) 1, observedAt, null, null, null, false, null, "{}", edge);
    }

    static AlertPass.Context live(Set<String> snoozed, int cap) {
        return new AlertPass.Context(T, snoozed, cap, false, null);
    }

    static List<String> reasons(AlertPass.Result result) {
        List<String> out = new ArrayList<>();
        for (AlertPass.Op op : result.ops()) {
            if (op instanceof AlertPass.Fire fire) {
                out.add(fire.row().externalId() + ":" + (fire.suppressedReason() == null ? "fired" : fire.suppressedReason()));
            } else {
                out.add("resolve");
            }
        }
        return out;
    }

    @Test
    @DisplayName("cooldown per entity on observed_at, deterministic whatever the clock")
    void recordCooldown() {
        RuleSpec spec = rule().sources("s").cooldown(Duration.ofMinutes(10), CooldownScope.RECORD).build();
        List<MatchRow> rows = List.of(row("a", T, "match"), row("a", T.plusSeconds(300), "match"),
                row("b", T.plusSeconds(301), "match"), row("a", T.plusSeconds(601), "match"));
        AlertPass.Result result = AlertPass.run(spec, rows, List.of(), new AlertPass.State(), live(Set.of(), 0));
        assertThat(reasons(result)).containsExactly("a:fired", "b:fired", "a:fired");
        assertThat(result.cooldownSuppressed()).isEqualTo(1);
    }

    @Test
    @DisplayName("cooldown per rule spans entities and carries over between passes")
    void ruleCooldown() {
        RuleSpec spec = rule().sources("s").cooldown(Duration.ofMinutes(10), CooldownScope.RULE).build();
        AlertPass.State state = new AlertPass.State();
        AlertPass.Result first = AlertPass.run(spec, List.of(row("a", T, "match"), row("b", T.plusSeconds(60), "match")),
                List.of(), state, live(Set.of(), 0));
        assertThat(reasons(first)).containsExactly("a:fired");
        AlertPass.Result second = AlertPass.run(spec, List.of(row("c", T.plusSeconds(599), "match"),
                row("d", T.plusSeconds(600), "match")), List.of(), state, live(Set.of(), 0));
        assertThat(reasons(second)).containsExactly("d:fired");
    }

    @Test
    @DisplayName("an alert snooze, then a rule snooze, record suppressed alerts that start no cooldown")
    void snoozes() {
        TestRules builder = rule().sources("s").cooldown(Duration.ofMinutes(10), CooldownScope.RECORD);
        AlertPass.Result alertSnoozed = AlertPass.run(builder.build(),
                List.of(row("a", T, "match"), row("a", T.plusSeconds(1), "match")),
                List.of(), new AlertPass.State(), live(Set.of("s\u0000a"), 0));
        assertThat(reasons(alertSnoozed)).containsExactly("a:alert_snoozed", "a:alert_snoozed");
        assertThat(alertSnoozed.snoozed()).isEqualTo(2);
        builder.snoozedUntil = T.plusSeconds(60);
        AlertPass.Result ruleSnoozed = AlertPass.run(builder.build(), List.of(row("b", T, "match")), List.of(),
                new AlertPass.State(), live(Set.of(), 0));
        assertThat(reasons(ruleSnoozed)).containsExactly("b:rule_snoozed");
    }

    @Test
    @DisplayName("the storm cap drops the excess, signals one event per window and can disable the rule")
    void storm() {
        RuleSpec spec = rule().sources("s").build();
        AlertPass.State state = new AlertPass.State();
        state.seedStorm(List.of(T.minusSeconds(3000)));
        List<MatchRow> rows = List.of(row("a", T, "match"), row("b", T, "match"), row("c", T, "match"), row("d", T, "match"));
        AlertPass.Result result = AlertPass.run(spec, rows, List.of(), state, live(Set.of(), 2));
        assertThat(reasons(result)).containsExactly("a:fired");
        assertThat(result.stormSuppressed()).isEqualTo(3);
        assertThat(result.stormEventDue()).isTrue();
        AlertPass.Result again = AlertPass.run(spec, List.of(row("e", T, "match")), List.of(), state, live(Set.of(), 2));
        assertThat(again.stormEventDue()).as("once per window").isFalse();
        assertThat(again.stormSuppressed()).isEqualTo(1);

        TestRules disabling = rule().sources("s");
        disabling.autoDisable = true;
        disabling.cap = 1;
        AlertPass.Result disabled = AlertPass.run(disabling.build(), List.of(row("a", T, "match"), row("b", T, "match"),
                row("c", T, "match")), List.of(), new AlertPass.State(), live(Set.of(), 120));
        assertThat(disabled.disable()).isTrue();
        assertThat(disabled.fired()).isEqualTo(1);
        assertThat(disabled.stormSuppressed()).isEqualTo(1);
    }

    @Test
    @DisplayName("a cap of 0 is unlimited, and the window slides")
    void unlimited() {
        TestRules builder = rule().sources("s");
        builder.cap = 0;
        AlertPass.Result result = AlertPass.run(builder.build(), List.of(row("a", T, "match"), row("b", T, "match")),
                List.of(), new AlertPass.State(), live(Set.of(), 1));
        assertThat(result.fired()).isEqualTo(2);
        AlertPass.State state = new AlertPass.State();
        state.seedStorm(List.of(T.minus(Duration.ofMinutes(61))));
        assertThat(AlertPass.run(rule().sources("s").build(), List.of(row("a", T, "match")), List.of(), state,
                live(Set.of(), 1)).fired()).isEqualTo(1);
    }

    @Test
    @DisplayName("matches outside the active period are skipped")
    void window() {
        TestRules builder = rule().sources("s");
        builder.activeUntil = T.plusSeconds(10);
        AlertPass.Result result = AlertPass.run(builder.build(),
                List.of(row("a", T, "match"), row("b", T.plusSeconds(10), "match")),
                List.of(), new AlertPass.State(), live(Set.of(), 0));
        assertThat(reasons(result)).containsExactly("a:fired");
        assertThat(result.windowSkipped()).isEqualTo(1);
    }

    @Test
    @DisplayName("hysteresis edges alternate per entity; the opposite edge only moves the state")
    void hysteresis() {
        RuleSpec spec = rule().fence(0, 0, 10, 10).mode(TriggerMode.ENTER)
                .band(box(1, 1, 9, 9), box(-1, -1, 11, 11), 100).build();
        List<MatchRow> rows = List.of(row("a", T, "enter"), row("a", T.plusSeconds(1), "enter"),
                row("a", T.plusSeconds(2), "exit"), row("a", T.plusSeconds(3), "exit"), row("a", T.plusSeconds(4), "enter"));
        AlertPass.Result result = AlertPass.run(spec, rows, List.of(), new AlertPass.State(), live(Set.of(), 0));
        assertThat(reasons(result)).containsExactly("a:fired", "a:fired");
        AlertPass.Result seeded = AlertPass.run(spec, List.of(row("b", T, "enter")), List.of(), new AlertPass.State(),
                new AlertPass.Context(T, Set.of(), 0, false, key -> "enter"));
        assertThat(seeded.ops()).as("a stored open enter alert means the entity is inside").isEmpty();
        assertThat(AlertPass.impliedEdge(TriggerMode.ENTER, false)).isEqualTo("exit");
        assertThat(AlertPass.impliedEdge(TriggerMode.EXIT, true)).isEqualTo("exit");
        assertThat(AlertPass.impliedEdge(TriggerMode.MATCH, true)).isNull();
    }

    @Test
    @DisplayName("resolve rows merge in seq order; simulate ignores snoozes and runs the cap on observed_at")
    void mergeAndSimulate() {
        RuleSpec spec = rule().sources("s").build();
        MatchRow first = row("a", T, "match");
        ResolveRow resolve = new ResolveRow(first.seq() + 1, "s", "a", true);
        seq++;
        MatchRow third = row("a", T.plusSeconds(5), "match");
        AlertPass.Result result = AlertPass.run(spec, List.of(first, third), List.of(resolve), new AlertPass.State(),
                live(Set.of(), 0));
        assertThat(reasons(result)).containsExactly("a:fired", "resolve", "a:fired");
        assertThat(resolve.resolvedBy()).isEqualTo("tombstone");
        assertThat(new ResolveRow(1, "s", "a", false).resolvedBy()).isEqualTo("inverse_edge");

        AlertPass.Result simulated = AlertPass.run(spec,
                List.of(row("a", T, "match"), row("b", T.plusSeconds(3601), "match")),
                List.of(), new AlertPass.State(), new AlertPass.Context(T, Set.of("s\u0000a"), 1, true, null));
        assertThat(simulated.fired()).isEqualTo(2);
        assertThat(simulated.snoozed()).isZero();
    }
}
