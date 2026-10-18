package ai.foresignal.cli.commands;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.ApiException;
import ai.foresignal.cli.http.NetworkException;
import ai.foresignal.cli.output.OutputFormat;
import ai.foresignal.cli.output.Printer;
import ai.foresignal.cli.sse.SseEvent;
import ai.foresignal.cli.sse.SseReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal ask "…"}: {@code POST /v1/ask/stream} ({@code askStream}). In {@code table} output the answer
 * text streams to standard output as {@code delta} events arrive, tool progress and warnings go to standard error,
 * and the verified answer's citations follow the final {@code done}. In {@code json} the final {@code AskResponse}
 * is printed; in {@code ndjson} every event is a line. {@code --no-stream} calls {@code POST /v1/ask} instead.
 */
@Command(name = "ask", description = "Ask a question about the data (streams the answer).")
public final class AskCommand implements Callable<Integer> {

    @ParentCommand
    ForesignalCli root;

    @Parameters(paramLabel = "QUESTION", arity = "1..*", description = "The question")
    List<String> question;

    @Option(names = "--thread", description = "Continue an ask thread")
    String threadId;

    @Option(names = "--source", split = ",", description = "Limit to these sources")
    List<String> sources;

    @Option(names = "--kind", split = ",", description = "Limit to these kinds")
    List<String> kinds;

    @Option(names = "--bbox", description = "Limit to minLon,minLat,maxLon,maxLat")
    String bbox;

    @Option(names = "--since", description = "Limit to data since this ISO-8601 instant")
    String since;

    @Option(names = "--allow-actions", description = "Let the answer propose actions")
    boolean allowActions;

    @Option(names = "--no-stream", description = "Wait for the whole answer (POST /v1/ask)")
    boolean noStream;

    /** Creates the command (picocli). */
    public AskCommand() {
    }

    /**
     * Asks the question.
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        ObjectNode body = root.json().createObjectNode();
        body.put("question", String.join(" ", question));
        if (threadId != null) {
            body.put("threadId", threadId);
        }
        if (allowActions) {
            body.put("allowActions", true);
        }
        if (sources != null || kinds != null || bbox != null || since != null) {
            ObjectNode scope = body.putObject("scope");
            if (sources != null) {
                sources.forEach(scope.putArray("sources")::add);
            }
            if (kinds != null) {
                kinds.forEach(scope.putArray("kinds")::add);
            }
            if (bbox != null) {
                scope.put("bbox", bbox);
            }
            if (since != null) {
                scope.put("since", since);
            }
        }
        ApiClient api = root.api();
        Printer printer = root.printer();
        if (noStream) {
            JsonNode response = api.post("/v1/ask", null, body);
            if (printer.format() == OutputFormat.table) {
                printer.text(response.path("answer").asString(""));
                printCitations(printer, response);
            } else {
                printer.print(response);
            }
            return ExitCodes.OK;
        }
        HttpResponse<InputStream> response = api.open("POST", "/v1/ask/stream", null, body, Map.of(),
                "text/event-stream", root.timeout());
        if (response.statusCode() != 200) {
            byte[] bytes;
            try (InputStream in = response.body()) {
                bytes = in.readNBytes(64 * 1024);
            } catch (IOException e) {
                bytes = new byte[0];
            }
            throw api.problem(response.statusCode(), response.headers(), bytes);
        }
        PrintWriter out = root.ctx().out();
        boolean streamedText = false;
        try (SseReader reader = new SseReader(response.body())) {
            SseEvent event;
            while ((event = reader.next()) != null) {
                JsonNode data = parse(api, event.data());
                if (printer.format() == OutputFormat.ndjson) {
                    ObjectNode line = root.json().createObjectNode();
                    line.put("event", event.event());
                    line.set("data", data);
                    printer.print(line);
                }
                switch (event.event()) {
                    case "delta" -> {
                        if (printer.format() == OutputFormat.table) {
                            out.print(data.path("text").asString(""));
                            out.flush();
                            streamedText = true;
                        }
                    }
                    case "progress" -> root.notice("[" + data.path("tool").asString("tool") + " "
                            + data.path("status").asString("") + "]"
                            + (data.hasNonNull("summary") ? " " + data.path("summary").asString() : ""));
                    case "warning" -> root.notice("warning: " + data.path("message").asString(""));
                    case "action" -> root.notice("proposed action " + data.path("id").asString("") + ": "
                            + data.path("description").asString("") + " (confirm with the API)");
                    case "error" -> throw new ApiException(data.path("status").asInt(500),
                            data.path("code").asString("ASK_FAILED"), data.path("title").asString(null),
                            data.path("detail").asString(null), data, null);
                    case "done" -> {
                        if (printer.format() == OutputFormat.table) {
                            if (streamedText) {
                                out.println();
                                out.println();
                                out.println("Verified answer:");
                            }
                            out.println(data.path("answer").asString(""));
                            out.flush();
                            printCitations(printer, data);
                        } else if (printer.format() == OutputFormat.json) {
                            printer.print(data);
                        }
                        return ExitCodes.OK;
                    }
                    default -> {
                        // citation events are repeated in done; unknown events are ignored.
                    }
                }
            }
        } catch (IOException e) {
            throw new NetworkException("The answer stream failed: " + e.getMessage(), e);
        }
        throw new NetworkException("The answer stream ended before the answer was complete", null);
    }

    private static void printCitations(Printer printer, JsonNode response) {
        JsonNode citations = response.path("citations");
        if (citations.isArray() && !citations.isEmpty()) {
            printer.text("");
            printer.text("Citations:");
            int i = 1;
            for (JsonNode c : citations) {
                printer.text("  [" + i++ + "] " + c.path("title").asString("") + " (" + c.path("id").asString("")
                        + ")");
            }
        }
    }

    private static JsonNode parse(ApiClient api, String data) {
        if (data == null || data.isEmpty()) {
            return MissingNode.getInstance();
        }
        try {
            return api.json().readTree(data);
        } catch (JacksonException e) {
            throw new ApiException(200, "INVALID_RESPONSE", "The server sent an event that is not JSON",
                    e.getOriginalMessage(), null, null);
        }
    }
}
