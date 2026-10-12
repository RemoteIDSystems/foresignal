package ai.foresignal.briefing;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.ETags;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.source.SourceRegistry;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/**
 * {@code GET /v1/digest} and the {@code get_digest} MCP tool (prompt 029): a structured catch-up
 * document polled with a seq cursor and an alert-id cursor.
 *
 * <p>{@link #prepare} validates the parameters and reads the bounds ({@code toSeq} first), so the
 * caller can answer {@code 304} on the ETag of {@code (toSeq, untilAlertId, params)} before the
 * statistics are computed. Private sources the caller may not see are counted but never listed, and
 * naming one is reported as an unknown source.
 */
@Service
public class DigestService {

    /** The largest {@code limit} (notable records per kind). */
    public static final int MAX_LIMIT = 20;

    /** The default {@code limit}. */
    public static final int DEFAULT_LIMIT = 5;

    private final DigestStatsService stats;
    private final SourceRegistry registry;

    /** Creates the service. */
    public DigestService(DigestStatsService stats, SourceRegistry registry) {
        this.stats = stats;
        this.registry = registry;
    }

    /**
     * The parameters of one digest.
     *
     * @param sinceSeq the seq cursor, or null for the last seq ingested before {@code now − 24 h}
     * @param sinceAlertId the alert cursor, or null for the first alert after {@code now − 24 h}
     * @param sources source ids
     * @param kinds kinds
     * @param bbox {@code minLon,minLat,maxLon,maxLat}, or null
     * @param limit notable records per kind (1–20)
     * @param includeHealth whether source health is included
     * @param places place ids for "Weather at places" (prompt 056); refused until then
     */
    public record Params(Long sinceSeq, Long sinceAlertId, List<String> sources, List<String> kinds, String bbox,
            int limit, boolean includeHealth, List<String> places) {

        /** Copies the lists. */
        public Params {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            places = places == null ? List.of() : List.copyOf(places);
        }
    }

    /**
     * A validated digest with its bounds and ETag.
     *
     * @param request the statistics request
     * @param bounds the bounds, read before the statistics
     * @param etag the strong ETag of {@code (toSeq, untilAlertId, params)}
     */
    public record Prepared(DigestStatsService.Request request, DigestStatsService.Bounds bounds, String etag) {}

    /**
     * Validates {@code params} for {@code caller} and reads the bounds.
     *
     * @throws ValidationException listing every problem
     */
    public Prepared prepare(Params params, McpCaller caller) {
        List<FieldError> errors = new ArrayList<>();
        Set<String> hidden = caller.excludedSources();
        for (String source : params.sources()) {
            if (!registry.contains(source) || hidden.contains(source)) {
                errors.add(new FieldError("source", "unknown source: " + source));
            }
        }
        Set<String> knownKinds = registry.kinds();
        for (String kind : params.kinds()) {
            if (!knownKinds.contains(kind)) {
                errors.add(new FieldError("kind", "unknown kind: " + kind));
            }
        }
        if (params.limit() < 1 || params.limit() > MAX_LIMIT) {
            errors.add(new FieldError("limit", "must be from 1 to " + MAX_LIMIT));
        }
        if (params.sinceSeq() != null && params.sinceSeq() < 0) {
            errors.add(new FieldError("sinceSeq", "must not be negative"));
        }
        if (params.sinceAlertId() != null && params.sinceAlertId() < 0) {
            errors.add(new FieldError("sinceAlertId", "must not be negative"));
        }
        if (!params.places().isEmpty()) {
            errors.add(new FieldError("places", "the \"Weather at places\" section is not available until prompt 056"));
        }
        BboxFilter bbox = null;
        try {
            bbox = BboxParser.parse(params.bbox());
        } catch (ValidationException e) {
            errors.addAll(e.getErrors().isEmpty() ? List.of(new FieldError("bbox", e.getMessage())) : e.getErrors());
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The digest parameters are not valid.", errors);
        }
        ScopeResolver.Resolved scope = new ScopeResolver.Resolved(params.sources(), params.kinds(), bbox, Set.of(),
                params.includeHealth());
        DigestStatsService.Request request = new DigestStatsService.Request(
                params.sinceSeq() == null ? stats.defaultSinceSeq() : params.sinceSeq(), null, null,
                params.sinceAlertId(), scope, params.limit(), hidden, ZoneOffset.UTC, null);
        DigestStatsService.Bounds bounds = stats.bounds(request);
        String canonical = "digest\n" + bounds.toSeq() + "\n" + bounds.untilAlertId() + "\n" + bounds.fromSeq() + "\n"
                + bounds.sinceAlertId() + "\n" + new TreeSet<>(params.sources()) + "\n" + new TreeSet<>(params.kinds())
                + "\n" + (bbox == null ? "" : bbox.canonical()) + "\n" + params.limit() + "\n" + params.includeHealth()
                + "\n" + caller.readCaller().canonical() + "\n" + new TreeSet<>(hidden);
        return new Prepared(request, bounds, ETags.forDocument(canonical));
    }

    /** The {@code Digest} document of {@code prepared}. */
    public Map<String, Object> render(Prepared prepared) {
        DigestStats digest = stats.compute(prepared.request(), prepared.bounds());
        Map<String, Object> out = new LinkedHashMap<>(digest.toMap());
        out.put("fromTime", digest.fromTime().toString());
        out.put("toTime", digest.toTime().toString());
        Map<String, Object> next = new LinkedHashMap<>();
        next.put("sinceSeq", digest.toSeq());
        next.put("sinceAlertId", prepared.bounds().untilAlertId());
        out.put("next", next);
        return out;
    }

    /** Validates, reads and renders in one call (the MCP tool). */
    public Map<String, Object> digest(Params params, McpCaller caller) {
        return render(prepare(params, caller));
    }
}
