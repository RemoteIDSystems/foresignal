package ai.foresignal.source.adapters.satdump;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * Parses the files SatDump writes into a pass folder (prompt 049): {@code dataset.json} and
 * {@code product.cbor}, from the observed output of SatDump 2.0.0-alpha; no SatDump code, headers or
 * structs (Weather-Imagery-Design §3).
 *
 * <ul>
 *   <li>{@code dataset.json}: {@code satellite} (string), {@code timestamp} (epoch seconds, double),
 *       {@code products} (relative product directories).
 *   <li>{@code product.cbor}: {@code instrument}, {@code type}, optional {@code product_timestamp} and
 *       {@code product_source}, {@code images[]} ({@code file}, {@code name}, {@code abs_index},
 *       {@code bit_depth}, …) and {@code projection_cfg}.
 * </ul>
 *
 * <p>Parsing is lenient about keys that differ between versions (SatDump 1.2.2 names some image keys
 * differently): an image entry needs only a file name, and the channel name falls back to the file
 * stem.
 */
public final class SatDumpFiles {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final CBORMapper CBOR = CBORMapper.builder().build();

    private SatDumpFiles() {}

    /** A file that is not what SatDump writes, or not yet completely written. */
    public static final class FormatException extends Exception {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public FormatException(String message) {
            super(message);
        }
    }

    /**
     * A {@code dataset.json}.
     *
     * @param satellite the satellite name
     * @param timestamp the pass timestamp
     * @param products the product directories, relative to the pass folder
     */
    public record Dataset(String satellite, Instant timestamp, List<String> products) {}

    /**
     * One image of a {@code product.cbor}.
     *
     * @param file the file name, relative to the product directory
     * @param name the channel name
     * @param bitDepth the bit depth, or null
     */
    public record Image(String file, String name, Integer bitDepth) {}

    /**
     * A {@code product.cbor}.
     *
     * @param instrument the instrument
     * @param type the product type
     * @param timestamp {@code product_timestamp}, or null
     * @param source {@code product_source}, or null
     * @param images the images
     * @param projection {@code projection_cfg} as a tree, or null
     */
    public record Product(String instrument, String type, Instant timestamp, String source, List<Image> images,
            JsonNode projection) {

        /** The TLE in {@code projection_cfg.tle} ({@code name}, {@code line1}, {@code line2}), or null. */
        public Map<String, Object> tle() {
            if (projection == null || !projection.path("tle").isObject()) {
                return null;
            }
            JsonNode tle = projection.path("tle");
            Map<String, Object> out = new LinkedHashMap<>();
            for (String key : List.of("name", "line1", "line2")) {
                if (tle.path(key).isString()) {
                    out.put(key, tle.path(key).asString());
                }
            }
            return out.isEmpty() ? null : out;
        }
    }

    /**
     * Parses a {@code dataset.json}.
     *
     * @throws FormatException when it is not JSON or lacks {@code satellite} or {@code timestamp}
     */
    public static Dataset dataset(byte[] bytes) throws FormatException {
        JsonNode root;
        try {
            root = JSON.readTree(bytes);
        } catch (JacksonException e) {
            throw new FormatException("dataset.json is not JSON: " + e.getOriginalMessage());
        }
        if (root == null || !root.isObject() || !root.path("satellite").isString() || !root.path("timestamp").isNumber()) {
            throw new FormatException("dataset.json needs a satellite and a numeric timestamp");
        }
        List<String> products = new ArrayList<>();
        for (JsonNode p : root.path("products")) {
            if (p.isString() && !p.asString().isBlank()) {
                products.add(p.asString());
            }
        }
        return new Dataset(root.path("satellite").asString().trim(), seconds(root.path("timestamp").asDouble()),
                products);
    }

    /**
     * Parses a {@code product.cbor}.
     *
     * @throws FormatException when it is not CBOR or not an object with an {@code instrument}
     */
    public static Product product(byte[] bytes) throws FormatException {
        JsonNode root;
        try {
            root = CBOR.readTree(bytes);
        } catch (JacksonException e) {
            throw new FormatException("product.cbor could not be read: " + e.getOriginalMessage());
        }
        if (root == null || !root.isObject()) {
            throw new FormatException("product.cbor is not a map");
        }
        String instrument = root.path("instrument").isString() ? root.path("instrument").asString() : null;
        if (instrument == null || instrument.isBlank()) {
            throw new FormatException("product.cbor has no instrument");
        }
        List<Image> images = new ArrayList<>();
        for (JsonNode img : root.path("images")) {
            String file = img.path("file").isString() ? img.path("file").asString() : null;
            if (file == null || file.isBlank()) {
                continue;
            }
            String name = img.path("name").isString() ? img.path("name").asString() : stem(file);
            Integer bitDepth = img.path("bit_depth").isNumber() ? img.path("bit_depth").asInt() : null;
            images.add(new Image(file, name, bitDepth));
        }
        Instant timestamp = root.path("product_timestamp").isNumber() ? seconds(root.path("product_timestamp").asDouble())
                : null;
        String source = root.path("product_source").isString() ? root.path("product_source").asString() : null;
        JsonNode projection = root.path("projection_cfg").isObject() ? root.path("projection_cfg") : null;
        String type = root.path("type").isString() ? root.path("type").asString() : null;
        return new Product(instrument.trim(), type, timestamp, source, images, projection);
    }

    private static Instant seconds(double epochSeconds) {
        return Instant.ofEpochMilli(Math.round(epochSeconds * 1000));
    }

    private static String stem(String file) {
        String name = file.contains("/") ? file.substring(file.lastIndexOf('/') + 1) : file;
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
