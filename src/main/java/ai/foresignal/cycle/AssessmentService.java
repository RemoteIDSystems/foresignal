package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystFeedbackEntity;
import ai.foresignal.analyst.AnalystFeedbackRepository;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationService;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Reads assessments, cycle runs and their views (prompt 128 §4, §7) and records assessment feedback into the 102
 * analyst feedback (target {@code assessment}). Private assessments, and the runs that produced them, are visible
 * only to callers allowed private data; to others they are {@code 404}.
 */
@Service
public class AssessmentService {

    /** The feedback target of assessments. */
    public static final String FEEDBACK_TARGET = "assessment";

    /** The ratings. */
    public static final List<String> RATINGS = List.of("useful", "not_useful", "wrong");

    /** The longest feedback comment. */
    public static final int MAX_COMMENT = 4000;

    private final AssessmentRepository assessments;
    private final CycleRunRepository runs;
    private final MaintenanceRunRepository maintenance;
    private final AnalystFeedbackRepository feedback;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public AssessmentService(AssessmentRepository assessments, CycleRunRepository runs,
            MaintenanceRunRepository maintenance, AnalystFeedbackRepository feedback, NamedParameterJdbcTemplate jdbc,
            Clock clock) {
        this.assessments = assessments;
        this.runs = runs;
        this.maintenance = maintenance;
        this.feedback = feedback;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * A page.
     *
     * @param items the items
     * @param nextCursor the next cursor, or null
     * @param <T> the item type
     */
    public record Page<T>(List<T> items, String nextCursor) {}

    /**
     * Assessments newest first.
     *
     * @param cadence the cadence, or null
     * @param situationId the situation, or null
     * @param since {@code created_at ≥ since}, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     * @param seesPrivate whether private assessments are listed
     * @throws ValidationException 400 for a bad cursor
     */
    public Page<AssessmentEntity> list(String cadence, String situationId, Instant since, String cursor, int limit,
            boolean seesPrivate) {
        StringBuilder sql = new StringBuilder("SELECT id FROM assessment WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("limit", limit + 1);
        if (cadence != null) {
            sql.append(" AND cadence = :cadence");
            p.addValue("cadence", cadence);
        }
        if (situationId != null) {
            sql.append(" AND situation_id = :situation");
            p.addValue("situation", situationId);
        }
        if (since != null) {
            sql.append(" AND created_at >= :since");
            p.addValue("since", Timestamp.from(since));
        }
        if (!seesPrivate) {
            sql.append(" AND NOT \"private\"");
        }
        if (cursor != null) {
            sql.append(" AND id < :after");
            p.addValue("after", decode(cursor));
        }
        sql.append(" ORDER BY id DESC LIMIT :limit");
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        boolean more = ids.size() > limit;
        List<String> page = more ? ids.subList(0, limit) : ids;
        Map<String, AssessmentEntity> byId = new HashMap<>();
        for (AssessmentEntity a : assessments.findAllById(page)) {
            byId.put(a.getId(), a);
        }
        List<AssessmentEntity> items = new ArrayList<>();
        for (String id : page) {
            if (byId.containsKey(id)) {
                items.add(byId.get(id));
            }
        }
        return new Page<>(items, more ? encode(page.get(page.size() - 1)) : null);
    }

    /**
     * The newest assessment of a cadence (any when null) the caller may see.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_ASSESSMENT} when none exists yet
     */
    public AssessmentEntity latest(String cadence, boolean seesPrivate) {
        Page<AssessmentEntity> page = list(cadence, null, null, null, 1, seesPrivate);
        if (page.items().isEmpty()) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_ASSESSMENT, "No " + (cadence == null ? "" : cadence + " ")
                    + "assessment exists yet.");
        }
        return page.items().get(0);
    }

