package ai.foresignal.federation;

import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.security.restrict.PlaceAreas;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The consuming side's rules for arriving federation items (prompt 113), shared by the PULL runtime and the
 * RECEIVE endpoint so both apply exactly the same checks, in this order:
 *
 * <ol>
 *   <li><strong>Loop prevention:</strong> a chain that names this instance ({@value FederationRules#DROP_LOOP}) or
 *       has {@code maxHops} or more entries ({@value FederationRules#DROP_HOPS}).
 *   <li><strong>Private:</strong> an item marked {@code private} is never stored ({@value
 *       FederationRules#DROP_PRIVATE}); the serving side never sends one.
 *   <li><strong>Selection, re-checked locally:</strong> sources, kinds, bbox, a place's polygon and {@code maxAge}
 *       ({@value FederationRules#DROP_SELECTION}, {@value FederationRules#DROP_EXPIRED}); an item of a kind the
 *       target source does not declare is dropped as selection too.
 *   <li><strong>License:</strong> a {@code FORBIDDEN} license is never stored; a {@code LOCAL_ONLY} or unknown one
 *       only from a same-operator peer ({@value FederationRules#DROP_LICENSE}).
 *   <li><strong>Dedup:</strong> the {@link OriginLedger} claim: duplicate, stale ({@value FederationRules#DROP_DEDUP})
 *       or applied into the origin's owner source.
 * </ol>
 *
 * <p>An applied item becomes a {@link NormalizedRecord}: local external id {@code {originSourceId}/{originExternalId}}
 * ({@link RecordRef#localExternalId}), kind, geometry, {@code eventTime}, explicit {@code validUntil} and quality
 * preserved, and the payload plus {@code _foresignal: {originRef, originSeq, originLicence, chain}} (volatile, so
 * never hashed). A remote tombstone becomes a local tombstone. Counts go to {@code foresignal.federation.items}
 * and {@code foresignal.federation.dropped}.
 */
@Component
public class FederationIngestor {

    /** The item metric. */
    public static final String ITEMS_METRIC = "foresignal.federation.items";

    /** The loop/hops/dedup metric. */
    public static final String DROPPED_METRIC = "foresignal.federation.dropped";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final FederationIdentity identity;
    private final OriginLedger ledger;
    private final LicenceCatalog catalog;
    private final SourceRegistry registry;
    private final JsonMapper json;
    private final ObjectProvider<MeterRegistry> meters;

    /** Creates the ingestor. */
    public FederationIngestor(FederationIdentity identity, OriginLedger ledger, LicenceCatalog catalog,
            SourceRegistry registry, JsonMapper json, ObjectProvider<MeterRegistry> meters) {
        this.identity = identity;
        this.ledger = ledger;
        this.catalog = catalog;
        this.registry = registry;
        this.json = json;
        this.meters = meters;
    }

    /**
     * Where and how a batch of items arrives.
     *
     * @param peerId the peer
     * @param standardSourceId {@code peer-{id}}
     * @param trackSourceId {@code peer-{id}-trk}, or null when the peer has none
     * @param selection the peer's selection
     * @param sameOperator the peer is the operator's own edge or hub: {@code LOCAL_ONLY} records are accepted
     * @param aliveSources the peer sources currently enabled (origin ownership)
     * @param snapshot true while a snapshot is replayed: a duplicate is still written so its head is touched
     */
    public record Context(String peerId, String standardSourceId, String trackSourceId, PeerSelection selection,
            boolean sameOperator, Set<String> aliveSources, boolean snapshot) {

        /** Copies the alive set. */
        public Context {
            aliveSources = aliveSources == null ? Set.of() : Set.copyOf(aliveSources);
        }

        /** The same context in or out of snapshot replay. */
        public Context withSnapshot(boolean value) {
            return new Context(peerId, standardSourceId, trackSourceId, selection, sameOperator, aliveSources, value);
        }
    }

    /**
     * One applied item.
     *
     * @param sourceId the local source to write into (the origin's owner)
     * @param record the record
     * @param remoteSeq the item's seq on the peer it came from
     * @param release what undoes the item's {@link OriginLedger} claim if the write fails, or null
     */
    public record Mapped(String sourceId, NormalizedRecord record, long remoteSeq, OriginLedger.Release release) {

        /** An applied item without a claim to undo. */
        public Mapped(String sourceId, NormalizedRecord record, long remoteSeq) {
            this(sourceId, record, remoteSeq, null);
        }
    }

    /**
     * What a batch did.
     *
     * @param records the applied records, in arrival order
     * @param accepted how many items were applied
     * @param duplicates how many were already applied at the same origin seq
     * @param dropped dropped items by reason
     * @param lastSeq the highest remote seq seen, or -1
     */
    public record Result(List<Mapped> records, int accepted, int duplicates, Map<String, Integer> dropped,
            long lastSeq) {

        /** The {@code FederationIngestResult} document. */
        public Map<String, Object> toJson() {
            Map<String, Object> drops = new LinkedHashMap<>();
            for (String reason : List.of(FederationRules.DROP_LOOP, FederationRules.DROP_HOPS,
                    FederationRules.DROP_DEDUP, FederationRules.DROP_LICENSE, FederationRules.DROP_PRIVATE,
                    FederationRules.DROP_EXERCISE, FederationRules.DROP_EXPIRED)) {
                drops.put(reason, dropped.getOrDefault(reason, 0));
            }
            if (dropped.containsKey(FederationRules.DROP_SELECTION)) {
                drops.put(FederationRules.DROP_SELECTION, dropped.get(FederationRules.DROP_SELECTION));
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("accepted", accepted);
            out.put("duplicates", duplicates);
            out.put("dropped", drops);
            return out;
        }
    }

    /** Applies the rules to {@code items} at {@code now}. */
    public Result process(Collection<JsonNode> items, Context context, Instant now) {
        List<Mapped> out = new ArrayList<>();
        Map<String, Integer> dropped = new LinkedHashMap<>();
        int accepted = 0;
        int duplicates = 0;
        long lastSeq = -1;
        String localInstance = identity.instanceId();
        for (JsonNode item : items) {
            long seq = item.path("seq").asLong(-1);
            lastSeq = Math.max(lastSeq, seq);
            String reason;
            try {
                Outcome outcome = one(item, context, now, localInstance);
                if (outcome.mapped() != null) {
                    out.add(outcome.mapped());
                    accepted++;
                    count(context.peerId(), "accepted");
                    continue;
                }
                if (outcome.duplicate()) {
                    duplicates++;
                    count(context.peerId(), "duplicate");
                    continue;
                }
                reason = outcome.dropReason();
            } catch (RuntimeException e) {
                reason = FederationRules.DROP_SELECTION;
            }
            dropped.merge(reason, 1, Integer::sum);
            count(context.peerId(), reason);
            if (FederationRules.DROP_LOOP.equals(reason) || FederationRules.DROP_HOPS.equals(reason)
                    || FederationRules.DROP_DEDUP.equals(reason)) {
                MeterRegistry registry = meters == null ? null : meters.getIfAvailable();
                if (registry != null) {
                    registry.counter(DROPPED_METRIC, "reason", reason).increment();
                }
            }
        }
        return new Result(out, accepted, duplicates, dropped, lastSeq);
    }

    /**
     * Undoes the ledger claims of {@code mapped} after their write failed, so a re-send of the same versions is
     * applied rather than counted as duplicates (checkpoint 124).
     */
    public void release(Collection<Mapped> mapped) {
        for (Mapped m : mapped) {
            ledger.release(m.release());
        }
    }

    /** The outcome of one item: a record, a duplicate or a drop reason. */
    private record Outcome(Mapped mapped, boolean duplicate, String dropReason) {

        static Outcome drop(String reason) {
            return new Outcome(null, false, reason);
        }
    }

    private Outcome one(JsonNode item, Context context, Instant now, String localInstance) {
        String remoteSource = text(item, "sourceId");
        String remoteExternal = text(item, "externalId");
        String kind = text(item, "kind");
        long seq = item.path("seq").asLong(-1);
        if (remoteSource == null || remoteExternal == null || kind == null || seq < 0) {
            return Outcome.drop(FederationRules.DROP_SELECTION);
        }
        JsonNode chainNode = item.path("provenance").path("chain");
        List<Map<String, Object>> chain = chainNode.isArray()
                ? FederationRules.chain(json.convertValue(chainNode, Object.class)) : new ArrayList<>();
        if (chain.isEmpty()) {
            RecordRef ref = refOf(item);
            if (ref == null) {
                return Outcome.drop(FederationRules.DROP_SELECTION);
            }
            chain = List.of(FederationRules.hop(ref.instanceId(), remoteSource, remoteExternal, seq,
                    instant(item, "observedAt")));
        }
        String loop = FederationRules.loopReason(chain, localInstance, context.selection().maxHops());
        if (loop != null) {
            return Outcome.drop(loop);
        }
        if (item.path("private").asBoolean(false)) {
            return Outcome.drop(FederationRules.DROP_PRIVATE);
        }
        PeerSelection selection = context.selection();
        if (!selection.sources().isEmpty() && !selection.sources().contains(remoteSource)) {
            return Outcome.drop(FederationRules.DROP_SELECTION);
        }
        if (!selection.kinds().isEmpty() && !selection.kinds().contains(kind)) {
            return Outcome.drop(FederationRules.DROP_SELECTION);
        }
        boolean tombstone = item.path("tombstone").asBoolean(false);
        Geometry geometry = tombstone ? null : geometry(item.get("geometry"));
        if (geometry != null && !inArea(geometry, selection)) {
            return Outcome.drop(FederationRules.DROP_SELECTION);
        }
        Instant observedAt = instant(item, "observedAt");
        if (!tombstone && selection.maxAge() != null && observedAt != null
                && observedAt.isBefore(now.minus(selection.maxAge()))) {
            return Outcome.drop(FederationRules.DROP_EXPIRED);
        }
        String license = text(item, "licence");
        LicenceTerms terms = catalog.terms(license == null ? LicenceCatalog.UNKNOWN : license);
        if (terms.forbidsRedistribution() || (terms.localOnly() && !context.sameOperator())) {
            return Outcome.drop(FederationRules.DROP_LICENSE);
        }
        String target = targetSource(item, context);
        if (target == null || !registry.contains(target) || !registry.descriptor(target).kinds().contains(kind)) {
            return Outcome.drop(FederationRules.DROP_SELECTION);
        }
        Map<String, Object> origin = FederationRules.origin(chain);
        String originRef = FederationRules.originRef(origin);
        long originSeq = FederationRules.seqOf(origin);
        if (originRef == null || originSeq < 0) {
            return Outcome.drop(FederationRules.DROP_SELECTION);
        }
        String originSource = String.valueOf(origin.get("sourceId"));
        String originExternal = String.valueOf(origin.get("externalId"));
        String localExternalId = RecordRef.localExternalId(originSource, originExternal);
        OriginLedger.Claim claim = ledger.claim(originRef, target, localExternalId, originSeq,
                context.aliveSources());
        String owner = claim.ownerSourceId();
        if (claim.verdict() == OriginLedger.Verdict.DUPLICATE) {
            if (!context.snapshot()) {
                return new Outcome(null, true, null);
            }
            String current = ledger.owner(originRef);
            owner = current == null ? target : current;
        } else if (claim.verdict() == OriginLedger.Verdict.STALE) {
            return Outcome.drop(FederationRules.DROP_DEDUP);
        }
        if (!registry.contains(owner) || !registry.descriptor(owner).kinds().contains(kind)) {
            owner = target;
        }
        String localId = claim.localExternalId() == null ? localExternalId : claim.localExternalId();
        NormalizedRecord record;
        if (tombstone) {
            record = NormalizedRecord.tombstone(localId, kind, observedAt == null ? now : observedAt);
        } else {
            Map<String, Object> member = new LinkedHashMap<>();
            member.put(FederationRules.ORIGIN_REF, originRef);
            member.put(FederationRules.ORIGIN_SEQ, originSeq);
            member.put(FederationRules.ORIGIN_LICENSE, terms.id());
            member.put(FederationRules.CHAIN, chain);
            if (RecordRef.hashed(localId)) {
                member.put("originSourceId", originSource);
                member.put(FederationRules.ORIGIN_EXTERNAL_ID, originExternal);
            }
            NormalizedRecord.Builder builder = NormalizedRecord.builder(localId, kind)
                    .observedAt(observedAt == null ? now : observedAt)
                    .eventTime(instant(item, "eventTime"))
                    .validUntil(instant(item, "validUntil"))
                    .geometry(geometry)
                    .quality(quality(item.get("quality")))
                    .putAll(payload(item.get("payload")))
                    .put(FederationRules.MEMBER, member);
            record = builder.build();
        }
        return new Outcome(new Mapped(owner, record, seq, claim.release()), false, null);
    }

    /** The local source for an item: the TRACK source for a TRACK item when the peer has one. */
    static String targetSource(JsonNode item, Context context) {
        boolean track = HistoryTier.TRACK.name().equalsIgnoreCase(text(item, "historyTier"));
        return track && context.trackSourceId() != null ? context.trackSourceId() : context.standardSourceId();
    }

    private static boolean inArea(Geometry geometry, PeerSelection selection) {
        Envelope envelope = geometry.getEnvelopeInternal();
        if (selection.bbox() != null) {
            List<Double> b = selection.bbox();
            if (envelope.getMaxX() < b.get(0) || envelope.getMinX() > b.get(2) || envelope.getMaxY() < b.get(1)
                    || envelope.getMinY() > b.get(3)) {
                return false;
            }
        }
        if (selection.placeId() != null) {
            Geometry area = PlaceAreas.resolve(List.of(selection.placeId()));
            return area != null && !area.isEmpty() && area.intersects(geometry);
        }
        return true;
    }

    private RecordRef refOf(JsonNode item) {
        String ref = text(item, "ref");
        if (ref == null) {
            return null;
        }
        try {
            return RecordRef.parse(ref);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Geometry geometry(JsonNode node) {
        if (node == null || node.isNull() || !node.isObject()) {
            return null;
        }
        return new GeoJsonReader().read(node);
    }

    /** The payload without the sender's {@code _foresignal} member and without top-level nulls. */
    private Map<String, Object> payload(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        Map<String, Object> parsed = json.convertValue(node, MAP);
        Map<String, Object> out = new LinkedHashMap<>();
        parsed.forEach((key, value) -> {
            if (value != null && !FederationRules.MEMBER.equals(key)) {
                out.put(key, value);
            }
        });
        return out;
    }

    /** The item's quality ({@code accuracyMeters}, {@code confidence}, {@code positionSource}, {@code extra}). */
    @SuppressWarnings("unchecked")
    static Quality quality(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Quality.none();
        }
        Double accuracy = node.path("accuracyMeters").isNumber() ? node.path("accuracyMeters").asDouble() : null;
        Double confidence = node.path("confidence").isNumber() ? node.path("confidence").asDouble() : null;
        PositionSource position = null;
        String wire = node.path("positionSource").isString() ? node.path("positionSource").asString() : null;
        if (wire != null) {
            try {
                position = PositionSource.valueOf(wire.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                position = PositionSource.UNKNOWN;
            }
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        JsonNode raw = node.get("extra");
        if (raw != null && raw.isObject()) {
            for (String name : raw.propertyNames()) {
                JsonNode value = raw.get(name);
                if (value != null && !value.isNull()) {
                    extra.put(name, value.isNumber() ? value.numberValue() : value.isBoolean() ? value.asBoolean()
                            : value.isString() ? value.asString() : value.toString());
                }
            }
        }
        return new Quality(accuracy, confidence, position, extra);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isString() && !value.asString().isBlank() ? value.asString() : null;
    }

    private static Instant instant(JsonNode node, String field) {
        String text = text(node, field);
        if (text == null) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            try {
                return java.time.OffsetDateTime.parse(text).toInstant();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private void count(String peerId, String result) {
        MeterRegistry registry = meters == null ? null : meters.getIfAvailable();
        if (registry != null) {
            registry.counter(ITEMS_METRIC, "peer", peerId == null ? "unknown" : peerId, "result", result).increment();
        }
    }
}
