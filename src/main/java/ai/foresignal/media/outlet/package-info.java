/**
 * The news outlet registry and its ratings (prompt 133, Intelligence-Design §12.5).
 *
 * <p>The prompt names {@code ai.foresignal.media}; that package is already the 043 image store, so the registry lives
 * in this subpackage.
 *
 * <ul>
 *   <li>{@link ai.foresignal.media.outlet.OutletRegistry}: outlets ({@code media_outlet}) by domain, unknown domains
 *       added as {@code unrated}, wires mapped to their own 116 originator, operator edits and merges;
 *   <li>{@link ai.foresignal.media.outlet.MediaProviders} and {@link ai.foresignal.media.outlet.RatingImportService}:
 *       the declared rating providers ({@code wikidata}, {@code partisan-web}, {@code allsides}, {@code mbfc},
 *       {@code operator}) with their license, egress and the admin license acknowledgement gate;
 *   <li>{@link ai.foresignal.media.outlet.Consensus}: the per-dimension consensus (median, disagreement, bucket);
 *   <li>{@link ai.foresignal.media.outlet.NewsReliability}: outlet factuality consensus to a 109 reliability grade
 *       ({@code media.reliabilityMap}).
 * </ul>
 *
 * <p><strong>Ratings are imported, never invented</strong> (NEWS-1): every {@code media_rating} row names its
 * provider, license, attribution and date, and nothing in Foresignal (no model, no heuristic) writes a rating.
 */
package ai.foresignal.media.outlet;
