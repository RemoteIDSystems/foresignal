package ai.foresignal.export;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.bytes.HeapByteBufferAllocator;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.conf.PlainParquetConfiguration;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.filter2.compat.FilterCompat;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.LocalInputFile;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;

/**
 * Reads a GeoParquet file back row by row with parquet-java's own reader, for the round-trip tests
 * (prompt 111), without Hadoop on the classpath.
 *
 * <p>{@code ParquetReadOptions.builder()} instantiates Hadoop codec classes in its constructor, so
 * the options are built through the package-private constructor that takes a codec factory, with
 * {@link ParquetCodecs} as the factory. Test-only: production code reads only the footer
 * ({@link GeoParquetFiles}).
 */
final class GeoParquetTestReader {

    private GeoParquetTestReader() {}

    /** Hadoop-free read options with {@link ParquetCodecs}. */
    static ParquetReadOptions options() {
        try {
            for (Constructor<?> constructor : ParquetReadOptions.class.getDeclaredConstructors()) {
                Class<?>[] types = constructor.getParameterTypes();
                if (types.length == 18) {
                    constructor.setAccessible(true);
                    return (ParquetReadOptions) constructor.newInstance(false, false, false, false, false, false, false,
                            false, false, FilterCompat.NOOP, ParquetMetadataConverter.NO_FILTER, ParquetCodecs.INSTANCE,
                            new HeapByteBufferAllocator(), 8 * 1024 * 1024, new HashMap<String, String>(), null, null,
                            new PlainParquetConfiguration());
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not build ParquetReadOptions", e);
        }
        throw new IllegalStateException("ParquetReadOptions has no 18-argument constructor in this parquet-java version");
    }

    /** The file's footer. */
    static ParquetMetadata footer(Path file) throws IOException {
        try (ParquetFileReader reader = ParquetFileReader.open(new LocalInputFile(file), options())) {
            return reader.getFooter();
        }
    }

    /** Every row of {@code file}, as example {@link Group}s. */
    static List<Group> rows(Path file) throws IOException {
        List<Group> out = new ArrayList<>();
        try (ParquetFileReader reader = ParquetFileReader.open(new LocalInputFile(file), options())) {
            MessageType schema = reader.getFooter().getFileMetaData().getSchema();
            MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
            PageReadStore pages;
            while ((pages = reader.readNextRowGroup()) != null) {
                RecordReader<Group> records = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));
                for (long k = 0; k < pages.getRowCount(); k++) {
                    out.add(records.read());
                }
            }
        }
        return out;
    }
}