    /**
     * An assessment the caller may see.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_ASSESSMENT}
     */
    public AssessmentEntity get(String id, boolean seesPrivate) {
        return assessments.findById(id == null ? "" : id).filter(a -> seesPrivate || !a.isPrivateAssessment())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_ASSESSMENT, "No assessment '" + id
                        + "'."));
    }

    /**
     * Records feedback on an assessment into the 102 analyst feedback.
     *
     * @throws ValidationException 400 for an unknown rating or a long comment
     * @throws NotFoundException 404 {@code UNKNOWN_ASSESSMENT}
     */
    public Map<String, Object> feedback(String id, String rating, String comment, String by, boolean seesPrivate) {
        AssessmentEntity a = get(id, seesPrivate);
        if (rating == null || !RATINGS.contains(rating)) {
            throw ValidationException.forField("rating", "must be useful, not_useful or wrong");
        }
        if (comment != null && comment.length() > MAX_COMMENT) {
            throw ValidationException.forField("comment", "must be at most " + MAX_COMMENT + " characters");
        }
        Instant now = clock.instant();
        AnalystFeedbackEntity f = new AnalystFeedbackEntity(Ulids.next(now), FEEDBACK_TARGET, a.getId(), rating,
                by == null ? "anonymous" : by, now);
        f.setComment(comment);
        feedback.save(f);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", f.getId());
        out.put("assessmentId", a.getId());
        out.put("rating", rating);
        out.put("comment", comment);
        out.put("by", f.getBy());
        out.put("at", now.toString());
        return out;
    }

    /**
     * A cycle's runs newest first.
     *
     * @throws ValidationException 400 for a bad cursor
     */
    public Page<CycleRunEntity> runs(String cycleId, String cursor, int limit) {
        List<CycleRunEntity> rows = cursor == null ? runs.findByCycleIdOrderByIdDesc(cycleId, Limit.of(limit + 1))
                : runs.findByCycleIdAndIdLessThanOrderByIdDesc(cycleId, decode(cursor), Limit.of(limit + 1));
        boolean more = rows.size() > limit;
        List<CycleRunEntity> page = more ? rows.subList(0, limit) : rows;
        return new Page<>(page, more ? encode(page.get(page.size() - 1).getId()) : null);
    }

    /**
     * A run the caller may see (a run whose assessment is private is {@code 404} without private access).
     *
     * @throws NotFoundException 404 {@code UNKNOWN_CYCLE_RUN}
     */
    public CycleRunEntity run(String id, boolean seesPrivate) {
        CycleRunEntity run = runs.findById(id == null ? "" : id).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_CYCLE_RUN, "No cycle run '" + id + "'."));
        if (!seesPrivate && run.getAssessmentId() != null && assessments.findById(run.getAssessmentId())
                .map(AssessmentEntity::isPrivateAssessment).orElse(false)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_CYCLE_RUN, "No cycle run '" + id + "'.");
        }
        return run;
    }

    // ------------------------------------------------------------------------- views

    /** The {@code Assessment} view; {@code full} adds the markdown and HTML. */
    public static Map<String, Object> view(AssessmentEntity a, boolean full) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", a.getId());
        out.put("ref", "assessment:" + a.getId());
        out.put("cycleId", a.getCycleId());
        out.put("situationId", a.getSituationId());
        out.put("cadence", a.getCadence());
        out.put("periodStart", a.getPeriodStart().toString());
        out.put("periodEnd", a.getPeriodEnd().toString());
        out.put("previousAssessmentId", a.getPreviousAssessmentId());
        out.put("status", a.getStatus());
        out.put("bluf", a.getBluf());
        out.put("headline", a.getHeadline());
        out.put("sections", a.getSections());
        out.put("markdown", full ? a.getMarkdown() : null);
        out.put("html", full ? a.getHtml() : null);
        out.put("packetRef", a.getPacketRef());
        out.put("roles", a.getRoles());
        out.put("model", a.getModel());
        out.put("effort", a.getEffort() == null || !List.of("low", "medium", "high").contains(a.getEffort()) ? null
                : a.getEffort());
        out.put("manifestId", a.getManifestId());
        out.put("promptTemplates", a.getPromptTemplates());
        out.put("citations", a.getCitations());
        out.put("lintWarnings", a.getLintWarnings());
        out.put("footer", CycleVocabulary.FOOTER);
        out.put("private", a.isPrivateAssessment());
        out.put("createdAt", a.getCreatedAt().toString());
        out.put("coverage", null);
        out.put("social", null);
        return out;
    }

    /** The {@code CycleRun} view; {@code withPacket} adds the stored packet. */
    public Map<String, Object> runView(CycleRunEntity r, boolean withPacket) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("cycleId", r.getCycleId());
        out.put("situationId", r.getSituationId());
        out.put("cadence", r.getCadence());
        out.put("trigger", r.getTrigger());
        out.put("status", r.getStatus());
        out.put("periodStart", r.getPeriodStart().toString());
        out.put("periodEnd", r.getPeriodEnd().toString());
        out.put("previousRunId", r.getPreviousRunId());
        out.put("startedAt", r.getStartedAt() == null ? null : r.getStartedAt().toString());
        out.put("finishedAt", r.getFinishedAt() == null ? null : r.getFinishedAt().toString());
        out.put("packet", withPacket ? r.getPacket() : null);
        out.put("roles", r.getRoles());
        out.put("tokensUsed", r.getTokensUsed());
        out.put("assessmentId", r.getAssessmentId());
        out.put("maintenance", r.getMaintenanceId() == null ? null : maintenance.findById(r.getMaintenanceId())
                .map(MonthlyMaintenance::view).orElse(null));
        out.put("manifestId", r.getManifestId());
        out.put("warnings", r.getWarnings());
        out.put("error", r.getError());
        return out;
    }

    /** The {@code AnalysisCycle} view. */
    public static Map<String, Object> cycleView(AnalysisCycleEntity c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.getId());
        out.put("name", c.getName());
        out.put("cadence", c.getCadence());
        out.put("schedule", c.getSchedule());
        out.put("scope", c.getScope());
        out.put("analystProfileId", c.getAnalystProfileId());
        out.put("roles", c.getRoles());
        out.put("forecasterProviders", c.getForecasterProviders());
        out.put("samples", c.getSamples());
        out.put("maxQuestions", c.getMaxQuestions());
        out.put("maxTokensPerCycle", c.getMaxTokensPerCycle());
        out.put("channelIds", c.getChannelIds());
        out.put("notifyMinPriority", c.getNotifyMinPriority());
        out.put("enabled", c.isEnabled());
        out.put("builtIn", c.isBuiltIn());
        out.put("nextRunAt", c.getNextRunAt() == null ? null : c.getNextRunAt().toString());
        out.put("lastRunId", c.getLastRunId());
        out.put("createdAt", c.getCreatedAt().toString());
        out.put("updatedAt", c.getUpdatedAt().toString());
        return out;
    }

    // ------------------------------------------------------------------------- cursors

    static String encode(String id) {
        return SituationService.encodeCursor("a:" + id);
    }

    static String decode(String cursor) {
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith("a:") || text.length() < 3) {
                throw new IllegalArgumentException("cursor");
            }
            return text.substring(2);
        } catch (RuntimeException e) {
            throw ValidationException.forField("cursor", "is not a cursor of this list");
        }
    }
}
