package ai.foresignal.notify.push.apns;

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
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Delivers native pushes through the APNs HTTP/2 provider API with token-based authentication
 * (prompt 032).
 *
 * <p>{@code POST /3/device/<token>} on {@code api.push.apple.com} or
 * {@code api.sandbox.push.apple.com} (by the device's environment), port 443, one HTTP/2
 * connection per provider and environment, with the headers and body of {@link ApnsPayloads} and a
 * provider JWT from {@link ApnsJwtCache}.
 *
 * <p>Responses (Apple's "Handling notification responses from APNs"):
 * <ul>
 *   <li>200 → sent; the provider's error is cleared.
 *   <li>410 ({@code Unregistered}, {@code ExpiredToken}) and 400 {@code BadDeviceToken} /
 *       {@code DeviceTokenNotForTopic} → gone; the device is deleted.
 *   <li>403 {@code ExpiredProviderToken} → the JWT is regenerated once and the push retried.
 *   <li>Other 403 ({@code InvalidProviderToken}, {@code MissingProviderToken},
 *       {@code BadEnvironmentKeyIdInToken}, …) and 429 {@code TooManyProviderTokenUpdates} →
 *       provider error: {@code push_provider.last_error}, one system notification, retried later.
 *   <li>429 {@code TooManyRequests} → retried after {@code Retry-After} or a minute; 5xx → retried
 *       on the 018 ladder.
 *   <li>413 and other 4xx → failed for good.
 * </ul>
 * Reasons recorded anywhere are Apple's reason codes, never the device token or alert text.
 */
@Component
public class ApnsTransport implements NativePushTransport {

    private static final Logger log = LoggerFactory.getLogger(ApnsTransport.class);

    static final Set<String> GONE_400 = Set.of("BadDeviceToken", "DeviceTokenNotForTopic");
    static final Duration TOO_MANY_REQUESTS_WAIT = Duration.ofMinutes(1);

    private final PushProviderService providers;
    private final ApnsJwtCache jwts;
    private final NativePushHttp http;
    private final NativePushProperties properties;
    private final NativePushEndpoints endpoints;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the transport. */
    public ApnsTransport(PushProviderService providers, ApnsJwtCache jwts, NativePushHttp http,
            NativePushProperties properties, NativePushEndpoints endpoints, JsonMapper json, Clock clock) {
        this.endpoints = endpoints;
        this.providers = providers;
        this.jwts = jwts;
        this.http = http;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String id() {
        return NativeDeviceTarget.APNS;
    }

    @Override
    public boolean supports(PushTarget target) {
        return target instanceof NativeDeviceTarget device && NativeDeviceTarget.APNS.equals(device.transport());
    }

    @Override
    public PushResult send(PushTarget target, PushMessage message, PushOptions options) {
        if (!(target instanceof NativeDeviceTarget device)) {
            return new PushResult.Rejected(null, null, "not an APNs device");
        }
        Optional<ProviderCredentials> credentials = providers.credentialsFor(NativeDeviceTarget.APNS, device.appId(),
                device.environment());
        if (credentials.isEmpty()) {
            return new PushResult.Rejected(null, ErrorCodes.PUSH_PROVIDER_NOT_CONFIGURED,
                    "no enabled APNs provider serves this app and environment");
        }
        return sendVia(credentials.get(), device, message, options);
    }

    @Override
    public PushResult sendVia(ProviderCredentials credentials, NativeDeviceTarget device, PushMessage message,
            PushOptions options) {
        if (!(credentials instanceof ProviderCredentials.Apns apns)) {
            return new PushResult.Rejected(null, null, "not an APNs provider");
        }
        String environment = "sandbox".equals(device.environment()) ? "sandbox" : "production";
        URI uri = URI.create(endpoints.apns(environment) + "/3/device/" + device.token());
        byte[] body = ApnsPayloads.fit(json, message, options, device.contentLevel(), apns.timeSensitive());
        String clientKey = PushProviderService.apnsClientKey(apns.providerId(), environment);
        try {
            String jwt = jwts.token(apns);
            NativePushHttp.Response response = http.post(clientKey, uri,
                    ApnsPayloads.headers(jwt, device.appId(), message, options, clock.instant()), body);
            if (response.status() == 403 && "ExpiredProviderToken".equals(reason(response))) {
                jwt = jwts.forceRefresh(apns, jwt);
                response = http.post(clientKey, uri,
                        ApnsPayloads.headers(jwt, device.appId(), message, options, clock.instant()), body);
            }
            return map(apns, response);
        } catch (IOException e) {
            return new PushResult.Failed(null, "APNs I/O error: " + e.getClass().getSimpleName());
        } catch (IllegalStateException e) {
            providers.providerError(apns.providerId(), "APNs provider token could not be signed");
            return new PushResult.Retry(properties.providerRetry(), true, null, "provider: token could not be signed");
        }
    }

    private PushResult map(ProviderCredentials.Apns apns, NativePushHttp.Response response) {
        int status = response.status();
        String reason = reason(response);
        if (status == 200) {
            providers.providerOk(apns.providerId());
            return new PushResult.Sent(status);
        }
        if (status == 410 || (status == 400 && GONE_400.contains(reason))) {
            return new PushResult.Gone(status);
        }
        if (status == 403 || (status == 429 && "TooManyProviderTokenUpdates".equals(reason))) {
            String error = "APNs " + status + " " + (reason == null ? "" : reason);
            log.warn("APNs provider {} was refused: {}", apns.providerId(), error.strip());
            providers.providerError(apns.providerId(), error.strip());
            return new PushResult.Retry(properties.providerRetry(), true, status, "provider: " + error.strip());
        }
        if (status == 429) {
            return new PushResult.Retry(retryAfter(response).orElse(TOO_MANY_REQUESTS_WAIT), true, status,
                    "APNs 429 " + (reason == null ? "TooManyRequests" : reason));
        }
        if (status >= 500) {
            return new PushResult.Failed(status, "APNs " + status + (reason == null ? "" : " " + reason));
        }
        return new PushResult.Rejected(status, reason == null ? null : reason,
                "APNs refused the push (" + status + ")");
    }

    @Override
    public void mint(ProviderCredentials credentials) {
        if (!(credentials instanceof ProviderCredentials.Apns apns)) {
            throw new NativePushException("not an APNs provider");
        }
        try {
            jwts.token(apns);
        } catch (IllegalStateException e) {
            throw new NativePushException("the APNs provider token could not be signed");
        }
    }

    /** Apple's {@code reason} code, letters only, or null. */
    String reason(NativePushHttp.Response response) {
        if (response.body() == null || response.body().isBlank()) {
            return null;
        }
        try {
            JsonNode node = json.readTree(response.body());
            JsonNode reason = node == null ? null : node.get("reason");
            if (reason != null && reason.isString() && reason.asString().matches("^[A-Za-z]{1,64}$")) {
                return reason.asString();
            }
        } catch (RuntimeException e) {
            // not JSON: no reason
        }
        return null;
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
