package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.audit.AuditService;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.stream.StreamTestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * The counterpart of {@link TrustedProxyIT} (prompt 023): when the connecting address is not a
 * configured internal proxy, forwarded headers are ignored and the client IP stays the socket's.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
@TestPropertySource(properties = "server.tomcat.remoteip.internal-proxies=10\\.255\\.255\\.254")
class UntrustedProxyIT {

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

    @Test
    @DisplayName("X-Forwarded-For from an address that is not a trusted proxy is ignored")
    void ignoredFromUntrustedPeer() throws Exception {
        String ip = TrustedProxyIT.forwardedWrite(port, audit, jdbc);

        assertThat(ip).isIn("127.0.0.1", "0:0:0:0:0:0:0:1");
        assertThat(secureRequests.sawHttps()).isFalse();
    }
}
