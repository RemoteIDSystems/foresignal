package ai.foresignal.cli.commands;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.Query;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.Printer;
import ai.foresignal.cli.sse.Backoff;
import ai.foresignal.cli.sse.ResilientSseStream;
import ai.foresignal.cli.sse.SseEvent;
import ai.foresignal.cli.sse.StreamRequest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal alerts …}: {@code listAlerts}, {@code acknowledgeAlert}, {@code resolveAlert} and
 * {@code streamAlerts} (tail, resumed with {@code Last-Event-ID}).
 */
@Command(name = "alerts", description = "List, acknowledge, resolve and follow alerts.")
public final class AlertsCommand {

    private static final Column[] ALERT_COLUMNS = {
        Column.of("id"), Column.of("severity"), Column.of("state"), new Column("FIRED", "firedAt"),
        new Column("RULE", "ruleName"), Column.of("source"), new Column("EXTERNAL ID", "externalId"),
        Column.of("message")
    };

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public AlertsCommand() {
    }

    /**
     * {@code alerts list}: {@code GET /v1/alerts}.
     *
     * @param states      {@code state} filters
     * @param ruleId      {@code ruleId}
     * @param sources     {@code source} filters
     * @param kinds       {@code kind} filters
     * @param minSeverity {@code minSeverity}
     * @param since       {@code since}
     * @param tag         {@code tag}
     * @param limit       {@code limit}
     * @param cursor      {@code cursor}
     * @param all         follow {@code nextCursor}
     * @return the exit code
     */
    @Command(name = "list", description = "List alerts.")
    public int list(
            @Option(names = "--state", split = ",", description = "firing, acknowledged, resolved") List<String> states,
            @Option(names = "--rule", description = "Trigger rule id") String ruleId,
            @Option(names = "--source", split = ",", description = "Source id (repeatable)") List<String> sources,
            @Option(names = "--kind", split = ",", description = "Kind (repeatable)") List<String> kinds,
            @Option(names = "--min-severity", description = "info, warning or critical") String minSeverity,
            @Option(names = "--since", description = "ISO-8601 instant") String since,
            @Option(names = "--tag", description = "Rule tag") String tag,
            @Option(names = "--limit", description = "Page size (1–5000)") Integer limit,
            @Option(names = "--cursor", description = "Continue from a page's nextCursor") String cursor,
            @Option(names = "--all", description = "Fetch every page") boolean all) {
        Query q = Query.of().addAll("state", states).add("ruleId", ruleId).addAll("source", sources)
                .addAll("kind", kinds).add("minSeverity", minSeverity).add("since", since).add("tag", tag)
                .add("limit", limit).add("cursor", cursor);
        Pages.list(root, "/v1/alerts", q, all, ALERT_COLUMNS);
        return ExitCodes.OK;
    }

    /**
     * {@code alerts ack}: {@code POST /v1/alerts/{alertId}/ack}.
     *
     * @param alertId  the alert id
     * @param note     an optional note
     * @param feedback optional feedback ({@code useful}, {@code noise}, {@code late}, {@code false_positive})
     * @return the exit code
     */
    @Command(name = "ack", description = "Acknowledge an alert (agent).")
    public int ack(@Parameters(paramLabel = "ALERT_ID") long alertId,
            @Option(names = "--note", description = "Note") String note,
            @Option(names = "--feedback", description = "useful, noise, late or false_positive") String feedback) {
        return transition(alertId, "ack", note, feedback);
    }

    /**
     * {@code alerts resolve}: {@code POST /v1/alerts/{alertId}/resolve}.
     *
     * @param alertId  the alert id
     * @param note     an optional note
     * @param feedback optional feedback
     * @return the exit code
     */
    @Command(name = "resolve", description = "Resolve an alert (agent).")
    public int resolve(@Parameters(paramLabel = "ALERT_ID") long alertId,
            @Option(names = "--note", description = "Note") String note,
            @Option(names = "--feedback", description = "useful, noise, late or false_positive") String feedback) {
        return transition(alertId, "resolve", note, feedback);
    }

    private int transition(long alertId, String action, String note, String feedback) {
        ObjectNode body = root.json().createObjectNode();
        if (note != null) {
            body.put("note", note);
        }
        if (feedback != null) {
            body.put("feedback", feedback);
        }
        root.printer().print(root.api().post("/v1/alerts/" + alertId + "/" + action, null, body));
        return ExitCodes.OK;
    }

