package ai.foresignal.cli.commands;

import static ai.foresignal.cli.commands.Pages.seg;

import ai.foresignal.cli.CliUsageException;
import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.http.Query;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.OutputFormat;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal triggers …}: {@code listTriggers}, {@code createTrigger}, {@code testTrigger},
 * {@code getTriggerQuality} and tuning ({@code getTriggerSuggestions}, {@code applyTuningSuggestion},
 * {@code dismissTuningSuggestion}).
 */
@Command(name = "triggers", description = "List, create, test, assess and tune trigger rules.")
public final class TriggersCommand {

    private static final Column[] RULE_COLUMNS = {
        Column.of("id"), Column.of("name"), Column.of("enabled"), Column.of("mode"), Column.of("severity"),
        new Column("ALERTS", "alertCount"), new Column("LAST FIRED", "lastFiredAt")
    };

    private static final Column[] SUGGESTION_COLUMNS = {
        Column.of("id"), Column.of("kind"), Column.of("rationale"), new Column("EXPECTED EFFECT", "expectedEffect")
    };

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public TriggersCommand() {
    }

    /**
     * {@code triggers list}: {@code GET /v1/triggers}.
     *
     * @param tag     {@code tag}
     * @param enabled {@code enabled}
     * @return the exit code
     */
    @Command(name = "list", description = "List trigger rules.")
    public int list(@Option(names = "--tag", description = "Tag") String tag,
            @Option(names = "--enabled", arity = "1", description = "true or false") Boolean enabled) {
        root.printer().print(root.api().get("/v1/triggers", Query.of().add("tag", tag).add("enabled", enabled)),
                RULE_COLUMNS);
        return ExitCodes.OK;
    }

    /**
     * {@code triggers create -f rule.json}: {@code POST /v1/triggers} with a {@code TriggerRuleInput}.
     *
     * @param file the rule file ({@code -} for standard input)
     * @return the exit code
     */
    @Command(name = "create", description = "Create a trigger rule from a JSON file (agent).")
    public int create(@Option(names = {"-f", "--file"}, required = true, paramLabel = "RULE_JSON",
            description = "TriggerRuleInput JSON file, or - for standard input") Path file) {
        JsonNode rule = root.readJsonFile(file);
        if (!rule.isObject()) {
            throw new CliUsageException(file + " must hold a JSON object (TriggerRuleInput)");
        }
        root.printer().print(root.api().post("/v1/triggers", null, rule));
        return ExitCodes.OK;
    }

    /**
     * {@code triggers test}: {@code POST /v1/triggers/{ruleId}/test}.
     *
     * @param ruleId   the rule id
     * @param since    {@code since}
     * @param until    {@code until}
     * @param fromSeq  {@code fromSeq}
     * @param toSeq    {@code toSeq}
     * @param limit    {@code limit}
     * @param simulate {@code simulate}
     * @return the exit code
     */
    @Command(name = "test", description = "Test a rule against stored history (agent).")
    public int test(@Parameters(paramLabel = "RULE_ID") String ruleId,
            @Option(names = "--since", description = "ISO-8601 instant") String since,
            @Option(names = "--until", description = "ISO-8601 instant") String until,
            @Option(names = "--from-seq", description = "First seq") Long fromSeq,
            @Option(names = "--to-seq", description = "Last seq") Long toSeq,
            @Option(names = "--limit", description = "Matches to return (1–1000)") Integer limit,
            @Option(names = "--simulate", description = "Simulate cooldown and suppression") boolean simulate) {
        Query q = Query.of().add("since", since).add("until", until).add("fromSeq", fromSeq).add("toSeq", toSeq)
                .add("limit", limit).flag("simulate", simulate);
        JsonNode result = root.api().post("/v1/triggers/" + seg(ruleId) + "/test", q, null);
        if (root.printer().format() == OutputFormat.table) {
            root.printer().text("Rule " + result.path("ruleId").asString(ruleId) + ": "
                    + result.path("matchCount").asString(Integer.toString(result.path("matches").size()))
                    + " matches" + (result.path("truncated").asBoolean(false) ? " (truncated)" : "")
                    + (result.has("wouldFire") ? ", would fire " + result.path("wouldFire").asString() : ""));
            root.printer().table(result.path("matches"), Column.of("seq"), Column.of("source"),
                    new Column("EXTERNAL ID", "externalId"), Column.of("kind"), new Column("OBSERVED", "observedAt"),
                    Column.of("title"));
        } else {
            root.printer().print(result);
        }
        return ExitCodes.OK;
    }

