package ai.foresignal.source.adapters.wx;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.RasterReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * The state of one raster input of a derived weather source (prompt 052): whether its newest frame or
 * run can be used now.
 *
 * @param status {@link WxSection#OK}, {@link WxSection#STALE} or {@link WxSection#UNAVAILABLE}
 * @param frame the newest complete frame (observation input), or null
 * @param run the newest complete run (forecast input), or null
 */
public record WxInput(String status, RasterFrameRow frame, Instant run) {

    /** True when usable now. */
    public boolean ok() {
        return WxSection.OK.equals(status);
    }

    /**
     * An observation product's newest frame: {@code unavailable} when the product is disabled, its source is
     * failing or it has no frame; {@code stale} when the frame is older than {@code maxGap} at {@code now}.
     */
    public static WxInput observation(DerivedContext ctx, String productId, Duration maxGap, Instant now) {
        RasterReader rasters = ctx.rasters();
        if (rasters.unavailableReason(productId).isPresent()) {
            return new WxInput(WxSection.UNAVAILABLE, null, null);
        }
        Optional<RasterFrameRow> newest = rasters.newestFrame(productId);
        if (newest.isEmpty()) {
            return new WxInput(WxSection.UNAVAILABLE, null, null);
        }
        RasterFrameRow frame = newest.get();
        if (Duration.between(frame.validTime(), now).compareTo(maxGap) > 0) {
            return new WxInput(WxSection.STALE, frame, null);
        }
        return new WxInput(WxSection.OK, frame, null);
    }

    /**
     * A forecast product's newest complete run: {@code unavailable} when there is none or the product cannot be
     * used; {@code stale} when the run is older than {@code maxRunAge} at {@code now}.
     */
    public static WxInput forecast(DerivedContext ctx, String productId, Duration maxRunAge, Instant now) {
        RasterReader rasters = ctx.rasters();
        if (rasters.unavailableReason(productId).isPresent()) {
            return new WxInput(WxSection.UNAVAILABLE, null, null);
        }
        Optional<Instant> run = rasters.latestCompleteRun(productId);
        if (run.isEmpty()) {
            return new WxInput(WxSection.UNAVAILABLE, null, null);
        }
        if (Duration.between(run.get(), now).compareTo(maxRunAge) > 0) {
            return new WxInput(WxSection.STALE, null, run.get());
        }
        return new WxInput(WxSection.OK, null, run.get());
    }
}
