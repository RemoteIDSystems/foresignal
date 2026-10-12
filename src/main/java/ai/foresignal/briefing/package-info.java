/**
 * Scheduled briefings and the catch-up digest (prompt 029, Architecture §22).
 *
 * <p>A {@link ai.foresignal.briefing.BriefingEntity briefing} is a schedule (5-field cron plus an IANA
 * timezone), a scope, a lookback and a mode. {@link ai.foresignal.briefing.BriefingScheduler} claims
 * due briefings with {@code FOR UPDATE SKIP LOCKED} and runs each on a virtual thread through
 * {@link ai.foresignal.briefing.BriefingRunner}, which computes the digest statistics
 * ({@link ai.foresignal.briefing.DigestStatsService}, shared with {@code GET /v1/digest}), renders
 * deterministic markdown ({@link ai.foresignal.briefing.DigestRenderer}) or, in LLM mode, a cited
 * narrative ({@link ai.foresignal.briefing.LlmBriefingWriter}, falling back to the digest), stores the
 * run, embeds it and publishes a {@code briefing} notification. Runs are readable as JSON and as
 * sanitized HTML ({@link ai.foresignal.briefing.BriefingHtmlRenderer}).
 */
package ai.foresignal.briefing;
