package ai.foresignal.mapping;

import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Applies a {@link CompiledMapping} to one inbound document (prompt 059). Pure: no I/O and no state.
 *
 * <p>Per item (the document, or each element {@code select} produces):
 *
 * <ol>
 *   <li>pairs are flattened, then every {@code filter} condition must hold, else the item is skipped and counted;
 *   <li>{@code externalId} must be a non-blank string or a number ({@code EXTERNAL_ID_INVALID});
 *   <li>the kind is the mapped value or the source's single kind, and must be one of the source's kinds
 *       ({@code UNKNOWN_KIND});
 *   <li>times are parsed; an unparseable value rejects the item ({@code INVALID_ITEM}); an absent
 *       {@code observedAt} is left to the normalizer ({@code MISSING_OBSERVED_AT} under UPSTREAM);
 *   <li>a {@code tombstone} match makes a tombstone;
 *   <li>geometry: an out-of-range coordinate leaves the geometry out (with the fallback, if any) and warns;
 *   <li>each field: the value, or its default when absent or a sentinel, coerced to its type, converted
 *       (unit, then {@code scale}, then {@code offset}); a value that cannot be coerced, or that is NaN or infinite,
 *       leaves that field out with a {@link MappingWarning#FIELD_REJECTED}; a repeated name keeps its first value;
 *   <li>{@code spread} copies an object's scalar members; {@code drop} keys are never stored.
 * </ol>
 *
 * <p>The records then go through the standard normalizer like any adapter's (limits, null island, clamp): in
 * the writer for ingest, in {@link MappingPreviewService} for a preview.
 */
@Component
public class MappingEngine {

    /** The most warnings one result carries. */
    public static final int MAX_WARNINGS = 100;

    private final MappingJsonPath dialect;
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** Creates the engine over Boot's {@code JsonMapper}. */
    public MappingEngine(JsonMapper json) {
        this.dialect = new MappingJsonPath(json);
    }

    /** The JSONPath dialect the engine reads with. */
    public MappingJsonPath dialect() {
        return dialect;
    }

    /**
     * Maps {@code doc}.
     *
     * @param doc the input: parsed JSON, or the flat string object of a form or query
     * @param m the compiled mapping
     * @param receivedAt when the request was received ({@code {received: true}})
     * @return the records, rejections, warnings and counts
     */
    public MappingResult apply(JsonNode doc, CompiledMapping m, Instant receivedAt) {
        List<JsonNode> items = items(doc, m);
        Out out = new Out();
        out.warnings.addAll(m.warnings());
        for (int i = 0; i < items.size(); i++) {
            mapItem(i, items.get(i), m, receivedAt, out);
        }
        return new MappingResult(out.records, out.indexes, out.rejections, out.warnings(), out.skipped, items.size());
    }

    private List<JsonNode> items(JsonNode doc, CompiledMapping m) {
        List<JsonNode> items = new ArrayList<>();
        if (doc == null || doc.isMissingNode() || doc.isNull()) {
            return items;
        }
        if (m.select() == null) {
            items.add(doc);
            return items;
        }
        JsonNode selected = dialect.read(m.select(), doc);
        if (selected.isArray()) {
            selected.forEach(items::add);
        } else if (selected.isObject()) {
            items.add(selected);
        }
        return items;
    }

    private void mapItem(int index, JsonNode item, CompiledMapping m, Instant receivedAt, Out out) {
        MappingScope scope = new MappingScope(item, pairs(item, m), dialect);
        for (CompiledMapping.Filter filter : m.filters()) {
            if (!holds(filter, scope)) {
                out.skipped++;
                return;
            }
        }
        String externalId = idText(m.externalId().resolve(scope));
        if (externalId == null) {
            out.rejections.add(new ItemRejection(index, null, "EXTERNAL_ID_INVALID", "no external id"));
            return;
        }
        String kind = m.kind() == null ? m.defaultKind() : text(m.kind().resolve(scope));
        if (kind == null || kind.isBlank() || (m.kinds() != null && !m.kinds().contains(kind))) {
            out.rejections.add(new ItemRejection(index, externalId, "UNKNOWN_KIND",
                    "kind is missing or not one of the source's kinds"));
            return;
        }
        Instant observedAt;
        Instant eventTime;
        Instant validUntil;
        try {
            observedAt = time(m.observedAt(), scope, receivedAt, "observedAt");
            eventTime = time(m.eventTime(), scope, receivedAt, "eventTime");
            validUntil = time(m.validUntil(), scope, receivedAt, "validUntil");
        } catch (IllegalArgumentException e) {
            out.rejections.add(new ItemRejection(index, externalId, "INVALID_ITEM", e.getMessage()));
            return;
        }
        if (m.tombstone() != null && isTombstone(m.tombstone(), scope)) {
            out.add(index, NormalizedRecord.builder(externalId, kind).observedAt(observedAt).tombstone(true).build());
            return;
        }
        NormalizedRecord.Builder record = NormalizedRecord.builder(externalId, kind)
                .observedAt(observedAt)
                .eventTime(eventTime)
                .validUntil(validUntil);
        try {
            record.geometry(geometry(m.geometry(), scope, index, out));
        } catch (IllegalArgumentException e) {
            out.rejections.add(new ItemRejection(index, externalId, "INVALID_ITEM", e.getMessage()));
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int f = 0; f < m.fields().size(); f++) {
            CompiledMapping.FieldMapping field = m.fields().get(f);
            if (payload.containsKey(field.name())) {
                continue;
            }
            Object value = fieldValue(field, scope, index, "mapping.fields[" + f + "]", out);
            if (value != null) {
                payload.put(field.name(), value);
            }
        }
        spread(m, scope, payload);
        m.drop().forEach(payload::remove);
        record.putAll(payload);
        record.quality(quality(m.quality(), scope));
        out.add(index, record.build());
    }

    // ---------- pairs and filters ----------

    private Map<String, JsonNode> pairs(JsonNode item, CompiledMapping m) {
        if (m.pairs() == null) {
            return Map.of();
        }
        JsonNode array = dialect.read(m.pairs().path(), item);
        Map<String, JsonNode> out = new LinkedHashMap<>();
        if (array.isArray()) {
            for (JsonNode pair : array) {
                if (pair == null || !pair.isObject()) {
                    continue;
                }
                String key = text(pair.get(m.pairs().key()));
                JsonNode value = pair.get(m.pairs().value());
                if (key != null && value != null && !value.isNull()) {
                    out.putIfAbsent(key, value);
                }
            }
        }
        return out;
    }

    /** True when {@code filter} holds for the item. */
    static boolean holds(CompiledMapping.Filter filter, MappingScope scope) {
        JsonNode value = filter.value().resolve(scope);
        return switch (filter.op()) {
            case "exists" -> {
                boolean wanted = filter.operand() == null || filter.operand().isNull() || filter.operand().booleanValue();
                yield wanted != ValueSpec.absent(value);
            }
            case "==" -> equal(value, filter.operand());
            case "!=" -> !equal(value, filter.operand());
            case "in" -> {
                for (JsonNode candidate : filter.operand()) {
                    if (equal(value, candidate)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> false;
        };
    }

    private static boolean isTombstone(CompiledMapping.Tombstone tombstone, MappingScope scope) {
        JsonNode value = tombstone.value().resolve(scope);
        if (tombstone.equals() != null) {
            return equal(value, tombstone.equals());
        }
        for (JsonNode candidate : tombstone.in()) {
            if (equal(value, candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mapping equality: absent equals JSON null; numbers compare numerically, also against numeric strings (form and
     * query inputs are strings); anything else compares by text.
     */
    static boolean equal(JsonNode value, JsonNode operand) {
        boolean valueAbsent = ValueSpec.absent(value);
        boolean operandAbsent = operand == null || operand.isNull();
        if (valueAbsent || operandAbsent) {
            return valueAbsent && operandAbsent;
        }
        if (!value.isValueNode() || !operand.isValueNode()) {
            return value.equals(operand);
        }
        Double a = numeric(value);
        Double b = numeric(operand);
        if ((value.isNumber() || operand.isNumber()) && a != null && b != null) {
            return a.doubleValue() == b.doubleValue();
        }
        return value.asString().equals(operand.asString());
    }

    // ---------- time ----------

    /** The instant {@code spec} gives, null when absent. */
    Instant time(TimeSpec spec, MappingScope scope, Instant receivedAt, String name) {
        if (spec == null) {
            return null;
        }
        if (spec instanceof TimeSpec.Received) {
            return receivedAt;
        }
        TimeSpec.FromValue from = (TimeSpec.FromValue) spec;
        JsonNode value = from.value().resolve(scope);
        if (ValueSpec.absent(value)) {
            return null;
        }
        try {
            return switch (from.format()) {
                case "epochS" -> epoch(value, 1000d, name);
                case "epochMs" -> epoch(value, 1d, name);
                case "pattern" -> pattern(text(value), from, name);
                default -> iso(text(value), from, name);
            };
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(name + ": '" + abbreviate(value.asString()) + "' is not a valid time");
        }
    }

    private static Instant epoch(JsonNode value, double toMillis, String name) {
        Double number = numeric(value);
        if (number == null || !Double.isFinite(number)) {
            throw new IllegalArgumentException(name + ": '" + abbreviate(value.asString()) + "' is not an epoch number");
        }
        return Instant.ofEpochMilli((long) (number * toMillis));
    }

    private static Instant pattern(String text, TimeSpec.FromValue from, String name) {
        if (text == null || text.isBlank()) {
            return null;
        }
        TemporalAccessor parsed = from.pattern().parse(text.strip());
        if (parsed.isSupported(java.time.temporal.ChronoField.OFFSET_SECONDS)) {
            return OffsetDateTime.from(parsed).toInstant();
        }
        if (parsed.isSupported(java.time.temporal.ChronoField.HOUR_OF_DAY)) {
            return LocalDateTime.from(parsed).atZone(from.zone()).toInstant();
        }
        return LocalDate.from(parsed).atStartOfDay(from.zone()).toInstant();
    }

    private static Instant iso(String text, TimeSpec.FromValue from, String name) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.strip();
        try {
            return Instant.parse(trimmed);
        } catch (DateTimeException ignored) {
            // Try the offset and zoned forms, then a zone-less local time.
        }
        try {
            return OffsetDateTime.parse(trimmed).toInstant();
        } catch (DateTimeException ignored) {
            // Fall through.
        }
        try {
            return ZonedDateTime.parse(trimmed, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant();
        } catch (DateTimeException ignored) {
            // Fall through.
        }
        return LocalDateTime.parse(trimmed).atZone(from.zone()).toInstant();
    }

    // ---------- geometry ----------

    private Geometry geometry(GeometrySpec spec, MappingScope scope, int index, Out out) {
        if (spec == null) {
            return null;
        }
        return switch (spec) {
            case GeometrySpec.ConstPoint p -> Geo.factory().createPoint(new Coordinate(p.lon(), p.lat()));
            case GeometrySpec.GeoJson g -> {
                JsonNode value = g.value().resolve(scope);
                if (ValueSpec.absent(value)) {
                    yield fallback(g.fallback());
                }
                try {
                    yield geoJson.read(value);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("geometry is not valid GeoJSON: " + e.getMessage());
                }
            }
            case GeometrySpec.LonLat ll -> {
                Double lon = numeric(ll.lon().resolve(scope));
                Double lat = numeric(ll.lat().resolve(scope));
                if (lon == null || lat == null) {
                    yield fallback(ll.fallback());
                }
                if (!(lon >= -180 && lon <= 180) || !(lat >= -90 && lat <= 90)) {
                    out.warn(new MappingWarning(MappingWarning.GEOMETRY_OUT_OF_RANGE, "item " + index + ": lon " + lon
                            + ", lat " + lat + " is out of range; the geometry was left out", "mapping.geometry"));
                    yield fallback(ll.fallback());
                }
                yield Geo.factory().createPoint(new Coordinate(lon, lat));
            }
        };
    }

    private static Geometry fallback(double[] point) {
        return point == null ? null : Geo.factory().createPoint(new Coordinate(point[0], point[1]));
    }

    // ---------- fields ----------

    private Object fieldValue(CompiledMapping.FieldMapping field, MappingScope scope, int index, String where,
            Out out) {
        JsonNode raw = field.value().resolve(scope);
        if (ValueSpec.absent(raw) || isSentinel(raw, field.sentinels())) {
            raw = field.defaultValue();
            if (raw == null) {
                return null;
            }
        }
        if (!raw.isValueNode()) {
            out.warn(new MappingWarning(MappingWarning.FIELD_REJECTED, "item " + index + ": " + field.name()
                    + " is not a scalar", where));
            return null;
        }
        Object value = coerce(raw, field.type());
        if (value == null) {
            out.warn(new MappingWarning(MappingWarning.FIELD_REJECTED, "item " + index + ": " + field.name() + " '"
                    + abbreviate(raw.asString()) + "' is not a valid " + field.type(), where));
            return null;
        }
        if (value instanceof Number number && ("number".equals(field.type()) || "integer".equals(field.type()))) {
            double v = number.doubleValue();
            if (field.conversion() != null) {
                v = field.conversion().applyAsDouble(v);
            }
            if (field.scale() != null) {
                v *= field.scale();
            }
            if (field.offset() != null) {
                v += field.offset();
            }
            if (!Double.isFinite(v)) {
                out.warn(new MappingWarning(MappingWarning.FIELD_REJECTED, "item " + index + ": " + field.name()
                        + " is not finite", where));
                return null;
            }
            if ("integer".equals(field.type())) {
                return Math.round(v);
            }
            return v;
        }
        return value;
    }

    /** {@code raw} as {@code type}: a Double, Long, String or Boolean; null when it cannot be. */
    static Object coerce(JsonNode raw, String type) {
        switch (type) {
            case "number" -> {
                Double number = numeric(raw);
                return number == null || !Double.isFinite(number) ? null : number;
            }
            case "integer" -> {
                Double number = numeric(raw);
                if (number == null || !Double.isFinite(number) || number != Math.rint(number)) {
                    return null;
                }
                return (long) number.doubleValue();
            }
            case "boolean" -> {
                if (raw.isBoolean()) {
                    return raw.booleanValue();
                }
                String text = raw.asString().strip().toLowerCase(Locale.ROOT);
                return switch (text) {
                    case "true", "1", "yes", "on" -> Boolean.TRUE;
                    case "false", "0", "no", "off" -> Boolean.FALSE;
                    default -> null;
                };
            }
            default -> {
                String text = raw.asString();
                return text == null || text.isEmpty() ? null : text;
            }
        }
    }

    private static boolean isSentinel(JsonNode raw, List<JsonNode> sentinels) {
        for (JsonNode sentinel : sentinels) {
            if (equal(raw, sentinel)) {
                return true;
            }
        }
        return false;
    }

    private static void spread(CompiledMapping m, MappingScope scope, Map<String, Object> payload) {
        if (m.spread() == null) {
            return;
        }
        JsonNode object = m.spread().value().resolve(scope);
        if (!object.isObject()) {
            return;
        }
        Set<String> produced = new LinkedHashSet<>(payload.keySet());
        int copied = 0;
        for (Map.Entry<String, JsonNode> member : object.properties()) {
            if (copied >= m.spread().max()) {
                break;
            }
            String name = member.getKey();
            if (produced.contains(name) || m.drop().contains(name) || !MappingCompiler.FIELD_NAME.matcher(name).matches()
                    || !member.getValue().isValueNode() || member.getValue().isNull()) {
                continue;
            }
            Object value = coerce(member.getValue(), m.spread().type());
            if (value != null) {
                payload.put(name, value);
                copied++;
            }
        }
    }

    // ---------- quality ----------

    private static Quality quality(CompiledMapping.QualitySpec spec, MappingScope scope) {
        if (spec == null) {
            return null;
        }
        Double accuracy = spec.accuracyMeters() == null ? null : numeric(spec.accuracyMeters().resolve(scope));
        if (accuracy != null && (!Double.isFinite(accuracy) || accuracy < 0)) {
            accuracy = null;
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        spec.extra().forEach((key, value) -> {
            JsonNode node = value.resolve(scope);
            if (ValueSpec.absent(node) || !node.isValueNode()) {
                return;
            }
            if (node.isBoolean()) {
                extra.put(key, node.booleanValue());
            } else {
                Double number = numeric(node);
                if (number != null && Double.isFinite(number)) {
                    extra.put(key, number);
                } else if (!node.isNumber()) {
                    extra.put(key, node.asString());
                }
            }
        });
        Quality quality = new Quality(accuracy, null, null, extra);
        return quality.isEmpty() ? null : quality;
    }

    // ---------- small readers ----------

    /** A number node, or a string holding a number; null otherwise. */
    static Double numeric(JsonNode value) {
        if (ValueSpec.absent(value)) {
            return null;
        }
        if (value.isNumber()) {
            return value.doubleValue();
        }
        if (value.isString()) {
            String text = value.asString().strip();
            if (text.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(text).doubleValue();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** A string or number id as text; null when absent or blank. */
    static String idText(JsonNode value) {
        if (ValueSpec.absent(value)) {
            return null;
        }
        if (value.isString()) {
            String text = value.asString().strip();
            return text.isEmpty() ? null : text;
        }
        if (value.isIntegralNumber()) {
            return value.bigIntegerValue().toString();
        }
        if (value.isNumber()) {
            return value.asString();
        }
        return null;
    }

    private static String text(JsonNode value) {
        return ValueSpec.absent(value) || !value.isValueNode() ? null : value.asString();
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 64 ? text : text.substring(0, 64) + "…";
    }

    /** The accumulating result. */
    private static final class Out {
        final List<NormalizedRecord> records = new ArrayList<>();
        final List<Integer> indexes = new ArrayList<>();
        final List<ItemRejection> rejections = new ArrayList<>();
        final Map<String, MappingWarning> warningsByKey = new LinkedHashMap<>();
        final List<MappingWarning> warnings = new ArrayList<>();
        int skipped;

        void add(int index, NormalizedRecord record) {
            records.add(record);
            indexes.add(index);
        }

        void warn(MappingWarning warning) {
            String key = warning.code() + "|" + warning.path();
            if (warningsByKey.size() < MAX_WARNINGS && !warningsByKey.containsKey(key)) {
                warningsByKey.put(key, warning);
            }
        }

        List<MappingWarning> warnings() {
            List<MappingWarning> all = new ArrayList<>(warnings);
            all.addAll(warningsByKey.values());
            return all.size() > MAX_WARNINGS ? all.subList(0, MAX_WARNINGS) : all;
        }
    }
}
