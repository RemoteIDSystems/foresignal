/**
 * Orbit propagation with Orekit (prompt 054, Weather-Imagery-Design §3 "SGP4" and §4.5 item 3).
 *
 * <ul>
 *   <li>{@link ai.foresignal.orbit.OrekitData}: a private Orekit {@code DataContext} over the pinned,
 *       minimal {@code orekit-data} subset on the classpath (leap seconds and one IERS EOP file), loaded
 *       at first use; Orekit is never touched before a code path that needs it runs, and
 *       {@code DataContext.getDefault()} is never used;
 *   <li>{@link ai.foresignal.orbit.SwathEstimator}: the approximate swath of a SatDump pass without a
 *       usable GeoTIFF, from its TLE by SGP4.
 * </ul>
 *
 * <p>Prompt 064 ({@code celestrak-gp} predicted passes) reuses this setup. Stateless helpers, so the
 * SatDump adapter may call them directly (Architecture §3).
 */
package ai.foresignal.orbit;
