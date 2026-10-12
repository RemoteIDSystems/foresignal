package ai.foresignal.testsupport.tiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * A small, test-only decoder of Mapbox Vector Tile 2.1 protobuf bytes.
 *
 * <p>Written for the tile tests because the decoder the prompt suggests,
 * {@code no.ecc.vectortile:java-vector-tile}, is not published on Maven Central. It reads the
 * protobuf wire format directly: {@code Tile.layers} (3); {@code Layer.name} (1), {@code features}
 * (2), {@code keys} (3), {@code values} (4), {@code extent} (5), {@code version} (15);
 * {@code Feature.id} (1), {@code tags} (2), {@code type} (3), {@code geometry} (4); and every
 * {@code Value} variant. Geometry commands (MoveTo, LineTo, ClosePath) are expanded into absolute
 * tile coordinates.
 */
public final class MvtDecoder {

    /** Feature geometry types (MVT {@code GeomType}). */
    public enum GeomType {
        UNKNOWN,
        POINT,
        LINESTRING,
        POLYGON
    }

    /** One decoded layer. */
    public record Layer(String name, int extent, int version, List<Feature> features) {}

    /**
     * One decoded feature.
     *
     * @param type geometry type
     * @param properties property name to {@code String}, {@code Long}, {@code Double},
     *     {@code Float} or {@code Boolean}
     * @param parts each MoveTo starts a new part; a part is a list of {@code [x, y]} tile coordinates
     */
    public record Feature(GeomType type, Map<String, Object> properties, List<List<int[]>> parts) {

        /** The first coordinate of the geometry. */
        public int[] firstPoint() {
            return parts.getFirst().getFirst();
        }

        /** A property. */
        public Object get(String name) {
            return properties.get(name);
        }
    }

    private MvtDecoder() {}

    /** Decodes {@code bytes}, gunzipping them first when they start with the gzip magic. */
    public static List<Layer> decode(byte[] bytes) {
        byte[] raw = gunzipIfNeeded(bytes);
        List<Layer> layers = new ArrayList<>();
        Reader tile = new Reader(raw, 0, raw.length);
        while (tile.hasMore()) {
            int tag = (int) tile.varint();
            if (tag >>> 3 == 3 && (tag & 7) == 2) {
                layers.add(layer(tile.message()));
            } else {
                tile.skip(tag & 7);
            }
        }
        return layers;
    }

    /** The single layer of {@code bytes}, or null for an empty tile. */
    public static Layer singleLayer(byte[] bytes) {
        List<Layer> layers = decode(bytes);
        if (layers.isEmpty()) {
            return null;
        }
        if (layers.size() > 1) {
            throw new IllegalStateException("expected one layer, got " + layers.size());
        }
        return layers.getFirst();
    }

    /** The features of {@code bytes}' single layer; empty for an empty tile. */
    public static List<Feature> features(byte[] bytes) {
        Layer layer = singleLayer(bytes);
        return layer == null ? List.of() : layer.features();
    }

    private static Layer layer(Reader r) {
        String name = null;
        int extent = 4096;
        int version = 1;
        List<String> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        List<Reader> rawFeatures = new ArrayList<>();
        while (r.hasMore()) {
            int tag = (int) r.varint();
            int field = tag >>> 3;
            int wire = tag & 7;
            switch (field) {
                case 1 -> name = r.string();
                case 2 -> rawFeatures.add(r.message());
                case 3 -> keys.add(r.string());
                case 4 -> values.add(value(r.message()));
                case 5 -> extent = (int) r.varint();
                case 15 -> version = (int) r.varint();
                default -> r.skip(wire);
            }
        }
        List<Feature> features = new ArrayList<>();
        for (Reader f : rawFeatures) {
            features.add(feature(f, keys, values));
        }
        return new Layer(name, extent, version, features);
    }

