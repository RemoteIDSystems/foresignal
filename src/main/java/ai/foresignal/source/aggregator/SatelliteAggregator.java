package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.goes.GoesAbiAdapter;
import ai.foresignal.source.adapters.goes.GoesWestAbiAdapter;
import ai.foresignal.source.adapters.satdump.SatDumpAggregatorAdapter;
import ai.foresignal.source.adapters.satdump.SatDumpDescriptor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The {@code satellite} domain (prompt R2-022): GOES and weather satellites received off the operator's own dish by a
 * local SatDump, in place of the NOAA imagery pulls of the same satellite.
 *
 * <p>The read is the {@code satdump-aggregator} ground station ({@link SatDumpAggregatorAdapter}). {@code endpoint} is
 * SatDump's web server ({@code http://host:port}, with any base path; its {@code /api} is read); {@code config} carries the station:
 *
 * <ul>
 *   <li>{@code location} (required with an endpoint): {@code lat,lon[,altM]} or a place id;
 *   <li>{@code inboxPath}: SatDump's output directory under {@code FORESIGNAL_INBOX_DIR}, where passes, products and
 *       xRIT frames are read;
 *   <li>{@code satdumpVersion}: {@code 1.2} or {@code 2.0};
 *   <li>{@code predictNorad}: the polar satellites whose passes are predicted;
 *   <li>{@code goes}: {@code east} or {@code west}, the GOES the dish receives.
 * </ul>
 *
 * <p>Applying writes them into the station and turns it on while the aggregator is live, off otherwise. It supersedes
 * the ABI imagery pull of the GOES named by {@code goes} ({@code goes-east-abi} or {@code goes-west-abi}) and nothing
 * else: without {@code goes} (a polar-only station) no NOAA pull is replaced. GLM lightning ({@code goes-glm}) and ABI
 * fire detection ({@code goes-fdc}) are never superseded: a dish's HRIT broadcast carries neither, and the local GLM
 * from a GRB station ({@code glm-local-*}) is designed to run alongside {@code goes-glm}.
 */
@Component
public class SatelliteAggregator implements DomainAggregator {

    /** The domain. */
    public static final String DOMAIN = "satellite";

    /** The {@code config} member naming the station's position. */
    static final String LOCATION = SatDumpDescriptor.LOCATION;

    /** The {@code config} member naming SatDump's output directory. */
    static final String INBOX_PATH = SatDumpDescriptor.INBOX_PATH;

    /** The {@code config} member naming the SatDump version. */
    static final String SATDUMP_VERSION = SatDumpDescriptor.SATDUMP_VERSION;

    /** The {@code config} member listing the predicted polar satellites. */
    static final String PREDICT_NORAD = SatDumpDescriptor.PREDICT_NORAD;

    /** The {@code config} member naming the GOES the dish receives. */
    static final String GOES = "goes";

    /** The ABI imagery pull each {@code goes} value supersedes. */
    static final Map<String, String> GOES_IMAGERY = Map.of("east", GoesAbiAdapter.ID, "west", GoesWestAbiAdapter.ID);

    /** The {@code config} members. */
    static final Set<String> CONFIG_KEYS = Set.of(LOCATION, INBOX_PATH, SATDUMP_VERSION, PREDICT_NORAD, GOES);

    /** How long {@link #test} runs the station, in seconds. */
    static final int TEST_SECONDS = ProbeService.DEFAULT_SECONDS;

    private final SourceService sources;
    private final SourceRegistry registry;
    private final ProbeService probes;

    /** Creates the domain. */
    public SatelliteAggregator(SourceService sources, SourceRegistry registry, ProbeService probes) {
        this.sources = sources;
        this.registry = registry;
        this.probes = probes;
    }

    @Override
    public String domain() {
        return DOMAIN;
    }

    @Override
    public String name() {
        return "Direct satellite reception";
    }

    @Override
    public List<String> sourceIds() {
        return List.of(SatDumpAggregatorAdapter.ID);
    }

    /** Every imagery pull this domain can supersede: both GOES ABI sources that are registered. */
    @Override
    public List<String> supersededSourceIds() {
        return GOES_IMAGERY.values().stream().filter(registry.ids()::contains).sorted().toList();
    }

    /** The ABI imagery pull of the GOES {@code settings} receives, or none. */
    @Override
    public List<String> supersededSourceIds(AggregatorSettings settings) {
        String id = GOES_IMAGERY.get(String.valueOf(settings.config().get(GOES)));
        return id == null || !registry.ids().contains(id) ? List.of() : List.of(id);
    }

