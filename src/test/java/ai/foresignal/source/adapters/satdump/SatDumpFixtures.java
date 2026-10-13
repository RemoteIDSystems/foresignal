package ai.foresignal.source.adapters.satdump;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * The SatDump fixtures of prompt 049, under {@code src/test/resources/fixtures/satdump/{version}/}, one
 * set per pinned SatDump version (README there: what is captured and what is hand-written).
 */
final class SatDumpFixtures {

    /** The pinned version the fixtures stand for. */
    static final String VERSION = "2.0.0-alpha";

    /** The Meteor LRPT pass folder, relative to the version directory. */
    static final String PASS = "pass/2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz";

    /** The GOES-19 HRIT timestamp folder, relative to the version directory. */
    static final String XRIT_FOLDER = "xrit/IMAGES/GOES-19/Full Disk/2026-10-12_18-00-20";

    private SatDumpFixtures() {}

    /** The version directory on the test classpath. */
    static Path root() {
        URL url = SatDumpFixtures.class.getResource("/fixtures/satdump/" + VERSION);
        if (url == null) {
            throw new IllegalStateException("The SatDump fixtures are missing from the test classpath");
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** A fixture file's text. */
    static String text(String relative) {
        return new String(bytes(relative), StandardCharsets.UTF_8);
    }

    /** A fixture file's bytes. */
    static byte[] bytes(String relative) {
        try {
            return Files.readAllBytes(root().resolve(relative));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Copies the fixture tree {@code relative} into {@code target}, which is created. */
    static void copy(String relative, Path target) {
        Path from = root().resolve(relative);
        try (Stream<Path> walk = Files.walk(from)) {
            List<Path> paths = walk.toList();
            for (Path p : paths) {
                Path to = target.resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(to);
                } else {
                    Files.createDirectories(to.getParent());
                    Files.copy(p, to);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
