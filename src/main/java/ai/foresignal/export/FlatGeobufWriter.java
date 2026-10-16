package ai.foresignal.export;

import com.google.flatbuffers.FlatBufferBuilder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Crs;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;
import org.wololo.flatgeobuf.generated.Header;

/**
 * A FlatGeobuf writer (prompt 111) on {@code org.wololo:flatgeobuf} and JTS.
 *
 * <p>Columns are the {@link ExportSchema} columns ({@code seq} Long, {@code version} Int, timestamps
 * as ISO 8601 {@code DateTime}, text as {@code String}, {@code payload} as {@code Json}). The header's
 * geometry type is the one type every feature has, else {@code Unknown} with each feature carrying
 * its own type; the CRS is EPSG:4326.
 *
 * <p>The packed Hilbert R-tree index (node size {@value #NODE_SIZE}) needs every feature's envelope
 * and puts the features in Hilbert order, so the writer works in two passes without holding features
 * in memory: {@link #write} serializes each feature into a spill file and keeps only its offset,
 * size and envelope in primitive arrays; {@link #close} sorts by Hilbert value, writes the header and
 * the index, then copies the features from the spill file in index order. A FlatGeobuf index cannot
 * address a feature without a geometry, so when any row lacks one the file is written without an
 * index (in arrival order) rather than dropping rows.
 */
public final class FlatGeobufWriter implements ExportWriter {

    /** The R-tree node size. */
    public static final int NODE_SIZE = 16;

    private static final int HILBERT_MAX = (1 << 16) - 1;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private Path file;
    private Path spill;
    private DataOutputStream spillOut;
    private ExportSchema schema;
    private Map<String, String> metadata;
    private long rows;
    private long spillBytes;
    private long[] offsets = new long[1024];
    private int[] sizes = new int[1024];
    private double[] boxes = new double[4096];
    private boolean allHaveGeometry = true;
    private final java.util.BitSet withGeometry = new java.util.BitSet();
    private int commonType = -1;
    private boolean closed;
    private final FlatBufferBuilder builder = new FlatBufferBuilder(1024);

