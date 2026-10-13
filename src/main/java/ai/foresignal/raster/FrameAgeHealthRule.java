package ai.foresignal.raster;

import ai.foresignal.collect.FrameAgeRule;
import ai.foresignal.collect.HealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Frame-age health for product sources (prompt 042 on the prompt 006 {@link HealthRule} SPI;
 * Weather-Imagery-Design §2.2, Health row).
 *
 * <p>For each of the source's products with frames: degraded when the newest frame is older than
 * {@code maxGap} (or {@code 3 × cadence} when the product declares no gap), via {@link FrameAgeRule}.
 * An outstanding silent format change of any of its products also degrades the source, with the
 * decoder's reason, and so does a product the decoder self-test disabled (prompt 046), with its
 * reason. The newest frame time across the products is reported as {@code newestFrameAt}
 * (and so {@code frameAgeSeconds}) in the health view.
 *
 * <p>Frame time is the newest complete valid time for observation products and the newest ingest
 * time for forecast products, whose valid times lie in the future. A source with products but no
 * frame yet gets no frame-age verdict.
 *
 * <p>Forecast products of a source whose run cadence is known ({@link ForecastRunCadence}, prompt
 * 051) are judged against the run cadence instead of their step spacing: degraded when the newest
 * frame was ingested more than two runs ago.
 */
@Component
public class FrameAgeHealthRule implements HealthRule {

    private final ProductRegistry registry;
    private final RasterFrameQueries frames;
    private final ProductStatusTracker status;
    private final java.util.List<ForecastRunCadence> cadences;

    /** Creates the rule without run cadences (tests). */
    public FrameAgeHealthRule(ProductRegistry registry, RasterFrameQueries frames, ProductStatusTracker status) {
        this(registry, frames, status, java.util.List.of());
    }

    /**
     * Creates the rule.
     *
     * @param cadences the run cadences of forecast sources (prompt 051)
     */
    @org.springframework.beans.factory.annotation.Autowired
    public FrameAgeHealthRule(ProductRegistry registry, RasterFrameQueries frames, ProductStatusTracker status,
            java.util.List<ForecastRunCadence> cadences) {
        this.registry = registry;
        this.frames = frames;
        this.status = status;
        this.cadences = cadences == null ? java.util.List.of() : java.util.List.copyOf(cadences);
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        List<ProductRegistry.Registered> products = registry.forSource(context.sourceId()).stream()
                .filter(p -> !p.spec().isExternal()).toList();
        if (products.isEmpty()) {
            return Optional.empty();
        }
        List<String> observations = new ArrayList<>();
        List<String> forecasts = new ArrayList<>();
        for (ProductRegistry.Registered p : products) {
            (p.spec().isForecast() ? forecasts : observations).add(p.id());
        }
        Map<String, Instant> newest = frames.newestFrameTimes(observations, forecasts);
        java.time.Duration runEvery = null;
        for (ForecastRunCadence c : cadences) {
            Optional<java.time.Duration> d = c.runCadence(context.sourceId());
            if (d.isPresent()) {
                runEvery = d.get();
                break;
            }
        }
        return verdict(products, newest, status, context.now(), runEvery);
    }

    /** The combined verdict over {@code products} (pure, for tests), without a run cadence. */
    static Optional<HealthVerdict> verdict(List<ProductRegistry.Registered> products, Map<String, Instant> newest,
            ProductStatusTracker status, Instant now) {
        return verdict(products, newest, status, now, null);
    }

    /**
     * The combined verdict over {@code products} (pure, for tests).
     *
     * @param runEvery the source's forecast run cadence, or null to judge forecasts by their steps
     */
    static Optional<HealthVerdict> verdict(List<ProductRegistry.Registered> products, Map<String, Instant> newest,
            ProductStatusTracker status, Instant now, java.time.Duration runEvery) {
        Instant newestOverall = null;
        HealthVerdict worst = null;
        for (ProductRegistry.Registered product : products) {
            ProductSpec spec = product.spec();
            Instant frameAt = newest.get(spec.id());
            if (frameAt != null && (newestOverall == null || frameAt.isAfter(newestOverall))) {
                newestOverall = frameAt;
            }
            Optional<ProductStatusTracker.Report> off = status == null ? Optional.empty() : status.disabled(spec.id());
            if (off.isPresent()) {
                // Prompt 046: a product whose decoder failed the startup self-test is disabled.
                worst = worse(worst, HealthVerdict.degraded(spec.id() + ": disabled: " + off.get().reason(), frameAt));
                continue;
            }
            Optional<ProductStatusTracker.Report> change = status == null ? Optional.empty() : status.formatChange(spec.id());
            if (change.isPresent()) {
                worst = worse(worst, HealthVerdict.degraded(spec.id() + ": upstream format changed: "
                        + change.get().reason(), frameAt));
                continue;
            }
            if (spec.temporal() == null || ProductSpec.Temporal.PASS.equals(spec.temporal().type())) {
                // Prompt 054: a pass product (SatDump polar overlays) has frames only when a satellite
                // passes over the station, so frame age says nothing about its health.
                continue;
            }
            boolean byRun = spec.isForecast() && runEvery != null;
            Optional<HealthVerdict> age = byRun ? FrameAgeRule.verdict(frameAt, runEvery, runEvery.multipliedBy(2), now)
                    : FrameAgeRule.verdict(frameAt, spec.temporal().cadence(), spec.temporal().maxGap(), now);
            if (age.isPresent() && age.get().status() != HealthStatus.HEALTHY) {
                worst = worse(worst, HealthVerdict.degraded(spec.id() + ": " + age.get().reason(), frameAt));
            }
        }
        if (worst != null) {
            return Optional.of(new HealthVerdict(worst.status(), worst.reason(), newestOverall));
        }
        return newestOverall == null ? Optional.empty() : Optional.of(HealthVerdict.healthy(newestOverall));
    }

    private static HealthVerdict worse(HealthVerdict current, HealthVerdict candidate) {
        if (current == null) {
            return candidate;
        }
        return candidate.status().worse(current.status()) == candidate.status() && candidate.status() != current.status()
                ? candidate : current;
    }
}
