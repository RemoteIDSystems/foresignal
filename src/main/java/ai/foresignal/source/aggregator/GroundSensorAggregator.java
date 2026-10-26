package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceHealthSummary;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.env.airq.AirqLocalAdapter;
import ai.foresignal.source.adapters.env.airq.AirqLocalAggregatorAdapter;
import ai.foresignal.source.adapters.env.openaq.OpenAqAdapter;
import ai.foresignal.source.adapters.env.sensorcommunity.SensorCommunityAdapter;
import ai.foresignal.source.adapters.local.DavisWllAggregatorAdapter;
import ai.foresignal.source.adapters.local.EcowittAggregatorAdapter;
import ai.foresignal.source.adapters.mesh.MeshCoreBridgeAdapter;
import ai.foresignal.source.adapters.mesh.MeshCoreLocalAdapter;
import ai.foresignal.source.adapters.mesh.MeshtasticLocalAdapter;
import ai.foresignal.source.adapters.mesh.MeshtasticMapAdapter;
import ai.foresignal.source.adapters.mesh.MeshtasticMqttAdapter;
import ai.foresignal.source.adapters.space.localsonde.LocalSondeAdapter;
import ai.foresignal.source.adapters.space.sondehub.SondehubAdapter;
import ai.foresignal.source.spi.Mode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.stereotype.Component;

/**
 * The {@code ground-sensors} domain (prompt R2-023): the operator's own ground sensors, each a sub-section of
 * {@code config} with its own {@code enabled} and local endpoint, in place of the public networks that aggregate the
 * same kind of data.
 *
 * <table>
 *   <caption>The sub-sections</caption>
 *   <tr><th>Section</th><th>Members</th><th>Local source</th><th>Supersedes</th></tr>
 *   <tr><td>{@code seismic}</td><td>{@code source}: the custom source the rsudp bridge posts to</td>
 *       <td>that push source</td><td>nothing (USGS and EMSC are authorities)</td></tr>
 *   <tr><td>{@code same}</td><td>{@code source}: the custom source {@code contrib/same} posts to</td>
 *       <td>that push source</td><td>nothing (NWS is the authority)</td></tr>
 *   <tr><td>{@code radiosonde}</td><td>{@code endpoint} ({@code udp://bindHost:port}), {@code allowSenders},
 *       {@code stationCallsign}</td><td>{@code local-sonde} (radiosonde_auto_rx)</td><td>{@code sondehub}</td></tr>
 *   <tr><td>{@code airq}</td><td>{@code deviceType}, {@code endpoint}, {@code lat}, {@code lon}, {@code indoor},
 *       {@code name}</td><td>{@code airq-local-aggregator}</td><td>{@code openaq}, {@code sensor-community}</td></tr>
 *   <tr><td>{@code pws}</td><td>{@code station} ({@code davis} or {@code ecowitt}), {@code endpoint}, {@code lat},
 *       {@code lon}</td><td>{@code wxstation-davis-aggregator} or {@code wxstation-ecowitt-aggregator}</td>
 *       <td>nothing (no public PWS network is registered)</td></tr>
 *   <tr><td>{@code mesh}</td><td>{@code meshtastic} (the node's MQTT broker) with {@code root},
 *       {@code meshcore} (the companion's {@code tcp://}), {@code bridge} (accept the MeshCore bridge's pushes)</td>
 *       <td>{@code meshtastic-local}, {@code meshcore-local}, {@code meshcore-bridge}</td>
 *       <td>{@code meshtastic-mqtt}, {@code meshtastic-map}, while {@code meshtastic} is set</td></tr>
 * </table>
 *
 * <p>The aggregator is live while it is enabled with at least one sub-section enabled; {@code endpoint} is not used
 * (each sensor names its own). A sub-section's sources are configured and turned on while the aggregator is live with
 * it enabled. When it stops being so, the sources it configured before are turned off, and a source it never
 * configured is left as the operator set it. Only the mirrors of the live sub-sections are superseded.
 */
@Component
public class GroundSensorAggregator implements DomainAggregator {

    /** The domain. */
    public static final String DOMAIN = "ground-sensors";

    /** The sub-sections, in order. */
    static final List<String> SENSORS = List.of("seismic", "same", "radiosonde", "airq", "pws", "mesh");

    /** The members each sub-section accepts besides {@code enabled}. */
    static final Map<String, Set<String>> MEMBERS = Map.of(
            "seismic", Set.of("source"),
            "same", Set.of("source"),
            "radiosonde", Set.of("endpoint", "allowSenders", "stationCallsign"),
            "airq", Set.of("deviceType", "endpoint", "lat", "lon", "indoor", "name"),
            "pws", Set.of("station", "endpoint", "lat", "lon"),
            "mesh", Set.of("meshtastic", "root", "meshcore", "bridge"));

