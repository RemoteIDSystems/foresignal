/**
 * Social signals (prompt 135, Intelligence-Design §12.7): public social media collected as reporting, never as
 * observation.
 *
 * <ul>
 *   <li>{@link ai.foresignal.social.SocialPost} and {@link ai.foresignal.social.SocialPosts}: the {@code social-post}
 *       record kind ({@code externalId} = {@code {platform}:{postId}}), edits as new versions, deletion sync as
 *       tombstoned versions (text and media links removed, aggregates kept).
 *   <li>{@link ai.foresignal.social.TermIndex} and {@link ai.foresignal.social.TermIndexService}: the Aho–Corasick
 *       matcher over every enabled attention topic's {@code social} block (case- and diacritic-folded, word-boundary
 *       aware per script), with place, region and entity names expanded into terms.
 *   <li>{@link ai.foresignal.social.SocialCollector}: what every platform adapter hands its posts to. Matching posts
 *       become records; every post on a complete stream is counted into the platform-language denominators.
 *   <li>{@link ai.foresignal.social.SocialVolumeStore}, {@link ai.foresignal.social.SocialBaseline} 1.0.0 and
 *       {@link ai.foresignal.social.VelocityDetector}: hourly mention-volume series per topic, place and entity with
 *       seasonal, denominator-scaled baselines and {@code social-velocity} findings.
 *   <li>{@link ai.foresignal.social.StreamLedger} and {@link ai.foresignal.social.SocialObservability}: collection
 *       health, {@code social_coverage} and the {@code social-attention} observability family, so silence is told
 *       apart from not looking.
 *   <li>{@link ai.foresignal.social.SocialPlatformService} and {@link ai.foresignal.social.XSpend}: the operator's
 *       per-platform choices, quotas and money caps.
 * </ul>
 *
 * <p><strong>Accounts are sources, not people:</strong> handles are kept with posts so analysts can drill down, the
 * {@code authorKey} (an HMAC of the account id with the instance social salt) is what counting uses, and nothing
 * builds a profile of an account.
 */
package ai.foresignal.social;
