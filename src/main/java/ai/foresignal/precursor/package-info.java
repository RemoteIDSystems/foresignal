/**
 * Precursor mining and the precursor library (prompt 120, Intelligence-Design §5.5): candidate patterns are generated
 * automatically (event-family pairs per region and analysis-series pairs) or registered by the operator, tested on a
 * discovery window with false-discovery control, confirmed on a later, disjoint validation window, and kept in a
 * library with a lifecycle ({@code candidate}, {@code validated}, {@code stale}, {@code retired}). A live watch writes
 * {@code precursor-active} findings when a validated precursor's antecedent occurs.
 *
 * <p>Every output states association in stored data, not causation; reverse-direction tests are always reported.
 */
package ai.foresignal.precursor;