    /** The weather stations' parameters (prompt 069's {@code WeatherStations}). */
    static final String URL = "url";

    /** How long {@link #test} listens to each read, in seconds. */
    static final int TEST_SECONDS = ProbeService.DEFAULT_SECONDS;

    /**
     * One source a sub-section drives.
     *
     * @param sensor the sub-section
     * @param sourceId the source
     * @param params its parameters, or null when the aggregator only turns it on and off (push sources)
     * @param fields the sub-section member each parameter comes from, for error paths
     */
    record Target(String sensor, String sourceId, Map<String, Object> params, Map<String, String> fields) {
    }

    private final SourceService sources;
    private final SourceRegistry registry;
    private final ProbeService probes;
    private final AppSettingService settings;

    /** Creates the domain. */
    public GroundSensorAggregator(SourceService sources, SourceRegistry registry, ProbeService probes,
            AppSettingService settings) {
        this.sources = sources;
        this.registry = registry;
        this.probes = probes;
        this.settings = settings;
    }

    @Override
    public String domain() {
        return DOMAIN;
    }

    @Override
    public String name() {
        return "Local ground sensors";
    }

    /** Enabled with at least one sub-section enabled. */
    @Override
    public boolean live(AggregatorSettings settings) {
        return settings.enabled() && !enabledSensors(settings).isEmpty();
    }

    @Override
    public String notLiveReason() {
        return "not enabled with a sensor enabled";
    }

    /**
     * The sources of the live sub-sections in force, in order; when none is live, every fixed source this domain can
     * drive, so the view has a status source.
     */
    @Override
    public List<String> sourceIds() {
        AggregatorSettings current = stored();
        if (live(current)) {
            List<String> ids = targets(current).stream().map(Target::sourceId).filter(registry::contains).distinct()
                    .toList();
            if (!ids.isEmpty()) {
                return ids;
            }
        }
        return List.of(LocalSondeAdapter.ID, AirqLocalAggregatorAdapter.ID, DavisWllAggregatorAdapter.ID,
                EcowittAggregatorAdapter.ID, MeshtasticLocalAdapter.ID, MeshCoreLocalAdapter.ID, MeshCoreBridgeAdapter.ID);
    }

    /** Every registered public mirror any sub-section can supersede. */
    @Override
    public List<String> supersededSourceIds() {
        return registered(List.of(SondehubAdapter.ID, OpenAqAdapter.ID, SensorCommunityAdapter.ID,
                MeshtasticMqttAdapter.ID, MeshtasticMapAdapter.ID));
    }

    /** The registered mirrors of the sub-sections {@code settings} enables. */
    @Override
    public List<String> supersededSourceIds(AggregatorSettings settings) {
        List<String> out = new ArrayList<>();
        Set<String> on = enabledSensors(settings);
        if (on.contains("radiosonde")) {
            out.add(SondehubAdapter.ID);
        }
        if (on.contains("airq")) {
            out.add(OpenAqAdapter.ID);
            out.add(SensorCommunityAdapter.ID);
        }
        if (on.contains("mesh") && section(settings, "mesh").get("meshtastic") != null) {
            out.add(MeshtasticMqttAdapter.ID);
            out.add(MeshtasticMapAdapter.ID);
        }
        return registered(out);
    }

    private List<String> registered(List<String> ids) {
        return ids.stream().filter(registry.ids()::contains).sorted().toList();
    }

    @Override
    public void validate(AggregatorSettings proposed, List<FieldError> errors) {
        if (proposed.endpoint() != null) {
            errors.add(new FieldError("endpoint", "is not used by the ground-sensor aggregator: set each sensor's"
                    + " endpoint in its config section"));
        }
        for (Map.Entry<String, Object> entry : proposed.config().entrySet()) {
            String sensor = entry.getKey();
            if (!MEMBERS.containsKey(sensor)) {
                errors.add(new FieldError("config." + sensor, "is not a ground sensor; the sensors are "
                        + String.join(", ", SENSORS)));
                continue;
            }
            if (!(entry.getValue() instanceof Map<?, ?> section)) {
                errors.add(new FieldError("config." + sensor, "must be an object"));
                continue;
            }
            validateSection(sensor, section, errors);
        }
        if (proposed.enabled() && enabledSensors(proposed).isEmpty()) {
            errors.add(new FieldError("config", "enable at least one sensor: " + String.join(", ", SENSORS)));
        }
    }

