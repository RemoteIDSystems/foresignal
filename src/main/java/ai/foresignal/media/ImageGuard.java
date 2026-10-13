package ai.foresignal.media;

import ai.foresignal.source.spi.ImageRejectedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.awt.image.SampleModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Inspects an image from its header before any pixel is decoded (prompt 043, Weather-Imagery-Design
 * §3 "Image guard"), so a hostile or corrupt file in an inbox or an ingest body cannot exhaust memory.
 *
 * <p>In order, {@link #inspect(Path, String, boolean)}:
 *
 * <ol>
 *   <li>refuses a file over {@code foresignal.media.max-file-bytes} (default 25 MB);
 *   <li>sniffs the magic bytes: PNG {@code 89 50 4E 47}, JPEG {@code FF D8 FF}, TIFF {@code II*\0} or
 *       {@code MM\0*}; BigTIFF ({@code II+\0}, {@code MM\0+}) is refused with
 *       {@value ImageRejectedException#BIGTIFF_REASON};
 *   <li>requires the declared content type to agree with the sniffed format;
 *   <li>reads the dimensions from the header only ({@code ImageIO.getImageReaders(ImageInputStream)},
 *       then {@code getWidth(0)} and {@code getHeight(0)} with {@code ignoreMetadata=true}; never
 *       {@code read()});
 *   <li>refuses more than {@value #MAX_PIXELS} pixels, then a side over {@value #MAX_SIDE} px;
 *   <li>refuses a TIFF with more than one image file directory unless the caller opts in.
 * </ol>
 *
 * <p>Every refusal increments {@code foresignal.media.guard.rejected{reason}}. Decoding goes through
 * {@link GuardedImages}, which calls this first.
 */
@Component
public class ImageGuard {

    /** The rejection counter. */
    public static final String REJECTED_METRIC = "foresignal.media.guard.rejected";

    /** The longest side accepted, pixels. */
    public static final int MAX_SIDE = 12_000;

    /** The most pixels accepted (150 megapixels). */
    public static final long MAX_PIXELS = 150_000_000L;

    /** Most TIFF directories walked when counting images. */
    private static final int MAX_IFD_WALK = 2;

    private final long maxFileBytes;
    private final MeterRegistry meters;

    /** Creates the guard from {@code foresignal.media.*}. */
    @org.springframework.beans.factory.annotation.Autowired
    public ImageGuard(MediaProperties properties, ObjectProvider<MeterRegistry> meters) {
        this(properties.maxFileBytes().toBytes(), meters.getIfAvailable());
    }

    /**
     * Creates a guard (tests).
     *
     * @param maxFileBytes the largest file accepted
     * @param meters the registry for the rejection counter, or null for none
     */
    public ImageGuard(long maxFileBytes, MeterRegistry meters) {
        this.maxFileBytes = maxFileBytes;
        this.meters = meters;
    }

    /** The largest file accepted, bytes. */
    public long maxFileBytes() {
        return maxFileBytes;
    }

    /**
     * Inspects a file, refusing multi-image TIFFs.
     *
     * @param declaredContentType the declared type, or null to accept whatever is sniffed
     * @throws ImageRejectedException when the image is refused
     */
    public ImageInfo inspect(Path file, String declaredContentType) {
        return inspect(file, declaredContentType, false);
    }

    /**
     * Inspects a file.
     *
     * @param declaredContentType the declared type, or null to accept whatever is sniffed
     * @param allowMultiImage true to accept a TIFF with more than one image
     * @throws ImageRejectedException when the image is refused
     */
    public ImageInfo inspect(Path file, String declaredContentType, boolean allowMultiImage) {
        long size;
        byte[] head = new byte[8];
        int read;
        try {
            size = Files.size(file);
            if (size > maxFileBytes) {
                throw reject(ImageRejectedException.FILE_TOO_LARGE, tooLarge(size));
            }
            try (InputStream in = Files.newInputStream(file)) {
                read = in.readNBytes(head, 0, head.length);
            }
        } catch (IOException e) {
            throw reject(ImageRejectedException.UNREADABLE, "The file could not be read: " + e.getMessage());
        }
        ImageFormat format = sniffAndCheck(head, read, declaredContentType);
        // A stream of unknown length, not ImageIO's file stream: given the file length, the JDK TIFF reader
        // checks the strip offsets against it while reading the header, so a header-only file failed as
        // "Data segment out of stream" instead of reporting its dimensions (checkpoint 057). Only the bytes
        // the header reads are cached.
        try (InputStream in = Files.newInputStream(file);
                ImageInputStream iis = new MemoryCacheImageInputStream(in)) {
            return header(iis, format, allowMultiImage);
        } catch (IOException e) {
            throw reject(ImageRejectedException.TRUNCATED, "The image header could not be read: " + e.getMessage());
        }
    }

    /**
     * Inspects a stream, refusing multi-image TIFFs. At most {@code max-file-bytes + 1} bytes are read;
     * the stream is not closed.
     *
     * @throws ImageRejectedException when the image is refused
     */
    public ImageInfo inspect(InputStream in, String declaredContentType) {
        return inspect(readBounded(in), declaredContentType, false);
    }

    /**
     * Inspects bytes.
     *
     * @throws ImageRejectedException when the image is refused
     */
    public ImageInfo inspect(byte[] bytes, String declaredContentType, boolean allowMultiImage) {
        if (bytes.length > maxFileBytes) {
            throw reject(ImageRejectedException.FILE_TOO_LARGE, tooLarge(bytes.length));
        }
        ImageFormat format = sniffAndCheck(bytes, Math.min(bytes.length, 8), declaredContentType);
        try (ImageInputStream iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            return header(iis, format, allowMultiImage);
        } catch (IOException e) {
            throw reject(ImageRejectedException.TRUNCATED, "The image header could not be read: " + e.getMessage());
        }
    }

    /**
     * The format of the first bytes of a file.
     *
     * @param head the first bytes
     * @param length how many of them are valid
     * @throws ImageRejectedException for BigTIFF, an unknown format or too few bytes
     */
    public static ImageFormat sniff(byte[] head, int length) {
        if (length >= 4 && u(head, 0) == 0x89 && u(head, 1) == 0x50 && u(head, 2) == 0x4E && u(head, 3) == 0x47) {
            return ImageFormat.PNG;
        }
        if (length >= 3 && u(head, 0) == 0xFF && u(head, 1) == 0xD8 && u(head, 2) == 0xFF) {
            return ImageFormat.JPEG;
        }
        if (length >= 4) {
            boolean little = u(head, 0) == 0x49 && u(head, 1) == 0x49;
            boolean big = u(head, 0) == 0x4D && u(head, 1) == 0x4D;
            if (little && u(head, 2) == 0x2A && u(head, 3) == 0x00 || big && u(head, 2) == 0x00 && u(head, 3) == 0x2A) {
                return ImageFormat.TIFF;
            }
            if (little && u(head, 2) == 0x2B && u(head, 3) == 0x00 || big && u(head, 2) == 0x00 && u(head, 3) == 0x2B) {
                throw new ImageRejectedException(ImageRejectedException.BIGTIFF, ImageRejectedException.BIGTIFF_REASON);
            }
        }
        if (length < 4) {
            throw new ImageRejectedException(ImageRejectedException.TRUNCATED,
                    "The file is too short to identify (" + length + " bytes).");
        }
        throw new ImageRejectedException(ImageRejectedException.UNKNOWN_FORMAT,
                "The file is not a PNG, JPEG or TIFF image.");
    }

    private ImageFormat sniffAndCheck(byte[] head, int length, String declared) {
        ImageFormat format;
        try {
            format = sniff(head, length);
        } catch (ImageRejectedException e) {
            count(e.tag());
            throw e;
        }
        if (declared != null && ImageFormat.ofContentType(declared).orElse(null) != format) {
            throw reject(ImageRejectedException.TYPE_MISMATCH, "The declared content type '" + declared
                    + "' does not match the file, which is " + format.contentType() + ".");
        }
        return format;
    }

    private ImageInfo header(ImageInputStream iis, ImageFormat format, boolean allowMultiImage) {
        int[] pngIhdr = format == ImageFormat.PNG ? pngIhdr(iis) : null;
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (!readers.hasNext()) {
            throw reject(ImageRejectedException.TRUNCATED, "No reader recognizes the " + format.contentType()
                    + " header.");
        }
        ImageReader reader = readers.next();
        try {
            // seekForwardOnly=false: the TIFF directory count below seeks; ignoreMetadata=true.
            reader.setInput(iis, false, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            checkDimensions(width, height);
            if (format == ImageFormat.TIFF && !allowMultiImage && tiffDirectories(iis) > 1) {
                throw reject(ImageRejectedException.MULTI_IMAGE,
                        "The TIFF holds more than one image; only single-image TIFFs are accepted here.");
            }
            if (pngIhdr != null) {
                // Color types 4 (gray + alpha) and 6 (RGBA) carry an alpha channel.
                return new ImageInfo(format, width, height, pngIhdr[0], pngIhdr[1] == 4 || pngIhdr[1] == 6);
            }
            ImageTypeSpecifier type = imageType(reader);
            int bitDepth = 8;
            boolean alpha = false;
            if (type != null) {
                SampleModel sm = type.getSampleModel();
                bitDepth = sm.getSampleSize(0);
                alpha = type.getColorModel().hasAlpha();
            }
            return new ImageInfo(format, width, height, bitDepth, alpha);
        } catch (ImageRejectedException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw reject(ImageRejectedException.TRUNCATED, "The " + format.contentType()
                    + " header is truncated or invalid: " + e.getMessage());
        } finally {
            reader.dispose();
        }
    }

    /**
     * Refuses more than {@value #MAX_PIXELS} pixels, then a side over {@value #MAX_SIDE} px.
     *
     * @throws ImageRejectedException when either limit is exceeded
     */
    void checkDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw reject(ImageRejectedException.TRUNCATED, "The header declares " + width + " x " + height + " px.");
        }
        long pixels = (long) width * height;
        if (pixels > MAX_PIXELS) {
            throw reject(ImageRejectedException.TOO_MANY_PIXELS, String.format(Locale.ROOT,
                    "%d x %d px is %.1f megapixels, over the %d megapixel limit.", width, height, pixels / 1e6,
                    MAX_PIXELS / 1_000_000));
        }
        if (width > MAX_SIDE || height > MAX_SIDE) {
            throw reject(ImageRejectedException.SIDE_TOO_LONG, width + " x " + height
                    + " px has a side over the " + MAX_SIDE + " px limit.");
        }
    }

    /** The reader's raw type, else its first offered type, else null. */
    /**
     * Bit depth and color type of a PNG from its IHDR chunk (bytes 24 and 25), read before the JDK
     * reader sees the stream (it flushes what it has read). The reader's image type needs the metadata
     * after IHDR, which it reads by skipping chunks up to the first IDAT, so a header-only file failed
     * there (checkpoint 057). A short file is left to the reader, which reports it as truncated.
     */
    private static int[] pngIhdr(ImageInputStream iis) {
        try {
            iis.seek(24);
            int bitDepth = iis.read();
            int colorType = iis.read();
            iis.seek(0);
            return bitDepth < 0 || colorType < 0 ? null : new int[] {bitDepth, colorType};
        } catch (IOException e) {
            return null;
        }
    }

    private static ImageTypeSpecifier imageType(ImageReader reader) throws IOException {
        ImageTypeSpecifier raw = reader.getRawImageType(0);
        if (raw != null) {
            return raw;
        }
        Iterator<ImageTypeSpecifier> types = reader.getImageTypes(0);
        return types.hasNext() ? types.next() : null;
    }

    /**
     * Counts TIFF image file directories by walking the next-IFD offsets, stopping after
     * {@value #MAX_IFD_WALK} and on a loop, without reading any pixel data.
     */
    static int tiffDirectories(ImageInputStream iis) throws IOException {
        iis.seek(0);
        int b0 = iis.read();
        iis.setByteOrder(b0 == 0x4D ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        iis.seek(4);
        long offset = iis.readUnsignedInt();
        Set<Long> seen = new HashSet<>();
        int count = 0;
        while (offset != 0 && count < MAX_IFD_WALK && seen.add(offset)) {
            iis.seek(offset);
            int entries = iis.readUnsignedShort();
            count++;
            iis.seek(offset + 2 + 12L * entries);
            offset = iis.readUnsignedInt();
        }
        return count;
    }

    private byte[] readBounded(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[64 * 1024];
        long total = 0;
        try {
            int n;
            while ((n = in.read(buffer)) > 0) {
                total += n;
                if (total > maxFileBytes) {
                    throw reject(ImageRejectedException.FILE_TOO_LARGE, "The stream exceeds the " + maxFileBytes
                            + "-byte limit.");
                }
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw reject(ImageRejectedException.UNREADABLE, "The stream could not be read: " + e.getMessage());
        }
        return out.toByteArray();
    }

    private String tooLarge(long size) {
        return "The file is " + size + " bytes, over the " + maxFileBytes + "-byte limit.";
    }

    /** Counts and returns a refusal. */
    ImageRejectedException reject(String tag, String reason) {
        count(tag);
        return new ImageRejectedException(tag, reason);
    }

    private void count(String tag) {
        if (meters != null) {
            Counter.builder(REJECTED_METRIC).tag("reason", tag).register(meters).increment();
        }
    }

    private static int u(byte[] bytes, int index) {
        return bytes[index] & 0xff;
    }
}
