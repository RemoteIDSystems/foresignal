/**
 * APRS (prompt 082), shared format code for {@code aprs-is} and the {@code local-aprs-{n}} slots: the in-house TNC2
 * parser ({@link ai.foresignal.source.formats.aprs.AprsParser}, after APRS Protocol Reference 1.0.1 and the 1.1/1.2
 * addenda), the per-connection record mapping with per-station merge ({@link
 * ai.foresignal.source.formats.aprs.AprsRecords}) and the cross-source duplicate filter ({@link
 * ai.foresignal.source.formats.aprs.AprsPacketDedup}). No GPL or unlicensed APRS library is used.
 */
package ai.foresignal.source.formats.aprs;
