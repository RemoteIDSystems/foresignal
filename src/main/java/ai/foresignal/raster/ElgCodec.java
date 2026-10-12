package ai.foresignal.raster;

import ai.foresignal.source.spi.RasterDtype;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32C;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * The {@code .fsg} frame codec (Weather-Imagery-Design §2.3). The class keeps its pre-rename name
 * ({@code Elg}, from Elaro) as the prompt names it; the format's magic is {@code FSRG}.
 *
 * <p>Layout, little-endian, a 64-byte header then the payload:
 *
 * <pre>
 *  0  4  magic "FSRG"          24 8  scale f64
 *  4  2  version u16 = 1       32 8  offset f64
 *  6  1  dtype u8              40 8  nodata f64 (raw code)
 *  7  1  codec u8 (0, 1)       48 8  payloadLength u64 (as stored)
 *  8  4  nx u32                56 4  crc32c u32 of the stored payload
 * 12  4  ny u32                60 4  reserved, 0
 * 16  2  bands u16
 * 18  2  flags u16 (bit 0 partial)
 * 20  4  reserved, 0
 * </pre>
 *
 * <p>The decoded payload is band-sequential, rows north to south, samples little-endian. Codec 1
 * compresses it with {@code Deflater(1)}. Reading verifies the magic, the version, both reserved
 * words, the flag bits, the payload length and the CRC32C, and rejects anything else with
 * {@link RasterFormatException}: a frame file is never half-trusted.
 */
public final class ElgCodec {

    /** The header length. */
    public static final int HEADER_BYTES = 64;

    /** The file magic. */
    public static final byte[] MAGIC = "FSRG".getBytes(StandardCharsets.US_ASCII);

    /** The only version this build writes and reads. */
    public static final int VERSION = 1;

    /** Flags bit 0: partial frame. */
    public static final int FLAG_PARTIAL = 1;

    /** The deflate level the format uses. */
    public static final int DEFLATE_LEVEL = 1;

    private ElgCodec() {}

    /**
     * A decoded frame: its header and the decoded payload.
     *
     * @param header the header
     * @param payload the decoded payload, {@link FsgHeader#decodedLength()} bytes
     */
    public record Decoded(FsgHeader header, byte[] payload) {}

    /**
     * Encodes a frame file.
     *
     * @param header the header; its codec decides the compression
     * @param decoded the decoded payload
     * @return the whole file, header and stored payload
     * @throws IllegalArgumentException when {@code decoded} has the wrong length
     */
    public static byte[] encode(FsgHeader header, byte[] decoded) {
        if (decoded.length != header.decodedLength()) {
            throw new IllegalArgumentException("Decoded payload is " + decoded.length + " bytes; the header needs "
                    + header.decodedLength());
        }
        byte[] stored = header.codec() == FsgHeader.CODEC_DEFLATE ? deflate(decoded) : decoded;
        ByteBuffer out = ByteBuffer.allocate(HEADER_BYTES + stored.length).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(out, header, stored);
        out.put(stored);
        return out.array();
    }

