/**
 * abuse.ch threat feeds (prompt 076): Feodo Tracker botnet C2 servers ({@code abusech-feodo}, keyless, placed at the
 * country label point) and URLhaus malware URLs ({@code abusech-urlhaus}, needs a free Auth-Key, no geometry).
 *
 * <p>Malware URLs are hostile: they are stored as data, never fetched, never rendered as links ({@code fieldSpecs}
 * {@code noLink}) and reach an LLM only inside an untrusted block (prompt 028).
 */
package ai.foresignal.source.adapters.abusech;
