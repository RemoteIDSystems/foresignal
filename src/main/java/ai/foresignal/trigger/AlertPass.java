package ai.foresignal.trigger;

import ai.foresignal.store.TriggerMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The exact Java pass over one rule's matches, in seq order (prompt 017, task 3). The engine runs it
 * on live chunks and {@code testTrigger simulate=true} on a backtest.
 *
 * <p>For each match, in order:
 *
 * <ol>
 *   <li><b>Hysteresis</b> (edge rules with a band): the per-entity edge state flips on every edge,
 *       and an edge equal to the current state is dropped, so edges alternate and jitter inside the
 *       band never fires. Only edges of the rule's own mode go on.
 *   <li><b>Active period and windows</b> on the version's {@code observed_at}.
 *   <li><b>Cooldown</b> on {@code observed_at}, per entity or per rule, against the last
 *       unsuppressed alert this pass (or an earlier one) recorded; the SQL seed covers alerts already
 *       in the database.
 *   <li><b>Alert snooze</b>, then <b>rule snooze</b>: the alert is recorded suppressed
 *       ({@code alert_snoozed} / {@code rule_snoozed}).
 *   <li><b>Storm cap</b>: a sliding one-hour window of unsuppressed alerts per rule. Over the cap
 *       nothing is recorded; the match is counted, a storm event is due once per window, and with
 *       {@code autoDisableOnStorm} the rest of the chunk is dropped.
 * </ol>
 *
 * <p>Auto-resolve rows ({@link ResolveRow}) are merged in by seq unchanged; the writer resolves the
 * entity's open alerts that fired before them.
 */
public final class AlertPass {

    /** The storm window. */
    public static final Duration STORM_WINDOW = Duration.ofHours(1);

    /** Per-entity cooldown entries kept before old ones are pruned. */
    static final int PRUNE_AT = 100_000;

    private AlertPass() {}

    /** One planned write, in seq order. */
    public sealed interface Op {

        /** The version the operation belongs to. */
        long seq();
    }

    /**
     * Record an alert for {@code row}.
     *
     * @param row the matching version
     * @param suppressedReason {@code alert_snoozed}, {@code rule_snoozed}, or null for a routed alert
     */
    public record Fire(MatchRow row, String suppressedReason) implements Op {
        @Override
        public long seq() {
            return row.seq();
        }
    }

    /**
     * Resolve the entity's open alerts that fired before {@code row}.
     *
     * @param row the inverse edge or tombstone
     */
    public record Resolve(ResolveRow row) implements Op {
        @Override
        public long seq() {
            return row.seq();
        }
    }

    /**
     * What a pass decided.
     *
     * @param ops the writes, in seq order
     * @param cooldownSuppressed matches dropped by the cooldown
     * @param windowSkipped matches outside the active period or windows
     * @param stormSuppressed matches dropped by the storm cap
     * @param stormEventDue true when a storm event should be published now
     * @param disable true when {@code autoDisableOnStorm} should disable the rule
     */
    public record Result(
            List<Op> ops,
            int cooldownSuppressed,
            int windowSkipped,
            int stormSuppressed,
            boolean stormEventDue,
            boolean disable) {

        /** The alerts the pass would record unsuppressed. */
        public long fired() {
            return ops.stream().filter(op -> op instanceof Fire f && f.suppressedReason() == null).count();
        }

        /** The alerts the pass would record suppressed (snoozes). */
        public long snoozed() {
            return ops.stream().filter(op -> op instanceof Fire f && f.suppressedReason() != null).count();
        }
    }

    /**
     * What the pass reads besides the rule.
     *
     * @param now the fire time (the storm window's clock in a live pass)
     * @param snoozedEntities entity keys with an open, snoozed alert of the rule
     * @param defaultCap the storm cap of a rule without one
     * @param simulate true for a backtest: no snoozes, and the storm window runs on
     *     {@code observed_at} instead of the clock
     * @param edgeSeed the stored edge state of an entity ({@code enter}/{@code exit}) when this pass
     *     has none yet, or null when unknown
     */
    public record Context(
            Instant now,
            Set<String> snoozedEntities,
            int defaultCap,
            boolean simulate,
            Function<String, String> edgeSeed) {

        /** Fills defaults. */
        public Context {
            snoozedEntities = snoozedEntities == null ? Set.of() : snoozedEntities;
            edgeSeed = edgeSeed == null ? key -> null : edgeSeed;
        }
    }

    /** The mutable per-rule state a pass reads and updates; the engine keeps one per rule. */
    public static final class State {

        final Map<String, Instant> lastByEntity = new HashMap<>();
        final Map<String, String> edges = new HashMap<>();
        final Deque<Instant> storm = new ArrayDeque<>();
        Instant ruleLast;
        Instant lastStormEvent;
        boolean stormSeeded;

