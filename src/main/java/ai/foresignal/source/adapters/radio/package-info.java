/**
 * The radio sources.
 *
 * <ul>
 *   <li>The Radio Browser station directory ({@code radio-browser}, prompt 075): mirror discovery with an SSRF guard
 *       and failover, a paged crawl of stations with a position, and absence tombstones after untruncated crawls.
 *       Stream URLs are links for clients; Foresignal never fetches a stream.
 *   <li>The amateur-radio sources of prompt 082: {@code pskreporter-ft8} (PSK Reporter spots, hardened XML),
 *       {@code kiwisdr} (the public KiwiSDR list, a lenient JavaScript literal), {@code openmhz-systems} (blocked by a
 *       challenge, reported as such), the opt-in, receive-only {@code aprs-is} feed over raw TCP to a fixed public
 *       host, and the provably receive-only {@code local-aprs-{n}} slots reading the operator's KISS TNC. The
 *       receiving station of every spot or packet is an {@code Observer}, never payload.
 * </ul>
 * The KISS/AX.25 decoders and the APRS parser are shared format code in {@code source.formats.kiss} and
 * {@code source.formats.aprs}.
 */
package ai.foresignal.source.adapters.radio;
