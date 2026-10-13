/**
 * The shared, supervised MQTT client (prompt 060), built on the HiveMQ MQTT Client.
 *
 * <p>{@link ai.foresignal.mqtt.MqttConnections} holds one connection per owner (a channel of type
 * {@code mqtt}; later the MQTT source adapters of prompts 083 and 085), each with automatic reconnect,
 * a retained Last Will on {@code {prefix}/status}, TLS with hostname verification and the outbound URL
 * policy applied to the broker host. {@link ai.foresignal.mqtt.channel} builds the notification
 * channel, the retained state topics and Home Assistant discovery on top of it.
 */
package ai.foresignal.mqtt;
