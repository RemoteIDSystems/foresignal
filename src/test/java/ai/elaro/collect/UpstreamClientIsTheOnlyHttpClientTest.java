package ai.elaro.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The acceptance criterion of prompt 005, as a test: nothing outside {@code ai.elaro.collect} may
 * build an {@link java.net.http.HttpClient}.
 *
 * <p>Every guarantee in this prompt — the budget, the backoff, the User-Agent, the byte cap, the
 * metering, the redaction — holds because {@link UpstreamClient} is the only way out to the
 * network. That is an architectural property, and an architectural property that nothing checks is
 * a comment. A future adapter author reaching for {@code HttpClient.newHttpClient()} fails here
 * rather than silently bypassing the per-source spend guarantee (Product Charter G2).
 *
 * <p>Scanning source text rather than bytecode is deliberate: it names the offending file and the
 * offending call, which is what the author needs to see.
 */
class UpstreamClientIsTheOnlyHttpClientTest {

    private static final Path MAIN = Path.of("src", "main", "java");
    private static final Path COLLECT = MAIN.resolve(Path.of("ai", "elaro", "collect"));

    /** The two ways to get an {@code HttpClient}. */
    private static final List<String> CONSTRUCTIONS =
            List.of("HttpClient.newHttpClient", "HttpClient.newBuilder");

    @Test
    @DisplayName("only the collect package constructs an HttpClient")
    void onlyCollectBuildsAnHttpClient() throws IOException {
        Map<String, List<String>> offenders = new LinkedHashMap<>();

        for (Path file : javaSources()) {
            String source = Files.readString(file);
            List<String> found = CONSTRUCTIONS.stream().filter(source::contains).toList();
            if (!found.isEmpty() && !file.startsWith(COLLECT)) {
                offenders.put(file.toString(), found);
            }
        }

        assertThat(offenders)
                .as(
                        "These files build their own HttpClient and so escape the budget, the backoff,"
                                + " the metering and the redaction. Take a SourceHttp from"
                                + " UpstreamClient.forSource instead.")
                .isEmpty();
    }

    @Test
    @DisplayName("the scan is looking in the right place, so it cannot pass vacuously")
    void theScanActuallyFindsTheOneAllowedConstruction() throws IOException {
        assertThat(Files.isDirectory(MAIN))
                .as("the scan must run from the repository root, where %s exists", MAIN)
                .isTrue();

        String upstreamClient = Files.readString(COLLECT.resolve("UpstreamClient.java"));

        assertThat(upstreamClient).contains("HttpClient.newBuilder");
    }

    private static List<Path> javaSources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }
}
