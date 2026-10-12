package ai.foresignal.collect;

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
 * The acceptance criterion of prompt 005, as a test: nothing outside {@code ai.foresignal.collect} may
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
    private static final Path COLLECT = MAIN.resolve(Path.of("ai", "foresignal", "collect"));

    /**
     * Outbound clients that are not upstream requests, each sanctioned by name. Invariant SRC-1
     * covers <em>upstream</em> requests (source fetches). Notification deliveries are outbound
     * traffic to operator-configured receivers: prompt 018 specifies their own dedicated
     * {@code HttpClient} (no redirects, 5 s connect, 15 s request), and Architecture §17 says
     * outbound traffic is not counted against source budgets. Prompt 035 adds the outbound URL policy
     * to this sender. Prompt 022 narrowed the guard from "nothing outside collect" to this list.
     * LLM provider calls (prompt 026) are not upstream fetches either: they are budgeted by
     * {@code LlmBudgetGovernor}, and every provider client is built by the one factory
     * {@code LlmHttp}, where prompt 035 enforces the outbound URL policy. Web Push (prompt 031) sends
     * to push services through {@code PushHttp}, pinned to the address its push-endpoint policy
     * checked (prompt 035 swaps that check for {@code OutboundUrlPolicy} {@code PUSH_ENDPOINT}).
     */
    private static final List<Path> SANCTIONED_OUTBOUND =
            List.of(MAIN.resolve(Path.of("ai", "foresignal", "notify", "ChannelSender.java")),
                    MAIN.resolve(Path.of("ai", "foresignal", "llm", "LlmHttp.java")),
                    MAIN.resolve(Path.of("ai", "foresignal", "notify", "push", "PushHttp.java")));

    /** The two ways to get an {@code HttpClient}. */
    private static final List<String> CONSTRUCTIONS =
            List.of("HttpClient.newHttpClient", "HttpClient.newBuilder");

    @Test
    @DisplayName("only the collect package (and the sanctioned notification sender) constructs an HttpClient")
    void onlyCollectBuildsAnHttpClient() throws IOException {
        Map<String, List<String>> offenders = new LinkedHashMap<>();

        for (Path file : javaSources()) {
            String source = Files.readString(file);
            List<String> found = CONSTRUCTIONS.stream().filter(source::contains).toList();
            if (!found.isEmpty() && !file.startsWith(COLLECT) && !SANCTIONED_OUTBOUND.contains(file)) {
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
        for (Path sanctioned : SANCTIONED_OUTBOUND) {
            assertThat(sanctioned).as("a sanctioned outbound sender that no longer exists must leave the list").exists();
        }
    }

    private static List<Path> javaSources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }
}
