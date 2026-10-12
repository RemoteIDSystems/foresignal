package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.inbound.format.DeviceEntityMismatchException;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.security.restrict.PathKeyFilter;
import ai.foresignal.source.spi.NormalizedRecord;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * The key-in-path ingest URLs (prompt 039): path parsing, redaction everywhere the URI is seen, the
 * sender allowlist and the device-binding check.
 */
class IngestPathKeyTest {

    static final String KEY = "eid_AbCdEfGhIjKlMnOpQrStUvWxYz0123456789-_xyz";

    @Test
    @DisplayName("the k path names the source, the format and marks the key as carried in the path")
    void parsesKeyPath() {
        IngestAuthenticator.IngestPath path =
                IngestAuthenticator.IngestPath.of("/v1/ingest/custom-family/k/" + KEY + "/osmand?id=x");
        assertThat(path.sourceId()).isEqualTo("custom-family");
        assertThat(path.format()).isEqualTo("osmand");
        assertThat(path.keyInPath()).isTrue();
        assertThat(IngestAuthenticator.IngestPath.of("/v1/ingest/custom-family/osmand").keyInPath()).isFalse();
        assertThat(IngestAuthenticator.IngestPath.of("/v1/ingest/custom-family/osmand").format()).isEqualTo("osmand");
    }

    @Test
    @DisplayName("the path key and key= are redacted; auth= and token= stay redacted; other parameters stay")
    void redactsKeys() {
        String path = "/v1/ingest/custom-family/k/" + KEY + "/gpslogger";
        assertThat(IngestUrls.pathKey(path)).isEqualTo(KEY);
        assertThat(IngestUrls.redactPath(path)).isEqualTo("/v1/ingest/custom-family/k/redacted/gpslogger");
        assertThat(IngestUrls.redacted(path, "lat=1&key=" + KEY + "&auth=x&token=y&lon=2"))
                .isEqualTo("/v1/ingest/custom-family/k/redacted/gpslogger?lat=1&key=***&auth=***&token=***&lon=2")
                .doesNotContain(KEY);
        assertThat(IngestUrls.redactPath("http://h:8080" + path)).isEqualTo("http://h:8080/v1/ingest/custom-family/k/redacted/gpslogger");
        assertThat(IngestUrls.redactPath("/v1/ingest/custom-family/osmand")).isEqualTo("/v1/ingest/custom-family/osmand");
        assertThat(IngestUrls.pathKey("/v1/ingest/custom-family/osmand")).isNull();
        assertThat(IngestUrls.redactPath(null)).isNull();
    }

    @Test
    @DisplayName("the redaction filter hides the key from the rest of the chain and the access log")
    void filterHidesKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/ingest/custom-family/k/" + KEY + "/osmand");
        request.setServletPath("/v1/ingest/custom-family/k/" + KEY + "/osmand");
        AtomicReference<HttpServletRequest> seen = new AtomicReference<>();
        new IngestKeyRedactionFilter().doFilter(request, new MockHttpServletResponse(),
                (req, res) -> seen.set((HttpServletRequest) req));
        HttpServletRequest downstream = seen.get();
        assertThat(downstream.getRequestURI()).isEqualTo("/v1/ingest/custom-family/k/redacted/osmand");
        assertThat(downstream.getServletPath()).doesNotContain(KEY);
        assertThat(downstream.getRequestURL().toString()).doesNotContain(KEY);
        assertThat(IngestKeyRedactionFilter.pathKey(downstream)).isEqualTo(KEY);
        assertThat(request.getAttribute(PathKeyFilter.LOG_URI_ATTRIBUTE)).isEqualTo("/v1/ingest/custom-family/k/redacted/osmand");

        MockHttpServletRequest plain = new MockHttpServletRequest("POST", "/v1/ingest/custom-family/owntracks");
        AtomicReference<HttpServletRequest> passed = new AtomicReference<>();
        new IngestKeyRedactionFilter().doFilter(plain, new MockHttpServletResponse(),
                (req, res) -> passed.set((HttpServletRequest) req));
        assertThat(passed.get()).isSameAs(plain);
        assertThat(IngestKeyRedactionFilter.pathKey(plain)).isNull();
    }

    @Test
    @DisplayName("CIDR blocks parse, normalize and match IPv4, IPv6 and IPv4-mapped senders; names never resolve")
    void cidrs() {
        assertThat(Cidr.parse("192.168.1.77/24").toString()).isEqualTo("192.168.1.0/24");
        assertThat(Cidr.parse("192.168.1.0/24").contains("192.168.1.200")).isTrue();
        assertThat(Cidr.parse("192.168.1.0/24").contains("192.168.2.1")).isFalse();
        assertThat(Cidr.parse("192.168.1.0/24").contains("::ffff:192.168.1.9")).isTrue();
        assertThat(Cidr.parse("2001:db8::/32").contains("2001:db8:1::5")).isTrue();
        assertThat(Cidr.parse("2001:db8::/32").contains("10.0.0.1")).isFalse();
        assertThat(Cidr.parse("203.0.113.9").contains("203.0.113.9")).isTrue();
        assertThatThrownBy(() -> Cidr.parse("example.com/24")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cidr.parse("a.b.c.d")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cidr.parse("10.0.0.0/33")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cidr.parse("10.0.0.0/x")).isInstanceOf(IllegalArgumentException.class);
        assertThat(Cidr.parse("10.0.0.0/8")).isEqualTo(Cidr.parse("10.1.2.3/8"));
    }

    @Test
    @DisplayName("without ingestAllowCidrs only private senders pass; with it only listed senders; refusal is 403 INGEST_SOURCE_DENIED")
    void senders() {
        assertThat(InboundRequests.senderAllowed(null, "127.0.0.1")).isTrue();
        assertThat(InboundRequests.senderAllowed(null, "192.168.4.20")).isTrue();
        assertThat(InboundRequests.senderAllowed(null, "fd12::1")).isTrue();
        assertThat(InboundRequests.senderAllowed(null, "203.0.113.5")).isFalse();
        assertThat(InboundRequests.senderAllowed(null, "not-an-address")).isFalse();
        assertThat(InboundRequests.senderAllowed(List.of("203.0.113.0/24"), "203.0.113.5")).isTrue();
        assertThat(InboundRequests.senderAllowed(List.of("203.0.113.0/24"), "127.0.0.1")).isFalse();
        assertThat(InboundRequests.senderAllowed(List.of(), "127.0.0.1")).isFalse();
        assertThatThrownBy(() -> InboundRequests.checkSender(null, "198.51.100.1"))
                .isInstanceOfSatisfying(ForesignalException.class,
                        e -> assertThat(e.getCode()).isEqualTo("INGEST_SOURCE_DENIED"));
    }

    @Test
    @DisplayName("a device key's records must all belong to its bound entity (or its OwnTracks transition entity)")
    void boundEntity() {
        Instant t = Instant.parse("2026-10-12T08:00:00Z");
        ParsedIngest ok = ParsedIngest.builder()
                .add(0, NormalizedRecord.tombstone("phone-a", "tracker", t))
                .add(1, NormalizedRecord.tombstone("phone-a:transition", "tracker-transition", t)).build();
        InboundRequests.requireBound(ok, "phone-a");
        ParsedIngest other = ParsedIngest.builder()
                .add(0, NormalizedRecord.tombstone("phone-a", "tracker", t))
                .add(1, NormalizedRecord.tombstone("phone-b", "tracker", t)).build();
        assertThatThrownBy(() -> InboundRequests.requireBound(other, "phone-a"))
                .isInstanceOf(DeviceEntityMismatchException.class);
    }
}
