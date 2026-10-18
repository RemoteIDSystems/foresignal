package ai.foresignal.exchange;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Where package ZIPs live (prompt 132): {@code {data-dir}/exchange/sent/{id}.zip} for packages built here and
 * {@code {data-dir}/exchange/received/{id}.zip} for imported ones. Paths are built only from ULIDs, never from package
 * content.
 */
@Component
public class ExchangeFiles {

    private final Path root;

    /** Creates the store under {@code foresignal.data-dir}. */
    @Autowired
    public ExchangeFiles(ForesignalProperties properties) {
        this(properties.dataDir());
    }

    /** Creates the store under {@code dataDir} (tests). */
    public ExchangeFiles(Path dataDir) {
        this.root = dataDir.toAbsolutePath().normalize().resolve("exchange");
        try {
            Files.createDirectories(root.resolve("sent"));
            Files.createDirectories(root.resolve("received"));
            Files.createDirectories(root.resolve("tmp"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the exchange directory " + root, e);
        }
    }

    /** The relative path of a sent package. */
    public static String sent(String id) {
        return "sent/" + safe(id) + ".zip";
    }

    /** The relative path of a received package. */
    public static String received(String id) {
        return "received/" + safe(id) + ".zip";
    }

    private static String safe(String id) {
        if (id == null || !id.matches("[0-9A-Z]{26}")) {
            throw new IllegalArgumentException("not a ULID: " + id);
        }
        return id;
    }

    /** The absolute path of a stored file. */
    public Path path(String relative) {
        Path p = root.resolve(relative).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("outside the exchange directory: " + relative);
        }
        return p;
    }

    /** A new temporary file in the exchange directory. */
    public Path temp(String suffix) {
        try {
            return Files.createTempFile(root.resolve("tmp"), "pkg-", suffix);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Moves a temporary file into place. */
    public void move(Path temp, String relative) {
        try {
            Files.move(temp, path(relative), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Writes bytes to a stored file. */
    public void write(String relative, byte[] bytes) {
        try {
            Files.write(path(relative), bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Reads a stored file. */
    public byte[] read(String relative) {
        try {
            return Files.readAllBytes(path(relative));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Deletes a file quietly. */
    public void delete(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // best effort
        }
    }
}
