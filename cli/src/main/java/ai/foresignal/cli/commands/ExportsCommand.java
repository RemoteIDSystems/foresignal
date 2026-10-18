package ai.foresignal.cli.commands;

import static ai.foresignal.cli.commands.Pages.seg;

import ai.foresignal.cli.CliUsageException;
import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.NetworkException;
import ai.foresignal.cli.http.Query;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.OutputFormat;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal exports …} (prompt 111): {@code createExport}, {@code getExport} (status) and
 * {@code downloadExport}.
 */
@Command(name = "exports", description = "Create bulk exports, follow them and download the files.")
public final class ExportsCommand {

    /** Job statuses after which a job no longer changes (the API's {@code ExportJob.status} values, verbatim). */
    public static final Set<String> FINAL_STATUSES = Set.of("DONE", "FAILED", "CANCELLED", "EXPIRED");

    private static final Pattern FILENAME = Pattern.compile("filename\\*?=(?:UTF-8'')?\"?([^\";]+)\"?",
            Pattern.CASE_INSENSITIVE);

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public ExportsCommand() {
    }

    /**
     * {@code exports create}: {@code POST /v1/exports} with an {@code ExportSpec} from {@code -f} or from flags
     * (flags override the file's fields).
     *
     * @param file         an {@code ExportSpec} JSON file
     * @param format       {@code geoparquet}, {@code geopackage}, {@code flatgeobuf} or {@code gpx}
     * @param sources      sources
     * @param kinds        kinds
     * @param bbox         {@code minLon,minLat,maxLon,maxLat}
     * @param from         start instant
     * @param to           end instant
     * @param mode         {@code current}, {@code history}, {@code asOf} or {@code atSeq}
     * @param name         a job name
     * @param wait         poll until the job finishes
     * @param pollInterval seconds between polls
     * @return the exit code
     */
    @Command(name = "create", description = "Create an export job (agent).")
    public int create(
            @Option(names = {"-f", "--file"}, paramLabel = "SPEC_JSON", description = "ExportSpec JSON file") Path file,
            @Option(names = "--format", description = "geoparquet, geopackage, flatgeobuf or gpx") String format,
            @Option(names = "--source", split = ",", description = "Source id (repeatable)") List<String> sources,
            @Option(names = "--kind", split = ",", description = "Kind (repeatable)") List<String> kinds,
            @Option(names = "--bbox", description = "minLon,minLat,maxLon,maxLat") String bbox,
            @Option(names = "--from", description = "ISO-8601 instant") String from,
            @Option(names = "--to", description = "ISO-8601 instant") String to,
            @Option(names = "--mode", description = "current, history, asOf or atSeq") String mode,
            @Option(names = "--name", description = "Job name") String name,
            @Option(names = "--wait", description = "Wait until the job finishes") boolean wait,
            @Option(names = "--poll-interval", defaultValue = "5",
                    description = "Seconds between status polls with --wait (default: ${DEFAULT-VALUE})")
            int pollInterval) {
        ObjectNode spec;
        if (file != null) {
            JsonNode read = root.readJsonFile(file);
            if (!read.isObject()) {
                throw new CliUsageException(file + " must hold a JSON object (ExportSpec)");
            }
            spec = (ObjectNode) read;
        } else {
            spec = root.json().createObjectNode();
        }
        putIfSet(spec, "format", format);
        putIfSet(spec, "mode", mode);
        putIfSet(spec, "name", name);
        putIfSet(spec, "from", from);
        putIfSet(spec, "to", to);
        if (sources != null) {
            ArrayNode array = spec.putArray("sources");
            sources.forEach(array::add);
        }
        if (kinds != null) {
            ArrayNode array = spec.putArray("kinds");
            kinds.forEach(array::add);
        }
        if (bbox != null) {
            ArrayNode array = spec.putArray("bbox");
            for (String part : bbox.split(",")) {
                try {
                    array.add(Double.parseDouble(part.trim()));
                } catch (NumberFormatException e) {
                    throw new CliUsageException("--bbox must be four numbers: minLon,minLat,maxLon,maxLat");
                }
            }
            if (array.size() != 4) {
                throw new CliUsageException("--bbox must be four numbers: minLon,minLat,maxLon,maxLat");
            }
        }
        if (!spec.has("format")) {
            throw new CliUsageException("An export needs a format: --format or \"format\" in the spec file");
        }
        ApiClient api = root.api();
        JsonNode job = api.post("/v1/exports", null, spec);
        if (wait) {
            job = waitFor(api, job, pollInterval);
        }
        printJob(job);
        return ExitCodes.OK;
    }

