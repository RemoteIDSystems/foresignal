/**
 * The shared {@code radiosonde} record contract (prompt 077): {@code sondehub} and the {@code sonde-udp} codec of
 * {@code local-sonde} build their records with {@link ai.foresignal.source.formats.sonde.RadiosondeRecords}, so the
 * two sources serve one payload vocabulary without importing each other (Conventions, "Shared parsers").
 */
package ai.foresignal.source.formats.sonde;
