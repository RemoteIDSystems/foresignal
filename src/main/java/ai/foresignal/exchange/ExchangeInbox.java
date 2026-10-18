package ai.foresignal.exchange;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.AttachmentBlobStore;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.collab.PersonalNotifier;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerRepository;
import ai.foresignal.federation.PeerService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.provenance.JwsSigner;
import ai.foresignal.report.ReportRenderer;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Receives signed intelligence packages (prompt 132, RPT-2): from a 113 peer ({@code receiveExchangePackage}, the
 * peer's ingest key) or as an uploaded file ({@code importExchangePackage}, admin).
 *
 * <ol>
 *   <li><b>Verify</b> before anything is stored: the ZIP is well formed and within {@code exchange.maxPackageMb}; the
 *       detached JWS over {@code manifest.json} verifies with the sender's key (an admin-pinned JWKS first, then the
 *       delivering peer's pinned key, then the JWKS fetched from the peer's own base URL), the signing key is the one
 *       the manifest names, and every content file matches its hash. Anything else is
 *       {@code 400 PACKAGE_SIGNATURE_INVALID}.</li>
 *   <li><b>Recipients</b> must exist here ({@code 422 PACKAGE_RECIPIENT_UNKNOWN}); {@code *} is accepted only for
 *       GREEN and CLEAR.</li>
 *   <li><b>Loops and duplicates:</b> a package this instance originated or already passed through is
 *       {@code 409 PACKAGE_LOOP}; one already imported is {@code 409 PACKAGE_ALREADY_IMPORTED}.</li>
 *   <li><b>Import read-only:</b> the package is stored, attachments go to the content-addressed media store, the
 *       renditions are rendered again here (sanitized, marked with their origin), and every cited object becomes a
 *       {@code remote:{instanceId}/{type}:{id}} reference with a local snapshot. Nothing enters local evidence groups,
 *       the ledger or any analytic table; an analyst may copy an item into a local investigation deliberately.</li>
 *   <li><b>TLP locally:</b> a package is visible only to its named recipients (RED: to them alone, not even admins),
 *       or to everyone for {@code *}. Named recipients get {@code package.received}.</li>
 * </ol>
 */
@Service
public class ExchangeInbox {

    private static final Logger log = LoggerFactory.getLogger(ExchangeInbox.class);

    /** Key source: the JWKS fetched from the delivering peer. */
    public static final String PEER_JWKS = "peer-jwks";

