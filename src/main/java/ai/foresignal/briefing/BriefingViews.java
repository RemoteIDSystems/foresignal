package ai.foresignal.briefing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps briefings and runs to the OpenAPI {@code Briefing} and {@code BriefingRun} objects (prompt 029). */
public final class BriefingViews {

    private BriefingViews() {}

    /**
     * The {@code Briefing} object of {@code entity}.
     *
     * @param entity the briefing
     * @param warnings its current validation warnings
     */
    public static Map<String, Object> briefing(BriefingEntity entity, List<String> warnings) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId().toString());
        out.put("name", entity.getName());
        out.put("enabled", entity.isEnabled());
        out.put("schedule", entity.getSchedule());
        out.put("timezone", entity.getTimezone());
        out.put("scope", BriefingScope.fromMap(entity.getScope()).toMap());
        out.put("lookback", entity.getLookback());
        out.put("mode", entity.getMode());
        out.put("instructions", entity.getInstructions());
        out.put("channelIds", new ArrayList<>(entity.getChannelIds()));
        out.put("maxItemsPerKind", entity.getMaxItemsPerKind());
        out.put("audio", entity.getAudio() == null ? null : new LinkedHashMap<>(entity.getAudio()));
        // NP-003: the owning user of an edition and its output language.
        out.put("outputLanguage", entity.getOutputLanguage());
        out.put("ownerUserId", entity.getOwnerUserId() == null ? null : entity.getOwnerUserId().toString());
        // NP-005: the hosted site.
        out.put("site", new BriefingInput.Site(entity.isSiteEnabled(), entity.isSiteShareable()).toMap());
        // NP-004: where each run goes.
        out.put("deliveryTargets", entity.getDeliveryTargets() == null ? List.of()
                : BriefingInput.Edition.targets(entity.getDeliveryTargets()).stream().map(DeliveryTarget::toMap).toList());
        out.put("createdAt", entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString());
        out.put("updatedAt", entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString());
        out.put("nextRunAt", entity.getNextRunAt() == null ? null : entity.getNextRunAt().toString());
        out.put("lastRunAt", entity.getLastRunAt() == null ? null : entity.getLastRunAt().toString());
        out.put("lastRunStatus", entity.getLastRunStatus());
        out.put("warnings", warnings == null ? List.of() : List.copyOf(warnings));
        return out;
    }

    /** The {@code BriefingRun} object of a stored run. */
    public static Map<String, Object> run(BriefingRunEntity run) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", run.getId().toString());
        out.put("briefingId", run.getBriefingId().toString());
        out.put("status", run.getStatus());
        out.put("startedAt", run.getStartedAt().toString());
        out.put("finishedAt", run.getFinishedAt() == null ? null : run.getFinishedAt().toString());
        out.put("periodFrom", run.getPeriodFrom().toString());
        out.put("periodTo", run.getPeriodTo().toString());
        out.put("mode", run.getMode());
        out.put("contentMarkdown", run.getContentMarkdown());
        out.put("citations", run.getCitations() == null ? List.of() : run.getCitations());
        out.put("stats", run.getStats());
        out.put("error", run.getError());
        out.put("usage", run.getUsage());
        out.put("deliveries", run.getDeliveries());
        out.put("warnings", run.getWarnings() == null ? List.of() : run.getWarnings());
        // Detached JWS over contentMarkdown's UTF-8 bytes (prompt 109); null for runs without markdown.
        out.put("signature", run.getSignature());
        // Prompt 141: the synthesized audio (status, mediaId, format, voice, characters, truncated, error, signature).
        out.put("audio", run.getAudio() == null ? null : new LinkedHashMap<>(run.getAudio()));
        // NP-004: the outcome of each delivery target, or null for a briefing without targets.
        out.put("deliveryResults", run.getDeliveryResults() == null ? null : new ArrayList<>(run.getDeliveryResults()));
        return out;
    }

    /** The {@code BriefingRun} object of a preview (nothing stored: {@code id} and {@code briefingId} null). */
    public static Map<String, Object> preview(BriefingRunner.Outcome outcome) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", null);
        out.put("briefingId", null);
        out.put("status", outcome.status());
        out.put("startedAt", outcome.startedAt().toString());
        out.put("finishedAt", outcome.finishedAt() == null ? null : outcome.finishedAt().toString());
        out.put("periodFrom", outcome.periodFrom().toString());
        out.put("periodTo", outcome.periodTo().toString());
        out.put("mode", outcome.mode());
        out.put("contentMarkdown", outcome.markdown());
        out.put("citations", outcome.citations());
        out.put("stats", outcome.stats());
        out.put("error", outcome.error());
        out.put("usage", outcome.usage());
        out.put("deliveries", 0);
        out.put("warnings", outcome.warnings());
        out.put("audio", null);
        return out;
    }

    /**
     * The {@code EditionSummary} of an owned briefing (prompt NP-003, {@code listMyEditions}) without a site URL.
     *
     * @param entity the edition
     * @param latestRunId its latest completed run, or null
     */
    public static Map<String, Object> editionSummary(BriefingEntity entity, java.util.UUID latestRunId) {
        return editionSummary(entity, latestRunId, null);
    }

    /**
     * The {@code EditionSummary} with its site URL (NP-005).
     *
     * @param entity the edition
     * @param latestRunId its latest completed run, or null
     * @param siteUrl the canonical site URL while the site is enabled, else null
     */
    public static Map<String, Object> editionSummary(BriefingEntity entity, java.util.UUID latestRunId,
            String siteUrl) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("briefingId", entity.getId().toString());
        out.put("name", entity.getName());
        out.put("enabled", entity.isEnabled());
        out.put("outputLanguage", entity.getOutputLanguage());
        out.put("schedule", entity.getSchedule());
        out.put("timezone", entity.getTimezone());
        out.put("nextRunAt", entity.getNextRunAt() == null ? null : entity.getNextRunAt().toString());
        out.put("lastRunAt", entity.getLastRunAt() == null ? null : entity.getLastRunAt().toString());
        out.put("lastRunStatus", entity.getLastRunStatus());
        out.put("latestRunId", latestRunId == null ? null : latestRunId.toString());
        out.put("siteUrl", entity.isSiteEnabled() ? siteUrl : null);
        return out;
    }
}
