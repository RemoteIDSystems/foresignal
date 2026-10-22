package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code foresignal-manifest.json} reader (prompt 107, SatDump 2.0 hook): a version-agnostic
 * manifest a SatDump product hook writes beside its products, listing the equirectangular GeoTIFFs and
 * PNGs to ingest. Foresignal recognizes it independently of the SatDump version; an unknown schema is
 * rejected.
 *
 * <pre>{@code
 * {"schema": 1, "satellite": "METEOR-M2 3", "norad": 57166, "timestamp": "2026-10-15T18:02:00Z",
 *  "products": [{"name": "rgb", "instrument": "MSU-MR", "file": "MSU-MR/rgb.tif",
 *                "projection": "equirec", "bounds": [-180,-90,180,90], "composite": "rgb_MSU-MR"}]}
 * }</pre>
 */
public final class ForesignalManifest {

    /** The recognized schema version. */
    public static final int SCHEMA = 1;

    /** The manifest file name. */
    public static final String FILE = "foresignal-manifest.json";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ForesignalManifest() {}

    /**
     * One product.
     *
     * @param name the product name
     * @param instrument the instrument, or null
     * @param file the file path relative to the manifest, {@code /}-separated
     * @param projection the projection ({@code equirec})
     * @param bounds the bounds {@code [w,s,e,n]}, or null
     * @param composite the composite name, or null
     */
    public record Product(String name, String instrument, String file, String projection, double[] bounds,
            String composite) {}

    /**
     * A manifest.
     *
     * @param schema the schema version ({@value #SCHEMA})
     * @param satellite the satellite name
     * @param norad the NORAD id, or null
     * @param timestamp the pass timestamp
     * @param products the products
     */
    public record Manifest(int schema, String satellite, Long norad, Instant timestamp, List<Product> products) {}

    /** Parses and validates a manifest. */
    public static Manifest parse(byte[] json) throws FormatException {
        if (json == null || json.length == 0) {
            throw new FormatException(FILE + " is empty");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (RuntimeException e) {
            throw new FormatException(FILE + " is not JSON: " + e.getMessage());
        }
        if (root == null || !root.isObject()) {
            throw new FormatException(FILE + " is not an object");
        }
        JsonNode schemaNode = root.get("schema");
        if (schemaNode == null || !schemaNode.isNumber()) {
            throw new FormatException(FILE + " has no numeric schema");
        }
        int schema = schemaNode.asInt();
        if (schema != SCHEMA) {
            throw new FormatException(FILE + " schema " + schema + " is not supported (expected " + SCHEMA + ")");
        }
        String satellite = text(root, "satellite");
        if (satellite == null) {
            throw new FormatException(FILE + " has no satellite");
        }
        Instant timestamp = instant(root.get("timestamp"));
        if (timestamp == null) {
            throw new FormatException(FILE + " has no parseable timestamp");
        }
        Long norad = root.has("norad") && root.get("norad").isNumber() ? root.get("norad").asLong() : null;
        JsonNode productsNode = root.get("products");
        if (productsNode == null || !productsNode.isArray() || productsNode.isEmpty()) {
            throw new FormatException(FILE + " has no products");
        }
        List<Product> products = new ArrayList<>();
        for (JsonNode node : productsNode) {
            if (!node.isObject()) {
                continue;
            }
            String name = text(node, "name");
            String file = text(node, "file");
            if (name == null || file == null) {
                throw new FormatException(FILE + " product needs a name and a file");
            }
            if (file.startsWith("/") || file.contains("..")) {
                throw new FormatException(FILE + " product file '" + file + "' is unsafe");
            }
            products.add(new Product(name, text(node, "instrument"), file.replace('\\', '/'),
                    text(node, "projection"), bounds(node.get("bounds")), text(node, "composite")));
        }
        if (products.isEmpty()) {
            throw new FormatException(FILE + " has no valid products");
        }
        return new Manifest(schema, satellite, norad, timestamp, products);
    }

    private static double[] bounds(JsonNode node) {
        if (node == null || !node.isArray() || node.size() != 4) {
            return null;
        }
        double[] out = new double[4];
        for (int i = 0; i < 4; i++) {
            if (!node.get(i).isNumber()) {
                return null;
            }
            out[i] = node.get(i).asDouble();
        }
        return out;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || !v.isString()) {
            return null;
        }
        String s = v.asString("");
        return s.isBlank() ? null : s;
    }

    private static Instant instant(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isNumber()) {
            long n = node.asLong();
            return n > 1_000_000_000_000L ? Instant.ofEpochMilli(n) : Instant.ofEpochSecond(n);
        }
        if (node.isString()) {
            try {
                return Instant.parse(node.asString().strip());
            } catch (RuntimeException e) {
                return null;
            }
        }
        return null;
    }
}
