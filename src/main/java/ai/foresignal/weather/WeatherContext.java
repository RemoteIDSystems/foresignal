package ai.foresignal.weather;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The computed {@code context.weather} of an alert (prompt 056; OpenAPI {@code WeatherContext}): deterministic
 * facts around a geometry at a time, one section per input, each with its own status, plus one summary sentence.
 *
 * @param status {@code ok}, or {@code unavailable} when the computation failed as a whole (sections omitted)
 * @param at the time computed for (the alert's {@code observed_at})
 * @param computedAt when it was computed
 * @param sections the sections by name ({@link #SECTIONS} order), empty when unavailable
 * @param summary the deterministic sentence
 */
public record WeatherContext(String status, Instant at, Instant computedAt, Map<String, Map<String, Object>> sections,
        String summary) {

    /** The section names, in order. */
    public static final List<String> SECTIONS = List.of("radar", "lightning", "precip", "warnings", "wind", "smoke");

    /** Requires the status and times; copies the sections. */
    public WeatherContext {
        Objects.requireNonNull(status, "status");
        sections = sections == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(sections));
    }

    /**
     * A context whose computation failed as a whole.
     *
     * @param at the time it was for
     * @param computedAt when it failed
     */
    public static WeatherContext unavailable(Instant at, Instant computedAt) {
        return new WeatherContext(WeatherSections.UNAVAILABLE, at, computedAt, Map.of(), WeatherSummary.UNAVAILABLE);
    }

    /** True when the computation succeeded (each section still has its own status). */
    public boolean ok() {
        return WeatherSections.OK.equals(status);
    }

    /** The section named {@code name}, or null. */
    public Map<String, Object> section(String name) {
        return sections.get(name);
    }

    /** The JSON members, as {@code WeatherContext}. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", status);
        out.put("at", at == null ? null : at.toString());
        out.put("computedAt", computedAt == null ? null : computedAt.toString());
        for (String name : SECTIONS) {
            Map<String, Object> s = sections.get(name);
            if (s != null) {
                out.put(name, s);
            }
        }
        out.put("summary", summary);
        return out;
    }
}
