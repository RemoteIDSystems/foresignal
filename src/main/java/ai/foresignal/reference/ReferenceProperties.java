package ai.foresignal.reference;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Reference data and region tagging mechanics ({@code foresignal.reference.*}, prompt 040).
 *
 * @param autoLoad load the default datasets in the background on the first start (when none is loaded);
 *     default true, off in the test profile. It never blocks startup or readiness
 * @param datasets the datasets the first-start load fetches (default: every non-optional one)
 * @param offlineDir a directory of pre-placed dataset files (named as in their manifest URLs) to load
 *     instead of downloading; null to download
 * @param neAdmin0Variant a Natural Earth point-of-view admin-0 file ({@code ind}, {@code chn} …) to load
 *     instead of the default de facto boundaries; null for the default
 * @param checksums SHA-256 pins by dataset name, overriding the manifest; required to verify the rolling
 *     GeoNames dumps, which change daily
 * @param nearMinPopulation the smallest population a {@code near} place may have (default 0: the
 *     cities15000 file is already filtered)
 * @param coastalKm how far a point outside every country may be from one to still be tagged with it
 *     (default 5 km)
 * @param retagKm how far a STANDARD entity must move before it is tagged again (default 2 km)
 * @param trackRetagKm the same for TRACK sources (default 10 km)
 * @param cacheSize the tag cache's size, keyed by coordinates rounded to 0.01° (default 50,000)
 * @param requestsPerMinute the download budget per minute (default 10)
 * @param requestsPerDay the download budget per day (default 100)
 * @param autoLoadDelay how long after startup the first-start load begins (default 30 s)
 */
@ConfigurationProperties("foresignal.reference")
public record ReferenceProperties(
        @DefaultValue("true") boolean autoLoad,
        List<String> datasets,
        Path offlineDir,
        String neAdmin0Variant,
        Map<String, String> checksums,
        @DefaultValue("0") long nearMinPopulation,
        @DefaultValue("5") double coastalKm,
        @DefaultValue("2") double retagKm,
        @DefaultValue("10") double trackRetagKm,
        @DefaultValue("50000") int cacheSize,
        @DefaultValue("10") int requestsPerMinute,
        @DefaultValue("100") int requestsPerDay,
        @DefaultValue("30s") Duration autoLoadDelay) {

    /** Normalizes nulls and bounds. */
    public ReferenceProperties {
        datasets = datasets == null ? List.of() : List.copyOf(datasets);
        neAdmin0Variant = neAdmin0Variant == null || neAdmin0Variant.isBlank() ? null : neAdmin0Variant.strip();
        checksums = checksums == null ? Map.of() : Map.copyOf(checksums);
        nearMinPopulation = Math.max(0, nearMinPopulation);
        coastalKm = coastalKm < 0 ? 5 : coastalKm;
        retagKm = retagKm <= 0 ? 2 : retagKm;
        trackRetagKm = trackRetagKm <= 0 ? 10 : trackRetagKm;
        cacheSize = cacheSize < 1 ? 50_000 : cacheSize;
        requestsPerMinute = requestsPerMinute < 1 ? 10 : requestsPerMinute;
        requestsPerDay = requestsPerDay < 1 ? 100 : requestsPerDay;
        autoLoadDelay = autoLoadDelay == null ? Duration.ofSeconds(30) : autoLoadDelay;
    }

    /** The shipped defaults. */
    public static ReferenceProperties defaults() {
        return new ReferenceProperties(true, null, null, null, null, 0, 5, 2, 10, 50_000, 10, 100, null);
    }
}
