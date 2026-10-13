package ai.foresignal.mapping.format;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.derive.DerivedRecords;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.mapping.CompiledMapping;
import ai.foresignal.mapping.MappingCache;
import ai.foresignal.mapping.MappingEngine;
import ai.foresignal.mapping.MappingInputs;
import ai.foresignal.mapping.MappingResult;
import ai.foresignal.mapping.preset.MappingPreset;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The ingest format of one device preset (prompt 059): {@code ecowitt}, {@code wu-pws}, {@code aiscatcher},
 * {@code chirpstack-v4}, {@code tts-v3}, {@code airrohr} or {@code gmc}.
 *
 * <p>Each maps the request with the source's stored {@code RecordMapping}, or the preset's own when the source
 * stores none, and answers the way the device expects: {@code ecowitt} an empty {@code 200}, {@code wu-pws}
 * {@code 200 text/plain success}, {@code gmc} {@code 200 text/plain OK.} (unverified), the others the
 * {@code IngestOutcome}. GET is accepted where the preset says so ({@code wu-pws}, {@code gmc}).
 *
 * <p>What a mapping cannot say is code here, per preset:
 *
 * <ul>
 *   <li><strong>Merging</strong> ({@code aiscatcher}): AIS splits a vessel over position and static messages, so
 *       each record is laid over the entity's current state (Core-Data-Model §3 rule 7: the payload is a complete
 *       snapshot): earlier records of the same request first, else the stored head. A missing geometry keeps the
 *       current one.
 *   <li><strong>ChirpStack events</strong>: {@code ?event=up} (or no event) is mapped; {@code location} moves the
 *       device (current payload kept, {@code quality.extra lora.locationSource} set); every other event is
 *       acknowledged and ignored; the Protobuf encoding is {@code 415}.
 *   <li><strong>TTS static location</strong>: without an {@code frm-payload} location, the device's configured
 *       {@code locations.user} is used and flagged {@code lora.locationSource=static}.
 * </ul>
 */
public class DeviceMappingFormat implements IngestFormat {

    /** ChirpStack's event query parameter. */
    static final String EVENT = "event";

    private final MappingPreset preset;
    private final MappingCache mappings;
    private final MappingEngine engine;
    private final JsonMapper json;
    private final DerivedRecords current;

    /**
     * The format of {@code preset}.
     *
     * @param current the current-state reader, for merging and ChirpStack location events
     */
    public DeviceMappingFormat(MappingPreset preset, MappingCache mappings, MappingEngine engine, JsonMapper json,
            DerivedRecords current) {
        this.preset = preset;
        this.mappings = mappings;
        this.engine = engine;
        this.json = json;
        this.current = current;
    }

    /** The preset this format serves. */
    public MappingPreset preset() {
        return preset;
    }

    @Override
    public String id() {
        return preset.format();
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return switch (preset.format()) {
            case "ecowitt" -> Set.of(MediaType.APPLICATION_FORM_URLENCODED);
            case "wu-pws", "gmc" -> Set.of(MediaType.APPLICATION_FORM_URLENCODED, MediaType.TEXT_PLAIN);
            default -> Set.of(MediaType.APPLICATION_JSON);
        };
    }

    @Override
    public String description() {
        return preset.title() + " (preset " + preset.id() + ")";
    }

    @Override
    public Set<HttpMethod> methods() {
        return preset.allowsGet() ? Set.of(HttpMethod.GET, HttpMethod.POST) : Set.of(HttpMethod.POST);
    }

    @Override
    public boolean pathKeyFriendly() {
        return preset.document().path("allowPathKey").asBoolean(false);
    }

