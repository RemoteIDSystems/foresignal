package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Keeps the metrics table in {@code docs/Metrics.md} in step with the meters the broker registers (prompt
 * 143), so the drift that {@code scripts/verify/metric-drift.sh} finds against a live scrape is also caught
 * by the suite.
 *
 * <p>Two directions are checked. Every {@code foresignal.*} meter registered once the application context
 * has started must have a row in the table. And every row must name a meter that still appears as a string
 * literal under {@code src/main/java}, so a renamed or deleted meter cannot leave a stale row behind.
 * Meters registered lazily (on their first event) are covered by the second check only, since a fresh
 * context has not seen their events.
 *
 * <p>It is an integration test because only a started context knows which meters exist at startup. It
 * uses the shared {@link IntegrationTest} context, so it adds no context to the cache.
 */
@IntegrationTest
class MetricNamesDocumentedTest {

    /** The document holding the metrics table. */
    static final Path METRICS_DOC = Path.of("docs/Metrics.md");

    /** The main sources searched for each documented meter name. */
    static final Path MAIN_SOURCES = Path.of("src/main/java");

    /** A table row whose first cell is a back-quoted {@code foresignal.} meter name. */
    private static final Pattern ROW = Pattern.compile("^\\|\\s*`(foresignal\\.[a-z0-9_.-]+)`\\s*\\|", Pattern.MULTILINE);

    @Autowired MeterRegistry meters;

    @Test
    @DisplayName("every foresignal.* meter registered at startup has a row in docs/Metrics.md")
    void registeredMetersAreDocumented() {
        Set<String> documented = documentedNames(read(METRICS_DOC));
        Set<String> registered = new TreeSet<>();
        for (Meter meter : meters.getMeters()) {
            String name = meter.getId().getName();
            if (name.startsWith("foresignal.")) {
                registered.add(name);
            }
        }

        assertThat(documented).as("rows in %s", METRICS_DOC).isNotEmpty();
        assertThat(registered)
                .as("foresignal.* meters registered at startup but missing from the %s table", METRICS_DOC)
                .isSubsetOf(documented);
    }

    @Test
    @DisplayName("every row in docs/Metrics.md names a meter that still exists in the main sources")
    void documentedMetersExistInSource() {
        Set<String> documented = documentedNames(read(METRICS_DOC));
        String sources = allMainSources();

        Set<String> stale = new TreeSet<>();
        for (String name : documented) {
            if (!sources.contains('"' + name + '"')) {
                stale.add(name);
            }
        }
        assertThat(stale).as("rows in %s naming no meter in %s", METRICS_DOC, MAIN_SOURCES).isEmpty();
    }

    /**
     * The meter names in the first column of the metrics table.
     *
     * @param markdown the document text
     * @return the names, sorted
     */
    static Set<String> documentedNames(String markdown) {
        Set<String> names = new TreeSet<>();
        Matcher m = ROW.matcher(markdown);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static String allMainSources() {
        StringBuilder all = new StringBuilder();
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(p -> all.append(read(p)).append('\n'));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return all.toString();
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
