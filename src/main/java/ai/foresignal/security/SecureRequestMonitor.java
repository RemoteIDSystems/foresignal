package ai.foresignal.security;

import org.springframework.stereotype.Component;

/**
 * Remembers whether any request has arrived over HTTPS (directly or with a trusted proxy's
 * {@code X-Forwarded-Proto: https}, which Tomcat's {@code RemoteIpValve} turns into a secure
 * request), so the settings and {@code /v1/system} can warn {@code PUBLIC_BASE_URL_MISMATCH} when
 * {@code publicBaseUrl} is still {@code http://} (prompt 023).
 */
@Component
public class SecureRequestMonitor {

    private volatile boolean sawHttps;

    /** Records that a request arrived over HTTPS. */
    public void secureRequest() {
        sawHttps = true;
    }

    /** True once any request arrived over HTTPS since start. */
    public boolean sawHttps() {
        return sawHttps;
    }

    /** Forgets what was seen (tests). */
    public void reset() {
        sawHttps = false;
    }
}
