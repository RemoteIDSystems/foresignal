package ai.foresignal.precursor;

import ai.foresignal.analytics.eventstudy.EventSelectorResolver;
import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.PrecursorSummaries;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The precursor library API (prompt 120): list and read precursors, retire or pin them (logged in their history and
 * labeled), read batches, manage operator candidates, and start mining or re-test runs as 103 jobs of type
 * {@code precursor-mining}, at most one queued or running at a time ({@code 409 PRECURSOR_MINING_IN_PROGRESS}).
 *
 * <p>Also resolves precursor summaries for the {@code precursor.*} events of the {@code analytics} topic.
 */
@Service
public class PrecursorService implements PrecursorSummaries {

    private static final Logger log = LoggerFactory.getLogger(PrecursorService.class);

    /** The statuses. */
    public static final List<String> STATUSES = List.of(PrecursorRules.CANDIDATE, PrecursorRules.VALIDATED,
            PrecursorRules.STALE, PrecursorRules.RETIRED);

    /** The kinds. */
    public static final List<String> KINDS = List.of(Candidate.EVENT, Candidate.SERIES);

    /** The longest event lag of an operator candidate (as 103). */
    public static final Duration MAX_EVENT_LAG = Duration.ofDays(30);

    /** The longest series lag of an operator candidate. */
    public static final Duration MAX_SERIES_LAG = Duration.ofDays(8 * 7);

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");
    private static final Set<String> CANDIDATE_FIELDS = Set.of("name", "kind", "antecedent", "consequent", "spatial",
            "lagMin", "lagMax", "regionCodes");
    private static final Set<String> SIDE_FIELDS = Set.of("selector", "seriesRef", "family", "label", "threshold");

    private final PrecursorRepository precursors;
    private final PrecursorBatchRepository batches;
    private final PrecursorCandidateRepository candidates;
    private final PrecursorOccurrences occurrences;
    private final PrecursorMiner miner;
    private final BacktestJobService jobs;
    private final EventSelectorResolver selectors;
    private final SeriesReader reader;
    private final FindingService findings;
    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public PrecursorService(PrecursorRepository precursors, PrecursorBatchRepository batches,
            PrecursorCandidateRepository candidates, PrecursorOccurrences occurrences, PrecursorMiner miner,
            BacktestJobService jobs, EventSelectorResolver selectors, SeriesReader reader, FindingService findings,
            NamedParameterJdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.precursors = precursors;
        this.batches = batches;
        this.candidates = candidates;
        this.occurrences = occurrences;
        this.miner = miner;
        this.jobs = jobs;
        this.selectors = selectors;
        this.reader = reader;
        this.findings = findings;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- library

    /**
     * A page of ids.
     *
     * @param <T> the row type
     * @param items the rows
     * @param nextCursor the next cursor, or null
     */
    public record Page<T>(List<T> items, String nextCursor) {}

    /**
     * Lists the library, newest first (keyset on the id).
     *
     * @param statuses statuses (any), or empty
     * @param kind a kind, or null
     * @param regions region codes (overlap), or empty
     * @param families families (overlap), or empty
     * @param cursor the cursor, or null
     * @param limit the page size
     * @return the page
     */
    @Transactional(readOnly = true)
    public Page<PrecursorEntity> list(List<String> statuses, String kind, List<String> regions, List<String> families,
            String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id FROM precursor WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (statuses != null && !statuses.isEmpty()) {
            sql.append(" AND status IN (:statuses)");
            p.addValue("statuses", statuses);
        }
        if (kind != null) {
            sql.append(" AND kind = :kind");
            p.addValue("kind", kind);
        }
        if (regions != null && !regions.isEmpty()) {
            sql.append(" AND region_codes && CAST(:regions AS text[])");
            p.addValue("regions", ai.foresignal.store.query.PgArrays.text(regions));
        }
        if (families != null && !families.isEmpty()) {
            sql.append(" AND families && CAST(:families AS text[])");
            p.addValue("families", ai.foresignal.store.query.PgArrays.text(families));
        }
        String after = decode(cursor);
        if (after != null) {
            sql.append(" AND id < :after");
            p.addValue("after", after);
        }
        sql.append(" ORDER BY id DESC LIMIT :limit");
        p.addValue("limit", limit + 1);
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        List<PrecursorEntity> items = new ArrayList<>();
        for (int i = 0; i < ids.size() && i < limit; i++) {
            precursors.findById(ids.get(i)).ifPresent(items::add);
        }
        return new Page<>(items, ids.size() > limit && !items.isEmpty() ? encode(items.get(items.size() - 1).getId())
                : null);
    }

    /**
     * One precursor.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_PRECURSOR}
     */
    @Transactional(readOnly = true)
    public PrecursorEntity get(String id) {
        PrecursorEntity p = id == null ? null : precursors.findById(id).orElse(null);
        if (p == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PRECURSOR, "No precursor '" + id + "'.");
        }
        return p;
    }

