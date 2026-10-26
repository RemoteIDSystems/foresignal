package ai.foresignal.settings;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.SecureRequestMonitor;
import ai.foresignal.system.PublicBaseUrlWarnings;
import ai.foresignal.system.SystemWarnings;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * {@code getServerSettings} and {@code updateServerSettings} (prompt 023), shared with the settings
 * import.
 *
 * <ul>
 *   <li>{@code publicBaseUrl}: an absolute {@code http://} or {@code https://} URL without query or
 *       fragment, or null for "the request's own scheme, host and port". Prompt 018 already stored
 *       it under the {@code server} key, so nothing moves.
 *   <li>{@code timezone}: an IANA zone (default UTC).
 *   <li>{@code clientLinks}: deep-link templates ({@code alert}, {@code record},
 *       {@code briefingRun}, {@code pair}, {@code share}, and from R2-007 {@code activate} and
 *       {@code passwordReset} for the account emails); each must use the {@code foresignal://} scheme or a
 *       verified {@code https://} link.
 *   <li>{@code push} (prompt 031): {@link PushSettings} — the VAPID contact, the private-host
 *       allowlist for push endpoints and the default payload language.
 *   <li>{@code contactEmail} (prompt 047): an optional operator address appended to the upstream
 *       User-Agent, at most 254 characters; null removes it.
 * </ul>
 *
 * <p>Responses carry {@code warnings} ({@link PublicBaseUrlWarnings}); {@code warnings} in a
 * request are ignored (read-only).
 */
@Service
public class ServerSettingsService {

    static final Set<String> MEMBERS = Set.of("publicBaseUrl", "timezone", "clientLinks", "warnings", "push",
            "contactEmail");

    /** A plain address check for {@code contactEmail}: one {@code @}, no spaces, no header-breaking characters. */
    static final java.util.regex.Pattern EMAIL = java.util.regex.Pattern.compile("[^\\s@;()<>\",]+@[^\\s@;()<>\",]+\\.[^\\s@;()<>\",]+");

    static final Set<String> LINK_MEMBERS = Set.of("alert", "record", "briefingRun", "pair", "share", "activate",
            "passwordReset");

    /**
     * One {@code SettingsWarning}.
     *
     * @param code the stable code
     * @param message what to do about it
     */
    public record Warning(String code, String message) {}

    /**
     * The API form: the settings in force plus their warnings.
     *
     * @param publicBaseUrl the stored base URL, or null
     * @param timezone the timezone (UTC when unset)
     * @param clientLinks the templates in force
     * @param warnings the warnings that apply
     * @param push the push settings in force (031)
     * @param contactEmail the User-Agent contact (047), or null
     */
    public record View(String publicBaseUrl, String timezone, ClientLinks clientLinks, List<Warning> warnings,
            PushSettings push, String contactEmail) {}

    private final AppSettingService settings;
    private final SecureRequestMonitor secureRequests;

    /** Creates the service. */
    public ServerSettingsService(AppSettingService settings, SecureRequestMonitor secureRequests) {
        this.settings = settings;
        this.secureRequests = secureRequests;
    }

    /** The stored settings, or the defaults. */
    public ServerSettings get() {
        return settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
    }

    /** The API view of the stored settings. */
    public View view() {
        return view(get());
    }

    /** The API view of {@code value}. */
    public View view(ServerSettings value) {
        List<Warning> warnings = new ArrayList<>();
        for (SystemWarnings.Warning w : PublicBaseUrlWarnings.of(value, secureRequests.sawHttps())) {
            warnings.add(new Warning(w.code(), w.message()));
        }
        return new View(value.publicBaseUrl(), value.timezone() == null ? ServerSettings.DEFAULT_TIMEZONE : value.timezone(),
                value.effectiveClientLinks(), warnings, value.effectivePush(), value.normalizedContactEmail());
    }

    /**
     * Validates and stores {@code body}, replacing the setting.
     *
     * @throws ValidationException listing every problem
     */
    public View update(JsonNode body) {
        ServerSettings value = parse(body);
        settings.put(SettingKeys.SERVER, value);
        return view(value);
    }

    /** Stores already validated settings (settings import). */
    public void apply(ServerSettings value) {
        settings.put(SettingKeys.SERVER, value);
    }

    /**
     * Parses and validates a {@code ServerSettings} body without storing it.
     *
     * @throws ValidationException listing every problem
     */
    public static ServerSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a ServerSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a ServerSettings member"));
            }
        }
        PushSettings push = PushSettings.parse(body.get("push"), errors);
        String base = null;
        if (!body.has("publicBaseUrl")) {
            errors.add(new FieldError("publicBaseUrl", "is required (null for the request's own address)"));
        } else {
            JsonNode node = body.get("publicBaseUrl");
            if (node.isString()) {
                base = baseUrl(node.asString(), errors);
            } else if (!node.isNull()) {
                errors.add(new FieldError("publicBaseUrl", "must be an http:// or https:// URL, or null"));
            }
        }
        String timezone = null;
        JsonNode zone = body.get("timezone");
        if (zone != null && !zone.isNull()) {
            if (!zone.isString()) {
                errors.add(new FieldError("timezone", "must be an IANA timezone such as Europe/London"));
            } else {
                try {
                    timezone = ZoneId.of(zone.asString().strip()).getId();
                } catch (DateTimeException e) {
                    errors.add(new FieldError("timezone", "'" + zone.asString() + "' is not an IANA timezone"));
                }
            }
        }
        ClientLinks links = clientLinks(body.get("clientLinks"), errors);
        String contactEmail = null;
        JsonNode contact = body.get("contactEmail");
        if (contact != null && !contact.isNull()) {
            String value = contact.isString() ? contact.asString().strip() : null;
            if (value == null || value.length() > 254 || !EMAIL.matcher(value).matches()) {
                errors.add(new FieldError("contactEmail", "must be an email address such as ops@example.org, or null"));
            } else {
                contactEmail = value;
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The server settings are not valid.", errors);
        }
        return new ServerSettings(base, timezone, links, push, contactEmail);
    }

    private static String baseUrl(String raw, List<FieldError> errors) {
        String value = raw.strip();
        if (value.isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                errors.add(new FieldError("publicBaseUrl", "must use http:// or https://"));
                return null;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                errors.add(new FieldError("publicBaseUrl", "must name a host"));
                return null;
            }
            if (uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getRawUserInfo() != null) {
                errors.add(new FieldError("publicBaseUrl", "must not carry credentials, a query or a fragment"));
                return null;
            }
            return value;
        } catch (URISyntaxException e) {
            errors.add(new FieldError("publicBaseUrl", "is not a valid URL"));
            return null;
        }
    }

    private static ClientLinks clientLinks(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("clientLinks", "must be an object"));
            return null;
        }
        for (String member : node.propertyNames()) {
            if (!LINK_MEMBERS.contains(member)) {
                errors.add(new FieldError("clientLinks." + member, "is not a ClientLinks member"));
            }
        }
        return new ClientLinks(
                template(node.get("alert"), "clientLinks.alert", errors),
                template(node.get("record"), "clientLinks.record", errors),
                template(node.get("briefingRun"), "clientLinks.briefingRun", errors),
                template(node.get("pair"), "clientLinks.pair", errors),
                template(node.get("share"), "clientLinks.share", errors),
                template(node.get("activate"), "clientLinks.activate", errors),
                template(node.get("passwordReset"), "clientLinks.passwordReset", errors));
    }

    private static String template(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(field, "must be a non-blank template"));
            return null;
        }
        String value = node.asString().strip();
        String lower = value.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("foresignal://") && !lower.startsWith("https://")) {
            errors.add(new FieldError(field, "must use the foresignal:// scheme or a verified https:// link"));
            return null;
        }
        return value;
    }
}
