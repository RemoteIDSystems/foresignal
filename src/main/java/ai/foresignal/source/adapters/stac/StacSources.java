package ai.foresignal.source.adapters.stac;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The reviewed Earth Search feed list (prompt 096), verified 2026-10 against
 * {@code https://earth-search.aws.element84.com/v1} (STAC 1.0.0; items appear about 2 h after acquisition;
 * no deprecation announced).
 *
 * <p>{@code sentinel-2-c1-l2a} is the recommended Collection 1 Sentinel-2 surface-reflectance collection
 * (the legacy {@code sentinel-2-l2a} still ingests but is not used); {@code landsat-c2-l2} is Landsat
 * Collection 2 Level 2; {@code sentinel-1-grd} is the Sentinel-1 GRD collection, off by default.
 *
 * <p>The list is validated at startup ({@link #problems}); an invalid list stops Foresignal rather than
 * registering a URL nobody reviewed.
 */
public final class StacSources {

    /** The Sentinel-2 Collection 1 source id. */
    public static final String SENTINEL2 = "stac-sentinel2";

    /** The Landsat source id. */
    public static final String LANDSAT = "stac-landsat";

    /** The Sentinel-1 source id. */
    public static final String SENTINEL1 = "stac-sentinel1";

    /** The Sentinel-2 Collection 1 collection. */
    public static final String SENTINEL2_COLLECTION = "sentinel-2-c1-l2a";

    /** The Landsat Collection 2 Level 2 collection. */
    public static final String LANDSAT_COLLECTION = "landsat-c2-l2";

    /** The Sentinel-1 GRD collection. */
    public static final String SENTINEL1_COLLECTION = "sentinel-1-grd";

    /** Copernicus Sentinel terms and attribution (free, full and open). */
    public static final String COPERNICUS_LICENCE = "Copernicus Sentinel data terms (free, full and open)";

    /** The Copernicus legal notice. */
    public static final String COPERNICUS_TERMS = "https://www.copernicus.eu/en/legal-notice";

    /** The USGS Landsat data policy. */
    public static final String USGS_TERMS =
            "https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits";

    /** Every reviewed feed, in registration order. */
    public static final List<StacSource> REVIEWED = List.of(
            new StacSource(
                    SENTINEL2,
                    "Sentinel-2 L2A (Collection 1)",
                    SENTINEL2_COLLECTION,
                    true,
                    COPERNICUS_LICENCE,
                    "Contains modified Copernicus Sentinel data " + Year.now(),
                    COPERNICUS_TERMS,
                    "https://earth-search.aws.element84.com/v1",
                    true),
            new StacSource(
                    LANDSAT,
                    "Landsat Collection 2 Level 2",
                    LANDSAT_COLLECTION,
                    true,
                    "US-PD (USGS Landsat)",
                    "Landsat imagery courtesy of the U.S. Geological Survey",
                    USGS_TERMS,
                    "https://earth-search.aws.element84.com/v1",
                    true),
            new StacSource(
                    SENTINEL1,
                    "Sentinel-1 GRD",
                    SENTINEL1_COLLECTION,
                    false,
                    COPERNICUS_LICENCE,
                    "Contains modified Copernicus Sentinel data " + Year.now(),
                    COPERNICUS_TERMS,
                    "https://earth-search.aws.element84.com/v1",
                    false));

    /** Kebab-case, like the descriptor id rule. */
    private static final Pattern ID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private StacSources() {}

    /** The reviewed feed with {@code id}, or null. */
    public static StacSource reviewed(String id) {
        return REVIEWED.stream().filter(source -> source.id().equals(id)).findFirst().orElse(null);
    }

    /** Every problem with {@code sources}; empty when the list is sound. */
    public static List<String> problems(List<StacSource> sources) {
        List<String> problems = new ArrayList<>();
        if (sources == null || sources.isEmpty()) {
            problems.add("the reviewed Earth Search feed list is empty");
            return problems;
        }
        Set<String> ids = new LinkedHashSet<>();
        Set<String> collections = new LinkedHashSet<>();
        for (StacSource source : sources) {
            if (source == null) {
                problems.add("the reviewed Earth Search feed list contains a null entry");
                continue;
            }
            String id = source.id();
            if (id == null || !ID.matcher(id).matches()) {
                problems.add("feed id '" + id + "' must be kebab-case");
            } else if (!ids.add(id)) {
                problems.add("duplicate feed id '" + id + "'");
            }
            if (source.collection() == null || source.collection().isBlank()) {
                problems.add("feed '" + id + "' has no collection");
            } else if (!collections.add(source.collection())) {
                problems.add("feed '" + id + "' repeats collection '" + source.collection() + "'");
            }
            if (source.licence() == null || source.licence().isBlank()) {
                problems.add("feed '" + id + "' has no licence");
            }
            if (source.attribution() == null || source.attribution().isBlank()) {
                problems.add("feed '" + id + "' has no attribution");
            }
            if (source.termsUrl() == null || !source.termsUrl().toLowerCase(Locale.ROOT).startsWith("https://")) {
                problems.add("feed '" + id + "' has a non-https termsUrl");
            }
        }
        return problems;
    }
}
