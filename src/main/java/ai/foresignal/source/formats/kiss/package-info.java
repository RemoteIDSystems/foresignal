/**
 * KISS framing and AX.25 UI frames (prompt 082), shared format code for the {@code kiss-ax25} socket codec: a TNC
 * such as Direwolf serves received packets as KISS over TCP ({@code KISSPORT}), and {@link
 * ai.foresignal.source.formats.kiss.Ax25UiDecoder} turns an AX.25 UI frame into the TNC2 text that the APRS parser
 * reads. Pure: no I/O, no clock, no Spring.
 */
package ai.foresignal.source.formats.kiss;
