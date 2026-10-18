package ai.foresignal.exchange;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The HTTP side of exchange with 113 peers (prompt 132): package and receipt deliveries ({@code POST {peer}/v1/exchange/
 * inbox} and {@code /receipts}, with the peer's exchange key as Bearer token) and the sender's JWKS
 * ({@code {peer}/.well-known/jwks.json}, always from the peer's own base URL). Every request passes the outbound URL
 * policy ({@code LAN_ALLOWED}, as 113's push does) and goes to the resolved, checked address.
 */
@Component
public class PeerExchangeClient {

    /** The largest JWKS read. */
    static final int MAX_JWKS = 256 * 1024;

    private static final PinnedHttp.ClientSpec CLIENT =
            new PinnedHttp.ClientSpec(Duration.ofSeconds(10), HttpClient.Version.HTTP_1_1);

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final OutboundUrlPolicy policy;
    private final PinnedHttp pinned;
    private final JsonMapper json;

    /** Creates the client. */
    public PeerExchangeClient(OutboundUrlPolicy policy, PinnedHttp pinned, JsonMapper json) {
        this.policy = policy;
        this.pinned = pinned;
        this.json = json;
    }

    /**
     * A peer's answer.
     *
     * @param status the HTTP status
     * @param body the body
     */
    public record Response(int status, byte[] body) {

        /** True for a 2xx status. */
        public boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    /** A delivery that could not be made. */
    public static final class DeliveryException extends Exception {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public DeliveryException(String message) {
            super(message);
        }
    }

    /**
     * POSTs {@code body} to {@code base + path}.
     *
     * @param base the peer's base URL
     * @param path the path ({@code /v1/exchange/inbox})
     * @param contentType the content type
     * @param body the body
     * @param headers extra headers (the Bearer token)
     * @return the answer
     * @throws DeliveryException when the URL is refused or the peer cannot be reached
     */
    public Response post(String base, String path, String contentType, byte[] body, Map<String, String> headers)
            throws DeliveryException {
        URI target = URI.create(trim(base) + path);
        OutboundUrlPolicy.Result check = policy.check(target, OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        if (!(check instanceof OutboundUrlPolicy.Allowed allowed)) {
            throw new DeliveryException("the outbound URL policy refuses " + target.getHost());
        }
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(target, allowed.pinned()))
                    .header("Content-Type", contentType)
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body));
            for (Map.Entry<String, String> h : headers.entrySet()) {
                builder.header(h.getKey(), h.getValue());
            }
            PinnedHttp.setHostHeader(builder, target);
            HttpResponse<byte[]> response = pinned.client(target, CLIENT).send(builder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            return new Response(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new DeliveryException("the peer could not be reached: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeliveryException("interrupted");
        }
    }

    /**
     * Fetches a peer's JWKS from its own base URL.
     *
     * @throws DeliveryException when it cannot be fetched or parsed
     */
    public Map<String, Object> jwks(String base) throws DeliveryException {
        URI target = URI.create(trim(base) + "/.well-known/jwks.json");
        OutboundUrlPolicy.Result check = policy.check(target, OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        if (!(check instanceof OutboundUrlPolicy.Allowed allowed)) {
            throw new DeliveryException("the outbound URL policy refuses " + target.getHost());
        }
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(target, allowed.pinned()))
                    .header("Accept", "application/json").timeout(Duration.ofSeconds(20)).GET();
            PinnedHttp.setHostHeader(builder, target);
            HttpResponse<byte[]> response = pinned.client(target, CLIENT).send(builder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200 || response.body() == null || response.body().length > MAX_JWKS) {
                throw new DeliveryException("the peer's JWKS answered " + response.statusCode());
            }
            return json.readValue(response.body(), MAP);
        } catch (IOException e) {
            throw new DeliveryException("the peer's JWKS could not be fetched: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeliveryException("interrupted");
        } catch (RuntimeException e) {
            throw new DeliveryException("the peer's JWKS is not JSON: " + e.getMessage());
        }
    }

    private static String trim(String base) {
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
