package ai.foresignal.export;

import com.github.luben.zstd.Zstd;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.parquet.bytes.BytesInput;
import org.apache.parquet.compression.CompressionCodecFactory;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

/**
 * The Parquet compression codecs of the export writers (prompt 111), without Hadoop: {@code ZSTD}
 * through zstd-jni (already a dependency of parquet-hadoop) and {@code UNCOMPRESSED}.
 *
 * <p>parquet-hadoop's own {@code CodecFactory} instantiates Hadoop {@code CompressionCodec} classes
 * through a Hadoop {@code Configuration}, which drags in the Hadoop runtime. A Parquet ZSTD page is a
 * plain Zstandard frame, so compressing and decompressing it with zstd-jni directly is equivalent and
 * keeps the runtime classpath free of Hadoop. Both the writer ({@link GeoParquetWriter}) and the
 * read-back used to verify archive files ({@link GeoParquetFiles}) use this factory.
 */
public final class ParquetCodecs implements CompressionCodecFactory {

    /** The shared instance; it holds no state. */
    public static final ParquetCodecs INSTANCE = new ParquetCodecs();

    /** The codec every export writes. */
    public static final CompressionCodecName DEFAULT_CODEC = CompressionCodecName.ZSTD;

    /** The Zstandard level used for pages (zstd-jni's default level 3). */
    public static final int ZSTD_LEVEL = 3;

    private ParquetCodecs() {}

    @Override
    public BytesInputCompressor getCompressor(CompressionCodecName codec) {
        return switch (codec) {
            case ZSTD -> new ZstdCompressor();
            case UNCOMPRESSED -> new Identity();
            default -> throw new IllegalArgumentException("Unsupported export codec " + codec);
        };
    }

    @Override
    public BytesInputDecompressor getDecompressor(CompressionCodecName codec) {
        return switch (codec) {
            case ZSTD -> new ZstdDecompressor();
            case UNCOMPRESSED -> new Identity();
            default -> throw new IllegalArgumentException("Unsupported export codec " + codec);
        };
    }

    @Override
    public void release() {
        // Stateless.
    }

    /** Compresses a page as one Zstandard frame. */
    static final class ZstdCompressor implements BytesInputCompressor {

        @Override
        public BytesInput compress(BytesInput bytes) throws IOException {
            return BytesInput.from(Zstd.compress(bytes.toByteArray(), ZSTD_LEVEL));
        }

        @Override
        public CompressionCodecName getCodecName() {
            return CompressionCodecName.ZSTD;
        }

        @Override
        public void release() {
            // Stateless.
        }
    }

    /** Decompresses a Zstandard page. */
    static final class ZstdDecompressor implements BytesInputDecompressor {

        @Override
        public BytesInput decompress(BytesInput bytes, int decompressedSize) throws IOException {
            byte[] out = Zstd.decompress(bytes.toByteArray(), decompressedSize);
            if (out.length != decompressedSize) {
                throw new IOException("A ZSTD page decompressed to " + out.length + " bytes, expected "
                        + decompressedSize);
            }
            return BytesInput.from(out);
        }

        @Override
        public void decompress(ByteBuffer input, int compressedSize, ByteBuffer output, int decompressedSize)
                throws IOException {
            byte[] in = new byte[compressedSize];
            input.get(in);
            output.put(Zstd.decompress(in, decompressedSize));
        }

        @Override
        public void release() {
            // Stateless.
        }
    }

    /** {@code UNCOMPRESSED} in both directions. */
    static final class Identity implements BytesInputCompressor, BytesInputDecompressor {

        @Override
        public BytesInput compress(BytesInput bytes) {
            return bytes;
        }

        @Override
        public CompressionCodecName getCodecName() {
            return CompressionCodecName.UNCOMPRESSED;
        }

        @Override
        public BytesInput decompress(BytesInput bytes, int decompressedSize) {
            return bytes;
        }

        @Override
        public void decompress(ByteBuffer input, int compressedSize, ByteBuffer output, int decompressedSize) {
            ByteBuffer slice = input.slice();
            slice.limit(compressedSize);
            output.put(slice);
            input.position(input.position() + compressedSize);
        }

        @Override
        public void release() {
            // Stateless.
        }
    }
}
