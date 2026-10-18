package ai.foresignal.cli.commands;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.output.Printer;
import ai.foresignal.cli.sse.Backoff;
import ai.foresignal.cli.sse.ChangeTail;
import java.time.Duration;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal changes tail}: follows {@code GET /v1/changes/stream} with resume, {@code reset} and
 * {@code resync} handling and a heartbeat watchdog (see {@link ChangeTail}).
 *
 * <p>In {@code json} and {@code ndjson} each event is one line {@code {"event": …, "id": …, "data": …}} and each
 * snapshot record one line {@code {"event": "snapshot", "data": …}}; in {@code table} each is a short summary line.
 * Notices ({@code reset}, {@code resync}, reconnects) go to standard error.
 */
@Command(name = "changes", description = "Follow changes as they happen.")
public final class ChangesCommand {

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public ChangesCommand() {
    }

    /**
     * {@code changes tail}.
     *
     * @param sources          {@code source} filters
     * @param kinds            {@code kind} filters
     * @param bbox             {@code bbox}
     * @param live             receive the conflated {@code live} lane
     * @param touches          receive {@code touch} events
     * @param after            start after this seq
     * @param snapshot         page the snapshot first, then follow from its watermark
     * @param heartbeatSeconds the server's heartbeat interval
     * @param maxEvents        stop after this many events
     * @param maxRetries       consecutive failed reconnects before exiting with code 3
     * @return the exit code
     */
    @Command(name = "tail", description = "Stream changes (SSE) with resume and reset handling.")
    public int tail(
            @Option(names = "--source", split = ",", description = "Source id (repeatable)") List<String> sources,
            @Option(names = "--kind", split = ",", description = "Kind (repeatable)") List<String> kinds,
            @Option(names = "--bbox", description = "minLon,minLat,maxLon,maxLat") String bbox,
            @Option(names = "--live", description = "Also show conflated live position updates") boolean live,
            @Option(names = "--touches", description = "Also show touch events (implies --live)") boolean touches,
            @Option(names = "--after", description = "Start after this seq (default: live from now)") Long after,
            @Option(names = "--snapshot", description = "Print the current records first, then follow")
            boolean snapshot,
            @Option(names = "--heartbeat-seconds", defaultValue = "15",
                    description = "Server heartbeat interval; the watchdog reconnects after 2.5x (default: "
                            + "${DEFAULT-VALUE})")
            double heartbeatSeconds,
            @Option(names = "--max-events", defaultValue = "0",
                    description = "Exit after this many events (default: never)")
            long maxEvents,
            @Option(names = "--max-retries", defaultValue = "-1",
                    description = "Give up after this many consecutive failed reconnects (default: never)")
            int maxRetries) {
        Printer printer = root.printer();
        ChangeTail.Sink sink = new ChangeTail.Sink() {
            @Override
            public void event(String name, String id, JsonNode data) {
                ObjectNode line = root.json().createObjectNode();
                line.put("event", name);
                if (id != null) {
                    line.put("id", id);
                }
                line.set("data", data);
                printer.streamItem(line, summary(name, data));
            }

            @Override
            public void snapshot(JsonNode record) {
                ObjectNode line = root.json().createObjectNode();
                line.put("event", "snapshot");
                line.set("data", record);
                printer.streamItem(line, summary("snapshot", record));
            }

            @Override
            public void notice(String message) {
                root.notice(message);
            }
        };
        ChangeTail.Options options = new ChangeTail.Options(sources, kinds, bbox, live || touches, touches, after,
                snapshot, maxEvents);
        Duration heartbeat = Duration.ofMillis(Math.max(1, Math.round(heartbeatSeconds * 1000)));
        ChangeTail tail = ChangeTail.create(root.api(), options, sink, heartbeat, new Backoff(root.ctx().random()),
                root.ctx().sleeper(), root.ctx().err(), maxRetries);
        tail.run();
        return ExitCodes.OK;
    }

    /**
     * The table line of a change, live update, touch or snapshot record.
     *
     * @param name the event name
     * @param data the event data
     * @return e.g. {@code change  1234  usgs-earthquakes/us7000abcd  earthquake  v3  2026-10-18T10:00:00Z}
     */
    static String summary(String name, JsonNode data) {
        String source = data.has("source") ? data.path("source").asString("") : data.path("sourceId").asString("");
        StringBuilder sb = new StringBuilder(name);
        if (data.has("seq")) {
            sb.append("  ").append(data.path("seq").asString(""));
        }
        sb.append("  ").append(source).append('/').append(data.path("externalId").asString(""));
        if (data.has("kind")) {
            sb.append("  ").append(data.path("kind").asString(""));
        }
        if (data.has("version")) {
            sb.append("  v").append(data.path("version").asString(""));
        }
        sb.append("  ").append(data.path("observedAt").asString(""));
        if (data.path("tombstone").asBoolean(false)) {
            sb.append("  tombstone");
        }
        if (data.path("leftFilter").asBoolean(false)) {
            sb.append("  left-filter");
        }
        return sb.toString();
    }
}
