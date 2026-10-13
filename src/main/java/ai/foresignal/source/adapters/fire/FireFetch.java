package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import tools.jackson.databind.JsonNode;

/**
 * The rules the fire feature sources share (prompt 075): features mapped one by one, skips counted, and absence
 * tombstones only after complete fetches.
 *
 * <ul>
 *   <li>{@link #map}: each feature becomes a record or a skip reason; a repeated id keeps its first feature
 *       ({@value MappedRecord#DUPLICATE_ID}).
 *   <li>{@link #result}: a fetch the client cut short (page or feature cap) is partial and tombstones nothing
 *       ({@link AbsenceTombstoner.Outcome#TRUNCATED}). A complete fetch of a source with absence tombstones runs
 *       {@link AbsenceTombstoner}, so a feature missing from two consecutive complete fetches is tombstoned. A
 *       well-formed empty layer counts as complete: fire layers are legitimately empty out of season, and two
 *       consecutive empty fetches are still needed before anything is retired.
 *   <li>Skips are counted in {@value #SKIPPED_METRIC}, tagged {@code source} and {@code reason}.
 * </ul>
 */
final class FireFetch {

    /** The counter of features the fire mappers skipped. */
    static final String SKIPPED_METRIC = "foresignal.fire.skipped";

    private FireFetch() {}

    /**
     * Mapped features.
     *
     * @param records the records, one per id, in feature order
     * @param skipped skip reason to count
     */
    record Batch(List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    /**
     * Maps every feature.
     *
     * @param features the features, in the order that wins on a repeated id
     * @param mapper the pure per-feature mapper
     */
    static Batch map(Iterable<JsonNode> features, Function<JsonNode, MappedRecord> mapper) {
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode feature : features) {
            MappedRecord mapped = mapper.apply(feature);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
            } else if (ids.add(mapped.record().externalId())) {
                records.add(mapped.record());
            } else {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
            }
        }
        return new Batch(records, skipped);
    }

    /**
     * The result of one fetch (see the class note).
     *
     * @param fetch what the client fetched
     * @param batch its mapped features
     * @param kind the records' kind, for tombstones
     * @param tombstones true when the source tombstones absent features
     * @param meters supplies the registry of the skip counter, or null
     */
    static FetchResult result(FetchContext ctx, FeatureFetch fetch, Batch batch, String kind, boolean tombstones,
            Supplier<MeterRegistry> meters) {
        SkipCounter.count(meters, SKIPPED_METRIC, "Fire features the mappers skipped", ctx.sourceId(),
                batch.skipped());
        ctx.log().debug("{}: {} feature(s) in {} page(s) ({}), {} record(s), skipped {}", ctx.sourceId(),
                fetch.size(), fetch.pages(), fetch.stopReason(), batch.records().size(), batch.skipped());
        if (!fetch.complete()) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.TRUNCATED, List.of());
            ctx.log().warn("{}: the fetch stopped early ({}); returning a partial set and tombstoning nothing",
                    ctx.sourceId(), fetch.stopReason());
            return FetchResult.of(batch.records());
        }
        if (!tombstones) {
            return FetchResult.complete(batch.records());
        }
        List<String> gone = AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.COMPLETE,
                batch.records().stream().map(NormalizedRecord::externalId).toList());
        List<NormalizedRecord> out = new ArrayList<>(batch.records());
        out.addAll(AbsenceTombstoner.tombstones(gone, kind, ctx.now()));
        if (!gone.isEmpty()) {
            ctx.log().debug("{}: tombstoned {} feature(s) missing from two complete fetches", ctx.sourceId(),
                    gone.size());
        }
        return FetchResult.complete(out);
    }
}
