package ai.foresignal.chronicle.network;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.ShareService;
import ai.foresignal.exchange.ExchangePackageEntity;
import ai.foresignal.exchange.ExchangeService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.report.ReportDrafter;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportPublisher;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Access requests on chronicle links (prompt 134): an analyst who was shown a related chronicle asks its owner for
 * access without revealing its own work.
 *
 * <ul>
 *   <li><b>Request</b> ({@code requestChronicleAccess}): the owner receives only the requester's handle, the message
 *       and, if the requester chose, an offer to share one of its own chronicles (the requester must own it). Local
 *       owners are notified ({@code chronicle.access.requested}); remote ones through the peer channel. At most 10
 *       requests per principal per day ({@code 429 REQUEST_RATE_LIMITED}); a repeat to an owner who declined the same
 *       requester within 30 days is stored as suppressed and never shown to the owner.</li>
 *   <li><b>Grant</b>: on this instance, a 130 access-list grant (viewer or commenter, TLP checked) and, when offered,
 *       the requester's own chronicle shared back with the owner; for a remote requester, a 132 signed package of a
 *       chronicle extract or a chronicle-recap report addressed to the requester's handle on its instance.</li>
 *   <li><b>Decline</b> or ignore. The requester is told of a grant or a decline only.</li>
 * </ul>
 */
@Service
public class ChronicleAccessService {

    private static final Logger log = LoggerFactory.getLogger(ChronicleAccessService.class);

    private static final Set<String> REQUEST_FIELDS = Set.of("message", "offerShare");

    private static final Set<String> GRANT_FIELDS = Set.of("role", "expiresAt", "deliverAs", "message");

    /** The most incoming remote requests accepted from one instance per day. */
    static final int REMOTE_REQUESTS_PER_INSTANCE_PER_DAY = 100;

    private final ChronicleAccessRequestRepository requests;
    private final ChronicleLinkRepository links;
    private final ChronicleLinkViews views;
    private final ChronicleRepository chronicles;
    private final ChronicleKeyStore store;
    private final NetworkSalts salts;
    private final ShareService shares;
    private final FederatedSketches federated;
    private final ChronicleNetworkNotices notices;
    private final PrincipalService principals;
    private final ObjectProvider<ExchangeService> exchange;
    private final ObjectProvider<ReportDrafter> drafter;
    private final ObjectProvider<ReportPublisher> publisher;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public ChronicleAccessService(ChronicleAccessRequestRepository requests, ChronicleLinkRepository links,
            ChronicleLinkViews views, ChronicleRepository chronicles, ChronicleKeyStore store, NetworkSalts salts,
            ShareService shares, FederatedSketches federated, ChronicleNetworkNotices notices,
            PrincipalService principals, ObjectProvider<ExchangeService> exchange,
            ObjectProvider<ReportDrafter> drafter, ObjectProvider<ReportPublisher> publisher, JsonMapper json,
            Clock clock) {
        this.requests = requests;
        this.links = links;
        this.views = views;
        this.chronicles = chronicles;
        this.store = store;
        this.salts = salts;
        this.shares = shares;
        this.federated = federated;
        this.notices = notices;
        this.principals = principals;
        this.exchange = exchange;
        this.drafter = drafter;
        this.publisher = publisher;
        this.json = json;
        this.clock = clock;
    }

