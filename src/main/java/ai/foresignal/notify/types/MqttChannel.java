package ai.foresignal.notify.types;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttMessage;
import ai.foresignal.mqtt.channel.MqttChannelConfig;
import ai.foresignal.mqtt.channel.MqttPayloads;
import ai.foresignal.mqtt.channel.MqttTopics;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.SpecialPurposeAddresses;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.ChannelWarning;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code mqtt}: publishes events to the operator's MQTT broker (prompt 060).
 *
 * <p>Each delivery is one PUBLISH on the channel's supervised connection, through the ordinary 018 pipeline
 * (delivery rows, the retry ladder, 50 messages per second per channel); it succeeds on PUBACK (QoS 1) or a
 * completed write (QoS 0). Topics (see {@link MqttTopics}): a fired alert (or a digest) on
 * {@code {prefix}/alerts/{severity}}, acknowledgements, snoozes and resolutions on
 * {@code {prefix}/alerts/updates}, a briefing run retained on {@code {prefix}/briefings/{briefingId}}, system
 * events on {@code {prefix}/system/events} and the channel test on {@code {prefix}/test}. Non-retained
 * messages carry the {@code messageExpiry}; every payload is UTF-8 JSON ({@link MqttPayloads}) with the
 * MQTT 5 content type {@code application/json}. Lifecycle updates are follow-ups.
 *
 * <p>The retained state topics and Home Assistant discovery are not deliveries; they are published by
 * {@code MqttStatePublisher} and {@code HaDiscoveryPublisher} on the same connection.
 *
 * <p>Warning {@code MQTT_INSECURE_PUBLIC_BROKER}: the broker host resolves to a public address and the
 * channel uses no TLS ({@code mqtt://}, {@code ws://}) or no credentials. It is returned with the channel
 * and never refuses it.
 */
@Component
public class MqttChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "mqtt";

    /** The warning about a public broker without TLS or credentials. */
    public static final String INSECURE_PUBLIC_BROKER = "MQTT_INSECURE_PUBLIC_BROKER";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "MQTT",
            "Publishes alerts, briefings, system events, source health and a live summary to an MQTT broker,"
                    + " with a Last Will and optional Home Assistant discovery",
            List.of(ChannelFieldSpec.required("brokerUrl", ChannelFieldSpec.STRING,
                            "Broker URL: mqtt://, mqtts://, ws:// or wss://, e.g. mqtts://broker.lan:8883"),
                    ChannelFieldSpec.optional("topicPrefix", ChannelFieldSpec.STRING,
                            "Topic prefix, 1-64 of A-Z a-z 0-9 _ - /, no wildcards", MqttChannelConfig.DEFAULT_PREFIX),
                    ChannelFieldSpec.optional("qos", ChannelFieldSpec.INTEGER, "QoS of event messages: 0 or 1", 1),
                    ChannelFieldSpec.choice("protocolVersion", false, "MQTT protocol version", "5",
                            List.of("5", "3.1.1")),
                    ChannelFieldSpec.optional("messageExpiry", ChannelFieldSpec.STRING,
                            "Message expiry of non-retained event messages (MQTT 5)", "24h"),
                    ChannelFieldSpec.optional("payloadTemplate", ChannelFieldSpec.STRING,
                            "JSON payload with JSON-escaped {{…}} placeholders, like the webhook bodyTemplate", null),
                    ChannelFieldSpec.optional("publishHealth", ChannelFieldSpec.BOOLEAN,
                            "Publish source health on {prefix}/sources/{sourceId}/health", true),
                    ChannelFieldSpec.optional("publishSummary", ChannelFieldSpec.BOOLEAN,
                            "Publish the glance on {prefix}/summary", true),
                    ChannelFieldSpec.optional("summaryInterval", ChannelFieldSpec.STRING,
                            "How often the summary is checked; it is published only when it changed", "60s"),
                    ChannelFieldSpec.optional("haDiscovery", ChannelFieldSpec.BOOLEAN,
                            "Publish Home Assistant MQTT discovery", false),
                    ChannelFieldSpec.optional("haPrefix", ChannelFieldSpec.STRING, "Home Assistant discovery prefix",
                            MqttChannelConfig.DEFAULT_HA_PREFIX),
                    ChannelFieldSpec.optional("haMaxRules", ChannelFieldSpec.INTEGER,
                            "Most rules given a problem sensor (0-500)", 50),
                    ChannelFieldSpec.optional("haMaxSources", ChannelFieldSpec.INTEGER,
                            "Most sources given a problem sensor (0-500)", 100),
                    ChannelFieldSpec.optional("haRetainDiscovery", ChannelFieldSpec.BOOLEAN,
                            "Retain the discovery config (Home Assistant prefers republishing on its birth message)",
                            false)),
            List.of(ChannelFieldSpec.optional("username", ChannelFieldSpec.STRING, "Broker user name", null),
                    ChannelFieldSpec.optional("password", ChannelFieldSpec.STRING, "Broker password", null),
                    ChannelFieldSpec.optional("caCertPem", ChannelFieldSpec.STRING,
                            "Extra trusted CA certificate(s) in PEM for mqtts:// and wss://", null)),
            null);

    private final OutboundUrlPolicy policy;

    /** Creates the type; {@code policy} classifies the broker host for the insecure-broker warning. */
    @Autowired
    public MqttChannel(OutboundUrlPolicy policy) {
        this.policy = policy;
    }

    /** A type without the warning check (unit tests). */
    public MqttChannel() {
        this(null);
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ChannelTypeSpec spec() {
        return SPEC;
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perSecond(50);
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        return MqttChannelConfig.validate(config, secrets);
    }

    @Override
    public List<ChannelWarning> warnings(Map<String, Object> config, List<String> secretFields) {
        if (policy == null || !(config.get("brokerUrl") instanceof String url)) {
            return List.of();
        }
        MqttEndpoint endpoint;
        try {
            endpoint = MqttEndpoint.parse(url);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        boolean credentials = secretFields != null && secretFields.contains("username");
        if (endpoint.tls() && credentials) {
            return List.of();
        }
        if (!isPublic(endpoint.host())) {
            return List.of();
        }
        String why = !endpoint.tls() ? "without TLS" : "without credentials";
        return List.of(new ChannelWarning(INSECURE_PUBLIC_BROKER, "The broker " + endpoint.host()
                + " is on a public address and the channel connects " + why
                + "; anyone on the path can read or forge Foresignal's messages. Use mqtts:// or wss:// with a"
                + " username and password."));
    }

    /** True when {@code host} resolves to at least one public address. */
    boolean isPublic(String host) {
        OutboundUrlPolicy.Result result = policy.checkHost(host, OutboundUrlPolicy.Profile.LAN_ALLOWED);
        if (!(result instanceof OutboundUrlPolicy.Allowed allowed)) {
            return false;
        }
        for (InetAddress address : allowed.pinnedAddresses()) {
            if (SpecialPurposeAddresses.classify(address).addressClass()
                    == SpecialPurposeAddresses.AddressClass.GLOBAL) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        MqttChannelConfig c = MqttChannelConfig.of(s.config());
        String prefix = c.topicPrefix();
        String topic;
        boolean retain = false;
        Long expiry = c.messageExpiry().toSeconds();
        if (NotificationMessage.TEST.equals(m.event())) {
            topic = MqttTopics.test(prefix);
        } else if (MqttPayloads.isAlertUpdate(m)) {
            topic = MqttTopics.alertUpdates(prefix);
        } else if (NotificationMessage.ALERT.equals(m.event())) {
            topic = MqttTopics.alerts(prefix, m.severity());
        } else if (NotificationMessage.BRIEFING.equals(m.event())) {
            String briefingId = MqttPayloads.briefingId(m);
            topic = MqttTopics.briefing(prefix, briefingId == null ? "unknown" : briefingId);
            retain = true;
            expiry = null;
        } else {
            topic = MqttTopics.systemEvents(prefix);
        }
        byte[] payload = MqttPayloads.event(m, c.payloadTemplate());
        MqttMessage message = new MqttMessage(topic, payload, c.qos(), retain, expiry, MqttMessage.JSON);
        return new PreparedRequest("PUBLISH", c.brokerUrl(), Map.of(), new PreparedRequest.Mqtt(s.channelId(), message));
    }
}
