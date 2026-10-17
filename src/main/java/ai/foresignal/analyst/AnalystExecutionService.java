package ai.foresignal.analyst;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Confirms, rejects and executes analyst proposals (prompt 102). Auto-execution runs only when the
 * profile allows actions, the type is allowed, the owner token is still an active agent-scope token
 * and the daily cap is not reached. The analyst never deletes or disables anything; watches are
 * created enabled and tagged {@code analyst} and {@code origin-analyst}, events with
 * {@code origin=analyst} (status {@code emerging}, members' identities only, the score computed by
 * {@code EventScorer}), investigation items only into open investigations, and {@code annotate_event}
 * only appends labeled speculation.
 *
 * <p>Each proposal executes in <strong>its own transaction</strong> ({@code REQUIRES_NEW}): a failing
 * proposal rolls back only its own effects and is marked {@code failed}; the others and the caller's
 * transaction are unaffected. Every execution, successful or not, is written to the 023 audit log with
 * the profile and run ids. A proposal reads private records only when the profile owner's token may see
 * private sources.
 */
@Service
public class AnalystExecutionService {

    private static final Logger log = LoggerFactory.getLogger(AnalystExecutionService.class);

    /** The audit {@code targetType} of a proposal execution. */
    public static final String AUDIT_TARGET = "analyst_proposal";

    /**
     * The tags every analyst-created watch carries (prompt 102 §2.5 and §4). Prompt 102 names the origin tag
     * {@code origin:analyst}, but a rule tag must match {@code ^[a-z0-9][a-z0-9_-]{0,31}$} (017, docs/OpenAPI.json
     * {@code TriggerRuleInput.tags}), which has no colon, so the rule validator refused every analyst watch; the
     * origin tag is {@code origin-analyst} (checkpoint 124).
     */
    public static final List<String> WATCH_TAGS = List.of("analyst", "origin-analyst");

    private final AnalystProposalRepository proposals;
    private final AnalystProfileRepository profiles;
    private final TriggerRuleService rules;
    private final IntelEventService intelEvents;
    private final IntelligenceEventRepository events;
    private final InvestigationService investigations;
    private final TokenService tokens;
    private final TransactionTemplate isolated;
    private final ObjectProvider<AuditService> audit;
    private final JsonMapper json;
    private final Clock clock;
    private ObjectProvider<AnalystProposalEvents> proposalEvents;

    /** Creates the service. */
    public AnalystExecutionService(AnalystProposalRepository proposals, AnalystProfileRepository profiles,
            TriggerRuleService rules, IntelEventService intelEvents, IntelligenceEventRepository events,
            InvestigationService investigations, TokenService tokens, TransactionTemplate transactions,
            ObjectProvider<AuditService> audit, JsonMapper json, Clock clock) {
        this.proposals = proposals;
        this.profiles = profiles;
        this.rules = rules;
        this.intelEvents = intelEvents;
        this.events = events;
        this.investigations = investigations;
        this.tokens = tokens;
        this.isolated = new TransactionTemplate(transactions.getTransactionManager());
        this.isolated.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.audit = audit;
        this.json = json;
        this.clock = clock;
    }

    /** Injects the {@code analyst.proposal} publisher (checkpoint 124); optional in slices without notifications. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setProposalEvents(ObjectProvider<AnalystProposalEvents> proposalEvents) {
        this.proposalEvents = proposalEvents;
    }

    /** Announces a proposal change on the {@code analyst} topic when the publisher is present. */
    private void announce(AnalystProposalEntity proposal, String change) {
        AnalystProposalEvents events = proposalEvents == null ? null : proposalEvents.getIfAvailable();
        if (events != null) {
            events.publish(proposal, change);
        }
    }

