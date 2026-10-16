package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;

/**
 * A per-fetch tally of country references the 040 gazetteer could not resolve (prompt 090: "unknown country
 * → null geometry, counted").
 *
 * <p>An adapter creates one per fetch, calls {@link #miss()} for each unresolved country, then
 * {@link #report reports} it: the count goes to the Micrometer counter {@value #METRIC} (tags {@code source},
 * {@code reason=unknown-country}) and to the source's debug log. Nothing is counted without a registry.
 */
final class UnresolvedCountries {

    /** The counter name. */
    static final String METRIC = "foresignal.humanitarian.country.unresolved";

    /** The counter's {@code reason} tag. */
    static final String REASON = "unknown-country";

    private int misses;

    /** Records one country reference that did not resolve. */
    void miss() {
        misses++;
    }

    /** How many references did not resolve so far. */
    int count() {
        return misses;
    }

    /**
     * Adds this tally to the counter and logs it, when anything was missed.
     *
     * @param ctx the fetch, for the source id and logger
     * @param meters supplies the registry, or null
     */
    void report(FetchContext ctx, Supplier<MeterRegistry> meters) {
        if (misses == 0) {
            return;
        }
        SkipCounter.count(meters, METRIC, "Country references the gazetteer could not resolve", ctx.sourceId(),
                Map.of(REASON, misses));
        ctx.log().debug("{}: {} country reference(s) did not resolve; those records have no geometry",
                ctx.sourceId(), misses);
    }

    /** A lazy registry supplier from an optional Spring provider (null when absent). */
    static Supplier<MeterRegistry> lazy(ObjectProvider<MeterRegistry> provider) {
        return provider == null ? () -> null : provider::getIfAvailable;
    }
}
