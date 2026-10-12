package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.audit.AuditService;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Trusted proxies (prompt 023): with the default {@code FORESIGNAL_TRUSTED_PROXIES} (loopback),
 * Tomcat's {@code RemoteIpValve} honours {@code X-Forwarded-For} and {@code X-Forwarded-Proto}
 * from this test, which connects over loopback. The client IP is read back from the audit log of a
 * refused write. {@link UntrustedProxyIT} is the same request from a proxy that is not trusted.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class TrustedProxyIT {

    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @LocalServerPort int port;
    @Autowired AuditService audit;
    @Autowired JdbcTemplate jdbc;
    @Autowired SecureRequestMonitor secureRequests;

    @BeforeEach
    void clean() {
        audit.flush();
        jdbc.update("DELETE FROM audit_event");
        secureRequests.reset();
    }

    /** A refused write carrying forwarded headers; returns the audited client IP. */
    static String forwardedWrite(int port, AuditService audit, JdbcTemplate jdbc) throws Exception {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v1/settings/cors"))
                .timeout(Duration.ofSeconds(10))
                .header("X-Forwarded-For", "203.0.113.9")
                .header("X-Forwarded-Proto", "https")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(401);
        audit.flush();
        return jdbc.queryForObject("SELECT remote_ip FROM audit_event WHERE operation_id = 'updateCors' ORDER BY id DESC LIMIT 1",
                String.class);
    }

    @Test
    @DisplayName("X-Forwarded-For and X-Forwarded-Proto are honoured from a configured internal proxy")
    void honouredFromTrustedProxy() throws Exception {
        assertThat(forwardedWrite(port, audit, jdbc)).isEqualTo("203.0.113.9");
        assertThat(secureRequests.sawHttps()).isTrue();
    }
}
