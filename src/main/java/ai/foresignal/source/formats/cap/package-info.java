/**
 * The shared OASIS CAP 1.2 support of Foresignal (prompt 067): {@link CapParser} (hardened StAX, CAP 1.2
 * and 1.1), {@link CapGeometry} (polygons, circles), {@link CapThreadResolver} (one record per alert
 * thread) and {@link CapRecordMapper} (the alert time mapping, {@code state}, the payload), configured per
 * source by a {@link CapMappingProfile}.
 *
 * <p>Used by prompt 024's {@code cap} ingest format and by {@code tsunami-warnings}; prompt 089's CAP family
 * uses it too. Shared format parsers live under {@code ai.foresignal.source.formats.*} (Conventions,
 * "Shared parsers"), and there is only one CAP parser in the codebase.
 */
package ai.foresignal.source.formats.cap;
