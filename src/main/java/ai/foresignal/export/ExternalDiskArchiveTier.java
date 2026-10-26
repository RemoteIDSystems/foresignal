package ai.foresignal.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * The external-disk archive tier (prompt R2-016, target {@code local}): segment files under the archive
 * {@code root}, which is meant to be a mounted external disk. It needs no cloud account.
 *
 * <p>A file is copied under a temporary name, fsynced, renamed into place, and its SHA-256 is read back before
 * {@link #offload} returns; {@link #verify} and {@link #restore} read it again. The files are gzip-compressed by the
 * archive runner before they get here.
 */
@Component
public class ExternalDiskArchiveTier implements ArchiveTier {

    private final ArchiveService archive;

    /** Creates the tier. */
    public ExternalDiskArchiveTier(ArchiveService archive) {
        this.archive = archive;
    }

    @Override
    public String target() {
        return ArchiveSettings.LOCAL;
    }

    @Override
    public ArchiveRef offload(ArchiveSettings settings, ArchiveSpec spec) {
        Path target = resolve(settings, spec.key());
        try {
            if (!samePath(spec.file(), target)) {
                Files.createDirectories(target.getParent());
                Path tmp = target.resolveSibling("." + target.getFileName() + "." + System.nanoTime() + ".tmp");
                try {
                    Files.copy(spec.file(), tmp, StandardCopyOption.REPLACE_EXISTING);
                    try (FileChannel channel = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
                        channel.force(true);
                    }
                    move(tmp, target);
                } finally {
                    Files.deleteIfExists(tmp);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + spec.key() + " to the archive root", e);
        }
        ArchiveRef ref = new ArchiveRef(target(), spec.key(), target.toString(), spec.sha256(), spec.bytes(), null);
        if (!verify(settings, ref)) {
            throw new IllegalStateException("The archive copy " + target + " does not match its SHA-256");
        }
        return ref;
    }

    @Override
    public boolean verify(ArchiveSettings settings, ArchiveRef ref) {
        Path file = resolve(settings, ref.key());
        try {
            return Files.isRegularFile(file) && Files.size(file) == ref.bytes()
                    && ref.sha256() != null && ref.sha256().equalsIgnoreCase(ExportManifests.sha256(file));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Restored restore(ArchiveSettings settings, ArchiveRef ref, Path into) {
        Path file = resolve(settings, ref.key());
        if (!verify(settings, ref)) {
            throw new IllegalStateException("The archive copy " + file + " is missing or does not match its SHA-256");
        }
        try {
            Files.createDirectories(into.toAbsolutePath().getParent());
            Path tmp = into.resolveSibling(into.getFileName() + ".part");
            Files.copy(file, tmp, StandardCopyOption.REPLACE_EXISTING);
            move(tmp, into);
            if (!ref.sha256().equalsIgnoreCase(ExportManifests.sha256(into))) {
                Files.deleteIfExists(into);
                throw new IllegalStateException("The restored copy of " + ref.key() + " does not match its SHA-256");
            }
            return Restored.ready(into);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not restore " + ref.key(), e);
        }
    }

    @Override
    public List<ArchiveRef> list(ArchiveSettings settings, String keyPrefix) {
        Path root = archive.root(settings);
        Path start = resolve(settings, keyPrefix);
        Path dir = Files.isDirectory(start) ? start : start.getParent();
        List<ArchiveRef> out = new ArrayList<>();
        if (dir == null || !Files.isDirectory(dir)) {
            return out;
        }
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                String key = root.relativize(file).toString().replace('\\', '/');
                if (key.startsWith(keyPrefix) && !file.getFileName().toString().startsWith(".")) {
                    out.add(new ArchiveRef(target(), key, file.toString(), null, Files.size(file), null));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list " + dir, e);
        }
        return out;
    }

    @Override
    public void delete(ArchiveSettings settings, ArchiveRef ref) {
        try {
            Files.deleteIfExists(resolve(settings, ref.key()));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete " + ref.key(), e);
        }
    }

    /** {@code key} under the archive root, refusing a key that escapes it. */
    Path resolve(ArchiveSettings settings, String key) {
        Path root = archive.root(settings);
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("The archive key escapes the archive root: " + key);
        }
        return resolved;
    }

    private static boolean samePath(Path a, Path b) {
        return a.toAbsolutePath().normalize().equals(b.toAbsolutePath().normalize());
    }

    private static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