    private void validateSection(String sensor, Map<?, ?> section, List<FieldError> errors) {
        String path = "config." + sensor;
        for (Map.Entry<?, ?> member : section.entrySet()) {
            String key = String.valueOf(member.getKey());
            if (!key.equals("enabled") && !MEMBERS.get(sensor).contains(key)) {
                errors.add(new FieldError(path + "." + key, "is not a " + sensor + " setting"));
            }
        }
        if (section.get("enabled") != null && !(section.get("enabled") instanceof Boolean)) {
            errors.add(new FieldError(path + ".enabled", "must be true or false"));
        }
        if (!Boolean.TRUE.equals(section.get("enabled"))) {
            return;
        }
        switch (sensor) {
            case "seismic", "same" -> {
                Object source = section.get("source");
                if (!(source instanceof String id) || !registry.contains(id)) {
                    errors.add(new FieldError(path + ".source", "must name the custom source the " + sensor
                            + " bridge posts to (POST /v1/custom-sources)"));
                } else if (registry.descriptor(id).mode() != Mode.PUSH) {
                    errors.add(new FieldError(path + ".source", "'" + id + "' is not a push source"));
                }
            }
            case "radiosonde", "airq" -> {
                if (!(section.get("endpoint") instanceof String)) {
                    errors.add(new FieldError(path + ".endpoint", "is required to enable " + sensor));
                }
            }
            case "pws" -> {
                if (!"davis".equals(section.get("station")) && !"ecowitt".equals(section.get("station"))) {
                    errors.add(new FieldError(path + ".station", "must be davis or ecowitt"));
                }
                if (!(section.get("endpoint") instanceof String)) {
                    errors.add(new FieldError(path + ".endpoint", "is required to enable pws"));
                }
            }
            case "mesh" -> {
                if (section.get("meshtastic") == null && section.get("meshcore") == null
                        && !Boolean.TRUE.equals(section.get("bridge"))) {
                    errors.add(new FieldError(path, "set meshtastic, meshcore or bridge"));
                }
                if (section.get("bridge") != null && !(section.get("bridge") instanceof Boolean)) {
                    errors.add(new FieldError(path + ".bridge", "must be true or false"));
                }
            }
            default -> {
                // Every sensor is handled above.
            }
        }
    }

    /** The sub-sections {@code settings} enables. */
    static Set<String> enabledSensors(AggregatorSettings settings) {
        Set<String> out = new LinkedHashSet<>();
        for (String sensor : SENSORS) {
            if (Boolean.TRUE.equals(section(settings, sensor).get("enabled"))) {
                out.add(sensor);
            }
        }
        return out;
    }

    /** The sub-section {@code sensor} of {@code settings}, or an empty map. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> section(AggregatorSettings settings, String sensor) {
        return settings.config().get(sensor) instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    /** The sources the enabled sub-sections of {@code settings} drive, with their parameters. */
    static List<Target> targets(AggregatorSettings settings) {
        List<Target> out = new ArrayList<>();
        for (String sensor : enabledSensors(settings)) {
            Map<String, Object> s = section(settings, sensor);
            switch (sensor) {
                case "seismic", "same" -> out.add(new Target(sensor, String.valueOf(s.get("source")), null,
                        Map.of()));
                case "radiosonde" -> out.add(target(sensor, LocalSondeAdapter.ID, s, Map.of(
                        "endpoint", LocalSondeAdapter.ENDPOINT_PARAM,
                        "allowSenders", LocalSondeAdapter.ALLOW_SENDERS_PARAM,
                        "stationCallsign", LocalSondeAdapter.STATION_CALLSIGN_PARAM)));
                case "airq" -> out.add(target(sensor, AirqLocalAggregatorAdapter.ID, s, Map.of(
                        "deviceType", AirqLocalAdapter.DEVICE_TYPE_PARAM,
                        "endpoint", AirqLocalAdapter.URL_PARAM,
                        "lat", AirqLocalAdapter.LAT_PARAM,
                        "lon", AirqLocalAdapter.LON_PARAM,
                        "indoor", AirqLocalAdapter.INDOOR_PARAM,
                        "name", AirqLocalAdapter.NAME_PARAM)));
                case "pws" -> out.add(target(sensor, "ecowitt".equals(s.get("station"))
                        ? EcowittAggregatorAdapter.ID : DavisWllAggregatorAdapter.ID, s,
                        Map.of("endpoint", URL, "lat", "lat", "lon", "lon")));
                case "mesh" -> {
                    if (s.get("meshtastic") != null) {
                        out.add(target(sensor, MeshtasticLocalAdapter.ID, s, Map.of(
                                "meshtastic", MeshtasticLocalAdapter.BROKER_URL,
                                "root", MeshtasticLocalAdapter.ROOT)));
                    }
                    if (s.get("meshcore") != null) {
                        out.add(target(sensor, MeshCoreLocalAdapter.ID, s, Map.of(
                                "meshcore", MeshCoreLocalAdapter.ENDPOINT)));
                    }
                    if (Boolean.TRUE.equals(s.get("bridge"))) {
                        out.add(new Target(sensor, MeshCoreBridgeAdapter.ID, null, Map.of()));
                    }
                }
                default -> {
                    // Every sensor is handled above.
                }
            }
        }
        return out;
    }

