package ai.foresignal.notify.mesh;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * One mesh channel's LoRa airtime budget (prompt 084): a one-hour sliding window of the estimated airtime of every
 * transmission it admitted. Every transmission of the channel (deliveries, tests and field-report replies) asks it
 * first; nothing bypasses it.
 *
 * <ul>
 *   <li><strong>Ordinary messages</strong> fit when the window's airtime plus theirs stays within the operator's
 *       share ({@code dutyCyclePercent} of the hour, default 1 %), at least {@code minSpacing} (default 30 s) has
 *       passed since the last transmission, and fewer than {@code maxMessagesPerHour} (default 20) went out in the
 *       window. Otherwise they are deferred to the earliest time all three hold.
 *   <li><strong>Critical messages</strong> jump that queue: they ignore the operator's share, spacing and count, but
 *       never exceed the region's regulatory ceiling (10 % in EU_868, UK_868 and EU_433; the 10 % maximum where a
 *       region has none). Without room under the ceiling they are deferred too.
 *   <li><strong>Dropped:</strong> a message longer than the whole share (or, for a critical one, the whole ceiling)
 *       could never fit and is refused.
 * </ul>
 *
 * <p>Thread-safe. Pure: the caller passes the time, so tests drive it with any clock.
 */
public final class AirtimeGovernor {

    /** The sliding window. */
    public static final Duration WINDOW = Duration.ofHours(1);

    /** The default operator share of the channel, in percent. */
    public static final double DEFAULT_DUTY_CYCLE_PERCENT = 1.0;

    /** The default spacing between transmissions. */
    public static final Duration DEFAULT_MIN_SPACING = Duration.ofSeconds(30);

    /** The default hourly message cap. */
    public static final int DEFAULT_MAX_MESSAGES_PER_HOUR = 20;

    /**
     * The governor's settings.
     *
     * @param region the regulatory region
     * @param dutyCyclePercent the operator's share, above 0 and at most the region's ceiling (and at most 10)
     * @param minSpacing the least time between transmissions
     * @param maxMessagesPerHour the most transmissions in the window, at least 1
     */
    public record Limits(MeshRegion region, double dutyCyclePercent, Duration minSpacing, int maxMessagesPerHour) {

        /**
         * Checks the settings.
         *
         * @throws IllegalArgumentException when {@code dutyCyclePercent} exceeds the ceiling or another value is out
         *     of range
         */
        public Limits {
            Objects.requireNonNull(region, "region");
            minSpacing = minSpacing == null ? DEFAULT_MIN_SPACING : minSpacing;
            if (!(dutyCyclePercent > 0)) {
                throw new IllegalArgumentException("dutyCyclePercent must be above 0");
            }
            if (dutyCyclePercent > MeshRegion.MAX_DUTY_CYCLE_PERCENT) {
                throw new IllegalArgumentException("dutyCyclePercent must be at most "
                        + MeshRegion.MAX_DUTY_CYCLE_PERCENT);
            }
            if (region.ceilingPercent() != null && dutyCyclePercent > region.ceilingPercent()) {
                throw new IllegalArgumentException("dutyCyclePercent " + dutyCyclePercent + " exceeds the "
                        + region.ceilingPercent() + " % duty-cycle ceiling of " + region);
            }
            if (minSpacing.isNegative()) {
                throw new IllegalArgumentException("minSpacing cannot be negative");
            }
            if (maxMessagesPerHour < 1) {
                throw new IllegalArgumentException("maxMessagesPerHour must be at least 1");
            }
        }

        /** The defaults for {@code region}: 1 %, 30 s, 20 per hour. */
        public static Limits defaults(MeshRegion region) {
            return new Limits(region, DEFAULT_DUTY_CYCLE_PERCENT, DEFAULT_MIN_SPACING, DEFAULT_MAX_MESSAGES_PER_HOUR);
        }

        /** The operator's share of the window, in seconds. */
        public double limitSeconds() {
            return WINDOW.toSeconds() * dutyCyclePercent / 100.0;
        }

        /** The ceiling of the window, in seconds, that even critical messages never exceed. */
        public double ceilingSeconds() {
            return WINDOW.toSeconds() * region.effectiveCeilingPercent() / 100.0;
        }
    }

    /** What the governor decided for one transmission. */
    public sealed interface Decision permits Admit, Defer, Drop {}

    /**
     * Transmit now; the airtime is counted.
     *
     * @param seconds the airtime counted
     */
    public record Admit(double seconds) implements Decision {}

    /**
     * Not now.
     *
     * @param until the earliest time it fits
     * @param why which limit held it back
     */
    public record Defer(Instant until, String why) implements Decision {}

    /**
     * Never: the message is longer than the whole share or ceiling.
     *
     * @param why the reason
     */
    public record Drop(String why) implements Decision {}

    /**
     * The window's state.
     *
     * @param windowSeconds the window length
     * @param usedSeconds the airtime admitted in the window
     * @param limitSeconds the operator's share
     * @param ceilingSeconds the regulatory (or maximum) ceiling
     * @param dutyCyclePercent the operator's share in percent
     * @param transmissions the transmissions in the window
     * @param deferredLastHour deferrals in the window
     * @param foldedLastHour alerts folded into digest lines in the window
     * @param droppedLastHour transmissions dropped in the window
     */
    public record Snapshot(long windowSeconds, double usedSeconds, double limitSeconds, double ceilingSeconds,
            double dutyCyclePercent, int transmissions, int deferredLastHour, int foldedLastHour, int droppedLastHour) {}

