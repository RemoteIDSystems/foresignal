package ai.foresignal.egress;

import ai.foresignal.net.OutboundUrlPolicy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code testEgressRoute} (prompt R2-018): one {@code GET} of the configured exit-IP service through the route, which
 * answers with the public address it saw the request come from.
 *
 * <p>The URL is the operator's configuration ({@code foresignal.egress.probe-url}, default
 * {@value #DEFAULT_PROBE_URL}, run by Amazon Web Services), never one taken from a request body or from observed
 * content, and it must pass the outbound URL policy's {@code PUBLIC_ONLY} profile. The answer must be one address
 * literal, or the test fails. A passing test records the exit IP on the route and counts as a success for its health;
 * a failing one counts as a failure.
 */
@Component
public class EgressProbe {

    /** The default exit-IP service: it answers with the caller's address and a newline. */
    public static final String DEFAULT_PROBE_URL = "https://checkip.amazonaws.com/";

    /** How long a test may take. */
    static final Duration TIMEOUT = Duration.ofSeconds(15);

    /** The largest answer read. */
    static final int MAX_ANSWER = 256;

    private static final Pattern IPV4 = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");
    private static final Pattern IPV6 = Pattern.compile("[0-9A-Fa-f:.]*:[0-9A-Fa-f:.]*");

    private final EgressRouter router;
    private final EgressHealthRegistry health;
    private final EgressRouteStore store;
    private final OutboundUrlPolicy outbound;
    private final Clock clock;
    private volatile URI probe;
    private volatile boolean policyChecked = true;

    /** Creates the probe. */
    public EgressProbe(EgressRouter router, EgressHealthRegistry health, EgressRouteStore store,
            OutboundUrlPolicy outbound, Clock clock,
            @Value("${foresignal.egress.probe-url:" + DEFAULT_PROBE_URL + "}") String probeUrl) {
        this.router = router;
        this.health = health;
        this.store = store;
        this.outbound = outbound;
        this.clock = clock;
        this.probe = URI.create(probeUrl);
    }

    /** Tests {@code route} and returns the {@code EgressTestResult} view. */
    public Map<String, Object> test(EgressRoute route) {
        Instant started = clock.instant();
        EgressRouter.Availability availability = router.availability(route);
        if (!availability.available()) {
            return result(false, null, null, availability.reason(), started);
        }
        OutboundUrlPolicy.Result allowed = policyChecked
                ? outbound.check(probe, OutboundUrlPolicy.Profile.PUBLIC_ONLY) : null;
        if (allowed instanceof OutboundUrlPolicy.Denied denied) {
            return result(false, null, null, "The exit-IP service " + probe + " is refused by the outbound URL policy: "
                    + denied.reason(), started);
        }
        EgressRouter.Routing routing = router.routing(route);
        long t0 = System.nanoTime();
        try {
            HttpResponse<byte[]> response = routing.client(false).send(HttpRequest.newBuilder(probe).timeout(TIMEOUT)
                    .header("Accept", "text/plain").GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            Duration latency = Duration.ofNanos(System.nanoTime() - t0);
            byte[] body = response.body();
            String answer = new String(body, 0, Math.min(body.length, MAX_ANSWER), StandardCharsets.US_ASCII).strip();
            if (response.statusCode() != 200) {
                health.failure(route, "the exit-IP service answered HTTP " + response.statusCode());
                return result(false, null, millis(latency), "The exit-IP service answered HTTP "
                        + response.statusCode(), started);
            }
            String ip = address(answer);
            if (ip == null) {
                health.success(route, latency, null);
                return result(false, null, millis(latency), "The exit-IP service's answer is not an address", started);
            }
            health.success(route, latency, ip);
            store.save(route.withExitIp(ip, clock.instant()));
            return result(true, ip, millis(latency), null, started);
        } catch (IOException e) {
            health.failure(route, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            return result(false, null, null, "The call through the route failed: " + e.getMessage(), started);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result(false, null, null, "Interrupted", started);
        }
    }

    /**
     * Points the probe at a local stub (tests). The stub is loopback, which {@code PUBLIC_ONLY} refuses, so the policy
     * check is skipped until {@link #resetProbe} restores the configured URL.
     */
    void probeUrl(URI uri) {
        this.probe = uri;
        this.policyChecked = false;
    }

    /** Restores {@code url} with the policy check (tests). */
    void resetProbe(String url) {
        this.probe = URI.create(url);
        this.policyChecked = true;
    }

    /** {@code answer} as a normalized address literal, or null when it is not one. Never resolves a name. */
    static String address(String answer) {
        if (answer == null || !(IPV4.matcher(answer).matches() || IPV6.matcher(answer).matches())) {
            return null;
        }
        try {
            return InetAddress.getByName(answer).getHostAddress();
        } catch (IOException e) {
            return null;
        }
    }

    private static Double millis(Duration latency) {
        return latency == null ? null : latency.toNanos() / 1e6;
    }

    private Map<String, Object> result(boolean ok, String ip, Double latency, String message, Instant started) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", ok);
        out.put("exitIp", ip);
        out.put("latencyMs", latency);
        out.put("message", message);
        out.put("checkedAt", started);
        return out;
    }
}
