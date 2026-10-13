package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.Cidr;
import ai.foresignal.net.HostResolver;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The {@code tcp://} and {@code udp://} {@code LAN_URL} rules of prompt 069 (SRC-7): a TCP host resolves once
 * to a private address and is pinned; a UDP bind host is a wildcard or a local address and needs
 * {@code allowSenders} of wholly private blocks; the port is always explicit; and a socket scheme is accepted
 * only by a parameter that lists it.
 */
class LanEndpointValidationTest {

    @TempDir Path inbox;

    /** The resolver's answers; the test may change them between validations (DNS rebinding). */
    private final AtomicReference<Map<String, String>> dns = new AtomicReference<>(Map.of(
            "pi.local", "192.168.1.40",
            "receiver.local", "93.184.216.34",
            "localhost", "127.0.0.1"));

    private ParamValidator validator;

    @BeforeEach
    void setUp() {
        HostResolver resolver = host -> {
            String address = dns.get().get(host);
            if (address == null) {
                throw new UnknownHostException(host);
            }
            return new InetAddress[] {InetAddress.getByName(address)};
        };
        validator = new ParamValidator(new InboxFiles(inbox), OutboundUrlPolicy.withoutOverrides(resolver));
    }

    private static SourceDescriptor tcpSource() {
        return SourceDescriptor.builder("tcp-source").kind("thing").defaultBudget(2, 200)
                .param(ParamSpec.lanUrl("endpoint", "", true, "tcp"))
                .build();
    }

    private static SourceDescriptor udpSource() {
        return SourceDescriptor.builder("udp-source").kind("thing").defaultBudget(2, 200)
                .param(ParamSpec.lanUrl("bind", "", true, "udp"))
                .param(ParamSpec.optional(ParamValidator.ALLOW_SENDERS, ParamType.STRING_LIST, "", null))
                .build();
    }

    private ValidationException refusal(SourceDescriptor descriptor, Map<String, Object> raw) {
        try {
            validator.validate(descriptor, raw);
        } catch (ValidationException e) {
            return e;
        }
        throw new AssertionError("expected " + raw + " to be refused");
    }

    private static List<String> fields(ValidationException e) {
        return e.getErrors().stream().map(FieldError::field).toList();
    }

    @Nested
    @DisplayName("tcp://")
    class Tcp {

        @Test
        @DisplayName("a public literal is refused with URL_NOT_ALLOWED")
        void publicLiteral() {
            ValidationException e = refusal(tcpSource(), Map.of("endpoint", "tcp://8.8.8.8:30003"));
            assertThat(e.getCode()).isEqualTo(ErrorCodes.URL_NOT_ALLOWED);
            assertThat(fields(e)).containsExactly("params.endpoint");
        }

        @Test
        @DisplayName("a .local name that resolves to a public address is refused")
        void localNameResolvingPublic() {
            ValidationException e = refusal(tcpSource(), Map.of("endpoint", "tcp://receiver.local:30003"));
            assertThat(e.getCode()).isEqualTo(ErrorCodes.URL_NOT_ALLOWED);
        }

        @Test
        @DisplayName("a private host is pinned at validation and a later rebinding does not move the endpoint")
        void pinned() {
            ValidatedParams params = validator.validate(tcpSource(), Map.of("endpoint", "tcp://pi.local:30003"));
            dns.set(Map.of("pi.local", "93.184.216.34"));

            LanEndpoint endpoint = LanEndpoint.fromParams(params, "endpoint");
            assertThat(endpoint.scheme()).isEqualTo(LanEndpoint.Scheme.TCP);
            assertThat(endpoint.port()).isEqualTo(30003);
            assertThat(endpoint.address().getHostAddress()).isEqualTo("192.168.1.40");
            assertThat(endpoint.host()).isEqualTo("pi.local");
            assertThat(endpoint).hasToString("tcp://pi.local:30003");
        }

        @Test
        @DisplayName("loopback and unique-local literals are accepted")
        void loopbackAndUla() {
            assertThat(LanEndpoint.fromParams(validator.validate(tcpSource(), Map.of("endpoint", "tcp://127.0.0.1:2947")),
                    "endpoint").address().isLoopbackAddress()).isTrue();
            assertThat(LanEndpoint.fromParams(validator.validate(tcpSource(), Map.of("endpoint", "tcp://[fd00::5]:2947")),
                    "endpoint").port()).isEqualTo(2947);
        }

        @ParameterizedTest
        @ValueSource(strings = {"tcp://192.168.1.5", "tcp://192.168.1.5:0", "tcp://192.168.1.5:70000"})
        @DisplayName("a missing or impossible port is refused: no default is ever assumed")
        void portRequired(String url) {
            ValidationException e = refusal(tcpSource(), Map.of("endpoint", url));
            assertThat(fields(e)).containsExactly("params.endpoint");
            assertThat(e.getErrors().getFirst().message()).contains("port");
        }

        @ParameterizedTest
        @ValueSource(strings = {"tcp://192.168.1.5:30003/feed", "tcp://192.168.1.5:30003?x=1",
                "tcp://user@192.168.1.5:30003", "tcp://192.168.1.5:30003#frag"})
        @DisplayName("a path, query, fragment or user info is refused")
        void onlyHostAndPort(String url) {
            assertThat(fields(refusal(tcpSource(), Map.of("endpoint", url)))).containsExactly("params.endpoint");
        }

        @Test
        @DisplayName("a tcp-only parameter refuses http, and a default LAN_URL refuses tcp")
        void schemesPerParameter() {
            assertThat(refusal(tcpSource(), Map.of("endpoint", "http://192.168.1.5:8080")).getErrors().getFirst()
                    .message()).contains("scheme");
            SourceDescriptor http = SourceDescriptor.builder("http-source").kind("thing").defaultBudget(2, 200)
                    .param(ParamSpec.optional("baseUrl", ParamType.LAN_URL, "", null)).build();
            assertThat(refusal(http, Map.of("baseUrl", "tcp://192.168.1.5:30003")).getErrors().getFirst().message())
                    .contains("scheme");
        }
    }

    @Nested
    @DisplayName("udp://")
    class Udp {

        @Test
        @DisplayName("a udp endpoint without allowSenders is refused")
        void allowSendersRequired() {
            ValidationException e = refusal(udpSource(), Map.of("bind", "udp://0.0.0.0:5000"));
            assertThat(fields(e)).containsExactly("params." + ParamValidator.ALLOW_SENDERS);
        }

        @Test
        @DisplayName("a udp endpoint on a descriptor that declares no allowSenders is refused")
        void companionMustBeDeclared() {
            SourceDescriptor bare = SourceDescriptor.builder("bare").kind("thing").defaultBudget(2, 200)
                    .param(ParamSpec.lanUrl("bind", "", true, "udp")).build();
            assertThat(fields(refusal(bare, Map.of("bind", "udp://0.0.0.0:5000"))))
                    .containsExactly("params." + ParamValidator.ALLOW_SENDERS);
        }

        @ParameterizedTest
        @ValueSource(strings = {"8.8.8.0/24", "10.0.0.0/7", "100.64.0.0/10", "2001:db8::/32", "not-a-cidr"})
        @DisplayName("a public, partly private or malformed sender block is refused")
        void publicSendersRefused(String block) {
            ValidationException e = refusal(udpSource(), Map.of("bind", "udp://0.0.0.0:5000",
                    ParamValidator.ALLOW_SENDERS, List.of("192.168.1.0/24", block)));
            assertThat(fields(e)).containsExactly("params." + ParamValidator.ALLOW_SENDERS);
        }

        @Test
        @DisplayName("private, loopback, link-local and unique-local senders are accepted, and the bind is kept")
        void privateSendersAccepted() {
            ValidatedParams params = validator.validate(udpSource(), Map.of("bind", "udp://0.0.0.0:5000",
                    ParamValidator.ALLOW_SENDERS, List.of("192.168.1.0/24", "10.1.2.3", "127.0.0.1", "169.254.0.0/16",
                            "fd00::/8")));
            LanEndpoint bind = LanEndpoint.fromParams(params, "bind");
            assertThat(bind.scheme()).isEqualTo(LanEndpoint.Scheme.UDP);
            assertThat(bind.address().isAnyLocalAddress()).isTrue();
            assertThat(bind.port()).isEqualTo(5000);
        }

        @ParameterizedTest
        @ValueSource(strings = {"udp://8.8.8.8:5000", "udp://receiver.local:5000"})
        @DisplayName("a bind host that is not a wildcard or a local address is refused")
        void bindMustBeLocal(String url) {
            ValidationException e = refusal(udpSource(), Map.of("bind", url,
                    ParamValidator.ALLOW_SENDERS, List.of("192.168.1.0/24")));
            assertThat(fields(e)).contains("params.bind");
        }

        @Test
        @DisplayName("a loopback bind and the IPv6 wildcard are accepted")
        void loopbackBind() {
            assertThat(LanEndpoint.fromParams(validator.validate(udpSource(), Map.of("bind", "udp://127.0.0.1:5000",
                    ParamValidator.ALLOW_SENDERS, List.of("127.0.0.1"))), "bind").address().isLoopbackAddress()).isTrue();
            assertThat(LanEndpoint.fromParams(validator.validate(udpSource(), Map.of("bind", "udp://[::]:5000",
                    ParamValidator.ALLOW_SENDERS, List.of("fd00::/8"))), "bind").address().isAnyLocalAddress()).isTrue();
        }

        @Test
        @DisplayName("a missing port is refused")
        void portRequired() {
            assertThat(fields(refusal(udpSource(), Map.of("bind", "udp://0.0.0.0",
                    ParamValidator.ALLOW_SENDERS, List.of("192.168.1.0/24"))))).containsExactly("params.bind");
        }
    }

    @Test
    @DisplayName("isPrivateBlock: wholly inside private space, never merely overlapping it")
    void privateBlocks() {
        assertThat(LocalAddresses.isPrivateBlock(Cidr.parse("192.168.0.0/16").orElseThrow())).isTrue();
        assertThat(LocalAddresses.isPrivateBlock(Cidr.parse("172.16.5.0/24").orElseThrow())).isTrue();
        assertThat(LocalAddresses.isPrivateBlock(Cidr.parse("172.0.0.0/8").orElseThrow())).isFalse();
        assertThat(LocalAddresses.isPrivateBlock(Cidr.parse("fe80::/64").orElseThrow())).isTrue();
        assertThat(LocalAddresses.isPrivateBlock(Cidr.parse("::1").orElseThrow())).isTrue();
        assertThat(LocalAddresses.isPrivateBlock(Cidr.parse("0.0.0.0/0").orElseThrow())).isFalse();
        assertThat(LocalAddresses.isPrivateBlock(null)).isFalse();
    }

    @Test
    @DisplayName("LanEndpoint.fromParams refuses an http parameter and an unpinned one")
    void endpointFromParams() {
        ValidatedParams http = new ValidatedParams(Map.of("u", java.net.URI.create("http://192.168.1.5/")), Map.of());
        assertThatThrownBy(() -> LanEndpoint.fromParams(http, "u")).isInstanceOf(IllegalStateException.class);
        ValidatedParams unpinned = new ValidatedParams(Map.of("u", java.net.URI.create("tcp://192.168.1.5:1")), Map.of());
        assertThatThrownBy(() -> LanEndpoint.fromParams(unpinned, "u")).isInstanceOf(IllegalStateException.class);
    }
}
