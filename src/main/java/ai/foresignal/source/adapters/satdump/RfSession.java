package ai.foresignal.source.adapters.satdump;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One RF session of a SatDump station (prompt 049, Weather-Imagery-Design §4.2): from tracking start
 * (or the first pipeline activity) to LOS.
 *
 * <p>About every {@value #SAMPLE_SECONDS} s the {@code /api} figures ({@code snr}, {@code peak_snr},
 * {@code viterbi_ber}, {@code deframer_state}, {@code rs_avg}, azimuth and elevation) go into an
 * in-memory ring of at most {@value #RING_CAPACITY} samples. The station loop flushes the ring to the
 * adapter state every {@value #FLUSH_SECONDS} s ({@link #flushDue}) and at LOS, and {@link #aggregate}
 * computes the statistics a pass record carries.
 *
 * <p>Not thread-safe; the station synchronizes.
 */
public final class RfSession {

    /** The sampling period, seconds. */
    public static final int SAMPLE_SECONDS = 2;

    /** The flush period, seconds. */
    public static final int FLUSH_SECONDS = 30;

    /** The most samples kept (about 66 minutes at 2 s). */
    public static final int RING_CAPACITY = 2_000;

    /** The target length of the SNR-against-elevation series. */
    public static final int SERIES_POINTS = 60;

    /**
     * One sample.
     *
     * @param at when it was taken (epoch milliseconds)
     * @param snr SNR in dB, or null
     * @param peakSnr peak SNR in dB, or null
     * @param ber Viterbi BER, or null
     * @param deframerState the deframer state, or null
     * @param locked true when a deframer or Viterbi decoder was locked
     * @param rsAvg the Reed-Solomon figure, or null
     * @param az azimuth in degrees, or null
     * @param el elevation in degrees, or null
     */
    public record Sample(long at, Double snr, Double peakSnr, Double ber, String deframerState, boolean locked,
            Double rsAvg, Double az, Double el) {}

    private final String passId;
    private final String satKey;
    private final String satellite;
    private final String mode;
    private final Instant start;
    private final ArrayDeque<Sample> ring = new ArrayDeque<>();
    private Long frequencyHz;
    private Instant lastActivity;
    private Instant lastSampleAt;
    private Instant lastFlushAt;
    private Instant los;

    /**
     * Opens a session.
     *
     * @param passId the pass {@code externalId} minted at the start
     * @param satKey the satellite key, or null when unknown (live mode)
     * @param satellite the satellite name as SatDump gave it, or null
     * @param mode {@code live} or {@code autotrack}
     * @param start tracking start or first pipeline activity
     */
    public RfSession(String passId, String satKey, String satellite, String mode, Instant start) {
        this.passId = passId;
        this.satKey = satKey;
        this.satellite = satellite;
        this.mode = mode;
        this.start = start;
        this.lastActivity = start;
        this.lastFlushAt = start;
    }

    /**
     * Records a reading; a sample is kept when {@value #SAMPLE_SECONDS} s have passed since the last.
     *
     * @return true when a sample was kept
     */
    public boolean sample(Instant now, ApiSnapshot snapshot) {
        if (snapshot.frequencyHz() != null) {
            frequencyHz = snapshot.frequencyHz();
        }
        if (snapshot.pipelineRunning() || snapshot.trackingAboveHorizon()) {
            lastActivity = now;
        }
        if (lastSampleAt != null && Duration.between(lastSampleAt, now).toMillis() < SAMPLE_SECONDS * 1000L - 250) {
            return false;
        }
        lastSampleAt = now;
        ring.addLast(new Sample(now.toEpochMilli(), snapshot.snr(), snapshot.peakSnr(), snapshot.ber(),
                snapshot.deframerState(), snapshot.locked(), snapshot.rsAvg(), snapshot.az(), snapshot.el()));
        while (ring.size() > RING_CAPACITY) {
            ring.removeFirst();
        }
        return true;
    }

    /** Adds a sample restored from the adapter state; it counts as activity at its time. */
    void restore(Sample sample) {
        ring.addLast(sample);
        lastSampleAt = Instant.ofEpochMilli(sample.at());
        lastActivity = lastSampleAt;
        while (ring.size() > RING_CAPACITY) {
            ring.removeFirst();
        }
    }

    /** True when {@value #FLUSH_SECONDS} s have passed since the last flush. */
    public boolean flushDue(Instant now) {
        return Duration.between(lastFlushAt, now).toSeconds() >= FLUSH_SECONDS;
    }

    /** Marks a flush done at {@code now}. */
    public void flushed(Instant now) {
        lastFlushAt = now;
    }

    /** Closes the session at {@code los}. */
    public void close(Instant los) {
        this.los = los;
    }

    /** The samples, oldest first. */
    public List<Sample> samples() {
        return new ArrayList<>(ring);
    }

    /** The pass id. */
    public String passId() {
        return passId;
    }

    /** The satellite key, or null. */
    public String satKey() {
        return satKey;
    }

    /** The satellite name as SatDump gave it, or null. */
    public String satellite() {
        return satellite;
    }

    /** The mode the session started in. */
    public String mode() {
        return mode;
    }

    /** The start. */
    public Instant start() {
        return start;
    }

    /** The last tracking or pipeline activity. */
    public Instant lastActivity() {
        return lastActivity;
    }

    /** The LOS, or null while open. */
    public Instant los() {
        return los;
    }

    /** The last frequency seen, or null. */
    public Long frequencyHz() {
        return frequencyHz;
    }

    /** The first sample with a lock, or null. */
    public Instant firstLock() {
        for (Sample s : ring) {
            if (s.locked()) {
                return Instant.ofEpochMilli(s.at());
            }
        }
        return null;
    }

    /** True when any sample had a lock. */
    public boolean hadLock() {
        return firstLock() != null;
    }

    /** The highest elevation sampled, or null. */
    public Double maxElevation() {
        Double max = null;
        for (Sample s : ring) {
            if (s.el() != null) {
                max = max == null ? s.el() : Math.max(max, s.el());
            }
        }
        return max;
    }

    /**
     * The session statistics of a pass record's {@code rf{}}: {@code snrMaxDb}, {@code snrMeanDb},
     * {@code berMin}, {@code berMean}, {@code syncedFraction}, {@code firstLockAt}, {@code lastLockAt},
     * {@code firstLockElevationDeg}, {@code lastLockElevationDeg}, {@code samples} and
     * {@code snrVsElevation} (at most {@value #SERIES_POINTS} {@code [elDeg, snrDb]} pairs in time order,
     * so the rising and setting halves stay apart). Figures that no sample carried are left out.
     */
    public Map<String, Object> aggregate() {
        Map<String, Object> rf = new LinkedHashMap<>();
        List<Sample> samples = samples();
        rf.put("samples", samples.size());
        if (samples.isEmpty()) {
            return rf;
        }
        Double snrMax = null;
        double snrSum = 0;
        int snrCount = 0;
        Double berMin = null;
        double berSum = 0;
        int berCount = 0;
        int synced = 0;
        Sample firstLock = null;
        Sample lastLock = null;
        for (Sample s : samples) {
            if (s.snr() != null) {
                snrMax = snrMax == null ? s.snr() : Math.max(snrMax, s.snr());
                snrSum += s.snr();
                snrCount++;
            }
            if (s.ber() != null) {
                berMin = berMin == null ? s.ber() : Math.min(berMin, s.ber());
                berSum += s.ber();
                berCount++;
            }
            if (s.locked()) {
                synced++;
                if (firstLock == null) {
                    firstLock = s;
                }
                lastLock = s;
            }
        }
        put(rf, "snrMaxDb", round(snrMax, 10));
        put(rf, "snrMeanDb", snrCount == 0 ? null : round(snrSum / snrCount, 10));
        put(rf, "berMin", round(berMin, 10_000));
        put(rf, "berMean", berCount == 0 ? null : round(berSum / berCount, 10_000));
        rf.put("syncedFraction", round((double) synced / samples.size(), 1000));
        if (firstLock != null) {
            rf.put("firstLockAt", Instant.ofEpochMilli(firstLock.at()).toString());
            rf.put("lastLockAt", Instant.ofEpochMilli(lastLock.at()).toString());
            put(rf, "firstLockElevationDeg", round(firstLock.el(), 10));
            put(rf, "lastLockElevationDeg", round(lastLock.el(), 10));
        }
        List<List<Double>> series = series(samples);
        if (!series.isEmpty()) {
            rf.put("snrVsElevation", series);
        }
        return rf;
    }

    /** At most {@value #SERIES_POINTS} {@code [el, snr]} means over consecutive buckets of samples. */
    static List<List<Double>> series(List<Sample> samples) {
        List<Sample> usable = samples.stream().filter(s -> s.el() != null && s.snr() != null).toList();
        List<List<Double>> out = new ArrayList<>();
        if (usable.isEmpty()) {
            return out;
        }
        int buckets = Math.min(SERIES_POINTS, usable.size());
        for (int b = 0; b < buckets; b++) {
            int from = (int) ((long) b * usable.size() / buckets);
            int to = (int) ((long) (b + 1) * usable.size() / buckets);
            double el = 0;
            double snr = 0;
            for (int i = from; i < to; i++) {
                el += usable.get(i).el();
                snr += usable.get(i).snr();
            }
            int n = to - from;
            out.add(List.of(round(el / n, 10), round(snr / n, 10)));
        }
        return out;
    }

    private static Double round(Double v, int factor) {
        return v == null ? null : Math.round(v * factor) / (double) factor;
    }

    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
