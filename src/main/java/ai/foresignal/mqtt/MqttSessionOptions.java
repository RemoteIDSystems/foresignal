package ai.foresignal.mqtt;

import java.time.Duration;

/**
 * How one supervised connection behaves on the wire (prompt 083). The defaults are prompt 060's publisher session;
 * {@link #subscriber(Duration)} is the session of an MQTT <em>source</em> (prompt 083's mesh sources), which must not
 * publish anything, keep no state on the broker and leave reconnecting to the streaming supervisor, so every CONNECT
 * is paid for from the source's budget.
 *
 * @param keepAlive the keepalive, or null for {@link MqttProperties#keepAlive()}
 * @param cleanStart true for a clean session (MQTT 5 clean start with session expiry 0; 3.1.1 {@code cleanSession})
 * @param subscribeQos the QoS of every SUBSCRIBE, 0 or 1
 * @param autoReconnect false when a lost connection is reported through
 *     {@link MqttConnection#onDisconnected} and never retried by the connection itself
 */
public record MqttSessionOptions(Duration keepAlive, boolean cleanStart, int subscribeQos, boolean autoReconnect) {

    /** Checks the QoS and the keepalive. */
    public MqttSessionOptions {
        if (subscribeQos < 0 || subscribeQos > 1) {
            throw new IllegalArgumentException("subscribeQos must be 0 or 1, but was " + subscribeQos);
        }
        if (keepAlive != null && (keepAlive.isNegative() || keepAlive.toSeconds() > 65_535)) {
            throw new IllegalArgumentException("keepAlive must be 0 to 65535 s, but was " + keepAlive);
        }
    }

    /** Prompt 060's session: the configured keepalive, a persistent session, QoS 1 subscriptions, reconnecting. */
    public static MqttSessionOptions defaults() {
        return new MqttSessionOptions(null, false, 1, true);
    }

    /**
     * A source's subscriber session (prompt 083): {@code keepAlive}, a clean session, QoS 0 subscriptions and no
     * reconnect of its own.
     */
    public static MqttSessionOptions subscriber(Duration keepAlive) {
        return new MqttSessionOptions(keepAlive, true, 0, false);
    }
}
