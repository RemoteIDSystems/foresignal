package ai.foresignal.object;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The curated, operator-extensible list of satellite capabilities used by sensor discovery (prompt 129): the bundled
 * {@value #RESOURCE} (public facts: operator, sensor types, nominal resolution and swath), merged with an optional
 * operator file ({@code foresignal.sensors.capabilities-file}) whose entries replace bundled ones with the same
 * catalog number.
 */
@Component
public class SatelliteCapabilities {

    /** The bundled resource. */
    public static final String RESOURCE = "intel/satellite-capabilities.json";

    private static final Logger log = LoggerFactory.getLogger(SatelliteCapabilities.class);
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    /**
     * One satellite.
     *
     * @param norad the catalog number
     * @param name the object name
     * @param operator who operates it
     * @param sensorTypes optical, sar, weather, ais, …
     * @param resolutionM best nominal resolution in meters, or null
     * @param swathKm nominal swath in km, or null
     */
    public record Capability(long norad, String name, String operator, List<String> sensorTypes, Double resolutionM,
            Double swathKm) {

        /** The {@code Sensor.capabilities} view. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("operator", operator);
            out.put("sensorTypes", sensorTypes);
            out.put("resolutionM", resolutionM);
            out.put("swathKm", swathKm);
            return out;
        }
    }

    private final List<Capability> all;

    /** Loads the bundled list and the operator file. */
    public SatelliteCapabilities(JsonMapper json,
            @Value("${foresignal.sensors.capabilities-file:}") String operatorFile) {
        Map<Long, Capability> merged = new LinkedHashMap<>();
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            parse(json.readValue(in, MAP)).forEach(c -> merged.put(c.norad(), c));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
        if (operatorFile != null && !operatorFile.isBlank()) {
            try {
                parse(json.readValue(Files.readString(Path.of(operatorFile)), MAP))
                        .forEach(c -> merged.put(c.norad(), c));
            } catch (IOException | RuntimeException e) {
                log.warn("Ignoring the satellite capabilities file {}: {}", operatorFile, e.toString());
            }
        }
        this.all = List.copyOf(merged.values());
    }

    /** Every satellite, bundled order first. */
    public List<Capability> all() {
        return all;
    }

    /** The satellite with {@code norad}, or null. */
    public Capability byNorad(long norad) {
        for (Capability c : all) {
            if (c.norad() == norad) {
                return c;
            }
        }
        return null;
    }

    /** The entries of a capabilities document. */
    static List<Capability> parse(Map<String, Object> document) {
        List<Capability> out = new ArrayList<>();
        if (!(document.get("satellites") instanceof List<?> list)) {
            return out;
        }
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m) || !(m.get("norad") instanceof Number n)) {
                continue;
            }
            List<String> types = new ArrayList<>();
            if (m.get("sensorTypes") instanceof List<?> t) {
                t.forEach(x -> types.add(String.valueOf(x)));
            }
            out.add(new Capability(n.longValue(), m.get("name") == null ? "NORAD " + n : String.valueOf(m.get("name")),
                    m.get("operator") == null ? null : String.valueOf(m.get("operator")), List.copyOf(types),
                    m.get("resolutionM") instanceof Number r ? r.doubleValue() : null,
                    m.get("swathKm") instanceof Number s ? s.doubleValue() : null));
        }
        return out;
    }
}
