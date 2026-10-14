package ai.foresignal.source.spi;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

/**
 * Options of one TCP connection opened through {@link SocketTransport#tcp} or {@link SocketTransport#tcpPublic}
 * (prompts 069, 082 and 083).
 *
 * <p>The connect timeout defaults to {@link SocketTransport#CONNECT_TIMEOUT}; a source whose upstream is slow to
 * accept may set a longer one ({@link #withConnectTimeout}, prompt 085: {@code ais-kystverket} waits 20 s). TCP
 * keepalive is always on and is not an option.
 *
 * <p><strong>Writing.</strong> A plain connection's {@link StreamHandle} has no write method, so the only bytes
 * Foresignal can send on such a socket are {@link #onConnect()}, once, right after connecting:
 * <ul>
 *   <li>{@link WriteMode#ON_CONNECT_ONLY} (the default, and {@link #receiveOnlyAfterConnect()}): the
 *       {@code onConnect} bytes, if any, then nothing (gpsd's {@code ?WATCH}, the APRS-IS login line);
 *   <li>{@link WriteMode#NONE} ({@link #receiveOnly()}): nothing at all. An {@code onConnect} write is refused when
 *       the options are built, and the transport never touches the socket's output stream (prompt 082:
 *       {@code local-aprs-{n}} sends no KISS command). The socket is not half-closed either: Direwolf and aprsc read
 *       EOF from a client as a disconnect;
 *   <li>{@link WriteMode#GUARDED_COMMANDS} ({@link #commandWriter(CommandGuard)}, prompt 083): the one exception. The
 *       transport returns a {@link CommandHandle} whose {@code send} writes a frame only after the {@link CommandGuard}
 *       allowed it (the MeshCore companion protocol, which must ask the radio for its contacts and messages). No
 *       {@code onConnect} bytes; everything goes through the guard.
 * </ul>
 *
 * @param silenceTimeout how long the connection may deliver nothing before the transport closes it, so the
 *     supervisor's ladder reconnects; {@link #DEFAULT_SILENCE_TIMEOUT} when null
 * @param onConnect bytes written once right after the connection opens, or null
 * @param writeMode what may be written; {@link WriteMode#ON_CONNECT_ONLY} when null
 * @param commandGuard the allowlist of a {@link WriteMode#GUARDED_COMMANDS} connection, else null
 * @param connectTimeout how long a connect may take; {@link SocketTransport#CONNECT_TIMEOUT} when null (prompt 085)
 */
