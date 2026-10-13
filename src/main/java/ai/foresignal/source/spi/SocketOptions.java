package ai.foresignal.source.spi;

import java.time.Duration;
import java.util.Arrays;

/**
 * Options of one TCP connection opened through {@link SocketTransport#tcp} (prompt 069).
 *
 * <p>The connect timeout is fixed at {@link SocketTransport#CONNECT_TIMEOUT} and TCP keepalive is always
 * on; neither is an option.
 *
 * @param silenceTimeout how long the connection may deliver nothing before the transport closes it, so the
 *     supervisor's ladder reconnects; {@link #DEFAULT_SILENCE_TIMEOUT} when null
 * @param onConnect bytes written once right after the connection opens (gpsd's {@code ?WATCH}), or null
 */
public record SocketOptions(Duration silenceTimeout, byte[] onConnect) {

    /** The silence timeout when none is given. */
    public static final Duration DEFAULT_SILENCE_TIMEOUT = Duration.ofSeconds(120);

    /** Applies the default timeout, refuses a non-positive one and copies the write. */
    public SocketOptions {
        silenceTimeout = silenceTimeout == null ? DEFAULT_SILENCE_TIMEOUT : silenceTimeout;
        if (silenceTimeout.isNegative() || silenceTimeout.isZero()) {
            throw new IllegalArgumentException("silenceTimeout must be positive, but was " + silenceTimeout);
        }
        onConnect = onConnect == null || onConnect.length == 0 ? null : onConnect.clone();
    }

    /** The default options: a 120 s silence timeout and nothing written on connect. */
    public static SocketOptions defaults() {
        return new SocketOptions(null, null);
    }

    /** These options with another silence timeout. */
    public SocketOptions withSilenceTimeout(Duration timeout) {
        return new SocketOptions(timeout, onConnect);
    }

    /** These options with {@code bytes} written on connect. */
    public SocketOptions withOnConnect(byte[] bytes) {
        return new SocketOptions(silenceTimeout, bytes);
    }

    /** The write on connect, copied, or null. */
    @Override
    public byte[] onConnect() {
        return onConnect == null ? null : onConnect.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SocketOptions o && silenceTimeout.equals(o.silenceTimeout)
                && Arrays.equals(onConnect, o.onConnect);
    }

    @Override
    public int hashCode() {
        return 31 * silenceTimeout.hashCode() + Arrays.hashCode(onConnect);
    }

    @Override
    public String toString() {
        return "SocketOptions[silenceTimeout=" + silenceTimeout + ", onConnect="
                + (onConnect == null ? "none" : onConnect.length + " bytes") + "]";
    }
}
