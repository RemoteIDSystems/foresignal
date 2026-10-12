package ai.foresignal.reference;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The bundled list of reference datasets ({@code reference/manifest.json}, prompt 040): per dataset its
 * URL, pinned SHA-256, edition, licence and attribution. Callers can only name a dataset; there are no
 * caller-supplied URLs.
 */
@Component
public class ReferenceManifest {

    /** The bundled manifest. */
    public static final String RESOURCE = "/reference/manifest.json";

    /** Natural Earth admin-0 countries. */
    public static final String KIND_NE_ADMIN0 = "ne-admin0";
    /** Natural Earth admin-1 states and provinces. */
    public static final String KIND_NE_ADMIN1 = "ne-admin1";
    /** Natural Earth admin-2 (US counties). */
    public static final String KIND_NE_ADMIN2 = "ne-admin2";
    /** Natural Earth marine polygons. */
    public static final String KIND_NE_MARINE = "ne-marine";
    /** A GeoNames cities dump (zip). */
    public static final String KIND_GN_CITIES = "geonames-cities";
    /** GeoNames {@code admin1CodesASCII.txt}. */
    public static final String KIND_GN_ADMIN1 = "geonames-admin1";
    /** GeoNames {@code countryInfo.txt}. */
    public static final String KIND_GN_COUNTRYINFO = "geonames-countryinfo";

    /**
     * One dataset.
     *
     * @param name the dataset name, for example {@code ne-admin1}
     * @param kind how it is parsed and which table it fills (the {@code KIND_*} constants)
     * @param url where it is downloaded from
     * @param sha256 the pinned SHA-256 (hex)
     * @param edition the edition text
     * @param licence the licence
     * @param attribution the credit, or null
     * @param optional true for datasets the first-start load skips
     * @param rolling true when upstream regenerates the file in place (GeoNames, daily): the pin is then
     *     verified only when the operator configures one
     * @param maxBytes the largest download accepted
     * @param variants point-of-view alternatives by key ({@code ind} …), each with its own URL and hash
     */
    public record DatasetSpec(String name, String kind, String url, String sha256, String edition, String licence,
            String attribution, boolean optional, boolean rolling, long maxBytes, Map<String, Variant> variants) {

        /** Copies the variants. */
        public DatasetSpec {
            variants = variants == null ? Map.of() : Map.copyOf(variants);
        }

        /** The file name at the end of the URL. */
        public String fileName() {
            String path = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
            return path.substring(path.lastIndexOf('/') + 1);
        }

        /** This dataset with another URL and hash (a point-of-view variant, or a test stub). */
        public DatasetSpec with(String newUrl, String newSha256) {
            return new DatasetSpec(name, kind, newUrl, newSha256, edition, licence, attribution, optional, rolling,
                    maxBytes, variants);
        }

        /** True when this dataset fills the gazetteer (GeoNames cities). */
        public boolean cities() {
            return KIND_GN_CITIES.equals(kind);
        }
    }

    /**
     * A point-of-view alternative of a dataset.
     *
     * @param url its URL
     * @param sha256 its pinned SHA-256
     */
    public record Variant(String url, String sha256) {}

    private final List<DatasetSpec> datasets;

    /** Loads the bundled manifest. */
    public ReferenceManifest() {
        this(load());
    }

    /** A manifest over {@code datasets} (tests). */
    public ReferenceManifest(List<DatasetSpec> datasets) {
        this.datasets = List.copyOf(datasets);
    }

    /** Every dataset, in manifest order. */
    public List<DatasetSpec> datasets() {
        return datasets;
    }

    /** The dataset named {@code name}. */
    public Optional<DatasetSpec> find(String name) {
        return datasets.stream().filter(d -> d.name().equals(name)).findFirst();
    }

    /** The names of the non-optional datasets (the first-start load). */
    public List<String> defaults() {
        return datasets.stream().filter(d -> !d.optional()).map(DatasetSpec::name).toList();
    }

    static List<DatasetSpec> load() {
        try (InputStream in = ReferenceManifest.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The bundled reference manifest " + RESOURCE + " is missing");
            }
            return parse(JsonMapper.builder().build().readTree(in));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }

    /** The datasets of a manifest document. */
    static List<DatasetSpec> parse(JsonNode root) {
        List<DatasetSpec> out = new ArrayList<>();
        for (JsonNode d : root.path("datasets")) {
            Map<String, Variant> variants = new LinkedHashMap<>();
            JsonNode v = d.path("variants");
            if (v.isObject()) {
                for (String key : v.propertyNames()) {
                    variants.put(key, new Variant(v.get(key).path("url").asString(), v.get(key).path("sha256").asString()));
                }
            }
            out.add(new DatasetSpec(
                    d.path("name").asString(),
                    d.path("kind").asString(),
                    d.path("url").asString(),
                    d.path("sha256").asString(null),
                    d.path("edition").asString(null),
                    d.path("licence").asString(null),
                    d.path("attribution").asString(null),
                    d.path("optional").asBoolean(false),
                    d.path("rolling").asBoolean(false),
                    d.path("maxBytes").asLong(64L * 1024 * 1024),
                    variants));
        }
        return out;
    }
}
