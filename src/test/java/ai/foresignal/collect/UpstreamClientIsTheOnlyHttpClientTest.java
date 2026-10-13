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
     * Outbound clients that are not upstream requests, each sanctioned by name with its reason.
     * Invariant SRC-1 covers <em>upstream</em> requests (source fetches); Architecture §17 says other
     * outbound traffic is not counted against source budgets. Prompt 022 narrowed the guard from
     * "nothing outside collect" to an explicit list, and checkpoint 041 made the list exact: every
     * entry must still construct an {@code HttpClient} (a stale entry fails), and any other file that
     * constructs one fails. Channel sends ({@code notify/ChannelSender}, 018) and LLM provider calls
     * ({@code llm/LlmHttp}, 026) no longer construct their own client: prompt 035 moved both onto
     * {@code net/PinnedHttp}. Whether these senders should share one client is an owner decision
     * (041 report).
     */
    private static final Map<Path, String> SANCTIONED_OUTBOUND = Map.of(
            // 035: the one pinned client for operator-supplied URLs (channels, LLM providers), connected to
            // the address OutboundUrlPolicy checked.
            MAIN.resolve(Path.of("ai", "foresignal", "net", "PinnedHttp.java")),
            "operator-supplied URLs (channels, LLM providers), pinned by OutboundUrlPolicy (035)",
            // 024: EDP provider pulls, pinned to the address EdpUrlGuard vetted (HTTPS with SNI and its own
            // RFC 6125 name check, which UpstreamClient cannot do); still charged to BudgetGovernor.
            MAIN.resolve(Path.of("ai", "foresignal", "inbound", "edp", "EdpHttpClient.java")),
            "EDP provider URLs pinned to the EdpUrlGuard address, budgeted through BudgetGovernor (024)",
            // 031: Web Push to push services, pinned to the address its PUSH_ENDPOINT policy checked.
            MAIN.resolve(Path.of("ai", "foresignal", "notify", "push", "PushHttp.java")),
            "Web Push deliveries to push services, pinned by the PUSH_ENDPOINT policy (031, 035)",
            // 032: APNs (HTTP/2, one connection per provider), FCM and Google's OAuth token endpoint.
            MAIN.resolve(Path.of("ai", "foresignal", "notify", "push", "NativePushHttp.java")),
            "native push to APNs, FCM and the OAuth token endpoint (032)");

    /**
     * The two ways to get an {@code HttpClient}, and the Java-WebSocket client package, which only
     * {@code collect/DeflateWebSocket} may use (063: the permessage-deflate path of {@code openWebSocket}).
     */
    private static final List<String> CONSTRUCTIONS =
            List.of("HttpClient.newHttpClient", "HttpClient.newBuilder", "org.java_websocket.client");

    @Test
    @DisplayName("only the collect package (and the sanctioned outbound senders) constructs an HttpClient")
    void onlyCollectBuildsAnHttpClient() throws IOException {
        Map<String, List<String>> offenders = new LinkedHashMap<>();

        for (Path file : javaSources()) {
            String source = Files.readString(file);
            List<String> found = CONSTRUCTIONS.stream().filter(source::contains).toList();
            if (!found.isEmpty() && !file.startsWith(COLLECT) && !SANCTIONED_OUTBOUND.containsKey(file)) {
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
        for (Map.Entry<Path, String> sanctioned : SANCTIONED_OUTBOUND.entrySet()) {
            Path file = sanctioned.getKey();
            assertThat(file).as("a sanctioned outbound sender that no longer exists must leave the list").exists();
            String source = Files.readString(file);
            assertThat(CONSTRUCTIONS.stream().anyMatch(source::contains))
                    .as("%s (%s) no longer builds an HttpClient, so it must leave the list", file,
                            sanctioned.getValue())
                    .isTrue();
        }
    }

    /** The raw socket constructions only the 069 socket transport may make (adapter rule 2, SRC-1). */
    private static final List<String> SOCKETS = List.of("new Socket(", "DatagramChannel.open", "SocketChannel.open",
            "new DatagramSocket(", "new ServerSocket(", "ServerSocketChannel.open");

    @Test
    @DisplayName("only collect/socket/DefaultSocketTransport opens a socket, and nothing opens a server socket (069)")
    void onlyTheSocketTransportOpensSockets() throws IOException {
        Path transport = COLLECT.resolve(Path.of("socket", "DefaultSocketTransport.java"));
        Map<String, List<String>> offenders = new LinkedHashMap<>();
        for (Path file : javaSources()) {
            String source = Files.readString(file);
            List<String> found = SOCKETS.stream().filter(source::contains).toList();
            boolean server = found.stream().anyMatch(c -> c.contains("ServerSocket"));
            if (!found.isEmpty() && (!file.equals(transport) || server)) {
                offenders.put(file.toString(), found);
            }
        }
        assertThat(offenders)
                .as("Sockets are opened only by the budgeted, LAN-only socket transport (StreamContext.sockets());"
                        + " inbound TCP serving is prompt 110's SERVE sinks")
                .isEmpty();
        assertThat(Files.readString(transport)).contains("new Socket(", "DatagramChannel.open");
    }

    private static List<Path> javaSources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }
}
