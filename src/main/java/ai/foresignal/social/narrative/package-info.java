/**
 * Narratives, claims and media (prompt 136, Intelligence-Design §12.7): social posts turned into the units analysts
 * reason with.
 *
 * <ul>
 *   <li>{@link ai.foresignal.social.narrative.NarrativeClusterer} 1.0.0: the 116 {@code social-narrative} family; posts
 *       join a narrative by repost, quote, shared link, shared media or similar text, and its evidence groups count
 *       independent origins ({@link ai.foresignal.social.narrative.NarrativeGrouper}), so 50,000 posts that descend
 *       from three reports count as three.
 *   <li>{@link ai.foresignal.social.narrative.ClaimExtractor} 1.0.0 and
 *       {@link ai.foresignal.social.narrative.LlmClaimExtraction}: what, where, when and who, deterministic first, with
 *       optional budgeted LLM extraction for large narratives; claims are graded 125 evidence items
 *       ({@link ai.foresignal.social.narrative.SocialEvidenceGrades}).
 *   <li>{@link ai.foresignal.social.narrative.MediaFingerprinter} 1.0.0 with {@link ai.foresignal.social.narrative.Pdq}
 *       (a port of Meta's PDQ) and {@link ai.foresignal.social.narrative.MediaReuseDetector}: reused images.
 *   <li>{@link ai.foresignal.social.narrative.PropagationTracer} 1.0.0: spread across reposts, platforms and news;
 *       earliest observed post per platform, never an originator.
 *   <li>{@link ai.foresignal.social.narrative.CoordinationDetector} 1.0.0: coordinated-amplification indicators as
 *       statistics against null models, never labels on accounts.
 * </ul>
 *
 * <p><strong>Claims are reports, not facts:</strong> nothing here marks a claim true or false; statuses come from 137's
 * comparison with independent evidence.
 */
package ai.foresignal.social.narrative;
