package ai.foresignal.cli.commands;

import static ai.foresignal.cli.commands.Pages.seg;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.ApiResponse;
import ai.foresignal.cli.http.NetworkException;
import ai.foresignal.cli.http.Query;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.OutputFormat;
import ai.foresignal.cli.output.Printer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal sources …}: {@code listSources}, {@code getSource}, {@code updateSource} (enable, disable),
 * {@code listSourceHealth}/{@code getSourceHealth}, {@code fetchSourceNow}, {@code probeSource} and
 * {@code runSourceDoctor} (prompt 140).
 */
@Command(name = "sources", description = "List, inspect and operate sources.")
public final class SourcesCommand {

    /** The label the doctor prints with every patch; the server sends the same text in {@code patch.label}. */
    public static final String PATCH_LABEL = "proposed, not applied";

    private static final Column[] SOURCE_COLUMNS = {
        Column.of("id"), Column.of("name"), Column.of("mode"), Column.of("enabled"), Column.of("status"),
        new Column("LAST SUCCESS", "lastSuccessAt")
    };

    private static final Column[] HEALTH_COLUMNS = {
        new Column("SOURCE", "sourceId"), Column.of("status"), new Column("FAILURES", "consecutiveFailures"),
        new Column("LAST SUCCESS", "lastSuccessAt"), new Column("LAST ERROR", "lastError")
    };

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public SourcesCommand() {
    }

    /**
     * {@code sources list}: {@code GET /v1/sources}.
     *
     * @return the exit code
     */
    @Command(name = "list", description = "List sources.")
    public int list() {
        root.printer().print(root.api().get("/v1/sources", null), SOURCE_COLUMNS);
        return ExitCodes.OK;
    }

    /**
     * {@code sources show}: {@code GET /v1/sources/{sourceId}}.
     *
     * @param sourceId the source id
     * @return the exit code
     */
    @Command(name = "show", description = "Show one source.")
    public int show(@Parameters(paramLabel = "SOURCE_ID", description = "Source id") String sourceId) {
        root.printer().print(root.api().get("/v1/sources/" + seg(sourceId), null));
        return ExitCodes.OK;
    }

    /**
     * {@code sources enable}: {@code PUT /v1/sources/{sourceId}} with {@code {"enabled": true}}.
     *
     * @param sourceId the source id
     * @return the exit code
     */
    @Command(name = "enable", description = "Enable a source (admin).")
    public int enable(@Parameters(paramLabel = "SOURCE_ID", description = "Source id") String sourceId) {
        return setEnabled(sourceId, true);
    }

    /**
     * {@code sources disable}: {@code PUT /v1/sources/{sourceId}} with {@code {"enabled": false}}.
     *
     * @param sourceId the source id
     * @return the exit code
     */
    @Command(name = "disable", description = "Disable a source (admin).")
    public int disable(@Parameters(paramLabel = "SOURCE_ID", description = "Source id") String sourceId) {
        return setEnabled(sourceId, false);
    }

    private int setEnabled(String sourceId, boolean enabled) {
        ObjectNode body = root.json().createObjectNode().put("enabled", enabled);
        JsonNode source = root.api().put("/v1/sources/" + seg(sourceId), body);
        if (root.printer().format() == OutputFormat.table) {
            root.printer().text(source.path("id").asString(sourceId) + ": enabled="
                    + source.path("enabled").asString(Boolean.toString(enabled)));
        } else {
            root.printer().print(source);
        }
        return ExitCodes.OK;
    }

    /**
     * {@code sources health}: {@code GET /v1/sources/health}, or {@code GET /v1/sources/{sourceId}/health}.
     *
     * @param sourceId the source id, or null for every source
     * @param status   status filters ({@code healthy}, {@code degraded}, {@code failing}, {@code idle},
     *                 {@code disabled})
     * @return the exit code
     */
    @Command(name = "health", description = "Show source health (all sources, or one).")
    public int health(
            @Parameters(paramLabel = "SOURCE_ID", arity = "0..1", description = "Source id (default: all)")
            String sourceId,
            @Option(names = "--status", split = ",", description = "Filter by status (all sources only)")
            List<String> status) {
        if (sourceId != null) {
            root.printer().print(root.api().get("/v1/sources/" + seg(sourceId) + "/health", null));
        } else {
            root.printer().print(root.api().get("/v1/sources/health", Query.of().addAll("status", status)),
                    HEALTH_COLUMNS);
        }
        return ExitCodes.OK;
    }

