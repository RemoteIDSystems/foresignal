package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystProfileRepository;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.briefing.BriefingHtmlRenderer;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Runs one analysis cycle run or on-demand situation assessment end to end (prompt 128):
 *
 * <ol>
 *   <li>the period: from the end of the previous successful run of the same cycle (or situation), else the cadence's
 *       natural lookback (1, 7 or 30 days);</li>
 *   <li>the deterministic packet ({@link CyclePacketReader}, {@link CyclePacketBuilder}), stored complete on the run;</li>
 *   <li>the configured LLM roles within the run's {@code max_tokens_per_cycle} ({@link CycleRoles}); without a
 *       provider they are {@code not-configured}, a budget refusal or failure marks only that role;</li>
 *   <li>the assessment, always ({@link AssessmentComposer}): {@code ok}, {@code partial}, {@code digest-only} or
 *       {@code failed}, linked to the previous one, rendered to markdown and HTML (029), with a 116 manifest;</li>
 *   <li>delivery: {@code assessment.published} on SSE, the 018 notification (inbox and channels) and the 027
 *       embedding; a failed run also emits {@code cycle.run_failed};</li>
 *   <li>for a monthly cycle, the {@link MonthlyMaintenance}.</li>
 * </ol>
 */
@Service
public class CycleRunner {

    private static final Logger log = LoggerFactory.getLogger(CycleRunner.class);

    /** The component name of cycle manifests. */
    public static final String MANIFEST_COMPONENT = "analysis-cycle";

    /** The version recorded on cycle manifests. */
    public static final String VERSION = "1.0.0";

    /** The run statuses a following run builds on. */
    static final List<String> SUCCESSFUL = List.of(CycleVocabulary.OK, CycleVocabulary.PARTIAL,
            CycleVocabulary.DIGEST_ONLY);

    /** The run statuses that hold a cycle or situation. */
    static final List<String> ACTIVE = List.of(CycleVocabulary.QUEUED, CycleVocabulary.RUNNING);

