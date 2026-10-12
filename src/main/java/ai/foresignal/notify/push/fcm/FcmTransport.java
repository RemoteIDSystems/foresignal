package ai.foresignal.notify.push.fcm;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.NativePushEndpoints;
import ai.foresignal.notify.push.NativePushHttp;
import ai.foresignal.notify.push.NativePushProperties;
import ai.foresignal.notify.push.NativePushTransport;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushOptions;
import ai.foresignal.notify.push.PushResult;
import ai.foresignal.notify.push.PushTarget;
import ai.foresignal.notify.push.provider.ProviderCredentials;
import ai.foresignal.notify.push.provider.PushProviderService;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Delivers native pushes through the FCM HTTP v1 API (prompt 032): {@code POST
 * https://fcm.googleapis.com/v1/projects/<projectId>/messages:send} with an OAuth 2.0 access token
 * from {@link FcmAccessTokenCache} and the body of {@link FcmPayloads}. The legacy FCM APIs are gone
 * and are not used.
 *
 * <p>Errors (Google's FCM error codes, read from the {@code FcmError} detail, else the
 * {@code status}):
 * <ul>
 *   <li>200 → sent; the provider's error is cleared.
 *   <li>404 {@code UNREGISTERED}, 403 {@code SENDER_ID_MISMATCH}, and 400 {@code INVALID_ARGUMENT}
 *       whose field violation names the token → gone; the device is deleted.
 *   <li>401 → the access token is dropped and the push retried once; a second 401, or
 *       {@code THIRD_PARTY_AUTH_ERROR} or another 403 → provider error (last error, one system
 *       notification, retried later).
 *   <li>429 {@code QUOTA_EXCEEDED} → retried after {@code Retry-After} (at least a minute).
 *   <li>503 {@code UNAVAILABLE} → after {@code Retry-After} when given; 500 {@code INTERNAL} and
 *       other 5xx → the 018 ladder.
 *   <li>Other 4xx → failed for good.
 * </ul>
 */
@Component
public class FcmTransport implements NativePushTransport {

    private static final Logger log = LoggerFactory.getLogger(FcmTransport.class);

    /** The HTTP client key of FCM sends. */
    static final String CLIENT_KEY = "fcm";

    static final Duration QUOTA_WAIT = Duration.ofMinutes(1);

    private final PushProviderService providers;
    private final FcmAccessTokenCache tokens;
    private final NativePushHttp http;
    private final NativePushProperties properties;
    private final NativePushEndpoints endpoints;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the transport. */
    public FcmTransport(PushProviderService providers, FcmAccessTokenCache tokens, NativePushHttp http,
            NativePushProperties properties, NativePushEndpoints endpoints, JsonMapper json, Clock clock) {
        this.endpoints = endpoints;
        this.providers = providers;
        this.tokens = tokens;
        this.http = http;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String id() {
        return NativeDeviceTarget.FCM;
    }

    @Override
    public boolean supports(PushTarget target) {
        return target instanceof NativeDeviceTarget device && NativeDeviceTarget.FCM.equals(device.transport());
    }

    @Override
    public PushResult send(PushTarget target, PushMessage message, PushOptions options) {
        if (!(target instanceof NativeDeviceTarget device)) {
            return new PushResult.Rejected(null, null, "not an FCM device");
        }
        Optional<ProviderCredentials> credentials = providers.credentialsFor(NativeDeviceTarget.FCM, device.appId(), null);
        if (credentials.isEmpty()) {
            return new PushResult.Rejected(null, ErrorCodes.PUSH_PROVIDER_NOT_CONFIGURED,
                    "no enabled FCM provider serves this app");
        }
        return sendVia(credentials.get(), device, message, options);
    }

    @Override
    public PushResult sendVia(ProviderCredentials credentials, NativeDeviceTarget device, PushMessage message,
            PushOptions options) {
        if (!(credentials instanceof ProviderCredentials.Fcm fcm)) {
            return new PushResult.Rejected(null, null, "not an FCM provider");
        }
        URI uri = URI.create(endpoints.fcm() + "/v1/projects/"
                + URLEncoder.encode(fcm.projectId(), StandardCharsets.UTF_8) + "/messages:send");
        byte[] body = json.writeValueAsBytes(FcmPayloads.message(device, message, options, clock.instant()));
        try {
            NativePushHttp.Response response = post(fcm, uri, body);
            if (response.status() == 401) {
                tokens.invalidate(fcm.providerId());
                response = post(fcm, uri, body);
            }
            return map(fcm, response);
        } catch (NativePushException e) {
            String error = "FCM " + e.getMessage();
            providers.providerError(fcm.providerId(), error);
            return new PushResult.Retry(properties.providerRetry(), true, null, "provider: " + error);
        } catch (IOException e) {
            return new PushResult.Failed(null, "FCM I/O error: " + e.getClass().getSimpleName());
        }
    }

    private NativePushHttp.Response post(ProviderCredentials.Fcm fcm, URI uri, byte[] body) throws IOException {
        String accessToken = tokens.token(fcm);
        return http.post(CLIENT_KEY, uri, Map.of("authorization", "Bearer " + accessToken,
                "content-type", "application/json; charset=UTF-8"), body);
    }

    private PushResult map(ProviderCredentials.Fcm fcm, NativePushHttp.Response response) {
        int status = response.status();
        Error error = error(response);
        if (status == 200) {
            providers.providerOk(fcm.providerId());
            return new PushResult.Sent(status);
        }
        if ("UNREGISTERED".equals(error.code()) || "SENDER_ID_MISMATCH".equals(error.code())
                || (status == 404 && error.code() == null)
                || ("INVALID_ARGUMENT".equals(error.code()) && error.namesToken())) {
            return new PushResult.Gone(status);
        }
        if (status == 401 || status == 403 || "THIRD_PARTY_AUTH_ERROR".equals(error.code())) {
            String text = ("FCM " + status + " " + (error.code() == null ? "" : error.code())).strip();
            log.warn("FCM provider {} was refused: {}", fcm.providerId(), text);
            providers.providerError(fcm.providerId(), text);
            return new PushResult.Retry(properties.providerRetry(), true, status, "provider: " + text);
        }
        if (status == 429) {
            Duration wait = retryAfter(response).filter(d -> d.compareTo(QUOTA_WAIT) >= 0).orElse(QUOTA_WAIT);
            return new PushResult.Retry(wait, true, status, "FCM 429 " + (error.code() == null ? "" : error.code()));
        }
        if (status == 503 && retryAfter(response).isPresent()) {
            return new PushResult.Retry(retryAfter(response).get(), true, status, "FCM 503 UNAVAILABLE");
        }
        if (status >= 500) {
            return new PushResult.Failed(status, "FCM " + status + (error.code() == null ? "" : " " + error.code()));
        }
        return new PushResult.Rejected(status, error.code(), "FCM refused the push (" + status + ")");
    }

    @Override
    public void mint(ProviderCredentials credentials) {
        if (!(credentials instanceof ProviderCredentials.Fcm fcm)) {
            throw new NativePushException("not an FCM provider");
        }
        tokens.invalidate(fcm.providerId());
        tokens.token(fcm);
    }

    /**
     * An FCM error.
     *
     * @param code the {@code FcmError.errorCode}, else the error {@code status}, or null
     * @param namesToken true when a {@code BadRequest} field violation names {@code message.token}
     */
    record Error(String code, boolean namesToken) {}

    Error error(NativePushHttp.Response response) {
        try {
            JsonNode error = json.readTree(response.body()).path("error");
            String code = null;
            boolean namesToken = false;
            for (JsonNode detail : error.path("details")) {
                String type = detail.path("@type").asString("");
                if (type.endsWith("google.firebase.fcm.v1.FcmError") && detail.path("errorCode").isString()) {
                    code = detail.path("errorCode").asString();
                }
                if (type.endsWith("google.rpc.BadRequest")) {
                    for (JsonNode violation : detail.path("fieldViolations")) {
                        if (violation.path("field").asString("").contains("token")) {
                            namesToken = true;
                        }
                    }
                }
            }
            if (code == null && error.path("status").isString()) {
                code = error.path("status").asString();
            }
            if (code != null && !code.matches("^[A-Z_]{1,64}$")) {
                code = null;
            }
            return new Error(code, namesToken);
        } catch (RuntimeException e) {
            return new Error(null, false);
        }
    }

    static Optional<Duration> retryAfter(NativePushHttp.Response response) {
        return response.header("Retry-After").flatMap(value -> {
            try {
                long seconds = Long.parseLong(value.strip());
                return seconds >= 0 ? Optional.of(Duration.ofSeconds(Math.min(seconds, 86_400))) : Optional.empty();
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }
}
