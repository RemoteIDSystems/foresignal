package ai.foresignal.notify.push.device;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.NativePushProperties;
import ai.foresignal.notify.push.NativePushTransport;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushOptions;
import ai.foresignal.notify.push.PushResult;
import ai.foresignal.notify.push.PushTarget;
import ai.foresignal.notify.push.PushTopic;
import ai.foresignal.notify.push.Urgency;
import ai.foresignal.notify.push.provider.ProviderCredentials;
import ai.foresignal.notify.push.provider.PushProviderEntity;
import ai.foresignal.notify.push.provider.PushProviderService;
import ai.foresignal.security.AuthenticatedToken;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * {@code testPushProvider} and {@code testPushDevice} (prompt 032).
 *
 * <p>A provider test mints a fresh credential (an APNs provider JWT, an FCM OAuth access token) and,
 * when {@code deviceId} names a device of the provider's type, sends it a test alert through that
 * provider. A device test (owner only, at most {@code deviceTestsPerHour} per device, 5 by default,
 * else 429 {@code RATE_LIMITED}) sends a test alert through the provider that serves the device.
 * The test push is a minimal {@code system} notice ({@code id} 0) and is sent directly, not
 * through the Push channel. Results carry status codes and reason codes only.
 */
@Service
public class NativePushTester {

    private final PushProviderService providers;
    private final PushDeviceService devices;
    private final PushDeviceRepository deviceRows;
    private final NativeDeviceTargetSource targets;
    private final List<NativePushTransport> transports;
    private final NativePushProperties properties;
    private final Clock clock;
    private final Map<UUID, Deque<Instant>> deviceTests = new ConcurrentHashMap<>();

    /** Creates the tester. */
    public NativePushTester(PushProviderService providers, PushDeviceService devices, PushDeviceRepository deviceRows,
            NativeDeviceTargetSource targets, List<NativePushTransport> transports, NativePushProperties properties,
            Clock clock) {
        this.providers = providers;
        this.devices = devices;
        this.deviceRows = deviceRows;
        this.targets = targets;
        this.transports = List.copyOf(transports);
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Tests provider {@code providerId}; {@code body} may name a {@code deviceId}.
     *
     * @return the {@code PushTestResult}
     * @throws NotFoundException unknown provider or device
     */
    public Map<String, Object> testProvider(String providerId, JsonNode body) {
        PushProviderEntity provider = providers.get(providerId);
        UUID deviceId = deviceId(body);
        PushDeviceEntity device = null;
        if (deviceId != null) {
            device = deviceRows.findById(deviceId).filter(d -> d.getTransport().equals(provider.getType()))
                    .orElseThrow(() -> NotFoundException.of("Push device", deviceId));
        }
        NativePushTransport transport = transport(provider.getType());
        Optional<ProviderCredentials> credentials = providers.credentials(provider);
        if (credentials.isEmpty()) {
            String error = "the provider has no usable key";
            providers.recordTest(providerId, error);
            return result(false, false, null, null, null, error);
        }
        try {
            transport.mint(credentials.get());
        } catch (NativePushTransport.NativePushException e) {
            providers.recordTest(providerId, e.getMessage());
            return result(false, false, null, null, null, e.getMessage());
        }
        if (device == null) {
            providers.recordTest(providerId, null);
            return result(true, true, null, null, null, null);
        }
        Optional<PushTarget> target = targets.target(device);
        if (target.isEmpty()) {
            providers.recordTest(providerId, null);
            return result(false, true, false, null, null, "the device token cannot be read");
        }
        PushResult outcome = transport.sendVia(credentials.get(), (NativeDeviceTarget) target.get(), message(),
                options());
        targets.onOutcome(target.get(), outcome, clock.instant());
        providers.recordTest(providerId, outcome instanceof PushResult.Retry r && r.reason() != null
                && r.reason().startsWith("provider:") ? r.reason() : null);
        return outcome(outcome, true);
    }

    /**
     * Sends a test alert to {@code deviceId}, which {@code caller} must have registered.
     *
     * @throws NotFoundException when the caller does not own it
     * @throws ForesignalException 429 {@code RATE_LIMITED} past the hourly limit
     */
    public Map<String, Object> testDevice(UUID deviceId, AuthenticatedToken caller) {
        PushDeviceEntity device = devices.owned(deviceId, caller);
        rateLimit(deviceId);
        Optional<PushTarget> target = targets.target(device);
        if (target.isEmpty()) {
            return result(false, null, false, null, null, "the device token cannot be read");
        }
        PushResult outcome = transport(device.getTransport()).send(target.get(), message(), options());
        targets.onOutcome(target.get(), outcome, clock.instant());
        return outcome(outcome, null);
    }

    private void rateLimit(UUID deviceId) {
        Instant now = clock.instant();
        Deque<Instant> recent = deviceTests.computeIfAbsent(deviceId, id -> new ArrayDeque<>());
        synchronized (recent) {
            while (!recent.isEmpty() && !recent.peekFirst().isAfter(now.minus(Duration.ofHours(1)))) {
                recent.pollFirst();
            }
            if (recent.size() >= properties.deviceTestsPerHour()) {
                long wait = Math.max(1, Duration.between(now, recent.peekFirst().plus(Duration.ofHours(1))).toSeconds());
                throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                        "At most " + properties.deviceTestsPerHour() + " test pushes per device per hour.")
                        .withHeader("Retry-After", Long.toString(wait));
            }
            recent.addLast(now);
        }
    }

