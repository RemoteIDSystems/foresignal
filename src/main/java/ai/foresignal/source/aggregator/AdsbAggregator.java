package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.adapters.internet.GpsjamAdapter;
import ai.foresignal.source.adapters.local.LocalAdsbAdapter;
import ai.foresignal.source.adapters.local.LocalAdsbAggregatorAdapter;
import ai.foresignal.source.formats.readsb.ReadsbDocument;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * The {@code adsb} domain (prompt R2-019): the operator's own readsb, read whole from the LAN or this host, in place
 * of the public mirrors of the same data.
 *
 * <p>The read is the {@code local-adsb-aggregator} source ({@link LocalAdsbAggregatorAdapter}). Applying a
 * configuration writes its {@code url} parameter (the {@code endpoint}) and {@code band} (from {@code config.band}),
 * and turns it on while the aggregator is live and off otherwise. The endpoint is therefore checked like any
 * {@code LAN_URL}: it must resolve to a loopback, private or unique-local address, it is pinned, and it is never sent
 * through an egress route. It must name the {@code aircraft.json} file, for example
 * {@code http://readsb/data/aircraft.json} for the compose profile.
 *
 * <p>It supersedes the public mirrors of the same data — every registered {@code adsblol-*}, {@code adsbfi-*} and
 * {@code airplaneslive-*} source — and {@code gpsjam-daily}, since GPS jamming is derived from the local feed
 * ({@code derived-gps-jamming} reads every {@code local-adsb-*} source). {@code mode} records how the readsb is fed
 * ({@code receiver}, {@code feed} or {@code hybrid}); the server reads the same file whichever it is.
 */
@Component
public class AdsbAggregator implements DomainAggregator {

    /** The domain. */
    public static final String DOMAIN = "adsb";

    /** The prefixes of the public mirror sources. */
    static final List<String> MIRROR_PREFIXES = List.of("adsblol-", "adsbfi-", "airplaneslive-");

    /** The {@code config} members. */
    static final Set<String> CONFIG_KEYS = Set.of(LocalAdsbAdapter.BAND_PARAM);

    private static final Pattern AIRCRAFT_JSON = Pattern.compile("(?:^|.*/)aircraft\\.json$");

    private final SourceService sources;
    private final SourceRegistry registry;
    private final UpstreamClient upstream;

    /** Creates the domain. */
    public AdsbAggregator(SourceService sources, SourceRegistry registry, UpstreamClient upstream) {
        this.sources = sources;
        this.registry = registry;
        this.upstream = upstream;
    }

    @Override
    public String domain() {
        return DOMAIN;
    }

    @Override
    public String name() {
        return "Local ADS-B aggregator";
    }

    @Override
    public List<String> sourceIds() {
        return List.of(LocalAdsbAggregatorAdapter.ID);
    }

    @Override
    public List<String> supersededSourceIds() {
        return registry.ids().stream()
                .filter(id -> MIRROR_PREFIXES.stream().anyMatch(id::startsWith) || id.equals(GpsjamAdapter.ID))
                .sorted().toList();
    }

    @Override
    public void validate(AggregatorSettings proposed, List<FieldError> errors) {
        if (proposed.enabled() && proposed.endpoint() == null) {
            errors.add(new FieldError("endpoint", "is required to enable the ADS-B aggregator: the readsb"
                    + " aircraft.json, for example http://readsb/data/aircraft.json"));
        }
        if (proposed.endpoint() != null) {
            String problem = endpointProblem(proposed.endpoint());
            if (problem != null) {
                errors.add(new FieldError("endpoint", problem));
            }
        }
        for (Map.Entry<String, Object> entry : proposed.config().entrySet()) {
            if (!CONFIG_KEYS.contains(entry.getKey())) {
                errors.add(new FieldError("config." + entry.getKey(), "is not an ADS-B aggregator setting"));
            } else if (!"1090".equals(String.valueOf(entry.getValue()))
                    && !"978".equals(String.valueOf(entry.getValue()))) {
                errors.add(new FieldError("config.band", "must be 1090 or 978"));
            }
        }
    }

    /** Why {@code endpoint} is not an http(s) URL of an {@code aircraft.json}, or null when it is. */
    static String endpointProblem(String endpoint) {
        URI uri;
        try {
            uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            return "must be a URL such as http://readsb/data/aircraft.json";
        }
        if (uri.getScheme() == null || !(uri.getScheme().equalsIgnoreCase("http")
                || uri.getScheme().equalsIgnoreCase("https")) || uri.getHost() == null) {
            return "must be an http or https URL with a host";
        }
        if (uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            return "must not carry credentials, a query or a fragment";
        }
        if (uri.getRawPath() == null || !AIRCRAFT_JSON.matcher(uri.getRawPath()).matches()) {
            return "must name the aircraft.json file, for example http://readsb/data/aircraft.json";
        }
        return null;
    }

    @Override
    public void apply(AggregatorSettings settings) {
        String id = LocalAdsbAggregatorAdapter.ID;
        if (settings.endpoint() == null) {
            SourceView current = sources.view(id);
            if (current.enabled()) {
                sources.update(id, SourceSettingsUpdate.enabled(false));
            }
            return;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(LocalAdsbAdapter.URL_PARAM, settings.endpoint());
        params.put(LocalAdsbAdapter.BAND_PARAM, String.valueOf(settings.config().getOrDefault(
                LocalAdsbAdapter.BAND_PARAM, "1090")));
        try {
            sources.update(id, new SourceSettingsUpdate(settings.live(), null, params, null, null, false, null,
                    false));
        } catch (ValidationException e) {
            List<FieldError> errors = new ArrayList<>();
            for (FieldError error : e.getErrors()) {
                errors.add(new FieldError(error.field().contains(LocalAdsbAdapter.URL_PARAM) ? "endpoint"
                        : error.field(), error.message()));
            }
            if (errors.isEmpty()) {
                errors.add(new FieldError("endpoint", e.getMessage()));
            }
            throw new ValidationException(e.getCode(), "The ADS-B aggregator's endpoint is not usable.", errors);
        }
    }

    @Override
    public Map<String, Object> test(AggregatorSettings settings) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (settings.endpoint() == null) {
            return result(out, false, false, null, "No endpoint is configured.");
        }
        UpstreamResponse response;
        try {
            response = upstream.forSource(LocalAdsbAggregatorAdapter.ID,
                    registry.descriptor(LocalAdsbAggregatorAdapter.ID)).get(URI.create(settings.endpoint()));
        } catch (UpstreamHttpException e) {
            return result(out, false, true, null, "The endpoint answered HTTP " + e.getStatus() + ".");
        } catch (AdapterException | RuntimeException e) {
            return result(out, false, false, null, "The endpoint could not be read: " + e.getMessage());
        }
        if (response.truncated()) {
            return result(out, false, true, null, "aircraft.json is larger than the source's response cap.");
        }
        try {
            ReadsbDocument document = ReadsbDocument.parse(response.body());
            return result(out, true, true, document.aircraft().size(), null);
        } catch (RuntimeException e) {
            return result(out, false, true, null, "The endpoint did not answer with readsb aircraft.json: "
                    + e.getMessage());
        }
    }

    private static Map<String, Object> result(Map<String, Object> out, boolean ok, boolean reachable, Integer count,
            String message) {
        out.put("ok", ok);
        out.put("reachable", reachable);
        out.put("sampleCount", count);
        out.put("message", message);
        return out;
    }
}
