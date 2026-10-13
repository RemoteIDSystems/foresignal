package ai.foresignal.inbound;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.media.ImageGuard;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.ImageRejectedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes a remote pass bundle ({@code POST /v1/ingest/{sourceId}/rasters}, prompt 054,
 * Weather-Imagery-Design §4.7 b) where the SatDump inbox loop reads it.
 *
 * <p>Checks, before anything is written:
 *
 * <ul>
 *   <li>{@code meta} is {@code {passFolder, files[], product?}}: the pass folder and every file name are
 *       one safe path segment (no separator, no {@code ..}, no control character, at most
 *       {@value #MAX_NAME} characters); the names are unique and each has a part;
 *   <li>only {@code json}, {@code cbor}, {@code png}, {@code jpg}, {@code jpeg}, {@code tif} and
 *       {@code tiff} files, and {@code dataset.json} is one of them;
 *   <li>each file at most {@value #MAX_FILE_BYTES} bytes and the bundle at most
 *       {@value #MAX_BUNDLE_BYTES} ({@code 413 MEDIA_TOO_LARGE});
 *   <li>every image passes the {@link ImageGuard} header check (a BigTIFF is left to the pipeline, which
 *       can route it to GDAL).
 * </ul>
 *
 * <p>Layout: {@code dataset.json} goes to the pass folder; every other file goes to the product
 * directory, which is {@code product} when given, else the single product {@code dataset.json} lists,
 * else the pass folder itself. The files are staged under {@code rasters/tmp/} and the upload is
 * renamed into {@code rasters/satdump/{station}/incoming/{uploadId}/} in one atomic move, so the inbox
 * loop never sees half an upload (the inbox mount itself is read-only).
 */
@Component
public class RemoteBundles {

    /** The largest file in a bundle or a pushed image. */
    public static final long MAX_FILE_BYTES = 25L * 1024 * 1024;

    /** The largest bundle. */
    public static final long MAX_BUNDLE_BYTES = 200L * 1024 * 1024;

    /** The longest pass folder or file name. */
    public static final int MAX_NAME = 128;

    /** The most files in a bundle. */
    public static final int MAX_FILES = 64;

    /** The extensions accepted in a bundle. */
    public static final Set<String> EXTENSIONS = Set.of("json", "cbor", "png", "jpg", "jpeg", "tif", "tiff");

    /** The pass folder marker. */
    public static final String DATASET = "dataset.json";

    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}]");

    /**
     * One file part.
     *
     * @param name the submitted file name
     * @param size its size
     * @param content opens its content
     */
    public record Part(String name, long size, Content content) {}

    /** Opens a part's content. */
    @FunctionalInterface
    public interface Content {

        /** Opens the content. */
        InputStream open() throws IOException;
    }

    /**
     * The parsed {@code meta} part.
     *
     * @param passFolder the pass folder name
     * @param files the file names
     * @param product the product directory, or null
     */
    public record Meta(String passFolder, List<String> files, String product) {}

    /**
     * An accepted bundle.
     *
     * @param uploadId the upload id
     * @param passFolder the pass folder
     */
    public record Accepted(String uploadId, String passFolder) {}

    private final RasterPaths paths;
    private final ImageGuard guard;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the writer. */
    public RemoteBundles(RasterPaths paths, ImageGuard guard, JsonMapper json, Clock clock) {
        this.paths = paths;
        this.guard = guard;
        this.json = json;
        this.clock = clock;
    }

    /**
     * Parses and checks the {@code meta} part.
     *
     * @throws ValidationException {@code 400 VALIDATION_FAILED} for anything unsafe or missing
     */
    public Meta meta(byte[] bytes) {
        JsonNode root;
        try {
            root = json.readTree(bytes);
        } catch (JacksonException e) {
            throw ValidationException.forField("meta", "must be JSON {passFolder, files[]}");
        }
        if (root == null || !root.isObject()) {
            throw ValidationException.forField("meta", "must be a JSON object {passFolder, files[]}");
        }
        String folder = root.path("passFolder").isString() ? root.path("passFolder").asString() : null;
        requireSafe("meta.passFolder", folder);
        if (!root.path("files").isArray() || root.path("files").isEmpty()) {
            throw ValidationException.forField("meta.files", "must list at least one file name");
        }
        List<String> files = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (JsonNode f : root.path("files")) {
            String name = f.isString() ? f.asString() : null;
            requireSafe("meta.files", name);
            if (!EXTENSIONS.contains(extension(name))) {
                throw ValidationException.forField("meta.files", "'" + name + "' is not a json, cbor, png, jpg or tif file");
            }
            if (!seen.add(name)) {
                throw ValidationException.forField("meta.files", "'" + name + "' is listed twice");
            }
            files.add(name);
        }
        if (files.size() > MAX_FILES) {
            throw ValidationException.forField("meta.files", "at most " + MAX_FILES + " files");
        }
        if (!files.contains(DATASET)) {
            throw ValidationException.forField("meta.files", "must include dataset.json");
        }
        String product = null;
        if (root.has("product") && !root.path("product").isNull()) {
            product = root.path("product").isString() ? root.path("product").asString() : null;
            requireSafe("meta.product", product);
        }
        return new Meta(folder, List.copyOf(files), product);
    }

    /**
     * Checks the parts against {@code meta} and writes the upload.
     *
     * @param station the station name (the source id without {@code satdump-})
     * @throws ValidationException {@code 400} for a missing, extra or unsafe part or a refused image
     * @throws ForesignalException {@code 413 MEDIA_TOO_LARGE} for a file over 25 MB or a bundle over 200 MB
     */
    public Accepted write(String station, Meta meta, List<Part> parts) {
        Map<String, Part> byName = new LinkedHashMap<>();
        long total = 0;
        for (Part part : parts) {
            requireSafe("files", part.name());
            if (!meta.files().contains(part.name())) {
                throw ValidationException.forField("files", "the part '" + part.name() + "' is not listed in meta.files");
            }
            if (byName.put(part.name(), part) != null) {
                throw ValidationException.forField("files", "the part '" + part.name() + "' is sent twice");
            }
            if (part.size() > MAX_FILE_BYTES) {
                throw tooLarge("The file '" + part.name() + "' is " + part.size() + " bytes, over the " + MAX_FILE_BYTES
                        + "-byte limit.");
            }
            total += part.size();
            if (total > MAX_BUNDLE_BYTES) {
                throw tooLarge("The bundle is over the " + MAX_BUNDLE_BYTES + "-byte limit.");
            }
        }
        for (String name : meta.files()) {
            if (!byName.containsKey(name)) {
                throw ValidationException.forField("files", "meta.files lists '" + name + "' but no part carries it");
            }
        }
        byte[] dataset = read(byName.get(DATASET), MAX_FILE_BYTES);
        String productDir = meta.product() != null ? meta.product() : singleProduct(dataset);
        for (Part part : byName.values()) {
            String ext = extension(part.name());
            if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("tif") || ext.equals("tiff")) {
                guard(part);
            }
        }
        String uploadId = Ulids.next(clock.instant()).toLowerCase(Locale.ROOT);
        Path staging = paths.tmp().resolve("upload-" + uploadId);
        Path incoming = paths.root().resolve("satdump").resolve(station).resolve("incoming").toAbsolutePath().normalize();
        Path target = incoming.resolve(uploadId);
        try {
            Path folder = staging.resolve(meta.passFolder());
            Files.createDirectories(folder);
            for (Part part : byName.values()) {
                Path dir = part.name().equals(DATASET) || productDir == null ? folder : folder.resolve(productDir);
                Files.createDirectories(dir);
                Path file = dir.resolve(part.name()).normalize();
                if (!file.startsWith(staging)) {
                    throw ValidationException.forField("files", "'" + part.name() + "' leaves the upload");
                }
                try (InputStream in = part.content().open()) {
                    long copied = Files.copy(in, file);
                    if (copied > MAX_FILE_BYTES) {
                        throw tooLarge("The file '" + part.name() + "' is over the " + MAX_FILE_BYTES + "-byte limit.");
                    }
                }
            }
            Files.createDirectories(incoming);
            Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            deleteQuietly(staging);
            throw new UncheckedIOException("Could not write the upload: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            deleteQuietly(staging);
            throw e;
        }
        return new Accepted(uploadId, meta.passFolder());
    }

    /** The single product directory {@code dataset.json} lists, when it lists exactly one safe one; else null. */
    String singleProduct(byte[] dataset) {
        try {
            JsonNode root = json.readTree(dataset);
            JsonNode products = root == null ? null : root.path("products");
            if (products == null || !products.isArray() || products.size() != 1 || !products.get(0).isString()) {
                return null;
            }
            String dir = products.get(0).asString();
            return safe(dir) && !dir.equals(".") ? dir : null;
        } catch (JacksonException e) {
            throw ValidationException.forField("files", "dataset.json is not JSON");
        }
    }

    private void guard(Part part) {
        byte[] bytes = read(part, MAX_FILE_BYTES);
        try {
            guard.inspect(bytes, null, true);
        } catch (ImageRejectedException e) {
            if (ImageRejectedException.BIGTIFF.equals(e.tag())) {
                return; // the pipeline refuses it with a health reason, or routes it to GDAL
            }
            throw new ValidationException("The image '" + part.name() + "' was rejected: " + e.reason(),
                    List.of(new ai.foresignal.api.error.FieldError("files", e.reason())));
        }
    }

    private static byte[] read(Part part, long max) {
        try (InputStream in = part.content().open()) {
            byte[] bytes = in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8, max + 1));
            if (bytes.length > max) {
                throw tooLarge("The file '" + part.name() + "' is over the " + max + "-byte limit.");
            }
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the part '" + part.name() + "': " + e.getMessage(), e);
        }
    }

    /** True when {@code name} is one safe path segment. */
    public static boolean safe(String name) {
        return name != null && !name.isBlank() && name.length() <= MAX_NAME && !name.contains("/") && !name.contains("\\")
                && !name.equals(".") && !name.equals("..") && !name.contains("..") && !CONTROL.matcher(name).find()
                && !name.startsWith(".") && name.strip().equals(name);
    }

    private static void requireSafe(String field, String name) {
        if (!safe(name)) {
            throw ValidationException.forField(field, "must be one file or folder name: no separators, no '..', no"
                    + " leading dot or control characters, at most " + MAX_NAME + " characters");
        }
    }

    /** The lower-case extension of a file name. */
    static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** {@code 413 MEDIA_TOO_LARGE}. */
    public static ForesignalException tooLarge(String detail) {
        return new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.MEDIA_TOO_LARGE, detail);
    }

    private static void deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                    Files.delete(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // Left for the raster orphan sweep of rasters/tmp.
        }
    }
}