    @Override
    public void validate(AggregatorSettings proposed, List<FieldError> errors) {
        if (proposed.enabled() && proposed.endpoint() == null) {
            errors.add(new FieldError("endpoint", "is required to enable direct satellite reception: SatDump's web"
                    + " server, for example http://satdump:8081"));
        }
        if (proposed.endpoint() != null) {
            String problem = endpointProblem(proposed.endpoint());
            if (problem != null) {
                errors.add(new FieldError("endpoint", problem));
            }
            if (!(proposed.config().get(LOCATION) instanceof String location) || location.isBlank()) {
                errors.add(new FieldError("config." + LOCATION, "is required with an endpoint: the station position,"
                        + " lat,lon[,altM] or a place id"));
            }
        }
        for (Map.Entry<String, Object> entry : proposed.config().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!CONFIG_KEYS.contains(key)) {
                errors.add(new FieldError("config." + key, "is not a satellite aggregator setting"));
            } else if (GOES.equals(key) && !GOES_IMAGERY.containsKey(String.valueOf(value))) {
                errors.add(new FieldError("config." + key, "must be east or west, the GOES the dish receives"));
            } else if (PREDICT_NORAD.equals(key) && !(value instanceof List<?>)) {
                errors.add(new FieldError("config." + key, "must be a list of NORAD catalog numbers"));
            } else if (!PREDICT_NORAD.equals(key) && !GOES.equals(key) && !(value instanceof String)) {
                errors.add(new FieldError("config." + key, "must be a string"));
            }
        }
    }

    /** Why {@code endpoint} is not SatDump's web server (a base path is kept: {@code {endpoint}/api}), or null. */
    static String endpointProblem(String endpoint) {
        URI uri;
        try {
            uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            return "must be a URL such as http://satdump:8081";
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null) {
            return "must be an http or https URL with a host";
        }
        if (uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            return "must not carry credentials, a query or a fragment";
        }
        return null;
    }

    @Override
    public void apply(AggregatorSettings settings) {
        String id = SatDumpAggregatorAdapter.ID;
        if (settings.endpoint() == null) {
            if (sources.view(id).enabled()) {
                sources.update(id, SourceSettingsUpdate.enabled(false));
            }
            return;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(SatDumpDescriptor.API_URL, settings.endpoint());
        for (String key : List.of(LOCATION, INBOX_PATH, SATDUMP_VERSION)) {
            Object value = settings.config().get(key);
            if (value != null) {
                params.put(key, value);
            }
        }
        if (settings.config().get(PREDICT_NORAD) instanceof List<?> norad) {
            params.put(PREDICT_NORAD, norad.stream().map(String::valueOf).toList());
        }
        try {
            sources.update(id, new SourceSettingsUpdate(settings.live(), null, params, null, null, false, null,
                    false));
        } catch (ValidationException e) {
            List<FieldError> errors = new ArrayList<>();
            for (FieldError error : e.getErrors()) {
                errors.add(new FieldError(remap(error.field()), error.message()));
            }
            if (errors.isEmpty()) {
                errors.add(new FieldError("endpoint", e.getMessage()));
            }
            throw new ValidationException(e.getCode(), "The satellite aggregator's station is not usable.", errors);
        }
    }

    /** A station parameter's error path, as the aggregator's field. */
    static String remap(String field) {
        if (field.contains(SatDumpDescriptor.API_URL)) {
            return "endpoint";
        }
        for (String key : List.of(LOCATION, INBOX_PATH, SATDUMP_VERSION, PREDICT_NORAD)) {
            if (field.contains(key)) {
                return "config." + key;
            }
        }
        return field;
    }

    /**
     * Runs the station for {@value #TEST_SECONDS} seconds through the probe service — its own {@code /api} read,
     * inbox scan, budget and pinning — and counts the records it produced: the station state, passes and products
     * (at most {@value ProbeResult#MAX_RECORDS}).
     */
    @Override
    public Map<String, Object> test(AggregatorSettings settings) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (settings.endpoint() == null) {
            return result(out, false, false, null, "No endpoint is configured.");
        }
        ProbeResult probe;
        try {
            probe = probes.probe(SatDumpAggregatorAdapter.ID, null, TEST_SECONDS);
        } catch (RuntimeException e) {
            return result(out, false, false, null, "SatDump could not be read: " + e.getMessage());
        }
        if (probe.parseError() != null) {
            return result(out, false, false, null, "SatDump could not be read: " + probe.parseError());
        }
        int count = probe.records().size();
        if (count == 0) {
            return result(out, false, true, 0, "SatDump answered, but produced no station state, pass or product in "
                    + TEST_SECONDS + " s.");
        }
        return result(out, true, true, count, null);
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