    /** A target whose parameters are the members of {@code section} named in {@code mapping} (member → parameter). */
    private static Target target(String sensor, String sourceId, Map<String, Object> section,
            Map<String, String> mapping) {
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, String> fields = new LinkedHashMap<>();
        mapping.forEach((member, param) -> {
            Object value = section.get(member);
            if (value != null) {
                params.put(param, value);
            }
            fields.put(param, member);
        });
        return new Target(sensor, sourceId, params, fields);
    }

    /**
     * Configures the sources of the enabled sub-sections and turns them on while live; turns off the sources the
     * previous configuration drove and this one does not.
     */
    @Override
    public void apply(AggregatorSettings proposed) {
        boolean live = live(proposed);
        List<Target> wanted = targets(proposed);
        Set<String> keep = new LinkedHashSet<>();
        List<FieldError> errors = new ArrayList<>();
        for (Target target : wanted) {
            keep.add(target.sourceId());
            try {
                if (target.params() == null) {
                    sources.update(target.sourceId(), SourceSettingsUpdate.enabled(live));
                } else {
                    sources.update(target.sourceId(), new SourceSettingsUpdate(live, null, target.params(), null, null,
                            false, null, false));
                }
            } catch (ValidationException e) {
                for (FieldError error : e.getErrors()) {
                    errors.add(new FieldError(remap(target, error.field()), error.message()));
                }
                if (e.getErrors().isEmpty()) {
                    errors.add(new FieldError("config." + target.sensor(), e.getMessage()));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The ground-sensor aggregator's sensors are not usable.", errors);
        }
        for (Target before : targets(stored())) {
            if (!keep.contains(before.sourceId()) && registry.contains(before.sourceId())
                    && sources.view(before.sourceId()).enabled()) {
                sources.update(before.sourceId(), SourceSettingsUpdate.enabled(false));
            }
        }
    }

    /** A source parameter's error path, as the sub-section member it came from. */
    static String remap(Target target, String field) {
        for (Map.Entry<String, String> entry : target.fields().entrySet()) {
            if (field.equals("params." + entry.getKey()) || field.endsWith("." + entry.getKey())) {
                return "config." + target.sensor() + "." + entry.getValue();
            }
        }
        return "config." + target.sensor();
    }

    /** The configuration in force (before an update stores the new one). */
    private AggregatorSettings stored() {
        return settings.getOrDefault(AggregatorService.key(DOMAIN), AggregatorSettings.class,
                AggregatorSettings::defaults);
    }

    /**
     * Probes each enabled sub-section's sources at once for {@value #TEST_SECONDS} seconds and sums the records they
     * produced. A source that cannot be probed — a push source (seismic, SAME, the MeshCore bridge), or the
     * {@code local-sonde} UDP listener while it already holds its port — is reported from its health instead: reachable
     * when it has received data, adding nothing to {@code sampleCount}. {@code reachable} unless every source failed;
     * {@code ok} when none failed and at least one record arrived; each failure is named in {@code message}.
     */
    @Override
    public Map<String, Object> test(AggregatorSettings current) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<Target> targets = targets(current);
        if (targets.isEmpty()) {
            return result(out, false, false, null, "No sensor is enabled.");
        }
        Map<String, String> failures = new LinkedHashMap<>();
        Map<String, Future<ProbeResult>> running = new LinkedHashMap<>();
        int count = 0;
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Target target : targets) {
                String id = target.sourceId();
                if (!registry.contains(id)) {
                    failures.put(target.sensor() + " (" + id + ")", "is not a registered source");
                    continue;
                }
                if (!probeable(id)) {
                    SourceHealthSummary health = sources.view(id).health();
                    if (health.lastDataAt() == null) {
                        failures.put(target.sensor() + " (" + id + ")", "has received no data yet");
                    }
                    continue;
                }
                running.put(target.sensor() + " (" + id + ")", pool.submit(() -> probes.probe(id, null,
                        TEST_SECONDS)));
            }
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
            return result(out, false, failures.size() < targets.size(), count,
                    "Not every sensor could be read. " + String.join("; ", parts));
        }
        if (count == 0) {
            return result(out, false, true, 0, "Every sensor is reachable, but none produced a record in "
                    + TEST_SECONDS + " s.");
        }
        return result(out, true, true, count, null);
    }

    /** Whether {@code sourceId} can be probed now: not a push source, and not a UDP listener already running. */
    private boolean probeable(String sourceId) {
        if (registry.descriptor(sourceId).mode() == Mode.PUSH) {
            return false;
        }
        return !(LocalSondeAdapter.ID.equals(sourceId) && sources.view(sourceId).enabled());
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
