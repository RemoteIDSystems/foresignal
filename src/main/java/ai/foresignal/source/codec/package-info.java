/**
 * The socket codec SPI of prompt 069: pure decoders that turn the frames of a LAN socket into records.
 *
 * <ul>
 *   <li>{@link ai.foresignal.source.codec.SocketCodec}: one wire format, with its framing and an optional
 *       write on connect;
 *   <li>{@link ai.foresignal.source.codec.CodecSession}: the per-connection state (for example SBS's
 *       per-hex merge), created afresh for every connection;
 *   <li>{@link ai.foresignal.source.codec.CodecRegistry}: every codec bean, by id;
 *   <li>the first codecs: {@link ai.foresignal.source.codec.Sbs1Codec} ({@code sbs1}) and
 *       {@link ai.foresignal.source.codec.GpsdCodec} ({@code gpsd}). Prompt 085 added
 *       {@link ai.foresignal.source.codec.AivdmSocketCodec} ({@code aivdm}, AIS NMEA over the shared codec in
 *       {@code ai.foresignal.codec.ais}), 082 {@code kiss-ax25}, and 086
 *       {@link ai.foresignal.source.codec.AcarsJsonCodec} ({@code acars-json}, aviation datalink metadata, never message
 *       text) with the gpsd write allowlist {@link ai.foresignal.source.codec.GpsdCommandGuard} and the UBX monitor
 *       reader {@link ai.foresignal.source.codec.UbxMonParser}; prompt 077 added {@link ai.foresignal.source.codec.AutoRxPayloadCodec}
 *       ({@code sonde-udp}, radiosonde_auto_rx datagrams), and prompt 083
 *       {@link ai.foresignal.source.codec.MeshCoreCompanionCodec} ({@code meshcore-companion}, MeshCore WiFi companion
 *       frames).
 * </ul>
 *
 * <p>Codecs are shared format code, so they live here rather than under {@code adapters} (Conventions,
 * "Shared parsers"): adapters import them without importing each other. A codec does no I/O and reads no
 * clock; it is fixture-testable on its own.
 */
package ai.foresignal.source.codec;
