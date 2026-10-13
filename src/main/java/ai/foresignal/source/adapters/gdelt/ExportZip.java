package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Opens a GDELT export ZIP and streams its single entry into {@link GdeltExportParser} (prompt 066; pure).
 *
 * <p>The ZIP must hold exactly one entry, named {@code {ts}.export.CSV}, and no directory entries. The uncompressed
 * bytes are counted while they stream and refused past the cap, so a declared size in the ZIP header cannot be used
 * to smuggle a zip bomb past the check.
 */
public final class ExportZip {

    /** The default cap on the uncompressed entry (200 MB). */
    public static final long MAX_UNCOMPRESSED_BYTES = 200L * 1024 * 1024;

    private ExportZip() {}

    /**
     * Parses the export inside {@code zip}.
     *
     * @param zip the ZIP bytes, already checked against the declared size and MD5
     * @param entryName the one entry name allowed, {@code {ts}.export.CSV}
     * @param maxUncompressed the cap on the entry's uncompressed bytes
     * @param options the parser's filters
     * @param fipsToIso the FIPS→ISO crosswalk, or null
     * @return what the parser produced
     * @throws UpstreamFormatException when the ZIP is unreadable, holds another entry, a second entry or a directory,
     *     or the entry exceeds the cap
     */
    public static GdeltExportParser.Result parse(byte[] zip, String entryName, long maxUncompressed,
            GdeltExportParser.Options options, Function<String, String> fipsToIso) {
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                throw new UpstreamFormatException("The GDELT export ZIP has no entry", null);
            }
            check(entry, entryName);
            GdeltExportParser.Result result =
                    GdeltExportParser.parse(new CappedInputStream(in, maxUncompressed), options, fipsToIso);
            ZipEntry extra = in.getNextEntry();
            if (extra != null) {
                throw new UpstreamFormatException("The GDELT export ZIP holds more than one entry", extra.getName());
            }
            return result;
        } catch (UncheckedIOException e) {
            throw new UpstreamFormatException("The GDELT export ZIP could not be read: " + e.getCause().getMessage(),
                    null, e);
        } catch (IOException e) {
            throw new UpstreamFormatException("The GDELT export ZIP could not be read: " + e.getMessage(), null, e);
        }
    }

    private static void check(ZipEntry entry, String entryName) {
        if (entry.isDirectory()) {
            throw new UpstreamFormatException("The GDELT export ZIP holds a directory entry", entry.getName());
        }
        if (!entry.getName().equals(entryName)) {
            throw new UpstreamFormatException("The GDELT export ZIP holds an entry other than " + entryName,
                    entry.getName());
        }
    }

    /** Counts the bytes read and refuses to read past the cap; never closes the underlying stream. */
    static final class CappedInputStream extends FilterInputStream {

        private final long cap;
        private long count;

        CappedInputStream(InputStream in, long cap) {
            super(in);
            this.cap = cap;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                add(1);
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0) {
                add(n);
            }
            return n;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = super.skip(n);
            if (skipped > 0) {
                add(skipped);
            }
            return skipped;
        }

        @Override
        public void close() {
            // The ZIP stream is closed by its owner.
        }

        /** The bytes read so far. */
        long count() {
            return count;
        }

        private void add(long n) {
            count += n;
            if (count > cap) {
                throw new UpstreamFormatException("The GDELT export entry is larger than " + cap
                        + " bytes uncompressed; refused as a possible zip bomb", null);
            }
        }
    }
}
