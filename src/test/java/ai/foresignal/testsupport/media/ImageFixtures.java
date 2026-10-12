package ai.foresignal.testsupport.media;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;

/**
 * Images for the media tests (prompt 043): real small PNG, JPEG and TIFF files encoded with
 * {@code ImageIO}, and crafted headers that declare huge dimensions with no pixel data behind them, so
 * a test proves the guard refuses them from the header alone.
 */
public final class ImageFixtures {

    private ImageFixtures() {}

    /** An opaque RGB PNG with a gradient and some noise. */
    public static byte[] png(int width, int height) {
        return encode(rgb(width, height, false), "png");
    }

    /** A PNG with an alpha channel. */
    public static byte[] pngWithAlpha(int width, int height) {
        return encode(rgb(width, height, true), "png");
    }

    /** A 16-bit grayscale PNG whose samples all equal {@code value}. */
    public static byte[] png16Gray(int width, int height, int value) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster raster = image.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.setSample(x, y, 0, value);
            }
        }
        return encode(image, "png");
    }

    /** A baseline JPEG. */
    public static byte[] jpeg(int width, int height) {
        return encode(rgb(width, height, false), "jpeg");
    }

    /** An 8-bit grayscale single-image TIFF. */
    public static byte[] tiff(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return encode(image, "tiff");
    }

    /** A PNG of {@code width x height} random pixels, which compresses badly (for size limits). */
    public static byte[] noisyPng(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(43);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, random.nextInt());
            }
        }
        return encode(image, "png");
    }

    /**
     * A PNG signature and IHDR declaring {@code width x height} (8-bit grayscale), followed only by
     * IEND: no pixel data exists.
     */
    public static byte[] craftedPngHeader(int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
        ByteBuffer ihdr = ByteBuffer.allocate(13).order(ByteOrder.BIG_ENDIAN);
        ihdr.putInt(width).putInt(height).put((byte) 8).put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0);
        chunk(out, "IHDR", ihdr.array());
        chunk(out, "IEND", new byte[0]);
        return out.toByteArray();
    }

    /** The first {@code length} bytes of {@code bytes}: a truncated file. */
    public static byte[] truncated(byte[] bytes, int length) {
        return Arrays.copyOf(bytes, length);
    }

    /**
     * A little-endian TIFF with {@code directories} image file directories declaring
     * {@code width x height} 8-bit grayscale and a strip offset past the end of the file: no pixel
     * data exists.
     */
    public static byte[] craftedTiffHeader(int width, int height, int directories) {
        int entries = 9;
        int ifdSize = 2 + entries * 12 + 4;
        ByteBuffer b = ByteBuffer.allocate(8 + ifdSize * directories).order(ByteOrder.LITTLE_ENDIAN);
        b.put((byte) 'I').put((byte) 'I').putShort((short) 42).putInt(8);
        for (int d = 0; d < directories; d++) {
            int next = d + 1 < directories ? 8 + ifdSize * (d + 1) : 0;
            b.putShort((short) entries);
            entry(b, 256, 4, width);
            entry(b, 257, 4, height);
            entry(b, 258, 3, 8);
            entry(b, 259, 3, 1);
            entry(b, 262, 3, 1);
            entry(b, 273, 4, 1_000_000);
            entry(b, 277, 3, 1);
            entry(b, 278, 4, height);
            entry(b, 279, 4, (int) Math.min(Integer.MAX_VALUE, (long) width * height));
            b.putInt(next);
        }
        return b.array();
    }

    /** A BigTIFF header ({@code II+\0} or {@code MM\0+}). */
    public static byte[] bigTiffHeader(boolean littleEndian) {
        ByteBuffer b = ByteBuffer.allocate(16).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        b.put(littleEndian ? (byte) 'I' : (byte) 'M').put(littleEndian ? (byte) 'I' : (byte) 'M');
        b.putShort((short) 43).putShort((short) 8).putShort((short) 0).putLong(16);
        return b.array();
    }

    /**
     * A real JPEG whose SOF0 segment is patched to declare {@code width x height}: the scan data is
     * that of a 16 x 16 image, so decoding would fail, but the header reads as huge.
     */
    public static byte[] craftedJpegHeader(int width, int height) {
        byte[] jpeg = jpeg(16, 16);
        for (int i = 2; i + 8 < jpeg.length; i++) {
            if ((jpeg[i] & 0xff) == 0xFF && (jpeg[i + 1] & 0xff) == 0xC0) {
                jpeg[i + 5] = (byte) (height >> 8);
                jpeg[i + 6] = (byte) height;
                jpeg[i + 7] = (byte) (width >> 8);
                jpeg[i + 8] = (byte) width;
                return jpeg;
            }
        }
        throw new IllegalStateException("The encoder wrote no SOF0 segment");
    }

    /** A GIF header (an unsupported format). */
    public static byte[] gif() {
        return "GIF89a\u0001\u0000\u0001\u0000".getBytes(StandardCharsets.ISO_8859_1);
    }

    private static void entry(ByteBuffer b, int tag, int type, int value) {
        b.putShort((short) tag).putShort((short) type).putInt(1);
        if (type == 3) {
            b.putShort((short) value).putShort((short) 0);
        } else {
            b.putInt(value);
        }
    }

    private static void chunk(ByteArrayOutputStream out, String type, byte[] data) {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        out.writeBytes(ByteBuffer.allocate(4).putInt(data.length).array());
        out.writeBytes(typeBytes);
        out.writeBytes(data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        out.writeBytes(ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
    }

    private static BufferedImage rgb(int width, int height, boolean alpha) {
        BufferedImage image = new BufferedImage(width, height, alpha ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = x * 255 / Math.max(1, width - 1);
                int g = y * 255 / Math.max(1, height - 1);
                int a = alpha ? (x + y) % 256 : 255;
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | 128);
            }
        }
        return image;
    }

    private static byte[] encode(BufferedImage image, String format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, format, out)) {
                throw new IllegalStateException("No " + format + " writer");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }
}
