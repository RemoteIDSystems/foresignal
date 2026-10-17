package ai.foresignal.share;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.ratelimit.ClientRateLimiter;
import ai.foresignal.security.ratelimit.RateClass;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.ReadScopes;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.track.TrackQuery;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.view.SavedViewEntity;
import ai.foresignal.view.SavedViewService;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Path-bound share links (prompt 112): {@code fss_} keys that authorize only {@code GET /v1/k/{key}} and its
 * sub-paths, resolved server-side to one target.
 *
 * <p><strong>Egress.</strong> Every record leaves on {@link EgressPath#PUBLIC_SHARE} through the single
 * {@link EgressPolicy#decide} (109): a source or record whose decision is not allowed is removed; a target that
 * is itself such a record (or an export or briefing run built from such sources) is refused at creation with
 * {@code 409 SHARE_LICENCE_FORBIDDEN}. Attribution is always included.
 *
 * <p><strong>Private sources.</strong> A private target needs an admin token ({@code 403} otherwise), and the
 * privacy mask ({@link ShareMask}) is its default. {@code EgressPolicy} however never lets a private record leave
 * on a share-link path (invariant EGR-1), so an admin's private target is refused with {@code 403 RESTRICTED}
 * as well; the mask is applied to every rendered record whose source is private and therefore takes effect if
 * the policy is ever changed to allow it.
 *
 * <p><strong>Scope.</strong> A link renders within the creating token's read restriction (stored as a snapshot)
 * and never shows a private source.
 */
@Service
public class ShareLinkService {

    /** The key prefix. */
    public static final String KEY_PREFIX = "fss_";

    /** The target types. */
    public static final List<String> TARGET_TYPES = List.of("record", "alert", "view", "track", "briefing-run",
            "export");

    /** The default lifetime. */
    public static final Duration DEFAULT_TTL = Duration.ofDays(7);

    /** The longest lifetime. */
    public static final Duration MAX_TTL = Duration.ofDays(30);

    /** Uses per minute per link. */
    public static final int USES_PER_MINUTE = 60;

    /** At most one audited use per link in this interval (besides the first). */
    static final Duration USE_AUDIT_INTERVAL = Duration.ofHours(1);

    /** The most records a shared view renders. */
    public static final int VIEW_RECORD_LIMIT = 1_000;

    private static final int ENTROPY_BYTES = 32;
    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ShareLinkRepository links;
    private final TokenHasher hasher;
    private final EgressPolicy egress;
    private final SourceRegistry registry;
    private final RecordQueryService records;
    private final LicenceResolver licences;
    private final AttributionService attributions;
    private final AlertStore alerts;
    private final SavedViewService views;
    private final TrackService tracks;
    private final BriefingService briefings;
    private final ExportJobService exports;
    private final ObjectProvider<ReadScopes> scopes;
    private final ClientRateLimiter limiter;
    private final AuditService audit;
    private final JsonMapper json;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private volatile Map<String, ExtraShareTarget> extraTargets = Map.of();
    private final Map<String, Instant> lastAuditedUse = new ConcurrentHashMap<>();

    /** Creates the service. */
    public ShareLinkService(ShareLinkRepository links, TokenHasher hasher, EgressPolicy egress, SourceRegistry registry,
            RecordQueryService records, LicenceResolver licences, AttributionService attributions, AlertStore alerts,
            SavedViewService views, TrackService tracks, BriefingService briefings, ExportJobService exports,
            ObjectProvider<ReadScopes> scopes, ClientRateLimiter limiter, AuditService audit, JsonMapper json,
            Clock clock) {
        this.links = links;
        this.hasher = hasher;
        this.egress = egress;
        this.registry = registry;
        this.records = records;
        this.licences = licences;
        this.attributions = attributions;
        this.alerts = alerts;
        this.views = views;
        this.tracks = tracks;
        this.briefings = briefings;
        this.exports = exports;
        this.scopes = scopes;
        this.limiter = limiter;
        this.audit = audit;
        this.json = json;
        this.clock = clock;
    }

    /**
     * The target types other packages contribute (prompt 129: {@code workspace}).
     *
     * @param targets the contributed targets
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setExtraTargets(List<ExtraShareTarget> targets) {
        Map<String, ExtraShareTarget> byType = new java.util.LinkedHashMap<>();
        for (ExtraShareTarget t : targets) {
            byType.put(t.type(), t);
        }
        this.extraTargets = Map.copyOf(byType);
    }

    /** Every accepted target type: the built-in ones and the contributed ones. */
    public List<String> targetTypes() {
        List<String> out = new ArrayList<>(TARGET_TYPES);
        out.addAll(extraTargets.keySet());
        return out;
    }

    /**
     * The creating or listing caller.
     *
     * @param tokenId the token id
     * @param tokenName the token name, for the audit log
     * @param admin true for an admin token
     * @param viewer the private-gate viewer
     * @param restrictions the token's restrictions (stored with the link), or null
     * @param restriction the token's read restriction
     * @param excludedSources the sources hidden from the token
     */
    public record Caller(String tokenId, String tokenName, boolean admin, Viewer viewer, TokenRestrictions restrictions,
            ReadRestriction restriction, Set<String> excludedSources) {}

    /**
     * A create request ({@code ShareLinkCreate}).
     *
     * @param targetType the target type
     * @param targetRef the target reference
     * @param fieldMask the payload allowlist, or null
     * @param precisionM the precision cap in meters, or null
     * @param expiresAt the expiry, or null for 7 days
     * @param maxUses the most uses, or null
     */
    public record Create(String targetType, String targetRef, List<String> fieldMask, Integer precisionM,
            Instant expiresAt, Integer maxUses) {}

    /**
     * A created link with its plaintext key (shown once).
     *
     * @param link the stored link
     * @param key the plaintext {@code fss_} key
     */
    public record Created(ShareLinkEntity link, String key) {}

    /**
     * A page of links.
     *
     * @param items the links, newest first
     * @param nextCursor the next page's cursor, or null
     */
    public record Page(List<ShareLinkEntity> items, String nextCursor) {}

    // ------------------------------------------------------------------------------------- manage

    /**
     * Creates a link after checking the target exists, is visible to the caller and may leave on
     * {@code PUBLIC_SHARE}.
     */
    public Created create(Create request, Caller caller) {
        Instant now = clock.instant();
        List<FieldError> errors = new ArrayList<>();
        if (request.targetType() == null || !targetTypes().contains(request.targetType())) {
            errors.add(new FieldError("targetType", "must be one of " + String.join(", ", targetTypes())));
        }
        if (request.targetRef() == null || request.targetRef().isBlank() || request.targetRef().length() > 600) {
            errors.add(new FieldError("targetRef", "must be a non-empty reference of at most 600 characters"));
        }
        Instant expiresAt = request.expiresAt() == null ? now.plus(DEFAULT_TTL) : request.expiresAt();
        if (!expiresAt.isAfter(now) || expiresAt.isAfter(now.plus(MAX_TTL))) {
            errors.add(new FieldError("expiresAt", "must be in the future and at most 30 days ahead"));
        }
        if (request.maxUses() != null && request.maxUses() < 1) {
            errors.add(new FieldError("maxUses", "must be at least 1"));
        }
        if (request.precisionM() != null && request.precisionM() < 1) {
            errors.add(new FieldError("precisionM", "must be at least 1"));
        }
        if (request.fieldMask() != null) {
            for (String f : request.fieldMask()) {
                if (f == null || f.isBlank() || f.length() > 128) {
                    errors.add(new FieldError("fieldMask", "entries must be non-empty field names"));
                    break;
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The share link is invalid.", errors);
        }
        Target target = target(request.targetType(), request.targetRef().strip(), caller);
        boolean anyPrivate = target.sources().stream().anyMatch(egress::isPrivate);
        if (anyPrivate) {
            if (!caller.admin()) {
                throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.INSUFFICIENT_SCOPE,
                        "A share link to a private source needs an admin token.").withProperty("requiredScope", "admin");
            }
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.RESTRICTED,
                    "Records of private sources never leave on share links (EGR-1).");
        }
        if (target.forbidden()) {
            throw new ConflictException(ErrorCodes.SHARE_LICENCE_FORBIDDEN,
                    "The target's license does not allow redistribution, so it cannot be shared.");
        }
        Integer precision = request.precisionM();
        if (anyPrivate) {
            precision = precision == null ? ShareMask.PRIVATE_PRECISION_M : Math.max(precision, ShareMask.PRIVATE_PRECISION_M);
        }
        String key = generate();
        ShareLinkEntity link = new ShareLinkEntity(Ulids.next(now), hasher.hash(key), key.substring(0, 12),
                request.targetType(), request.targetRef().strip(),
                request.fieldMask() == null ? null : List.copyOf(new LinkedHashSet<>(request.fieldMask())), precision,
                anyPrivate, expiresAt, request.maxUses(), caller.tokenId(), now, caller.restrictions());
        links.save(link);
        audit(link, "createShareLink", "POST", caller, Map.of("targetType", link.getTargetType(), "targetRef",
                link.getTargetRef(), "expiresAt", expiresAt.toString()));
        return new Created(link, key);
    }

    /** The caller's links (all links for admin), newest first. */
    public Page list(Caller caller, String cursor, int limit) {
        List<ShareLinkEntity> page = links.page(caller.admin() ? null : caller.tokenId(), cursor == null || cursor.isBlank()
                ? null : cursor, Limit.of(limit + 1));
        String next = null;
        if (page.size() > limit) {
            page = new ArrayList<>(page.subList(0, limit));
            next = page.getLast().getId();
        }
        return new Page(page, next);
    }

    /** Revokes a link the caller created (any link for admin). */
    public void revoke(String id, Caller caller) {
        ShareLinkEntity link = links.findById(id)
                .filter(l -> caller.admin() || (l.getCreatedBy() != null && l.getCreatedBy().equals(caller.tokenId())))
                .orElseThrow(() -> NotFoundException.of("Share link", id));
        link.revoke(clock.instant());
        links.save(link);
        audit(link, "revokeShareLink", "DELETE", caller, Map.of());
    }

    /** The {@code ShareLink} JSON of a link (without its key). */
    public static Map<String, Object> json(ShareLinkEntity link, Instant now) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", link.getId());
        out.put("keyPrefix", link.getKeyPrefix());
        out.put("targetType", link.getTargetType());
        out.put("targetRef", link.getTargetRef());
        out.put("fieldMask", link.getFieldMask());
        out.put("precisionM", link.getPrecisionM());
        out.put("masked", link.isMasked());
        out.put("expiresAt", link.getExpiresAt().toString());
        out.put("maxUses", link.getMaxUses());
        out.put("uses", link.getUses());
        out.put("status", link.status(now));
        out.put("createdBy", link.getCreatedBy());
        out.put("createdAt", link.getCreatedAt().toString());
        out.put("revokedAt", link.getRevokedAt() == null ? null : link.getRevokedAt().toString());
        out.put("lastUsedAt", link.getLastUsedAt() == null ? null : link.getLastUsedAt().toString());
        return out;
    }

    // --------------------------------------------------------------------------------------- use

    /** The link a key names, whatever its status; empty for an unknown key. */
    public Optional<ShareLinkEntity> find(String key) {
        if (key == null || !key.startsWith(KEY_PREFIX)) {
            return Optional.empty();
        }
        return links.findByKeyHash(hasher.hash(key)).filter(l -> hasher.matches(key, l.getKeyHash()));
    }

    /**
     * Counts one use of the link of {@code key}: {@code 429} above {@value #USES_PER_MINUTE} per minute,
     * {@code 410 SHARE_EXPIRED} when revoked, expired or exhausted, {@code 404} for an unknown key.
     *
     * @return the link after the use
     */
    public ShareLinkEntity use(String key, String remoteIp) {
        ShareLinkEntity link = find(key).orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND,
                "Unknown share link."));
        Instant now = clock.instant();
        if (!"active".equals(link.status(now))) {
            throw gone();
        }
        ClientRateLimiter.Decision decision = limiter.tryConsume("share:" + link.getId(), RateClass.READ, USES_PER_MINUTE);
        if (!decision.allowed()) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "This share link is used too often; retry later.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(decision.retryAfterSeconds()));
        }
        if (links.use(link.getId(), now) == 0) {
            throw gone();
        }
        ShareLinkEntity used = links.findById(link.getId()).orElse(link);
        Instant last = lastAuditedUse.get(link.getId());
        if (used.getUses() <= 1 || last == null || !now.isBefore(last.plus(USE_AUDIT_INTERVAL))) {
            lastAuditedUse.put(link.getId(), now);
            audit.record(new AuditRecord(now, "share-link", null, null, null, null, "GET", "/v1/k/" + link.getKeyPrefix()
                    + "…", "openShareLink", 200, "share-links", link.getId(), remoteIp,
                    Map.of("uses", used.getUses(), "sampled", true)));
        }
        return used;
    }

    /** {@code 410 SHARE_EXPIRED}. */
    public static ForesignalException gone() {
        return new ForesignalException(HttpStatus.GONE, ErrorCodes.SHARE_EXPIRED,
                "This share link was revoked, has expired or has no uses left.");
    }

    // ------------------------------------------------------------------------------------ render

    /**
     * What a link shows.
     *
     * @param data the target's JSON after license filtering, masking and the allowlist
     * @param features the target as GeoJSON features
     * @param title a title, or null
     * @param attributions the attributions
     */
    public record Rendered(Map<String, Object> data, List<Map<String, Object>> features, String title,
            List<AttributionService.Attribution> attributions) {}

    /** Renders the link's target. */
    public Rendered render(ShareLinkEntity link) {
        ReadRestriction restriction = ReadRestriction.of(link.getRestrictionSnapshot(), true);
        Set<String> excluded = deniedSources();
        String ref = link.getTargetRef();
        return switch (link.getTargetType()) {
            case "record" -> renderRecord(link, ref, excluded, restriction);
            case "alert" -> renderAlert(link, ref, excluded, restriction);
            case "view" -> renderView(link, ref, excluded, restriction);
            case "track" -> renderTrack(link, ref, excluded, restriction);
            case "briefing-run" -> renderRun(ref, restriction);
            case "export" -> renderExport(ref);
            default -> {
                ExtraShareTarget extra = extraTargets.get(link.getTargetType());
                if (extra == null) {
                    throw new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown share target.");
                }
                yield extra.render(link);
            }
        };
    }

    /** The sources that never leave on {@code PUBLIC_SHARE}: private, local-only or not redistributable. */
    public Set<String> deniedSources() {
        Set<String> out = new LinkedHashSet<>();
        for (String id : registry.ids()) {
            if (!egress.decide(EgressPath.PUBLIC_SHARE, id, null, null, EgressPolicy.EgressContext.NONE).allowed()) {
                out.add(id);
            }
        }
        return out;
    }

    /** The view of a view link, for its tiles. */
    public SavedViewEntity view(ShareLinkEntity link) {
        return views.get(link.getTargetRef());
    }

    /** The read restriction of a view link's tiles: the creator's snapshot narrowed by the view. */
    public ReadRestriction viewRestriction(ShareLinkEntity link, SavedViewEntity view) {
        ReadRestriction base = ReadRestriction.of(link.getRestrictionSnapshot(), true);
        ReadScopes resolver = scopes.getIfAvailable();
        return resolver == null ? base : resolver.narrow(base, new ReadScope(List.of(), null, view.getId().toString(),
                null), true);
    }

    /** The export job of an export link. */
    public ExportJobEntity export(ShareLinkEntity link) {
        return exports.get(link.getTargetRef());
    }

    private Rendered renderRecord(ShareLinkEntity link, String ref, Set<String> excluded, ReadRestriction restriction) {
        String[] key = split(ref);
        RecordRow row = records.get(key[0], key[1], false, FieldSelection.ALL, GeometryOptions.DEFAULT, excluded,
                restriction).filter(r -> !r.tombstone()).filter(this::mayLeave)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "The shared record is no longer available."));
        Map<String, Object> data = recordMap(row, link);
        return new Rendered(data, List.of(feature(row, link)), row.title(), attributionsOf(List.of(row)));
    }

    private Rendered renderAlert(ShareLinkEntity link, String ref, Set<String> excluded, ReadRestriction restriction) {
        AlertView alert = visibleAlert(ref, excluded, restriction)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "The shared alert is no longer available."));
        Map<String, Object> snapshot = parse(alert.recordSnapshot());
        Map<String, Object> geometry = map(snapshot.get("geometry"));
        Map<String, Object> payload = ShareMask.payload(map(snapshot.get("payload")), maskFor(link, alert.sourceId()),
                link.getFieldMask());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", alert.id());
        data.put("ruleName", alert.ruleName());
        data.put("severity", alert.severity());
        data.put("state", alert.state());
        data.put("firedAt", alert.firedAt() == null ? null : alert.firedAt().toString());
        data.put("resolvedAt", alert.resolvedAt() == null ? null : alert.resolvedAt().toString());
        data.put("message", alert.message());
        data.put("summary", alert.summary());
        data.put("source", alert.sourceId());
        data.put("externalId", alert.externalId());
        data.put("kind", alert.kind());
        data.put("geometry", ShareMask.geometry(geometry, precisionFor(link, alert.sourceId())));
        data.put("payload", payload);
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", "alert:" + alert.id());
        feature.put("geometry", data.get("geometry"));
        Map<String, Object> props = new LinkedHashMap<>(data);
        props.remove("geometry");
        feature.put("properties", props);
        String title = alert.ruleName() != null ? alert.ruleName() : "Alert " + alert.id();
        return new Rendered(data, List.of(feature), title, attributions.scope(alert.sourceId(), alert.kind()));
    }

    private Rendered renderView(ShareLinkEntity link, String ref, Set<String> excluded, ReadRestriction restriction) {
        SavedViewEntity view = views.get(ref);
        ReadRestriction narrowed = viewRestriction(link, view);
        CurrentQuery query = new CurrentQuery(view.getSources() == null ? List.of() : List.copyOf(view.getSources()),
                List.of(), List.of(), null, null, null, null, ai.foresignal.api.support.TimeField.EFFECTIVE, null, false,
                ListOrder.ID, null, null, VIEW_RECORD_LIMIT, FieldSelection.ALL, GeometryOptions.DEFAULT,
                GeometryEncoding.GEOJSON, false, excluded, narrowed);
        List<RecordRow> rows = records.list(query).rows().stream().filter(this::mayLeave).toList();
        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> features = new ArrayList<>();
        for (RecordRow row : rows) {
            items.add(recordMap(row, link));
            features.add(feature(row, link));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", view.getId().toString());
        data.put("name", view.getName());
        data.put("slug", view.getSlug());
        data.put("records", items);
        data.put("truncated", rows.size() >= VIEW_RECORD_LIMIT);
        return new Rendered(data, features, view.getName(), attributionsOf(rows));
    }

    private Rendered renderTrack(ShareLinkEntity link, String ref, Set<String> excluded, ReadRestriction restriction) {
        String[] key = split(ref);
        RecordRow head = records.get(key[0], key[1], false, FieldSelection.ALL, GeometryOptions.DEFAULT, excluded,
                restriction).filter(this::mayLeave)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "The shared track is no longer available."));
        TrackResult track = tracks.track(new TrackQuery(key[0], key[1], null, null, null, TrackService.DEFAULT_MAX_POINTS,
                null, true, List.of(), excluded, restriction));
        Integer precision = precisionFor(link, key[0]);
        List<Object> coordinates = new ArrayList<>();
        for (Object line : track.coordinates()) {
            coordinates.add(precision == null ? line : ShareMask.coordinates(line, precision));
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("sourceId", track.sourceId());
        properties.put("externalId", track.externalId());
        properties.put("kind", track.kind());
        properties.put("from", track.from() == null ? null : track.from().toString());
        properties.put("to", track.to() == null ? null : track.to().toString());
        properties.put("times", track.times());
        properties.put("pointsReturned", track.pointsReturned());
        properties.put("truncated", track.truncated());
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", "track:" + track.sourceId() + ":" + track.externalId());
        feature.put("geometry", coordinates.isEmpty() ? null
                : Map.of("type", "MultiLineString", "coordinates", coordinates));
        feature.put("properties", properties);
        Map<String, Object> data = new LinkedHashMap<>(feature);
        return new Rendered(data, List.of(feature), head.title(), attributionsOf(List.of(head)));
    }

    private Rendered renderRun(String ref, ReadRestriction restriction) {
        String[] key = split(ref);
        UUID briefingId;
        UUID runId;
        try {
            briefingId = UUID.fromString(key[0]);
            runId = UUID.fromString(key[1]);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "The shared briefing run is no longer available.");
        }
        briefings.requireVisible(briefingId, restriction);
        BriefingRunEntity run = briefings.run(briefingId, runId);
        if (briefings.citesAny(run, deniedSources())) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "The shared briefing run is no longer available.");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("briefingId", run.getBriefingId().toString());
        data.put("runId", run.getId().toString());
        data.put("name", run.getBriefingName());
        data.put("status", run.getStatus());
        data.put("periodFrom", run.getPeriodFrom() == null ? null : run.getPeriodFrom().toString());
        data.put("periodTo", run.getPeriodTo() == null ? null : run.getPeriodTo().toString());
        data.put("startedAt", run.getStartedAt() == null ? null : run.getStartedAt().toString());
        data.put("finishedAt", run.getFinishedAt() == null ? null : run.getFinishedAt().toString());
        data.put("contentMarkdown", run.getContentMarkdown());
        List<AttributionService.Attribution> attrs = new ArrayList<>();
        for (String source : citedSources(run)) {
            attrs.addAll(attributions.scope(source, null));
        }
        return new Rendered(data, List.of(), run.getBriefingName(), attrs);
    }

    private Rendered renderExport(String ref) {
        ExportJobEntity job = exports.get(ref);
        List<String> sources = strings(job.getPlan() == null ? null : job.getPlan().get("allowedSources"));
        Set<String> denied = deniedSources();
        if (sources.stream().anyMatch(denied::contains)) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "The shared export is no longer available.");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", job.getId());
        data.put("kind", job.getKind());
        data.put("format", job.getFormat());
        data.put("status", job.getStatus());
        data.put("rowCount", job.getRowCount());
        data.put("bytes", job.getBytes());
        data.put("createdAt", job.getCreatedAt() == null ? null : job.getCreatedAt().toString());
        data.put("expiresAt", job.getExpiresAt() == null ? null : job.getExpiresAt().toString());
        data.put("sources", sources);
        List<AttributionService.Attribution> attrs = new ArrayList<>();
        for (String source : sources) {
            attrs.addAll(attributions.scope(source, null));
        }
        return new Rendered(data, List.of(), "Export " + job.getId(), attrs);
    }

    // ----------------------------------------------------------------------------- target checks

    /** What a target involves, as seen by its creator. */
    record Target(Set<String> sources, boolean forbidden) {}

    private Target target(String type, String ref, Caller caller) {
        return switch (type) {
            case "record", "track" -> {
                String[] key = splitOrBad(ref);
                RecordRow row = records.get(key[0], key[1], false, FieldSelection.ALL, GeometryOptions.DEFAULT,
                        caller.excludedSources(), caller.restriction())
                        .orElseThrow(() -> NotFoundException.of("Record", ref));
                yield new Target(Set.of(row.sourceId()), !egress.isPrivate(row.sourceId()) && !mayLeave(row));
            }
            case "alert" -> {
                AlertView alert = visibleAlert(ref, caller.excludedSources(), caller.restriction())
                        .orElseThrow(() -> NotFoundException.of("Alert", ref));
                yield new Target(Set.of(alert.sourceId()), !egress.isPrivate(alert.sourceId())
                        && !egress.decide(EgressPath.PUBLIC_SHARE, alert.sourceId(), null, null,
                                EgressPolicy.EgressContext.NONE).allowed());
            }
            case "view" -> {
                SavedViewEntity view = views.get(ref);
                if (views.compile(view).requiresToken()) {
                    throw ValidationException.forField("targetRef",
                            "views over places or watchlists cannot be shared (they reveal the operator's places)");
                }
                yield new Target(Set.of(), false);
            }
            case "briefing-run" -> {
                String[] key = splitOrBad(ref);
                UUID briefingId;
                UUID runId;
                try {
                    briefingId = UUID.fromString(key[0]);
                    runId = UUID.fromString(key[1]);
                } catch (IllegalArgumentException e) {
                    throw ValidationException.forField("targetRef", "must be {briefingId}/{runId}");
                }
                briefings.requireVisible(briefingId, caller.restriction());
                BriefingRunEntity run = briefings.run(briefingId, runId);
                Set<String> cited = new LinkedHashSet<>(citedSources(run));
                if (briefings.citesPrivate(run)) {
                    cited.addAll(briefings.privateSources());
                    yield new Target(cited, false);
                }
                yield new Target(cited, briefings.citesAny(run, deniedSources()));
            }
            case "export" -> {
                ExportJobEntity job = exports.visible(ref, caller.tokenId(), caller.admin());
                if (!ExportJobEntity.DONE.equals(job.getStatus())) {
                    throw new ConflictException(ErrorCodes.EXPORT_NOT_READY, "The export is not finished.");
                }
                List<String> sources = strings(job.getPlan() == null ? null : job.getPlan().get("allowedSources"));
                // an includePrivate export lists its private sources among allowedSources, so they count here
                Set<String> denied = deniedSources();
                boolean forbidden = sources.stream().anyMatch(s -> denied.contains(s) && !egress.isPrivate(s));
                yield new Target(new LinkedHashSet<>(sources), forbidden);
            }
            default -> {
                ExtraShareTarget extra = extraTargets.get(type);
                if (extra == null) {
                    throw ValidationException.forField("targetType", "is not supported");
                }
                yield new Target(extra.check(ref, caller), false);
            }
        };
    }

    private Optional<AlertView> visibleAlert(String ref, Set<String> excluded, ReadRestriction restriction) {
        long id;
        try {
            id = Long.parseLong(ref);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        AlertQuery query = new AlertQuery(Set.of(), List.of(), List.of(), null, null, List.of(), List.of(), null, null,
                false, excluded, restriction);
        return alerts.find(id).filter(a -> !a.suppressed() && !a.sensitive()).filter(query::matches);
    }

    /** True when the row may leave on {@code PUBLIC_SHARE} with its resolved license. */
    private boolean mayLeave(RecordRow row) {
        LicenceResolver.Resolution resolution = licences.resolve(row);
        return egress.decide(row, EgressPath.PUBLIC_SHARE, resolution == null ? null : resolution.terms(),
                EgressPolicy.EgressContext.NONE).allowed();
    }

    // ---------------------------------------------------------------------------------- helpers

    private boolean maskFor(ShareLinkEntity link, String sourceId) {
        return link.isMasked() || egress.isPrivate(sourceId);
    }

    private Integer precisionFor(ShareLinkEntity link, String sourceId) {
        Integer p = link.getPrecisionM();
        if (egress.isPrivate(sourceId)) {
            return p == null ? ShareMask.PRIVATE_PRECISION_M : Math.max(p, ShareMask.PRIVATE_PRECISION_M);
        }
        return p;
    }

    private Map<String, Object> recordMap(RecordRow row, ShareLinkEntity link) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.sourceId() + ":" + row.externalId());
        out.put("source", row.sourceId());
        out.put("externalId", row.externalId());
        out.put("kind", row.kind());
        out.put("title", row.title());
        out.put("version", row.version());
        out.put("observedAt", row.observedAt() == null ? null : row.observedAt().toString());
        out.put("eventTime", row.eventTime() == null ? null : row.eventTime().toString());
        out.put("validUntil", row.validUntil() == null ? null : row.validUntil().toString());
        out.put("geometry", ShareMask.geometry(parse(row.geometry()).isEmpty() ? null : parse(row.geometry()),
                precisionFor(link, row.sourceId())));
        out.put("payload", ShareMask.payload(parse(row.payload()), maskFor(link, row.sourceId()), link.getFieldMask()));
        return out;
    }

    private Map<String, Object> feature(RecordRow row, ShareLinkEntity link) {
        Map<String, Object> record = recordMap(row, link);
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", record.get("id"));
        feature.put("geometry", record.remove("geometry"));
        feature.put("properties", record);
        return feature;
    }

    private List<AttributionService.Attribution> attributionsOf(List<RecordRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        return attributions.aggregate(rows, licences.resolveAll(rows));
    }

    private Set<String> citedSources(BriefingRunEntity run) {
        Set<String> out = new LinkedHashSet<>();
        if (run.getCitations() != null) {
            for (Map<String, Object> c : run.getCitations()) {
                Object source = c.get("source");
                if (source == null) {
                    source = c.get("sourceId");
                }
                if (source instanceof String s && registry.contains(s)) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    private Map<String, Object> parse(String text) {
        if (text == null || text.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
        }
        return out;
    }

    private static String[] split(String ref) {
        int slash = ref.indexOf('/');
        if (slash <= 0 || slash == ref.length() - 1) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "The shared target is no longer available.");
        }
        return new String[] {ref.substring(0, slash), ref.substring(slash + 1)};
    }

    private static String[] splitOrBad(String ref) {
        int slash = ref.indexOf('/');
        if (slash <= 0 || slash == ref.length() - 1) {
            throw ValidationException.forField("targetRef", "must be {sourceId}/{externalId} or {briefingId}/{runId}");
        }
        return new String[] {ref.substring(0, slash), ref.substring(slash + 1)};
    }

    private String generate() {
        byte[] entropy = new byte[ENTROPY_BYTES];
        random.nextBytes(entropy);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
    }

    private void audit(ShareLinkEntity link, String operation, String method, Caller caller, Map<String, Object> detail) {
        UUID tokenId = null;
        try {
            tokenId = caller.tokenId() == null ? null : UUID.fromString(caller.tokenId());
        } catch (IllegalArgumentException e) {
            tokenId = null;
        }
        audit.record(new AuditRecord(clock.instant(), AuditRecord.TOKEN, tokenId, caller.tokenName(), null,
                caller.admin() ? "admin" : "agent", method, "/v1/share-links", operation, null, "share-links",
                link.getId(), null, detail));
    }
}
