package ai.foresignal.cli.commands;

import static ai.foresignal.cli.commands.Pages.seg;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.edp.EdpLocalValidator;
import ai.foresignal.cli.http.Query;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.OutputFormat;
import ai.foresignal.cli.output.Printer;
import java.nio.file.Path;
import java.util.Map;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal edp …}: the External Data Protocol. {@code validate} checks a manifest and items offline
 * against the bundled EDP 1.0 JSON Schemas, or with {@code --remote} through {@code POST /v1/external/validate}
 * ({@code validateEdp}); {@code sources} administers external sources; {@code tokens create} makes a registration
 * token ({@code fsx_…}).
 */
@Command(name = "edp", description = "External Data Protocol: validate, administer sources, registration tokens.",
        subcommands = {EdpCommand.Sources.class, EdpCommand.Tokens.class})
public final class EdpCommand {

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public EdpCommand() {
    }

    /**
     * {@code edp validate <manifest.json> [items.ndjson]}. Exits 0 when everything conforms and 1 when anything
     * does not.
     *
     * @param manifestFile the manifest (or a provider-kit example with {@code manifest} and {@code batch})
     * @param itemsFile    NDJSON items (or a {@code .json} array or batch), optional
     * @param remote       validate on the server instead ({@code POST /v1/external/validate})
     * @param sourceId     with {@code --remote}: the registered source to compare with (breaking changes)
     * @return the exit code
     */
    @Command(name = "validate", description = "Validate an EDP manifest and items (locally, or with --remote).")
    public int validate(@Parameters(index = "0", paramLabel = "MANIFEST_JSON") Path manifestFile,
            @Parameters(index = "1", arity = "0..1", paramLabel = "ITEMS_NDJSON") Path itemsFile,
            @Option(names = "--remote", description = "Validate on the server (POST /v1/external/validate)")
            boolean remote,
            @Option(names = "--source-id", description = "With --remote: compare with this registered source")
            String sourceId) {
        EdpLocalValidator validator = new EdpLocalValidator(root.json());
        EdpLocalValidator.Input input = validator.read(manifestFile, itemsFile);
        Printer printer = root.printer();
        if (remote) {
            ObjectNode body = root.json().createObjectNode();
            body.set("manifest", input.manifest());
            if (sourceId != null) {
                body.put("sourceId", sourceId);
            }
            ArrayNode items = body.putArray("items");
            for (Map.Entry<String, JsonNode> item : input.items()) {
                items.add(item.getValue());
            }
            JsonNode report = root.api().post("/v1/external/validate", null, body);
            boolean manifestOk = !report.path("manifestValid").isBoolean() || report.path("manifestValid").asBoolean();
            boolean ok = manifestOk && report.path("rejected").isEmpty();
            if (printer.format() == OutputFormat.table) {
                printer.text("manifest: " + (report.path("manifestValid").isBoolean()
                        ? (report.path("manifestValid").asBoolean() ? "valid" : "invalid") : "not checked"));
                for (JsonNode error : report.path("manifestErrors")) {
                    printer.text("  " + pointer(error.path("path").asString("")) + ": "
                            + error.path("message").asString(""));
                }
                for (JsonNode change : report.path("breakingChanges")) {
                    printer.text("  breaking change: " + change.asString());
                }
                printer.text("items: " + report.path("itemsValid").asInt(0) + " valid, "
                        + report.path("rejected").size() + " rejected");
                for (JsonNode rejected : report.path("rejected")) {
                    printer.text("  " + printer.cell(rejected, Integer.MAX_VALUE));
                }
            } else {
                printer.print(report);
            }
            return ok ? ExitCodes.OK : ExitCodes.USAGE;
        }
        EdpLocalValidator.Report report = validator.validate(input.manifest(), input.items());
        if (printer.format() == OutputFormat.table) {
            printer.text("manifest: " + (report.manifestValid() ? "valid" : "invalid"));
            for (EdpLocalValidator.Problem p : report.problems()) {
                if ("manifest".equals(p.location())) {
                    printer.text("  " + pointer(p.path()) + ": " + p.message());
                }
            }
            printer.text("items: " + report.itemsValid() + " valid, " + report.itemsRejected() + " rejected");
            for (EdpLocalValidator.Problem p : report.problems()) {
                if (!"manifest".equals(p.location())) {
                    printer.text("  " + p.location() + " " + pointer(p.path()) + ": " + p.message());
                }
            }
        } else {
            ObjectNode out = root.json().createObjectNode();
            out.put("valid", report.valid());
            out.put("manifestValid", report.manifestValid());
            out.put("itemsValid", report.itemsValid());
            out.put("itemsRejected", report.itemsRejected());
            ArrayNode problems = out.putArray("problems");
            for (EdpLocalValidator.Problem p : report.problems()) {
                problems.addObject().put("location", p.location()).put("path", p.path()).put("message", p.message());
            }
            printer.print(out);
        }
        return report.valid() ? ExitCodes.OK : ExitCodes.USAGE;
    }

    private static String pointer(String path) {
        return path == null || path.isEmpty() ? "(root)" : path;
    }

