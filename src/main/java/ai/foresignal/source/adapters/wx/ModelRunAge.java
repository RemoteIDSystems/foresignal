package ai.foresignal.source.adapters.wx;

import java.time.Duration;

/**
 * How old a forecast model's newest complete run may be before a derived weather section calls it
 * {@code stale} (prompt 105 post-review; Weather-Imagery-Design §7.1 "unknown is not clear").
 *
 * <p>A run goes stale only when its successor should already have been ingested: the run cadence
 * (the next run's reference time), plus the availability lag (when the upstream starts publishing
 * it), plus an ingest allowance (how long the staged, budgeted fetch takes to complete every step).
 * A single fixed age does not fit every model: with a 12-hour limit, IFS (00z/12z, about 7 h of lag
 * and a multi-poll fetch) was stale about three quarters of the day, so its sections flapped between
 * {@code ok} and {@code stale} and churned versions.
 *
 * <p>The numbers are each model adapter's default cadence and lag (the slower profile where the
 * profiles differ). An operator who slows a model's cadence should expect its sections to go stale
 * sooner than its data really is; that is the safe direction.
 */
public final class ModelRunAge {

    /** One model's timing. */
    record Timing(Duration cadence, Duration lag, Duration ingest) {

        /** The age past which the run is stale. */
        Duration maxAge() {
            return cadence.plus(lag).plus(ingest);
        }
    }

    /** GFS: 6-hourly, 3.5 h lag, about 2 h to post and fetch 120 hourly steps. */
    static final Timing GFS = new Timing(Duration.ofHours(6), Duration.ofMinutes(210), Duration.ofHours(2));

    /** GFS-Wave: 6-hourly, 4 h lag. */
    static final Timing GFS_WAVE = new Timing(Duration.ofHours(6), Duration.ofHours(4), Duration.ofHours(2));

    /** HRRR: 3-hourly (6-hourly on the small profile, used here), 50 min lag, about 1 h to fetch. */
    static final Timing HRRR = new Timing(Duration.ofHours(6), Duration.ofMinutes(50), Duration.ofHours(1));

    /**
     * IFS: 00z/12z, 7 h lag, and 3 h to complete: the run is published over about an hour and 49 steps
     * are fetched ten per 20-minute poll.
     */
    static final Timing IFS = new Timing(Duration.ofHours(12), Duration.ofHours(7), Duration.ofHours(3));

    /** AIFS: 6-hourly, 7 h lag, 3 h to complete. */
    static final Timing AIFS = new Timing(Duration.ofHours(6), Duration.ofHours(7), Duration.ofHours(3));

    /** NBM: 6-hourly, 90 min lag, 90 min to fetch. */
    static final Timing NBM = new Timing(Duration.ofHours(6), Duration.ofMinutes(90), Duration.ofMinutes(90));

    /** Any other model product: the previous fixed limit. */
    static final Duration DEFAULT = Duration.ofHours(12);

    private ModelRunAge() {}

    /**
     * The maximum run age of a model product.
     *
     * @param productId a forecast product id ({@code gfs-gust}, {@code hrrr-smoke}, {@code ifs-t2m}, …)
     * @return cadence + lag + ingest allowance of the product's model, or 12 h for an unknown model
     */
    public static Duration of(String productId) {
        if (productId == null) {
            return DEFAULT;
        }
        if (productId.equals("gfs-wave") || productId.startsWith("gfs-wave-")) {
            return GFS_WAVE.maxAge();
        }
        return switch (model(productId)) {
            case "gfs" -> GFS.maxAge();
            case "hrrr" -> HRRR.maxAge();
            case "ifs" -> IFS.maxAge();
            case "aifs" -> AIFS.maxAge();
            case "nbm" -> NBM.maxAge();
            default -> DEFAULT;
        };
    }

    /** The model prefix of a product id ({@code ifs} for {@code ifs-precip-24h}). */
    static String model(String productId) {
        int dash = productId.indexOf('-');
        return dash < 0 ? productId : productId.substring(0, dash);
    }
}
