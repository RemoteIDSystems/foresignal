package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.parquet.bytes.BytesInput;
import org.apache.parquet.compression.CompressionCodecFactory;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The Hadoop-free Parquet codecs (prompt 111): ZSTD through zstd-jni, and UNCOMPRESSED. */
class ParquetCodecsTest {

    private static final byte[] PAGE = "a page of parquet values ".repeat(200).getBytes(StandardCharsets.UTF_8);

    @Test
    @DisplayName("ZSTD compresses a page to a smaller frame that decompresses to the same bytes")
    void zstdRoundTrip() throws Exception {
        CompressionCodecFactory.BytesInputCompressor compressor =
                ParquetCodecs.INSTANCE.getCompressor(CompressionCodecName.ZSTD);
        assertThat(compressor.getCodecName()).isEqualTo(CompressionCodecName.ZSTD);
        byte[] compressed = compressor.compress(BytesInput.from(PAGE)).toByteArray();
        assertThat(compressed.length).isLessThan(PAGE.length);
        CompressionCodecFactory.BytesInputDecompressor decompressor =
                ParquetCodecs.INSTANCE.getDecompressor(CompressionCodecName.ZSTD);
        assertThat(decompressor.decompress(BytesInput.from(compressed), PAGE.length).toByteArray()).isEqualTo(PAGE);
        ByteBuffer out = ByteBuffer.allocate(PAGE.length);
        decompressor.decompress(ByteBuffer.wrap(compressed), compressed.length, out, PAGE.length);
        assertThat(out.array()).isEqualTo(PAGE);
        assertThatThrownBy(() -> decompressor.decompress(BytesInput.from(compressed), PAGE.length + 1))
                .isInstanceOf(Exception.class);
        compressor.release();
        decompressor.release();
        ParquetCodecs.INSTANCE.release();
    }

    @Test
    @DisplayName("UNCOMPRESSED passes bytes through; other codecs are refused")
    void identityAndUnsupported() throws Exception {
        assertThat(ParquetCodecs.INSTANCE.getCompressor(CompressionCodecName.UNCOMPRESSED)
                .compress(BytesInput.from(PAGE)).toByteArray()).isEqualTo(PAGE);
        CompressionCodecFactory.BytesInputDecompressor identity =
                ParquetCodecs.INSTANCE.getDecompressor(CompressionCodecName.UNCOMPRESSED);
        assertThat(identity.decompress(BytesInput.from(PAGE), PAGE.length).toByteArray()).isEqualTo(PAGE);
        ByteBuffer out = ByteBuffer.allocate(PAGE.length);
        identity.decompress(ByteBuffer.wrap(PAGE), PAGE.length, out, PAGE.length);
        assertThat(out.array()).isEqualTo(PAGE);
        assertThatThrownBy(() -> ParquetCodecs.INSTANCE.getCompressor(CompressionCodecName.GZIP))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ParquetCodecs.INSTANCE.getDecompressor(CompressionCodecName.SNAPPY))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
