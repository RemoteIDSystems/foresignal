package ai.foresignal.source.formats.gnss;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * The GNSS integrity sentinel's judgment for one receiver (prompt 086): pure, deterministic, fed in receiver time.
 *
 * <p>Samples are collected into {@value #WINDOW_SECONDS}-second windows on the TPV time base. When a TPV falls past
 * the end of the open window, that window is closed and assessed against a rolling {@value #BASELINE_MINUTES}-minute
 * baseline built only from earlier windows judged {@link State#OK}, so an attack or an outage never becomes the
 * baseline. Rules that need the baseline wait until it holds {@value #MIN_BASELINE_WINDOWS} windows.
 *
 * <p>States, in precedence order:
 * <ol>
 *   <li>{@link State#LOST}: {@code mode < 2} at the end of the window and no 2D or 3D fix for more than
 *       {@value #LOST_AFTER_SECONDS} s;
 *   <li>{@link State#JAMMED}: the receiver reports jamming (u-blox {@code jammingState} warning or critical), or the
 *       mean C/N0 of the used satellites fell by at least {@value #CN0_DROP_DB} dB from the baseline while the used
 *       satellite count fell by at least half;
 *   <li>{@link State#SUSPECT_SPOOF}: a fixed station whose mean position moved more than {@value #JUMP_METERS} m from
 *       the baseline, or whose clock offset stepped by more than one second from the previous window, while C/N0 rose
 *       above the baseline;
 *   <li>{@link State#DEGRADED}: {@code eph} above three times the baseline, or fewer than {@value #MIN_USED} used
 *       satellites;
 *   <li>{@link State#OK} otherwise.
 * </ol>
 * Not thread-safe; one evaluator per receiver.
 */
public final class GnssIntegrityEvaluator {

    /** The window length in seconds. */
    public static final int WINDOW_SECONDS = 30;

    /** The baseline span in minutes. */
    public static final int BASELINE_MINUTES = 15;

    /** How long without a fix counts as lost, in seconds. */
    public static final int LOST_AFTER_SECONDS = 60;

    /** The C/N0 drop that, with the satellite drop, means jamming, in dB. */
    public static final double CN0_DROP_DB = 10.0;

    /** The fraction of used satellites lost that, with the C/N0 drop, means jamming. */
    public static final double USAT_DROP_FRACTION = 0.5;

    /** A fixed station's position jump that may mean spoofing, in meters. */
    public static final double JUMP_METERS = 500.0;

    /** A clock offset step that may mean spoofing, in nanoseconds (one second). */
    public static final long CLOCK_STEP_NS = 1_000_000_000L;

    /** The {@code eph} multiple of the baseline that means degraded. */
    public static final double EPH_FACTOR = 3.0;

    /** Fewer used satellites than this means degraded. */
    public static final int MIN_USED = 4;

    /** The baseline windows the baseline rules need. */
    public static final int MIN_BASELINE_WINDOWS = 4;

    /** The window length. */
    public static final Duration WINDOW = Duration.ofSeconds(WINDOW_SECONDS);

    /** The baseline span. */
    public static final Duration BASELINE = Duration.ofMinutes(BASELINE_MINUTES);

    /** The integrity states. */
    public enum State {
        /** Nothing suspicious. */
        OK,
        /** Accuracy is poor or too few satellites are used. */
        DEGRADED,
        /** Jamming is reported or inferred. */
        JAMMED,
        /** A fixed station's position or clock jumped while signals grew stronger. */
        SUSPECT_SPOOF,
        /** No fix for more than a minute. */
        LOST
    }

    /**
     * One closed window, summarized.
     *
     * @param start the window start
     * @param lastTime the last TPV time in the window, the record's {@code observedAt}
     * @param mode the last TPV mode
     * @param lat the last fix latitude, or null
     * @param lon the last fix longitude, or null
     * @param meanLat the mean fix latitude, or null
     * @param meanLon the mean fix longitude, or null
     * @param eph the mean {@code eph}, or null
     * @param meanUsed the mean used satellite count, or null
     * @param uSat the last used satellite count, or null
     * @param nSat the last satellite count, or null
     * @param hdop the last HDOP, or null
     * @param cn0MeanDb the mean C/N0 of the used satellites, or null
     * @param jamIndicator the highest receiver jamming indicator, or null
     * @param jammingState the worst receiver jamming state, or null
     * @param clockOffsetNs the last clock offset in nanoseconds, or null
     */
    public record Window(Instant start, Instant lastTime, int mode, Double lat, Double lon, Double meanLat,
            Double meanLon, Double eph, Double meanUsed, Integer uSat, Integer nSat, Double hdop, Double cn0MeanDb,
            Integer jamIndicator, Integer jammingState, Long clockOffsetNs) {}

    /**
     * The judgment of one window.
     *
     * @param state the integrity state
     * @param reason which rule decided it, for messages
     * @param window the window
     */
    public record Assessment(State state, String reason, Window window) {}

    private final boolean fixedStation;
    private final Deque<Window> baseline = new ArrayDeque<>();
    private Accumulator open;
    private Window previous;
    private Instant lastFix;
    private Instant firstSeen;
    private Integer pendingNSat;
    private Integer pendingUSat;
    private Double pendingHdop;

    /**
     * Creates an evaluator.
     *
     * @param fixedStation true when the antenna does not move, which enables the spoofing rule
     */
    public GnssIntegrityEvaluator(boolean fixedStation) {
        this.fixedStation = fixedStation;
    }

    /**
     * A TPV report. Closes the open window when {@code time} is past its end.
     *
     * @param time the TPV time
     * @param mode the fix mode (0–3)
     * @param lat the latitude of a 2D or 3D fix, else null
     * @param lon the longitude of a 2D or 3D fix, else null
     * @param eph the horizontal error estimate, or null
     * @return the assessment of the window this TPV closed, if any
     */
    public Optional<Assessment> onTpv(Instant time, int mode, Double lat, Double lon, Double eph) {
        if (time == null) {
            return Optional.empty();
        }
        if (firstSeen == null) {
            firstSeen = time;
        }
        Optional<Assessment> closed = Optional.empty();
        if (open != null && !time.isBefore(open.start.plus(WINDOW))) {
            closed = Optional.of(close());
        }
        if (open == null) {
            open = new Accumulator(time);
            if (pendingNSat != null || pendingUSat != null || pendingHdop != null) {
                open.sky(pendingNSat, pendingUSat, pendingHdop, null);
            }
        }
        if (mode >= 2) {
            lastFix = time;
        }
        open.tpv(time, mode, lat, lon, eph);
        return closed;
    }

    /** A SKY report: satellite counts, HDOP and the mean C/N0 of the used satellites (each may be null). */
    public void onSky(Integer nSat, Integer uSat, Double hdop, Double cn0MeanDb) {
        if (open == null) {
            pendingNSat = nSat != null ? nSat : pendingNSat;
            pendingUSat = uSat != null ? uSat : pendingUSat;
            pendingHdop = hdop != null ? hdop : pendingHdop;
            return;
        }
        open.sky(nSat, uSat, hdop, cn0MeanDb);
    }

    /** A PPS or TOFF clock offset (system clock minus GNSS time), in nanoseconds. */
    public void onClockOffset(long offsetNs) {
        if (open != null) {
            open.clockOffsetNs = offsetNs;
        }
    }

    /** A receiver monitor reading (u-blox MON-HW or MON-RF); either value may be null. */
    public void onReceiverJamming(Integer jamIndicator, Integer jammingState) {
        if (open == null) {
            return;
        }
        if (jamIndicator != null) {
            open.jamIndicator = open.jamIndicator == null ? jamIndicator : Math.max(open.jamIndicator, jamIndicator);
        }
        if (jammingState != null) {
            open.jammingState = open.jammingState == null ? jammingState : Math.max(open.jammingState, jammingState);
        }
    }

    /** The baseline windows currently held, for tests. */
    int baselineSize() {
        return baseline.size();
    }

    private Assessment close() {
        Window w = open.summary();
        open = null;
        Assessment a = assess(w);
        previous = w;
        if (a.state() == State.OK) {
            baseline.addLast(w);
        }
        Instant horizon = w.lastTime().minus(BASELINE);
        while (!baseline.isEmpty() && baseline.peekFirst().lastTime().isBefore(horizon)) {
            baseline.removeFirst();
        }
        return a;
    }

    private Assessment assess(Window w) {
        Instant since = lastFix != null ? lastFix : firstSeen;
        if (w.mode() < 2 && since != null && Duration.between(since, w.lastTime()).getSeconds() > LOST_AFTER_SECONDS) {
            return new Assessment(State.LOST, "no fix for more than " + LOST_AFTER_SECONDS + " s", w);
        }
        Baseline b = baseline();
        if (w.jammingState() != null && w.jammingState() >= 2) {
            return new Assessment(State.JAMMED, "the receiver reports jamming (state " + w.jammingState() + ")", w);
        }
        if (b != null && b.cn0 != null && w.cn0MeanDb() != null && b.used != null && w.meanUsed() != null
                && b.cn0 - w.cn0MeanDb() >= CN0_DROP_DB && w.meanUsed() <= b.used * (1 - USAT_DROP_FRACTION)) {
            return new Assessment(State.JAMMED, "C/N0 fell " + round1(b.cn0 - w.cn0MeanDb()) + " dB and used satellites"
                    + " fell from " + round1(b.used) + " to " + round1(w.meanUsed()), w);
        }
        if (fixedStation && b != null && b.cn0 != null && w.cn0MeanDb() != null && w.cn0MeanDb() > b.cn0) {
            if (b.lat != null && w.meanLat() != null) {
                double jump = meters(b.lat, b.lon, w.meanLat(), w.meanLon());
                if (jump > JUMP_METERS) {
                    return new Assessment(State.SUSPECT_SPOOF, "a fixed station moved " + Math.round(jump)
                            + " m while C/N0 rose", w);
                }
            }
            if (previous != null && previous.clockOffsetNs() != null && w.clockOffsetNs() != null
                    && Math.abs(w.clockOffsetNs() - previous.clockOffsetNs()) > CLOCK_STEP_NS) {
                return new Assessment(State.SUSPECT_SPOOF, "the clock offset stepped by more than 1 s while C/N0 rose",
                        w);
            }
        }
        if (b != null && b.eph != null && w.eph() != null && w.eph() > EPH_FACTOR * b.eph) {
            return new Assessment(State.DEGRADED, "eph " + round1(w.eph()) + " m is more than three times the"
                    + " baseline " + round1(b.eph) + " m", w);
        }
        if (w.meanUsed() != null && w.meanUsed() < MIN_USED) {
            return new Assessment(State.DEGRADED, "fewer than " + MIN_USED + " satellites used", w);
        }
        return new Assessment(State.OK, "ok", w);
    }

    /** The baseline means, or null while it holds fewer than {@value #MIN_BASELINE_WINDOWS} windows. */
    private Baseline baseline() {
        if (baseline.size() < MIN_BASELINE_WINDOWS) {
            return null;
        }
        Mean cn0 = new Mean();
        Mean used = new Mean();
        Mean eph = new Mean();
        Mean lat = new Mean();
        Mean lon = new Mean();
        for (Window w : baseline) {
            cn0.add(w.cn0MeanDb());
            used.add(w.meanUsed());
            eph.add(w.eph());
            if (w.meanLat() != null && w.meanLon() != null) {
                lat.add(w.meanLat());
                lon.add(w.meanLon());
            }
        }
        return new Baseline(cn0.value(), used.value(), eph.value(), lat.value(), lon.value());
    }

    /** The great-circle distance in meters. */
    public static double meters(double lat1, double lon1, double lat2, double lon2) {
        double r = 6_371_008.8;
        double p1 = Math.toRadians(lat1);
        double p2 = Math.toRadians(lat2);
        double dp = p2 - p1;
        double dl = Math.toRadians(lon2 - lon1);
        double h = Math.sin(dp / 2) * Math.sin(dp / 2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2 * r * Math.asin(Math.min(1, Math.sqrt(h)));
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private record Baseline(Double cn0, Double used, Double eph, Double lat, Double lon) {}

    /** A running mean that ignores nulls. */
    private static final class Mean {
        private double sum;
        private int n;

        void add(Double v) {
            if (v != null && Double.isFinite(v)) {
                sum += v;
                n++;
            }
        }

        Double value() {
            return n == 0 ? null : sum / n;
        }
    }

    /** The open window's samples. */
    private static final class Accumulator {
        final Instant start;
        Instant lastTime;
        int mode;
        Double lat;
        Double lon;
        final Mean lats = new Mean();
        final Mean lons = new Mean();
        final Mean eph = new Mean();
        final Mean used = new Mean();
        final Mean cn0 = new Mean();
        Integer uSat;
        Integer nSat;
        Double hdop;
        Integer jamIndicator;
        Integer jammingState;
        Long clockOffsetNs;

        Accumulator(Instant start) {
            this.start = start;
            this.lastTime = start;
        }

        void tpv(Instant time, int mode, Double lat, Double lon, Double eph) {
            lastTime = time;
            this.mode = mode;
            if (mode >= 2 && lat != null && lon != null) {
                this.lat = lat;
                this.lon = lon;
                lats.add(lat);
                lons.add(lon);
            }
            this.eph.add(eph);
        }

        void sky(Integer nSat, Integer uSat, Double hdop, Double cn0MeanDb) {
            if (nSat != null) {
                this.nSat = nSat;
            }
            if (uSat != null) {
                this.uSat = uSat;
                used.add((double) uSat);
            }
            if (hdop != null) {
                this.hdop = hdop;
            }
            cn0.add(cn0MeanDb);
        }

        Window summary() {
            Double meanUsed = used.value();
            if (meanUsed == null && uSat != null) {
                meanUsed = (double) uSat;
            }
            return new Window(start, lastTime, mode, lat, lon, lats.value(), lons.value(), eph.value(), meanUsed, uSat,
                    nSat, hdop, cn0.value(), jamIndicator, jammingState, clockOffsetNs);
        }
    }
}