    @Override
    public NativeReply nativeReply(ParsedIngest parsed) {
        return switch (preset.format()) {
            case "ecowitt" -> NativeReply.empty();
            case "wu-pws" -> new NativeReply(200, MediaType.TEXT_PLAIN, "success");
            case "gmc" -> new NativeReply(200, MediaType.TEXT_PLAIN, "OK.");
            default -> null;
        };
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        if ("chirpstack-v4".equals(preset.format())) {
            if (request.hasBody() && request.contentType() != null
                    && !MediaType.APPLICATION_JSON.isCompatibleWith(request.contentType())
                    && !request.contentType().getSubtype().endsWith("+json")) {
                throw new ForesignalException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                        "Set the ChirpStack HTTP integration's payload encoding to JSON; Protobuf is not read.");
            }
            String event = request.queryParam(EVENT);
            if ("location".equals(event)) {
                return chirpstackLocation(MappingInputs.fromRequest(request, json), descriptor, request);
            }
            if (event != null && !"up".equals(event)) {
                return ParsedIngest.builder().items(1).build();
            }
        }
        JsonNode doc = MappingInputs.fromRequest(request, json);
        CompiledMapping mapping = mappings.require(descriptor, preset.mapping());
        MappingResult result = engine.apply(doc, mapping, request.receivedAt());
        List<NormalizedRecord> records = result.records();
        if ("tts-v3".equals(preset.format())) {
            records = ttsStaticLocation(doc, records);
        }
        if (preset.mergeCurrent()) {
            records = merge(descriptor.id(), records);
        }
        return new ParsedIngest(records, result.indexes(), result.rejections(), result.items());
    }

    // ---------- merging (aiscatcher) ----------

    /**
     * {@code records} laid over the current state of their entities: earlier records of the same request, else
     * the stored head. New values win; a missing geometry keeps the current one; a tombstone is never merged.
     */
    List<NormalizedRecord> merge(String sourceId, List<NormalizedRecord> records) {
        Map<String, NormalizedRecord> latest = new HashMap<>();
        List<NormalizedRecord> out = new ArrayList<>(records.size());
        for (NormalizedRecord record : records) {
            if (record.tombstone()) {
                latest.remove(record.externalId());
                out.add(record);
                continue;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            Geometry geometry = record.geometry();
            NormalizedRecord previous = latest.get(record.externalId());
            if (previous != null) {
                payload.putAll(previous.payload());
                geometry = geometry != null ? geometry : previous.geometry();
            } else {
                HeadSnapshot head = current.get(sourceId, record.externalId()).filter(h -> !h.tombstone()).orElse(null);
                if (head != null) {
                    payload.putAll(head.payload());
                    geometry = geometry != null ? geometry : head.geometry();
                }
            }
            payload.putAll(record.payload());
            NormalizedRecord merged = new NormalizedRecord(record.externalId(), record.kind(), record.observedAt(),
                    record.eventTime(), geometry, record.validUntil(), record.validFrom(), payload, false,
                    record.quality(), record.observers());
            latest.put(record.externalId(), merged);
            out.add(merged);
        }
        return out;
    }

    // ---------- ChirpStack location events ----------

    /** A {@code location} event: the device's current payload with the new position. */
    private ParsedIngest chirpstackLocation(JsonNode doc, SourceDescriptor descriptor, IngestRequest request) {
        ParsedIngest.Builder out = ParsedIngest.builder();
        String devEui = text(doc.path("deviceInfo").path("devEui"));
        if (devEui == null) {
            return out.reject(0, null, "EXTERNAL_ID_INVALID", "the location event has no deviceInfo.devEui").build();
        }
        JsonNode location = doc.path("location");
        Double lat = number(location.get("latitude"));
        Double lon = number(location.get("longitude"));
        if (lat == null || lon == null || Math.abs(lat) > 90 || Math.abs(lon) > 180) {
            return out.reject(0, devEui, "INVALID_ITEM", "the location event has no valid latitude/longitude").build();
        }
        Instant observedAt;
        try {
            observedAt = ai.foresignal.inbound.format.IngestTimes.parse(doc.get("time"));
        } catch (RuntimeException e) {
            return out.reject(0, devEui, "INVALID_ITEM", "the location event's time is not ISO-8601").build();
        }
        String externalId = request.externalId(devEui, devEui);
        HeadSnapshot head = current.get(descriptor.id(), externalId).filter(h -> !h.tombstone()).orElse(null);
        String kind = head != null ? head.kind() : descriptor.kinds().size() == 1
                ? descriptor.kinds().iterator().next() : null;
        if (kind == null) {
            return out.reject(0, externalId, "UNKNOWN_KIND", "no current record and the source has several kinds")
                    .build();
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        String source = text(location.get("source"));
        if (source != null) {
            extra.put("lora.locationSource", source.toLowerCase(java.util.Locale.ROOT));
        }
        Double accuracy = number(location.get("accuracy"));
        Quality quality = new Quality(accuracy != null && accuracy >= 0 ? accuracy : null, null, null, extra);
        NormalizedRecord record = NormalizedRecord.builder(externalId, kind)
                .observedAt(observedAt)
                .geometry(Geo.factory().createPoint(new Coordinate(lon, lat)))
                .putAll(head == null ? Map.of() : head.payload())
                .quality(quality.isEmpty() ? null : quality)
                .build();
        return out.add(0, record).build();
    }

    // ---------- TTS static location ----------

    /**
     * Without an {@code frm-payload} location, the device's configured {@code locations.user} position, flagged
     * {@code lora.locationSource=static}.
     */
    List<NormalizedRecord> ttsStaticLocation(JsonNode doc, List<NormalizedRecord> records) {
        JsonNode locations = doc.path("uplink_message").path("locations");
        if (locations.has("frm-payload") || !locations.has("user") || records.isEmpty()) {
            return records;
        }
        JsonNode user = locations.get("user");
        Double lat = number(user.get("latitude"));
        Double lon = number(user.get("longitude"));
        if (lat == null || lon == null || Math.abs(lat) > 90 || Math.abs(lon) > 180) {
            return records;
        }
        List<NormalizedRecord> out = new ArrayList<>(records.size());
        for (NormalizedRecord r : records) {
            if (r.tombstone()) {
                out.add(r);
                continue;
            }
            Quality quality = r.quality().withExtra("lora.locationSource", "static");
            out.add(new NormalizedRecord(r.externalId(), r.kind(), r.observedAt(), r.eventTime(),
                    Geo.factory().createPoint(new Coordinate(lon, lat)), r.validUntil(), r.validFrom(), r.payload(),
                    false, quality, r.observers()));
        }
        return out;
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isNumber()) {
            return Double.isFinite(node.doubleValue()) ? node.doubleValue() : null;
        }
        if (node.isString()) {
            try {
                double value = Double.parseDouble(node.asString().strip());
                return Double.isFinite(value) ? value : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || !node.isValueNode()) {
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() ? null : value;
    }
}
