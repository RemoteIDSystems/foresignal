package ai.foresignal.raster;

import ai.foresignal.source.spi.ProductSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The frame retention rules as pure functions (Weather-Imagery-Design §2.4): initial expiry, ladder
 * thinning and forecast run retention. {@link RasterRetentionStep} applies the plan.
 *
 * <p><b>Observation ladder.</b> Every frame is kept until the first rung's {@code olderThan}. Past
 * rung {@code i}, the frames still kept are grouped by their nearest {@code keepEvery} boundary
 * ({@code round(t / keepEvery) × keepEvery}, epoch-aligned) and only the frame nearest each boundary
 * survives; it is marked thinned and its expiry becomes {@code valid + olderThan(i+1)}, or
 * {@code valid + maxAge} after the last rung. Frames older than {@code maxAge} go.
 *
 * <p><b>Forecasts.</b> The newest {@code runs} complete runs are kept (2 by default: current and
 * previous), plus any run in progress newer than them; frames of every other run go.
 */
public final class FrameThinning {

    private FrameThinning() {}

    /**
     * What retention does to one product.
     *
     * @param delete frames to delete
     * @param thinned surviving frames to mark thinned, with their new expiry (null for none)
     */
    public record Plan(List<RasterFrameRow> delete, Map<RasterFrameRow, Instant> thinned) {

        /** True when nothing changes. */
        public boolean isEmpty() {
            return delete.isEmpty() && thinned.isEmpty();
        }
    }

    /** A new frame's expiry: when it may first be removed; null when nothing removes it. */
    public static Instant initialExpiry(ProductSpec spec, Instant validTime) {
        ProductSpec.RetentionLadder ladder = spec.retention();
        if (spec.isForecast() || ladder == null) {
            return null;
        }
        if (!ladder.rungs().isEmpty()) {
            return validTime.plus(ladder.rungs().get(0).olderThan());
        }
        return ladder.maxAge() == null ? null : validTime.plus(ladder.maxAge());
    }

    /** The retention plan of one product's frames at {@code now}. */
    public static Plan plan(ProductSpec spec, List<RasterFrameRow> frames, Instant now) {
        return spec.isForecast() ? forecastPlan(spec, frames) : ladderPlan(spec, frames, now);
    }

    /** The observation ladder plan. */
    static Plan ladderPlan(ProductSpec spec, List<RasterFrameRow> frames, Instant now) {
        ProductSpec.RetentionLadder ladder = spec.retention();
        List<RasterFrameRow> delete = new ArrayList<>();
        Map<RasterFrameRow, Instant> thinned = new LinkedHashMap<>();
        if (ladder == null) {
            return new Plan(delete, thinned);
        }
        List<RasterFrameRow> kept = new ArrayList<>(frames);
        kept.sort(Comparator.comparing(RasterFrameRow::validTime).thenComparing(RasterFrameRow::frameId));
        if (ladder.maxAge() != null) {
            Instant cutoff = now.minus(ladder.maxAge());
            List<RasterFrameRow> old = kept.stream().filter(f -> f.validTime().isBefore(cutoff)).toList();
            delete.addAll(old);
            kept.removeAll(old);
        }
        List<ProductSpec.RetentionLadder.Rung> rungs = ladder.rungs();
        for (int i = 0; i < rungs.size(); i++) {
            ProductSpec.RetentionLadder.Rung rung = rungs.get(i);
            Instant cutoff = now.minus(rung.olderThan());
            List<RasterFrameRow> candidates = kept.stream().filter(f -> f.validTime().isBefore(cutoff)).toList();
            if (candidates.isEmpty()) {
                continue;
            }
            Set<RasterFrameRow> survivors = nearestPerBoundary(candidates, rung.keepEvery());
            Duration nextAge = i + 1 < rungs.size() ? rungs.get(i + 1).olderThan() : ladder.maxAge();
            for (RasterFrameRow frame : candidates) {
                if (!survivors.contains(frame)) {
                    delete.add(frame);
                    kept.remove(frame);
                    thinned.remove(frame);
                    continue;
                }
                Instant expiry = nextAge == null ? null : frame.validTime().plus(nextAge);
                if (!frame.thinned() || !Objects.equals(frame.expiresAt(), expiry) || thinned.containsKey(frame)) {
                    thinned.put(frame, expiry);
                }
            }
        }
        return new Plan(delete, thinned);
    }

    /** Per boundary of {@code every}, the frame nearest it (ties to the earlier frame). */
    static Set<RasterFrameRow> nearestPerBoundary(List<RasterFrameRow> frames, Duration every) {
        long step = every.toMillis();
        Map<Long, RasterFrameRow> best = new LinkedHashMap<>();
        for (RasterFrameRow frame : frames) {
            long t = frame.validTime().toEpochMilli();
            long boundary = Math.round((double) t / step) * step;
            RasterFrameRow current = best.get(boundary);
            if (current == null || Math.abs(t - boundary) < Math.abs(current.validTime().toEpochMilli() - boundary)) {
                best.put(boundary, frame);
            }
        }
        return new HashSet<>(best.values());
    }

    /** The forecast run plan. */
    static Plan forecastPlan(ProductSpec spec, List<RasterFrameRow> frames) {
        int keep = spec.retention() == null ? ProductSpec.RetentionLadder.DEFAULT_RUNS : spec.retention().runsKept();
        Map<Instant, Boolean> runs = new java.util.TreeMap<>(Comparator.reverseOrder());
        for (RasterFrameRow frame : frames) {
            if (frame.referenceTime() != null) {
                runs.merge(frame.referenceTime(), frame.runComplete(), Boolean::logicalOr);
            }
        }
        Set<Instant> kept = new HashSet<>();
        int complete = 0;
        for (Map.Entry<Instant, Boolean> run : runs.entrySet()) {
            if (run.getValue()) {
                if (complete < keep) {
                    kept.add(run.getKey());
                }
                complete++;
            } else if (complete == 0) {
                kept.add(run.getKey());
            }
        }
        List<RasterFrameRow> delete = frames.stream()
                .filter(f -> f.referenceTime() != null && !kept.contains(f.referenceTime())).toList();
        return new Plan(new ArrayList<>(delete), new LinkedHashMap<>());
    }
}
