package ai.foresignal.federation.compact;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.FederationRules;
import ai.foresignal.federation.FederationWriter;
import ai.foresignal.federation.OriginLedger;
import ai.foresignal.federation.PeerDescriptors;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerStates;
import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.store.FetchTrigger;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * {@code POST /v1/federation/bundle} (prompt 113): decodes a CBOR array of compact messages from a peer into
 * {@value PeerDescriptors#REMOTE_EVENT_KIND} records on {@code peer-{id}-events}.
 *
 * <ul>
 *   <li>A message whose origin (key 10) is this instance is dropped ({@value FederationRules#DROP_LOOP}); one
 *       without a message id (key 11) is dropped as {@value FederationRules#DROP_SELECTION}.
 *   <li>Dedup through the {@link OriginLedger} on {@code urn:foresignal-compact:{origin}:{id}}, ordered by key 7
 *       and the type, so a resolution replaces the alert and a re-sent message counts as a duplicate.
 *   <li>A record carries reliability {@code F} (the source's), the quality flag {@code compact: true}, and its
 *       expiry (key 7 plus key 12) as {@code validUntil}; its license is {@code local-only-operator}, so received
 *       messages are never re-federated, and alerts on peer sources never enter the outbox.
 * </ul>
 */
@Component
public class CompactBundleReceiver {

    /** The license stamped on received compact messages: kept on this instance only. */
    public static final String LICENSE = "local-only-operator";

    private final PeerStates states;
    private final OriginLedger ledger;
    private final FederationWriter writer;
    private final FederationIdentity identity;
    private final SourceRegistry registry;
    private final Clock clock;

    /** Creates the receiver. */
    public CompactBundleReceiver(PeerStates states, OriginLedger ledger, FederationWriter writer,
            FederationIdentity identity, SourceRegistry registry, Clock clock) {
        this.states = states;
        this.ledger = ledger;
        this.writer = writer;
        this.identity = identity;
        this.registry = registry;
        this.clock = clock;
    }

    /**
     * Accepts a bundle from {@code peerId}.
     *
     * @return the {@code FederationIngestResult} document
     * @throws ValidationException malformed CBOR (400)
     */
    public Map<String, Object> receive(String peerId, byte[] body) {
        PeerEntity peer = states.find(peerId).orElseThrow(() -> NotFoundException.of("Peer", peerId));
        if (!peer.isEnabled()) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.SOURCE_DISABLED,
                    "Peer '" + peerId + "' is disabled.");
        }
        if (body.length > CompactBundles.MAX_BYTES) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                    "A bundle is at most " + CompactBundles.MAX_BYTES + " bytes.");
        }
        List<MeshCborMessage> messages;
        try {
            messages = CompactBundles.decode(body);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.MALFORMED_REQUEST, "Malformed CBOR bundle: " + e.getMessage(),
                    List.of());
        }
        String sourceId = PeerDescriptors.eventsId(peerId);
        if (!registry.contains(sourceId)) {
            throw NotFoundException.of("Source", sourceId);
        }
        byte[] self = identity.shortId();
        Map<String, Integer> dropped = new LinkedHashMap<>();
        int accepted = 0;
        int duplicates = 0;
        List<NormalizedRecord> records = new ArrayList<>();
        Instant now = clock.instant();
        for (MeshCborMessage message : messages) {
            if (message.originShortId() != null && Arrays.equals(message.originShortId(), self)) {
                dropped.merge(FederationRules.DROP_LOOP, 1, Integer::sum);
                continue;
            }
            if (message.messageId() == null) {
                dropped.merge(FederationRules.DROP_SELECTION, 1, Integer::sum);
                continue;
            }
            Instant expires = CompactMessages.expiresAt(message);
            if (expires != null && !now.isBefore(expires)) {
                dropped.merge(FederationRules.DROP_EXPIRED, 1, Integer::sum);
                continue;
            }
            String origin = CompactMessages.originHex(message);
            String id = CompactMessages.idHex(message);
            String externalId = "msg-" + (origin == null ? "unknown" : origin) + "-" + id;
            long order = (message.epochSeconds() == null ? 0 : message.epochSeconds()) * 16 + message.type();
            OriginLedger.Claim claim = ledger.claim("urn:foresignal-compact:" + origin + ":" + id, sourceId,
                    externalId, order, Set.of(sourceId));
            if (claim.verdict() == OriginLedger.Verdict.DUPLICATE) {
                duplicates++;
                continue;
            }
            if (claim.verdict() == OriginLedger.Verdict.STALE) {
                dropped.merge(FederationRules.DROP_DEDUP, 1, Integer::sum);
                continue;
            }
            records.add(record(message, externalId, origin, id, expires, now));
            accepted++;
        }
        if (!records.isEmpty()) {
            writer.submit(Map.of(sourceId, records), FetchTrigger.PUSH, "peer:" + peerId, false);
        }
        Map<String, Object> drops = new LinkedHashMap<>();
        for (String reason : List.of(FederationRules.DROP_LOOP, FederationRules.DROP_HOPS, FederationRules.DROP_DEDUP,
                FederationRules.DROP_LICENSE, FederationRules.DROP_PRIVATE, FederationRules.DROP_EXERCISE,
                FederationRules.DROP_EXPIRED)) {
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

    /** The {@code remote-event} record of one message. */
    static NormalizedRecord record(MeshCborMessage message, String externalId, String origin, String id,
            Instant expires, Instant now) {
        Instant at = message.epochSeconds() == null ? now : Instant.ofEpochSecond(message.epochSeconds());
        NormalizedRecord.Builder builder = NormalizedRecord.builder(externalId, PeerDescriptors.REMOTE_EVENT_KIND)
                .observedAt(at.isAfter(now) ? now : at)
                .eventTime(at)
                .validUntil(expires)
                .quality(Quality.none().withExtra("compact", true))
                .put("type", message.type())
                .put("typeName", typeName(message.type()))
                .put("state", message.type() == MeshCborProfile.TYPE_RESOLVED ? "resolved" : "open")
                .put("shortCode", message.shortCode())
                .put("severity", CompactMessages.severityWire(message.severity()))
                .put("eventKind", message.kind())
                .put("title", message.title())
                .put("originSourceId", message.sourceId())
                .put("originShortId", origin)
                .put("messageId", id);
        Map<String, Object> member = new LinkedHashMap<>();
        member.put(FederationRules.ORIGIN_REF, "urn:foresignal-compact:" + origin + ":" + id);
        member.put(FederationRules.ORIGIN_LICENSE, LICENSE);
        member.put("compact", true);
        builder.put(FederationRules.MEMBER, member);
        if (message.latitudeE5() != null && message.longitudeE5() != null) {
            builder.geometry(Geo.point(message.longitudeE5() / 1e5, message.latitudeE5() / 1e5));
        }
        return builder.build();
    }

    private static String typeName(int type) {
        return switch (type) {
            case MeshCborProfile.TYPE_ALERT -> "alert";
            case MeshCborProfile.TYPE_RESOLVED -> "resolved";
            case MeshCborProfile.TYPE_REPORT_ACK -> "report-ack";
            case MeshCborProfile.TYPE_STATUS -> "status";
            case MeshCborProfile.TYPE_INTEL_EVENT -> "intel-event";
            default -> "type-" + type;
        };
    }
}
