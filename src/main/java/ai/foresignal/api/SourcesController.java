package ai.foresignal.api;

import jakarta.servlet.http.HttpServletRequest;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.api.dto.CredentialsUpdate;
import ai.foresignal.api.dto.FetchOutcomeDto;
import ai.foresignal.api.dto.FetchPage;
import ai.foresignal.api.dto.ProbeResultDto;
import ai.foresignal.api.dto.SourceDetail;
import ai.foresignal.api.dto.SourceHealthDto;
import ai.foresignal.api.dto.SourceSettingsUpdate;
import ai.foresignal.api.dto.SourceSummary;
import ai.foresignal.api.dto.UsageSeries;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.FetchLogMapper;
import ai.foresignal.api.mapper.FetchOutcomeMapper;
import ai.foresignal.api.mapper.HealthMapper;
import ai.foresignal.api.mapper.SourceMapper;
import ai.foresignal.api.mapper.UsageMapper;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.ManualFetchService;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.collect.UsageResolution;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.store.query.FetchLogQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Source management over REST: the subscription control point (OpenAPI tag {@code Sources}).
 *
 * <p>Thin on purpose: every decision lives in {@link SourceService}, {@link HealthService},
 * {@link ManualFetchService}, {@link ProbeService}, {@link UsageMeter} and {@link FetchLogQuery},
 * and mapping lives in {@code ai.foresignal.api.mapper}. Nothing here changes what a source fetches
 * except {@code updateSource}, the credential operations and the explicit {@code fetchSourceNow}.
 *
 * <p><strong>Protected operations</strong> (admin token, enforced by {@code TokenFilter} through
 * {@code AccessPolicy}, not here):
 * <ul>
 *   <li>{@code PUT /v1/sources/{sourceId}} ({@code updateSource})
 *   <li>{@code PUT /v1/sources/{sourceId}/credentials} ({@code setSourceCredentials})
 *   <li>{@code DELETE /v1/sources/{sourceId}/credentials} ({@code deleteSourceCredentials})
 *   <li>{@code POST /v1/sources/{sourceId}/fetch} ({@code fetchSourceNow})
 *   <li>{@code POST /v1/sources/{sourceId}/probe} ({@code probeSource})
 *   <li>{@code GET /v1/sources/{sourceId}/fetches} ({@code listSourceFetches})
 * </ul>
 * The other GETs need the {@code read} scope, which is open while {@code security.openReads} is on.
 *
 * <p>Credential values are accepted by {@code setSourceCredentials} and appear in no response.
 */
@RestController
@Tag(name = "Sources")
@RequestMapping("/v1/sources")
public class SourcesController {

    /** Longest range {@code getSourceUsage} serves at minute resolution. */
    static final Duration MAX_MINUTE_RANGE = Duration.ofDays(31);

    /** Range {@code getSourceUsage} covers when {@code from} is omitted. */
    static final Duration DEFAULT_USAGE_RANGE = Duration.ofHours(24);

    private final SourceRegistry registry;
    private final SourceService sources;
    private final HealthService health;
    private final ManualFetchService manualFetch;
    private final ProbeService probes;
    private final UsageMeter usage;
    private final FetchLogQuery fetchLog;
    private final SourceMapper mapper;
    private final SourceVisibility visibility;
    private final ai.foresignal.stats.StatsSettingsService statsSettings;

