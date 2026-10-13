package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.Supplier;

/**
 * One {@code transit-{id}} (or {@code transit-mdb-{id}}) source (prompt 070): a GTFS-RT VehiclePositions feed as
 * TRACK {@code transit-vehicle} records ({@link TransitDescriptors#vehicles}, {@link GtfsRtVehicleParser}).
 *
 * <p>One conditional GET per run with the feed's headers (Entur's {@code ET-Client-Name: foresignal-{installId}});
 * a 304 changes nothing. A truncated, HTML, JSON or unparseable body is an {@code UpstreamFormatException}. Vehicles
 * are never tombstoned: a vehicle that stops reporting expires with the 5-minute TTL. Vehicles dropped for lack of a
 * timestamp under a VEHICLE time base are counted in {@value #UNTIMED_METRIC}{@code {source}}.
 */
public class TransitVehicleAdapter implements PollingAdapter {

    /** The counter of vehicles dropped for lack of a timestamp. */
    public static final String UNTIMED_METRIC = "foresignal.transit.untimed";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "vehicles";

    private final TransitFeed feed;
    private final SourceDescriptor descriptor;
    private final Supplier<String> installId;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates the source.
     *
     * @param feed the feed entry; its {@code vehiclePositionsUrl} must be set
     * @param installId supplies the install id for the feed's headers
     * @param meters supplies the registry of the untimed counter, or null
     */
    public TransitVehicleAdapter(TransitFeed feed, Supplier<String> installId, Supplier<MeterRegistry> meters) {
        if (feed.vehiclePositionsUrl() == null) {
            throw new IllegalArgumentException(feed.id() + " has no VehiclePositions URL");
        }
        this.feed = feed;
        this.descriptor = TransitDescriptors.vehicles(feed);
        this.installId = installId == null ? () -> null : installId;
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The feed entry. */
    public TransitFeed feed() {
        return feed;
    }

    /** The request headers, with the install id filled in. */
    public java.util.Map<String, String> requestHeaders() {
        return feed.resolvedHeaders(installId.get());
    }

    /**
     * Fetches the feed once.
     *
     * @return {@link FetchResult#unchanged()} on a 304; otherwise the vehicles, complete when the feed is a
     *     FULL_DATASET
     * @throws AdapterException when the request fails or the body is not a GTFS-RT FeedMessage
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        FeedMessage message = GtfsRt.fetch(ctx, feed.vehiclePositionsUrl(), STATE_KEY, requestHeaders());
        if (message == null) {
            return FetchResult.unchanged();
        }
        GtfsRtVehicleParser.Result result = GtfsRtVehicleParser.parse(message, feed);
        if (result.untimed() > 0) {
            MeterRegistry registry = meters.get();
            if (registry != null) {
                Counter.builder(UNTIMED_METRIC).tag("source", ctx.sourceId())
                        .description("GTFS-RT vehicles dropped because the feed's time base gave them no timestamp")
                        .register(registry).increment(result.untimed());
            }
        }
        ctx.log().debug("{}: {} vehicle(s), {} untimed, {} skipped", ctx.sourceId(), result.records().size(),
                result.untimed(), result.skipped());
        return result.fullDataset() ? FetchResult.complete(result.records()) : FetchResult.of(result.records());
    }
}
