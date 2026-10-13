package ai.foresignal.source;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Coerces and checks a source's parameters against its declared {@link ParamSpec}s.
 *
 * <p>Runs at the API boundary, so bad configuration is refused before it can become an upstream
 * request. Every error is collected rather than the first being thrown, because an operator pasting
 * a settings block wants to see everything wrong with it at once.
 *
 * <p>Unknown keys are rejected rather than ignored: a typo in a parameter name would otherwise look
 * like it worked while the adapter silently used its default.
 *
 * <p>A {@link ParamType#LAN_URL} passes {@link OutboundUrlPolicy} profile {@code LAN_ONLY} (prompt
 * 035, replacing prompt 004's {@code LocalAddresses} check); when any parameter is refused by the
 * policy, the 400 carries code {@code URL_NOT_ALLOWED}.
 */
@Component
public class ParamValidator {

    private final InboxFiles inboxFiles;
    private final OutboundUrlPolicy policy;
    private final ObjectProvider<SourceRegistry> registry;

    /** Creates the validator without cross-parameter checks (tests). */
    public ParamValidator(InboxFiles inboxFiles, OutboundUrlPolicy policy) {
        this(inboxFiles, policy, null);
    }

    /**
     * Creates the validator.
     *
     * @param registry finds the adapter whose {@link SourceAdapter#checkParams} runs after the
     *     per-parameter checks (prompt 049), resolved lazily; null for none
     */
    @Autowired
    public ParamValidator(InboxFiles inboxFiles, OutboundUrlPolicy policy, ObjectProvider<SourceRegistry> registry) {
        this.inboxFiles = inboxFiles;
        this.policy = policy;
        this.registry = registry;
    }

    /** A {@link ParamType#LAN_URL} refused by the outbound URL policy. */
    private static final class UrlRefusedException extends InvalidInputException {

        private static final long serialVersionUID = 1L;

        UrlRefusedException(String detail) {
            super(detail);
        }
    }

    /**
     * Validates {@code raw} against {@code descriptor}, applying defaults.
     *
     * @throws ValidationException listing every problem, with field paths like {@code params.feed}
     */
    public ValidatedParams validate(SourceDescriptor descriptor, Map<String, Object> raw) {
        Map<String, Object> supplied = raw == null ? Map.of() : raw;
        List<FieldError> errors = new ArrayList<>();
        Map<String, Object> values = new LinkedHashMap<>();
        Map<String, InetAddress> pinned = new LinkedHashMap<>();
        Set<String> refused = new HashSet<>();

        for (String key : supplied.keySet()) {
            if (descriptor.param(key) == null) {
                errors.add(
                        new FieldError(
                                path(key),
                                "is not a parameter of "
                                        + descriptor.id()
                                        + ". Known parameters: "
                                        + knownNames(descriptor)));
            }
        }

        for (ParamSpec spec : descriptor.params()) {
            Object value = supplied.get(spec.name());
            boolean present = value != null && !(value instanceof String s && s.isBlank());

            if (!present) {
                if (spec.required()) {
                    errors.add(new FieldError(path(spec.name()), "is required"));
                } else if (spec.hasDefault()) {
                    coerceInto(spec, spec.defaultValue(), values, pinned, errors, refused, true);
                }
                continue;
            }
            coerceInto(spec, value, values, pinned, errors, refused, false);
        }

        if (!errors.isEmpty()) {
            if (!refused.isEmpty()) {
                throw OutboundUrlPolicy.notAllowed(errors);
            }
            throw new ValidationException(
                    "The parameters for " + descriptor.id() + " are not valid.", errors);
        }
        ValidatedParams validated = new ValidatedParams(values, pinned);
        crossCheck(descriptor, validated, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(
                    "The parameters for " + descriptor.id() + " are not valid.", errors);
        }
        return validated;
    }

    /** Runs the adapter's {@link SourceAdapter#checkParams}, when there is an adapter (prompt 049). */
    private void crossCheck(SourceDescriptor descriptor, ValidatedParams params, List<FieldError> errors) {
        SourceRegistry sources = registry == null ? null : registry.getIfAvailable();
        if (sources == null) {
            return;
        }
        SourceAdapter adapter = sources.find(descriptor.id())
                .flatMap(SourceRegistry.Registration::adapterIfPresent)
                .orElse(null);
        if (adapter == null) {
            return;
        }
        adapter.checkParams(params).forEach((name, problem) -> errors.add(new FieldError(path(name), problem)));
    }

    private void coerceInto(
            ParamSpec spec,
            Object raw,
            Map<String, Object> values,
            Map<String, InetAddress> pinned,
            List<FieldError> errors,
            Set<String> refused,
            boolean fromDefault) {
        try {
            Object coerced = coerce(spec, raw, pinned);
            checkAllowed(spec, coerced);
            values.put(spec.name(), coerced);
        } catch (InvalidInputException e) {
            if (e instanceof UrlRefusedException) {
                refused.add(spec.name());
            }
            String message = fromDefault ? "has an invalid default: " + e.getMessage() : e.getMessage();
            errors.add(new FieldError(path(spec.name()), message));
        }
    }

    private Object coerce(ParamSpec spec, Object raw, Map<String, InetAddress> pinned) {
        return switch (spec.type()) {
            case STRING -> asString(spec, raw);
            case INTEGER -> asInteger(spec, raw);
            case NUMBER -> asNumber(spec, raw);
            case BOOLEAN -> asBoolean(spec, raw);
            case DURATION -> asDuration(spec, raw);
            case BBOX -> asBbox(spec, raw);
            case STRING_LIST -> asStringList(spec, raw);
            case LAN_URL -> asLanUrl(spec, raw, pinned);
            case INBOX_PATH -> asInboxPath(spec, raw);
        };
    }

    // ---------- coercion per type ----------

    private static String asString(ParamSpec spec, Object raw) {
        if (raw instanceof String s) {
            return s.trim();
        }
        if (raw instanceof Number || raw instanceof Boolean) {
            return String.valueOf(raw);
        }
        throw invalid(spec, raw, "a string");
    }

    private static Integer asInteger(ParamSpec spec, Object raw) {
        if (raw instanceof Integer i) {
            return i;
        }
        if (raw instanceof Long l) {
            return toIntExact(spec, l);
        }
        if (raw instanceof Number n) {
            double value = n.doubleValue();
            if (value != Math.rint(value)) {
                throw new InvalidInputException(
                        "must be a whole number, but was " + describe(raw));
            }
            return toIntExact(spec, (long) value);
        }
        if (raw instanceof String s) {
            try {
                return Integer.valueOf(s.trim());
            } catch (NumberFormatException e) {
                throw invalid(spec, raw, "an integer");
            }
        }
        throw invalid(spec, raw, "an integer");
    }

    private static Integer toIntExact(ParamSpec spec, long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new InvalidInputException("must fit in a 32-bit integer, but was " + value);
        }
        return (int) value;
    }

    private static Double asNumber(ParamSpec spec, Object raw) {
        if (raw instanceof Number n) {
            return checkFinite(n.doubleValue());
        }
        if (raw instanceof String s) {
            try {
                return checkFinite(Double.parseDouble(s.trim()));
            } catch (NumberFormatException e) {
                throw invalid(spec, raw, "a number");
            }
        }
        throw invalid(spec, raw, "a number");
    }

    private static Double checkFinite(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new InvalidInputException("must be a finite number, but was " + value);
        }
        return value;
    }

    private static Boolean asBoolean(ParamSpec spec, Object raw) {
        if (raw instanceof Boolean b) {
            return b;
        }
        if (raw instanceof String s) {
            String trimmed = s.trim().toLowerCase();
            if (trimmed.equals("true")) {
                return Boolean.TRUE;
            }
            if (trimmed.equals("false")) {
                return Boolean.FALSE;
            }
        }
        throw invalid(spec, raw, "true or false");
    }

    private static Duration asDuration(ParamSpec spec, Object raw) {
        if (raw instanceof Duration d) {
            return d;
        }
        if (raw instanceof Number n) {
            return Duration.ofSeconds(n.longValue());
        }
        if (raw instanceof String s) {
            // Durations already reports invalid input with a message written for an API caller.
            return Durations.parse(s);
        }
        throw invalid(spec, raw, "a duration");
    }

    /** {@code minLon,minLat,maxLon,maxLat}, as a string or a 4-element list. */
    private static Envelope asBbox(ParamSpec spec, Object raw) {
        List<Double> parts = new ArrayList<>(4);
        if (raw instanceof Envelope envelope) {
            return envelope;
        }
        if (raw instanceof String s) {
            String[] pieces = s.split(",");
            if (pieces.length != 4) {
                throw new InvalidInputException(
                        "must be minLon,minLat,maxLon,maxLat, but had "
                                + pieces.length
                                + " value(s): '"
                                + s
                                + "'");
            }
            for (String piece : pieces) {
                parts.add(parseBboxNumber(piece.trim()));
            }
        } else if (raw instanceof List<?> list) {
            if (list.size() != 4) {
                throw new InvalidInputException(
                        "must have 4 values (minLon, minLat, maxLon, maxLat), but had " + list.size());
            }
            for (Object element : list) {
                parts.add(parseBboxNumber(String.valueOf(element)));
            }
        } else {
            throw invalid(spec, raw, "a bounding box");
        }

        double minLon = parts.get(0);
        double minLat = parts.get(1);
        double maxLon = parts.get(2);
        double maxLat = parts.get(3);

        List<String> problems = new ArrayList<>();
        if (minLon < -180.0 || minLon > 180.0) {
            problems.add("minLon " + minLon + " is outside -180..180");
        }
        if (maxLon < -180.0 || maxLon > 180.0) {
            problems.add("maxLon " + maxLon + " is outside -180..180");
        }
        if (minLat < -90.0 || minLat > 90.0) {
            problems.add("minLat " + minLat + " is outside -90..90");
        }
        if (maxLat < -90.0 || maxLat > 90.0) {
            problems.add("maxLat " + maxLat + " is outside -90..90");
        }
        if (minLat > maxLat) {
            problems.add("minLat " + minLat + " is above maxLat " + maxLat);
        }
        // minLon > maxLon is allowed: that is a box crossing the antimeridian.
        if (!problems.isEmpty()) {
            throw new InvalidInputException(String.join("; ", problems));
        }
        return new Envelope(minLon, maxLon, minLat, maxLat);
    }

    private static double parseBboxNumber(String text) {
        try {
            return checkFinite(Double.parseDouble(text));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("'" + text + "' is not a number");
        }
    }

    private static List<String> asStringList(ParamSpec spec, Object raw) {
        if (raw instanceof List<?> list) {
            List<String> result = new ArrayList<>(list.size());
            for (Object element : list) {
                if (element == null) {
                    throw new InvalidInputException("must not contain a null entry");
                }
                result.add(String.valueOf(element).trim());
            }
            return List.copyOf(result);
        }
        if (raw instanceof String s) {
            if (s.isBlank()) {
                return List.of();
            }
            return Arrays.stream(s.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
        }
        throw invalid(spec, raw, "a list of strings");
    }

    /**
     * A local-network URL, resolved once, checked under {@code LAN_ONLY} and pinned.
     *
     * <p>The resolved address goes into {@link ValidatedParams#pinnedAddress}, and prompt 005
     * connects to that address with the original Host header and redirects disabled. Validating the
     * name alone would be useless: a name that resolves privately now can resolve to anything on
     * the next lookup, which is DNS rebinding. {@code UpstreamClient} validates the parameters
     * again whenever it binds a source's HTTP client, so the check also runs at fetch time.
     */
    private URI asLanUrl(ParamSpec spec, Object raw, Map<String, InetAddress> pinned) {
        String text = asString(spec, raw);
        URI uri;
        try {
            uri = new URI(text);
        } catch (URISyntaxException e) {
            throw new InvalidInputException("'" + text + "' is not a URL");
        }
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new InvalidInputException(
                    "'" + text + "' must be an absolute URL, for example http://192.168.1.50:8080");
        }
        String scheme = uri.getScheme().toLowerCase();
        if (!scheme.equals("http") && !scheme.equals("https") && !scheme.equals("ws") && !scheme.equals("wss")) {
            throw new InvalidInputException(
                    "scheme '" + uri.getScheme() + "' is not allowed; use http, https, ws or wss");
        }

        OutboundUrlPolicy.Result result = policy.check(uri, OutboundUrlPolicy.Profile.LAN_ONLY);
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            if (denied.unresolved()) {
                throw new InvalidInputException("the host '" + uri.getHost() + "' could not be resolved");
            }
            throw new UrlRefusedException(denied.reason());
        }
        InetAddress resolved = ((OutboundUrlPolicy.Allowed) result).pinned();
        pinned.put(spec.name(), resolved);
        return uri;
    }

    /** A path whose realpath must sit inside the inbox directory. */
    private Path asInboxPath(ParamSpec spec, Object raw) {
        String text = asString(spec, raw);
        Path candidate;
        try {
            Path given = Path.of(text);
            candidate = given.isAbsolute() ? given : inboxFiles.root().resolve(given);
        } catch (InvalidPathException e) {
            throw new InvalidInputException("'" + text + "' is not a valid path");
        }
        return inboxFiles.requireInside(candidate);
    }

    // ---------- shared ----------

    private static void checkAllowed(ParamSpec spec, Object coerced) {
        if (spec.allowed().isEmpty()) {
            return;
        }
        if (coerced instanceof List<?> list) {
            for (Object element : list) {
                if (!spec.allowed().contains(String.valueOf(element))) {
                    throw new InvalidInputException(
                            "contains '" + element + "', which is not one of " + spec.allowed());
                }
            }
            return;
        }
        if (!spec.allowed().contains(String.valueOf(coerced))) {
            throw new InvalidInputException(
                    "must be one of " + spec.allowed() + ", but was '" + coerced + "'");
        }
    }

    private static InvalidInputException invalid(ParamSpec spec, Object raw, String expected) {
        return new InvalidInputException("must be " + expected + ", but was " + describe(raw));
    }

    private static String describe(Object raw) {
        if (raw == null) {
            return "null";
        }
        String text = String.valueOf(raw);
        if (text.length() > 60) {
            text = text.substring(0, 60) + "…";
        }
        return "'" + text + "' (" + raw.getClass().getSimpleName() + ")";
    }

    private static String path(String name) {
        return "params." + name;
    }

    private static String knownNames(SourceDescriptor descriptor) {
        return descriptor.params().stream().map(ParamSpec::name).sorted().toList().toString();
    }
}
