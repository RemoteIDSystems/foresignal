package ai.foresignal.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.parquet.format.FileMetaData;
import org.apache.parquet.format.KeyValue;
import org.apache.parquet.format.RowGroup;
import org.apache.parquet.format.Util;

/**
 * Reads back the footer of a file {@link GeoParquetWriter} wrote (prompt 111), without Hadoop.
 *
 * <p>parquet-hadoop's {@code ParquetFileReader} cannot be used here: building its
 * {@code ParquetReadOptions} instantiates Hadoop codec classes, which are deliberately not on the
 * runtime classpath. The footer is read directly instead: a Parquet file ends with the Thrift
 * {@code FileMetaData}, its 4-byte little-endian length and the magic {@code PAR1}, and
 * {@code parquet-format-structures} decodes it. The cold archive verifies every part file with
 * {@link #rowCount} before retention deletes the rows it holds.
 */
public final class GeoParquetFiles {

    /** The Parquet magic at the start and end of every file. */
    static final byte[] MAGIC = "PAR1".getBytes(StandardCharsets.US_ASCII);

    private GeoParquetFiles() {}

    /** The decoded footer of {@code file}. */
    public static FileMetaData footer(Path file) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            long size = channel.size();
            if (size < 12) {
                throw new IOException(file + " is too short to be a Parquet file");
            }
            ByteBuffer head = ByteBuffer.allocate(4);
            channel.read(head, 0);
            ByteBuffer tail = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(tail, size - 8);
            byte[] trailing = new byte[4];
            tail.position(4);
            tail.get(trailing);
            if (!java.util.Arrays.equals(head.array(), MAGIC) || !java.util.Arrays.equals(trailing, MAGIC)) {
                throw new IOException(file + " does not carry the Parquet magic");
            }
            int length = tail.getInt(0);
            if (length <= 0 || length > size - 12) {
                throw new IOException(file + " has a footer length of " + length);
            }
            ByteBuffer footer = ByteBuffer.allocate(length);
            long position = size - 8 - length;
            while (footer.hasRemaining()) {
                int read = channel.read(footer, position + footer.position());
                if (read < 0) {
                    throw new IOException(file + " ended inside its footer");
                }
            }
            return Util.readFileMetaData(new ByteArrayInputStream(footer.array()));
        }
    }

    /**
     * The number of rows in {@code file}, from its footer.
     *
     * @throws IOException when the file is not Parquet, or the footer's total disagrees with the sum
     *     of its row groups
     */
    public static long rowCount(Path file) throws IOException {
        FileMetaData footer = footer(file);
        long sum = 0;
        if (footer.getRow_groups() != null) {
            for (RowGroup group : footer.getRow_groups()) {
                sum += group.getNum_rows();
            }
        }
        if (sum != footer.getNum_rows()) {
            throw new IOException(file + " declares " + footer.getNum_rows() + " rows but its row groups hold " + sum);
        }
        return footer.getNum_rows();
    }

    /** The key/value metadata of {@code file}'s footer ({@code geo} among them), in file order. */
    public static Map<String, String> keyValueMetadata(Path file) throws IOException {
        Map<String, String> out = new LinkedHashMap<>();
        FileMetaData footer = footer(file);
        if (footer.getKey_value_metadata() != null) {
            for (KeyValue entry : footer.getKey_value_metadata()) {
                out.put(entry.getKey(), entry.getValue());
            }
        }
        return out;
    }
}
