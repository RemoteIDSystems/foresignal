package ai.foresignal.settings;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Reads and validates the CORS origin lists (FR-33).
 *
 * <p>Validation happens here rather than in a controller because the same rules apply to a settings
 * import as to {@code PUT /v1/settings/cors}, which arrives in prompt 016.
 */
@Service
public class CorsSettingsService {

    /** Means "any origin". Only meaningful for reads. */
    public static final String WILDCARD = "*";

    private final AppSettingService settings;

    public CorsSettingsService(AppSettingService settings) {
        this.settings = settings;
    }

    /** The stored settings, or the defaults when none have been saved. */
    public CorsSettings get() {
        return settings.getOrDefault(SettingKeys.CORS, CorsSettings.class, CorsSettings::defaults);
    }

    /** Validates and stores {@code candidate}. */
    public void update(CorsSettings candidate) {
        CorsSettings validated = validate(candidate);
        settings.put(SettingKeys.CORS, validated);
    }

    /**
     * Checks every origin is {@code *} or a bare {@code scheme://host[:port]}.
     *
     * <p>A browser's {@code Origin} header is exactly that — no path, no query, no trailing slash —
     * so an entry with a path could never match and would look like a silent misconfiguration.
     */
    CorsSettings validate(CorsSettings candidate) {
        if (candidate == null) {
            throw new ValidationException("CORS settings are required.");
        }
        List<FieldError> errors = new ArrayList<>();
        checkOrigins("readOrigins", candidate.readOrigins(), true, errors);
        checkOrigins("writeOrigins", candidate.writeOrigins(), false, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The CORS settings are not valid.", List.copyOf(errors));
        }
        return candidate;
    }

    private static void checkOrigins(
            String field, List<String> origins, boolean wildcardAllowed, List<FieldError> errors) {
        for (String origin : origins) {
            if (origin == null || origin.isBlank()) {
                errors.add(new FieldError(field, "must not contain a blank origin"));
                continue;
            }
            String trimmed = origin.trim();
            if (WILDCARD.equals(trimmed)) {
                if (!wildcardAllowed) {
                    // Allowing any origin to perform authenticated writes would hand every page the
                    // user visits the ability to reconfigure the broker.
                    errors.add(
                            new FieldError(
                                    field,
                                    "must not be '*'; cross-origin writes require an explicit origin list"));
                }
                continue;
            }
            if (!isValidOrigin(trimmed)) {
                errors.add(
                        new FieldError(
                                field,
                                "'"
                                        + trimmed
                                        + "' is not an origin; expected scheme://host[:port], for example"
                                        + " http://localhost:5173"));
            }
        }
    }

    static boolean isValidOrigin(String origin) {
        try {
            URI uri = new URI(origin);
            return uri.getScheme() != null
                    && uri.getHost() != null
                    && (uri.getPath() == null || uri.getPath().isEmpty())
                    && uri.getQuery() == null
                    && uri.getFragment() == null
                    && uri.getUserInfo() == null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
