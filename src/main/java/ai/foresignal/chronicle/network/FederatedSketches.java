package ai.foresignal.chronicle.network;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.exchange.PackageVerifier;
import ai.foresignal.exchange.PeerExchangeClient;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerRepository;
import ai.foresignal.federation.PeerService;
import ai.foresignal.provenance.JwsSigner;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Federated chronicle discovery over the 113 peer channel (prompt 134).
 *
 * <ul>
 *   <li><b>Sending:</b> for each current network salt, a {@code ChronicleSketchBatch} of the sketch packages of this
 *       instance's {@code network}-discoverable chronicles (at least 50 keys): a random sketch id per chronicle per
 *       salt, the key types, the MinHash slots, log2 size bands, the capture span in ISO weeks, the TLP label, and the
 *       title or owner handle only when revealed; plus the sketch ids withdrawn in the last 30 days. It goes to every
 *       enabled peer with an exchange key ({@code POST {peer}/v1/federation/chronicle-sketches}, Bearer exchange key),
 *       with the body signed by the instance key (109) in {@code Foresignal-Signature} (detached JWS).</li>
 *   <li><b>Receiving:</b> the signature is verified against the peer's pinned key or its JWKS; the batch must name a
 *       network with a current salt here while {@code network.federated} is true; each sketch is checked (TLP that
 *       allows network discoverability, 128 slots per key type, week labels) and stored for 30 days; withdrawn
 *       sketches and their links are removed.</li>
 *   <li><b>Access relays:</b> the same channel carries {@code accessRequests} (handle, message and whether a share is
 *       offered, never the requester's chronicle) and {@code accessDecisions} (status and package id) between
 *       instances.</li>
 * </ul>
 */
@Component
public class FederatedSketches {

    private static final Logger log = LoggerFactory.getLogger(FederatedSketches.class);

    /** The peer path. */
    public static final String PATH = "/v1/federation/chronicle-sketches";

    private static final Pattern SKETCH_ID = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private static final Pattern HANDLE = Pattern.compile("^[a-z0-9][a-z0-9._-]{0,63}$");

    private final NetworkSalts salts;
    private final ChronicleKeyStore store;
    private final ChronicleRepository chronicles;
    private final ChronicleLinkViews views;
    private final ChronicleRemoteSketchRepository remotes;
    private final ChronicleLinkRepository links;
    private final PeerRepository peers;
    private final PeerService peerService;
    private final PeerExchangeClient client;
    private final JwsSigner signer;
    private final FederationIdentity identity;
    private final PrincipalService principals;
    private final ObjectProvider<ChronicleAccessService> accessRequests;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the component. */
    public FederatedSketches(NetworkSalts salts, ChronicleKeyStore store, ChronicleRepository chronicles,
            ChronicleLinkViews views, ChronicleRemoteSketchRepository remotes, ChronicleLinkRepository links,
            PeerRepository peers, PeerService peerService, PeerExchangeClient client, JwsSigner signer,
            FederationIdentity identity, PrincipalService principals,
            ObjectProvider<ChronicleAccessService> accessRequests, JsonMapper json, Clock clock) {
        this.salts = salts;
        this.store = store;
        this.chronicles = chronicles;
        this.views = views;
        this.remotes = remotes;
        this.links = links;
        this.peers = peers;
        this.peerService = peerService;
        this.client = client;
        this.signer = signer;
        this.identity = identity;
        this.principals = principals;
        this.accessRequests = accessRequests;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- sending

    /**
     * Builds the batch of one network salt, recording withdrawals of sketches that are no longer eligible.
     *
     * @param network the network salt
     * @return the {@code ChronicleSketchBatch}
     */
    public Map<String, Object> batch(NetworkSalts.Salt network) {
        Instant now = clock.instant();
        List<Map<String, Object>> sketches = new ArrayList<>();
        for (ChronicleKeyStore.State s : store.states(network.id())) {
            ChronicleEntity c = chronicles.findById(s.chronicleId()).orElse(null);
            boolean eligible = c != null && s.sketchId() != null
                    && s.totalKeys() >= NetworkVocabulary.MIN_KEYS_FOR_DISCOVERY
                    && views.effective(c) == DiscoverabilityLevel.NETWORK;
            if (!eligible) {
                store.withdrawn(s.chronicleId(), network.id(), now);
                continue;
            }
            Map<String, Object> pkg = sketchPackage(c, s, network, now);
            if (pkg != null && sketches.size() < NetworkVocabulary.MAX_BATCH_SKETCHES) {
                sketches.add(pkg);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("instanceId", identity.instanceId());
        out.put("networkId", network.networkId());
        out.put("sketches", sketches);
        out.put("withdrawn", store.withdrawnSince(network.id(), now.minus(NetworkVocabulary.SKETCH_EXPIRY)));
        return out;
    }

    /** The {@code ChronicleSketchPackage} of one chronicle under a network salt, or null without fingerprints. */
    Map<String, Object> sketchPackage(ChronicleEntity c, ChronicleKeyStore.State s, NetworkSalts.Salt network,
            Instant now) {
        Map<String, Object> minhash = new LinkedHashMap<>();
        Map<String, Object> bands = new LinkedHashMap<>();
        List<String> types = new ArrayList<>();
        for (String type : NetworkVocabulary.KEY_TYPES) {
            Optional<ChronicleKeyStore.Fingerprint> f = store.fingerprint(c.getId(), type, network.id());
            if (f.isEmpty() || f.get().size() == 0) {
                continue;
            }
            types.add(type);
            minhash.put(type, f.get().sketch().toBase64());
            bands.put(type, MinHashSketch.sizeBand(f.get().size()));
        }
        if (types.isEmpty()) {
            return null;
        }
        Map<String, Object> span = new LinkedHashMap<>();
        String from = IsoWeeks.of(c.getStartedAt() == null ? c.getCreatedAt() : c.getStartedAt());
        span.put("from", from);
        span.put("to", IsoWeeks.of(c.getStoppedAt()));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sketchId", s.sketchId());
        out.put("instanceId", identity.instanceId());
        out.put("networkId", network.networkId());
        out.put("keyTypes", types);
        out.put("minhash", minhash);
        out.put("sizeBand", bands);
        out.put("captureSpan", span);
        out.put("title", c.isRevealTitle() ? cut(c.getName(), 200) : null);
        String owner = views.owner(c.getId());
        out.put("ownerHandle", c.isRevealOwner() && owner != null
                ? principals.find(owner).map(PrincipalEntity::getHandle).orElse(null) : null);
        out.put("tlp", views.tlp(c.getId()).wireValue());
        out.put("issuedAt", now.toString());
        out.put("expiresAt", now.plus(NetworkVocabulary.SKETCH_EXPIRY).toString());
        return out;
    }

    /**
     * Sends every current network salt's batch to every enabled peer that holds an exchange key.
     *
     * @return the deliveries that succeeded
     */
    public int sendAll() {
        if (!salts.federated()) {
            return 0;
        }
        int ok = 0;
        Instant now = clock.instant();
        for (NetworkSalts.Salt network : salts.networkSalts()) {
            Map<String, Object> body = batch(network);
            boolean delivered = false;
            for (PeerEntity peer : peers.findAllByOrderByIdAsc()) {
                if (peer.isEnabled() && post(peer, body)) {
                    ok++;
                    delivered = true;
                }
            }
            if (delivered) {
                for (Object o : (List<?>) body.get("sketches")) {
                    if (o instanceof Map<?, ?> m) {
                        store.stateBySketchId(String.valueOf(m.get("sketchId")))
                                .ifPresent(st -> store.sent(st.chronicleId(), network.id(), now));
                    }
                }
            }
        }
        return ok;
    }

    /**
     * Relays access requests or decisions to another instance on the peer channel.
     *
     * @param instanceId the other instance
     * @param networkId the network the link was found in
     * @param field {@code accessRequests} or {@code accessDecisions}
     * @param items the entries
     * @return true when a peer accepted it
     */
    public boolean relay(String instanceId, String networkId, String field, List<Map<String, Object>> items) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("instanceId", identity.instanceId());
        body.put("networkId", networkId);
        body.put("sketches", List.of());
        body.put(field, items);
        for (PeerEntity peer : peersOf(instanceId)) {
            if (post(peer, body)) {
                return true;
            }
        }
        log.info("No peer of {} accepted the chronicle {} relay", instanceId, field);
        return false;
    }

    /** The enabled peers of a remote instance: by verified remote instance id, else by peer id. */
    List<PeerEntity> peersOf(String instanceId) {
        List<PeerEntity> out = new ArrayList<>();
        for (PeerEntity p : peers.findByRemoteInstanceIdOrderByIdAsc(instanceId)) {
            if (p.isEnabled()) {
                out.add(p);
            }
        }
        if (out.isEmpty()) {
            peers.findById(instanceId).filter(PeerEntity::isEnabled).ifPresent(out::add);
        }
        return out;
    }

    private boolean post(PeerEntity peer, Map<String, Object> body) {
        String key = peerService.exchangeKey(peer);
        if (key == null || peer.getBaseUrl() == null) {
            return false;
        }
        try {
            byte[] bytes = json.writeValueAsBytes(body);
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Authorization", "Bearer " + key);
            headers.put("Foresignal-Signature", signer.signDetached(bytes, "application/json"));
            PeerExchangeClient.Response r = client.post(peer.getBaseUrl(), PATH, "application/json", bytes, headers);
            if (!r.ok()) {
                log.info("Peer {} refused chronicle sketches: {}", peer.getId(), r.status());
            }
            return r.ok();
        } catch (PeerExchangeClient.DeliveryException e) {
            log.info("Chronicle sketches not delivered to {}: {}", peer.getId(), e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------------- receiving

    /**
     * Receives a signed batch from a peer ({@code receiveChronicleSketches}).
     *
     * @param body the exact body bytes (the signature covers them)
     * @param signature the {@code Foresignal-Signature} header
     * @param peerId the calling peer
     * @return the {@code ChronicleSketchBatchResult}
     * @throws ForesignalException {@code 400 SKETCH_BATCH_INVALID}, {@code 409 FEDERATED_DISCOVERY_DISABLED} or
     *     {@code 409 NETWORK_SALT_UNKNOWN}
     */
    public Map<String, Object> receive(byte[] body, String signature, String peerId) {
        PeerEntity peer = peers.findById(peerId == null ? "" : peerId).orElseThrow(() -> invalid(
                "the peer credential names no registered peer"));
        if (signature == null || signature.isBlank()) {
            throw invalid("the Foresignal-Signature header is required");
        }
        PackageVerifier.Result verified = verify(body, signature.strip(), peer);
        if (!verified.valid()) {
            throw invalid("the signature does not verify: " + verified.error());
        }
        JsonNode root;
        try {
            root = json.readTree(body);
        } catch (RuntimeException e) {
            throw invalid("the body is not JSON");
        }
        if (root == null || !root.isObject()) {
            throw invalid("the body must be a ChronicleSketchBatch object");
        }
        String instanceId = text(root, "instanceId");
        String networkId = text(root, "networkId");
        if (instanceId == null || networkId == null) {
            throw invalid("instanceId and networkId are required");
        }
        if (peer.getRemoteInstanceId() != null && !peer.getRemoteInstanceId().equals(instanceId)) {
            throw invalid("the batch names instance " + instanceId + " but the peer is " + peer.getRemoteInstanceId());
        }
        if (!salts.federated()) {
            throw new ConflictException(ErrorCodes.FEDERATED_DISCOVERY_DISABLED, "Federated chronicle discovery is"
                    + " off on this instance.");
        }
        NetworkSalts.Salt network = salts.networkSalt(networkId).orElseThrow(() -> new ConflictException(
                ErrorCodes.NETWORK_SALT_UNKNOWN, "This instance holds no current salt for network '" + networkId
                        + "'."));
        JsonNode sketches = root.get("sketches");
        if (sketches == null || !sketches.isArray()) {
            throw invalid("sketches must be an array");
        }
        if (sketches.size() > NetworkVocabulary.MAX_BATCH_SKETCHES) {
            throw invalid("a batch holds at most " + NetworkVocabulary.MAX_BATCH_SKETCHES + " sketches");
        }
        Instant now = clock.instant();
        int accepted = 0;
        List<Map<String, Object>> rejected = new ArrayList<>();
        for (JsonNode s : sketches) {
            String sketchId = s == null ? null : text(s, "sketchId");
            String reason = accept(s, instanceId, networkId, network, peer, now);
            if (reason == null) {
                accepted++;
            } else {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("sketchId", sketchId == null ? "" : sketchId);
                r.put("reason", reason);
                rejected.add(r);
            }
        }
        JsonNode withdrawn = root.get("withdrawn");
        if (withdrawn != null && withdrawn.isArray()) {
            for (JsonNode w : withdrawn) {
                if (w.isString()) {
                    withdraw(instanceId, w.asString());
                }
            }
        }
        ChronicleAccessService access = accessRequests.getIfAvailable();
        if (access != null) {
            JsonNode requests = root.get("accessRequests");
            if (requests != null && requests.isArray()) {
                for (JsonNode r : requests) {
                    access.receiveRemoteRequest(r, instanceId, networkId);
                }
            }
            JsonNode decisions = root.get("accessDecisions");
            if (decisions != null && decisions.isArray()) {
                for (JsonNode d : decisions) {
                    access.receiveRemoteDecision(d, instanceId);
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("accepted", accepted);
        out.put("rejected", rejected);
        return out;
    }

    /** Verifies the detached JWS with the peer's pinned key, else with its JWKS. */
    PackageVerifier.Result verify(byte[] body, String jws, PeerEntity peer) {
        String kid = PackageVerifier.kid(jws);
        if (peer.getPinnedKid() != null && peer.getPinnedKey() != null) {
            if (!peer.getPinnedKid().equals(kid)) {
                return new PackageVerifier.Result(PackageVerifier.INVALID, kid, "the peer's pinned key is "
                        + peer.getPinnedKid());
            }
            return PackageVerifier.verifyPinned(body, jws, peer.getPinnedKey());
        }
        try {
            return PackageVerifier.verify(body, jws, client.jwks(peer.getBaseUrl()));
        } catch (PeerExchangeClient.DeliveryException e) {
            return new PackageVerifier.Result(PackageVerifier.UNKNOWN_KEY, kid, e.getMessage());
        }
    }

    /** Checks and stores one sketch; returns the rejection reason, or null when accepted. */
    String accept(JsonNode s, String instanceId, String networkId, NetworkSalts.Salt network, PeerEntity peer,
            Instant now) {
        if (s == null || !s.isObject()) {
            return "not a ChronicleSketchPackage object";
        }
        String sketchId = text(s, "sketchId");
        if (sketchId == null || !SKETCH_ID.matcher(sketchId).matches()) {
            return "sketchId must be 1 to 64 letters, digits, - or _";
        }
        if (!instanceId.equals(text(s, "instanceId")) || !networkId.equals(text(s, "networkId"))) {
            return "instanceId and networkId must match the batch";
        }
        Tlp tlp;
        try {
            tlp = Tlp.fromWire(text(s, "tlp"));
        } catch (RuntimeException e) {
            return "tlp is not a TLP label";
        }
        if (tlp == null || DiscoverabilityLevel.maxAllowed(tlp) != DiscoverabilityLevel.NETWORK) {
            return "the TLP label does not allow network discoverability";
        }
        JsonNode types = s.get("keyTypes");
        JsonNode minhash = s.get("minhash");
        JsonNode bands = s.get("sizeBand");
        if (types == null || !types.isArray() || types.isEmpty() || minhash == null || !minhash.isObject()
                || bands == null || !bands.isObject()) {
            return "keyTypes, minhash and sizeBand are required";
        }
        List<String> keyTypes = new ArrayList<>();
        Map<String, String> slots = new LinkedHashMap<>();
        Map<String, Integer> sizes = new LinkedHashMap<>();
        for (JsonNode t : types) {
            String type = t.isString() ? t.asString() : null;
            if (type == null || !NetworkVocabulary.KEY_TYPES.contains(type) || keyTypes.contains(type)) {
                return "keyTypes must be distinct among " + NetworkVocabulary.KEY_TYPES;
            }
            JsonNode m = minhash.get(type);
            JsonNode b = bands.get(type);
            if (m == null || !m.isString() || b == null || !b.isNumber()) {
                return "minhash and sizeBand need an entry for " + type;
            }
            try {
                MinHashSketch.fromBase64(m.asString());
            } catch (IllegalArgumentException e) {
                return "minhash." + type + " must be base64 of " + MinHashSketch.PERMUTATIONS + " 64-bit values";
            }
            int band = b.asInt();
            if (band < 0 || band > 62) {
                return "sizeBand." + type + " must be 0 to 62";
            }
            keyTypes.add(type);
            slots.put(type, m.asString());
            sizes.put(type, band);
        }
        JsonNode span = s.get("captureSpan");
        String from = span == null ? null : text(span, "from");
        String to = span == null ? null : text(span, "to");
        if (!IsoWeeks.valid(from) || (to != null && !IsoWeeks.valid(to))) {
            return "captureSpan must give ISO weeks such as 2026-W40";
        }
        Instant issued;
        try {
            String text = text(s, "issuedAt");
            issued = text == null ? now : Instant.parse(text);
        } catch (DateTimeParseException e) {
            return "issuedAt is not a date-time";
        }
        String title = text(s, "title");
        String owner = text(s, "ownerHandle");
        if (owner != null && !HANDLE.matcher(owner).matches()) {
            owner = null;
        }
        ChronicleRemoteSketchEntity row = remotes.findByInstanceIdAndSketchId(instanceId, sketchId)
                .orElseGet(() -> new ChronicleRemoteSketchEntity(Ulids.next(now), instanceId, sketchId));
        row.setPeerId(peer.getId());
        row.setNetworkId(networkId);
        row.setSaltId(network.id());
        row.setKeyTypes(keyTypes);
        row.setMinhash(slots);
        row.setSizeBand(sizes);
        row.setCaptureSpan(from, to);
        row.setTitle(cut(title, 200));
        row.setOwnerHandle(owner);
        row.setTlp(tlp.wireValue());
        row.setIssuedAt(issued);
        row.refreshed(now);
        remotes.save(row);
        return null;
    }

    /** Removes a withdrawn sketch and its links. */
    void withdraw(String instanceId, String sketchId) {
        remotes.findByInstanceIdAndSketchId(instanceId, sketchId).ifPresent(r -> {
            links.deleteBySide(r.node());
            remotes.delete(r);
        });
    }

    /** Removes expired sketches and their links; returns how many expired. */
    public int expire() {
        int n = 0;
        for (ChronicleRemoteSketchEntity r : remotes.findByExpiresAtBeforeOrderByIdAsc(clock.instant())) {
            links.deleteBySide(r.node());
            remotes.delete(r);
            n++;
        }
        return n;
    }

    /** Forgets the sketches received for a network (its salt was replaced) and their links. */
    public void forgetNetwork(String networkId) {
        for (ChronicleRemoteSketchEntity r : remotes.findByNetworkIdOrderByIdAsc(networkId)) {
            links.deleteBySide(r.node());
            remotes.delete(r);
        }
    }

    private static ForesignalException invalid(String detail) {
        return new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.SKETCH_BATCH_INVALID,
                "The sketch batch is not valid: " + detail + ".");
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || !v.isString() || v.asString().isBlank() ? null : v.asString().strip();
    }

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
