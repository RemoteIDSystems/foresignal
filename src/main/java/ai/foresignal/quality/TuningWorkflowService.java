package ai.foresignal.quality;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.backtest.TuningSuggester;
import ai.foresignal.security.Ulids;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.trigger.TriggerRulesChangedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * The tuning workflow (prompt 115): 103's deterministic suggestions persisted with a lifecycle, applied only as
 * an explicit rule update, logged, and undoable. Nothing here changes a rule on its own.
 *
 * <ul>
 *   <li><b>Generation</b> ({@link #generate}, on {@code GET …/suggestions} and in the nightly job) persists each
 *       suggestion of {@link TuningSuggester} once per {@code (rule, kind, patch)}: an open one is refreshed, a
 *       stale one with the same patch is reopened at the rule's current revision, an applied or dismissed one is
 *       left alone.
 *   <li><b>Staleness:</b> any rule change ({@link TriggerRulesChangedEvent}) whose definition hash differs from an
 *       open suggestion's {@code rule_rev} marks it {@code STALE}; a deleted rule's open suggestions all go stale.
 *   <li><b>Apply</b> refuses with {@code 409 SUGGESTION_STALE} when the suggestion is not open or the rule changed,
 *       then merges the patch into the rule's current {@code TriggerRuleInput} and goes through
 *       {@link TriggerRuleService#update} (same validation, same events), and writes {@code rule_tuning_log}.
 *   <li><b>Undo</b> restores {@code before} through the same update while the rule is unchanged since the change
 *       ({@code 409} otherwise), and reopens the suggestion.
 * </ul>
 */
@Service
public class TuningWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(TuningWorkflowService.class);

    /** The longest note or dismissal reason. */
    public static final int MAX_NOTE = 2_000;

    private final TuningSuggester suggester;
    private final TuningSuggestionRepository suggestions;
    private final RuleTuningLogRepository logs;
    private final RuleRevisions revisions;
    private final TriggerRuleService rules;
    private final JsonMapper json;
    private final Clock clock;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    /** Creates the service. */
    public TuningWorkflowService(TuningSuggester suggester, TuningSuggestionRepository suggestions,
            RuleTuningLogRepository logs, RuleRevisions revisions, TriggerRuleService rules, JsonMapper json,
            Clock clock) {
        this.suggester = suggester;
        this.suggestions = suggestions;
        this.logs = logs;
        this.revisions = revisions;
        this.rules = rules;
        this.json = json;
        this.clock = clock;
    }

    /**
     * Generates and persists the suggestions of {@code ruleId} since {@code since}.
     *
     * @return the persisted records of this generation, in the suggester's order
     * @throws NotFoundException when the rule does not exist
     */
    public synchronized List<TuningSuggestionEntity> generate(UUID ruleId, Instant since) {
        String rev = revisions.revision(revisions.input(ruleId));
        List<TuningSuggestionEntity> out = new ArrayList<>();
        for (Map<String, Object> suggestion : suggester.forRule(ruleId, since)) {
            out.add(persist(ruleId, rev, suggestion));
        }
        return out;
    }

    /** Persists one generated suggestion with the deduplication rules of the class comment. */
    @SuppressWarnings("unchecked")
    TuningSuggestionEntity persist(UUID ruleId, String rev, Map<String, Object> suggestion) {
        String kind = String.valueOf(suggestion.get("kind"));
        Map<String, Object> patch = suggestion.get("patch") instanceof Map<?, ?> p
                ? new LinkedHashMap<>((Map<String, Object>) p) : new LinkedHashMap<>();
        String patchHash = RuleRevisions.sha256(revisions.canonical(patch));
        Instant now = clock.instant();
        TuningSuggestionEntity entity = suggestions.findByRuleIdAndKindAndPatchHash(ruleId, kind, patchHash)
                .orElse(null);
        if (entity == null) {
            entity = new TuningSuggestionEntity(Ulids.next(now), ruleId, kind, patchHash);
            entity.setCreatedAt(now);
            entity.setPatch(patch);
            entity.setRuleRev(rev);
        } else if (TuningSuggestionEntity.STALE.equals(entity.getStatus())) {
            entity.setStatus(TuningSuggestionEntity.OPEN);
            entity.setRuleRev(rev);
            entity.setDecidedAt(null);
            entity.setDecidedBy(null);
        } else if (!TuningSuggestionEntity.OPEN.equals(entity.getStatus())) {
            return entity;
        } else {
            entity.setRuleRev(rev);
        }
        entity.setKey(suggestion.get("id") == null ? null : String.valueOf(suggestion.get("id")));
        entity.setRationale(String.valueOf(suggestion.get("rationale")));
        entity.setExpectedEffect(suggestion.get("expectedEffect") instanceof Map<?, ?> e
                ? (Map<String, Object>) e : null);
        entity.setCaveats(suggestion.get("caveats") instanceof List<?> c ? (List<Map<String, Object>>) c : List.of());
        return suggestions.saveAndFlush(entity);
    }

    /**
     * The persisted suggestions of {@code ruleId} in {@code status} (all when null), newest first.
     *
     * @throws ValidationException for an unknown status
     */
    public List<TuningSuggestionEntity> list(UUID ruleId, String status) {
        if (status == null) {
            return suggestions.findByRuleIdOrderByCreatedAtDescIdDesc(ruleId);
        }
        if (!TuningSuggestionEntity.STATUSES.contains(status)) {
            throw ValidationException.forField("status", "must be one of OPEN, APPLIED, DISMISSED, STALE");
        }
        return suggestions.findByRuleIdAndStatusOrderByCreatedAtDescIdDesc(ruleId, status);
    }

    /** Marks the open suggestions of a changed rule stale when its definition moved on. */
    @EventListener
    public void onRuleChanged(TriggerRulesChangedEvent event) {
        try {
            markStale(event.ruleId());
        } catch (RuntimeException e) {
            log.warn("Could not mark the suggestions of rule {} stale: {}", event.ruleId(), e.toString());
        }
    }

    /**
     * Marks the open suggestions of {@code ruleId} whose {@code rule_rev} differs from the current revision stale
     * (all of them when the rule is gone).
     *
     * @return how many were marked
     */
    public int markStale(UUID ruleId) {
        if (ruleId == null) {
            return 0;
        }
        String rev = revisions.revision(ruleId);
        int marked = 0;
        for (TuningSuggestionEntity s : suggestions.findByRuleIdAndStatusOrderByCreatedAtDescIdDesc(ruleId,
                TuningSuggestionEntity.OPEN)) {
            if (inFlight.contains(s.getId()) || (rev != null && rev.equals(s.getRuleRev()))) {
                continue;
            }
            s.setStatus(TuningSuggestionEntity.STALE);
            suggestions.save(s);
            marked++;
        }
        if (marked > 0) {
            suggestions.flush();
        }
        return marked;
    }

    /**
     * {@code applyTuningSuggestion}: applies the patch through the normal rule update and logs it.
     *
     * @param ruleId the rule
     * @param suggestionId the suggestion
     * @param note an optional note (at most {@value #MAX_NOTE} characters)
     * @param actor who applied it
     * @return the tuning log entry
     * @throws NotFoundException when the rule or suggestion does not exist
     * @throws ConflictException {@code 409 SUGGESTION_STALE}
     * @throws ValidationException when the patched rule is not valid
     */
    public synchronized RuleTuningLogEntity apply(UUID ruleId, String suggestionId, String note, String actor) {
        checkNote(note, "note");
        TuningSuggestionEntity suggestion = suggestion(ruleId, suggestionId);
        if (!TuningSuggestionEntity.OPEN.equals(suggestion.getStatus())) {
            throw new ConflictException(ErrorCodes.SUGGESTION_STALE, "Suggestion " + suggestionId + " is "
                    + suggestion.getStatus() + "; only an open suggestion can be applied.");
        }
        Map<String, Object> before = revisions.input(ruleId);
        if (!revisions.revision(before).equals(suggestion.getRuleRev())) {
            suggestion.setStatus(TuningSuggestionEntity.STALE);
            suggestions.saveAndFlush(suggestion);
            throw new ConflictException(ErrorCodes.SUGGESTION_STALE, "The rule changed since suggestion "
                    + suggestionId + " was generated; generate suggestions again.");
        }
        Map<String, Object> patched = RuleRevisions.merge(before, suggestion.getPatch());
        TriggerRuleService.RuleView updated;
        inFlight.add(suggestion.getId());
        try {
            updated = rules.update(ruleId, json.valueToTree(patched));
        } finally {
            inFlight.remove(suggestion.getId());
        }
        Map<String, Object> after = revisions.input(updated);
        Instant now = clock.instant();
        suggestion.setStatus(TuningSuggestionEntity.APPLIED);
        suggestion.setDecidedBy(actor);
        suggestion.setDecidedAt(now);
        suggestion.setDecisionReason(note);
        suggestions.saveAndFlush(suggestion);
        RuleTuningLogEntity entry = new RuleTuningLogEntity(Ulids.next(now), ruleId);
        entry.setSuggestionId(suggestion.getId());
        entry.setBefore(before);
        entry.setAfter(after);
        entry.setAfterRev(revisions.revision(after));
        entry.setNote(note);
        entry.setAppliedBy(actor);
        entry.setAppliedAt(now);
        return logs.saveAndFlush(entry);
    }

    /**
     * {@code dismissTuningSuggestion}: an open or stale suggestion becomes {@code DISMISSED}.
     *
     * @throws ConflictException {@code 409 SUGGESTION_STALE} when it is applied or dismissed already
     */
    public TuningSuggestionEntity dismiss(UUID ruleId, String suggestionId, String reason, String actor) {
        checkNote(reason, "reason");
        TuningSuggestionEntity suggestion = suggestion(ruleId, suggestionId);
        if (TuningSuggestionEntity.APPLIED.equals(suggestion.getStatus())
                || TuningSuggestionEntity.DISMISSED.equals(suggestion.getStatus())) {
            throw new ConflictException(ErrorCodes.SUGGESTION_STALE, "Suggestion " + suggestionId + " is "
                    + suggestion.getStatus() + " already.");
        }
        suggestion.setStatus(TuningSuggestionEntity.DISMISSED);
        suggestion.setDecidedBy(actor);
        suggestion.setDecidedAt(clock.instant());
        suggestion.setDecisionReason(reason);
        return suggestions.saveAndFlush(suggestion);
    }

    /**
     * {@code undoTuning}: restores the entry's {@code before} through the normal rule update while the rule is
     * unchanged since the change, and reopens the suggestion.
     *
     * @throws ConflictException {@code 409 SUGGESTION_STALE} when the rule changed since, or the entry was undone
     */
    public synchronized RuleTuningLogEntity undo(UUID ruleId, String logId, String actor) {
        RuleTuningLogEntity entry = logs.findById(logId).filter(e -> e.getRuleId().equals(ruleId))
                .orElseThrow(() -> NotFoundException.of("Tuning log entry", logId));
        if (entry.getUndoneAt() != null) {
            throw new ConflictException(ErrorCodes.SUGGESTION_STALE, "Tuning log entry " + logId + " was undone"
                    + " already.");
        }
        String current = revisions.revision(ruleId);
        if (current == null || !current.equals(entry.getAfterRev())) {
            throw new ConflictException(ErrorCodes.SUGGESTION_STALE, "The rule changed since this tuning change was"
                    + " applied; it can no longer be undone.");
        }
        TuningSuggestionEntity suggestion = entry.getSuggestionId() == null ? null
                : suggestions.findById(entry.getSuggestionId()).orElse(null);
        if (suggestion != null) {
            inFlight.add(suggestion.getId());
        }
        try {
            rules.update(ruleId, json.valueToTree(entry.getBefore()));
        } finally {
            if (suggestion != null) {
                inFlight.remove(suggestion.getId());
            }
        }
        Instant now = clock.instant();
        if (suggestion != null) {
            suggestion.setStatus(TuningSuggestionEntity.OPEN);
            suggestion.setRuleRev(revisions.revision(ruleId));
            suggestion.setDecidedAt(null);
            suggestion.setDecidedBy(null);
            suggestions.saveAndFlush(suggestion);
        }
        entry.setUndoneBy(actor);
        entry.setUndoneAt(now);
        return logs.saveAndFlush(entry);
    }

    /**
     * {@code listTuningLog}: the rule's entries, newest first.
     *
     * @param beforeId only entries with a smaller id (the cursor), or null
     * @param limit the page size
     */
    public List<RuleTuningLogEntity> log(UUID ruleId, String beforeId, int limit) {
        rules.load(ruleId);
        return logs.findByRuleIdOrderByIdDesc(ruleId).stream()
                .filter(e -> beforeId == null || e.getId().compareTo(beforeId) < 0)
                .limit(limit)
                .toList();
    }

    /** The suggestion {@code suggestionId} of {@code ruleId}, or 404. */
    public TuningSuggestionEntity suggestion(UUID ruleId, String suggestionId) {
        rules.load(ruleId);
        return suggestions.findById(suggestionId).filter(s -> s.getRuleId().equals(ruleId))
                .orElseThrow(() -> NotFoundException.of("Tuning suggestion", suggestionId));
    }

    private static void checkNote(String note, String field) {
        if (note != null && note.length() > MAX_NOTE) {
            throw ValidationException.forField(field, "must be at most " + MAX_NOTE + " characters");
        }
    }

    /** The {@code TuningSuggestionRecord} object of {@code s}. */
    public static Map<String, Object> view(TuningSuggestionEntity s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId());
        out.put("kind", s.getKind());
        out.put("rationale", s.getRationale());
        out.put("patch", s.getPatch());
        out.put("expectedEffect", s.getExpectedEffect());
        out.put("caveats", s.getCaveats());
        out.put("ruleId", s.getRuleId().toString());
        out.put("ruleRev", s.getRuleRev());
        out.put("status", s.getStatus());
        out.put("createdAt", s.getCreatedAt() == null ? null : s.getCreatedAt().toString());
        out.put("decidedBy", s.getDecidedBy());
        out.put("decidedAt", s.getDecidedAt() == null ? null : s.getDecidedAt().toString());
        out.put("decisionReason", s.getDecisionReason());
        return out;
    }

    /** The {@code TuningLogEntry} object of {@code e}. */
    public static Map<String, Object> view(RuleTuningLogEntity e) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId());
        out.put("ruleId", e.getRuleId().toString());
        out.put("suggestionId", e.getSuggestionId());
        out.put("before", e.getBefore());
        out.put("after", e.getAfter());
        out.put("note", e.getNote());
        out.put("appliedBy", e.getAppliedBy());
        out.put("appliedAt", e.getAppliedAt() == null ? null : e.getAppliedAt().toString());
        out.put("undoneBy", e.getUndoneBy());
        out.put("undoneAt", e.getUndoneAt() == null ? null : e.getUndoneAt().toString());
        return out;
    }
}
