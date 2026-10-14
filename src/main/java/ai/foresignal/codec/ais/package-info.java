/**
 * The shared AIVDM codec (prompt 085): Foresignal's in-house reader and writer of AIS radio messages as NMEA 0183
 * {@code !AIVDM}/{@code !AIVDO} sentences, after ITU-R M.1371-5 and the gpsd AIVDM/AIVDO protocol notes.
 *
 * <p>A platform piece, not an adapter: the socket sources {@code ais-kystverket} and {@code local-ais-{n}} decode with
 * it (through the {@code aivdm} socket codec in {@code ai.foresignal.source.codec}), {@code ais-digitraffic} merges
 * its JSON through the same {@link ai.foresignal.codec.ais.VesselStateMerger}, and prompt 110's NMEA sink encodes
 * with {@link ai.foresignal.codec.ais.AisEncoder} and {@link ai.foresignal.codec.ais.NmeaWriter}. It lives outside
 * the adapter packages so adapters and sinks can share it (Adapter Guide, "Shared codecs").
 *
 * <ul>
 *   <li>{@link ai.foresignal.codec.ais.TagBlock}: IEC 61162-450 TAG blocks ({@code \c:…,s:…*hh\});
 *   <li>{@link ai.foresignal.codec.ais.NmeaSentence}: one sentence with its checksum;
 *   <li>{@link ai.foresignal.codec.ais.FragmentAssembler}: multi-sentence messages;
 *   <li>{@link ai.foresignal.codec.ais.SixBit}: payload armoring and bit fields;
 *   <li>{@link ai.foresignal.codec.ais.AisMessage} and {@link ai.foresignal.codec.ais.AisDecoder}: the message types
 *       Foresignal uses, with every "not available" sentinel turned into an absent value (adapter rule 11);
 *   <li>{@link ai.foresignal.codec.ais.AisEncoder} and {@link ai.foresignal.codec.ais.NmeaWriter}: the reverse
 *       direction for types 1, 5, 18 and 24;
 *   <li>{@link ai.foresignal.codec.ais.VesselStateMerger}: per-MMSI merge into complete vessel snapshots with
 *       prompt 063's payload keys (adapter rule 7).
 * </ul>
 *
 * <p>No third-party AIS library is used: {@code tbsalling/aismessages} is not under a free license, and the
 * encoder and the sentinel handling must follow Foresignal's own rules.
 */
package ai.foresignal.codec.ais;