    private final AnalysisCycleRepository cycles;
    private final CycleRunRepository runs;
    private final AssessmentRepository assessments;
    private final CyclePacketReader reader;
    private final CycleStateReader stateReader;
    private final CycleLlm llm;
    private final CycleRoles roles;
    private final AnalystProfileRepository profiles;
    private final SituationService situations;
    private final AssessmentEvents events;
    private final AssessmentDelivery delivery;
    private final BriefingHtmlRenderer html;
    private final MonthlyMaintenance maintenance;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<HypothesisService> hypotheses;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the runner. */
    public CycleRunner(AnalysisCycleRepository cycles, CycleRunRepository runs, AssessmentRepository assessments,
            CyclePacketReader reader, CycleStateReader stateReader, CycleLlm llm, CycleRoles roles,
            AnalystProfileRepository profiles, SituationService situations, AssessmentEvents events,
            AssessmentDelivery delivery, BriefingHtmlRenderer html, MonthlyMaintenance maintenance,
            ObjectProvider<ManifestService> manifests, ObjectProvider<HypothesisService> hypotheses,
            NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.cycles = cycles;
        this.runs = runs;
        this.assessments = assessments;
        this.reader = reader;
        this.stateReader = stateReader;
        this.llm = llm;
        this.roles = roles;
        this.profiles = profiles;
        this.situations = situations;
        this.events = events;
        this.delivery = delivery;
        this.html = html;
        this.maintenance = maintenance;
        this.manifests = manifests;
        this.hypotheses = hypotheses;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- queueing

    /** Creates the queued run of {@code cycle} with its period. */
    public CycleRunEntity queue(AnalysisCycleEntity cycle, String trigger) {
        Instant now = clock.instant();
        CycleRunEntity previous = runs.findTop1ByCycleIdAndStatusInOrderByIdDesc(cycle.getId(), SUCCESSFUL)
                .orElse(null);
        Instant since = previous == null || !previous.getPeriodEnd().isBefore(now)
                ? now.minus(CycleVocabulary.lookback(cycle.getCadence())) : previous.getPeriodEnd();
        CycleRunEntity run = new CycleRunEntity(Ulids.next(now), cycle.getId(), null, cycle.getCadence(), trigger,
                since, now, now);
        run.setPreviousRunId(previous == null ? null : previous.getId());
        return runs.save(run);
    }

    /** Creates the queued on-demand run of a situation. */
    public CycleRunEntity queueSituation(String situationId, List<String> requestedRoles, String effort) {
        Instant now = clock.instant();
        CycleRunEntity previous = runs.findTop1BySituationIdAndStatusInOrderByIdDesc(situationId, SUCCESSFUL)
                .orElse(null);
        Instant since = previous == null || !previous.getPeriodEnd().isBefore(now)
                ? now.minus(CycleVocabulary.lookback(CycleVocabulary.ON_DEMAND)) : previous.getPeriodEnd();
        CycleRunEntity run = new CycleRunEntity(Ulids.next(now), null, situationId, CycleVocabulary.ON_DEMAND,
                "situation", since, now, now);
        run.setPreviousRunId(previous == null ? null : previous.getId());
        run.setRequestedRoles(requestedRoles);
        run.setRequestedEffort(effort);
        return runs.save(run);
    }

    // ------------------------------------------------------------------------- execution

    /** Executes a queued run to completion; never throws. */
    public CycleRunEntity execute(String runId) {
        CycleRunEntity run = runs.findById(runId).orElse(null);
        if (run == null || !CycleVocabulary.QUEUED.equals(run.getStatus())) {
            return run;
        }
        run.setStatus(CycleVocabulary.RUNNING);
        run.setStartedAt(clock.instant());
        runs.save(run);
        AnalysisCycleEntity cycle = run.getCycleId() == null ? null : cycles.findById(run.getCycleId()).orElse(null);
        String providerId = null;
        try {
            if (run.getCycleId() != null && cycle == null) {
                throw new IllegalStateException("the cycle was deleted before the run started");
            }
            providerId = runRoles(run, cycle);
        } catch (RuntimeException e) {
            log.warn("Cycle run {} failed: {}", run.getId(), e.toString(), e);
            fail(run, cycle, e.getMessage() == null ? e.toString() : e.getMessage());
        }
        if (cycle != null && CycleVocabulary.MONTHLY.equals(run.getCadence())
                && !CycleVocabulary.FAILED.equals(run.getStatus())) {
            runMaintenance(run, providerId);
        }
        if (cycle != null) {
            cycles.findById(cycle.getId()).ifPresent(c -> {
                c.setLastRunId(run.getId());
                cycles.save(c);
            });
        }
        return runs.findById(run.getId()).orElse(run);
    }

    /** Builds the packet, runs the roles and publishes the assessment; returns the provider used, or null. */
    private String runRoles(CycleRunEntity run, AnalysisCycleEntity cycle) {
        Instant now = clock.instant();
        String cadence = run.getCadence();
        SituationEntity situation = run.getSituationId() == null ? null : situations.get(run.getSituationId());
        CycleScope scope = cycle != null ? CycleScope.of(cycle.getScope())
                : CycleScope.focusedOn(run.getSituationId(), setsOf(run.getSituationId()));
        AnalystProfileEntity real = cycle == null || cycle.getAnalystProfileId() == null ? null
                : profiles.findById(cycle.getAnalystProfileId()).orElse(null);
        if (cycle != null && cycle.getAnalystProfileId() != null && real == null) {
            run.getWarnings().add("analyst profile " + cycle.getAnalystProfileId() + " no longer exists; defaults used");
        }
        AnalystProfileEntity profile = real != null ? real : defaults(now);
        List<String> requested = cycle != null ? cycle.getRoles()
                : run.getRequestedRoles() == null ? List.of() : run.getRequestedRoles();
        String wanted = run.getRequestedEffort() != null ? run.getRequestedEffort()
                : CycleVocabulary.defaultEffort(cadence);
        String effort = CycleVocabulary.effectiveEffort(wanted, real == null ? null : real.getEffort());
        long limit = cycle != null ? cycle.getMaxTokensPerCycle() : CycleVocabulary.defaultMaxTokens(cadence);
        TokenBudget budget = new TokenBudget(limit);
        AtomicBoolean seen = new AtomicBoolean();
        CycleCitations citations = llm.citations(List.of());
        String providerId = llm.providerFor(profile);
        RoleContext ctx = requested.isEmpty() ? null
                : llm.context(providerId, effort, profile, budget, citations, seen, run.getId()).orElse(null);

        AssessmentEntity previous = cycle != null ? assessments.findTop1ByCycleIdOrderByIdDesc(cycle.getId())
                .orElse(null) : assessments.findTop1BySituationIdOrderByIdDesc(run.getSituationId()).orElse(null);
        List<Map<String, Object>> previousJudgments = previous == null ? List.of() : previous.getBluf();
        CyclePacketBuilder.Inputs inputs = reader.read(run.getPeriodStart(), run.getPeriodEnd(),
                run.getPreviousRunId() == null, cadence, previousJudgments);
        CyclePacketBuilder.Packet packet = CyclePacketBuilder.build(inputs, scope, cadence,
                ctx != null && ctx.privateAllowed());
        packet.refs().forEach(citations::seed);
        if (ctx != null && ctx.privateAllowed() && packet.privateInput()) {
            seen.set(true);
        }
        run.setPacket(packet.stored());
        runs.save(run);
        reviewDue(run, packet);
        CycleState state = stateReader.read(scope, cadence, now);

        List<RoleOutcome> outcomes = new ArrayList<>();
        CycleRoles.AnalystResult analyst = null;
        CycleRoles.RedTeamResult redTeam = null;
        String profileKey = CycleRoles.profileKey(real);
        for (String role : CycleVocabulary.ROLES) {
            if (!requested.contains(role)) {
                continue;
            }
            if (ctx == null) {
                outcomes.add(RoleOutcome.of(role, CycleVocabulary.NOT_CONFIGURED, providerId == null
                        ? "no chat provider is configured" : "chat provider " + providerId + " is not configured"));
                continue;
            }
            try {
                switch (role) {
                    case CycleVocabulary.ANALYST -> {
                        analyst = roles.analyst(ctx, packet.llm(), previousJudgments);
                        outcomes.add(analyst.outcome());
                    }
                    case CycleVocabulary.RED_TEAM -> {
                        redTeam = roles.redTeam(ctx, redTeamSets(state, packet, ctx.privateAllowed(), seen),
                                profileKey);
                        outcomes.add(redTeam.outcome());
                    }
                    case CycleVocabulary.FORECASTER -> outcomes.add(roles.forecaster(ctx,
                            cycle == null ? List.of() : cycle.getForecasterProviders(), scope, cadence,
                            cycle == null ? CycleVocabulary.defaultMaxQuestions(cadence) : cycle.getMaxQuestions(),
                            cycle == null ? CycleVocabulary.DEFAULT_SAMPLES : cycle.getSamples(), profileKey));
                    default -> {
                        if (real == null) {
                            outcomes.add(RoleOutcome.of(role, CycleVocabulary.NOT_CONFIGURED, "proposals belong to a"
                                    + " 102 analyst profile; set analystProfileId on the cycle"));
                        } else {
                            outcomes.add(roles.proposer(ctx, packet.llm(), real));
                        }
                    }
                }
            } catch (RoleSkippedException e) {
                outcomes.add(withProvider(RoleOutcome.of(role, CycleVocabulary.SKIPPED, e.getMessage()), ctx, role));
            } catch (LlmBudgetExceededException e) {
                outcomes.add(withProvider(RoleOutcome.of(role, CycleVocabulary.SKIPPED, "LLM budget exhausted for"
                        + " provider " + e.providerId()), ctx, role));
            } catch (RuntimeException e) {
                log.warn("Role {} of cycle run {} failed: {}", role, run.getId(), e.toString());
                outcomes.add(withProvider(RoleOutcome.of(role, CycleVocabulary.FAILED, e.getMessage() == null
                        ? e.toString() : e.getMessage()), ctx, role));
            }
        }

        long ok = outcomes.stream().filter(o -> CycleVocabulary.OK.equals(o.status())).count();
        String status = requested.isEmpty() || ok == 0 ? CycleVocabulary.DIGEST_ONLY
                : ok == requested.size() ? CycleVocabulary.OK : CycleVocabulary.PARTIAL;
        String title = cycle != null ? cycle.getName() + ", " + run.getPeriodEnd().toString().substring(0, 10)
                : "Situation assessment: " + situation.getTitle();
        AssessmentComposer.Previous prev = previous == null ? null : new AssessmentComposer.Previous(previous.getId(),
                previous.getCreatedAt(), previous.getBluf(), snapshotOf(previous));
        AssessmentComposer.Composed composed = AssessmentComposer.compose(new AssessmentComposer.Inputs(title, cadence,
                run.getPeriodStart(), run.getPeriodEnd(), situation == null ? null : situation.getTitle(), packet,
                state, analyst, redTeam, outcomes, prev));
        boolean privateAssessment = packet.privateInput() || state.anyPrivate() || seen.get();
        AssessmentEntity a = new AssessmentEntity(Ulids.next(clock.instant()), cadence, run.getPeriodStart(),
                run.getPeriodEnd(), clock.instant());
        a.setCycleId(run.getCycleId());
        a.setSituationId(run.getSituationId());
        a.setPreviousAssessmentId(previous == null ? null : previous.getId());
        a.setStatus(status);
        a.setBluf(composed.bluf());
        a.setHeadline(composed.headline() == null ? null : composed.headline().length() > 1000
                ? composed.headline().substring(0, 1000) : composed.headline());
        a.setSections(composed.sections());
        a.setMarkdown(composed.markdown());
        a.setHtml(html.page(title, composed.markdown(), CycleVocabulary.FOOTER));
        a.setPacketRef(run.getId());
        Map<String, Object> roleMap = new LinkedHashMap<>();
        List<Map<String, Object>> templates = new ArrayList<>();
        String model = null;
        for (RoleOutcome o : outcomes) {
            roleMap.put(o.role(), o.toMap());
            if (model == null && o.model() != null) {
                model = o.model();
            }
            if (o.templateId() != null && !CycleVocabulary.NOT_CONFIGURED.equals(o.status())) {
                templates.add(Map.of("id", o.templateId(), "version", PromptTemplates.V1));
            }
        }
        a.setRoles(roleMap);
        a.setModel(model);
        a.setEffort(ctx == null ? null : effort);
        a.setPromptTemplates(templates);
        a.setCitations(composed.citations());
        a.setLintWarnings(composed.lintWarnings());
        a.setPrivateAssessment(privateAssessment);
        a.setManifestId(manifest(run, cycle, packet, outcomes, model, effort, providerId, templates));
        assessments.save(a);

        List<Map<String, Object>> roleList = new ArrayList<>();
        outcomes.forEach(o -> roleList.add(o.toMap()));
        run.setRoles(roleList);
        run.setTokensUsed(budget.spent());
        run.setAssessmentId(a.getId());
        run.setManifestId(a.getManifestId());
        run.setStatus(status);
        run.setFinishedAt(clock.instant());
        runs.save(run);
        events.published(a);
        delivery.deliver(a, cycle == null ? List.of() : cycle.getChannelIds(),
                cycle == null ? "normal" : cycle.getNotifyMinPriority());
        return ctx == null ? providerId : ctx.providerId();
    }

    private static RoleOutcome withProvider(RoleOutcome outcome, RoleContext ctx, String role) {
        String template = switch (role) {
            case CycleVocabulary.ANALYST -> PromptTemplates.CYCLE_ANALYST;
            case CycleVocabulary.RED_TEAM -> PromptTemplates.CYCLE_RED_TEAM;
            case CycleVocabulary.FORECASTER -> PromptTemplates.CYCLE_FORECASTER;
            default -> PromptTemplates.CYCLE_PROPOSER;
        };
        outcome.using(ctx.providerId(), ctx.effort(), template);
        return outcome;
    }

    /**
     * Records the routine review (125) of every in-scope set due for review whose cadence this cycle covers (daily
     * sets daily, plus weekly sets weekly, plus monthly sets monthly; any due set of an on-demand situation): a
     * revision with cause {@code review} that changes no probability and advances {@code next_review_at}. A set past
     * its horizon without a ledger question is archived by that review. Failures are warnings.
     */
    void reviewDue(CycleRunEntity run, CyclePacketBuilder.Packet packet) {
        HypothesisService service = hypotheses.getIfAvailable();
        if (service == null) {
            return;
        }
        List<String> reviewed = new ArrayList<>();
        for (CyclePacketItem item : packet.section("hypotheses.dueForReview")) {
            String setId = item.setId();
            if (setId == null) {
                continue;
            }
            String rationale = "Routine " + run.getCadence() + " review by analysis cycle run " + run.getId()
                    + ": the period's evidence was reviewed and no probability was changed by the review.";
            try {
                service.review(setId, new HypothesisInputs.ReviewDraft("review", rationale, null, Map.of(), List.of(),
                        List.of(), false, List.of("cycle-run:" + run.getId()), null), "cycle:" + run.getId(), true);
                reviewed.add(setId);
            } catch (RuntimeException e) {
                run.getWarnings().add("routine review of set " + setId + " failed: " + e.getMessage());
            }
        }
        if (!reviewed.isEmpty()) {
            run.getWarnings().add("routine reviews recorded for " + reviewed.size() + " hypothesis set"
                    + (reviewed.size() == 1 ? "" : "s") + ": " + reviewed);
            runs.save(run);
        }
    }

    /** The hypothesis sets the red team challenges: chosen from the state by movement and stakes, with assumptions. */
    @SuppressWarnings("unchecked")
    List<CycleRoles.SetBrief> redTeamSets(CycleState state, CyclePacketBuilder.Packet packet, boolean privateAllowed,
            AtomicBoolean seen) {
        Map<String, Double> movement = new HashMap<>();
        for (CyclePacketItem i : packet.section("hypotheses.revisions")) {
            if (i.setId() != null) {
                movement.merge(i.setId(), i.score(), Math::max);
            }
        }
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        List<RedTeamSelection.Candidate> candidates = new ArrayList<>();
        for (Map<String, Object> set : state.sets()) {
            if (Boolean.TRUE.equals(set.get("private")) && !privateAllowed) {
                continue;
            }
            String id = String.valueOf(set.get("setId"));
            byId.put(id, set);
            candidates.add(new RedTeamSelection.Candidate(id, movement.getOrDefault(id, 0.0),
                    set.get("stakes") instanceof Number n ? n.doubleValue() : 1.0));
        }
        List<RedTeamSelection.Candidate> chosen = RedTeamSelection.choose(candidates, RedTeamSelection.MAX_SETS);
        if (chosen.isEmpty()) {
            return List.of();
        }
        Map<String, List<Map<String, Object>>> assumptions = new HashMap<>();
        jdbc.query("SELECT id, set_id, text, status, linchpin FROM key_assumption WHERE set_id IN (:ids) ORDER BY id",
                new MapSqlParameterSource("ids", chosen.stream().map(RedTeamSelection.Candidate::setId).toList()),
                rs -> {
                    Map<String, Object> a = new LinkedHashMap<>();
                    a.put("id", rs.getString("id"));
                    a.put("text", rs.getString("text"));
                    a.put("status", rs.getString("status"));
                    a.put("linchpin", rs.getBoolean("linchpin"));
                    assumptions.computeIfAbsent(rs.getString("set_id"), k -> new ArrayList<>()).add(a);
                });
        List<CycleRoles.SetBrief> out = new ArrayList<>();
        for (RedTeamSelection.Candidate c : chosen) {
            Map<String, Object> set = byId.get(c.setId());
            if (Boolean.TRUE.equals(set.get("private"))) {
                seen.set(true);
            }
            out.add(new CycleRoles.SetBrief(c.setId(), String.valueOf(set.get("question")),
                    set.get("questionId") == null ? null : String.valueOf(set.get("questionId")),
                    (List<Map<String, Object>>) set.get("hypotheses"), assumptions.getOrDefault(c.setId(), List.of()),
                    c.stakes(), c.movement()));
        }
        return out;
    }

    /** The ids of the active hypothesis sets of a situation. */
    List<String> setsOf(String situationId) {
        return jdbc.queryForList("SELECT id FROM hypothesis_set WHERE situation_id = :id AND status = 'active'",
                new MapSqlParameterSource("id", situationId), String.class);
    }

    /** The snapshot an earlier assessment stored in its {@code what-changed} section, or null. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> snapshotOf(AssessmentEntity a) {
        for (Map<String, Object> s : a.getSections()) {
            if ("what-changed".equals(s.get("key")) && s.get("data") instanceof Map<?, ?> data
                    && data.get("snapshot") instanceof Map<?, ?> snapshot) {
                return (Map<String, Object>) snapshot;
            }
        }
        return null;
    }

    /** The transient 102 limits used when a cycle names no profile. */
    static AnalystProfileEntity defaults(Instant now) {
        AnalystProfileEntity p = new AnalystProfileEntity("cycle-defaults", "Analysis cycle defaults", now);
        p.setEffort("high");
        return p;
    }

    private String manifest(CycleRunEntity run, AnalysisCycleEntity cycle, CyclePacketBuilder.Packet packet,
            List<RoleOutcome> outcomes, String model, String effort, String providerId,
            List<Map<String, Object>> templates) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("cycleId", run.getCycleId());
        config.put("situationId", run.getSituationId());
        config.put("cadence", run.getCadence());
        config.put("periodStart", run.getPeriodStart().toString());
        config.put("periodEnd", run.getPeriodEnd().toString());
        config.put("packetVersion", CyclePacketBuilder.VERSION);
        if (cycle != null) {
            config.put("scope", cycle.getScope());
            config.put("roles", cycle.getRoles());
            config.put("samples", cycle.getSamples());
            config.put("maxQuestions", cycle.getMaxQuestions());
            config.put("maxTokensPerCycle", cycle.getMaxTokensPerCycle());
            config.put("analystProfileId", cycle.getAnalystProfileId());
        } else {
            config.put("roles", run.getRequestedRoles());
        }
        Map<String, Object> modelMap = null;
        boolean ran = outcomes.stream().anyMatch(o -> o.model() != null);
        if (ran) {
            modelMap = new LinkedHashMap<>();
            modelMap.put("provider", providerId == null ? "unknown" : providerId);
            modelMap.put("model", model == null ? "unknown" : model);
            modelMap.put("effort", effort);
            modelMap.put("promptHash", ai.foresignal.manifest.CanonicalJson.sha256(String.valueOf(templates)));
            List<Map<String, Object>> refs = new ArrayList<>();
            for (Map<String, Object> t : templates) {
                refs.add(PromptTemplates.reference(String.valueOf(t.get("id")), String.valueOf(t.get("version"))));
            }
            modelMap.put("templates", refs);
        }
        List<String> refs = packet.refs().size() > ManifestService.MAX_EXPLICIT_REFS
                ? packet.refs().subList(0, ManifestService.MAX_EXPLICIT_REFS) : packet.refs();
        try {
            return service.record(ManifestRequest.of(MANIFEST_COMPONENT, VERSION).config(config).inputs(refs)
                    .model(modelMap));
        } catch (RuntimeException e) {
            log.warn("Could not record the cycle manifest: {}", e.toString());
            return null;
        }
    }

