package ai.foresignal.source.adapters.remoteid;

import ai.foresignal.mqtt.MqttConnection;
import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Opens one MQTT subscriber connection for {@code local-remoteid} through 060's {@link MqttConnections} (prompt 086, prompt 083's pattern; adapters do not import each other), paying a
 * budget unit for the CONNECT and one for each SUBSCRIBE, and returns its {@link StreamHandle}.
 *
 * <p>The session must be a non-reconnecting subscriber ({@code MqttSessionOptions.subscriber}): when the connection
 * drops, the handle reports it through {@link StreamHandle#onClose} and the streaming supervisor reconnects on the
 * descriptor's ladder, so every attempt is budgeted. Not an adapter.
 */
final class RemoteIdMqttSubscriber {

    /** How long the CONNECT and each SUBSCRIBE may take. */
    static final Duration TIMEOUT = Duration.ofSeconds(20);

    private RemoteIdMqttSubscriber() {}

    /**
     * Connects and subscribes.
     *
     * @param ctx the stream context that pays for the CONNECT and the SUBSCRIBEs
     * @param connections the shared MQTT connections
     * @param spec the connection; its options must not reconnect
     * @param filters the subscriptions, each charged
     * @param handler receives every message, on a virtual thread
     * @return the running handle
     * @throws AdapterException when the budget is spent, the connect fails, or a subscription is refused
     */
    static StreamHandle open(StreamContext ctx, MqttConnections connections, MqttConnectionSpec spec,
            List<String> filters, Consumer<MqttInbound> handler) throws AdapterException {
        if (spec.options().autoReconnect()) {
            throw new IllegalArgumentException("the Remote ID source's MQTT session must leave reconnecting to the supervisor");
        }
        ctx.chargeRequest("mqtt-connect " + spec.endpoint().host() + ":" + spec.endpoint().port());
        MqttConnection connection = connections.create(spec);
        Handle handle = new Handle(connections, spec.key());
        connection.onDisconnected(handle::ended);
        connection.start();
        try {
            await(connection.connectedFuture());
            for (String filter : filters) {
                ctx.chargeRequest("mqtt-subscribe " + filter);
                await(connection.subscribeAcknowledged(filter, message -> {
                    if (message.payload() != null) {
                        ctx.reportBytes(message.payload().length);
                    }
                    handler.accept(message);
                }));
            }
        } catch (AdapterException | RuntimeException e) {
            connections.close(spec.key());
            throw e;
        }
        ctx.connected();
        return handle;
    }

    private static void await(java.util.concurrent.CompletableFuture<Void> future) throws AdapterException {
        try {
            future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AdapterException("interrupted while connecting to the MQTT broker", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new AdapterException("MQTT broker: " + cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new AdapterException("the MQTT broker did not answer within " + TIMEOUT.toSeconds() + " s", e);
        }
    }

    /** The handle of one connection: closing it closes the connection; a drop is reported once. */
    static final class Handle implements StreamHandle {

        private final MqttConnections connections;
        private final String key;
        private final AtomicBoolean ended = new AtomicBoolean();
        private volatile Consumer<Throwable> callback;
        private volatile Throwable cause;

        Handle(MqttConnections connections, String key) {
            this.connections = connections;
            this.key = key;
        }

        void ended(String reason) {
            if (ended.compareAndSet(false, true)) {
                cause = new java.io.IOException("MQTT connection lost: " + reason);
                Consumer<Throwable> listener = callback;
                if (listener != null) {
                    listener.accept(cause);
                }
            }
        }

        @Override
        public void close() {
            ended.set(true);
            connections.close(key);
        }

        @Override
        public boolean isRunning() {
            return !ended.get();
        }

        @Override
        public void onClose(Consumer<Throwable> listener) {
            callback = listener;
            if (ended.get() && cause != null && listener != null) {
                listener.accept(cause);
            }
        }
    }
}