    @Override
    public void open(Path target, ExportSchema exportSchema, Map<String, String> meta) throws IOException {
        this.file = target;
        this.schema = exportSchema;
        this.metadata = meta == null ? Map.of() : meta;
        this.spill = Files.createTempFile(target.toAbsolutePath().getParent(), "fgb-", ".spill");
        this.spillOut = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(spill), 1 << 16));
    }

    @Override
    public void write(Map<String, Object> row) throws IOException {
        builder.clear();
        byte[] properties = properties(row);
        int propertiesOffset = Feature.createPropertiesVector(builder, properties);
        int geometryOffset = 0;
        double[] box = null;
        if (schema.geometry() && row.get(GEOMETRY) instanceof byte[] wkb && wkb.length > 0) {
            Geometry geometry = Wkb.parse(wkb);
            if (!geometry.isEmpty()) {
                geometryOffset = GeometryConversions.serialize(builder, geometry, (byte) GeometryType.Unknown);
                int type = GeometryConversions.toGeometryType(geometry.getClass());
                commonType = commonType == -1 || commonType == type ? type : GeometryType.Unknown;
                var envelope = geometry.getEnvelopeInternal();
                box = new double[] {envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY()};
            }
        }
        Feature.startFeature(builder);
        if (geometryOffset != 0) {
            Feature.addGeometry(builder, geometryOffset);
        }
        Feature.addProperties(builder, propertiesOffset);
        int feature = Feature.endFeature(builder);
        Feature.finishSizePrefixedFeatureBuffer(builder, feature);
        byte[] bytes = builder.sizedByteArray();
        ensureCapacity((int) rows + 1);
        int index = (int) rows;
        offsets[index] = spillBytes;
        sizes[index] = bytes.length;
        if (box == null) {
            allHaveGeometry = false;
        } else {
            System.arraycopy(box, 0, boxes, index * 4, 4);
            withGeometry.set(index);
        }
        spillOut.write(bytes);
        spillBytes += bytes.length;
        rows++;
    }

    private byte[] properties(Map<String, Object> row) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(256);
        ByteBuffer scratch = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        int index = 0;
        for (ExportSchema.Column column : schema.columns()) {
            Object value = row.get(column.name());
            if (value != null) {
                scratch.clear();
                scratch.putShort((short) index);
                bytes.write(scratch.array(), 0, 2);
                switch (columnType(column)) {
                    case ColumnType.Long -> {
                        scratch.clear();
                        scratch.putLong(((Number) value).longValue());
                        bytes.write(scratch.array(), 0, 8);
                    }
                    case ColumnType.Int -> {
                        scratch.clear();
                        scratch.putInt(((Number) value).intValue());
                        bytes.write(scratch.array(), 0, 4);
                    }
                    case ColumnType.Double -> {
                        scratch.clear();
                        scratch.putDouble(((Number) value).doubleValue());
                        bytes.write(scratch.array(), 0, 8);
                    }
                    default -> {
                        String text = column.type() == ExportSchema.Type.TIMESTAMP_MICROS ? dateTime(value)
                                : String.valueOf(value);
                        byte[] utf8 = text.getBytes(StandardCharsets.UTF_8);
                        scratch.clear();
                        scratch.putInt(utf8.length);
                        bytes.write(scratch.array(), 0, 4);
                        bytes.write(utf8);
                    }
                }
            }
            index++;
        }
        return bytes.toByteArray();
    }

    private static String dateTime(Object value) {
        return value instanceof Instant instant ? ISO.format(instant) : String.valueOf(value);
    }

    /** The FlatGeobuf column type of {@code column}. */
    static int columnType(ExportSchema.Column column) {
        if ("payload".equals(column.name())) {
            return ColumnType.Json;
        }
        return switch (column.type()) {
            case INT64 -> ColumnType.Long;
            case INT32 -> ColumnType.Int;
            case DOUBLE -> ColumnType.Double;
            case TIMESTAMP_MICROS -> ColumnType.DateTime;
            case STRING -> ColumnType.String;
        };
    }

    private void ensureCapacity(int count) {
        if (count > offsets.length) {
            int capacity = Math.max(count, offsets.length * 2);
            offsets = Arrays.copyOf(offsets, capacity);
            sizes = Arrays.copyOf(sizes, capacity);
            boxes = Arrays.copyOf(boxes, capacity * 4);
        }
    }

    @Override
    public long rows() {
        return rows;
    }

    @Override
    public void close() throws IOException {
        if (closed || spillOut == null) {
            closed = true;
            return;
        }
        closed = true;
        spillOut.close();
        try {
            int n = (int) rows;
            boolean indexed = n > 0 && allHaveGeometry;
            int[] order = indexed ? hilbertOrder(n) : identity(n);
            double[] extent = extent(n);
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), 1 << 16)) {
                out.write(Constants.MAGIC_BYTES);
                out.write(header(n, indexed, extent));
                if (indexed) {
                    writeIndex(out, order, n);
                }
                copyFeatures(out, order);
            }
        } finally {
            Files.deleteIfExists(spill);
        }
    }

    private byte[] header(int n, boolean indexed, double[] extent) {
        FlatBufferBuilder header = new FlatBufferBuilder(1024);
        int[] columnOffsets = new int[schema.columns().size()];
        for (int k = 0; k < columnOffsets.length; k++) {
            ExportSchema.Column column = schema.columns().get(k);
            int name = header.createString(column.name());
            org.wololo.flatgeobuf.generated.Column.startColumn(header);
            org.wololo.flatgeobuf.generated.Column.addName(header, name);
            org.wololo.flatgeobuf.generated.Column.addType(header, columnType(column));
            org.wololo.flatgeobuf.generated.Column.addNullable(header, column.nullable());
            columnOffsets[k] = org.wololo.flatgeobuf.generated.Column.endColumn(header);
        }
        int columns = Header.createColumnsVector(header, columnOffsets);
        int name = header.createString(metadata.getOrDefault("name", "foresignal"));
        int description = metadata.get("attribution") == null ? 0 : header.createString(metadata.get("attribution"));
        int license = metadata.get("licence") == null ? 0 : header.createString(metadata.get("licence"));
        int org = header.createString("EPSG");
        Crs.startCrs(header);
        Crs.addOrg(header, org);
        Crs.addCode(header, 4326);
        int crs = Crs.endCrs(header);
        int envelope = extent == null ? 0 : Header.createEnvelopeVector(header, extent);
        Header.startHeader(header);
        Header.addName(header, name);
        if (envelope != 0) {
            Header.addEnvelope(header, envelope);
        }
        Header.addGeometryType(header, commonType < 0 ? GeometryType.Unknown : commonType);
        Header.addColumns(header, columns);
        Header.addFeaturesCount(header, n);
        Header.addIndexNodeSize(header, indexed ? NODE_SIZE : 0);
        Header.addCrs(header, crs);
        if (description != 0) {
            Header.addDescription(header, description);
        }
        if (license != 0) {
            Header.addMetadata(header, license);
        }
        int root = Header.endHeader(header);
        Header.finishSizePrefixedHeaderBuffer(header, root);
        return header.sizedByteArray();
    }

    private double[] extent(int n) {
        double[] out = null;
        for (int k = withGeometry.nextSetBit(0); k >= 0 && k < n; k = withGeometry.nextSetBit(k + 1)) {
            if (out == null) {
                out = new double[] {boxes[k * 4], boxes[k * 4 + 1], boxes[k * 4 + 2], boxes[k * 4 + 3]};
            } else {
                out[0] = Math.min(out[0], boxes[k * 4]);
                out[1] = Math.min(out[1], boxes[k * 4 + 1]);
                out[2] = Math.max(out[2], boxes[k * 4 + 2]);
                out[3] = Math.max(out[3], boxes[k * 4 + 3]);
            }
        }
        return out;
    }

    /** The feature indexes sorted by the Hilbert value of their envelope centers (descending, as FlatGeobuf). */
    private int[] hilbertOrder(int n) {
        double[] extent = extent(n);
        double width = extent[2] - extent[0];
        double height = extent[3] - extent[1];
        long[] keys = new long[n];
        for (int k = 0; k < n; k++) {
            double cx = (boxes[k * 4] + boxes[k * 4 + 2]) / 2;
            double cy = (boxes[k * 4 + 1] + boxes[k * 4 + 3]) / 2;
            int hx = width == 0 ? 0 : (int) Math.floor(HILBERT_MAX * (cx - extent[0]) / width);
            int hy = height == 0 ? 0 : (int) Math.floor(HILBERT_MAX * (cy - extent[1]) / height);
            long h = hilbert(hx, hy) & 0xFFFFFFFFL;
            keys[k] = (h << 31) | k;
        }
        Arrays.sort(keys);
        int[] order = new int[n];
        for (int k = 0; k < n; k++) {
            order[k] = (int) (keys[n - 1 - k] & 0x7FFFFFFFL);
        }
        return order;
    }

    private static int[] identity(int n) {
        int[] order = new int[n];
        for (int k = 0; k < n; k++) {
            order[k] = k;
        }
        return order;
    }

    /**
     * The 2D Hilbert curve index of {@code (x, y)} in a 16-bit grid (the bit-twiddling form used by
     * FlatGeobuf and flatbush, after Rawrunprotected's public-domain algorithm).
     */
    static int hilbert(int x, int y) {
        int a = x ^ y;
        int b = 0xFFFF ^ a;
        int c = 0xFFFF ^ (x | y);
        int d = x & (y ^ 0xFFFF);
        int aa = a | (b >>> 1);
        int bb = (a >>> 1) ^ a;
        int cc = ((c >>> 1) ^ (b & (d >>> 1))) ^ c;
        int dd = ((a & (c >>> 1)) ^ (d >>> 1)) ^ d;
        a = aa;
        b = bb;
        c = cc;
        d = dd;
        aa = (a & (a >>> 2)) ^ (b & (b >>> 2));
        bb = (a & (b >>> 2)) ^ (b & ((a ^ b) >>> 2));
        cc ^= (a & (c >>> 2)) ^ (b & (d >>> 2));
        dd ^= (b & (c >>> 2)) ^ ((a ^ b) & (d >>> 2));
        a = aa;
        b = bb;
        c = cc;
        d = dd;
        aa = (a & (a >>> 4)) ^ (b & (b >>> 4));
        bb = (a & (b >>> 4)) ^ (b & ((a ^ b) >>> 4));
        cc ^= (a & (c >>> 4)) ^ (b & (d >>> 4));
        dd ^= (b & (c >>> 4)) ^ ((a ^ b) & (d >>> 4));
        a = aa;
        b = bb;
        c = cc;
        d = dd;
        cc ^= (a & (c >>> 8)) ^ (b & (d >>> 8));
        dd ^= (b & (c >>> 8)) ^ ((a ^ b) & (d >>> 8));
        a = cc ^ (cc >>> 1);
        b = dd ^ (dd >>> 1);
        int i0 = x ^ y;
        int i1 = b | (0xFFFF ^ (i0 | a));
        i0 = (i0 | (i0 << 8)) & 0x00FF00FF;
        i0 = (i0 | (i0 << 4)) & 0x0F0F0F0F;
        i0 = (i0 | (i0 << 2)) & 0x33333333;
        i0 = (i0 | (i0 << 1)) & 0x55555555;
        i1 = (i1 | (i1 << 8)) & 0x00FF00FF;
        i1 = (i1 | (i1 << 4)) & 0x0F0F0F0F;
        i1 = (i1 | (i1 << 2)) & 0x33333333;
        i1 = (i1 | (i1 << 1)) & 0x55555555;
        return (i1 << 1) | i0;
    }

    /**
     * The level bounds of a packed R-tree over {@code numItems} leaves: for each level from the
     * leaves up to the root, {@code [start, end)} node indexes. Leaves are stored last, the root first.
     */
    static long[][] levelBounds(long numItems, int nodeSize) {
        java.util.List<Long> levelNumNodes = new java.util.ArrayList<>();
        long n = numItems;
        long numNodes = n;
        levelNumNodes.add(n);
        do {
            n = (n + nodeSize - 1) / nodeSize;
            numNodes += n;
            levelNumNodes.add(n);
        } while (n != 1);
        long[][] bounds = new long[levelNumNodes.size()][2];
        long end = numNodes;
        for (int k = 0; k < levelNumNodes.size(); k++) {
            long size = levelNumNodes.get(k);
            bounds[k][0] = end - size;
            bounds[k][1] = end;
            end -= size;
        }
        return bounds;
    }

    private void writeIndex(OutputStream out, int[] order, int n) throws IOException {
        long[][] levels = levelBounds(n, NODE_SIZE);
        int total = (int) levels[0][1];
        double[] nodes = new double[total * 4];
        long[] nodeOffsets = new long[total];
        long featureOffset = 0;
        int leafStart = (int) levels[0][0];
        for (int k = 0; k < n; k++) {
            int item = order[k];
            System.arraycopy(boxes, item * 4, nodes, (leafStart + k) * 4, 4);
            nodeOffsets[leafStart + k] = featureOffset;
            featureOffset += sizes[item];
        }
        for (int level = 0; level < levels.length - 1; level++) {
            int pos = (int) levels[level][0];
            int end = (int) levels[level][1];
            int parent = (int) levels[level + 1][0];
            while (pos < end) {
                double minX = Double.POSITIVE_INFINITY;
                double minY = Double.POSITIVE_INFINITY;
                double maxX = Double.NEGATIVE_INFINITY;
                double maxY = Double.NEGATIVE_INFINITY;
                nodeOffsets[parent] = pos;
                for (int j = 0; j < NODE_SIZE && pos < end; j++, pos++) {
                    minX = Math.min(minX, nodes[pos * 4]);
                    minY = Math.min(minY, nodes[pos * 4 + 1]);
                    maxX = Math.max(maxX, nodes[pos * 4 + 2]);
                    maxY = Math.max(maxY, nodes[pos * 4 + 3]);
                }
                nodes[parent * 4] = minX;
                nodes[parent * 4 + 1] = minY;
                nodes[parent * 4 + 2] = maxX;
                nodes[parent * 4 + 3] = maxY;
                parent++;
            }
        }
        ByteBuffer node = ByteBuffer.allocate(40).order(ByteOrder.LITTLE_ENDIAN);
        for (int k = 0; k < total; k++) {
            node.clear();
            node.putDouble(nodes[k * 4]).putDouble(nodes[k * 4 + 1]).putDouble(nodes[k * 4 + 2])
                    .putDouble(nodes[k * 4 + 3]).putLong(nodeOffsets[k]);
            out.write(node.array());
        }
    }

    private void copyFeatures(OutputStream out, int[] order) throws IOException {
        try (FileChannel channel = FileChannel.open(spill, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1 << 16);
            for (int item : order) {
                long position = offsets[item];
                int remaining = sizes[item];
                while (remaining > 0) {
                    buffer.clear();
                    buffer.limit(Math.min(buffer.capacity(), remaining));
                    int read = channel.read(buffer, position);
                    if (read < 0) {
                        throw new IOException("The FlatGeobuf spill file ended early");
                    }
                    out.write(buffer.array(), 0, read);
                    position += read;
                    remaining -= read;
                }
            }
        }
    }
}
