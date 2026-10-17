package ai.foresignal.federation;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.StreamingSupervisor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.PeerKeyAuthenticator;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Federation peers (prompt 113): CRUD, the dynamic sources of the {@code foresignal-peer} family, peer ingest keys,
 * the {@code /info} test and resync requests.
 *
 * <ul>
 *   <li><strong>Sources:</strong> each peer registers {@code peer-{id}} (and {@code peer-{id}-trk} when the selection
 *       includes TRACK kinds, see {@link PeerDescriptors}) through {@link SourceRegistry#registerDynamic}, plus
 *       {@code peer-{id}-events} for compact messages; they are enabled with the peer. A {@code PULL} peer's record
 *       sources are streams the supervisor runs ({@link PeerAdapter}); a {@code RECEIVE} peer's are push sources.
 *   <li><strong>Base URL:</strong> checked by the outbound URL policy (035, {@code LAN_ALLOWED}: LAN, Tailnet and
 *       public), at save and again before every pass.
 *   <li><strong>Token:</strong> the remote instance's token (read plus {@code federation}), encrypted at rest with
 *       the install key (SEC-3) and never returned.
 *   <li><strong>Key pinning:</strong> TOFU on the first verified {@code /info}; only an admin changes
 *       {@code pinnedKid} (null re-pins on the next verified contact).
 *   <li><strong>Peer ingest keys:</strong> {@code fsp_} plus 32 random bytes, shown once, stored as a SHA-256 hash;
 *       a rotation revokes the previous key. They authenticate {@code POST /v1/federation/ingest/{peerId}} and
 *       {@code POST /v1/federation/bundle}.
 * </ul>
 *
 * <p>Persisted peers are registered at startup and re-reconciled after the test database cleaner runs.
 */
@Component
public class PeerService implements PeerKeyAuthenticator, SmartInitializingSingleton, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(PeerService.class);

    /** {@code PULL}: consume the peer's feed. */
    public static final String MODE_PULL = "PULL";

    /** {@code RECEIVE}: the peer (an edge) pushes here. */
    public static final String MODE_RECEIVE = "RECEIVE";

    /** State: not replicating. */
    public static final String STATE_STOPPED = "stopped";

    /** State: fetching {@code /info} or catching up. */
    public static final String STATE_CONNECTING = "connecting";

    /** State: the SSE stream is open. */
    public static final String STATE_STREAMING = "streaming";

    /** State: replaying a snapshot. */
    public static final String STATE_RESYNCING = "resyncing";

    /** State: failing (see the health reason). */
    public static final String STATE_FAILING = "failing";

    /** State: a RECEIVE peer waiting for pushes. */
    public static final String STATE_RECEIVING = "receiving";

    /** Health reason: the remote key does not match the pinned one. */
    public static final String REASON_KEY_MISMATCH = ErrorCodes.PEER_KEY_MISMATCH;

    /** Health reason: the peer cannot be reached. */
    public static final String REASON_UNREACHABLE = "PEER_UNREACHABLE";

    /** Health reason: a resync is running. */
    public static final String REASON_RESYNC = "PEER_RESYNC";

    /** The id pattern. */
    static final Pattern ID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** The longest id ({@code peer-{id}-events} must fit the 64-character source id). */
    static final int MAX_ID = 48;

    /** The largest page. */
    public static final int MAX_LIMIT = 500;

    private static final Set<String> RELIABILITY = Set.of("A", "B", "C", "D", "E", "F");

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final PeerRepository peers;
    private final SourceRegistry registry;
    private final ObjectProvider<SourceService> sources;
    private final PeerRuntime runtime;
    private final OutboundUrlPolicy policy;
    private final CredentialCipher cipher;
    private final TokenHasher hasher;
    private final JsonMapper json;
    private final ObjectProvider<UpstreamClient> upstream;
    private final ObjectProvider<StreamingSupervisor> supervisor;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    /** Creates the service. */
    public PeerService(PeerRepository peers, SourceRegistry registry, ObjectProvider<SourceService> sources,
            PeerRuntime runtime, OutboundUrlPolicy policy, CredentialCipher cipher, TokenHasher hasher, JsonMapper json,
            ObjectProvider<UpstreamClient> upstream, ObjectProvider<StreamingSupervisor> supervisor, Clock clock) {
        this.peers = peers;
        this.registry = registry;
        this.sources = sources;
        this.runtime = runtime;
        this.policy = policy;
        this.cipher = cipher;
        this.hasher = hasher;
        this.json = json;
        this.upstream = upstream;
        this.supervisor = supervisor;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- lifecycle

    @Override
    public void afterSingletonsInstantiated() {
        reconcile();
    }

    @Override
    public void resetState() {
        reconcile();
    }

    /** Registers every persisted peer's sources and unregisters peer sources without a peer row. */
    public synchronized void reconcile() {
        Set<String> wanted = new HashSet<>();
        for (PeerEntity peer : peers.findAll()) {
            try {
                wanted.addAll(register(peer, false));
            } catch (RuntimeException e) {
                log.error("Could not register the sources of peer {}; fix or delete it", peer.getId(), e);
            }
        }
        for (SourceRegistry.Registration registration : registry.all()) {
            String id = registration.id();
            if (registration.dynamic() && PeerDescriptors.isPeerSource(id) && !wanted.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    // ------------------------------------------------------------------------- CRUD

    /** Every peer, ordered by id. */
    public List<PeerEntity> list() {
        return peers.findAllByOrderByIdAsc();
    }

    /** The peer, or 404. */
    public PeerEntity get(String peerId) {
        return peers.findById(peerId).orElseThrow(() -> NotFoundException.of("Peer", peerId));
    }

    /**
     * Creates a peer from a {@code PeerCreate} body.
     *
     * @throws ValidationException on a bad field ({@code URL_NOT_ALLOWED} for a refused base URL)
     * @throws ConflictException {@code PEER_EXISTS}
     */
    public synchronized PeerEntity create(JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        String id = text(body, "id");
        if (id == null || !ID.matcher(id).matches() || id.length() > MAX_ID) {
            errors.add(new FieldError("id", "must be kebab-case (lowercase letters and digits), at most " + MAX_ID
                    + " characters"));
        }
        String name = name(body, errors);
        String baseUrl = baseUrl(body, errors);
        String mode = text(body, "mode") == null ? MODE_PULL : text(body, "mode");
        if (!MODE_PULL.equals(mode) && !MODE_RECEIVE.equals(mode)) {
            errors.add(new FieldError("mode", "must be PULL or RECEIVE"));
        }
        String token = text(body, "token");
        if (MODE_PULL.equals(mode) && token == null) {
            errors.add(new FieldError("token", "is required for a PULL peer: the remote token (read + federation)"));
        }
        PeerSelection selection = selection(body, errors);
        String reliability = reliability(body, errors);
        throwIf(errors);
        if (peers.existsById(id)) {
            throw new ConflictException(ErrorCodes.PEER_EXISTS, "A peer with id '" + id + "' exists.");
        }
        for (String sourceId : List.of(PeerDescriptors.standardId(id), PeerDescriptors.trackId(id),
                PeerDescriptors.eventsId(id))) {
            if (registry.contains(sourceId)) {
                throw new ConflictException(ErrorCodes.SOURCE_EXISTS, "A source with id '" + sourceId + "' exists.");
            }
        }
        Instant now = clock.instant();
        PeerEntity peer = new PeerEntity(id, name, baseUrl, mode, now);
        peer.setEnabled(!body.has("enabled") || body.get("enabled").asBoolean(true));
        peer.setSameOperator(body.path("sameOperator").asBoolean(false));
        peer.setReliability(reliability);
        peer.setSelection(selection.toMap());
        if (token != null) {
            Encrypted encrypted = cipher.encrypt(token);
            peer.setToken(encrypted.ciphertext(), encrypted.nonce());
        }
        peer.setState(MODE_RECEIVE.equals(mode) ? STATE_RECEIVING : STATE_STOPPED);
        peers.save(peer);
        register(peer, true);
        return peer;
    }

    /**
     * Replaces a peer's settings from a {@code PeerUpdate} body: an omitted token keeps the stored one; a present
     * {@code pinnedKid} re-pins (null: on the next verified contact).
     */
    public synchronized PeerEntity update(String peerId, JsonNode body) {
        PeerEntity peer = get(peerId);
        List<FieldError> errors = new ArrayList<>();
        String name = name(body, errors);
        String baseUrl = baseUrl(body, errors);
        PeerSelection selection = selection(body, errors);
        String reliability = reliability(body, errors);
        if (body.has("mode") && !peer.getMode().equals(text(body, "mode"))) {
            errors.add(new FieldError("mode", "cannot be changed; delete the peer and create it again"));
        }
        throwIf(errors);
        peer.setName(name);
        peer.setBaseUrl(baseUrl);
        if (body.has("enabled")) {
            peer.setEnabled(body.get("enabled").asBoolean(peer.isEnabled()));
        }
        if (body.has("sameOperator")) {
            peer.setSameOperator(body.get("sameOperator").asBoolean(false));
        }
        peer.setReliability(reliability);
        peer.setSelection(selection.toMap());
        String token = text(body, "token");
        if (token != null) {
            Encrypted encrypted = cipher.encrypt(token);
            peer.setToken(encrypted.ciphertext(), encrypted.nonce());
        }
        if (body.has("pinnedKid")) {
            JsonNode kid = body.get("pinnedKid");
            peer.setPinned(kid == null || kid.isNull() || kid.asString().isBlank() ? null : kid.asString().strip(),
                    null);
            if (kid == null || kid.isNull()) {
                peer.setRemoteInstanceId(null);
            }
            if (REASON_KEY_MISMATCH.equals(peer.getHealthReason())) {
                peer.setHealthReason(null);
            }
        }
        peer.setUpdatedAt(clock.instant());
        peers.save(peer);
        runtime.stop(peerId);
        register(peer, false);
        restart(PeerDescriptors.standardId(peerId));
        return peer;
    }

    /** Deletes a peer: stops replication and unregisters its sources (ownership moves at the next arrival). */
    public synchronized void delete(String peerId) {
        PeerEntity peer = get(peerId);
        runtime.stop(peerId);
        for (String sourceId : List.of(PeerDescriptors.standardId(peerId), PeerDescriptors.trackId(peerId),
                PeerDescriptors.eventsId(peerId))) {
            if (registry.contains(sourceId)) {
                registry.unregisterDynamic(sourceId);
            }
            SourceService service = sources.getIfAvailable();
            if (service != null) {
                service.deleteDynamicSettings(sourceId);
            }
        }
        peers.delete(peer);
    }

    /**
     * Issues a new peer ingest key (shown once) and revokes the previous one.
     *
     * @return the {@code PeerIngestKey} document
     */
    public synchronized Map<String, Object> rotateIngestKey(String peerId) {
        PeerEntity peer = get(peerId);
        byte[] entropy = new byte[32];
        random.nextBytes(entropy);
        String key = PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
        String prefix = key.substring(0, 12);
        peer.setIngestKey(hasher.hash(key), prefix);
        Instant now = clock.instant();
        peer.setUpdatedAt(now);
        peers.save(peer);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("peerId", peerId);
        out.put("key", key);
        out.put("keyPrefix", prefix);
        out.put("createdAt", now.toString());
        return out;
    }

    @Override
    public Optional<String> authenticate(String key) {
        if (key == null || !key.startsWith(PREFIX)) {
            return Optional.empty();
        }
        return peers.findByIngestKeyHash(hasher.hash(key))
                .filter(peer -> hasher.matches(key, peer.getIngestKeyHash()))
                .map(PeerEntity::getId);
    }

    /** Asks the peer's session to re-snapshot and tombstone absent heads (explicit: the shrink guard is released). */
    public PeerEntity resync(String peerId) {
        PeerEntity peer = get(peerId);
        runtime.requestResync(peerId, true);
        if (MODE_PULL.equals(peer.getMode())) {
            peer.setState(STATE_RESYNCING);
            peer.setHealthReason(REASON_RESYNC);
            peer.setUpdatedAt(clock.instant());
            peers.save(peer);
        }
        return peer;
    }

    /**
     * Fetches the peer's {@code /info}, verifies its JWS against the peer's JWKS and the pinned key, and reports
     * ({@code PeerTestResult}). Pins nothing.
     */
    public Map<String, Object> test(String peerId) {
        PeerEntity peer = get(peerId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", false);
        out.put("reachable", false);
        out.put("latencyMs", null);
        out.put("info", null);
        out.put("signatureValid", null);
        out.put("kid", null);
        out.put("pinnedKid", peer.getPinnedKid());
        out.put("keyMatches", null);
        out.put("error", null);
        String problem = policy.saveProblem(peer.getBaseUrl(), OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        if (problem != null) {
            out.put("error", "the outbound URL policy refuses the base URL: " + problem);
            return out;
        }
        UpstreamClient client = upstream.getIfAvailable();
        String sourceId = PeerDescriptors.standardId(peerId);
        if (client == null || !registry.contains(sourceId)) {
            out.put("error", "the peer's source is not registered");
            return out;
        }
        long started = System.nanoTime();
        try {
            PeerRemote remote = new PeerRemote(client.forSource(sourceId, registry.descriptor(sourceId)),
                    peer.getBaseUrl(), runtime.token(peer), json);
            PeerRemote.Verified verified = remote.verifiedInfo();
            out.put("reachable", true);
            out.put("latencyMs", (System.nanoTime() - started) / 1_000_000);
            out.put("info", verified.info().body());
            PeerKeys.Verification v = verified.verification();
            out.put("signatureValid", v.signatureValid());
            out.put("kid", v.kid());
            Boolean matches = peer.getPinnedKid() == null ? null : peer.getPinnedKid().equals(v.kid());
            out.put("keyMatches", matches);
            if (!v.signatureValid()) {
                out.put("error", v.error());
            } else if (Boolean.FALSE.equals(matches)) {
                out.put("error", REASON_KEY_MISMATCH + ": the peer signs with " + v.kid() + ", " + peer.getPinnedKid()
                        + " is pinned");
            }
            out.put("ok", v.signatureValid() && !Boolean.FALSE.equals(matches));
        } catch (Exception e) {
            out.put("latencyMs", (System.nanoTime() - started) / 1_000_000);
            out.put("error", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
        return out;
    }

    /** The {@code Peer} document (tokens are never returned). */
    public Map<String, Object> view(PeerEntity peer) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", peer.getId());
        out.put("name", peer.getName());
        out.put("baseUrl", peer.getBaseUrl());
        out.put("mode", peer.getMode());
        out.put("enabled", peer.isEnabled());
        out.put("state", peer.isEnabled() ? peer.getState() : STATE_STOPPED);
        out.put("healthReason", peer.getHealthReason());
        out.put("lastSeq", peer.getLastSeq());
        Long lag = runtime.lagOf(peer.getId());
        if (lag == null && peer.getRemoteWatermark() != null && peer.getLastSeq() != null) {
            lag = Math.max(0, peer.getRemoteWatermark() - peer.getLastSeq());
        }
        out.put("lagSeq", lag);
        out.put("lastError", peer.getLastError());
        out.put("pinnedKid", peer.getPinnedKid());
        out.put("remoteInstanceId", peer.getRemoteInstanceId());
        out.put("selection", PeerSelection.parse(peer.getSelection(), "selection", new ArrayList<>()).toMap());
        out.put("sameOperator", peer.isSameOperator());
        out.put("reliability", peer.getReliability() == null ? PeerDescriptors.DEFAULT_RELIABILITY
                : peer.getReliability());
        out.put("tokenSet", peer.getTokenCiphertext() != null);
        out.put("ingestKeyPrefix", peer.getIngestKeyPrefix());
        List<String> sourceIds = new ArrayList<>();
        for (String sourceId : List.of(PeerDescriptors.standardId(peer.getId()), PeerDescriptors.trackId(peer.getId()),
                PeerDescriptors.eventsId(peer.getId()))) {
            if (registry.contains(sourceId)) {
                sourceIds.add(sourceId);
            }
        }
        out.put("sourceIds", sourceIds);
        out.put("createdAt", peer.getCreatedAt() == null ? null : peer.getCreatedAt().toString());
        out.put("updatedAt", peer.getUpdatedAt() == null ? null : peer.getUpdatedAt().toString());
        return out;
    }

    /** The peers with their replication state (the MCP {@code list_peers} and {@code peer_status} tools). */
    public List<Map<String, Object>> views() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (PeerEntity peer : list()) {
            out.add(view(peer));
        }
        return out;
    }

    // ------------------------------------------------------------------------- sources

    /**
     * Registers (or redefines) the peer's sources and switches them on or off with the peer.
     *
     * @param created true for a new peer (its sources' settings rows are created)
     * @return the peer's source ids
     */
    Set<String> register(PeerEntity peer, boolean created) {
        PeerSelection selection = PeerSelection.parse(peer.getSelection(), "selection", new ArrayList<>());
        boolean pull = MODE_PULL.equals(peer.getMode());
        Set<String> localKinds = new TreeSet<>();
        Set<String> trackKinds = new TreeSet<>();
        for (SourceRegistry.Registration registration : registry.all()) {
            if (PeerDescriptors.isPeerSource(registration.id())) {
                continue;
            }
            localKinds.addAll(registration.descriptor().kinds());
            if (registration.descriptor().historyTier() == HistoryTier.TRACK) {
                trackKinds.addAll(registration.descriptor().kinds());
            }
        }
        List<String> kinds = PeerDescriptors.recordKinds(selection, localKinds);
        Set<String> ids = new HashSet<>();
        SourceDescriptor standard = PeerDescriptors.standard(peer.getId(), peer.getName(), pull,
                peer.getReliability(), kinds);
        put(standard, pull ? new PeerAdapter(standard, peer.getId(), runtime) : null, peer.isEnabled());
        ids.add(standard.id());
        String trackId = PeerDescriptors.trackId(peer.getId());
        if (PeerDescriptors.wantsTrack(selection, trackKinds)) {
            SourceDescriptor track = PeerDescriptors.track(peer.getId(), peer.getName(), pull, peer.getReliability(),
                    kinds, selection.trackTtl());
            put(track, pull ? new PeerTrackAdapter(track, peer.getId(), runtime) : null, peer.isEnabled());
            ids.add(track.id());
        } else if (registry.contains(trackId)) {
            registry.unregisterDynamic(trackId);
        }
        SourceDescriptor events = PeerDescriptors.events(peer.getId(), peer.getName(), peer.getReliability());
        put(events, null, peer.isEnabled());
        ids.add(events.id());
        if (!peer.isEnabled()) {
            runtime.stop(peer.getId());
        }
        return ids;
    }

    private void put(SourceDescriptor descriptor, SourceAdapter adapter, boolean enabled) {
        boolean existed = registry.contains(descriptor.id());
        if (existed) {
            registry.redefineDynamic(descriptor, adapter);
        } else {
            registry.registerDynamic(descriptor, adapter);
        }
        SourceService service = sources.getIfAvailable();
        if (service == null) {
            return;
        }
        try {
            if (!existed) {
                service.createDynamicSettings(descriptor, enabled);
            } else {
                service.refreshDynamicSettings(descriptor);
                service.setDynamicEnabled(descriptor.id(), enabled);
            }
        } catch (RuntimeException e) {
            log.warn("Could not store the settings of {}: {}", descriptor.id(), e.getMessage());
        }
    }

    private void restart(String sourceId) {
        StreamingSupervisor s = supervisor.getIfAvailable();
        if (s != null) {
            try {
                s.restart(sourceId);
            } catch (RuntimeException e) {
                log.warn("Could not restart the stream of {}: {}", sourceId, e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------- validation

    private String name(JsonNode body, List<FieldError> errors) {
        String name = text(body, "name");
        if (name == null || name.length() > 200) {
            errors.add(new FieldError("name", "is required, 1 to 200 characters"));
        }
        return name;
    }

    private String baseUrl(JsonNode body, List<FieldError> errors) {
        String url = text(body, "baseUrl");
        if (url == null) {
            errors.add(new FieldError("baseUrl", "is required"));
            return null;
        }
        String problem = policy.saveProblem(url, OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        if (problem != null) {
            errors.add(new FieldError("baseUrl", problem));
            throw OutboundUrlPolicy.notAllowed(errors);
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private PeerSelection selection(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("selection");
        if (node == null || node.isNull()) {
            return PeerSelection.all();
        }
        if (!node.isObject()) {
            errors.add(new FieldError("selection", "must be an object"));
            return PeerSelection.all();
        }
        PeerSelection selection = PeerSelection.parse(json.convertValue(node, MAP), "selection", errors);
        for (String kind : selection.kinds()) {
            if (!ID.matcher(kind).matches()) {
                errors.add(new FieldError("selection.kinds", "'" + kind + "' is not a kebab-case kind"));
            }
        }
        if (selection.placeId() != null && Boolean.FALSE.equals(
                ai.foresignal.security.restrict.PlaceAreas.exists(selection.placeId()))) {
            errors.add(new FieldError("selection.placeId", "unknown place"));
        }
        return selection;
    }

    private static String reliability(JsonNode body, List<FieldError> errors) {
        String value = text(body, "reliability");
        if (value != null && !RELIABILITY.contains(value)) {
            errors.add(new FieldError("reliability", "must be one of A, B, C, D, E, F"));
            return null;
        }
        return value;
    }

    private static void throwIf(List<FieldError> errors) {
        if (!errors.isEmpty()) {
            throw new ValidationException("The peer is not valid.", errors);
        }
    }

    private static String text(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        return value != null && value.isString() && !value.asString().isBlank() ? value.asString().strip() : null;
    }
}
