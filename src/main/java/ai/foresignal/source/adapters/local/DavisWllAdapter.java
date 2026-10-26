package ai.foresignal.source.adapters.local;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One {@code wxstation-davis-{n}} slot (prompt 069): a Davis WeatherLink Live console's current conditions
 * over its local API.
 *
 * <ul>
 *   <li><strong>Request:</strong> {@code GET http://<pinned>/v1/current_conditions}; the {@code url}
 *       parameter names the console ({@code http://192.168.1.70}, any path is replaced).
 *   <li><strong>Schedule:</strong> never more often than every 10 s (the Davis API's rule, enforced as the
 *       minimum interval); 60 s by default.
 *   <li><strong>Record:</strong> {@link DavisWllParser}: kind {@code weather-observation}, SI units,
 *       {@code externalId = did}, {@code observedAt = eventTime = ts}, coarse geometry from {@code lat} and
 *       {@code lon}; the policy of {@link WeatherStations}.
 * </ul>
 */
public class DavisWllAdapter implements PollingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "wxstation-davis-";

    /** The local API path. */
    public static final String PATH = "/v1/current_conditions";

    /** The Davis rule: no more often than every 10 s. */
    public static final Duration MIN_INTERVAL = Duration.ofSeconds(10);

    private final SourceDescriptor descriptor;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     */
    public DavisWllAdapter(int slot) {
        this(buildDescriptor(ID_PREFIX + slot, "Davis WeatherLink Live " + slot));
    }

    /**
     * Creates a console read with its own descriptor (prompt R2-023: the ground-sensor aggregator's
     * {@code wxstation-davis-aggregator}).
     *
     * @param descriptor the source's descriptor, from {@link #buildDescriptor(String, String)}
     */
    protected DavisWllAdapter(SourceDescriptor descriptor) {
        this.descriptor = descriptor;
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
            problems.put(WeatherStations.URL_PARAM, "must be the console's address only, for example http://192.168.1.70");
        }
        return problems;
    }

    /**
     * Reads the current conditions once.
     *
     * @throws AdapterException when the console cannot be reached or answers something else
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        UpstreamResponse response = ctx.http().get(requestUri(params.lanUrl(WeatherStations.URL_PARAM)));
        return FetchResult.complete(List.of(DavisWllParser.parse(response.body(),
                params.number(WeatherStations.LAT_PARAM), params.number(WeatherStations.LON_PARAM),
                params.bool(WeatherStations.INDOOR_PARAM, false))));
    }

    /** {@code http://host[:port]/v1/current_conditions} of the console URL; the host stays the pinned one. */
    static URI requestUri(URI console) {
        return console.resolve(PATH);
    }

    /**
     * The descriptor of a console read under {@code id}: a slot's, or the ground-sensor aggregator's (prompt R2-023).
     *
     * @param id the source id
     * @param name the display name
     * @return the descriptor
     */
    protected static SourceDescriptor buildDescriptor(String id, String name) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(id)
                .name(name)
                .description("Current conditions from the operator's Davis WeatherLink Live console over its local"
                        + " API (/v1/current_conditions), converted to SI units. Never more often than every 10 s"
                        + " (Davis's rule); 60 s by default. Location from lat/lon, rounded to about 1 km."
                        + LocalReceivers.LOCAL_NOTE)
                .homepage("https://weatherlink.github.io/weatherlink-live-local-api/")
                .observedAtMode(ObservedAtMode.UPSTREAM);
        WeatherStations.apply(builder, MIN_INTERVAL, "The WeatherLink Live console on the LAN, for example"
                + " http://192.168.1.70");
        return builder.build();
    }
}
