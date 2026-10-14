package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import tools.jackson.databind.JsonNode;

/**
 * Fetches one AWC GeoJSON product source (prompt 087): its requests one after another (AWC asks clients to wait
 * between consecutive requests, and no AWC source runs requests in parallel), each mapped by its parser, merged with
 * the first record of each identity kept.
 *
 * <p><strong>Removal:</strong> only when every request of the run succeeded is the set complete; an identity that
 * was in the previous complete set and is missing now is tombstoned at once ({@link LiveIdSet}), so a canceled
 * SIGMET or CWA stops reading as current before its {@code validTimeTo}. A failed request fails the run before
 * anything is tombstoned.
 */
final class AwcProducts {

    /**
     * One request of a source.
     *
     * @param label what it reads, for messages ({@code airsigmet})
     * @param uri the request
     * @param parser maps its features
     */
    record Request(String label, URI uri, Function<List<JsonNode>, AwcHazardParser.Parsed> parser) {}

    private AwcProducts() {}

    /**
     * Runs the requests and returns the complete set with tombstones for vanished identities.
     *
     * @param ctx the fetch context
     * @param kind the record kind (the {@link LiveIdSet} scope)
     * @param requests the requests, run in order
     * @throws AdapterException when a request fails
     */
    static FetchResult fetch(FetchContext ctx, String kind, List<Request> requests) throws AdapterException {
        Map<String, NormalizedRecord> records = new LinkedHashMap<>();
        int skipped = 0;
        int noGeometry = 0;
        for (Request request : requests) {
            UpstreamResponse response = ctx.http().get(request.uri());
            List<JsonNode> features = AwcSupport.features(AwcSupport.body(response, "AWC " + request.label()),
                    "AWC " + request.label());
            AwcHazardParser.Parsed parsed = request.parser().apply(features);
            for (NormalizedRecord record : parsed.records()) {
                records.putIfAbsent(record.externalId(), record);
            }
            skipped += parsed.skipped();
            noGeometry += parsed.noGeometry();
        }
        List<NormalizedRecord> out = new ArrayList<>(records.values());
        for (String gone : LiveIdSet.absentSince(ctx.state(), kind, records.keySet())) {
            out.add(NormalizedRecord.tombstone(gone, kind, ctx.now()));
        }
        ctx.log().debug("{}: {} record(s), {} skipped, {} without geometry, {} tombstone(s)", ctx.sourceId(),
                records.size(), skipped, noGeometry, out.size() - records.size());
        return FetchResult.complete(out);
    }
}
