/**
 * Aviation datalink metadata (prompt 086): {@link ai.foresignal.source.formats.acars.AcarsMetadataMapper} maps
 * acarsdec, dumpvdl2 and dumphfdl JSON to {@code acars-message} records from an allowlist that never includes message
 * text, and {@link ai.foresignal.source.formats.acars.JsonObjectStream} cuts a TCP stream into JSON objects.
 */
package ai.foresignal.source.formats.acars;
