package ai.foresignal.notify.inbox;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ClientDeviceResolver;
import ai.foresignal.security.CurrentToken;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * The device key of an inbox request (prompt 033): the device of a token with 023 device metadata
 * ({@link ClientDeviceResolver#currentDevice()}, the token id); else the client-chosen
 * {@value #HEADER} header (a UUID of at most 64 characters) prefixed with the token id, so two
 * tokens never share read state by choosing the same id; else {@code 400 DEVICE_ID_REQUIRED}.
 */
@Component
public class DeviceKeys {

    /** The client-chosen device id header. */
    public static final String HEADER = "Foresignal-Device-Id";

    /** The longest header value. */
    static final int MAX_HEADER = 64;

    private final ClientDeviceResolver devices;

    /** Creates the resolver. */
    public DeviceKeys(ClientDeviceResolver devices) {
        this.devices = devices;
    }

    /**
     * The device key of {@code request}.
     *
     * @throws ValidationException {@code 400 DEVICE_ID_REQUIRED} when none can be derived, or the
     *     header is not a UUID
     */
    public String resolve(HttpServletRequest request) {
        Optional<ClientDeviceResolver.ClientDevice> device = devices.deviceOf(request);
        if (device.isPresent()) {
            return device.get().deviceId().toString();
        }
        Optional<CurrentToken> token = CurrentToken.of(request);
        String header = request.getHeader(HEADER);
        if (token.isEmpty() || header == null || header.isBlank()) {
            throw required("Send a Foresignal-Device-Id header (a UUID), or report device metadata for the token"
                    + " with PUT /v1/tokens/current/device.");
        }
        return of(token.get().id(), header);
    }

    /**
     * The key of a header-identified device of {@code tokenId}: {@code <tokenId>:<uuid>}.
     *
     * @throws ValidationException {@code 400 DEVICE_ID_REQUIRED} when {@code header} is not a UUID
     */
    public static String of(UUID tokenId, String header) {
        String value = header.strip();
        if (value.length() > MAX_HEADER) {
            throw required("Foresignal-Device-Id must be a UUID of at most 64 characters.");
        }
        try {
            return tokenId + ":" + UUID.fromString(value).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            throw required("Foresignal-Device-Id must be a UUID.");
        }
    }

    private static ValidationException required(String detail) {
        return new ValidationException(ErrorCodes.DEVICE_ID_REQUIRED, detail, List.of(new FieldError(HEADER, detail)));
    }
}
