package ai.foresignal.federation;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.query.FederationQueries;
import ai.foresignal.store.query.RecordRow;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The serving side of federation (prompt 113): the change feed, the snapshot and versions by seq as
 * {@code FederationItem}s, each with its license id and provenance chain.
 *
 * <p><strong>Egress, per record, before serialization.</strong> Every row goes through the one
 * {@link EgressPolicy#decide(EgressPath, String, LicenceTerms, Map, EgressPolicy.EgressContext)} on
 * {@link EgressPath#FEDERATION}, with the record's resolved license ({@link LicenceResolver}: the batch license, or
 * a peer record's {@code _foresignal.originLicence}) and its payload (so MLAT positions never leave). FORBIDDEN and
 * {@code LOCAL_ONLY} licenses and sources ({@code opensky-states}, {@code acled}, {@code aisstream}, local
 * receivers) and private sources (039) are excluded; private and {@code LOCAL_ONLY} sources are already pruned in
 * SQL. Exercise records ({@code "exercise": true}, 115) are excluded unless the token sets
 * {@code includeExercise}. Each exclusion counts in {@code foresignal.federation.denied{reason}}.
 *
 * <p>The one documented exception is an edge relay pushing to its operator's own hub ({@link Caller#sameOperator}):
 * {@code LOCAL_ONLY} records may then leave, labelled with the {@value #LOCAL_ONLY_LICENSE} license so the hub
 * keeps them {@code LOCAL_ONLY} and never re-federates them.
 */
@Component
public class FederationFeedService {

    /** The largest page. */
    public static final int MAX_LIMIT = 1_000;

    /** The license id a same-operator push stamps on a record that is {@code LOCAL_ONLY} here. */
    public static final String LOCAL_ONLY_LICENSE = "local-only-operator";

    /** The denial metric. */
    public static final String DENIED_METRIC = "foresignal.federation.denied";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final FederationQueries queries;
    private final EgressPolicy egress;
    private final LicenceResolver licenses;
    private final FederationIdentity identity;
    private final SourceRegistry registry;
    private final SourceVisibility visibility;
    private final JsonMapper json;
    private final ObjectProvider<MeterRegistry> meters;
    private final java.time.Clock clock;

    /** Creates the service. */
    public FederationFeedService(FederationQueries queries, EgressPolicy egress, LicenceResolver licenses,
            FederationIdentity identity, SourceRegistry registry, SourceVisibility visibility, JsonMapper json,
            ObjectProvider<MeterRegistry> meters, java.time.Clock clock) {
        this.queries = queries;
        this.egress = egress;
        this.licenses = licenses;
        this.identity = identity;
        this.registry = registry;
        this.visibility = visibility;
        this.json = json;
        this.meters = meters;
        this.clock = clock;
    }

    /**
     * Who receives the items.
     *
     * @param restriction the token's read restriction (sources, kinds, bbox, area still apply)
     * @param includeExercise the token's {@code includeExercise}
     * @param sameOperator an edge relay pushing to its operator's own hub (the {@code LOCAL_ONLY} exception)
     */
    public record Caller(ReadRestriction restriction, boolean includeExercise, boolean sameOperator) {

        /** Normalizes a null restriction. */
        public Caller {
            restriction = restriction == null ? ReadRestriction.NONE : restriction;
        }

        /** A push sink's caller: no restriction, exercise records included, with or without the exception. */
        public static Caller push(boolean sameOperator) {
            return new Caller(ReadRestriction.NONE, true, sameOperator);
        }
    }

    /**
     * A page of the change feed.
     *
     * @param items the items allowed to leave, in seq order
     * @param lastSeq pass as {@code after} next time (covers excluded rows too)
     * @param hasMore true when more versions follow
     * @param watermarkSeq the watermark read before the query
     */
    public record Page(List<Map<String, Object>> items, long lastSeq, boolean hasMore, long watermarkSeq) {}

    /**
     * A snapshot page.
     *
     * @param items the current heads allowed to leave
     * @param atSeq the pinned watermark: resume the change feed from here
     * @param nextCursor the next page's cursor, or null on the last page
     */
    public record SnapshotPage(List<Map<String, Object>> items, long atSeq, String nextCursor) {}

    /**
     * The versions after {@code after}.
     *
     * @throws ForesignalException {@code 410 RESYNC_REQUIRED} below the purge horizon
     */
    public Page changes(long after, int limit, List<String> sources, List<String> kinds, BboxFilter bbox,
            Caller caller) {
        FederationQueries.Filter filter = filter(sources, kinds, bbox, caller);
        long horizon = queries.purgeHorizon(filter.sources());
        if (after < horizon) {
            throw resync(after, horizon, queries.watermark());
        }
        FederationQueries.ChangeRows rows = queries.changes(after, Math.max(1, Math.min(MAX_LIMIT, limit)), filter);
        return new Page(serve(rows.rows(), caller, false), rows.lastSeq(), rows.hasMore(), rows.watermarkSeq());
    }

    /**
     * A snapshot page of current heads pinned to {@code atSeq} (the current watermark on the first page).
     *
     * @throws ForesignalException {@code 410 RESYNC_REQUIRED} when the pinned watermark is below the purge horizon
     */
    public SnapshotPage snapshot(Long atSeq, String cursor, int limit, List<String> sources, List<String> kinds,
            BboxFilter bbox, Caller caller) {
        FederationQueries.Filter filter = filter(sources, kinds, bbox, caller);
        String fingerprint = CursorCodec.fingerprint("federation-snapshot|" + String.join(",", filter.sources())
                + "|" + String.join(",", filter.kinds()) + "|" + (bbox == null ? "" : bbox.toString()));
        Cursor.ById position = CursorCodec.decode(cursor, Cursor.ById.class, fingerprint);
        long pinned = position != null && position.w() != null ? position.w()
                : atSeq != null ? atSeq : queries.watermark();
        long horizon = queries.purgeHorizon(filter.sources());
        if (pinned < horizon) {
            throw resync(pinned, horizon, queries.watermark());
        }
        FederationQueries.SnapshotRows rows = queries.snapshot(pinned, position == null ? null : position.s(),
                position == null ? null : position.e(), Math.max(1, Math.min(MAX_LIMIT, limit)), filter,
                clock.instant());
        String next = null;
        if (rows.hasMore() && !rows.rows().isEmpty()) {
            RecordRow last = rows.rows().getLast();
            next = CursorCodec.encode(new Cursor.ById(last.sourceId(), last.externalId(), pinned), fingerprint);
        }
        return new SnapshotPage(serve(rows.rows(), caller, true), pinned, next);
    }

    /** The items of {@code seqs} allowed to leave for {@code caller}, in seq order (the push sink's read). */
    public List<Map<String, Object>> itemsBySeq(Collection<Long> seqs, Caller caller) {
        List<RecordRow> rows = queries.bySeq(seqs);
        List<RecordRow> kept = new ArrayList<>(rows.size());
        Set<String> excluded = excludedSources(caller);
        for (RecordRow row : rows) {
            if (excluded.contains(row.sourceId())) {
                denied(reasonOfExclusion(row.sourceId()));
            } else {
                kept.add(row);
            }
        }
        return serve(kept, caller, false);
    }

    /** The current watermark. */
    public long watermark() {
        return queries.watermark();
    }

    /** The row filter of a caller: private sources, and {@code LOCAL_ONLY} sources unless same-operator, pruned. */
    FederationQueries.Filter filter(List<String> sources, List<String> kinds, BboxFilter bbox, Caller caller) {
        return new FederationQueries.Filter(sources, kinds, bbox, excludedSources(caller), caller.restriction());
    }

    /** The sources never served to {@code caller}: private ones, and {@code LOCAL_ONLY} ones unless same-operator. */
    Set<String> excludedSources(Caller caller) {
        Set<String> out = new HashSet<>(visibility.privateSourceIds());
        if (!caller.sameOperator()) {
            for (String id : registry.ids()) {
                if (egress.isLocalOnly(id)) {
                    out.add(id);
                }
            }
        }
        return out;
    }

    private String reasonOfExclusion(String sourceId) {
        return visibility.isPrivate(sourceId) ? EgressPolicy.REASON_PRIVATE : EgressPolicy.REASON_LOCAL_ONLY;
    }

    /**
     * The items of {@code rows} allowed to leave for {@code caller}; each refusal counts in
     * {@value #DENIED_METRIC}.
     *
     * @param snapshot true for snapshot rows ({@code persistReason: SNAPSHOT})
     */
    List<Map<String, Object>> serve(List<RecordRow> rows, Caller caller, boolean snapshot) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, LicenceResolver.Resolution> resolved = licenses.resolveAll(rows);
        EgressPolicy.EgressContext context = EgressPolicy.EgressContext.federation(caller.sameOperator());
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (RecordRow row : rows) {
            Map<String, Object> payload = parse(row.payload());
            LicenceResolver.Resolution resolution = resolved.get(row.seq());
            LicenceTerms terms = resolution == null ? null : resolution.terms();
            EgressPolicy.Decision decision = egress.decide(EgressPath.FEDERATION, row.sourceId(), terms, payload,
                    context);
            if (!decision.allowed()) {
                denied(decision.reason());
                continue;
            }
            if (!caller.includeExercise() && FederationRules.isExercise(payload)) {
                denied(FederationRules.DROP_EXERCISE);
                continue;
            }
            String license = decision.terms() != null ? decision.terms().id() : terms != null ? terms.id()
                    : egress.sourceTerms(row.sourceId()).id();
            if (caller.sameOperator()) {
                EgressPolicy.Decision strict = egress.decide(EgressPath.FEDERATION, row.sourceId(), terms, payload,
                        EgressPolicy.EgressContext.federation(false));
                if (!strict.allowed()) {
                    // Allowed only by the same-operator exception: the hub must keep it LOCAL_ONLY.
                    license = LOCAL_ONLY_LICENSE;
                }
            }
            out.add(item(row, payload, license, resolution == null ? null : resolution.basis(), snapshot));
        }
        return out;
    }

    /** One {@code FederationItem}. */
    Map<String, Object> item(RecordRow row, Map<String, Object> payload, String license, String basis,
            boolean snapshot) {
        String instanceId = identity.instanceId();
        Map<String, Object> self = FederationRules.hop(instanceId, row.sourceId(), row.externalId(), row.seq(),
                row.ingestedAt());
        List<Map<String, Object>> chain = FederationRules.servedChain(FederationRules.storedChain(payload), self);
        Map<String, Object> provenance = new LinkedHashMap<>();
        provenance.put("origin", FederationRules.origin(chain));
        provenance.put("chain", chain);
        provenance.put("sourceIds", List.of(row.sourceId()));
        if (basis != null) {
            provenance.put("licenceBasis", basis);
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("seq", row.seq());
        item.put("ref", RecordRef.format(instanceId, row.sourceId(), row.externalId()));
        item.put("sourceId", row.sourceId());
        item.put("externalId", row.externalId());
        item.put("kind", row.kind());
        item.put("version", row.version());
        item.put("persistReason", snapshot ? "SNAPSHOT"
                : row.persistReason() == null ? null : PersistReason.fromCode(row.persistReason()).name());
        item.put("historyTier", tierOf(row));
        item.put("observedAt", row.observedAt() == null ? null : row.observedAt().toString());
        item.put("eventTime", row.eventTime() == null ? null : row.eventTime().toString());
        item.put("validUntil", row.validUntil() == null ? null : row.validUntil().toString());
        item.put("tombstone", row.tombstone());
        item.put("geometry", parseAny(row.geometry()));
        item.put("payload", FederationRules.withoutMember(payload));
        item.put("quality", parseAny(row.quality()));
        item.put("licence", license);
        item.put("provenance", provenance);
        return item;
    }

    /** The row's history tier name, from the stored code or else the source's descriptor. */
    private String tierOf(RecordRow row) {
        if (row.historyTier() != null) {
            return HistoryTier.fromCode(row.historyTier()).name();
        }
        return registry.contains(row.sourceId()) ? registry.descriptor(row.sourceId()).historyTier().name()
                : HistoryTier.STANDARD.name();
    }

    private void denied(String reason) {
        MeterRegistry registry = meters == null ? null : meters.getIfAvailable();
        if (registry != null) {
            registry.counter(DENIED_METRIC, "reason", reason == null ? "denied" : reason).increment();
        }
    }

    private Map<String, Object> parse(String text) {
        if (text == null || text.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> map = json.readValue(text, MAP);
            return map == null ? new LinkedHashMap<>() : map;
        } catch (JacksonException e) {
            return new LinkedHashMap<>();
        }
    }

    private Object parseAny(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return json.readValue(text, Object.class);
        } catch (JacksonException e) {
            return null;
        }
    }

    /** {@code 410 RESYNC_REQUIRED}, as {@code /v1/changes} answers it. */
    static ForesignalException resync(long after, long horizon, long watermark) {
        return new ForesignalException(HttpStatus.GONE, ErrorCodes.RESYNC_REQUIRED,
                "Changes after seq " + after + " are no longer complete: retention purged entities at seq " + horizon
                        + ". Re-snapshot with GET /v1/federation/snapshot, then resume from its atSeq.")
                .withProperty("purgeHorizon", horizon)
                .withProperty("watermarkSeq", watermark);
    }
}
