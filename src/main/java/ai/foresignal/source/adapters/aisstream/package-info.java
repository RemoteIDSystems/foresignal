/**
 * {@code aisstream}: global AIS from aisstream.io's WebSocket stream (prompt 063), the first streaming
 * adapter and the reference for the later ones (Adapter Guide, "Streaming adapters").
 *
 * <ul>
 *   <li>{@link ai.foresignal.source.adapters.aisstream.AisStreamAdapter}: the descriptor and the connection;
 *   <li>{@link ai.foresignal.source.adapters.aisstream.AisStreamSession}: one connection's listener and
 *       handle (subscription, frames, liveness, errors, watchdog);
 *   <li>{@link ai.foresignal.source.adapters.aisstream.AisStreamMessageParser}: the pure parser;
 *   <li>{@link ai.foresignal.source.adapters.aisstream.VesselStateStore}: the per-MMSI merge into complete
 *       snapshots;
 *   <li>{@link ai.foresignal.source.adapters.aisstream.AisStreamHealth}: the 120 s silence rule and the
 *       health details.
 * </ul>
 */
package ai.foresignal.source.adapters.aisstream;
