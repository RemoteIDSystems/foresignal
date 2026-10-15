package ai.foresignal.analyst;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shapes analyst profiles, runs, notes, proposals and feedback for the API and MCP (prompt 102). */
public final class AnalystViews {

    private AnalystViews() {}

    /** A profile. */
    public static Map<String, Object> profile(AnalystProfileEntity profile) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", profile.getId());
        out.put("name", profile.getName());
        out.put("enabled", profile.isEnabled());
        out.put("schedule", profile.getSchedule());
        out.put("scope", profile.getScope());
        out.put("lookback", profile.getLookbackSeconds() == null ? null
                : java.time.Duration.ofSeconds(profile.getLookbackSeconds()).toString());
        out.put("instructions", profile.getInstructions());
        out.put("providerId", profile.getProviderId());
        out.put("effort", profile.getEffort());
        out.put("maxRunsPerDay", profile.getMaxRunsPerDay());
        out.put("maxTokensPerRun", profile.getMaxTokensPerRun());
        out.put("maxToolCalls", profile.getMaxToolCalls());
        out.put("maxWallSeconds", profile.getMaxWallSeconds());
        out.put("allowActions", profile.isAllowActions());
        out.put("allowedActions", profile.getAllowedActions());
        out.put("maxAutoActionsPerDay", profile.getMaxAutoActionsPerDay());
        out.put("channelIds", profile.getChannelIds());
        out.put("notifyMinPriority", profile.getNotifyMinPriority());
        out.put("ownerTokenId", profile.getOwnerTokenId());
        out.put("nextRunAt", profile.getNextRunAt());
        out.put("lastRunAt", profile.getLastRunAt());
        out.put("createdAt", profile.getCreatedAt());
        out.put("updatedAt", profile.getUpdatedAt());
        return out;
    }

    /** A run. */
    public static Map<String, Object> run(AnalystRunEntity run) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", run.getId());
        out.put("profileId", run.getProfileId());
        out.put("status", run.getStatus());
        out.put("startedAt", run.getStartedAt());
        out.put("finishedAt", run.getFinishedAt());
        out.put("cursorFrom", run.getCursorFrom());
        out.put("cursorTo", run.getCursorTo());
        out.put("inputCounts", run.getInputCounts());
        out.put("provider", run.getProvider());
        out.put("model", run.getModel());
        out.put("effort", run.getEffort());
        out.put("tokensIn", run.getTokensIn());
        out.put("tokensOut", run.getTokensOut());
        out.put("cachedTokens", run.getCachedTokens());
        out.put("toolCalls", run.getToolCalls());
        out.put("durationMs", run.getDurationMs());
        out.put("notes", run.getNotes());
        out.put("proposals", run.getProposals());
        out.put("autoExecuted", run.getAutoExecuted());
        out.put("citationsDropped", run.getCitationsDropped());
        out.put("claimsDowngraded", run.getClaimsDowngraded());
        out.put("warnings", run.getWarnings());
        out.put("reason", run.getReason());
        out.put("promptHash", run.getPromptHash());
        out.put("detectorVersion", run.getDetectorVersion());
        out.put("content", run.getContent());
        return out;
    }

    /** A note with its footer. */
    public static Map<String, Object> note(AnalystNoteEntity note) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", note.getId());
        out.put("profileId", note.getProfileId());
        out.put("runId", note.getRunId());
        out.put("title", note.getTitle());
        out.put("body", note.getBody());
        out.put("priority", note.getPriority());
        out.put("claims", note.getClaims());
        out.put("citations", note.getCitations());
        out.put("private", note.isPrivateNote());
        out.put("status", note.getStatus());
        out.put("model", note.getModel());
        out.put("footer", String.format(AnalystRunner.FOOTER,
                note.getModel() == null ? "the configured model" : note.getModel()));
        out.put("createdAt", note.getCreatedAt());
        return out;
    }

    /** A proposal. */
    public static Map<String, Object> proposal(AnalystProposalEntity proposal) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", proposal.getId());
        out.put("runId", proposal.getRunId());
        out.put("profileId", proposal.getProfileId());
        out.put("type", proposal.getType());
        out.put("payload", proposal.getPayload());
        out.put("rationale", proposal.getRationale());
        out.put("citations", proposal.getCitations());
        out.put("status", proposal.getStatus());
        out.put("autoExecuted", proposal.isAutoExecuted());
        out.put("executedRef", proposal.getExecutedRef());
        out.put("decidedBy", proposal.getDecidedBy());
        out.put("decidedAt", proposal.getDecidedAt());
        out.put("reason", proposal.getReason());
        out.put("createdAt", proposal.getCreatedAt());
        out.put("expiresAt", proposal.getExpiresAt());
        return out;
    }

    /** Feedback. */
    public static Map<String, Object> feedback(AnalystFeedbackEntity entity) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId());
        out.put("target", entity.getTarget());
        out.put("targetId", entity.getTargetId());
        out.put("rating", entity.getRating());
        out.put("comment", entity.getComment());
        out.put("by", entity.getBy());
        out.put("at", entity.getAt());
        return out;
    }

    /** A list wrapper. */
    public static Map<String, Object> page(List<Map<String, Object>> items) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items == null ? List.of() : new ArrayList<>(items));
        out.put("nextCursor", null);
        return out;
    }
}
