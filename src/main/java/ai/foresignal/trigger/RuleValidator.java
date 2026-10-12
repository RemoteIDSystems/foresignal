package ai.foresignal.trigger;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerCondition;
import ai.foresignal.store.TriggerMode;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Validates and normalizes a {@code TriggerRuleInput} (prompt 017, task 1).
 *
 * <p>Field problems are collected and reported together as {@code 400 VALIDATION_FAILED}. Four
 * findings have their own code and win over the rest: {@code TRIGGER_PATH_VOLATILE} (a condition
 * reads a volatile field of an applicable source), {@code PLACES_UNAVAILABLE} ({@code near.placeId}
 * without a {@link PlaceLookup}), {@code FEATURE_UNAVAILABLE} ({@code forecastHorizon} without
 * {@code record_version.valid_from}, or a moving {@code near.anchor}, which arrives in 036) and
 * {@code GEOFENCE_TOO_SMALL_FOR_HYSTERESIS}.
 *
 * <p>Normalization: the deprecated {@code source}/{@code kind} fold into the arrays, numbers in
 * condition values become decimals, geofences are materialized by the
 * {@link GeofenceMaterializer} (lines buffered by {@code bufferMeters}, {@code near} buffered by its
 * radius, fixed and split at the antimeridian), and defaults follow the spec ({@code reappearAsEnter}
 * true with a fence, {@code autoResolve} all on for enter rules).
 */
@Component
public class RuleValidator {

    private static final Logger log = LoggerFactory.getLogger(RuleValidator.class);

    /** The condition path grammar. */
    public static final Pattern PATH = Pattern.compile("^[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)*$");

    /** The tag grammar. */
    public static final Pattern TAG = Pattern.compile("^[a-z0-9][a-z0-9_-]{0,31}$");

    /** Most sources or kinds per rule. */
    public static final int MAX_FILTER = 50;

    /** Most tags per rule. */
    public static final int MAX_TAGS = 16;

    /** Longest rule name. */
    public static final int MAX_NAME = 200;

    static final Pattern HH_MM = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    static final String VALID_FROM_PROBE =
            "SELECT count(*) FROM information_schema.columns WHERE table_name = 'record_version' AND column_name = 'valid_from'";

    private final SourceRegistry registry;
    private final OperatorRegistry operators;
    private final GeofenceMaterializer geofences;
    private final ObjectProvider<PlaceLookup> places;
    private final JdbcTemplate jdbc;
    private final GeoJsonReader geoJson = new GeoJsonReader();
    private volatile Boolean validFromPresent;

    /** Creates the validator. */
    public RuleValidator(
            SourceRegistry registry,
            OperatorRegistry operators,
            GeofenceMaterializer geofences,
            ObjectProvider<PlaceLookup> places,
            JdbcTemplate jdbc) {
        this.registry = registry;
        this.operators = operators;
        this.geofences = geofences;
        this.places = places;
        this.jdbc = jdbc;
    }

