package ai.foresignal.place;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The places-AOI settings ({@code foresignal.weather.*}, prompt 036; Weather-Imagery-Design §5.1).
 *
 * @param aoiBufferKm each place's area is buffered to at least this many kilometers (default 150)
 * @param defaultAoi the AOI as {@code minLon,minLat,maxLon,maxLat} when there are no places (default:
 *     the Texas box)
 * @param aoiSources source ids that consume the AOI (weather sources also register themselves with
 *     {@link PlacesAoiService#registerConsumer}); they get a health warning while the default AOI is
 *     used
 */
@ConfigurationProperties("foresignal.weather")
public record WeatherAoiProperties(
        @DefaultValue("150") double aoiBufferKm,
        @DefaultValue("-106.65,25.84,-93.51,36.50") String defaultAoi,
        List<String> aoiSources) {

    /** Fills defaults. */
    public WeatherAoiProperties {
        aoiBufferKm = aoiBufferKm <= 0 ? 150 : aoiBufferKm;
        defaultAoi = defaultAoi == null || defaultAoi.isBlank() ? "-106.65,25.84,-93.51,36.50" : defaultAoi;
        aoiSources = aoiSources == null ? List.of() : List.copyOf(aoiSources);
    }

    /** The shipped defaults. */
    public static WeatherAoiProperties defaults() {
        return new WeatherAoiProperties(150, null, new ArrayList<>());
    }

    /** The default AOI as {@code [minLon, minLat, maxLon, maxLat]}. */
    public double[] defaultBox() {
        String[] parts = defaultAoi.split(",");
        double[] out = new double[4];
        for (int i = 0; i < 4; i++) {
            out[i] = Double.parseDouble(parts[i].strip());
        }
        return out;
    }
}
