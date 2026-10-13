package ai.foresignal.source.codec;

import ai.foresignal.source.spi.Framing;

/**
 * One wire format a LAN socket source speaks (prompt 069): Spring beans collected by {@link CodecRegistry}.
 *
 * <p>A codec is stateless; everything that lives as long as a connection lives in the {@link CodecSession}
 * it creates.
 */
public interface SocketCodec {

    /** The stable id, kebab-case ({@code sbs1}, {@code gpsd}, {@code aivdm}). */
    String id();

    /** How the transport cuts the byte stream into frames for this codec. */
    Framing framing();

    /** A new session for one connection. */
    CodecSession newSession(CodecContext context);

    /**
     * Bytes to write right after a TCP connection opens (gpsd's {@code ?WATCH}), or null for none.
     */
    default byte[] onConnect() {
        return null;
    }
}
