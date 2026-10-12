package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.ServerSettings;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code PUBLIC_BASE_URL_INSECURE} and {@code PUBLIC_BASE_URL_MISMATCH} (prompt 023). */
class PublicBaseUrlWarningsTest {

    private static List<String> codes(String base, boolean https) {
        return PublicBaseUrlWarnings.of(new ServerSettings(base), https).stream().map(SystemWarnings.Warning::code).toList();
    }

    @Test
    @DisplayName("http:// on a public host is insecure; local, private, CGNAT and .local hosts are not")
    void insecure() {
        assertThat(codes("http://foresignal.example.com", false)).containsExactly("PUBLIC_BASE_URL_INSECURE");
        assertThat(codes("http://203.0.113.5:8080", false)).containsExactly("PUBLIC_BASE_URL_INSECURE");
        for (String local : List.of("http://localhost:8080", "http://127.0.0.1", "http://192.168.1.20:8080",
                "http://10.0.0.2", "http://100.101.102.103", "http://foresignal.local:8080", "http://[fd00::1]:8080")) {
            assertThat(codes(local, false)).as(local).isEmpty();
        }
        assertThat(codes("https://foresignal.example.com", false)).isEmpty();
        assertThat(codes(null, true)).isEmpty();
    }

    @Test
    @DisplayName("requests over https while publicBaseUrl is http:// is a mismatch")
    void mismatch() {
        assertThat(codes("http://192.168.1.20:8080", true)).containsExactly("PUBLIC_BASE_URL_MISMATCH");
        assertThat(codes("https://fs.example", true)).isEmpty();
        assertThat(codes("http://fs.example", true)).containsExactly("PUBLIC_BASE_URL_INSECURE", "PUBLIC_BASE_URL_MISMATCH");
    }
}
