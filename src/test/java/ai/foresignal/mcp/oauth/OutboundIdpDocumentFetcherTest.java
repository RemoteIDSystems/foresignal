package ai.foresignal.mcp.oauth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.net.TestOutbound;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link OutboundIdpDocumentFetcher} (prompt 141): documents fetched through the outbound URL policy without
 * credentials, non-200 answers and oversized bodies refused, and plain http only towards non-public addresses.
 */
class OutboundIdpDocumentFetcherTest extends WireMockSupport {

    final OutboundIdpDocumentFetcher fetcher = new OutboundIdpDocumentFetcher(TestOutbound.offlinePolicy(),
            TestOutbound.pinnedHttp());

    @Test
    @DisplayName("a 200 document is returned; no Authorization header is ever sent")
    void ok() throws Exception {
        stubFor(get(urlEqualTo("/certs")).willReturn(aResponse().withStatus(200).withBody("{\"keys\":[]}")));
        assertThat(fetcher.fetch(URI.create(url("/certs")))).isEqualTo("{\"keys\":[]}");
        verify(getRequestedFor(urlEqualTo("/certs")).withHeader("Authorization", absent()));
    }

    @Test
    @DisplayName("a non-200 answer is an IOException naming the status")
    void notFound() {
        stubFor(get(urlEqualTo("/missing")).willReturn(aResponse().withStatus(404)));
        assertThatThrownBy(() -> fetcher.fetch(URI.create(url("/missing"))))
                .isInstanceOf(IOException.class).hasMessageContaining("404");
    }

    @Test
    @DisplayName("a body over 512 KB is refused")
    void tooLarge() {
        stubFor(get(urlEqualTo("/big")).willReturn(aResponse().withStatus(200)
                .withBody("x".repeat(OutboundIdpDocumentFetcher.MAX_BYTES + 1))));
        assertThatThrownBy(() -> fetcher.fetch(URI.create(url("/big"))))
                .isInstanceOf(IOException.class).hasMessageContaining("more than");
    }

    @Test
    @DisplayName("a host the policy refuses is never contacted")
    void refusedHost() {
        assertThatThrownBy(() -> fetcher.fetch(URI.create("http://169.254.169.254/latest/meta-data")))
                .isInstanceOf(IOException.class).hasMessageContaining("outbound URL policy");
    }

    @Test
    @DisplayName("plain http is refused for a public address and accepted for loopback; saving accepts unresolved hosts")
    void plainHttp() {
        OutboundUrlPolicy policy = OutboundUrlPolicy.withoutOverrides(host -> switch (host) {
            case "public.example" -> new InetAddress[] {InetAddress.getByName("93.184.216.34")};
            case "tail.example" -> new InetAddress[] {InetAddress.getByName("100.101.102.103")};
            default -> throw new UnknownHostException(host);
        });
        assertThat(OutboundIdpDocumentFetcher.problem(policy, URI.create("http://public.example/jwks"), false))
                .contains("plain http");
        assertThat(OutboundIdpDocumentFetcher.problem(policy, URI.create("https://public.example/jwks"), false))
                .isNull();
        assertThat(OutboundIdpDocumentFetcher.problem(policy, URI.create("http://tail.example/jwks"), false)).isNull();
        assertThat(OutboundIdpDocumentFetcher.problem(policy, URI.create("http://127.0.0.1:8080/jwks"), false))
                .isNull();
        assertThat(OutboundIdpDocumentFetcher.problem(policy, URI.create("https://gone.example/jwks"), true)).isNull();
        assertThat(OutboundIdpDocumentFetcher.problem(policy, URI.create("https://gone.example/jwks"), false))
                .contains("does not resolve");
        assertThatThrownBy(() -> OutboundIdpDocumentFetcher.check(policy, URI.create("http://public.example/jwks")))
                .isInstanceOf(IOException.class).hasMessageContaining("use https");
    }
}
