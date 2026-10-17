package ai.foresignal.api;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.evidence.EvidenceQueries;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Evidence identity (prompt 116, tag {@code Evidence}): canonical events, their observations and evidence groups,
 * and the evidence identity of one record. Observations from sources the caller may not see (private sources,
 * {@code LOCAL_ONLY} refusals of {@code EgressPolicy}, a restricted token's sources) are left out.
 */
@RestController
@Tag(name = "Evidence")
@RequestMapping("/v1/evidence")
public class EvidenceController {

    private final EvidenceQueries queries;
    private final ReadCallerResolver callers;
    private final SeriesEgress egress;

    /** Creates the controller. */
    public EvidenceController(EvidenceQueries queries, ReadCallerResolver callers, SeriesEgress egress) {
        this.queries = queries;
        this.callers = callers;
        this.egress = egress;
    }

    /** {@code listCanonicalEvents}. */
    @GetMapping("/events")
    @RestrictedRead("observations of hidden sources are left out; events only from hidden sources are withheld")
    @Operation(operationId = "listCanonicalEvents", tags = "Evidence")
    public Map<String, Object> listCanonicalEvents(@RequestParam(required = false) String family,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
            @RequestParam(required = false) List<String> flags, @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "100") int limit, HttpServletRequest request) {
        return queries.list(family, region, since, split(flags), cursor, limit, excluded(request));
    }

    /** {@code getCanonicalEvent}. */
    @GetMapping("/events/{canonicalEventId}")
    @RestrictedRead("observations of hidden sources are left out; events only from hidden sources are 404")
    @Operation(operationId = "getCanonicalEvent", tags = "Evidence")
    public Map<String, Object> getCanonicalEvent(@PathVariable String canonicalEventId, HttpServletRequest request) {
        return queries.detail(canonicalEventId, excluded(request));
    }

    /** {@code getRecordEvidence}. */
    @GetMapping("/of")
    @RestrictedRead("a hidden source is 404")
    @Operation(operationId = "getRecordEvidence", tags = "Evidence")
    public Map<String, Object> getRecordEvidence(@RequestParam String source, @RequestParam String externalId,
            HttpServletRequest request) {
        return queries.ofRecord(source, externalId, excluded(request));
    }

    /** The sources the caller may not read on {@code API_READ}. */
    private Set<String> excluded(HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        return egress.excluded(caller, callers.excludedSources(caller), EgressPath.API_READ);
    }

    /** {@code flags} as one list, whether repeated or comma-separated. */
    static List<String> split(List<String> values) {
        List<String> out = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                for (String part : value.split(",")) {
                    if (!part.isBlank()) {
                        out.add(part.strip());
                    }
                }
            }
        }
        return out;
    }
}
