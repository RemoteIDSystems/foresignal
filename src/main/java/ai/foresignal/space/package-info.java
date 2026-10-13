/**
 * Space data helpers (prompt 064).
 *
 * <ul>
 *   <li>{@link ai.foresignal.space.OmmElements}: one CCSDS OMM element set as CelesTrak publishes it;
 *   <li>{@link ai.foresignal.space.OmmFormats}: the CelesTrak-compatible output formats (TLE with checksums,
 *       CSV, JSON, OMM XML, OMM KVN);
 *   <li>{@link ai.foresignal.space.PassPredictor}: Orekit SGP4 pass prediction over a ground
 *       {@link ai.foresignal.space.Observer}, reusing {@link ai.foresignal.orbit.OrekitData};
 *   <li>{@link ai.foresignal.space.GroundTracks}: a pass's ground track as a geometry;
 *   <li>{@link ai.foresignal.space.CelestrakElementsService}: the current {@code celestrak-gp} heads, also the
 *       {@link ai.foresignal.source.spi.OrbitalElements} adapters read;
 *   <li>{@link ai.foresignal.space.OrekitSelfTest}: the startup check of a bundled ISS pass.
 * </ul>
 *
 * <p>The helpers are stateless, so adapters (a SatDump station) may call them directly, as they call
 * {@code ai.foresignal.orbit} (Architecture §3).
 */
package ai.foresignal.space;