    /** A proposal, or 404. */
    public AnalystProposalEntity get(String id) {
        return proposals.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PROPOSAL,
                "No such proposal: " + id));
    }

    /**
     * The proposal {@code id} as a caller sees it: a private proposal (one from a run that saw private data) is
     * {@code 404 UNKNOWN_PROPOSAL} to a caller without private access, as on the list (EGR-1, checkpoint 124).
     */
    public AnalystProposalEntity visible(String id, boolean privateAccess) {
        AnalystProposalEntity proposal = get(id);
        if (proposal.isPrivateProposal() && !privateAccess) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PROPOSAL, "No such proposal: " + id);
        }
        return proposal;
    }

    /** Confirms a proposal the caller may see (see {@link #visible}); otherwise as {@link #confirm(String, String)}. */
    @Transactional
    public AnalystProposalEntity confirm(String id, String actor, boolean privateAccess) {
        visible(id, privateAccess);
        return confirm(id, actor);
    }

    /** Rejects a proposal the caller may see (see {@link #visible}); otherwise as {@link #reject(String, String)}. */
    @Transactional
    public AnalystProposalEntity reject(String id, String actor, boolean privateAccess) {
        visible(id, privateAccess);
        return reject(id, actor);
    }

    /** Confirms and executes a pending proposal (the execution runs in its own transaction). */
    @Transactional
    public AnalystProposalEntity confirm(String id, String actor) {
        AnalystProposalEntity proposal = get(id);
        if (!"pending".equals(proposal.getStatus())) {
            throw new ConflictException(ErrorCodes.PROPOSAL_NOT_PENDING, "Proposal " + id + " is not pending.");
        }
        AnalystProfileEntity profile = profiles.findById(proposal.getProfileId()).orElse(null);
        if (profile != null && !profile.isAllowActions()) {
            throw new ConflictException(ErrorCodes.PROPOSAL_NOT_PENDING,
                    "The profile does not allow actions; use the payload as a suggestion.");
        }
        execute(proposal, profile, false, actor);
        return proposals.save(proposal);
    }

    /** Rejects a pending proposal. */
    @Transactional
    public AnalystProposalEntity reject(String id, String actor) {
        AnalystProposalEntity proposal = get(id);
        if (!"pending".equals(proposal.getStatus())) {
            throw new ConflictException(ErrorCodes.PROPOSAL_NOT_PENDING, "Proposal " + id + " is not pending.");
        }
        proposal.setStatus("rejected");
        proposal.setDecidedBy(actor);
        proposal.setDecidedAt(clock.instant());
        AnalystProposalEntity saved = proposals.save(proposal);
        announce(saved, AnalystProposalEvents.REJECTED);
        return saved;
    }

    /**
     * Executes the proposals of a just-finished run when the profile allows it. Not transactional itself:
     * each proposal runs in its own transaction and its outcome is saved on its own.
     */
    public int autoExecute(AnalystProfileEntity profile, AnalystRunEntity run, List<AnalystProposalEntity> runProposals,
            AuthenticatedToken caller) {
        if (!profile.isAllowActions()) {
            return 0;
        }
        boolean ownerAgent = ownerHasAgentScope(profile, caller);
        if (!ownerAgent) {
            return 0;
        }
        Instant dayStart = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        long already = proposals.countByProfileIdAndAutoExecutedTrueAndCreatedAtGreaterThanEqual(profile.getId(),
                dayStart);
        int executed = 0;
        for (AnalystProposalEntity proposal : runProposals) {
            if (!"pending".equals(proposal.getStatus()) || !profile.getAllowedActions().contains(proposal.getType())) {
                continue;
            }
            if (already + executed >= profile.getMaxAutoActionsPerDay()) {
                break;
            }
            execute(proposal, profile, true, "analyst:" + profile.getId());
            proposals.save(proposal);
            if ("executed".equals(proposal.getStatus())) {
                executed++;
            }
        }
        run.setAutoExecuted(executed);
        return executed;
    }

    private boolean ownerHasAgentScope(AnalystProfileEntity profile, AuthenticatedToken caller) {
        if (caller != null && caller.scope() != null && caller.scope().includes(Scope.AGENT)) {
            return true;
        }
        return owner(profile).map(t -> t.scope() != null && t.scope().includes(Scope.AGENT)).orElse(false);
    }

    /** The profile owner's current token (unrevoked, unexpired), or empty. */
    private Optional<AuthenticatedToken> owner(AnalystProfileEntity profile) {
        if (profile == null || profile.getOwnerTokenId() == null) {
            return Optional.empty();
        }
        try {
            return tokens.current(java.util.UUID.fromString(profile.getOwnerTokenId()));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** True when the profile owner's token may see private sources (039), so the proposal may pin private records. */
    private boolean ownerSeesPrivate(AnalystProfileEntity profile) {
        return owner(profile).map(t -> SourceVisibility.canSeePrivate(t.scope(), t.privateAccess())).orElse(false);
    }

    /** Executes one proposal in its own transaction; records the outcome on the entity and in the audit log. */
    private void execute(AnalystProposalEntity proposal, AnalystProfileEntity profile, boolean auto, String actor) {
        boolean privateAccess = ownerSeesPrivate(profile);
        try {
            String ref = isolated.execute(status -> switch (proposal.getType()) {
                case AnalystActions.PROPOSE_WATCH -> createWatch(proposal);
                case AnalystActions.PROPOSE_INTEL_EVENT -> createEvent(proposal, privateAccess);
                case AnalystActions.PROPOSE_INVESTIGATION_ITEM -> addInvestigationItem(proposal, privateAccess);
                case AnalystActions.ANNOTATE_EVENT -> annotate(proposal, privateAccess);
                default -> throw new IllegalStateException("unknown proposal type " + proposal.getType());
            });
            proposal.setStatus("executed");
            proposal.setExecutedRef(ref);
            proposal.setAutoExecuted(auto);
            proposal.setDecidedBy(actor);
            proposal.setDecidedAt(clock.instant());
        } catch (RuntimeException e) {
            log.warn("Proposal {} failed: {}", proposal.getId(), e.toString());
            proposal.setStatus("failed");
            proposal.setReason(failureReason(e));
            proposal.setDecidedBy(actor);
            proposal.setDecidedAt(clock.instant());
        }
        audit(proposal, profile, auto, actor);
        announce(proposal, "executed".equals(proposal.getStatus()) ? AnalystProposalEvents.EXECUTED
                : AnalystProposalEvents.FAILED);
    }

    /** The failure reason: the message, with each field error of a validation failure (so the reviewer sees why). */
    static String failureReason(RuntimeException e) {
        String reason = e.getMessage();
        if (e instanceof ai.foresignal.api.error.ValidationException validation && validation.getErrors() != null
                && !validation.getErrors().isEmpty()) {
            List<String> fields = new ArrayList<>();
            for (ai.foresignal.api.error.FieldError error : validation.getErrors()) {
                fields.add(error.field() + ": " + error.message());
            }
            reason = reason + " (" + String.join("; ", fields) + ")";
        }
        return reason;
    }

    private void audit(AnalystProposalEntity proposal, AnalystProfileEntity profile, boolean auto, String actor) {
        AuditService service = audit == null ? null : audit.getIfAvailable();
        if (service == null) {
            return;
        }
        try {
            Optional<AuthenticatedToken> owner = owner(profile);
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("profileId", proposal.getProfileId());
            detail.put("runId", proposal.getRunId());
            detail.put("type", proposal.getType());
            detail.put("status", proposal.getStatus());
            detail.put("executedRef", proposal.getExecutedRef());
            detail.put("auto", auto);
            detail.put("actor", actor);
            service.record(new AuditRecord(clock.instant(), auto ? AuditRecord.SYSTEM : AuditRecord.TOKEN,
                    owner.map(AuthenticatedToken::id).orElse(null), owner.map(AuthenticatedToken::name).orElse(null),
                    owner.map(AuthenticatedToken::principalId).orElse(null),
                    owner.map(t -> t.scope() == null ? null : t.scope().wireValue()).orElse(null), null, null,
                    auto ? null : "confirmAnalystProposal", null, AUDIT_TARGET, proposal.getId(), null, detail));
        } catch (RuntimeException e) {
            log.warn("Could not audit proposal {}: {}", proposal.getId(), e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private String createWatch(AnalystProposalEntity proposal) {
        Object rule = proposal.getPayload().get("rule");
        Map<String, Object> ruleMap = rule instanceof Map<?, ?> map ? (Map<String, Object>) map : proposal.getPayload();
        Map<String, Object> body = new LinkedHashMap<>(ruleMap);
        body.remove("citations");
        List<String> tags = new ArrayList<>(WATCH_TAGS);
        if (body.get("tags") instanceof List<?> existing) {
            for (Object tag : existing) {
                String value = String.valueOf(tag);
                if (!tags.contains(value)) {
                    tags.add(value);
                }
            }
        }
        body.put("tags", tags);
        body.put("enabled", true);
        return rules.create(json.valueToTree(body)).rule().getId().toString();
    }

    /** An analyst event: the proposal's type, title and member identities only; the server scores it. */
    private String createEvent(AnalystProposalEntity proposal, boolean privateAccess) {
        Map<String, Object> body = AnalystOutputValidator.scoreFree(proposal.getPayload());
        body.put("origin", "analyst");
        IntelligenceEventEntity event = intelEvents.create(json.valueToTree(body), "analyst", "analyst", privateAccess);
        return event.getId();
    }

    private String addInvestigationItem(AnalystProposalEntity proposal, boolean privateAccess) {
        Map<String, Object> payload = proposal.getPayload();
        String investigationId = String.valueOf(payload.get("investigationId"));
        InvestigationEntity investigation = investigations.get(investigationId);
        if (!"open".equals(investigation.getStatus())) {
            throw new IllegalStateException("investigation " + investigationId + " is not open");
        }
        Map<String, Object> item = new LinkedHashMap<>();
        for (String key : List.of("itemType", "ref", "seq", "note")) {
            if (payload.get(key) != null) {
                item.put(key, payload.get(key));
            }
        }
        var added = investigations.addItem(investigationId, json.valueToTree(item), "analyst", privateAccess);
        return String.valueOf(added.getId());
    }

    private String annotate(AnalystProposalEntity proposal, boolean privateAccess) {
        String eventId = String.valueOf(proposal.getPayload().get("eventId"));
        IntelligenceEventEntity event = intelEvents.visible(eventId, privateAccess);
        String note = String.valueOf(proposal.getPayload().get("note"));
        String existing = event.getNarrative() == null ? "" : event.getNarrative() + "\n\n";
        event.setNarrative(existing + "[speculation] " + note);
        events.save(event);
        return eventId;
    }

    /** Expires pending proposals past their deadline. */
    @Transactional
    public int expire() {
        List<AnalystProposalEntity> stale = proposals.findByStatusAndExpiresAtBefore("pending", clock.instant(),
                org.springframework.data.domain.Limit.of(500));
        for (AnalystProposalEntity proposal : stale) {
            proposal.setStatus("expired");
            proposal.setReason("expired after 7 days");
        }
        proposals.saveAll(stale);
        for (AnalystProposalEntity proposal : stale) {
            announce(proposal, AnalystProposalEvents.EXPIRED);
        }
        return stale.size();
    }
}
