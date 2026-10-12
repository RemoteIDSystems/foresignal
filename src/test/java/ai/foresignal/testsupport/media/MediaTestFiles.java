package ai.foresignal.testsupport.media;

import ai.foresignal.media.MediaPaths;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * The media directory in tests (prompt 043): the cached contexts share one data directory, so each
 * media test clears it first.
 */
public final class MediaTestFiles {

    private MediaTestFiles() {}

    /** Deletes every file and directory under {@code media/}, keeping {@code media/tmp/} itself. */
    public static void clear(MediaPaths paths) {
        try (Stream<Path> walk = Files.walk(paths.root())) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                if (!p.equals(paths.root()) && !p.equals(paths.tmp())) {
                    Files.deleteIfExists(p);
                }
            }
            Files.createDirectories(paths.tmp());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The content files under {@code media/{ab}/}, sorted. */
    public static List<Path> contentFiles(MediaPaths paths) {
        try (Stream<Path> walk = Files.walk(paths.root())) {
            return walk.filter(Files::isRegularFile).filter(p -> !p.startsWith(paths.tmp())).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The files in {@code media/tmp/}. */
    public static List<Path> staged(MediaPaths paths) {
        try (Stream<Path> list = Files.list(paths.tmp())) {
            return list.toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
