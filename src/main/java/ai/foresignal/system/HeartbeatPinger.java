package ai.foresignal.system;

import ai.foresignal.net.OutboundUrlDeniedException;
import ai.foresignal.net.OutboundUrlPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import org.springframework.stereotype.Component;

/**
 * Sends one watchdog ping (prompt 019): {@code GET} or an empty {@code POST}, a 5 s connect and read
 * timeout, redirects never followed. A 2xx answer is success.
 *
 * <p>Uses {@link HttpURLConnection} rather than an {@code HttpClient}: the watchdog is not a source,
 * and {@code HttpClient} construction is reserved for {@code UpstreamClient}.
 *
 * <p>Prompt 035: the URL passes {@link OutboundUrlPolicy} profile {@code LAN_ALLOWED} before every
 * ping (an Uptime Kuma on the LAN is allowed; link-local and metadata addresses never), and a refusal
 * is an {@link OutboundUrlDeniedException}. {@link HttpURLConnection} cannot be pointed at an address
 * while keeping the host name for TLS, so it resolves again right after the check, which the JVM's
 * DNS cache answers with the checked address; redirects stay off.
 */
@Component
public class HeartbeatPinger {

    /** The outbound profile of the watchdog URLs. */
    public static final OutboundUrlPolicy.Profile PROFILE = OutboundUrlPolicy.Profile.LAN_ALLOWED;

    private final OutboundUrlPolicy policy;

    /** Creates the pinger. */
    public HeartbeatPinger(OutboundUrlPolicy policy) {
        this.policy = policy;
    }

    /**
     * Pings {@code url}.
     *
     * @param url an absolute http or https URL
     * @param method {@code GET} or {@code POST}
     * @param timeoutMillis connect and read timeout
     * @return the HTTP status
     * @throws OutboundUrlDeniedException when the outbound URL policy refuses the URL
     * @throws IOException when the request fails
     */
    public int ping(String url, String method, int timeoutMillis) throws IOException {
        OutboundUrlDeniedException.require(policy.check(url, PROFILE, false));
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        try {
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);
            connection.setRequestMethod("POST".equals(method) ? "POST" : "GET");
            connection.setRequestProperty("User-Agent", "Foresignal-Watchdog");
            if ("POST".equals(method)) {
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(0);
                try (OutputStream out = connection.getOutputStream()) {
                    out.flush();
                }
            }
            int status = connection.getResponseCode();
            try (InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                if (in != null) {
                    in.readNBytes(4096);
                }
            }
            return status;
        } finally {
            connection.disconnect();
        }
    }

    /** {@code scheme://host[:port]/…}: monitor tokens live in the path and query, so neither is logged. */
    public static String redact(String url) {
        if (url == null) {
            return null;
        }
        try {
            URI uri = URI.create(url);
            String port = uri.getPort() < 0 ? "" : ":" + uri.getPort();
            return uri.getScheme() + "://" + uri.getHost() + port + "/…";
        } catch (IllegalArgumentException e) {
            return "<invalid url>";
        }
    }
}