    /**
     * Validates {@code body}.
     *
     * @return the normalized rule
     * @throws ValidationException {@code 400} with the code and field errors described above
     */
    public ValidatedRule validate(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The rule must be a JSON object.");
        }
        Run run = new Run(body);
        return run.validate();
    }

    /** True when {@code record_version.valid_from} exists (checked once). */
    boolean validFromPresent() {
        Boolean present = validFromPresent;
        if (present == null) {
            Long count = jdbc.queryForObject(VALID_FROM_PROBE, Long.class);
            present = count != null && count > 0;
            validFromPresent = present;
        }
        return present;
    }

    /** One validation, holding its collected errors. */
    private final class Run {

        private final JsonNode body;
        private final List<FieldError> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private ValidationException special;

        Run(JsonNode body) {
            this.body = body;
        }

        ValidatedRule validate() {
            String name = name();
            boolean enabled = bool("enabled", true);
            List<String> sources = filter("sources", "source", true);
            List<String> kinds = filter("kinds", "kind", false);
            List<String> tags = tags();
            List<TriggerCondition> conditions = conditions();
            checkVolatile(sources, kinds, conditions);

            // A rule read back from the API carries the materialized polygon in geofence and the route
            // line in geofenceRoute; when geofenceRoute is given, the route is the input.
            boolean hasGeofence = JsonTrees.present(body, "geofence") || JsonTrees.present(body, "geofenceRoute");
            boolean hasNear = JsonTrees.present(body, "near");
            if (hasGeofence && hasNear) {
                error("near", "geofence and near are mutually exclusive");
            }
            Fence fence = hasGeofence ? geofence() : hasNear && !hasGeofence ? near() : Fence.NONE;
            if (!hasGeofence && JsonTrees.present(body, "bufferMeters")) {
                error("bufferMeters", "applies only to a LineString or MultiLineString geofence");
            }

            TriggerMode mode = mode();
            boolean fenced = hasGeofence || hasNear;
            if (mode != TriggerMode.MATCH && !fenced && conditions.isEmpty()) {
                error("mode", "enter and exit need a geofence, near or at least one condition");
            }
            if (sources.isEmpty() && kinds.isEmpty() && conditions.isEmpty() && !fenced) {
                error("sources", "a rule needs at least one of sources, kinds, conditions, geofence or near");
            }

            boolean onlyNew = bool("onlyNew", false);
            boolean reappearAsEnter = bool("reappearAsEnter", fenced);
            Duration cooldown = duration("cooldown", Duration.ZERO, false);
            if (cooldown.toSeconds() > Integer.MAX_VALUE) {
                error("cooldown", "is too long");
                cooldown = Duration.ZERO;
            }
            CooldownScope cooldownScope = enumValue("cooldownScope", CooldownScope.RECORD, CooldownScope::fromWire);
            Severity severity = enumValue("severity", Severity.WARNING, Severity::fromWire);
            boolean enrich = bool("enrich", false);
            double enrichRadiusKm = number("enrichRadiusKm", 100, 1, 1000);
            Duration enrichWindow = duration("enrichWindow", Duration.ofHours(24), false);
            if (enrichWindow.compareTo(Duration.ofMinutes(10)) < 0 || enrichWindow.compareTo(Duration.ofDays(7)) > 0) {
                error("enrichWindow", "must be between 10m and 7d");
            }

            double hysteresis = number("hysteresisMeters", 0, 0, 5000);
            MultiPolygon inner = null;
            MultiPolygon outer = null;
            if (hysteresis > 0) {
                if (!fenced) {
                    error("hysteresisMeters", "needs a geofence or near");
                } else if (mode == TriggerMode.MATCH) {
                    error("hysteresisMeters", "applies only to enter and exit rules");
                } else if (fence.geometry() != null && errors.isEmpty() && special == null) {
                    inner = geofences.offset(fence.geometry(), -hysteresis);
                    if (inner.isEmpty()) {
                        special(ErrorCodes.GEOFENCE_TOO_SMALL_FOR_HYSTERESIS, "hysteresisMeters",
                                "shrinking the geofence by " + hysteresis + " m leaves nothing");
                        inner = null;
                    } else {
                        outer = geofences.offset(fence.geometry(), hysteresis);
                    }
                }
            }

            AutoResolve autoResolve = autoResolve(mode);
            Integer maxAlertsPerHour = nullableInt("maxAlertsPerHour", 0);
            boolean autoDisableOnStorm = bool("autoDisableOnStorm", false);
            Instant activeFrom = instant("activeFrom");
            Instant activeUntil = instant("activeUntil");
            if (activeFrom != null && activeUntil != null && !activeFrom.isBefore(activeUntil)) {
                error("activeUntil", "must be after activeFrom");
            }
            ActiveWindow activeWindow = activeWindow();
            Duration forecastHorizon = JsonTrees.present(body, "forecastHorizon")
                    ? duration("forecastHorizon", null, true) : null;
            if (forecastHorizon != null && !validFromPresent()) {
                special(ErrorCodes.FEATURE_UNAVAILABLE, "forecastHorizon",
                        "needs record_version.valid_from, which this installation does not have");
            }
            String template = template();

            if (special != null) {
                throw special;
            }
            if (!errors.isEmpty()) {
                throw new ValidationException("The rule is not valid.", errors);
            }
            for (String warning : warnings) {
                log.warn("Trigger rule '{}': {}", name, warning);
            }
            return new ValidatedRule(name, enabled, sources, kinds, tags, conditions, fence.geometry(), fence.route(),
                    fence.bufferMeters(), hysteresis > 0 ? hysteresis : 0, inner, outer, fence.near(), mode, onlyNew,
                    reappearAsEnter, cooldown, cooldownScope, severity, enrich, enrichRadiusKm, enrichWindow,
                    autoResolve, maxAlertsPerHour, autoDisableOnStorm, activeFrom, activeUntil, activeWindow,
                    forecastHorizon, template, warnings);
        }

        // ------------------------------------------------------------------ fields

        private String name() {
            JsonNode node = body.get("name");
            if (node == null || node.isNull()) {
                error("name", "is required");
                return "";
            }
            if (!node.isString() || node.asString().isBlank()) {
                error("name", "must be a non-empty string");
                return "";
            }
            String name = node.asString();
            if (name.length() > MAX_NAME) {
                error("name", "must be at most " + MAX_NAME + " characters");
            }
            return name;
        }

        private List<String> filter(String arrayField, String aliasField, boolean sources) {
            boolean array = JsonTrees.present(body, arrayField);
            boolean alias = JsonTrees.present(body, aliasField);
            if (array && alias) {
                error(aliasField, "give " + arrayField + " or the deprecated " + aliasField + ", not both");
                return List.of();
            }
            List<String> values = new ArrayList<>();
            if (alias) {
                JsonNode node = body.get(aliasField);
                if (!node.isString() || node.asString().isBlank()) {
                    error(aliasField, "must be a non-empty string");
                    return List.of();
                }
                values.add(node.asString().strip());
            } else if (array) {
                values = strings(arrayField, MAX_FILTER);
            }
            for (int i = 0; i < values.size(); i++) {
                String value = values.get(i);
                String field = alias ? aliasField : arrayField + "[" + i + "]";
                if (sources && !registry.contains(value)) {
                    error(field, "unknown source '" + value + "'");
                } else if (!sources && !registry.kinds().contains(value)) {
                    warnings.add("kind '" + value + "' is not declared by any registered source");
                }
            }
            return values;
        }

        private List<String> tags() {
            List<String> tags = JsonTrees.present(body, "tags") ? strings("tags", MAX_TAGS) : List.of();
            for (int i = 0; i < tags.size(); i++) {
                if (!TAG.matcher(tags.get(i)).matches()) {
                    error("tags[" + i + "]", "must match " + TAG.pattern());
                }
            }
            return tags;
        }

        private List<TriggerCondition> conditions() {
            JsonNode node = body.get("conditions");
            if (node == null || node.isNull()) {
                return List.of();
            }
            if (!node.isArray()) {
                error("conditions", "must be an array");
                return List.of();
            }
            List<TriggerCondition> conditions = new ArrayList<>();
            for (int i = 0; i < node.size(); i++) {
                JsonNode item = node.get(i);
                String prefix = "conditions[" + i + "]";
                if (!item.isObject()) {
                    error(prefix, "must be an object with path, op and value");
                    continue;
                }
                JsonNode path = item.get("path");
                JsonNode op = item.get("op");
                boolean ok = true;
                if (path == null || !path.isString() || !PATH.matcher(path.asString()).matches()) {
                    error(prefix + ".path", "must match " + PATH.pattern());
                    ok = false;
                }
                Optional<Operator> operator = op != null && op.isString() ? operators.find(op.asString()) : Optional.empty();
                if (operator.isEmpty()) {
                    error(prefix + ".op", "must be one of " + String.join(", ", operators.names()));
                    ok = false;
                }
                if (!item.has("value")) {
                    error(prefix + ".value", "is required");
                    ok = false;
                }
                if (!ok) {
                    continue;
                }
                try {
                    Object value = operator.get().normalize(JsonTrees.toJava(item.get("value")));
                    conditions.add(new TriggerCondition(path.asString(), operator.get().name(), value));
                } catch (IllegalArgumentException e) {
                    error(prefix + ".value", e.getMessage());
                }
            }
            return conditions;
        }

        private void checkVolatile(List<String> sources, List<String> kinds, List<TriggerCondition> conditions) {
            if (conditions.isEmpty()) {
                return;
            }
            List<SourceDescriptor> applicable = new ArrayList<>();
            if (!sources.isEmpty()) {
                for (String id : sources) {
                    registry.find(id).ifPresent(r -> applicable.add(r.descriptor()));
                }
            } else {
                for (SourceDescriptor descriptor : registry.descriptors()) {
                    if (kinds.isEmpty() || kinds.stream().anyMatch(descriptor.kinds()::contains)) {
                        applicable.add(descriptor);
                    }
                }
            }
            for (int i = 0; i < conditions.size(); i++) {
                String path = conditions.get(i).path();
                int dot = path.indexOf('.');
                String root = dot < 0 ? path : path.substring(0, dot);
                for (SourceDescriptor descriptor : applicable) {
                    if (descriptor.volatileFields().contains(root)) {
                        special(ErrorCodes.TRIGGER_PATH_VOLATILE, "conditions[" + i + "].path",
                                "'" + root + "' is a volatile field of source '" + descriptor.id()
                                        + "'; it changes on every fetch and is not persisted reliably");
                        return;
                    }
                }
            }
        }

        private Fence geofence() {
            boolean fromRoute = JsonTrees.present(body, "geofenceRoute");
            JsonNode node = fromRoute ? body.get("geofenceRoute") : body.get("geofence");
            String field = fromRoute ? "geofenceRoute" : "geofence";
            Geometry geometry;
            try {
                geometry = geoJson.read(node);
            } catch (InvalidInputException e) {
                error(field, e.getMessage());
                return Fence.NONE;
            }
            if (fromRoute && !(geometry instanceof LineString || geometry instanceof MultiLineString)) {
                error("geofenceRoute", "must be a LineString or MultiLineString");
                return Fence.NONE;
            }
            JsonNode bufferNode = body.get("bufferMeters");
            boolean buffered = bufferNode != null && !bufferNode.isNull();
            MultiPolygon fence;
            Map<String, Object> route = null;
            Double bufferMeters = null;
            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                if (buffered) {
                    error("bufferMeters", "applies only to a LineString or MultiLineString geofence");
                    return Fence.NONE;
                }
                fence = geofences.polygonal(geometry);
            } else if (geometry instanceof LineString || geometry instanceof MultiLineString) {
                if (!buffered) {
                    error("bufferMeters", "is required for a LineString or MultiLineString geofence");
                    return Fence.NONE;
                }
                double meters = number("bufferMeters", 0, 1, 50_000);
                if (meters < 1) {
                    return Fence.NONE;
                }
                fence = geofences.bufferLine(geometry, meters);
                @SuppressWarnings("unchecked")
                Map<String, Object> original = (Map<String, Object>) JsonTrees.toJava(node);
                route = original;
                bufferMeters = meters;
            } else {
                error(field, "must be a Polygon, MultiPolygon, LineString or MultiLineString");
                return Fence.NONE;
            }
            if (fence.isEmpty()) {
                error(field, "is empty after fixing");
                return Fence.NONE;
            }
            return new Fence(fence, route, bufferMeters, null);
        }

        private Fence near() {
            JsonNode node = body.get("near");
            if (!node.isObject()) {
                error("near", "must be an object");
                return Fence.NONE;
            }
            if (node.has("anchor")) {
                special(ErrorCodes.FEATURE_UNAVAILABLE, "near.anchor", "moving anchors arrive with prompt 036");
                return Fence.NONE;
            }
            if (node.has("placeId") || node.has("placeIds")) {
                return nearPlace(node);
            }
            JsonNode point = node.get("point");
            if (point == null || !point.isArray() || point.size() != 2 || !point.get(0).isNumber() || !point.get(1).isNumber()) {
                error("near.point", "must be [lon, lat]");
                return Fence.NONE;
            }
            double lon = point.get(0).doubleValue();
            double lat = point.get(1).doubleValue();
            if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
                error("near.point", "must be a longitude in -180..180 and a latitude in -90..90");
                return Fence.NONE;
            }
            JsonNode radius = node.get("radiusKm");
            if (radius == null || !radius.isNumber() || radius.doubleValue() < 0.1 || radius.doubleValue() > 2000) {
                error("near.radiusKm", "must be a number from 0.1 to 2000");
                return Fence.NONE;
            }
            for (String field : node.propertyNames()) {
                if (!field.equals("point") && !field.equals("radiusKm")) {
                    error("near." + field, "is not a near field");
                    return Fence.NONE;
                }
            }
            MultiPolygon fence = geofences.bufferPoint(lon, lat, radius.doubleValue() * 1000.0);
            Map<String, Object> near = new LinkedHashMap<>();
            near.put("point", List.of(lon, lat));
            near.put("radiusKm", radius.doubleValue());
            return new Fence(fence, null, null, near);
        }

        private Fence nearPlace(JsonNode node) {
            PlaceLookup lookup = places.getIfAvailable();
            if (lookup == null) {
                special(ErrorCodes.PLACES_UNAVAILABLE, "near.placeId", "places are not available on this installation");
                return Fence.NONE;
            }
            JsonNode placeId = node.get("placeId");
            if (placeId == null || !placeId.isString()) {
                error("near.placeId", "must be a place id");
                return Fence.NONE;
            }
            Optional<Geometry> place = lookup.geometry(placeId.asString());
            if (place.isEmpty()) {
                error("near.placeId", "unknown place '" + placeId.asString() + "'");
                return Fence.NONE;
            }
            JsonNode radius = node.get("radiusKm");
            double km = radius == null || radius.isNull() ? 0 : radius.doubleValue();
            if (km < 0 || km > 2000) {
                error("near.radiusKm", "must be a number from 0 to 2000");
                return Fence.NONE;
            }
            Geometry geometry = place.get();
            MultiPolygon fence;
            if (geometry instanceof Point p) {
                if (km < 0.1) {
                    error("near.radiusKm", "must be at least 0.1 around a point place");
                    return Fence.NONE;
                }
                fence = geofences.bufferPoint(p.getX(), p.getY(), km * 1000.0);
            } else {
                MultiPolygon area = geofences.polygonal(geometry);
                fence = km > 0 ? geofences.offset(area, km * 1000.0) : area;
            }
            Map<String, Object> near = new LinkedHashMap<>();
            near.put("placeId", placeId.asString());
            near.put("radiusKm", km);
            return new Fence(fence, null, null, near);
        }

        private TriggerMode mode() {
            JsonNode node = body.get("mode");
            if (node == null || node.isNull()) {
                return TriggerMode.MATCH;
            }
            if (node.isString()) {
                switch (node.asString()) {
                    case "match":
                        return TriggerMode.MATCH;
                    case "enter":
                        return TriggerMode.ENTER;
                    case "exit":
                        return TriggerMode.EXIT;
                    default:
                        break;
                }
            }
            error("mode", "must be match, enter or exit");
            return TriggerMode.MATCH;
        }

        private AutoResolve autoResolve(TriggerMode mode) {
            if (!body.has("autoResolve")) {
                return mode == TriggerMode.ENTER ? AutoResolve.ALL : null;
            }
            JsonNode node = body.get("autoResolve");
            if (node.isNull() || (node.isBoolean() && !node.booleanValue())) {
                return null;
            }
            if (mode == TriggerMode.EXIT) {
                error("autoResolve", "is not allowed on exit rules");
                return null;
            }
            if (node.isBoolean()) {
                return AutoResolve.ALL;
            }
            if (!node.isObject()) {
                error("autoResolve", "must be true, false, null or an object");
                return null;
            }
            boolean onInverseEdge = nestedBool(node, "onInverseEdge");
            boolean onTombstone = nestedBool(node, "onTombstone");
            boolean onExpiry = nestedBool(node, "onExpiry");
            Duration staleAfter = null;
            JsonNode stale = node.get("staleAfter");
            if (stale != null && !stale.isNull()) {
                if (!stale.isString()) {
                    error("autoResolve.staleAfter", "must be a duration");
                } else {
                    try {
                        staleAfter = Durations.parsePositive(stale.asString());
                    } catch (InvalidInputException e) {
                        error("autoResolve.staleAfter", e.getMessage());
                    }
                }
            }
            return new AutoResolve(onInverseEdge, onTombstone, onExpiry, staleAfter);
        }

        private boolean nestedBool(JsonNode node, String field) {
            JsonNode value = node.get(field);
            if (value == null || value.isNull()) {
                return true;
            }
            if (!value.isBoolean()) {
                error("autoResolve." + field, "must be true or false");
                return true;
            }
            return value.booleanValue();
        }

        private ActiveWindow activeWindow() {
            JsonNode node = body.get("activeWindow");
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isObject()) {
                error("activeWindow", "must be an object");
                return null;
            }
            ZoneId zone = null;
            JsonNode tz = node.get("timezone");
            if (tz == null || !tz.isString()) {
                error("activeWindow.timezone", "is required (an IANA timezone)");
            } else {
                try {
                    zone = ZoneId.of(tz.asString());
                } catch (DateTimeException e) {
                    error("activeWindow.timezone", "is not a known IANA timezone");
                }
            }
            JsonNode list = node.get("windows");
            if (list == null || !list.isArray() || list.isEmpty()) {
                error("activeWindow.windows", "must be a non-empty array");
                return null;
            }
            List<ActiveWindow.Window> windows = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                String prefix = "activeWindow.windows[" + i + "]";
                JsonNode w = list.get(i);
                JsonNode days = w.get("days");
                Set<DayOfWeek> parsedDays = EnumSet.noneOf(DayOfWeek.class);
                if (days == null || !days.isArray() || days.isEmpty()) {
                    error(prefix + ".days", "must be a non-empty array of MON..SUN");
                } else {
                    for (JsonNode day : days) {
                        try {
                            parsedDays.add(ActiveWindow.day(day.isString() ? day.asString() : ""));
                        } catch (IllegalArgumentException e) {
                            error(prefix + ".days", e.getMessage());
                        }
                    }
                }
                LocalTime start = clock(w, "start", prefix);
                LocalTime end = clock(w, "end", prefix);
                if (start != null && end != null && start.equals(end)) {
                    error(prefix + ".end", "must differ from start");
                }
                if (start != null && end != null && !parsedDays.isEmpty()) {
                    windows.add(new ActiveWindow.Window(parsedDays, start, end));
                }
            }
            return zone == null || windows.size() != list.size() ? null : new ActiveWindow(zone, windows);
        }

        private LocalTime clock(JsonNode window, String field, String prefix) {
            JsonNode node = window.get(field);
            if (node == null || !node.isString() || !HH_MM.matcher(node.asString()).matches()) {
                error(prefix + "." + field, "must be HH:mm");
                return null;
            }
            return LocalTime.parse(node.asString(), ActiveWindow.HH_MM);
        }

        private String template() {
            JsonNode node = body.get("messageTemplate");
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isString()) {
                error("messageTemplate", "must be a string");
                return null;
            }
            if (node.asString().length() > MessageTemplate.MAX_TEMPLATE) {
                error("messageTemplate", "must be at most " + MessageTemplate.MAX_TEMPLATE + " characters");
            }
            return node.asString();
        }

        // ------------------------------------------------------------------ primitives

        private List<String> strings(String field, int max) {
            JsonNode node = body.get(field);
            if (!node.isArray()) {
                error(field, "must be an array of strings");
                return List.of();
            }
            if (node.size() > max) {
                error(field, "must have at most " + max + " items");
                return List.of();
            }
            Set<String> values = new LinkedHashSet<>();
            for (int i = 0; i < node.size(); i++) {
                JsonNode item = node.get(i);
                if (!item.isString() || item.asString().isBlank()) {
                    error(field + "[" + i + "]", "must be a non-empty string");
                    continue;
                }
                values.add(item.asString().strip());
            }
            return new ArrayList<>(values);
        }

        private boolean bool(String field, boolean defaultValue) {
            JsonNode node = body.get(field);
            if (node == null || node.isNull()) {
                return defaultValue;
            }
            if (!node.isBoolean()) {
                error(field, "must be true or false");
                return defaultValue;
            }
            return node.booleanValue();
        }

        private double number(String field, double defaultValue, double min, double max) {
            JsonNode node = body.get(field);
            if (node == null || node.isNull()) {
                return defaultValue;
            }
            if (!node.isNumber()) {
                error(field, "must be a number");
                return defaultValue;
            }
            BigDecimal value = node.decimalValue();
            if (value.compareTo(BigDecimal.valueOf(min)) < 0 || value.compareTo(BigDecimal.valueOf(max)) > 0) {
                error(field, "must be between " + trim(min) + " and " + trim(max));
                return defaultValue;
            }
            return value.doubleValue();
        }

        private Integer nullableInt(String field, int min) {
            JsonNode node = body.get(field);
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isIntegralNumber() || !node.canConvertToInt() || node.intValue() < min) {
                error(field, "must be an integer of at least " + min + ", or null");
                return null;
            }
            return node.intValue();
        }

        private Duration duration(String field, Duration defaultValue, boolean positive) {
            JsonNode node = body.get(field);
            if (node == null || node.isNull()) {
                return defaultValue;
            }
            if (!node.isString()) {
                error(field, "must be a duration such as 15m or PT15M");
                return defaultValue == null ? Duration.ZERO : defaultValue;
            }
            try {
                return positive ? Durations.parsePositive(node.asString()) : Durations.parse(node.asString());
            } catch (InvalidInputException e) {
                error(field, e.getMessage());
                return defaultValue == null ? Duration.ZERO : defaultValue;
            }
        }

        private Instant instant(String field) {
            JsonNode node = body.get(field);
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isString()) {
                error(field, "must be an RFC 3339 date-time");
                return null;
            }
            try {
                return OffsetDateTime.parse(node.asString()).toInstant();
            } catch (DateTimeParseException e) {
                error(field, "must be an RFC 3339 date-time");
                return null;
            }
        }

        private <E> E enumValue(String field, E defaultValue, java.util.function.Function<String, E> parse) {
            JsonNode node = body.get(field);
            if (node == null || node.isNull()) {
                return defaultValue;
            }
            if (!node.isString()) {
                error(field, "must be a string");
                return defaultValue;
            }
            try {
                return parse.apply(node.asString());
            } catch (IllegalArgumentException e) {
                error(field, e.getMessage());
                return defaultValue;
            }
        }

        private void error(String field, String message) {
            errors.add(new FieldError(field, message));
        }

        private void special(String code, String field, String message) {
            if (special == null) {
                special = new ValidationException(code, "The rule is not valid: " + field + " " + message + ".",
                        List.of(new FieldError(field, message)));
            }
        }

        private static String trim(double value) {
            return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
        }
    }

    /** A materialized fence and what it came from. */
    private record Fence(MultiPolygon geometry, Map<String, Object> route, Double bufferMeters, Map<String, Object> near) {
        static final Fence NONE = new Fence(null, null, null, null);
    }
}
