package ai.foresignal.raster.decode;

import ai.foresignal.raster.RasterFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;

/**
 * Gzip inputs ({@code .grib2.gz}, prompt 046): inflated by streaming into a sibling temporary file,
 * so a 7000 × 3500 MRMS field is never held compressed and inflated in memory at once.
 */
public final class GzipInputs {

    /** Inflated output is capped, so a hostile gzip bomb cannot fill the disk: 512 MB. */
    public static final long MAX_INFLATED_BYTES = 512L * 1024 * 1024;

    private GzipInputs() {}

    /** True when {@code file} starts with the gzip magic bytes {@code 1f 8b}. */
    public static boolean isGzip(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            return in.read() == 0x1f && in.read() == 0x8b;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Inflates {@code file} into a new sibling file (same directory, so the same root checks apply)
     * and returns it; the caller deletes it.
     *
     * @throws RasterFormatException when the input is not valid gzip or inflates past
     *     {@link #MAX_INFLATED_BYTES}
     */
    public static Path inflateSibling(Path file) {
        String name = file.getFileName().toString();
        String stem = name.endsWith(".gz") ? name.substring(0, name.length() - 3) : name + ".inflated";
        Path target;
        try {
            target = Files.createTempFile(file.toAbsolutePath().getParent(), stem + ".", ".tmp");
        } catch (IOException e) {
            throw new RasterFormatException("Cannot create a file to inflate " + name + " into: " + e.getMessage(), e);
        }
        try (InputStream in = new GZIPInputStream(Files.newInputStream(file), 64 * 1024);
                OutputStream out = Files.newOutputStream(target, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[64 * 1024];
            long total = 0;
            int read;
            while ((read = in.read(buffer)) >= 0) {
                total += read;
                if (total > MAX_INFLATED_BYTES) {
                    throw new RasterFormatException(name + " inflates past " + MAX_INFLATED_BYTES + " bytes");
                }
                out.write(buffer, 0, read);
            }
            return target;
        } catch (IOException | RuntimeException e) {
            deleteQuietly(target);
            if (e instanceof RasterFormatException rfe) {
                throw rfe;
            }
            throw new RasterFormatException("Cannot inflate " + name + ": " + e.getMessage(), e);
        }
    }

    /** Deletes {@code file} if it exists, ignoring failures. */
    public static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Best effort: a leftover file in rasters/tmp is harmless and nothing else can be done here.
        }
    }
}
