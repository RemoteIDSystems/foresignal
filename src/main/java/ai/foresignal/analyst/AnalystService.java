package ai.foresignal.analyst;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.llm.LlmService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Analyst profiles, notes, proposals, runs and feedback (prompt 102). Validation enforces the
 * bindings: enabling needs a chat provider ({@code LLM_NOT_CONFIGURED}); {@code allowActions}
 * requires an agent-scope owner; a restricted token cannot own an action-enabled profile.
 */
@Service
public class AnalystService {

    private final AnalystProfileRepository profiles;
    private final AnalystRunRepository runs;
    private final AnalystNoteRepository notes;
    private final AnalystProposalRepository proposals;
    private final AnalystFeedbackRepository feedback;
    private final LlmService llm;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public AnalystService(AnalystProfileRepository profiles, AnalystRunRepository runs, AnalystNoteRepository notes,
            AnalystProposalRepository proposals, AnalystFeedbackRepository feedback, LlmService llm, JsonMapper json,
            Clock clock) {
        this.profiles = profiles;
        this.runs = runs;
        this.notes = notes;
        this.proposals = proposals;
        this.feedback = feedback;
        this.llm = llm;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ profiles

    /** Every profile. */
    public List<AnalystProfileEntity> listProfiles() {
        return profiles.findAll();
    }

    /** One profile, or 404. */
    public AnalystProfileEntity getProfile(String id) {
        return profiles.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_ANALYST_PROFILE,
                "No such analyst profile: " + id));
    }

    /** Creates a profile. */
    @Transactional
    public AnalystProfileEntity create(JsonNode input, AuthenticatedToken owner) {
        Instant now = clock.instant();
        AnalystProfileEntity profile = new AnalystProfileEntity(Ulids.next(now),
                text(input, "name") == null ? "Analyst" : text(input, "name"), now);
        apply(profile, input, owner, true);
        if (profile.isEnabled() && !llm.chatConfigured()) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED,
                    "Enabling an analyst profile needs a configured chat provider.");
        }
        profile.setNextRunAt(profile.isEnabled() ? AnalystSchedule.of(profile.getSchedule()).next(now) : null);
        return profiles.save(profile);
    }

    /** Updates a profile whole. */
    @Transactional
    public AnalystProfileEntity update(String id, JsonNode input, AuthenticatedToken owner) {
        AnalystProfileEntity profile = getProfile(id);
        apply(profile, input, owner, false);
        if (profile.isEnabled() && !llm.chatConfigured()) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED,
                    "Enabling an analyst profile needs a configured chat provider.");
        }
        profile.setNextRunAt(profile.isEnabled() ? AnalystSchedule.of(profile.getSchedule()).next(clock.instant()) : null);
        profile.setUpdatedAt(clock.instant());
        return profiles.save(profile);
    }

    /** Deletes a profile. */
    @Transactional
    public void delete(String id) {
        profiles.delete(getProfile(id));
    }

    private void apply(AnalystProfileEntity profile, JsonNode input, AuthenticatedToken owner, boolean creating) {
        boolean allowActions = input.path("allowActions").asBoolean(false);
        if (allowActions) {
            if (owner == null || owner.scope() == null || !owner.scope().includes(Scope.AGENT)) {
                throw new ValidationException(ErrorCodes.ANALYST_PROFILE_INVALID,
                        "allowActions requires an agent-scope owner token.",
                        List.of(new ai.foresignal.api.error.FieldError("allowActions", "requires agent scope")));
            }
            if (owner.restrictions() != null && !owner.restrictions().isEmpty()) {
                throw new ValidationException(ErrorCodes.ANALYST_PROFILE_INVALID,
                        "A restricted token cannot own an action-enabled profile.",
                        List.of(new ai.foresignal.api.error.FieldError("allowActions", "restricted token")));
            }
        }
        if (creating) {
            profile.setOwnerTokenId(owner == null ? null : owner.id().toString());
        }
        if (input.hasNonNull("name")) {
            profile.setName(text(input, "name"));
        }
        if (input.hasNonNull("enabled")) {
            profile.setEnabled(input.get("enabled").asBoolean());
        }
        if (input.hasNonNull("instructions")) {
            String instructions = text(input, "instructions");
            if (instructions != null && instructions.length() > 4_000) {
                throw ValidationException.forField("instructions", "must be at most 4000 characters");
            }
            profile.setInstructions(instructions);
        }
        if (input.has("schedule") && input.get("schedule").isObject()) {
            try {
                profile.setSchedule(AnalystSchedule.of(map(input.get("schedule"))).toMap());
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("schedule", e.getMessage());
            }
        }
        if (input.has("scope") && input.get("scope").isObject()) {
            Map<String, Object> scope = map(input.get("scope"));
            AnalystScope.of(scope); // validates sources, kinds, places, bbox, eventTypes, ruleTags and minSeverity
            profile.setScope(scope);
        }
        if (input.hasNonNull("lookback")) {
            profile.setLookbackSeconds((int) ai.foresignal.settings.Durations.parse(text(input, "lookback")).toSeconds());
        }
        if (input.hasNonNull("providerId")) {
            profile.setProviderId(text(input, "providerId"));
        }
        String effort = text(input, "effort");
        if (effort != null) {
            if (!List.of("low", "medium").contains(effort)) {
                throw ValidationException.forField("effort", "must be low or medium");
            }
            profile.setEffort(effort);
        }
        profile.setMaxRunsPerDay(intOr(input, "maxRunsPerDay", profile.getMaxRunsPerDay()));
        profile.setMaxTokensPerRun(intOr(input, "maxTokensPerRun", profile.getMaxTokensPerRun()));
        profile.setMaxToolCalls(intOr(input, "maxToolCalls", profile.getMaxToolCalls()));
        profile.setMaxWallSeconds(intOr(input, "maxWallSeconds", profile.getMaxWallSeconds()));
        profile.setAllowActions(allowActions);
        if (input.has("allowedActions") && input.get("allowedActions").isArray()) {
            List<String> actions = new ArrayList<>();
            for (JsonNode node : input.get("allowedActions")) {
                String action = node.asString();
                if (!AnalystActions.ALL.contains(action)) {
                    throw ValidationException.forField("allowedActions", "unknown action " + action);
                }
                actions.add(action);
            }
            profile.setAllowedActions(actions);
        }
        profile.setMaxAutoActionsPerDay(intOr(input, "maxAutoActionsPerDay", profile.getMaxAutoActionsPerDay()));
        if (input.has("channelIds") && input.get("channelIds").isArray()) {
            List<String> channels = new ArrayList<>();
            for (JsonNode node : input.get("channelIds")) {
                channels.add(node.asString());
            }
            profile.setChannelIds(channels);
        }
        String priority = text(input, "notifyMinPriority");
        if (priority != null) {
            if (!List.of("low", "normal", "high").contains(priority)) {
                throw ValidationException.forField("notifyMinPriority", "must be low, normal or high");
            }
            profile.setNotifyMinPriority(priority);
        }
    }

    // ------------------------------------------------------------------ runs, notes, proposals

    /** A profile's runs, newest first. */
    public List<AnalystRunEntity> listRuns(String profileId, String status, int limit) {
        List<AnalystRunEntity> rows = profileId == null ? runs.findAllByOrderByStartedAtDescIdDesc(Limit.of(limit))
                : runs.findByProfileIdOrderByStartedAtDesc(profileId, Limit.of(limit));
        if (status == null) {
            return rows;
        }
        List<AnalystRunEntity> out = new ArrayList<>();
        for (AnalystRunEntity run : rows) {
            if (status.equals(run.getStatus())) {
                out.add(run);
            }
        }
        return out;
    }

    /** One run, or 404. */
    public AnalystRunEntity getRun(String id) {
        return runs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PROPOSAL,
                "No such analyst run: " + id));
    }

    /**
     * Notes the caller may see, filtered: a private note (one written from private evidence) is left out
     * unless {@code privateAccess}.
     */
    public List<AnalystNoteEntity> listNotes(String profileId, String status, String priority, Instant since, int limit,
            boolean privateAccess) {
        List<AnalystNoteEntity> out = new ArrayList<>();
        for (AnalystNoteEntity note : listNotes(profileId, status, priority, since, limit)) {
            if (privateAccess || !note.isPrivateNote()) {
                out.add(note);
            }
        }
        return out;
    }

    /** One note the caller may see, or {@code 404} (a private note is not disclosed without private access). */
    public AnalystNoteEntity visibleNote(String id, boolean privateAccess) {
        AnalystNoteEntity note = getNote(id);
        if (note.isPrivateNote() && !privateAccess) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PROPOSAL, "No such analyst note: " + id);
        }
        return note;
    }

    /** Notes, filtered, private ones included (internal callers only; egress paths use the private-aware form). */
    List<AnalystNoteEntity> listNotes(String profileId, String status, String priority, Instant since, int limit) {
        List<AnalystNoteEntity> rows;
        if (profileId != null && status != null) {
            rows = notes.findByProfileIdAndStatusOrderByCreatedAtDescIdDesc(profileId, status, Limit.of(limit));
        } else if (profileId != null) {
            rows = notes.findByProfileIdOrderByCreatedAtDescIdDesc(profileId, Limit.of(limit));
        } else {
            rows = notes.findAllByOrderByCreatedAtDescIdDesc(Limit.of(limit));
        }
        List<AnalystNoteEntity> out = new ArrayList<>();
        for (AnalystNoteEntity note : rows) {
            if (priority != null && !priority.equals(note.getPriority())) {
                continue;
            }
            if (since != null && note.getCreatedAt().isBefore(since)) {
                continue;
            }
            out.add(note);
        }
        return out;
    }

    /** One note, or 404. */
    public AnalystNoteEntity getNote(String id) {
        return notes.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PROPOSAL,
                "No such analyst note: " + id));
    }

    /** Sets a note's read status. */
    @Transactional
    public AnalystNoteEntity updateNoteStatus(String id, String status, boolean privateAccess) {
        if (!List.of("new", "read", "dismissed").contains(status)) {
            throw ValidationException.forField("status", "must be new, read or dismissed");
        }
        AnalystNoteEntity note = visibleNote(id, privateAccess);
        note.setStatus(status);
        return notes.save(note);
    }

    /** Records note feedback. */
    @Transactional
    public AnalystFeedbackEntity noteFeedback(String id, String rating, String comment, String actor,
            boolean privateAccess) {
        visibleNote(id, privateAccess);
        return saveFeedback("note", id, rating, comment, actor);
    }

    /** Proposals the caller may see, filtered by status: a private proposal needs private access. */
    public List<AnalystProposalEntity> listProposals(String status, int limit, boolean privateAccess) {
        List<AnalystProposalEntity> out = new ArrayList<>();
        for (AnalystProposalEntity proposal : listProposals(status, limit)) {
            if (privateAccess || !proposal.isPrivateProposal()) {
                out.add(proposal);
            }
        }
        return out;
    }

    /** Proposals, filtered by status, private ones included (internal callers only). */
    List<AnalystProposalEntity> listProposals(String status, int limit) {
        return status == null ? proposals.findAllByOrderByCreatedAtDescIdDesc(Limit.of(limit))
                : proposals.findByStatusOrderByCreatedAtDescIdDesc(status, Limit.of(limit));
    }

    /** One proposal, or 404. */
    public AnalystProposalEntity getProposal(String id) {
        return proposals.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PROPOSAL,
                "No such proposal: " + id));
    }

    /** Records proposal feedback. */
    @Transactional
    public AnalystFeedbackEntity proposalFeedback(String id, String rating, String comment, String actor) {
        getProposal(id);
        return saveFeedback("proposal", id, rating, comment, actor);
    }

    private AnalystFeedbackEntity saveFeedback(String target, String id, String rating, String comment, String actor) {
        if (!List.of("useful", "not_useful", "wrong").contains(rating)) {
            throw ValidationException.forField("rating", "must be useful, not_useful or wrong");
        }
        AnalystFeedbackEntity entity = new AnalystFeedbackEntity(Ulids.next(clock.instant()), target, id, rating,
                actor == null ? "operator" : actor, clock.instant());
        entity.setComment(comment);
        return feedback.save(entity);
    }

    /** Per-profile evaluation aggregates since {@code since}. */
    public Map<String, Object> evaluation(String profileId, Instant since) {
        List<AnalystProfileEntity> targets = profileId == null ? profiles.findAll()
                : List.of(getProfile(profileId));
        List<Map<String, Object>> out = new ArrayList<>();
        for (AnalystProfileEntity profile : targets) {
            List<AnalystRunEntity> rows = runs.findByProfileIdOrderByStartedAtDesc(profile.getId(), Limit.of(5_000)).stream()
                    .filter(run -> run.getStartedAt() == null || !run.getStartedAt().isBefore(since)).toList();
            Map<String, Integer> byStatus = new LinkedHashMap<>();
            long tokens = 0;
            for (AnalystRunEntity run : rows) {
                byStatus.merge(run.getStatus(), 1, Integer::sum);
                tokens += run.getTokensIn() + run.getTokensOut();
            }
            Map<String, Object> aggregate = new LinkedHashMap<>();
            aggregate.put("profileId", profile.getId());
            aggregate.put("runs", rows.size());
            aggregate.put("byStatus", byStatus);
            aggregate.put("noopShare", rows.isEmpty() ? 0.0
                    : (double) byStatus.getOrDefault("noop", 0) / rows.size());
            aggregate.put("tokens", tokens);
            long confirmed = proposals.countByProfileIdAndStatusAndCreatedAtGreaterThanEqual(profile.getId(), "executed",
                    since);
            long rejected = proposals.countByProfileIdAndStatusAndCreatedAtGreaterThanEqual(profile.getId(), "rejected",
                    since);
            aggregate.put("proposalAcceptance", confirmed + rejected == 0 ? null
                    : (double) confirmed / (confirmed + rejected));
            aggregate.put("notesDismissedShare", dismissedShare(profile.getId(), since));
            aggregate.put("useful", feedback.countByTargetAndRating("note", "useful"));
            aggregate.put("wrong", feedback.countByTargetAndRating("note", "wrong"));
            out.add(aggregate);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("since", since.toString());
        result.put("profiles", out);
        return result;
    }

    private double dismissedShare(String profileId, Instant since) {
        List<AnalystNoteEntity> rows = notes.findByProfileIdOrderByCreatedAtDescIdDesc(profileId, Limit.of(5_000)).stream()
                .filter(note -> !note.getCreatedAt().isBefore(since)).toList();
        if (rows.isEmpty()) {
            return 0;
        }
        long dismissed = rows.stream().filter(n -> "dismissed".equals(n.getStatus())).count();
        return (double) dismissed / rows.size();
    }

    // ------------------------------------------------------------------ helpers

    private static int intOr(JsonNode input, String field, int fallback) {
        return input.hasNonNull(field) ? input.get(field).asInt() : fallback;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private Map<String, Object> map(JsonNode node) {
        return json.readValue(json.writeValueAsString(node), new tools.jackson.core.type.TypeReference<>() {});
    }
}
