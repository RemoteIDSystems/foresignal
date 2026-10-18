package ai.foresignal.exchange;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Writes and reads the signed intelligence package ZIP (prompt 132, {@value ExchangeVocabulary#MEDIA_TYPE}):
 * {@code manifest.json} first, the {@code content/} files, then {@code signature.jws}. Reading is defensive: at most
 * {@value ExchangeVocabulary#MAX_ENTRIES} entries, a cap on the uncompressed total, only {@code manifest.json},
 * {@code signature.jws} and normalized relative paths under {@code content/} (no {@code ..}, no absolute paths, no
 * backslashes, no duplicates), so a package can never write outside its folder or exhaust memory.
 */
public final class PackageCodec {

    private PackageCodec() {}

    /**
     * A package as read.
     *
     * @param manifestBytes the exact manifest bytes (what the signature covers)
     * @param signature the detached JWS
     * @param contents the content files by ZIP path ({@code content/…})
     */
    public record Contents(byte[] manifestBytes, String signature, Map<String, byte[]> contents) {}

    /** A malformed package. */
    public static final class FormatException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public FormatException(String message) {
            super(message);
        }
    }

    /** A package larger than the limit. */
    public static final class TooLargeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public TooLargeException(String message) {
            super(message);
        }
    }

    /**
     * Writes a package.
     *
     * @param out where to write the ZIP
     * @param manifestBytes the exact manifest bytes
     * @param signature the detached JWS over them
     * @param contents content files by ZIP path ({@code content/…}): {@code byte[]} or a {@link Path}
     */
    public static void write(OutputStream out, byte[] manifestBytes, String signature, Map<String, Object> contents) {
        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            put(zip, ExchangeVocabulary.MANIFEST, manifestBytes);
            for (Map.Entry<String, Object> e : new TreeMap<>(contents).entrySet()) {
                checkContentPath(e.getKey());
                zip.putNextEntry(new ZipEntry(e.getKey()));
                if (e.getValue() instanceof byte[] bytes) {
                    zip.write(bytes);
                } else if (e.getValue() instanceof Path path) {
                    Files.copy(path, zip);
                } else {
                    throw new IllegalArgumentException("content " + e.getKey() + " is neither bytes nor a file");
                }
                zip.closeEntry();
            }
            put(zip, ExchangeVocabulary.SIGNATURE, signature.getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** {@link #write(OutputStream, byte[], String, Map)} into memory. */
    public static byte[] write(byte[] manifestBytes, String signature, Map<String, Object> contents) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, manifestBytes, signature, contents);
        return out.toByteArray();
    }

    private static void put(ZipOutputStream zip, String name, byte[] bytes) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
    }

    /**
     * Reads a package.
     *
     * @param zip the ZIP bytes
     * @param maxBytes the cap on the uncompressed total
     * @return the manifest bytes, signature and content files
     * @throws FormatException when it is not a well-formed package
     * @throws TooLargeException above {@code maxBytes}
     */
    public static Contents read(byte[] zip, long maxBytes) {
        if (zip == null || zip.length == 0) {
            throw new FormatException("the package is empty");
        }
        if (zip.length > maxBytes) {
            throw new TooLargeException("the package is " + zip.length + " bytes; the limit is " + maxBytes);
        }
        byte[] manifest = null;
        String signature = null;
        Map<String, byte[]> contents = new LinkedHashMap<>();
        long total = 0;
        int entries = 0;
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (++entries > ExchangeVocabulary.MAX_ENTRIES) {
                    throw new FormatException("the package holds more than " + ExchangeVocabulary.MAX_ENTRIES
                            + " entries");
                }
                String name = entry.getName();
                if (entry.isDirectory()) {
                    if (!name.startsWith(ExchangeVocabulary.CONTENT)) {
                        throw new FormatException("unexpected folder " + name);
                    }
                    continue;
                }
                byte[] bytes = readLimited(in, maxBytes - total);
                total += bytes.length;
                if (ExchangeVocabulary.MANIFEST.equals(name)) {
                    if (manifest != null) {
                        throw new FormatException("the package holds two manifests");
                    }
                    manifest = bytes;
                } else if (ExchangeVocabulary.SIGNATURE.equals(name)) {
                    if (signature != null) {
                        throw new FormatException("the package holds two signatures");
                    }
                    signature = new String(bytes, StandardCharsets.US_ASCII).strip();
                } else {
                    checkContentPath(name);
                    if (contents.put(name, bytes) != null) {
                        throw new FormatException("the package holds " + name + " twice");
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new FormatException("the package is not a readable ZIP: " + e.getMessage());
        }
        if (manifest == null) {
            throw new FormatException("the package has no manifest.json");
        }
        if (signature == null || signature.isEmpty()) {
            throw new FormatException("the package has no signature.jws");
        }
        return new Contents(manifest, signature, contents);
    }

    private static byte[] readLimited(InputStream in, long remaining) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[64 * 1024];
        long read = 0;
        int n;
        while ((n = in.read(buffer)) > 0) {
            read += n;
            if (read > remaining) {
                throw new TooLargeException("the package's uncompressed content exceeds the limit");
            }
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * Checks a content path: under {@code content/}, relative, normalized, no backslash, no {@code ..} or empty
     * segment, at most 512 characters.
     *
     * @throws FormatException otherwise
     */
    public static void checkContentPath(String name) {
        if (name == null || !name.startsWith(ExchangeVocabulary.CONTENT) || name.length() > 512
                || name.length() == ExchangeVocabulary.CONTENT.length() || name.contains("\\")
                || name.contains("\u0000")) {
            throw new FormatException("unexpected entry " + name);
        }
        for (String segment : name.split("/", -1)) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                throw new FormatException("unexpected entry " + name);
            }
        }
    }

    /** The manifest key of a content ZIP path ({@code content/bundle.json} → {@code bundle.json}). */
    public static String key(String zipPath) {
        return zipPath.substring(ExchangeVocabulary.CONTENT.length());
    }

    /**
     * The {@code contentHashes} of content files: path under {@code content/} → SHA-256, sorted by path.
     *
     * @param contents content files by ZIP path: {@code byte[]} or a {@link Path}
     */
    public static Map<String, String> hashes(Map<String, ?> contents) {
        Map<String, String> out = new TreeMap<>();
        for (Map.Entry<String, ?> e : contents.entrySet()) {
            Object v = e.getValue();
            out.put(key(e.getKey()), v instanceof byte[] bytes ? sha256(bytes) : sha256((Path) v));
        }
        return out;
    }

    /**
     * The problems of a package's content against its manifest's {@code contentHashes}: a file whose digest differs,
     * a listed file that is missing, or a file that is not listed.
     *
     * @return the problems (empty when every hash matches)
     */
    public static List<String> verifyHashes(Map<?, ?> contentHashes, Map<String, byte[]> contents) {
        List<String> problems = new ArrayList<>();
        Map<String, String> actual = hashes(contents);
        if (contentHashes == null) {
            problems.add("the manifest has no contentHashes");
            return problems;
        }
        for (Map.Entry<?, ?> e : contentHashes.entrySet()) {
            String path = String.valueOf(e.getKey());
            String found = actual.get(path);
            if (found == null) {
                problems.add(path + " is listed but missing");
            } else if (!found.equalsIgnoreCase(String.valueOf(e.getValue()))) {
                problems.add(path + " does not match its hash");
            }
        }
        for (String path : actual.keySet()) {
            if (!contentHashes.containsKey(path)) {
                problems.add(path + " is not listed in the manifest");
            }
        }
        return problems;
    }

    /** The SHA-256 of {@code bytes}, lower-case hex. */
    public static String sha256(byte[] bytes) {
        return HexFormat.of().formatHex(digest().digest(bytes));
    }

    /** The SHA-256 of a file, lower-case hex. */
    public static String sha256(Path file) {
        MessageDigest digest = digest();
        try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
            in.transferTo(OutputStream.nullOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
