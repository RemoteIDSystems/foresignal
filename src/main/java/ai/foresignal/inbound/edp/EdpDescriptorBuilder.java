package ai.foresignal.inbound.edp;

import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceAccess;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Builds an external provider's {@link SourceDescriptor} from its manifest (prompt 024,
 * docs/External-Data-Protocol.md §2).
 *
 * <ul>
 *   <li>{@code ext-{source.id}}, {@code custom=true}, history tier STANDARD, PUSH, or POLLING when the
 *       manifest declares {@code transport.pull} (interval {@code intervalSeconds}, at least 60 s, and
 *       the credential fields of its auth style, entered with {@code PUT /v1/sources/{id}/credentials});
 *   <li>kinds from the streams; atomic-only kinds are hidden from the default tile layer lists;
 *   <li>{@code fieldSpecs} from each {@code payloadSchema}'s top-level properties, the title,
 *       significant and volatile fields from the streams' {@code fields} ({@code edpTimes} is always
 *       volatile, so a repeated item stays a touch), the TTL (the longest declared), the analytic
 *       retention class per kind (docs/Conventions.md table), layer presentation hints;
 *   <li>{@code private}, the license, egress from {@code licence.redistribution}
 *       ({@code allowed} → OPEN, otherwise LOCAL_ONLY), the originator and the access block.
 * </ul>
 *
 * <p>The time base is UPSTREAM; streams declared {@code observedAtMode: received} get
 * {@code observedAt = receivedAt} from the item mapper, so one source never mixes time bases per
 * record.
 */
public final class EdpDescriptorBuilder {

    private static final Pattern FIELD = Pattern.compile("^[A-Za-z0-9_]{1,64}$");
    private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    /** Default raw retention per analytic class (docs/Conventions.md, "Analytic retention"). */
    static final Map<String, Duration> RETENTION = Map.of(
            "event", Duration.ofDays(1825),
            "bulk-event", Duration.ofDays(90),
            "series", Duration.ofDays(3650),
            "state", Duration.ofDays(365),
            "track", Duration.ofDays(3),
            "reference", Duration.ofDays(90),
            "social", Duration.ofDays(30));

    private EdpDescriptorBuilder() {}

    /**
     * The descriptor of a provider.
     *
     * @param manifest the validated manifest
     * @param rateLimitPerMinute the request budget per minute
     * @return the descriptor
     */
    public static SourceDescriptor build(EdpManifestModel manifest, int rateLimitPerMinute) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(manifest.sourceId())
                .name(manifest.name())
                .description(manifest.description())
                .homepage(manifest.homepage())
                .attribution(manifest.attribution() != null ? manifest.attribution() : manifest.publisherName())
                .licence(manifest.licenceId())
                .termsUrl(manifest.termsUrl())
                .custom(true)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .zeroResultsNormal(true)
                .privateSource(manifest.privateSource())
                .egress("allowed".equals(manifest.redistribution()) ? Egress.OPEN : Egress.LOCAL_ONLY)
                .originator(manifest.originatorId())
                .defaultBudget(rateLimitPerMinute, rateLimitPerMinute * 1440);
        if (manifest.accessRoute() != null) {
            b.access(new SourceAccess(manifest.accessRoute(), manifest.termsSummary(), manifest.termsUrl(),
                    manifest.accessFragility()));
        }
        if (manifest.pull() != null) {
            Duration interval = Duration.ofSeconds(Math.max(60, manifest.pull().intervalSeconds()));
            b.mode(Mode.POLLING).interval(Duration.ofSeconds(60), interval);
            credentials(manifest.pull().auth()).forEach(b::credential);
        } else {
            b.mode(Mode.PUSH);
        }

