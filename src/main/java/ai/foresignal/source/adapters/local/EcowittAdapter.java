package ai.foresignal.source.adapters.local;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * One {@code wxstation-ecowitt-{n}} slot (prompt 069, <strong>experimental</strong>): an Ecowitt gateway's
 * live data over its local HTTP endpoint.
 *
 * <ul>
 *   <li><strong>Request:</strong> {@code GET http://<pinned>/get_livedata_info}; the {@code url} parameter
 *       names the gateway.
 *   <li><strong>Record:</strong> {@link EcowittLiveDataParser}, the record model, coarse geometry and policy of
 *       Davis ({@link WeatherStations}); {@code externalId} is the source id, because the endpoint names no
 *       device.
 *   <li><strong>Time:</strong> the body carries no timestamp, so {@code observedAtMode} is RECEIVED (the
 *       framework imputes it and flags {@code TIME_IMPUTED}).
 *   <li><strong>Counters:</strong> unknown {@code common_list} ids add to
 *       {@code foresignal.ecowitt.unknown_ids{source}}.
 * </ul>
 *
 * <p>Ecowitt's form-POST push ("Customized upload") is the 059 {@code ecowitt} preset, not this source.
 */
public class EcowittAdapter implements PollingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "wxstation-ecowitt-";

    /** The gateway path. */
    public static final String PATH = "/get_livedata_info";

    /** The counter of unknown {@code common_list} ids. */
    public static final String UNKNOWN_IDS_METRIC = "foresignal.ecowitt.unknown_ids";

    private final SourceDescriptor descriptor;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     * @param meters the registry of the unknown-id counter, or a supplier of null
     */
    public EcowittAdapter(int slot, Supplier<MeterRegistry> meters) {
        this.descriptor = buildDescriptor(slot);
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The coordinates must be in range; the URL must carry no credentials, query or fragment. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>(WeatherStations.checkCoordinates(params));
        URI url = params.lanUrl(WeatherStations.URL_PARAM);
        if (url != null && (url.getRawUserInfo() != null || url.getRawQuery() != null || url.getRawFragment() != null)) {
            problems.put(WeatherStations.URL_PARAM, "must be the gateway's address only, for example http://192.168.1.80");
        }
        return problems;
    }

    /**
     * Reads the live data once.
     *
     * @throws AdapterException when the gateway cannot be reached or the body is not live data
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        URI uri = params.lanUrl(WeatherStations.URL_PARAM).resolve(PATH);
        UpstreamResponse response = ctx.http().get(uri);
        EcowittLiveDataParser.Result result = EcowittLiveDataParser.parse(response.body(), ctx.sourceId(),
                params.number(WeatherStations.LAT_PARAM), params.number(WeatherStations.LON_PARAM),
                params.bool(WeatherStations.INDOOR_PARAM, false));
        if (result.unknownIds() > 0) {
            ctx.log().debug("{} common_list ids were not recognized", result.unknownIds());
            MeterRegistry registry = meters.get();
            if (registry != null) {
                Counter.builder(UNKNOWN_IDS_METRIC).tag("source", ctx.sourceId()).register(registry)
                        .increment(result.unknownIds());
            }
        }
        return FetchResult.complete(List.of(result.record()));
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Ecowitt gateway " + slot)
                .description("Experimental: live data from the operator's Ecowitt gateway (/get_livedata_info), which"
                        + " has no official specification and changes with firmware. Units are read from the"
                        + " response and converted to SI. 60 s by default. For Ecowitt's form-POST push use the"
                        + " ecowitt mapping preset instead." + LocalReceivers.LOCAL_NOTE)
                .homepage("https://github.com/Ecowitt/ha-ecowitt-iot")
                .observedAtMode(ObservedAtMode.RECEIVED);
        WeatherStations.apply(builder, Duration.ofSeconds(10), "The Ecowitt gateway on the LAN, for example"
                + " http://192.168.1.80");
        return builder.build();
    }
}