    /**
     * {@code triggers quality}: {@code GET /v1/triggers/{ruleId}/quality}.
     *
     * @param ruleId the rule id
     * @param since  {@code since}
     * @return the exit code
     */
    @Command(name = "quality", description = "Show a rule's alert quality (precision, feedback, flapping).")
    public int quality(@Parameters(paramLabel = "RULE_ID") String ruleId,
            @Option(names = "--since", description = "ISO-8601 instant") String since) {
        root.printer().print(root.api().get("/v1/triggers/" + seg(ruleId) + "/quality",
                Query.of().add("since", since)));
        return ExitCodes.OK;
    }

    /**
     * {@code triggers tune}: lists tuning suggestions ({@code GET /v1/triggers/{ruleId}/suggestions}), or applies
     * ({@code --apply}) or dismisses ({@code --dismiss}) one.
     *
     * @param ruleId   the rule id
     * @param since    {@code since}
     * @param simulate {@code simulate}
     * @param explain  {@code explain}
     * @param status   {@code status}
     * @param apply    the suggestion to apply
     * @param dismiss  the suggestion to dismiss
     * @param note     the note of an apply
     * @param reason   the reason of a dismissal
     * @return the exit code
     */
    @Command(name = "tune", description = "List, apply or dismiss tuning suggestions for a rule.")
    public int tune(@Parameters(paramLabel = "RULE_ID") String ruleId,
            @Option(names = "--since", description = "ISO-8601 instant") String since,
            @Option(names = "--simulate", description = "Simulate each suggestion") boolean simulate,
            @Option(names = "--explain", description = "Add an LLM explanation") boolean explain,
            @Option(names = "--status", description = "OPEN, APPLIED, DISMISSED or STALE") String status,
            @Option(names = "--apply", paramLabel = "SUGGESTION_ID", description = "Apply a suggestion (agent)")
            String apply,
            @Option(names = "--dismiss", paramLabel = "SUGGESTION_ID", description = "Dismiss a suggestion (agent)")
            String dismiss,
            @Option(names = "--note", description = "Note recorded with --apply") String note,
            @Option(names = "--reason", description = "Reason recorded with --dismiss") String reason) {
        String base = "/v1/triggers/" + seg(ruleId) + "/suggestions";
        if (apply != null && dismiss != null) {
            throw new CliUsageException("--apply and --dismiss are mutually exclusive");
        }
        if (apply != null) {
            ObjectNode body = root.json().createObjectNode();
            if (note != null) {
                body.put("note", note);
            }
            root.printer().print(root.api().post(base + "/" + seg(apply) + "/apply", null, body));
            return ExitCodes.OK;
        }
        if (dismiss != null) {
            ObjectNode body = root.json().createObjectNode();
            if (reason != null) {
                body.put("reason", reason);
            }
            root.printer().print(root.api().post(base + "/" + seg(dismiss) + "/dismiss", null, body));
            return ExitCodes.OK;
        }
        JsonNode list = root.api().get(base, Query.of().add("since", since).flag("simulate", simulate)
                .flag("explain", explain).add("status", status));
        if (root.printer().format() == OutputFormat.json) {
            root.printer().print(list);
        } else {
            root.printer().print(list.path("suggestions"), SUGGESTION_COLUMNS);
        }
        return ExitCodes.OK;
    }
}
