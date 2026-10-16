package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.cap.CapMessageEntity;
import ai.foresignal.cap.CapMessageWriter;
import ai.foresignal.cap.CapService;
import ai.foresignal.feed.AtomFeedWriter;
import ai.foresignal.feed.Feed;
import ai.foresignal.feed.FeedEntry;
import ai.foresignal.feed.FeedService;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CAP 1.2 output (prompt 112): {@code capAtom} (an Atom 1.0 index with one entry per alert's current message,
 * linked as {@code application/cap+xml}), {@code getCapForAlert} (an alert's latest message) and
 * {@code getCapMessage} (a stored message by identifier).
 *
 * <p>All need {@code read} (open while {@code security.openReads} is on); header-less clients use a feed key
 * ({@code /v1/k/{key}/cap/alerts.atom}). An alert is served only when the caller's restriction admits it, its
 * source is not hidden and {@code EgressPolicy} allows its source on the {@code FEED} path (as the 058 alert
 * feeds). Strong ETags over the content answer {@code If-None-Match} with {@code 304}.
 */
@RestController
@Tag(name = "CAP")
public class CapController {

    /** The CAP media type with its charset. */
    static final MediaType CAP_XML = new MediaType("application", "cap+xml", StandardCharsets.UTF_8);

    /** The Atom media type with its charset. */
    static final MediaType ATOM = new MediaType("application", "atom+xml", StandardCharsets.UTF_8);

    /** The most alerts the Atom index lists. */
    static final int INDEX_LIMIT = 200;

    private final CapService cap;
    private final AlertStore alerts;
    private final AtomFeedWriter atom;
    private final ReadCallerResolver callers;
    private final EgressPolicy egress;
    private final InstallIdService installId;
    private final AppSettingService settings;

    /** Creates the controller. */
    public CapController(CapService cap, AlertStore alerts, AtomFeedWriter atom, ReadCallerResolver callers,
            EgressPolicy egress, InstallIdService installId, AppSettingService settings) {
        this.cap = cap;
        this.alerts = alerts;
        this.atom = atom;
        this.callers = callers;
        this.egress = egress;
        this.installId = installId;
        this.settings = settings;
    }

    /** {@code capAtom}: the Atom index of the current CAP message of each visible alert. */
    @GetMapping("/v1/cap/alerts.atom")
    @Operation(operationId = "capAtom", tags = "CAP")
    @RestrictedRead("the alert query carries the caller's restriction (narrowed by placeId); FEED egress applies")
    public ResponseEntity<String> capAtom(@RequestParam(required = false) List<String> ruleId,
            @RequestParam(required = false) String minSeverity, @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String placeId, @RequestParam(required = false) String since,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        Set<UUID> rules = new LinkedHashSet<>();
        for (String id : QueryParams.multi("ruleId", ruleId)) {
            try {
                rules.add(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("ruleId", "'" + id + "' is not a UUID");
            }
        }
        Severity min = minSeverity == null ? null
                : Severity.fromWire(QueryParams.oneOf("minSeverity", minSeverity, Severity.WIRE_VALUES, null));
        List<String> wantedTags = QueryParams.multi("tags", tags);
        Instant from = QueryParams.instant("since", since);
        ReadCaller caller = placeId == null || placeId.isBlank() ? callers.resolve(request)
                : callers.resolveScoped(request, new ReadScope(List.of(placeId.strip()), null, null, null));
        AlertQuery query = new AlertQuery(rules, List.of(), List.of(), null, null, List.of(), List.of(), min,
                wantedTags.size() == 1 ? wantedTags.getFirst() : null, false, callers.excludedSources(caller),
                caller.restriction());
        List<AlertView> visible = alerts.feed(query, INDEX_LIMIT).stream()
                .filter(a -> wantedTags.isEmpty() || a.tags().containsAll(wantedTags))
                .filter(a -> egress.allows(EgressPath.FEED, a.sourceId(), FeedsController.viewer(caller)))
                .toList();
        Map<Long, AlertView> byId = visible.stream().collect(Collectors.toMap(AlertView::id, Function.identity()));
        List<CapMessageEntity> latest = new ArrayList<>(cap.latest(new ArrayList<>(byId.keySet())));
        latest.removeIf(m -> from != null && m.getSent().isBefore(from));
        latest.sort(Comparator.comparing(CapMessageEntity::getSent).reversed()
                .thenComparing(CapMessageEntity::getIdentifier));
        PublicUrls urls = PublicUrls.of(settings, request);
        List<FeedEntry> entries = new ArrayList<>();
        for (CapMessageEntity m : latest) {
            AlertView alert = byId.get(m.getAlertId());
            String title = m.getHeadline() == null ? "Alert " + m.getAlertId() : m.getHeadline();
            String text = m.getMsgType() + (m.getReferences() == null ? "" : " (updates an earlier message)")
                    + " — " + (alert == null ? "" : alert.severity());
            entries.add(new FeedEntry(FeedService.TAG_PREFIX + installId.installId() + "/cap/" + m.getIdentifier(),
                    title, urls.api("/v1/cap/messages/" + m.getIdentifier() + ".cap"), m.getSent(), m.getSent(), "",
                    text, List.of(new FeedEntry.Category(m.getStatus(), FeedService.TAG_PREFIX + "cap-status")), null,
                    null, CapMessageWriter.MEDIA_TYPE));
        }
        Instant updated = latest.isEmpty() ? Instant.EPOCH : latest.getFirst().getSent();
        Feed feed = new Feed(FeedService.TAG_PREFIX + installId.installId() + "/cap", "Foresignal CAP alerts",
                "CAP 1.2 messages generated from Foresignal alerts. Not official warnings.",
                urls.api("/v1/cap/alerts.atom"), null, updated, entries);
        String body = atom.write(feed);
        return respond(body, ATOM, ifNoneMatch, caller);
    }

    /** {@code getCapForAlert}: the latest CAP message of an alert. */
    @GetMapping("/v1/cap/alerts/{alertId}.cap")
    @Operation(operationId = "getCapForAlert", tags = "CAP")
    @RestrictedRead("404 unless the alert passes the caller's restriction and FEED egress")
    public ResponseEntity<String> getCapForAlert(@PathVariable String alertId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        long id;
        try {
            id = Long.parseLong(alertId);
        } catch (NumberFormatException e) {
            throw NotFoundException.of("Alert", alertId);
        }
        ReadCaller caller = callers.resolve(request);
        visibleAlert(id, caller).orElseThrow(() -> NotFoundException.of("Alert", alertId));
        CapMessageEntity message = cap.latest(id).orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND,
                "Alert " + alertId + " has no CAP message."));
        return respond(message.getXml(), CAP_XML, ifNoneMatch, caller);
    }

    /** {@code getCapMessage}: a stored CAP message by identifier. */
    @GetMapping("/v1/cap/messages/{identifier}.cap")
    @Operation(operationId = "getCapMessage", tags = "CAP")
    @RestrictedRead("404 unless the message's alert passes the caller's restriction and FEED egress")
    public ResponseEntity<String> getCapMessage(@PathVariable String identifier,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        CapMessageEntity message = cap.message(identifier)
                .filter(m -> visibleAlert(m.getAlertId(), caller).isPresent())
                .orElseThrow(() -> NotFoundException.of("CAP message", identifier));
        return respond(message.getXml(), CAP_XML, ifNoneMatch, caller);
    }

    private Optional<AlertView> visibleAlert(long id, ReadCaller caller) {
        AlertQuery query = new AlertQuery(Set.of(), List.of(), List.of(), null, null, List.of(), List.of(), null, null,
                false, callers.excludedSources(caller), caller.restriction());
        return alerts.find(id)
                .filter(a -> !a.suppressed())
                .filter(query::matches)
                .filter(a -> egress.allows(EgressPath.FEED, a.sourceId(), FeedsController.viewer(caller)));
    }

    private static ResponseEntity<String> respond(String body, MediaType type, String ifNoneMatch, ReadCaller caller) {
        String etag = ETags.forDocument(body);
        boolean publicCache = caller.publicCache() && !FeedsController.seesPrivate(caller);
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), etag, publicCache);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(type).body(body);
    }
}
