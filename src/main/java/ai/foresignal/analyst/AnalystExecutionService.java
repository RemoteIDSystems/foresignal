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
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Confirms, rejects and executes analyst proposals (prompt 102). Auto-execution runs only when the
 * profile allows actions, the type is allowed, the owner token is still an active agent-scope token
 * and the daily cap is not reached. The analyst never deletes or disables anything; watches are
 * created enabled and tagged {@code analyst}, events with {@code origin=analyst}, investigation items
 * only into open investigations, and {@code annotate_event} only appends labelled speculation.
 */
@Service
public class AnalystExecutionService {

    private static final Logger log = LoggerFactory.getLogger(AnalystExecutionService.class);

    private final AnalystProposalRepository proposals;
    private final AnalystProfileRepository profiles;
    private final TriggerRuleService rules;
    private final IntelEventService intelEvents;
    private final IntelligenceEventRepository events;
    private final InvestigationService investigations;
    private final TokenService tokens;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public AnalystExecutionService(AnalystProposalRepository proposals, AnalystProfileRepository profiles,
            TriggerRuleService rules, IntelEventService intelEvents, IntelligenceEventRepository events,
            InvestigationService investigations, TokenService tokens, JsonMapper json, Clock clock) {
        this.proposals = proposals;
        this.profiles = profiles;
        this.rules = rules;
        this.intelEvents = intelEvents;
        this.events = events;
        this.investigations = investigations;
        this.tokens = tokens;
        this.json = json;
        this.clock = clock;
    }

    /** A proposal, or 404. */
    public AnalystProposalEntity get(String id) {
        return proposals.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PROPOSAL,
                "No such proposal: " + id));
    }

    /** Confirms and executes a pending proposal. */
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
        return proposals.save(proposal);
    }

    /** Executes the proposals of a just-finished run when the profile allows it. */
    @Transactional
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
            executed++;
        }
        run.setAutoExecuted(executed);
        return executed;
    }

    private boolean ownerHasAgentScope(AnalystProfileEntity profile, AuthenticatedToken caller) {
        if (caller != null && caller.scope() != null && caller.scope().includes(Scope.AGENT)) {
            return true;
        }
        if (profile.getOwnerTokenId() == null) {
            return false;
        }
        try {
            Optional<AuthenticatedToken> token = tokens.current(java.util.UUID.fromString(profile.getOwnerTokenId()));
            return token.isPresent() && token.get().scope() != null && token.get().scope().includes(Scope.AGENT);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void execute(AnalystProposalEntity proposal, AnalystProfileEntity profile, boolean auto, String actor) {
        try {
            String ref = switch (proposal.getType()) {
                case AnalystActions.PROPOSE_WATCH -> createWatch(proposal);
                case AnalystActions.PROPOSE_INTEL_EVENT -> createEvent(proposal);
                case AnalystActions.PROPOSE_INVESTIGATION_ITEM -> addInvestigationItem(proposal);
                case AnalystActions.ANNOTATE_EVENT -> annotate(proposal);
                default -> throw new IllegalStateException("unknown proposal type " + proposal.getType());
            };
            proposal.setStatus("executed");
            proposal.setExecutedRef(ref);
            proposal.setAutoExecuted(auto);
            proposal.setDecidedBy(actor);
            proposal.setDecidedAt(clock.instant());
        } catch (RuntimeException e) {
            log.warn("Proposal {} failed: {}", proposal.getId(), e.toString());
            proposal.setStatus("failed");
            proposal.setReason(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String createWatch(AnalystProposalEntity proposal) {
        Object rule = proposal.getPayload().get("rule");
        Map<String, Object> ruleMap = rule instanceof Map<?, ?> map ? (Map<String, Object>) map : proposal.getPayload();
        Map<String, Object> body = new java.util.LinkedHashMap<>(ruleMap);
        List<String> tags = new ArrayList<>(List.of("analyst"));
        if (body.get("tags") instanceof List<?> existing) {
            for (Object tag : existing) {
                tags.add(String.valueOf(tag));
            }
        }
        body.put("tags", tags);
        body.putIfAbsent("enabled", true);
        return rules.create(json.valueToTree(body)).rule().getId().toString();
    }

    private String createEvent(AnalystProposalEntity proposal) {
        Map<String, Object> body = new java.util.LinkedHashMap<>(proposal.getPayload());
        body.put("origin", "analyst");
        IntelligenceEventEntity event = intelEvents.create(json.valueToTree(body), "analyst", "analyst");
        return event.getId();
    }

    private String addInvestigationItem(AnalystProposalEntity proposal) {
        Map<String, Object> payload = proposal.getPayload();
        String investigationId = String.valueOf(payload.get("investigationId"));
        InvestigationEntity investigation = investigations.get(investigationId);
        if (!"open".equals(investigation.getStatus())) {
            throw new IllegalStateException("investigation " + investigationId + " is not open");
        }
        var item = investigations.addItem(investigationId, json.valueToTree(payload), "analyst");
        return String.valueOf(item.getId());
    }

    private String annotate(AnalystProposalEntity proposal) {
        String eventId = String.valueOf(proposal.getPayload().get("eventId"));
        IntelligenceEventEntity event = events.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_EVENT, "No such event: " + eventId));
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
        return stale.size();
    }
}