public record SocketOptions(Duration silenceTimeout, byte[] onConnect, WriteMode writeMode, CommandGuard commandGuard,
        Duration connectTimeout) {

    /** The silence timeout when none is given. */
    public static final Duration DEFAULT_SILENCE_TIMEOUT = Duration.ofSeconds(120);

    /** What a connection may write (prompt 082). */
    public enum WriteMode {
        /** The {@code onConnect} bytes once, if any, and nothing afterwards. */
        ON_CONNECT_ONLY,
        /** Nothing, ever: provably receive-only. */
        NONE,
        /** Frames through {@link CommandHandle#send}, each checked by the {@link CommandGuard} (prompt 083). */
        GUARDED_COMMANDS
    }

    /**
     * Applies the defaults, refuses a non-positive timeout, an {@code onConnect} write under {@link WriteMode#NONE} or
     * {@link WriteMode#GUARDED_COMMANDS}, a guarded mode without a guard and a guard in any other mode, and copies the
     * write.
     *
     * @throws IllegalArgumentException on any of those
     */
    public SocketOptions {
        silenceTimeout = silenceTimeout == null ? DEFAULT_SILENCE_TIMEOUT : silenceTimeout;
        if (silenceTimeout.isNegative() || silenceTimeout.isZero()) {
            throw new IllegalArgumentException("silenceTimeout must be positive, but was " + silenceTimeout);
        }
        connectTimeout = connectTimeout == null ? SocketTransport.CONNECT_TIMEOUT : connectTimeout;
        if (connectTimeout.isNegative() || connectTimeout.isZero() || connectTimeout.toMillis() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("connectTimeout must be positive, but was " + connectTimeout);
        }
        writeMode = writeMode == null ? WriteMode.ON_CONNECT_ONLY : writeMode;
        onConnect = onConnect == null || onConnect.length == 0 ? null : onConnect.clone();
        if (writeMode == WriteMode.NONE && onConnect != null) {
            throw new IllegalArgumentException("A receive-only connection writes nothing, not even on connect");
        }
        if (writeMode == WriteMode.GUARDED_COMMANDS) {
            if (commandGuard == null) {
                throw new IllegalArgumentException("A command-writing connection needs a CommandGuard");
            }
            if (onConnect != null) {
                throw new IllegalArgumentException("A command-writing connection sends everything through its guard");
            }
        } else if (commandGuard != null) {
            throw new IllegalArgumentException("Only a GUARDED_COMMANDS connection has a CommandGuard");
        }
    }

    /**
     * Options with the default connect timeout (prompt 083's constructor).
     *
     * @param silenceTimeout the silence timeout, or null for the default
     * @param onConnect bytes written once on connect, or null
     * @param writeMode what may be written
     * @param commandGuard the guard of a command-writing connection, else null
     */
    public SocketOptions(Duration silenceTimeout, byte[] onConnect, WriteMode writeMode, CommandGuard commandGuard) {
        this(silenceTimeout, onConnect, writeMode, commandGuard, null);
    }

    /**
     * Options without a guard (prompt 082's constructor).
     *
     * @param silenceTimeout the silence timeout, or null for the default
     * @param onConnect bytes written once on connect, or null
     * @param writeMode what may be written
     */
    public SocketOptions(Duration silenceTimeout, byte[] onConnect, WriteMode writeMode) {
        this(silenceTimeout, onConnect, writeMode, null);
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

    /**
     * A connection that writes frames only through its {@code guard} (prompt 083): the transport returns a
     * {@link CommandHandle}, and every {@code send} is checked first.
     *
     * @param guard the allowlist; required
     */
    public static SocketOptions commandWriter(CommandGuard guard) {
        return new SocketOptions(null, null, WriteMode.GUARDED_COMMANDS, java.util.Objects.requireNonNull(guard, "guard"));
    }

    /** True when frames are written through a {@link CommandGuard}. */
    public boolean isCommandWriter() {
        return writeMode == WriteMode.GUARDED_COMMANDS;
    }

    /** True when nothing at all may be written. */
    public boolean isReceiveOnly() {
        return writeMode == WriteMode.NONE;
    }

    /** These options with another silence timeout. */
    public SocketOptions withSilenceTimeout(Duration timeout) {
        return new SocketOptions(timeout, onConnect, writeMode, commandGuard, connectTimeout);
    }

    /** These options with another connect timeout (prompt 085). */
    public SocketOptions withConnectTimeout(Duration timeout) {
        return new SocketOptions(silenceTimeout, onConnect, writeMode, commandGuard, timeout);
    }

    /**
     * These options with {@code bytes} written on connect.
     *
     * @throws IllegalStateException under {@link WriteMode#NONE} (a receive-only connection rejects any writer) and
     *     {@link WriteMode#GUARDED_COMMANDS} (everything goes through the guard)
     */
    public SocketOptions withOnConnect(byte[] bytes) {
        if (writeMode == WriteMode.NONE && bytes != null && bytes.length > 0) {
            throw new IllegalStateException("A receive-only connection rejects an onConnect writer");
        }
        if (writeMode == WriteMode.GUARDED_COMMANDS && bytes != null && bytes.length > 0) {
            throw new IllegalStateException("A command-writing connection sends everything through its guard");
        }
        return new SocketOptions(silenceTimeout, bytes, writeMode, commandGuard, connectTimeout);
    }

    /** The write on connect, copied, or null. */
    @Override
    public byte[] onConnect() {
        return onConnect == null ? null : onConnect.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SocketOptions o && silenceTimeout.equals(o.silenceTimeout)
                && Arrays.equals(onConnect, o.onConnect) && writeMode == o.writeMode
                && Objects.equals(commandGuard, o.commandGuard) && connectTimeout.equals(o.connectTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(silenceTimeout, Arrays.hashCode(onConnect), writeMode, commandGuard, connectTimeout);
    }

    @Override
    public String toString() {
        return "SocketOptions[silenceTimeout=" + silenceTimeout + ", connectTimeout=" + connectTimeout + ", onConnect="
                + (onConnect == null ? "none" : onConnect.length + " bytes") + ", writeMode=" + writeMode
                + (commandGuard == null ? "" : ", commandGuard=" + commandGuard.getClass().getSimpleName()) + "]";
    }
}
