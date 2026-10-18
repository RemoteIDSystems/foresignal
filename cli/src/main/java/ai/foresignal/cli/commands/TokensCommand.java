package ai.foresignal.cli.commands;

import static ai.foresignal.cli.commands.Pages.seg;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.OutputFormat;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code foresignal tokens …}: {@code listTokens}, {@code createToken} and {@code revokeToken} (admin). The new
 * token's secret is shown once, as the API returns it; the CLI's own configured token is never printed.
 */
@Command(name = "tokens", description = "List, create and revoke API tokens (admin).")
public final class TokensCommand {

    private static final Column[] TOKEN_COLUMNS = {
        Column.of("id"), Column.of("name"), Column.of("scope"), new Column("PREFIX", "tokenPrefix"),
        new Column("CREATED", "createdAt"), new Column("LAST USED", "lastUsedAt"), new Column("EXPIRES", "expiresAt"),
        new Column("REVOKED", "revokedAt")
    };

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public TokensCommand() {
    }

    /**
     * {@code tokens list}: {@code GET /v1/tokens}.
     *
     * @return the exit code
     */
    @Command(name = "list", description = "List tokens.")
    public int list() {
        root.printer().print(root.api().get("/v1/tokens", null), TOKEN_COLUMNS);
        return ExitCodes.OK;
    }

    /**
     * {@code tokens create}: {@code POST /v1/tokens}.
     *
     * @param name          the token name
     * @param scope         {@code read}, {@code agent} or {@code admin}
     * @param expiresAt     optional expiry (ISO-8601)
     * @param privateAccess grant access to private sources
     * @return the exit code
     */
    @Command(name = "create", description = "Create a token; its secret is shown once.")
    public int create(@Option(names = "--name", required = true, description = "Token name") String name,
            @Option(names = "--scope", required = true, description = "read, agent or admin") String scope,
            @Option(names = "--expires-at", description = "ISO-8601 expiry") String expiresAt,
            @Option(names = "--private-access", description = "Allow private sources") boolean privateAccess) {
        ObjectNode body = root.json().createObjectNode();
        body.put("name", name);
        body.put("scope", scope);
        if (expiresAt != null) {
            body.put("expiresAt", expiresAt);
        }
        if (privateAccess) {
            body.put("privateAccess", true);
        }
        JsonNode created = root.api().post("/v1/tokens", null, body);
        if (root.printer().format() == OutputFormat.table) {
            root.printer().text("Created token " + created.path("id").asString("?") + " ("
                    + created.path("scope").asString(scope) + "). Store it now; it is not shown again:");
            root.printer().text(created.path("token").asString(""));
        } else {
            root.printer().print(created);
        }
        return ExitCodes.OK;
    }

    /**
     * {@code tokens revoke}: {@code DELETE /v1/tokens/{tokenId}}.
     *
     * @param tokenId the token id
     * @return the exit code
     */
    @Command(name = "revoke", description = "Revoke a token.")
    public int revoke(@Parameters(paramLabel = "TOKEN_ID") String tokenId) {
        root.api().delete("/v1/tokens/" + seg(tokenId));
        if (root.printer().format() == OutputFormat.table) {
            root.printer().text("Revoked token " + tokenId);
        } else {
            root.printer().print(root.json().createObjectNode().put("id", tokenId).put("revoked", true));
        }
        return ExitCodes.OK;
    }
}
