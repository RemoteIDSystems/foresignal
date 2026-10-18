package ai.foresignal.exchange;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleExporter;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.AttachmentBlobStore;
import ai.foresignal.collab.AttachmentEntity;
import ai.foresignal.collab.AttachmentRepository;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.EvidenceItemEntity;
import ai.foresignal.collab.EvidenceItemService;
import ai.foresignal.collab.PrincipalViews;
import ai.foresignal.collab.ShareService;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerRepository;
import ai.foresignal.federation.PeerService;
import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationItemEntity;
import ai.foresignal.intel.InvestigationService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.provenance.ManifestSigner;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportPublisher;
import ai.foresignal.report.ReportRenderer;
import ai.foresignal.report.ReportRevisionEntity;
import ai.foresignal.report.ReportService;
import ai.foresignal.report.ReportVocabulary;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds, signs and delivers intelligence packages (prompt 132, {@code createExchangePackage}).
 *
 * <ul>
 *   <li><b>What:</b> a published report revision (its bundle, renditions and attachments), an investigation (its
 *       visible items and their snapshots), an evidence item (with its attachments), a chronicle extract (bounded by
 *       window, layers and {@code exchange.maxPackageMb}) or a received package being forwarded.</li>
 *   <li><b>TLP</b> is checked per recipient before anything is built ({@code 403 TLP_VIOLATION} naming the rule):
 *       {@code *} needs GREEN or CLEAR, named principals on another instance follow the 130 remote-recipient
 *       rules.</li>
 *   <li><b>Egress:</b> every cited envelope whose sources may not leave on the federation path (private,
 *       {@code LOCAL_ONLY}, not redistributable) is left out and reduced to counts per type in {@code reductions};
 *       the renditions are rendered again from what remains.</li>
 *   <li><b>Signing:</b> {@code manifest.json} carries the sender instance and key id, the authors, TLP, recipients,
 *       content hashes and provenance chain, and is signed with a detached JWS over its exact bytes (109).</li>
 *   <li><b>Loop prevention</b> (113): a received package is never sent back to an instance already in its provenance
 *       chain ({@code 409 PACKAGE_LOOP}); a forward keeps the original authors and adds the forwarder.</li>
 *   <li><b>Delivery:</b> {@code deliver=peer} POSTs the ZIP to each recipient instance's 113 peer
 *       ({@code {baseUrl}/v1/exchange/inbox}, with the peer's exchange key); {@code file} keeps it for download.</li>
 * </ul>
 */
@Service
public class ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

    /** The members of {@code ExchangePackageInput}. */
    static final Set<String> FIELDS = Set.of("objectRef", "recipients", "message", "deliver", "extract");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ExchangePackageRepository packages;
    private final ReceivedPackageRepository received;
    private final ExchangeFiles files;
    private final ExchangeRenditions renditions;
    private final ReportService reports;
    private final ReportPublisher publisher;
    private final InvestigationService investigations;
    private final EvidenceItemService evidence;
    private final ChronicleService chronicles;
    private final ChronicleExporter chronicleExporter;
    private final AccessResolver access;
    private final AttachmentRepository attachmentRows;
    private final AttachmentBlobStore blobs;
    private final EgressPolicy egress;
    private final FederationIdentity identity;
    private final SigningKeyService signingKeys;
    private final ManifestSigner signer;
    private final PeerRepository peers;
    private final PeerService peerService;
    private final PeerExchangeClient client;
    private final AppSettingService settings;
    private final PrincipalRepository principals;
    private final PrincipalViews views;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public ExchangeService(ExchangePackageRepository packages, ReceivedPackageRepository received, ExchangeFiles files,
            ExchangeRenditions renditions, ReportService reports, ReportPublisher publisher,
            InvestigationService investigations, EvidenceItemService evidence, ChronicleService chronicles,
            ChronicleExporter chronicleExporter, AccessResolver access, AttachmentRepository attachmentRows,
            AttachmentBlobStore blobs, EgressPolicy egress, FederationIdentity identity, SigningKeyService signingKeys,
            ManifestSigner signer, PeerRepository peers, PeerService peerService, PeerExchangeClient client,
            AppSettingService settings, PrincipalRepository principals, PrincipalViews views, JsonMapper json,
            Clock clock) {
        this.packages = packages;
        this.received = received;
        this.files = files;
        this.renditions = renditions;
        this.reports = reports;
        this.publisher = publisher;
        this.investigations = investigations;
        this.evidence = evidence;
        this.chronicles = chronicles;
        this.chronicleExporter = chronicleExporter;
        this.access = access;
        this.attachmentRows = attachmentRows;
        this.blobs = blobs;
        this.egress = egress;
        this.identity = identity;
        this.signingKeys = signingKeys;
        this.signer = signer;
        this.peers = peers;
        this.peerService = peerService;
        this.client = client;
        this.settings = settings;
        this.principals = principals;
        this.views = views;
        this.json = json;
        this.clock = clock;
    }

    /** The largest package built or accepted, in bytes ({@code exchange.maxPackageMb}, default 200). */
    public long maxBytes() {
        double mb = ExchangeVocabulary.DEFAULT_MAX_MB;
        try {
            mb = settings.get(SettingKeys.EXCHANGE_MAX_PACKAGE_MB, Double.class).filter(v -> v > 0)
                    .orElse((double) ExchangeVocabulary.DEFAULT_MAX_MB);
        } catch (RuntimeException e) {
            mb = ExchangeVocabulary.DEFAULT_MAX_MB;
        }
        return (long) (mb * 1024 * 1024);
    }

    /** {@code 413 PACKAGE_TOO_LARGE}. */
    static ForesignalException tooLarge(String detail) {
        return new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.PACKAGE_TOO_LARGE, detail);
    }

    /** {@code 404 UNKNOWN_PACKAGE}. */
    static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_PACKAGE, "No package '" + id + "' exists, or you may not see"
                + " it.");
    }

    // ------------------------------------------------------------------------- recipients

    /**
     * Checks a TLP label against every recipient (130 rules); an instance whose peer is marked {@code sameOperator}
     * counts as the owner's organization.
     *
     * @throws ForesignalException {@code 403 TLP_VIOLATION} naming the rule
     */
    void checkTlp(Tlp tlp, ExchangeTlp.Recipients recipients) {
        ExchangeTlp.enforce(tlp, recipients, instance -> peerFor(instance).map(PeerEntity::isSameOperator)
                .orElse(false));
    }

    /** The enabled peer of a remote instance: by verified remote instance id, else by peer id. */
    Optional<PeerEntity> peerFor(String instanceId) {
        for (PeerEntity p : peers.findByRemoteInstanceIdOrderByIdAsc(instanceId)) {
            if (p.isEnabled()) {
                return Optional.of(p);
            }
        }
        return peers.findById(instanceId).filter(PeerEntity::isEnabled);
    }

    // ------------------------------------------------------------------------- create

    /**
     * What a build gathered before signing.
     */
    static final class Built {

        String type;
        String objectRef;
        Tlp tlp;
        String title;
        final List<Map<String, Object>> authors = new ArrayList<>();
        final List<Map<String, Object>> chain = new ArrayList<>();
        final Map<String, Object> contents = new LinkedHashMap<>();
        final List<Map<String, Object>> reductions = new ArrayList<>();
        final Set<String> loopInstances = new LinkedHashSet<>();
        final List<Path> temps = new ArrayList<>();
    }

    /**
     * Builds a signed package and delivers it ({@code createExchangePackage}; agent).
     *
     * @param body the {@code ExchangePackageInput}
     * @param caller the creator
     * @return the package with its delivery state
     */
    public ExchangePackageEntity create(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "ExchangePackageInput", FIELDS);
        String objectRef = CollabInputs.required(body, "objectRef", 3, ObjectRef.MAX_LENGTH).strip();
        ObjectRef ref = ObjectRef.parse(objectRef, "objectRef");
        ExchangeTlp.Recipients recipients = ExchangeTlp.parse(body.get("recipients"));
        String message = CollabInputs.text(body, "message", 0, 2000);
        String deliver = CollabInputs.text(body, "deliver", 1, 8);
        deliver = deliver == null ? "peer" : deliver;
        if (!List.of("peer", "file").contains(deliver)) {
            throw ValidationException.forField("deliver", "must be peer or file");
        }
        if (!recipients.everyone() && recipients.byInstance().containsKey(identity.instanceId())) {
            throw ValidationException.forField("recipients", "names this instance; send to principals here with"
                    + " POST /v1/reports/{id}/send");
        }
        Built built = switch (ref.type()) {
            case ObjectTypes.REPORT -> report(ref, caller);
            case ObjectTypes.INVESTIGATION -> investigation(ref, caller);
            case ObjectTypes.EVIDENCE_ITEM -> evidenceItem(ref, caller);
            case ObjectTypes.CHRONICLE -> chronicle(ref, body.get("extract"), caller);
            case ObjectTypes.REMOTE -> forward(ref, caller);
            default -> throw ValidationException.forField("objectRef", "must be a report, investigation, chronicle,"
                    + " evidence-item or remote reference");
        };
        try {
            if (!recipients.everyone()) {
                for (String instance : recipients.byInstance().keySet()) {
                    if (built.loopInstances.contains(instance)) {
                        throw new ConflictException(ErrorCodes.PACKAGE_LOOP, "The package came from " + instance
                                + " (or passed through it); it is not sent back unchanged.");
                    }
                }
            }
            checkTlp(built.tlp, recipients);
            return sign(built, recipients, message, deliver, caller);
        } finally {
            for (Path p : built.temps) {
                files.delete(p);
            }
        }
    }

    private ExchangePackageEntity sign(Built built, ExchangeTlp.Recipients recipients, String message, String deliver,
            ObjectCaller caller) {
        Instant now = clock.instant();
        String id = Ulids.next(now);
        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("instanceId", identity.instanceId());
        sender.put("instanceName", identity.instanceName());
        sender.put("keyId", signingKeys.activeKid());
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("packageId", id);
        manifest.put("type", built.type);
        manifest.put("objectRef", built.objectRef);
        manifest.put("title", built.title);
        manifest.put("sender", sender);
        manifest.put("authors", built.authors);
        manifest.put("tlp", built.tlp.wireValue());
        manifest.put("createdAt", now.toString());
        manifest.put("recipients", recipients.toManifest());
        manifest.put("contentHashes", PackageCodec.hashes(built.contents));
        manifest.put("message", message);
        manifest.put("provenanceChain", built.chain);
        byte[] manifestBytes = json.writeValueAsBytes(manifest);
        String signature = signer.sign(manifestBytes);
        Path temp = files.temp(".zip");
        long size;
        try {
            try (OutputStream out = Files.newOutputStream(temp)) {
                PackageCodec.write(out, manifestBytes, signature, built.contents);
            }
            size = Files.size(temp);
        } catch (IOException e) {
            files.delete(temp);
            throw new UncheckedIOException(e);
        }
        if (size > maxBytes()) {
            files.delete(temp);
            throw tooLarge("The package is " + size + " bytes; the limit is " + maxBytes()
                    + " bytes (exchange.maxPackageMb).");
        }
        String relative = ExchangeFiles.sent(id);
        files.move(temp, relative);
        ExchangePackageEntity p = new ExchangePackageEntity(id, built.type, built.objectRef, built.tlp.wireValue(),
                caller.principal(), now);
        p.setManifest(manifest, new String(manifestBytes, StandardCharsets.UTF_8), signature);
        p.setFile(relative, size);
        p.setReductions(built.reductions);
        p.setDeliver(deliver);
        p.setStatus(ExchangeVocabulary.READY);
        packages.save(p);
        if ("peer".equals(deliver)) {
            deliver(p, recipients);
        }
        return packages.findById(id).orElse(p);
    }

    // ------------------------------------------------------------------------- content

    private Built report(ObjectRef ref, ObjectCaller caller) {
        ReportEntity r = reports.require(ref.id(), caller, AccessAction.VIEW);
        ReportRevisionEntity rev = publisher.revision(r, null);
        if (ReportVocabulary.RETRACTED.equals(rev.getStatus())) {
            throw new ConflictException(ErrorCodes.REPORT_NOT_PUBLISHED, "The latest revision of " + r.getDisplayId()
                    + " is retracted.");
        }
        Built b = new Built();
        b.type = "report";
        b.objectRef = ReportService.ref(r.getId());
        b.title = rev.getTitle();
        b.tlp = Tlp.stricter(Tlp.fromWire(rev.getTlp()), reports.tlp(r));
        for (String a : r.getAuthors()) {
            b.authors.add(author(a, "author"));
        }
        b.chain.addAll(reportChain(b.objectRef, handle(reports.record(r).getOwner()), rev.getManifestId(),
                rev.getPublishedAt()));
        Map<String, Object> bundle = copy(rev.getBundle());
        Set<String> excluded = reduce(bundle, b.reductions);
        filterReport(bundle, excluded);
        Map<String, ReportRenderer.Image> images = new LinkedHashMap<>();
        for (Object o : ExchangeRenditions.list(bundle.get("attachments"))) {
            Map<String, Object> f = ExchangeRenditions.map(o);
            if (!(f.get("attachmentId") instanceof String attachmentId)) {
                continue;
            }
            attachmentRows.findById(attachmentId).ifPresent(a -> {
                byte[] bytes = blobs.read(a.getStoragePath());
                b.contents.put(ExchangeVocabulary.ATTACHMENTS + a.getSha256() + "." + extension(a.getContentType()),
                        bytes);
                if (a.isImage()) {
                    images.put(a.getId(), new ReportRenderer.Image(a.getSha256(), a.getContentType(), bytes));
                }
            });
        }
        content(b, bundle, images);
        return b;
    }

    private Built investigation(ObjectRef ref, ObjectCaller caller) {
        InvestigationEntity inv = investigations.get(ref.id());
        ShareableObjectEntity rec = access.require(caller, ObjectTypes.INVESTIGATION + ":" + inv.getId(),
                AccessAction.VIEW);
        Built b = new Built();
        b.type = "investigation";
        b.objectRef = ObjectTypes.INVESTIGATION + ":" + inv.getId();
        b.title = inv.getTitle();
        b.tlp = Tlp.fromWire(rec.getTlp());
        b.authors.add(author(rec.getOwner(), "author"));
        b.chain.add(chainEntry(identity.instanceId(), handle(rec.getOwner()), b.objectRef, null, "origin",
                clock.instant()));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", inv.getId());
        view.put("ref", b.objectRef);
        view.put("displayId", inv.getDisplayId());
        view.put("title", inv.getTitle());
        view.put("status", inv.getStatus());
        view.put("summary", inv.getSummary());
        view.put("createdAt", inv.getCreatedAt().toString());
        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> envelopes = new ArrayList<>();
        for (InvestigationItemEntity item : investigations.items(inv.getId(), false)) {
            Map<String, Object> snap = item.getSnapshot() == null ? Map.of() : item.getSnapshot();
            Map<String, Object> env = ExchangeRenditions.map(snap.get("envelope"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("itemType", item.getItemType());
            m.put("ref", env.get("ref") instanceof String r ? r : null);
            m.put("title", env.get("title") != null ? env.get("title") : item.getNote());
            m.put("note", item.getNote());
            m.put("addedAt", item.getAddedAt().toString());
            m.put("envelope", env.isEmpty() ? null : env);
            items.add(m);
            if (!env.isEmpty()) {
                envelopes.add(env);
            }
        }
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("format", "foresignal-investigation-bundle");
        bundle.put("version", "1.0.0");
        bundle.put("tlp", b.tlp.wireValue());
        bundle.put("investigation", view);
        bundle.put("items", items);
        bundle.put("envelopes", envelopes);
        Set<String> excluded = reduce(bundle, b.reductions);
        items.removeIf(i -> i.get("ref") != null && excluded.contains(String.valueOf(i.get("ref"))));
        content(b, bundle, Map.of());
        return b;
    }

    private Built evidenceItem(ObjectRef ref, ObjectCaller caller) {
        EvidenceItemEntity e = evidence.get(ref.id(), caller);
        ShareableObjectEntity rec = access.record(ObjectTypes.EVIDENCE_ITEM + ":" + e.getId()).orElseThrow();
        Built b = new Built();
        b.type = "evidence";
        b.objectRef = ObjectTypes.EVIDENCE_ITEM + ":" + e.getId();
        b.title = e.getTitle();
        b.tlp = Tlp.fromWire(rec.getTlp());
        b.authors.add(author(rec.getOwner(), "author"));
        b.chain.add(chainEntry(identity.instanceId(), handle(rec.getOwner()), b.objectRef, null, "origin",
                e.getCreatedAt()));
        Map<String, Object> view = json.convertValue(evidence.view(e, caller), MAP);
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("format", "foresignal-evidence-bundle");
        bundle.put("version", "1.0.0");
        bundle.put("tlp", b.tlp.wireValue());
        bundle.put("evidenceItem", view);
        List<Map<String, Object>> attachments = new ArrayList<>();
        for (String id : e.getAttachmentIds()) {
            Optional<AttachmentEntity> a = attachmentRows.findById(id);
            if (a.isEmpty() || access.role(caller, ObjectTypes.ATTACHMENT + ":" + id) == null) {
                continue;
            }
            b.tlp = Tlp.stricter(b.tlp, access.tlpOf(ObjectTypes.ATTACHMENT + ":" + id));
            byte[] bytes = blobs.read(a.get().getStoragePath());
            b.contents.put(ExchangeVocabulary.ATTACHMENTS + a.get().getSha256() + "."
                    + extension(a.get().getContentType()), bytes);
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("sha256", a.get().getSha256());
            f.put("attachmentId", id);
            f.put("filename", a.get().getFilename());
            f.put("contentType", a.get().getContentType());
            f.put("size", a.get().getSize());
            attachments.add(f);
        }
        bundle.put("attachments", attachments);
        bundle.put("envelopes", List.of());
        content(b, bundle, Map.of());
        return b;
    }

    private Built chronicle(ObjectRef ref, JsonNode extract, ObjectCaller caller) {
        ChronicleEntity c = chronicles.require(ref.id(), caller, AccessAction.VIEW);
        Built b = new Built();
        b.type = "chronicle-extract";
        b.objectRef = ChronicleService.ref(c.getId());
        b.title = c.getDisplayId() + " " + c.getName();
        b.tlp = access.tlpOf(b.objectRef);
        ShareableObjectEntity rec = access.record(b.objectRef).orElseThrow();
        b.authors.add(author(rec.getOwner(), "author"));
        b.chain.add(chainEntry(identity.instanceId(), handle(rec.getOwner()), b.objectRef, null, "origin",
                clock.instant()));
        Instant from = null;
        Instant to = null;
        List<String> layers = List.of();
        if (extract != null && !extract.isNull()) {
            CollabInputs.object(extract, "ChronicleExtract", Set.of("from", "to", "layers"));
            from = CollabInputs.instant(extract, "from");
            to = CollabInputs.instant(extract, "to");
            layers = CollabInputs.strings(extract, "layers", 100);
            if (from != null && to != null && !from.isBefore(to)) {
                throw ValidationException.forField("extract.to", "must be after from");
            }
        }
        Path file = files.temp(".json");
        b.temps.add(file);
        ChronicleExporter.Extract written;
        try {
            written = chronicleExporter.writeExtract(c, caller, layers, from, to, this::federates,
                    b.tlp.wireValue(), file);
            if (Files.size(file) > maxBytes()) {
                throw tooLarge("The chronicle extract is " + Files.size(file) + " bytes; the limit is " + maxBytes()
                        + " bytes (exchange.maxPackageMb). Narrow the window or the layers.");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (!written.excludedSources().isEmpty()) {
            Map<String, Object> caveat = new LinkedHashMap<>();
            caveat.put("code", "EXCLUDED_SOURCES");
            caveat.put("text", written.excludedSources().size() + " sources may not leave this instance (private,"
                    + " LOCAL_ONLY or not redistributable) and are left out of the extract.");
            caveat.put("data", Map.of("sources", written.excludedSources()));
            b.reductions.add(caveat);
        }
        b.contents.put(ExchangeVocabulary.EXTRACT, file);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", c.getId());
        summary.put("ref", b.objectRef);
        summary.put("displayId", c.getDisplayId());
        summary.put("name", c.getName());
        summary.put("status", c.getStatus());
        Map<String, Object> window = new LinkedHashMap<>();
        window.put("from", from == null ? null : from.toString());
        window.put("to", to == null ? null : to.toString());
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("format", "foresignal-chronicle-extract");
        bundle.put("version", "1.0.0");
        bundle.put("tlp", b.tlp.wireValue());
        bundle.put("chronicle", summary);
        bundle.put("window", window);
        bundle.put("layers", layers);
        bundle.put("rows", written.rows());
        bundle.put("includedSources", written.includedSources());
        bundle.put("excludedSources", written.excludedSources());
        bundle.put("envelopes", List.of());
        content(b, bundle, Map.of());
        return b;
    }

    /** A received package forwarded: its content as received, the original authors plus the forwarder. */
    private Built forward(ObjectRef ref, ObjectCaller caller) {
        ReceivedPackageEntity rp = null;
        for (ReceivedPackageEntity candidate : received.findByObjectRefOrderByReceivedAtDescIdDesc(ref.format())) {
            if (ExchangeInbox.visible(candidate, caller)) {
                rp = candidate;
                break;
            }
        }
        if (rp == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_OBJECT, "No received object '" + ref + "' exists, or you"
                    + " may not see it.");
        }
        Built b = new Built();
        b.type = rp.getType();
        b.objectRef = String.valueOf(rp.getManifest().get("objectRef"));
        b.title = rp.getTitle();
        b.tlp = Tlp.fromWire(rp.getTlp());
        for (Map<String, Object> a : rp.getAuthors()) {
            b.authors.add(new LinkedHashMap<>(a));
        }
        b.authors.add(author(caller.principal(), "forwarder"));
        for (Map<String, Object> entry : rp.getProvenanceChain()) {
            if (!identity.instanceId().equals(entry.get("instanceId"))) {
                b.chain.add(new LinkedHashMap<>(entry));
            }
            if (entry.get("instanceId") != null && ExchangeInbox.transportHop(entry, b.objectRef)) {
                b.loopInstances.add(String.valueOf(entry.get("instanceId")));
            }
        }
        if (rp.getSender() != null && rp.getSender().get("instanceId") != null) {
            b.loopInstances.add(String.valueOf(rp.getSender().get("instanceId")));
        }
        b.loopInstances.remove(identity.instanceId());
        b.chain.add(chainEntry(identity.instanceId(), handle(caller.principal()), b.objectRef, null, "forwarder",
                clock.instant()));
        PackageCodec.Contents original = PackageCodec.read(files.read(rp.getFilePath()), Long.MAX_VALUE);
        b.contents.putAll(original.contents());
        return b;
    }

    /** Adds the bundle and its renditions to the package content. */
    private void content(Built b, Map<String, Object> bundle, Map<String, ReportRenderer.Image> images) {
        b.contents.put(ExchangeVocabulary.BUNDLE, json.writeValueAsBytes(bundle));
        ExchangeRenditions.Rendered r = renditions.render(b.type, bundle, b.tlp.wireValue(), images, null);
        b.contents.put(ExchangeVocabulary.RENDITION_MD, r.markdown().getBytes(StandardCharsets.UTF_8));
        b.contents.put(ExchangeVocabulary.RENDITION_HTML, r.html().getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------------- egress

    /** True when a source's records may leave on the federation path. */
    boolean federates(String sourceId) {
        return egress.decide(EgressPath.FEDERATION, sourceId, null, null, EgressPolicy.EgressContext.federation(false))
                .allowed();
    }

    /**
     * Removes the envelopes of a bundle that may not leave (private, or with a source the federation path refuses) and
     * records them as counts per type and reason.
     *
     * @return the references left out
     */
    Set<String> reduce(Map<String, Object> bundle, List<Map<String, Object>> reductions) {
        Set<String> excluded = new LinkedHashSet<>();
        Map<String, Map<String, Integer>> counts = new TreeMap<>();
        List<Object> kept = new ArrayList<>();
        for (Object o : ExchangeRenditions.list(bundle.get("envelopes"))) {
            Map<String, Object> env = ExchangeRenditions.map(o);
            String reason = null;
            if (Boolean.TRUE.equals(env.get("private"))) {
                reason = EgressPolicy.REASON_PRIVATE;
            } else if (env.get("provenance") instanceof Map<?, ?> prov && prov.get("sourceIds") instanceof List<?> ids) {
                for (Object id : ids) {
                    EgressPolicy.Decision d = egress.decide(EgressPath.FEDERATION, String.valueOf(id), null, null,
                            EgressPolicy.EgressContext.federation(false));
                    if (!d.allowed()) {
                        reason = d.reason();
                        break;
                    }
                }
            }
            if (reason == null) {
                kept.add(o);
            } else {
                excluded.add(String.valueOf(env.get("ref")));
                counts.computeIfAbsent(reason, k -> new TreeMap<>()).merge(String.valueOf(env.get("type")), 1,
                        Integer::sum);
            }
        }
        bundle.put("envelopes", kept);
        if (bundle.get("snapshots") instanceof Map<?, ?> snaps) {
            Map<String, Object> filtered = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : snaps.entrySet()) {
                if (!excluded.contains(String.valueOf(e.getKey()))) {
                    filtered.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
            bundle.put("snapshots", filtered);
        }
        for (Map.Entry<String, Map<String, Integer>> e : counts.entrySet()) {
            int total = e.getValue().values().stream().mapToInt(Integer::intValue).sum();
            Map<String, Object> caveat = new LinkedHashMap<>();
            caveat.put("code", "EXCLUDED_" + e.getKey().toUpperCase(Locale.ROOT).replace('-', '_'));
            caveat.put("text", total + " cited objects may not leave this instance (" + e.getKey() + ") and are"
                    + " reduced to counts by type.");
            caveat.put("data", Map.of("reason", e.getKey(), "countsByType", e.getValue()));
            reductions.add(caveat);
        }
        return excluded;
    }

    /** Drops excluded references from a report bundle's frozen blocks. */
    static void filterReport(Map<String, Object> bundle, Set<String> excluded) {
        if (excluded.isEmpty()) {
            return;
        }
        Map<String, Object> report = new LinkedHashMap<>(ExchangeRenditions.map(bundle.get("report")));
        List<Object> blocks = new ArrayList<>();
        for (Object o : ExchangeRenditions.list(report.get("blocks"))) {
            Map<String, Object> b = new LinkedHashMap<>(ExchangeRenditions.map(o));
            for (String key : List.of("items", "citations")) {
                if (b.get(key) instanceof List<?> list) {
                    List<Object> kept = new ArrayList<>();
                    for (Object item : list) {
                        String ref = item instanceof Map<?, ?> m ? String.valueOf(m.get("ref")) : String.valueOf(item);
                        if (!excluded.contains(ref)) {
                            kept.add(item);
                        }
                    }
                    b.put(key, kept);
                }
            }
            blocks.add(b);
        }
        report.put("blocks", blocks);
        bundle.put("report", report);
    }

    // ------------------------------------------------------------------------- delivery

    private void deliver(ExchangePackageEntity p, ExchangeTlp.Recipients recipients) {
        List<PeerEntity> targets = new ArrayList<>();
        List<Map<String, Object>> deliveries = new ArrayList<>();
        if (recipients.everyone()) {
            for (PeerEntity peer : peers.findAllByOrderByIdAsc()) {
                if (peer.isEnabled() && peer.getExchangeKeyCiphertext() != null) {
                    targets.add(peer);
                }
            }
        } else {
            for (String instance : recipients.byInstance().keySet()) {
                Optional<PeerEntity> peer = peerFor(instance);
                if (peer.isPresent()) {
                    targets.add(peer.get());
                } else {
                    deliveries.add(delivery(instance, null, "failed", null, "no enabled peer is registered for this"
                            + " instance; download the package and hand it over"));
                }
            }
        }
        p.setStatus(ExchangeVocabulary.DELIVERING);
        packages.save(p);
        byte[] zip = files.read(p.getFilePath());
        for (PeerEntity peer : targets) {
            String instance = peer.getRemoteInstanceId() != null ? peer.getRemoteInstanceId() : peer.getId();
            String key = peerService.exchangeKey(peer);
            if (key == null) {
                deliveries.add(delivery(instance, peer.getId(), "failed", null, "the peer has no exchange key (set"
                        + " exchangeKey on the peer)"));
                continue;
            }
            try {
                PeerExchangeClient.Response response = client.post(peer.getBaseUrl(), "/v1/exchange/inbox",
                        ExchangeVocabulary.MEDIA_TYPE, zip, Map.of("Authorization", "Bearer " + key,
                                "Accept", "application/json"));
                if (response.ok()) {
                    deliveries.add(delivery(instance, peer.getId(), "delivered", clock.instant(), null));
                } else {
                    deliveries.add(delivery(instance, peer.getId(), "failed", null, "the peer answered "
                            + response.status() + ": " + snippet(response.body())));
                }
            } catch (PeerExchangeClient.DeliveryException e) {
                deliveries.add(delivery(instance, peer.getId(), "failed", null, e.getMessage()));
            }
        }
        p.setDeliveries(deliveries);
        boolean allDelivered = !deliveries.isEmpty() && deliveries.stream()
                .allMatch(d -> "delivered".equals(d.get("status")));
        p.setStatus(allDelivered ? ExchangeVocabulary.DELIVERED : ExchangeVocabulary.FAILED);
        if (!allDelivered) {
            log.info("Package {} not delivered to every recipient: {}", p.getId(), deliveries);
        }
        packages.save(p);
    }

    private static Map<String, Object> delivery(String instance, String peerId, String status, Instant at,
            String error) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("instanceId", instance);
        d.put("peerId", peerId);
        d.put("status", status);
        d.put("at", at == null ? null : at.toString());
        d.put("error", error);
        return d;
    }

    private static String snippet(byte[] body) {
        if (body == null) {
            return "";
        }
        String s = new String(body, StandardCharsets.UTF_8).replaceAll("\\s+", " ").strip();
        return s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }

    /**
     * Records a receipt from a peer ({@code receiveExchangeReceipt}): a delivery to that peer's instance becomes
     * {@code acknowledged} (or {@code delivered}).
     *
     * @param body the {@code ExchangeReceipt}
     * @param peerId the peer whose key authenticated the call
     * @throws NotFoundException {@code UNKNOWN_PACKAGE} when no delivery of that package went to that peer
     */
    public void receipt(JsonNode body, String peerId) {
        CollabInputs.object(body, "ExchangeReceipt", Set.of("packageId", "type", "receiverInstanceId",
                "principalHandle", "at"));
        String packageId = CollabInputs.required(body, "packageId", 26, 26);
        String type = CollabInputs.required(body, "type", 1, 16);
        if (!List.of("delivered", "acknowledged").contains(type)) {
            throw ValidationException.forField("type", "must be delivered or acknowledged");
        }
        String receiver = CollabInputs.required(body, "receiverInstanceId", 1, 128);
        PeerEntity peer = peerId == null ? null : peers.findById(peerId).orElse(null);
        if (peer == null) {
            throw unknown(packageId);
        }
        if (peer.getRemoteInstanceId() != null && !peer.getRemoteInstanceId().equals(receiver)) {
            throw ValidationException.forField("receiverInstanceId", "is not the instance of the calling peer");
        }
        ExchangePackageEntity p = packages.findById(packageId).orElseThrow(() -> unknown(packageId));
        List<Map<String, Object>> deliveries = new ArrayList<>();
        boolean matched = false;
        for (Map<String, Object> d : p.getDeliveries()) {
            Map<String, Object> copy = new LinkedHashMap<>(d);
            if (peerId.equals(d.get("peerId")) || receiver.equals(d.get("instanceId"))) {
                matched = true;
                copy.put("status", "acknowledged".equals(type) ? "acknowledged" : "delivered");
                Instant at = CollabInputs.instant(body, "at");
                copy.put("at", (at == null ? clock.instant() : at).toString());
                copy.put("error", null);
                if (body.hasNonNull("principalHandle")) {
                    copy.put("acknowledgedBy", body.get("principalHandle").asString());
                }
            }
            deliveries.add(copy);
        }
        if (!matched) {
            throw unknown(packageId);
        }
        p.setDeliveries(deliveries);
        packages.save(p);
    }

    // ------------------------------------------------------------------------- reads

    /**
     * A package built here, for its creator or an admin.
     *
     * @throws NotFoundException {@code UNKNOWN_PACKAGE} otherwise
     */
    public ExchangePackageEntity get(String id, ObjectCaller caller) {
        ExchangePackageEntity p = id == null ? null : packages.findById(id).orElse(null);
        if (p == null || !(caller.admin() || p.getCreatedBy().equals(caller.principal()))) {
            throw unknown(id);
        }
        return p;
    }

    /**
     * The ZIP of a package ({@code downloadExchangePackage}).
     *
     * @throws ConflictException {@code 409 PACKAGE_NOT_READY} while building or after a failed build
     */
    public byte[] download(String id, ObjectCaller caller) {
        ExchangePackageEntity p = get(id, caller);
        if (p.getFilePath() == null) {
            throw new ConflictException(ErrorCodes.PACKAGE_NOT_READY, "Package " + id + " is " + p.getStatus() + ".");
        }
        return files.read(p.getFilePath());
    }

    /** Packages built here, newest first ({@code listSentPackages}): the caller's own, or all for an admin. */
    public Map<String, Object> sent(ObjectCaller caller, String status, String cursor, int limit) {
        if (status != null && !ExchangeVocabulary.STATUSES.contains(status)) {
            throw ValidationException.forField("status", "must be one of " + ExchangeVocabulary.STATUSES);
        }
        List<ExchangePackageEntity> all = caller.admin() ? packages.findAllByOrderByCreatedAtDescIdDesc()
                : packages.findByCreatedByOrderByCreatedAtDescIdDesc(String.valueOf(caller.principal()));
        List<ExchangePackageEntity> visible = all.stream().filter(p -> status == null || status.equals(p.getStatus()))
                .toList();
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ExchangePackageEntity p : visible.subList(Math.min(offset, visible.size()),
                Math.min(offset + limit, visible.size()))) {
            items.add(view(p));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < visible.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** The {@code ExchangePackage} view. */
    public Map<String, Object> view(ExchangePackageEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("manifest", p.getManifest());
        out.put("status", p.getStatus());
        out.put("sizeBytes", p.getSizeBytes());
        out.put("deliveries", p.getDeliveries());
        out.put("reductions", p.getReductions());
        Map<String, Object> by = views.summary(p.getCreatedBy());
        out.put("createdBy", by == null ? Map.of("id", p.getCreatedBy()) : by);
        out.put("createdAt", p.getCreatedAt().toString());
        out.put("downloadHref", p.getFilePath() == null ? null : "/v1/exchange/packages/" + p.getId() + "/download");
        out.put("error", p.getError());
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private Map<String, Object> author(String principalId, String role) {
        PrincipalEntity p = principalId == null ? null : principals.findById(principalId).orElse(null);
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("handle", p == null ? String.valueOf(principalId) : p.getHandle());
        a.put("displayName", p == null ? String.valueOf(principalId) : p.getDisplayName());
        a.put("instanceId", identity.instanceId());
        a.put("role", role);
        return a;
    }

    private String handle(String principalId) {
        return principalId == null ? "unknown" : principals.findById(principalId).map(PrincipalEntity::getHandle)
                .orElse(principalId);
    }

    private ProvenanceChains provenance;

    /** Installs the provenance chains of prompt 134 (reports carry the chains of what they cite). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setProvenanceChains(ProvenanceChains provenance) {
        this.provenance = provenance;
    }

    /**
     * The chain of a report package (prompt 134): the chains of the reports, received material and evidence items it
     * cites, origin first, then the report itself (with its manifest), so a report that comes back through other
     * analysts is recognized as circular.
     */
    List<Map<String, Object>> reportChain(String reportRef, String ownerHandle, String manifestId, Instant at) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (provenance != null) {
            try {
                for (Map<String, Object> e : provenance.of(reportRef)) {
                    if (!reportRef.equals(e.get("productRef"))) {
                        out.add(e);
                    }
                }
            } catch (RuntimeException e) {
                out.clear();
            }
        }
        out.add(chainEntry(identity.instanceId(), ownerHandle, reportRef, manifestId, out.isEmpty() ? "origin"
                : "author", at));
        return out;
    }

    /** One {@code ProvenanceChainEntry}. */
    static Map<String, Object> chainEntry(String instanceId, String principal, String productRef, String manifestId,
            String role, Instant at) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("instanceId", instanceId);
        e.put("principal", principal);
        e.put("productRef", productRef);
        e.put("manifestId", manifestId);
        e.put("role", role);
        e.put("at", at == null ? null : at.toString());
        return e;
    }

    private Map<String, Object> copy(Map<String, Object> value) {
        return json.convertValue(value, MAP);
    }

    /** The file extension of a stored attachment's media type. */
    static String extension(String contentType) {
        if (contentType == null) {
            return "bin";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "application/pdf" -> "pdf";
            case "text/plain" -> "txt";
            case "text/csv" -> "csv";
            case "application/json" -> "json";
            case "application/geo+json" -> "geojson";
            case "application/vnd.google-earth.kml+xml" -> "kml";
            case "text/markdown" -> "md";
            default -> "bin";
        };
    }
}