    /**
     * {@code exports status}: {@code GET /v1/exports/{exportId}}.
     *
     * @param exportId     the job id
     * @param wait         poll until the job finishes
     * @param pollInterval seconds between polls
     * @return the exit code
     */
    @Command(name = "status", description = "Show an export job.")
    public int status(@Parameters(paramLabel = "EXPORT_ID") String exportId,
            @Option(names = "--wait", description = "Wait until the job finishes") boolean wait,
            @Option(names = "--poll-interval", defaultValue = "5",
                    description = "Seconds between status polls with --wait (default: ${DEFAULT-VALUE})")
            int pollInterval) {
        ApiClient api = root.api();
        JsonNode job = api.get("/v1/exports/" + seg(exportId), null);
        if (wait) {
            job = waitFor(api, job, pollInterval);
        }
        printJob(job);
        return ExitCodes.OK;
    }

    /**
     * {@code exports download}: {@code GET /v1/exports/{exportId}/download}, written to {@code --dest} (a file, or
     * a directory where the server's file name is used).
     *
     * @param exportId the job id
     * @param fileName one file of a multi-file job
     * @param zip      ask for the whole job as one zip ({@code bundle=zip})
     * @param dest     the target file or directory (default: the current directory)
     * @return the exit code
     */
    @Command(name = "download", description = "Download an export's file.")
    public int download(@Parameters(paramLabel = "EXPORT_ID") String exportId,
            @Option(names = "--file", description = "One file of a multi-file export") String fileName,
            @Option(names = "--zip", description = "Download every file as one zip") boolean zip,
            @Option(names = "--dest", description = "Target file or directory (default: current directory)")
            Path dest) {
        Path target = dest == null ? Path.of(".") : dest;
        boolean intoDirectory = Files.isDirectory(target);
        Path dir = intoDirectory ? target : (target.toAbsolutePath().getParent());
        Path tmp;
        try {
            Files.createDirectories(dir);
            tmp = Files.createTempFile(dir, ".foresignal-download-", ".part");
        } catch (IOException e) {
            throw new CliUsageException("Cannot write to " + dir + ": " + e.getMessage(), e);
        }
        Query q = Query.of().add("file", fileName).add("bundle", zip ? "zip" : null);
        Path written;
        try {
            HttpHeaders headers = root.api().download("/v1/exports/" + seg(exportId) + "/download", q, Map.of(),
                    tmp);
            if (intoDirectory) {
                String name = headers.firstValue("Content-Disposition").map(ExportsCommand::fileName)
                        .orElse(fileName != null ? fileName : exportId + (zip ? ".zip" : ".bin"));
                written = target.resolve(name);
            } else {
                written = target;
            }
            Files.move(tmp, written, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CliUsageException("Cannot write the download: " + e.getMessage(), e);
        } finally {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
                // Best effort.
            }
        }
        long size;
        try {
            size = Files.size(written);
        } catch (IOException e) {
            size = -1;
        }
        if (root.printer().format() == OutputFormat.table) {
            root.printer().text("Saved " + written + " (" + size + " bytes)");
        } else {
            root.printer().print(root.json().createObjectNode().put("file", written.toString()).put("bytes", size));
        }
        return ExitCodes.OK;
    }

    /**
     * Extracts the file name of a {@code Content-Disposition} header, keeping only its last path element.
     *
     * @param header the header value
     * @return the file name, or null when the header names none
     */
    static String fileName(String header) {
        Matcher m = FILENAME.matcher(header);
        String found = null;
        while (m.find()) {
            found = m.group(1).trim();
        }
        if (found == null || found.isEmpty()) {
            return null;
        }
        String name = found.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        return name.isEmpty() || name.equals("..") || name.equals(".") ? null : name;
    }

    private JsonNode waitFor(ApiClient api, JsonNode job, int pollInterval) {
        String id = job.path("id").asString(null);
        while (id != null && !FINAL_STATUSES.contains(job.path("status").asString(""))) {
            root.notice("export " + id + ": " + job.path("status").asString("?") + " "
                    + job.path("progress").asString("0") + "%");
            try {
                root.ctx().sleeper().sleep(Duration.ofSeconds(Math.max(1, pollInterval)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NetworkException("Interrupted while waiting for the export", e);
            }
            job = api.get("/v1/exports/" + seg(id), null);
        }
        return job;
    }

    private void printJob(JsonNode job) {
        if (root.printer().format() == OutputFormat.table) {
            root.printer().text("Export " + job.path("id").asString("?") + ": " + job.path("status").asString("?")
                    + " " + job.path("progress").asString("0") + "% (" + job.path("format").asString("?") + ")"
                    + (job.hasNonNull("rowCount") ? ", " + job.path("rowCount").asString() + " rows" : "")
                    + (job.hasNonNull("error") ? ", error: " + job.path("error").asString() : ""));
            JsonNode files = job.path("files");
            if (files.isArray() && !files.isEmpty()) {
                root.printer().table(files, new Column("FILE", "name"),
                        Column.of("bytes"), Column.of("sha256"),
                        new Column("EXPIRES", "expiresAt"));
            }
        } else {
            root.printer().print(job);
        }
    }

    private static void putIfSet(ObjectNode spec, String field, String value) {
        if (value != null) {
            spec.put(field, value);
        }
    }
}
