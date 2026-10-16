/*
 * Ported from God's Eye View, server/providers/wind/catalog.js (selectLatestGfsCycle, gfsObjectKey),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; any run cadence that divides a day; several candidate cycles, newest first; object
 * keys from a configurable path template. nearestGfsStep is not ported (Foresignal's step lists are
 * configured per source).
 */
package ai.foresignal.raster.model;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model cycles from the clock (prompt 051): the newest cycle that should be published is the run
 * cadence boundary at or before {@code now − availabilityLag} (GFS: 6-hourly with a 3.5 h lag; HRRR:
 * 3-hourly, or 6-hourly in the small profile, with a 50-minute lag). The adapter then confirms a
 * candidate by its {@code .idx} existing; the clock alone never decides.
 *
 * <p>Object keys come from a path template with the tokens {@code {yyyyMMdd}}, {@code {HH}},
 * {@code {FFF}} (three-digit step), {@code {FF}} (two-digit step) and {@code {step}} (unpadded step,
 * prompt 105: ECMWF writes {@code 20261015000000-6h-oper-fc.grib2}), so a GFSv17 or an ECMWF layout
 * change is a setting, not a release.
 */
public final class ModelCycleSelector {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);

    private ModelCycleSelector() {}

    /**
     * The candidate cycles, newest first.
     *
     * @param now the clock
     * @param runEvery the run cadence; whole hours that divide 24
     * @param availabilityLag how long after its reference time a cycle starts to appear
     * @param count how many candidates
     * @return {@code count} reference times, newest first, each a multiple of {@code runEvery} after
     *     00z
     * @throws IllegalArgumentException for a cadence that is not a whole number of hours dividing 24
     */
    public static List<Instant> candidates(Instant now, Duration runEvery, Duration availabilityLag, int count) {
        return candidates(now, runEvery, Duration.ZERO, availabilityLag, count);
    }

    /**
     * The candidate cycles of a model whose cycles sit {@code cycleOffset} after each multiple of the
     * cadence (prompt 106: NBM runs 01/07/13/19z, a 6-hour cadence offset by one hour).
     *
     * @param now the clock
     * @param runEvery the run cadence; whole hours that divide 24
     * @param cycleOffset whole hours in {@code [0, runEvery)}; null or zero for cycles on the cadence
     * @param availabilityLag how long after its reference time a cycle starts to appear
     * @param count how many candidates
     * @return {@code count} reference times, newest first, each {@code cycleOffset} plus a multiple of
     *     {@code runEvery} after 00z
     * @throws IllegalArgumentException for a bad cadence, or an offset that is not whole hours in
     *     {@code [0, runEvery)}
     */
    public static List<Instant> candidates(Instant now, Duration runEvery, Duration cycleOffset,
            Duration availabilityLag, int count) {
        long hours = requireCadence(runEvery);
        long offset = requireOffset(cycleOffset, hours);
        Instant shifted = now.minus(availabilityLag == null ? Duration.ZERO : availabilityLag);
        ZonedDateTime t = shifted.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS).minusHours(offset);
        ZonedDateTime newest = t.withHour((int) (t.getHour() / hours * hours)).plusHours(offset);
        List<Instant> out = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            out.add(newest.minusHours(hours * k).toInstant());
        }
        return out;
    }

    /**
     * Checks a cycle offset.
     *
     * @return the offset in hours
     * @throws IllegalArgumentException unless it is whole hours in {@code [0, cadenceHours)}
     */
    static long requireOffset(Duration cycleOffset, long cadenceHours) {
        if (cycleOffset == null || cycleOffset.isZero()) {
            return 0;
        }
        if (cycleOffset.isNegative() || cycleOffset.toSeconds() % 3600 != 0 || cycleOffset.toHours() >= cadenceHours) {
            throw new IllegalArgumentException("A cycle offset must be whole hours below the cadence, was " + cycleOffset);
        }
        return cycleOffset.toHours();
    }

    /** The newest candidate cycle. */
    public static Instant latest(Instant now, Duration runEvery, Duration availabilityLag) {
        return candidates(now, runEvery, availabilityLag, 1).get(0);
    }

    /**
     * Checks a run cadence.
     *
     * @return the cadence in hours
     * @throws IllegalArgumentException unless it is 1 to 24 whole hours dividing 24
     */
    public static long requireCadence(Duration runEvery) {
        if (runEvery == null || runEvery.isNegative() || runEvery.isZero() || runEvery.toSeconds() % 3600 != 0) {
            throw new IllegalArgumentException("A run cadence must be a whole number of hours, was " + runEvery);
        }
        long hours = runEvery.toHours();
        if (hours > 24 || 24 % hours != 0) {
            throw new IllegalArgumentException("A run cadence must divide 24 hours, was " + hours + " h");
        }
        return hours;
    }

    /**
     * The object key of one step of a cycle.
     *
     * @param template the path template, for example
     *     {@code gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}}
     * @param cycle the reference time
     * @param step the step, hours
     */
    public static String objectKey(String template, Instant cycle, int step) {
        ZonedDateTime t = cycle.atZone(ZoneOffset.UTC);
        return template.replace("{yyyyMMdd}", DAY.format(t))
                .replace("{HH}", String.format(Locale.ROOT, "%02d", t.getHour()))
                .replace("{step}", Integer.toString(step))
                .replace("{FFF}", String.format(Locale.ROOT, "%03d", step))
                .replace("{FF}", String.format(Locale.ROOT, "%02d", step));
    }

    /** {@code 2026101012Z}: the cycle as adapter-state keys and file paths name it. */
    public static String cycleKey(Instant cycle) {
        return DateTimeFormatter.ofPattern("yyyyMMddHH'Z'", Locale.ROOT).withZone(ZoneOffset.UTC).format(cycle);
    }

    /** The cycle named by {@link #cycleKey}, or null when {@code key} is not one. */
    public static Instant parseCycleKey(String key) {
        if (key == null || !key.matches("^\\d{10}Z$")) {
            return null;
        }
        try {
            return ZonedDateTime.of(Integer.parseInt(key.substring(0, 4)), Integer.parseInt(key.substring(4, 6)),
                    Integer.parseInt(key.substring(6, 8)), Integer.parseInt(key.substring(8, 10)), 0, 0, 0,
                    ZoneOffset.UTC).toInstant();
        } catch (java.time.DateTimeException e) {
            return null;
        }
    }
}