    /** Key source: an admin-pinned JWKS or the peer's pinned key. */
    public static final String PINNED = "pinned";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ReceivedPackageRepository received;
    private final ReceivedObjectRepository objects;
    private final ExchangeSenderKeyRepository senderKeys;
    private final ExchangeService exchange;
    private final ExchangeFiles files;
    private final ExchangeRenditions renditions;
    private final AttachmentBlobStore blobs;
    private final FederationIdentity identity;
    private final PeerRepository peers;
    private final PeerService peerService;
    private final PeerExchangeClient client;
    private final PrincipalRepository principals;
    private final PersonalNotifier notifier;
    private final JwsSigner signer;
    private final JsonMapper json;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the inbox. */
    public ExchangeInbox(ReceivedPackageRepository received, ReceivedObjectRepository objects,
            ExchangeSenderKeyRepository senderKeys, ExchangeService exchange, ExchangeFiles files,
            ExchangeRenditions renditions, AttachmentBlobStore blobs, FederationIdentity identity, PeerRepository peers,
            PeerService peerService, PeerExchangeClient client, PrincipalRepository principals,
            PersonalNotifier notifier, JwsSigner signer, JsonMapper json, Clock clock,
            PlatformTransactionManager transactions) {
        this.received = received;
        this.objects = objects;
        this.senderKeys = senderKeys;
        this.exchange = exchange;
        this.files = files;
        this.renditions = renditions;
        this.blobs = blobs;
        this.identity = identity;
        this.peers = peers;
        this.peerService = peerService;
        this.client = client;
        this.principals = principals;
        this.notifier = notifier;
        this.signer = signer;
        this.json = json;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    // ------------------------------------------------------------------------- receive

    /**
     * A package delivered by a peer ({@code receiveExchangePackage}).
     *
     * @param zip the package
     * @param peerId the peer whose ingest key authenticated the call
     * @return the {@code ExchangeReceipt} ({@code delivered})
     */
    public Map<String, Object> receive(byte[] zip, String peerId) {
        PeerEntity peer = peerId == null ? null : peers.findById(peerId).orElse(null);
        if (peer == null) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "A peer ingest key is required.");
        }
        ReceivedPackageEntity rp = accept(zip, "peer", peer, null, null);
        return receipt(rp.getPackageId(), "delivered", null, rp.getReceivedAt());
    }

    /**
     * An uploaded package ({@code importExchangePackage}; admin).
     *
     * @param zip the package
     * @param jwksText the sender's JWKS to pin, or null
     * @param caller the admin
     * @return the received package
     */
    public ReceivedPackageEntity importPackage(byte[] zip, String jwksText, ObjectCaller caller) {
        Map<String, Object> jwks = null;
        if (jwksText != null && !jwksText.isBlank()) {
            try {
                jwks = json.readValue(jwksText, MAP);
            } catch (RuntimeException e) {
                throw ValidationException.forField("jwks", "must be a JWKS JSON document");
            }
            if (!(jwks.get("keys") instanceof List<?>)) {
                throw ValidationException.forField("jwks", "must be a JWKS JSON document with keys");
            }
        }
        return accept(zip, "import", null, jwks, caller);
    }

    /** Verifies and imports a package. */
    ReceivedPackageEntity accept(byte[] zip, String via, PeerEntity peer, Map<String, Object> pinnedJwks,
            ObjectCaller importer) {
        PackageCodec.Contents pkg;
        try {
            pkg = PackageCodec.read(zip, exchange.maxBytes());
        } catch (PackageCodec.TooLargeException e) {
            throw ExchangeService.tooLarge(e.getMessage() + " (exchange.maxPackageMb).");
        } catch (PackageCodec.FormatException e) {
            throw invalid("The package cannot be verified: " + e.getMessage() + ".", PackageVerifier.INVALID);
        }
        Map<String, Object> manifest;
        try {
            manifest = json.readValue(pkg.manifestBytes(), MAP);
        } catch (RuntimeException e) {
            throw invalid("The manifest is not JSON.", PackageVerifier.INVALID);
        }
        Manifest m = Manifest.parse(manifest);
        String ours = identity.instanceId();
        if (ours.equals(m.senderInstance) || m.chainInstances.contains(ours)) {
            throw new ConflictException(ErrorCodes.PACKAGE_LOOP, "Package " + m.packageId + " originated on, or"
                    + " already passed through, this instance.");
        }
        if (received.findByPackageId(m.packageId).isPresent()) {
            throw new ConflictException(ErrorCodes.PACKAGE_ALREADY_IMPORTED, "Package " + m.packageId
                    + " was already imported.");
        }
        Verified v = verifySignature(pkg, m, peer, pinnedJwks);
        if (!v.result.valid()) {
            throw invalid("The package signature does not verify: " + v.result.error() + ".", v.result.status());
        }
        if (!m.keyId.equals(v.result.kid())) {
            throw invalid("The package is signed with " + v.result.kid() + " but its manifest names " + m.keyId + ".",
                    PackageVerifier.INVALID);
        }
        List<String> problems = PackageCodec.verifyHashes((Map<?, ?>) manifest.get("contentHashes"),
                pkg.contents());
        if (!problems.isEmpty()) {
            throw invalid("The package content does not match its manifest: " + String.join("; ", problems) + ".",
                    PackageVerifier.VALID);
        }
        Local local = recipients(m);
        if (pinnedJwks != null) {
            pin(m.senderInstance, pinnedJwks, importer);
        }
        return store(zip, pkg, manifest, m, v, local, via, peer, importer);
    }

    /** {@code 400 PACKAGE_SIGNATURE_INVALID}. */
    static ForesignalException invalid(String detail, String signatureStatus) {
        return new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.PACKAGE_SIGNATURE_INVALID, detail)
                .withProperty("signatureStatus", signatureStatus);
    }

    /** The parsed fields of a manifest the inbox relies on. */
    static final class Manifest {

        String packageId;
        String type;
        String objectRef;
        String senderInstance;
        String senderName;
        String keyId;
        Tlp tlp;
        String title;
        String message;
        Object recipients;
        List<Map<String, Object>> authors = new ArrayList<>();
        List<Map<String, Object>> chain = new ArrayList<>();
        final Set<String> chainInstances = new LinkedHashSet<>();

        /** The origin instance: the chain's origin entry, else the sender. */
        String origin() {
            for (Map<String, Object> e : chain) {
                if ("origin".equals(e.get("role")) && e.get("instanceId") != null) {
                    return String.valueOf(e.get("instanceId"));
                }
            }
            return senderInstance;
        }

        @SuppressWarnings("unchecked")
        static Manifest parse(Map<String, Object> manifest) {
            Manifest m = new Manifest();
            m.packageId = string(manifest, "packageId");
            if (m.packageId == null || !Ulids.isUlid(m.packageId)) {
                throw invalid("The manifest has no valid packageId.", PackageVerifier.INVALID);
            }
            m.type = string(manifest, "type");
            if (m.type == null || !ExchangeVocabulary.TYPES.contains(m.type)) {
                throw invalid("The manifest type must be one of " + ExchangeVocabulary.TYPES + ".",
                        PackageVerifier.INVALID);
            }
            m.objectRef = string(manifest, "objectRef");
            if (m.objectRef == null || !ObjectRef.isValid(m.objectRef)
                    || ObjectRef.parse(m.objectRef).isRemote()) {
                throw invalid("The manifest objectRef is not a valid reference.", PackageVerifier.INVALID);
            }
            Map<String, Object> sender = manifest.get("sender") instanceof Map<?, ?> s ? (Map<String, Object>) s
                    : Map.of();
            m.senderInstance = string(sender, "instanceId");
            m.senderName = string(sender, "instanceName");
            m.keyId = string(sender, "keyId");
            if (m.senderInstance == null || m.senderInstance.isBlank() || m.senderInstance.contains("/")
                    || m.keyId == null) {
                throw invalid("The manifest names no sender instance and key id.", PackageVerifier.INVALID);
            }
            try {
                m.tlp = Tlp.fromWire(string(manifest, "tlp"));
            } catch (IllegalArgumentException e) {
                m.tlp = null;
            }
            if (m.tlp == null) {
                throw invalid("The manifest has no valid TLP label.", PackageVerifier.INVALID);
            }
            m.title = string(manifest, "title");
            m.message = string(manifest, "message");
            m.recipients = manifest.get("recipients");
            for (Object a : ExchangeRenditions.list(manifest.get("authors"))) {
                if (a instanceof Map<?, ?> author) {
                    m.authors.add(new LinkedHashMap<>((Map<String, Object>) author));
                }
            }
            for (Object e : ExchangeRenditions.list(manifest.get("provenanceChain"))) {
                if (e instanceof Map<?, ?> entry) {
                    m.chain.add(new LinkedHashMap<>((Map<String, Object>) entry));
                    if (entry.get("instanceId") != null && transportHop(entry, m.objectRef)) {
                        m.chainInstances.add(String.valueOf(entry.get("instanceId")));
                    }
                }
            }
            if (!(manifest.get("contentHashes") instanceof Map<?, ?>)) {
                throw invalid("The manifest has no contentHashes.", PackageVerifier.INVALID);
            }
            return m;
        }

        private static String string(Map<String, Object> map, String key) {
            return map.get(key) instanceof String s ? s : null;
        }
    }

    /**
     * A signature check and where the key came from.
     *
     * @param result the verification
     * @param keySource {@value #PINNED} or {@value #PEER_JWKS}
     */
    record Verified(PackageVerifier.Result result, String keySource) {}

    /** Verifies the manifest signature with the best key available. */
    Verified verifySignature(PackageCodec.Contents pkg, Manifest m, PeerEntity peer, Map<String, Object> pinnedJwks) {
        byte[] bytes = pkg.manifestBytes();
        String jws = pkg.signature();
        if (pinnedJwks != null) {
            return new Verified(PackageVerifier.verify(bytes, jws, pinnedJwks), PINNED);
        }
        Optional<ExchangeSenderKeyEntity> pinned = senderKeys.findById(m.senderInstance);
        if (pinned.isPresent() && PackageVerifier.holds(pinned.get().getJwks(), PackageVerifier.kid(jws))) {
            return new Verified(PackageVerifier.verify(bytes, jws, pinned.get().getJwks()), PINNED);
        }
        PeerEntity source = peer;
        if (source == null) {
            source = exchange.peerFor(m.senderInstance).orElse(null);
        }
        if (source == null) {
            return new Verified(new PackageVerifier.Result(PackageVerifier.UNKNOWN_KEY, PackageVerifier.kid(jws),
                    "no key is pinned for " + m.senderInstance + " and no peer is registered for it; pin its JWKS"
                            + " with the import"), null);
        }
        if (source.getRemoteInstanceId() != null && !source.getRemoteInstanceId().equals(m.senderInstance)) {
            return new Verified(new PackageVerifier.Result(PackageVerifier.INVALID, PackageVerifier.kid(jws),
                    "peer " + source.getId() + " is instance " + source.getRemoteInstanceId() + " but the manifest"
                            + " names " + m.senderInstance), null);
        }
        String kid = PackageVerifier.kid(jws);
        if (source.getPinnedKid() != null && source.getPinnedKey() != null && source.getPinnedKid().equals(kid)) {
            return new Verified(PackageVerifier.verifyPinned(bytes, jws, source.getPinnedKey()), PINNED);
        }
        if (source.getPinnedKid() != null && !source.getPinnedKid().equals(kid)) {
            return new Verified(new PackageVerifier.Result(PackageVerifier.INVALID, kid, "the peer's pinned key is "
                    + source.getPinnedKid() + "; re-pin it on the peer after a rotation"), null);
        }
        try {
            return new Verified(PackageVerifier.verify(bytes, jws, client.jwks(source.getBaseUrl())), PEER_JWKS);
        } catch (PeerExchangeClient.DeliveryException e) {
            return new Verified(new PackageVerifier.Result(PackageVerifier.UNKNOWN_KEY, kid, e.getMessage()), null);
        }
    }

    /**
     * The local recipients of a package.
     *
     * @param everyone true for {@code *}
     * @param principalIds the named local principals
     */
    record Local(boolean everyone, List<String> principalIds) {}

    /**
     * Resolves the recipients on this instance.
     *
     * @throws ForesignalException {@code 422 PACKAGE_RECIPIENT_UNKNOWN}
     */
    Local recipients(Manifest m) {
        boolean wide = m.tlp == Tlp.GREEN || m.tlp == Tlp.CLEAR;
        if (ExchangeVocabulary.EVERYONE.equals(m.recipients)) {
            if (!wide) {
                throw recipientUnknown("A " + m.tlp.wireValue() + " package must name its recipients.");
            }
            return new Local(true, List.of());
        }
        String ours = identity.instanceId();
        List<String> handles = null;
        for (Object o : ExchangeRenditions.list(m.recipients)) {
            Map<String, Object> r = ExchangeRenditions.map(o);
            if (ours.equals(r.get("instanceId"))) {
                handles = new ArrayList<>(ExchangeRenditions.list(r.get("principals")).stream()
                        .map(String::valueOf).toList());
            }
        }
        if (handles == null || handles.isEmpty()) {
            throw recipientUnknown("The package is not addressed to this instance (" + ours + ").");
        }
        if (handles.contains(ExchangeVocabulary.EVERYONE)) {
            if (!wide) {
                throw recipientUnknown("A " + m.tlp.wireValue() + " package must name its recipients.");
            }
            return new Local(true, List.of());
        }
        List<String> ids = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String h : handles) {
            Optional<PrincipalEntity> p = principals.findByHandle(h.strip().toLowerCase(Locale.ROOT));
            if (p.isEmpty() || p.get().isDisabled()) {
                missing.add(h);
            } else if (!ids.contains(p.get().getId())) {
                ids.add(p.get().getId());
            }
        }
        if (!missing.isEmpty()) {
            throw recipientUnknown("No principal here has the handle " + String.join(", ", missing) + ".")
                    .withProperty("handles", missing);
        }
        return new Local(false, ids);
    }

    private static ForesignalException recipientUnknown(String detail) {
        return new ForesignalException(HttpStatus.UNPROCESSABLE_CONTENT, ErrorCodes.PACKAGE_RECIPIENT_UNKNOWN, detail);
    }

    private void pin(String instanceId, Map<String, Object> jwks, ObjectCaller importer) {
        Instant now = clock.instant();
        String by = importer == null ? null : importer.principal();
        ExchangeSenderKeyEntity row = senderKeys.findById(instanceId).orElse(null);
        if (row == null) {
            senderKeys.save(new ExchangeSenderKeyEntity(instanceId, jwks, by, now));
        } else {
            row.repin(jwks, by, now);
            senderKeys.save(row);
        }
    }

    private ReceivedPackageEntity store(byte[] zip, PackageCodec.Contents pkg, Map<String, Object> manifest,
            Manifest m, Verified v, Local local, String via, PeerEntity peer, ObjectCaller importer) {
        Instant now = clock.instant();
        String id = Ulids.next(now);
        String origin = m.origin();
        String remoteRef = ObjectRef.remote(origin, m.objectRef).format();
        Map<String, Object> bundle = null;
        byte[] bundleBytes = pkg.contents().get(ExchangeVocabulary.BUNDLE);
        if (bundleBytes != null) {
            try {
                bundle = json.readValue(bundleBytes, MAP);
            } catch (RuntimeException e) {
                bundle = null;
            }
        }
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, String> mediaBySha = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> e : pkg.contents().entrySet()) {
            String sha = PackageCodec.sha256(e.getValue());
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("path", PackageCodec.key(e.getKey()));
            c.put("sha256", sha);
            c.put("size", (long) e.getValue().length);
            String mediaId = null;
            if (e.getKey().startsWith(ExchangeVocabulary.ATTACHMENTS)) {
                blobs.store(e.getValue(), sha, extension(e.getKey()));
                mediaId = "sha256:" + sha;
                mediaBySha.put(sha, mediaId);
            }
            c.put("mediaId", mediaId);
            contents.add(c);
        }
        Map<String, ReportRenderer.Image> images = images(bundle, pkg.contents());
        String title = m.title != null ? m.title : titleOf(m.type, bundle, m.objectRef);
        String originNote = originNote(m, v, via, now);
        ExchangeRenditions.Rendered rendered = renditions.render(m.type, bundle, m.tlp.wireValue(), images, originNote);
        List<Map<String, Object>> chain = new ArrayList<>(m.chain);
        String importerHandle = importer == null ? "peer:" + (peer == null ? "unknown" : peer.getId())
                : principals.findById(String.valueOf(importer.principal())).map(PrincipalEntity::getHandle)
                        .orElse(String.valueOf(importer.principal()));
        chain.add(ExchangeService.chainEntry(identity.instanceId(), importerHandle, remoteRef, null, "importer", now));
        final Map<String, Object> finalBundle = bundle;
        String relative = ExchangeFiles.received(id);
        files.write(relative, zip);
        ReceivedPackageEntity saved = tx.execute(s -> {
            ReceivedPackageEntity rp = new ReceivedPackageEntity(id, m.packageId, m.type, remoteRef,
                    title.length() > 600 ? title.substring(0, 600) : title, m.tlp.wireValue(), via, now);
            Map<String, Object> sender = new LinkedHashMap<>();
            sender.put("instanceId", m.senderInstance);
            sender.put("instanceName", m.senderName);
            sender.put("keyId", m.keyId);
            sender.put("peerId", peer == null ? null : peer.getId());
            rp.setSender(sender);
            rp.setAuthors(m.authors);
            rp.setVerification(v.result.status(), v.keySource, true);
            rp.setPeerId(peer == null ? null : peer.getId());
            rp.setRecipientIds(local.principalIds());
            rp.setEveryone(local.everyone());
            rp.setMessage(m.message == null || m.message.length() <= 2000 ? m.message : m.message.substring(0, 2000));
            rp.setProvenanceChain(chain);
            rp.setContents(contents);
            rp.setManifest(manifest);
            rp.setBundle(finalBundle);
            rp.setRenditions(rendered.markdown(), rendered.html());
            rp.setFilePath(relative);
            received.save(rp);
            objects.save(new ReceivedObjectEntity(Ulids.next(now), id, remoteRef, mainEnvelope(remoteRef, title, m,
                    finalBundle)));
            Set<String> seen = new LinkedHashSet<>(List.of(remoteRef));
            if (finalBundle != null) {
                for (Object o : ExchangeRenditions.list(finalBundle.get("envelopes"))) {
                    Map<String, Object> env = ExchangeRenditions.map(o);
                    if (!(env.get("ref") instanceof String ref) || !ObjectRef.isValid(ref)
                            || ObjectRef.parse(ref).isRemote()) {
                        continue;
                    }
                    String rref = ObjectRef.remote(origin, ref).format();
                    if (seen.add(rref)) {
                        objects.save(new ReceivedObjectEntity(Ulids.next(now), id, rref, env));
                    }
                }
            }
            return rp;
        });
        Map<String, Object> payload = MeEvents.packageReceived(saved.getId(), m.packageId, m.type, m.senderInstance,
                saved.getTitle(), m.tlp.wireValue(), v.result.status(), now);
        for (String p : local.principalIds()) {
            notifier.send(new PersonalNotifier.Notice(p, MeEvents.PACKAGE_RECEIVED, "Package from "
                    + (m.senderName == null ? m.senderInstance : m.senderName) + ": " + saved.getTitle(), m.message,
                    remoteRef, null, m.tlp.wireValue(), payload));
        }
        log.info("Imported package {} ({}) from {} via {}", m.packageId, m.type, m.senderInstance, via);
        return saved;
    }

    private static Map<String, Object> mainEnvelope(String remoteRef, String title, Manifest m,
            Map<String, Object> bundle) {
        Map<String, Object> env = new LinkedHashMap<>();
        env.put("ref", m.objectRef);
        env.put("type", ObjectRef.parse(m.objectRef).type());
        env.put("title", title);
        env.put("summary", m.message);
        env.put("status", "received");
        env.put("tlp", m.tlp.wireValue());
        if (bundle != null && bundle.get("report") instanceof Map<?, ?> report) {
            env.put("status", report.get("status"));
            env.put("summary", report.get("subtitle") == null ? m.message : report.get("subtitle"));
        }
        return env;
    }

    private static String originNote(Manifest m, Verified v, String via, Instant now) {
        List<String> authors = new ArrayList<>();
        for (Map<String, Object> a : m.authors) {
            authors.add(a.get("handle") + "@" + a.get("instanceId") + ("forwarder".equals(a.get("role"))
                    ? " (forwarder)" : ""));
        }
        return "Received from " + (m.senderName == null ? m.senderInstance : m.senderName + " (" + m.senderInstance
                + ")") + " on " + now + " via " + via + "; authors: " + String.join(", ", authors)
                + "; signature " + v.result.status() + " (" + v.keySource + "). Read-only material from another"
                + " instance, not local analysis.";
    }

    /**
     * True when a chain entry is a hop of this package's own product (its origin, a forwarder, or an importer of the
     * same product), as opposed to the ancestry of material the product cites (prompt 134). Only own hops count for
     * loop prevention: a new report that cites this instance's work may come back (and is then marked
     * {@code CIRCULAR_REPORTING} when used as support), while the same package never does.
     */
    static boolean transportHop(Map<?, ?> entry, String objectRef) {
        Object product = entry.get("productRef");
        if (product == null || objectRef == null) {
            return true;
        }
        String p = String.valueOf(product);
        return p.equals(objectRef) || p.endsWith("/" + objectRef);
    }

    private static String titleOf(String type, Map<String, Object> bundle, String objectRef) {
        if (bundle != null) {
            Map<String, Object> inner = ExchangeRenditions.map(switch (type) {
                case "report" -> bundle.get("report");
                case "investigation" -> bundle.get("investigation");
                case "evidence" -> bundle.get("evidenceItem");
                default -> bundle.get("chronicle");
            });
            for (String key : List.of("title", "name")) {
                if (inner.get(key) instanceof String s && !s.isBlank()) {
                    return s;
                }
            }
        }
        return objectRef;
    }

    /** The map images of a received report: attachment id → the content file of its hash. */
    private static Map<String, ReportRenderer.Image> images(Map<String, Object> bundle, Map<String, byte[]> contents) {
        Map<String, ReportRenderer.Image> out = new LinkedHashMap<>();
        if (bundle == null) {
            return out;
        }
        for (Object o : ExchangeRenditions.list(bundle.get("attachments"))) {
            Map<String, Object> f = ExchangeRenditions.map(o);
            if (!(f.get("attachmentId") instanceof String attachmentId) || !(f.get("sha256") instanceof String sha)) {
                continue;
            }
            for (Map.Entry<String, byte[]> e : contents.entrySet()) {
                if (e.getKey().startsWith(ExchangeVocabulary.ATTACHMENTS + sha + ".")) {
                    String type = f.get("contentType") instanceof String t ? t : "application/octet-stream";
                    if (type.startsWith("image/png") || type.startsWith("image/jpeg")) {
                        out.put(attachmentId, new ReportRenderer.Image(sha, type, e.getValue()));
                    }
                }
            }
        }
        return out;
    }

    private static String extension(String zipPath) {
        int dot = zipPath.lastIndexOf('.');
        String ext = dot < 0 ? "bin" : zipPath.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ext.matches("[a-z0-9]{1,8}") ? ext : "bin";
    }

    // ------------------------------------------------------------------------- reads

    /**
     * True when the caller may see a received package: a named recipient, anyone for {@code *}, and admins except
     * for RED (only its named recipients).
     */
    public static boolean visible(ReceivedPackageEntity rp, ObjectCaller caller) {
        if (caller == null || caller.anonymous()) {
            return false;
        }
        if (rp.isEveryone() || (caller.principal() != null && rp.getRecipientIds().contains(caller.principal()))) {
            return true;
        }
        return caller.admin() && !Tlp.RED.wireValue().equals(rp.getTlp());
    }

    /**
     * A received package the caller may see.
     *
     * @throws NotFoundException {@code UNKNOWN_PACKAGE} otherwise
     */
    public ReceivedPackageEntity get(String id, ObjectCaller caller) {
        ReceivedPackageEntity rp = id == null ? null : received.findById(id).orElse(null);
        if (rp == null || !visible(rp, caller)) {
            throw ExchangeService.unknown(id);
        }
        return rp;
    }

    /** Received packages the caller may see, newest first ({@code listReceivedPackages}). */
    public Map<String, Object> list(ObjectCaller caller, String type, String senderInstanceId, Boolean acknowledged,
            String cursor, int limit) {
        if (type != null && !ExchangeVocabulary.TYPES.contains(type)) {
            throw ValidationException.forField("type", "must be one of " + ExchangeVocabulary.TYPES);
        }
        List<ReceivedPackageEntity> visible = new ArrayList<>();
        for (ReceivedPackageEntity rp : received.findAllByOrderByReceivedAtDescIdDesc()) {
            if (!visible(rp, caller) || (type != null && !type.equals(rp.getType()))) {
                continue;
            }
            if (senderInstanceId != null && !senderInstanceId.equals(rp.getSender().get("instanceId"))) {
                continue;
            }
            if (acknowledged != null && acknowledged != (rp.getAcknowledgedAt() != null)) {
                continue;
            }
            visible.add(rp);
        }
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ReceivedPackageEntity rp : visible.subList(Math.min(offset, visible.size()),
                Math.min(offset + limit, visible.size()))) {
            items.add(view(rp));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < visible.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /**
     * A received rendition, always marked with its origin: Markdown or HTML rendered here, or the JSON bundle with an
     * {@code origin} member.
     */
    public Object rendition(String id, String format, ObjectCaller caller) {
        ReceivedPackageEntity rp = get(id, caller);
        return switch (format) {
            case "md" -> rp.getMarkdown();
            case "html" -> rp.getHtml();
            case "json" -> {
                Map<String, Object> out = new LinkedHashMap<>(rp.getBundle() == null ? Map.of() : rp.getBundle());
                out.put("origin", origin(rp));
                yield out;
            }
            default -> throw ValidationException.forField("format", "must be md, html or json");
        };
    }

    private static Map<String, Object> origin(ReceivedPackageEntity rp) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("receivedId", rp.getId());
        o.put("objectRef", rp.getObjectRef());
        o.put("sender", rp.getSender());
        o.put("authors", rp.getAuthors());
        o.put("signatureStatus", rp.getSignatureStatus());
        o.put("tlp", rp.getTlp());
        o.put("receivedAt", rp.getReceivedAt().toString());
        o.put("readOnly", true);
        return o;
    }

    /**
     * Records the caller's acknowledgement and sends a receipt to the sender when a peer with an exchange key exists
     * for it ({@code acknowledgeReceivedPackage}).
     *
     * @return the {@code ExchangeReceipt}
     */
    public Map<String, Object> acknowledge(String id, ObjectCaller caller) {
        ReceivedPackageEntity rp = get(id, caller);
        Instant now = clock.instant();
        if (rp.getAcknowledgedAt() == null) {
            rp.acknowledge(caller.principal(), now);
            received.save(rp);
        }
        String handle = principals.findById(String.valueOf(caller.principal())).map(PrincipalEntity::getHandle)
                .orElse(null);
        Map<String, Object> receipt = receipt(rp.getPackageId(), "acknowledged", handle, rp.getAcknowledgedAt());
        PeerEntity peer = rp.getPeerId() == null ? null : peers.findById(rp.getPeerId()).orElse(null);
        if (peer == null) {
            peer = exchange.peerFor(String.valueOf(rp.getSender().get("instanceId"))).orElse(null);
        }
        String key = peer == null ? null : peerService.exchangeKey(peer);
        if (peer != null && key != null) {
            try {
                byte[] body = json.writeValueAsBytes(receipt);
                Map<String, String> headers = new LinkedHashMap<>();
                headers.put("Authorization", "Bearer " + key);
                headers.put("Foresignal-Signature", signer.signDetached(body, "application/json"));
                PeerExchangeClient.Response r = client.post(peer.getBaseUrl(), "/v1/exchange/receipts",
                        "application/json", body, headers);
                if (!r.ok()) {
                    log.info("Receipt for package {} refused by {}: {}", rp.getPackageId(), peer.getId(), r.status());
                }
            } catch (PeerExchangeClient.DeliveryException e) {
                log.info("Receipt for package {} not delivered to {}: {}", rp.getPackageId(), peer.getId(),
                        e.getMessage());
            }
        }
        return receipt;
    }

    /** An {@code ExchangeReceipt}. */
    Map<String, Object> receipt(String packageId, String type, String handle, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("packageId", packageId);
        out.put("type", type);
        out.put("receiverInstanceId", identity.instanceId());
        out.put("principalHandle", handle);
        out.put("at", (at == null ? clock.instant() : at).toString());
        return out;
    }

    /** The {@code ReceivedPackage} view. */
    public Map<String, Object> view(ReceivedPackageEntity rp) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", rp.getId());
        out.put("packageId", rp.getPackageId());
        out.put("type", rp.getType());
        out.put("objectRef", rp.getObjectRef());
        out.put("title", rp.getTitle());
        out.put("sender", rp.getSender());
        out.put("authors", rp.getAuthors());
        out.put("tlp", rp.getTlp());
        out.put("signatureStatus", rp.getSignatureStatus());
        out.put("keySource", rp.getKeySource());
        out.put("hashesVerified", rp.isHashesVerified());
        out.put("receivedVia", rp.getReceivedVia());
        out.put("receivedAt", rp.getReceivedAt().toString());
        List<String> handles = new ArrayList<>();
        for (String p : rp.getRecipientIds()) {
            handles.add(principals.findById(p).map(PrincipalEntity::getHandle).orElse(p));
        }
        out.put("recipients", rp.isEveryone() ? List.of(ExchangeVocabulary.EVERYONE) : handles);
        out.put("message", rp.getMessage());
        out.put("acknowledgedAt", rp.getAcknowledgedAt() == null ? null : rp.getAcknowledgedAt().toString());
        out.put("provenanceChain", rp.getProvenanceChain());
        out.put("contents", rp.getContents());
        return out;
    }
}