    private static Feature feature(Reader r, List<String> keys, List<Object> values) {
        List<Long> tags = new ArrayList<>();
        List<Long> geometry = new ArrayList<>();
        GeomType type = GeomType.UNKNOWN;
        while (r.hasMore()) {
            int tag = (int) r.varint();
            int field = tag >>> 3;
            int wire = tag & 7;
            switch (field) {
                case 2 -> tags.addAll(r.packed());
                case 3 -> type = GeomType.values()[(int) r.varint()];
                case 4 -> geometry.addAll(r.packed());
                default -> r.skip(wire);
            }
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        for (int i = 0; i + 1 < tags.size(); i += 2) {
            properties.put(keys.get(tags.get(i).intValue()), values.get(tags.get(i + 1).intValue()));
        }
        return new Feature(type, properties, geometry(geometry));
    }

    private static List<List<int[]>> geometry(List<Long> commands) {
        List<List<int[]>> parts = new ArrayList<>();
        List<int[]> current = null;
        int x = 0;
        int y = 0;
        int i = 0;
        while (i < commands.size()) {
            long command = commands.get(i++);
            int id = (int) (command & 7);
            int count = (int) (command >>> 3);
            if (id == 7) {
                continue;
            }
            for (int n = 0; n < count; n++) {
                x += zigzag(commands.get(i++));
                y += zigzag(commands.get(i++));
                if (id == 1) {
                    current = new ArrayList<>();
                    parts.add(current);
                }
                current.add(new int[] {x, y});
            }
        }
        return parts;
    }

    private static Object value(Reader r) {
        Object value = null;
        while (r.hasMore()) {
            int tag = (int) r.varint();
            int field = tag >>> 3;
            int wire = tag & 7;
            switch (field) {
                case 1 -> value = r.string();
                case 2 -> value = r.fixed32Float();
                case 3 -> value = r.fixed64Double();
                case 4 -> value = r.varint();
                case 5 -> value = r.varint();
                case 6 -> value = (long) zigzag(r.varint());
                case 7 -> value = r.varint() != 0;
                default -> r.skip(wire);
            }
        }
        return value;
    }

    private static int zigzag(long n) {
        return (int) ((n >>> 1) ^ -(n & 1));
    }

    private static byte[] gunzipIfNeeded(byte[] bytes) {
        if (bytes.length < 2 || (bytes[0] & 0xFF) != 0x1F || (bytes[1] & 0xFF) != 0x8B) {
            return bytes;
        }
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** A cursor over one protobuf message. */
    private static final class Reader {

        private final byte[] bytes;
        private int position;
        private final int end;

        Reader(byte[] bytes, int start, int end) {
            this.bytes = bytes;
            this.position = start;
            this.end = end;
        }

        boolean hasMore() {
            return position < end;
        }

        long varint() {
            long result = 0;
            int shift = 0;
            while (true) {
                byte b = bytes[position++];
                result |= (long) (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    return result;
                }
                shift += 7;
            }
        }

        Reader message() {
            int length = (int) varint();
            Reader child = new Reader(bytes, position, position + length);
            position += length;
            return child;
        }

        String string() {
            int length = (int) varint();
            String s = new String(bytes, position, length, StandardCharsets.UTF_8);
            position += length;
            return s;
        }

        List<Long> packed() {
            Reader inner = message();
            List<Long> out = new ArrayList<>();
            while (inner.hasMore()) {
                out.add(inner.varint());
            }
            return out;
        }

        float fixed32Float() {
            float f = ByteBuffer.wrap(bytes, position, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            position += 4;
            return f;
        }

        double fixed64Double() {
            double d = ByteBuffer.wrap(bytes, position, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            position += 8;
            return d;
        }

        void skip(int wire) {
            switch (wire) {
                case 0 -> varint();
                case 1 -> position += 8;
                case 2 -> position += (int) varint();
                case 5 -> position += 4;
                default -> throw new IllegalStateException("unsupported wire type " + wire);
            }
        }
    }
}
