/**
 * Social–physical correlation (prompt 137): what people say compared with what Foresignal observes.
 *
 * <ul>
 *   <li>{@link ai.foresignal.social.correlation.SocialPhysicalComparator} 1.0.0 compares each significant claim with
 *       independent (non-social) physical evidence in the same place and window, respecting detectability:
 *       {@code corroborated} (with measured lead times), {@code unobserved} (only when {@code canSeeAbsence} is ok),
 *       {@code contradicted}, {@code insufficient-coverage} or {@code pending}; {@link
 *       ai.foresignal.social.correlation.ComparisonService} keeps the revisions, sets claim and narrative statuses and
 *       opens and resolves the 122 ledger questions of the {@code social-claim} forecaster;</li>
 *   <li>{@link ai.foresignal.social.correlation.AttentionGapDetector} 1.0.0 records {@code attention-gap} findings for
 *       significant physical events that drew far less discussion than similar past events, only where social
 *       coverage is adequate;</li>
 *   <li>{@link ai.foresignal.social.correlation.SocialCueService} runs budgeted, targeted searches cued by physical
 *       triggers or by an agent; cue results are a targeted sample that never feeds baselines and never corroborates
 *       its own trigger;</li>
 *   <li>{@link ai.foresignal.social.correlation.LeadTimeStatistics} aggregates measured lead times; only comparisons
 *       evaluated after the training cut-off support "leads by" statements (SOC-5).</li>
 * </ul>
 *
 * Social activity before a physical event is reported as timing, never as foreknowledge.
 */
package ai.foresignal.social.correlation;