    /** Marks a run failed, writes a failed assessment and emits {@code cycle.run_failed}. */
    private void fail(CycleRunEntity run, AnalysisCycleEntity cycle, String reason) {
        try {
            Instant now = clock.instant();
            AssessmentEntity a = new AssessmentEntity(Ulids.next(now), run.getCadence(), run.getPeriodStart(),
                    run.getPeriodEnd(), now);
            a.setCycleId(run.getCycleId());
            a.setSituationId(run.getSituationId());
            a.setStatus(CycleVocabulary.FAILED);
            String text = "The analysis run failed before its assessment could be built: " + reason;
            Map<String, Object> judgment = new LinkedHashMap<>();
            judgment.put("id", "dj-1");
            judgment.put("text", text);
            judgment.put("citations", List.of());
            judgment.put("textOrigin", "deterministic");
            a.setBluf(List.of(judgment));
            a.setHeadline(text.length() > 1000 ? text.substring(0, 1000) : text);
            Map<String, Object> section = new LinkedHashMap<>();
            section.put("key", "sources");
            section.put("title", "Sources, coverage and caveats");
            section.put("markdown", ai.foresignal.briefing.DigestRenderer.escape(text));
            section.put("textOrigin", "deterministic");
            section.put("data", null);
            a.setSections(List.of(section));
            String md = "# Analysis run failed\n\n" + ai.foresignal.briefing.DigestRenderer.escape(text) + "\n\n---\n\n*"
                    + CycleVocabulary.FOOTER + "*\n";
            a.setMarkdown(md);
            a.setHtml(html.page("Analysis run failed", md, CycleVocabulary.FOOTER));
            a.setPacketRef(run.getId());
            assessments.save(a);
            run.setAssessmentId(a.getId());
        } catch (RuntimeException e) {
            log.warn("Could not write the failed assessment of run {}: {}", run.getId(), e.toString());
        }
        run.setStatus(CycleVocabulary.FAILED);
        run.setError(reason);
        run.setFinishedAt(clock.instant());
        runs.save(run);
        try {
            events.runFailed(run, reason, false);
        } catch (RuntimeException e) {
            log.warn("Could not emit cycle.run_failed for {}: {}", run.getId(), e.toString());
        }
    }

    private void runMaintenance(CycleRunEntity run, String providerId) {
        try {
            MaintenanceRunEntity m = maintenance.start("monthly-cycle", run.getId());
            run.setMaintenanceId(m.getId());
            runs.save(run);
            maintenance.execute(m.getId(), providerId);
        } catch (ConflictException e) {
            CycleRunEntity fresh = runs.findById(run.getId()).orElse(run);
            fresh.getWarnings().add("monthly maintenance not started: " + e.getMessage());
            runs.save(fresh);
        } catch (RuntimeException e) {
            log.warn("Monthly maintenance of run {} failed: {}", run.getId(), e.toString());
        }
    }
}
