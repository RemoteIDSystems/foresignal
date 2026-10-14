package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.HostResolver;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The {@code mqtt://} and {@code mqtts://} {@code LAN_URL} schemes of prompt 083 (SRC-7): an explicit port, a host pinned
 * to a private address, nothing after the port, and only where the parameter lists the scheme.
 */
class MqttLanUrlTest {

    @TempDir Path inbox;

    private ParamValidator validator;

    @BeforeEach
    void setUp() {
        Map<String, String> dns = Map.of("broker.lan", "192.168.1.10", "broker.example", "93.184.216.34");
        HostResolver resolver = host -> {
            String address = dns.get(host);
            if (address == null) {
                throw new UnknownHostException(host);
            }
            return new InetAddress[] {InetAddress.getByName(address)};
        };
        validator = new ParamValidator(new InboxFiles(inbox), OutboundUrlPolicy.withoutOverrides(resolver));
    }

    private static SourceDescriptor source(String... schemes) {
        return SourceDescriptor.builder("broker-source").kind("thing").defaultBudget(2, 200)
                .param(ParamSpec.lanUrl("brokerUrl", "", true, schemes)).build();
    }

    @Test
    @DisplayName("mqtt:// and mqtts:// with an explicit port pin a private address")
    void accepted() throws Exception {
        ValidatedParams params = validator.validate(source("mqtt", "mqtts"), Map.of("brokerUrl", "mqtt://broker.lan:1883"));
        LanEndpoint endpoint = LanEndpoint.fromParams(params, "brokerUrl");
        assertThat(endpoint.scheme()).isEqualTo(LanEndpoint.Scheme.MQTT);
        assertThat(endpoint.port()).isEqualTo(1883);
        assertThat(endpoint.address()).isEqualTo(InetAddress.getByName("192.168.1.10"));
        ValidatedParams tls = validator.validate(source("mqtt", "mqtts"), Map.of("brokerUrl", "mqtts://192.168.1.11:8883"));
        assertThat(LanEndpoint.fromParams(tls, "brokerUrl").scheme()).isEqualTo(LanEndpoint.Scheme.MQTTS);
        assertThat(LanEndpoint.fromParams(tls, "brokerUrl").toString()).isEqualTo("mqtts://192.168.1.11:8883");
    }

    @Test
    @DisplayName("no default port, no public host, no path, and not where the parameter does not list the scheme")
    void refused() {
        for (String url : new String[] {"mqtt://broker.lan", "mqtt://broker.example:1883", "mqtt://broker.lan:1883/x",
                "mqtt://user@broker.lan:1883", "mqtt://8.8.8.8:1883"}) {
            assertThatThrownBy(() -> validator.validate(source("mqtt", "mqtts"), Map.of("brokerUrl", url))).as(url)
                    .isInstanceOfAny(ValidationException.class, InvalidInputException.class);
        }
        assertThatThrownBy(() -> validator.validate(source("tcp"), Map.of("brokerUrl", "mqtt://broker.lan:1883")))
                .isInstanceOfAny(ValidationException.class, InvalidInputException.class);
        assertThatThrownBy(() -> validator.validate(source("mqtt"), Map.of("brokerUrl", "mqtts://broker.lan:8883")))
                .isInstanceOfAny(ValidationException.class, InvalidInputException.class);
    }
}