    /**
     * Retires a precursor with a reason, or pins it as operator-validated ({@code PrecursorPatch}). The change is
     * logged in the precursor's history with the actor, and a pinned precursor carries the {@code OPERATOR_VALIDATED}
     * caveat.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_PRECURSOR}
     * @throws ValidationException 400 for anything but {@code {status: retired|validated, reason}}
     */
    @Transactional
    public PrecursorEntity patch(String id, JsonNode body, String actor) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PrecursorPatch object.");
        }
        for (String field : body.propertyNames()) {
            if (!Set.of("status", "reason").contains(field)) {
                throw ValidationException.forField(field, "is not a PrecursorPatch member");
            }
        }
        String status = body.hasNonNull("status") ? body.get("status").asString() : null;
        if (!PrecursorRules.RETIRED.equals(status) && !PrecursorRules.VALIDATED.equals(status)) {
            throw ValidationException.forField("status", "must be retired or validated");
        }
        String reason = body.hasNonNull("reason") ? body.get("reason").asString().strip() : "";
        if (reason.isEmpty() || reason.length() > 2000) {
            throw ValidationException.forField("reason", "is required (1 to 2000 characters)");
        }
        PrecursorEntity p = get(id);
        Instant now = clock.instant();
        String before = p.getStatus();
        String by = actor == null ? "operator" : actor;
        String event;
        if (PrecursorRules.RETIRED.equals(status)) {
            p.setStatus(PrecursorRules.RETIRED);
            p.setStatusReason("retired by an operator: " + reason);
            p.setOperatorValidated(false);
            event = FindingEvent.PRECURSOR_RETIRED;
        } else {
            p.setStatus(PrecursorRules.VALIDATED);
            p.setStatusReason("pinned as operator-validated: " + reason);
            p.setOperatorValidated(true);
            if (p.getFirstValidatedAt() == null) {
                p.setFirstValidatedAt(now);
            }
            List<Map<String, Object>> caveats = new ArrayList<>(p.getCaveats());
            if (caveats.stream().noneMatch(c -> "OPERATOR_VALIDATED".equals(c.get("code")))) {
                Map<String, Object> caveat = new LinkedHashMap<>();
                caveat.put("code", "OPERATOR_VALIDATED");
                caveat.put("text", "An operator pinned this pattern as validated; the statistics alone may not"
                        + " support it.");
                caveats.add(caveat);
            }
            p.setCaveats(caveats);
            event = FindingEvent.PRECURSOR_VALIDATED;
        }
        p.setHistory(PrecursorMiner.append(p.getHistory(), now, by, before, p.getStatus(), p.getStatusReason()));
        p.setUpdatedAt(now);
        precursors.save(p);
        log.info("Precursor {} set to {} by {}: {}", p.getId(), p.getStatus(), by, reason);
        findings.announcePrecursor(p.getId(), event, by, p.getStatusReason());
        return p;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> summary(String precursorId) {
        return precursors.findById(precursorId).map(PrecursorViews::summary).orElse(null);
    }

    // ------------------------------------------------------------------------- batches and runs

    /** Batches newest first (keyset on the id). */
    @Transactional(readOnly = true)
    public Page<PrecursorBatchEntity> batches(String cursor, int limit) {
        List<String> ids = keyset("precursor_batch", cursor, limit);
        List<PrecursorBatchEntity> items = new ArrayList<>();
        for (int i = 0; i < ids.size() && i < limit; i++) {
            batches.findById(ids.get(i)).ifPresent(items::add);
        }
        return new Page<>(items, ids.size() > limit && !items.isEmpty() ? encode(items.get(items.size() - 1).getId())
                : null);
    }

    /**
     * One batch.
     *
     * @throws NotFoundException 404 {@code NOT_FOUND}
     */
    @Transactional(readOnly = true)
    public PrecursorBatchEntity batch(String id) {
        return (id == null ? java.util.Optional.<PrecursorBatchEntity>empty() : batches.findById(id))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "No precursor batch '" + id + "'."));
    }

    /**
     * Starts a mining or re-test run as a 103 job.
     *
     * @param mode {@code mine} or {@code retest}
     * @param trigger {@code schedule}, {@code manual} or {@code cycle}
     * @param tokenId the submitting token, or null for the schedule and cycles
     * @return the queued job
     * @throws ConflictException 409 {@code PRECURSOR_MINING_IN_PROGRESS} while a run is queued or running
     */
    @Transactional
    public BacktestJobEntity submit(String mode, String trigger, String tokenId) {
        Long inFlight = jdbc.queryForObject("SELECT count(*) FROM backtest_job WHERE type = :type AND status IN"
                + " ('queued', 'running')", new MapSqlParameterSource("type", PrecursorJobRunner.TYPE), Long.class);
        if ((inFlight != null && inFlight > 0) || miner.running()) {
            throw new ConflictException(ErrorCodes.PRECURSOR_MINING_IN_PROGRESS,
                    "A precursor mining or re-test run is already queued or running.");
        }
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("mode", "retest".equals(mode) ? "retest" : "mine");
        definition.put("trigger", trigger == null ? "manual" : trigger);
        return jobs.submitRunnerJob(PrecursorJobRunner.TYPE, null, definition, tokenId);
    }

    // ------------------------------------------------------------------------- candidates

    /** Operator candidates, newest first (keyset on the id). */
    @Transactional(readOnly = true)
    public Page<PrecursorCandidateEntity> candidates(String cursor, int limit) {
        List<String> ids = keyset("precursor_candidate", cursor, limit);
        List<PrecursorCandidateEntity> items = new ArrayList<>();
        for (int i = 0; i < ids.size() && i < limit; i++) {
            candidates.findById(ids.get(i)).ifPresent(items::add);
        }
        return new Page<>(items, ids.size() > limit && !items.isEmpty() ? encode(items.get(items.size() - 1).getId())
                : null);
    }

    /**
     * Registers an operator candidate ({@code PrecursorCandidateInput}).
     *
     * @throws ValidationException 400 {@code VALIDATION_FAILED} with the failing fields
     */
    @Transactional
    public PrecursorCandidateEntity createCandidate(JsonNode body, String actor) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PrecursorCandidateInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!CANDIDATE_FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a PrecursorCandidateInput member"));
            }
        }
        String kind = body.hasNonNull("kind") ? body.get("kind").asString() : null;
        if (!KINDS.contains(kind)) {
            errors.add(new FieldError("kind", "must be event or series"));
        }
        String name = body.hasNonNull("name") ? body.get("name").asString() : null;
        if (name != null && name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }
        Map<String, Object> antecedent = side(body, "antecedent", kind, errors);
        Map<String, Object> consequent = side(body, "consequent", kind, errors);
        Duration lagMax = duration(body, "lagMax", errors);
        Duration lagMin = duration(body, "lagMin", errors);
        boolean series = Candidate.SERIES.equals(kind);
        if (lagMax == null && errors.stream().noneMatch(e -> e.field().equals("lagMax"))) {
            errors.add(new FieldError("lagMax", "is required"));
        }
        if (lagMin == null) {
            lagMin = series ? Duration.ofDays(1) : Duration.ZERO;
        }
        if (lagMax != null) {
            Duration cap = series ? MAX_SERIES_LAG : MAX_EVENT_LAG;
            if (lagMax.isNegative() || lagMax.isZero() || lagMax.compareTo(cap) > 0) {
                errors.add(new FieldError("lagMax", "must be more than zero and at most " + PrecursorRules.iso(cap)));
            } else if (series && lagMax.toSeconds() % 86_400 != 0) {
                errors.add(new FieldError("lagMax", "must be a whole number of days for a series candidate"));
            }
            if (lagMin.isNegative() || lagMin.compareTo(lagMax) > 0) {
                errors.add(new FieldError("lagMin", "must be between zero and lagMax"));
            }
        }
        Map<String, Object> spatial = spatial(body.get("spatial"), errors);
        List<String> regions = new ArrayList<>();
        JsonNode regionNode = body.get("regionCodes");
        if (regionNode != null && !regionNode.isNull()) {
            if (!regionNode.isArray()) {
                errors.add(new FieldError("regionCodes", "must be an array of 040 region codes"));
            } else {
                for (JsonNode code : regionNode) {
                    String value = code.asString();
                    if (!REGION.matcher(value).matches()) {
                        errors.add(new FieldError("regionCodes", "'" + value + "' is not a 040 region code"));
                    } else if (!regions.contains(value)) {
                        regions.add(value);
                    }
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The precursor candidate is not valid.",
                    errors);
        }
        Instant now = clock.instant();
        PrecursorCandidateEntity c = new PrecursorCandidateEntity(Ulids.next(now), kind,
                actor == null ? "operator" : actor, now);
        c.setName(name);
        c.setAntecedent(antecedent);
        c.setConsequent(consequent);
        c.setSpatial(series ? Map.of("rule", "none") : spatial);
        c.setLagMinSeconds(lagMin.toSeconds());
        c.setLagMaxSeconds(lagMax.toSeconds());
        c.setRegionCodes(regions);
        return candidates.save(c);
    }

    /**
     * Deletes an operator candidate; precursors already in the library are kept.
     *
     * @throws NotFoundException 404 {@code NOT_FOUND}
     */
    @Transactional
    public void deleteCandidate(String id) {
        PrecursorCandidateEntity c = (id == null ? java.util.Optional.<PrecursorCandidateEntity>empty()
                : candidates.findById(id)).orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND,
                        "No precursor candidate '" + id + "'."));
        candidates.delete(c);
    }

    private Map<String, Object> side(JsonNode body, String field, String kind, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || !node.isObject()) {
            errors.add(new FieldError(field, "is required (a PrecursorSide object)"));
            return Map.of();
        }
        for (String member : node.propertyNames()) {
            if (!SIDE_FIELDS.contains(member)) {
                errors.add(new FieldError(field + "." + member, "is not a PrecursorSide member"));
            }
        }
        Map<String, Object> side = json.readValue(json.writeValueAsString(node), new TypeReference<>() {});
        if (Candidate.EVENT.equals(kind)) {
            Map<String, Object> selector = Candidate.selector(side);
            if (selector == null) {
                errors.add(new FieldError(field + ".selector", "is required for an event candidate"));
                return side;
            }
            try {
                selectors.validate(selector);
            } catch (ValidationException e) {
                errors.add(new FieldError(field + ".selector", e.getMessage()));
                return side;
            }
            side.remove("seriesRef");
            side.putIfAbsent("family", occurrences.signalFamily(selector) != null ? occurrences.signalFamily(selector)
                    : PrecursorOccurrences.familyKey(selector));
            side.putIfAbsent("label", PrecursorOccurrences.label(selector));
        } else if (Candidate.SERIES.equals(kind)) {
            String ref = Candidate.seriesRef(side);
            if (ref == null) {
                errors.add(new FieldError(field + ".seriesRef", "is required for a series candidate"));
                return side;
            }
            SeriesRef parsed;
            try {
                parsed = SeriesRef.parse(ref);
            } catch (RuntimeException e) {
                errors.add(new FieldError(field + ".seriesRef", "is not a series reference"));
                return side;
            }
            if (!reader.exists(parsed)) {
                errors.add(new FieldError(field + ".seriesRef", "names no series"));
                return side;
            }
            side.put("seriesRef", parsed.format());
            side.remove("selector");
            if (side.get("family") == null) {
                try {
                    side.put("family", reader.meta(parsed).signalFamily());
                } catch (RuntimeException e) {
                    side.put("family", null);
                }
            }
            side.putIfAbsent("label", ai.foresignal.analytics.AnalyticsSupport.label(parsed));
            Object threshold = side.get("threshold");
            if (threshold != null && !(threshold instanceof Number n && n.doubleValue() > 0)) {
                errors.add(new FieldError(field + ".threshold", "must be a positive number"));
            }
            side.putIfAbsent("threshold", PrecursorRules.LIVE_THRESHOLD);
        }
        return side;
    }

    private static Map<String, Object> spatial(JsonNode node, List<FieldError> errors) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            out.put("rule", "none");
            return out;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("spatial", "must be a PrecursorSpatial object"));
            return out;
        }
        String rule = node.hasNonNull("rule") ? node.get("rule").asString() : null;
        if (rule == null || !List.of("sameEntity", "withinKm", "sameRegion", "none").contains(rule)) {
            errors.add(new FieldError("spatial.rule", "must be sameEntity, withinKm, sameRegion or none"));
            return out;
        }
        out.put("rule", rule);
        if ("withinKm".equals(rule)) {
            JsonNode km = node.get("withinKm");
            if (km == null || !km.isNumber() || km.asDouble() <= 0) {
                errors.add(new FieldError("spatial.withinKm", "must be a positive number"));
            } else {
                out.put("withinKm", km.asDouble());
            }
        }
        if ("sameRegion".equals(rule)) {
            String level = node.hasNonNull("regionLevel") ? node.get("regionLevel").asString() : "country";
            if (!"country".equals(level) && !"admin1".equals(level)) {
                errors.add(new FieldError("spatial.regionLevel", "must be country or admin1"));
            } else {
                out.put("regionLevel", level);
            }
        }
        return out;
    }

    private static Duration duration(JsonNode body, String field, List<FieldError> errors) {
        if (!body.hasNonNull(field)) {
            return null;
        }
        try {
            return Duration.parse(body.get(field).asString());
        } catch (DateTimeParseException e) {
            errors.add(new FieldError(field, "must be an ISO-8601 duration such as P3D"));
            return null;
        }
    }

    private List<String> keyset(String table, String cursor, int limit) {
        String after = decode(cursor);
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        String where = "";
        if (after != null) {
            where = " WHERE id < :after";
            p.addValue("after", after);
        }
        return jdbc.queryForList("SELECT id FROM " + table + where + " ORDER BY id DESC LIMIT :limit", p, String.class);
    }

    private static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String id = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (Ulids.isUlid(id)) {
                return id;
            }
        } catch (IllegalArgumentException e) {
            // fall through
        }
        throw ValidationException.forField("cursor", "is not a cursor of this list");
    }
}
