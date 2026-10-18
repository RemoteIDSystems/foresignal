package ai.foresignal.cli.commands;

import static ai.foresignal.cli.commands.Pages.seg;

import ai.foresignal.cli.ExitCodes;
import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.http.Query;
import ai.foresignal.cli.output.Column;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * {@code foresignal records …}: {@code listRecords} (query), {@code getRecord} (get), {@code listRecordVersions}
 * (history: the persisted versions of one record) and {@code getRecordTrack} (track).
 */
@Command(name = "records", description = "Query records, one record, its history and its track.")
public final class RecordsCommand {

    private static final Column[] RECORD_COLUMNS = {
        Column.of("source"), new Column("EXTERNAL ID", "externalId"), Column.of("kind"), Column.of("title"),
        new Column("OBSERVED", "observedAt"), Column.of("version")
    };

    private static final Column[] VERSION_COLUMNS = {
        Column.of("seq"), Column.of("version"), new Column("REASON", "persistReason"),
        new Column("OBSERVED", "observedAt"), new Column("INGESTED", "ingestedAt"), Column.of("tombstone")
    };

    @ParentCommand
    ForesignalCli root;

    /** Creates the command group (picocli). */
    public RecordsCommand() {
    }

    /**
     * {@code records query}: {@code GET /v1/records}.
     *
     * @param sources        {@code source} filters
     * @param kinds          {@code kind} filters
     * @param bbox           {@code bbox}
     * @param since          {@code since}
     * @param until          {@code until}
     * @param text           {@code q}
     * @param near           {@code near}
     * @param radiusKm       {@code radiusKm}
     * @param order          {@code order}
     * @param includeExpired {@code includeExpired}
     * @param limit          {@code limit}
     * @param cursor         {@code cursor}
     * @param all            follow {@code nextCursor}
     * @return the exit code
     */
    @Command(name = "query", description = "Query current records.")
    public int query(
            @Option(names = "--source", split = ",", description = "Source id (repeatable)") List<String> sources,
            @Option(names = "--kind", split = ",", description = "Kind (repeatable)") List<String> kinds,
            @Option(names = "--bbox", description = "minLon,minLat,maxLon,maxLat") String bbox,
            @Option(names = "--since", description = "ISO-8601 instant") String since,
            @Option(names = "--until", description = "ISO-8601 instant") String until,
            @Option(names = {"-q", "--q"}, description = "Text search") String text,
            @Option(names = "--near", description = "lon,lat") String near,
            @Option(names = "--radius-km", description = "Radius around --near") Double radiusKm,
            @Option(names = "--order", description = "observed or id") String order,
            @Option(names = "--include-expired", description = "Include expired records") boolean includeExpired,
            @Option(names = "--limit", description = "Page size (1–5000)") Integer limit,
            @Option(names = "--cursor", description = "Continue from a page's nextCursor") String cursor,
            @Option(names = "--all", description = "Fetch every page") boolean all) {
        Query q = Query.of().addAll("source", sources).addAll("kind", kinds).add("bbox", bbox).add("since", since)
                .add("until", until).add("q", text).add("near", near).add("radiusKm", radiusKm).add("order", order)
                .flag("includeExpired", includeExpired).add("limit", limit).add("cursor", cursor);
        Pages.list(root, "/v1/records", q, all, RECORD_COLUMNS);
        return ExitCodes.OK;
    }

    /**
     * {@code records get}: {@code GET /v1/records/{sourceId}/{externalId}}.
     *
     * @param sourceId   the source id
     * @param externalId the external id
     * @return the exit code
     */
    @Command(name = "get", description = "Show one record.")
    public int get(@Parameters(index = "0", paramLabel = "SOURCE_ID") String sourceId,
            @Parameters(index = "1", paramLabel = "EXTERNAL_ID") String externalId) {
        root.printer().print(root.api().get("/v1/records/" + seg(sourceId) + "/" + seg(externalId), null));
        return ExitCodes.OK;
    }

    /**
     * {@code records history}: {@code GET /v1/records/{sourceId}/{externalId}/versions}.
     *
     * @param sourceId   the source id
     * @param externalId the external id
     * @param since      {@code since}
     * @param until      {@code until}
     * @param limit      {@code limit}
     * @param cursor     {@code cursor}
     * @param all        follow {@code nextCursor}
     * @return the exit code
     */
    @Command(name = "history", description = "List the persisted versions of one record.")
    public int history(@Parameters(index = "0", paramLabel = "SOURCE_ID") String sourceId,
            @Parameters(index = "1", paramLabel = "EXTERNAL_ID") String externalId,
            @Option(names = "--since", description = "ISO-8601 instant") String since,
            @Option(names = "--until", description = "ISO-8601 instant") String until,
            @Option(names = "--limit", description = "Page size (1–5000)") Integer limit,
            @Option(names = "--cursor", description = "Continue from a page's nextCursor") String cursor,
            @Option(names = "--all", description = "Fetch every page") boolean all) {
        Query q = Query.of().add("since", since).add("until", until).add("limit", limit).add("cursor", cursor);
        Pages.list(root, "/v1/records/" + seg(sourceId) + "/" + seg(externalId) + "/versions", q, all,
                VERSION_COLUMNS);
        return ExitCodes.OK;
    }

    /**
     * {@code records track}: {@code GET /v1/records/{sourceId}/{externalId}/track}.
     *
     * @param sourceId   the source id
     * @param externalId the external id
     * @param from       {@code from}
     * @param to         {@code to}
     * @param maxPoints  {@code maxPoints}
     * @param tolerance  {@code tolerance}
     * @param gap        {@code gap}
     * @param noLive     send {@code includeLive=false}
     * @return the exit code
     */
    @Command(name = "track", description = "Show the track (GeoJSON) of one moving record.")
    public int track(@Parameters(index = "0", paramLabel = "SOURCE_ID") String sourceId,
            @Parameters(index = "1", paramLabel = "EXTERNAL_ID") String externalId,
            @Option(names = "--from", description = "ISO-8601 instant") String from,
            @Option(names = "--to", description = "ISO-8601 instant") String to,
            @Option(names = "--max-points", description = "2–10000") Integer maxPoints,
            @Option(names = "--tolerance", description = "auto or a number") String tolerance,
            @Option(names = "--gap", description = "Segment break, e.g. 10m") String gap,
            @Option(names = "--no-live", description = "Leave out the live position") boolean noLive) {
        Query q = Query.of().add("from", from).add("to", to).add("maxPoints", maxPoints).add("tolerance", tolerance)
                .add("gap", gap);
        if (noLive) {
            q.add("includeLive", "false");
        }
        root.printer().print(root.api().get("/v1/records/" + seg(sourceId) + "/" + seg(externalId) + "/track", q));
        return ExitCodes.OK;
    }
}