    /** {@code 404 UNKNOWN_LINK}. */
    static NotFoundException unknownLink(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_LINK, "No chronicle link '" + id + "' exists, or you may not"
                + " learn of it.");
    }

    /** {@code 404 UNKNOWN_ACCESS_REQUEST}. */
    static NotFoundException unknownRequest(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_ACCESS_REQUEST, "No access request '" + id + "' is addressed"
                + " to you or was sent by you.");
    }

    // ------------------------------------------------------------------------- request

    /**
     * Requests access to the other chronicle of a link ({@code requestChronicleAccess}).
     *
     * @param linkId the link
     * @param body the {@code AccessRequestInput}, or null
     * @param caller the requester
     * @return the {@code AccessRequest} (outgoing)
     * @throws ForesignalException {@code 404 UNKNOWN_LINK}, {@code 409 CHRONICLE_ALREADY_VISIBLE},
     *     {@code 429 REQUEST_RATE_LIMITED}
     */
    public Map<String, Object> request(String linkId, JsonNode body, ObjectCaller caller) {
        String principal = requirePrincipal(caller);
        ChronicleLinkEntity link = links.findById(linkId == null ? "" : linkId).orElseThrow(() -> unknownLink(linkId));
        String side = null;
        for (String s : List.of(link.getA(), link.getB())) {
            if (!NetworkVocabulary.isRemote(s) && views.sees(caller, s)) {
                side = s;
                break;
            }
        }
        if (side == null) {
            throw unknownLink(linkId);
        }
        Disclosure.Level level = views.level(link, side, caller);
        if (level == Disclosure.Level.FULL) {
            throw new ConflictException(ErrorCodes.CHRONICLE_ALREADY_VISIBLE, "You can already see both chronicles of"
                    + " this link.");
        }
        if (level == Disclosure.Level.NONE || !views.allowOpaque(caller, link.getId(), false)) {
            throw unknownLink(linkId);
        }
        String message = null;
        String offer = null;
        if (body != null && !body.isNull()) {
            CollabInputs.object(body, "AccessRequestInput", REQUEST_FIELDS);
            message = CollabInputs.text(body, "message", 0, NetworkVocabulary.MAX_MESSAGE);
            offer = CollabInputs.text(body, "offerShare", 1, 32);
        }
        String offerId = null;
        if (offer != null) {
            final String wanted = offer;
            ChronicleEntity offered = chronicles.findById(wanted).or(() -> chronicles.findByDisplayId(wanted))
                    .orElseThrow(() -> ValidationException.forField("offerShare", "names no chronicle of yours"));
            if (views.role(caller, offered.getId()) != AclRole.OWNER) {
                throw ValidationException.forField("offerShare", "must be a chronicle you own");
            }
            offerId = offered.getId();
        }
        Instant now = clock.instant();
        if (!AntiProbing.mayRequest(requests.countByRequesterPrincipalAndCreatedAtAfter(principal,
                now.minus(Duration.ofDays(1))))) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.REQUEST_RATE_LIMITED,
                    "You sent " + NetworkVocabulary.REQUESTS_PER_DAY + " access requests in the last 24 hours; try"
                            + " again later.").withProperty("retryAfterSeconds", 3600);
        }
        String handle = principals.find(principal).map(PrincipalEntity::getHandle).orElse(principal);
        String other = link.other(side);
        ChronicleAccessRequestEntity r = new ChronicleAccessRequestEntity(Ulids.next(now), link.getId(), handle, now);
        r.setRequesterPrincipal(principal);
        r.setMessage(message);
        r.setOfferShare(offerId);
        Instant since = now.minus(NetworkVocabulary.DECLINE_SUPPRESSION);
        if (NetworkVocabulary.isRemote(other)) {
            ChronicleRemoteSketchEntity sketch = views.remote(other).orElseThrow(() -> unknownLink(linkId));
            r.setTargetNode(other);
            r.setNetworkId(sketch.getNetworkId());
            r.setSuppressed(!requests.findByRequesterPrincipalAndTargetNodeAndStatusAndDecidedAtAfter(principal, other,
                    NetworkVocabulary.DECLINED, since).isEmpty());
            requests.save(r);
            if (!r.isSuppressed()) {
                Map<String, Object> relay = new LinkedHashMap<>();
                relay.put("requestId", r.getId());
                relay.put("linkId", link.getId());
                relay.put("sketchId", sketch.getSketchId());
                relay.put("requesterHandle", handle);
                relay.put("message", message);
                relay.put("offerShare", offerId != null);
                federated.relay(sketch.getInstanceId(), sketch.getNetworkId(), "accessRequests", List.of(relay));
            }
        } else {
            String owner = views.owner(other);
            r.setTargetChronicleId(other);
            r.setOwnerPrincipal(owner);
            r.setSuppressed(owner != null && !requests.findByRequesterPrincipalAndOwnerPrincipalAndStatusAndDecidedAtAfter(
                    principal, owner, NetworkVocabulary.DECLINED, since).isEmpty());
            requests.save(r);
            if (!r.isSuppressed()) {
                notices.requested(r);
            }
        }
        return view(r, "outgoing");
    }

    /**
     * Stores a request relayed by another instance for one of this instance's sketches.
     *
     * @param node the {@code accessRequests} entry
     * @param instanceId the requester's instance
     * @param networkId the network
     */
    public void receiveRemoteRequest(JsonNode node, String instanceId, String networkId) {
        try {
            String remoteId = text(node, "requestId");
            String sketchId = text(node, "sketchId");
            String handle = text(node, "requesterHandle");
            if (remoteId == null || !Ulids.isUlid(remoteId) || sketchId == null || handle == null
                    || handle.length() > 64) {
                return;
            }
            if (requests.findByRequesterInstanceAndRemoteRequestId(instanceId, remoteId).isPresent()) {
                return;
            }
            String saltId = salts.networkSalt(networkId).map(NetworkSalts.Salt::id).orElse(null);
            ChronicleKeyStore.State state = store.stateBySketchId(sketchId).orElse(null);
            if (saltId == null || state == null || !saltId.equals(state.saltId())) {
                return;
            }
            ChronicleEntity target = chronicles.findById(state.chronicleId()).orElse(null);
            if (target == null || views.effective(target) != DiscoverabilityLevel.NETWORK) {
                return;
            }
            Instant now = clock.instant();
            long recent = requests.findByTargetChronicleId(target.getId()).stream()
                    .filter(x -> instanceId.equals(x.getRequesterInstance())
                            && x.getCreatedAt().isAfter(now.minus(Duration.ofDays(1)))).count();
            if (recent >= REMOTE_REQUESTS_PER_INSTANCE_PER_DAY) {
                return;
            }
            String linkId = null;
            for (ChronicleLinkEntity l : links.findBySide(target.getId())) {
                if (l.isRemote() && l.getB().startsWith(NetworkVocabulary.REMOTE_PREFIX + instanceId + "/")) {
                    linkId = l.getId();
                    break;
                }
            }
            if (linkId == null) {
                String relayed = text(node, "linkId");
                linkId = relayed != null && Ulids.isUlid(relayed) ? relayed : remoteId;
            }
            String message = text(node, "message");
            ChronicleAccessRequestEntity r = new ChronicleAccessRequestEntity(Ulids.next(now), linkId, handle, now);
            r.setRequesterInstance(instanceId);
            r.setRemoteRequestId(remoteId);
            r.setNetworkId(networkId);
            r.setTargetChronicleId(target.getId());
            String owner = views.owner(target.getId());
            r.setOwnerPrincipal(owner);
            r.setMessage(message == null || message.length() <= NetworkVocabulary.MAX_MESSAGE ? message
                    : message.substring(0, NetworkVocabulary.MAX_MESSAGE));
            JsonNode offer = node.get("offerShare");
            r.setOfferRemote(offer != null && offer.asBoolean(false));
            boolean declinedBefore = requests.findByTargetChronicleId(target.getId()).stream().anyMatch(x ->
                    instanceId.equals(x.getRequesterInstance()) && handle.equals(x.getRequesterHandle())
                            && NetworkVocabulary.DECLINED.equals(x.getStatus()) && x.getDecidedAt() != null
                            && x.getDecidedAt().isAfter(now.minus(NetworkVocabulary.DECLINE_SUPPRESSION)));
            r.setSuppressed(declinedBefore);
            requests.save(r);
            if (!r.isSuppressed()) {
                notices.requested(r);
            }
        } catch (RuntimeException e) {
            log.info("A relayed access request from {} was not stored: {}", instanceId, e.toString());
        }
    }

    /**
     * Applies a decision relayed by the instance that owns the requested chronicle.
     *
     * @param node the {@code accessDecisions} entry
     * @param instanceId the deciding instance
     */
    public void receiveRemoteDecision(JsonNode node, String instanceId) {
        String id = text(node, "requestId");
        String status = text(node, "status");
        if (id == null || status == null || !List.of(NetworkVocabulary.GRANTED, NetworkVocabulary.DECLINED)
                .contains(status)) {
            return;
        }
        requests.findById(id).ifPresent(r -> {
            if (r.getTargetNode() == null || !r.getTargetNode().startsWith(NetworkVocabulary.REMOTE_PREFIX
                    + instanceId + "/") || !NetworkVocabulary.PENDING.equals(r.getStatus())) {
                return;
            }
            Map<String, Object> grant = null;
            if (NetworkVocabulary.GRANTED.equals(status)) {
                grant = new LinkedHashMap<>();
                String as = text(node, "deliveredAs");
                grant.put("deliveredAs", "report".equals(as) ? "report" : "chronicle-extract");
                grant.put("role", null);
                grant.put("packageId", text(node, "packageId"));
            }
            r.decide(status, grant, clock.instant());
            requests.save(r);
            notices.decided(r, null);
        });
    }

    // ------------------------------------------------------------------------- decisions

    /**
     * Grants an incoming request ({@code grantChronicleAccessRequest}, owner).
     *
     * @param id the request
     * @param body the {@code AccessGrantInput}, or null
     * @param caller the owner
     * @return the {@code AccessRequest} (incoming)
     * @throws ForesignalException {@code 404 UNKNOWN_ACCESS_REQUEST}, {@code 409 ACCESS_REQUEST_DECIDED},
     *     {@code 403 TLP_VIOLATION}
     */
    public Map<String, Object> grant(String id, JsonNode body, ObjectCaller caller) {
        ChronicleAccessRequestEntity r = incoming(id, caller);
        String role = "viewer";
        String deliverAs = "chronicle-extract";
        String message = null;
        Instant expiresAt = null;
        if (body != null && !body.isNull()) {
            CollabInputs.object(body, "AccessGrantInput", GRANT_FIELDS);
            String requested = CollabInputs.text(body, "role", 1, 16);
            if (requested != null && !List.of("viewer", "commenter").contains(requested)) {
                throw ValidationException.forField("role", "must be viewer or commenter");
            }
            role = requested == null ? role : requested;
            String as = CollabInputs.text(body, "deliverAs", 1, 32);
            if (as != null && !List.of("report", "chronicle-extract").contains(as)) {
                throw ValidationException.forField("deliverAs", "must be report or chronicle-extract");
            }
            deliverAs = as == null ? deliverAs : as;
            message = CollabInputs.text(body, "message", 0, NetworkVocabulary.MAX_MESSAGE);
            expiresAt = CollabInputs.instant(body, "expiresAt");
        }
        Instant now = clock.instant();
        Map<String, Object> grant = new LinkedHashMap<>();
        String target = r.getTargetChronicleId();
        if (r.getRequesterPrincipal() != null) {
            Map<String, Object> share = new LinkedHashMap<>();
            share.put("grantees", List.of("principal:" + r.getRequesterPrincipal()));
            share.put("role", role);
            share.put("message", message);
            share.put("expiresAt", expiresAt == null ? null : expiresAt.toString());
            shares.share("chronicle:" + target, json.valueToTree(share), caller, "grantChronicleAccessRequest");
            grant.put("deliveredAs", "acl");
            grant.put("role", role);
            grant.put("packageId", null);
            if (r.getOfferShare() != null && chronicles.findById(r.getOfferShare()).isPresent()) {
                shareBack(r);
            }
        } else {
            ExchangeService service = exchange.getIfAvailable();
            if (service == null) {
                throw new ConflictException(ErrorCodes.FEATURE_UNAVAILABLE, "Intelligence exchange is not available.");
            }
            String objectRef = "chronicle:" + target;
            if ("report".equals(deliverAs)) {
                objectRef = "report:" + recap(target, caller);
            }
            Map<String, Object> pkg = new LinkedHashMap<>();
            pkg.put("objectRef", objectRef);
            pkg.put("recipients", List.of(Map.of("instanceId", r.getRequesterInstance(), "principals",
                    List.of(r.getRequesterHandle()))));
            pkg.put("deliver", "peer");
            pkg.put("message", message);
            ExchangePackageEntity p = service.create(json.valueToTree(pkg), caller);
            grant.put("deliveredAs", deliverAs);
            grant.put("role", null);
            grant.put("packageId", p.getId());
        }
        r.decide(NetworkVocabulary.GRANTED, grant, now);
        requests.save(r);
        if (r.getRequesterPrincipal() != null) {
            notices.decided(r, target);
        } else {
            relayDecision(r, grant);
        }
        return view(r, "incoming");
    }

    /**
     * Declines an incoming request ({@code declineChronicleAccessRequest}, owner); the requester is told the status
     * only.
     *
     * @return the {@code AccessRequest} (incoming)
     */
    public Map<String, Object> decline(String id, ObjectCaller caller) {
        ChronicleAccessRequestEntity r = incoming(id, caller);
        r.decide(NetworkVocabulary.DECLINED, null, clock.instant());
        requests.save(r);
        if (r.getRequesterPrincipal() != null) {
            notices.decided(r, null);
        } else {
            relayDecision(r, null);
        }
        return view(r, "incoming");
    }

    private ChronicleAccessRequestEntity incoming(String id, ObjectCaller caller) {
        requirePrincipal(caller);
        ChronicleAccessRequestEntity r = requests.findById(id == null ? "" : id).orElseThrow(() -> unknownRequest(id));
        boolean owner = caller.principal().equals(r.getOwnerPrincipal()) || (caller.admin()
                && r.getOwnerPrincipal() != null);
        if (!owner || r.isSuppressed() || r.getTargetChronicleId() == null) {
            throw unknownRequest(id);
        }
        if (!NetworkVocabulary.PENDING.equals(r.getStatus())) {
            throw new ConflictException(ErrorCodes.ACCESS_REQUEST_DECIDED, "The request was already " + r.getStatus()
                    + ".");
        }
        return r;
    }

    /** Shares the requester's offered chronicle with the owner (viewer); a refusal (TLP) is logged, not thrown. */
    private void shareBack(ChronicleAccessRequestEntity r) {
        try {
            Map<String, Object> share = new LinkedHashMap<>();
            share.put("grantees", List.of("principal:" + r.getOwnerPrincipal()));
            share.put("role", "viewer");
            share.put("message", "Shared in return for access to a related chronicle.");
            shares.share("chronicle:" + r.getOfferShare(), json.valueToTree(share),
                    ChronicleNetworkNotices.as(r.getRequesterPrincipal()), "grantChronicleAccessRequest");
        } catch (RuntimeException e) {
            log.info("The offered chronicle {} was not shared back: {}", r.getOfferShare(), e.getMessage());
        }
    }

    /** Drafts and publishes a chronicle-recap report of {@code chronicleId} as the owner; returns its id. */
    private String recap(String chronicleId, ObjectCaller caller) {
        ReportDrafter d = drafter.getIfAvailable();
        ReportPublisher p = publisher.getIfAvailable();
        if (d == null || p == null) {
            throw new ConflictException(ErrorCodes.FEATURE_UNAVAILABLE, "Reports are not available.");
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("source", Map.of("type", "chronicle", "ref", "chronicle:" + chronicleId));
        request.put("template", "chronicle-recap");
        ReportEntity report = d.draft(json.valueToTree(request), caller);
        p.publish(report.getId(), caller);
        return report.getId();
    }

    private void relayDecision(ChronicleAccessRequestEntity r, Map<String, Object> grant) {
        if (r.getRequesterInstance() == null || r.getRemoteRequestId() == null) {
            return;
        }
        Map<String, Object> decision = new LinkedHashMap<>();
        decision.put("requestId", r.getRemoteRequestId());
        decision.put("status", r.getStatus());
        if (grant != null) {
            decision.put("deliveredAs", grant.get("deliveredAs"));
            decision.put("packageId", grant.get("packageId"));
        }
        federated.relay(r.getRequesterInstance(), r.getNetworkId(), "accessDecisions", List.of(decision));
    }

    // ------------------------------------------------------------------------- reads

    /**
     * The caller's incoming and outgoing requests, newest first ({@code listChronicleAccessRequests}).
     *
     * @param direction {@code incoming}, {@code outgoing} or null for both
     * @param status a status filter, or null
     * @return the {@code AccessRequestPage}
     */
    public Map<String, Object> list(ObjectCaller caller, String direction, String status, String cursor, int limit) {
        String principal = requirePrincipal(caller);
        List<Map.Entry<String, ChronicleAccessRequestEntity>> all = new ArrayList<>();
        if (direction == null || "incoming".equals(direction)) {
            for (ChronicleAccessRequestEntity r : requests.findByOwnerPrincipalOrderByCreatedAtDescIdDesc(principal)) {
                if (!r.isSuppressed() && r.getTargetChronicleId() != null) {
                    all.add(Map.entry("incoming", r));
                }
            }
        }
        if (direction == null || "outgoing".equals(direction)) {
            for (ChronicleAccessRequestEntity r : requests.findByRequesterPrincipalOrderByCreatedAtDescIdDesc(
                    principal)) {
                all.add(Map.entry("outgoing", r));
            }
        }
        all.removeIf(e -> status != null && !status.equals(e.getValue().getStatus()));
        all.sort(Comparator.comparing((Map.Entry<String, ChronicleAccessRequestEntity> e) ->
                e.getValue().getCreatedAt()).reversed().thenComparing(e -> e.getValue().getId(),
                        Comparator.reverseOrder()));
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, ChronicleAccessRequestEntity> e : all.subList(Math.min(offset, all.size()),
                Math.min(offset + limit, all.size()))) {
            items.add(view(e.getValue(), e.getKey()));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < all.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** The {@code AccessRequest} view; the target chronicle is shown on incoming requests only. */
    public Map<String, Object> view(ChronicleAccessRequestEntity r, String direction) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("linkId", r.getLinkId());
        out.put("direction", direction);
        out.put("chronicleId", "incoming".equals(direction) ? r.getTargetChronicleId() : null);
        out.put("requesterHandle", r.getRequesterHandle());
        out.put("requesterInstanceId", r.getRequesterInstance());
        out.put("message", r.getMessage());
        Object offer = null;
        if (r.getOfferShare() != null) {
            offer = Map.of("chronicleId", r.getOfferShare());
        } else if (r.isOfferRemote()) {
            offer = Map.of("chronicleId", NetworkVocabulary.REMOTE_PREFIX + r.getRequesterInstance());
        }
        out.put("offerShare", offer);
        out.put("status", r.getStatus());
        out.put("createdAt", r.getCreatedAt().toString());
        out.put("decidedAt", r.getDecidedAt() == null ? null : r.getDecidedAt().toString());
        out.put("grant", r.getGrant());
        return out;
    }

    private static String requirePrincipal(ObjectCaller caller) {
        if (caller == null || caller.principal() == null) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Access requests need a token.");
        }
        return caller.principal();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        return v == null || !v.isString() || v.asString().isBlank() ? null : v.asString().strip();
    }
}
