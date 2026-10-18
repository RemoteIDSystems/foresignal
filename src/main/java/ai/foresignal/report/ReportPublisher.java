package ai.foresignal.report;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.AttachmentEntity;
import ai.foresignal.collab.AttachmentRepository;
import ai.foresignal.collab.AttachmentService;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.collab.PersonalNotifier;
import ai.foresignal.collab.PrincipalViews;
import ai.foresignal.collab.ShareService;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.raster.render.MapRenderer;
import ai.foresignal.raster.render.StaticMapService;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Publishes and retracts report revisions and serves their renditions (prompt 132, RPT-1).
 *
 * <p>{@link #publish} freezes the draft as the publisher sees it: every cited reference is resolved and pinned
 * ({@code @seq} for records), envelopes and snapshots are stored, map blocks are rendered with the 055 static map
 * service and stored as attachments, the {@code sources} block is regenerated from the cited sources' 109
 * attributions and licenses, the TLP label is raised to the strictest input, a 116 manifest records the inputs, and
 * the Markdown, HTML and JSON bundle renditions are written once. A revision never changes afterwards; retraction only
 * marks it withdrawn and notifies everyone it was sent to ({@code report.retracted}).
 */
@Service
public class ReportPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReportPublisher.class);

    /** The manifest component of report revisions. */
    public static final String COMPONENT = "report";

    /** The component version. */
    public static final String VERSION = "1.0.0";

    /** Map image width in pixels. */
    static final int MAP_WIDTH = 800;

    /** Map image height in pixels. */
    static final int MAP_HEIGHT = 600;

    private final ReportService reports;
    private final ReportRepository reportRows;
    private final ReportRevisionRepository revisions;
    private final ReportSendRepository sends;
    private final ReportSnapshots snapshots;
    private final ReportRenderer renderer;
    private final AccessResolver access;
    private final ShareService shares;
    private final AttachmentService attachments;
    private final AttachmentRepository attachmentRows;
    private final AttributionService attributions;
    private final PersonalNotifier notifier;
    private final PrincipalViews views;
    private final PrincipalRepository principals;
    private final ObjectProvider<StaticMapService> maps;
    private final ObjectProvider<ManifestService> manifests;
    private final JsonMapper json;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the publisher. */
    public ReportPublisher(ReportService reports, ReportRepository reportRows, ReportRevisionRepository revisions,
            ReportSendRepository sends, ReportSnapshots snapshots, ReportRenderer renderer, AccessResolver access,
            ShareService shares, AttachmentService attachments, AttachmentRepository attachmentRows,
            AttributionService attributions,
            PersonalNotifier notifier, PrincipalViews views, PrincipalRepository principals,
            ObjectProvider<StaticMapService> maps, ObjectProvider<ManifestService> manifests, JsonMapper json,
            Clock clock, PlatformTransactionManager transactions) {
        this.reports = reports;
        this.reportRows = reportRows;
        this.revisions = revisions;
        this.sends = sends;
        this.snapshots = snapshots;
        this.renderer = renderer;
        this.access = access;
        this.shares = shares;
        this.attachments = attachments;
        this.attachmentRows = attachmentRows;
        this.attributions = attributions;
        this.notifier = notifier;
        this.views = views;
        this.principals = principals;
        this.maps = maps;
        this.manifests = manifests;
        this.json = json;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    // ------------------------------------------------------------------------- publish

    /**
     * Publishes the draft as a new immutable revision (editors).
     *
     * @param id the report
     * @param caller the publisher
     * @return the new revision
     * @throws ConflictException {@code 409 REPORT_VERSION_CONFLICT} when the report is not a draft
     */
    public ReportRevisionEntity publish(String id, ObjectCaller caller) {
        ReportEntity r = reports.require(id, caller, AccessAction.EDIT);
        if (!ReportVocabulary.editable(r.getStatus())) {
            throw ReportService.notDraft(r);
        }
        Instant now = clock.instant();
        ShareableObjectEntity rec = reports.record(r);
        ReportSnapshots.Collected collected = new ReportSnapshots.Collected();
        Map<String, ReportRenderer.Image> images = new LinkedHashMap<>();
        List<Map<String, Object>> frozen = new ArrayList<>();
        Tlp label = Tlp.fromWire(rec.getTlp());
        for (Map<String, Object> b : r.getSections()) {
            Map<String, Object> f = snapshots.fill(b, caller, true, collected);
            if ("map".equals(f.get("type"))) {
                f = renderMap(f, r, label, caller, images);
            }
            if (!"sources".equals(f.get("type")) || !ReportVocabulary.DETERMINISTIC.equals(f.get("textOrigin"))) {
                frozen.add(f);
            }
        }
        frozen.add(sourcesBlock(collected, now));
        Tlp strictest = Tlp.strictest(collected.tlps, null);
        if (strictest != null && strictest.stricterThan(label)) {
            shares.checkTlp(strictest, rec.getOwner(), reports.grantees(rec), rec.getVisibility());
            label = strictest;
        }
        for (Map.Entry<String, AttachmentEntity> a : collected.attachments.entrySet()) {
            if (a.getValue().isImage()) {
                images.put(a.getKey(), new ReportRenderer.Image(a.getValue().getSha256(), a.getValue().getContentType(),
                        snapshots.attachmentContent(a.getValue())));
            }
        }
        int revision = revisions.findFirstByReportIdOrderByRevisionDesc(r.getId()).map(x -> x.getRevision() + 1)
                .orElse(1);
        String manifestId = manifest(r, revision, collected);
        PrincipalEntity publisher = principals.findById(caller.principal()).orElse(null);
        ReportRenderer.Document doc = document(r, frozen, label.wireValue(), revision, now,
                publisher == null ? null : publisher.getHandle(), images, null);
        String markdown = renderer.markdown(doc);
        String html = renderer.html(doc);
        Map<String, Object> view = reports.view(r, caller);
        view.put("blocks", frozen);
        view.put("tlp", label.wireValue());
        view.put("status", ReportVocabulary.PUBLISHED);
        view.put("publishedRevision", revision);
        view.put("publishedAt", now.toString());
        view.put("manifestId", manifestId);
        view.put("machineGenerated", ReportBlocks.machineGenerated(frozen));
        Map<String, Object> bundle = bundle(view, revision, label.wireValue(), collected, manifestId);
        String sha = sha256(json.writeValueAsBytes(bundle));
        final Tlp finalLabel = label;
        ReportRevisionEntity saved = tx.execute(s -> {
            ReportRevisionEntity rev = new ReportRevisionEntity(Ulids.next(now), r.getId(), revision, r.getTitle(),
                    finalLabel.wireValue(), caller.principal(), now);
            rev.setManifestId(manifestId);
            rev.setPinnedRefs(new ArrayList<>(collected.pinnedRefs));
            rev.setBundle(bundle, sha);
            rev.setRenditions(markdown, html);
            rev.setMachineGenerated(ReportBlocks.machineGenerated(frozen));
            revisions.save(rev);
            ReportEntity fresh = reportRows.findById(r.getId()).orElseThrow(() -> ReportService.unknown(id));
            fresh.setStatus(ReportVocabulary.PUBLISHED);
            fresh.setPublishedRevision(revision);
            fresh.setPublishedAt(now);
            fresh.setManifestId(manifestId);
            fresh.touch(now);
            reportRows.save(fresh);
            ShareableObjectEntity current = access.record(ReportService.ref(r.getId())).orElseThrow();
            if (finalLabel.stricterThan(Tlp.fromWire(current.getTlp()))) {
                current.setTlp(finalLabel.wireValue());
                access.save(current);
            }
            return rev;
        });
        return saved;
    }

    /** Renders a map block with the 055 static map service and stores it as an attachment; failures become a caveat. */
    private Map<String, Object> renderMap(Map<String, Object> block, ReportEntity r, Tlp label, ObjectCaller caller,
            Map<String, ReportRenderer.Image> images) {
        Map<String, Object> b = new LinkedHashMap<>(block);
        StaticMapService service = maps.getIfAvailable();
        double[] bbox = bbox(b, r);
        if (service == null || bbox == null) {
            b.put("caveat", service == null ? "The static map service is not available on this instance."
                    : "The map has no area or bounding box, so it was not rendered.");
            return b;
        }
        try {
            List<String> layers = ReportBlocks.asList(b.get("layers")).stream().map(String::valueOf).toList();
            String time = null;
            if (b.get("timeRange") instanceof Map<?, ?> range && range.get("end") instanceof String end) {
                time = end;
            }
            StaticMapService.Prepared prepared = service.prepare(new StaticMapService.Request(layers, List.of(), bbox,
                    null, null, time, null, MAP_WIDTH, MAP_HEIGHT, null, null, false, false),
                    caller.excludedSources(), clock.instant());
            MapRenderer.Rendered png = service.draw(prepared.spec());
            AttachmentEntity a = attachments.upload(png.png(), "report-" + r.getDisplayId() + "-map.png", "image/png",
                    label.wireValue(), "private", caller);
            b.put("attachmentId", a.getId());
            b.put("mediaId", a.getMediaId());
            b.put("bbox", List.of(bbox[0], bbox[1], bbox[2], bbox[3]));
            images.put(a.getId(), new ReportRenderer.Image(a.getSha256(), a.getContentType(), png.png()));
        } catch (RuntimeException e) {
            log.info("Report {} map not rendered: {}", r.getDisplayId(), e.toString());
            b.put("caveat", "The map could not be rendered: " + e.getMessage());
        }
        return b;
    }

    /** The bounding box of a map block: its {@code bbox}, else its area's, else the report's area's. */
    @SuppressWarnings("unchecked")
    static double[] bbox(Map<String, Object> b, ReportEntity r) {
        List<Object> box = ReportBlocks.asList(b.get("bbox"));
        if (box.size() == 4) {
            return new double[] {num(box.get(0)), num(box.get(1)), num(box.get(2)), num(box.get(3))};
        }
        Object area = b.get("area") instanceof Map<?, ?> m ? m : r.getArea();
        if (area instanceof Map<?, ?> geometry) {
            return envelope((Map<String, Object>) geometry);
        }
        return null;
    }

    /** The bounding box of a GeoJSON geometry's coordinates, or null. */
    static double[] envelope(Map<String, Object> geometry) {
        double[] box = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        walk(geometry.get("coordinates"), box);
        for (Object g : ReportBlocks.asList(geometry.get("geometries"))) {
            if (g instanceof Map<?, ?> m) {
                walk(m.get("coordinates"), box);
            }
        }
        if (box[0] > box[2] || box[1] > box[3]) {
            return null;
        }
        if (box[0] == box[2]) {
            box[0] -= 0.25;
            box[2] += 0.25;
        }
        if (box[1] == box[3]) {
            box[1] -= 0.25;
            box[3] += 0.25;
        }
        return new double[] {Math.max(-180, box[0]), Math.max(-90, box[1]), Math.min(180, box[2]),
            Math.min(90, box[3])};
    }

    private static void walk(Object coords, double[] box) {
        if (coords instanceof List<?> list && !list.isEmpty()) {
            if (list.get(0) instanceof Number && list.size() >= 2 && list.get(1) instanceof Number) {
                double lon = ((Number) list.get(0)).doubleValue();
                double lat = ((Number) list.get(1)).doubleValue();
                box[0] = Math.min(box[0], lon);
                box[1] = Math.min(box[1], lat);
                box[2] = Math.max(box[2], lon);
                box[3] = Math.max(box[3], lat);
                return;
            }
            for (Object o : list) {
                walk(o, box);
            }
        }
    }

    private static double num(Object o) {
        return o instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(o));
    }

    /** The {@code sources} block: attribution and license of every source behind the cited envelopes (109). */
    Map<String, Object> sourcesBlock(ReportSnapshots.Collected collected, Instant now) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String sourceId : collected.sourceIds) {
            for (AttributionService.Attribution a : attributions.scope(sourceId, null)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("sourceId", a.sourceId());
                m.put("licence", a.licence());
                m.put("attribution", a.text());
                m.put("url", a.termsUrl());
                list.add(m);
            }
            if (attributions.scope(sourceId, null).isEmpty()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("sourceId", sourceId);
                m.put("licence", null);
                m.put("attribution", sourceId);
                m.put("url", null);
                list.add(m);
            }
        }
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("id", ReportBlocks.newId(now));
        b.put("type", "sources");
        b.put("title", null);
        b.put("textOrigin", ReportVocabulary.DETERMINISTIC);
        b.put("attributions", list);
        b.put("updatedAt", now.toString());
        b.put("updatedBy", null);
        return b;
    }

    private String manifest(ReportEntity r, int revision, ReportSnapshots.Collected collected) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("reportId", r.getId());
            config.put("revision", revision);
            config.put("template", r.getTemplate());
            config.put("source", r.getSource());
            return service.record(ManifestRequest.of(COMPONENT, VERSION).config(config)
                    .inputs(new ArrayList<>(collected.pinnedRefs)));
        } catch (RuntimeException e) {
            log.warn("Report {} revision {} manifest not recorded: {}", r.getDisplayId(), revision, e.toString());
            return null;
        }
    }

    /** The {@code ReportBundle}: the frozen report, every cited envelope and snapshot, attachments by hash. */
    Map<String, Object> bundle(Map<String, Object> view, int revision, String tlp, ReportSnapshots.Collected collected,
            String manifestId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("report", view);
        out.put("revision", revision);
        out.put("tlp", tlp);
        out.put("envelopes", new ArrayList<>(collected.envelopes.values()));
        out.put("snapshots", new LinkedHashMap<>(collected.snapshots));
        List<Map<String, Object>> files = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Map<String, Object> b : ReportBlocks.asList(view.get("blocks")).stream()
                .filter(Map.class::isInstance).map(o -> asMap(o)).toList()) {
            if ("map".equals(b.get("type")) && b.get("attachmentId") instanceof String id) {
                attachmentFile(id, files, seen);
            }
        }
        for (AttachmentEntity a : collected.attachments.values()) {
            if (seen.add(a.getSha256())) {
                files.add(file(a));
            }
        }
        out.put("attachments", files);
        out.put("missing", new ArrayList<>(collected.missing));
        out.put("manifestId", manifestId);
        return out;
    }

    private void attachmentFile(String id, List<Map<String, Object>> files, Set<String> seen) {
        attachmentRows.findById(id).ifPresent(a -> {
            if (seen.add(a.getSha256())) {
                files.add(file(a));
            }
        });
    }

    private static Map<String, Object> file(AttachmentEntity a) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("sha256", a.getSha256());
        f.put("attachmentId", a.getId());
        f.put("filename", a.getFilename());
        f.put("contentType", a.getContentType());
        f.put("size", a.getSize());
        return f;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return (Map<String, Object>) o;
    }

    /** The document {@link ReportRenderer} renders. */
    ReportRenderer.Document document(ReportEntity r, List<Map<String, Object>> blocks, String tlp, Integer revision,
            Instant publishedAt, String publishedBy, Map<String, ReportRenderer.Image> images, String retracted) {
        List<String> authors = new ArrayList<>();
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        for (String a : r.getAuthors()) {
            Object s = reports.summary(a, cache);
            authors.add(s instanceof Map<?, ?> m && m.get("displayName") != null ? String.valueOf(m.get("displayName"))
                    : a);
        }
        return new ReportRenderer.Document(r.getTitle(), r.getSubtitle(), r.getDisplayId(), revision, tlp,
                publishedAt == null ? null : publishedAt.toString(), publishedBy, authors, blocks, images, null,
                retracted);
    }

    /** The SHA-256 of {@code bytes}, lower-case hex. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    // ------------------------------------------------------------------------- retract

    /**
     * Retracts a published revision (owner) and notifies everyone it was sent to ({@code report.retracted}).
     *
     * @param id the report
     * @param body the {@code ReportRetractRequest}
     * @param caller the owner
     * @return the retracted revision
     * @throws ConflictException {@code 409 REPORT_NOT_PUBLISHED}
     */
    public ReportRevisionEntity retract(String id, JsonNode body, ObjectCaller caller) {
        ReportEntity r = reports.require(id, caller, AccessAction.DELETE);
        CollabInputs.object(body, "ReportRetractRequest", Set.of("revision", "reason"));
        String reason = CollabInputs.required(body, "reason", 1, 2000);
        Integer number = body.hasNonNull("revision") ? body.get("revision").asInt() : null;
        ReportRevisionEntity rev = (number == null ? revisions.findFirstByReportIdOrderByRevisionDesc(r.getId())
                : revisions.findByReportIdAndRevision(r.getId(), number))
                .orElseThrow(() -> notPublished(r, "has no such published revision"));
        if (ReportVocabulary.RETRACTED.equals(rev.getStatus())) {
            throw notPublished(r, "revision " + rev.getRevision() + " is already retracted");
        }
        Instant now = clock.instant();
        Set<String> recipients = new LinkedHashSet<>();
        ReportRevisionEntity saved = tx.execute(s -> {
            rev.retract(reason, now);
            revisions.save(rev);
            ReportEntity fresh = reportRows.findById(r.getId()).orElseThrow(() -> ReportService.unknown(id));
            boolean latest = fresh.getPublishedRevision() != null && fresh.getPublishedRevision() == rev.getRevision();
            if (latest && ReportVocabulary.PUBLISHED.equals(fresh.getStatus())) {
                fresh.setStatus(ReportVocabulary.RETRACTED);
                reportRows.save(fresh);
            }
            for (ReportSendEntity send : sends.findByReportIdOrderBySentAtAscIdAsc(r.getId())) {
                if (send.getRevision() == null || send.getRevision() == rev.getRevision()) {
                    recipients.addAll(send.getPrincipalIds());
                }
            }
            recipients.remove(caller.principal());
            Map<String, Object> from = views.summary(caller.principal());
            for (String p : recipients) {
                notifier.send(new PersonalNotifier.Notice(p, MeEvents.REPORT_RETRACTED, r.getDisplayId() + " "
                        + r.getTitle() + " (revision " + rev.getRevision() + ") was retracted", reason,
                        ReportService.ref(r.getId()), caller.principal(), rev.getTlp(),
                        MeEvents.report(ReportService.ref(r.getId()), r.getTitle(), rev.getTlp(), rev.getRevision(),
                                from, null, reason, now)));
            }
            return rev;
        });
        return saved;
    }

    /** {@code 409 REPORT_NOT_PUBLISHED}. */
    static ConflictException notPublished(ReportEntity r, String why) {
        return new ConflictException(ErrorCodes.REPORT_NOT_PUBLISHED, "Report " + r.getDisplayId() + " " + why + ".");
    }

    // ------------------------------------------------------------------------- reads

    /** A page of a report's revisions, newest first ({@code listReportRevisions}). */
    public Map<String, Object> revisions(String id, String cursor, int limit, ObjectCaller caller) {
        ReportEntity r = reports.require(id, caller, AccessAction.VIEW);
        List<ReportRevisionEntity> all = revisions.findByReportIdOrderByRevisionDesc(r.getId());
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ReportRevisionEntity rev : all.subList(Math.min(offset, all.size()), Math.min(offset + limit,
                all.size()))) {
            items.add(revisionView(rev));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < all.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /**
     * The published revision of a report (the latest when {@code number} is null).
     *
     * @throws ConflictException {@code 409 REPORT_NOT_PUBLISHED} when there is none
     * @throws NotFoundException {@code UNKNOWN_REPORT} for an unknown revision number
     */
    public ReportRevisionEntity revision(ReportEntity r, Integer number) {
        if (number == null) {
            return revisions.findFirstByReportIdOrderByRevisionDesc(r.getId())
                    .orElseThrow(() -> notPublished(r, "is not published yet"));
        }
        return revisions.findByReportIdAndRevision(r.getId(), number).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_REPORT, "Report " + r.getDisplayId() + " has no revision " + number + "."));
    }

    /**
     * A rendition ({@code getReportRendition}).
     *
     * @param id the report
     * @param format {@code md}, {@code html} or {@code json}
     * @param number the revision, or null for the latest
     * @param draft true to render the current draft as a preview
     * @param caller the reader
     * @return the rendition: a string for Markdown and HTML, the bundle map for JSON
     */
    public Object rendition(String id, String format, Integer number, boolean draft, ObjectCaller caller) {
        if (!List.of("md", "html", "json").contains(format)) {
            throw ValidationException.forField("format", "must be md, html or json");
        }
        ReportEntity r = reports.require(id, caller, AccessAction.VIEW);
        if (draft) {
            ReportSnapshots.Collected collected = new ReportSnapshots.Collected();
            List<Map<String, Object>> blocks = new ArrayList<>();
            for (Map<String, Object> b : r.getSections()) {
                blocks.add(snapshots.fill(b, caller, false, collected));
            }
            String tlp = reports.tlp(r).wireValue();
            ReportRenderer.Document doc = document(r, blocks, tlp, null, null, null, Map.of(), null);
            return switch (format) {
                case "md" -> renderer.markdown(doc);
                case "html" -> renderer.html(doc);
                default -> {
                    Map<String, Object> view = reports.view(r, caller);
                    view.put("blocks", blocks);
                    yield bundle(view, 0, tlp, collected, null);
                }
            };
        }
        ReportRevisionEntity rev = revision(r, number);
        return switch (format) {
            case "md" -> rev.getMarkdown();
            case "html" -> rev.getHtml();
            default -> rev.getBundle();
        };
    }

    /** The {@code ReportRevision} view. */
    public Map<String, Object> revisionView(ReportRevisionEntity rev) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reportId", rev.getReportId());
        out.put("revision", rev.getRevision());
        out.put("title", rev.getTitle());
        out.put("tlp", rev.getTlp());
        out.put("status", rev.getStatus());
        out.put("publishedAt", rev.getPublishedAt().toString());
        out.put("publishedBy", reports.summary(rev.getPublishedBy(), PrincipalViews.cache()));
        out.put("retractedAt", rev.getRetractedAt() == null ? null : rev.getRetractedAt().toString());
        out.put("retractionReason", rev.getRetractionReason());
        out.put("manifestId", rev.getManifestId());
        out.put("pinnedRefs", rev.getPinnedRefs());
        out.put("bundleSha256", rev.getBundleSha256());
        String base = "/v1/reports/" + rev.getReportId() + "/rendition?revision=" + rev.getRevision() + "&format=";
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("markdown", base + "md");
        links.put("html", base + "html");
        links.put("json", base + "json");
        out.put("renditions", links);
        return out;
    }
}