        /** A fresh state. */
        public State() {}

        /** Seeds the storm window with earlier fire times (oldest first); only the first call counts. */
        public void seedStorm(List<Instant> firedAt) {
            if (!stormSeeded) {
                storm.addAll(firedAt);
                stormSeeded = true;
            }
        }

        /** True once the storm window was seeded. */
        public boolean stormSeeded() {
            return stormSeeded;
        }
    }

    /**
     * Runs the pass.
     *
     * @param rule the rule
     * @param matches its matches, in seq order
     * @param resolves its auto-resolve rows, in seq order
     * @param state the rule's state, updated in place
     * @param context the pass inputs
     */
    public static Result run(RuleSpec rule, List<MatchRow> matches, List<ResolveRow> resolves, State state, Context context) {
        List<Op> ops = new ArrayList<>();
        for (ResolveRow resolve : resolves) {
            ops.add(new Resolve(resolve));
        }
        int cooldownSuppressed = 0;
        int windowSkipped = 0;
        int stormSuppressed = 0;
        boolean stormEventDue = false;
        boolean disable = false;
        String ownEdge = rule.mode().wireValue();
        Duration cooldown = rule.cooldown();
        int cap = rule.stormCap(context.defaultCap());

        for (MatchRow row : matches) {
            String key = row.entityKey();
            if (rule.hysteresis()) {
                String current = state.edges.containsKey(key) ? state.edges.get(key) : context.edgeSeed().apply(key);
                if (row.edge().equals(current)) {
                    continue;
                }
                state.edges.put(key, row.edge());
                if (!row.edge().equals(ownEdge)) {
                    continue;
                }
            }
            if (!rule.isActiveAt(row.observedAt())) {
                windowSkipped++;
                continue;
            }
            if (!cooldown.isZero()) {
                Instant last = rule.cooldownScope() == CooldownScope.RULE ? state.ruleLast : state.lastByEntity.get(key);
                if (last != null && last.isAfter(row.observedAt().minus(cooldown))) {
                    cooldownSuppressed++;
                    continue;
                }
            }
            if (!context.simulate() && context.snoozedEntities().contains(key)) {
                ops.add(new Fire(row, "alert_snoozed"));
                continue;
            }
            if (!context.simulate() && rule.snoozedAt(context.now())) {
                ops.add(new Fire(row, "rule_snoozed"));
                continue;
            }
            Instant fireTime = context.simulate() ? row.observedAt() : context.now();
            if (cap > 0) {
                Instant horizon = fireTime.minus(STORM_WINDOW);
                while (!state.storm.isEmpty() && !state.storm.peekFirst().isAfter(horizon)) {
                    state.storm.pollFirst();
                }
                if (state.storm.size() >= cap) {
                    stormSuppressed++;
                    if (state.lastStormEvent == null || !state.lastStormEvent.isAfter(fireTime.minus(STORM_WINDOW))) {
                        state.lastStormEvent = fireTime;
                        stormEventDue = true;
                    }
                    if (rule.autoDisableOnStorm()) {
                        disable = true;
                        break;
                    }
                    continue;
                }
                state.storm.addLast(fireTime);
            }
            ops.add(new Fire(row, null));
            remember(state, key, row.observedAt(), cooldown);
        }
        ops.sort(Comparator.comparingLong(Op::seq));
        return new Result(List.copyOf(ops), cooldownSuppressed, windowSkipped, stormSuppressed, stormEventDue, disable);
    }

    private static void remember(State state, String key, Instant observedAt, Duration cooldown) {
        if (cooldown.isZero()) {
            return;
        }
        if (state.ruleLast == null || observedAt.isAfter(state.ruleLast)) {
            state.ruleLast = observedAt;
        }
        Instant previous = state.lastByEntity.get(key);
        if (previous == null || observedAt.isAfter(previous)) {
            state.lastByEntity.put(key, observedAt);
        }
        if (state.lastByEntity.size() > PRUNE_AT) {
            Instant horizon = state.ruleLast.minus(cooldown);
            Iterator<Map.Entry<String, Instant>> it = state.lastByEntity.entrySet().iterator();
            while (it.hasNext()) {
                if (!it.next().getValue().isAfter(horizon)) {
                    it.remove();
                }
            }
        }
    }

    /** The stored edge state an alert implies for its rule's mode: open enter alert → enter, etc. */
    static String impliedEdge(TriggerMode mode, boolean open) {
        if (mode == TriggerMode.ENTER) {
            return open ? "enter" : "exit";
        }
        if (mode == TriggerMode.EXIT) {
            return "exit";
        }
        return null;
    }
}