    /** {@code foresignal edp sources …}: external-source administration (admin). */
    @Command(name = "sources", description = "List, show, approve and suspend external sources (admin).")
    public static final class Sources {

        private static final Column[] COLUMNS = {
            new Column("SOURCE", "sourceId"), Column.of("state"), Column.of("health"),
            new Column("VIA", "registeredVia"), new Column("CREATED", "createdAt"), new Column("LAST ITEM", "lastItemAt")
        };

        @ParentCommand
        EdpCommand edp;

        /** Creates the command group (picocli). */
        public Sources() {
        }

        /**
         * {@code edp sources list}: {@code GET /v1/external/sources}.
         *
         * @param state  {@code pending}, {@code active} or {@code suspended}
         * @param limit  {@code limit}
         * @param cursor {@code cursor}
         * @param all    follow {@code nextCursor}
         * @return the exit code
         */
        @Command(name = "list", description = "List external sources.")
        public int list(@Option(names = "--state", description = "pending, active or suspended") String state,
                @Option(names = "--limit", description = "Page size (1–500)") Integer limit,
                @Option(names = "--cursor", description = "Continue from a page's nextCursor") String cursor,
                @Option(names = "--all", description = "Fetch every page") boolean all) {
            Pages.list(edp.root, "/v1/external/sources",
                    Query.of().add("state", state).add("limit", limit).add("cursor", cursor), all, COLUMNS);
            return ExitCodes.OK;
        }

        /**
         * {@code edp sources show}: {@code GET /v1/external/sources/{sourceId}}.
         *
         * @param sourceId the source id ({@code ext-…})
         * @return the exit code
         */
        @Command(name = "show", description = "Show an external source.")
        public int show(@Parameters(paramLabel = "SOURCE_ID") String sourceId) {
            edp.root.printer().print(edp.root.api().get("/v1/external/sources/" + seg(sourceId), null));
            return ExitCodes.OK;
        }

        /**
         * {@code edp sources approve}: {@code POST /v1/external/sources/{sourceId}/approve}.
         *
         * @param sourceId the source id
         * @return the exit code
         */
        @Command(name = "approve", description = "Approve a pending external source.")
        public int approve(@Parameters(paramLabel = "SOURCE_ID") String sourceId) {
            return transition(sourceId, "approve");
        }

        /**
         * {@code edp sources suspend}: {@code POST /v1/external/sources/{sourceId}/suspend}.
         *
         * @param sourceId the source id
         * @return the exit code
         */
        @Command(name = "suspend", description = "Suspend an external source.")
        public int suspend(@Parameters(paramLabel = "SOURCE_ID") String sourceId) {
            return transition(sourceId, "suspend");
        }

        private int transition(String sourceId, String action) {
            JsonNode source = edp.root.api().post("/v1/external/sources/" + seg(sourceId) + "/" + action, null, null);
            if (edp.root.printer().format() == OutputFormat.table) {
                edp.root.printer().text(source.path("sourceId").asString(sourceId) + ": "
                        + source.path("state").asString("?"));
            } else {
                edp.root.printer().print(source);
            }
            return ExitCodes.OK;
        }
    }

    /** {@code foresignal edp tokens …}: registration tokens (admin). */
    @Command(name = "tokens", description = "Create EDP registration tokens (admin).")
    public static final class Tokens {

        @ParentCommand
        EdpCommand edp;

        /** Creates the command group (picocli). */
        public Tokens() {
        }

        /**
         * {@code edp tokens create}: {@code POST /v1/external/registration-tokens}; the {@code fsx_…} token is
         * shown once.
         *
         * @param name           the token name
         * @param expiresAt      optional expiry
         * @param maxUses        optional use limit
         * @param sourceIdPrefix optional source id prefix the token may register
         * @return the exit code
         */
        @Command(name = "create", description = "Create a registration token; it is shown once.")
        public int create(@Option(names = "--name", required = true, description = "Token name") String name,
                @Option(names = "--expires-at", description = "ISO-8601 expiry") String expiresAt,
                @Option(names = "--max-uses", description = "Registrations allowed") Integer maxUses,
                @Option(names = "--source-id-prefix", description = "Source ids it may register must start so")
                String sourceIdPrefix) {
            ObjectNode body = edp.root.json().createObjectNode();
            body.put("name", name);
            if (expiresAt != null) {
                body.put("expiresAt", expiresAt);
            }
            if (maxUses != null) {
                body.put("maxUses", maxUses);
            }
            if (sourceIdPrefix != null) {
                body.put("sourceIdPrefix", sourceIdPrefix);
            }
            JsonNode created = edp.root.api().post("/v1/external/registration-tokens", null, body);
            if (edp.root.printer().format() == OutputFormat.table) {
                edp.root.printer().text("Created registration token " + created.path("id").asString("?")
                        + ". Store it now; it is not shown again:");
                edp.root.printer().text(created.path("token").asString(""));
            } else {
                edp.root.printer().print(created);
            }
            return ExitCodes.OK;
        }
    }
}
