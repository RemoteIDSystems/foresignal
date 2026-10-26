package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.AuthController;
import ai.foresignal.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** The per-address limit on the credential operations (prompt R2-003). */
@IntegrationTest
@TestPropertySource(properties = "foresignal.ratelimit.enabled=true")
class AuthRateLimitIT {

    @Autowired private MockMvc mvc;

    private MockHttpServletResponse attempt(String path, String body, String ip) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body).with(r -> {
            r.setRemoteAddr(ip);
            return r;
        })).andReturn().getResponse();
    }

    @Test
    @DisplayName("past the per-minute limit the credential operations answer 429 with Retry-After")
    void limited() throws Exception {
        for (String path : new String[] {"/v1/auth/login", "/v1/auth/password/reset-request",
                "/v1/auth/password/reset", "/v1/auth/activate"}) {
            String ip = "198.51.100." + path.length();
            String body = "{\"email\":\"x@example.com\",\"password\":\"p\",\"token\":\"t\",\"newPassword\":\"p\"}";
            for (int i = 0; i < AuthController.AUTH_PER_MINUTE; i++) {
                assertThat(attempt(path, body, ip).getStatus()).as(path).isNotEqualTo(429);
            }
            MockHttpServletResponse refused = attempt(path, body, ip);
            assertThat(refused.getStatus()).as(path).isEqualTo(429);
            assertThat(refused.getHeader(HttpHeaders.RETRY_AFTER)).isNotBlank();
        }
    }
}
