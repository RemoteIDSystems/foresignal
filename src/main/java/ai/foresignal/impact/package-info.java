/**
 * Impact projection (prompt 127, Intelligence-Design §10): hazard forecasts Foresignal already ingests become
 * probabilistic, scored impact statements. {@code P(impact) = P(hazard at the asset or region) × P(impact | hazard
 * class)}: the first factor is the producer's published probability ({@link ai.foresignal.impact.hazard}), the second a
 * historical conditional rate with its interval and sample size ({@link ai.foresignal.impact.rate}). Each statement is
 * a {@code projected-impact} finding with its exposed assets and dependency paths, and a binary ledger question that
 * Foresignal resolves from its own outage, aviation and gauge sources. Cones and categorical labels are never
 * probabilities. Every output is labeled "modelled; heuristic links may be wrong; not an official warning".
 */
package ai.foresignal.impact;