    /** Forgets the test counters (tests). */
    public void reset() {
        deviceTests.clear();
    }

    private NativePushTransport transport(String type) {
        return transports.stream().filter(t -> t.id().equals(type)).findFirst()
                .orElseThrow(() -> new IllegalStateException("no native push transport " + type));
    }

    /** The test push: a minimal system notice. */
    PushMessage message() {
        return new PushMessage("system", 0L, null, null, "info", "Foresignal test", "Test notification",
                "Test notification from Foresignal", null, null, null, List.of(), "foresignal-test",
                PushTopic.system("test"), false, clock.millis(), null, null, null, PushMessage.categoryOf("system"));
    }

    private static PushOptions options() {
        return new PushOptions(PushOptions.SYSTEM_TTL, Urgency.LOW, PushTopic.system("test"), false, false, null);
    }

    private static UUID deviceId(JsonNode body) {
        if (body == null || body.isNull() || body.isMissingNode()) {
            return null;
        }
        if (!body.isObject()) {
            throw new ValidationException("The request body must be a PushProviderTestRequest object.");
        }
        JsonNode id = body.get("deviceId");
        if (id == null || id.isNull()) {
            return null;
        }
        try {
            return UUID.fromString(id.asString());
        } catch (RuntimeException e) {
            throw ValidationException.forField("deviceId", "must be a device id");
        }
    }

    private static Map<String, Object> outcome(PushResult outcome, Boolean minted) {
        String reason = switch (outcome) {
            case PushResult.Sent s -> null;
            case PushResult.Gone g -> "the device token is no longer valid; the device was removed";
            case PushResult.Retry r -> r.reason();
            case PushResult.Rejected r -> r.code() == null ? r.reason() : r.code() + ": " + r.reason();
            case PushResult.Failed f -> f.reason();
        };
        boolean sent = outcome instanceof PushResult.Sent;
        return result(sent, minted == null ? sent || !(outcome instanceof PushResult.Retry) : minted, sent,
                outcome.status(), reason, null);
    }

    private static Map<String, Object> result(boolean success, Boolean minted, Boolean sent, Integer status,
            String reason, String error) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", success);
        out.put("credentialMinted", minted);
        out.put("sent", sent);
        out.put("status", status);
        out.put("reason", reason);
        out.put("error", error);
        return out;
    }
}