    /**
     * {@code alerts tail}: {@code GET /v1/alerts/stream}, reconnecting with {@code Last-Event-ID} after any drop;
     * a {@code reset} resumes from its {@code resumeAfter}, or starts fresh when it has none.
     *
     * @param ruleIds          {@code ruleId} filters
     * @param minSeverity      {@code minSeverity}
     * @param sources          {@code source} filters
     * @param kinds            {@code kind} filters
     * @param heartbeatSeconds the server's heartbeat interval
     * @param maxEvents        stop after this many events
     * @param maxRetries       consecutive failed reconnects before exiting with code 3
     * @return the exit code
     */
    @Command(name = "tail", description = "Stream alert events (SSE).")
    public int tail(
            @Option(names = "--rule", split = ",", description = "Trigger rule id (repeatable)") List<String> ruleIds,
            @Option(names = "--min-severity", description = "info, warning or critical") String minSeverity,
            @Option(names = "--source", split = ",", description = "Source id (repeatable)") List<String> sources,
            @Option(names = "--kind", split = ",", description = "Kind (repeatable)") List<String> kinds,
            @Option(names = "--heartbeat-seconds", defaultValue = "15",
                    description = "Server heartbeat interval (default: ${DEFAULT-VALUE})") double heartbeatSeconds,
            @Option(names = "--max-events", defaultValue = "0",
                    description = "Exit after this many events (default: never)") long maxEvents,
            @Option(names = "--max-retries", defaultValue = "-1",
                    description = "Give up after this many consecutive failed reconnects (default: never)")
            int maxRetries) {
        ApiClient api = root.api();
        Printer printer = root.printer();
        String[] lastId = {null};
        long[] count = {0};
        Query base = Query.of().addAll("ruleId", ruleIds).add("minSeverity", minSeverity).addAll("source", sources)
                .addAll("kind", kinds);
        ResilientSseStream stream = new ResilientSseStream(api,
                Duration.ofMillis(Math.max(1, Math.round(heartbeatSeconds * 1000))),
                new Backoff(root.ctx().random()), root.ctx().sleeper(), root.ctx().err(), maxRetries);
        stream.run(() -> {
            Map<String, String> headers = new LinkedHashMap<>();
            if (lastId[0] != null) {
                headers.put("Last-Event-ID", lastId[0]);
            }
            return new StreamRequest("/v1/alerts/stream", base.copy(), headers);
        }, (SseEvent event) -> {
            switch (event.event()) {
                case "heartbeat", "hello" -> {
                    return ResilientSseStream.Action.CONTINUE;
                }
                case "reset" -> {
                    JsonNode data = parse(api, event.data());
                    JsonNode resume = data.path("resumeAfter");
                    lastId[0] = resume.canConvertToLong() && !resume.isNull() ? resume.asString() : null;
                    root.notice("reset (" + data.path("reason").asString("unknown") + "): "
                            + (lastId[0] == null ? "starting fresh" : "resuming after " + lastId[0]));
                    return ResilientSseStream.Action.RECONNECT_NOW;
                }
                default -> {
                    if (event.id() != null) {
                        lastId[0] = event.id();
                    }
                    JsonNode data = parse(api, event.data());
                    ObjectNode line = root.json().createObjectNode();
                    line.put("event", event.event());
                    if (event.id() != null) {
                        line.put("id", event.id());
                    }
                    line.set("data", data);
                    JsonNode alert = data.has("alert") ? data.path("alert") : data;
                    printer.streamItem(line, event.event() + "  " + alert.path("id").asString("") + "  "
                            + alert.path("severity").asString("") + "  " + alert.path("state").asString("") + "  "
                            + alert.path("message").asString(""));
                    count[0]++;
                    return maxEvents > 0 && count[0] >= maxEvents
                            ? ResilientSseStream.Action.STOP
                            : ResilientSseStream.Action.CONTINUE;
                }
            }
        });
        return ExitCodes.OK;
    }

    private static JsonNode parse(ApiClient api, String data) {
        if (data == null || data.isEmpty()) {
            return MissingNode.getInstance();
        }
        try {
            return api.json().readTree(data);
        } catch (JacksonException e) {
            return MissingNode.getInstance();
        }
    }
}