    /** The per-source region tagging mode (prompt 040); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.reference.RegionTaggingService regionTagging;
    private final Clock clock;

    /** Creates the controller. */
    public SourcesController(
            SourceRegistry registry,
            SourceService sources,
            HealthService health,
            ManualFetchService manualFetch,
            ProbeService probes,
            UsageMeter usage,
            FetchLogQuery fetchLog,
            SourceMapper mapper,
            SourceVisibility visibility,
            ai.foresignal.stats.StatsSettingsService statsSettings,
            Clock clock) {
        this.visibility = visibility;
        this.statsSettings = statsSettings;
        this.registry = registry;
        this.sources = sources;
        this.health = health;
        this.manualFetch = manualFetch;
        this.probes = probes;
        this.usage = usage;
        this.fetchLog = fetchLog;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** {@code listSources}: every source, sorted by id. */
    @GetMapping
    @Operation(operationId = "listSources", tags = "Sources")
    public List<SourceSummary> listSources(HttpServletRequest request) {
        Optional<CurrentToken> caller = CurrentToken.of(request);
        return sources.list().stream()
                .filter(view -> visibility.visible(view.id(), caller, RequestPrincipal.of(request).orElse(null)))
                .sorted(Comparator.comparing(SourceView::id))
                .map(mapper::summary)
                .toList();
    }

    /**
     * {@code listSourceHealth}: health of every source, optionally filtered by status (repeatable or
     * comma-separated). Mapped as a literal path, which Spring prefers over {@code /{sourceId}}.
     */
    @GetMapping("/health")
    @Operation(operationId = "listSourceHealth", tags = "Sources")
    public List<SourceHealthDto> listSourceHealth(@RequestParam(name = "status", required = false) List<String> status,
            HttpServletRequest request) {
        Optional<CurrentToken> caller = CurrentToken.of(request);
        Set<HealthStatus> wanted = parseAll(status, "status", HealthStatus.class, HealthStatus::fromValue);
        Optional<Set<HealthStatus>> filter = wanted.isEmpty() ? Optional.empty() : Optional.of(wanted);
        return health.all(filter).stream()
                .filter(v -> visibility.visible(v.sourceId(), caller, RequestPrincipal.of(request).orElse(null)))
                .sorted(Comparator.comparing(v -> v.sourceId()))
                .map(HealthMapper::health)
                .toList();
    }

    /** {@code getSource}: descriptor, settings and health; 404 for an unknown id. */
    @GetMapping("/{sourceId}")
    @Operation(operationId = "getSource", tags = "Sources")
    public SourceDetail getSource(@PathVariable String sourceId, HttpServletRequest request) {
        requireVisible(sourceId, request);
        return detail(sourceId);
    }

    /**
     * {@code updateSource}: a partial settings change. Omitted fields are unchanged; explicit
     * {@code null} for {@code retention}, {@code ingestPolicy} or {@code downsampleInterval} (prompt
     * 044, TRACK sources only) clears that override.
     *
     * <p>400 {@code VALIDATION_FAILED}, {@code INGEST_POLICY_INVALID}, {@code RETENTION_TIER_FIXED} or
     * {@code EGRESS_FIXED} (prompt 069: {@code egress} on a source that is not {@code egressOverridable});
     * 409 {@code CREDENTIALS_REQUIRED}.
     */
    @PutMapping("/{sourceId}")
    @Operation(operationId = "updateSource", tags = "Sources")
    public SourceDetail updateSource(
            @PathVariable String sourceId, @Valid @RequestBody SourceSettingsUpdate body) {
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        if (body.getRegionTagging() != null && !ai.foresignal.reference.RegionTagMode.WIRE_VALUES.contains(
                body.getRegionTagging().strip().toLowerCase(java.util.Locale.ROOT))) {
            throw ValidationException.forField("regionTagging", "must be one of "
                    + ai.foresignal.reference.RegionTagMode.WIRE_VALUES);
        }
        if (Boolean.FALSE.equals(body.getPrivateSource()) && descriptor.privateSource()) {
            throw ValidationException.forField("private",
                    "this source is private by definition; it cannot be made public");
        }
        requirePrivacyForParams(sourceId, descriptor, body);
        if (body.getEmbed() != null && body.getEmbed()
                && (descriptor.privateSource() || Boolean.TRUE.equals(body.getPrivateSource())
                        || (body.getPrivateSource() == null && visibility.isPrivate(sourceId)))) {
            throw ValidationException.forField("embed", "records of a private source are never embedded");
        }
        sources.update(sourceId, mapper.update(body, descriptor));
        // The operator's private tightening (prompt 039) lives in app_setting['visibility'].
        visibility.setOperatorPrivate(sourceId, body.getPrivateSource());
        // statsEnabled and statsIncludePrivate (prompt 038) live in app_setting['stats'].
        statsSettings.update(sourceId, body.getStatsEnabled(), body.getStatsIncludePrivate());
        // regionTagging (prompt 040) lives in app_setting['regionTagging'].
        if (regionTagging != null) {
            regionTagging.update(sourceId, body.getRegionTagging());
        }
        return detail(sourceId);
    }

    /**
     * 400 {@code PRIVATE_REQUIRED} (prompt 086) when a parameter value the adapter allows only on a private source
     * ({@code SourceAdapter.paramsRequiringPrivacy}) would be in force while the source is not effectively private:
     * set on a public source, or kept while the source is being made public. Effective privacy is the descriptor's
     * flag OR the operator's setting (prompt 039), taking this request's {@code private} into account.
     */
    private void requirePrivacyForParams(String sourceId, SourceDescriptor descriptor, SourceSettingsUpdate body) {
        if (descriptor.privateSource()) {
            return;
        }
        boolean effectivelyPrivate = body.getPrivateSource() != null ? body.getPrivateSource()
                : visibility.isPrivate(sourceId);
        if (effectivelyPrivate) {
            return;
        }
        java.util.Map<String, Object> params = body.getParams();
        if (params == null) {
            params = sources.effectiveParams(sourceId).values();
        }
        java.util.Map<String, Object> checked = params;
        java.util.List<String> names = registry.find(sourceId)
                .flatMap(ai.foresignal.source.SourceRegistry.Registration::adapterIfPresent)
                .map(adapter -> adapter.paramsRequiringPrivacy(checked))
                .orElseGet(java.util.List::of);
        if (names.isEmpty()) {
            return;
        }
        throw new ai.foresignal.api.error.ValidationException(ai.foresignal.api.error.ErrorCodes.PRIVATE_REQUIRED,
                sourceId + " is not private, so " + String.join(", ", names) + " cannot be set; set private=true in"
                        + " the same request, or leave it off.",
                names.stream().map(n -> new ai.foresignal.api.error.FieldError("params." + n,
                        "allowed only while the source is private")).toList());
    }

    /** {@code setSourceCredentials}: stores credentials, encrypted; 204. */
    @PutMapping("/{sourceId}/credentials")
    @Operation(operationId = "setSourceCredentials", tags = "Sources")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setSourceCredentials(
            @PathVariable String sourceId, @Valid @RequestBody CredentialsUpdate body) {
        sources.setCredentials(sourceId, body.values());
    }

    /** {@code deleteSourceCredentials}: removes credentials (and disables a source needing them); 204. */
    @DeleteMapping("/{sourceId}/credentials")
    @Operation(operationId = "deleteSourceCredentials", tags = "Sources")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSourceCredentials(@PathVariable String sourceId) {
        sources.deleteCredentials(sourceId);
    }

    /** {@code fetchSourceNow}: one MANUAL fetch, ingested normally; 409 and 429 as in prompt 010. */
    @PostMapping("/{sourceId}/fetch")
    @Operation(operationId = "fetchSourceNow", tags = "Sources")
    public FetchOutcomeDto fetchSourceNow(@PathVariable String sourceId) {
        return FetchOutcomeMapper.outcome(manualFetch.fetchNow(sourceId));
    }

    /**
     * {@code probeSource}: a diagnostic fetch or stream capture that never ingests. {@code maxBytes}
     * 1024..10485760 (default 262144), {@code seconds} 1..60 (default 10), both checked by
     * {@link ProbeService}.
     */
    @PostMapping("/{sourceId}/probe")
    @Operation(operationId = "probeSource", tags = "Sources")
    public ProbeResultDto probeSource(
            @PathVariable String sourceId,
            @RequestParam(name = "maxBytes", required = false) Integer maxBytes,
            @RequestParam(name = "seconds", required = false) Integer seconds) {
        return FetchOutcomeMapper.probe(probes.probe(sourceId, maxBytes, seconds));
    }

    /** {@code getSourceHealth}: health of one source. */
    @GetMapping("/{sourceId}/health")
    @Operation(operationId = "getSourceHealth", tags = "Sources")
    public SourceHealthDto getSourceHealth(@PathVariable String sourceId, HttpServletRequest request) {
        requireVisible(sourceId, request);
        return HealthMapper.health(health.health(sourceId));
    }

    /**
     * {@code getSourceUsage}: request, byte and ingest counters per bucket over {@code [from, to)},
     * with totals. Defaults: the last 24 hours at hour resolution. A range over 31 days at minute
     * resolution, or a {@code from} not before {@code to}, is 400.
     */
    @GetMapping("/{sourceId}/usage")
    @Operation(operationId = "getSourceUsage", tags = "Sources")
    public UsageSeries getSourceUsage(
            @PathVariable String sourceId,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "resolution", required = false) String resolution,
            HttpServletRequest request) {
        registry.descriptor(sourceId);
        requireVisible(sourceId, request);
        Instant now = clock.instant();
        Instant end = to == null ? now : parseInstant("to", to);
        Instant start = from == null ? end.minus(DEFAULT_USAGE_RANGE) : parseInstant("from", from);
        UsageResolution res = parseResolution(resolution);
        if (!start.isBefore(end)) {
            throw ValidationException.forField("from", "must be before to");
        }
        if (res == UsageResolution.MINUTE && Duration.between(start, end).compareTo(MAX_MINUTE_RANGE) > 0) {
            throw ValidationException.forField(
                    "resolution", "minute resolution serves at most 31 days; use hour or day for longer ranges");
        }
        return UsageMapper.series(sourceId, start, res, usage.series(sourceId, start, end, res));
    }

