package ai.foresignal.source.kit.feature;

import ai.foresignal.source.kit.KitOverlay;
import ai.foresignal.source.kit.KitSpecLoader;
import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.IngestPolicyValidator;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks feature-kit specs at startup (prompt 073). Every problem is collected with its field path
 * ({@code kits/features/x.json[0] feature-y: fields.Acres.type: …}), and the family stops startup with the whole
 * list.
 *
 * <p>The rules:
 * <ul>
 *   <li><strong>Identity:</strong> {@code feature-} (or a reviewed family prefix: {@code faa-uas-}, prompt 087;
 *       {@code imf-portwatch-}, prompt 088)
 *       plus kebab-case (overlay specs {@code op-}), at most 64 characters, unique across every kit spec;
 *   <li><strong>URL:</strong> https only, no user info, no credential-looking query parameter ({@link KitUrls});
 *       overlay URLs also pass the outbound URL policy;
 *   <li><strong>Protocol:</strong> {@code arcgis}, {@code ogc} (needs {@code collection}) or {@code wfs} (needs
 *       {@code typeName}); {@code format: esrijson} only for ArcGIS;
 *   <li><strong>Fields:</strong> at least one; targets top-level, unique and not reserved; types known; units UCUM;
 *       constants never shadow a field;
 *   <li><strong>References:</strong> {@code idField}, {@code titleField}, every time field, {@code significantFields},
 *       {@code volatileFields}, {@code searchFields} and {@code tileProperties} name declared targets (or constants);
 *   <li><strong>Time base:</strong> each time entry names exactly one timestamp target, or {@code RECEIVED} for
 *       {@code observedAt}; never a list or fallback (Core-Data-Model §2); {@code latestPerKey} needs a
 *       {@code time.observedAt} field (prompt 079);
 *   <li><strong>Schedule:</strong> durations parse, {@code minInterval ≤ interval}, and a {@code recordTtl} leaves
 *       room for a keyframe: {@code recordTtl − interval − 30 s > 0} (Core-Data-Model §5.1);
 *   <li><strong>Limits:</strong> page size 1–10,000, at most 100 pages, at most 50,000 features; only {@code Accept}
 *       and {@code Digitraffic-User} headers.
 * </ul>
 */
public final class FeatureSpecValidator {

    /**
     * A built-in feature spec id: {@code feature-…}, or one of the reviewed family prefixes whose ids the source
     * catalog fixes ({@code faa-uas-…}, prompt 087; {@code imf-portwatch-…}, prompt 088; {@code wpc-ero-…},
     * prompt 089).
     */
    public static final Pattern ID_PATTERN = Pattern.compile(
            "^(?:feature|faa-uas|imf-portwatch|wpc-ero)-[a-z0-9]+(-[a-z0-9]+)*$");

    /** An operator overlay spec id. */
    public static final Pattern OVERLAY_ID_PATTERN = Pattern.compile("^op-[a-z0-9]+(-[a-z0-9]+)*$");