    private record Entry(Instant at, double seconds) {}

    private final Limits limits;
    private final Deque<Entry> entries = new ArrayDeque<>();
    private final Deque<Instant> deferred = new ArrayDeque<>();
    private final Deque<Instant> folded = new ArrayDeque<>();
    private final Deque<Instant> dropped = new ArrayDeque<>();

    /** Creates an empty governor. */
    public AirtimeGovernor(Limits limits) {
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    /**
     * A governor with {@code newLimits} that keeps this one's window and counts, so changing a channel's settings
     * never resets the airtime already spent.
     */
    public synchronized AirtimeGovernor withLimits(Limits newLimits) {
        AirtimeGovernor copy = new AirtimeGovernor(newLimits);
        copy.entries.addAll(entries);
        copy.deferred.addAll(deferred);
        copy.folded.addAll(folded);
        copy.dropped.addAll(dropped);
        return copy;
    }

    /** The settings. */
    public Limits limits() {
        return limits;
    }

    /**
     * Decides one transmission of {@code seconds} of airtime at {@code now}, counting it when admitted and counting
     * a deferral or a drop otherwise.
     *
     * @param critical true for a critical alert (jumps the operator's limits, never the ceiling)
     */
    public synchronized Decision tryAcquire(double seconds, boolean critical, Instant now) {
        prune(now);
        double used = used();
        if (critical) {
            if (seconds > limits.ceilingSeconds()) {
                dropped.addLast(now);
                return new Drop(String.format(java.util.Locale.ROOT,
                        "%.1f s of airtime exceeds the whole %.0f s hourly ceiling", seconds, limits.ceilingSeconds()));
            }
            if (used + seconds > limits.ceilingSeconds()) {
                deferred.addLast(now);
                return new Defer(fitsAt(seconds, limits.ceilingSeconds(), now), "the regulatory duty-cycle ceiling");
            }
            entries.addLast(new Entry(now, seconds));
            return new Admit(seconds);
        }
        if (seconds > limits.limitSeconds()) {
            dropped.addLast(now);
            return new Drop(String.format(java.util.Locale.ROOT,
                    "%.1f s of airtime exceeds the whole %.1f s hourly share", seconds, limits.limitSeconds()));
        }
        Instant until = now;
        String why = null;
        Entry last = entries.peekLast();
        if (last != null && now.isBefore(last.at().plus(limits.minSpacing()))) {
            until = last.at().plus(limits.minSpacing());
            why = "minSpacing";
        }
        if (entries.size() >= limits.maxMessagesPerHour()) {
            List<Entry> list = new ArrayList<>(entries);
            Instant countFree = list.get(entries.size() - limits.maxMessagesPerHour()).at().plus(WINDOW);
            if (countFree.isAfter(until)) {
                until = countFree;
                why = "maxMessagesPerHour";
            }
        }
        if (used + seconds > limits.limitSeconds()) {
            Instant shareFree = fitsAt(seconds, limits.limitSeconds(), now);
            if (shareFree.isAfter(until)) {
                until = shareFree;
                why = "dutyCyclePercent";
            }
        }
        if (until.isAfter(now)) {
            deferred.addLast(now);
            return new Defer(until, why);
        }
        entries.addLast(new Entry(now, seconds));
        return new Admit(seconds);
    }

    /** Records that a digest line carried {@code count} folded alerts. */
    public synchronized void noteFolded(int count, Instant now) {
        for (int i = 0; i < count; i++) {
            folded.addLast(now);
        }
        prune(now);
    }

    /** Records a dropped transmission decided elsewhere (an over-rate field-report reply). */
    public synchronized void noteDropped(Instant now) {
        dropped.addLast(now);
        prune(now);
    }

    /** The window's state at {@code now}. */
    public synchronized Snapshot snapshot(Instant now) {
        prune(now);
        return new Snapshot(WINDOW.toSeconds(), used(), limits.limitSeconds(), limits.ceilingSeconds(),
                limits.dutyCyclePercent(), entries.size(), deferred.size(), folded.size(), dropped.size());
    }

    /** The earliest time the window's airtime plus {@code seconds} fits under {@code limit}. */
    private Instant fitsAt(double seconds, double limit, Instant now) {
        double used = used();
        Iterator<Entry> oldest = entries.iterator();
        Instant at = now;
        while (used + seconds > limit && oldest.hasNext()) {
            Entry e = oldest.next();
            used -= e.seconds();
            at = e.at().plus(WINDOW);
        }
        return at.isAfter(now) ? at : now;
    }

    private double used() {
        double sum = 0;
        for (Entry e : entries) {
            sum += e.seconds();
        }
        return sum;
    }

    private void prune(Instant now) {
        Instant from = now.minus(WINDOW);
        while (!entries.isEmpty() && !entries.peekFirst().at().isAfter(from)) {
            entries.removeFirst();
        }
        for (Deque<Instant> times : List.of(deferred, folded, dropped)) {
            while (!times.isEmpty() && !times.peekFirst().isAfter(from)) {
                times.removeFirst();
            }
        }
    }
}
