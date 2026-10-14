package ai.foresignal.source.spi;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

/**
 * Options of one TCP connection opened through {@link SocketTransport#tcp} or {@link SocketTransport#tcpPublic}
 * (prompts 069 and 082).
 *
 * <p>The connect timeout is fixed at {@link SocketTransport#CONNECT_TIMEOUT} and TCP keepalive is always on; neither
 * is an option.
 *
 * <p><strong>Writing.</strong> A connection's {@link StreamHandle} never has a write method, so the only bytes
 * Foresignal can ever send on a source socket are {@link #onConnect()}, once, right after connecting:
 * <ul>
 *   <li>{@link WriteMode#ON_CONNECT_ONLY} (the default, and {@link #receiveOnlyAfterConnect()}): the
 *       {@code onConnect} bytes, if any, then nothing (gpsd's {@code ?WATCH}, the APRS-IS login line);
 *   <li>{@link WriteMode#NONE} ({@link #receiveOnly()}): nothing at all. An {@code onConnect} write is refused when
 *       the options are built, and the transport never touches the socket's output stream (prompt 082:
 *       {@code local-aprs-{n}} sends no KISS command). The socket is not half-closed either: Direwolf and aprsc read
 *       EOF from a client as a disconnect.
 * </ul>
 *
 * @param silenceTimeout how long the connection may deliver nothing before the transport closes it, so the
 *     supervisor's ladder reconnects; {@link #DEFAULT_SILENCE_TIMEOUT} when null
 * @param onConnect bytes written once right after the connection opens, or null
 * @param writeMode what may be written; {@link WriteMode#ON_CONNECT_ONLY} when null
 */
public record SocketOptions(Duration silenceTimeout, byte[] onConnect, WriteMode writeMode) {

    /** The silence timeout when none is given. */
    public static final Duration DEFAULT_SILENCE_TIMEOUT = Duration.ofSeconds(120);

    /** What a connection may write (prompt 082). */
    public enum WriteMode {
        /** The {@code onConnect} bytes once, if any, and nothing afterwards. */
        ON_CONNECT_ONLY,
        /** Nothing, ever: provably receive-only. */
        NONE
    }

    /**
     * Applies the defaults, refuses a non-positive timeout and an {@code onConnect} write under
     * {@link WriteMode#NONE}, and copies the write.
     *
     * @throws IllegalArgumentException on a non-positive timeout or a write under {@link WriteMode#NONE}
     */
    public SocketOptions {
        silenceTimeout = silenceTimeout == null ? DEFAULT_SILENCE_TIMEOUT : silenceTimeout;
        if (silenceTimeout.isNegative() || silenceTimeout.isZero()) {
            throw new IllegalArgumentException("silenceTimeout must be positive, but was " + silenceTimeout);
        }
        writeMode = writeMode == null ? WriteMode.ON_CONNECT_ONLY : writeMode;
        onConnect = onConnect == null || onConnect.length == 0 ? null : onConnect.clone();
        if (writeMode == WriteMode.NONE && onConnect != null) {
            throw new IllegalArgumentException("A receive-only connection writes nothing, not even on connect");
        }
    }

    /**
     * Options with the default write mode (prompt 069's constructor).
     *
     * @param silenceTimeout the silence timeout, or null for the default
     * @param onConnect bytes written once on connect, or null
     */
    public SocketOptions(Duration silenceTimeout, byte[] onConnect) {
        this(silenceTimeout, onConnect, WriteMode.ON_CONNECT_ONLY);
    }

    /** The default options: a 120 s silence timeout and nothing written on connect. */
    public static SocketOptions defaults() {
        return new SocketOptions(null, null, WriteMode.ON_CONNECT_ONLY);
    }

    /** Provably receive-only options (prompt 082): nothing is ever written, and no write can be added. */
    public static SocketOptions receiveOnly() {
        return new SocketOptions(null, null, WriteMode.NONE);
    }

    /**
     * Receive-only after one write on connect (prompt 082): give the bytes with {@link #withOnConnect}; nothing is
     * written afterwards.
     */
    public static SocketOptions receiveOnlyAfterConnect() {
        return new SocketOptions(null, null, WriteMode.ON_CONNECT_ONLY);
    }

    /** True when nothing at all may be written. */
    public boolean isReceiveOnly() {
        return writeMode == WriteMode.NONE;
    }

    /** These options with another silence timeout. */
    public SocketOptions withSilenceTimeout(Duration timeout) {
        return new SocketOptions(timeout, onConnect, writeMode);
    }

    /**
     * These options with {@code bytes} written on connect.
     *
     * @throws IllegalStateException under {@link WriteMode#NONE}: a receive-only connection rejects any writer
     */
    public SocketOptions withOnConnect(byte[] bytes) {
        if (writeMode == WriteMode.NONE && bytes != null && bytes.length > 0) {
            throw new IllegalStateException("A receive-only connection rejects an onConnect writer");
        }
        return new SocketOptions(silenceTimeout, bytes, writeMode);
    }

    /** The write on connect, copied, or null. */
    @Override
    public byte[] onConnect() {
        return onConnect == null ? null : onConnect.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SocketOptions o && silenceTimeout.equals(o.silenceTimeout)
                && Arrays.equals(onConnect, o.onConnect) && writeMode == o.writeMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(silenceTimeout, Arrays.hashCode(onConnect), writeMode);
    }

    @Override
    public String toString() {
        return "SocketOptions[silenceTimeout=" + silenceTimeout + ", onConnect="
                + (onConnect == null ? "none" : onConnect.length + " bytes") + ", writeMode=" + writeMode + "]";
    }
}
