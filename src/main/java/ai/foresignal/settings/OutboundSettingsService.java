package ai.foresignal.settings;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.Cidr;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link OutboundSettings} ({@code GET/PUT /v1/settings/outbound},
 * prompt 035). A PUT replaces the whole value; every problem is reported in one 400
 * {@code VALIDATION_FAILED} with its {@code field}. The policy drops its cached copy on the
 * {@link AppSettingChangedEvent} that the store publishes after commit.
 */
@Service
public class OutboundSettingsService {

    /** The members of {@code OutboundSettings}. */
    static final Set<String> FIELDS = Set.of("allowHosts", "allowCidrs", "denyHosts", "denyCidrs");

    /** The most entries per list. */
    static final int MAX_ENTRIES = 500;

    /** A host entry: a host name or IP literal, optionally {@code *.}-prefixed for subdomains. */
    private static final Pattern HOST_ENTRY = Pattern.compile(
            "^(\\*\\.)?[A-Za-z0-9_]([A-Za-z0-9_-]{0,61}[A-Za-z0-9_])?(\\.[A-Za-z0-9_]([A-Za-z0-9_-]{0,61}[A-Za-z0-9_])?)*\\.?$"
                    + "|^\\[?[0-9A-Fa-f:.]+\\]?$");

    private final AppSettingService settings;

    /** Creates the service. */
    public OutboundSettingsService(AppSettingService settings) {
        this.settings = settings;
    }

    /** The stored overrides, or none. */
    public OutboundSettings get() {
        return settings.getOrDefault(SettingKeys.OUTBOUND, OutboundSettings.class, OutboundSettings::defaults);
    }

    /**
     * Validates and stores {@code body}, replacing the overrides.
     *
     * @throws ValidationException listing every problem
     */
    public OutboundSettings update(JsonNode body) {
        OutboundSettings value = parse(body);
        settings.put(SettingKeys.OUTBOUND, value);
        return value;
    }

    /**
     * Parses an {@code OutboundSettings} body: arrays of strings; host entries non-blank host names
     * (or {@code *.suffix}, or IP literals); CIDR entries that parse.
     *
     * @throws ValidationException listing every problem
     */
    public static OutboundSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an OutboundSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not an OutboundSettings member"));
            }
        }
        List<String> allowHosts = hosts(body, "allowHosts", errors);
        List<String> allowCidrs = cidrs(body, "allowCidrs", errors);
        List<String> denyHosts = hosts(body, "denyHosts", errors);
        List<String> denyCidrs = cidrs(body, "denyCidrs", errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The outbound settings are not valid.", errors);
        }
        return new OutboundSettings(allowHosts, allowCidrs, denyHosts, denyCidrs);
    }

    private static List<String> hosts(JsonNode body, String field, List<FieldError> errors) {
        List<String> out = new ArrayList<>();
        List<String> values = strings(body, field, errors);
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            String path = field + "[" + i + "]";
            if (value == null) {
                continue;
            }
            if (value.isBlank()) {
                errors.add(new FieldError(path, "must be a non-blank host name"));
            } else if (value.strip().length() > 255 || !HOST_ENTRY.matcher(value.strip()).matches()) {
                errors.add(new FieldError(path, "must be a host name such as nas.lan, *.example.org or an IP literal"));
            } else {
                out.add(value.strip());
            }
        }
        return out;
    }

    private static List<String> cidrs(JsonNode body, String field, List<FieldError> errors) {
        List<String> out = new ArrayList<>();
        List<String> values = strings(body, field, errors);
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            String path = field + "[" + i + "]";
            if (value == null) {
                continue;
            }
            if (Cidr.parse(value).isEmpty()) {
                errors.add(new FieldError(path, "must be an address block such as 192.168.1.0/24 or fd00::/8"));
            } else {
                out.add(Cidr.parse(value).get().toString());
            }
        }
        return out;
    }

    private static List<String> strings(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return out;
        }
        if (node.size() > MAX_ENTRIES) {
            errors.add(new FieldError(field, "must have at most " + MAX_ENTRIES + " entries"));
            return out;
        }
        int i = 0;
        for (JsonNode item : node) {
            if (!item.isString()) {
                errors.add(new FieldError(field + "[" + i + "]", "must be a string"));
                out.add(null);
            } else {
                out.add(item.asString());
            }
            i++;
        }
        return out;
    }
}
