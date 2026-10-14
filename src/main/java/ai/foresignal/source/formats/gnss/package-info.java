/**
 * GNSS integrity judgment (prompt 086): {@link ai.foresignal.source.formats.gnss.GnssIntegrityEvaluator} turns gpsd's
 * TPV, SKY, PPS/TOFF reports and u-blox monitor readings into the {@code gnss-integrity} state of
 * {@code local-gpsd-{n}}. Pure and deterministic; no I/O and no clock.
 */
package ai.foresignal.source.formats.gnss;
