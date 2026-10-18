package ai.foresignal.plugin.feed;

import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.plugin.ParserOutput;
import ai.foresignal.plugin.PluginException;
import ai.foresignal.plugin.PluginParsers;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One {@code wasm-feed} source (prompt 141): an operator spec's URL fetched by the budgeted {@code SourceHttp} and
 * parsed by the named WASM plugin.
 *
 * <p><strong>Each run:</strong>
 * <ol>
 *   <li>the URL passes the {@link UrlGate} (the outbound URL policy, prompt 035), and the plugin could run now
 *       ({@link PluginParsers#requireAvailable}: plugins on, plugin enabled, hash still the reviewed one), else the run
 *       fails before any budget is spent;
 *   <li>a conditional GET through {@code ctx.http()} (a plain GET when the spec turns that off); a 304 is
 *       {@link FetchResult#unchanged()};
 *   <li>a 3xx (redirects are never followed), another non-200 status or a truncated body is an
 *       {@link UpstreamFormatException}, and the stored validators are forgotten;
 *   <li>the body goes to the plugin; a refusal or failure ({@link PluginException}: plugins off, plugin not enabled,
 *       hash mismatch, timeout, trap, oversized or malformed output) fails the run with the plugin's message as the
 *       health reason, and the validators are forgotten so the next run fetches the body again;
 *   <li>items the record mapper rejects are counted in {@value #REJECTED_METRIC}; the records go to the writer, whose
 *       normalizer applies the usual checks.
 * </ol>
 *
 * <p>The plugin's {@code complete: true} makes the result the upstream's full current set (absent records are then
 * reconciled), but only when no item was rejected: a rejected item would otherwise be read as a deletion.
 */
public class WasmFeedAdapter implements PollingAdapter {

    /** The counter of plugin output items that did not become records. */
    public static final String REJECTED_METRIC = "foresignal.plugin.rejected";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "wasm-feed";

    private final WasmFeedSpec spec;
    private final SourceDescriptor descriptor;
    private final PluginParsers parsers;
    private final UrlGate gate;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates the source.
     *
     * @param spec the checked spec
     * @param descriptor its descriptor
     * @param parsers runs the plugin
     * @param gate checks the URL before every fetch
     * @param meters supplies the meter registry, or null
     */
    public WasmFeedAdapter(WasmFeedSpec spec, SourceDescriptor descriptor, PluginParsers parsers, UrlGate gate,
            Supplier<MeterRegistry> meters) {
        this.spec = Objects.requireNonNull(spec);
        this.descriptor = Objects.requireNonNull(descriptor);
        this.parsers = Objects.requireNonNull(parsers);
        this.gate = Objects.requireNonNull(gate);
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The spec. */
    public WasmFeedSpec spec() {
        return spec;
    }

    /**
     * Fetches and parses once (see the class note).
     *
     * @throws AdapterException when the URL is refused, the request fails or the plugin refuses or fails
     * @throws UpstreamFormatException on a redirect, a bad status or a truncated body
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        URI uri = URI.create(spec.url().strip());
        String refusal = gate.refusal(uri);
        if (refusal != null) {
            throw new AdapterException(spec.id() + ": the URL is refused by the outbound URL policy: " + refusal);
        }
        try {
            parsers.requireAvailable(spec.plugin());
        } catch (PluginException e) {
            throw new AdapterException(spec.id() + ": " + e.getMessage(), e);
        }
        UpstreamResponse response = spec.wantsConditional()
                ? ctx.http().getConditional(uri, STATE_KEY, Map.of())
                : ctx.http().get(uri);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        byte[] body = checked(ctx, response);
        ParserOutput output;
        try {
            output = parsers.parse(spec.plugin(), body, descriptor);
        } catch (PluginException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new AdapterException(spec.id() + ": " + e.getMessage(), e);
        }
        count(ctx, output);
        ctx.log().debug("{}: plugin {} returned {} record(s), {} rejected, complete={}", ctx.sourceId(), spec.plugin(),
                output.records().size(), output.rejections().size(), output.complete());
        if (output.complete() && output.rejections().isEmpty()) {
            return FetchResult.complete(output.records());
        }
        return FetchResult.of(output.records());
    }

    private byte[] checked(FetchContext ctx, UpstreamResponse response) {
        int status = response.status();
        if (status >= 300 && status < 400) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(spec.id() + ": redirect refused (HTTP " + status + ")", "");
        }
        if (status != 200) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(spec.id() + ": the upstream answered HTTP " + status, "");
        }
        if (response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(spec.id() + ": the response exceeds " + descriptor.maxResponseBytes()
                    + " bytes", "");
        }
        return response.body();
    }

    private void count(FetchContext ctx, ParserOutput output) {
        if (output.rejections().isEmpty()) {
            return;
        }
        Map<String, Integer> byReason = new LinkedHashMap<>();
        for (ItemRejection rejection : output.rejections()) {
            byReason.merge(rejection.reason(), 1, Integer::sum);
        }
        ctx.log().debug("{}: rejected plugin items {}", ctx.sourceId(), byReason);
        MeterRegistry registry = meters.get();
        if (registry == null) {
            return;
        }
        byReason.forEach((reason, n) -> Counter.builder(REJECTED_METRIC).tag("source", ctx.sourceId())
                .tag("plugin", spec.plugin()).tag("reason", reason)
                .description("wasm-feed plugin output items not turned into records").register(registry)
                .increment(n));
    }
}
