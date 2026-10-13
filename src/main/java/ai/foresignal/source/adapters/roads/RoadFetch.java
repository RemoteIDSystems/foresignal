package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fetch rules shared by the road-event adapters (prompt 074).
 *
 * <ul>
 *   <li>{@link #body}: a 200 body, or an {@link UpstreamFormatException} for a 3xx the client did not follow,
 *       another status or a truncated body; stored validators are forgotten and the absence state is left alone.
 *   <li>{@link #records}: the events of a complete or partial fetch plus their tombstones. Events the upstream marks
 *       ended are tombstoned at once. Only after a complete fetch does {@link AbsenceTombstoner} run, so an event
 *       missing from two consecutive complete fetches is tombstoned. A well-formed empty document counts as
 *       complete: road-event feeds are legitimately empty at times, and two consecutive empty documents are needed
 *       before anything is retired.
 *   <li>{@link #count}: skipped entries in {@value #SKIPPED_METRIC}, tagged {@code source} and {@code reason}.
 * </ul>
 */
public final class RoadFetch {

    /** The counter of entries the parsers skipped. */
    public static final String SKIPPED_METRIC = "foresignal.roads.skipped";

    /** The placeholder of Foresignal's version in a header value. */
    public static final String VERSION_PLACEHOLDER = "{version}";

    private RoadFetch() {}

    /**
     * The body of a 200 response.
     *
     * @param sourceId names the source in the error
     * @param stateKey the {@code getConditional} validators to forget on failure, or null
     * @param scope the absence scope (left untouched on failure), or null
     * @throws UpstreamFormatException on a 3xx, another status or a truncated body
     */
    static byte[] body(FetchContext ctx, UpstreamResponse response, String sourceId, String stateKey, String scope) {
        int status = response.status();
        if (status >= 300 && status < 400) {
            forget(ctx, stateKey);
            throw new UpstreamFormatException(sourceId + ": unexpected redirect (HTTP " + status + ")", "");
        }
        if (status != 200) {
            forget(ctx, stateKey);
            throw new UpstreamFormatException(sourceId + ": the upstream answered HTTP " + status, "");
        }
        if (response.truncated()) {
            forget(ctx, stateKey);
            AbsenceTombstoner.update(ctx.state(), scope, AbsenceTombstoner.Outcome.TRUNCATED, List.of());
            throw new UpstreamFormatException(sourceId + ": the response exceeds the "
                    + ctx.descriptor().maxResponseBytes() + "-byte cap", "");
        }
        return response.body();
    }

    /** Forgets the validators stored under {@code stateKey}, when given. */
    static void forget(FetchContext ctx, String stateKey) {
        if (stateKey != null) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
        }
    }

    /**
     * The records of one parsed scope: its events, a tombstone per ended id, and, when {@code complete}, a tombstone
     * per id missing from two consecutive complete fetches.
     *
     * @param parsed the parsed document(s) of the scope
     * @param scope the absence scope, or null for the source's only one
     * @param complete true when the whole current set arrived
     * @param now the tombstones' {@code observedAt}
     */
    static List<NormalizedRecord> records(FetchContext ctx, ParsedEvents parsed, String scope, boolean complete,
            Instant now) {
        List<NormalizedRecord> out = new ArrayList<>(parsed.records());
        out.addAll(AbsenceTombstoner.tombstones(parsed.endedIds(), RoadEvents.KIND, now));
        if (complete) {
            List<String> gone = AbsenceTombstoner.update(ctx.state(), scope, AbsenceTombstoner.Outcome.COMPLETE,
                    parsed.ids());
            List<String> extra = new ArrayList<>(gone);
            extra.removeAll(parsed.endedIds());
            out.addAll(AbsenceTombstoner.tombstones(extra, RoadEvents.KIND, now));
            if (!extra.isEmpty()) {
                ctx.log().debug("{}: tombstoned {} event(s) missing from two complete fetches", ctx.sourceId(),
                        extra.size());
            }
        }
        return out;
    }

    /** Counts {@code skipped} under {@value #SKIPPED_METRIC}. */
    static void count(Supplier<MeterRegistry> meters, String sourceId, Map<String, Integer> skipped) {
        MeterRegistry registry = meters == null ? null : meters.get();
        if (registry == null) {
            return;
        }
        skipped.forEach((reason, amount) -> Counter.builder(SKIPPED_METRIC).tag("source", sourceId)
                .tag("reason", reason).description("Road-event entries skipped by the parsers")
                .register(registry).increment(amount));
    }

    /**
     * {@code url} moved under a test base URL as {@code {base}/{host}{path}?{query}}, or unchanged when {@code base}
     * is blank (the {@code foresignal.test.adapters.roads.base-url} property).
     */
    public static URI rebased(URI url, String base) {
        if (base == null || base.isBlank()) {
            return url;
        }
        String root = base.strip();
        while (root.endsWith("/")) {
            root = root.substring(0, root.length() - 1);
        }
        return URI.create(root + "/" + url.getHost() + (url.getRawPath() == null ? "" : url.getRawPath())
                + (url.getRawQuery() == null ? "" : "?" + url.getRawQuery()));
    }

    /** The test-only property moving every road-event URL under one base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.roads.base-url";
}