    /**
     * {@code sources fetch}: {@code POST /v1/sources/{sourceId}/fetch}.
     *
     * @param sourceId    the source id
     * @param allowShrink pass {@code allowShrink=true}
     * @return the exit code
     */
    @Command(name = "fetch", description = "Fetch a source now (admin).")
    public int fetch(@Parameters(paramLabel = "SOURCE_ID", description = "Source id") String sourceId,
            @Option(names = "--allow-shrink", description = "Accept a much smaller result than usual")
            boolean allowShrink) {
        JsonNode outcome = root.api().post("/v1/sources/" + seg(sourceId) + "/fetch",
                Query.of().flag("allowShrink", allowShrink), null);
        root.printer().print(outcome);
        return ExitCodes.OK;
    }

    /**
     * {@code sources probe}: {@code POST /v1/sources/{sourceId}/probe}.
     *
     * @param sourceId the source id
     * @param maxBytes the {@code maxBytes} parameter
     * @param seconds  the {@code seconds} parameter
     * @return the exit code
     */
    @Command(name = "probe", description = "Probe a source's upstream without storing anything (admin).")
    public int probe(@Parameters(paramLabel = "SOURCE_ID", description = "Source id") String sourceId,
            @Option(names = "--max-bytes", description = "Largest body to read (1024–10485760)") Integer maxBytes,
            @Option(names = "--seconds", description = "Probe duration of streaming sources (1–60)") Integer seconds) {
        JsonNode result = root.api().post("/v1/sources/" + seg(sourceId) + "/probe",
                Query.of().add("maxBytes", maxBytes).add("seconds", seconds), null);
        root.printer().print(result);
        return ExitCodes.OK;
    }

