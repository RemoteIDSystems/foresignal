package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.acars.LocalAcarsAdapter;
import ai.foresignal.source.adapters.acars.LocalAcarsAggregatorAdapter;
import ai.foresignal.source.adapters.radio.AprsIsAdapter;
import ai.foresignal.source.adapters.radio.LocalAprsAdapter;
import ai.foresignal.source.adapters.radio.LocalAprsAggregatorAdapter;
import ai.foresignal.source.codec.AcarsJsonCodec;
import ai.foresignal.source.codec.KissAx25Codec;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.stereotype.Component;

/**
 * The {@code datalink} domain (prompt R2-021): the operator's own aviation datalink decoders (ACARS, VDL Mode 2 and
 * HFDL through acars_router, fed by acarsdec, dumpvdl2 and dumphfdl) and own APRS igate TNC, read from this host or the
 * LAN, in place of the public APRS-IS network.
 *
 * <p>{@code endpoint} is the decoder host, {@code tcp://host} with no port. {@code config} selects the decoders, each
 * a port on that host or a whole {@code tcp://host:port} on another one:
 *
 * <ul>
 *   <li>{@code acars}, {@code vdlm2}, {@code hfdl}: acars_router's TCP servers (its defaults are 15550, 15555 and
 *       15556; no port is assumed), written into {@code local-acars-aggregator} ({@link LocalAcarsAggregatorAdapter});
 *   <li>{@code aprs}: the igate's KISS TCP port (Direwolf's conventional {@code KISSPORT} is 8001), written into
 *       {@code local-aprs-aggregator} ({@link LocalAprsAggregatorAdapter});
 *   <li>{@code stationId} (the ACARS observer), {@code stationCallsign} (the APRS observer) and
 *       {@code includeMessages} (also keep APRS messages heard on RF).
 * </ul>
 *
 * <p>Each read is turned on while the aggregator is live with one of its decoders selected, and off otherwise; its
 * endpoints are therefore checked, pinned and kept off egress routes like any {@code LAN_URL}. It supersedes
 * {@code aprs-is}, the public APRS network. No airframes.io source is registered, so there is nothing to supersede for
 * ACARS, VDL Mode 2 and HFDL.
 */
@Component
public class DatalinkAggregator implements DomainAggregator {

    /** The domain. */
    public static final String DOMAIN = "datalink";

    /** The ACARS decoder member of {@code config}. */
    static final String ACARS = "acars";

    /** The VDL Mode 2 decoder member of {@code config}. */
    static final String VDLM2 = "vdlm2";

    /** The HFDL decoder member of {@code config}. */
    static final String HFDL = "hfdl";

    /** The APRS igate member of {@code config}. */
    static final String APRS = "aprs";

    /** The acars_router decoders and the {@code local-acars-aggregator} parameter each is written into. */
    static final Map<String, String> ACARS_DECODERS = Map.of(
            ACARS, LocalAcarsAdapter.ACARS_ENDPOINT,
            VDLM2, LocalAcarsAdapter.VDLM2_ENDPOINT,
            HFDL, LocalAcarsAdapter.HFDL_ENDPOINT);

    /** The decoder members, in order. */
    static final List<String> DECODERS = List.of(ACARS, VDLM2, HFDL, APRS);

    /** The ACARS observer member of {@code config}. */
    static final String STATION_ID = AcarsJsonCodec.STATION_PARAM;

    /** The APRS observer member of {@code config}. */
    static final String STATION_CALLSIGN = KissAx25Codec.STATION_PARAM;

    /** The APRS messages member of {@code config}. */
    static final String INCLUDE_MESSAGES = KissAx25Codec.INCLUDE_MESSAGES_PARAM;

    /** The {@code config} members. */
    static final Set<String> CONFIG_KEYS = Set.of(ACARS, VDLM2, HFDL, APRS, STATION_ID, STATION_CALLSIGN,
            INCLUDE_MESSAGES);

    /** How long {@link #test} listens to each read, in seconds. */
    static final int TEST_SECONDS = ProbeService.DEFAULT_SECONDS;

    private final SourceService sources;
    private final SourceRegistry registry;
    private final ProbeService probes;

    /** Creates the domain. */
    public DatalinkAggregator(SourceService sources, SourceRegistry registry, ProbeService probes) {
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
        return "Local aircraft datalink aggregator";
    }