    /**
     * {@code listSourceFetches}: the source's {@code fetch_log}, newest first, keyset-paged on id.
     * {@code limit} 1..1000 (default 100); {@code status} and {@code trigger} are repeatable or
     * comma-separated.
     */
    @GetMapping("/{sourceId}/fetches")
    @Operation(operationId = "listSourceFetches", tags = "Sources")
    public FetchPage listSourceFetches(
            @PathVariable String sourceId,
            @RequestParam(name = "after", required = false) Long after,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "status", required = false) List<String> status,
            @RequestParam(name = "trigger", required = false) List<String> trigger) {
        registry.descriptor(sourceId);
        int size = limit == null ? FetchLogQuery.DEFAULT_LIMIT : limit;
        if (size < 1 || size > FetchLogQuery.MAX_LIMIT) {
            throw ValidationException.forField("limit", "must be between 1 and " + FetchLogQuery.MAX_LIMIT);
        }
        if (after != null && after < 0) {
            throw ValidationException.forField("after", "must not be negative");
        }
        Set<FetchStatus> statuses = parseAll(status, "status", FetchStatus.class, SourcesController::fetchStatus);
        Set<FetchTrigger> triggers = parseAll(trigger, "trigger", FetchTrigger.class, SourcesController::fetchTrigger);
        return FetchLogMapper.page(fetchLog.page(sourceId, after, size, statuses, triggers));
    }

    // ---------- helpers ----------

    /**
     * 404 for a private source the caller may not see, exactly as for an unknown one
     * ({@link SourceVisibility}, prompts 023 and 039).
     */
    private void requireVisible(String sourceId, HttpServletRequest request) {
        if (!visibility.visible(sourceId, CurrentToken.of(request), RequestPrincipal.of(request).orElse(null))) {
            throw NotFoundException.of("Source", sourceId);
        }
    }

    private SourceDetail detail(String sourceId) {
        SourceView view = sources.view(sourceId);
        return new SourceDetail(
                mapper.descriptor(view.descriptor()),
                mapper.settings(
                        view,
                        sources.ingestPolicyOverride(sourceId),
                        sources.retentionOverride(sourceId),
                        sources.effectiveRetention(sourceId),
                        sources.downsampleIntervalOverride(sourceId),
                        sources.egressOverride(sourceId)),
                HealthMapper.health(health.health(sourceId)));
    }

    private static FetchStatus fetchStatus(String value) {
        return FetchStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private static FetchTrigger fetchTrigger(String value) {
        return FetchTrigger.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private static <E extends Enum<E>> Set<E> parseAll(
            List<String> values, String field, Class<E> type, Function<String, E> parse) {
        Set<E> out = EnumSet.noneOf(type);
        if (values == null) {
            return out;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                out.add(parse.apply(value));
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField(field, "unknown value '" + value + "'");
            }
        }
        return out;
    }

    private static Instant parseInstant(String field, String value) {
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(field, "must be an ISO-8601 instant, for example 2026-10-11T00:00:00Z");
        }
    }

    private static UsageResolution parseResolution(String value) {
        if (value == null || value.isBlank()) {
            return UsageResolution.HOUR;
        }
        try {
            return UsageResolution.fromWireValue(value);
        } catch (InvalidInputException e) {
            throw ValidationException.forField("resolution", "must be minute, hour or day");
        }
    }
}
