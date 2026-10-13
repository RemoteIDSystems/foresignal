/**
 * The {@code mqtt} notification channel (prompt 060) on top of {@link ai.foresignal.mqtt.MqttConnections}:
 * event deliveries through the 018 pipeline, retained state topics ({@link MqttStatePublisher}) and Home
 * Assistant MQTT discovery ({@link HaDiscoveryPublisher}), with one supervised connection per enabled
 * channel ({@link MqttChannelManager}).
 */
package ai.foresignal.mqtt.channel;