    @Override
    public List<String> sourceIds() {
        return List.of(LocalAcarsAggregatorAdapter.ID, LocalAprsAggregatorAdapter.ID);
    }

    @Override
    public List<String> supersededSourceIds() {
        return registry.ids().stream().filter(AprsIsAdapter.ID::equals).toList();
    }

    @Override
    public void validate(AggregatorSettings proposed, List<FieldError> errors) {
        String host = null;
        if (proposed.endpoint() != null) {
            host = host(proposed.endpoint());
            if (host == null) {
                errors.add(new FieldError("endpoint", "must be tcp://host, the host the decoders run on, with no"
                        + " port, path or credentials; give each decoder's port in config"));
            }
        } else if (proposed.enabled()) {
            errors.add(new FieldError("endpoint", "is required to enable the datalink aggregator: the decoder host,"
                    + " for example tcp://acars-router"));
        }
        boolean anyDecoder = false;
        for (Map.Entry<String, Object> entry : proposed.config().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!CONFIG_KEYS.contains(key)) {
                errors.add(new FieldError("config." + key, "is not a datalink aggregator setting"));
            } else if (DECODERS.contains(key)) {
                anyDecoder = true;
                if (decoderUrl(host == null ? "localhost" : host, value) == null) {
                    errors.add(new FieldError("config." + key, "must be a port (1 to 65535) on the endpoint host, or"
                            + " tcp://host:port"));
                }
            } else if (INCLUDE_MESSAGES.equals(key) && !(value instanceof Boolean)) {
                errors.add(new FieldError("config." + key, "must be true or false"));
            } else if (!INCLUDE_MESSAGES.equals(key) && !(value instanceof String)) {
                errors.add(new FieldError("config." + key, "must be a string"));
            }
        }
        if (proposed.enabled() && !anyDecoder) {
            errors.add(new FieldError("config", "select at least one decoder: acars, vdlm2, hfdl or aprs"));
        }
    }

    /** The host of a {@code tcp://host} endpoint, or null when it is not one (a port, path or user is refused). */
    static String host(String endpoint) {
        URI uri;
        try {
            uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            return null;
        }
        if (uri.getScheme() == null || !uri.getScheme().toLowerCase(Locale.ROOT).equals("tcp") || uri.getHost() == null
                || uri.getPort() != -1 || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())) {
            return null;
        }
        return uri.getHost();
    }

    /**
     * A decoder's {@code tcp://host:port}: {@code value} is a port on {@code host}, or a whole {@code tcp://host:port}.
     * Null when it is neither.
     */
    static String decoderUrl(String host, Object value) {
        if (value instanceof Number number) {
            int port = number.intValue();
            return number.doubleValue() == port && port >= 1 && port <= 65535 ? "tcp://" + host + ":" + port : null;
        }
        if (!(value instanceof String text)) {
            return null;
        }
        try {
            URI uri = new URI(text.strip());
            if (uri.getScheme() == null || !uri.getScheme().toLowerCase(Locale.ROOT).equals("tcp")
                    || uri.getHost() == null || uri.getPort() < 1 || uri.getRawUserInfo() != null) {
                return null;
            }
            return text.strip();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void apply(AggregatorSettings settings) {
        String host = settings.endpoint() == null ? null : host(settings.endpoint());
        Map<String, Object> acars = new LinkedHashMap<>();
        Map<String, Object> aprs = new LinkedHashMap<>();
        if (host != null) {
            for (Map.Entry<String, String> decoder : ACARS_DECODERS.entrySet()) {
                Object value = settings.config().get(decoder.getKey());
                if (value != null) {
                    acars.put(decoder.getValue(), decoderUrl(host, value));
                }
            }
            Object igate = settings.config().get(APRS);
            if (igate != null) {
                aprs.put(LocalAprsAdapter.ENDPOINT, decoderUrl(host, igate));
            }
        }
        if (!acars.isEmpty()) {
            copy(settings, STATION_ID, acars);
        }
        if (!aprs.isEmpty()) {
            copy(settings, STATION_CALLSIGN, aprs);
            copy(settings, INCLUDE_MESSAGES, aprs);
        }
        List<FieldError> errors = new ArrayList<>();
        configure(LocalAcarsAggregatorAdapter.ID, settings.live(), acars, errors);
        configure(LocalAprsAggregatorAdapter.ID, settings.live(), aprs, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The datalink aggregator's decoders are not usable.", errors);
        }
    }

    private static void copy(AggregatorSettings settings, String key, Map<String, Object> params) {
        Object value = settings.config().get(key);
        if (value != null) {
            params.put(key, value);
        }
    }

    /**
     * Writes {@code params} into the read and turns it on with {@code live}; with no parameters (none of its decoders
     * selected) it is only turned off, and keeps what it had. Errors are reported against the aggregator's fields.
     */
    private void configure(String id, boolean live, Map<String, Object> params, List<FieldError> errors) {
        try {
            if (params.isEmpty()) {
                if (sources.view(id).enabled()) {
                    sources.update(id, SourceSettingsUpdate.enabled(false));
                }
                return;
            }
            sources.update(id, new SourceSettingsUpdate(live, null, params, null, null, false, null, false));
        } catch (ValidationException e) {
            for (FieldError error : e.getErrors()) {
                errors.add(new FieldError(remap(error.field()), error.message()));
            }
            if (e.getErrors().isEmpty()) {
                errors.add(new FieldError("config", e.getMessage()));
            }
        }
    }

    /** A read parameter's error path, as the aggregator's field. */
    static String remap(String field) {
        for (Map.Entry<String, String> decoder : ACARS_DECODERS.entrySet()) {
            if (field.contains(decoder.getValue())) {
                return "config." + decoder.getKey();
            }
        }
        if (field.contains(STATION_ID)) {
            return "config." + STATION_ID;
        }
        if (field.contains(STATION_CALLSIGN)) {
            return "config." + STATION_CALLSIGN;
        }
        if (field.contains(INCLUDE_MESSAGES)) {
            return "config." + INCLUDE_MESSAGES;
        }
        if (field.contains(LocalAprsAdapter.ENDPOINT)) {
            return "config." + APRS;
        }
        return field;
    }

    /**
     * Listens to each read whose decoders are selected for {@value #TEST_SECONDS} seconds at once, through the probe
     * service — the read's own socket path, budget and pinning — and sums the records decoded (at most {@value
     * ProbeResult#MAX_RECORDS} a read). {@code reachable} when every selected read connected; {@code ok} when, in
     * addition, at least one record arrived. Datalink traffic is sparse, so a quiet sky can be reachable but not ok.
     */
    @Override
    public Map<String, Object> test(AggregatorSettings settings) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (settings.endpoint() == null) {
            return result(out, false, false, null, "No endpoint is configured.");
        }
        Map<String, String> reads = new LinkedHashMap<>();
        if (ACARS_DECODERS.keySet().stream().anyMatch(settings.config()::containsKey)) {
            reads.put("ACARS/VDLM2/HFDL", LocalAcarsAggregatorAdapter.ID);
        }
        if (settings.config().containsKey(APRS)) {
            reads.put("APRS", LocalAprsAggregatorAdapter.ID);
        }
        if (reads.isEmpty()) {
            return result(out, false, false, null, "No decoder is selected.");
        }
        Map<String, Future<ProbeResult>> running = new LinkedHashMap<>();
        Map<String, String> failures = new LinkedHashMap<>();
        int count = 0;
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            reads.forEach((label, id) -> running.put(label, pool.submit(() -> probes.probe(id, null, TEST_SECONDS))));
            for (Map.Entry<String, Future<ProbeResult>> entry : running.entrySet()) {
                try {
                    ProbeResult probe = entry.getValue().get();
                    if (probe.parseError() != null) {
                        failures.put(entry.getKey(), probe.parseError());
                    } else {
                        count += probe.records().size();
                    }
                } catch (ExecutionException e) {
                    failures.put(entry.getKey(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failures.put(entry.getKey(), "interrupted");
                }
            }
        }
        if (!failures.isEmpty()) {
            List<String> parts = new ArrayList<>();
            failures.forEach((label, reason) -> parts.add(label + ": " + reason));
            return result(out, false, failures.size() < reads.size(), count,
                    "Not every decoder could be read. " + String.join("; ", parts));
        }
        if (count == 0) {
            return result(out, false, true, 0, "Connected to " + String.join(" and ", reads.keySet()) + ", but no"
                    + " message arrived in " + TEST_SECONDS + " s.");
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
