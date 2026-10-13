package ai.foresignal.source.kit.feed;

import ai.foresignal.source.kit.KitUrls;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One feed-kit source (prompt 073): a reviewed {@link FeedSourceSpec} polled with a conditional GET and parsed by
 * {@link FeedParser}; items become records through {@link FeedRecords}.
 *
 * <p><strong>Each run:</strong>
 * <ol>
 *   <li>WebSub upkeep ({@link WebSubCoordinator#maintain}); while a subscription is active, a run within
 *       {@link #WEBSUB_SAFETY_NET} of the last real poll returns {@link FetchResult#unchanged()} without a request;
 *   <li>a conditional GET ({@code If-None-Match}/{@code If-Modified-Since}); a 304 is
 *       {@link FetchResult#unchanged()};
 *   <li>a 3xx is refused, another status, a truncated body or a document that does not parse is an
 *       {@link UpstreamFormatException}, and the stored validators are forgotten;
 *   <li>the items, filtered and capped, become records; skips are counted in {@value #SKIPPED_METRIC};
 *   <li>WebSub discovery ({@link WebSubCoordinator#afterPoll}).
 * </ol>
 * Feeds are windows, so the result is never complete and absent items are never tombstoned; they expire by
 * {@code recordTtl}. Item text is untrusted upstream data, kept as plain text only.
 */
public class KitFeedAdapter implements PollingAdapter {

    /** The counter of items not turned into records. */
    public static final String SKIPPED_METRIC = "foresignal.kit.feed.skipped";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "feed";

    /** The state key of the last real poll (ISO-8601). */
    public static final String LAST_POLL_KEY = "feed:lastPollAt";

    /** The poll interval while a WebSub subscription is active. */
    public static final Duration WEBSUB_SAFETY_NET = FeedSpecValidator.WEBSUB_POLL;

    private final FeedSourceSpec spec;
    private final SourceDescriptor descriptor;
    private final Supplier<WebSubCoordinator> websub;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates the source.
     *
     * @param spec the validated spec, with any test rebasing already applied
     * @param websub supplies the WebSub coordinator, or null for none
     * @param meters supplies the meter registry, or null
     */
    public KitFeedAdapter(FeedSourceSpec spec, Supplier<WebSubCoordinator> websub, Supplier<MeterRegistry> meters) {
        this.spec = Objects.requireNonNull(spec);
        this.descriptor = FeedDescriptors.descriptor(spec);
        this.websub = websub == null ? () -> WebSubCoordinator.DISABLED : websub;
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The spec. */
    public FeedSourceSpec spec() {
        return spec;
    }

    /**
     * Polls the feed once (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a redirect, a bad status, a truncated body or a document that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        WebSubCoordinator coordinator = coordinator();
        if (spec.wantsWebSub()) {
            coordinator.maintain(ctx, spec);
            if (coordinator.active(spec.id(), now)) {
                Instant last = ctx.state().get(LAST_POLL_KEY).map(Instant::parse).orElse(null);
                if (last != null && now.isBefore(last.plus(WEBSUB_SAFETY_NET))) {
                    return FetchResult.unchanged();
                }
            }
        }
        UpstreamResponse response = ctx.http().getConditional(URI.create(spec.url()), STATE_KEY, Map.of());
        ctx.state().put(LAST_POLL_KEY, now.toString());
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        byte[] body = checked(ctx, response);
        FeedDocument document;
        try {
            document = FeedParser.parse(body, response.contentType());
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        FeedRecords.Mapped mapped = FeedRecords.map(spec, document);
        count(ctx, mapped.skipped());
        if (!document.warnings().isEmpty()) {
            ctx.log().debug("{}: parser warnings {}", ctx.sourceId(), document.warnings());
        }
        ctx.log().debug("{}: {} item(s), {} record(s), skipped {}", ctx.sourceId(), document.items().size(),
                mapped.records().size(), mapped.skipped());
        if (spec.wantsWebSub()) {
            coordinator.afterPoll(ctx, spec, document);
        }
        return FetchResult.of(mapped.records());
    }

    private byte[] checked(FetchContext ctx, UpstreamResponse response) {
        int status = response.status();
        if (status >= 300 && status < 400) {
            throw new UpstreamFormatException(spec.id() + ": redirect refused", "");
        }
        if (status != 200) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(spec.id() + ": the feed answered HTTP " + status, "");
        }
        if (response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(spec.id() + ": the feed exceeds " + spec.effectiveMaxResponseBytes()
                    + " bytes", "");
        }
        return response.body();
    }

    private WebSubCoordinator coordinator() {
        WebSubCoordinator c = websub.get();
        return c == null ? WebSubCoordinator.DISABLED : c;
    }

    private void count(FetchContext ctx, Map<String, Integer> skipped) {
        MeterRegistry registry = meters.get();
        if (registry == null || skipped.isEmpty()) {
            return;
        }
        skipped.forEach((reason, n) -> Counter.builder(SKIPPED_METRIC).tag("source", ctx.sourceId())
                .tag("reason", reason).description("Feed-kit items not turned into records").register(registry)
                .increment(n));
    }

    /**
     * {@code spec} with its URL moved under a test base URL ({@link KitUrls#rebased}).
     *
     * @param spec the spec
     * @param base the test base URL, or blank
     */
    public static FeedSourceSpec rebased(FeedSourceSpec spec, String base) {
        if (base == null || base.isBlank()) {
            return spec;
        }
        return new FeedSourceSpec(spec.id(), spec.name(), spec.description(), KitUrls.rebased(spec.url(), base),
                spec.kind(), spec.language(), spec.categories(), spec.maxItems(), spec.recordTtl(), spec.ttlAnchor(),
                spec.observedAt(), spec.interval(), spec.minInterval(), spec.budget(), spec.license(), spec.termsUrl(),
                spec.attribution(), spec.homepage(), spec.itemMapper(), spec.websub(), spec.retention(),
                spec.maxResponseBytes());
    }
}
