/**
 * Per-user quotas (prompt R2-026): a user's limits ({@link ai.foresignal.security.quota.Quota}, stored in
 * {@code user_quota}) merged over the instance default ({@code app_setting} key {@code quota.default}) by
 * {@link ai.foresignal.security.quota.QuotaService}; the per-user API rate limit
 * ({@link ai.foresignal.security.quota.UserRateLimitFilter}); and the count and storage limits checked where a user
 * creates trigger rules, channels, saved views, workspaces and attachments ({@code 409 QUOTA_EXCEEDED}).
 */
package ai.foresignal.security.quota;