    /**
     * {@code sources doctor}: {@code POST /v1/sources/{sourceId}/doctor} with {@code DoctorOptions}. The server
     * answers within 90 s with the report, or {@code 202} with a {@code Location}, which is polled until it answers
     * {@code 200}. Prints the findings, and any LLM patch with its "proposed, not applied" label.
     *
     * @param sourceId     the source id
     * @param noProbe      send {@code probe: false}
     * @param llm          {@code auto}, {@code on} or {@code off}
     * @param detail       {@code compact} or {@code full}
     * @param pollInterval seconds between polls of the {@code Location}
     * @param maxPolls     polls before giving up (0 = no limit)
     * @return the exit code
     */
    @Command(name = "doctor", description = "Diagnose a source with the adapter doctor (admin).")
    public int doctor(@Parameters(paramLabel = "SOURCE_ID", description = "Source id") String sourceId,
            @Option(names = "--no-probe", description = "Do not probe the live upstream") boolean noProbe,
            @Option(names = "--llm", description = "LLM explanation and patch: auto, on or off (default: auto)")
            String llm,
            @Option(names = "--detail", description = "Context size: compact or full (default: compact)")
            String detail,
            @Option(names = "--poll-interval", defaultValue = "5",
                    description = "Seconds between polls while the run is in progress (default: ${DEFAULT-VALUE})")
            int pollInterval,
            @Option(names = "--max-polls", defaultValue = "0",
                    description = "Give up after this many polls (default: no limit)")
            int maxPolls) {
        ApiClient api = root.api();
        ObjectNode options = root.json().createObjectNode();
        options.put("probe", !noProbe);
        if (llm != null) {
            options.put("llm", llm);
        }
        if (detail != null) {
            options.put("detail", detail);
        }
        Duration callTimeout = root.timeout().compareTo(Duration.ofSeconds(150)) > 0
                ? root.timeout()
                : Duration.ofSeconds(150);
        ApiResponse response = api.checked(api.send("POST", "/v1/sources/" + seg(sourceId) + "/doctor", null,
                options, Map.of(), callTimeout));
        int polls = 0;
        while (response.status() == 202) {
            JsonNode accepted = api.parse(response);
            String location = response.header("Location").orElse(accepted.path("location").asString(null));
            if (location == null) {
                location = "/v1/doctor/reports/" + seg(accepted.path("reportId").asString(""));
            }
            if (maxPolls > 0 && polls >= maxPolls) {
                root.notice("doctor run " + accepted.path("reportId").asString("") + " still running after "
                        + polls + " polls; check later with GET " + location);
                root.printer().print(accepted);
                return ExitCodes.OK;
            }
            root.notice("doctor run " + accepted.path("reportId").asString("") + " in progress; polling " + location);
            try {
                root.ctx().sleeper().sleep(Duration.ofSeconds(Math.max(1, pollInterval)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NetworkException("Interrupted while waiting for the doctor", e);
            }
            polls++;
            response = api.checked(api.send("GET", location, null, null, Map.of(), root.timeout()));
        }
        JsonNode report = api.parse(response);
        if (root.printer().format() == OutputFormat.table) {
            printDoctor(root.printer(), report);
        } else {
            root.printer().print(report);
        }
        return ExitCodes.OK;
    }

    /**
     * Prints a doctor report for people: status, findings with evidence, the LLM explanation, and the patch under
     * its "proposed, not applied" label.
     *
     * @param printer the printer
     * @param report  the {@code DoctorReport}
     */
    static void printDoctor(Printer printer, JsonNode report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Doctor report ").append(report.path("id").asString("?"))
                .append(" for ").append(report.path("sourceId").asString("?"))
                .append(": ").append(report.path("status").asString("?"));
        if (report.hasNonNull("createdAt")) {
            sb.append(" (").append(report.path("trigger").asString("MANUAL")).append(", ")
                    .append(report.path("createdAt").asString()).append(')');
        }
        printer.text(sb.toString());
        JsonNode findings = report.path("findings");
        if (findings.isArray() && !findings.isEmpty()) {
            printer.text("Findings:");
            for (JsonNode f : findings) {
                printer.text("  [" + f.path("severity").asString("?") + "] " + f.path("code").asString("?")
                        + (f.hasNonNull("path") ? " at " + f.path("path").asString() : "")
                        + (f.path("heuristic").asBoolean(false) ? " (heuristic)" : ""));
                for (JsonNode evidence : f.path("evidence")) {
                    printer.text("      evidence: " + printer.cell(evidence, Integer.MAX_VALUE));
                }
                if (f.hasNonNull("suggestedAction")) {
                    printer.text("      action: " + f.path("suggestedAction").asString());
                }
            }
        } else {
            printer.text("Findings: none");
        }
        JsonNode probe = report.path("probe");
        if (probe.isObject()) {
            printer.text("Probe: " + (probe.path("skipped").asBoolean(false)
                    ? "skipped (" + probe.path("skipReason").asString("unknown") + ")"
                    : probe.path("status").asString("?") + ", HTTP " + probe.path("httpStatus").asString("-")
                            + ", " + probe.path("records").asString("-") + " records"));
        }
        JsonNode llm = report.path("llm");
        if (llm.isObject()) {
            printer.text("LLM: " + llm.path("status").asString("?")
                    + (llm.hasNonNull("model") ? " (" + llm.path("model").asString() + ")" : ""));
            if (llm.hasNonNull("summary")) {
                printer.text("  summary: " + llm.path("summary").asString());
            }
            if (llm.hasNonNull("likelyCause")) {
                printer.text("  likely cause: " + llm.path("likelyCause").asString());
            }
            for (JsonNode action : llm.path("suggestedActions")) {
                printer.text("  suggested: " + action.asString());
            }
            JsonNode patch = llm.path("patch");
            if (patch.isObject()) {
                String label = patch.path("label").asString(PATCH_LABEL);
                printer.text("Patch (" + label + "): " + patch.path("file").asString("?")
                        + (patch.path("patchApplies").asBoolean(false) ? "" : " [does not apply"
                        + (patch.hasNonNull("error") ? ": " + patch.path("error").asString() : "") + "]"));
                printer.text(patch.path("unifiedDiff").asString(""));
            }
        }
    }
}
