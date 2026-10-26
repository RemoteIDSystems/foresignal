package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.adapters.ais.LocalAisAdapter;
import ai.foresignal.source.adapters.ais.LocalAisAggregatorAdapter;
import ai.foresignal.source.adapters.aisstream.AisStreamAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The {@code ais} domain (prompt R2-020): the operator's own AIS decoder — AIS-catcher, rtl-ais or any decoder that
 * emits AIVDM NMEA lines, fed by its own receivers, by feeds it aggregates, or both — read whole from the LAN or this
 * host, in place of the public mirror of the same data.
 *
 * <p>The read is the {@code local-ais-aggregator} source ({@link LocalAisAggregatorAdapter}). Applying a configuration
 * writes its {@code endpoint} parameter and, from {@code config}, {@code allowSenders} and {@code stationName}, and
 * turns it on while the aggregator is live and off otherwise. The endpoint is therefore checked like any
 * {@code LAN_URL}: a {@code tcp://host:port} must resolve to a loopback, private or unique-local address and is
 * pinned; a {@code udp://bindHost:port} is a local bind and needs {@code config.allowSenders}. It is never sent
 * through an egress route.
 *
 * <p>It supersedes {@code aisstream}, the only public global-AIS mirror registered. The official regional feeds
 * ({@code ais-digitraffic}, {@code ais-kystverket}) are authorities, not mirrors, and are never touched; neither are
 * the {@code local-ais-{n}} slots nor the maritime event sources. {@code mode} records how the decoder is fed
 * ({@code receiver}, {@code feed} or {@code hybrid}); the server reads the same stream whichever it is.
 */
@Component
public class AisAggregator implements DomainAggregator {

    /** The domain. */
    public static final String DOMAIN = "ais";

    /** The {@code config} member listing the UDP senders. */
    static final String ALLOW_SENDERS = LocalAisAggregatorAdapter.ALLOW_SENDERS;

    /** The {@code config} member naming the station. */
    static final String STATION_NAME = LocalAisAdapter.STATION_NAME;

    /** The {@code config} members. */
    static final Set<String> CONFIG_KEYS = Set.of(ALLOW_SENDERS, STATION_NAME);

    /** How long {@link #test} listens to the endpoint, in seconds. */
    static final int TEST_SECONDS = ProbeService.DEFAULT_SECONDS;

    private final SourceService sources;
    private final SourceRegistry registry;
    private final ProbeService probes;
    private final LocalAisAggregatorAdapter adapter;
    private final Clock clock;

    /** Creates the domain. */
    public AisAggregator(SourceService sources, SourceRegistry registry, ProbeService probes,
            LocalAisAggregatorAdapter adapter, Clock clock) {
        this.sources = sources;
        this.registry = registry;
        this.probes = probes;
        this.adapter = adapter;
        this.clock = clock;
    }

    @Override
    public String domain() {
        return DOMAIN;
    }

    @Override
    public String name() {
        return "Local AIS aggregator";
    }

    @Override
    public List<String> sourceIds() {
        return List.of(LocalAisAggregatorAdapter.ID);
    }

    @Override
    public List<String> supersededSourceIds() {
        return registry.ids().stream().filter(AisStreamAdapter.ID::equals).toList();
    }

    @Override
    public void validate(AggregatorSettings proposed, List<FieldError> errors) {
        if (proposed.enabled() && proposed.endpoint() == null) {
            errors.add(new FieldError("endpoint", "is required to enable the AIS aggregator: the decoder's NMEA feed,"
                    + " for example tcp://ais-catcher:5012 or udp://0.0.0.0:10110"));
        }
        String scheme = null;
        if (proposed.endpoint() != null) {
            scheme = scheme(proposed.endpoint());
            if (scheme == null) {
                errors.add(new FieldError("endpoint", "must be tcp://host:port or udp://bindHost:port with an explicit"
                        + " port"));
            }
        }
        for (Map.Entry<String, Object> entry : proposed.config().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!CONFIG_KEYS.contains(key)) {
                errors.add(new FieldError("config." + key, "is not an AIS aggregator setting"));
            } else if (ALLOW_SENDERS.equals(key) && !(value instanceof List<?>)) {
                errors.add(new FieldError("config." + key, "must be a list of IPs or CIDRs"));
            } else if (STATION_NAME.equals(key) && !(value instanceof String)) {
                errors.add(new FieldError("config." + key, "must be a string"));
            }
        }
        if ("udp".equals(scheme) && !(proposed.config().get(ALLOW_SENDERS) instanceof List<?> list
                && !list.isEmpty())) {
            errors.add(new FieldError("config." + ALLOW_SENDERS, "is required with a udp:// endpoint: list the IPs or"
                    + " CIDRs of the decoder hosts"));
        }
    }

    /** The scheme of {@code endpoint}, {@code tcp} or {@code udp}, or null when it is not one with host and port. */
    static String scheme(String endpoint) {
        URI uri;
        try {
            uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            return null;
        }
        if (uri.getScheme() == null || uri.getHost() == null || uri.getPort() < 1) {
            return null;
        }
        String scheme = uri.getScheme().toLowerCase(java.util.Locale.ROOT);
        return scheme.equals("tcp") || scheme.equals("udp") ? scheme : null;
    }

    @Override
    public void apply(AggregatorSettings settings) {
        String id = LocalAisAggregatorAdapter.ID;
        if (settings.endpoint() == null) {
            SourceView current = sources.view(id);
            if (current.enabled()) {
                sources.update(id, SourceSettingsUpdate.enabled(false));
            }
            return;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(LocalAisAdapter.ENDPOINT, settings.endpoint());
        Object senders = settings.config().get(ALLOW_SENDERS);
        if (senders != null) {
            params.put(ALLOW_SENDERS, senders);
        }
        Object station = settings.config().get(STATION_NAME);
        if (station != null) {
            params.put(STATION_NAME, station);
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
            throw new ValidationException(e.getCode(), "The AIS aggregator's endpoint is not usable.", errors);
        }
    }

    /** A source parameter's error path, as the aggregator's field. */
    static String remap(String field) {
        if (field.contains(ALLOW_SENDERS)) {
            return "config." + ALLOW_SENDERS;
        }
        if (field.contains(STATION_NAME)) {
            return "config." + STATION_NAME;
        }
        if (field.contains(LocalAisAdapter.ENDPOINT)) {
            return "endpoint";
        }
        return field;
    }

    /**
     * Listens to the stored endpoint for {@value #TEST_SECONDS} seconds through the probe service — the source's own
     * socket path, budget and pinning — and counts the vessel records decoded (at most {@value
     * ProbeResult#MAX_RECORDS}). A {@code udp://} endpoint that the live source already holds cannot be bound twice,
     * so it is reported from the live source's statistics instead: {@code sampleCount} is then the distinct MMSIs of
     * the last 10 minutes.
     */
    @Override
    public Map<String, Object> test(AggregatorSettings settings) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (settings.endpoint() == null) {
            return result(out, false, false, null, "No endpoint is configured.");
        }
        if ("udp".equals(scheme(settings.endpoint()))
                && sources.view(LocalAisAggregatorAdapter.ID).enabled()) {
            return fromStatistics(out);
        }
        ProbeResult probe;
        try {
            probe = probes.probe(LocalAisAggregatorAdapter.ID, null, TEST_SECONDS);
        } catch (RuntimeException e) {
            return result(out, false, false, null, "The endpoint could not be read: " + e.getMessage());
        }
        if (probe.parseError() != null) {
            return result(out, false, false, null, "The endpoint could not be read: " + probe.parseError());
        }
        int count = probe.records().size();
        if (count == 0) {
            return result(out, false, true, 0, "Connected, but no AIS message was decoded in " + TEST_SECONDS
                    + " s.");
        }
        return result(out, true, true, count, count >= ProbeResult.MAX_RECORDS
                ? "At least " + count + " records in " + TEST_SECONDS + " s; the probe keeps the first "
                        + ProbeResult.MAX_RECORDS + "." : null);
    }

    private Map<String, Object> fromStatistics(Map<String, Object> out) {
        Map<String, Object> details = adapter.healthDetails(clock.instant());
        Object mmsi = details.get("distinctMmsi10m");
        int count = mmsi instanceof Number number ? number.intValue() : 0;
        if (details.get("lastMessageAt") == null) {
            return result(out, false, false, 0, "The live UDP listener has received no AIS message yet.");
        }
        return result(out, count > 0, true, count, "From the live UDP listener: distinct MMSIs in the last 10"
                + " minutes (last message " + details.get("lastMessageAt") + ").");
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
