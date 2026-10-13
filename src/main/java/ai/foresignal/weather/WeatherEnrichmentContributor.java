package ai.foresignal.weather;

import ai.foresignal.trigger.EnrichmentAnchor;
import ai.foresignal.trigger.EnrichmentContextContributor;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adds the alert's stored {@code context.weather} to its enrichment context (prompt 056, through the 030 SPI) as
 * the labeled data block {@value #KEY}. It sits inside the enrichment's untrusted data envelope with everything
 * else, so NWS warning text in it is data, never instructions; the system prompt says that weather sections with a
 * status other than {@code ok} are unknown, not clear. The context is the one computed when the alert fired (no
 * recomputation, no upstream fetch); an alert without one contributes nothing.
 */
@Component
public class WeatherEnrichmentContributor implements EnrichmentContextContributor {

    /** The section key. */
    public static final String KEY = "alert.context.weather";

    private final JsonMapper json;

    /** Creates the contributor. */
    public WeatherEnrichmentContributor(JsonMapper json) {
        this.json = json;
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public int order() {
        return -100;
    }

    @Override
    public Optional<Object> contribute(EnrichmentAnchor anchor) {
        String context = anchor.alert().context();
        if (context == null || context.isBlank()) {
            return Optional.empty();
        }
        Map<?, ?> map = json.readValue(context, Map.class);
        Object weather = map == null ? null : map.get("weather");
        return weather instanceof Map<?, ?> ? Optional.of(weather) : Optional.empty();
    }
}
