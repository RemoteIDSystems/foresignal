package ai.foresignal.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The client device behind a request (prompt 032; shared with 033's per-device read cursors).
 *
 * <p>A device is identified by its API token: the token id is the device id when the token carries
 * 023 device metadata ({@code PUT /v1/tokens/current/device}). A token without device metadata (a
 * script, an agent) is not a device.
 */
@Component
public class ClientDeviceResolver {

    /**
     * A client device.
     *
     * @param deviceId the device identity (the API token id)
     * @param info the token's device metadata
     */
    public record ClientDevice(UUID deviceId, DeviceInfo info) {}

    /** The device of the current request, if it is served by a token with device metadata. */
    public Optional<ClientDevice> currentDevice() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Optional.empty();
        }
        return deviceOf(attributes.getRequest());
    }

    /** The device of {@code request}, if its token carries device metadata. */
    public Optional<ClientDevice> deviceOf(HttpServletRequest request) {
        return CurrentToken.of(request).flatMap(current -> deviceOf(current.token()));
    }

    /** The device of {@code token}, if it carries device metadata. */
    public Optional<ClientDevice> deviceOf(AuthenticatedToken token) {
        if (token == null || token.device() == null) {
            return Optional.empty();
        }
        return Optional.of(new ClientDevice(token.id(), token.device()));
    }
}
