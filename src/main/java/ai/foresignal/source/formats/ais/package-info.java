/**
 * Vessel descriptor defaults (prompt 085), shared by every vessel source: {@code aisstream} (063),
 * {@code ais-kystverket}, {@code ais-digitraffic} and the {@code local-ais-{n}} slots. The AIVDM codec itself is a
 * platform piece in {@code ai.foresignal.codec.ais}, because prompt 110's NMEA sink uses it too; this package holds
 * only what the adapters share (Conventions, "Shared parsers": adapters never import each other).
 */
package ai.foresignal.source.formats.ais;
