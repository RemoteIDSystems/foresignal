package ai.foresignal.settings;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.net.Cidr;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Push settings inside {@link ServerSettings} (OpenAPI {@code PushSettings}, prompt 031).
 *
 * @param contact the VAPID {@code sub}: a {@code mailto:} or {@code https:} URI, default
 *     {@value #DEFAULT_CONTACT}
 * @param allowedPrivateHosts exact host names or address blocks allowed as push endpoints although
 *     they resolve to non-global addresses (a LAN UnifiedPush distributor); no wildcards, IPv4
 *     prefix at least /8, IPv6 at least /32
 * @param defaultLang the payload {@code lang} when a subscription has none, default
 *     {@value #DEFAULT_LANG}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PushSettings(String contact, List<String> allowedPrivateHosts, String defaultLang) {

    /** The default VAPID contact. */
    public static final String DEFAULT_CONTACT = "mailto:admin@localhost";

    /** The default payload language. */
    public static final String DEFAULT_LANG = "en";

    /** The most allowlist entries. */
    public static final int MAX_ALLOWED_HOSTS = 100;

    static final Set<String> MEMBERS = Set.of("contact", "allowedPrivateHosts", "defaultLang");

    private static final Pattern HOSTNAME = Pattern.compile(
            "^(?=.{1,253}$)[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)*$");

    /** A BCP 47-shaped language tag. */
    public static final Pattern LANG = Pattern.compile("^[A-Za-z]{2,8}(-[A-Za-z0-9]{1,8}){0,4}$");

    /** Fills defaults and copies the list. */
    public PushSettings {
        contact = contact == null || contact.isBlank() ? DEFAULT_CONTACT : contact.strip();
        allowedPrivateHosts = allowedPrivateHosts == null ? List.of() : List.copyOf(allowedPrivateHosts);
        defaultLang = defaultLang == null || defaultLang.isBlank() ? DEFAULT_LANG : defaultLang.strip();
    }

    /** The defaults. */
    public static PushSettings defaults() {
        return new PushSettings(null, null, null);
    }

    /**
     * Parses and validates a {@code PushSettings} object, adding problems to {@code errors} with the
     * field prefix {@code push.}.
     *
     * @return the settings, or null when {@code node} is null or JSON null
     */
    public static PushSettings parse(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("push", "must be an object"));
            return null;
        }
        for (String member : node.propertyNames()) {
            if (!MEMBERS.contains(member)) {
                errors.add(new FieldError("push." + member, "is not a PushSettings member"));
            }
        }
        String contact = null;
        JsonNode contactNode = node.get("contact");
        if (contactNode != null && !contactNode.isNull()) {
            if (!contactNode.isString() || !validContact(contactNode.asString().strip())) {
                errors.add(new FieldError("push.contact", "must be a mailto: or https: URI"));
            } else {
                contact = contactNode.asString().strip();
            }
        }
        List<String> hosts = new ArrayList<>();
        JsonNode hostsNode = node.get("allowedPrivateHosts");
        if (hostsNode != null && !hostsNode.isNull()) {
            if (!hostsNode.isArray()) {
                errors.add(new FieldError("push.allowedPrivateHosts", "must be an array of host names or CIDRs"));
            } else if (hostsNode.size() > MAX_ALLOWED_HOSTS) {
                errors.add(new FieldError("push.allowedPrivateHosts", "must have at most " + MAX_ALLOWED_HOSTS + " entries"));
            } else {
                int i = 0;
                for (JsonNode item : hostsNode) {
                    String field = "push.allowedPrivateHosts[" + i++ + "]";
                    if (!item.isString()) {
                        errors.add(new FieldError(field, "must be a string"));
                        continue;
                    }
                    String problem = allowedHostProblem(item.asString());
                    if (problem != null) {
                        errors.add(new FieldError(field, problem));
                    } else {
                        hosts.add(item.asString().strip().toLowerCase(Locale.ROOT));
                    }
                }
            }
        }
        String lang = null;
        JsonNode langNode = node.get("defaultLang");
        if (langNode != null && !langNode.isNull()) {
            if (!langNode.isString() || !LANG.matcher(langNode.asString().strip()).matches()) {
                errors.add(new FieldError("push.defaultLang", "must be a language tag such as en or pt-BR"));
            } else {
                lang = langNode.asString().strip();
            }
        }
        return new PushSettings(contact, hosts, lang);
    }

    /**
     * Why {@code entry} is not an acceptable {@code allowedPrivateHosts} entry, or null when it is:
     * an exact host name (no wildcard) or a CIDR with an IPv4 prefix of at least /8 and an IPv6
     * prefix of at least /32.
     */
    public static String allowedHostProblem(String entry) {
        String value = entry == null ? "" : entry.strip().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            return "must not be blank";
        }
        if (value.contains("*")) {
            return "wildcards are not allowed; list each host name exactly";
        }
        if (value.contains("/") || Cidr.literal(value.replace("[", "").replace("]", "")).isPresent()) {
            var cidr = Cidr.parse(value);
            if (cidr.isEmpty()) {
                return "is not a valid CIDR such as 192.168.1.0/24 or fd00::/48";
            }
            int min = cidr.get().ipv4() ? Cidr.MIN_ALLOWED_V4_PREFIX : Cidr.MIN_ALLOWED_V6_PREFIX;
            if (cidr.get().prefix() < min) {
                return "the prefix must be at least /" + min + (cidr.get().ipv4() ? " for IPv4" : " for IPv6");
            }
            return null;
        }
        if (!HOSTNAME.matcher(value).matches()) {
            return "is neither a host name nor a CIDR";
        }
        return null;
    }

    private static boolean validContact(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (scheme.equals("mailto")) {
                return uri.getSchemeSpecificPart() != null && uri.getSchemeSpecificPart().contains("@");
            }
            return scheme.equals("https") && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