    /**
     * The 64 header bytes for {@code header} over {@code stored} (the payload as written).
     */
    public static byte[] headerBytes(FsgHeader header, byte[] stored) {
        ByteBuffer out = ByteBuffer.allocate(HEADER_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(out, header, stored);
        return out.array();
    }

    private static void writeHeader(ByteBuffer out, FsgHeader header, byte[] stored) {
        CRC32C crc = new CRC32C();
        crc.update(stored);
        out.put(MAGIC);
        out.putShort((short) VERSION);
        out.put((byte) header.dtype().code());
        out.put((byte) header.codec());
        out.putInt(header.nx());
        out.putInt(header.ny());
        out.putShort((short) header.bands());
        out.putShort((short) (header.partial() ? FLAG_PARTIAL : 0));
        out.putInt(0);
        out.putDouble(header.scale());
        out.putDouble(header.offset());
        out.putDouble(header.nodata());
        out.putLong(stored.length);
        out.putInt((int) crc.getValue());
        out.putInt(0);
    }

    /**
     * Reads and checks a header.
     *
     * @param bytes at least {@value #HEADER_BYTES} bytes, the start of a file
     * @return the header and the stored payload length and CRC
     * @throws RasterFormatException for anything this build does not write
     */
    public static RawHeader readHeader(byte[] bytes) {
        if (bytes.length < HEADER_BYTES) {
            throw new RasterFormatException("A frame file needs a 64-byte header; got " + bytes.length + " bytes");
        }
        ByteBuffer in = ByteBuffer.wrap(bytes, 0, HEADER_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        byte[] magic = new byte[4];
        in.get(magic);
        if (!java.util.Arrays.equals(magic, MAGIC)) {
            throw new RasterFormatException("Not a frame file: bad magic");
        }
        int version = Short.toUnsignedInt(in.getShort());
        if (version != VERSION) {
            throw new RasterFormatException("Unsupported frame file version " + version);
        }
        int dtypeCode = Byte.toUnsignedInt(in.get());
        int codec = Byte.toUnsignedInt(in.get());
        long nx = Integer.toUnsignedLong(in.getInt());
        long ny = Integer.toUnsignedLong(in.getInt());
        int bands = Short.toUnsignedInt(in.getShort());
        int flags = Short.toUnsignedInt(in.getShort());
        int reserved1 = in.getInt();
        double scale = in.getDouble();
        double offset = in.getDouble();
        double nodata = in.getDouble();
        long payloadLength = in.getLong();
        long crc = Integer.toUnsignedLong(in.getInt());
        int reserved2 = in.getInt();
        if (reserved1 != 0 || reserved2 != 0) {
            throw new RasterFormatException("Reserved header bytes must be zero");
        }
        if ((flags & ~FLAG_PARTIAL) != 0) {
            throw new RasterFormatException("Unknown header flags 0x" + Integer.toHexString(flags));
        }
        RasterDtype dtype;
        try {
            dtype = RasterDtype.fromCode(dtypeCode);
        } catch (IllegalArgumentException e) {
            throw new RasterFormatException("Unknown dtype code " + dtypeCode, e);
        }
        if (nx == 0 || ny == 0 || nx > Integer.MAX_VALUE || ny > Integer.MAX_VALUE || bands == 0) {
            throw new RasterFormatException("Impossible frame dimensions " + nx + "x" + ny + "x" + bands);
        }
        if (payloadLength < 0 || payloadLength > Integer.MAX_VALUE) {
            throw new RasterFormatException("Impossible payload length " + payloadLength);
        }
        FsgHeader header;
        try {
            header = new FsgHeader(dtype, codec, (int) nx, (int) ny, bands, (flags & FLAG_PARTIAL) != 0, scale,
                    offset, nodata);
        } catch (IllegalArgumentException e) {
            throw new RasterFormatException(e.getMessage(), e);
        }
        if (header.decodedLength() > Integer.MAX_VALUE) {
            throw new RasterFormatException("Frame too large to decode: " + header.decodedLength() + " bytes");
        }
        if (codec == FsgHeader.CODEC_NONE && payloadLength != header.decodedLength()) {
            throw new RasterFormatException("Uncompressed payload is " + payloadLength + " bytes; expected "
                    + header.decodedLength());
        }
        return new RawHeader(header, payloadLength, crc);
    }

    /**
     * A header as read, with the stored payload's length and CRC32C.
     *
     * @param header the header
     * @param payloadLength stored payload bytes
     * @param crc32c the CRC32C of the stored payload
     */
    public record RawHeader(FsgHeader header, long payloadLength, long crc32c) {}

    /**
     * Decodes a whole frame file.
     *
     * @throws RasterFormatException for a bad header, a short or long payload, a CRC mismatch or a
     *     payload that does not inflate to the expected length
     */
    public static Decoded decode(byte[] file) {
        RawHeader raw = readHeader(file);
        long available = file.length - HEADER_BYTES;
        if (available != raw.payloadLength()) {
            throw new RasterFormatException("Payload is " + available + " bytes; the header says " + raw.payloadLength());
        }
        CRC32C crc = new CRC32C();
        crc.update(file, HEADER_BYTES, (int) raw.payloadLength());
        if (crc.getValue() != raw.crc32c()) {
            throw new RasterFormatException("CRC32C mismatch: the frame file is corrupt");
        }
        int expected = (int) raw.header().decodedLength();
        byte[] payload;
        if (raw.header().codec() == FsgHeader.CODEC_DEFLATE) {
            payload = inflate(file, HEADER_BYTES, (int) raw.payloadLength(), expected);
        } else {
            payload = java.util.Arrays.copyOfRange(file, HEADER_BYTES, file.length);
        }
        return new Decoded(raw.header(), payload);
    }

    /** Deflates {@code data} at level {@value #DEFLATE_LEVEL}. */
    static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater(DEFLATE_LEVEL);
        try {
            deflater.setInput(data);
            deflater.finish();
            ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, data.length / 4));
            byte[] buffer = new byte[64 * 1024];
            while (!deflater.finished()) {
                int n = deflater.deflate(buffer);
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } finally {
            deflater.end();
        }
    }

    private static byte[] inflate(byte[] data, int offset, int length, int expected) {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(data, offset, length);
            byte[] out = new byte[expected];
            int filled = 0;
            while (filled < expected) {
                int n = inflater.inflate(out, filled, expected - filled);
                if (n == 0) {
                    if (inflater.finished() || inflater.needsInput() || inflater.needsDictionary()) {
                        break;
                    }
                }
                filled += n;
            }
            if (filled != expected) {
                throw new RasterFormatException("Payload inflates to the wrong length (expected " + expected + ")");
            }
            if (!inflater.finished()) {
                // The stream may still hold its end marker; anything more is a longer payload.
                int extra = inflater.inflate(new byte[1]);
                if (extra > 0 || !inflater.finished()) {
                    throw new RasterFormatException("Payload inflates past the expected length " + expected);
                }
            }
            return out;
        } catch (DataFormatException e) {
            throw new RasterFormatException("Payload is not valid deflate data", e);
        } finally {
            inflater.end();
        }
    }
}