    /** A kind. */
    public static final Pattern KIND_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** A payload key: a top-level name, no dots, brackets or slashes. */
    public static final Pattern KEY_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}$");

    /** A UCUM code, as the descriptor validator checks it. */
    static final Pattern UCUM_PATTERN = Pattern.compile("^[\\x21-\\x7E]{1,32}$");

    /** The largest id. */
    public static final int MAX_ID_LENGTH = 64;

    /** The field types. */
    public static final Set<String> TYPES = Set.of("string", "number", "integer", "boolean", "timestamp");

    /** The headers a spec may set. */
    public static final Set<String> HEADER_ALLOWLIST = Set.of("Accept", "Digitraffic-User");

    /** The payload keys the kit adds itself. */
    public static final Set<String> RESERVED_TARGETS = Set.of(FeatureGeometry.SIMPLIFIED_KEY,
            FeatureGeometry.TOLERANCE_KEY);

    /** The named time formats. */
    public static final Set<String> TIME_FORMATS = Set.of("auto", "epoch-ms", "epoch-s", "iso");

    private FeatureSpecValidator() {}

    /**
     * Checks every loaded spec.
     *
     * @param specs the specs, built-in and overlay
     * @param takenIds ids already used elsewhere (the feed kit's), for uniqueness
     * @param overlay the overlay settings, for overlay URL checks
     * @return every problem, empty when all specs are valid
     */
    public static List<String> problems(List<KitSpecLoader.Loaded<FeatureSourceSpec>> specs, Set<String> takenIds,
            KitOverlay overlay) {
        List<String> problems = new ArrayList<>();
        Set<String> seen = new HashSet<>(takenIds == null ? Set.of() : takenIds);
        for (KitSpecLoader.Loaded<FeatureSourceSpec> loaded : specs) {
            FeatureSourceSpec spec = loaded.spec();
            String where = loaded.where() + " " + spec.id() + ": ";
            for (String problem : problems(spec, loaded.overlay())) {
                problems.add(where + problem);
            }
            if (spec.id() != null && !seen.add(spec.id())) {
                problems.add(where + "id: '" + spec.id() + "' is declared twice");
            }
            if (loaded.overlay() && overlay != null) {
                List<String> urlProblems = new ArrayList<>();
                overlay.checkUrl("url", spec.url(), urlProblems);
                urlProblems.forEach(p -> problems.add(where + p));
            }
        }
        return problems;
    }

    /**
     * The problems of one spec, each starting with its field path.
     *
     * @param spec the spec
     * @param overlay true for an operator overlay spec
     */
    public static List<String> problems(FeatureSourceSpec spec, boolean overlay) {
        List<String> p = new ArrayList<>();
        checkIdentity(spec, overlay, p);
        p.addAll(KitUrls.problems("url", spec.url(), true));
        checkProtocol(spec, p);
        Set<String> targets = checkFields(spec, p);
        Set<String> referable = new LinkedHashSet<>(targets);
        referable.addAll(spec.constants().keySet());
        checkReferences(spec, targets, referable, p);
        checkTime(spec, targets, p);
        checkSchedule(spec, p);
        checkLimits(spec, p);
        checkText(spec, p);
        return p;
    }

    private static void checkIdentity(FeatureSourceSpec spec, boolean overlay, List<String> p) {
        String id = spec.id();
        Pattern pattern = overlay ? OVERLAY_ID_PATTERN : ID_PATTERN;
        if (id == null || id.isBlank()) {
            p.add("id: is required");
        } else {
            if (!pattern.matcher(id).matches()) {
                p.add("id: '" + id + "' must match " + pattern.pattern());
            }
            if (id.length() > MAX_ID_LENGTH) {
                p.add("id: is longer than " + MAX_ID_LENGTH + " characters");
            }
        }
        if (spec.name() == null || spec.name().isBlank()) {
            p.add("name: is required");
        }
        if (spec.kind() == null || !KIND_PATTERN.matcher(spec.kind()).matches()) {
            p.add("kind: must be kebab-case, but was '" + spec.kind() + "'");
        }
    }

    private static void checkProtocol(FeatureSourceSpec spec, List<String> p) {
        String protocol = spec.protocol();
        if (!FeatureSourceSpec.ARCGIS.equals(protocol) && !FeatureSourceSpec.OGC.equals(protocol)
                && !FeatureSourceSpec.WFS.equals(protocol)) {
            p.add("protocol: must be arcgis, ogc or wfs, but was '" + protocol + "'");
            return;
        }
        String format = spec.effectiveFormat();
        if (!FeatureSourceSpec.GEOJSON.equals(format) && !FeatureSourceSpec.ESRIJSON.equals(format)) {
            p.add("format: must be geojson or esrijson, but was '" + format + "'");
        } else if (FeatureSourceSpec.ESRIJSON.equals(format) && !FeatureSourceSpec.ARCGIS.equals(protocol)) {
            p.add("format: esrijson is only for protocol arcgis");
        }
        if (FeatureSourceSpec.OGC.equals(protocol) && blank(spec.collection())) {
            p.add("collection: is required for protocol ogc");
        }
        if (FeatureSourceSpec.WFS.equals(protocol) && blank(spec.typeName())) {
            p.add("typeName: is required for protocol wfs");
        }
        if (spec.layer() != null && !spec.layer().matches("^\\d{1,6}$")) {
            p.add("layer: must be a layer number, but was '" + spec.layer() + "'");
        }
        if (!FeatureSourceSpec.ARCGIS.equals(protocol)) {
            if (spec.query().where() != null) {
                p.add("query.where: is only for protocol arcgis");
            }
            if (!spec.query().outFields().isEmpty()) {
                p.add("query.outFields: is only for protocol arcgis");
            }
        }
        if (spec.query().datetime() != null && !FeatureSourceSpec.OGC.equals(protocol)) {
            p.add("query.datetime: is only for protocol ogc");
        }
        List<Double> bbox = spec.query().bbox();
        if (!bbox.isEmpty()) {
            if (bbox.size() != 4 || bbox.stream().anyMatch(v -> v == null || !Double.isFinite(v))) {
                p.add("query.bbox: must be four numbers [minLon, minLat, maxLon, maxLat]");
            } else if (bbox.get(0) < -180 || bbox.get(2) > 180 || bbox.get(1) < -90 || bbox.get(3) > 90
                    || bbox.get(0) >= bbox.get(2) || bbox.get(1) >= bbox.get(3)) {
                p.add("query.bbox: must be [minLon, minLat, maxLon, maxLat] within WGS 84, min below max");
            }
        }
        spec.query().filters().forEach((name, value) -> {
            if (name == null || !name.matches("^[A-Za-z_][A-Za-z0-9_.:-]{0,63}$")) {
                p.add("query.filters: '" + name + "' is not a parameter name");
            } else if (KitUrls.CREDENTIAL_PARAMS.contains(name.toLowerCase(java.util.Locale.ROOT))) {
                p.add("query.filters." + name + ": looks like a credential; declare it as the spec's credential");
            }
            if (value == null) {
                p.add("query.filters." + name + ": has no value");
            }
        });
    }

    private static Set<String> checkFields(FeatureSourceSpec spec, List<String> p) {
        Set<String> targets = new LinkedHashSet<>();
        if (spec.fields().isEmpty()) {
            p.add("fields: at least one field is required");
        }
        for (Map.Entry<String, FeatureSourceSpec.FieldMapping> e : spec.fields().entrySet()) {
            String source = e.getKey();
            String path = "fields." + source;
            FeatureSourceSpec.FieldMapping m = e.getValue();
            if (source == null || source.isBlank()) {
                p.add("fields: an attribute name is blank");
                continue;
            }
            if (m == null) {
                p.add(path + ": is null");
                continue;
            }
            String target = m.target() == null ? source : m.target();
            if (!KEY_PATTERN.matcher(target).matches()) {
                p.add(path + ".target: '" + target + "' must be a top-level payload key (letters, digits, _)");
            } else if (RESERVED_TARGETS.contains(target)) {
                p.add(path + ".target: '" + target + "' is reserved for the kit");
            } else if (!targets.add(target)) {
                p.add(path + ".target: '" + target + "' is used twice");
            }
            if (m.type() == null || !TYPES.contains(m.type())) {
                p.add(path + ".type: must be one of " + new java.util.TreeSet<>(TYPES) + ", but was '" + m.type() + "'");
            }
            if (m.unit() != null && !UCUM_PATTERN.matcher(m.unit()).matches()) {
                p.add(path + ".unit: '" + m.unit() + "' is not a UCUM code");
            }
            m.valueMap().forEach((k, v) -> {
                if (v != null && !(v instanceof String) && !(v instanceof Number) && !(v instanceof Boolean)) {
                    p.add(path + ".valueMap." + k + ": must be a string, number or boolean");
                }
            });
        }
        for (Map.Entry<String, Object> c : spec.constants().entrySet()) {
            String key = c.getKey();
            if (key == null || !KEY_PATTERN.matcher(key).matches()) {
                p.add("constants: '" + key + "' must be a top-level payload key");
            } else if (targets.contains(key)) {
                p.add("constants." + key + ": shadows a field target");
            } else if (RESERVED_TARGETS.contains(key)) {
                p.add("constants." + key + ": is reserved for the kit");
            }
            Object v = c.getValue();
            if (!(v instanceof String) && !(v instanceof Number) && !(v instanceof Boolean)) {
                p.add("constants." + key + ": must be a string, number or boolean");
            }
        }
        return targets;
    }

    private static void checkReferences(FeatureSourceSpec spec, Set<String> targets, Set<String> referable,
            List<String> p) {
        if (blank(spec.idField())) {
            p.add("idField: is required");
        } else if (!targets.contains(spec.idField())) {
            p.add("idField: '" + spec.idField() + "' is not a declared field target");
        }
        if (spec.titleField() != null && !referable.contains(spec.titleField())) {
            p.add("titleField: '" + spec.titleField() + "' is not a declared field target");
        }
        list("significantFields", spec.significantFields(), referable, p);
        list("volatileFields", spec.volatileFields(), referable, p);
        list("searchFields", spec.searchFields(), referable, p);
        list("tileProperties", spec.tileProperties(), referable, p);
        for (String f : spec.significantFields()) {
            if (spec.volatileFields().contains(f)) {
                p.add("significantFields: '" + f + "' is also volatile");
            }
        }
    }

    private static void list(String path, List<String> names, Set<String> referable, List<String> p) {
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            boolean kitKey = name != null && RESERVED_TARGETS.contains(name);
            if (name == null || (!referable.contains(name) && !kitKey)) {
                p.add(path + "[" + i + "]: '" + name + "' is not a declared field target");
            }
        }
    }

    private static void checkTime(FeatureSourceSpec spec, Set<String> targets, List<String> p) {
        FeatureSourceSpec.TimeSpec time = spec.time();
        if (!time.observedAtReceived()) {
            timeField(spec, "time.observedAt", time.observedAt(), targets, p);
        }
        timeField(spec, "time.eventTime", time.eventTime(), targets, p);
        timeField(spec, "time.validFrom", time.validFrom(), targets, p);
        timeField(spec, "time.validUntil", time.validUntil(), targets, p);
        String format = time.format();
        if (format != null && !TIME_FORMATS.contains(format)) {
            try {
                DateTimeFormatter.ofPattern(format);
            } catch (IllegalArgumentException e) {
                p.add("time.format: '" + format + "' is neither auto, epoch-ms, epoch-s, iso nor a date-time pattern");
            }
        }
        if (time.zone() != null) {
            try {
                ZoneId.of(time.zone());
            } catch (RuntimeException e) {
                p.add("time.zone: '" + time.zone() + "' is not a zone id");
            }
        }
    }

    private static void timeField(FeatureSourceSpec spec, String path, String value, Set<String> targets,
            List<String> p) {
        if (value == null) {
            return;
        }
        if (value.contains(",") || value.contains("|") || value.contains(" ") || value.contains("??")) {
            p.add(path + ": names one field only; the time base is fixed per source (Core-Data-Model §2), so a"
                    + " fallback between fields is not allowed");
            return;
        }
        if (FeatureSourceSpec.TimeSpec.RECEIVED.equals(value)) {
            p.add(path + ": RECEIVED is only for time.observedAt");
            return;
        }
        if (!targets.contains(value)) {
            p.add(path + ": '" + value + "' is not a declared field target");
            return;
        }
        FeatureSourceSpec.FieldMapping m = spec.mappingOf(value);
        if (m != null && !"timestamp".equals(m.type())) {
            p.add(path + ": '" + value + "' must have type timestamp");
        }
    }

    private static void checkSchedule(FeatureSourceSpec spec, List<String> p) {
        Duration interval = duration("interval", spec.interval(), FeatureSourceSpec.DEFAULT_INTERVAL, p);
        Duration minInterval = duration("minInterval", spec.minInterval(), FeatureSourceSpec.DEFAULT_MIN_INTERVAL, p);
        Duration ttl = duration("recordTtl", spec.recordTtl(), null, p);
        duration("retention", spec.retention(), FeatureSourceSpec.DEFAULT_RETENTION, p);
        if (interval != null && minInterval != null && minInterval.compareTo(interval) > 0) {
            p.add("minInterval: " + minInterval + " exceeds interval " + interval);
        }
        if (minInterval != null && minInterval.compareTo(Duration.ofMinutes(1)) < 0) {
            p.add("minInterval: must be at least 1 min");
        }
        if (ttl != null && interval != null) {
            Duration ceiling = ttl.minus(interval).minus(IngestPolicyValidator.KEYFRAME_TTL_MARGIN);
            if (ceiling.isZero() || ceiling.isNegative()) {
                p.add("recordTtl: " + ttl + " leaves no keyframe for interval " + interval + "; recordTtl − interval"
                        + " − 30 s must be positive (Core-Data-Model §5.1)");
            }
        }
        String anchor = spec.ttlAnchor();
        if (anchor != null && !anchor.equals("RECEIVED") && !anchor.equals("OBSERVED")) {
            p.add("ttlAnchor: must be RECEIVED or OBSERVED, but was '" + anchor + "'");
        }
        if ("OBSERVED".equals(anchor) && spec.time().observedAtReceived()) {
            p.add("ttlAnchor: OBSERVED needs time.observedAt to name a field");
        }
        if (spec.keepsLatestPerKey() && spec.time().observedAtReceived()) {
            p.add("latestPerKey: needs time.observedAt to name a field");
        }
        FeatureSourceSpec.BudgetSpec budget = spec.budget();
        if (budget == null || budget.perMinute() == null || budget.perDay() == null) {
            p.add("budget: perMinute and perDay are required");
        } else {
            if (budget.perMinute() < 1 || budget.perDay() < 1) {
                p.add("budget: perMinute and perDay must be positive");
            }
            if (budget.mbPerDay() != null && budget.mbPerDay() < 1) {
                p.add("budget.mbPerDay: must be positive");
            }
        }
    }

    private static Duration duration(String path, String text, Duration fallback, List<String> p) {
        try {
            Duration d = FeatureSourceSpec.parse(text, fallback);
            if (d != null && (d.isZero() || d.isNegative())) {
                p.add(path + ": must be positive");
                return null;
            }
            return d;
        } catch (RuntimeException e) {
            p.add(path + ": '" + text + "' is not a duration (" + e.getMessage() + ")");
            return null;
        }
    }

    private static void checkLimits(FeatureSourceSpec spec, List<String> p) {
        FeatureSourceSpec.PagingSpec paging = spec.paging();
        if (paging.effectivePageSize() < 1 || paging.effectivePageSize() > 10_000) {
            p.add("paging.pageSize: must be between 1 and 10000");
        }
        if (paging.effectiveMaxPages() < 1 || paging.effectiveMaxPages() > 100) {
            p.add("paging.maxPages: must be between 1 and 100");
        }
        if (paging.effectiveMaxFeatures() < 1
                || paging.effectiveMaxFeatures() > FeatureSourceSpec.PagingSpec.DEFAULT_MAX_FEATURES) {
            p.add("paging.maxFeatures: must be between 1 and " + FeatureSourceSpec.PagingSpec.DEFAULT_MAX_FEATURES);
        }
        FeatureSourceSpec.GeometrySpec geometry = spec.geometry();
        if (geometry.axisOrder() != null && !geometry.axisOrder().equals("lonlat")
                && !geometry.axisOrder().equals("latlon")) {
            p.add("geometry.axisOrder: must be lonlat or latlon");
        }
        if (geometry.axisOrder() != null && !FeatureSourceSpec.WFS.equals(spec.protocol())) {
            p.add("geometry.axisOrder: is only for protocol wfs");
        }
        if (geometry.maxAllowableOffset() != null && !(geometry.maxAllowableOffset() > 0)) {
            p.add("geometry.maxAllowableOffset: must be positive");
        }
        if (geometry.geometryPrecision() != null
                && (geometry.geometryPrecision() < 0 || geometry.geometryPrecision() > 15)) {
            p.add("geometry.geometryPrecision: must be between 0 and 15");
        }
        if ((geometry.maxAllowableOffset() != null || geometry.geometryPrecision() != null)
                && !FeatureSourceSpec.ARCGIS.equals(spec.protocol())) {
            p.add("geometry: maxAllowableOffset and geometryPrecision are only for protocol arcgis");
        }
        for (String header : spec.headers().keySet()) {
            if (!HEADER_ALLOWLIST.contains(header)) {
                p.add("headers." + header + ": only " + new java.util.TreeSet<>(HEADER_ALLOWLIST) + " may be set");
            }
        }
        FeatureSourceSpec.CredentialSpec credential = spec.credential();
        if (credential != null) {
            if (blank(credential.name()) || !credential.name().matches("^[A-Za-z][A-Za-z0-9_]{0,63}$")) {
                p.add("credential.name: must be a field name");
            }
            if (!"query".equals(credential.placement()) && !"header".equals(credential.placement())) {
                p.add("credential.placement: must be query or header");
            }
            if (blank(credential.param()) || !credential.param().matches("^[A-Za-z][A-Za-z0-9_-]{0,63}$")) {
                p.add("credential.param: must be a parameter or header name");
            } else if ("header".equals(credential.placement())
                    && List.of("user-agent", "referer", "origin", "host", "cookie")
                            .contains(credential.param().toLowerCase(java.util.Locale.ROOT))) {
                p.add("credential.param: '" + credential.param() + "' is not a credential header");
            }
        }
        if (spec.maxResponseBytes() != null
                && (spec.maxResponseBytes() < 1024 || spec.maxResponseBytes() > 64 * 1024 * 1024)) {
            p.add("maxResponseBytes: must be between 1 KB and 64 MB");
        }
    }

    private static void checkText(FeatureSourceSpec spec, List<String> p) {
        p.addAll(KitUrls.problems("homepage", spec.homepage(), true));
        p.addAll(KitUrls.problems("termsUrl", spec.termsUrl(), false));
        if (blank(spec.license())) {
            p.add("license: is required");
        }
        if (blank(spec.attribution())) {
            p.add("attribution: is required");
        }
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