        Set<String> layerKinds = new LinkedHashSet<>();
        Set<String> atomicKinds = new LinkedHashSet<>();
        Set<String> significant = new LinkedHashSet<>();
        Set<String> volatileFields = new LinkedHashSet<>();
        Set<String> tileProperties = new LinkedHashSet<>();
        Map<String, FieldSpec> fieldSpecs = new LinkedHashMap<>();
        String titleField = null;
        Duration ttl = null;
        for (EdpManifestModel.Stream stream : manifest.streams()) {
            b.kind(stream.kind());
            (stream.atomic() ? atomicKinds : layerKinds).add(stream.kind());
            significant.addAll(stream.significant());
            volatileFields.addAll(stream.volatileFields());
            if (titleField == null && stream.titleField() != null && FIELD.matcher(stream.titleField()).matches()) {
                titleField = stream.titleField();
            }
            if (stream.ttl() != null && (ttl == null || stream.ttl().compareTo(ttl) > 0)) {
                ttl = stream.ttl();
            }
            Duration retention = RETENTION.get(stream.retentionClass());
            if (retention != null) {
                b.kindRetention(stream.kind(), retention);
            }
            fieldSpecs(stream, fieldSpecs);
            if (!stream.atomic() && stream.presentation() != null) {
                presentation(b, stream, tileProperties);
            }
        }
        atomicKinds.removeAll(layerKinds);
        atomicKinds.forEach(b::hiddenKind);
        volatileFields.add(EdpItemMapper.PAYLOAD_TIMES);
        volatileFields.removeAll(significant);
        b.volatileFields(volatileFields.toArray(String[]::new));
        b.significantFields(significant.toArray(String[]::new));
        b.tileProperties(tileProperties.toArray(String[]::new));
        b.titleField(titleField);
        b.recordTtl(ttl);
        fieldSpecs.forEach(b::fieldSpec);
        Set<String> search = new LinkedHashSet<>(SourceDescriptor.DEFAULT_SEARCH_FIELDS);
        if (titleField != null) {
            search.add(titleField);
        }
        if (manifest.streams().stream().anyMatch(s -> "document".equals(s.itemType()))) {
            search.add("text");
        }
        b.searchFields(search.toArray(String[]::new));
        return b.build();
    }

    /** The credential fields of a pull auth style; all optional, so approval never needs them first. */
    static java.util.List<CredentialField> credentials(String auth) {
        return switch (auth == null ? "none" : auth) {
            case "bearer" -> java.util.List.of(CredentialField.optional("token",
                    "Bearer token for the provider's items endpoint"));
            case "header" -> java.util.List.of(CredentialField.optional("value",
                    "Value of the provider's auth header"));
            case "basic" -> java.util.List.of(
                    CredentialField.optional("username", "Basic auth user name for the provider"),
                    CredentialField.optional("password", "Basic auth password for the provider"));
            default -> java.util.List.of();
        };
    }

    private static void fieldSpecs(EdpManifestModel.Stream stream, Map<String, FieldSpec> specs) {
        JsonNode properties = stream.payloadSchema() == null ? null : stream.payloadSchema().get("properties");
        if (properties == null || !properties.isObject()) {
            return;
        }
        for (Map.Entry<String, JsonNode> entry : properties.properties()) {
            String key = entry.getKey();
            if (!FIELD.matcher(key).matches() || !entry.getValue().isObject()) {
                continue;
            }
            FieldType type = type(entry.getValue());
            if (type == null) {
                continue;
            }
            FieldSpec existing = specs.get(key);
            Set<String> kinds = new LinkedHashSet<>();
            if (existing != null) {
                if (existing.kinds().isEmpty()) {
                    continue;
                }
                kinds.addAll(existing.kinds());
            }
            kinds.add(stream.kind());
            String unit = "value".equals(key) ? stream.unit() : null;
            specs.put(key, new FieldSpec(existing != null ? existing.type() : type, unit,
                    text(entry.getValue(), "title"), text(entry.getValue(), "description"), null, kinds));
        }
    }

    /** The field type of a property schema; null when it cannot be told. */
    static FieldType type(JsonNode schema) {
        JsonNode type = schema.get("type");
        String name = null;
        if (type != null && type.isString()) {
            name = type.asString();
        } else if (type != null && type.isArray()) {
            for (JsonNode t : type) {
                if (t.isString() && !"null".equals(t.asString())) {
                    name = t.asString();
                    break;
                }
            }
        }
        if (name == null) {
            return null;
        }
        return switch (name) {
            case "string" -> "date-time".equals(text(schema, "format")) ? FieldType.TIMESTAMP : FieldType.STRING;
            case "integer" -> FieldType.INTEGER;
            case "number" -> FieldType.NUMBER;
            case "boolean" -> FieldType.BOOLEAN;
            case "object" -> FieldType.OBJECT;
            case "array" -> FieldType.ARRAY;
            default -> null;
        };
    }

    private static void presentation(SourceDescriptor.Builder b, EdpManifestModel.Stream stream, Set<String> tiles) {
        JsonNode p = stream.presentation();
        Map<String, Object> hints = new LinkedHashMap<>();
        for (String name : java.util.List.of("color", "icon", "labelField")) {
            String value = text(p, name);
            if (value != null) {
                hints.put(name, value);
            }
        }
        for (String name : java.util.List.of("minZoom", "maxZoom")) {
            JsonNode value = p.get(name);
            if (value != null && value.isIntegralNumber()) {
                hints.put(name, value.asInt());
            }
        }
        JsonNode tileProperties = p.get("tileProperties");
        if (tileProperties != null && tileProperties.isArray()) {
            java.util.List<String> names = new java.util.ArrayList<>();
            for (JsonNode name : tileProperties) {
                if (name.isString() && FIELD.matcher(name.asString()).matches()) {
                    names.add(name.asString());
                    tiles.add(name.asString());
                }
            }
            hints.put("tileProperties", names);
        }
        if (!hints.isEmpty()) {
            b.kindPresentation(stream.kind(), hints);
        }
        String color = text(p, "color");
        String icon = text(p, "icon");
        // Prompt 058: kindDisplay.icon must be a sprite icon; a provider's other names stay presentation hints only.
        if (icon != null && !ai.foresignal.map.SpriteCatalog.iconNames().contains(icon)) {
            icon = null;
        }
        if ((color != null && COLOR.matcher(color).matches()) || icon != null) {
            b.kindDisplay(stream.kind(), new KindDisplay(null, icon,
                    color != null && COLOR.matcher(color).matches() ? color : null, null));
        }
    }

    private static String text(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }
}
